// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfNop;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class DebugPosition extends Instruction {

  public DebugPosition() {
    super(null);
  }

  @Override
  public int opcode() {
    return Opcodes.DEBUG_POSITION;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isDebugPosition() {
    return true;
  }

  @Override
  public DebugPosition asDebugPosition() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    assert getPosition().isSome() && !getPosition().synthetic;
    builder.addDebugPosition(this);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isDebugPosition();
  }

  @Override
  public int maxInValueRegister() {
    throw new Unreachable();
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forDebugPosition();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return false;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // Nothing to do for positions which are not actual instructions.
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public void buildCf(CfBuilder builder) {
    assert getPosition().isSome() && !getPosition().synthetic;
    // All redundant debug positions are removed. Remaining ones must force a pc advance.
    builder.add(new CfNop());
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }

  @Override
  public boolean isAllowedAfterThrowingInstruction() {
    return true;
  }
}
