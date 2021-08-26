// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;

public class CfIinc extends CfInstruction {

  private final int var;
  private final int increment;

  public CfIinc(int var, int increment) {
    this.var = var;
    this.increment = increment;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitIincInsn(var, increment);
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  public int getLocalIndex() {
    return var;
  }

  public int getIncrement() {
    return increment;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    int local = state.read(var).register;
    builder.addAddLiteral(NumericType.INT, local, local, increment);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return ConstraintWithTarget.ALWAYS;
  }
}
