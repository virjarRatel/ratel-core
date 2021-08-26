// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.code.InvokeStaticRange;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.DefaultInliningOracle;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.List;
import java.util.function.Predicate;

public class InvokeStatic extends InvokeMethod {

  private final boolean itf;

  public InvokeStatic(DexMethod target, Value result, List<Value> arguments) {
    this(target, result, arguments, false);
    assert target.asDexReference().asDexMethod().proto.parameters.size() == arguments.size();
  }

  public InvokeStatic(DexMethod target, Value result, List<Value> arguments, boolean itf) {
    super(target, result, arguments);
    this.itf = itf;
  }

  @Override
  public int opcode() {
    return Opcodes.INVOKE_STATIC;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public Type getType() {
    return Type.STATIC;
  }

  @Override
  protected String getTypeString() {
    return "Static";
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new InvokeStaticRange(firstRegister, argumentRegisters, getInvokedMethod());
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new com.android.tools.r8.code.InvokeStatic(
          argumentRegistersCount,
          getInvokedMethod(),
          individualArgumentRegisters[0],  // C
          individualArgumentRegisters[1],  // D
          individualArgumentRegisters[2],  // E
          individualArgumentRegisters[3],  // F
          individualArgumentRegisters[4]); // G
    }
    addInvokeAndMoveResult(instruction, builder);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isInvokeStatic() && super.identicalNonValueNonPositionParts(other);
  }

  @Override
  public boolean isInvokeStatic() {
    return true;
  }

  @Override
  public InvokeStatic asInvokeStatic() {
    return this;
  }

  @Override
  public DexEncodedMethod lookupSingleTarget(AppView<?> appView, DexType invocationContext) {
    DexMethod invokedMethod = getInvokedMethod();
    if (appView.appInfo().hasLiveness()) {
      AppInfoWithLiveness appInfo = appView.appInfo().withLiveness();
      return appInfo.lookupStaticTarget(invokedMethod);
    }
    // In D8, we can treat invoke-static instructions as having a single target if the invoke is
    // targeting a method in the enclosing class.
    if (invokedMethod.holder == invocationContext) {
      DexClass clazz = appView.definitionFor(invokedMethod.holder);
      if (clazz != null && clazz.isProgramClass()) {
        DexEncodedMethod singleTarget = clazz.lookupDirectMethod(invokedMethod);
        if (singleTarget.isStatic()) {
          return singleTarget;
        }
      } else {
        assert false;
      }
    }
    return null;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInvokeStatic(getInvokedMethod(), invocationContext);
  }

  @Override
  public InlineAction computeInlining(
      DexEncodedMethod singleTarget,
      Reason reason,
      DefaultInliningOracle decider,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    return decider.computeForInvokeStatic(
        this, singleTarget, reason, classInitializationAnalysis, whyAreYouNotInliningReporter);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfInvoke(org.objectweb.asm.Opcodes.INVOKESTATIC, getInvokedMethod(), itf));
  }

  @Override
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forInvokeStatic(
        this, clazz, appView, mode, assumption);
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    if (!appView.enableWholeProgramOptimizations()) {
      return true;
    }

    if (appView.options().debug) {
      return true;
    }

    // Check if it is a call to one of library methods that are known to be side-effect free.
    Predicate<InvokeMethod> noSideEffectsPredicate =
        appView.dexItemFactory().libraryMethodsWithoutSideEffects.get(getInvokedMethod());
    if (noSideEffectsPredicate != null && noSideEffectsPredicate.test(this)) {
      return false;
    }

    // Find the target and check if the invoke may have side effects.
    if (appView.appInfo().hasLiveness()) {
      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      DexEncodedMethod target = lookupSingleTarget(appViewWithLiveness, context);
      if (target == null) {
        return true;
      }

      // Verify that the target method is accessible in the current context.
      if (!isMemberVisibleFromOriginalContext(
          appView, context, target.method.holder, target.accessFlags)) {
        return true;
      }

      // Verify that the target method does not have side-effects.
      boolean targetMayHaveSideEffects;
      if (appViewWithLiveness.appInfo().noSideEffects.containsKey(target.method)) {
        targetMayHaveSideEffects = false;
      } else {
        targetMayHaveSideEffects =
            target.getOptimizationInfo().mayHaveSideEffects()
                // Verify that calling the target method won't lead to class initialization.
                || target.method.holder.classInitializationMayHaveSideEffects(
                    appView,
                    // Types that are a super type of `context` are guaranteed to be initialized
                    // already.
                    type -> appView.isSubtype(context, type).isTrue());
      }

      return targetMayHaveSideEffects;
    }

    return true;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
  }
}
