// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.Cmp;
import com.android.tools.r8.ir.code.Cmp.Bias;
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

public class CfCmp extends CfInstruction {

  private final Bias bias;
  private final NumericType type;

  public CfCmp(Bias bias, NumericType type) {
    assert bias != null;
    assert type != null;
    assert type == NumericType.LONG || type == NumericType.FLOAT || type == NumericType.DOUBLE;
    assert type != NumericType.LONG || bias == Cmp.Bias.NONE;
    assert type == NumericType.LONG || bias != Cmp.Bias.NONE;
    this.bias = bias;
    this.type = type;
  }

  public Bias getBias() {
    return bias;
  }

  public NumericType getType() {
    return type;
  }

  public static CfCmp fromAsm(int opcode) {
    switch (opcode) {
      case Opcodes.LCMP:
        return new CfCmp(Bias.NONE, NumericType.LONG);
      case Opcodes.FCMPL:
        return new CfCmp(Bias.LT, NumericType.FLOAT);
      case Opcodes.FCMPG:
        return new CfCmp(Bias.GT, NumericType.FLOAT);
      case Opcodes.DCMPL:
        return new CfCmp(Bias.LT, NumericType.DOUBLE);
      case Opcodes.DCMPG:
        return new CfCmp(Bias.GT, NumericType.DOUBLE);
      default:
        throw new Unreachable("Wrong ASM opcode for CfCmp " + opcode);
    }
  }

  public int getAsmOpcode() {
    switch (type) {
      case LONG:
        return Opcodes.LCMP;
      case FLOAT:
        return bias == Cmp.Bias.LT ? Opcodes.FCMPL : Opcodes.FCMPG;
      case DOUBLE:
        return bias == Cmp.Bias.LT ? Opcodes.DCMPL : Opcodes.DCMPG;
      default:
        throw new Unreachable("CfCmp has unknown type " + type);
    }
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitInsn(getAsmOpcode());
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    int right = state.pop().register;
    int left = state.pop().register;
    builder.addCmp(type, bias, state.push(ValueType.INT).register, left, right);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forBinop();
  }
}
