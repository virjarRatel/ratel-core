// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public abstract class ConstInstruction extends Instruction {

  public static ConstInstruction copyOf(Value value, ConstInstruction instr) {
    if (instr.isConstClass()) {
      return ConstClass.copyOf(value, instr.asConstClass());
    } else if (instr.isConstMethodHandle()) {
      return ConstMethodHandle.copyOf(value, instr.asConstMethodHandle());
    } else if (instr.isConstMethodType()) {
      return ConstMethodType.copyOf(value, instr.asConstMethodType());
    } else if (instr.isConstNumber()) {
      return ConstNumber.copyOf(value, instr.asConstNumber());
    } else if (instr.isConstString()) {
      return ConstString.copyOf(value, instr.asConstString());
    } else if (instr.isDexItemBasedConstString()) {
      return DexItemBasedConstString.copyOf(value, instr.asDexItemBasedConstString());
    } else {
      throw new Unreachable();
    }
  }

  public ConstInstruction(Value out) {
    super(out);
  }

  @Override
  public ConstInstruction getOutConstantConstInstruction() {
    return this;
  }

  @Override
  public boolean isConstInstruction() {
    return true;
  }

  @Override
  public ConstInstruction asConstInstruction() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forConstInstruction();
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
