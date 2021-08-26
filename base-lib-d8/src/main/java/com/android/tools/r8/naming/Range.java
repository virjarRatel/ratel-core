// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

/** Represents a line number range. */
public class Range {

  public final int from;
  public final int to;

  public Range(int from, int to) {
    this.from = from;
    this.to = to;
  }

  public boolean contains(int value) {
    return from <= value && value <= to;
  }

  @Override
  public String toString() {
    return from + ":" + to;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Range)) {
      return false;
    }

    Range range = (Range) o;
    return from == range.from && to == range.to;
  }

  @Override
  public int hashCode() {
    int result = from;
    result = 31 * result + to;
    return result;
  }
}
