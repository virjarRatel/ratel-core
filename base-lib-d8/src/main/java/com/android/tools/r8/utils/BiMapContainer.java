// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.util.Map;

/**
 * BiMapContainer is a utility class that can be used when testing BiMaps in R8, reducing the need
 * to relocate com.google.common in our tests.
 *
 * @param <K> The type of keys
 * @param <V> The type of values
 */
public class BiMapContainer<K, V> {

  public final Map<K, V> original;
  public final Map<K, V> inverse;

  public BiMapContainer(Map<K, V> original, Map<K, V> inverse) {
    this.original = original;
    this.inverse = inverse;
  }
}
