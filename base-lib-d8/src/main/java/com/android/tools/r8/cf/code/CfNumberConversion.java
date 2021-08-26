// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CfNumberConversion extends CfInstruction {

  private final NumericType from;
  private final NumericType to;

  public CfNumberConversion(NumericType from, NumericType to) {
    assert from != to;
    assert from != NumericType.BYTE && from != NumericType.SHORT && from != NumericType.CHAR;
    assert (to != NumericType.BYTE && to != NumericType.SHORT && to != NumericType.CHAR)
        || from == NumericType.INT;
    this.from = from;
    this.to = to;
  }

  public NumericType getFromType() {
    return from;
  }

  public NumericType getToType() {
    return to;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitInsn(this.getAsmOpcode());
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  public int getAsmOpcode() {
    switch (from) {
      case INT:
        switch (to) {
          case BYTE:
            return Opcodes.I2B;
          case CHAR:
            return Opcodes.I2C;
          case SHORT:
            return Opcodes.I2S;
          case LONG:
            return Opcodes.I2L;
          case FLOAT:
            return Opcodes.I2F;
          case DOUBLE:
            return Opcodes.I2D;
          default:
            throw new Unreachable("Invalid CfNumberConversion from " + from + " to " + to);
        }
      case LONG:
        switch (to) {
          case INT:
            return Opcodes.L2I;
          case FLOAT:
            return Opcodes.L2F;
          case DOUBLE:
            return Opcodes.L2D;
          default:
            throw new Unreachable("Invalid CfNumberConversion from " + from + " to " + to);
        }
      case FLOAT:
        switch (to) {
          case INT:
            return Opcodes.F2I;
          case LONG:
            return Opcodes.F2L;
          case DOUBLE:
            return Opcodes.F2D;
          default:
            throw new Unreachable("Invalid CfNumberConversion from " + from + " to " + to);
        }
      case DOUBLE:
        switch (to) {
          case INT:
            return Opcodes.D2I;
          case LONG:
            return Opcodes.D2L;
          case FLOAT:
            return Opcodes.D2F;
          default:
            throw new Unreachable("Invalid CfNumberConversion from " + from + " to " + to);
        }
      default:
        throw new Unreachable("Invalid CfNumberConversion from " + from + " to " + to);
    }
  }

  public static CfNumberConversion fromAsm(int opcode) {
    switch (opcode) {
      case Opcodes.I2L:
        return new CfNumberConversion(NumericType.INT, NumericType.LONG);
      case Opcodes.I2F:
        return new CfNumberConversion(NumericType.INT, NumericType.FLOAT);
      case Opcodes.I2D:
        return new CfNumberConversion(NumericType.INT, NumericType.DOUBLE);
      case Opcodes.L2I:
        return new CfNumberConversion(NumericType.LONG, NumericType.INT);
      case Opcodes.L2F:
        return new CfNumberConversion(NumericType.LONG, NumericType.FLOAT);
      case Opcodes.L2D:
        return new CfNumberConversion(NumericType.LONG, NumericType.DOUBLE);
      case Opcodes.F2I:
        return new CfNumberConversion(NumericType.FLOAT, NumericType.INT);
      case Opcodes.F2L:
        return new CfNumberConversion(NumericType.FLOAT, NumericType.LONG);
      case Opcodes.F2D:
        return new CfNumberConversion(NumericType.FLOAT, NumericType.DOUBLE);
      case Opcodes.D2I:
        return new CfNumberConversion(NumericType.DOUBLE, NumericType.INT);
      case Opcodes.D2L:
        return new CfNumberConversion(NumericType.DOUBLE, NumericType.LONG);
      case Opcodes.D2F:
        return new CfNumberConversion(NumericType.DOUBLE, NumericType.FLOAT);
      case Opcodes.I2B:
        return new CfNumberConversion(NumericType.INT, NumericType.BYTE);
      case Opcodes.I2C:
        return new CfNumberConversion(NumericType.INT, NumericType.CHAR);
      case Opcodes.I2S:
        return new CfNumberConversion(NumericType.INT, NumericType.SHORT);
      default:
        throw new Unreachable("Unexpected CfNumberConversion opcode " + opcode);
    }
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    int source = state.pop().register;
    builder.addConversion(to, from, state.push(ValueType.fromNumericType(to)).register, source);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forUnop();
  }
}
