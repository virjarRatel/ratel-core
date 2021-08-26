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
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.CfState.Slot;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CfArrayStore extends CfInstruction {

  private final MemberType type;

  public CfArrayStore(MemberType type) {
    this.type = type;
  }

  public MemberType getType() {
    return type;
  }

  private int getStoreType() {
    switch (type) {
      case OBJECT:
        return Opcodes.AASTORE;
      case BOOLEAN_OR_BYTE:
        return Opcodes.BASTORE;
      case CHAR:
        return Opcodes.CASTORE;
      case SHORT:
        return Opcodes.SASTORE;
      case INT:
        return Opcodes.IASTORE;
      case FLOAT:
        return Opcodes.FASTORE;
      case LONG:
        return Opcodes.LASTORE;
      case DOUBLE:
        return Opcodes.DASTORE;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitInsn(getStoreType());
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
    Slot value = state.pop();
    Slot index = state.pop();
    Slot array = state.pop();
    builder.addArrayPut(type, value.register, array.register, index.register);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forArrayPut();
  }
}
