// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MainDexClasses {

  public static MainDexClasses NONE = new MainDexClasses(ImmutableSet.of(), ImmutableSet.of());

  public static class Builder {
    public final AppInfo appInfo;
    public final Set<DexType> roots = Sets.newIdentityHashSet();
    public final Set<DexType> dependencies = Sets.newIdentityHashSet();

    private Builder(AppInfo appInfo) {
      this.appInfo = appInfo;
    }

    public Builder addRoot(DexType type) {
      assert isProgramClass(type) : type.toSourceString();
      roots.add(type);
      return this;
    }

    public Builder addRoots(Collection<DexType> rootSet) {
      assert rootSet.stream().allMatch(this::isProgramClass);
      this.roots.addAll(rootSet);
      return this;
    }

    public Builder addDependency(DexType type) {
      assert isProgramClass(type);
      dependencies.add(type);
      return this;
    }

    public boolean contains(DexType type) {
      return roots.contains(type) || dependencies.contains(type);
    }

    public MainDexClasses build() {
      return new MainDexClasses(roots, dependencies);
    }

    private boolean isProgramClass(DexType dexType) {
      DexClass clazz = appInfo.definitionFor(dexType);
      return clazz != null && clazz.isProgramClass();
    }
  }

  // The classes in the root set.
  private final Set<DexType> roots;
  // Additional dependencies (direct dependencies and runtime annotations with enums).
  private final Set<DexType> dependencies;
  // All main dex classes.
  private final Set<DexType> classes;

  private MainDexClasses(Set<DexType> roots, Set<DexType> dependencies) {
    assert Sets.intersection(roots, dependencies).isEmpty();
    this.roots = Collections.unmodifiableSet(roots);
    this.dependencies = Collections.unmodifiableSet(dependencies);
    this.classes = Sets.union(roots, dependencies);
  }

  public boolean isEmpty() {
    assert !roots.isEmpty() || dependencies.isEmpty();
    return roots.isEmpty();
  }

  public Set<DexType> getRoots() {
    return roots;
  }

  public Set<DexType> getDependencies() {
    return dependencies;
  }

  public Set<DexType> getClasses() {
    return classes;
  }

  private void collectTypesMatching(
      Set<DexType> types, Predicate<DexType> predicate, Consumer<DexType> consumer) {
    types.forEach(
        type -> {
          if (predicate.test(type)) {
            consumer.accept(type);
          }
        });
  }

  public MainDexClasses prunedCopy(AppInfoWithLiveness appInfo) {
    Builder builder = builder(appInfo);
    Predicate<DexType> wasPruned = appInfo::wasPruned;
    collectTypesMatching(roots, wasPruned.negate(), builder::addRoot);
    collectTypesMatching(dependencies, wasPruned.negate(), builder::addDependency);
    return builder.build();
  }

  public static Builder builder(AppInfo appInfo) {
    return new Builder(appInfo);
  }
}
