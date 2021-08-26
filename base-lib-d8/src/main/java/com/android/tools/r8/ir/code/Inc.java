// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfArithmeticBinop;
import com.android.tools.r8.cf.code.CfArithmeticBinop.Opcode;
import com.android.tools.r8.cf.code.CfConstNumber;
import com.android.tools.r8.cf.code.CfIinc;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.function.Function;

public class Inc extends Unop {

  private final int increment;

  public Inc(Value dest, Value source, int increment) {
    super(dest, source);
    assert source.outType() == ValueType.INT;
    this.increment = increment;
  }

  @Override
  public int opcode() {
    return Opcodes.INC;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  protected void addInValue(Value value) {
    // Overriding IR addInValue since userinfo is cleared.
    assert !value.hasUsersInfo();
    inValues.add(value);
  }

  @Override
  public boolean canBeFolded() {
    return source().isConstant();
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other instanceof Inc;
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U16BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U16BIT_MAX;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // Inc is inserted after load/store insertion, after register allocation.
    throw new Unreachable();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable("The Inc instruction is not intended for Dex code.");
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    // Inc is inserted after register allocation so this is not used.
    throw new Unreachable();
  }

  @Override
  public void buildCf(CfBuilder builder) {
    Value inValue = inValues.get(0);
    int inRegister = builder.getLocalRegister(inValue);
    int outRegister = builder.getLocalRegister(outValue);
    if (inRegister == outRegister) {
      builder.add(new CfIinc(inRegister, increment));
    } else {
      assert inValue.outType() == ValueType.INT;
      builder.add(new CfLoad(ValueType.INT, inRegister));
      builder.add(new CfConstNumber(increment, ValueType.INT));
      builder.add(new CfArithmeticBinop(Opcode.Add, NumericType.INT));
      builder.add(new CfStore(ValueType.INT, outRegister));
    }
  }

  @Override
  public String toString() {
    return super.toString() + ", " + increment;
  }
}
