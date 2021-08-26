// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;

public class CollectionUtils {
  public static <T> Set<T> mergeSets(Collection<T> first, Collection<T> second) {
    ImmutableSet.Builder<T> builder = ImmutableSet.builder();
    builder.addAll(first);
    builder.addAll(second);
    return builder.build();
  }
}
