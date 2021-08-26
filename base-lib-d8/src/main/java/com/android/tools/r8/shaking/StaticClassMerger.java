// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.VerticalClassMerger.IllegalAccessDetector;
import com.android.tools.r8.utils.FieldSignatureEquivalence;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.MethodJavaSignatureEquivalence;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.android.tools.r8.utils.SingletonEquivalence;
import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This optimization merges all classes that only have static members and private virtual methods.
 *
 * <p>If a merge candidate does not access any package-private or protected members, then it is
 * merged into a global representative. Otherwise it is merged into a representative for its
 * package. If no such representatives exist, then the merge candidate is promoted to be the
 * representative.
 *
 * <p>Note that, when merging a merge candidate X into Y, this optimization merely moves the members
 * of X into Y -- it does not change all occurrences of X in the program into Y. This makes the
 * optimization more applicable, because it would otherwise not be possible to merge two classes if
 * they inherited from, say, X' and Y' (since multiple inheritance is not allowed).
 *
 * <p>When there is a main dex specification, merging will only happen within the three groups of
 * classes found from main dex tracing ("main dex roots", "main dex dependencies" and
 * "non main dex classes"), and not between them. This ensures that the size of the main dex
 * will not grow out of proportions due to non main dex classes getting merged into main dex
 * classes.
 */
public class StaticClassMerger {

  enum MergeGroup {
    MAIN_DEX_ROOTS,
    MAIN_DEX_DEPENDENCIES,
    NOT_MAIN_DEX,
    DONT_MERGE;

    private static final String GLOBAL = "<global>";
    private static Key mainDexRootsGlobalKey = new Key(MergeGroup.MAIN_DEX_ROOTS, GLOBAL);
    private static Key mainDexDependenciesGlobalKey =
        new Key(MergeGroup.MAIN_DEX_DEPENDENCIES, GLOBAL);
    private static Key notMainDexGlobalKey = new Key(MergeGroup.NOT_MAIN_DEX, GLOBAL);

    private static class Key {
      private final MergeGroup mergeGroup;
      private final String packageOrGlobal;

      public Key(MergeGroup mergeGroup, String packageOrGlobal) {
        this.mergeGroup = mergeGroup;
        this.packageOrGlobal = packageOrGlobal;
      }

      public MergeGroup getMergeGroup() {
        return mergeGroup;
      }

      public String getPackageOrGlobal() {
        return packageOrGlobal;
      }

      public boolean isGlobal() {
        return packageOrGlobal.equals(GLOBAL);
      }

      @Override
      public int hashCode() {
        return mergeGroup.ordinal() * 13 + packageOrGlobal.hashCode();
      }

      @Override
      public boolean equals(Object other) {
        if (other == this) {
          return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
          return false;
        }
        Key o = (Key) other;
        return o.mergeGroup == mergeGroup && o.packageOrGlobal.equals(packageOrGlobal);
      }
    }

    public Key globalKey() {
      switch (this) {
        case NOT_MAIN_DEX:
          return notMainDexGlobalKey;
        case MAIN_DEX_ROOTS:
          return mainDexRootsGlobalKey;
        case MAIN_DEX_DEPENDENCIES:
          return mainDexDependenciesGlobalKey;
        default:
          throw new Unreachable("Unexpected MergeGroup value");
      }
    }

    public Key key(String pkg) {
      assert this != DONT_MERGE;
      return new Key(this, pkg);
    }

    @Override
    public String toString() {
      switch (this) {
        case NOT_MAIN_DEX:
          return "outside main dex";
        case MAIN_DEX_ROOTS:
          return "main dex roots";
        case MAIN_DEX_DEPENDENCIES:
          return "main dex dependencies";
        default:
          assert this == DONT_MERGE;
          return "don't merge";
      }
    }
  }

  // There are 52 characters in [a-zA-Z], so with a capacity just below 52 the minifier should be
  // able to find single-character names for all members, but around 30 appears to work better in
  // practice.
  private static final int HEURISTIC_FOR_CAPACITY_OF_REPRESENTATIVES = 30;

  private class Representative {

    private final DexProgramClass clazz;

    // Put all members of this class into buckets, where each bucket represent members that have a
    // conflicting signature, and therefore must be given distinct names.
    private final HashMultiset<Wrapper<DexField>> fieldBuckets = HashMultiset.create();
    private final HashMultiset<Wrapper<DexMethod>> methodBuckets = HashMultiset.create();

    private boolean hasSynchronizedMethods = false;

    public Representative(DexProgramClass clazz) {
      this.clazz = clazz;
      include(clazz);
    }

    // Includes the members of `clazz` in `fieldBuckets` and `methodBuckets`.
    public void include(DexProgramClass clazz) {
      for (DexEncodedField field : clazz.fields()) {
        Wrapper<DexField> wrapper = fieldEquivalence.wrap(field.field);
        fieldBuckets.add(wrapper);
      }
      boolean classHasSynchronizedMethods = false;
      for (DexEncodedMethod method : clazz.methods()) {
        assert !hasSynchronizedMethods || !method.accessFlags.isSynchronized();
        classHasSynchronizedMethods |= method.accessFlags.isSynchronized();
        Wrapper<DexMethod> wrapper = methodEquivalence.wrap(method.method);
        methodBuckets.add(wrapper);
      }
      hasSynchronizedMethods |= classHasSynchronizedMethods;
    }

    // Returns true if this representative should no longer be used. The current heuristic is to
    // stop using a representative when the number of members with the same signature (ignoring the
    // name) exceeds a given threshold. This way it is unlikely that we will not be able to find a
    // single-character name for all members.
    public boolean isFull() {
      int numberOfNamesNeeded = 1;
      for (Entry<Wrapper<DexField>> entry : fieldBuckets.entrySet()) {
        numberOfNamesNeeded = Math.max(entry.getCount(), numberOfNamesNeeded);
      }
      for (Entry<Wrapper<DexMethod>> entry : methodBuckets.entrySet()) {
        numberOfNamesNeeded = Math.max(entry.getCount(), numberOfNamesNeeded);
      }
      return numberOfNamesNeeded > HEURISTIC_FOR_CAPACITY_OF_REPRESENTATIVES;
    }
  }

  private final AppView<AppInfoWithLiveness> appView;
  private final MainDexClasses mainDexClasses;

  /** The equivalence that should be used for the member buckets in {@link Representative}. */
  private final Equivalence<DexField> fieldEquivalence;
  private final Equivalence<DexMethod> methodEquivalence;

  private final Map<MergeGroup.Key, Representative> representatives = new HashMap<>();

  private final BiMap<DexField, DexField> fieldMapping = HashBiMap.create();
  private final BiMap<DexMethod, DexMethod> methodMapping = HashBiMap.create();

  private int numberOfMergedClasses = 0;

  public StaticClassMerger(
      AppView<AppInfoWithLiveness> appView,
      InternalOptions options,
      MainDexClasses mainDexClasses) {
    this.appView = appView;
    if (options.getProguardConfiguration().isOverloadAggressively()) {
      fieldEquivalence = FieldSignatureEquivalence.getEquivalenceIgnoreName();
      methodEquivalence = MethodSignatureEquivalence.getEquivalenceIgnoreName();
    } else {
      fieldEquivalence = new SingletonEquivalence<>();
      methodEquivalence = MethodJavaSignatureEquivalence.getEquivalenceIgnoreName();
    }
    this.mainDexClasses = mainDexClasses;
  }

  public GraphLense run() {
    for (DexProgramClass clazz : appView.appInfo().app().classesWithDeterministicOrder()) {
      MergeGroup group = satisfiesMergeCriteria(clazz);
      if (group != MergeGroup.DONT_MERGE) {
        merge(clazz, group);
      }
    }
    if (Log.ENABLED) {
      Log.info(
          getClass(),
          "Merged %s classes with %s members.",
          numberOfMergedClasses,
          fieldMapping.size() + methodMapping.size());
    }
    return buildGraphLense();
  }

  private GraphLense buildGraphLense() {
    if (!fieldMapping.isEmpty() || !methodMapping.isEmpty()) {
      BiMap<DexField, DexField> originalFieldSignatures = fieldMapping.inverse();
      BiMap<DexMethod, DexMethod> originalMethodSignatures = methodMapping.inverse();
      return new NestedGraphLense(
          ImmutableMap.of(),
          methodMapping,
          fieldMapping,
          originalFieldSignatures,
          originalMethodSignatures,
          appView.graphLense(),
          appView.dexItemFactory());
    }
    return appView.graphLense();
  }

  private MergeGroup satisfiesMergeCriteria(DexProgramClass clazz) {
    if (appView.appInfo().neverMerge.contains(clazz.type)) {
      return MergeGroup.DONT_MERGE;
    }
    if (appView.options().featureSplitConfiguration != null &&
        appView.options().featureSplitConfiguration.isInFeature(clazz)) {
      // TODO(b/141452765): Allow class merging between classes in features.
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.staticFields().size() + clazz.directMethods().size() + clazz.virtualMethods().size()
        == 0) {
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.instanceFields().size() > 0) {
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.staticFields().stream()
        .anyMatch(field -> appView.appInfo().isPinned(field.field))) {
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.directMethods().stream().anyMatch(DexEncodedMethod::isInitializer)) {
      return MergeGroup.DONT_MERGE;
    }
    if (!clazz.virtualMethods().stream().allMatch(DexEncodedMethod::isPrivateMethod)) {
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.isInANest()) {
      // Breaks nest access control, abort merging.
      return MergeGroup.DONT_MERGE;
    }
    if (Streams.stream(clazz.methods())
        .anyMatch(
            method ->
                method.accessFlags.isNative()
                    || appView.appInfo().isPinned(method.method)
                    // TODO(christofferqa): Remove the invariant that the graph lense should not
                    // modify any methods from the sets alwaysInline and noSideEffects.
                    || appView.appInfo().alwaysInline.contains(method.method)
                    || appView.appInfo().noSideEffects.keySet().contains(method.method))) {
      return MergeGroup.DONT_MERGE;
    }
    if (clazz.classInitializationMayHaveSideEffects(appView)) {
      // This could have a negative impact on inlining.
      //
      // See {@link com.android.tools.r8.ir.optimize.DefaultInliningOracle#canInlineStaticInvoke}
      // for further details.
      //
      // Note that this will be true for all classes that inherit from or implement a library class.
      return MergeGroup.DONT_MERGE;
    }
    if (!mainDexClasses.isEmpty()) {
      if (mainDexClasses.getRoots().contains(clazz.type)) {
        return MergeGroup.MAIN_DEX_ROOTS;
      }
      if (mainDexClasses.getDependencies().contains(clazz.type)) {
        return MergeGroup.MAIN_DEX_DEPENDENCIES;
      }
    }
    return MergeGroup.NOT_MAIN_DEX;
  }

  private boolean isValidRepresentative(DexProgramClass clazz) {
    // Disallow interfaces from being representatives, since interface methods require desugaring.
    return !clazz.isInterface();
  }

  private boolean merge(DexProgramClass clazz, MergeGroup group) {
    assert satisfiesMergeCriteria(clazz) == group;
    assert group != MergeGroup.DONT_MERGE;

    return merge(
        clazz,
        mayMergeAcrossPackageBoundaries(clazz)
            ? group.globalKey()
            : group.key(clazz.type.getPackageDescriptor()));
  }

  private boolean merge(DexProgramClass clazz, MergeGroup.Key key) {
    Representative representative = representatives.get(key);
    if (representative != null) {
      if (representative.hasSynchronizedMethods && clazz.hasStaticSynchronizedMethods()) {
        // We are not allowed to merge synchronized classes with synchronized methods.
        return false;
      }
      if (appView.appInfo().constClassReferences.contains(clazz.type)) {
        // Since the type is const-class referenced (and the static merger does not create a lense
        // to map the merged type) the class will likely remain and there is no gain from merging.
        return false;
      }
      // Check if current candidate is a better choice depending on visibility. For package private
      // or protected, the key is parameterized by the package name already, so we just have to
      // check accessibility-flags. For global this is no-op.
      if (isValidRepresentative(clazz)
          && !representative.clazz.accessFlags.isAtLeastAsVisibleAs(clazz.accessFlags)) {
        assert clazz.type.getPackageDescriptor().equals(key.packageOrGlobal);
        assert representative.clazz.type.getPackageDescriptor().equals(key.packageOrGlobal);
        Representative newRepresentative = getOrCreateRepresentative(key, clazz);
        newRepresentative.include(representative.clazz);
        if (!newRepresentative.isFull()) {
          setRepresentative(key, newRepresentative);
          moveMembersFromSourceToTarget(representative.clazz, clazz);
          return true;
        }
      } else {
        representative.include(clazz);
        if (!representative.isFull()) {
          moveMembersFromSourceToTarget(clazz, representative.clazz);
          return true;
        }
      }
    }
    if (isValidRepresentative(clazz)) {
      setRepresentative(key, getOrCreateRepresentative(key, clazz));
    }
    return false;
  }

  private Representative getOrCreateRepresentative(MergeGroup.Key key, DexProgramClass clazz) {
    Representative globalRepresentative = representatives.get(key.getMergeGroup().globalKey());
    if (globalRepresentative != null && globalRepresentative.clazz == clazz) {
      return globalRepresentative;
    }
    Representative packageRepresentative = representatives.get(key);
    if (packageRepresentative != null && packageRepresentative.clazz == clazz) {
      return packageRepresentative;
    }
    return new Representative(clazz);
  }

  private void setRepresentative(MergeGroup.Key key, Representative representative) {
    assert isValidRepresentative(representative.clazz);
    if (Log.ENABLED) {
      if (key.isGlobal()) {
        Log.info(
            getClass(),
            "Making %s the global representative in group %s",
            representative.clazz.type.toSourceString(),
            key.getMergeGroup().toString());
      } else {
        Log.info(
            getClass(),
            "Making %s the representative for package %s in group %s",
            representative.clazz.type.toSourceString(),
            key.getPackageOrGlobal(),
            key.getMergeGroup().toString());
      }
    }
    representatives.put(key, representative);
  }

  private void clearRepresentative(MergeGroup.Key key) {
    if (Log.ENABLED) {
      if (key.isGlobal()) {
        Log.info(getClass(), "Removing the global representative");
      } else {
        Log.info(
            getClass(), "Removing the representative for package %s", key.getPackageOrGlobal());
      }
    }
    representatives.remove(key);
  }

  private boolean mayMergeAcrossPackageBoundaries(DexProgramClass clazz) {
    // Check that the class is public. Otherwise, accesses to `clazz` from within its current
    // package may become illegal.
    if (!clazz.accessFlags.isPublic()) {
      return false;
    }
    // Check that all of the members are private or public.
    if (!clazz.directMethods().stream()
        .allMatch(method -> method.accessFlags.isPrivate() || method.accessFlags.isPublic())) {
      return false;
    }
    if (!clazz.staticFields().stream()
        .allMatch(field -> field.accessFlags.isPrivate() || field.accessFlags.isPublic())) {
      return false;
    }

    // Note that a class is only considered a candidate if it has no instance fields and all of its
    // virtual methods are private. Therefore, we don't need to consider check if there are any
    // package-private or protected instance fields or virtual methods here.
    assert clazz.instanceFields().size() == 0;
    assert clazz.virtualMethods().stream().allMatch(method -> method.accessFlags.isPrivate());

    // Check that no methods access package-private or protected members.
    IllegalAccessDetector registry = new IllegalAccessDetector(appView, clazz);
    for (DexEncodedMethod method : clazz.methods()) {
      registry.setContext(method);
      method.registerCodeReferences(registry);
      if (registry.foundIllegalAccess()) {
        return false;
      }
    }
    return true;
  }

  private void moveMembersFromSourceToTarget(
      DexProgramClass sourceClass, DexProgramClass targetClass) {
    if (Log.ENABLED) {
      Log.info(
          getClass(),
          "Merging %s into %s",
          sourceClass.type.toSourceString(),
          targetClass.type.toSourceString());
    }

    // TODO(b/136457753) This check is a bit weird for protected, since it is moving access.
    assert targetClass.accessFlags.isAtLeastAsVisibleAs(sourceClass.accessFlags);
    assert sourceClass.instanceFields().size() == 0;
    assert targetClass.instanceFields().size() == 0;

    numberOfMergedClasses++;

    // Move members from source to target.
    targetClass.appendDirectMethods(
        mergeMethods(sourceClass.directMethods(), targetClass.directMethods(), targetClass));
    targetClass.appendVirtualMethods(
        mergeMethods(sourceClass.virtualMethods(), targetClass.virtualMethods(), targetClass));
    targetClass.setStaticFields(
        mergeFields(sourceClass.staticFields(), targetClass.staticFields(), targetClass));

    // Cleanup source.
    sourceClass.setDirectMethods(DexEncodedMethod.EMPTY_ARRAY);
    sourceClass.setVirtualMethods(DexEncodedMethod.EMPTY_ARRAY);
    sourceClass.setStaticFields(DexEncodedField.EMPTY_ARRAY);
  }

  private List<DexEncodedMethod> mergeMethods(
      List<DexEncodedMethod> sourceMethods,
      List<DexEncodedMethod> targetMethods,
      DexProgramClass targetClass) {
    // Move source methods to result one by one, renaming them if needed.
    MethodSignatureEquivalence equivalence = MethodSignatureEquivalence.get();
    Set<Wrapper<DexMethod>> existingMethods =
        targetMethods.stream()
            .map(targetMethod -> equivalence.wrap(targetMethod.method))
            .collect(Collectors.toSet());

    Predicate<DexMethod> availableMethodSignatures =
        method -> !existingMethods.contains(equivalence.wrap(method));

    List<DexEncodedMethod> newMethods = new ArrayList<>(sourceMethods.size());
    for (DexEncodedMethod sourceMethod : sourceMethods) {
      DexEncodedMethod sourceMethodAfterMove =
          renameMethodIfNeeded(sourceMethod, targetClass, availableMethodSignatures);
      newMethods.add(sourceMethodAfterMove);

      DexMethod originalMethod =
          methodMapping.inverse().getOrDefault(sourceMethod.method, sourceMethod.method);
      methodMapping.forcePut(originalMethod, sourceMethodAfterMove.method);

      existingMethods.add(equivalence.wrap(sourceMethodAfterMove.method));
    }
    return newMethods;
  }

  private DexEncodedField[] mergeFields(
      List<DexEncodedField> sourceFields,
      List<DexEncodedField> targetFields,
      DexProgramClass targetClass) {
    DexEncodedField[] result = new DexEncodedField[sourceFields.size() + targetFields.size()];

    // Move all target fields to result.
    int index = 0;
    for (DexEncodedField targetField : targetFields) {
      result[index++] = targetField;
    }

    // Move source fields to result one by one, renaming them if needed.
    FieldSignatureEquivalence equivalence = FieldSignatureEquivalence.get();
    Set<Wrapper<DexField>> existingFields =
        targetFields.stream()
            .map(targetField -> equivalence.wrap(targetField.field))
            .collect(Collectors.toSet());

    Predicate<DexField> availableFieldSignatures =
        field -> !existingFields.contains(equivalence.wrap(field));

    for (DexEncodedField sourceField : sourceFields) {
      DexEncodedField sourceFieldAfterMove =
          renameFieldIfNeeded(sourceField, targetClass, availableFieldSignatures);
      result[index++] = sourceFieldAfterMove;

      DexField originalField =
          fieldMapping.inverse().getOrDefault(sourceField.field, sourceField.field);
      fieldMapping.forcePut(originalField, sourceFieldAfterMove.field);

      existingFields.add(equivalence.wrap(sourceFieldAfterMove.field));
    }

    assert index == result.length;
    return result;
  }

  private DexEncodedMethod renameMethodIfNeeded(
      DexEncodedMethod method,
      DexProgramClass targetClass,
      Predicate<DexMethod> availableMethodSignatures) {
    assert !method.accessFlags.isConstructor();
    DexString oldName = method.method.name;
    DexMethod newSignature =
        appView.dexItemFactory().createMethod(targetClass.type, method.method.proto, oldName);
    if (!availableMethodSignatures.test(newSignature)) {
      int count = 1;
      do {
        DexString newName = appView.dexItemFactory().createString(oldName.toSourceString() + count);
        newSignature =
            appView.dexItemFactory().createMethod(targetClass.type, method.method.proto, newName);
        count++;
      } while (!availableMethodSignatures.test(newSignature));
    }
    return method.toTypeSubstitutedMethod(newSignature);
  }

  private DexEncodedField renameFieldIfNeeded(
      DexEncodedField field,
      DexProgramClass targetClass,
      Predicate<DexField> availableFieldSignatures) {
    DexString oldName = field.field.name;
    DexField newSignature =
        appView.dexItemFactory().createField(targetClass.type, field.field.type, oldName);
    if (!availableFieldSignatures.test(newSignature)) {
      int count = 1;
      do {
        DexString newName = appView.dexItemFactory().createString(oldName.toSourceString() + count);
        newSignature =
            appView.dexItemFactory().createField(targetClass.type, field.field.type, newName);
        count++;
      } while (!availableFieldSignatures.test(newSignature));
    }
    return field.toTypeSubstitutedField(newSignature);
  }
}
