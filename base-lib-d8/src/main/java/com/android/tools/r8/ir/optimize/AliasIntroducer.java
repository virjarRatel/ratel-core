// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.Assume.NoAssumption;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.Sets;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

public class AliasIntroducer implements Assumer {
  private final AppView<?> appView;

  public AliasIntroducer(AppView<?> appView) {
    this.appView = appView;
  }

  @Override
  public void insertAssumeInstructionsInBlocks(
      IRCode code, ListIterator<BasicBlock> blockIterator, Predicate<BasicBlock> blockTester) {
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (blockTester.test(block)) {
        insertAssumeNoneInstructionsInBlock(code, blockIterator, block);
      }
    }
  }

  private void insertAssumeNoneInstructionsInBlock(
      IRCode code, ListIterator<BasicBlock> blockIterator, BasicBlock block) {
    Set<Assume<NoAssumption>> deferredInstructions = Sets.newIdentityHashSet();
    InstructionListIterator instructionIterator = block.listIterator(code);
    while (instructionIterator.hasNext()) {
      Instruction current = instructionIterator.next();
      if (!current.hasOutValue() || !current.outValue().isUsed()) {
        continue;
      }
      Value outValue = current.outValue();
      // TODO(b/129859039): We may need similar concept when adding/testing assume-range
      if (outValue.getTypeLattice().isPrimitive()
          || outValue.getTypeLattice().isNullType()) {
        continue;
      }
      // Split block if needed
      BasicBlock insertionBlock =
          block.hasCatchHandlers() ? instructionIterator.split(code, blockIterator) : block;
      // Replace usages of out-value by the out-value of the AssumeNone instruction.
      Value aliasedValue = code.createValue(outValue.getTypeLattice(), outValue.getLocalInfo());
      outValue.replaceUsers(aliasedValue);
      // Insert AssumeNone instruction.
      Assume<NoAssumption> assumeNone =
          Assume.createAssumeNoneInstruction(aliasedValue, outValue, current, appView);
      // {@link BasicBlock} needs Argument instructions to be packed.
      if (current.isArgument()) {
        deferredInstructions.add(assumeNone);
        continue;
      }
      assumeNone.setPosition(
          appView.options().debug ? current.getPosition() : Position.none());
      if (insertionBlock == block) {
        instructionIterator.add(assumeNone);
      } else {
        insertionBlock.listIterator(code).add(assumeNone);
      }
    }
    // For deferred instructions due to packed arguments,
    //   restart the iterator; move up to the last Argument instruction; and then add aliases.
    if (!deferredInstructions.isEmpty()) {
      final Position firstNonNonePosition = code.findFirstNonNonePosition();
      final InstructionListIterator it = block.listIterator(code);
      it.nextUntil(i -> !i.isArgument());
      it.previous();
      deferredInstructions.forEach(i -> {
        i.setPosition(firstNonNonePosition);
        it.add(i);
      });
    }
  }
}
