// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Store;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.peepholes.PeepholeHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhiOptimizations {

  public boolean optimize(IRCode code) {
    return tryMovePhisToStack(code);
  }

  private static boolean predecessorsHaveNormalFlow(BasicBlock block) {
    for (BasicBlock predecessor : block.getPredecessors()) {
      if (!predecessor.exit().isGoto() || predecessor.exit().asGoto().getTarget() != block) {
        return false;
      }
    }
    return true;
  }

  private static boolean singleUseOfPhiAndOperands(Phi phi) {
    if (phi.numberOfAllUsers() != 1 || phi.numberOfUsers() != 1) {
      return false;
    }
    for (Value operand : phi.getOperands()) {
      if (operand.numberOfAllUsers() != 1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Searches through blocks with linear flow for the instruction and report the stack-height.
   *
   * @param block start of search having relative stack height 0
   * @param instruction instruction to search for
   * @return the stack height if the instruction was found, otherwise Integer.MIN_VALUE
   */
  private static int getRelativeStackHeightForInstruction(
      BasicBlock block, Instruction instruction) {
    int stackHeight = 0;
    Set<BasicBlock> seenBlocks = new HashSet<>();
    while (block != null) {
      seenBlocks.add(block);
      for (Instruction current : block.getInstructions()) {
        if (instruction == current) {
          return stackHeight;
        }
        stackHeight -= PeepholeHelper.numberOfValuesConsumedFromStack(instruction);
        if (stackHeight < 0) {
          return Integer.MIN_VALUE;
        }
        stackHeight += PeepholeHelper.numberOfValuesPutOnStack(instruction);
      }
      if (block.exit().isGoto() && !seenBlocks.contains(block.exit().asGoto().getTarget())) {
        block = block.exit().asGoto().getTarget();
      } else {
        block = null;
      }
    }
    return Integer.MIN_VALUE;
  }

  /**
   * Searches for an instruction by navigating backwards in the block of the instruction.
   *
   * @param instruction instruction to search for
   * @return the stack height if the instruction was found, otherwise Integer.MIN_VALUE
   */
  private static int getStackHeightAtInstructionBackwards(Instruction instruction) {
    int stackHeight = 0;
    BasicBlock block = instruction.getBlock();
    InstructionIterator it = block.iterator(block.getInstructions().size() - 1);
    while (it.hasPrevious()) {
      Instruction current = it.previous();
      if (current == instruction) {
        break;
      }
      stackHeight -= PeepholeHelper.numberOfValuesPutOnStack(instruction);
      if (stackHeight < 0) {
        return Integer.MIN_VALUE;
      }
      stackHeight += PeepholeHelper.numberOfValuesConsumedFromStack(instruction);
    }
    return stackHeight;
  }

  /**
   * Try to move all phis for all blocks to the stack.
   *
   * @param code
   * @return true if it could move one or more phi's to the stack.
   */
  private static boolean tryMovePhisToStack(IRCode code) {
    boolean hadChange = false;
    for (BasicBlock block : code.blocks) {
      boolean changed = true;
      Set<BasicBlock> predecessors = new HashSet<>(block.getPredecessors());
      while (changed) {
        changed = false;
        for (Phi phi : block.getPhis()) {
          changed |= tryMovePhiToStack(block, phi, predecessors);
        }
        if (changed) {
          hadChange = true;
        }
      }
    }
    return hadChange;
  }

  /**
   * Try to move a single phi to the stack.
   *
   * @param block block of the phi
   * @param phi the phi to try and move to the stack
   * @param predecessors set containing all predecessors of the block
   * @return true if it could move phi to the stack
   */
  private static boolean tryMovePhiToStack(
      BasicBlock block, Phi phi, Set<BasicBlock> predecessors) {
    if (!predecessorsHaveNormalFlow(block) || !singleUseOfPhiAndOperands(phi)) {
      return false;
    }
    Load phiLoad = phi.singleUniqueUser().asLoad();
    if (phiLoad == null || phiLoad.src().hasLocalInfo()) {
      return false;
    }
    if (getRelativeStackHeightForInstruction(block, phiLoad) != 0) {
      return false;
    }
    for (Value operand : phi.getOperands()) {
      if (operand.definition == null || !operand.definition.isStore()) {
        return false;
      }
      if (!predecessors.contains(operand.definition.getBlock())) {
        return false;
      }
      if (getStackHeightAtInstructionBackwards(operand.definition) != 0) {
        return false;
      }
    }
    // The phi can be passed through the stack.
    List<Store> stores = new ArrayList<>();
    for (Value operand : phi.getOperands()) {
      stores.add(operand.definition.asStore());
    }
    for (int i = 0; i < stores.size(); i++) {
      Store store = stores.get(i);
      phi.replaceOperandAt(i, store.src());
      store.src().removeUser(store);
      store.getBlock().removeInstruction(store);
    }
    phiLoad.outValue().replaceUsers(phi);
    phiLoad.src().removeUser(phiLoad);
    phiLoad.getBlock().removeInstruction(phiLoad);
    phi.setIsStackPhi(true);
    return true;
  }
}
