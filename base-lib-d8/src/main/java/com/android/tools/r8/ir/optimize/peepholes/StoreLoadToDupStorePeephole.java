// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Dup;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.StackValue;
import com.android.tools.r8.ir.code.StackValues;
import com.android.tools.r8.ir.code.Store;
import java.util.List;

/**
 * Peephole that looks for the following pattern:
 *
 * <pre>
 * Store                v0 <- sa
 * Load                 sb <- v0   # where v0 has multiple users
 * zero or more Dup/Dup2
 * </pre>
 *
 * and replace it with
 *
 * <pre>
 * zero or more Dup/Dup2
 * Dup            [sb, sc] <- sa
 * Store                v0 <- sc
 * </pre>
 */
public class StoreLoadToDupStorePeephole implements BasicBlockPeephole {

  private final Point storeExp = new Point(PeepholeHelper.withoutLocalInfo(Instruction::isStore));
  private final Point loadExp = new Point(Instruction::isLoad);
  private final Wildcard dupsExp = new Wildcard(i -> i.isDup() || i.isDup2());

  private final PeepholeLayout layout = PeepholeLayout.lookForward(storeExp, loadExp, dupsExp);

  @Override
  public boolean match(InstructionListIterator it) {
    Match match = layout.test(it);
    if (match == null) {
      return false;
    }
    Store store = storeExp.get(match).asStore();
    Load load = loadExp.get(match).asLoad();
    if (load.src() != store.outValue() || store.outValue().numberOfAllUsers() <= 1) {
      return false;
    }
    List<Instruction> dups = dupsExp.get(match);
    StackValue oldStoreSrc = (StackValue) store.src();
    StackValue dupIn;
    StackValue lastOut;
    // Store and load may be in two different basic blocks (see b/122445224) so move all
    // instructions to the block with the store.
    if (dups.isEmpty()) {
      lastOut = (StackValue) load.outValue();
      dupIn = oldStoreSrc;
    } else {
      assert dups.get(0).isDup() && dups.get(0).inValues().get(0) == load.outValue();
      dups.get(0).replaceValue(0, oldStoreSrc);
      PeepholeHelper.moveInstructionsUpToCurrentPosition(it, dups);
      StackValue[] lastDupOutValues =
          ((StackValues) dups.get(dups.size() - 1).outValue()).getStackValues();
      lastOut = lastDupOutValues[lastDupOutValues.length - 1];
      dupIn = lastOut;
    }
    StackValue newLastOut = lastOut.duplicate(lastOut.getHeight());
    StackValue newStoreSrc = lastOut.duplicate(lastOut.getHeight() + 1);
    lastOut.replaceUsers(newLastOut);
    assert !load.outValue().isUsed();
    Dup dup = new Dup(newLastOut, newStoreSrc, dupIn);
    dup.setPosition(store.getPosition());
    it.add(dup);
    store.replaceValue(0, newStoreSrc);
    it.nextUntil(i -> i == load);
    it.removeOrReplaceByDebugLocalRead();
    PeepholeHelper.resetNext(it, dups.size() + 1);
    return true;
  }

  @Override
  public boolean resetAfterMatch() {
    return false;
  }
}
