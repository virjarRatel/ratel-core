// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Dup;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.StackValue;

/**
 * {@link LoadLoadDupPeephole} looks for the following pattern:
 *
 * <pre>
 * Load                 s0 <- v0
 * Load                 s0 <- v0
 * </pre>
 *
 * and replaces with:
 *
 * <pre>
 * Load                 s0 <- v0
 * Dup
 * </pre>
 *
 * This saves a load and also removes a use of v0.
 */
public class LoadLoadDupPeephole implements BasicBlockPeephole {

  private final Point lastLoadExp =
      new Point(PeepholeHelper.withoutLocalInfo(Instruction::isLoad));
  private final Point firstLoadExp = new Point(PeepholeHelper.withoutLocalInfo(Instruction::isLoad));

  // This searches backwards thus the pattern is built from the bottom.
  private final PeepholeLayout layout = PeepholeLayout.lookBackward(lastLoadExp, firstLoadExp);

  @Override
  public boolean match(InstructionListIterator it) {
    Match match = layout.test(it);
    if (match == null) {
      return false;
    }
    Load lastLoad = lastLoadExp.get(match).asLoad();
    Load firstLoad = firstLoadExp.get(match).asLoad();
    if (firstLoad.src() != lastLoad.src()) {
      return false;
    }

    assert !firstLoad.src().hasLocalInfo();
    assert !lastLoad.src().hasLocalInfo();

    StackValue src = (StackValue) firstLoad.outValue();
    src.removeUser(lastLoad);

    int height = src.getHeight();
    StackValue newFirstLoadOut = src.duplicate(height);
    StackValue newLastLoadOut = src.duplicate(height + 1);

    firstLoad.outValue().replaceUsers(newFirstLoadOut);
    lastLoad.outValue().replaceUsers(newLastLoadOut);

    it.replaceCurrentInstruction(new Dup(newFirstLoadOut, newLastLoadOut, src));
    return true;
  }

  @Override
  public boolean resetAfterMatch() {
    return false;
  }

}
