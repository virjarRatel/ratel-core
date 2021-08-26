// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.Assume.NonNullAssumption;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CheckCast;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeInterface;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites all invoke-interface instructions that have a unique target on a class into
 * invoke-virtual with the corresponding unique target.
 */
public class Devirtualizer {

  private final AppView<AppInfoWithLiveness> appView;

  public Devirtualizer(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
  }

  public void devirtualizeInvokeInterface(IRCode code, DexType invocationContext) {
    Set<Value> affectedValues = Sets.newIdentityHashSet();
    Map<InvokeInterface, InvokeVirtual> devirtualizedCall = new IdentityHashMap<>();
    DominatorTree dominatorTree = new DominatorTree(code);
    Map<Value, Map<DexType, Value>> castedReceiverCache = new IdentityHashMap<>();
    Set<CheckCast> newCheckCastInstructions = Sets.newIdentityHashSet();

    ListIterator<BasicBlock> blocks = code.listIterator();
    while (blocks.hasNext()) {
      BasicBlock block = blocks.next();
      InstructionListIterator it = block.listIterator(code);
      while (it.hasNext()) {
        Instruction current = it.next();

        // (out <-) invoke-interface rcv_i, ... I#foo
        // ...  // could be split due to catch handlers
        // non_null_rcv <- non-null rcv_i
        //
        //   ~>
        //
        // rcv_c <- check-cast C rcv_i
        // (out <-) invoke-virtual rcv_c, ... C#foo
        // ...
        // non_null_rcv <- non-null rcv_c  // <- Update the input rcv to the non-null, too.
        if (current.isAssumeNonNull()) {
          Assume<NonNullAssumption> nonNull = current.asAssumeNonNull();
          Instruction origin = nonNull.origin();
          if (origin.isInvokeInterface()
              && !origin.asInvokeInterface().getReceiver().hasLocalInfo()
              && devirtualizedCall.containsKey(origin.asInvokeInterface())
              && origin.asInvokeInterface().getReceiver() == nonNull.getAliasForOutValue()) {
            InvokeVirtual devirtualizedInvoke = devirtualizedCall.get(origin.asInvokeInterface());

            // Extract the newly added check-cast instruction, if any.
            CheckCast newCheckCast = null;
            Value newReceiver = devirtualizedInvoke.getReceiver();
            if (!newReceiver.isPhi() && newReceiver.definition.isCheckCast()) {
              CheckCast definition = newReceiver.definition.asCheckCast();
              if (newCheckCastInstructions.contains(definition)) {
                newCheckCast = definition;
              }
            }

            if (newCheckCast != null) {
              // If this non-null instruction is dominated by the check-cast instruction, then
              // replace the in-value to the non-null instruction, since this gives us more precise
              // type information in the rest of the method. This should only be done, though, if
              // the out-value of the cast instruction is a more precise type than the in-value,
              // otherwise we could introduce type errors.
              Value oldReceiver = newCheckCast.object();
              TypeLatticeElement oldReceiverType = oldReceiver.getTypeLattice();
              TypeLatticeElement newReceiverType = newReceiver.getTypeLattice();
              if (newReceiverType.lessThanOrEqual(oldReceiverType, appView)
                  && dominatorTree.dominatedBy(block, devirtualizedInvoke.getBlock())) {
                assert nonNull.src() == oldReceiver;
                assert !oldReceiver.hasLocalInfo();
                oldReceiver.replaceSelectiveUsers(
                    newReceiver, ImmutableSet.of(nonNull), ImmutableMap.of());
              }
            }
          }
        }

        if (!current.isInvokeInterface()) {
          continue;
        }
        InvokeInterface invoke = current.asInvokeInterface();
        DexEncodedMethod target = invoke.lookupSingleTarget(appView, invocationContext);
        if (target == null) {
          continue;
        }
        DexType holderType = target.method.holder;
        DexClass holderClass = appView.definitionFor(holderType);
        // Make sure we are not landing on another interface, e.g., interface's default method.
        if (holderClass == null || holderClass.isInterface()) {
          continue;
        }
        // Due to the potential downcast below, make sure the new target holder is visible.
        ConstraintWithTarget visibility =
            ConstraintWithTarget.classIsVisible(invocationContext, holderType, appView);
        if (visibility == ConstraintWithTarget.NEVER) {
          continue;
        }

        InvokeVirtual devirtualizedInvoke =
            new InvokeVirtual(target.method, invoke.outValue(), invoke.inValues());
        it.replaceCurrentInstruction(devirtualizedInvoke);
        devirtualizedCall.put(invoke, devirtualizedInvoke);

        // We may need to add downcast together. E.g.,
        // i <- check-cast I o  // suppose it is known to be of type class A, not interface I
        // (out <-) invoke-interface i, ... I#foo
        //
        //  ~>
        //
        // i <- check-cast I o  // could be removed by {@link
        // CodeRewriter#removeTrivialCheckCastAndInstanceOfInstructions}.
        // a <- check-cast A i  // Otherwise ART verification error.
        // (out <-) invoke-virtual a, ... A#foo
        if (holderType != invoke.getInvokedMethod().holder) {
          Value receiver = invoke.getReceiver();
          TypeLatticeElement receiverTypeLattice = receiver.getTypeLattice();
          TypeLatticeElement castTypeLattice =
              TypeLatticeElement.fromDexType(
                  holderType, receiverTypeLattice.nullability(), appView);
          // Avoid adding trivial cast and up-cast.
          // We should not use strictlyLessThan(castType, receiverType), which detects downcast,
          // due to side-casts, e.g., A (unused) < I, B < I, and cast from A to B.
          if (!receiverTypeLattice.lessThanOrEqual(castTypeLattice, appView)) {
            Value newReceiver = null;
            // If this value is ever downcast'ed to the same holder type before, and that casted
            // value is safely accessible, i.e., the current line is dominated by that cast, use it.
            // Otherwise, we will see something like:
            // ...
            // a1 <- check-cast A i
            // invoke-virtual a1, ... A#m1 (from I#m1)
            // ...
            // a2 <- check-cast A i  // We should be able to reuse a1 here!
            // invoke-virtual a2, ... A#m2 (from I#m2)
            if (castedReceiverCache.containsKey(receiver)
                && castedReceiverCache.get(receiver).containsKey(holderType)) {
              Value cachedReceiver = castedReceiverCache.get(receiver).get(holderType);
              if (dominatorTree.dominatedBy(block, cachedReceiver.definition.getBlock())) {
                newReceiver = cachedReceiver;
              }
            }

            // No cached, we need a new downcast'ed receiver.
            if (newReceiver == null) {
              newReceiver = code.createValue(castTypeLattice);
              // Cache the new receiver with a narrower type to avoid redundant checkcast.
              if (!receiver.hasLocalInfo()) {
                castedReceiverCache.putIfAbsent(receiver, new IdentityHashMap<>());
                castedReceiverCache.get(receiver).put(holderType, newReceiver);
              }
              CheckCast checkCast = new CheckCast(newReceiver, receiver, holderType);
              checkCast.setPosition(invoke.getPosition());
              newCheckCastInstructions.add(checkCast);

              // We need to add this checkcast *before* the devirtualized invoke-virtual.
              assert it.peekPrevious() == devirtualizedInvoke;
              it.previous();
              // If the current block has catch handlers, split the new checkcast on its own block.
              // Because checkcast is also a throwing instr, we should split before adding it.
              // Otherwise, catch handlers are bound to a block with checkcast, not invoke IR.
              BasicBlock blockWithDevirtualizedInvoke =
                  block.hasCatchHandlers() ? it.split(code, blocks) : block;
              if (blockWithDevirtualizedInvoke != block) {
                // If we split, add the new checkcast at the end of the currently visiting block.
                it = block.listIterator(code, block.getInstructions().size());
                it.previous();
                it.add(checkCast);
                // Update the dominator tree after the split.
                dominatorTree = new DominatorTree(code);
                // Restore the cursor.
                it = blockWithDevirtualizedInvoke.listIterator(code);
                assert it.peekNext() == devirtualizedInvoke;
                it.next();
              } else {
                // Otherwise, just add it to the current block at the position of the iterator.
                it.add(checkCast);
                // Restore the cursor.
                assert it.peekNext() == devirtualizedInvoke;
                it.next();
              }
            }

            affectedValues.addAll(receiver.affectedValues());
            if (!receiver.hasLocalInfo()) {
              receiver.replaceSelectiveUsers(
                  newReceiver, ImmutableSet.of(devirtualizedInvoke), ImmutableMap.of());
            } else {
              receiver.removeUser(devirtualizedInvoke);
              devirtualizedInvoke.replaceValue(receiver, newReceiver);
            }
          }
        }
      }
    }
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
    assert code.isConsistentSSA();
  }
}
