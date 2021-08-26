// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;
import java.util.Collections;

public final class CollectionMethodRewrites {

  private CollectionMethodRewrites() {}

  public static void rewriteListOfEmpty(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    rewriteToCollectionMethod(invoke, iterator, factory, "emptyList");
  }

  public static void rewriteSetOfEmpty(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    rewriteToCollectionMethod(invoke, iterator, factory, "emptySet");
  }

  public static void rewriteMapOfEmpty(
      InvokeMethod invoke, InstructionListIterator iterator, DexItemFactory factory) {
    rewriteToCollectionMethod(invoke, iterator, factory, "emptyMap");
  }

  private static void rewriteToCollectionMethod(InvokeMethod invoke,
      InstructionListIterator iterator, DexItemFactory factory, String methodName) {
    assert invoke.inValues().isEmpty();

    DexMethod collectionsEmptyList =
        factory.createMethod(factory.collectionsType, invoke.getInvokedMethod().proto, methodName);
    InvokeStatic newInvoke =
        new InvokeStatic(collectionsEmptyList, invoke.outValue(), Collections.emptyList());
    iterator.replaceCurrentInstruction(newInvoke);
  }
}
