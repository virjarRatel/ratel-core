// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import java.util.List;
import java.util.function.Predicate;

public class Wildcard implements PeepholeExpression {

  private final Predicate<Instruction> predicate;
  private int index = -1;

  public Wildcard(Predicate<Instruction> predicate) {
    this.predicate = predicate;
  }

  @Override
  public Predicate<Instruction> getPredicate() {
    return predicate;
  }

  @Override
  public int getMin() {
    return 0;
  }

  @Override
  public int getMax() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void setIndex(int index) {
    assert this.index == -1;
    this.index = index;
  }

  public List<Instruction> get(Match match) {
    return match.instructions.get(index);
  }
}
