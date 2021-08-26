// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexString;
import java.util.concurrent.ConcurrentHashMap;

final class DexStringCache {
  private final ConcurrentHashMap<DexString, String> stringCache = new ConcurrentHashMap<>();

  public String lookupString(DexString name) {
    return stringCache.computeIfAbsent(name, DexString::toString);
  }
}
