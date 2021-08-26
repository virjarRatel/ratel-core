// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.staticizer;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.optimize.CodeRewriter;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.ir.optimize.staticizer.ClassStaticizer.CandidateInfo;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.SetUtils;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO(b/140766440): Use PostProcessor, instead of having its own post processing.
final class StaticizingProcessor {

  private final AppView<AppInfoWithLiveness> appView;
  private final ClassStaticizer classStaticizer;
  private final IRConverter converter;

  // Optimization order matters, hence a collection that preserves orderings.
  private final Map<DexEncodedMethod, ImmutableList.Builder<Consumer<IRCode>>> processingQueue =
      new IdentityHashMap<>();

  private final Set<DexEncodedMethod> referencingExtraMethods = Sets.newIdentityHashSet();
  private final Map<DexEncodedMethod, CandidateInfo> hostClassInits = new IdentityHashMap<>();
  private final Set<DexEncodedMethod> methodsToBeStaticized = Sets.newIdentityHashSet();
  private final Map<DexField, CandidateInfo> singletonFields = new IdentityHashMap<>();
  private final Map<DexType, DexType> candidateToHostMapping = new IdentityHashMap<>();

  StaticizingProcessor(
      AppView<AppInfoWithLiveness> appView,
      ClassStaticizer classStaticizer,
      IRConverter converter) {
    this.appView = appView;
    this.classStaticizer = classStaticizer;
    this.converter = converter;
  }

  final void run(OptimizationFeedback feedback, ExecutorService executorService)
      throws ExecutionException {
    // Filter out candidates based on the information we collected while examining methods.
    finalEligibilityCheck();

    // Prepare interim data.
    prepareCandidates();

    // Enqueue all host class initializers (only remove instantiations).
    enqueueMethodsWithCodeOptimizations(
        hostClassInits.keySet(),
        optimizations ->
            optimizations
                .add(this::removeCandidateInstantiation)
                .add(this::insertAssumeInstructions)
                .add(collectOptimizationInfo(feedback)));

    // Enqueue instance methods to be staticized (only remove references to 'this'). Intentionally
    // not collection optimization info for these methods, since they will be reprocessed again
    // below once staticized.
    enqueueMethodsWithCodeOptimizations(
        methodsToBeStaticized, optimizations -> optimizations.add(this::removeReferencesToThis));

    // Process queued methods with associated optimizations
    processMethodsConcurrently(feedback, executorService);

    // TODO(b/140767158): Merge the remaining part below.
    // Convert instance methods into static methods with an extra parameter.
    Set<DexEncodedMethod> methods = staticizeMethodSymbols();

    // Process all other methods that may reference singleton fields and call methods on them.
    // (Note that we exclude the former instance methods, but include new static methods created as
    // a result of staticizing.)
    methods.addAll(referencingExtraMethods);
    methods.addAll(hostClassInits.keySet());
    enqueueMethodsWithCodeOptimizations(
        methods,
        optimizations ->
            optimizations
                .add(this::rewriteReferences)
                .add(this::insertAssumeInstructions)
                .add(collectOptimizationInfo(feedback)));

    // Process queued methods with associated optimizations
    processMethodsConcurrently(feedback, executorService);
  }

  private void finalEligibilityCheck() {
    Set<Phi> visited = Sets.newIdentityHashSet();
    Set<Phi> trivialPhis = Sets.newIdentityHashSet();
    Iterator<Entry<DexType, CandidateInfo>> it =
        classStaticizer.candidates.entrySet().iterator();
    while (it.hasNext()) {
      Entry<DexType, CandidateInfo> entry = it.next();
      DexType candidateType = entry.getKey();
      CandidateInfo info = entry.getValue();
      DexProgramClass candidateClass = info.candidate;
      DexType candidateHostType = info.hostType();
      DexEncodedMethod constructorUsed = info.constructor.get();

      int instancesCreated = info.instancesCreated.get();
      assert instancesCreated == info.fieldWrites.get();
      assert instancesCreated <= 1;
      assert (instancesCreated == 0) == (constructorUsed == null);

      // CHECK: One instance, one singleton field, known constructor
      if (instancesCreated == 0) {
        // Give up on the candidate, if there are any reads from instance
        // field the user should read null.
        it.remove();
        continue;
      }

      // CHECK: instance initializer used to create an instance is trivial.
      // NOTE: Along with requirement that candidate does not have instance
      // fields this should guarantee that the constructor is empty.
      assert candidateClass.instanceFields().size() == 0;
      assert constructorUsed.isProcessed();
      if (constructorUsed.getOptimizationInfo().mayHaveSideEffects()) {
        it.remove();
        continue;
      }

      // CHECK: class initializer should only be present if candidate itself is its own host.
      DexEncodedMethod classInitializer = candidateClass.getClassInitializer();
      assert classInitializer != null || candidateType != candidateHostType;
      if (classInitializer != null && candidateType != candidateHostType) {
        it.remove();
        continue;
      }

      // CHECK: no abstract or native instance methods.
      if (Streams.stream(candidateClass.methods()).anyMatch(
          method -> !method.isStatic() && (method.shouldNotHaveCode()))) {
        it.remove();
        continue;
      }

      // CHECK: references to 'this' in instance methods are fixable.
      boolean fixableThisPointer = true;
      for (DexEncodedMethod method : candidateClass.methods()) {
        if (method.isStatic() || factory().isConstructor(method.method)) {
          continue;
        }
        IRCode code = method.buildIR(appView, appView.appInfo().originFor(method.method.holder));
        assert code != null;
        Value thisValue = code.getThis();
        assert thisValue != null;
        visited.clear();
        trivialPhis.clear();
        boolean onlyHasTrivialPhis = testAndCollectPhisComposedOfThis(
            visited, thisValue.uniquePhiUsers(), thisValue, trivialPhis);
        if (thisValue.hasPhiUsers() && !onlyHasTrivialPhis) {
          fixableThisPointer = false;
          break;
        }
      }
      if (!fixableThisPointer) {
        it.remove();
        continue;
      }

      // CHECK: references to field read usages are fixable.
      boolean fixableFieldReads = true;
      for (DexEncodedMethod method : info.referencedFrom) {
        IRCode code = method.buildIR(appView, appView.appInfo().originFor(method.method.holder));
        assert code != null;
        List<StaticGet> singletonFieldReads =
            Streams.stream(code.instructionIterator())
                .filter(Instruction::isStaticGet)
                .map(Instruction::asStaticGet)
                .filter(get -> get.getField() == info.singletonField.field)
                .collect(Collectors.toList());
        boolean fixableFieldReadsPerUsage = true;
        for (StaticGet read : singletonFieldReads) {
          Value dest = read.dest();
          visited.clear();
          trivialPhis.clear();
          boolean onlyHasTrivialPhis = testAndCollectPhisComposedOfSameFieldRead(
              visited, dest.uniquePhiUsers(), read.getField(), trivialPhis);
          if (dest.hasPhiUsers() && !onlyHasTrivialPhis) {
            fixableFieldReadsPerUsage = false;
            break;
          }
        }
        if (!fixableFieldReadsPerUsage) {
          fixableFieldReads = false;
          break;
        }
      }
      if (!fixableFieldReads) {
        it.remove();
        continue;
      }
    }
  }

  private void prepareCandidates() {
    Set<DexEncodedMethod> removedInstanceMethods = Sets.newIdentityHashSet();

    for (CandidateInfo candidate : classStaticizer.candidates.values()) {
      DexProgramClass candidateClass = candidate.candidate;
      // Host class initializer
      DexClass hostClass = candidate.hostClass();
      DexEncodedMethod hostClassInitializer = hostClass.getClassInitializer();
      assert hostClassInitializer != null;
      CandidateInfo previous = hostClassInits.put(hostClassInitializer, candidate);
      assert previous == null;

      // Collect instance methods to be staticized.
      for (DexEncodedMethod method : candidateClass.methods()) {
        if (!method.isStatic()) {
          removedInstanceMethods.add(method);
          if (!factory().isConstructor(method.method)) {
            methodsToBeStaticized.add(method);
          }
        }
      }
      singletonFields.put(candidate.singletonField.field, candidate);
      referencingExtraMethods.addAll(candidate.referencedFrom);
    }

    referencingExtraMethods.removeAll(removedInstanceMethods);
  }

  private void enqueueMethodsWithCodeOptimizations(
      Iterable<DexEncodedMethod> methods,
      Consumer<ImmutableList.Builder<Consumer<IRCode>>> extension) {
    for (DexEncodedMethod method : methods) {
      extension.accept(processingQueue.computeIfAbsent(method, ignore -> ImmutableList.builder()));
    }
  }

  /**
   * Processes the given methods concurrently using the given strategy.
   *
   * <p>Note that, when the strategy {@link #rewriteReferences(IRCode)} is being applied, it is
   * important that we never inline a method from `methods` which has still not been reprocessed.
   * This could lead to broken code, because the strategy that rewrites the broken references is
   * applied *before* inlining (because the broken references in the inlinee are never rewritten).
   * We currently avoid this situation by processing all the methods concurrently
   * (inlining of a method that is processed concurrently is not allowed).
   */
  private void processMethodsConcurrently(
      OptimizationFeedback feedback, ExecutorService executorService) throws ExecutionException {
    ThreadUtils.processItems(
        processingQueue.keySet(),
        method -> forEachMethod(method, processingQueue.get(method).build(), feedback),
        executorService);
    // TODO(b/140767158): No need to clear if we can do every thing in one go.
    processingQueue.clear();
  }

  // TODO(b/140766440): Should be part or variant of PostProcessor.
  private void forEachMethod(
      DexEncodedMethod method,
      Collection<Consumer<IRCode>> codeOptimizations,
      OptimizationFeedback feedback) {
    Origin origin = appView.appInfo().originFor(method.method.holder);
    IRCode code = method.buildIR(appView, origin);
    codeOptimizations.forEach(codeOptimization -> codeOptimization.accept(code));
    CodeRewriter.removeAssumeInstructions(appView, code);
    converter.finalizeIR(method, code, feedback);
  }

  private void insertAssumeInstructions(IRCode code) {
    CodeRewriter.insertAssumeInstructions(code, converter.assumers);
  }

  private Consumer<IRCode> collectOptimizationInfo(OptimizationFeedback feedback) {
    return code -> converter.collectOptimizationInfo(code, feedback);
  }

  private void removeCandidateInstantiation(IRCode code) {
    CandidateInfo candidateInfo = hostClassInits.get(code.method);
    assert candidateInfo != null;

    // Find and remove instantiation and its users.
    for (Instruction instruction : code.instructions()) {
      if (instruction.isNewInstance()
          && instruction.asNewInstance().clazz == candidateInfo.candidate.type) {
        // Remove all usages
        // NOTE: requiring (a) the instance initializer to be trivial, (b) not allowing
        //       candidates with instance fields and (c) requiring candidate to directly
        //       extend java.lang.Object guarantees that the constructor is actually
        //       empty and does not need to be inlined.
        assert candidateInfo.candidate.superType == factory().objectType;
        assert candidateInfo.candidate.instanceFields().size() == 0;

        Value singletonValue = instruction.outValue();
        assert singletonValue != null;
        singletonValue.uniqueUsers().forEach(user -> user.removeOrReplaceByDebugLocalRead(code));
        instruction.removeOrReplaceByDebugLocalRead(code);
        return;
      }
    }

    assert false : "Must always be able to find and remove the instantiation";
  }

  private void removeReferencesToThis(IRCode code) {
    fixupStaticizedThisUsers(code, code.getThis());
  }

  private void rewriteReferences(IRCode code) {
    // Process all singleton field reads and rewrite their users.
    List<StaticGet> singletonFieldReads =
        Streams.stream(code.instructionIterator())
            .filter(Instruction::isStaticGet)
            .map(Instruction::asStaticGet)
            .filter(get -> singletonFields.containsKey(get.getField()))
            .collect(Collectors.toList());

    singletonFieldReads.forEach(
        read -> {
          DexField field = read.getField();
          CandidateInfo candidateInfo = singletonFields.get(field);
          assert candidateInfo != null;
          Value value = read.dest();
          if (value != null) {
            fixupStaticizedFieldReadUsers(code, value, field);
          }
          if (!candidateInfo.preserveRead.get()) {
            read.removeOrReplaceByDebugLocalRead(code);
          }
        });

    if (!candidateToHostMapping.isEmpty()) {
      remapMovedCandidates(code);
    }
  }

  private boolean testAndCollectPhisComposedOfThis(
      Set<Phi> visited, Set<Phi> phisToCheck, Value thisValue, Set<Phi> trivialPhis) {
    for (Phi phi : phisToCheck) {
      if (!visited.add(phi)) {
        continue;
      }
      Set<Phi> chainedPhis = Sets.newIdentityHashSet();
      for (Value operand : phi.getOperands()) {
        Value v = operand.getAliasedValue();
        if (v.isPhi()) {
          chainedPhis.add(operand.asPhi());
        } else {
          if (v != thisValue) {
            return false;
          }
        }
      }
      if (!chainedPhis.isEmpty()) {
        if (!testAndCollectPhisComposedOfThis(visited, chainedPhis, thisValue, trivialPhis)) {
          return false;
        }
      }
      trivialPhis.add(phi);
    }
    return true;
  }

  // Fixup `this` usages: rewrites all method calls so that they point to static methods.
  private void fixupStaticizedThisUsers(IRCode code, Value thisValue) {
    assert thisValue != null && thisValue == thisValue.getAliasedValue();
    // Depending on other optimizations, e.g., inlining, `this` can be flown to phis.
    Set<Phi> trivialPhis = Sets.newIdentityHashSet();
    boolean onlyHasTrivialPhis = testAndCollectPhisComposedOfThis(
        Sets.newIdentityHashSet(), thisValue.uniquePhiUsers(), thisValue, trivialPhis);
    assert !thisValue.hasPhiUsers() || onlyHasTrivialPhis;
    assert trivialPhis.isEmpty() || onlyHasTrivialPhis;

    Set<Instruction> users = SetUtils.newIdentityHashSet(thisValue.aliasedUsers());
    // If that is the case, method calls we want to fix up include users of those phis.
    for (Phi phi : trivialPhis) {
      users.addAll(phi.aliasedUsers());
    }

    fixupStaticizedValueUsers(code, users);

    // We can't directly use Phi#removeTrivialPhi because they still refer to different operands.
    trivialPhis.forEach(Phi::removeDeadPhi);

    // No matter what, number of phi users should be zero too.
    assert !thisValue.hasUsers() && !thisValue.hasPhiUsers();
  }

  // Re-processing finalized code may create slightly different IR code than what the examining
  // phase has seen. For example,
  //
  //  b1:
  //    s1 <- static-get singleton
  //    ...
  //    invoke-virtual { s1, ... } mtd1
  //    goto Exit
  //  b2:
  //    s2 <- static-get singleton
  //    ...
  //    invoke-virtual { s2, ... } mtd1
  //    goto Exit
  //  ...
  //  Exit: ...
  //
  // ~>
  //
  //  b1:
  //    s1 <- static-get singleton
  //    ...
  //    goto Exit
  //  b2:
  //    s2 <- static-get singleton
  //    ...
  //    goto Exit
  //  Exit:
  //    sp <- phi(s1, s2)
  //    invoke-virtual { sp, ... } mtd1
  //    ...
  //
  // From staticizer's viewpoint, `sp` is trivial in the sense that it is composed of values that
  // refer to the same singleton field. If so, we can safely relax the assertion; remove uses of
  // field reads; remove quasi-trivial phis; and then remove original field reads.
  private boolean testAndCollectPhisComposedOfSameFieldRead(
      Set<Phi> visited, Set<Phi> phisToCheck, DexField field, Set<Phi> trivialPhis) {
    for (Phi phi : phisToCheck) {
      if (!visited.add(phi)) {
        continue;
      }
      Set<Phi> chainedPhis = Sets.newIdentityHashSet();
      for (Value operand : phi.getOperands()) {
        Value v = operand.getAliasedValue();
        if (v.isPhi()) {
          chainedPhis.add(operand.asPhi());
        } else {
          if (!v.definition.isStaticGet()) {
            return false;
          }
          if (v.definition.asStaticGet().getField() != field) {
            return false;
          }
        }
      }
      if (!chainedPhis.isEmpty()) {
        if (!testAndCollectPhisComposedOfSameFieldRead(visited, chainedPhis, field, trivialPhis)) {
          return false;
        }
      }
      trivialPhis.add(phi);
    }
    return true;
  }

  // Fixup field read usages. Same as {@link #fixupStaticizedThisUsers} except this one determines
  // quasi-trivial phis, based on the original field.
  private void fixupStaticizedFieldReadUsers(IRCode code, Value dest, DexField field) {
    assert dest != null;
    // During the examine phase, field reads with any phi users have been invalidated, hence zero.
    // However, it may be not true if re-processing introduces phis after optimizing common suffix.
    Set<Phi> trivialPhis = Sets.newIdentityHashSet();
    boolean onlyHasTrivialPhis = testAndCollectPhisComposedOfSameFieldRead(
        Sets.newIdentityHashSet(), dest.uniquePhiUsers(), field, trivialPhis);
    assert !dest.hasPhiUsers() || onlyHasTrivialPhis;
    assert trivialPhis.isEmpty() || onlyHasTrivialPhis;

    Set<Instruction> users = SetUtils.newIdentityHashSet(dest.aliasedUsers());
    // If that is the case, method calls we want to fix up include users of those phis.
    for (Phi phi : trivialPhis) {
      users.addAll(phi.aliasedUsers());
    }

    fixupStaticizedValueUsers(code, users);

    // We can't directly use Phi#removeTrivialPhi because they still refer to different operands.
    trivialPhis.forEach(Phi::removeDeadPhi);

    // No matter what, number of phi users should be zero too.
    assert !dest.hasUsers() && !dest.hasPhiUsers();
  }

  private void fixupStaticizedValueUsers(IRCode code, Set<Instruction> users) {
    for (Instruction user : users) {
      if (user.isAssume()) {
        continue;
      }
      assert user.isInvokeVirtual() || user.isInvokeDirect();
      InvokeMethodWithReceiver invoke = user.asInvokeMethodWithReceiver();
      Value newValue = null;
      Value outValue = invoke.outValue();
      if (outValue != null) {
        newValue = code.createValue(outValue.getTypeLattice());
        DebugLocalInfo localInfo = outValue.getLocalInfo();
        if (localInfo != null) {
          newValue.setLocalInfo(localInfo);
        }
      }
      List<Value> args = invoke.inValues();
      invoke.replace(
          new InvokeStatic(invoke.getInvokedMethod(), newValue, args.subList(1, args.size())),
          code);
    }
  }

  private void remapMovedCandidates(IRCode code) {
    InstructionListIterator it = code.instructionListIterator();
    while (it.hasNext()) {
      Instruction instruction = it.next();

      if (instruction.isStaticGet()) {
        StaticGet staticGet = instruction.asStaticGet();
        DexField field = mapFieldIfMoved(staticGet.getField());
        if (field != staticGet.getField()) {
          Value outValue = staticGet.dest();
          assert outValue != null;
          it.replaceCurrentInstruction(
              new StaticGet(
                  code.createValue(
                      TypeLatticeElement.fromDexType(
                          field.type, outValue.getTypeLattice().nullability(), appView),
                      outValue.getLocalInfo()),
                  field));
        }
        continue;
      }

      if (instruction.isStaticPut()) {
        StaticPut staticPut = instruction.asStaticPut();
        DexField field = mapFieldIfMoved(staticPut.getField());
        if (field != staticPut.getField()) {
          it.replaceCurrentInstruction(new StaticPut(staticPut.value(), field));
        }
        continue;
      }

      if (instruction.isInvokeStatic()) {
        InvokeStatic invoke = instruction.asInvokeStatic();
        DexMethod method = invoke.getInvokedMethod();
        DexType hostType = candidateToHostMapping.get(method.holder);
        if (hostType != null) {
          DexMethod newMethod = factory().createMethod(hostType, method.proto, method.name);
          Value outValue = invoke.outValue();
          DexType returnType = method.proto.returnType;
          Value newOutValue =
              returnType.isVoidType() || outValue == null
                  ? null
                  : code.createValue(
                      TypeLatticeElement.fromDexType(
                          returnType, outValue.getTypeLattice().nullability(), appView),
                      outValue.getLocalInfo());
          it.replaceCurrentInstruction(new InvokeStatic(newMethod, newOutValue, invoke.inValues()));
        }
        continue;
      }
    }
  }

  private DexField mapFieldIfMoved(DexField field) {
    DexType hostType = candidateToHostMapping.get(field.holder);
    if (hostType != null) {
      field = factory().createField(hostType, field.type, field.name);
    }
    hostType = candidateToHostMapping.get(field.type);
    if (hostType != null) {
      field = factory().createField(field.holder, hostType, field.name);
    }
    return field;
  }

  private Set<DexEncodedMethod> staticizeMethodSymbols() {
    BiMap<DexMethod, DexMethod> methodMapping = HashBiMap.create();
    BiMap<DexField, DexField> fieldMapping = HashBiMap.create();

    Set<DexEncodedMethod> staticizedMethods = Sets.newIdentityHashSet();
    for (CandidateInfo candidate : classStaticizer.candidates.values()) {
      DexProgramClass candidateClass = candidate.candidate;

      // Move instance methods into static ones.
      List<DexEncodedMethod> newDirectMethods = new ArrayList<>();
      for (DexEncodedMethod method : candidateClass.methods()) {
        if (method.isStatic()) {
          newDirectMethods.add(method);
        } else if (!factory().isConstructor(method.method)) {
          DexEncodedMethod staticizedMethod = method.toStaticMethodWithoutThis();
          newDirectMethods.add(staticizedMethod);
          staticizedMethods.add(staticizedMethod);
          methodMapping.put(method.method, staticizedMethod.method);
        }
      }
      candidateClass.setVirtualMethods(DexEncodedMethod.EMPTY_ARRAY);
      candidateClass.setDirectMethods(newDirectMethods.toArray(DexEncodedMethod.EMPTY_ARRAY));

      // Consider moving static members from candidate into host.
      DexType hostType = candidate.hostType();
      if (candidateClass.type != hostType) {
        DexClass hostClass = appView.definitionFor(hostType);
        assert hostClass != null;
        if (!classMembersConflict(candidateClass, hostClass)) {
          // Move all members of the candidate class into host class.
          moveMembersIntoHost(staticizedMethods,
              candidateClass, hostType, hostClass, methodMapping, fieldMapping);
        }
      }
    }

    if (!methodMapping.isEmpty() || !fieldMapping.isEmpty()) {
      appView.setGraphLense(new ClassStaticizerGraphLense(appView, fieldMapping, methodMapping));
    }
    return staticizedMethods;
  }

  private boolean classMembersConflict(DexClass a, DexClass b) {
    assert Streams.stream(a.methods()).allMatch(DexEncodedMethod::isStatic);
    assert a.instanceFields().size() == 0;
    return a.staticFields().stream().anyMatch(fld -> b.lookupField(fld.field) != null)
        || Streams.stream(a.methods()).anyMatch(method -> b.lookupMethod(method.method) != null);
  }

  private void moveMembersIntoHost(
      Set<DexEncodedMethod> staticizedMethods,
      DexProgramClass candidateClass,
      DexType hostType, DexClass hostClass,
      BiMap<DexMethod, DexMethod> methodMapping,
      BiMap<DexField, DexField> fieldMapping) {
    candidateToHostMapping.put(candidateClass.type, hostType);

    // Process static fields.
    int numOfHostStaticFields = hostClass.staticFields().size();
    DexEncodedField[] newFields =
        candidateClass.staticFields().size() > 0
            ? new DexEncodedField[numOfHostStaticFields + candidateClass.staticFields().size()]
            : new DexEncodedField[numOfHostStaticFields];
    List<DexEncodedField> oldFields = hostClass.staticFields();
    for (int i = 0; i < oldFields.size(); i++) {
      DexEncodedField field = oldFields.get(i);
      DexField newField = mapCandidateField(field.field, candidateClass.type, hostType);
      if (newField != field.field) {
        newFields[i] = field.toTypeSubstitutedField(newField);
        fieldMapping.put(field.field, newField);
      } else {
        newFields[i] = field;
      }
    }
    if (candidateClass.staticFields().size() > 0) {
      List<DexEncodedField> extraFields = candidateClass.staticFields();
      for (int i = 0; i < extraFields.size(); i++) {
        DexEncodedField field = extraFields.get(i);
        DexField newField = mapCandidateField(field.field, candidateClass.type, hostType);
        if (newField != field.field) {
          newFields[numOfHostStaticFields + i] = field.toTypeSubstitutedField(newField);
          fieldMapping.put(field.field, newField);
        } else {
          newFields[numOfHostStaticFields + i] = field;
        }
      }
    }
    hostClass.setStaticFields(newFields);

    // Process static methods.
    List<DexEncodedMethod> extraMethods = candidateClass.directMethods();
    if (!extraMethods.isEmpty()) {
      List<DexEncodedMethod> newMethods = new ArrayList<>(extraMethods.size());
      for (DexEncodedMethod method : extraMethods) {
        DexEncodedMethod newMethod = method.toTypeSubstitutedMethod(
            factory().createMethod(hostType, method.method.proto, method.method.name));
        newMethods.add(newMethod);
        // If the old method from the candidate class has been staticized,
        if (staticizedMethods.remove(method)) {
          // Properly update staticized methods to reprocess, i.e., add the corresponding one that
          // has just been migrated to the host class.
          staticizedMethods.add(newMethod);
        }
        DexMethod originalMethod = methodMapping.inverse().get(method.method);
        if (originalMethod == null) {
          methodMapping.put(method.method, newMethod.method);
        } else {
          methodMapping.put(originalMethod, newMethod.method);
        }
      }
      hostClass.appendDirectMethods(newMethods);
    }
  }

  private DexField mapCandidateField(DexField field, DexType candidateType, DexType hostType) {
    return field.holder != candidateType && field.type != candidateType ? field
        : factory().createField(
            field.holder == candidateType ? hostType : field.holder,
            field.type == candidateType ? hostType : field.type,
            field.name);
  }

  private DexItemFactory factory() {
    return appView.dexItemFactory();
  }
}
