// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.google.common.collect.ImmutableList;

// Source code representing synthesized lambda class constructor.
// Used for stateless lambdas to instantiate singleton instance.
final class LambdaClassConstructorSourceCode extends SynthesizedLambdaSourceCode {

  LambdaClassConstructorSourceCode(LambdaClass lambda, Position callerPosition) {
    super(lambda, lambda.classConstructor, callerPosition, null /* Class initializer is static */);
    assert lambda.lambdaField != null;
  }

  @Override
  protected void prepareInstructions() {
    // Create and initialize an instance.
    int instance = nextRegister(ValueType.OBJECT);
    add(builder -> builder.addNewInstance(instance, lambda.type));
    add(
        builder ->
            builder.addInvoke(
                Invoke.Type.DIRECT,
                lambda.constructor,
                lambda.constructor.proto,
                ImmutableList.of(ValueType.OBJECT),
                ImmutableList.of(instance),
                false /* isInterface */));

    // Assign to a field.
    add(builder -> builder.addStaticPut(instance, lambda.lambdaField));

    // Final return.
    add(IRBuilder::addReturn);
  }
}
