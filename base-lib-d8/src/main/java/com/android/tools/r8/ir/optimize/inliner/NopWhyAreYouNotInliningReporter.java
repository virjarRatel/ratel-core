// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import java.util.Set;

public class NopWhyAreYouNotInliningReporter extends WhyAreYouNotInliningReporter {

  private static final NopWhyAreYouNotInliningReporter INSTANCE =
      new NopWhyAreYouNotInliningReporter();

  private NopWhyAreYouNotInliningReporter() {}

  public static NopWhyAreYouNotInliningReporter getInstance() {
    return INSTANCE;
  }

  @Override
  public void reportBlacklisted() {}

  @Override
  public void reportCallerNotSameClass() {}

  @Override
  public void reportCallerNotSameNest() {}

  @Override
  public void reportCallerNotSamePackage() {}

  @Override
  public void reportCallerNotSubtype() {}

  @Override
  public void reportClasspathMethod() {}

  @Override
  public void reportInaccessible() {}

  @Override
  public void reportIncorrectArity(int numberOfArguments, int arity) {}

  @Override
  public void reportInlineeDoesNotHaveCode() {}

  @Override
  public void reportInlineeNotInliningCandidate() {}

  @Override
  public void reportInlineeNotProcessed() {}

  @Override
  public void reportInlineeNotSimple() {}

  @Override
  public void reportInlineeRefersToClassesNotInMainDex() {}

  @Override
  public void reportInliningAcrossFeatureSplit() {}

  @Override
  public void reportInstructionBudgetIsExceeded() {}

  @Override
  public void reportInvalidDoubleInliningCandidate() {}

  @Override
  public void reportInvalidInliningReason(Reason reason, Set<Reason> validInliningReasons) {}

  @Override
  public void reportLibraryMethod() {}

  @Override
  public void reportMarkedAsNeverInline() {}

  @Override
  public void reportMustTriggerClassInitialization() {}

  @Override
  public void reportNoInliningIntoConstructorsWhenGeneratingClassFiles() {}

  @Override
  public void reportPinned() {}

  @Override
  public void reportPotentialExplosionInExceptionalControlFlowResolutionBlocks(
      int estimatedNumberOfControlFlowResolutionBlocks, int threshold) {}

  @Override
  public void reportProcessedConcurrently() {}

  @Override
  public void reportReceiverDefinitelyNull() {}

  @Override
  public void reportReceiverMaybeNull() {}

  @Override
  public void reportRecursiveMethod() {}

  @Override
  public void reportUnknownTarget() {}

  @Override
  public void reportUnsafeConstructorInliningDueToFinalFieldAssignment(InstancePut instancePut) {}

  @Override
  public void reportUnsafeConstructorInliningDueToIndirectConstructorCall(InvokeDirect invoke) {}

  @Override
  public void reportUnsafeConstructorInliningDueToUninitializedObjectUse(Instruction user) {}

  @Override
  public void reportWillExceedInstructionBudget(int numberOfInstructions, int threshold) {}

  @Override
  public void reportWillExceedMaxInliningDepth(int actualInliningDepth, int threshold) {}

  @Override
  public void reportWillExceedMonitorEnterValuesBudget(
      int numberOfMonitorEnterValuesAfterInlining, int threshold) {}

  @Override
  public boolean unsetReasonHasBeenReportedFlag() {
    return true;
  }
}
