// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfArithmeticBinop;
import com.android.tools.r8.code.DivDouble;
import com.android.tools.r8.code.DivDouble2Addr;
import com.android.tools.r8.code.DivFloat;
import com.android.tools.r8.code.DivFloat2Addr;
import com.android.tools.r8.code.DivInt;
import com.android.tools.r8.code.DivInt2Addr;
import com.android.tools.r8.code.DivIntLit16;
import com.android.tools.r8.code.DivIntLit8;
import com.android.tools.r8.code.DivLong;
import com.android.tools.r8.code.DivLong2Addr;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import java.util.function.Function;

public class Div extends ArithmeticBinop {

  public Div(NumericType type, Value dest, Value left, Value right) {
    super(type, dest, left, right);
  }

  @Override
  public int opcode() {
    return Opcodes.DIV;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isDiv() {
    return true;
  }

  @Override
  public Div asDiv() {
    return this;
  }

  @Override
  public boolean isCommutative() {
    return false;
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt(int dest, int left, int right) {
    return new DivInt(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong(int dest, int left, int right) {
    return new DivLong(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateFloat(int dest, int left, int right) {
    return new DivFloat(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateDouble(int dest, int left, int right) {
    return new DivDouble(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt2Addr(int left, int right) {
    return new DivInt2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong2Addr(int left, int right) {
    return new DivLong2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateFloat2Addr(int left, int right) {
    return new DivFloat2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateDouble2Addr(int left, int right) {
    return new DivDouble2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit8(int dest, int left, int constant) {
    return new DivIntLit8(dest, left, constant);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit16(int dest, int left, int constant) {
    return new DivIntLit16(dest, left, constant);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isDiv() && other.asDiv().type == type;
  }

  @Override
  public boolean canBeFolded() {
    return super.canBeFolded() && !rightValue().isZero();
  }

  @Override
  int foldIntegers(int left, int right) {
    return left / right;
  }

  @Override
  long foldLongs(long left, long right) {
    return left / right;
  }

  @Override
  float foldFloat(float left, float right) {
    return left / right;
  }

  @Override
  double foldDouble(double left, double right) {
    return left / right;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return type != NumericType.DOUBLE && type != NumericType.FLOAT;
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    LatticeElement rightLattice = getLatticeElement.apply(rightValue());
    if (rightLattice.isConst() && !rightLattice.asConst().getConstNumber().isZero()) {
      return super.evaluate(code, getLatticeElement);
    }
    return Bottom.getInstance();
  }

  @Override
  CfArithmeticBinop.Opcode getCfOpcode() {
    return CfArithmeticBinop.Opcode.Div;
  }
}
