// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.CfState.Slot;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CfLoad extends CfInstruction {

  private final int var;
  private final ValueType type;

  public CfLoad(ValueType type, int var) {
    this.var = var;
    this.type = type;
  }

  private int getLoadType() {
    switch (type) {
      case OBJECT:
        return Opcodes.ALOAD;
      case INT:
        return Opcodes.ILOAD;
      case FLOAT:
        return Opcodes.FLOAD;
      case LONG:
        return Opcodes.LLOAD;
      case DOUBLE:
        return Opcodes.DLOAD;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
  }

  @Override
  public CfLoad asLoad() {
    return this;
  }

  @Override
  public boolean isLoad() {
    return true;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitVarInsn(getLoadType(), var);
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  public ValueType getType() {
    return type;
  }

  public int getLocalIndex() {
    return var;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    Slot local = state.read(var);
    Slot stack = state.push(local);
    builder.addMove(local.type, stack.register, local.register);
  }

  @Override
  public boolean emitsIR() {
    return false;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forLoad();
  }
}
