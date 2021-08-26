// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import java.util.ListIterator;
import java.util.function.Predicate;

public interface PreviousUntilIterator<T> extends ListIterator<T> {

  /**
   * Continue to call {@link #previous} while {@code predicate} tests {@code false}.
   *
   * @returns the item that matched the predicate or {@code null} if all items fail the predicate
   *     test
   */
  default T previousUntil(Predicate<T> predicate) {
    while (hasPrevious()) {
      T item = previous();
      if (predicate.test(item)) {
        return item;
      }
    }
    return null;
  }
}
