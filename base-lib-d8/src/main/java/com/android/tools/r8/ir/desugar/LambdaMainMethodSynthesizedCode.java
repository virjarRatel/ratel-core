// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.code.Invoke;
import java.util.function.Consumer;

class LambdaMainMethodSynthesizedCode extends LambdaSynthesizedCode {

  private final DexMethod mainMethod;

  LambdaMainMethodSynthesizedCode(LambdaClass lambda, DexMethod mainMethod) {
    super(lambda);
    this.mainMethod = mainMethod;
  }

  @Override
  public SourceCodeProvider getSourceCodeProvider() {
    return callerPosition -> new LambdaMainMethodSourceCode(lambda, mainMethod, callerPosition);
  }

  @Override
  public Consumer<UseRegistry> getRegistryCallback() {
    return registry -> {
      LambdaClass.Target target = lambda.target;
      assert target.invokeType == Invoke.Type.STATIC
          || target.invokeType == Invoke.Type.VIRTUAL
          || target.invokeType == Invoke.Type.DIRECT
          || target.invokeType == Invoke.Type.INTERFACE;

      registry.registerNewInstance(target.callTarget.holder);

      DexType[] capturedTypes = captures();
      for (int i = 0; i < capturedTypes.length; i++) {
        registry.registerInstanceFieldRead(lambda.getCaptureField(i));
      }

      switch (target.invokeType) {
        case DIRECT:
          registry.registerInvokeDirect(target.callTarget);
          break;
        case INTERFACE:
          registry.registerInvokeInterface(target.callTarget);
          break;
        case STATIC:
          registry.registerInvokeStatic(target.callTarget);
          break;
        case VIRTUAL:
          registry.registerInvokeVirtual(target.callTarget);
          break;
        default:
          throw new Unreachable();
      }
    };
  }
}
