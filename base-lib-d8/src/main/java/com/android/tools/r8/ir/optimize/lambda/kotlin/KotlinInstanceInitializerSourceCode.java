// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.synthetic.SyntheticSourceCode;
import java.util.function.IntFunction;

abstract class KotlinInstanceInitializerSourceCode extends SyntheticSourceCode {
  private final DexField idField;
  private final IntFunction<DexField> fieldGenerator;

  KotlinInstanceInitializerSourceCode(
      DexType lambdaGroupType,
      DexField idField,
      IntFunction<DexField> fieldGenerator,
      DexMethod method,
      Position callerPosition) {
    super(lambdaGroupType, method, callerPosition);
    this.idField = idField;
    this.fieldGenerator = fieldGenerator;
  }

  @Override
  protected void prepareInstructions() {
    int receiverRegister = getReceiverRegister();

    // Initialize lambda id field.
    add(builder -> builder.addInstancePut(getParamRegister(0), receiverRegister, idField));

    // Initialize capture values.
    DexType[] values = proto.parameters.values;
    for (int i = 1; i < values.length; i++) {
      int index = i;
      add(builder -> builder.addInstancePut(
          getParamRegister(index), receiverRegister, fieldGenerator.apply(index - 1)));
    }

    // Call superclass constructor.
    prepareSuperConstructorCall(receiverRegister);

    add(IRBuilder::addReturn);
  }

  abstract void prepareSuperConstructorCall(int receiverRegister);
}
