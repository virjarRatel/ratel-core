// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.staticizer;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.ListUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class ClassStaticizer {

  private final AppView<AppInfoWithLiveness> appView;
  private final DexItemFactory factory;
  private final IRConverter converter;

  // Represents a staticizing candidate with all information
  // needed for staticizing.
  final class CandidateInfo {
    final DexProgramClass candidate;
    final DexEncodedField singletonField;
    final AtomicBoolean preserveRead = new AtomicBoolean(false);
    // Number of singleton field writes.
    final AtomicInteger fieldWrites = new AtomicInteger();
    // Number of instances created.
    final AtomicInteger instancesCreated = new AtomicInteger();
    final Set<DexEncodedMethod> referencedFrom = Sets.newConcurrentHashSet();
    final AtomicReference<DexEncodedMethod> constructor = new AtomicReference<>();
    final AtomicReference<DexEncodedMethod> getter = new AtomicReference<>();

    CandidateInfo(DexProgramClass candidate, DexEncodedField singletonField) {
      assert candidate != null;
      assert singletonField != null;
      this.candidate = candidate;
      this.singletonField = singletonField;

      // register itself
      candidates.put(candidate.type, this);
    }

    boolean isHostClassInitializer(DexEncodedMethod method) {
      return factory.isClassConstructor(method.method) && method.method.holder == hostType();
    }

    DexType hostType() {
      return singletonField.field.holder;
    }

    DexClass hostClass() {
      DexClass hostClass = appView.definitionFor(hostType());
      assert hostClass != null;
      return hostClass;
    }

    CandidateInfo invalidate() {
      candidates.remove(candidate.type);
      return null;
    }
  }

  // The map storing all the potential candidates for staticizing.
  final ConcurrentHashMap<DexType, CandidateInfo> candidates = new ConcurrentHashMap<>();

  public ClassStaticizer(AppView<AppInfoWithLiveness> appView, IRConverter converter) {
    this.appView = appView;
    this.factory = appView.dexItemFactory();
    this.converter = converter;
  }

  // Before doing any usage-based analysis we collect a set of classes that can be
  // candidates for staticizing. This analysis is very simple, but minimizes the
  // set of eligible classes staticizer tracks and thus time and memory it needs.
  public final void collectCandidates(DexApplication app) {
    Set<DexType> notEligible = Sets.newIdentityHashSet();
    Map<DexType, DexEncodedField> singletonFields = new HashMap<>();

    app.classes()
        .forEach(
            cls -> {
              // We only consider classes eligible for staticizing if there is just
              // one single static field in the whole app which has a type of this
              // class. This field will be considered to be a candidate for a singleton
              // field. The requirements for the initialization of this field will be
              // checked later.
              for (DexEncodedField field : cls.staticFields()) {
                DexType type = field.field.type;
                if (singletonFields.put(type, field) != null) {
                  // There is already candidate singleton field found.
                  notEligible.add(type);
                }
              }

              // Don't allow fields with this candidate types.
              for (DexEncodedField field : cls.instanceFields()) {
                notEligible.add(field.field.type);
              }

              // Don't allow methods that take a value of this type.
              for (DexEncodedMethod method : cls.methods()) {
                DexProto proto = method.method.proto;
                notEligible.addAll(Arrays.asList(proto.parameters.values));
              }

              // High-level limitations on what classes we consider eligible.
              if (cls.isInterface() // Must not be an interface or an abstract class.
                  || cls.accessFlags.isAbstract()
                  // Don't support candidates with instance fields
                  || cls.instanceFields().size() > 0
                  // Only support classes directly extending java.lang.Object
                  || cls.superType != factory.objectType
                  // Instead of requiring the class being final,
                  // just ensure it does not have subtypes
                  || appView.appInfo().hasSubtypes(cls.type)
                  // Staticizing classes implementing interfaces is more
                  // difficult, so don't support it until we really need it.
                  || !cls.interfaces.isEmpty()) {
                notEligible.add(cls.type);
              }
            });

    // Finalize the set of the candidates.
    app.classes().forEach(cls -> {
      DexType type = cls.type;
      if (!notEligible.contains(type)) {
        DexEncodedField field = singletonFields.get(type);
        if (field != null && // Singleton field found
            !field.accessFlags.isVolatile() && // Don't remove volatile fields.
            !isPinned(cls, field)) { // Don't remove pinned objects.
          assert field.accessFlags.isStatic();
          // Note: we don't check that the field is final, since we will analyze
          //       later how and where it is initialized.
          new CandidateInfo(cls, field); // will self-register
        }
      }
    });
  }

  private boolean isPinned(DexClass clazz, DexEncodedField singletonField) {
    AppInfoWithLiveness appInfo = appView.appInfo();
    if (appInfo.isPinned(clazz.type) || appInfo.isPinned(singletonField.field)) {
      return true;
    }
    for (DexEncodedMethod method : clazz.methods()) {
      if (!method.isStatic() && appInfo.isPinned(method.method)) {
        return true;
      }
    }
    return false;
  }

  // Check staticizing candidates' usages to ensure the candidate can be staticized.
  //
  // The criteria for type CANDIDATE to be eligible for staticizing fall into
  // these categories:
  //
  //  * checking that there is only one instance of the class created, and it is created
  //    inside the host class initializer, and it is guaranteed that nobody can access this
  //    field before it is assigned.
  //
  //  * no other singleton field writes (except for those used to store the only candidate
  //    class instance described above) are allowed.
  //
  //  * values read from singleton field should only be used for instance method calls.
  //
  // NOTE: there are more criteria eligible class needs to satisfy to be actually staticized,
  // those will be checked later in staticizeCandidates().
  //
  // This method also collects all DexEncodedMethod instances that need to be rewritten if
  // appropriate candidate is staticized. Essentially anything that references instance method
  // or field defined in the class.
  //
  // NOTE: can be called concurrently.
  public final void examineMethodCode(DexEncodedMethod method, IRCode code) {
    Set<Instruction> alreadyProcessed = Sets.newIdentityHashSet();

    CandidateInfo receiverClassCandidateInfo = candidates.get(method.method.holder);
    Value receiverValue = code.getThis(); // NOTE: is null for static methods.
    if (receiverClassCandidateInfo != null) {
      if (receiverValue != null) {
        // We are inside an instance method of candidate class (not an instance initializer
        // which we will check later), check if all the references to 'this' are valid
        // (the call will invalidate the candidate if some of them are not valid).
        analyzeAllValueUsers(
            receiverClassCandidateInfo, receiverValue, factory.isConstructor(method.method));

        // If the candidate is still valid, ignore all instructions
        // we treat as valid usages on receiver.
        if (candidates.get(method.method.holder) != null) {
          alreadyProcessed.addAll(receiverValue.uniqueUsers());
        }
      } else {
        // We are inside a static method of candidate class.
        // Check if this is a valid getter of the singleton field.
        if (method.method.proto.returnType == method.method.holder) {
          List<Instruction> examined = isValidGetter(receiverClassCandidateInfo, code);
          if (examined != null) {
            DexEncodedMethod getter = receiverClassCandidateInfo.getter.get();
            if (getter == null) {
              receiverClassCandidateInfo.getter.set(method);
              // Except for static-get and return, iterate other remaining instructions if any.
              alreadyProcessed.addAll(examined);
            } else {
              assert getter != method;
              // Not sure how to deal with many getters.
              receiverClassCandidateInfo.invalidate();
            }
          } else {
            // Invalidate the candidate if it has a static method whose return type is a candidate
            // type but doesn't return the singleton field (in a trivial way).
            receiverClassCandidateInfo.invalidate();
          }
        }
      }
    }

    // TODO(b/143375203): if fully implemented, the following iterator could be:
    //   InstructionListIterator iterator = code.instructionListIterator();
    ListIterator<Instruction> iterator =
        Lists.newArrayList(code.instructionIterator()).listIterator();
    while (iterator.hasNext()) {
      Instruction instruction = iterator.next();
      if (alreadyProcessed.contains(instruction)) {
        continue;
      }

      if (instruction.isNewInstance()) {
        // Check the class being initialized against valid staticizing candidates.
        NewInstance newInstance = instruction.asNewInstance();
        CandidateInfo candidateInfo = processInstantiation(method, iterator, newInstance);
        if (candidateInfo != null) {
          // For host class initializers having eligible instantiation we also want to
          // ensure that the rest of the initializer consist of code w/o side effects.
          // This must guarantee that removing field access will not result in missing side
          // effects, otherwise we can still staticize, but cannot remove singleton reads.
          while (iterator.hasNext()) {
            if (!isAllowedInHostClassInitializer(method.method.holder, iterator.next(), code)) {
              candidateInfo.preserveRead.set(true);
              iterator.previous();
              break;
            }
            // Ignore just read instruction.
          }
          candidateInfo.referencedFrom.add(method);
        }
        continue;
      }

      if (instruction.isStaticPut()) {
        // Check the field being written to: no writes to singleton fields are allowed
        // except for those processed in processInstantiation(...).
        DexType candidateType = instruction.asStaticPut().getField().type;
        CandidateInfo candidateInfo = candidates.get(candidateType);
        if (candidateInfo != null) {
          candidateInfo.invalidate();
        }
        continue;
      }

      if (instruction.isStaticGet()) {
        // Check the field being read: make sure all usages are valid.
        CandidateInfo info = processStaticFieldRead(instruction.asStaticGet());
        if (info != null) {
          info.referencedFrom.add(method);
          // If the candidate is still valid, ignore all usages in further analysis.
          Value value = instruction.outValue();
          if (value != null) {
            alreadyProcessed.addAll(value.aliasedUsers());
          }
        }
        continue;
      }

      if (instruction.isInvokeStatic()) {
        // Check if it is a static singleton getter.
        CandidateInfo info = processInvokeStatic(instruction.asInvokeStatic());
        if (info != null) {
          info.referencedFrom.add(method);
          // If the candidate is still valid, ignore all usages in further analysis.
          Value value = instruction.outValue();
          if (value != null) {
            alreadyProcessed.addAll(value.aliasedUsers());
          }
        }
        continue;
      }

      if (instruction.isInvokeMethodWithReceiver()) {
        DexMethod invokedMethod = instruction.asInvokeMethodWithReceiver().getInvokedMethod();
        CandidateInfo candidateInfo = candidates.get(invokedMethod.holder);
        if (candidateInfo != null) {
          // A call to instance method of the candidate class we don't know how to deal with.
          candidateInfo.invalidate();
        }
        continue;
      }

      if (instruction.isInvokeCustom()) {
        // Just invalidate any candidates referenced from non-static context.
        CallSiteReferencesInvalidator invalidator = new CallSiteReferencesInvalidator(factory);
        invalidator.registerCallSite(instruction.asInvokeCustom().getCallSite());
        continue;
      }

      if (instruction.isInstanceGet() || instruction.isInstancePut()) {
        DexField fieldReferenced = instruction.asFieldInstruction().getField();
        CandidateInfo candidateInfo = candidates.get(fieldReferenced.holder);
        if (candidateInfo != null) {
          // Reads/writes to instance field of the candidate class are not supported.
          candidateInfo.invalidate();
        }
        continue;
      }
    }
  }

  private boolean isAllowedInHostClassInitializer(
      DexType host, Instruction insn, IRCode code) {
    return (insn.isStaticPut() && insn.asStaticPut().getField().holder == host) ||
        insn.isConstNumber() ||
        insn.isConstString() ||
        (insn.isGoto() && insn.asGoto().isTrivialGotoToTheNextBlock(code)) ||
        insn.isReturn();
  }

  private CandidateInfo processInstantiation(
      DexEncodedMethod method, ListIterator<Instruction> iterator, NewInstance newInstance) {

    DexType candidateType = newInstance.clazz;
    CandidateInfo candidateInfo = candidates.get(candidateType);
    if (candidateInfo == null) {
      return null; // Not interested.
    }

    if (iterator.previousIndex() != 0) {
      // Valid new instance must be the first instruction in the class initializer
      return candidateInfo.invalidate();
    }

    if (!candidateInfo.isHostClassInitializer(method)) {
      // A valid candidate must only have one instantiation which is
      // done in the static initializer of the host class.
      return candidateInfo.invalidate();
    }

    if (candidateInfo.instancesCreated.incrementAndGet() > 1) {
      // Only one instance must be ever created.
      return candidateInfo.invalidate();
    }

    Value candidateValue = newInstance.dest();
    if (candidateValue == null) {
      // Must be assigned to a singleton field.
      return candidateInfo.invalidate();
    }

    if (candidateValue.numberOfPhiUsers() > 0) {
      return candidateInfo.invalidate();
    }

    if (candidateValue.numberOfUsers() != 2) {
      // We expect only two users for each instantiation: constructor call and
      // static field write. We only check count here, since the exact instructions
      // will be checked later.
      return candidateInfo.invalidate();
    }

    // Check usages. Currently we only support the patterns like:
    //
    //     static constructor void <clinit>() {
    //        new-instance v0, <candidate-type>
    //  (opt) const/4 v1, #int 0 // (optional)
    //        invoke-direct {v0, ...}, void <candidate-type>.<init>(...)
    //        sput-object v0, <instance-field>
    //        ...
    //        ...
    //
    // In case we guarantee candidate constructor does not access <instance-field>
    // directly or indirectly we can guarantee that all the potential reads get
    // same non-null value.

    // Skip potential constant instructions
    while (iterator.hasNext() && isNonThrowingConstInstruction(iterator.next())) {
      // Intentionally empty.
    }
    iterator.previous();

    if (!iterator.hasNext()) {
      return candidateInfo.invalidate();
    }
    if (!isValidInitCall(candidateInfo, iterator.next(), candidateValue, candidateType)) {
      iterator.previous();
      return candidateInfo.invalidate();
    }

    if (!iterator.hasNext()) {
      return candidateInfo.invalidate();
    }
    if (!isValidStaticPut(candidateInfo, iterator.next())) {
      iterator.previous();
      return candidateInfo.invalidate();
    }
    if (candidateInfo.fieldWrites.incrementAndGet() > 1) {
      return candidateInfo.invalidate();
    }

    return candidateInfo;
  }

  private boolean isNonThrowingConstInstruction(Instruction instruction) {
    return instruction.isConstInstruction() && !instruction.instructionTypeCanThrow();
  }

  private boolean isValidInitCall(
      CandidateInfo info, Instruction instruction, Value candidateValue, DexType candidateType) {
    if (!instruction.isInvokeDirect()) {
      return false;
    }

    // Check constructor.
    InvokeDirect invoke = instruction.asInvokeDirect();
    DexEncodedMethod methodInvoked =
        appView.appInfo().lookupDirectTarget(invoke.getInvokedMethod());
    List<Value> values = invoke.inValues();

    if (ListUtils.lastIndexMatching(values, v -> v.getAliasedValue() == candidateValue) != 0
        || methodInvoked == null
        || methodInvoked.method.holder != candidateType) {
      return false;
    }

    // Check arguments.
    for (int i = 1; i < values.size(); i++) {
      Value arg = values.get(i).getAliasedValue();
      if (arg.isPhi() || !arg.definition.isConstInstruction()) {
        return false;
      }
    }

    DexEncodedMethod previous = info.constructor.getAndSet(methodInvoked);
    assert previous == null;
    return true;
  }

  private boolean isValidStaticPut(CandidateInfo info, Instruction instruction) {
    if (!instruction.isStaticPut()) {
      return false;
    }
    // Allow single assignment to a singleton field.
    StaticPut staticPut = instruction.asStaticPut();
    DexEncodedField fieldAccessed =
        appView.appInfo().lookupStaticTarget(staticPut.getField().holder, staticPut.getField());
    return fieldAccessed == info.singletonField;
  }

  // Only allow a very trivial pattern: load the singleton field and return it, which looks like:
  //
  //   v <- static-get singleton-field
  //   <assume instructions on v> // (optional)
  //   return v // or aliased value
  //
  // Returns a list of instructions that are examined (as long as the method is a trivial getter).
  private List<Instruction> isValidGetter(CandidateInfo info, IRCode code) {
    List<Instruction> instructions = new ArrayList<>();
    StaticGet staticGet = null;
    for (Instruction instr : code.instructions()) {
      if (instr.isStaticGet()) {
        staticGet = instr.asStaticGet();
        DexEncodedField fieldAccessed =
            appView.appInfo().lookupStaticTarget(staticGet.getField().holder, staticGet.getField());
        if (fieldAccessed != info.singletonField) {
          return null;
        }
        instructions.add(instr);
        continue;
      }
      if (instr.isAssume() || instr.isReturn()) {
        Value v = instr.inValues().get(0).getAliasedValue();
        if (v.isPhi() || v.definition != staticGet) {
          return null;
        }
        instructions.add(instr);
        continue;
      }
      // All other instructions are not allowed.
      return null;
    }
    return instructions;
  }

  // Static field get: can be a valid singleton field for a
  // candidate in which case we should check if all the usages of the
  // value read are eligible.
  private CandidateInfo processStaticFieldRead(StaticGet staticGet) {
    DexField field = staticGet.getField();
    DexType candidateType = field.type;
    CandidateInfo candidateInfo = candidates.get(candidateType);
    if (candidateInfo == null) {
      return null;
    }

    assert candidateInfo.singletonField == appView.appInfo().lookupStaticTarget(field.holder, field)
        : "Added reference after collectCandidates(...)?";

    Value singletonValue = staticGet.dest();
    if (singletonValue != null) {
      candidateInfo = analyzeAllValueUsers(candidateInfo, singletonValue, false);
    }
    return candidateInfo;
  }

  // Static getter: if this invokes a registered getter, treat it as static field get.
  // That is, we should check if all the usages of the out value are eligible.
  private CandidateInfo processInvokeStatic(InvokeStatic invoke) {
    DexType candidateType = invoke.getInvokedMethod().proto.returnType;
    CandidateInfo candidateInfo = candidates.get(candidateType);
    if (candidateInfo == null) {
      return null;
    }

    if (invoke.hasOutValue()
        && candidateInfo.getter.get() != null
        && candidateInfo.getter.get().method == invoke.getInvokedMethod()) {
      candidateInfo = analyzeAllValueUsers(candidateInfo, invoke.outValue(), false);
    }
    return candidateInfo;
  }

  private CandidateInfo analyzeAllValueUsers(
      CandidateInfo candidateInfo, Value value, boolean ignoreSuperClassInitInvoke) {
    assert value != null && value == value.getAliasedValue();

    if (value.numberOfPhiUsers() > 0) {
      return candidateInfo.invalidate();
    }

    Set<Instruction> currentUsers = value.uniqueUsers();
    while (!currentUsers.isEmpty()) {
      Set<Instruction> indirectUsers = Sets.newIdentityHashSet();
      for (Instruction user : currentUsers) {
        if (user.isAssume()) {
          if (user.outValue().numberOfPhiUsers() > 0) {
            return candidateInfo.invalidate();
          }
          indirectUsers.addAll(user.outValue().uniqueUsers());
          continue;
        }
        if (user.isInvokeVirtual() || user.isInvokeDirect() /* private methods */) {
          InvokeMethodWithReceiver invoke = user.asInvokeMethodWithReceiver();
          Predicate<Value> isAliasedValue = v -> v.getAliasedValue() == value;
          DexMethod methodReferenced = invoke.getInvokedMethod();
          if (factory.isConstructor(methodReferenced)) {
            assert user.isInvokeDirect();
            if (ignoreSuperClassInitInvoke
                && ListUtils.lastIndexMatching(invoke.inValues(), isAliasedValue) == 0
                && methodReferenced == factory.objectMethods.constructor) {
              // If we are inside candidate constructor and analyzing usages
              // of the receiver, we want to ignore invocations of superclass
              // constructor which will be removed after staticizing.
              continue;
            }
            return candidateInfo.invalidate();
          }
          AppInfo appInfo = appView.appInfo();
          DexEncodedMethod methodInvoked = user.isInvokeDirect()
              ? appInfo.lookupDirectTarget(methodReferenced)
              : appInfo.lookupVirtualTarget(methodReferenced.holder, methodReferenced);
          if (ListUtils.lastIndexMatching(invoke.inValues(), isAliasedValue) == 0
              && methodInvoked != null
              && methodInvoked.method.holder == candidateInfo.candidate.type) {
            continue;
          }
        }

        // All other users are not allowed.
        return candidateInfo.invalidate();
      }
      currentUsers = indirectUsers;
    }

    return candidateInfo;
  }

  // Perform staticizing candidates:
  //
  //  1. After filtering candidates based on usage, finalize the list of candidates by
  //  filtering out candidates which don't satisfy the requirements:
  //
  //    * there must be one instance of the class
  //    * constructor of the class used to create this instance must be a trivial one
  //    * class initializer should only be present if candidate itself is own host
  //    * no abstract or native instance methods
  //
  //  2. Rewrite instance methods of classes being staticized into static ones
  //  3. Rewrite methods referencing staticized members, also remove instance creation
  //
  public final void staticizeCandidates(
      OptimizationFeedback feedback, ExecutorService executorService) throws ExecutionException {
    new StaticizingProcessor(appView, this, converter).run(feedback, executorService);
  }

  private class CallSiteReferencesInvalidator extends UseRegistry {

    private CallSiteReferencesInvalidator(DexItemFactory factory) {
      super(factory);
    }

    private boolean registerMethod(DexMethod method) {
      registerTypeReference(method.holder);
      registerProto(method.proto);
      return true;
    }

    private boolean registerField(DexField field) {
      registerTypeReference(field.holder);
      registerTypeReference(field.type);
      return true;
    }

    @Override
    public boolean registerInvokeVirtual(DexMethod method) {
      return registerMethod(method);
    }

    @Override
    public boolean registerInvokeDirect(DexMethod method) {
      return registerMethod(method);
    }

    @Override
    public boolean registerInvokeStatic(DexMethod method) {
      return registerMethod(method);
    }

    @Override
    public boolean registerInvokeInterface(DexMethod method) {
      return registerMethod(method);
    }

    @Override
    public boolean registerInvokeSuper(DexMethod method) {
      return registerMethod(method);
    }

    @Override
    public boolean registerInstanceFieldWrite(DexField field) {
      return registerField(field);
    }

    @Override
    public boolean registerInstanceFieldRead(DexField field) {
      return registerField(field);
    }

    @Override
    public boolean registerNewInstance(DexType type) {
      return registerTypeReference(type);
    }

    @Override
    public boolean registerStaticFieldRead(DexField field) {
      return registerField(field);
    }

    @Override
    public boolean registerStaticFieldWrite(DexField field) {
      return registerField(field);
    }

    @Override
    public boolean registerTypeReference(DexType type) {
      CandidateInfo candidateInfo = candidates.get(type);
      if (candidateInfo != null) {
        candidateInfo.invalidate();
      }
      return true;
    }
  }
}
