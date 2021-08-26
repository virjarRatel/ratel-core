// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap.FastSortedEntrySet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Canonicalize idempotent function calls.
 *
 * <p>For example,
 *
 *   v1 <- const4 0x0
 *   ...
 *   vx <- invoke-static { v1 } Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
 *   ...
 *   vy <- invoke-static { v1 } Ljava/lang/Boolean;->valueOf(Z);Ljava/lang/Boolean;
 *   ...
 *   vz <- invoke-static { v1 } Ljava/lang/Boolean;->valueOf(Z);Ljava/lang/Boolean;
 *   ...
 *
 * ~>
 *
 *   v1 <- const4 0x0
 *   v2 <- invoke-static { v1 } Ljava/lang/Boolean;->valueOf(Z);Ljava/lang/Boolean;
 *   // Update users of vx, vy, and vz.
 */
public class IdempotentFunctionCallCanonicalizer {

  // Threshold to limit the number of invocation canonicalization.
  private static final int MAX_CANONICALIZED_CALL = 15;

  private final AppView<?> appView;
  private final DexItemFactory factory;

  private int numberOfLibraryCallCanonicalization = 0;
  private int numberOfProgramCallCanonicalization = 0;
  private final Object2IntMap<Long> histogramOfCanonicalizationCandidatesPerMethod;

  public IdempotentFunctionCallCanonicalizer(AppView<?> appView) {
    this.appView = appView;
    this.factory = appView.dexItemFactory();
    if (Log.ENABLED) {
      histogramOfCanonicalizationCandidatesPerMethod = new Object2IntArrayMap<>();
    } else {
      histogramOfCanonicalizationCandidatesPerMethod = null;
    }
  }

  public void logResults() {
    assert Log.ENABLED;
    Log.info(getClass(),
        "# invoke canonicalization (library): %s", numberOfLibraryCallCanonicalization);
    Log.info(getClass(),
        "# invoke canonicalization (program): %s", numberOfProgramCallCanonicalization);
    assert histogramOfCanonicalizationCandidatesPerMethod != null;
    Log.info(getClass(), "------ histogram of invoke canonicalization candidates ------");
    histogramOfCanonicalizationCandidatesPerMethod.forEach(
        (length, count) ->
            Log.info(
                getClass(),
                "%s: %s (%s)",
                length,
                StringUtils.times("*", Math.min(count, 53)),
                count));
  }

  public void canonicalize(IRCode code) {
    Object2ObjectLinkedOpenCustomHashMap<InvokeMethod, List<Value>> returnValues =
        new Object2ObjectLinkedOpenCustomHashMap<>(
            new Strategy<InvokeMethod>() {
              @Override
              public int hashCode(InvokeMethod o) {
                return o.getInvokedMethod().hashCode() * 31 + o.inValues().hashCode();
              }

              @Override
              public boolean equals(InvokeMethod a, InvokeMethod b) {
                assert a == null || !a.outValue().hasLocalInfo();
                assert b == null || !b.outValue().hasLocalInfo();
                return a == b
                    || (a != null && b != null && a.identicalNonValueNonPositionParts(b)
                        && a.inValues().equals(b.inValues()));
              }
            });

    DexType context = code.method.method.holder;
    // Collect invocations along with arguments.
    for (BasicBlock block : code.blocks) {
      for (Instruction current : block.getInstructions()) {
        if (!current.isInvokeMethod()) {
          continue;
        }
        InvokeMethod invoke = current.asInvokeMethod();
        // If the out value of the current invocation is not used and removed, we don't care either.
        if (invoke.outValue() == null) {
          continue;
        }
        // Invocations with local info cannot be canonicalized.
        if (current.outValue().hasLocalInfo()) {
          continue;
        }
        // Interested in known-to-be idempotent methods.
        if (!isIdempotentLibraryMethodInvoke(invoke)) {
          if (!appView.enableWholeProgramOptimizations()) {
            // Give up in D8
            continue;
          }
          assert appView.appInfo().hasLiveness();
          // Check if the call has a single target; that target is side effect free; and
          // that target's output depends only on arguments.
          DexEncodedMethod target = invoke.lookupSingleTarget(appView.withLiveness(), context);
          if (target == null
              || target.getOptimizationInfo().mayHaveSideEffects()
              || !target.getOptimizationInfo().returnValueOnlyDependsOnArguments()) {
            continue;
          }
          // Verify that the target method is accessible in the current context.
          if (!isMemberVisibleFromOriginalContext(
              appView, context, target.method.holder, target.accessFlags)) {
            continue;
          }
          // Check if the call could throw a NPE as a result of the receiver being null.
          if (current.isInvokeMethodWithReceiver()) {
            Value receiver = current.asInvokeMethodWithReceiver().getReceiver().getAliasedValue();
            if (receiver.getTypeLattice().isNullable()) {
              continue;
            }
          }
        }
        // TODO(b/145259212): Use dominant tree to extend it to non-canonicalized in values?
        // For now, interested in inputs that are also canonicalized constants.
        boolean invocationCanBeMovedToEntryBlock = true;
        for (Value in : current.inValues()) {
          if (in.isPhi()
              || !in.definition.isConstInstruction()
              || in.definition.getBlock().getNumber() != 0) {
            invocationCanBeMovedToEntryBlock = false;
            break;
          }
        }
        if (!invocationCanBeMovedToEntryBlock) {
          continue;
        }
        List<Value> oldReturnValues = returnValues.computeIfAbsent(invoke, k -> new ArrayList<>());
        oldReturnValues.add(current.outValue());
      }
    }

    if (returnValues.isEmpty()) {
      return;
    }

    // Double-check the entry block does not have catch handlers.
    assert !code.entryBlock().hasCatchHandlers();

    // InvokeMethod is not treated as dead code explicitly, i.e., cannot rely on dead code remover.
    Map<InvokeMethod, Value> deadInvocations = Maps.newHashMap();

    FastSortedEntrySet<InvokeMethod, List<Value>> entries = returnValues.object2ObjectEntrySet();
    if (Log.ENABLED && Log.isLoggingEnabledFor(IdempotentFunctionCallCanonicalizer.class)) {
      Long numOfCandidates = entries.stream().filter(a -> a.getValue().size() > 1).count();
      synchronized (histogramOfCanonicalizationCandidatesPerMethod) {
        int count = histogramOfCanonicalizationCandidatesPerMethod.getOrDefault(numOfCandidates, 0);
        histogramOfCanonicalizationCandidatesPerMethod.put(numOfCandidates, count + 1);
      }
    }
    entries.stream()
        .filter(a -> a.getValue().size() > 1)
        .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
        .limit(MAX_CANONICALIZED_CALL)
        .forEach((entry) -> {
          InvokeMethod invoke = entry.getKey();
          if (Log.ENABLED) {
            if (factory.libraryMethodsWithReturnValueDependingOnlyOnArguments
                .contains(invoke.getInvokedMethod())) {
              numberOfLibraryCallCanonicalization += entry.getValue().size() - 1;
            } else {
              numberOfProgramCallCanonicalization += entry.getValue().size() - 1;
            }
          }
          Value canonicalizedValue = code.createValue(
              invoke.outValue().getTypeLattice(), invoke.outValue().getLocalInfo());
          Invoke canonicalizedInvoke =
              Invoke.create(
                  invoke.getType(),
                  invoke.getInvokedMethod(),
                  null,
                  canonicalizedValue,
                  invoke.inValues());
          // Note that it is fine to use any position, since the invoke has no side effects, which
          // is guaranteed not to throw. That is, we will never have a stack trace with this call.
          // Nonetheless, here we pick the position of the very first invocation.
          Position firstInvocationPosition = entry.getValue().get(0).definition.getPosition();
          canonicalizedInvoke.setPosition(firstInvocationPosition);
          if (invoke.inValues().size() > 0) {
            insertCanonicalizedInvokeWithInValues(code, canonicalizedInvoke);
          } else {
            insertCanonicalizedInvokeWithoutInValues(code, canonicalizedInvoke);
          }
          for (Value oldOutValue : entry.getValue()) {
            deadInvocations.put(oldOutValue.definition.asInvokeMethod(), canonicalizedValue);
          }
        });

    if (!deadInvocations.isEmpty()) {
      for (BasicBlock block : code.blocks) {
        InstructionListIterator it = block.listIterator(code);
        while (it.hasNext()) {
          Instruction current = it.next();
          if (!current.isInvokeMethod()) {
            continue;
          }
          InvokeMethod invoke = current.asInvokeMethod();
          if (deadInvocations.containsKey(invoke)) {
            Value newOutValue = deadInvocations.get(invoke);
            assert newOutValue != null;
            invoke.outValue().replaceUsers(newOutValue);
            it.removeOrReplaceByDebugLocalRead();
          }
        }
      }
    }

    code.removeAllTrivialPhis();
    assert code.isConsistentSSA();
  }

  private boolean isIdempotentLibraryMethodInvoke(InvokeMethod invoke) {
    DexMethod invokedMethod = invoke.getInvokedMethod();
    Predicate<InvokeMethod> noSideEffectPredicate =
        factory.libraryMethodsWithoutSideEffects.get(invokedMethod);
    if (noSideEffectPredicate == null || !noSideEffectPredicate.test(invoke)) {
      return false;
    }
    return factory.libraryMethodsWithReturnValueDependingOnlyOnArguments.contains(invokedMethod);
  }

  private static void insertCanonicalizedInvokeWithInValues(
      IRCode code, Invoke canonicalizedInvoke) {
    BasicBlock entryBlock = code.entryBlock();
    // Insert the canonicalized invoke after in values.
    int numberOfInValuePassed = 0;
    InstructionListIterator it = entryBlock.listIterator(code);
    while (it.hasNext()) {
      Instruction current = it.next();
      if (current.hasOutValue() && canonicalizedInvoke.inValues().contains(current.outValue())) {
        numberOfInValuePassed++;
      }
      if (numberOfInValuePassed == canonicalizedInvoke.inValues().size()) {
        // If this invocation uses arguments and this iteration ends in the middle of Arguments,
        // proceed further so that Arguments can be packed first (as per entry block's properties).
        if (it.hasNext() && it.peekNext().isArgument()) {
          it.nextUntil(instr -> !instr.isArgument());
        }
        break;
      }
    }
    it.add(canonicalizedInvoke);
  }

  private static void insertCanonicalizedInvokeWithoutInValues(
      IRCode code, Invoke canonicalizedInvoke) {
    BasicBlock entryBlock = code.entryBlock();
    // Insert the canonicalized invocation at the start of the block right after the argument
    // instructions.
    InstructionListIterator it = entryBlock.listIterator(code);
    while (it.hasNext()) {
      if (!it.next().isArgument()) {
        it.previous();
        break;
      }
    }
    it.add(canonicalizedInvoke);
  }
}
