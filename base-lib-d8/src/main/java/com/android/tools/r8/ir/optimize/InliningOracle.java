// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;

/**
 * The InliningOracle contains information needed for when inlining other methods into @method.
 */
public interface InliningOracle {

  boolean isForcedInliningOracle();

  // TODO(b/142116551): This should be equivalent to invoke.lookupSingleTarget(appView, context)!
  DexEncodedMethod lookupSingleTarget(InvokeMethod invoke, DexType context);

  boolean passesInliningConstraints(
      InvokeMethod invoke,
      DexEncodedMethod candidate,
      Reason reason,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);

  InlineAction computeInlining(
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);
}
