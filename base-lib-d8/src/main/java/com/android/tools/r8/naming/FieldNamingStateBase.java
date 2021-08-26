// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.ProguardConfiguration;
import java.util.Map;

abstract class FieldNamingStateBase<T> {

  final AppView<?> appView;
  final Map<DexType, T> internalStates;

  FieldNamingStateBase(AppView<?> appView, Map<DexType, T> internalStates) {
    this.appView = appView;
    this.internalStates = internalStates;
  }

  final T getInternalState(DexType type) {
    DexType internalStateKey = getInternalStateKey(type);
    return internalStates.get(internalStateKey);
  }

  final T getOrCreateInternalState(DexField field) {
    return getOrCreateInternalState(field.type);
  }

  final T getOrCreateInternalState(DexType type) {
    DexType internalStateKey = getInternalStateKey(type);
    return internalStates.computeIfAbsent(internalStateKey, key -> createInternalState());
  }

  private DexType getInternalStateKey(DexType type) {
    ProguardConfiguration proguardConfiguration = appView.options().getProguardConfiguration();
    return proguardConfiguration.isOverloadAggressively()
        ? type
        : appView.dexItemFactory().voidType;
  }

  abstract T createInternalState();
}
