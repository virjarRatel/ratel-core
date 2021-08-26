// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.code.FillArrayData;
import com.android.tools.r8.code.FillArrayDataPayload;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.AbstractError;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import java.util.Arrays;

public class NewArrayFilledData extends Instruction {
  private static final String ERROR_MESSAGE =
      "Conversion from DEX to classfile not supported for NewArrayFilledData";

  public final int element_width;
  public final long size;
  public final short[] data;

  // Primitive array with fill-array-data. The type is not known from the original Dex instruction.
  public NewArrayFilledData(Value src, int element_width, long size, short[] data) {
    super(null, src);
    this.element_width = element_width;
    this.size = size;
    this.data = data;
  }

  @Override
  public int opcode() {
    return Opcodes.NEW_ARRAY_FILLED_DATA;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value src() {
    return inValues.get(0);
  }

  public FillArrayDataPayload createPayload() {
    return new FillArrayDataPayload(element_width, size, data);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int src = builder.allocatedRegister(src(), getNumber());
    builder.addFillArrayData(this, new FillArrayData(src));
  }

  @Override
  public void buildCf(CfBuilder builder) {
    throw new Unreachable(ERROR_MESSAGE);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isNewArrayFilledData()) {
      return false;
    }
    NewArrayFilledData o = other.asNewArrayFilledData();
    return o.element_width == element_width
        && o.size == size
        && Arrays.equals(o.data, data);
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "NewArrayFilledData defines no values.";
    return 0;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean isNewArrayFilledData() {
    return true;
  }

  @Override
  public NewArrayFilledData asNewArrayFilledData() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forNewArrayFilledData();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    throw new Unreachable(ERROR_MESSAGE);
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public AbstractError instructionInstanceCanThrow(AppView<?> appView, DexType context) {
    if (appView.options().debug) {
      return AbstractError.top();
    }

    if (src().getTypeLattice().isNullable()) {
      return AbstractError.top();
    }

    return AbstractError.bottom();
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    // Treat the instruction as possibly having side-effects if it may throw or the array is used.
    if (instructionInstanceCanThrow(appView, context).isThrowing()
        || src().numberOfAllUsers() > 1) {
      return true;
    }

    assert src().singleUniqueUser() == this;
    assert !src().isPhi();
    assert src().definition.isNewArrayEmpty();

    return false;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
  }
}
