// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import static com.android.tools.r8.utils.InternalOptions.TestingOptions.NO_LIMIT;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.LinearFlowInstructionListIterator;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.ListIterator;

public class BasicBlockMuncher {

  private static List<BasicBlockPeephole> nonDestructivePeepholes() {
    return ImmutableList.of(new MoveLoadUpPeephole(), new StoreLoadPeephole());
  }

  // The StoreLoadPeephole and StoreSequenceLoadPeephole are non-destructive but we would like it
  // to run in a fix-point with the other peepholes to allow for more matches.
  private static List<BasicBlockPeephole> destructivePeepholes() {
    return ImmutableList.of(
        new StoreSequenceLoadPeephole(),
        new StoreLoadPeephole(),
        new LoadLoadDupPeephole(),
        new DupDupDupPeephole(),
        new StoreLoadToDupStorePeephole());
  }

  public static void optimize(IRCode code, InternalOptions options) {
    runPeepholes(code, nonDestructivePeepholes(), options);
    runPeepholes(code, destructivePeepholes(), options);
  }

  private static void runPeepholes(
      IRCode code, List<BasicBlockPeephole> peepholes, InternalOptions options) {
    ListIterator<BasicBlock> blocksIterator = code.listIterator(code.blocks.size());
    int iterations = 0;
    while (blocksIterator.hasPrevious()) {
      BasicBlock currentBlock = blocksIterator.previous();
      InstructionListIterator it =
          new LinearFlowInstructionListIterator(
              code, currentBlock, currentBlock.getInstructions().size());
      boolean matched = false;
      while (matched || it.hasPrevious()) {
        if (!it.hasPrevious()) {
          matched = false;
          it =
              new LinearFlowInstructionListIterator(
                  code, currentBlock, currentBlock.getInstructions().size());
        }
        for (BasicBlockPeephole peepHole : peepholes) {
          boolean localMatch = peepHole.match(it);
          if (localMatch && peepHole.resetAfterMatch()) {
            it =
                new LinearFlowInstructionListIterator(
                    code, currentBlock, currentBlock.getInstructions().size());
          } else {
            matched |= localMatch;
          }
        }
        if (it.hasPrevious()) {
          if (options.testing.basicBlockMuncherIterationLimit != NO_LIMIT) {
            if (iterations > options.testing.basicBlockMuncherIterationLimit) {
              throw new CompilationError("Too many iterations in BasicBlockMuncher");
            }
            iterations++;
          }
          it.previous();
        }
      }
    }
  }
}
