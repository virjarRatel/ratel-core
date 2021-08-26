// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class AlwaysMaterializingUser extends Instruction {

  public AlwaysMaterializingUser(Value src) {
    super(null, src);
  }

  @Override
  public int opcode() {
    return Opcodes.ALWAYS_MATERIALIZING_USER;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // This instruction may never be considered dead as it must remain.
    return false;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.addNothing(this);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    throw new Unreachable();
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return false;
  }

  @Override
  public int maxInValueRegister() {
    assert inValues.get(0).definition instanceof AlwaysMaterializingDefinition;
    return inValues.get(0).definition.maxOutValueRegister();
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forAlwaysMaterializingUser();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    throw new Unreachable();
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
