// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.desugar.NestBasedAccessDesugaring.DexFieldWithAccess;

// Source code representing simple forwarding method.
public final class FieldAccessorSourceCode extends SyntheticSourceCode {

  private final DexFieldWithAccess fieldWithAccess;

  public FieldAccessorSourceCode(
      DexType receiver,
      DexMethod method,
      Position callerPosition,
      DexMethod originalMethod,
      DexFieldWithAccess field) {
    super(receiver, method, callerPosition, originalMethod);
    this.fieldWithAccess = field;
    assert method.proto.returnType == fieldWithAccess.getType() || fieldWithAccess.isPut();
  }

  @Override
  protected void prepareInstructions() {
    if (fieldWithAccess.isInstanceGet()) {
      ValueType valueType = ValueType.fromDexType(proto.returnType);
      int objReg = getParamRegister(0);
      int returnReg = nextRegister(valueType);
      add(builder -> builder.addInstanceGet(returnReg, objReg, fieldWithAccess.getField()));
      add(builder -> builder.addReturn(returnReg));
    } else if (fieldWithAccess.isStaticGet()) {
      ValueType valueType = ValueType.fromDexType(proto.returnType);
      int returnReg = nextRegister(valueType);
      add(builder -> builder.addStaticGet(returnReg, fieldWithAccess.getField()));
      add(builder -> builder.addReturn(returnReg));
    } else if (fieldWithAccess.isInstancePut()) {
      int objReg = getParamRegister(0);
      int putValueReg = getParamRegister(1);
      add(builder -> builder.addInstancePut(putValueReg, objReg, fieldWithAccess.getField()));
      add(IRBuilder::addReturn);
    } else {
      assert fieldWithAccess.isStaticPut();
      int putValueReg = getParamRegister(0);
      add(builder -> builder.addStaticPut(putValueReg, fieldWithAccess.getField()));
      add(IRBuilder::addReturn);
    }
  }
}
