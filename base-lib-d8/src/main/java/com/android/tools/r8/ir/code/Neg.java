// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfNeg;
import com.android.tools.r8.code.NegDouble;
import com.android.tools.r8.code.NegFloat;
import com.android.tools.r8.code.NegInt;
import com.android.tools.r8.code.NegLong;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.ConstLatticeElement;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.analysis.type.PrimitiveTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.function.Function;

public class Neg extends Unop {

  public final NumericType type;

  public Neg(NumericType type, Value dest, Value source) {
    super(dest, source);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.NEG;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean canBeFolded() {
    return (type == NumericType.INT || type == NumericType.LONG || type == NumericType.FLOAT
            || type == NumericType.DOUBLE)
        && source().isConstant();
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isNeg() && other.asNeg().type == type;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int dest = builder.allocatedRegister(dest(), getNumber());
    int src = builder.allocatedRegister(source(), getNumber());
    switch (type) {
      case INT:
        instruction = new NegInt(dest, src);
        break;
      case LONG:
        instruction = new NegLong(dest, src);
        break;
      case FLOAT:
        instruction = new NegFloat(dest, src);
        break;
      case DOUBLE:
        instruction = new NegDouble(dest, src);
        break;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean isNeg() {
    return true;
  }

  @Override
  public Neg asNeg() {
    return this;
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    LatticeElement sourceLattice = getLatticeElement.apply(source());
    if (sourceLattice.isConst()) {
      ConstNumber sourceConst = sourceLattice.asConst().getConstNumber();
      TypeLatticeElement typeLattice = PrimitiveTypeLatticeElement.fromNumericType(type);
      Value value = code.createValue(typeLattice, getLocalInfo());
      ConstNumber newConst;
      if (type == NumericType.INT) {
        newConst = new ConstNumber(value, -sourceConst.getIntValue());
      } else if (type == NumericType.LONG) {
        newConst = new ConstNumber(value, -sourceConst.getLongValue());
      } else if (type == NumericType.FLOAT) {
        newConst = new ConstNumber(value, Float.floatToIntBits(-sourceConst.getFloatValue()));
      } else {
        assert type == NumericType.DOUBLE;
        newConst = new ConstNumber(value, Double.doubleToLongBits(-sourceConst.getDoubleValue()));
      }
      return new ConstLatticeElement(newConst);
    }
    return Bottom.getInstance();
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfNeg(type));
  }
}
