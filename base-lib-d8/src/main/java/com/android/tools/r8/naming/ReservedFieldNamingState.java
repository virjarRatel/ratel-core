// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.ReservedFieldNamingState.InternalState;
import java.util.IdentityHashMap;
import java.util.Map;

class ReservedFieldNamingState extends FieldNamingStateBase<InternalState> {

  ReservedFieldNamingState(AppView<?> appView) {
    super(appView, new IdentityHashMap<>());
  }

  boolean isReserved(DexString name, DexType type) {
    return getReservedByName(name, type) != null;
  }

  DexString getReservedByName(DexString name, DexType type) {
    InternalState internalState = getInternalState(type);
    return internalState == null ? null : internalState.getReservedByName(name);
  }

  void markReservedDirectly(DexString name, DexString originalName, DexType type) {
    getOrCreateInternalState(type).markReservedDirectly(name, originalName);
  }

  void includeReservations(ReservedFieldNamingState reservedNames) {
    for (Map.Entry<DexType, InternalState> entry : reservedNames.internalStates.entrySet()) {
      getOrCreateInternalState(entry.getKey()).includeReservations(entry.getValue());
    }
  }

  void includeReservationsFromBelow(ReservedFieldNamingState reservedNames) {
    for (Map.Entry<DexType, InternalState> entry : reservedNames.internalStates.entrySet()) {
      getOrCreateInternalState(entry.getKey()).includeReservationsFromBelow(entry.getValue());
    }
  }

  @Override
  InternalState createInternalState() {
    return new InternalState();
  }

  static class InternalState {

    private Map<DexString, DexString> reservedNamesDirect = new IdentityHashMap<>();
    private Map<DexString, DexString> reservedNamesBelow = new IdentityHashMap<>();

    DexString getReservedByName(DexString name) {
      DexString reservedBy = reservedNamesDirect.get(name);
      return reservedBy != null ? reservedBy : reservedNamesBelow.get(name);
    }

    void markReservedDirectly(DexString name, DexString originalName) {
      reservedNamesDirect.put(name, originalName);
    }

    void includeReservations(InternalState state) {
      reservedNamesDirect.putAll(state.reservedNamesDirect);
    }

    void includeReservationsFromBelow(InternalState state) {
      reservedNamesBelow.putAll(state.reservedNamesDirect);
      reservedNamesBelow.putAll(state.reservedNamesBelow);
    }
  }
}
