// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.graph.KeyedDexItem;
import com.android.tools.r8.graph.PresortedComparable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class OrderedMergingIterator<T extends KeyedDexItem<S>, S extends PresortedComparable<S>>
    implements Iterator<T> {

  private final List<T> one;
  private final List<T> other;
  private int oneIndex = 0;
  private int otherIndex = 0;

  public OrderedMergingIterator(List<T> one, List<T> other) {
    this.one = one;
    this.other = other;
  }

  private static <T> T getNextChecked(List<T> list, int position) {
    if (position >= list.size()) {
      throw new NoSuchElementException();
    }
    return list.get(position);
  }

  @Override
  public boolean hasNext() {
    return oneIndex < one.size() || otherIndex < other.size();
  }

  @Override
  public T next() {
    if (oneIndex >= one.size()) {
      return getNextChecked(other, otherIndex++);
    }
    if (otherIndex >= other.size()) {
      return getNextChecked(one, oneIndex++);
    }
    int comparison = one.get(oneIndex).getKey().compareTo(other.get(otherIndex).getKey());
    if (comparison < 0) {
      return one.get(oneIndex++);
    }
    if (comparison == 0) {
      throw new InternalCompilerError("Source arrays are not disjoint.");
    }
    return other.get(otherIndex++);
  }
}
