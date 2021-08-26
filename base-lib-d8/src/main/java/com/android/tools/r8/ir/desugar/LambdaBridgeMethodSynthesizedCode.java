// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;
import java.util.function.Consumer;

class LambdaBridgeMethodSynthesizedCode extends LambdaSynthesizedCode {

  private final DexMethod mainMethod;
  private final DexMethod bridgeMethod;

  LambdaBridgeMethodSynthesizedCode(
      LambdaClass lambda, DexMethod mainMethod, DexMethod bridgeMethod) {
    super(lambda);
    this.mainMethod = mainMethod;
    this.bridgeMethod = bridgeMethod;
  }

  @Override
  public SourceCodeProvider getSourceCodeProvider() {
    return callerPosition ->
        new LambdaBridgeMethodSourceCode(lambda, mainMethod, bridgeMethod, callerPosition);
  }

  @Override
  public Consumer<UseRegistry> getRegistryCallback() {
    return registry -> {
      registry.registerInvokeVirtual(mainMethod);

      DexType bridgeMethodReturnType = bridgeMethod.proto.returnType;
      if (!bridgeMethodReturnType.isVoidType()
          && bridgeMethodReturnType != mainMethod.proto.returnType
          && bridgeMethodReturnType != dexItemFactory().objectType) {
        registry.registerCheckCast(bridgeMethodReturnType);
      }
    };
  }
}
