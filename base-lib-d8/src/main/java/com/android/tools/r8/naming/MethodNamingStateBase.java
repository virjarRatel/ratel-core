// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

abstract class MethodNamingStateBase<KeyType, InternalState> {

  final Map<KeyType, InternalState> internalStates;
  final Function<DexMethod, KeyType> keyTransform;

  MethodNamingStateBase(Function<DexMethod, KeyType> keyTransform) {
    this.keyTransform = keyTransform;
    this.internalStates = new HashMap<>();
  }

  final InternalState getInternalState(DexMethod method) {
    KeyType internalStateKey = keyTransform.apply(method);
    return internalStates.get(internalStateKey);
  }

  final InternalState getOrCreateInternalState(DexMethod method) {
    KeyType internalStateKey = keyTransform.apply(method);
    return internalStates.computeIfAbsent(internalStateKey, key -> createInternalState(method));
  }

  abstract InternalState createInternalState(DexMethod method);
}
