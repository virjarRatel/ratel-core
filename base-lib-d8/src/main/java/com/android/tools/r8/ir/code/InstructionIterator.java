// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import java.util.Iterator;

public interface InstructionIterator extends Iterator<Instruction>, NextUntilIterator<Instruction> {

  /** @deprecated Use {@link InstructionListIterator#remove()} instead. */
  @Deprecated
  @Override
  default void remove() {
    throw new UnsupportedOperationException("remove");
  }

  boolean hasPrevious();

  Instruction previous();

  /**
   * Peek the next instruction.
   *
   * @return what will be returned by calling {@link #next}. If there is no next instruction <code>
   *     null</code> is returned.
   */
  default Instruction peekNext() {
    Instruction next = null;
    if (hasNext()) {
      next = next();
      previous();
    }
    return next;
  }

  /**
   * Peek the previous instruction.
   *
   * @return what will be returned by calling {@link #previous}. If there is no previous instruction
   *     <code>null</code> is returned.
   */
  default Instruction peekPrevious() {
    Instruction previous = null;
    if (hasPrevious()) {
      previous = previous();
      next();
    }
    return previous;
  }
}
