// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.MemberType;
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

public class CfArrayLoad extends CfInstruction {

  private final MemberType type;

  public CfArrayLoad(MemberType type) {
    assert type.isPrecise();
    this.type = type;
  }

  public MemberType getType() {
    return type;
  }

  private int getLoadType() {
    switch (type) {
      case OBJECT:
        return Opcodes.AALOAD;
      case BOOLEAN_OR_BYTE:
        return Opcodes.BALOAD;
      case CHAR:
        return Opcodes.CALOAD;
      case SHORT:
        return Opcodes.SALOAD;
      case INT:
        return Opcodes.IALOAD;
      case FLOAT:
        return Opcodes.FALOAD;
      case LONG:
        return Opcodes.LALOAD;
      case DOUBLE:
        return Opcodes.DALOAD;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitInsn(getLoadType());
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    Slot index = state.pop();
    Slot array = state.pop();
    Slot value;
    assert array.type.isObject();
    ValueType memberType = ValueType.fromMemberType(type);
    if (array.preciseType != null) {
      value = state.push(array.preciseType.toArrayElementType(builder.appView.dexItemFactory()));
      assert state.peek().type == memberType;
    } else {
      value = state.push(memberType);
    }
    builder.addArrayGet(type, value.register, array.register, index.register);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forArrayGet();
  }
}
