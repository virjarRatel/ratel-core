// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.InternalOptions;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class IRCodeInstructionListIterator implements InstructionListIterator {

  private final ListIterator<BasicBlock> blockIterator;
  private InstructionListIterator instructionIterator;

  private final IRCode code;

  public IRCodeInstructionListIterator(IRCode code) {
    this.blockIterator = code.listIterator();
    this.code = code;
    this.instructionIterator = blockIterator.next().listIterator(code);
  }

  @Override
  public Value insertConstNullInstruction(IRCode code, InternalOptions options) {
    return instructionIterator.insertConstNullInstruction(code, options);
  }

  @Override
  public Value insertConstIntInstruction(IRCode code, InternalOptions options, int value) {
    return instructionIterator.insertConstIntInstruction(code, options, value);
  }

  @Override
  public void replaceCurrentInstructionWithConstInt(
      AppView<? extends AppInfoWithSubtyping> appView, IRCode code, int value) {
    instructionIterator.replaceCurrentInstructionWithConstInt(appView, code, value);
  }

  @Override
  public void replaceCurrentInstructionWithStaticGet(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      DexField field,
      Set<Value> affectedValues) {
    instructionIterator.replaceCurrentInstructionWithStaticGet(
        appView, code, field, affectedValues);
  }

  @Override
  public void replaceCurrentInstructionWithThrowNull(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      Set<Value> affectedValues) {
    throw new Unimplemented();
  }

  @Override
  public BasicBlock split(IRCode code, ListIterator<BasicBlock> blockIterator) {
    throw new Unimplemented();
  }

  @Override
  public BasicBlock split(IRCode code, int instructions, ListIterator<BasicBlock> blockIterator) {
    throw new Unimplemented();
  }

  @Override
  public BasicBlock inlineInvoke(
      AppView<?> appView,
      IRCode code,
      IRCode inlinee,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      DexType downcast) {
    throw new Unimplemented();
  }

  @Override
  public boolean hasNext() {
    return instructionIterator.hasNext() || blockIterator.hasNext();
  }

  @Override
  public Instruction next() {
    if (instructionIterator.hasNext()) {
      return instructionIterator.next();
    }
    if (!blockIterator.hasNext()) {
      throw new NoSuchElementException();
    }
    instructionIterator = blockIterator.next().listIterator(code);
    assert instructionIterator.hasNext();
    return instructionIterator.next();
  }

  @Override
  public boolean hasPrevious() {
    return instructionIterator.hasPrevious() || blockIterator.hasPrevious();
  }

  @Override
  public Instruction previous() {
    if (instructionIterator.hasPrevious()) {
      return instructionIterator.previous();
    }
    if (!blockIterator.hasPrevious()) {
      throw new NoSuchElementException();
    }
    BasicBlock block = blockIterator.previous();
    instructionIterator = block.listIterator(code, block.getInstructions().size());
    assert instructionIterator.hasPrevious();
    return instructionIterator.previous();
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
  public void add(Instruction instruction) {
    instructionIterator.add(instruction);
  }

  @Override
  public void remove() {
    instructionIterator.remove();
  }

  @Override
  public void set(Instruction instruction) {
    instructionIterator.set(instruction);
  }

  @Override
  public void replaceCurrentInstruction(Instruction newInstruction) {
    instructionIterator.replaceCurrentInstruction(newInstruction);
  }

  @Override
  public void removeOrReplaceByDebugLocalRead() {
    instructionIterator.removeOrReplaceByDebugLocalRead();
  }
}
