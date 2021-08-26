// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfSwitch;
import com.android.tools.r8.cf.code.CfSwitch.Kind;
import com.android.tools.r8.code.Nop;
import com.android.tools.r8.code.PackedSwitch;
import com.android.tools.r8.code.PackedSwitchPayload;
import com.android.tools.r8.code.SparseSwitch;
import com.android.tools.r8.code.SparseSwitchPayload;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.IntObjConsumer;
import com.android.tools.r8.utils.InternalOutputMode;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import java.util.ArrayList;
import java.util.List;

public class IntSwitch extends Switch {

  private final int[] keys;

  public IntSwitch(Value value, int[] keys, int[] targetBlockIndices, int fallthroughBlockIndex) {
    super(value, targetBlockIndices, fallthroughBlockIndex);
    this.keys = keys;
    assert valid();
  }

  @Override
  public int opcode() {
    return Opcodes.INT_SWITCH;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public void forEachCase(IntObjConsumer<BasicBlock> fn) {
    for (int i = 0; i < keys.length; i++) {
      fn.accept(getKey(i), targetBlock(i));
    }
  }

  @Override
  public boolean valid() {
    assert super.valid();
    assert keys.length >= 1;
    assert keys.length <= Constants.U16BIT_MAX;
    // Keys must be acceding, and cannot target the fallthrough.
    assert keys.length == numberOfKeys();
    for (int i = 1; i < keys.length - 1; i++) {
      assert keys[i - 1] < keys[i];
    }
    return true;
  }

  // Number of targets if this switch is emitted as a packed switch.
  private static long numberOfTargetsIfPacked(int[] keys) {
    return ((long) keys[keys.length - 1]) - ((long) keys[0]) + 1;
  }

  public static boolean canBePacked(InternalOutputMode mode, int[] keys) {
    // The size of a switch payload is stored in an ushort in the Dex file.
    return canBePacked(mode, numberOfTargetsIfPacked(keys));
  }

  public static boolean canBePacked(InternalOutputMode mode, long numberOfTargets) {
    // The size of a switch payload is stored in an ushort in the Dex file.
    return numberOfTargets
        <= (mode.isGeneratingClassFiles() ? Constants.U32BIT_MAX : Constants.U16BIT_MAX);
  }

  // Estimated size of the resulting dex instruction in code units (bytes in CF, 16-bit in Dex).
  public static long estimatedSize(InternalOutputMode mode, int[] keys) {
    long sparseSize = sparsePayloadSize(mode, keys) + baseSparseSize(mode);
    long packedSize = Long.MAX_VALUE;
    if (canBePacked(mode, keys)) {
      long packedPayloadSize = packedPayloadSize(mode, keys);
      packedSize = packedPayloadSize + basePackedSize(mode);
      if (packedSize < packedPayloadSize) {
        packedSize = Integer.MAX_VALUE;
      }
    }
    return Math.min(sparseSize, packedSize);
  }

  public static long estimatedSparseSize(InternalOutputMode mode, long keys) {
    return sparsePayloadSize(mode, keys) + baseSparseSize(mode);
  }

  // Estimated size of the packed/table instructions in code units excluding the payload (bytes in
  // CF, 16-bit in Dex).
  public static int basePackedSize(InternalOutputMode mode) {
    if (mode.isGeneratingClassFiles()) {
      // Table switch: opcode + padding + default + high + low.
      return 1 + 3 + 4 + 4 + 4;
    } else {
      return 3;
    }
  }

  // Estimated size of the sparse/lookup instructions in code units excluding the payload (bytes in
  // CF, 16-bit in Dex).
  public static int baseSparseSize(InternalOutputMode mode) {
    if (mode.isGeneratingClassFiles()) {
      // Lookup switch: opcode + padding + default + number of keys.
      return 1 + 3 + 4 + 4;
    } else {
      return 3;
    }
  }

  // Size of the switch payload if emitted as packed in code units (bytes in CF, 16-bit in Dex).
  public static long packedPayloadSize(InternalOutputMode mode, long numberOfTargets) {
    if (mode.isGeneratingClassFiles()) {
      assert numberOfTargets <= Constants.U32BIT_MAX;
      return numberOfTargets * 4;
    } else {
      // This size can not exceed Constants.U16BIT_MAX * 2 + 4 and can be contained in an 32-bit
      // integer.
      return (numberOfTargets * 2) + 4;
    }
  }

  // Size of the switch payload if emitted as packed in code units (bytes in CF, 16-bit in Dex).
  public static long packedPayloadSize(InternalOutputMode mode, int[] keys) {
    assert canBePacked(mode, keys);
    long numberOfTargets = numberOfTargetsIfPacked(keys);
    return packedPayloadSize(mode, numberOfTargets);
  }

  // Size of the switch payload if emitted as sparse in code units (bytes in CF, 16-bit in Dex).
  public static long sparsePayloadSize(InternalOutputMode mode, int[] keys) {
    return sparsePayloadSize(mode, keys.length);
  }

  // Size of the switch payload if emitted as sparse in code units (bytes in CF, 16-bit in Dex).
  public static long sparsePayloadSize(InternalOutputMode mode, long keys) {
    if (mode.isGeneratingClassFiles()) {
      // Lookup-switch has a 32-bit int key and 32-bit int value for each offset, 8 bytes per entry.
      return keys * 8;
    } else {
      // This size can not exceed Constants.U16BIT_MAX * 4 and can be contained in a
      // 32-bit integer.
      return keys * 4 + 2;
    }
  }

  private boolean canBePacked(InternalOutputMode mode) {
    return canBePacked(mode, keys);
  }

  // Size of the switch payload if emitted as packed in code units (bytes in CF, 16-bit in Dex).
  private long packedPayloadSize(InternalOutputMode mode) {
    return packedPayloadSize(mode, keys);
  }

  // Size of the switch payload if emitted as sparse in code units (bytes in CF, 16-bit in Dex).
  private long sparsePayloadSize(InternalOutputMode mode) {
    return sparsePayloadSize(mode, keys);
  }

  private boolean emitPacked(InternalOutputMode mode) {
    return canBePacked(mode) && packedPayloadSize(mode) <= sparsePayloadSize(mode);
  }

  public int getFirstKey() {
    return keys[0];
  }

  @Override
  public boolean isIntSwitch() {
    return true;
  }

  @Override
  public IntSwitch asIntSwitch() {
    return this;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return false;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int value = builder.allocatedRegister(value(), getNumber());
    if (emitPacked(InternalOutputMode.DexIndexed)) {
      builder.addSwitch(this, new PackedSwitch(value));
    } else {
      builder.addSwitch(this, new SparseSwitch(value));
    }
  }

  public int getKey(int index) {
    return keys[index];
  }

  public int[] getKeys() {
    return keys;
  }

  public Int2ReferenceSortedMap<BasicBlock> getKeyToTargetMap() {
    Int2ReferenceSortedMap<BasicBlock> result = new Int2ReferenceAVLTreeMap<>();
    for (int i = 0; i < keys.length; i++) {
      result.put(getKey(i), targetBlock(i));
    }
    return result;
  }

  public Nop buildPayload(int[] targets, int fallthroughTarget, InternalOutputMode mode) {
    assert keys.length == targets.length;
    assert mode.isGeneratingDex();
    if (emitPacked(mode)) {
      int targetsCount = (int) numberOfTargetsIfPacked(keys);
      if (targets.length == targetsCount) {
        // All targets are already present.
        return new PackedSwitchPayload(getFirstKey(), targets);
      } else {
        // Generate the list of targets for all key values. Set the target for keys not present
        // to the fallthrough.
        int[] packedTargets = new int[targetsCount];
        int originalIndex = 0;
        for (int i = 0; i < targetsCount; i++) {
          int key = getFirstKey() + i;
          if (keys[originalIndex] == key) {
            packedTargets[i] = targets[originalIndex];
            originalIndex++;
          } else {
            packedTargets[i] = fallthroughTarget;
          }
        }
        assert originalIndex == keys.length;
        return new PackedSwitchPayload(getFirstKey(), packedTargets);
      }
    } else {
      assert numberOfKeys() == keys.length;
      return new SparseSwitchPayload(keys, targets);
    }
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(super.toString()).append(System.lineSeparator());
    for (int i = 0; i < numberOfKeys(); i++) {
      builder
          .append("          ")
          .append(getKey(i))
          .append(" -> ")
          .append(targetBlock(i).getNumberAsString())
          .append(System.lineSeparator());
    }
    return builder.append("          F -> ").append(fallthroughBlock().getNumber()).toString();
  }

  @Override
  public void print(CfgPrinter printer) {
    super.print(printer);
    for (int index : targetBlockIndices()) {
      BasicBlock target = getBlock().getSuccessors().get(index);
      printer.append(" B").append(target.getNumber());
    }
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    CfLabel fallthroughLabel = builder.getLabel(fallthroughBlock());
    List<CfLabel> labels = new ArrayList<>(numberOfKeys());
    List<BasicBlock> successors = getBlock().getSuccessors();
    if (emitPacked(InternalOutputMode.ClassFile)) {
      int min = keys[0];
      int max = keys[keys.length - 1];
      int index = 0;
      for (long i = min; i <= max; i++) {
        if (i == keys[index]) {
          labels.add(builder.getLabel(successors.get(getTargetBlockIndex(index))));
          index++;
        } else {
          labels.add(fallthroughLabel);
        }
      }
      assert index == numberOfKeys();
      builder.add(new CfSwitch(Kind.TABLE, fallthroughLabel, new int[] {min}, labels));
    } else {
      for (int index : targetBlockIndices()) {
        labels.add(builder.getLabel(successors.get(index)));
      }
      builder.add(new CfSwitch(Kind.LOOKUP, fallthroughLabel, this.keys, labels));
    }
  }
}
