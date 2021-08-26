// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;

public class CfConstString extends CfInstruction {

  private DexString string;

  public CfConstString(DexString string) {
    this.string = string;
  }

  public DexString getString() {
    return string;
  }

  public void setString(DexString string) {
    this.string = string;
  }

  @Override
  public CfConstString asConstString() {
    return this;
  }

  @Override
  public boolean isConstString() {
    return true;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitLdcInsn(string.toString());
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public boolean canThrow() {
    // const-string* may throw in dex. (Not in CF, see CfSourceCode.canThrowHelper)
    return true;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    builder.addConstString(
        state.push(builder.appView.dexItemFactory().stringType).register, string);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forConstInstruction();
  }
}
