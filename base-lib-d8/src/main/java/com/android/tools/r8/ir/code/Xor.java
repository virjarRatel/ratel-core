// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfLogicalBinop;
import com.android.tools.r8.code.XorInt;
import com.android.tools.r8.code.XorInt2Addr;
import com.android.tools.r8.code.XorIntLit16;
import com.android.tools.r8.code.XorIntLit8;
import com.android.tools.r8.code.XorLong;
import com.android.tools.r8.code.XorLong2Addr;
import java.util.Set;

public class Xor extends LogicalBinop {

  public Xor(NumericType type, Value dest, Value left, Value right) {
    super(type, dest, left, right);
  }

  @Override
  public int opcode() {
    return Opcodes.XOR;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isXor() {
    return true;
  }

  @Override
  public Xor asXor() {
    return this;
  }

  @Override
  public boolean isCommutative() {
    return true;
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt(int dest, int left, int right) {
    return new XorInt(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong(int dest, int left, int right) {
    return new XorLong(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt2Addr(int left, int right) {
    return new XorInt2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong2Addr(int left, int right) {
    return new XorLong2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit8(int dest, int left, int constant) {
    return new XorIntLit8(dest, left, constant);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit16(int dest, int left, int constant) {
    return new XorIntLit16(dest, left, constant);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isXor() && other.asXor().type == type;
  }

  @Override
  int foldIntegers(int left, int right) {
    return left ^ right;
  }

  @Override
  long foldLongs(long left, long right) {
    return left ^ right;
  }

  @Override
  CfLogicalBinop.Opcode getCfOpcode() {
    return CfLogicalBinop.Opcode.Xor;
  }

  @Override
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return leftValue().knownToBeBoolean(seen) && rightValue().knownToBeBoolean(seen);
  }
}
