// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.UseRegistry;

public interface EnqueuerUseRegistryFactory {

  UseRegistry create(
      AppView<?> appView,
      DexProgramClass currentHolder,
      DexEncodedMethod currentMethod,
      Enqueuer enqueuer);
}
