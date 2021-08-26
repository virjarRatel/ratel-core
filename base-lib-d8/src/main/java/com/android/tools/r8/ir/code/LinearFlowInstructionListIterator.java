// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.InternalOptions;
import java.util.ListIterator;
import java.util.Set;

public class LinearFlowInstructionListIterator implements InstructionListIterator {

  private final IRCode code;

  private BasicBlock currentBlock;
  private InstructionListIterator currentBlockIterator;

  public LinearFlowInstructionListIterator(IRCode code, BasicBlock block) {
    this(code, block, 0);
  }

  public LinearFlowInstructionListIterator(IRCode code, BasicBlock block, int index) {
    this.code = code;
    this.currentBlock = block;
    this.currentBlockIterator = block.listIterator(code, index);
    // If index is pointing after the last instruction, and it is a goto with a linear edge,
    // we have to move the pointer. This is achieved by calling previous and next.
    if (index > 0) {
      this.previous();
      this.next();
    }
  }

  @Override
  public void replaceCurrentInstruction(Instruction newInstruction) {
    currentBlockIterator.replaceCurrentInstruction(newInstruction);
  }

  @Override
  public Value insertConstNullInstruction(IRCode code, InternalOptions options) {
    return currentBlockIterator.insertConstNullInstruction(code, options);
  }

  @Override
  public Value insertConstIntInstruction(IRCode code, InternalOptions options, int value) {
    return currentBlockIterator.insertConstIntInstruction(code, options, value);
  }

  @Override
  public void replaceCurrentInstructionWithConstInt(
      AppView<? extends AppInfoWithSubtyping> appView, IRCode code, int value) {
    currentBlockIterator.replaceCurrentInstructionWithConstInt(appView, code, value);
  }

  @Override
  public void replaceCurrentInstructionWithStaticGet(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      DexField field,
      Set<Value> affectedValues) {
    currentBlockIterator.replaceCurrentInstructionWithStaticGet(
        appView, code, field, affectedValues);
  }

  @Override
  public void replaceCurrentInstructionWithThrowNull(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      Set<Value> affectedValues) {
    currentBlockIterator.replaceCurrentInstructionWithThrowNull(
        appView, code, blockIterator, blocksToRemove, affectedValues);
  }

  @Override
  public BasicBlock split(IRCode code, ListIterator<BasicBlock> blockIterator) {
    return currentBlockIterator.split(code, blockIterator);
  }

  @Override
  public BasicBlock split(IRCode code, int instructions, ListIterator<BasicBlock> blockIterator) {
    return currentBlockIterator.split(code, instructions, blockIterator);
  }

  @Override
  public BasicBlock inlineInvoke(
      AppView<?> appView,
      IRCode code,
      IRCode inlinee,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      DexType downcast) {
    return currentBlockIterator.inlineInvoke(
        appView, code, inlinee, blockIterator, blocksToRemove, downcast);
  }

  @Override
  public void add(Instruction instruction) {
    currentBlockIterator.add(instruction);
  }

  @Override
  public void removeOrReplaceByDebugLocalRead() {
    currentBlockIterator.removeOrReplaceByDebugLocalRead();
  }

  private boolean isLinearEdge(BasicBlock pred, BasicBlock succ) {
    assert pred.getSuccessors().contains(succ);
    assert succ.getPredecessors().contains(pred);
    Goto exit = pred.exit().asGoto();
    return exit != null && exit.getTarget() == succ && succ.getPredecessors().size() == 1;
  }

  @Override
  public boolean hasNext() {
    return currentBlockIterator.hasNext();
  }

  @Override
  public Instruction next() {
    Instruction current = currentBlockIterator.next();
    if (!current.isGoto()) {
      return current;
    }
    BasicBlock target = current.asGoto().getTarget();
    if (!isLinearEdge(currentBlock, target)) {
      return current;
    }
    while (target.isTrivialGoto()) {
      BasicBlock candidate = target.exit().asGoto().getTarget();
      if (!isLinearEdge(target, candidate)) {
        break;
      }
      target = candidate;
    }
    currentBlock = target;
    currentBlockIterator = currentBlock.listIterator(code);
    return currentBlockIterator.next();
  }

  private BasicBlock getBeginningOfTrivialLinearGotoChain(BasicBlock block) {
    if (block.getPredecessors().size() != 1
        || !isLinearEdge(block.getPredecessors().get(0), block)) {
      return null;
    }
    BasicBlock target = block.getPredecessors().get(0);
    while (target.getPredecessors().size() == 1
        && isLinearEdge(target.getPredecessors().get(0), target)
        && target.isTrivialGoto()) {
      target = target.getPredecessors().get(0);
    }
    return target.isTrivialGoto() ? null : target;
  }

  @Override
  public boolean hasPrevious() {
    if (currentBlockIterator.hasPrevious()) {
      return true;
    }
    return getBeginningOfTrivialLinearGotoChain(currentBlock) != null;
  }

  @Override
  public Instruction previous() {
    if (currentBlockIterator.hasPrevious()) {
      return currentBlockIterator.previous();
    }
    BasicBlock target = getBeginningOfTrivialLinearGotoChain(currentBlock);
    if (target == null) {
      // No existing linear predecessor. Force an error by calling previous.
      return currentBlockIterator.previous();
    }
    currentBlock = target;
    currentBlockIterator = currentBlock.listIterator(code, currentBlock.getInstructions().size());
    // Iterate over the jump.
    currentBlockIterator.previous();
    return currentBlockIterator.previous();
  }

  @Override
  public int nextIndex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int previousIndex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove() {
    currentBlockIterator.remove();
  }

  @Override
  public void set(Instruction instruction) {
    currentBlockIterator.set(instruction);
  }
}
