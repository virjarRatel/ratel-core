// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.utils.StringUtils;
import java.io.PrintStream;
import java.util.Set;

class WhyAreYouNotInliningReporterImpl extends WhyAreYouNotInliningReporter {

  private final DexEncodedMethod callee;
  private final DexEncodedMethod context;
  private final PrintStream output;

  private boolean reasonHasBeenReported = false;

  WhyAreYouNotInliningReporterImpl(
      DexEncodedMethod callee, DexEncodedMethod context, PrintStream output) {
    this.callee = callee;
    this.context = context;
    this.output = output;
  }

  private void print(String reason) {
    output.print("Method `");
    output.print(callee.method.toSourceString());
    output.print("` was not inlined into `");
    output.print(context.method.toSourceString());
    if (reason != null) {
      output.print("`: ");
      output.println(reason);
    } else {
      output.println("`.");
    }
    reasonHasBeenReported = true;
  }

  private void printWithExceededThreshold(
      String reason, String description, int value, int threshold) {
    print(reason + " (" + description + ": " + value + ", threshold: " + threshold + ").");
  }

  @Override
  public void reportBlacklisted() {
    print("method is blacklisted from inlining.");
  }

  @Override
  public void reportCallerNotSameClass() {
    print("inlinee can only be inlined into methods in the same class.");
  }

  @Override
  public void reportCallerNotSameNest() {
    print("inlinee can only be inlined into methods in the same class (and its nest members).");
  }

  @Override
  public void reportCallerNotSamePackage() {
    print(
        "inlinee can only be inlined into methods in the same package "
            + "(declared package private or accesses package private type or member).");
  }

  @Override
  public void reportCallerNotSubtype() {
    print(
        "inlinee can only be inlined into methods in the same package and methods in subtypes of "
            + "the inlinee's enclosing class"
            + "(declared protected or accesses protected type or member).");
  }

  @Override
  public void reportClasspathMethod() {
    print("inlinee is on the classpath.");
  }

  @Override
  public void reportInaccessible() {
    print("inlinee is not accessible from the caller context.");
  }

  @Override
  public void reportIncorrectArity(int numberOfArguments, int arity) {
    print(
        "number of arguments ("
            + numberOfArguments
            + ") does not match arity of method ("
            + arity
            + ").");
  }

  @Override
  public void reportInlineeDoesNotHaveCode() {
    print("inlinee does not have code.");
  }

  @Override
  public void reportInlineeNotInliningCandidate() {
    print("unsupported instruction in inlinee.");
  }

  @Override
  public void reportInlineeNotProcessed() {
    print("inlinee not processed yet.");
  }

  @Override
  public void reportInlineeNotSimple() {
    print(
        "not inlining due to code size heuristic "
            + "(inlinee may have multiple callers and is not considered trivial).");
  }

  @Override
  public void reportInlineeRefersToClassesNotInMainDex() {
    print(
        "inlining could increase the main dex size "
            + "(caller is in main dex and inlinee refers to classes not in main dex).");
  }

  @Override
  public void reportInliningAcrossFeatureSplit() {
    print("cannot inline across feature splits.");
  }

  @Override
  public void reportInstructionBudgetIsExceeded() {
    print("caller's instruction budget is exceeded.");
  }

  @Override
  public void reportInvalidDoubleInliningCandidate() {
    print("inlinee is invoked more than once and could not be inlined into all call sites.");
  }

  @Override
  public void reportInvalidInliningReason(Reason reason, Set<Reason> validInliningReasons) {
    print(
        "not a valid inlining reason (was: "
            + reason
            + ", allowed: one of "
            + StringUtils.join(validInliningReasons, ", ")
            + ").");
  }

  @Override
  public void reportLibraryMethod() {
    print("inlinee is a library method.");
  }

  @Override
  public void reportMarkedAsNeverInline() {
    print("method is marked by a -neverinline rule.");
  }

  @Override
  public void reportMustTriggerClassInitialization() {
    print(
        "cannot guarantee that the enclosing class of the inlinee is guaranteed to be class "
            + "initialized before the first side-effecting instruction in the inlinee.");
  }

  @Override
  public void reportNoInliningIntoConstructorsWhenGeneratingClassFiles() {
    print("inlining into constructors not supported when generating class files.");
  }

  @Override
  public void reportPinned() {
    print("method is kept by a Proguard configuration rule.");
  }

  @Override
  public void reportPotentialExplosionInExceptionalControlFlowResolutionBlocks(
      int estimatedNumberOfControlFlowResolutionBlocks, int threshold) {
    printWithExceededThreshold(
        "could lead to an explosion in the number of moves due to the exceptional control flow",
        "estimated number of control flow resolution blocks",
        estimatedNumberOfControlFlowResolutionBlocks,
        threshold);
  }

  @Override
  public void reportProcessedConcurrently() {
    print(
        "could lead to nondeterministic output since the inlinee is being optimized concurrently.");
  }

  @Override
  public void reportReceiverDefinitelyNull() {
    print("the receiver is always null at the call site.");
  }

  @Override
  public void reportReceiverMaybeNull() {
    print("the receiver may be null at the call site.");
  }

  @Override
  public void reportRecursiveMethod() {
    print("recursive calls are not inlined.");
  }

  @Override
  public void reportUnknownTarget() {
    print("could not find a single target.");
  }

  @Override
  public void reportUnsafeConstructorInliningDueToFinalFieldAssignment(InstancePut instancePut) {
    print(
        "final field `"
            + instancePut.getField()
            + "` must be initialized in a constructor of `"
            + callee.method.holder.toSourceString()
            + "`.");
  }

  @Override
  public void reportUnsafeConstructorInliningDueToIndirectConstructorCall(InvokeDirect invoke) {
    print(
        "must invoke a constructor from the class being instantiated (would invoke `"
            + invoke.getInvokedMethod().toSourceString()
            + "`).");
  }

  @Override
  public void reportUnsafeConstructorInliningDueToUninitializedObjectUse(Instruction user) {
    print("would lead to use of uninitialized object (user: `" + user.toString() + "`).");
  }

  @Override
  public void reportWillExceedInstructionBudget(int numberOfInstructions, int threshold) {
    printWithExceededThreshold(
        "would exceed the caller's instruction budget",
        "number of instructions in inlinee",
        numberOfInstructions,
        threshold);
  }

  @Override
  public void reportWillExceedMaxInliningDepth(int actualInliningDepth, int threshold) {
    printWithExceededThreshold(
        "would exceed the maximum inlining depth",
        "current inlining depth",
        actualInliningDepth,
        threshold);
  }

  @Override
  public void reportWillExceedMonitorEnterValuesBudget(
      int numberOfMonitorEnterValuesAfterInlining, int threshold) {
    printWithExceededThreshold(
        "could negatively impact register allocation due to the number of monitor instructions",
        "estimated number of locks after inlining",
        numberOfMonitorEnterValuesAfterInlining,
        threshold);
  }

  @Override
  public boolean unsetReasonHasBeenReportedFlag() {
    assert reasonHasBeenReported;
    reasonHasBeenReported = false;
    return true;
  }
}
