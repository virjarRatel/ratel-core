// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IRCodeInstructionIterator implements InstructionIterator {

  private final ListIterator<BasicBlock> blockIterator;
  private InstructionListIterator instructionIterator;

  private final IRCode code;

  public IRCodeInstructionIterator(IRCode code) {
    this.blockIterator = code.listIterator();
    this.code = code;
    this.instructionIterator = blockIterator.next().listIterator(code);
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
}
