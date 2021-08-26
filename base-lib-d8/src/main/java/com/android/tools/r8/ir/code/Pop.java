// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class Pop extends Instruction {

  public Pop(StackValue src) {
    super(null, src);
  }

  @Override
  public int opcode() {
    return Opcodes.POP;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  protected void addInValue(Value value) {
    if (value.hasUsersInfo()) {
      super.addInValue(value);
    } else {
      // Overriding IR addInValue since userinfo is cleared.
      inValues.add(value);
    }
  }

  @Override
  public boolean isPop() {
    return true;
  }

  @Override
  public Pop asPop() {
    return this;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isPop();
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
    return inliningConstraints.forPop();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable("This classfile-specific IR should not be inserted in the Dex backend.");
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(CfStackInstruction.popType(inValues.get(0).outType()));
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    throw new Unreachable("This IR must not be inserted before load and store insertion.");
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // Pop cannot be dead code as it modifies the stack height.
    return false;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
