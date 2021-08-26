// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.PrimitiveTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.Goto;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.JumpInstruction;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.StringSwitch;
import com.android.tools.r8.naming.IdentifierNameStringMarker;
import com.android.tools.r8.utils.SetUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.IdentityHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StringSwitchRemover {

  private final AppView<?> appView;
  private final IdentifierNameStringMarker identifierNameStringMarker;
  private final ClassTypeLatticeElement stringType;
  private final ThrowingInfo throwingInfo;

  StringSwitchRemover(AppView<?> appView, IdentifierNameStringMarker identifierNameStringMarker) {
    this.appView = appView;
    this.identifierNameStringMarker = identifierNameStringMarker;
    this.stringType = TypeLatticeElement.stringClassType(appView, Nullability.definitelyNotNull());
    this.throwingInfo = ThrowingInfo.defaultForConstString(appView.options());
  }

  void run(DexEncodedMethod method, IRCode code) {
    if (!code.metadata().mayHaveStringSwitch()) {
      assert Streams.stream(code.instructions()).noneMatch(Instruction::isStringSwitch);
      return;
    }

    Set<BasicBlock> newBlocks = Sets.newIdentityHashSet();

    ListIterator<BasicBlock> blockIterator = code.listIterator();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      JumpInstruction exit = block.exit();
      if (exit.isStringSwitch()) {
        removeStringSwitch(code, blockIterator, block, exit.asStringSwitch(), newBlocks);
      }
    }

    if (identifierNameStringMarker != null) {
      identifierNameStringMarker.decoupleIdentifierNameStringsInBlocks(method, code, newBlocks);
    }

    assert code.isConsistentSSA();
  }

  private void removeStringSwitch(
      IRCode code,
      ListIterator<BasicBlock> blockIterator,
      BasicBlock block,
      StringSwitch theSwitch,
      Set<BasicBlock> newBlocks) {
    BasicBlock fallthroughBlock = theSwitch.fallthroughBlock();
    Map<DexString, BasicBlock> stringToTargetMap = new IdentityHashMap<>();
    theSwitch.forEachCase(stringToTargetMap::put);

    // Remove outgoing control flow edges from the block containing the string switch.
    for (BasicBlock successor : block.getNormalSuccessors()) {
      successor.removePredecessor(block, null);
    }
    block.removeAllNormalSuccessors();

    Set<BasicBlock> blocksTargetedByMultipleSwitchCases = Sets.newIdentityHashSet();
    {
      Set<BasicBlock> seenBefore = SetUtils.newIdentityHashSet(stringToTargetMap.size());
      for (BasicBlock targetBlock : stringToTargetMap.values()) {
        if (!seenBefore.add(targetBlock)) {
          blocksTargetedByMultipleSwitchCases.add(targetBlock);
        }
      }
    }

    // Create a String.equals() check for each case in the string-switch instruction.
    BasicBlock previous = null;
    for (Entry<DexString, BasicBlock> entry : stringToTargetMap.entrySet()) {
      ConstString constStringInstruction =
          new ConstString(code.createValue(stringType), entry.getKey(), throwingInfo);
      constStringInstruction.setPosition(Position.syntheticNone());

      InvokeVirtual invokeInstruction =
          new InvokeVirtual(
              appView.dexItemFactory().stringMethods.equals,
              code.createValue(PrimitiveTypeLatticeElement.INT),
              ImmutableList.of(theSwitch.value(), constStringInstruction.outValue()));
      invokeInstruction.setPosition(Position.syntheticNone());

      If ifInstruction = new If(If.Type.NE, invokeInstruction.outValue());
      ifInstruction.setPosition(Position.none());

      BasicBlock targetBlock = entry.getValue();
      if (blocksTargetedByMultipleSwitchCases.contains(targetBlock)) {
        // Need an intermediate block to avoid critical edges.
        BasicBlock intermediateBlock =
            BasicBlock.createGotoBlock(code.blocks.size(), Position.none(), code.metadata());
        intermediateBlock.link(targetBlock);
        blockIterator.add(intermediateBlock);
        newBlocks.add(intermediateBlock);
        targetBlock = intermediateBlock;
      }

      BasicBlock newBlock =
          BasicBlock.createIfBlock(
              code.blocks.size(),
              ifInstruction,
              code.metadata(),
              constStringInstruction,
              invokeInstruction);
      newBlock.link(targetBlock);
      blockIterator.add(newBlock);
      newBlocks.add(newBlock);

      if (previous == null) {
        // Replace the string-switch instruction by a goto instruction.
        block.exit().replace(new Goto(newBlock), code);
        block.link(newBlock);
      } else {
        // Set the fallthrough block for the previously added if-instruction.
        previous.link(newBlock);
      }

      previous = newBlock;
    }

    assert previous != null;

    // Set the fallthrough block for the last if-instruction.
    previous.link(fallthroughBlock);
  }
}
