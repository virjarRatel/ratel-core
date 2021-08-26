// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;
import static com.google.common.base.Predicates.not;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AccessFlags;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.NestMemberClassAttribute;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CatchHandlers.CatchHandler;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.MoveException;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.Throw;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.CodeOptimization;
import com.android.tools.r8.ir.conversion.LensCodeRewriter;
import com.android.tools.r8.ir.conversion.MethodProcessor;
import com.android.tools.r8.ir.conversion.PostOptimization;
import com.android.tools.r8.ir.desugar.TwrCloseResourceRewriter;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedbackIgnore;
import com.android.tools.r8.ir.optimize.inliner.InliningIRProvider;
import com.android.tools.r8.ir.optimize.inliner.NopWhyAreYouNotInliningReporter;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import com.android.tools.r8.ir.optimize.lambda.LambdaMerger;
import com.android.tools.r8.kotlin.Kotlin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.MainDexClasses;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.IteratorUtils;
import com.android.tools.r8.utils.ListUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class Inliner implements PostOptimization {

  protected final AppView<AppInfoWithLiveness> appView;
  private final Set<DexMethod> blacklist;
  private final LambdaMerger lambdaMerger;
  private final LensCodeRewriter lensCodeRewriter;
  final MainDexClasses mainDexClasses;

  // State for inlining methods which are known to be called twice.
  private boolean applyDoubleInlining = false;
  private final Set<DexEncodedMethod> doubleInlineCallers = Sets.newIdentityHashSet();
  private final Set<DexEncodedMethod> doubleInlineSelectedTargets = Sets.newIdentityHashSet();
  private final Map<DexEncodedMethod, DexEncodedMethod> doubleInlineeCandidates = new HashMap<>();

  public Inliner(
      AppView<AppInfoWithLiveness> appView,
      MainDexClasses mainDexClasses,
      LambdaMerger lambdaMerger,
      LensCodeRewriter lensCodeRewriter) {
    Kotlin.Intrinsics intrinsics = appView.dexItemFactory().kotlin.intrinsics;
    this.appView = appView;
    this.blacklist =
        appView.options().kotlinOptimizationOptions().disableKotlinSpecificOptimizations
            ? ImmutableSet.of()
            : ImmutableSet.of(intrinsics.throwNpe, intrinsics.throwParameterIsNullException);
    this.lambdaMerger = lambdaMerger;
    this.lensCodeRewriter = lensCodeRewriter;
    this.mainDexClasses = mainDexClasses;
  }

  boolean isBlacklisted(
      DexEncodedMethod encodedMethod, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    DexMethod method = encodedMethod.method;
    if (encodedMethod.getOptimizationInfo().forceInline()
        && appView.appInfo().neverInline.contains(method)) {
      throw new Unreachable();
    }

    if (appView.appInfo().isPinned(method)) {
      whyAreYouNotInliningReporter.reportPinned();
      return true;
    }

    if (blacklist.contains(appView.graphLense().getOriginalMethodSignature(method))
        || TwrCloseResourceRewriter.isSynthesizedCloseResourceMethod(method, appView)) {
      whyAreYouNotInliningReporter.reportBlacklisted();
      return true;
    }

    if (appView.appInfo().neverInline.contains(method)) {
      whyAreYouNotInliningReporter.reportMarkedAsNeverInline();
      return true;
    }

    return false;
  }

  boolean isDoubleInliningEnabled() {
    return applyDoubleInlining;
  }

  private ConstraintWithTarget instructionAllowedForInlining(
      Instruction instruction, InliningConstraints inliningConstraints, DexType invocationContext) {
    ConstraintWithTarget result =
        instruction.inliningConstraint(inliningConstraints, invocationContext);
    if (result == ConstraintWithTarget.NEVER && instruction.isDebugInstruction()) {
      return ConstraintWithTarget.ALWAYS;
    }
    return result;
  }

  public ConstraintWithTarget computeInliningConstraint(IRCode code, DexEncodedMethod method) {
    if (appView.options().canHaveDalvikCatchHandlerVerificationBug()
        && useReflectiveOperationExceptionOrUnknownClassInCatch(code)) {
      return ConstraintWithTarget.NEVER;
    }

    if (appView.options().canHaveDalvikIntUsedAsNonIntPrimitiveTypeBug()
        && returnsIntAsBoolean(code, method)) {
      return ConstraintWithTarget.NEVER;
    }

    ConstraintWithTarget result = ConstraintWithTarget.ALWAYS;
    InliningConstraints inliningConstraints =
        new InliningConstraints(appView, GraphLense.getIdentityLense());
    for (Instruction instruction : code.instructions()) {
      ConstraintWithTarget state =
          instructionAllowedForInlining(instruction, inliningConstraints, method.method.holder);
      if (state == ConstraintWithTarget.NEVER) {
        result = state;
        break;
      }
      // TODO(b/128967328): we may need to collect all meaningful constraints.
      result = ConstraintWithTarget.meet(result, state, appView);
    }
    return result;
  }

  private boolean returnsIntAsBoolean(IRCode code, DexEncodedMethod method) {
    DexType returnType = method.method.proto.returnType;
    for (BasicBlock basicBlock : code.blocks) {
      InstructionIterator instructionIterator = basicBlock.iterator();
      while (instructionIterator.hasNext()) {
        Instruction instruction = instructionIterator.nextUntil(Instruction::isReturn);
        if (instruction != null) {
          if (returnType.isBooleanType() && !instruction.inValues().get(0).knownToBeBoolean()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  boolean hasInliningAccess(DexEncodedMethod method, DexEncodedMethod target) {
    if (!isVisibleWithFlags(target.method.holder, method.method.holder, target.accessFlags)) {
      return false;
    }
    // The class needs also to be visible for us to have access.
    DexClass targetClass = appView.definitionFor(target.method.holder);
    return isVisibleWithFlags(target.method.holder, method.method.holder, targetClass.accessFlags);
  }

  private boolean isVisibleWithFlags(DexType target, DexType context, AccessFlags flags) {
    if (flags.isPublic()) {
      return true;
    }
    if (flags.isPrivate()) {
      return NestUtils.sameNest(target, context, appView);
    }
    if (flags.isProtected()) {
      return appView.appInfo().isSubtype(context, target) || target.isSamePackage(context);
    }
    // package-private
    return target.isSamePackage(context);
  }

  public synchronized boolean isDoubleInlineSelectedTarget(DexEncodedMethod method) {
    return doubleInlineSelectedTargets.contains(method);
  }

  synchronized boolean satisfiesRequirementsForDoubleInlining(
      DexEncodedMethod method, DexEncodedMethod target) {
    if (applyDoubleInlining) {
      // Don't perform the actual inlining if this was not selected.
      return doubleInlineSelectedTargets.contains(target);
    }

    // Just preparing for double inlining.
    recordDoubleInliningCandidate(method, target);
    return false;
  }

  synchronized void recordDoubleInliningCandidate(
      DexEncodedMethod method, DexEncodedMethod target) {
    if (applyDoubleInlining) {
      return;
    }

    if (doubleInlineeCandidates.containsKey(target)) {
      // Both calls can be inlined.
      doubleInlineCallers.add(doubleInlineeCandidates.get(target));
      doubleInlineCallers.add(method);
      doubleInlineSelectedTargets.add(target);
    } else {
      // First call can be inlined.
      doubleInlineeCandidates.put(target, method);
    }
  }

  @Override
  public Set<DexEncodedMethod> methodsToRevisit() {
    applyDoubleInlining = true;
    return doubleInlineCallers;
  }

  @Override
  public Collection<CodeOptimization> codeOptimizationsForPostProcessing() {
    // Run IRConverter#optimize.
    return null;  // Technically same as return converter.getOptimizationForPostIRProcessing();
  }

  /**
   * Encodes the constraints for inlining a method's instructions into a different context.
   * <p>
   * This only takes the instructions into account and not whether a method should be inlined or
   * what reason for inlining it might have. Also, it does not take the visibility of the method
   * itself into account.
   */
  public enum Constraint {
    // The ordinal values are important so please do not reorder.
    // Each constraint includes all constraints <= to it.
    // For example, SAMENEST with class X means:
    // - the target is in the same nest as X, or
    // - the target has the same class as X (SAMECLASS <= SAMENEST).
    // SUBCLASS with class X means:
    // - the target is a subclass of X in different package, or
    // - the target is in the same package (PACKAGE <= SUBCLASS), or
    // ...
    // - the target is the same class as X (SAMECLASS <= SUBCLASS).
    NEVER(1), // Never inline this.
    SAMECLASS(2), // Inlineable into methods in the same holder.
    SAMENEST(4), // Inlineable into methods with same nest.
    PACKAGE(8), // Inlineable into methods with holders from the same package.
    SUBCLASS(16), // Inlineable into methods with holders from a subclass in a different package.
    ALWAYS(32); // No restrictions for inlining this.

    int value;

    Constraint(int value) {
      this.value = value;
    }

    static {
      assert NEVER.ordinal() < SAMECLASS.ordinal();
      assert SAMECLASS.ordinal() < SAMENEST.ordinal();
      assert SAMENEST.ordinal() < PACKAGE.ordinal();
      assert PACKAGE.ordinal() < SUBCLASS.ordinal();
      assert SUBCLASS.ordinal() < ALWAYS.ordinal();
    }

    boolean isSet(int value) {
      return (this.value & value) != 0;
    }
  }

  /**
   * Encodes the constraints for inlining, along with the target holder.
   * <p>
   * Constraint itself cannot determine whether or not the method can be inlined if instructions in
   * the method have different constraints with different targets. For example,
   *   SUBCLASS of x.A v.s. PACKAGE of y.B
   * Without any target holder information, min of those two Constraints is PACKAGE, meaning that
   * the current method can be inlined to any method whose holder is in package y. This could cause
   * an illegal access error due to protect members in x.A. Because of different target holders,
   * those constraints should not be combined.
   * <p>
   * Instead, a right constraint for inlining constraint for the example above is: a method whose
   * holder is a subclass of x.A _and_ in the same package of y.B can inline this method.
   */
  public static class ConstraintWithTarget {
    public final Constraint constraint;
    // Note that this is not context---where this constraint is encoded.
    // It literally refers to the holder type of the target, which could be:
    // invoked method in invocations, field in field instructions, type of check-cast, etc.
    final DexType targetHolder;

    public static final ConstraintWithTarget NEVER = new ConstraintWithTarget(Constraint.NEVER);
    public static final ConstraintWithTarget ALWAYS = new ConstraintWithTarget(Constraint.ALWAYS);

    private ConstraintWithTarget(Constraint constraint) {
      assert constraint == Constraint.NEVER || constraint == Constraint.ALWAYS;
      this.constraint = constraint;
      this.targetHolder = null;
    }

    ConstraintWithTarget(Constraint constraint, DexType targetHolder) {
      assert constraint != Constraint.NEVER && constraint != Constraint.ALWAYS;
      assert targetHolder != null;
      this.constraint = constraint;
      this.targetHolder = targetHolder;
    }

    @Override
    public int hashCode() {
      if (targetHolder == null) {
        return constraint.ordinal();
      }
      return constraint.ordinal() * targetHolder.computeHashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ConstraintWithTarget)) {
        return false;
      }
      ConstraintWithTarget o = (ConstraintWithTarget) other;
      return this.constraint.ordinal() == o.constraint.ordinal()
          && this.targetHolder == o.targetHolder;
    }

    public static ConstraintWithTarget deriveConstraint(
        DexType contextHolder, DexType targetHolder, AccessFlags flags, AppView<?> appView) {
      if (flags.isPublic()) {
        return ALWAYS;
      } else if (flags.isPrivate()) {
        DexClass contextHolderClass = appView.definitionFor(contextHolder);
        assert contextHolderClass != null;
        if (contextHolderClass.isInANest()) {
          return NestUtils.sameNest(contextHolder, targetHolder, appView)
              ? new ConstraintWithTarget(Constraint.SAMENEST, targetHolder)
              : NEVER;
        }
        return targetHolder == contextHolder
            ? new ConstraintWithTarget(Constraint.SAMECLASS, targetHolder) : NEVER;
      } else if (flags.isProtected()) {
        if (targetHolder.isSamePackage(contextHolder)) {
          // Even though protected, this is visible via the same package from the context.
          return new ConstraintWithTarget(Constraint.PACKAGE, targetHolder);
        } else if (appView.isSubtype(contextHolder, targetHolder).isTrue()) {
          return new ConstraintWithTarget(Constraint.SUBCLASS, targetHolder);
        }
        return NEVER;
      } else {
        /* package-private */
        return targetHolder.isSamePackage(contextHolder)
            ? new ConstraintWithTarget(Constraint.PACKAGE, targetHolder) : NEVER;
      }
    }

    public static ConstraintWithTarget classIsVisible(
        DexType context, DexType clazz, AppView<?> appView) {
      if (clazz.isArrayType()) {
        return classIsVisible(context, clazz.toArrayElementType(appView.dexItemFactory()), appView);
      }

      if (clazz.isPrimitiveType()) {
        return ALWAYS;
      }

      DexClass definition = appView.definitionFor(clazz);
      return definition == null
          ? NEVER
          : deriveConstraint(context, clazz, definition.accessFlags, appView);
    }

    public static ConstraintWithTarget meet(
        ConstraintWithTarget one, ConstraintWithTarget other, AppView<?> appView) {
      if (one.equals(other)) {
        return one;
      }
      if (other.constraint.ordinal() < one.constraint.ordinal()) {
        return meet(other, one, appView);
      }
      // From now on, one.constraint.ordinal() <= other.constraint.ordinal()
      if (one == NEVER) {
        return NEVER;
      }
      if (other == ALWAYS) {
        return one;
      }
      int constraint = one.constraint.value | other.constraint.value;
      assert !Constraint.NEVER.isSet(constraint);
      assert !Constraint.ALWAYS.isSet(constraint);
      // SAMECLASS <= SAMECLASS, SAMENEST, PACKAGE, SUBCLASS
      if (Constraint.SAMECLASS.isSet(constraint)) {
        assert one.constraint == Constraint.SAMECLASS;
        if (other.constraint == Constraint.SAMECLASS) {
          assert one.targetHolder != other.targetHolder;
          return NEVER;
        }
        if (other.constraint == Constraint.SAMENEST) {
          if (NestUtils.sameNest(one.targetHolder, other.targetHolder, appView)) {
            return one;
          }
          return NEVER;
        }
        if (other.constraint == Constraint.PACKAGE) {
          if (one.targetHolder.isSamePackage(other.targetHolder)) {
            return one;
          }
          return NEVER;
        }
        assert other.constraint == Constraint.SUBCLASS;
        if (appView.isSubtype(one.targetHolder, other.targetHolder).isTrue()) {
          return one;
        }
        return NEVER;
      }
      // SAMENEST <= SAMENEST, PACKAGE, SUBCLASS
      if (Constraint.SAMENEST.isSet(constraint)) {
        assert one.constraint == Constraint.SAMENEST;
        if (other.constraint == Constraint.SAMENEST) {
          if (NestUtils.sameNest(one.targetHolder, other.targetHolder, appView)) {
            return one;
          }
          return NEVER;
        }
        assert verifyAllNestInSamePackage(one.targetHolder, appView);
        if (other.constraint == Constraint.PACKAGE) {
          if (one.targetHolder.isSamePackage(other.targetHolder)) {
            return one;
          }
          return NEVER;
        }
        assert other.constraint == Constraint.SUBCLASS;
        if (allNestMembersSubtypeOf(one.targetHolder, other.targetHolder, appView)) {
          // Then, SAMENEST is a more restrictive constraint.
          return one;
        }
        return NEVER;
      }
      // PACKAGE <= PACKAGE, SUBCLASS
      if (Constraint.PACKAGE.isSet(constraint)) {
        assert one.constraint == Constraint.PACKAGE;
        if (other.constraint == Constraint.PACKAGE) {
          assert one.targetHolder != other.targetHolder;
          if (one.targetHolder.isSamePackage(other.targetHolder)) {
            return one;
          }
          // PACKAGE of x and PACKAGE of y cannot be satisfied together.
          return NEVER;
        }
        assert other.constraint == Constraint.SUBCLASS;
        if (other.targetHolder.isSamePackage(one.targetHolder)) {
          // Then, PACKAGE is more restrictive constraint.
          return one;
        }
        if (appView.isSubtype(one.targetHolder, other.targetHolder).isTrue()) {
          return new ConstraintWithTarget(Constraint.SAMECLASS, one.targetHolder);
        }
        // TODO(b/128967328): towards finer-grained constraints, we need both.
        // The target method is still inlineable to methods with a holder from the same package of
        // one's holder and a subtype of other's holder.
        return NEVER;
      }
      // SUBCLASS <= SUBCLASS
      assert Constraint.SUBCLASS.isSet(constraint);
      assert one.constraint == other.constraint;
      assert one.targetHolder != other.targetHolder;
      if (appView.isSubtype(one.targetHolder, other.targetHolder).isTrue()) {
        return one;
      }
      if (appView.isSubtype(other.targetHolder, one.targetHolder).isTrue()) {
        return other;
      }
      // SUBCLASS of x and SUBCLASS of y while x and y are not a subtype of each other.
      return NEVER;
    }

    private static boolean allNestMembersSubtypeOf(
        DexType nestType, DexType superType, AppView<?> appView) {
      DexClass dexClass = appView.definitionFor(nestType);
      if (dexClass == null) {
        assert false;
        return false;
      }
      if (!dexClass.isInANest()) {
        return appView.isSubtype(dexClass.type, superType).isTrue();
      }
      DexClass nestHost =
          dexClass.isNestHost() ? dexClass : appView.definitionFor(dexClass.getNestHost());
      if (nestHost == null) {
        assert false;
        return false;
      }
      for (NestMemberClassAttribute member : nestHost.getNestMembersClassAttributes()) {
        if (!appView.isSubtype(member.getNestMember(), superType).isTrue()) {
          return false;
        }
      }
      return true;
    }

    private static boolean verifyAllNestInSamePackage(DexType type, AppView<?> appView) {
      String descr = type.getPackageDescriptor();
      DexClass dexClass = appView.definitionFor(type);
      assert dexClass != null;
      if (!dexClass.isInANest()) {
        return true;
      }
      DexClass nestHost =
          dexClass.isNestHost() ? dexClass : appView.definitionFor(dexClass.getNestHost());
      assert nestHost != null;
      for (NestMemberClassAttribute member : nestHost.getNestMembersClassAttributes()) {
        assert member.getNestMember().getPackageDescriptor().equals(descr);
      }
      return true;
    }
  }

  /**
   * Encodes the reason why a method should be inlined.
   * <p>
   * This is independent of determining whether a method can be inlined, except for the FORCE state,
   * that will inline a method irrespective of visibility and instruction checks.
   */
  public enum Reason {
    FORCE,         // Inlinee is marked for forced inlining (bridge method or renamed constructor).
    ALWAYS,        // Inlinee is marked for inlining due to alwaysinline directive.
    SINGLE_CALLER, // Inlinee has precisely one caller.
    DUAL_CALLER,   // Inlinee has precisely two callers.
    SIMPLE,        // Inlinee has simple code suitable for inlining.
    NEVER;         // Inlinee must not be inlined.

    public boolean mustBeInlined() {
      // TODO(118734615): Include SINGLE_CALLER and DUAL_CALLER here as well?
      return this == FORCE || this == ALWAYS;
    }
  }

  public static class InlineAction {

    public final DexEncodedMethod target;
    public final Invoke invoke;
    final Reason reason;

    private boolean shouldSynthesizeNullCheckForReceiver;

    InlineAction(DexEncodedMethod target, Invoke invoke, Reason reason) {
      this.target = target;
      this.invoke = invoke;
      this.reason = reason;
    }

    void setShouldSynthesizeNullCheckForReceiver() {
      shouldSynthesizeNullCheckForReceiver = true;
    }

    InlineeWithReason buildInliningIR(
        AppView<? extends AppInfoWithSubtyping> appView,
        InvokeMethod invoke,
        DexEncodedMethod context,
        InliningIRProvider inliningIRProvider,
        LambdaMerger lambdaMerger,
        LensCodeRewriter lensCodeRewriter) {
      DexItemFactory dexItemFactory = appView.dexItemFactory();
      InternalOptions options = appView.options();

      // Build the IR for a yet not processed method, and perform minimal IR processing.
      IRCode code = inliningIRProvider.getInliningIR(invoke, target);

      // Insert a null check if this is needed to preserve the implicit null check for the receiver.
      // This is only needed if we do not also insert a monitor-enter instruction, since that will
      // throw a NPE if the receiver is null.
      //
      // Note: When generating DEX, we synthesize monitor-enter/exit instructions during IR
      // building, and therefore, we do not need to do anything here. Upon writing, we will use the
      // flag "declared synchronized" instead of "synchronized".
      boolean shouldSynthesizeMonitorEnterExit =
          target.accessFlags.isSynchronized() && options.isGeneratingClassFiles();
      boolean isSynthesizingNullCheckForReceiverUsingMonitorEnter =
          shouldSynthesizeMonitorEnterExit && !target.isStatic();
      if (shouldSynthesizeNullCheckForReceiver
          && !isSynthesizingNullCheckForReceiverUsingMonitorEnter) {
        List<Value> arguments = code.collectArguments();
        if (!arguments.isEmpty()) {
          Value receiver = arguments.get(0);
          assert receiver.isThis();

          BasicBlock entryBlock = code.entryBlock();

          // Insert a new block between the last argument instruction and the first actual
          // instruction of the method.
          BasicBlock throwBlock =
              entryBlock.listIterator(code, arguments.size()).split(code, 0, null);
          assert !throwBlock.hasCatchHandlers();

          // Link the entry block to the successor of the newly inserted block.
          BasicBlock continuationBlock = throwBlock.unlinkSingleSuccessor();
          entryBlock.link(continuationBlock);

          // Replace the last instruction of the entry block, which is now a goto instruction,
          // with an `if-eqz` instruction that jumps to the newly inserted block if the receiver
          // is null.
          If ifInstruction = new If(If.Type.EQ, receiver);
          entryBlock.replaceLastInstruction(ifInstruction, code);
          assert ifInstruction.getTrueTarget() == throwBlock;
          assert ifInstruction.fallthroughBlock() == continuationBlock;

          // Replace the single goto instruction in the newly inserted block by `throw null`.
          InstructionListIterator iterator = throwBlock.listIterator(code);
          Value nullValue = iterator.insertConstNullInstruction(code, appView.options());
          iterator.next();
          iterator.replaceCurrentInstruction(new Throw(nullValue));
        } else {
          assert false : "Unable to synthesize a null check for the receiver";
        }
      }

      // Insert monitor-enter and monitor-exit instructions if the method is synchronized.
      if (shouldSynthesizeMonitorEnterExit) {
        TypeLatticeElement throwableType =
            TypeLatticeElement.fromDexType(
                dexItemFactory.throwableType, Nullability.definitelyNotNull(), appView);

        code.prepareBlocksForCatchHandlers();

        int nextBlockNumber = code.getHighestBlockNumber() + 1;

        // Create a block for holding the monitor-exit instruction.
        BasicBlock monitorExitBlock = new BasicBlock();
        monitorExitBlock.setNumber(nextBlockNumber++);

        // For each block in the code that may throw, add a catch-all handler targeting the
        // monitor-exit block.
        List<BasicBlock> moveExceptionBlocks = new ArrayList<>();
        for (BasicBlock block : code.blocks) {
          if (!block.canThrow()) {
            continue;
          }
          if (block.hasCatchHandlers()
              && block.getCatchHandlersWithSuccessorIndexes().hasCatchAll(dexItemFactory)) {
            continue;
          }
          BasicBlock moveExceptionBlock =
              BasicBlock.createGotoBlock(
                  nextBlockNumber++, Position.none(), code.metadata(), monitorExitBlock);
          InstructionListIterator moveExceptionBlockIterator =
              moveExceptionBlock.listIterator(code);
          moveExceptionBlockIterator.setInsertionPosition(Position.syntheticNone());
          moveExceptionBlockIterator.add(
              new MoveException(
                  code.createValue(throwableType), dexItemFactory.throwableType, options));
          block.appendCatchHandler(moveExceptionBlock, dexItemFactory.throwableType);
          moveExceptionBlocks.add(moveExceptionBlock);
        }

        // Create a phi for the exception values such that we can rethrow the exception if needed.
        Value exceptionValue;
        if (moveExceptionBlocks.size() == 1) {
          exceptionValue =
              ListUtils.first(moveExceptionBlocks).getInstructions().getFirst().outValue();
        } else {
          Phi phi = code.createPhi(monitorExitBlock, throwableType);
          List<Value> operands =
              ListUtils.map(
                  moveExceptionBlocks, block -> block.getInstructions().getFirst().outValue());
          phi.addOperands(operands);
          exceptionValue = phi;
        }

        InstructionListIterator monitorExitBlockIterator = monitorExitBlock.listIterator(code);
        monitorExitBlockIterator.setInsertionPosition(Position.syntheticNone());
        monitorExitBlockIterator.add(new Throw(exceptionValue));
        monitorExitBlock.getMutablePredecessors().addAll(moveExceptionBlocks);

        // Insert the newly created blocks.
        code.blocks.addAll(moveExceptionBlocks);
        code.blocks.add(monitorExitBlock);

        // Create a block for holding the monitor-enter instruction. Note that, since this block
        // is created after we attach catch-all handlers to the code, this block will not have any
        // catch handlers.
        BasicBlock entryBlock = code.entryBlock();
        InstructionListIterator entryBlockIterator = entryBlock.listIterator(code);
        entryBlockIterator.nextUntil(not(Instruction::isArgument));
        entryBlockIterator.previous();
        BasicBlock monitorEnterBlock = entryBlockIterator.split(code, 0, null);
        assert !monitorEnterBlock.hasCatchHandlers();

        InstructionListIterator monitorEnterBlockIterator = monitorEnterBlock.listIterator(code);
        monitorEnterBlockIterator.setInsertionPosition(Position.syntheticNone());

        // If this is a static method, then the class object will act as the lock, so we load it
        // using a const-class instruction.
        Value lockValue;
        if (target.isStatic()) {
          lockValue =
              code.createValue(
                  TypeLatticeElement.fromDexType(
                      dexItemFactory.objectType, definitelyNotNull(), appView));
          monitorEnterBlockIterator.add(new ConstClass(lockValue, target.method.holder));
        } else {
          lockValue = entryBlock.getInstructions().getFirst().asArgument().outValue();
        }

        // Insert the monitor-enter and monitor-exit instructions.
        monitorEnterBlockIterator.add(new Monitor(Monitor.Type.ENTER, lockValue));
        monitorExitBlockIterator.previous();
        monitorExitBlockIterator.add(new Monitor(Monitor.Type.EXIT, lockValue));
        monitorExitBlock.close(null);

        for (BasicBlock block : code.blocks) {
          if (block.exit().isReturn()) {
            // Since return instructions are not allowed after a throwing instruction in a block
            // with catch handlers, the call to prepareBlocksForCatchHandlers() has already taken
            // care of ensuring that all return blocks have no throwing instructions.
            assert !block.canThrow();

            InstructionListIterator instructionIterator =
                block.listIterator(code, block.getInstructions().size() - 1);
            instructionIterator.setInsertionPosition(Position.syntheticNone());
            instructionIterator.add(new Monitor(Monitor.Type.EXIT, lockValue));
          }
        }
      }

      if (!target.isProcessed()) {
        lensCodeRewriter.rewrite(code, target);
      }
      if (lambdaMerger != null) {
        lambdaMerger.rewriteCodeForInlining(target, code, context);
      }
      assert code.isConsistentSSA();
      return new InlineeWithReason(code, reason);
    }
  }

  static class InlineeWithReason {

    final Reason reason;
    final IRCode code;

    InlineeWithReason(IRCode code, Reason reason) {
      this.code = code;
      this.reason = reason;
    }
  }

  static int numberOfInstructions(IRCode code) {
    int numberOfInstructions = 0;
    for (BasicBlock block : code.blocks) {
      for (Instruction instruction : block.getInstructions()) {
        assert !instruction.isDebugInstruction();

        // Do not include argument instructions since they do not materialize in the output.
        if (instruction.isArgument()) {
          continue;
        }

        // Do not include assume instructions in the calculation of the inlining budget, since they
        // do not materialize in the output.
        if (instruction.isAssume()) {
          continue;
        }

        // Do not include goto instructions that target a basic block with exactly one predecessor,
        // since these goto instructions will generally not materialize.
        if (instruction.isGoto()) {
          if (instruction.asGoto().getTarget().getPredecessors().size() == 1) {
            continue;
          }
        }

        // Do not include return instructions since they do not materialize once inlined.
        if (instruction.isReturn()) {
          continue;
        }

        ++numberOfInstructions;
      }
    }
    return numberOfInstructions;
  }

  public static class InliningInfo {
    public final DexEncodedMethod target;
    public final DexType receiverType; // null, if unknown

    public InliningInfo(DexEncodedMethod target, DexType receiverType) {
      this.target = target;
      this.receiverType = receiverType;
    }
  }

  public void performForcedInlining(
      DexEncodedMethod method,
      IRCode code,
      Map<? extends InvokeMethod, InliningInfo> invokesToInline) {
    performForcedInlining(
        method, code, invokesToInline, new InliningIRProvider(appView, method, code));
  }

  public void performForcedInlining(
      DexEncodedMethod method,
      IRCode code,
      Map<? extends InvokeMethod, InliningInfo> invokesToInline,
      InliningIRProvider inliningIRProvider) {
    ForcedInliningOracle oracle = new ForcedInliningOracle(appView, method, invokesToInline);
    performInliningImpl(
        oracle, oracle, method, code, OptimizationFeedbackIgnore.getInstance(), inliningIRProvider);
  }

  public void performInlining(
      DexEncodedMethod method,
      IRCode code,
      OptimizationFeedback feedback,
      MethodProcessor methodProcessor) {
    InternalOptions options = appView.options();
    DefaultInliningOracle oracle =
        createDefaultOracle(
            method,
            code,
            methodProcessor,
            options.inliningInstructionLimit,
            options.inliningInstructionAllowance - numberOfInstructions(code));
    InliningIRProvider inliningIRProvider = new InliningIRProvider(appView, method, code);
    assert inliningIRProvider.verifyIRCacheIsEmpty();
    performInliningImpl(oracle, oracle, method, code, feedback, inliningIRProvider);
  }

  public DefaultInliningOracle createDefaultOracle(
      DexEncodedMethod method,
      IRCode code,
      MethodProcessor methodProcessor,
      int inliningInstructionLimit,
      int inliningInstructionAllowance) {
    return new DefaultInliningOracle(
        appView,
        this,
        method,
        code,
        methodProcessor,
        inliningInstructionLimit,
        inliningInstructionAllowance);
  }

  private void performInliningImpl(
      InliningStrategy strategy,
      InliningOracle oracle,
      DexEncodedMethod context,
      IRCode code,
      OptimizationFeedback feedback,
      InliningIRProvider inliningIRProvider) {
    AssumeDynamicTypeRemover assumeDynamicTypeRemover = new AssumeDynamicTypeRemover(appView, code);
    Set<BasicBlock> blocksToRemove = Sets.newIdentityHashSet();
    ListIterator<BasicBlock> blockIterator = code.listIterator();
    ClassInitializationAnalysis classInitializationAnalysis =
        new ClassInitializationAnalysis(appView, code);
    Deque<BasicBlock> inlineeStack = new ArrayDeque<>();
    InternalOptions options = appView.options();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (!inlineeStack.isEmpty() && inlineeStack.peekFirst() == block) {
        inlineeStack.pop();
      }
      if (blocksToRemove.contains(block)) {
        continue;
      }
      InstructionListIterator iterator = block.listIterator(code);
      while (iterator.hasNext()) {
        Instruction current = iterator.next();
        if (current.isInvokeMethod()) {
          InvokeMethod invoke = current.asInvokeMethod();
          // TODO(b/142116551): This should be equivalent to invoke.lookupSingleTarget()!
          DexEncodedMethod singleTarget = oracle.lookupSingleTarget(invoke, context.method.holder);
          if (singleTarget == null) {
            WhyAreYouNotInliningReporter.handleInvokeWithUnknownTarget(invoke, appView, context);
            continue;
          }

          WhyAreYouNotInliningReporter whyAreYouNotInliningReporter =
              oracle.isForcedInliningOracle()
                  ? NopWhyAreYouNotInliningReporter.getInstance()
                  : WhyAreYouNotInliningReporter.createFor(singleTarget, appView, context);
          InlineAction action =
              oracle.computeInlining(
                  invoke, singleTarget, classInitializationAnalysis, whyAreYouNotInliningReporter);
          if (action == null) {
            assert whyAreYouNotInliningReporter.unsetReasonHasBeenReportedFlag();
            continue;
          }

          if (!inlineeStack.isEmpty()
              && !strategy.allowInliningOfInvokeInInlinee(
                  action, inlineeStack.size(), whyAreYouNotInliningReporter)) {
            assert whyAreYouNotInliningReporter.unsetReasonHasBeenReportedFlag();
            continue;
          }

          if (!strategy.stillHasBudget(action, whyAreYouNotInliningReporter)) {
            assert whyAreYouNotInliningReporter.unsetReasonHasBeenReportedFlag();
            continue;
          }

          InlineeWithReason inlinee =
              action.buildInliningIR(
                  appView, invoke, context, inliningIRProvider, lambdaMerger, lensCodeRewriter);
          if (strategy.willExceedBudget(
              code, invoke, inlinee, block, whyAreYouNotInliningReporter)) {
            assert whyAreYouNotInliningReporter.unsetReasonHasBeenReportedFlag();
            continue;
          }

          // If this code did not go through the full pipeline, apply inlining to make sure
          // that force inline targets get processed.
          strategy.ensureMethodProcessed(singleTarget, inlinee.code, feedback);

          // Make sure constructor inlining is legal.
          assert !singleTarget.isClassInitializer();
          if (singleTarget.isInstanceInitializer()
              && !strategy.canInlineInstanceInitializer(
                  inlinee.code, whyAreYouNotInliningReporter)) {
            assert whyAreYouNotInliningReporter.unsetReasonHasBeenReportedFlag();
            continue;
          }

          // Mark AssumeDynamicType instruction for the out-value for removal, if any.
          Value outValue = invoke.outValue();
          if (outValue != null) {
            assumeDynamicTypeRemover.markUsersForRemoval(outValue);
          }

          boolean inlineeMayHaveInvokeMethod = inlinee.code.metadata().mayHaveInvokeMethod();

          // Inline the inlinee code in place of the invoke instruction
          // Back up before the invoke instruction.
          iterator.previous();
          strategy.markInlined(inlinee);
          iterator.inlineInvoke(
              appView,
              code,
              inlinee.code,
              blockIterator,
              blocksToRemove,
              getDowncastTypeIfNeeded(strategy, invoke, singleTarget));

          if (inlinee.reason == Reason.SINGLE_CALLER) {
            feedback.markInlinedIntoSingleCallSite(singleTarget);
          }

          classInitializationAnalysis.notifyCodeHasChanged();
          strategy.updateTypeInformationIfNeeded(inlinee.code, blockIterator, block);

          // If we inlined the invoke from a bridge method, it is no longer a bridge method.
          if (context.accessFlags.isBridge()) {
            context.accessFlags.unsetSynthetic();
            context.accessFlags.unsetBridge();
          }

          context.copyMetadata(singleTarget);

          if (inlineeMayHaveInvokeMethod && options.applyInliningToInlinee) {
            if (inlineeStack.size() + 1 > options.applyInliningToInlineeMaxDepth
                && appView.appInfo().alwaysInline.isEmpty()
                && appView.appInfo().forceInline.isEmpty()) {
              continue;
            }
            // Record that we will be inside the inlinee until the next block.
            BasicBlock inlineeEnd = IteratorUtils.peekNext(blockIterator);
            inlineeStack.push(inlineeEnd);
            // Move the cursor back to where the first inlinee block was added.
            IteratorUtils.previousUntil(blockIterator, previous -> previous == block);
            blockIterator.next();
          }
        } else if (current.isAssumeDynamicType()) {
          assumeDynamicTypeRemover.removeIfMarked(current.asAssumeDynamicType(), iterator);
        }
      }
    }
    assert inlineeStack.isEmpty();
    assumeDynamicTypeRemover.removeMarkedInstructions(blocksToRemove);
    assumeDynamicTypeRemover.finish();
    classInitializationAnalysis.finish();
    code.removeBlocks(blocksToRemove);
    code.removeAllTrivialPhis();
    assert code.isConsistentSSA();
  }

  private boolean useReflectiveOperationExceptionOrUnknownClassInCatch(IRCode code) {
    for (BasicBlock block : code.blocks) {
      for (CatchHandler<BasicBlock> catchHandler : block.getCatchHandlers()) {
        if (catchHandler.guard == appView.dexItemFactory().reflectiveOperationExceptionType) {
          return true;
        }
        if (appView.definitionFor(catchHandler.guard) == null) {
          return true;
        }
      }
    }
    return false;
  }

  private DexType getDowncastTypeIfNeeded(
      InliningStrategy strategy, InvokeMethod invoke, DexEncodedMethod target) {
    if (invoke.isInvokeMethodWithReceiver()) {
      // If the invoke has a receiver but the actual type of the receiver is different
      // from the computed target holder, inlining requires a downcast of the receiver.
      DexType receiverType = strategy.getReceiverTypeIfKnown(invoke);
      if (receiverType == null) {
        // In case we don't know exact type of the receiver we use declared
        // method holder as a fallback.
        receiverType = invoke.getInvokedMethod().holder;
      }
      if (!appView.appInfo().isSubtype(receiverType, target.method.holder)) {
        return target.method.holder;
      }
    }
    return null;
  }

  public static boolean verifyNoMethodsInlinedDueToSingleCallSite(AppView<?> appView) {
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      for (DexEncodedMethod method : clazz.methods()) {
        assert !method.getOptimizationInfo().hasBeenInlinedIntoSingleCallSite();
      }
    }
    return true;
  }
}
