// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.MethodNamingState.InternalNewNameState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

class MethodNamingState<KeyType> extends MethodNamingStateBase<KeyType, InternalNewNameState> {

  private final MethodReservationState<?> reservationState;
  private final MethodNamingState<KeyType> parentNamingState;
  private final MemberNamingStrategy namingStrategy;

  private MethodNamingState(
      MethodNamingState<KeyType> parentNamingState,
      Function<DexMethod, KeyType> keyTransform,
      MemberNamingStrategy strategy,
      MethodReservationState<?> reservationState) {
    super(keyTransform);
    this.parentNamingState = parentNamingState;
    this.namingStrategy = strategy;
    this.reservationState = reservationState;
  }

  static <KeyType> MethodNamingState<KeyType> createRoot(
      Function<DexMethod, KeyType> keyTransform,
      MemberNamingStrategy namingStrategy,
      MethodReservationState<?> reservationState) {
    return new MethodNamingState<>(null, keyTransform, namingStrategy, reservationState);
  }

  MethodNamingState<KeyType> createChild(MethodReservationState<?> frontierReservationState) {
    return new MethodNamingState<>(
        this, this.keyTransform, this.namingStrategy, frontierReservationState);
  }

  DexString newOrReservedNameFor(DexMethod method) {
    return newOrReservedNameFor(method, this::isAvailable);
  }

  DexString newOrReservedNameFor(DexMethod method, BiPredicate<DexString, DexMethod> isAvailable) {
    DexString newName = getAssignedName(method);
    if (newName != null) {
      return newName;
    }
    Set<DexString> reservedNamesFor = reservationState.getReservedNamesFor(method);
    // Reservations with applymapping can cause multiple reserved names added to the frontier. In
    // that case, the strategy will return the correct one.
    if (reservedNamesFor != null && reservedNamesFor.size() == 1) {
      DexString candidate = reservedNamesFor.iterator().next();
      if (isAvailable(candidate, method)) {
        return candidate;
      }
    }
    InternalNewNameState internalState = getOrCreateInternalState(method);
    newName = namingStrategy.next(method, internalState, isAvailable);
    assert newName != null;
    return newName;
  }

  void addRenaming(DexString newName, DexMethod method) {
    InternalNewNameState internalState = getOrCreateInternalState(method);
    internalState.addRenaming(newName, method);
  }

  boolean isAvailable(DexString candidate, DexMethod method) {
    Set<DexString> usedBy = getUsedBy(candidate, method);
    if (usedBy != null && usedBy.contains(method.name)) {
      return true;
    }
    boolean isReserved = reservationState.isReserved(candidate, method);
    if (!isReserved && usedBy == null) {
      return true;
    }
    // We now have a reserved name. We therefore have to check if the reservation is
    // equal to candidate, otherwise the candidate is not available.
    if (isReserved && usedBy == null) {
      Set<DexString> methodReservedNames = reservationState.getReservedNamesFor(method);
      return methodReservedNames != null && methodReservedNames.contains(candidate);
    }
    return false;
  }

  private Set<DexString> getUsedBy(DexString name, DexMethod method) {
    InternalNewNameState internalState = getInternalState(method);
    Set<DexString> nameUsedBy = null;
    if (internalState != null) {
      nameUsedBy = internalState.getUsedBy(name);
    }
    if (nameUsedBy == null && parentNamingState != null) {
      return parentNamingState.getUsedBy(name, method);
    }
    return nameUsedBy;
  }

  private DexString getAssignedName(DexMethod method) {
    DexString assignedName = null;
    InternalNewNameState internalState = getInternalState(method);
    if (internalState != null) {
      assignedName = internalState.getAssignedName(method.name);
    }
    if (assignedName == null && parentNamingState != null) {
      assignedName = parentNamingState.getAssignedName(method);
    }
    return assignedName;
  }

  @Override
  InternalNewNameState createInternalState(DexMethod method) {
    InternalNewNameState parentInternalState = null;
    if (this.parentNamingState != null) {
      parentInternalState = parentNamingState.getOrCreateInternalState(method);
    }
    return new InternalNewNameState(parentInternalState);
  }

  static class InternalNewNameState implements InternalNamingState {

    private final InternalNewNameState parentInternalState;
    private Map<DexString, DexString> originalToRenamedNames = new HashMap<>();
    private Map<DexString, Set<DexString>> usedBy = new HashMap<>();

    private static final int INITIAL_NAME_COUNT = 1;
    private static final int INITIAL_DICTIONARY_INDEX = 0;

    private int virtualNameCount;
    private int directNameCount = 0;
    private int dictionaryIndex;

    private InternalNewNameState(InternalNewNameState parentInternalState) {
      this.parentInternalState = parentInternalState;
      this.dictionaryIndex =
          parentInternalState == null
              ? INITIAL_DICTIONARY_INDEX
              : parentInternalState.dictionaryIndex;
      this.virtualNameCount =
          parentInternalState == null ? INITIAL_NAME_COUNT : parentInternalState.virtualNameCount;
    }

    @Override
    public int getDictionaryIndex() {
      return dictionaryIndex;
    }

    @Override
    public int incrementDictionaryIndex() {
      return dictionaryIndex++;
    }

    Set<DexString> getUsedBy(DexString name) {
      return usedBy.get(name);
    }

    DexString getAssignedName(DexString originalName) {
      return originalToRenamedNames.get(originalName);
    }

    void addRenaming(DexString newName, DexMethod method) {
      originalToRenamedNames.put(method.name, newName);
      usedBy.computeIfAbsent(newName, ignore -> new HashSet<>()).add(method.name);
    }

    private boolean checkParentPublicNameCountIsLessThanOrEqual() {
      int maxParentCount = 0;
      InternalNewNameState tmp = parentInternalState;
      while (tmp != null) {
        maxParentCount = Math.max(tmp.virtualNameCount, maxParentCount);
        tmp = tmp.parentInternalState;
      }
      assert maxParentCount <= virtualNameCount;
      return true;
    }

    @Override
    public int incrementNameIndex(boolean isDirectMethodCall) {
      assert checkParentPublicNameCountIsLessThanOrEqual();
      if (isDirectMethodCall) {
        return virtualNameCount + directNameCount++;
      } else {
        // TODO(b/144877828): is it guaranteed?
        assert directNameCount == 0;
        return virtualNameCount++;
      }
    }
  }
}
