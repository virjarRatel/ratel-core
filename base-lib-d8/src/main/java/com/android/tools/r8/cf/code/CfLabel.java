// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class CfLabel extends CfInstruction {

  private Label label = null;

  public Label getLabel() {
    if (label == null) {
      label = new Label();
    }
    return label;
  }

  @Override
  public CfLabel asLabel() {
    return this;
  }

  @Override
  public boolean isLabel() {
    return true;
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitLabel(getLabel());
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    // Intentionally left empty.
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
    return ConstraintWithTarget.ALWAYS;
  }
}
