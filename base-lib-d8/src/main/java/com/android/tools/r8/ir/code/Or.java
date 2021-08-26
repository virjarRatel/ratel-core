// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfLogicalBinop;
import com.android.tools.r8.code.OrInt;
import com.android.tools.r8.code.OrInt2Addr;
import com.android.tools.r8.code.OrIntLit16;
import com.android.tools.r8.code.OrIntLit8;
import com.android.tools.r8.code.OrLong;
import com.android.tools.r8.code.OrLong2Addr;
import java.util.Set;

public class Or extends LogicalBinop {

  public Or(NumericType type, Value dest, Value left, Value right) {
    super(type, dest, left, right);
  }

  @Override
  public int opcode() {
    return Opcodes.OR;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isOr() {
    return true;
  }

  @Override
  public Or asOr() {
    return this;
  }

  @Override
  public boolean isCommutative() {
    return true;
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt(int dest, int left, int right) {
    return new OrInt(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong(int dest, int left, int right) {
    return new OrLong(dest, left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateInt2Addr(int left, int right) {
    return new OrInt2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateLong2Addr(int left, int right) {
    return new OrLong2Addr(left, right);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit8(int dest, int left, int constant) {
    return new OrIntLit8(dest, left, constant);
  }

  @Override
  public com.android.tools.r8.code.Instruction CreateIntLit16(int dest, int left, int constant) {
    return new OrIntLit16(dest, left, constant);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isOr() && other.asOr().type == type;
  }

  @Override
  int foldIntegers(int left, int right) {
    return left | right;
  }

  @Override
  long foldLongs(long left, long right) {
    return left | right;
  }

  @Override
  CfLogicalBinop.Opcode getCfOpcode() {
    return CfLogicalBinop.Opcode.Or;
  }

  @Override
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return leftValue().knownToBeBoolean(seen) && rightValue().knownToBeBoolean(seen);
  }
}
