// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import java.util.List;

public abstract class JumpInstruction extends Instruction {

  public JumpInstruction() {
    super(null);
  }

  public JumpInstruction(Value in) {
    super(null, in);
  }

  public JumpInstruction(List<? extends Value> ins) {
    super(null, ins);
  }

  public BasicBlock fallthroughBlock() {
    return null;
  }

  public void setFallthroughBlock(BasicBlock block) {
    assert false : "We should not change the fallthrough of a JumpInstruction with no fallthrough.";
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return false;
  }

  @Override
  public boolean isJumpInstruction() {
    return true;
  }

  @Override
  public JumpInstruction asJumpInstruction() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forJumpInstruction();
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
