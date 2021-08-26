// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;

public final class FloatMethodRewrites {

  private FloatMethodRewrites() {}

  public static void rewriteHashCode(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    InvokeStatic mathInvoke = new InvokeStatic(
        factory.createMethod(factory.boxedFloatType, invoke.getInvokedMethod().proto,
            "floatToIntBits"), invoke.outValue(), invoke.inValues(), false);
    iterator.replaceCurrentInstruction(mathInvoke);
  }
}
