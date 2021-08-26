// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfReturnVoid;
import com.android.tools.r8.code.ReturnObject;
import com.android.tools.r8.code.ReturnVoid;
import com.android.tools.r8.code.ReturnWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class Return extends JumpInstruction {

  public Return() {
    super();
  }

  public Return(Value value) {
    super(value);
  }

  @Override
  public int opcode() {
    return Opcodes.RETURN;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public boolean isReturnVoid() {
    return inValues.size() == 0;
  }

  public TypeLatticeElement getReturnType() {
    assert !isReturnVoid();
    return returnValue().getTypeLattice();
  }

  public Value returnValue() {
    assert !isReturnVoid();
    return inValues.get(0);
  }

  public com.android.tools.r8.code.Instruction createDexInstruction(DexBuilder builder) {
    if (isReturnVoid()) {
      return new ReturnVoid();
    }
    int register = builder.allocatedRegister(returnValue(), getNumber());
    TypeLatticeElement returnType = getReturnType();
    if (returnType.isReference()) {
      return new ReturnObject(register);
    }
    if (returnType.isSinglePrimitive()) {
      return new com.android.tools.r8.code.Return(register);
    }
    if (returnType.isWidePrimitive()) {
      return new ReturnWide(register);
    }
    throw new Unreachable();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.addReturn(this, createDexInstruction(builder));
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isReturn()) {
      return false;
    }
    Return o = other.asReturn();
    if (isReturnVoid()) {
      return o.isReturnVoid();
    }
    return getReturnType().isValueTypeCompatible(o.getReturnType());
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "Return defines no values.";
    return 0;
  }

  @Override
  public boolean isReturn() {
    return true;
  }

  @Override
  public Return asReturn() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forReturn();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    if (!isReturnVoid()) {
      helper.loadInValues(this, it);
    }
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(
        isReturnVoid()
            ? new CfReturnVoid()
            : new CfReturn(ValueType.fromTypeLattice(getReturnType())));
  }
}
