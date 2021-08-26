// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.code.InvokeInterfaceRange;
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

public class InvokeInterface extends InvokeMethodWithReceiver {

  public InvokeInterface(DexMethod target, Value result, List<Value> arguments) {
    super(target, result, arguments);
  }

  @Override
  public int opcode() {
    return Opcodes.INVOKE_INTERFACE;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public Type getType() {
    return Type.INTERFACE;
  }

  @Override
  protected String getTypeString() {
    return "Interface";
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new InvokeInterfaceRange(firstRegister, argumentRegisters, getInvokedMethod());
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new com.android.tools.r8.code.InvokeInterface(
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
    return other.isInvokeInterface() && super.identicalNonValueNonPositionParts(other);
  }

  @Override
  public boolean isInvokeInterface() {
    return true;
  }

  @Override
  public InvokeInterface asInvokeInterface() {
    return this;
  }

  @Override
  public DexEncodedMethod lookupSingleTarget(AppView<?> appView, DexType invocationContext) {
    if (appView.appInfo().hasLiveness()) {
      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      AppInfoWithLiveness appInfo = appViewWithLiveness.appInfo();
      return appInfo.lookupSingleInterfaceTarget(
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
    return inliningConstraints.forInvokeInterface(getInvokedMethod(), invocationContext);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfInvoke(org.objectweb.asm.Opcodes.INVOKEINTERFACE, getInvokedMethod(), true));
  }

  @Override
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forInvokeInterface(
        this, clazz, context, appView, mode, assumption);
  }
}
