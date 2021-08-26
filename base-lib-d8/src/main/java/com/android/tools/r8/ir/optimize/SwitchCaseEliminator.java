// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.Goto;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.IntSwitch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Comparator;
import java.util.function.IntPredicate;

/** Helper to remove dead switch cases from a switch instruction. */
class SwitchCaseEliminator {

  private final BasicBlock block;
  private final BasicBlock defaultTarget;
  private final InstructionListIterator iterator;
  private final IntSwitch theSwitch;

  private boolean mayHaveIntroducedUnreachableBlocks = false;
  private IntSet switchCasesToBeRemoved;

  SwitchCaseEliminator(IntSwitch theSwitch, InstructionListIterator iterator) {
    this.block = theSwitch.getBlock();
    this.defaultTarget = theSwitch.fallthroughBlock();
    this.iterator = iterator;
    this.theSwitch = theSwitch;
  }

  private boolean allSwitchCasesMarkedForRemoval() {
    assert switchCasesToBeRemoved != null;
    return switchCasesToBeRemoved.size() == theSwitch.numberOfKeys();
  }

  private boolean canBeOptimized() {
    assert switchCasesToBeRemoved == null || !switchCasesToBeRemoved.isEmpty();
    return switchCasesToBeRemoved != null;
  }

  boolean mayHaveIntroducedUnreachableBlocks() {
    return mayHaveIntroducedUnreachableBlocks;
  }

  void markSwitchCaseForRemoval(int i) {
    if (switchCasesToBeRemoved == null) {
      switchCasesToBeRemoved = new IntOpenHashSet();
    }
    switchCasesToBeRemoved.add(i);
  }

  boolean optimize() {
    if (canBeOptimized()) {
      int originalNumberOfSuccessors = block.getSuccessors().size();
      unlinkDeadSuccessors();
      if (allSwitchCasesMarkedForRemoval()) {
        // Replace switch with a simple goto since only the fall through is left.
        replaceSwitchByGoto();
      } else {
        // Replace switch by a new switch where the dead switch cases have been removed.
        replaceSwitchByOptimizedSwitch(originalNumberOfSuccessors);
      }
      return true;
    }
    return false;
  }

  private void unlinkDeadSuccessors() {
    IntPredicate successorHasBecomeDeadPredicate = computeSuccessorHasBecomeDeadPredicate();
    IntList successorIndicesToBeRemoved = new IntArrayList();
    for (int i = 0; i < block.getSuccessors().size(); i++) {
      if (successorHasBecomeDeadPredicate.test(i)) {
        BasicBlock successor = block.getSuccessors().get(i);
        successor.removePredecessor(block, null);
        successorIndicesToBeRemoved.add(i);
        if (successor.getPredecessors().isEmpty()) {
          mayHaveIntroducedUnreachableBlocks = true;
        }
      }
    }
    successorIndicesToBeRemoved.sort(Comparator.naturalOrder());
    block.removeSuccessorsByIndex(successorIndicesToBeRemoved);
  }

  private IntPredicate computeSuccessorHasBecomeDeadPredicate() {
    int[] numberOfControlFlowEdgesToBlockWithIndex = new int[block.getSuccessors().size()];
    for (int i = 0; i < theSwitch.numberOfKeys(); i++) {
      if (!switchCasesToBeRemoved.contains(i)) {
        int targetBlockIndex = theSwitch.getTargetBlockIndex(i);
        numberOfControlFlowEdgesToBlockWithIndex[targetBlockIndex] += 1;
      }
    }
    numberOfControlFlowEdgesToBlockWithIndex[theSwitch.getFallthroughBlockIndex()] += 1;
    for (int i : block.getCatchHandlersWithSuccessorIndexes().getUniqueTargets()) {
      numberOfControlFlowEdgesToBlockWithIndex[i] += 1;
    }
    return i -> numberOfControlFlowEdgesToBlockWithIndex[i] == 0;
  }

  private void replaceSwitchByGoto() {
    iterator.replaceCurrentInstruction(new Goto(defaultTarget));
  }

  private void replaceSwitchByOptimizedSwitch(int originalNumberOfSuccessors) {
    int[] targetBlockIndexOffset = new int[originalNumberOfSuccessors];
    for (int i : switchCasesToBeRemoved) {
      int targetBlockIndex = theSwitch.getTargetBlockIndex(i);
      // Add 1 because we are interested in the number of targets removed before a given index.
      if (targetBlockIndex + 1 < targetBlockIndexOffset.length) {
        targetBlockIndexOffset[targetBlockIndex + 1] = 1;
      }
    }

    for (int i = 1; i < targetBlockIndexOffset.length; i++) {
      targetBlockIndexOffset[i] += targetBlockIndexOffset[i - 1];
    }

    int newNumberOfKeys = theSwitch.numberOfKeys() - switchCasesToBeRemoved.size();
    int[] newKeys = new int[newNumberOfKeys];
    int[] newTargetBlockIndices = new int[newNumberOfKeys];
    for (int i = 0, j = 0; i < theSwitch.numberOfKeys(); i++) {
      if (!switchCasesToBeRemoved.contains(i)) {
        newKeys[j] = theSwitch.getKey(i);
        newTargetBlockIndices[j] =
            theSwitch.getTargetBlockIndex(i)
                - targetBlockIndexOffset[theSwitch.getTargetBlockIndex(i)];
        assert newTargetBlockIndices[j] < block.getSuccessors().size();
        assert newTargetBlockIndices[j] != theSwitch.getFallthroughBlockIndex();
        j++;
      }
    }

    assert targetBlockIndexOffset[theSwitch.getFallthroughBlockIndex()] == 0;

    iterator.replaceCurrentInstruction(
        new IntSwitch(
            theSwitch.value(),
            newKeys,
            newTargetBlockIndices,
            theSwitch.getFallthroughBlockIndex()));
  }
}
