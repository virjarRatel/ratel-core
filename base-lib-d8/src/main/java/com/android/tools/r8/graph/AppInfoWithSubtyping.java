// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import static com.android.tools.r8.ir.desugar.LambdaRewriter.LAMBDA_GROUP_CLASS_NAME_PREFIX;

import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.desugar.LambdaDescriptor;
import com.android.tools.r8.utils.SetUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class AppInfoWithSubtyping extends AppInfo implements ClassHierarchy {

  private static final int ROOT_LEVEL = 0;
  private static final int UNKNOWN_LEVEL = -1;
  private static final int INTERFACE_LEVEL = -2;
  // Since most Java types has no sub-types, we can just share an empty immutable set until we need
  // to add to it.
  private static final Set<DexType> NO_DIRECT_SUBTYPE = ImmutableSet.of();

  private static class TypeInfo {

    private final DexType type;

    int hierarchyLevel = UNKNOWN_LEVEL;
    /**
     * Set of direct subtypes. This set has to remain sorted to ensure determinism. The actual
     * sorting is not important but {@link DexType#slowCompareTo(DexType)} works well.
     */
    Set<DexType> directSubtypes = NO_DIRECT_SUBTYPE;

    // Caching what interfaces this type is implementing. This includes super-interface hierarchy.
    Set<DexType> implementedInterfaces = null;

    TypeInfo(DexType type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "TypeInfo{" + type + ", level:" + hierarchyLevel + "}";
    }

    private void ensureDirectSubTypeSet() {
      if (directSubtypes == NO_DIRECT_SUBTYPE) {
        directSubtypes = new TreeSet<>(DexType::slowCompareTo);
      }
    }

    private void setLevel(int level) {
      if (level == hierarchyLevel) {
        return;
      }
      if (hierarchyLevel == INTERFACE_LEVEL) {
        assert level == ROOT_LEVEL + 1;
      } else if (level == INTERFACE_LEVEL) {
        assert hierarchyLevel == ROOT_LEVEL + 1 || hierarchyLevel == UNKNOWN_LEVEL;
        hierarchyLevel = INTERFACE_LEVEL;
      } else {
        assert hierarchyLevel == UNKNOWN_LEVEL;
        hierarchyLevel = level;
      }
    }

    synchronized void addDirectSubtype(TypeInfo subtypeInfo) {
      assert hierarchyLevel != UNKNOWN_LEVEL;
      ensureDirectSubTypeSet();
      directSubtypes.add(subtypeInfo.type);
      subtypeInfo.setLevel(hierarchyLevel + 1);
    }

    void tagAsSubtypeRoot() {
      setLevel(ROOT_LEVEL);
    }

    void tagAsInterface() {
      setLevel(INTERFACE_LEVEL);
    }

    public boolean isInterface() {
      assert hierarchyLevel != UNKNOWN_LEVEL : "Program class missing: " + this;
      assert type.isClassType();
      return hierarchyLevel == INTERFACE_LEVEL;
    }

    public boolean isUnknown() {
      return hierarchyLevel == UNKNOWN_LEVEL;
    }

    synchronized void addInterfaceSubtype(DexType type) {
      // Interfaces all inherit from java.lang.Object. However, we assign a special level to
      // identify them later on.
      setLevel(INTERFACE_LEVEL);
      ensureDirectSubTypeSet();
      directSubtypes.add(type);
    }
  }

  // Set of missing classes, discovered during subtypeMap computation.
  private final Set<DexType> missingClasses = Sets.newIdentityHashSet();

  // Map from types to their subtypes.
  private final Map<DexType, ImmutableSet<DexType>> subtypeMap = new IdentityHashMap<>();

  // Map from synthesized classes to their supertypes.
  private final Map<DexType, ImmutableSet<DexType>> supertypesForSynthesizedClasses =
      new ConcurrentHashMap<>();

  // Map from types to their subtyping information.
  private final Map<DexType, TypeInfo> typeInfo;

  // Caches which static types that may store an object that has a non-default finalize() method.
  // E.g., `java.lang.Object -> TRUE` if there is a subtype of Object that overrides finalize().
  private final Map<DexType, Boolean> mayHaveFinalizeMethodDirectlyOrIndirectlyCache =
      new ConcurrentHashMap<>();

  public AppInfoWithSubtyping(DexApplication application) {
    super(application);
    typeInfo = new ConcurrentHashMap<>();
    // Recompute subtype map if we have modified the graph.
    populateSubtypeMap(application.asDirect(), application.dexItemFactory);
  }

  protected AppInfoWithSubtyping(AppInfoWithSubtyping previous) {
    super(previous);
    missingClasses.addAll(previous.missingClasses);
    subtypeMap.putAll(previous.subtypeMap);
    supertypesForSynthesizedClasses.putAll(previous.supertypesForSynthesizedClasses);
    typeInfo = new ConcurrentHashMap<>(previous.typeInfo);
    assert app() instanceof DirectMappedDexApplication;
  }

  @Override
  public void addSynthesizedClass(DexProgramClass synthesizedClass) {
    super.addSynthesizedClass(synthesizedClass);
    // Register synthesized type, which has two side effects:
    //   1) Set the hierarchy level of synthesized type based on that of its super type,
    //   2) Register the synthesized type as a subtype of the supertype.
    //
    // The first one makes method resolutions on that synthesized class free from assertion errors
    // about unknown hierarchy level.
    //
    // For the second one, note that such addition is synchronized, but the retrieval of direct
    // subtypes isn't. Thus, there is a chance of race conditions: utils that check/iterate direct
    // subtypes, e.g., allImmediateSubtypes, hasSubTypes, etc., may not be able to see this new
    // synthesized class. However, in practice, this would be okay because, in most cases,
    // synthesized class's super type is Object, which in general has other subtypes in any way.
    // Also, iterating all subtypes of Object usually happens before/after IR processing, i.e., as
    // part of structural changes, such as bottom-up traversal to collect all method signatures,
    // which are free from such race conditions. Another exceptional case is synthesized classes
    // whose synthesis is isolated from IR processing. For example, lambda group class that merges
    // lambdas with the same interface are synthesized/finalized even after post processing of IRs.
    assert synthesizedClass.superType == dexItemFactory().objectType
            || synthesizedClass.type.toString().contains(LAMBDA_GROUP_CLASS_NAME_PREFIX)
        : "Make sure retrieval and iteration of sub types of `" + synthesizedClass.superType
            + "` is guaranteed to be thread safe and able to see `" + synthesizedClass + "`";
    registerNewType(synthesizedClass.type, synthesizedClass.superType);

    // TODO(b/129458850): Remove when we no longer synthesize classes on-the-fly.
    Set<DexType> visited = SetUtils.newIdentityHashSet(synthesizedClass.allImmediateSupertypes());
    Deque<DexType> worklist = new ArrayDeque<>(visited);
    while (!worklist.isEmpty()) {
      DexType type = worklist.removeFirst();
      assert visited.contains(type);

      DexClass clazz = definitionFor(type);
      if (clazz == null) {
        continue;
      }

      for (DexType supertype : clazz.allImmediateSupertypes()) {
        if (visited.add(supertype)) {
          worklist.addLast(supertype);
        }
      }
    }
    if (!visited.isEmpty()) {
      supertypesForSynthesizedClasses.put(synthesizedClass.type, ImmutableSet.copyOf(visited));
    }
  }

  private boolean isSynthesizedClassStrictSubtypeOf(DexType synthesizedClass, DexType supertype) {
    Set<DexType> supertypesOfSynthesizedClass =
        supertypesForSynthesizedClasses.get(synthesizedClass);
    return supertypesOfSynthesizedClass != null && supertypesOfSynthesizedClass.contains(supertype);
  }

  private DirectMappedDexApplication getDirectApplication() {
    // TODO(herhut): Remove need for cast.
    return (DirectMappedDexApplication) app();
  }

  public Iterable<DexLibraryClass> libraryClasses() {
    assert checkIfObsolete();
    return getDirectApplication().libraryClasses();
  }

  public Set<DexType> getMissingClasses() {
    assert checkIfObsolete();
    return Collections.unmodifiableSet(missingClasses);
  }

  public Set<DexType> subtypes(DexType type) {
    assert checkIfObsolete();
    assert type.isClassType();
    ImmutableSet<DexType> subtypes = subtypeMap.get(type);
    return subtypes == null ? ImmutableSet.of() : subtypes;
  }

  private void populateSuperType(Map<DexType, Set<DexType>> map, DexType superType,
      DexClass baseClass, Function<DexType, DexClass> definitions) {
    if (superType != null) {
      Set<DexType> set = map.computeIfAbsent(superType, ignore -> new HashSet<>());
      if (set.add(baseClass.type)) {
        // Only continue recursion if type has been added to set.
        populateAllSuperTypes(map, superType, baseClass, definitions);
      }
    }
  }

  private TypeInfo getTypeInfo(DexType type) {
    assert type != null;
    return typeInfo.computeIfAbsent(type, TypeInfo::new);
  }

  private void populateAllSuperTypes(
      Map<DexType, Set<DexType>> map,
      DexType holder,
      DexClass baseClass,
      Function<DexType, DexClass> definitions) {
    DexClass holderClass = definitions.apply(holder);
    // Skip if no corresponding class is found.
    if (holderClass != null) {
      populateSuperType(map, holderClass.superType, baseClass, definitions);
      if (holderClass.superType != null) {
        getTypeInfo(holderClass.superType).addDirectSubtype(getTypeInfo(holder));
      } else {
        // We found java.lang.Object
        assert dexItemFactory().objectType == holder;
      }
      for (DexType inter : holderClass.interfaces.values) {
        populateSuperType(map, inter, baseClass, definitions);
        getTypeInfo(inter).addInterfaceSubtype(holder);
      }
      if (holderClass.isInterface()) {
        getTypeInfo(holder).tagAsInterface();
      }
    } else {
      if (baseClass.isProgramClass() || baseClass.isClasspathClass()) {
        missingClasses.add(holder);
      }
      // The subtype chain is broken, at least make this type a subtype of Object.
      if (holder != dexItemFactory().objectType) {
        getTypeInfo(dexItemFactory().objectType).addDirectSubtype(getTypeInfo(holder));
      }
    }
  }

  private void populateSubtypeMap(DirectMappedDexApplication app, DexItemFactory dexItemFactory) {
    getTypeInfo(dexItemFactory.objectType).tagAsSubtypeRoot();
    Map<DexType, Set<DexType>> map = new IdentityHashMap<>();
    for (DexClass clazz : app.allClasses()) {
      populateAllSuperTypes(map, clazz.type, clazz, app::definitionFor);
    }
    for (Map.Entry<DexType, Set<DexType>> entry : map.entrySet()) {
      subtypeMap.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
    }
    assert validateLevelsAreCorrect(app::definitionFor, dexItemFactory);
  }

  private boolean validateLevelsAreCorrect(
      Function<DexType, DexClass> definitions, DexItemFactory dexItemFactory) {
    Set<DexType> seenTypes = Sets.newIdentityHashSet();
    Deque<DexType> worklist = new ArrayDeque<>();
    DexType objectType = dexItemFactory.objectType;
    worklist.add(objectType);
    while (!worklist.isEmpty()) {
      DexType next = worklist.pop();
      DexClass nextHolder = definitions.apply(next);
      DexType superType;
      if (nextHolder == null) {
        // We might lack the definition of Object, so guard against that.
        superType = next == dexItemFactory.objectType ? null : dexItemFactory.objectType;
      } else {
        superType = nextHolder.superType;
      }
      assert !seenTypes.contains(next);
      seenTypes.add(next);
      TypeInfo nextInfo = getTypeInfo(next);
      if (superType == null) {
        assert nextInfo.hierarchyLevel == ROOT_LEVEL;
      } else {
        TypeInfo superInfo = getTypeInfo(superType);
        assert superInfo.hierarchyLevel == nextInfo.hierarchyLevel - 1
            || (superInfo.hierarchyLevel == ROOT_LEVEL
                && nextInfo.hierarchyLevel == INTERFACE_LEVEL);
        assert superInfo.directSubtypes.contains(next);
      }
      if (nextInfo.hierarchyLevel != INTERFACE_LEVEL) {
        // Only traverse the class hierarchy subtypes, not interfaces.
        worklist.addAll(nextInfo.directSubtypes);
      } else if (nextHolder != null) {
        // Test that the interfaces of this class are interfaces and have this class as subtype.
        for (DexType iface : nextHolder.interfaces.values) {
          TypeInfo ifaceInfo = getTypeInfo(iface);
          assert ifaceInfo.directSubtypes.contains(next);
          assert ifaceInfo.hierarchyLevel == INTERFACE_LEVEL;
        }
      }
    }
    return true;
  }

  protected boolean hasAnyInstantiatedLambdas(DexProgramClass clazz) {
    assert checkIfObsolete();
    return true; // Don't know, there might be.
  }

  public boolean methodDefinedInInterfaces(DexEncodedMethod method, DexType implementingClass) {
    DexClass holder = definitionFor(implementingClass);
    if (holder == null) {
      return false;
    }
    for (DexType iface : holder.interfaces.values) {
      if (methodDefinedInInterface(method, iface)) {
        return true;
      }
    }
    return false;
  }

  public boolean methodDefinedInInterface(DexEncodedMethod method, DexType iface) {
    DexClass potentialHolder = definitionFor(iface);
    if (potentialHolder == null) {
      return false;
    }
    assert potentialHolder.isInterface();
    for (DexEncodedMethod virtualMethod : potentialHolder.virtualMethods) {
      if (virtualMethod.method.hasSameProtoAndName(method.method)
          && virtualMethod.accessFlags.isSameVisiblity(method.accessFlags)) {
        return true;
      }
    }
    for (DexType parentInterface : potentialHolder.interfaces.values) {
      if (methodDefinedInInterface(method, parentInterface)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Resolve the methods implemented by the lambda expression that created the {@code callSite}.
   *
   * <p>If {@code callSite} was not created as a result of a lambda expression (i.e. the metafactory
   * is not {@code LambdaMetafactory}), the empty set is returned.
   *
   * <p>If the metafactory is neither {@code LambdaMetafactory} nor {@code StringConcatFactory}, a
   * warning is issued.
   *
   * <p>The returned set of methods all have {@code callSite.methodName} as the method name.
   *
   * @param callSite Call site to resolve.
   * @return Methods implemented by the lambda expression that created the {@code callSite}.
   */
  public Set<DexEncodedMethod> lookupLambdaImplementedMethods(DexCallSite callSite) {
    assert checkIfObsolete();
    List<DexType> callSiteInterfaces = LambdaDescriptor.getInterfaces(callSite, this);
    if (callSiteInterfaces == null || callSiteInterfaces.isEmpty()) {
      return Collections.emptySet();
    }
    Set<DexEncodedMethod> result = new HashSet<>();
    Deque<DexType> worklist = new ArrayDeque<>(callSiteInterfaces);
    Set<DexType> visited = Sets.newIdentityHashSet();
    while (!worklist.isEmpty()) {
      DexType iface = worklist.removeFirst();
      if (getTypeInfo(iface).isUnknown()) {
        // Skip this interface. If the lambda only implements missing library interfaces and not any
        // program interfaces, then minification and tree shaking are not interested in this
        // DexCallSite anyway, so skipping this interface is harmless. On the other hand, if
        // minification is run on a program with a lambda interface that implements both a missing
        // library interface and a present program interface, then we might minify the method name
        // on the program interface even though it should be kept the same as the (missing) library
        // interface method. That is a shame, but minification is not suited for incomplete programs
        // anyway.
        continue;
      }
      if (!visited.add(iface)) {
        // Already visited previously. May happen due to "diamond shapes" in the interface
        // hierarchy.
        continue;
      }
      assert getTypeInfo(iface).isInterface();
      DexClass clazz = definitionFor(iface);
      if (clazz != null) {
        for (DexEncodedMethod method : clazz.virtualMethods()) {
          if (method.method.name == callSite.methodName && method.accessFlags.isAbstract()) {
            result.add(method);
          }
        }
        Collections.addAll(worklist, clazz.interfaces.values);
      }
    }
    return result;
  }

  public boolean isStringConcat(DexMethodHandle bootstrapMethod) {
    assert checkIfObsolete();
    return bootstrapMethod.type.isInvokeStatic()
        && (bootstrapMethod.asMethod() == dexItemFactory().stringConcatWithConstantsMethod
            || bootstrapMethod.asMethod() == dexItemFactory().stringConcatMethod);
  }

  private void registerNewType(DexType newType, DexType superType) {
    assert checkIfObsolete();
    // Register the relationship between this type and its superType.
    getTypeInfo(superType).addDirectSubtype(getTypeInfo(newType));
  }

  @VisibleForTesting
  public void registerNewTypeForTesting(DexType newType, DexType superType) {
    registerNewType(newType, superType);
  }

  @Override
  public boolean hasSubtyping() {
    assert checkIfObsolete();
    return true;
  }

  @Override
  public AppInfoWithSubtyping withSubtyping() {
    assert checkIfObsolete();
    return this;
  }

  public Set<DexType> allImmediateSubtypes(DexType type) {
    return getTypeInfo(type).directSubtypes;
  }

  public boolean isUnknown(DexType type) {
    return getTypeInfo(type).isUnknown();
  }

  public boolean isMarkedAsInterface(DexType type) {
    return getTypeInfo(type).isInterface();
  }

  @Override
  public boolean hasSubtypes(DexType type) {
    return !getTypeInfo(type).directSubtypes.isEmpty();
  }

  @Override
  public boolean isSubtype(DexType subtype, DexType supertype) {
    if (subtype == supertype || isStrictSubtypeOf(subtype, supertype)) {
      return true;
    }
    if (synthesizedClasses.containsKey(subtype)) {
      return isSynthesizedClassStrictSubtypeOf(subtype, supertype);
    }
    return false;
  }

  public boolean isStrictSubtypeOf(DexType subtype, DexType supertype) {
    // For all erroneous cases, saying `no`---not a strict subtype---is conservative.
    if (isStrictSubtypeOf(subtype, supertype, false)) {
      return true;
    }
    if (synthesizedClasses.containsKey(subtype)) {
      return isSynthesizedClassStrictSubtypeOf(subtype, supertype);
    }
    return false;
  }

  // Depending on optimizations, conservative answer of subtype relation may vary.
  // Pass different `orElse` in that case.
  private boolean isStrictSubtypeOf(DexType subtype, DexType supertype, boolean orElse) {
    if (subtype == supertype) {
      return false;
    }
    // Treat the object class special as it is always the supertype, even in the case of broken
    // subtype chains.
    if (subtype == dexItemFactory().objectType) {
      return false;
    }
    if (supertype == dexItemFactory().objectType) {
      return true;
    }
    TypeInfo subInfo = getTypeInfo(subtype);
    if (subInfo.hierarchyLevel == INTERFACE_LEVEL) {
      return isInterfaceSubtypeOf(subtype, supertype);
    }
    TypeInfo superInfo = getTypeInfo(supertype);
    if (superInfo.hierarchyLevel == INTERFACE_LEVEL) {
      return superInfo.directSubtypes.stream()
          .anyMatch(superSubtype -> isSubtype(subtype, superSubtype));
    }
    return isSubtypeOfClass(subInfo, superInfo, orElse);
  }

  private boolean isInterfaceSubtypeOf(DexType candidate, DexType other) {
    if (candidate == other || other == dexItemFactory().objectType) {
      return true;
    }
    DexClass candidateHolder = definitionFor(candidate);
    if (candidateHolder == null) {
      return false;
    }
    for (DexType iface : candidateHolder.interfaces.values) {
      assert getTypeInfo(iface).hierarchyLevel == INTERFACE_LEVEL;
      if (isInterfaceSubtypeOf(iface, other)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSubtypeOfClass(TypeInfo subInfo, TypeInfo superInfo, boolean orElse) {
    if (superInfo.hierarchyLevel == UNKNOWN_LEVEL) {
      // We have no definition for this class, hence it is not part of the hierarchy.
      return orElse;
    }
    while (superInfo.hierarchyLevel < subInfo.hierarchyLevel) {
      DexClass holder = definitionFor(subInfo.type);
      assert holder != null && !holder.isInterface();
      subInfo = getTypeInfo(holder.superType);
    }
    return subInfo.type == superInfo.type;
  }

  /**
   * Apply the given function to all classes that directly extend this class.
   *
   * <p>If this class is an interface, then this method will visit all sub-interfaces. This deviates
   * from the dex-file encoding, where subinterfaces "implement" their super interfaces. However, it
   * is consistent with the source language.
   */
  public void forAllImmediateExtendsSubtypes(DexType type, Consumer<DexType> f) {
    allImmediateExtendsSubtypes(type).forEach(f);
  }

  public Iterable<DexType> allImmediateExtendsSubtypes(DexType type) {
    TypeInfo info = getTypeInfo(type);
    assert info.hierarchyLevel != UNKNOWN_LEVEL;
    if (info.hierarchyLevel == INTERFACE_LEVEL) {
      return Iterables.filter(info.directSubtypes, t -> getTypeInfo(t).isInterface());
    } else if (info.hierarchyLevel == ROOT_LEVEL) {
      // This is the object type. Filter out interfaces
      return Iterables.filter(info.directSubtypes, t -> !getTypeInfo(t).isInterface());
    } else {
      return info.directSubtypes;
    }
  }

  /**
   * Apply the given function to all classes that directly implement this interface.
   *
   * <p>The implementation does not consider how the hierarchy is encoded in the dex file, where
   * interfaces "implement" their super interfaces. Instead it takes the view of the source
   * language, where interfaces "extend" their superinterface.
   */
  public void forAllImmediateImplementsSubtypes(DexType type, Consumer<DexType> f) {
    allImmediateImplementsSubtypes(type).forEach(f);
  }

  public Iterable<DexType> allImmediateImplementsSubtypes(DexType type) {
    TypeInfo info = getTypeInfo(type);
    if (info.hierarchyLevel == INTERFACE_LEVEL) {
      return Iterables.filter(info.directSubtypes, subtype -> !getTypeInfo(subtype).isInterface());
    }
    return ImmutableList.of();
  }

  public boolean isMissingOrHasMissingSuperType(DexType type) {
    DexClass clazz = definitionFor(type);
    return clazz == null || clazz.hasMissingSuperType(this);
  }

  public boolean isExternalizable(DexType type) {
    return implementedInterfaces(type).contains(dexItemFactory().externalizableType);
  }

  public boolean isSerializable(DexType type) {
    return implementedInterfaces(type).contains(dexItemFactory().serializableType);
  }

  /** Collect all interfaces that this type directly or indirectly implements. */
  public Set<DexType> implementedInterfaces(DexType type) {
    TypeInfo info = getTypeInfo(type);
    if (info.implementedInterfaces != null) {
      return info.implementedInterfaces;
    }
    synchronized (this) {
      if (info.implementedInterfaces == null) {
        Set<DexType> interfaces = Sets.newIdentityHashSet();
        implementedInterfaces(type, interfaces);
        info.implementedInterfaces = interfaces;
      }
    }
    return info.implementedInterfaces;
  }

  private void implementedInterfaces(DexType type, Set<DexType> interfaces) {
    DexClass dexClass = definitionFor(type);
    // Loop to traverse the super type hierarchy of the current type.
    while (dexClass != null) {
      if (dexClass.isInterface()) {
        interfaces.add(dexClass.type);
      }
      for (DexType itf : dexClass.interfaces.values) {
        implementedInterfaces(itf, interfaces);
      }
      if (dexClass.superType == null) {
        break;
      }
      dexClass = definitionFor(dexClass.superType);
    }
  }

  public DexType getSingleSubtype(DexType type) {
    TypeInfo info = getTypeInfo(type);
    assert info.hierarchyLevel != UNKNOWN_LEVEL;
    if (info.directSubtypes.size() == 1) {
      return Iterables.getFirst(info.directSubtypes, null);
    } else {
      return null;
    }
  }

  @Override
  public boolean isDirectSubtype(DexType subtype, DexType supertype) {
    TypeInfo info = getTypeInfo(supertype);
    assert info.hierarchyLevel != UNKNOWN_LEVEL;
    return info.directSubtypes.contains(subtype);
  }

  // TODO(b/130636783): inconsistent location
  public DexType computeLeastUpperBoundOfClasses(DexType subtype, DexType other) {
    if (subtype == other) {
      return subtype;
    }
    DexType objectType = dexItemFactory().objectType;
    TypeInfo subInfo = getTypeInfo(subtype);
    TypeInfo superInfo = getTypeInfo(other);
    // If we have no definition for either class, stop proceeding.
    if (subInfo.hierarchyLevel == UNKNOWN_LEVEL || superInfo.hierarchyLevel == UNKNOWN_LEVEL) {
      return objectType;
    }
    if (subtype == objectType || other == objectType) {
      return objectType;
    }
    TypeInfo t1;
    TypeInfo t2;
    if (superInfo.hierarchyLevel < subInfo.hierarchyLevel) {
      t1 = superInfo;
      t2 = subInfo;
    } else {
      t1 = subInfo;
      t2 = superInfo;
    }
    // From now on, t2.hierarchyLevel >= t1.hierarchyLevel
    DexClass dexClass;
    // Make both of other and this in the same level.
    while (t2.hierarchyLevel > t1.hierarchyLevel) {
      dexClass = definitionFor(t2.type);
      if (dexClass == null || dexClass.superType == null) {
        return objectType;
      }
      t2 = getTypeInfo(dexClass.superType);
    }
    // At this point, they are at the same level.
    // lubType starts from t1, and will move up; t2 starts from itself, and will move up, too.
    // They move up in their own hierarchy tree, and will repeat the process until they meet.
    // (It will stop at anytime when either one's definition is not found.)
    DexType lubType = t1.type;
    DexType t2Type = t2.type;
    while (t2Type != lubType) {
      assert getTypeInfo(t2Type).hierarchyLevel == getTypeInfo(lubType).hierarchyLevel;
      dexClass = definitionFor(t2Type);
      if (dexClass == null) {
        lubType = objectType;
        break;
      }
      t2Type = dexClass.superType;
      dexClass = definitionFor(lubType);
      if (dexClass == null) {
        lubType = objectType;
        break;
      }
      lubType = dexClass.superType;
    }
    return lubType;
  }

  public boolean inDifferentHierarchy(DexType type1, DexType type2) {
    return !isSubtype(type1, type2) && !isSubtype(type2, type1);
  }

  public boolean mayHaveFinalizeMethodDirectlyOrIndirectly(ClassTypeLatticeElement type) {
    Set<DexType> interfaces = type.getInterfaces();
    if (!interfaces.isEmpty()) {
      for (DexType interfaceType : interfaces) {
        if (computeMayHaveFinalizeMethodDirectlyOrIndirectlyIfAbsent(interfaceType, false)) {
          return true;
        }
      }
      return false;
    }
    return computeMayHaveFinalizeMethodDirectlyOrIndirectlyIfAbsent(type.getClassType(), true);
  }

  private boolean computeMayHaveFinalizeMethodDirectlyOrIndirectlyIfAbsent(
      DexType type, boolean lookUpwards) {
    assert type.isClassType();
    Boolean cache = mayHaveFinalizeMethodDirectlyOrIndirectlyCache.get(type);
    if (cache != null) {
      return cache;
    }
    DexClass clazz = definitionFor(type);
    if (clazz == null) {
      // This is strictly not conservative but is needed to avoid that we treat Object as having
      // a subtype that has a non-default finalize() implementation.
      mayHaveFinalizeMethodDirectlyOrIndirectlyCache.put(type, false);
      return false;
    }
    if (clazz.isProgramClass()) {
      if (lookUpwards) {
        DexEncodedMethod resolutionResult =
            resolveMethod(type, dexItemFactory().objectMethods.finalize).getSingleTarget();
        if (resolutionResult != null && resolutionResult.isProgramMethod(this)) {
          mayHaveFinalizeMethodDirectlyOrIndirectlyCache.put(type, true);
          return true;
        }
      } else {
        if (clazz.lookupVirtualMethod(dexItemFactory().objectMethods.finalize) != null) {
          mayHaveFinalizeMethodDirectlyOrIndirectlyCache.put(type, true);
          return true;
        }
      }
    }
    for (DexType subtype : allImmediateSubtypes(type)) {
      if (computeMayHaveFinalizeMethodDirectlyOrIndirectlyIfAbsent(subtype, false)) {
        mayHaveFinalizeMethodDirectlyOrIndirectlyCache.put(type, true);
        return true;
      }
    }
    mayHaveFinalizeMethodDirectlyOrIndirectlyCache.put(type, false);
    return false;
  }
}
