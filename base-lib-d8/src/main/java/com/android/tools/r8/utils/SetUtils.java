// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Function;

public class SetUtils {

  public static <T> Set<T> newIdentityHashSet(T element) {
    Set<T> result = Sets.newIdentityHashSet();
    result.add(element);
    return result;
  }

  public static <T> Set<T> newIdentityHashSet(Iterable<T> c) {
    Set<T> result = Sets.newIdentityHashSet();
    c.forEach(result::add);
    return result;
  }

  public static <T> Set<T> newIdentityHashSet(int capacity) {
    return Collections.newSetFromMap(new IdentityHashMap<>(capacity));
  }

  public static <T> Set<T> newIdentityHashSet(T element, int capacity) {
    Set<T> result = newIdentityHashSet(capacity);
    result.add(element);
    return result;
  }

  public static <T, S> Set<T> mapIdentityHashSet(Set<S> set, Function<S, T> fn) {
    Set<T> out = newIdentityHashSet(set.size());
    for (S element : set) {
      out.add(fn.apply(element));
    }
    return out;
  }
}
