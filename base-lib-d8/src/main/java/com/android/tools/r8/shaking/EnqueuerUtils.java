// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Comparator;
import java.util.Map;

class EnqueuerUtils {

  static <T, U> ImmutableSortedMap<T, U> toImmutableSortedMap(
      Map<T, U> map, Comparator<T> comparator) {
    ImmutableSortedMap.Builder<T, U> builder = new ImmutableSortedMap.Builder<>(comparator);
    map.forEach(builder::put);
    return builder.build();
  }
}
