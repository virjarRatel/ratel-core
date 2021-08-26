// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class Load extends Instruction {

  public Load(StackValue dest, Value src) {
    super(dest, src);
  }

  @Override
  public int opcode() {
    return Opcodes.LOAD;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value src() {
    return inValues.get(0);
  }

  @Override
  public boolean isLoad() {
    return true;
  }

  @Override
  public Load asLoad() {
    return this;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isLoad();
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U16BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forLoad();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable("This classfile-specific IR should not be inserted in the Dex backend.");
  }

  @Override
  public void buildCf(CfBuilder builder) {
    Value value = src();
    builder.add(new CfLoad(value.outType(), builder.getLocalRegister(value)));
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return helper.getDexType(src());
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // Nothing to do. This is only hit because loads and stores are insert for phis.
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return src().getTypeLattice();
  }

  @Override
  public boolean hasInvariantOutType() {
    return false;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
