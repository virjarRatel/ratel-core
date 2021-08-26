// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.util.function.IntConsumer;

public abstract class ThrowingIntIterator<E extends Exception> {

  public void forEachRemaining(IntConsumer fn) throws E {
    while (hasNext()) {
      fn.accept(nextInt());
    }
  }

  public abstract boolean hasNext();

  public abstract int nextInt() throws E;

  public int nextIntComputeIfAbsent(ThrowingIntSupplier<E> supplier) throws E {
    if (hasNext()) {
      return nextInt();
    }
    return supplier.getAsInt();
  }
}
