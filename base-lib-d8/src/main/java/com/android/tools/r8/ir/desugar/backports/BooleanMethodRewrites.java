// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.code.And;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Or;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.Xor;
import java.util.List;

public final class BooleanMethodRewrites {
  public static void rewriteLogicalAnd(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    List<Value> inValues = invoke.inValues();
    assert inValues.size() == 2;

    iterator.replaceCurrentInstruction(
        new And(NumericType.INT, invoke.outValue(), inValues.get(0), inValues.get(1)));
  }

  public static void rewriteLogicalOr(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    List<Value> inValues = invoke.inValues();
    assert inValues.size() == 2;

    iterator.replaceCurrentInstruction(
        new Or(NumericType.INT, invoke.outValue(), inValues.get(0), inValues.get(1)));
  }

  public static void rewriteLogicalXor(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    List<Value> inValues = invoke.inValues();
    assert inValues.size() == 2;

    iterator.replaceCurrentInstruction(
        new Xor(NumericType.INT, invoke.outValue(), inValues.get(0), inValues.get(1)));
  }

  private BooleanMethodRewrites() {
  }
}
