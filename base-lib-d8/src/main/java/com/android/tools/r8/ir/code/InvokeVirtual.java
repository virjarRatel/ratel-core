// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.code.InvokeVirtualRange;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.List;
import java.util.function.Predicate;

public class InvokeVirtual extends InvokeMethodWithReceiver {

  public InvokeVirtual(DexMethod target, Value result, List<Value> arguments) {
    super(target, result, arguments);
  }

  @Override
  public int opcode() {
    return Opcodes.INVOKE_VIRTUAL;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public Type getType() {
    return Type.VIRTUAL;
  }

  @Override
  protected String getTypeString() {
    return "Virtual";
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new InvokeVirtualRange(firstRegister, argumentRegisters, getInvokedMethod());
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new com.android.tools.r8.code.InvokeVirtual(
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
    return other.isInvokeVirtual() && super.identicalNonValueNonPositionParts(other);
  }

  @Override
  public boolean isInvokeVirtual() {
    return true;
  }

  @Override
  public InvokeVirtual asInvokeVirtual() {
    return this;
  }

  @Override
  public DexEncodedMethod lookupSingleTarget(AppView<?> appView, DexType invocationContext) {
    if (appView.appInfo().hasLiveness()) {
      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      AppInfoWithLiveness appInfo = appViewWithLiveness.appInfo();
      return appInfo.lookupSingleVirtualTarget(
          getInvokedMethod(),
          invocationContext,
          TypeAnalysis.getRefinedReceiverType(appViewWithLiveness, this),
          getReceiver().getDynamicLowerBoundType(appViewWithLiveness));
    }
    return null;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInvokeVirtual(getInvokedMethod(), invocationContext);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfInvoke(org.objectweb.asm.Opcodes.INVOKEVIRTUAL, getInvokedMethod(), false));
  }

  @Override
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forInvokeVirtual(
        this, clazz, context, appView, mode, assumption);
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    if (!appView.enableWholeProgramOptimizations()) {
      return true;
    }

    if (appView.options().debug) {
      return true;
    }

    // Check if it could throw a NullPointerException as a result of the receiver being null.
    Value receiver = getReceiver();
    if (receiver.getTypeLattice().isNullable()) {
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
        targetMayHaveSideEffects = target.getOptimizationInfo().mayHaveSideEffects();
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
