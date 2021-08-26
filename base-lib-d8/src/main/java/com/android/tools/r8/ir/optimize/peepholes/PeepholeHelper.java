// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.StackValues;
import com.android.tools.r8.ir.code.Value;
import java.util.List;
import java.util.function.Predicate;

public class PeepholeHelper {

  public static Predicate<Instruction> withoutLocalInfo(Predicate<Instruction> predicate) {
    return t ->
        predicate.test(t)
            && !t.hasInValueWithLocalInfo()
            && (t.outValue() == null || !t.outValue().hasLocalInfo());
  }

  public static void resetNext(InstructionListIterator it, int count) {
    for (int i = 0; i < count; i++) {
      it.previous();
    }
  }

  public static void resetPrevious(InstructionListIterator it, int count) {
    for (int i = 0; i < count; i++) {
      it.next();
    }
  }

  public static int numberOfValuesPutOnStack(Instruction instruction) {
    Value outValue = instruction.outValue();
    if (outValue instanceof StackValues) {
      return ((StackValues) outValue).getStackValues().length;
    } else if (outValue != null && outValue.isValueOnStack()) {
      return 1;
    }
    return 0;
  }

  public static int numberOfValuesConsumedFromStack(Instruction instruction) {
    int count = 0;
    for (int i = instruction.inValues().size() - 1; i >= 0; i--) {
      if (instruction.inValues().get(i).isValueOnStack()) {
        count += 1;
      }
    }
    return count;
  }

  public static void moveInstructionsUpToCurrentPosition(
      InstructionListIterator it, List<Instruction> instructions) {
    assert !instructions.isEmpty();
    for (Instruction instruction : instructions) {
      for (Value inValue : instruction.inValues()) {
        inValue.addUser(instruction);
      }
      it.add(instruction);
    }
    Instruction current = it.nextUntil(i -> i == instructions.get(0));
    for (int i = 0; i < instructions.size(); i++) {
      assert current == instructions.get(i);
      it.removeOrReplaceByDebugLocalRead();
      current = it.next();
    }
    it.previousUntil(i -> i == instructions.get(instructions.size() - 1));
    it.next();
  }
}
