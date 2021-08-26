// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.code.NotInt;
import com.android.tools.r8.code.NotLong;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.ConstLatticeElement;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.analysis.type.PrimitiveTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.function.Function;

public class Not extends Unop {

  public final NumericType type;

  public Not(NumericType type, Value dest, Value source) {
    super(dest, source);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.NOT;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean canBeFolded() {
    return source().isConstant();
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    LatticeElement sourceLattice = getLatticeElement.apply(source());
    if (sourceLattice.isConst()) {
      ConstNumber sourceConst = sourceLattice.asConst().getConstNumber();
      TypeLatticeElement typeLattice  = PrimitiveTypeLatticeElement.fromNumericType(type);
      Value value = code.createValue(typeLattice, getLocalInfo());
      ConstNumber newConst;
      if (type == NumericType.INT) {
        newConst = new ConstNumber(value, ~sourceConst.getIntValue());
      } else {
        assert type == NumericType.LONG;
        newConst = new ConstNumber(value, ~sourceConst.getLongValue());
      }
      return new ConstLatticeElement(newConst);
    }
    return Bottom.getInstance();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    assert builder.getOptions().canUseNotInstruction();
    com.android.tools.r8.code.Instruction instruction;
    int dest = builder.allocatedRegister(dest(), getNumber());
    int src = builder.allocatedRegister(source(), getNumber());
    switch (type) {
      case INT:
        instruction = new NotInt(dest, src);
        break;
      case LONG:
        instruction = new NotLong(dest, src);
        break;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isNot() && other.asNot().type == type;
  }

  @Override
  public boolean isNot() {
    return true;
  }

  @Override
  public Not asNot() {
    return this;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // JVM has no Not instruction, they should be replaced by "Load -1, Xor" before building CF.
    throw new Unreachable();
  }

  @Override
  public void buildCf(CfBuilder builder) {
    // JVM has no Not instruction, they should be replaced by "Load -1, Xor" before building CF.
    throw new Unreachable();
  }
}
