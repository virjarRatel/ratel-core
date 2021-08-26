// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import java.util.Collections;

// Source code representing simple forwarding method.
public final class ExceptionThrowingSourceCode extends SyntheticSourceCode {

  private static final int register = 0;
  private final DexType exceptionType;

  public ExceptionThrowingSourceCode(
      DexType receiver, DexMethod method, Position callerPosition, DexType exceptionType) {
    super(receiver, method, callerPosition);
    this.exceptionType = exceptionType;
  }

  @Override
  protected void prepareInstructions() {
    add(
        builder -> {
          DexItemFactory factory = builder.appView.dexItemFactory();
          DexProto initProto = factory.createProto(factory.voidType);
          DexMethod initMethod =
              factory.createMethod(exceptionType, initProto, factory.constructorMethodName);
          builder.addNewInstance(register, exceptionType);
          builder.addInvoke(
              Type.DIRECT,
              initMethod,
              initMethod.proto,
              Collections.singletonList(ValueType.OBJECT),
              Collections.singletonList(register),
              false /* isInterface */);
          builder.addThrow(register);
        });
  }
}
