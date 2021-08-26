// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.escape;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.code.Instruction;

public interface EscapeAnalysisConfiguration {

  boolean isLegitimateEscapeRoute(
      AppView<?> appView,
      EscapeAnalysis escapeAnalysis,
      Instruction escapeRoute,
      DexMethod context);
}
