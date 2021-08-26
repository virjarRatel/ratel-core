// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.dex.ApplicationReader.ProgramClassConflictResolver;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ProgramClassCollection;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class DexApplication {

  public final ImmutableList<DataResourceProvider> dataResourceProviders;

  public final ImmutableSet<DexType> mainDexList;

  private final ClassNameMapper proguardMap;

  public final Timing timing;

  public final InternalOptions options;
  public final DexItemFactory dexItemFactory;

  // Information on the lexicographically largest string referenced from code.
  public final DexString highestSortingString;

  /** Constructor should only be invoked by the DexApplication.Builder. */
  DexApplication(
      ClassNameMapper proguardMap,
      ImmutableList<DataResourceProvider> dataResourceProviders,
      ImmutableSet<DexType> mainDexList,
      InternalOptions options,
      DexString highestSortingString,
      Timing timing) {
    this.proguardMap = proguardMap;
    this.dataResourceProviders = dataResourceProviders;
    this.mainDexList = mainDexList;
    this.options = options;
    this.dexItemFactory = options.itemFactory;
    this.highestSortingString = highestSortingString;
    this.timing = timing;
  }

  public abstract Builder<?> builder();

  // Reorder classes randomly. Note that the order of classes in program or library
  // class collections should not matter for compilation of valid code and when running
  // with assertions enabled we reorder the classes randomly to catch possible issues.
  // Also note that the order may add to non-determinism in reporting errors for invalid
  // code, but this non-determinism exists even with the same order of classes since we
  // may process classes concurrently and fail-fast on the first error.
  private static class ReorderBox<T> {

    private List<T> classes;

    ReorderBox(List<T> classes) {
      this.classes = classes;
    }

    boolean reorderClasses() {
      if (!InternalOptions.DETERMINISTIC_DEBUGGING) {
        List<T> shuffled = new ArrayList<>(classes);
        Collections.shuffle(shuffled);
        classes = ImmutableList.copyOf(shuffled);
      }
      return true;
    }

    List<T> getClasses() {
      return classes;
    }
  }

  abstract List<DexProgramClass> programClasses();

  public List<DexProgramClass> classes() {
    ReorderBox<DexProgramClass> box = new ReorderBox<>(programClasses());
    assert box.reorderClasses();
    return box.getClasses();
  }

  public Iterable<DexProgramClass> classesWithDeterministicOrder() {
    List<DexProgramClass> classes = new ArrayList<>(programClasses());
    // We never actually sort by anything but the DexType, this is just here in case we ever change
    // that.
    if (options.testing.deterministicSortingBasedOnDexType) {
      // To keep the order deterministic, we sort the classes by their type, which is a unique key.
      classes.sort((a, b) -> a.type.slowCompareTo(b.type));
    }
    return classes;
  }

  public abstract DexClass definitionFor(DexType type);

  public abstract DexProgramClass programDefinitionFor(DexType type);

  @Override
  public abstract String toString();

  public ClassNameMapper getProguardMap() {
    return proguardMap;
  }

  public abstract static class Builder<T extends Builder<T>> {
    // We handle program class collection separately from classpath
    // and library class collections. Since while we assume program
    // class collection should always be fully loaded and thus fully
    // represented by the map (making it easy, for example, adding
    // new or removing existing classes), classpath and library
    // collections will be considered monolithic collections.

    final List<DexProgramClass> programClasses = new ArrayList<>();

    final List<DataResourceProvider> dataResourceProviders = new ArrayList<>();

    public final InternalOptions options;
    public final DexItemFactory dexItemFactory;
    ClassNameMapper proguardMap;
    final Timing timing;

    DexString highestSortingString;
    final Set<DexType> mainDexList = Sets.newIdentityHashSet();
    private final Collection<DexProgramClass> synthesizedClasses;

    public Builder(InternalOptions options, Timing timing) {
      this.options = options;
      this.dexItemFactory = options.itemFactory;
      this.timing = timing;
      this.synthesizedClasses = new ArrayList<>();
    }

    abstract T self();

    public Builder(DexApplication application) {
      programClasses.addAll(application.programClasses());
      dataResourceProviders.addAll(application.dataResourceProviders);
      proguardMap = application.getProguardMap();
      timing = application.timing;
      highestSortingString = application.highestSortingString;
      options = application.options;
      dexItemFactory = application.dexItemFactory;
      mainDexList.addAll(application.mainDexList);
      synthesizedClasses = new ArrayList<>();
    }

    public synchronized T setProguardMap(ClassNameMapper proguardMap) {
      assert this.proguardMap == null;
      this.proguardMap = proguardMap;
      return self();
    }

    public synchronized T replaceProgramClasses(List<DexProgramClass> newProgramClasses) {
      assert newProgramClasses != null;
      this.programClasses.clear();
      this.programClasses.addAll(newProgramClasses);
      return self();
    }

    public synchronized T addDataResourceProvider(DataResourceProvider provider) {
      dataResourceProviders.add(provider);
      return self();
    }

    public synchronized T setHighestSortingString(DexString value) {
      highestSortingString = value;
      return self();
    }

    public synchronized T addProgramClass(DexProgramClass clazz) {
      programClasses.add(clazz);
      return self();
    }

    public synchronized T addSynthesizedClass(
        DexProgramClass synthesizedClass, boolean addToMainDexList) {
      assert synthesizedClass.isProgramClass() : "All synthesized classes must be program classes";
      addProgramClass(synthesizedClass);
      synthesizedClasses.add(synthesizedClass);
      if (addToMainDexList && !mainDexList.isEmpty()) {
        mainDexList.add(synthesizedClass.type);
      }
      return self();
    }

    public Collection<DexProgramClass> getProgramClasses() {
      return programClasses;
    }

    public Collection<DexProgramClass> getSynthesizedClasses() {
      return synthesizedClasses;
    }

    public Set<DexType> getMainDexList() {
      return mainDexList;
    }

    public Builder<T> addToMainDexList(Collection<DexType> mainDexList) {
      this.mainDexList.addAll(mainDexList);
      return this;
    }

    public abstract DexApplication build();
  }

  public static LazyLoadedDexApplication.Builder builder(InternalOptions options, Timing timing) {
    return builder(
        options, timing, ProgramClassCollection.defaultConflictResolver(options.reporter));
  }

  public static LazyLoadedDexApplication.Builder builder(
      InternalOptions options, Timing timing, ProgramClassConflictResolver resolver) {
    return new LazyLoadedDexApplication.Builder(resolver, options, timing);
  }

  public DirectMappedDexApplication asDirect() {
    throw new Unreachable("Cannot use a LazyDexApplication where a DirectDexApplication is"
        + " expected.");
  }

  public abstract DirectMappedDexApplication toDirect();
}
