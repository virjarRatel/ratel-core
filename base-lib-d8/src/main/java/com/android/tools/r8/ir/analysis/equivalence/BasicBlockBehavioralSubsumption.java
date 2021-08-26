// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.equivalence;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.SetUtils;
import java.util.List;
import java.util.Set;

/**
 * Analysis that can be used to determine if the behavior of one basic block is subsumed by the
 * behavior of another basic block.
 *
 * <p>Example: If the behavior of a switch case is subsumed by the behavior of the switch's default
 * target, then the switch case can simply be removed from the switch instruction.
 */
public class BasicBlockBehavioralSubsumption {

  private final AppView<?> appView;
  private final DexType context;

  public BasicBlockBehavioralSubsumption(AppView<?> appView, DexType context) {
    this.appView = appView;
    this.context = context;
  }

  public boolean isSubsumedBy(BasicBlock block, BasicBlock other) {
    return isSubsumedBy(block.iterator(), other.iterator(), null);
  }

  private boolean isSubsumedBy(
      InstructionIterator iterator, InstructionIterator otherIterator, Set<BasicBlock> visited) {
    // Skip over block-local instructions (i.e., instructions that define values that are not used
    // outside the block itself) that do not have side effects.
    Instruction instruction =
        iterator.nextUntil(
            or(
                not(this::isBlockLocalInstructionWithoutSideEffects),
                Instruction::isJumpInstruction));

    // If the instruction defines a value with non-local usages, then we would need a dominator
    // analysis to verify that all these non-local usages can in fact be replaced by a value
    // defined in `other`. We just give up in this case.
    if (definesValueWithNonLocalUsages(instruction)) {
      return false;
    }

    // Skip over non-throwing instructions in the other block.
    Instruction otherInstruction =
        otherIterator.nextUntil(
            or(this::instructionMayHaveSideEffects, Instruction::isJumpInstruction));
    assert otherInstruction != null;

    if (instruction.isGoto()) {
      BasicBlock targetBlock = instruction.asGoto().getTarget();
      if (otherInstruction.isGoto()) {
        BasicBlock otherTargetBlock = otherInstruction.asGoto().getTarget();
        // If the other instruction is also a goto instruction, which targets the same block, then
        // we are done.
        if (targetBlock == otherTargetBlock) {
          return passesIdenticalValuesForPhis(
              instruction.getBlock(), otherInstruction.getBlock(), targetBlock);
        }
        if (otherTargetBlock.hasPhis()) {
          // TODO(b/136162993): handle this case.
          return false;
        }
        // Otherwise we continue the search from the two successor blocks.
        otherIterator = otherTargetBlock.iterator();
      } else {
        if (targetBlock.hasPhis()) {
          // TODO(b/136162993): handle this case.
          return false;
        }
        // Move the cursor backwards to ensure that the recursive call will revisit the current
        // instruction.
        otherIterator.previous();
      }
      if (visited == null) {
        visited = SetUtils.newIdentityHashSet(instruction.getBlock());
      }
      if (visited.add(targetBlock)) {
        return isSubsumedBy(targetBlock.iterator(), otherIterator, visited);
      }
      // Guard against cycles in the control flow graph.
      return false;
    }

    // If the current instruction is not a goto instruction, but the other instruction is, then
    // we continue the search from the target of the other goto instruction.
    Set<BasicBlock> otherVisited = null;
    while (otherInstruction.isGoto()) {
      BasicBlock block = otherInstruction.getBlock();
      if (otherVisited != null && !otherVisited.add(block)) {
        // Guard against cycles in the control flow graph.
        return false;
      }

      BasicBlock targetBlock = otherInstruction.asGoto().getTarget();
      if (targetBlock.hasPhis()) {
        // TODO(b/136162993): handle this case.
        return false;
      }

      otherIterator = targetBlock.iterator();
      otherInstruction =
          otherIterator.nextUntil(
              or(this::instructionMayHaveSideEffects, Instruction::isJumpInstruction));

      // If following the first goto instruction leads to another goto instruction, then we need to
      // keep track of the set of visited blocks to guard against cycles in the control flow graph.
      if (otherInstruction.isGoto() && otherVisited == null) {
        otherVisited = SetUtils.newIdentityHashSet(block);
      }
    }

    if (instruction.isReturn()) {
      Return returnInstruction = instruction.asReturn();
      if (otherInstruction.isReturn()) {
        Return otherReturnInstruction = otherInstruction.asReturn();
        if (returnInstruction.isReturnVoid()) {
          assert otherReturnInstruction.isReturnVoid();
          return true;
        }
        return valuesAreIdentical(
            otherReturnInstruction.returnValue(), returnInstruction.returnValue());
      }
      return false;
    }

    return false;
  }

  private boolean isBlockLocalInstructionWithoutSideEffects(Instruction instruction) {
    return definesBlockLocalValue(instruction) && !instructionMayHaveSideEffects(instruction);
  }

  private boolean definesBlockLocalValue(Instruction instruction) {
    return !definesValueWithNonLocalUsages(instruction);
  }

  private boolean definesValueWithNonLocalUsages(Instruction instruction) {
    if (instruction.hasOutValue()) {
      Value outValue = instruction.outValue();
      if (outValue.numberOfPhiUsers() > 0) {
        return true;
      }
      for (Instruction user : outValue.uniqueUsers()) {
        if (user.getBlock() != instruction.getBlock()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean instructionMayHaveSideEffects(Instruction instruction) {
    return instruction.instructionMayHaveSideEffects(appView, context);
  }

  private boolean valuesAreIdentical(Value value, Value other) {
    value = value.getAliasedValue();
    other = other.getAliasedValue();
    if (value == other) {
      return true;
    }
    if (value.isPhi() || other.isPhi()) {
      return false;
    }
    return instructionsDefineIdenticalValues(value.definition, other.definition);
  }

  private boolean instructionsDefineIdenticalValues(Instruction instruction, Instruction other) {
    assert instruction.hasOutValue();
    assert other.hasOutValue();

    Value outValue = instruction.outValue();
    Value otherOutValue = other.outValue();
    if (!outValue.getTypeLattice().equals(otherOutValue.getTypeLattice())) {
      return false;
    }

    if (instruction.isConstClass()) {
      if (!other.isConstClass()) {
        return false;
      }
      ConstClass constClassInstruction = instruction.asConstClass();
      ConstClass otherConstClassInstruction = other.asConstClass();
      return constClassInstruction.getValue() == otherConstClassInstruction.getValue();
    }

    if (instruction.isConstNumber()) {
      if (!other.isConstNumber()) {
        return false;
      }
      ConstNumber constNumberInstruction = instruction.asConstNumber();
      ConstNumber otherConstNumberInstruction = other.asConstNumber();
      return constNumberInstruction.getRawValue() == otherConstNumberInstruction.getRawValue();
    }

    if (instruction.isConstString()) {
      if (!other.isConstString()) {
        return false;
      }
      ConstString constStringInstruction = instruction.asConstString();
      ConstString otherConstStringInstruction = other.asConstString();
      return constStringInstruction.getValue() == otherConstStringInstruction.getValue();
    }

    if (instruction.isDexItemBasedConstString()) {
      if (!other.isDexItemBasedConstString()) {
        return false;
      }
      DexItemBasedConstString constStringInstruction = instruction.asDexItemBasedConstString();
      DexItemBasedConstString otherConstStringInstruction = other.asDexItemBasedConstString();
      return constStringInstruction.getItem() == otherConstStringInstruction.getItem();
    }

    return false;
  }

  private boolean passesIdenticalValuesForPhis(
      BasicBlock block, BasicBlock other, BasicBlock blockWithPhis) {
    if (block == other) {
      return true;
    }

    int predecessorIndex = -1, otherPredecessorIndex = -1;
    List<BasicBlock> predecessors = blockWithPhis.getPredecessors();
    for (int i = 0; i < predecessors.size(); i++) {
      BasicBlock predecessor = predecessors.get(i);
      if (predecessor == block) {
        predecessorIndex = i;
        if (otherPredecessorIndex >= 0) {
          break;
        }
      } else if (predecessor == other) {
        otherPredecessorIndex = i;
        if (predecessorIndex >= 0) {
          break;
        }
      }
    }

    assert predecessorIndex >= 0;
    assert otherPredecessorIndex >= 0;

    for (Phi phi : blockWithPhis.getPhis()) {
      if (!valuesAreIdentical(
          phi.getOperand(predecessorIndex), phi.getOperand(otherPredecessorIndex))) {
        return false;
      }
    }
    return true;
  }
}
