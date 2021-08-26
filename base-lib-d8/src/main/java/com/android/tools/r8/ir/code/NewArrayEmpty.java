// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfNewArray;
import com.android.tools.r8.code.NewArray;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class NewArrayEmpty extends Instruction {

  public final DexType type;

  public NewArrayEmpty(Value dest, Value size, DexType type) {
    super(dest, size);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.NEW_ARRAY_EMPTY;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return super.toString() + " " + type.toString();
  }

  public Value dest() {
    return outValue;
  }

  public Value size() {
    return inValues.get(0);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int size = builder.allocatedRegister(size(), getNumber());
    int dest = builder.allocatedRegister(dest(), getNumber());
    builder.add(this, new NewArray(dest, size, type));
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    // new-array throws if its size is negative, but can also potentially throw on out-of-memory.
    return true;
  }

  @Override
  public boolean instructionInstanceCanThrow() {
    return !(size().definition != null
        && size().definition.isConstNumber()
        && size().definition.asConstNumber().getRawValue() >= 0
        && size().definition.asConstNumber().getRawValue() < Integer.MAX_VALUE);
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    if (instructionInstanceCanThrow()) {
      return false;
    }
    // This would belong better in instructionInstanceCanThrow, but that is not passed an appInfo.
    DexType baseType = type.toBaseType(appView.dexItemFactory());
    return baseType.isPrimitiveType() || appView.definitionFor(baseType) != null;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isNewArrayEmpty() && other.asNewArrayEmpty().type == type;
  }

  @Override
  public boolean isNewArrayEmpty() {
    return true;
  }

  @Override
  public NewArrayEmpty asNewArrayEmpty() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forNewArrayEmpty(type, invocationContext);
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return type;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    assert type.isArrayType();
    builder.add(new CfNewArray(type));
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.fromDexType(type, Nullability.definitelyNotNull(), appView);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
