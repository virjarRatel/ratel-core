// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.Collection;
import java.util.Set;

public abstract class WhyAreYouNotInliningReporter {

  public static WhyAreYouNotInliningReporter createFor(
      DexEncodedMethod callee, AppView<AppInfoWithLiveness> appView, DexEncodedMethod context) {
    if (appView.appInfo().whyAreYouNotInlining.contains(callee.method)) {
      return new WhyAreYouNotInliningReporterImpl(
          callee, context, appView.options().testing.whyAreYouNotInliningConsumer);
    }
    return NopWhyAreYouNotInliningReporter.getInstance();
  }

  public static void handleInvokeWithUnknownTarget(
      InvokeMethod invoke, AppView<AppInfoWithLiveness> appView, DexEncodedMethod context) {
    if (appView.appInfo().whyAreYouNotInlining.isEmpty()) {
      return;
    }

    Collection<DexEncodedMethod> possibleTargets =
        invoke.lookupTargets(appView, context.method.holder);
    if (possibleTargets == null) {
      // In principle, this invoke might target any method in the program, but we do not want to
      // report a message for each of the methods in `AppInfoWithLiveness#whyAreYouNotInlining`,
      // since that would almost never be useful.
      return;
    }

    for (DexEncodedMethod possibleTarget : possibleTargets) {
      createFor(possibleTarget, appView, context).reportUnknownTarget();
    }
  }

  public abstract void reportBlacklisted();

  public abstract void reportCallerNotSameClass();

  public abstract void reportCallerNotSameNest();

  public abstract void reportCallerNotSamePackage();

  public abstract void reportCallerNotSubtype();

  public abstract void reportClasspathMethod();

  public abstract void reportInaccessible();

  public abstract void reportIncorrectArity(int numberOfArguments, int arity);

  public abstract void reportInlineeDoesNotHaveCode();

  public abstract void reportInlineeNotInliningCandidate();

  public abstract void reportInlineeNotProcessed();

  public abstract void reportInlineeNotSimple();

  public abstract void reportInlineeRefersToClassesNotInMainDex();

  public abstract void reportInliningAcrossFeatureSplit();

  public abstract void reportInstructionBudgetIsExceeded();

  public abstract void reportInvalidDoubleInliningCandidate();

  public abstract void reportInvalidInliningReason(Reason reason, Set<Reason> validInliningReasons);

  public abstract void reportLibraryMethod();

  public abstract void reportMarkedAsNeverInline();

  public abstract void reportMustTriggerClassInitialization();

  public abstract void reportNoInliningIntoConstructorsWhenGeneratingClassFiles();

  public abstract void reportPinned();

  public abstract void reportPotentialExplosionInExceptionalControlFlowResolutionBlocks(
      int estimatedNumberOfControlFlowResolutionBlocks, int threshold);

  public abstract void reportProcessedConcurrently();

  public abstract void reportReceiverDefinitelyNull();

  public abstract void reportReceiverMaybeNull();

  public abstract void reportRecursiveMethod();

  abstract void reportUnknownTarget();

  public abstract void reportUnsafeConstructorInliningDueToFinalFieldAssignment(
      InstancePut instancePut);

  public abstract void reportUnsafeConstructorInliningDueToIndirectConstructorCall(
      InvokeDirect invoke);

  public abstract void reportUnsafeConstructorInliningDueToUninitializedObjectUse(Instruction user);

  public abstract void reportWillExceedInstructionBudget(int numberOfInstructions, int threshold);

  public abstract void reportWillExceedMaxInliningDepth(int actualInliningDepth, int threshold);

  public abstract void reportWillExceedMonitorEnterValuesBudget(
      int numberOfMonitorEnterValuesAfterInlining, int threshold);

  public abstract boolean unsetReasonHasBeenReportedFlag();
}
