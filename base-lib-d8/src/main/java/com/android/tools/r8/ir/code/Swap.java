// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfStackInstruction.Opcode;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.google.common.collect.ImmutableList;

public class Swap extends Instruction {

  public Swap(StackValue destBottom, StackValue destTop, Value srcBottom, Value srcTop) {
    this(new StackValues(destBottom, destTop), srcBottom, srcTop);
  }

  private Swap(StackValues dest, Value src1, Value src2) {
    super(dest, ImmutableList.of(src1, src2));
    assert src1.isValueOnStack() && !(src1 instanceof StackValues);
    assert src2.isValueOnStack() && !(src2 instanceof StackValues);
    assert !src1.getTypeLattice().isWidePrimitive() && !src2.getTypeLattice().isWidePrimitive();
  }

  @Override
  public int opcode() {
    return Opcodes.SWAP;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public void setOutValue(Value value) {
    assert outValue == null || !outValue.hasUsersInfo() || !outValue.isUsed() ||
        value instanceof StackValues;
    this.outValue = value;
    this.outValue.definition = this;
    for (StackValue val : ((StackValues)value).getStackValues()) {
      val.definition = this;
    }
  }

  private StackValue[] getStackValues() {
    return ((StackValues) outValue()).getStackValues();
  }

  public StackValue outBottom() {
    return getStackValues()[0];
  }

  public StackValue outTop() {
    return getStackValues()[1];
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable("This classfile-specific IR should not be inserted in the Dex backend.");
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfStackInstruction(Opcode.Swap));
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isSwap();
  }

  @Override
  public int maxInValueRegister() {
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forSwap();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // Intentionally empty. Swap is a stack operation.
  }

  @Override
  public boolean hasInvariantOutType() {
    return false;
  }

  @Override
  public boolean isSwap() {
    return true;
  }

  @Override
  public Swap asSwap() {
    return this;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
