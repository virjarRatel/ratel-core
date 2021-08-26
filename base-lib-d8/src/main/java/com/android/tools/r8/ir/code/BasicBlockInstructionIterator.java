// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import java.util.ListIterator;

public class BasicBlockInstructionIterator implements InstructionIterator {

  private final ListIterator<Instruction> instructionIterator;

  BasicBlockInstructionIterator(BasicBlock block) {
    this.instructionIterator = block.getInstructions().listIterator();
  }

  BasicBlockInstructionIterator(BasicBlock block, int index) {
    this.instructionIterator = block.getInstructions().listIterator(index);
  }

  BasicBlockInstructionIterator(BasicBlock block, Instruction instruction) {
    this(block);
    nextUntil(x -> x == instruction);
  }

  @Override
  public boolean hasPrevious() {
    return instructionIterator.hasPrevious();
  }

  @Override
  public Instruction previous() {
    return instructionIterator.previous();
  }

  @Override
  public boolean hasNext() {
    return instructionIterator.hasNext();
  }

  @Override
  public Instruction next() {
    return instructionIterator.next();
  }
}
