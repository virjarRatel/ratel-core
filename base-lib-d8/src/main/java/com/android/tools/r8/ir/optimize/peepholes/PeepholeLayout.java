// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PeepholeLayout {
  private List<List<Instruction>> instructions;
  private final PeepholeExpression[] expressions;
  private final boolean backwards;

  private PeepholeLayout(boolean backwards, PeepholeExpression... expressions) {
    this.instructions = new ArrayList<>(expressions.length);
    for (int i = 0; i < expressions.length; i++) {
      this.instructions.add(new ArrayList<>());
      expressions[i].setIndex(i);
    }
    this.backwards = backwards;
    this.expressions = expressions;
  }

  public static PeepholeLayout lookForward(PeepholeExpression... expressions) {
    return new PeepholeLayout(false, expressions);
  }

  public static PeepholeLayout lookBackward(PeepholeExpression... expressions) {
    return new PeepholeLayout(true, expressions);
  }

  public Match test(InstructionListIterator it) {
    if (backwards) {
      return testDirection(() -> it.hasPrevious(), () -> it.previous(), () -> it.next());
    } else {
      return testDirection(() -> it.hasNext(), () -> it.next(), () -> it.previous());
    }
  }

  private Match testDirection(
      Supplier<Boolean> hasNextInstruction,
      Supplier<Instruction> nextInstruction,
      Runnable resetOne) {
    if (!hasNextInstruction.get()) {
      return null;
    }
    boolean success = true;
    Instruction current = nextInstruction.get();
    int index;
    for (index = 0; index < expressions.length; index++) {
      PeepholeExpression e = expressions[index];
      List<Instruction> expInstructions = instructions.get(index);
      expInstructions.clear();
      if (current != null) {
        for (int i = 0; i < e.getMax(); i++) {
          if (!e.getPredicate().test(current)) {
            break;
          }
          expInstructions.add(current);
          if (!hasNextInstruction.get()) {
            current = null;
            break;
          }
          current = nextInstruction.get();
        }
      }
      success &= expInstructions.size() >= e.getMin() && expInstructions.size() <= e.getMax();
      if (!success) {
        break;
      }
    }
    for (int i = 0; i < index; i++) {
      for (int j = 0; j < instructions.get(i).size(); j++) {
        resetOne.run();
      }
    }
    if (current != null) {
      resetOne.run();
    }
    return success ? new Match(expressions, instructions) : null;
  }
}
