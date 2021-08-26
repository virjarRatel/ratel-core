// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.conversion.CallSiteInformation;
import com.android.tools.r8.ir.optimize.Inliner;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.shaking.AppInfoWithLiveness;

public class DefaultInliningReasonStrategy implements InliningReasonStrategy {

  private final AppView<AppInfoWithLiveness> appView;
  private final CallSiteInformation callSiteInformation;
  private final Inliner inliner;

  public DefaultInliningReasonStrategy(
      AppView<AppInfoWithLiveness> appView,
      CallSiteInformation callSiteInformation,
      Inliner inliner) {
    this.appView = appView;
    this.callSiteInformation = callSiteInformation;
    this.inliner = inliner;
  }

  @Override
  public Reason computeInliningReason(InvokeMethod invoke, DexEncodedMethod target) {
    if (target.getOptimizationInfo().forceInline()
        || (appView.appInfo().hasLiveness()
            && appView.withLiveness().appInfo().forceInline.contains(target.method))) {
      assert !appView.appInfo().neverInline.contains(target.method);
      return Reason.FORCE;
    }
    if (appView.appInfo().hasLiveness()
        && appView.withLiveness().appInfo().alwaysInline.contains(target.method)) {
      return Reason.ALWAYS;
    }
    if (appView.options().disableInliningOfLibraryMethodOverrides
        && target.isLibraryMethodOverride().isTrue()) {
      // This method will always have an implicit call site from the library, so we won't be able to
      // remove it after inlining even if we have single or dual call site information from the
      // program.
      return Reason.SIMPLE;
    }
    if (callSiteInformation.hasSingleCallSite(target.method)) {
      return Reason.SINGLE_CALLER;
    }
    if (isDoubleInliningTarget(target)) {
      return Reason.DUAL_CALLER;
    }
    return Reason.SIMPLE;
  }

  private boolean isDoubleInliningTarget(DexEncodedMethod candidate) {
    // 10 is found from measuring.
    if (callSiteInformation.hasDoubleCallSite(candidate.method)
        || inliner.isDoubleInlineSelectedTarget(candidate)) {
      return candidate.getCode().estimatedSizeForInliningAtMost(10);
    }
    return false;
  }
}
