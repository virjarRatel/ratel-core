// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.ir.optimize.inliner.InlinerUtils.addMonitorEnterValue;
import static com.android.tools.r8.ir.optimize.inliner.InlinerUtils.collectAllMonitorEnterValues;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.proto.ProtoInliningReasonStrategy;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.MethodProcessor;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.InlineeWithReason;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.ir.optimize.inliner.DefaultInliningReasonStrategy;
import com.android.tools.r8.ir.optimize.inliner.InliningReasonStrategy;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.MainDexDirectReferenceTracer;
import com.android.tools.r8.utils.BooleanUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.IteratorUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class DefaultInliningOracle implements InliningOracle, InliningStrategy {

  private final AppView<AppInfoWithLiveness> appView;
  private final Inliner inliner;
  private final DexEncodedMethod method;
  private final IRCode code;
  private final MethodProcessor methodProcessor;
  private final Predicate<DexEncodedMethod> isProcessedConcurrently;
  private final InliningReasonStrategy reasonStrategy;
  private final int inliningInstructionLimit;
  private int instructionAllowance;

  DefaultInliningOracle(
      AppView<AppInfoWithLiveness> appView,
      Inliner inliner,
      DexEncodedMethod method,
      IRCode code,
      MethodProcessor methodProcessor,
      int inliningInstructionLimit,
      int inliningInstructionAllowance) {
    this.appView = appView;
    this.inliner = inliner;
    this.method = method;
    this.code = code;
    this.methodProcessor = methodProcessor;
    this.isProcessedConcurrently = methodProcessor::isProcessedConcurrently;
    this.inliningInstructionLimit = inliningInstructionLimit;
    this.instructionAllowance = inliningInstructionAllowance;

    DefaultInliningReasonStrategy defaultInliningReasonStrategy =
        new DefaultInliningReasonStrategy(
            appView, methodProcessor.getCallSiteInformation(), inliner);
    this.reasonStrategy =
        appView.withGeneratedMessageLiteShrinker(
            ignore -> new ProtoInliningReasonStrategy(appView, defaultInliningReasonStrategy),
            defaultInliningReasonStrategy);
  }

  @Override
  public boolean isForcedInliningOracle() {
    return false;
  }

  private boolean isSingleTargetInvalid(
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (singleTarget == null) {
      throw new Unreachable(
          "Unexpected attempt to inline invoke that does not have a single target");
    }

    if (singleTarget.isClassInitializer()) {
      throw new Unreachable(
          "Unexpected attempt to invoke a class initializer (`"
              + singleTarget.method.toSourceString()
              + "`)");
    }

    if (!singleTarget.hasCode()) {
      whyAreYouNotInliningReporter.reportInlineeDoesNotHaveCode();
      return true;
    }

    DexClass clazz = appView.definitionFor(singleTarget.method.holder);
    if (!clazz.isProgramClass()) {
      if (clazz.isClasspathClass()) {
        whyAreYouNotInliningReporter.reportClasspathMethod();
      } else {
        assert clazz.isLibraryClass();
        whyAreYouNotInliningReporter.reportLibraryMethod();
      }
      return true;
    }

    // Ignore the implicit receiver argument.
    int numberOfArguments =
        invoke.arguments().size() - BooleanUtils.intValue(invoke.isInvokeMethodWithReceiver());
    int arity = singleTarget.method.getArity();
    if (numberOfArguments != arity) {
      whyAreYouNotInliningReporter.reportIncorrectArity(numberOfArguments, arity);
      return true;
    }

    return false;
  }

  private boolean canInlineStaticInvoke(
      InvokeStatic invoke,
      DexEncodedMethod method,
      DexEncodedMethod target,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    // Only proceed with inlining a static invoke if:
    // - the holder for the target is a subtype of the holder for the method,
    // - the target method always triggers class initialization of its holder before any other side
    //   effect (hence preserving class initialization semantics),
    // - the current method has already triggered the holder for the target method to be
    //   initialized, or
    // - there is no non-trivial class initializer.
    DexType targetHolder = target.method.holder;
    if (appView.appInfo().isSubtype(method.method.holder, targetHolder)) {
      return true;
    }
    DexClass clazz = appView.definitionFor(targetHolder);
    assert clazz != null;
    if (target.getOptimizationInfo().triggersClassInitBeforeAnySideEffect()) {
      return true;
    }
    if (!method.isStatic()) {
      boolean targetIsGuaranteedToBeInitialized =
          appView.withInitializedClassesInInstanceMethods(
              analysis ->
                  analysis.isClassDefinitelyLoadedInInstanceMethodsOn(
                      target.method.holder, method.method.holder),
              false);
      if (targetIsGuaranteedToBeInitialized) {
        return true;
      }
    }
    if (classInitializationAnalysis.isClassDefinitelyLoadedBeforeInstruction(
        target.method.holder, invoke)) {
      return true;
    }
    // Check for class initializer side effects when loading this class, as inlining might remove
    // the load operation.
    //
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-5.html#jvms-5.5.
    //
    // For simplicity, we are conservative and consider all interfaces, not only the ones with
    // default methods.
    if (!clazz.classInitializationMayHaveSideEffects(appView)) {
      return true;
    }

    if (appView.rootSet().bypassClinitForInlining.contains(target.method)) {
      return true;
    }

    whyAreYouNotInliningReporter.reportMustTriggerClassInitialization();
    return false;
  }

  @Override
  public boolean passesInliningConstraints(
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      Reason reason,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (singleTarget.getOptimizationInfo().neverInline()) {
      whyAreYouNotInliningReporter.reportMarkedAsNeverInline();
      return false;
    }

    // We don't inline into constructors when producing class files since this can mess up
    // the stackmap, see b/136250031
    if (method.isInstanceInitializer()
        && appView.options().isGeneratingClassFiles()
        && reason != Reason.FORCE) {
      whyAreYouNotInliningReporter.reportNoInliningIntoConstructorsWhenGeneratingClassFiles();
      return false;
    }

    if (method == singleTarget) {
      // Cannot handle recursive inlining at this point.
      // Force inlined method should never be recursive.
      assert !singleTarget.getOptimizationInfo().forceInline();
      whyAreYouNotInliningReporter.reportRecursiveMethod();
      return false;
    }

    // We should never even try to inline something that is processed concurrently. It can lead
    // to non-deterministic behaviour as the inlining IR could be built from either original output
    // or optimized code. Right now this happens for the class class staticizer, as it just
    // processes all relevant methods in parallel with the full optimization pipeline enabled.
    // TODO(sgjesse): Add this assert "assert !isProcessedConcurrently.test(candidate);"
    if (reason != Reason.FORCE && isProcessedConcurrently.test(singleTarget)) {
      whyAreYouNotInliningReporter.reportProcessedConcurrently();
      return false;
    }

    InternalOptions options = appView.options();
    if (options.featureSplitConfiguration != null
        && !options.featureSplitConfiguration.inSameFeatureOrBase(
            singleTarget.method, method.method)) {
      whyAreYouNotInliningReporter.reportInliningAcrossFeatureSplit();
      return false;
    }

    Set<Reason> validInliningReasons = options.testing.validInliningReasons;
    if (validInliningReasons != null && !validInliningReasons.contains(reason)) {
      whyAreYouNotInliningReporter.reportInvalidInliningReason(reason, validInliningReasons);
      return false;
    }

    // Abort inlining attempt if method -> target access is not right.
    if (!inliner.hasInliningAccess(method, singleTarget)) {
      whyAreYouNotInliningReporter.reportInaccessible();
      return false;
    }

    if (reason == Reason.DUAL_CALLER) {
      if (satisfiesRequirementsForSimpleInlining(invoke, singleTarget)) {
        // When we have a method with two call sites, we simply inline the method as we normally do
        // when the method is small. We still need to ensure that the other call site is also
        // inlined, though. Therefore, we record here that we have seen one of the two call sites
        // as we normally do.
        inliner.recordDoubleInliningCandidate(method, singleTarget);
      } else if (inliner.isDoubleInliningEnabled()) {
        if (!inliner.satisfiesRequirementsForDoubleInlining(method, singleTarget)) {
          whyAreYouNotInliningReporter.reportInvalidDoubleInliningCandidate();
          return false;
        }
      } else {
        // TODO(b/142300882): Should in principle disallow inlining in this case.
        inliner.recordDoubleInliningCandidate(method, singleTarget);
      }
    } else if (reason == Reason.SIMPLE
        && !satisfiesRequirementsForSimpleInlining(invoke, singleTarget)) {
      whyAreYouNotInliningReporter.reportInlineeNotSimple();
      return false;
    }

    // Don't inline code with references beyond root main dex classes into a root main dex class.
    // If we do this it can increase the size of the main dex dependent classes.
    if (reason != Reason.FORCE
        && inlineeRefersToClassesNotInMainDex(method.method.holder, singleTarget)) {
      whyAreYouNotInliningReporter.reportInlineeRefersToClassesNotInMainDex();
      return false;
    }
    assert reason != Reason.FORCE
        || !inlineeRefersToClassesNotInMainDex(method.method.holder, singleTarget);
    return true;
  }

  private boolean inlineeRefersToClassesNotInMainDex(DexType holder, DexEncodedMethod target) {
    if (inliner.mainDexClasses.isEmpty() || !inliner.mainDexClasses.getRoots().contains(holder)) {
      return false;
    }
    return MainDexDirectReferenceTracer.hasReferencesOutsideFromCode(
        appView.appInfo(), target, inliner.mainDexClasses.getRoots());
  }

  private boolean satisfiesRequirementsForSimpleInlining(
      InvokeMethod invoke, DexEncodedMethod target) {
    // If we are looking for a simple method, only inline if actually simple.
    Code code = target.getCode();
    int instructionLimit = computeInstructionLimit(invoke, target);
    if (code.estimatedSizeForInliningAtMost(instructionLimit)) {
      return true;
    }
    return false;
  }

  private int computeInstructionLimit(InvokeMethod invoke, DexEncodedMethod candidate) {
    int instructionLimit = inliningInstructionLimit;
    BitSet hints = candidate.getOptimizationInfo().getNonNullParamOrThrow();
    if (hints != null) {
      List<Value> arguments = invoke.inValues();
      if (invoke.isInvokeMethodWithReceiver()) {
        arguments = arguments.subList(1, arguments.size());
      }
      for (int index = 0; index < arguments.size(); index++) {
        Value argument = arguments.get(index);
        if ((argument.isArgument()
            || (argument.getTypeLattice().isReference() && argument.isNeverNull()))
            && hints.get(index)) {
          // 5-4 instructions per parameter check are expected to be removed.
          instructionLimit += 4;
        }
      }
    }
    return instructionLimit;
  }

  @Override
  public DexEncodedMethod lookupSingleTarget(InvokeMethod invoke, DexType context) {
    return invoke.lookupSingleTarget(appView, context);
  }

  @Override
  public InlineAction computeInlining(
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (isSingleTargetInvalid(invoke, singleTarget, whyAreYouNotInliningReporter)) {
      return null;
    }

    if (inliner.isBlacklisted(singleTarget, whyAreYouNotInliningReporter)) {
      return null;
    }

    Reason reason = reasonStrategy.computeInliningReason(invoke, singleTarget);
    if (reason == Reason.NEVER) {
      return null;
    }

    if (!singleTarget.isInliningCandidate(
        method, reason, appView.appInfo(), whyAreYouNotInliningReporter)) {
      return null;
    }

    if (!passesInliningConstraints(invoke, singleTarget, reason, whyAreYouNotInliningReporter)) {
      return null;
    }

    return invoke.computeInlining(
        singleTarget, reason, this, classInitializationAnalysis, whyAreYouNotInliningReporter);
  }

  public InlineAction computeForInvokeWithReceiver(
      InvokeMethodWithReceiver invoke,
      DexEncodedMethod singleTarget,
      Reason reason,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    Value receiver = invoke.getReceiver();
    if (receiver.getTypeLattice().isDefinitelyNull()) {
      // A definitely null receiver will throw an error on call site.
      whyAreYouNotInliningReporter.reportReceiverDefinitelyNull();
      return null;
    }

    InlineAction action = new InlineAction(singleTarget, invoke, reason);
    if (receiver.getTypeLattice().isNullable()) {
      assert !receiver.getTypeLattice().isDefinitelyNull();
      // When inlining an instance method call, we need to preserve the null check for the
      // receiver. Therefore, if the receiver may be null and the candidate inlinee does not
      // throw if the receiver is null before any other side effect, then we must synthesize a
      // null check.
      if (!singleTarget.getOptimizationInfo().checksNullReceiverBeforeAnySideEffect()) {
        InternalOptions options = appView.options();
        if (!options.enableInliningOfInvokesWithNullableReceivers) {
          whyAreYouNotInliningReporter.reportReceiverMaybeNull();
          return null;
        }
        action.setShouldSynthesizeNullCheckForReceiver();
      }
    }
    return action;
  }

  public InlineAction computeForInvokeStatic(
      InvokeStatic invoke,
      DexEncodedMethod singleTarget,
      Reason reason,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    // Abort inlining attempt if we can not guarantee class for static target has been initialized.
    if (!canInlineStaticInvoke(
        invoke, method, singleTarget, classInitializationAnalysis, whyAreYouNotInliningReporter)) {
      return null;
    }
    return new InlineAction(singleTarget, invoke, reason);
  }

  @Override
  public void ensureMethodProcessed(
      DexEncodedMethod target, IRCode inlinee, OptimizationFeedback feedback) {
    if (!target.isProcessed()) {
      if (Log.ENABLED) {
        Log.verbose(getClass(), "Forcing extra inline on " + target.toSourceString());
      }
      inliner.performInlining(target, inlinee, feedback, methodProcessor);
    }
  }

  @Override
  public boolean allowInliningOfInvokeInInlinee(
      InlineAction action,
      int inliningDepth,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    assert inliningDepth > 0;

    if (action.reason.mustBeInlined()) {
      return true;
    }

    int threshold = appView.options().applyInliningToInlineeMaxDepth;
    if (inliningDepth <= threshold) {
      return true;
    }

    whyAreYouNotInliningReporter.reportWillExceedMaxInliningDepth(inliningDepth, threshold);
    return false;
  }

  @Override
  public boolean canInlineInstanceInitializer(
      IRCode inlinee, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    // In the Java VM Specification section "4.10.2.4. Instance Initialization Methods and
    // Newly Created Objects" it says:
    //
    // Before that method invokes another instance initialization method of myClass or its direct
    // superclass on this, the only operation the method can perform on this is assigning fields
    // declared within myClass.

    // Allow inlining a constructor into a constructor of the same class, as the constructor code
    // is expected to adhere to the VM specification.
    DexType callerMethodHolder = method.method.holder;
    DexType calleeMethodHolder = inlinee.method.method.holder;
    // Calling a constructor on the same class from a constructor can always be inlined.
    if (method.isInstanceInitializer() && callerMethodHolder == calleeMethodHolder) {
      return true;
    }

    // Only allow inlining a constructor into a non-constructor if:
    // (1) the first use of the uninitialized object is the receiver of an invoke of <init>(),
    // (2) the constructor does not initialize any final fields, as such is only allowed from within
    //     a constructor of the corresponding class, and
    // (3) the constructors own <init>() call is on the same class.
    //
    // Note that, due to (3), we do allow inlining of `A(int x)` into another class, but not the
    // default constructor `A()`, since the default constructor invokes Object.<init>().
    //
    //   class A {
    //     A() { ... }
    //     A(int x) {
    //       this()
    //       ...
    //     }
    //   }
    Value thisValue = inlinee.entryBlock().entry().asArgument().outValue();

    List<InvokeDirect> initCallsOnThis = new ArrayList<>();
    for (Instruction instruction : inlinee.instructions()) {
      if (instruction.isInvokeDirect()) {
        InvokeDirect initCall = instruction.asInvokeDirect();
        DexMethod invokedMethod = initCall.getInvokedMethod();
        if (appView.dexItemFactory().isConstructor(invokedMethod)) {
          Value receiver = initCall.getReceiver().getAliasedValue();
          if (receiver == thisValue) {
            // The <init>() call of the constructor must be on the same class.
            if (calleeMethodHolder != invokedMethod.holder) {
              whyAreYouNotInliningReporter
                  .reportUnsafeConstructorInliningDueToIndirectConstructorCall(initCall);
              return false;
            }
            initCallsOnThis.add(initCall);
          }
        }
      } else if (instruction.isInstancePut()) {
        // Final fields may not be initialized outside of a constructor in the enclosing class.
        InstancePut instancePut = instruction.asInstancePut();
        DexField field = instancePut.getField();
        DexEncodedField target = appView.appInfo().lookupInstanceTarget(field.holder, field);
        if (target == null || target.accessFlags.isFinal()) {
          whyAreYouNotInliningReporter.reportUnsafeConstructorInliningDueToFinalFieldAssignment(
              instancePut);
          return false;
        }
      }
    }

    // Check that there are no uses of the uninitialized object before it gets initialized.
    int markingColor = inlinee.reserveMarkingColor();
    for (InvokeDirect initCallOnThis : initCallsOnThis) {
      BasicBlock block = initCallOnThis.getBlock();
      for (Instruction instruction : block.instructionsBefore(initCallOnThis)) {
        for (Value inValue : instruction.inValues()) {
          Value root = inValue.getAliasedValue();
          if (root == thisValue) {
            inlinee.returnMarkingColor(markingColor);
            whyAreYouNotInliningReporter.reportUnsafeConstructorInliningDueToUninitializedObjectUse(
                instruction);
            return false;
          }
        }
      }
      for (BasicBlock predecessor : block.getPredecessors()) {
        inlinee.markTransitivePredecessors(predecessor, markingColor);
      }
    }

    for (BasicBlock block : inlinee.blocks) {
      if (block.isMarked(markingColor)) {
        for (Instruction instruction : block.getInstructions()) {
          for (Value inValue : instruction.inValues()) {
            Value root = inValue.getAliasedValue();
            if (root == thisValue) {
              inlinee.returnMarkingColor(markingColor);
              whyAreYouNotInliningReporter
                  .reportUnsafeConstructorInliningDueToUninitializedObjectUse(instruction);
              return false;
            }
          }
        }
      }
    }

    inlinee.returnMarkingColor(markingColor);
    return true;
  }

  @Override
  public boolean stillHasBudget(
      InlineAction action, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (action.reason.mustBeInlined()) {
      return true;
    }
    boolean stillHasBudget = instructionAllowance > 0;
    if (!stillHasBudget) {
      whyAreYouNotInliningReporter.reportInstructionBudgetIsExceeded();
    }
    return stillHasBudget;
  }

  @Override
  public boolean willExceedBudget(
      IRCode code,
      InvokeMethod invoke,
      InlineeWithReason inlinee,
      BasicBlock block,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (inlinee.reason.mustBeInlined()) {
      return false;
    }
    return willExceedInstructionBudget(inlinee, whyAreYouNotInliningReporter)
        || willExceedMonitorEnterValuesBudget(code, invoke, inlinee, whyAreYouNotInliningReporter)
        || willExceedControlFlowResolutionBlocksBudget(
            inlinee, block, whyAreYouNotInliningReporter);
  }

  private boolean willExceedInstructionBudget(
      InlineeWithReason inlinee, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    int numberOfInstructions = Inliner.numberOfInstructions(inlinee.code);
    if (instructionAllowance < Inliner.numberOfInstructions(inlinee.code)) {
      whyAreYouNotInliningReporter.reportWillExceedInstructionBudget(
          numberOfInstructions, instructionAllowance);
      return true;
    }
    return false;
  }

  /**
   * If inlining would lead to additional lock values in the caller, then check that the number of
   * lock values after inlining would not exceed the threshold.
   *
   * <p>The motivation for limiting the number of locks in a given method is that the register
   * allocator will attempt to pin a register for each lock value. Thus, if a method has many locks,
   * many registers will be pinned, which will lead to high register pressure.
   */
  private boolean willExceedMonitorEnterValuesBudget(
      IRCode code,
      InvokeMethod invoke,
      InlineeWithReason inlinee,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (!code.metadata().mayHaveMonitorInstruction()) {
      return false;
    }

    if (!inlinee.code.metadata().mayHaveMonitorInstruction()) {
      return false;
    }

    Set<DexType> constantMonitorEnterValues = Sets.newIdentityHashSet();
    Set<Value> nonConstantMonitorEnterValues = Sets.newIdentityHashSet();
    collectAllMonitorEnterValues(code, constantMonitorEnterValues, nonConstantMonitorEnterValues);
    if (constantMonitorEnterValues.isEmpty() && nonConstantMonitorEnterValues.isEmpty()) {
      return false;
    }

    for (Monitor monitor : inlinee.code.<Monitor>instructions(Instruction::isMonitorEnter)) {
      Value monitorEnterValue = monitor.object().getAliasedValue();
      if (monitorEnterValue.isArgument()) {
        monitorEnterValue =
            invoke
                .arguments()
                .get(monitorEnterValue.computeArgumentPosition(inlinee.code))
                .getAliasedValue();
      }
      addMonitorEnterValue(
          monitorEnterValue, constantMonitorEnterValues, nonConstantMonitorEnterValues);
    }

    int numberOfMonitorEnterValuesAfterInlining =
        constantMonitorEnterValues.size() + nonConstantMonitorEnterValues.size();
    int threshold = appView.options().inliningMonitorEnterValuesAllowance;
    if (numberOfMonitorEnterValuesAfterInlining > threshold) {
      whyAreYouNotInliningReporter.reportWillExceedMonitorEnterValuesBudget(
          numberOfMonitorEnterValuesAfterInlining, threshold);
      return true;
    }

    return false;
  }

  /**
   * Inlining could lead to an explosion of move-exception and resolution moves. As an example,
   * consider the following piece of code.
   *
   * <pre>
   *   try {
   *     ...
   *     foo();
   *     ...
   *   } catch (A e) { ... }
   *   } catch (B e) { ... }
   *   } catch (C e) { ... }
   * </pre>
   *
   * <p>The generated code for the above example will have a move-exception instruction for each of
   * the three catch handlers. Furthermore, the blocks with these move-exception instructions may
   * require a number of resolution moves to setup the register state for the catch handlers. When
   * inlining foo(), the generated code will have a move-exception instruction *for each of the
   * instructions in foo() that can throw*, along with the necessary resolution moves for each
   * exception-edge. We therefore abort inlining if the number of exception-edges explode.
   */
  private boolean willExceedControlFlowResolutionBlocksBudget(
      InlineeWithReason inlinee,
      BasicBlock block,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    if (!block.hasCatchHandlers()) {
      return false;
    }
    int numberOfThrowingInstructionsInInlinee = 0;
    for (BasicBlock inlineeBlock : inlinee.code.blocks) {
      numberOfThrowingInstructionsInInlinee += inlineeBlock.numberOfThrowingInstructions();
    }
    // Estimate the number of "control flow resolution blocks", where we will insert a
    // move-exception instruction (if needed), along with all the resolution moves that
    // will be needed to setup the register state for the catch handler.
    int estimatedNumberOfControlFlowResolutionBlocks =
        numberOfThrowingInstructionsInInlinee * block.numberOfCatchHandlers();
    // Abort if inlining could lead to an explosion in the number of control flow
    // resolution blocks that setup the register state before the actual catch handler.
    int threshold = appView.options().inliningControlFlowResolutionBlocksThreshold;
    if (estimatedNumberOfControlFlowResolutionBlocks >= threshold) {
      whyAreYouNotInliningReporter.reportPotentialExplosionInExceptionalControlFlowResolutionBlocks(
          estimatedNumberOfControlFlowResolutionBlocks, threshold);
      return true;
    }
    return false;
  }

  @Override
  public void markInlined(InlineeWithReason inlinee) {
    // TODO(118734615): All inlining use from the budget - should that only be SIMPLE?
    instructionAllowance -= Inliner.numberOfInstructions(inlinee.code);
  }

  @Override
  public void updateTypeInformationIfNeeded(
      IRCode inlinee, ListIterator<BasicBlock> blockIterator, BasicBlock block) {
    boolean assumersEnabled =
        appView.options().enableNonNullTracking
            || appView.options().enableDynamicTypeOptimization
            || appView.options().testing.forceAssumeNoneInsertion;
    if (assumersEnabled) {
      BasicBlock state = IteratorUtils.peekNext(blockIterator);

      Set<BasicBlock> inlineeBlocks = Sets.newIdentityHashSet();
      inlineeBlocks.addAll(inlinee.blocks);

      // Introduce aliases only to the inlinee blocks.
      if (appView.options().testing.forceAssumeNoneInsertion) {
        insertAssumeInstructionsToInlinee(
            new AliasIntroducer(appView), code, block, blockIterator, inlineeBlocks);
      }

      // Add non-null IRs only to the inlinee blocks.
      if (appView.options().enableNonNullTracking) {
        Consumer<BasicBlock> splitBlockConsumer = inlineeBlocks::add;
        Assumer nonNullTracker = new NonNullTracker(appView, splitBlockConsumer);
        insertAssumeInstructionsToInlinee(
            nonNullTracker, code, block, blockIterator, inlineeBlocks);
      }

      // Add dynamic type assumptions only to the inlinee blocks.
      if (appView.options().enableDynamicTypeOptimization) {
        insertAssumeInstructionsToInlinee(
            new DynamicTypeOptimization(appView), code, block, blockIterator, inlineeBlocks);
      }

      // Restore the old state of the iterator.
      while (blockIterator.hasPrevious() && blockIterator.previous() != state) {
        // Do nothing.
      }
      assert IteratorUtils.peekNext(blockIterator) == state;
    }
    // TODO(b/72693244): need a test where refined env in inlinee affects the caller.
  }

  private void insertAssumeInstructionsToInlinee(
      Assumer assumer,
      IRCode code,
      BasicBlock block,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> inlineeBlocks) {
    // Move the cursor back to where the first inlinee block was added.
    while (blockIterator.hasPrevious() && blockIterator.previous() != block) {
      // Do nothing.
    }
    assert IteratorUtils.peekNext(blockIterator) == block;

    assumer.insertAssumeInstructionsInBlocks(code, blockIterator, inlineeBlocks::contains);
    assert !blockIterator.hasNext();
  }

  @Override
  public DexType getReceiverTypeIfKnown(InvokeMethod invoke) {
    return null; // Maybe improve later.
  }
}
