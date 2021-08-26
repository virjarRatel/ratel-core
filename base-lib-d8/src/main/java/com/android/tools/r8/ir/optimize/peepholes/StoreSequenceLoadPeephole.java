// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.StackValue;
import com.android.tools.r8.ir.code.StackValues;
import com.android.tools.r8.ir.code.Store;
import com.android.tools.r8.ir.code.Swap;
import java.util.List;

/**
 * {@link StoreSequenceLoadPeephole} looks for the following pattern:
 *
 * <pre>
 * Store                v5 <- s0
 * ... (stackheight 1)  s0 <- v4
 * Load                 s1 <- v5
 * </pre>
 *
 * and replaces with
 *
 * <pre>
 * ...                  s1 <- v4
 * Swap                 [s0, s1] <- s0, s1
 * </pre>
 *
 * This saves a store and load and removes a use of a local.
 */
public class StoreSequenceLoadPeephole implements BasicBlockPeephole {

  private Store store;
  private int stackHeight = 0;

  private final Point storeExp =
      new Point(
          (i) -> {
            if (!i.isStore()
                || i.asStore().src().getTypeLattice().isWidePrimitive()
                || i.outValue().hasLocalInfo()
                || i.asStore().outValue().numberOfAllUsers() != 1) {
              return false;
            }
            store = i.asStore();
            stackHeight = 0;
            return true;
          });

  private final Wildcard seqExp =
      new Wildcard(
          (i) -> {
            if (stackHeight == 1 && i.isLoad() && i.asLoad().src() == store.outValue()) {
              // Allow loadExp to pick up the load.
              return false;
            }
            stackHeight -= PeepholeHelper.numberOfValuesConsumedFromStack(i);
            if (stackHeight < 0) {
              store = null;
              return false;
            }
            stackHeight += PeepholeHelper.numberOfValuesPutOnStack(i);
            return true;
          });

  private final Point loadExp =
      new Point(
          (i) -> {
            if (store == null
                || !i.isLoad()
                || i.asLoad().src().hasLocalInfo()
                || i.asLoad().src() != store.outValue()) {
              return false;
            }
            return true;
          });

  private final PeepholeLayout layout = PeepholeLayout.lookForward(storeExp, seqExp, loadExp);

  @Override
  public boolean match(InstructionListIterator it) {
    Match match = layout.test(it);
    if (match == null || store == null) {
      return false;
    }

    Store store = storeExp.get(match).asStore();
    List<Instruction> seq = seqExp.get(match);
    Load load = loadExp.get(match).asLoad();

    // Make sure last instruction is a stack value that is not wide.
    Instruction last = seq.get(seq.size() - 1);
    StackValue lastOut = null;

    if (last.outValue() instanceof StackValue) {
      lastOut = (StackValue) last.outValue();
    } else if (last.outValue() instanceof StackValues) {
      StackValue[] stackValues = ((StackValues) last.outValue()).getStackValues();
      lastOut = stackValues[stackValues.length - 1];
    }

    if (lastOut == null || lastOut.getTypeLattice().isWidePrimitive()) {
      return false;
    }

    // Create the swap instruction sources.
    StackValue source = (StackValue) store.src();

    store.outValue().removeUser(load);
    load.outValue().replaceUsers(source);

    // Remove the first store.
    it.removeOrReplaceByDebugLocalRead();

    it.nextUntil(i -> i == load);

    // Insert a swap instruction
    StackValue swapLastOut = lastOut.duplicate(source.getHeight());
    StackValue swapSource = source.duplicate(swapLastOut.getHeight() + 1);
    source.replaceUsers(swapSource);
    lastOut.replaceUsers(swapLastOut);
    it.replaceCurrentInstruction(new Swap(swapLastOut, swapSource, source, lastOut));
    PeepholeHelper.resetNext(it, 2);
    return true;
  }

  @Override
  public boolean resetAfterMatch() {
    return false;
  }
}
