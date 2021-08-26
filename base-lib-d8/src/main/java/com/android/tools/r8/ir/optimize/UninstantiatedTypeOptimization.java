// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.ir.optimize.UninstantiatedTypeOptimization.Strategy.ALLOW_ARGUMENT_REMOVAL;
import static com.android.tools.r8.ir.optimize.UninstantiatedTypeOptimization.Strategy.DISALLOW_ARGUMENT_REMOVAL;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription.RemovedArgumentInfo;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription.RemovedArgumentsInfo;
import com.android.tools.r8.graph.TopDownClassHierarchyTraversal;
import com.android.tools.r8.ir.analysis.AbstractError;
import com.android.tools.r8.ir.analysis.TypeChecker;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.MemberPoolCollection.MemberPool;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.android.tools.r8.utils.Timing;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class UninstantiatedTypeOptimization {

  enum Strategy {
    ALLOW_ARGUMENT_REMOVAL,
    DISALLOW_ARGUMENT_REMOVAL
  }

  static class UninstantiatedTypeOptimizationGraphLense extends NestedGraphLense {

    private final Map<DexMethod, RemovedArgumentsInfo> removedArgumentsInfoPerMethod;

    UninstantiatedTypeOptimizationGraphLense(
        BiMap<DexMethod, DexMethod> methodMap,
        Map<DexMethod, RemovedArgumentsInfo> removedArgumentsInfoPerMethod,
        AppView<?> appView) {
      super(
          ImmutableMap.of(),
          methodMap,
          ImmutableMap.of(),
          null,
          methodMap.inverse(),
          appView.graphLense(),
          appView.dexItemFactory());
      this.removedArgumentsInfoPerMethod = removedArgumentsInfoPerMethod;
    }

    @Override
    public RewrittenPrototypeDescription lookupPrototypeChanges(DexMethod method) {
      DexMethod originalMethod = originalMethodSignatures.getOrDefault(method, method);
      RewrittenPrototypeDescription result = previousLense.lookupPrototypeChanges(originalMethod);
      if (originalMethod != method) {
        if (method.proto.returnType.isVoidType() && !originalMethod.proto.returnType.isVoidType()) {
          result = result.withConstantReturn();
        }
        RemovedArgumentsInfo removedArgumentsInfo = removedArgumentsInfoPerMethod.get(method);
        if (removedArgumentsInfo != null) {
          result = result.withRemovedArguments(removedArgumentsInfo);
        }
      } else {
        assert !removedArgumentsInfoPerMethod.containsKey(method);
      }
      return result;
    }
  }

  private static final MethodSignatureEquivalence equivalence = MethodSignatureEquivalence.get();

  private final AppView<AppInfoWithLiveness> appView;
  private final TypeChecker typeChecker;

  private int numberOfInstanceGetOrInstancePutWithNullReceiver = 0;
  private int numberOfArrayInstructionsWithNullArray = 0;
  private int numberOfInvokesWithNullArgument = 0;
  private int numberOfInvokesWithNullReceiver = 0;
  private int numberOfMonitorWithNullReceiver = 0;

  public UninstantiatedTypeOptimization(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
    this.typeChecker = new TypeChecker(appView);
  }

  public GraphLense run(
      MethodPoolCollection methodPoolCollection, ExecutorService executorService, Timing timing) {

    try {
      methodPoolCollection.buildAll(executorService, timing);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Map<Wrapper<DexMethod>, Set<DexType>> changedVirtualMethods = new HashMap<>();
    BiMap<DexMethod, DexMethod> methodMapping = HashBiMap.create();
    Map<DexMethod, RemovedArgumentsInfo> removedArgumentsInfoPerMethod = new IdentityHashMap<>();

    TopDownClassHierarchyTraversal.forProgramClasses(appView)
        .visit(
            appView.appInfo().classes(),
            clazz ->
                processClass(
                    clazz,
                    changedVirtualMethods,
                    methodMapping,
                    methodPoolCollection,
                    removedArgumentsInfoPerMethod));

    if (!methodMapping.isEmpty()) {
      return new UninstantiatedTypeOptimizationGraphLense(
          methodMapping, removedArgumentsInfoPerMethod, appView);
    }
    return appView.graphLense();
  }

  private void processClass(
      DexProgramClass clazz,
      Map<Wrapper<DexMethod>, Set<DexType>> changedVirtualMethods,
      BiMap<DexMethod, DexMethod> methodMapping,
      MethodPoolCollection methodPoolCollection,
      Map<DexMethod, RemovedArgumentsInfo> removedArgumentsInfoPerMethod) {
    MemberPool<DexMethod> methodPool = methodPoolCollection.get(clazz);

    if (clazz.isInterface()) {
      // Do not allow changing the prototype of methods that override an interface method.
      // This achieved by faking that there is already a method with the given signature.
      for (DexEncodedMethod virtualMethod : clazz.virtualMethods()) {
        RewrittenPrototypeDescription prototypeChanges =
            new RewrittenPrototypeDescription(
                virtualMethod.method.proto.returnType.isAlwaysNull(appView),
                false,
                getRemovedArgumentsInfo(virtualMethod, ALLOW_ARGUMENT_REMOVAL));
        if (!prototypeChanges.isEmpty()) {
          DexMethod newMethod = getNewMethodSignature(virtualMethod, prototypeChanges);
          Wrapper<DexMethod> wrapper = equivalence.wrap(newMethod);
          if (!methodPool.hasSeenDirectly(wrapper)) {
            methodPool.seen(wrapper);
          }
        }
      }
      return;
    }

    Map<DexEncodedMethod, RewrittenPrototypeDescription> prototypeChangesPerMethod =
        new IdentityHashMap<>();
    for (DexEncodedMethod directMethod : clazz.directMethods()) {
      RewrittenPrototypeDescription prototypeChanges =
          getPrototypeChanges(directMethod, ALLOW_ARGUMENT_REMOVAL);
      if (!prototypeChanges.isEmpty()) {
        prototypeChangesPerMethod.put(directMethod, prototypeChanges);
      }
    }

    // Reserve all signatures which are known to not be touched below.
    Set<Wrapper<DexMethod>> usedSignatures = new HashSet<>();
    for (DexEncodedMethod method : clazz.methods()) {
      if (!prototypeChangesPerMethod.containsKey(method)) {
        usedSignatures.add(equivalence.wrap(method.method));
      }
    }

    // Change the return type of direct methods that return an uninstantiated type to void.
    List<DexEncodedMethod> directMethods = clazz.directMethods();
    for (int i = 0; i < directMethods.size(); ++i) {
      DexEncodedMethod encodedMethod = directMethods.get(i);
      DexMethod method = encodedMethod.method;
      RewrittenPrototypeDescription prototypeChanges =
          prototypeChangesPerMethod.getOrDefault(
              encodedMethod, RewrittenPrototypeDescription.none());
      RemovedArgumentsInfo removedArgumentsInfo = prototypeChanges.getRemovedArgumentsInfo();
      DexMethod newMethod = getNewMethodSignature(encodedMethod, prototypeChanges);
      if (newMethod != method) {
        Wrapper<DexMethod> wrapper = equivalence.wrap(newMethod);

        // TODO(b/110806787): Can be extended to handle collisions by renaming the given
        // method.
        if (usedSignatures.add(wrapper)) {
          clazz.setDirectMethod(
              i,
              encodedMethod.toTypeSubstitutedMethod(
                  newMethod,
                  removedArgumentsInfo.createParameterAnnotationsRemover(encodedMethod)));
          methodMapping.put(method, newMethod);
          if (removedArgumentsInfo.hasRemovedArguments()) {
            removedArgumentsInfoPerMethod.put(newMethod, removedArgumentsInfo);
          }
        }
      }
    }

    // Change the return type of virtual methods that return an uninstantiated type to void.
    // This is done in two steps. First we change the return type of all methods that override
    // a method whose return type has already been changed to void previously. Note that
    // all supertypes of the current class are always visited prior to the current class.
    // This is important to ensure that a method that used to override a method in its super
    // class will continue to do so after this optimization.
    List<DexEncodedMethod> virtualMethods = clazz.virtualMethods();
    for (int i = 0; i < virtualMethods.size(); ++i) {
      DexEncodedMethod encodedMethod = virtualMethods.get(i);
      DexMethod method = encodedMethod.method;
      RewrittenPrototypeDescription prototypeChanges =
          getPrototypeChanges(encodedMethod, DISALLOW_ARGUMENT_REMOVAL);
      RemovedArgumentsInfo removedArgumentsInfo = prototypeChanges.getRemovedArgumentsInfo();
      DexMethod newMethod = getNewMethodSignature(encodedMethod, prototypeChanges);
      if (newMethod != method) {
        Wrapper<DexMethod> wrapper = equivalence.wrap(newMethod);

        boolean isOverrideOfPreviouslyChangedMethodInSuperClass =
            changedVirtualMethods.getOrDefault(equivalence.wrap(method), ImmutableSet.of()).stream()
                .anyMatch(other -> appView.appInfo().isSubtype(clazz.type, other));
        if (isOverrideOfPreviouslyChangedMethodInSuperClass) {
          assert methodPool.hasSeen(wrapper);

          boolean signatureIsAvailable = usedSignatures.add(wrapper);
          assert signatureIsAvailable;

          clazz.setVirtualMethod(
              i,
              encodedMethod.toTypeSubstitutedMethod(
                  newMethod,
                  removedArgumentsInfo.createParameterAnnotationsRemover(encodedMethod)));
          methodMapping.put(method, newMethod);
        }
      }
    }
    for (int i = 0; i < virtualMethods.size(); ++i) {
      DexEncodedMethod encodedMethod = virtualMethods.get(i);
      DexMethod method = encodedMethod.method;
      RewrittenPrototypeDescription prototypeChanges =
          getPrototypeChanges(encodedMethod, DISALLOW_ARGUMENT_REMOVAL);
      RemovedArgumentsInfo removedArgumentsInfo = prototypeChanges.getRemovedArgumentsInfo();
      DexMethod newMethod = getNewMethodSignature(encodedMethod, prototypeChanges);
      if (newMethod != method) {
        Wrapper<DexMethod> wrapper = equivalence.wrap(newMethod);

        // TODO(b/110806787): Can be extended to handle collisions by renaming the given
        //  method. Note that this also requires renaming all of the methods that override this
        //  method, though.
        if (!methodPool.hasSeen(wrapper) && usedSignatures.add(wrapper)) {
          methodPool.seen(wrapper);

          clazz.setVirtualMethod(
              i,
              encodedMethod.toTypeSubstitutedMethod(
                  newMethod,
                  removedArgumentsInfo.createParameterAnnotationsRemover(encodedMethod)));
          methodMapping.put(method, newMethod);

          boolean added =
              changedVirtualMethods
                  .computeIfAbsent(equivalence.wrap(method), key -> Sets.newIdentityHashSet())
                  .add(clazz.type);
          assert added;
        }
      }
    }
  }

  private RewrittenPrototypeDescription getPrototypeChanges(
      DexEncodedMethod encodedMethod, Strategy strategy) {
    if (ArgumentRemovalUtils.isPinned(encodedMethod, appView)
        || appView.appInfo().keepConstantArguments.contains(encodedMethod.method)) {
      return RewrittenPrototypeDescription.none();
    }
    return new RewrittenPrototypeDescription(
        encodedMethod.method.proto.returnType.isAlwaysNull(appView),
        false,
        getRemovedArgumentsInfo(encodedMethod, strategy));
  }

  private RemovedArgumentsInfo getRemovedArgumentsInfo(
      DexEncodedMethod encodedMethod, Strategy strategy) {
    if (strategy == DISALLOW_ARGUMENT_REMOVAL) {
      return RemovedArgumentsInfo.empty();
    }

    List<RemovedArgumentInfo> removedArgumentsInfo = null;
    DexProto proto = encodedMethod.method.proto;
    int offset = encodedMethod.isStatic() ? 0 : 1;
    for (int i = 0; i < proto.parameters.size(); ++i) {
      DexType type = proto.parameters.values[i];
      if (type.isAlwaysNull(appView)) {
        if (removedArgumentsInfo == null) {
          removedArgumentsInfo = new ArrayList<>();
        }
        removedArgumentsInfo.add(
            RemovedArgumentInfo.builder()
                .setArgumentIndex(i + offset)
                .setIsAlwaysNull()
                .setType(type)
                .build());
      }
    }
    return removedArgumentsInfo != null
        ? new RemovedArgumentsInfo(removedArgumentsInfo)
        : RemovedArgumentsInfo.empty();
  }

  private DexMethod getNewMethodSignature(
      DexEncodedMethod encodedMethod, RewrittenPrototypeDescription prototypeChanges) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();

    DexMethod method = encodedMethod.method;
    RemovedArgumentsInfo removedArgumentsInfo = prototypeChanges.getRemovedArgumentsInfo();

    if (prototypeChanges.isEmpty()) {
      return method;
    }

    DexType newReturnType =
        prototypeChanges.hasBeenChangedToReturnVoid()
            ? dexItemFactory.voidType
            : method.proto.returnType;

    DexType[] newParameters;
    if (removedArgumentsInfo.hasRemovedArguments()) {
      // Currently not allowed to remove the receiver of an instance method. This would involve
      // changing invoke-direct/invoke-virtual into invoke-static.
      assert encodedMethod.isStatic() || !removedArgumentsInfo.isArgumentRemoved(0);
      newParameters =
          new DexType
              [method.proto.parameters.size() - removedArgumentsInfo.numberOfRemovedArguments()];
      int offset = encodedMethod.isStatic() ? 0 : 1;
      int newParametersIndex = 0;
      for (int argumentIndex = 0; argumentIndex < method.proto.parameters.size(); ++argumentIndex) {
        if (!removedArgumentsInfo.isArgumentRemoved(argumentIndex + offset)) {
          newParameters[newParametersIndex] = method.proto.parameters.values[argumentIndex];
          newParametersIndex++;
        }
      }
    } else {
      newParameters = method.proto.parameters.values;
    }

    return dexItemFactory.createMethod(
        method.holder, dexItemFactory.createProto(newReturnType, newParameters), method.name);
  }

  public void rewrite(IRCode code) {
    Set<BasicBlock> blocksToBeRemoved = Sets.newIdentityHashSet();
    ListIterator<BasicBlock> blockIterator = code.listIterator();
    Set<Value> valuesToNarrow = Sets.newIdentityHashSet();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (blocksToBeRemoved.contains(block)) {
        continue;
      }
      InstructionListIterator instructionIterator = block.listIterator(code);
      while (instructionIterator.hasNext()) {
        Instruction instruction = instructionIterator.next();
        if (instruction.throwsOnNullInput()) {
          Value couldBeNullValue = instruction.getNonNullInput();
          if (isThrowNullCandidate(
              couldBeNullValue, instruction, appView, code.method.method.holder)) {
            if (instruction.isInstanceGet() || instruction.isInstancePut()) {
              ++numberOfInstanceGetOrInstancePutWithNullReceiver;
            } else if (instruction.isInvokeMethodWithReceiver()) {
              ++numberOfInvokesWithNullReceiver;
            } else if (instruction.isArrayGet()
                || instruction.isArrayPut()
                || instruction.isArrayLength()) {
              ++numberOfArrayInstructionsWithNullArray;
            } else if (instruction.isMonitor()) {
              ++numberOfMonitorWithNullReceiver;
            } else {
              assert false;
            }
            instructionIterator.replaceCurrentInstructionWithThrowNull(
                appView, code, blockIterator, blocksToBeRemoved, valuesToNarrow);
            continue;
          }
        }
        if (instruction.isFieldInstruction()) {
          rewriteFieldInstruction(
              instruction.asFieldInstruction(),
              blockIterator,
              instructionIterator,
              code,
              valuesToNarrow);
        } else if (instruction.isInvokeMethod()) {
          rewriteInvoke(
              instruction.asInvokeMethod(),
              blockIterator,
              instructionIterator,
              code,
              blocksToBeRemoved,
              valuesToNarrow);
        }
      }
    }
    code.removeBlocks(blocksToBeRemoved);
    code.removeAllTrivialPhis(valuesToNarrow);
    code.removeUnreachableBlocks();
    if (!valuesToNarrow.isEmpty()) {
      new TypeAnalysis(appView).narrowing(valuesToNarrow);
    }
    assert code.isConsistentSSA();
  }

  private static boolean isThrowNullCandidate(
      Value couldBeNullValue,
      Instruction current,
      AppView<? extends AppInfoWithSubtyping> appView,
      DexType context) {
    if (!couldBeNullValue.isAlwaysNull(appView)) {
      return false;
    }
    if (current.isFieldInstruction()) {
      // Other resolution-related errors come first.
      AbstractError abstractError =
          current.asFieldInstruction().instructionInstanceCanThrow(appView, context);
      if (abstractError.isThrowing()
          && abstractError.getSpecificError(appView.dexItemFactory())
              != appView.dexItemFactory().npeType) {
        // We can't replace the current instruction with `throw null` if it may throw another
        // Error/Exception than NullPointerException.
        return false;
      }
    }
    return true;
  }

  public void logResults() {
    assert Log.ENABLED;
    Log.info(
        getClass(),
        "Number of instance-get/instance-put with null receiver: %s",
        numberOfInstanceGetOrInstancePutWithNullReceiver);
    Log.info(
        getClass(),
        "Number of array instructions with null reference: %s",
        numberOfArrayInstructionsWithNullArray);
    Log.info(
        getClass(), "Number of invokes with null argument: %s", numberOfInvokesWithNullArgument);
    Log.info(
        getClass(), "Number of invokes with null receiver: %s", numberOfInvokesWithNullReceiver);
    Log.info(
        getClass(), "Number of monitor with null receiver: %s", numberOfMonitorWithNullReceiver);
  }

  // instance-{get|put} with a null receiver has already been rewritten to `throw null`.
  // At this point, field-instruction whose target field type is uninstantiated will be handled.
  private void rewriteFieldInstruction(
      FieldInstruction instruction,
      ListIterator<BasicBlock> blockIterator,
      InstructionListIterator instructionIterator,
      IRCode code,
      Set<Value> affectedValues) {
    DexType context = code.method.method.holder;
    DexField field = instruction.getField();
    DexType fieldType = field.type;
    if (fieldType.isAlwaysNull(appView)) {
      // TODO(b/123857022): Should be possible to use definitionFor().
      DexEncodedField encodedField = appView.appInfo().resolveField(field);
      if (encodedField == null) {
        return;
      }

      boolean instructionCanBeRemoved =
          instruction.instructionInstanceCanThrow(appView, context).isThrowing();

      BasicBlock block = instruction.getBlock();
      if (instruction.isFieldPut()) {
        if (!typeChecker.checkFieldPut(instruction)) {
          // Broken type hierarchy. See FieldTypeTest#test_brokenTypeHierarchy.
          assert appView.options().testing.allowTypeErrors;
          return;
        }

        // We know that the right-hand side must be null, so this is a no-op.
        if (instructionCanBeRemoved) {
          instructionIterator.removeOrReplaceByDebugLocalRead();
        }
      } else {
        if (instructionCanBeRemoved) {
          // Replace the field read by the constant null.
          affectedValues.addAll(instruction.outValue().affectedValues());
          instructionIterator.replaceCurrentInstruction(code.createConstNull());
        } else {
          replaceOutValueByNull(instruction, instructionIterator, code, affectedValues);
        }
      }

      if (block.hasCatchHandlers()) {
        // This block can no longer throw.
        block.getCatchHandlers().getUniqueTargets().forEach(BasicBlock::unlinkCatchHandler);
      }
    }
  }

  // invoke instructions with a null receiver has already been rewritten to `throw null`.
  // At this point, we attempt to explore non-null-param-or-throw optimization info and replace
  // the invocation with `throw null` if an argument is known to be null and the method is going to
  // throw for that null argument.
  private void rewriteInvoke(
      InvokeMethod invoke,
      ListIterator<BasicBlock> blockIterator,
      InstructionListIterator instructionIterator,
      IRCode code,
      Set<BasicBlock> blocksToBeRemoved,
      Set<Value> affectedValues) {
    DexEncodedMethod target = invoke.lookupSingleTarget(appView, code.method.method.holder);
    if (target == null) {
      return;
    }

    BitSet facts = target.getOptimizationInfo().getNonNullParamOrThrow();
    if (facts != null) {
      for (int i = 0; i < invoke.arguments().size(); i++) {
        Value argument = invoke.arguments().get(i);
        if (argument.isAlwaysNull(appView) && facts.get(i)) {
          instructionIterator.replaceCurrentInstructionWithThrowNull(
              appView, code, blockIterator, blocksToBeRemoved, affectedValues);
          ++numberOfInvokesWithNullArgument;
          return;
        }
      }
    }

    DexType returnType = target.method.proto.returnType;
    if (returnType.isAlwaysNull(appView)) {
      replaceOutValueByNull(invoke, instructionIterator, code, affectedValues);
    }
  }

  private void replaceOutValueByNull(
      Instruction instruction,
      InstructionListIterator instructionIterator,
      IRCode code,
      Set<Value> affectedValues) {
    assert instructionIterator.peekPrevious() == instruction;
    if (instruction.hasOutValue()) {
      Value outValue = instruction.outValue();
      if (outValue.numberOfAllUsers() > 0) {
        instructionIterator.previous();
        affectedValues.addAll(outValue.affectedValues());
        outValue.replaceUsers(
            instructionIterator.insertConstNullInstruction(code, appView.options()));
        instructionIterator.next();
      }
    }
  }
}
