// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
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
import com.android.tools.r8.ir.conversion.CfState.Slot;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.DescriptorUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CfNewArray extends CfInstruction {

  private final DexType type;

  public CfNewArray(DexType type) {
    assert type.isArrayType();
    this.type = type;
  }

  public DexType getType() {
    return type;
  }

  private int getPrimitiveTypeCode() {
    switch (type.descriptor.content[1]) {
      case 'Z':
        return Opcodes.T_BOOLEAN;
      case 'C':
        return Opcodes.T_CHAR;
      case 'F':
        return Opcodes.T_FLOAT;
      case 'D':
        return Opcodes.T_DOUBLE;
      case 'B':
        return Opcodes.T_BYTE;
      case 'S':
        return Opcodes.T_SHORT;
      case 'I':
        return Opcodes.T_INT;
      case 'J':
        return Opcodes.T_LONG;
      default:
        throw new Unreachable("Unexpected type for new-array: " + type);
    }
  }

  private String getElementInternalName(NamingLens lens) {
    assert !type.isPrimitiveArrayType();
    String renamedArrayType = lens.lookupDescriptor(type).toString();
    assert renamedArrayType.charAt(0) == '[';
    String elementType = renamedArrayType.substring(1);
    return DescriptorUtils.descriptorToInternalName(elementType);
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    if (type.isPrimitiveArrayType()) {
      visitor.visitIntInsn(Opcodes.NEWARRAY, getPrimitiveTypeCode());
    } else {
      visitor.visitTypeInsn(Opcodes.ANEWARRAY, getElementInternalName(lens));
    }
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public void registerUse(UseRegistry registry, DexType clazz) {
    if (!type.isPrimitiveArrayType()) {
      registry.registerTypeReference(type);
    }
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    Slot size = state.pop();
    Slot push = state.push(type);
    builder.addNewArrayEmpty(push.register, size.register, type);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forNewArrayEmpty(type, invocationContext);
  }
}
