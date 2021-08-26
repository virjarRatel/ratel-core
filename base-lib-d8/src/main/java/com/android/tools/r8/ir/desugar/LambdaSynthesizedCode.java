// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import java.util.function.Consumer;

abstract class LambdaSynthesizedCode extends SynthesizedCode {

  final LambdaClass lambda;

  LambdaSynthesizedCode(LambdaClass lambda) {
    super(null);
    this.lambda = lambda;
  }

  final DexItemFactory dexItemFactory() {
    return lambda.rewriter.factory;
  }

  final LambdaDescriptor descriptor() {
    return lambda.descriptor;
  }

  final DexType[] captures() {
    DexTypeList captures = descriptor().captures;
    assert captures != null;
    return captures.values;
  }

  @Override
  public abstract SourceCodeProvider getSourceCodeProvider();

  @Override
  public abstract Consumer<UseRegistry> getRegistryCallback();
}
