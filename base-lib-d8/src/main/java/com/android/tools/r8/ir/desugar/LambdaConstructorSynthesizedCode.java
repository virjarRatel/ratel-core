// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;
import java.util.function.Consumer;

class LambdaConstructorSynthesizedCode extends LambdaSynthesizedCode {

  LambdaConstructorSynthesizedCode(LambdaClass lambda) {
    super(lambda);
  }

  @Override
  public SourceCodeProvider getSourceCodeProvider() {
    return callerPosition -> new LambdaConstructorSourceCode(lambda, callerPosition);
  }

  @Override
  public Consumer<UseRegistry> getRegistryCallback() {
    return registry -> {
      registry.registerInvokeDirect(lambda.rewriter.objectInitMethod);
      DexType[] capturedTypes = captures();
      for (int i = 0; i < capturedTypes.length; i++) {
        registry.registerInstanceFieldWrite(lambda.getCaptureField(i));
      }
    };
  }
}
