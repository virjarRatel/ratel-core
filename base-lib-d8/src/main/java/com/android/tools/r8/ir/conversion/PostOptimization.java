// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.DexEncodedMethod;
import java.util.Collection;
import java.util.Set;

/**
 * An abstraction of optimizations that require post processing of methods.
 */
public interface PostOptimization {

  /**
   * @return a set of methods that need post processing.
   */
  Set<DexEncodedMethod> methodsToRevisit();

  // TODO(b/127694949): different CodeOptimization for primary processor v.s. post processor?
  //  In that way, instead of internal state changes, such as COLLECT v.s. APPLY or REVISIT,
  //  optimizers that need post processing can return what to do at each processor.
  // Collection<CodeOptimization> codeOptimizationsForPrimaryProcessing();

  /**
   * @return specific collection of {@link CodeOptimization}s to conduct during post processing.
   *   Otherwise, i.e., if the default one---IRConverter's full processing---is okay,
   *   returns {@code null}.
   */
  Collection<CodeOptimization> codeOptimizationsForPostProcessing();
}
