// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.utils.DequeUtils;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class IRCodeUtils {

  /**
   * Removes {@param instruction} if it is a {@link NewArrayEmpty} instruction, which only has
   * array-put users. Also removes all instructions that contribute to the computation of the
   * indices and the elements, if they end up being unused, even if the instructions may have side
   * effects (!).
   *
   * <p>Use with caution!
   */
  public static void removeArrayAndTransitiveInputsIfNotUsed(IRCode code, Instruction definition) {
    Deque<InstructionOrPhi> worklist = new ArrayDeque<>();
    if (definition.isConstNumber()) {
      // No need to explicitly remove `null`, it will be removed by ordinary dead code elimination
      // anyway.
      assert definition.asConstNumber().isZero();
      return;
    }

    if (definition.isNewArrayEmpty()) {
      Value arrayValue = definition.outValue();
      if (arrayValue.hasPhiUsers() || arrayValue.hasDebugUsers()) {
        return;
      }

      for (Instruction user : arrayValue.uniqueUsers()) {
        // If we encounter an Assume instruction here, we also need to consider indirect users.
        assert !user.isAssume();
        if (!user.isArrayPut()) {
          return;
        }
        worklist.add(user);
      }
      internalRemoveInstructionAndTransitiveInputsIfNotUsed(code, worklist);
      return;
    }

    assert false;
  }

  /**
   * Removes the given instruction and all the instructions that are used to define the in-values of
   * the given instruction, even if the instructions may have side effects (!).
   *
   * <p>Use with caution!
   */
  public static void removeInstructionAndTransitiveInputsIfNotUsed(
      IRCode code, Instruction instruction) {
    internalRemoveInstructionAndTransitiveInputsIfNotUsed(
        code, DequeUtils.newArrayDeque(instruction));
  }

  private static void internalRemoveInstructionAndTransitiveInputsIfNotUsed(
      IRCode code, Deque<InstructionOrPhi> worklist) {
    Set<InstructionOrPhi> removed = Sets.newIdentityHashSet();
    while (!worklist.isEmpty()) {
      InstructionOrPhi instructionOrPhi = worklist.removeFirst();
      if (removed.contains(instructionOrPhi)) {
        // Already removed.
        continue;
      }
      if (instructionOrPhi.isPhi()) {
        Phi current = instructionOrPhi.asPhi();
        if (!current.hasUsers() && !current.hasDebugUsers()) {
          boolean hasOtherPhiUserThanSelf = false;
          for (Phi phiUser : current.uniquePhiUsers()) {
            if (phiUser != current) {
              hasOtherPhiUserThanSelf = true;
              break;
            }
          }
          if (!hasOtherPhiUserThanSelf) {
            current.removeDeadPhi();
            for (Value operand : current.getOperands()) {
              worklist.add(operand.isPhi() ? operand.asPhi() : operand.definition);
            }
            removed.add(current);
          }
        }
      } else {
        Instruction current = instructionOrPhi.asInstruction();
        if (!current.hasOutValue() || !current.outValue().hasAnyUsers()) {
          current.getBlock().listIterator(code, current).removeOrReplaceByDebugLocalRead();
          for (Value inValue : current.inValues()) {
            worklist.add(inValue.isPhi() ? inValue.asPhi() : inValue.definition);
          }
          removed.add(current);
        }
      }
    }
  }
}
