// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.Unreachable;

/**
 *  Simple pair to enable returning multiple values from a function.
 *
 *  <p>This should not be used for anything besides arguments and return values. In particular, it
 *  is not comparable and does not support hashing. Introduce a proper named type for use cases
 *  demanding such.
 */
public class Pair<T, S> {
  private T first;
  private S second;

  public Pair() {
    this(null, null);
  }

  public Pair(T first, S second) {
    this.first = first;
    this.second = second;
  }

  public T getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public void setFirst(T first) {
    this.first = first;
  }

  public void setSecond(S second) {
    this.second = second;
  }

  @Override
  public int hashCode() {
    throw new Unreachable("Pair does not want to support hashing!");
  }

  @Override
  public boolean equals(Object obj) {
    throw new Unreachable("Pair does not want to support equality!");
  }
}
