// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class CfConstClass extends CfInstruction {

  private final DexType type;

  public CfConstClass(DexType type) {
    this.type = type;
  }

  public DexType getType() {
    return type;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitLdcInsn(Type.getObjectType(getInternalName(lens)));
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  private String getInternalName(NamingLens lens) {
    switch (type.toShorty()) {
      case '[':
      case 'L':
        return lens.lookupInternalName(type);
      case 'Z':
        return "java/lang/Boolean/TYPE";
      case 'B':
        return "java/lang/Byte/TYPE";
      case 'S':
        return "java/lang/Short/TYPE";
      case 'C':
        return "java/lang/Character/TYPE";
      case 'I':
        return "java/lang/Integer/TYPE";
      case 'F':
        return "java/lang/Float/TYPE";
      case 'J':
        return "java/lang/Long/TYPE";
      case 'D':
        return "java/lang/Double/TYPE";
      default:
        throw new Unreachable("Unexpected type in const-class: " + type);
    }
  }

  @Override
  public void registerUse(UseRegistry registry, DexType clazz) {
    registry.registerConstClass(type);
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    builder.addConstClass(state.push(builder.appView.dexItemFactory().classType).register, type);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forConstClass(type, invocationContext);
  }
}
