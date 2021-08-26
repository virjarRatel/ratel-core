// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;

/**
 * An abstraction of {@link IRCode}-level optimization.
 */
public interface CodeOptimization {

  // TODO(b/140766440): if some other code optimizations require more info to pass, i.e., requires
  //  a signature change, we may need to turn this interface into an abstract base, instead of
  //  rewriting every affected optimization.
  // Note that a code optimization can be a collection of other code optimizations.
  // In that way, IRConverter will serve as the default full processing of all optimizations.
  void optimize(
      AppView<?> appView,
      IRCode code,
      OptimizationFeedback feedback,
      MethodProcessor methodProcessor);
}
