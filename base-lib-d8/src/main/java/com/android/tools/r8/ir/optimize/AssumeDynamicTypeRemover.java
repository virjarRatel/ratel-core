// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.Assume.DynamicTypeAssumption;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 * When we have Assume instructions with a DynamicTypeAssumption we generally verify that the
 * dynamic type in the Assume node is always at least as precise as the static type of the
 * corresponding value.
 *
 * <p>Therefore, when this property may no longer hold for an Assume instruction, we need to remove
 * it.
 *
 * <p>This class is a helper class to remove these instructions. Unlike {@link
 * CodeRewriter#removeAssumeInstructions} this class does not unconditionally remove all Assume
 * instructions, not does it remove all Assume instructions with a DynamicTypeAssumption.
 */
public class AssumeDynamicTypeRemover {

  private final AppView<?> appView;
  private final IRCode code;

  private final Set<Value> affectedValues = Sets.newIdentityHashSet();
  private final Set<Assume<DynamicTypeAssumption>> assumeDynamicTypeInstructionsToRemove =
      Sets.newIdentityHashSet();

  private boolean mayHaveIntroducedTrivialPhi = false;

  public AssumeDynamicTypeRemover(AppView<?> appView, IRCode code) {
    this.appView = appView;
    this.code = code;
  }

  public boolean mayHaveIntroducedTrivialPhi() {
    return mayHaveIntroducedTrivialPhi;
  }

  public void markForRemoval(Assume<DynamicTypeAssumption> assumeDynamicTypeInstruction) {
    assumeDynamicTypeInstructionsToRemove.add(assumeDynamicTypeInstruction);
  }

  public void markUsersForRemoval(Value value) {
    for (Instruction user : value.aliasedUsers()) {
      if (user.isAssumeDynamicType()) {
        assert value.numberOfAllUsers() == 1
            : "Expected value flowing into Assume<DynamicTypeAssumption> instruction to have a "
                + "unique user.";
        markForRemoval(user.asAssumeDynamicType());
      }
    }
  }

  public void removeIfMarked(
      Assume<DynamicTypeAssumption> assumeDynamicTypeInstruction,
      InstructionListIterator instructionIterator) {
    if (assumeDynamicTypeInstructionsToRemove.remove(assumeDynamicTypeInstruction)) {
      Value inValue = assumeDynamicTypeInstruction.src();
      Value outValue = assumeDynamicTypeInstruction.outValue();

      // Check if we need to run the type analysis for the affected values of the out-value.
      if (!outValue.getTypeLattice().equals(inValue.getTypeLattice())) {
        affectedValues.addAll(outValue.affectedValues());
      }

      if (outValue.numberOfPhiUsers() > 0) {
        mayHaveIntroducedTrivialPhi = true;
      }

      outValue.replaceUsers(inValue);
      instructionIterator.removeOrReplaceByDebugLocalRead();
    }
  }

  public void removeMarkedInstructions(Set<BasicBlock> blocksToBeRemoved) {
    if (!assumeDynamicTypeInstructionsToRemove.isEmpty()) {
      for (BasicBlock block : code.blocks) {
        if (blocksToBeRemoved.contains(block)) {
          continue;
        }
        InstructionListIterator instructionIterator = block.listIterator(code);
        while (instructionIterator.hasNext()) {
          Instruction instruction = instructionIterator.next();
          if (instruction.isAssumeDynamicType()) {
            removeIfMarked(instruction.asAssumeDynamicType(), instructionIterator);
          }
        }
      }
    }
  }

  public void finish() {
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
  }
}
