// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.Store;

/**
 * Peephole that looks for the following pattern:
 *
 * <pre>
 * Store                v0 <- s0
 * Load                 s0 <- v0
 * </pre>
 *
 * and removes both instructions.
 */
public class StoreLoadPeephole implements BasicBlockPeephole {

  private final Point storeExp = new Point(PeepholeHelper.withoutLocalInfo(Instruction::isStore));
  private final Point loadExp = new Point(Instruction::isLoad);

  private final PeepholeLayout layout = PeepholeLayout.lookForward(storeExp, loadExp);

  @Override
  public boolean match(InstructionListIterator it) {
    Match match = layout.test(it);
    if (match == null) {
      return false;
    }
    Store store = storeExp.get(match).asStore();
    Load load = loadExp.get(match).asLoad();
    if (load.src() != store.outValue() || store.outValue().numberOfAllUsers() != 1) {
      return false;
    }
    // Set the use of loads out to the source of the store.
    load.outValue().replaceUsers(store.src());
    // Remove all uses and remove the instructions.
    store.src().removeUser(store);
    load.src().removeUser(load);
    it.removeOrReplaceByDebugLocalRead();
    it.next();
    it.removeOrReplaceByDebugLocalRead();
    PeepholeHelper.resetNext(it, 1);
    return true;
  }

  @Override
  public boolean resetAfterMatch() {
    return false;
  }

}
