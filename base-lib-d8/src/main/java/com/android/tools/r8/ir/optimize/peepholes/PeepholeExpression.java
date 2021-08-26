// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import java.util.function.Predicate;

public interface PeepholeExpression {
  Predicate<Instruction> getPredicate();

  int getMin();

  int getMax();

  /**
   * This is set by PeepholeLayout when it observes the index of the expression in the pattern.
   *
   * @param index
   */
  void setIndex(int index);
}
