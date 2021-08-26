// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.CatchHandlers.CatchHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class CatchHandlers<T> implements Iterable<CatchHandler<T>> {

  public static class CatchHandler<T> {

    public final DexType guard;
    public final T target;

    public CatchHandler(DexType guard, T target) {
      this.guard = guard;
      this.target = target;
    }
  }

  private final List<DexType> guards;
  private final List<T> targets;
  private Set<T> uniqueTargets;

  public static final CatchHandlers<Integer> EMPTY_INDICES = new CatchHandlers<>();
  public static final CatchHandlers<BasicBlock> EMPTY_BASIC_BLOCK = new CatchHandlers<>();

  private CatchHandlers() {
    guards = ImmutableList.of();
    targets = ImmutableList.of();
  }

  public CatchHandlers(List<DexType> guards, List<T> targets) {
    assert !guards.isEmpty();
    assert guards.size() == targets.size();
    // Guava ImmutableList does not support null elements.
    this.guards = ImmutableList.copyOf(guards);
    this.targets = ImmutableList.copyOf(targets);
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    assert guards.size() == targets.size();
    return guards.size();
  }

  public List<DexType> getGuards() {
    return guards;
  }

  public List<T> getAllTargets() {
    return targets;
  }

  public Set<T> getUniqueTargets() {
    if (uniqueTargets == null) {
      uniqueTargets = ImmutableSet.copyOf(targets);
    }
    return uniqueTargets;
  }

  public boolean hasCatchAll(DexItemFactory factory) {
    return getGuards().size() > 0
        && getGuards().get(getGuards().size() - 1) == factory.throwableType;
  }

  public CatchHandlers<T> appendGuard(DexType guard, T target) {
    assert !guards.contains(guard);
    List<DexType> newGuards = ImmutableList.<DexType>builder().addAll(guards).add(guard).build();
    List<T> newTargets = ImmutableList.<T>builder().addAll(targets).add(target).build();
    return new CatchHandlers<>(newGuards, newTargets);
  }

  public CatchHandlers<T> removeGuard(DexType guardToBeRemoved) {
    List<DexType> newGuards = new ArrayList<>();
    List<T> newTargets = new ArrayList<>();
    forEach(
        (guard, target) -> {
          if (guard != guardToBeRemoved) {
            newGuards.add(guard);
            newTargets.add(target);
          }
        });
    return new CatchHandlers<>(newGuards, newTargets);
  }

  public void forEach(BiConsumer<DexType, T> consumer) {
    for (int i = 0; i < size(); ++i) {
      consumer.accept(guards.get(i), targets.get(i));
    }
  }

  @Override
  public Iterator<CatchHandler<T>> iterator() {
    return new Iterator<CatchHandler<T>>() {

      private int nextIndex = 0;

      @Override
      public boolean hasNext() {
        return nextIndex < size();
      }

      @Override
      public CatchHandler<T> next() {
        DexType guard = guards.get(nextIndex);
        T target = targets.get(nextIndex);
        ++nextIndex;
        return new CatchHandler<>(guard, target);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CatchHandlers)) {
      return false;
    }
    CatchHandlers<?> that = (CatchHandlers<?>) o;
    return guards.equals(that.guards) && targets.equals(that.targets);
  }

  @Override
  public int hashCode() {
    return 31 * guards.hashCode() + targets.hashCode();
  }
}
