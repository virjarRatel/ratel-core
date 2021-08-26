// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Dup;
import com.android.tools.r8.ir.code.Dup2;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.StackValue;

/**
 * Peephole that looks for the following pattern:
 *
 * <pre>
 * Dup
 * Dup
 * Dup
 * </pre>
 *
 * and replaces with
 *
 * <pre>
 * Dup
 * Dup2
 * </pre>
 */
public class DupDupDupPeephole implements BasicBlockPeephole {

  private final Point dup1Exp =
      new Point((i) -> i.isDup() && !i.inValues().get(0).getTypeLattice().isWidePrimitive());
  private final Point dup2Exp =
      new Point((i) -> i.isDup() && !i.inValues().get(0).getTypeLattice().isWidePrimitive());
  private final Point dup3Exp =
      new Point((i) -> i.isDup() && !i.inValues().get(0).getTypeLattice().isWidePrimitive());

  private final PeepholeLayout layout = PeepholeLayout.lookBackward(dup1Exp, dup2Exp, dup3Exp);

  @Override
  public boolean match(InstructionListIterator it) {
    Match match = layout.test(it);
    if (match == null) {
      return false;
    }

    Dup dupTop = dup3Exp.get(match).asDup();
    Dup dupMiddle = dup2Exp.get(match).asDup();
    Dup dupBottom = dup1Exp.get(match).asDup();

    // The stack looks like:
    // ..., dupTop0, dupMiddle0, dupBottom0, dupBottom1,.. -->
    // because dupTop1 was used by dupMiddle and dupMiddle1 was used by dupBottom.

    int height = dupTop.src().getHeight();

    StackValue tv0Dup2 = dupTop.outBottom().duplicate(height);
    StackValue mv0Dup2 = dupMiddle.outBottom().duplicate(height + 1);
    StackValue bv0Dup2 = dupBottom.outBottom().duplicate(height + 2);
    StackValue bv1Dup2 = dupBottom.outTop().duplicate(height + 3);

    // Remove tv1 use.
    dupMiddle.src().removeUser(dupMiddle);
    // Remove mv1 use.
    dupBottom.src().removeUser(dupBottom);
    // Replace other uses.
    dupTop.outBottom().replaceUsers(tv0Dup2);
    dupMiddle.outBottom().replaceUsers(mv0Dup2);
    dupBottom.outBottom().replaceUsers(bv0Dup2);
    dupBottom.outTop().replaceUsers(bv1Dup2);

    Dup2 dup2 = new Dup2(tv0Dup2, mv0Dup2, bv0Dup2, bv1Dup2, dupTop.outBottom(), dupTop.outTop());

    it.removeOrReplaceByDebugLocalRead();
    it.previous();
    it.replaceCurrentInstruction(dup2);

    // Reset the pointer
    PeepholeHelper.resetPrevious(it, 1);
    return true;
  }

  @Override
  public boolean resetAfterMatch() {
    return false;
  }
}
