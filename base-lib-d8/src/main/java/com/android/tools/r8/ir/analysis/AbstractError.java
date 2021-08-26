// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;

// Note that this is not what D8/R8 can throw.
//
// Finer-grained abstraction of Throwable that an instruction can throw if any.
//
//       Top        // throwing, but not quite sure what it would be.
//    /   |    \
//  NPE  ICCE  ...  // specific exceptions.
//    \   |    /
//      Bottom      // not throwing.
public class AbstractError {

  private static final AbstractError TOP = new AbstractError();
  private static final AbstractError BOTTOM = new AbstractError();

  private DexType simulatedError;

  private AbstractError() {}

  private AbstractError(DexType throwable) {
    simulatedError = throwable;
  }

  public static AbstractError top() {
    return TOP;
  }

  public static AbstractError bottom() {
    return BOTTOM;
  }

  public static AbstractError specific(DexType throwable) {
    return new AbstractError(throwable);
  }

  public boolean cannotThrow() {
    return this == BOTTOM;
  }

  public boolean isThrowing() {
    return this != BOTTOM;
  }

  public DexType getSpecificError(DexItemFactory factory) {
    assert isThrowing();
    if (simulatedError != null) {
      return simulatedError;
    }
    assert this == TOP;
    return factory.throwableType;
  }

}
