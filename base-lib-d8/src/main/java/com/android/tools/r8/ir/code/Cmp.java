// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfCmp;
import com.android.tools.r8.code.CmpLong;
import com.android.tools.r8.code.CmpgDouble;
import com.android.tools.r8.code.CmpgFloat;
import com.android.tools.r8.code.CmplDouble;
import com.android.tools.r8.code.CmplFloat;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.ConstLatticeElement;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.LongInterval;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.StringUtils.BraceType;
import java.util.function.Function;

public class Cmp extends Binop {

  public enum Bias {
    NONE, GT, LT
  }

  private final Bias bias;

  public Cmp(NumericType type, Bias bias, Value dest, Value left, Value right) {
    super(type, dest, left, right);
    this.bias = bias;
  }

  @Override
  public int opcode() {
    return Opcodes.CMP;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isCommutative() {
    return false;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int dest = builder.allocatedRegister(outValue, getNumber());
    int left = builder.allocatedRegister(leftValue(), getNumber());
    int right = builder.allocatedRegister(rightValue(), getNumber());
    switch (type) {
      case DOUBLE:
        assert bias != Bias.NONE;
        if (bias == Bias.GT) {
          instruction = new CmpgDouble(dest, left, right);
        } else {
          assert bias == Bias.LT;
          instruction = new CmplDouble(dest, left, right);
        }
        break;
      case FLOAT:
        assert bias != Bias.NONE;
        if (bias == Bias.GT) {
          instruction = new CmpgFloat(dest, left, right);
        } else {
          assert bias == Bias.LT;
          instruction = new CmplFloat(dest, left, right);
        }
        break;
      case LONG:
        assert bias == Bias.NONE;
        instruction = new CmpLong(dest, left, right);
        break;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.add(this, instruction);
  }

  private String biasToString(Bias bias) {
    switch (bias) {
      case NONE:
        return "none";
      case GT:
        return "gt";
      case LT:
        return "lt";
      default:
        throw new Unreachable("Unexpected bias " + bias);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" (");
    switch (type) {
      case DOUBLE:
        builder.append("double, ");
        builder.append(biasToString(bias));
        break;
      case FLOAT:
        builder.append("float, ");
        builder.append(biasToString(bias));
        break;
      case LONG:
        builder.append("long");
        break;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.append(")");
    for (int i = builder.length(); i < 20; i++) {
      builder.append(" ");
    }
    if (outValue != null) {
      builder.append(outValue);
      builder.append(" <- ");
    }
    StringUtils.append(builder, inValues, ", ", BraceType.NONE);
    return builder.toString();
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isCmp() && other.asCmp().bias == bias;
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  private boolean nonOverlapingRanges() {
    return type == NumericType.LONG
        && leftValue().hasValueRange()
        && rightValue().hasValueRange()
        && leftValue().getValueRange().doesntOverlapWith(rightValue().getValueRange());
  }

  @Override
  public boolean canBeFolded() {
    return (leftValue().isConstNumber() && rightValue().isConstNumber()) || nonOverlapingRanges();
  }

  @Override
  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    LatticeElement leftLattice = getLatticeElement.apply(leftValue());
    LatticeElement rightLattice = getLatticeElement.apply(rightValue());
    if (leftLattice.isConst() && rightLattice.isConst()) {
      ConstNumber leftConst = leftLattice.asConst().getConstNumber();
      ConstNumber rightConst = rightLattice.asConst().getConstNumber();
      int result;
      if (type == NumericType.LONG) {
        result = Integer.signum(Long.compare(leftConst.getLongValue(), rightConst.getLongValue()));
      } else if (type == NumericType.FLOAT) {
        float left = leftConst.getFloatValue();
        float right = rightConst.getFloatValue();
        if (Float.isNaN(left) || Float.isNaN(right)) {
          result = bias == Bias.GT ? 1 : -1;
        } else {
          result = (int) Math.signum(left - right);
        }
      } else {
        assert type == NumericType.DOUBLE;
        double left = leftConst.getDoubleValue();
        double right = rightConst.getDoubleValue();
        if (Double.isNaN(left) || Double.isNaN(right)) {
          result = bias == Bias.GT ? 1 : -1;
        } else {
          result = (int) Math.signum(left - right);
        }
      }
      Value value = code.createValue(TypeLatticeElement.INT, getLocalInfo());
      ConstNumber newConst = new ConstNumber(value, result);
      return new ConstLatticeElement(newConst);
    } else if (leftLattice.isValueRange() && rightLattice.isConst()) {
      Value leftValueRange = leftLattice.asConstRange().getConstRange();
      ConstNumber rightConst = rightLattice.asConst().getConstNumber();
      return buildLatticeResult(code, leftValueRange.getValueRange(),
          rightConst.outValue().getValueRange());
    } else if (leftLattice.isConst() && rightLattice.isValueRange()) {
      Value rigthValueRange = rightLattice.asConstRange().getConstRange();
      ConstNumber leftConst = leftLattice.asConst().getConstNumber();
      return buildLatticeResult(code, leftConst.outValue().getValueRange(),
          rigthValueRange.getValueRange());
    } else if (leftLattice.isValueRange() && rightLattice.isValueRange()) {
      Value rigthValueRange = rightLattice.asConstRange().getConstRange();
      Value leftValueRange = leftLattice.asConstRange().getConstRange();
      return buildLatticeResult(code, leftValueRange.getValueRange(),
          rigthValueRange.getValueRange());
    }
    return Bottom.getInstance();
  }

  private LatticeElement buildLatticeResult(IRCode code, LongInterval leftRange,
      LongInterval rightRange) {
    if (leftRange.overlapsWith(rightRange)) {
      return Bottom.getInstance();
    }
    int result = Integer.signum(Long.compare(leftRange.getMin(), rightRange.getMin()));
    Value value = code.createValue(TypeLatticeElement.INT, getLocalInfo());
    ConstNumber newConst = new ConstNumber(value, result);
    return new ConstLatticeElement(newConst);
  }

  @Override
  public boolean isCmp() {
    return true;
  }

  @Override
  public Cmp asCmp() {
    return this;
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfCmp(bias, type));
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.INT;
  }

}
