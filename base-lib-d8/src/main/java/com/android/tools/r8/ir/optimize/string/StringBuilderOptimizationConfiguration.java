// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.string;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.InvokeMethod;

interface StringBuilderOptimizationConfiguration {
  boolean isBuilderType(DexType type);

  boolean isBuilderInit(DexMethod method, DexType builderType);

  boolean isBuilderInit(DexMethod method);

  boolean isBuilderInitWithInitialValue(InvokeMethod invoke);

  boolean isAppendMethod(DexMethod method);

  boolean isSupportedAppendMethod(InvokeMethod invoke);

  boolean isToStringMethod(DexMethod method);
}
