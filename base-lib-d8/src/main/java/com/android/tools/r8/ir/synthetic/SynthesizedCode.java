// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.UseRegistry;
import java.util.function.Consumer;

public class SynthesizedCode extends AbstractSynthesizedCode {

  private final SourceCodeProvider sourceCodeProvider;
  private final Consumer<UseRegistry> registryCallback;

  public SynthesizedCode(SourceCodeProvider sourceCodeProvider) {
    this(sourceCodeProvider, SynthesizedCode::registerReachableDefinitionsDefault);
  }

  public SynthesizedCode(SourceCodeProvider sourceCodeProvider, Consumer<UseRegistry> callback) {
    this.sourceCodeProvider = sourceCodeProvider;
    this.registryCallback = callback;
  }

  @Override
  public SourceCodeProvider getSourceCodeProvider() {
    return sourceCodeProvider;
  }

  @Override
  public Consumer<UseRegistry> getRegistryCallback() {
    return registryCallback;
  }

  private static void registerReachableDefinitionsDefault(UseRegistry registry) {
    throw new Unreachable();
  }
}
