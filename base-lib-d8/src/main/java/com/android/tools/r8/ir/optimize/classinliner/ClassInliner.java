// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.classinliner;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionOrPhi;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.desugar.LambdaRewriter;
import com.android.tools.r8.ir.optimize.CodeRewriter;
import com.android.tools.r8.ir.optimize.Inliner;
import com.android.tools.r8.ir.optimize.InliningOracle;
import com.android.tools.r8.ir.optimize.classinliner.InlineCandidateProcessor.IllegalClassInlinerStateException;
import com.android.tools.r8.ir.optimize.inliner.InliningIRProvider;
import com.android.tools.r8.ir.optimize.string.StringOptimizer;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ClassInliner {

  enum EligibilityStatus {
    // Used by InlineCandidateProcessor#isInstanceEligible
    NON_CLASS_TYPE,
    NOT_A_SINGLETON_FIELD,
    RETRIEVAL_MAY_HAVE_SIDE_EFFECTS,
    UNKNOWN_TYPE,
    UNUSED_INSTANCE,

    // Used by isClassEligible
    NON_PROGRAM_CLASS,
    ABSTRACT_OR_INTERFACE,
    NEVER_CLASS_INLINE,
    IS_PINNED_TYPE,
    HAS_FINALIZER,
    TRIGGER_CLINIT,

    // Used by InlineCandidateProcessor#isClassAndUsageEligible
    HAS_CLINIT,
    HAS_INSTANCE_FIELDS,
    NON_FINAL_TYPE,
    NOT_INITIALIZED_AT_INIT,
    PINNED_FIELD,

    ELIGIBLE
  }

  private final LambdaRewriter lambdaRewriter;
  private final ConcurrentHashMap<DexClass, EligibilityStatus> knownClasses =
      new ConcurrentHashMap<>();

  public ClassInliner(LambdaRewriter lambdaRewriter) {
    this.lambdaRewriter = lambdaRewriter;
  }

  private void logEligibilityStatus(
      DexEncodedMethod context, Instruction root, EligibilityStatus status) {
    if (Log.ENABLED && Log.isLoggingEnabledFor(ClassInliner.class)) {
      Log.info(getClass(), "At %s,", context.toSourceString());
      Log.info(getClass(), "ClassInlining eligibility of `%s`: %s.", root, status);
    }
  }

  private void logIneligibleUser(
      DexEncodedMethod context, Instruction root, InstructionOrPhi ineligibleUser) {
    if (Log.ENABLED && Log.isLoggingEnabledFor(ClassInliner.class)) {
      Log.info(getClass(), "At %s,", context.toSourceString());
      Log.info(getClass(), "Ineligible user of `%s`: `%s`.", root, ineligibleUser);
    }
  }

  // Process method code and inline eligible class instantiations, in short:
  //
  // - collect all 'new-instance' and 'static-get' instructions (called roots below) in
  // the original code. Note that class inlining, if happens, mutates code and may add
  // new root instructions. Processing them as well is possible, but does not seem to
  // bring much value.
  //
  // - for each 'new-instance' root we check if it is eligible for inlining, i.e:
  //     -> the class of the new instance is 'eligible' (see computeClassEligible(...))
  //     -> the instance is initialized with 'eligible' constructor (see comments in
  //        CodeRewriter::identifyClassInlinerEligibility(...))
  //     -> has only 'eligible' uses, i.e:
  //          * as a receiver of a field read/write for a field defined in same class
  //            as method.
  //          * as a receiver of virtual or interface call with single target being
  //            an eligible method according to identifyClassInlinerEligibility(...);
  //            NOTE: if method receiver is used as a return value, the method call
  //            should ignore return value
  //
  // - for each 'static-get' root we check if it is eligible for inlining, i.e:
  //     -> the class of the new instance is 'eligible' (see computeClassEligible(...))
  //        *and* has a trivial class constructor (see CoreRewriter::computeClassInitializerInfo
  //        and description in isClassAndUsageEligible(...)) initializing the field root reads
  //     -> has only 'eligible' uses, (see above)
  //
  // - inline eligible root instructions, i.e:
  //     -> force inline methods called on the instance (including the initializer);
  //        (this may introduce additional instance field reads/writes on the receiver)
  //     -> replace instance field reads with appropriate values calculated based on
  //        fields writes
  //     -> remove the call to superclass initializer (if root is 'new-instance')
  //     -> remove all field writes
  //     -> remove root instructions
  //
  // For example:
  //
  // Original code:
  //   class C {
  //     static class L {
  //       final int x;
  //       L(int x) {
  //         this.x = x;
  //       }
  //       int getX() {
  //         return x;
  //       }
  //     }
  //     static class F {
  //       final static F I = new F();
  //       int getX() {
  //         return 123;
  //       }
  //     }
  //     static int method1() {
  //       return new L(1).x;
  //     }
  //     static int method2() {
  //       return new L(1).getX();
  //     }
  //     static int method3() {
  //       return F.I.getX();
  //     }
  //   }
  //
  // Code after class C is 'inlined':
  //   class C {
  //     static int method1() {
  //       return 1;
  //     }
  //     static int method2() {
  //       return 1;
  //     }
  //     static int method3() {
  //       return 123;
  //     }
  //   }
  //
  public final void processMethodCode(
      AppView<AppInfoWithLiveness> appView,
      CodeRewriter codeRewriter,
      StringOptimizer stringOptimizer,
      DexEncodedMethod method,
      IRCode code,
      Predicate<DexEncodedMethod> isProcessedConcurrently,
      Inliner inliner,
      Supplier<InliningOracle> defaultOracle) {

    // Collect all the new-instance and static-get instructions in the code before inlining.
    List<Instruction> roots =
        Streams.stream(code.instructionIterator())
            .filter(insn -> insn.isNewInstance() || insn.isStaticGet())
            .collect(Collectors.toList());

    // We loop inlining iterations until there was no inlining, but still use same set
    // of roots to avoid infinite inlining. Looping makes possible for some roots to
    // become eligible after other roots are inlined.

    boolean anyInlinedMethods = false;
    boolean repeat;
    do {
      repeat = false;

      Iterator<Instruction> rootsIterator = roots.iterator();
      while (rootsIterator.hasNext()) {
        Instruction root = rootsIterator.next();
        InlineCandidateProcessor processor =
            new InlineCandidateProcessor(
                appView,
                lambdaRewriter,
                inliner,
                clazz -> isClassEligible(appView, clazz),
                isProcessedConcurrently,
                method,
                root);

        // Assess eligibility of instance and class.
        EligibilityStatus status = processor.isInstanceEligible();
        if (status != EligibilityStatus.ELIGIBLE) {
          logEligibilityStatus(code.method, root, status);
          // This root will never be inlined.
          rootsIterator.remove();
          continue;
        }
        status = processor.isClassAndUsageEligible();
        logEligibilityStatus(code.method, root, status);
        if (status != EligibilityStatus.ELIGIBLE) {
          // This root will never be inlined.
          rootsIterator.remove();
          continue;
        }

        // Assess users eligibility and compute inlining of direct calls and extra methods needed.
        InstructionOrPhi ineligibleUser = processor.areInstanceUsersEligible(defaultOracle);
        if (ineligibleUser != null) {
          // This root may succeed if users change in future.
          logIneligibleUser(code.method, root, ineligibleUser);
          continue;
        }

        assert processor.getReceivers().verifyReceiverSetsAreDisjoint();

        // Is inlining allowed.
        InliningIRProvider inliningIRProvider = new InliningIRProvider(appView, method, code);
        ClassInlinerCostAnalysis costAnalysis =
            new ClassInlinerCostAnalysis(
                appView, inliningIRProvider, processor.getReceivers().getDefiniteReceiverAliases());
        if (costAnalysis.willExceedInstructionBudget(
            code, processor.getDirectInlinees(), processor.getIndirectInlinees())) {
          // This root is unlikely to be inlined in the future.
          rootsIterator.remove();
          continue;
        }

        // Inline the class instance.
        try {
          anyInlinedMethods |= processor.processInlining(code, defaultOracle, inliningIRProvider);
        } catch (IllegalClassInlinerStateException e) {
          // We introduced a user that we cannot handle in the class inliner as a result of force
          // inlining. Abort gracefully from class inlining without removing the instance.
          //
          // Alternatively we would need to collect additional information about the behavior of
          // methods (which is bad for memory), or we would need to analyze the called methods
          // before inlining them. The latter could be good solution, since we are going to build IR
          // for the methods that need to be inlined anyway.
          assert appView.options().testing.allowClassInlinerGracefulExit;
          anyInlinedMethods = true;
        }

        assert inliningIRProvider.verifyIRCacheIsEmpty();

        // Restore normality.
        Set<Value> affectedValues = Sets.newIdentityHashSet();
        code.removeAllTrivialPhis(affectedValues);
        if (!affectedValues.isEmpty()) {
          new TypeAnalysis(appView).narrowing(affectedValues);
        }
        assert code.isConsistentSSA();
        rootsIterator.remove();
        repeat = true;
      }
    } while (repeat);

    if (anyInlinedMethods) {
      // If a method was inlined we may be able to remove check-cast instructions because we may
      // have more information about the types of the arguments at the call site. This is
      // particularly important for bridge methods.
      codeRewriter.removeTrivialCheckCastAndInstanceOfInstructions(code);
      // If a method was inlined we may be able to prune additional branches.
      codeRewriter.simplifyControlFlow(code);
      // If a method was inlined we may see more trivial computation/conversion of String.
      boolean isDebugMode =
          appView.options().debug || method.getOptimizationInfo().isReachabilitySensitive();
      if (!isDebugMode) {
        // Reflection/string optimization 3. trivial conversion/computation on const-string
        stringOptimizer.computeTrivialOperationsOnConstString(code);
        stringOptimizer.removeTrivialConversions(code);
      }
    }
  }

  private EligibilityStatus isClassEligible(AppView<AppInfoWithLiveness> appView, DexClass clazz) {
    EligibilityStatus eligible = knownClasses.get(clazz);
    if (eligible == null) {
      EligibilityStatus computed = computeClassEligible(appView, clazz);
      EligibilityStatus existing = knownClasses.putIfAbsent(clazz, computed);
      assert existing == null || existing == computed;
      eligible = existing == null ? computed : existing;
    }
    return eligible;
  }

  // Class is eligible for this optimization. Eligibility implementation:
  //   - is not an abstract class or interface
  //   - does not declare finalizer
  //   - does not trigger any static initializers except for its own
  private EligibilityStatus computeClassEligible(
      AppView<AppInfoWithLiveness> appView, DexClass clazz) {
    if (clazz == null) {
      return EligibilityStatus.UNKNOWN_TYPE;
    }
    if (clazz.isNotProgramClass()) {
      return EligibilityStatus.NON_PROGRAM_CLASS;
    }
    if (clazz.isAbstract() || clazz.isInterface()) {
      return EligibilityStatus.ABSTRACT_OR_INTERFACE;
    }
    if (appView.appInfo().neverClassInline.contains(clazz.type)) {
      return EligibilityStatus.NEVER_CLASS_INLINE;
    }
    if (appView.appInfo().isPinned(clazz.type)) {
      return EligibilityStatus.IS_PINNED_TYPE;
    }

    // Class must not define finalizer.
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      if (method.method.name == dexItemFactory.finalizeMethodName
          && method.method.proto == dexItemFactory.objectMethods.finalize.proto) {
        return EligibilityStatus.HAS_FINALIZER;
      }
    }

    // Check for static initializers in this class or any of interfaces it implements.
    if (clazz.initializationOfParentTypesMayHaveSideEffects(appView)) {
      return EligibilityStatus.TRIGGER_CLINIT;
    }
    return EligibilityStatus.ELIGIBLE;
  }
}
