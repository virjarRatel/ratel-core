// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.InlineeWithReason;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import java.util.ListIterator;

interface InliningStrategy {

  boolean allowInliningOfInvokeInInlinee(
      InlineAction action,
      int inliningDepth,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);

  boolean canInlineInstanceInitializer(
      IRCode code, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);

  /** Return true if there is still budget for inlining into this method. */
  boolean stillHasBudget(
      InlineAction action, WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);

  /**
   * Check if the inlinee will exceed the the budget for inlining size into current method.
   *
   * <p>Return true if the strategy will *not* allow inlining.
   */
  boolean willExceedBudget(
      IRCode code,
      InvokeMethod invoke,
      InlineeWithReason inlinee,
      BasicBlock block,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter);

  /** Inform the strategy that the inlinee has been inlined. */
  void markInlined(InlineeWithReason inlinee);

  void ensureMethodProcessed(
      DexEncodedMethod target, IRCode inlinee, OptimizationFeedback feedback);

  void updateTypeInformationIfNeeded(
      IRCode inlinee, ListIterator<BasicBlock> blockIterator, BasicBlock block);

  DexType getReceiverTypeIfKnown(InvokeMethod invoke);
}
