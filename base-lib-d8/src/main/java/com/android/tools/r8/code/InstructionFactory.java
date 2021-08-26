// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.graph.OffsetToObjectMapping;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructionFactory extends BaseInstructionFactory {

  static private Instruction readFrom(ShortBufferBytecodeStream stream,
      OffsetToObjectMapping mapping) {
    int high = stream.nextByte();
    int opcode = stream.nextByte();
    return create(high, opcode, stream, mapping);
  }

  public Instruction[] readSequenceFrom(ShortBuffer buffer, int startIndex, int length,
      OffsetToObjectMapping mapping) {
    ShortBufferBytecodeStream range =
        new ShortBufferBytecodeStream(buffer, startIndex, length);
    List<Instruction> insn = new ArrayList<>(length);
    while (range.hasMore()) {
      Instruction instruction = readFrom(range, mapping);
      insn.add(instruction);
    }
    return insn.toArray(Instruction.EMPTY_ARRAY);
  }

  private static class ShortBufferBytecodeStream implements BytecodeStream {

    private final int length;
    private final int startIndex;
    private final ShortBuffer source;

    private int offset = 0;
    private int nextByte;
    private boolean cacheContainsValidByte = false;

    ShortBufferBytecodeStream(ShortBuffer source, int startIndex, int length) {
      this.startIndex = startIndex;
      this.length = length;
      this.source = source;
    }

    @Override
    public int nextShort() {
      assert !cacheContainsValidByte : "Unread byte in cache.";
      assert offset < length;
      int result = source.get(startIndex + offset);
      offset += 1;
      return result;
    }

    @Override
    public int nextByte() {
      if (cacheContainsValidByte) {
        cacheContainsValidByte = false;
        return nextByte;
      } else {
        int next = nextShort();
        nextByte = next & 0xff;
        cacheContainsValidByte = true;
        return (next >> 8) & 0xff;
      }
    }

    @Override
    public boolean hasMore() {
      return length - offset > 0;
    }

    @Override
    public int getOffset() {
      return offset;
    }
  }
}
