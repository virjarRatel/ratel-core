// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class ThrowingIterator<T, E extends Exception> {

  public abstract boolean hasNext();

  public abstract T next() throws E;

  public T computeNextIfAbsent(ThrowingSupplier<T, E> supplier) throws E {
    if (hasNext()) {
      return next();
    }
    return supplier.get();
  }

  public List<T> take(int number) throws E {
    List<T> result = new ArrayList<>(number);
    while (number > 0) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      result.add(next());
      number--;
    }
    return result;
  }
}
