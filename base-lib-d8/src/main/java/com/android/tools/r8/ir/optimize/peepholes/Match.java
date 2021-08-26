// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.peepholes;

import com.android.tools.r8.ir.code.Instruction;
import java.util.List;

public class Match {
  public final PeepholeExpression[] expressions;
  public final List<List<Instruction>> instructions;

  public Match(PeepholeExpression[] expressions, List<List<Instruction>> instructions) {
    this.expressions = expressions;
    this.instructions = instructions;
  }
}
