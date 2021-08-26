// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfLogicalBinop;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.ConstLatticeElement;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.function.Function;

public abstract class LogicalBinop extends Binop {

  public LogicalBinop(NumericType type, Value dest, Value left, Value right) {
    super(type, dest, left, right);
  }

  public abstract com.android.tools.r8.code.Instruction CreateInt(int dest, int left, int right);

  public abstract Instruction CreateLong(int dest, int left, int right);

  public abstract Instruction CreateInt2Addr(int left, int right);

  public abstract Instruction CreateLong2Addr(int left, int right);

  public abstract Instruction CreateIntLit8(int dest, int left, int constant);

  public abstract Instruction CreateIntLit16(int dest, int left, int constant);

  @Override
  public boolean canBeFolded() {
    return leftValue().isConstant() && rightValue().isConstant();
  }

  @Override
  public boolean needsValueInRegister(Value value) {
    // Always require the left value in a register. If left and right are the same value, then
    // both will use its register.
    if (value == leftValue()) {
      return true;
    }
    assert value == rightValue();
    return !fitsInDexInstruction(value);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    // TODO(ager, sgjesse): For now the left operand is always a value. With constant propagation
    // that will change.
    int left = builder.allocatedRegister(leftValue(), getNumber());
    int dest = builder.allocatedRegister(outValue, getNumber());
    Instruction instruction;
    if (isTwoAddr(builder.getRegisterAllocator())) {
      int right = builder.allocatedRegister(rightValue(), getNumber());
      if (left != dest) {
        assert isCommutative();
        assert right == dest;
        right = left;
      }
      switch (type) {
        case INT:
          instruction = CreateInt2Addr(dest, right);
          break;
        case LONG:
          instruction = CreateLong2Addr(dest, right);
          break;
        default:
          throw new Unreachable("Unexpected type " + type);
      }
    } else if (!needsValueInRegister(rightValue())) {
      assert fitsInDexInstruction(rightValue());
      ConstNumber right = rightValue().getConstInstruction().asConstNumber();
      if (right.is8Bit()) {
        instruction = CreateIntLit8(dest, left, right.getIntValue());
      } else {
        assert right.is16Bit();
        instruction = CreateIntLit16(dest, left, right.getIntValue());
      }
    } else {
      int right = builder.allocatedRegister(rightValue(), getNumber());
      switch (type) {
        case INT:
          instruction = CreateInt(dest, left, right);
          break;
        case LONG:
          instruction = CreateLong(dest, left, right);
          break;
        default:
          throw new Unreachable("Unexpected type " + type);
      }
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean isLogicalBinop() {
    return true;
  }

  @Override
  public LogicalBinop asLogicalBinop() {
    return this;
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    LatticeElement leftLattice = getLatticeElement.apply(leftValue());
    LatticeElement rightLattice = getLatticeElement.apply(rightValue());
    if (leftLattice.isConst() && rightLattice.isConst()) {
      ConstNumber leftConst = leftLattice.asConst().getConstNumber();
      ConstNumber rightConst = rightLattice.asConst().getConstNumber();
      ConstNumber newConst;
      if (type == NumericType.INT) {
        int result = foldIntegers(leftConst.getIntValue(), rightConst.getIntValue());
        Value value = code.createValue(TypeLatticeElement.INT, getLocalInfo());
        newConst = new ConstNumber(value, result);
      } else {
        assert type == NumericType.LONG;
        long right;
        if (isShl() || isShr() || isUshr()) {
          // Right argument for shl, shr and ushr is always of type single.
          right = rightConst.getIntValue();
        } else {
          right = rightConst.getLongValue();
        }
        long result = foldLongs(leftConst.getLongValue(), right);
        Value value = code.createValue(TypeLatticeElement.LONG, getLocalInfo());
        newConst = new ConstNumber(value, result);
      }
      return new ConstLatticeElement(newConst);
    }
    return Bottom.getInstance();
  }

  abstract CfLogicalBinop.Opcode getCfOpcode();

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfLogicalBinop(getCfOpcode(), type));
  }
}
