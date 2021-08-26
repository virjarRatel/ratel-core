// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.classinliner;


import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionOrPhi;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.desugar.LambdaRewriter;
import com.android.tools.r8.ir.optimize.Inliner;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.InliningInfo;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.InliningOracle;
import com.android.tools.r8.ir.optimize.classinliner.ClassInliner.EligibilityStatus;
import com.android.tools.r8.ir.optimize.info.FieldOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.MethodOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsage;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import com.android.tools.r8.ir.optimize.inliner.InliningIRProvider;
import com.android.tools.r8.ir.optimize.inliner.NopWhyAreYouNotInliningReporter;
import com.android.tools.r8.kotlin.KotlinInfo;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Pair;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class InlineCandidateProcessor {

  enum AliasKind {
    DEFINITE,
    MAYBE
  }

  private static final ImmutableSet<If.Type> ALLOWED_ZERO_TEST_TYPES =
      ImmutableSet.of(If.Type.EQ, If.Type.NE);

  private final AppView<AppInfoWithLiveness> appView;
  private final LambdaRewriter lambdaRewriter;
  private final Inliner inliner;
  private final Function<DexClass, EligibilityStatus> isClassEligible;
  private final Predicate<DexEncodedMethod> isProcessedConcurrently;
  private final DexEncodedMethod method;
  private final Instruction root;

  private Value eligibleInstance;
  private DexType eligibleClass;
  private DexClass eligibleClassDefinition;
  private boolean isDesugaredLambda;

  private final Map<InvokeMethodWithReceiver, InliningInfo> methodCallsOnInstance =
      new IdentityHashMap<>();
  private final Map<InvokeMethod, InliningInfo> extraMethodCalls
      = new IdentityHashMap<>();
  private final List<Pair<InvokeMethod, Integer>> unusedArguments
      = new ArrayList<>();

  private final Map<InvokeMethod, DexEncodedMethod> directInlinees = new IdentityHashMap<>();
  private final List<DexEncodedMethod> indirectInlinees = new ArrayList<>();

  // Sets of values that must/may be an alias of the "root" instance (including the root instance
  // itself).
  private final ClassInlinerReceiverSet receivers;

  InlineCandidateProcessor(
      AppView<AppInfoWithLiveness> appView,
      LambdaRewriter lambdaRewriter,
      Inliner inliner,
      Function<DexClass, EligibilityStatus> isClassEligible,
      Predicate<DexEncodedMethod> isProcessedConcurrently,
      DexEncodedMethod method,
      Instruction root) {
    this.appView = appView;
    this.lambdaRewriter = lambdaRewriter;
    this.inliner = inliner;
    this.isClassEligible = isClassEligible;
    this.method = method;
    this.root = root;
    this.isProcessedConcurrently = isProcessedConcurrently;
    this.receivers = new ClassInlinerReceiverSet(root.outValue());
  }

  Map<InvokeMethod, DexEncodedMethod> getDirectInlinees() {
    return directInlinees;
  }

  List<DexEncodedMethod> getIndirectInlinees() {
    return indirectInlinees;
  }

  ClassInlinerReceiverSet getReceivers() {
    return receivers;
  }

  // Checks if the root instruction defines eligible value, i.e. the value
  // exists and we have a definition of its class.
  EligibilityStatus isInstanceEligible() {
    eligibleInstance = root.outValue();
    if (eligibleInstance == null) {
      return EligibilityStatus.UNUSED_INSTANCE;
    }
    if (root.isNewInstance()) {
      eligibleClass = root.asNewInstance().clazz;
    } else {
      assert root.isStaticGet();
      StaticGet staticGet = root.asStaticGet();
      if (staticGet.instructionMayHaveSideEffects(appView, method.method.holder)) {
        return EligibilityStatus.RETRIEVAL_MAY_HAVE_SIDE_EFFECTS;
      }
      DexEncodedField field = appView.appInfo().resolveField(staticGet.getField());
      FieldOptimizationInfo optimizationInfo = field.getOptimizationInfo();
      ClassTypeLatticeElement dynamicLowerBoundType = optimizationInfo.getDynamicLowerBoundType();
      if (dynamicLowerBoundType == null
          || !dynamicLowerBoundType.equals(optimizationInfo.getDynamicUpperBoundType())) {
        return EligibilityStatus.NOT_A_SINGLETON_FIELD;
      }
      eligibleClass = dynamicLowerBoundType.getClassType();
    }
    if (!eligibleClass.isClassType()) {
      return EligibilityStatus.NON_CLASS_TYPE;
    }
    if (lambdaRewriter != null) {
      // Check if the class is synthesized for a desugared lambda
      eligibleClassDefinition = lambdaRewriter.getLambdaClass(eligibleClass);
      isDesugaredLambda = eligibleClassDefinition != null;
    }
    if (eligibleClassDefinition == null) {
      eligibleClassDefinition = appView.definitionFor(eligibleClass);
    }
    if (eligibleClassDefinition != null) {
      return EligibilityStatus.ELIGIBLE;
    } else {
      return EligibilityStatus.UNKNOWN_TYPE;
    }
  }

  // Checks if the class is eligible and is properly used. Regarding general class
  // eligibility rules see comment on computeClassEligible(...).
  //
  // In addition to class being eligible this method also checks:
  //   -- for 'new-instance' root:
  //      * class itself does not have static initializer
  //   -- for 'static-get' root:
  //      * class does not have instance fields
  //      * class is final
  //      * class has class initializer marked as TrivialClassInitializer, and
  //        class initializer initializes the field we are reading here.
  EligibilityStatus isClassAndUsageEligible() {
    EligibilityStatus status = isClassEligible.apply(eligibleClassDefinition);
    if (status != EligibilityStatus.ELIGIBLE) {
      return status;
    }

    if (root.isNewInstance()) {
      // NOTE: if the eligible class does not directly extend java.lang.Object,
      // we also have to guarantee that it is initialized with initializer classified as
      // TrivialInstanceInitializer. This will be checked in areInstanceUsersEligible(...).

      // There must be no static initializer on the class itself.
      if (eligibleClassDefinition.classInitializationMayHaveSideEffects(
          appView,
          // Types that are a super type of the current context are guaranteed to be initialized.
          type -> appView.isSubtype(method.method.holder, type).isTrue())) {
        return EligibilityStatus.HAS_CLINIT;
      }
      return EligibilityStatus.ELIGIBLE;
    }

    assert root.isStaticGet();

    // We know that desugared lambda classes satisfy eligibility requirements.
    if (isDesugaredLambda) {
      return EligibilityStatus.ELIGIBLE;
    }

    // Checking if we can safely inline class implemented following singleton-like
    // pattern, by which we assume a static final field holding on to the reference
    // initialized in class constructor.
    //
    // In general we are targeting cases when the class is defined as:
    //
    //   class X {
    //     static final X F;
    //     static {
    //       F = new X();
    //     }
    //   }
    //
    // and being used as follows:
    //
    //   void foo() {
    //     f = X.F;
    //     f.bar();
    //   }
    //
    // The main difference from the similar case of class inliner with 'new-instance'
    // instruction is that in this case the instance we inline is not just leaked, but
    // is actually published via X.F field. There are several risks we need to address
    // in this case:
    //
    //    Risk: instance stored in field X.F has changed after it was initialized in
    //      class initializer
    //    Solution: we assume that final field X.F is not modified outside the class
    //      initializer. In rare cases when it is (e.g. via reflections) it should
    //      be marked with keep rules
    //
    //    Risk: instance stored in field X.F is not initialized yet
    //    Solution: not initialized instance can only be visible if X.<clinit>
    //      triggers other class initialization which references X.F. This
    //      situation should never happen if we:
    //        -- don't allow any superclasses to have static initializer,
    //        -- don't allow any subclasses,
    //        -- guarantee the class has trivial class initializer
    //           (see CodeRewriter::computeClassInitializerInfo), and
    //        -- guarantee the instance is initialized with trivial instance
    //           initializer (see CodeRewriter::computeInstanceInitializerInfo)
    //
    //    Risk: instance stored in field X.F was mutated
    //    Solution: we require that class X does not have any instance fields, and
    //      if any of its superclasses has instance fields, accessing them will make
    //      this instance not eligible for inlining. I.e. even though the instance is
    //      publicized and its state has been mutated, it will not effect the logic
    //      of class inlining
    //

    if (!eligibleClassDefinition.instanceFields().isEmpty()) {
      return EligibilityStatus.HAS_INSTANCE_FIELDS;
    }
    return EligibilityStatus.ELIGIBLE;
  }

  /**
   * Checks if the inlining candidate instance users are eligible, see comment on {@link
   * ClassInliner#processMethodCode}.
   *
   * @return null if all users are eligible, or the first ineligible user.
   */
  InstructionOrPhi areInstanceUsersEligible(Supplier<InliningOracle> defaultOracle) {
    // No Phi users.
    if (eligibleInstance.hasPhiUsers()) {
      return eligibleInstance.firstPhiUser(); // Not eligible.
    }

    Set<Instruction> currentUsers = eligibleInstance.uniqueUsers();
    while (!currentUsers.isEmpty()) {
      Set<Instruction> indirectUsers = Sets.newIdentityHashSet();
      for (Instruction user : currentUsers) {
        if (user.isAssume()) {
          Value alias = user.outValue();
          if (receivers.isReceiverAlias(alias)) {
            continue; // Already processed.
          }
          if (alias.hasPhiUsers()) {
            return alias.firstPhiUser(); // Not eligible.
          }
          if (!receivers.addReceiverAlias(alias, AliasKind.DEFINITE)) {
            return user; // Not eligible.
          }
          indirectUsers.addAll(alias.uniqueUsers());
          continue;
        }
        // Field read/write.
        if (user.isInstanceGet()
            || (user.isInstancePut()
                && receivers.addIllegalReceiverAlias(user.asInstancePut().value()))) {
          DexEncodedField field =
              appView.appInfo().resolveField(user.asFieldInstruction().getField());
          if (field == null || field.isStatic()) {
            return user; // Not eligible.
          }
          continue;
        }

        if (user.isInvokeMethod()) {
          InvokeMethod invokeMethod = user.asInvokeMethod();
          DexEncodedMethod singleTarget =
              invokeMethod.lookupSingleTarget(appView, method.method.holder);
          if (!isEligibleSingleTarget(singleTarget)) {
            return user; // Not eligible.
          }

          // Eligible constructor call (for new instance roots only).
          if (user.isInvokeDirect()) {
            InvokeDirect invoke = user.asInvokeDirect();
            if (appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())) {
              boolean isCorrespondingConstructorCall =
                  root.isNewInstance()
                      && !invoke.inValues().isEmpty()
                      && root.outValue() == invoke.getReceiver();
              if (isCorrespondingConstructorCall) {
                InliningInfo inliningInfo =
                    isEligibleConstructorCall(invoke, singleTarget, defaultOracle);
                if (inliningInfo != null) {
                  methodCallsOnInstance.put(invoke, inliningInfo);
                  continue;
                }
              }
              assert !isExtraMethodCall(invoke);
              return user; // Not eligible.
            }
          }

          // Eligible virtual method call on the instance as a receiver.
          if (user.isInvokeVirtual() || user.isInvokeInterface()) {
            InvokeMethodWithReceiver invoke = user.asInvokeMethodWithReceiver();
            InliningInfo inliningInfo =
                isEligibleDirectVirtualMethodCall(
                    invoke, singleTarget, indirectUsers, defaultOracle);
            if (inliningInfo != null) {
              methodCallsOnInstance.put(invoke, inliningInfo);
              continue;
            }
          }

          // Eligible usage as an invocation argument.
          if (isExtraMethodCall(invokeMethod)) {
            assert !invokeMethod.isInvokeSuper();
            assert !invokeMethod.isInvokePolymorphic();
            if (isExtraMethodCallEligible(invokeMethod, singleTarget, defaultOracle)) {
              continue;
            }
          }

          return user; // Not eligible.
        }

        // Allow some IF instructions.
        if (user.isIf()) {
          If ifInsn = user.asIf();
          If.Type type = ifInsn.getType();
          if (ifInsn.isZeroTest() && (type == If.Type.EQ || type == If.Type.NE)) {
            // Allow ==/!= null tests, we know that the instance is a non-null value.
            continue;
          }
        }

        return user; // Not eligible.
      }
      currentUsers = indirectUsers;
    }

    return null; // Eligible.
  }

  // Process inlining, includes the following steps:
  //
  //  * remove linked assume instructions if any so that users of the eligible field are up-to-date.
  //  * replace unused instance usages as arguments which are never used
  //  * inline extra methods if any, collect new direct method calls
  //  * inline direct methods if any
  //  * remove superclass initializer call and field reads
  //  * remove field writes
  //  * remove root instruction
  //
  // Returns `true` if at least one method was inlined.
  boolean processInlining(
      IRCode code, Supplier<InliningOracle> defaultOracle, InliningIRProvider inliningIRProvider)
      throws IllegalClassInlinerStateException {
    // Verify that `eligibleInstance` is not aliased.
    assert eligibleInstance == eligibleInstance.getAliasedValue();
    replaceUsagesAsUnusedArgument(code);

    boolean anyInlinedMethods = forceInlineExtraMethodInvocations(code, inliningIRProvider);
    if (anyInlinedMethods) {
      // Reset the collections.
      methodCallsOnInstance.clear();
      extraMethodCalls.clear();
      unusedArguments.clear();
      receivers.reset();

      // Repeat user analysis
      InstructionOrPhi ineligibleUser = areInstanceUsersEligible(defaultOracle);
      if (ineligibleUser != null) {
        throw new IllegalClassInlinerStateException();
      }
      assert extraMethodCalls.isEmpty()
          : "Remaining extra method calls: " + StringUtils.join(extraMethodCalls.entrySet(), ", ");
      assert unusedArguments.isEmpty()
          : "Remaining unused arg: " + StringUtils.join(unusedArguments, ", ");
    }

    anyInlinedMethods |= forceInlineDirectMethodInvocations(code, inliningIRProvider);
    removeAssumeInstructionsLinkedToEligibleInstance();
    removeMiscUsages(code);
    removeFieldReads(code);
    removeFieldWrites();
    removeInstruction(root);
    return anyInlinedMethods;
  }

  private void replaceUsagesAsUnusedArgument(IRCode code) {
    for (Pair<InvokeMethod, Integer> unusedArgument : unusedArguments) {
      InvokeMethod invoke = unusedArgument.getFirst();
      BasicBlock block = invoke.getBlock();

      ConstNumber nullValue = code.createConstNull();
      nullValue.setPosition(invoke.getPosition());
      block.listIterator(code, invoke).add(nullValue);
      assert nullValue.getBlock() == block;

      int argIndex = unusedArgument.getSecond();
      invoke.replaceValue(argIndex, nullValue.outValue());
    }
    unusedArguments.clear();
  }

  private boolean forceInlineExtraMethodInvocations(
      IRCode code, InliningIRProvider inliningIRProvider) {
    if (extraMethodCalls.isEmpty()) {
      return false;
    }
    // Inline extra methods.
    inliner.performForcedInlining(method, code, extraMethodCalls, inliningIRProvider);
    return true;
  }

  private boolean forceInlineDirectMethodInvocations(
      IRCode code, InliningIRProvider inliningIRProvider) throws IllegalClassInlinerStateException {
    if (methodCallsOnInstance.isEmpty()) {
      return false;
    }

    assert methodCallsOnInstance.keySet().stream()
        .map(InvokeMethodWithReceiver::getReceiver)
        .allMatch(receivers::isReceiverAlias);

    inliner.performForcedInlining(method, code, methodCallsOnInstance, inliningIRProvider);

    // In case we are class inlining an object allocation that does not inherit directly from
    // java.lang.Object, we need keep force inlining the constructor until we reach
    // java.lang.Object.<init>().
    if (root.isNewInstance()) {
      do {
        methodCallsOnInstance.clear();
        for (Instruction instruction : eligibleInstance.uniqueUsers()) {
          if (instruction.isInvokeDirect()) {
            InvokeDirect invoke = instruction.asInvokeDirect();
            Value receiver = invoke.getReceiver().getAliasedValue();
            if (receiver != eligibleInstance) {
              continue;
            }

            DexMethod invokedMethod = invoke.getInvokedMethod();
            if (invokedMethod == appView.dexItemFactory().objectMethods.constructor) {
              continue;
            }

            if (!appView.dexItemFactory().isConstructor(invokedMethod)) {
              throw new IllegalClassInlinerStateException();
            }

            DexEncodedMethod singleTarget = appView.definitionFor(invokedMethod);
            if (singleTarget == null
                || !singleTarget.isInliningCandidate(
                    method,
                    Reason.SIMPLE,
                    appView.appInfo(),
                    NopWhyAreYouNotInliningReporter.getInstance())) {
              throw new IllegalClassInlinerStateException();
            }

            methodCallsOnInstance.put(
                invoke, new InliningInfo(singleTarget, root.asNewInstance().clazz));
            break;
          }
        }
        if (!methodCallsOnInstance.isEmpty()) {
          inliner.performForcedInlining(method, code, methodCallsOnInstance, inliningIRProvider);
        }
      } while (!methodCallsOnInstance.isEmpty());
    }

    return true;
  }

  private void removeAssumeInstructionsLinkedToEligibleInstance() {
    for (Instruction user : eligibleInstance.aliasedUsers()) {
      if (!user.isAssume()) {
        continue;
      }
      Assume<?> assumeInstruction = user.asAssume();
      Value src = assumeInstruction.src();
      Value dest = assumeInstruction.outValue();
      assert receivers.isReceiverAlias(dest);
      assert !dest.hasPhiUsers();
      dest.replaceUsers(src);
      removeInstruction(user);
    }
    // Verify that no more assume instructions are left as users.
    assert eligibleInstance.aliasedUsers().stream().noneMatch(Instruction::isAssume);
  }

  // Remove miscellaneous users before handling field reads.
  private void removeMiscUsages(IRCode code) {
    boolean needToRemoveUnreachableBlocks = false;
    for (Instruction user : eligibleInstance.uniqueUsers()) {
      // Remove the call to java.lang.Object.<init>().
      if (user.isInvokeDirect()) {
        InvokeDirect invoke = user.asInvokeDirect();
        if (root.isNewInstance()
            && invoke.getInvokedMethod() == appView.dexItemFactory().objectMethods.constructor) {
          removeInstruction(invoke);
          continue;
        }
      }

      if (user.isIf()) {
        If ifInsn = user.asIf();
        assert ifInsn.isZeroTest()
            : "Unexpected usage in non-zero-test IF instruction: " + user;
        BasicBlock block = user.getBlock();
        If.Type type = ifInsn.getType();
        assert type == If.Type.EQ || type == If.Type.NE
            : "Unexpected type in zero-test IF instruction: " + user;
        BasicBlock newBlock = type == If.Type.EQ
            ? ifInsn.fallthroughBlock() : ifInsn.getTrueTarget();
        BasicBlock blockToRemove = type == If.Type.EQ
            ? ifInsn.getTrueTarget() : ifInsn.fallthroughBlock();
        assert newBlock != blockToRemove;

        block.replaceSuccessor(blockToRemove, newBlock);
        blockToRemove.removePredecessor(block, null);
        assert block.exit().isGoto();
        assert block.exit().asGoto().getTarget() == newBlock;
        needToRemoveUnreachableBlocks = true;
        continue;
      }

      if (user.isInstanceGet() || user.isInstancePut()) {
        // Leave field reads/writes until next steps.
        continue;
      }

      if (user.isMonitor()) {
        // Since this instance never escapes and is guaranteed to be non-null, any monitor
        // instructions are no-ops.
        removeInstruction(user);
        continue;
      }

      throw new Unreachable(
          "Unexpected usage left in method `"
              + method.method.toSourceString()
              + "` after inlining: "
              + user);
    }

    if (needToRemoveUnreachableBlocks) {
      code.removeUnreachableBlocks();
    }
  }

  // Replace field reads with appropriate values, insert phis when needed.
  private void removeFieldReads(IRCode code) {
    TreeSet<InstanceGet> uniqueInstanceGetUsersWithDeterministicOrder =
        new TreeSet<>(Comparator.comparingInt(x -> x.outValue().getNumber()));
    for (Instruction user : eligibleInstance.uniqueUsers()) {
      if (user.isInstanceGet()) {
        if (user.outValue().hasAnyUsers()) {
          uniqueInstanceGetUsersWithDeterministicOrder.add(user.asInstanceGet());
        } else {
          removeInstruction(user);
        }
        continue;
      }

      if (user.isInstancePut()) {
        // Skip in this iteration since these instructions are needed to properly calculate what
        // value should field reads be replaced with.
        continue;
      }

      throw new Unreachable(
          "Unexpected usage left in method `"
              + method.method.toSourceString()
              + "` after inlining: "
              + user);
    }

    Map<DexField, FieldValueHelper> fieldHelpers = new IdentityHashMap<>();
    for (InstanceGet user : uniqueInstanceGetUsersWithDeterministicOrder) {
      // Replace a field read with appropriate value.
      replaceFieldRead(code, user, fieldHelpers);
    }
  }

  private void replaceFieldRead(
      IRCode code, InstanceGet fieldRead, Map<DexField, FieldValueHelper> fieldHelpers) {
    Value value = fieldRead.outValue();
    if (value != null) {
      FieldValueHelper helper =
          fieldHelpers.computeIfAbsent(
              fieldRead.getField(), field -> new FieldValueHelper(field, code, root, appView));
      Value newValue = helper.getValueForFieldRead(fieldRead.getBlock(), fieldRead);
      value.replaceUsers(newValue);
      for (FieldValueHelper fieldValueHelper : fieldHelpers.values()) {
        fieldValueHelper.replaceValue(value, newValue);
      }
      assert !value.hasAnyUsers();
      // `newValue` could be a phi introduced by FieldValueHelper. Its initial type is set as the
      // type of read field, but it could be more precise than that due to (multiple) inlining.
      // In addition to values affected by `newValue`, it's necessary to revisit `newValue` itself.
      new TypeAnalysis(appView).narrowing(
          Iterables.concat(ImmutableSet.of(newValue), newValue.affectedValues()));
    }
    removeInstruction(fieldRead);
  }

  private void removeFieldWrites() {
    for (Instruction user : eligibleInstance.uniqueUsers()) {
      if (!user.isInstancePut()) {
        throw new Unreachable(
            "Unexpected usage left in method `"
                + method.method.toSourceString()
                + "` after field reads removed: "
                + user);
      }
      InstancePut instancePut = user.asInstancePut();
      DexEncodedField field =
          appView.appInfo().resolveFieldOn(eligibleClassDefinition, instancePut.getField());
      if (field == null) {
        throw new Unreachable(
            "Unexpected field write left in method `"
                + method.method.toSourceString()
                + "` after field reads removed: "
                + user);
      }
      removeInstruction(user);
    }
  }

  private InliningInfo isEligibleConstructorCall(
      InvokeDirect invoke, DexEncodedMethod singleTarget, Supplier<InliningOracle> defaultOracle) {
    assert appView.dexItemFactory().isConstructor(invoke.getInvokedMethod());
    assert isEligibleSingleTarget(singleTarget);

    // Must be a constructor called on the receiver.
    if (!receivers.isDefiniteReceiverAlias(invoke.getReceiver())) {
      return null;
    }

    // None of the subsequent arguments may be an alias of the receiver.
    List<Value> inValues = invoke.inValues();
    for (int i = 1; i < inValues.size(); i++) {
      if (!receivers.addIllegalReceiverAlias(inValues.get(i))) {
        return null;
      }
    }

    // Must be a constructor of the exact same class.
    DexMethod init = invoke.getInvokedMethod();
    if (init.holder != eligibleClass) {
      // Calling a constructor on a class that is different from the type of the instance.
      // Gracefully abort class inlining (see the test B116282409).
      return null;
    }

    // Check that the `eligibleInstance` does not escape via the constructor.
    ParameterUsage parameterUsage = singleTarget.getOptimizationInfo().getParameterUsages(0);
    if (!isEligibleParameterUsage(parameterUsage, invoke, defaultOracle)) {
      return null;
    }

    if (isDesugaredLambda) {
      // Lambda desugaring synthesizes eligible constructors.
      markSizeForInlining(invoke, singleTarget);
      return new InliningInfo(singleTarget, eligibleClass);
    }

    // Check that the entire constructor chain can be inlined into the current context.
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    DexMethod parent = singleTarget.getOptimizationInfo().getInstanceInitializerInfo().getParent();
    while (parent != dexItemFactory.objectMethods.constructor) {
      if (parent == null) {
        return null;
      }
      DexEncodedMethod encodedParent = appView.definitionFor(parent);
      if (encodedParent == null) {
        return null;
      }
      if (!encodedParent.isInliningCandidate(
          method,
          Reason.SIMPLE,
          appView.appInfo(),
          NopWhyAreYouNotInliningReporter.getInstance())) {
        return null;
      }
      parent = encodedParent.getOptimizationInfo().getInstanceInitializerInfo().getParent();
    }

    return singleTarget.getOptimizationInfo().getClassInlinerEligibility() != null
        ? new InliningInfo(singleTarget, eligibleClass)
        : null;
  }

  // An invoke is eligible for inlining in the following cases:
  //
  // - if it does not return the receiver
  // - if there are no uses of the out value
  // - if it is a regular chaining pattern where the only users of the out value are receivers to
  //   other invocations. In that case, we should add all indirect users of the out value to ensure
  //   they can also be inlined.
  private boolean isEligibleInvokeWithAllUsersAsReceivers(
      ClassInlinerEligibilityInfo eligibility,
      InvokeMethodWithReceiver invoke,
      Set<Instruction> indirectUsers) {
    if (eligibility.returnsReceiver.isFalse()) {
      return true;
    }

    Value outValue = invoke.outValue();
    if (outValue == null || !outValue.hasAnyUsers()) {
      return true;
    }

    // For CF we no longer perform the code-rewrite in CodeRewriter.rewriteMoveResult that removes
    // out values if they alias to the receiver since that naively produces a lot of popping values
    // from the stack.
    if (outValue.hasPhiUsers() || outValue.hasDebugUsers()) {
      return false;
    }

    // Add the out-value as a definite-alias if the invoke instruction is guaranteed to return the
    // receiver. Otherwise, the out-value may be an alias of the receiver, and it is added to the
    // may-alias set.
    AliasKind kind = eligibility.returnsReceiver.isTrue() ? AliasKind.DEFINITE : AliasKind.MAYBE;
    if (!receivers.addReceiverAlias(outValue, kind)) {
      return false;
    }

    Set<Instruction> currentUsers = outValue.uniqueUsers();
    while (!currentUsers.isEmpty()) {
      Set<Instruction> indirectOutValueUsers = Sets.newIdentityHashSet();
      for (Instruction instruction : currentUsers) {
        if (instruction.isAssume()) {
          Value outValueAlias = instruction.outValue();
          if (outValueAlias.hasPhiUsers() || outValueAlias.hasDebugUsers()) {
            return false;
          }
          if (!receivers.addReceiverAlias(outValueAlias, kind)) {
            return false;
          }
          indirectOutValueUsers.addAll(outValueAlias.uniqueUsers());
          continue;
        }

        if (instruction.isInvokeMethodWithReceiver()) {
          InvokeMethodWithReceiver user = instruction.asInvokeMethodWithReceiver();
          if (user.getReceiver().getAliasedValue() != outValue) {
            return false;
          }
          for (int i = 1; i < user.inValues().size(); i++) {
            if (user.inValues().get(i).getAliasedValue() == outValue) {
              return false;
            }
          }
          indirectUsers.add(user);
          continue;
        }

        return false;
      }
      currentUsers = indirectOutValueUsers;
    }

    return true;
  }

  private InliningInfo isEligibleDirectVirtualMethodCall(
      InvokeMethodWithReceiver invoke,
      DexEncodedMethod singleTarget,
      Set<Instruction> indirectUsers,
      Supplier<InliningOracle> defaultOracle) {
    assert isEligibleSingleTarget(singleTarget);

    // None of the none-receiver arguments may be an alias of the receiver.
    List<Value> inValues = invoke.inValues();
    for (int i = 1; i < inValues.size(); i++) {
      if (!receivers.addIllegalReceiverAlias(inValues.get(i))) {
        return null;
      }
    }

    // TODO(b/141719453): Should not constrain library overrides if all instantiations are inlined.
    if (singleTarget.isLibraryMethodOverride().isTrue()) {
      InliningOracle inliningOracle = defaultOracle.get();
      if (!inliningOracle.passesInliningConstraints(
          invoke, singleTarget, Reason.SIMPLE, NopWhyAreYouNotInliningReporter.getInstance())) {
        return null;
      }
    }
    return isEligibleVirtualMethodCall(
        invoke,
        invoke.getInvokedMethod(),
        singleTarget,
        eligibility -> isEligibleInvokeWithAllUsersAsReceivers(eligibility, invoke, indirectUsers));
  }

  private InliningInfo isEligibleIndirectVirtualMethodCall(DexMethod callee) {
    DexEncodedMethod singleTarget =
        appView.appInfo().resolveMethod(eligibleClassDefinition, callee).getSingleTarget();
    if (isEligibleSingleTarget(singleTarget)) {
      return isEligibleVirtualMethodCall(
          null, callee, singleTarget, eligibility -> eligibility.returnsReceiver.isFalse());
    }
    return null;
  }

  private InliningInfo isEligibleVirtualMethodCall(
      InvokeMethodWithReceiver invoke,
      DexMethod callee,
      DexEncodedMethod singleTarget,
      Predicate<ClassInlinerEligibilityInfo> eligibilityAcceptanceCheck) {
    assert isEligibleSingleTarget(singleTarget);

    // We should not inline a method if the invocation has type interface or virtual and the
    // signature of the invocation resolves to a private or static method.
    ResolutionResult resolutionResult = appView.appInfo().resolveMethod(callee.holder, callee);
    if (resolutionResult.isSingleResolution()
        && !resolutionResult.getSingleTarget().isVirtualMethod()) {
      return null;
    }

    if (!singleTarget.isVirtualMethod()) {
      return null;
    }
    if (method == singleTarget) {
      return null; // Don't inline itself.
    }

    MethodOptimizationInfo optimizationInfo = singleTarget.getOptimizationInfo();
    ClassInlinerEligibilityInfo eligibility = optimizationInfo.getClassInlinerEligibility();
    if (eligibility == null) {
      return null;
    }

    // If the method returns receiver and the return value is actually
    // used in the code we need to make some additional checks.
    if (!eligibilityAcceptanceCheck.test(eligibility)) {
      return null;
    }

    markSizeForInlining(invoke, singleTarget);
    return new InliningInfo(singleTarget, eligibleClass);
  }

  private boolean isExtraMethodCall(InvokeMethod invoke) {
    if (invoke.isInvokeDirect()
        && appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())) {
      return false;
    }
    if (invoke.isInvokeMethodWithReceiver()) {
      Value receiver = invoke.asInvokeMethodWithReceiver().getReceiver();
      if (!receivers.addIllegalReceiverAlias(receiver)) {
        return false;
      }
    }
    if (invoke.isInvokeSuper()) {
      return false;
    }
    if (invoke.isInvokePolymorphic()) {
      return false;
    }
    return true;
  }

  // Analyzes if a method invoke the eligible instance is passed to is eligible. In short,
  // it can be eligible if:
  //
  //   -- eligible instance is passed as argument #N which is not used in the method,
  //      such cases are collected in 'unusedArguments' parameter and later replaced
  //      with 'null' value
  //
  //   -- eligible instance is passed as argument #N which is only used in the method to
  //      call a method on this object (we call it indirect method call), and method is
  //      eligible according to the same rules defined for direct method call eligibility
  //      (except we require the method receiver to not be used in return instruction)
  //
  //   -- eligible instance is used in zero-test 'if' instructions testing if the value
  //      is null/not-null (since we know the instance is not null, those checks can
  //      be rewritten)
  //
  //   -- method itself can be inlined
  //
  private boolean isExtraMethodCallEligible(
      InvokeMethod invoke, DexEncodedMethod singleTarget, Supplier<InliningOracle> defaultOracle) {
    // Don't consider constructor invocations and super calls, since we don't want to forcibly
    // inline them.
    assert isExtraMethodCall(invoke);
    assert isEligibleSingleTarget(singleTarget);

    List<Value> arguments = Lists.newArrayList(invoke.inValues());

    // If we got here with invocation on receiver the user is ineligible.
    if (invoke.isInvokeMethodWithReceiver()) {
      InvokeMethodWithReceiver invokeMethodWithReceiver = invoke.asInvokeMethodWithReceiver();
      Value receiver = invokeMethodWithReceiver.getReceiver();
      if (!receivers.addIllegalReceiverAlias(receiver)) {
        return false;
      }

      // A definitely null receiver will throw an error on call site.
      if (receiver.getTypeLattice().nullability().isDefinitelyNull()) {
        return false;
      }
    }

    MethodOptimizationInfo optimizationInfo = singleTarget.getOptimizationInfo();

    // Go through all arguments, see if all usages of eligibleInstance are good.
    if (!isEligibleParameterUsages(invoke, arguments, singleTarget, defaultOracle)) {
      return false;
    }

    for (int argIndex = 0; argIndex < arguments.size(); argIndex++) {
      Value argument = arguments.get(argIndex).getAliasedValue();
      ParameterUsage parameterUsage = optimizationInfo.getParameterUsages(argIndex);
      if (receivers.isDefiniteReceiverAlias(argument)
          && parameterUsage != null
          && parameterUsage.notUsed()) {
        // Reference can be removed since it's not used.
        unusedArguments.add(new Pair<>(invoke, argIndex));
      }
    }

    extraMethodCalls.put(invoke, new InliningInfo(singleTarget, null));

    // Looks good.
    markSizeForInlining(invoke, singleTarget);
    return true;
  }

  private boolean isEligibleParameterUsages(
      InvokeMethod invoke,
      List<Value> arguments,
      DexEncodedMethod singleTarget,
      Supplier<InliningOracle> defaultOracle) {
    // Go through all arguments, see if all usages of eligibleInstance are good.
    for (int argIndex = 0; argIndex < arguments.size(); argIndex++) {
      MethodOptimizationInfo optimizationInfo = singleTarget.getOptimizationInfo();
      ParameterUsage parameterUsage = optimizationInfo.getParameterUsages(argIndex);

      Value argument = arguments.get(argIndex);
      if (receivers.isReceiverAlias(argument)) {
        // Have parameter usage info?
        if (!isEligibleParameterUsage(parameterUsage, invoke, defaultOracle)) {
          return false;
        }
      } else {
        // Nothing to worry about, unless `argument` becomes an alias of the receiver later.
        receivers.addDeferredAliasValidityCheck(
            argument, () -> isEligibleParameterUsage(parameterUsage, invoke, defaultOracle));
      }
    }
    return true;
  }

  private boolean isEligibleParameterUsage(
      ParameterUsage parameterUsage, InvokeMethod invoke, Supplier<InliningOracle> defaultOracle) {
    if (parameterUsage == null) {
      return false; // Don't know anything.
    }

    if (parameterUsage.notUsed()) {
      return true;
    }

    if (parameterUsage.isAssignedToField) {
      return false;
    }

    if (parameterUsage.isReturned) {
      if (invoke.outValue() != null && invoke.outValue().hasAnyUsers()) {
        // Used as return value which is not ignored.
        return false;
      }
    }

    if (!Sets.difference(parameterUsage.ifZeroTest, ALLOWED_ZERO_TEST_TYPES).isEmpty()) {
      // Used in unsupported zero-check-if kinds.
      return false;
    }

    for (Pair<Type, DexMethod> call : parameterUsage.callsReceiver) {
      Type type = call.getFirst();
      DexMethod target = call.getSecond();

      if (type == Type.VIRTUAL || type == Type.INTERFACE) {
        // Is the method called indirectly still eligible?
        InliningInfo potentialInliningInfo = isEligibleIndirectVirtualMethodCall(target);
        if (potentialInliningInfo == null) {
          return false;
        }
      } else if (type == Type.DIRECT) {
        if (!isInstanceInitializerEligibleForClassInlining(target)) {
          // Only calls to trivial instance initializers are supported at this point.
          return false;
        }
      } else {
        // Static and super calls are not yet supported.
        return false;
      }

      // Check if the method is inline-able by standard inliner.
      DexEncodedMethod singleTarget = invoke.lookupSingleTarget(appView, method.method.holder);
      if (singleTarget == null) {
        return false;
      }

      InliningOracle oracle = defaultOracle.get();
      InlineAction inlineAction =
          oracle.computeInlining(
              invoke,
              singleTarget,
              ClassInitializationAnalysis.trivial(),
              NopWhyAreYouNotInliningReporter.getInstance());
      if (inlineAction == null) {
        return false;
      }
    }
    return true;
  }

  private boolean isInstanceInitializerEligibleForClassInlining(DexMethod method) {
    if (method == appView.dexItemFactory().objectMethods.constructor) {
      return true;
    }
    DexEncodedMethod encodedMethod = appView.definitionFor(method);
    if (encodedMethod == null) {
      return false;
    }
    InstanceInitializerInfo initializerInfo =
        encodedMethod.getOptimizationInfo().getInstanceInitializerInfo();
    return initializerInfo.receiverNeverEscapesOutsideConstructorChain();
  }

  private boolean exemptFromInstructionLimit(DexEncodedMethod inlinee) {
    DexType inlineeHolder = inlinee.method.holder;
    if (isDesugaredLambda && inlineeHolder == eligibleClass) {
      return true;
    }
    if (appView.appInfo().isPinned(inlineeHolder)) {
      return false;
    }
    DexClass inlineeClass = appView.definitionFor(inlineeHolder);
    assert inlineeClass != null;

    KotlinInfo kotlinInfo = inlineeClass.getKotlinInfo();
    return kotlinInfo != null &&
        kotlinInfo.isSyntheticClass() &&
        kotlinInfo.asSyntheticClass().isLambda();
  }

  private void markSizeForInlining(InvokeMethod invoke, DexEncodedMethod inlinee) {
    assert !isProcessedConcurrently.test(inlinee);
    if (!exemptFromInstructionLimit(inlinee)) {
      if (invoke != null) {
        directInlinees.put(invoke, inlinee);
      } else {
        indirectInlinees.add(inlinee);
      }
    }
  }

  private boolean isEligibleSingleTarget(DexEncodedMethod singleTarget) {
    if (singleTarget == null) {
      return false;
    }
    if (!singleTarget.isProgramMethod(appView)) {
      return false;
    }
    if (isProcessedConcurrently.test(singleTarget)) {
      return false;
    }
    if (isDesugaredLambda && !singleTarget.accessFlags.isBridge()) {
      // OK if this is the call to the main method of a desugared lambda (for both direct and
      // indirect calls).
      //
      // Note: This is needed because lambda methods are generally processed in the same batch as
      // they are class inlined, which means that the call to isInliningCandidate() below will
      // return false.
      return true;
    }
    if (!singleTarget.isInliningCandidate(
        method, Reason.SIMPLE, appView.appInfo(), NopWhyAreYouNotInliningReporter.getInstance())) {
      // If `singleTarget` is not an inlining candidate, we won't be able to inline it here.
      //
      // Note that there may be some false negatives here since the method may
      // reference private fields of its class which are supposed to be replaced
      // with arguments after inlining. We should try and improve it later.
      //
      // Using -allowaccessmodification mitigates this.
      return false;
    }
    return true;
  }

  private void removeInstruction(Instruction instruction) {
    instruction.inValues().forEach(v -> v.removeUser(instruction));
    instruction.getBlock().removeInstruction(instruction);
  }

  static class IllegalClassInlinerStateException extends Exception {}
}
