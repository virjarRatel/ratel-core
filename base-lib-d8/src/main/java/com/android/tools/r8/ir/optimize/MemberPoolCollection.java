// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.Descriptor;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.TopDownClassHierarchyTraversal;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Predicate;

// Per-class collection of member signatures.
public abstract class MemberPoolCollection<T extends Descriptor> {

  final Equivalence<T> equivalence;
  final AppView<? extends AppInfoWithSubtyping> appView;
  final Map<DexClass, MemberPool<T>> memberPools = new ConcurrentHashMap<>();

  MemberPoolCollection(
      AppView<? extends AppInfoWithSubtyping> appView, Equivalence<T> equivalence) {
    this.appView = appView;
    this.equivalence = equivalence;
  }

  public void buildAll(ExecutorService executorService, Timing timing) throws ExecutionException {
    timing.begin("Building member pool collection");
    try {
      List<Future<?>> futures = new ArrayList<>();

      // Generate a future for each class that will build the member pool collection for the
      // corresponding class. Note that, we visit the classes using a top-down class hierarchy
      // traversal, since this ensures that we do not visit library classes that are not
      // reachable from any program class.
      TopDownClassHierarchyTraversal.forAllClasses(appView)
          .visit(appView.appInfo().classes(), clazz -> submit(clazz, futures, executorService));
      ThreadUtils.awaitFutures(futures);
    } finally {
      timing.end();
    }
  }

  public MemberPool<T> buildForHierarchy(
      DexClass clazz, ExecutorService executorService, Timing timing) throws ExecutionException {
    timing.begin("Building member pool collection");
    try {
      List<Future<?>> futures = new ArrayList<>();
      submitAll(
          getAllSuperTypesInclusive(clazz, memberPools::containsKey), futures, executorService);
      submitAll(getAllSubTypesExclusive(clazz, memberPools::containsKey), futures, executorService);
      ThreadUtils.awaitFutures(futures);
    } finally {
      timing.end();
    }
    return get(clazz);
  }

  public boolean hasPool(DexClass clazz) {
    return memberPools.containsKey(clazz);
  }

  public MemberPool<T> get(DexClass clazz) {
    assert hasPool(clazz);
    return memberPools.get(clazz);
  }

  public boolean markIfNotSeen(DexClass clazz, T reference) {
    MemberPool<T> memberPool = get(clazz);
    Wrapper<T> key = equivalence.wrap(reference);
    if (memberPool.hasSeen(key)) {
      return true;
    }
    memberPool.seen(key);
    return false;
  }

  private void submitAll(
      Iterable<? extends DexClass> classes,
      List<Future<?>> futures,
      ExecutorService executorService) {
    for (DexClass clazz : classes) {
      submit(clazz, futures, executorService);
    }
  }

  private void submit(DexClass clazz, List<Future<?>> futures, ExecutorService executorService) {
    futures.add(executorService.submit(computeMemberPoolForClass(clazz)));
  }

  abstract Runnable computeMemberPoolForClass(DexClass clazz);

  // TODO(jsjeon): maybe be part of AppInfoWithSubtyping?
  private Set<DexClass> getAllSuperTypesInclusive(
      DexClass subject, Predicate<DexClass> stoppingCriterion) {
    Set<DexClass> superTypes = new HashSet<>();
    Deque<DexClass> worklist = new ArrayDeque<>();
    worklist.add(subject);
    while (!worklist.isEmpty()) {
      DexClass clazz = worklist.pop();
      if (stoppingCriterion.test(clazz)) {
        continue;
      }
      if (superTypes.add(clazz)) {
        if (clazz.superType != null) {
          addNonNull(worklist, appView.definitionFor(clazz.superType));
        }
        for (DexType interfaceType : clazz.interfaces.values) {
          addNonNull(worklist, appView.definitionFor(interfaceType));
        }
      }
    }
    return superTypes;
  }

  // TODO(jsjeon): maybe be part of AppInfoWithSubtyping?
  private Set<DexClass> getAllSubTypesExclusive(
      DexClass subject, Predicate<DexClass> stoppingCriterion) {
    Set<DexClass> subTypes = new HashSet<>();
    Deque<DexClass> worklist = new ArrayDeque<>();
    appView
        .appInfo()
        .forAllImmediateExtendsSubtypes(
            subject.type, type -> addNonNull(worklist, appView.definitionFor(type)));
    appView
        .appInfo()
        .forAllImmediateImplementsSubtypes(
            subject.type, type -> addNonNull(worklist, appView.definitionFor(type)));
    while (!worklist.isEmpty()) {
      DexClass clazz = worklist.pop();
      if (stoppingCriterion.test(clazz)) {
        continue;
      }
      if (subTypes.add(clazz)) {
        appView
            .appInfo()
            .forAllImmediateExtendsSubtypes(
                clazz.type, type -> addNonNull(worklist, appView.definitionFor(type)));
        appView
            .appInfo()
            .forAllImmediateImplementsSubtypes(
                clazz.type, type -> addNonNull(worklist, appView.definitionFor(type)));
      }
    }
    return subTypes;
  }

  public static class MemberPool<T> {

    private Equivalence<T> equivalence;
    private MemberPool<T> superType;
    private final Set<MemberPool<T>> interfaces = new HashSet<>();
    private final Set<MemberPool<T>> subTypes = new HashSet<>();
    private final Set<Wrapper<T>> memberPool = new HashSet<>();

    MemberPool(Equivalence<T> equivalence) {
      this.equivalence = equivalence;
    }

    synchronized void linkSupertype(MemberPool<T> superType) {
      assert this.superType == null;
      this.superType = superType;
    }

    synchronized void linkSubtype(MemberPool<T> subType) {
      boolean added = subTypes.add(subType);
      assert added;
    }

    synchronized void linkInterface(MemberPool<T> itf) {
      boolean added = interfaces.add(itf);
      assert added;
    }

    public void seen(T member) {
      seen(equivalence.wrap(member));
    }

    public synchronized void seen(Wrapper<T> member) {
      boolean added = memberPool.add(member);
      assert added;
    }

    public boolean hasSeen(Wrapper<T> member) {
      return hasSeenAbove(member, true) || hasSeenStrictlyBelow(member);
    }

    public boolean hasSeenDirectly(Wrapper<T> member) {
      return memberPool.contains(member);
    }

    public boolean hasSeenStrictlyAbove(Wrapper<T> member) {
      return hasSeenAbove(member, false);
    }

    private boolean hasSeenAbove(Wrapper<T> member, boolean inclusive) {
      if (inclusive && hasSeenDirectly(member)) {
        return true;
      }
      return (superType != null && superType.hasSeenAbove(member, true))
          || interfaces.stream().anyMatch(itf -> itf.hasSeenAbove(member, true));
    }

    public boolean hasSeenStrictlyBelow(Wrapper<T> member) {
      return hasSeenBelow(member, false);
    }

    private boolean hasSeenBelow(Wrapper<T> member, boolean inclusive) {
      if (inclusive
          && (hasSeenDirectly(member)
              || interfaces.stream().anyMatch(itf -> itf.hasSeenAbove(member, true)))) {
        return true;
      }
      return subTypes.stream().anyMatch(subType -> subType.hasSeenBelow(member, true));
    }
  }

  private static <T> void addNonNull(Collection<T> collection, T item) {
    if (item != null) {
      collection.add(item);
    }
  }
}
