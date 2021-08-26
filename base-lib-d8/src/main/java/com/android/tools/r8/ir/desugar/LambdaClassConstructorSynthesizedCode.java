// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.UseRegistry;
import java.util.function.Consumer;

class LambdaClassConstructorSynthesizedCode extends LambdaSynthesizedCode {

  LambdaClassConstructorSynthesizedCode(LambdaClass lambda) {
    super(lambda);
  }

  @Override
  public SourceCodeProvider getSourceCodeProvider() {
    return callerPosition -> new LambdaClassConstructorSourceCode(lambda, callerPosition);
  }

  @Override
  public Consumer<UseRegistry> getRegistryCallback() {
    return registry -> {
      registry.registerNewInstance(lambda.type);
      registry.registerInvokeDirect(lambda.constructor);
      registry.registerStaticFieldWrite(lambda.lambdaField);
    };
  }
}
