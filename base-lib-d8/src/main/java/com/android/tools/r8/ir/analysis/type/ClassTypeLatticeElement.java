// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassTypeLatticeElement extends ReferenceTypeLatticeElement {

  // Least upper bound of interfaces that this class type is implementing.
  // Lazily computed on demand via DexItemFactory, where the canonicalized set will be maintained.
  private Set<DexType> lazyInterfaces;
  private AppView<? extends AppInfoWithSubtyping> appView;
  // On-demand link between other nullability-variants.
  private final NullabilityVariants<ClassTypeLatticeElement> variants;
  private final DexType type;

  public static ClassTypeLatticeElement create(
      DexType classType, Nullability nullability, Set<DexType> interfaces) {
    assert interfaces != null;
    return NullabilityVariants.create(
        nullability,
        (variants) ->
            new ClassTypeLatticeElement(classType, nullability, interfaces, variants, null));
  }

  public static ClassTypeLatticeElement create(
      DexType classType, Nullability nullability, AppView<? extends AppInfoWithSubtyping> appView) {
    assert appView != null;
    return NullabilityVariants.create(
        nullability,
        (variants) -> new ClassTypeLatticeElement(classType, nullability, null, variants, appView));
  }

  private ClassTypeLatticeElement(
      DexType classType,
      Nullability nullability,
      Set<DexType> interfaces,
      NullabilityVariants<ClassTypeLatticeElement> variants,
      AppView<? extends AppInfoWithSubtyping> appView) {
    super(nullability);
    assert classType.isClassType();
    assert interfaces != null || appView != null;
    type = classType;
    this.appView = appView;
    lazyInterfaces = interfaces;
    this.variants = variants;
  }

  public DexType getClassType() {
    return type;
  }

  public Set<DexType> getInterfaces() {
    if (lazyInterfaces == null) {
      assert appView != null;
      lazyInterfaces =
          appView.dexItemFactory()
              .getOrComputeLeastUpperBoundOfImplementedInterfaces(type, appView);
    }
    assert lazyInterfaces != null;
    return lazyInterfaces;
  }

  private ClassTypeLatticeElement createVariant(
      Nullability nullability, NullabilityVariants<ClassTypeLatticeElement> variants) {
    assert this.nullability != nullability;
    return new ClassTypeLatticeElement(type, nullability, lazyInterfaces, variants, appView);
  }

  public boolean isRelatedTo(ClassTypeLatticeElement other, AppView<?> appView) {
    return lessThanOrEqualUpToNullability(other, appView)
        || other.lessThanOrEqualUpToNullability(this, appView);
  }

  @Override
  public ClassTypeLatticeElement getOrCreateVariant(Nullability nullability) {
    ClassTypeLatticeElement variant = variants.get(nullability);
    if (variant != null) {
      return variant;
    }
    return variants.getOrCreateElement(nullability, this::createVariant);
  }


  @Override
  public boolean isBasedOnMissingClass(AppView<? extends AppInfoWithSubtyping> appView) {
    return appView.appInfo().isMissingOrHasMissingSuperType(getClassType())
        || getInterfaces().stream()
            .anyMatch(type -> appView.appInfo().isMissingOrHasMissingSuperType(type));
  }

  @Override
  public boolean isClassType() {
    return true;
  }

  @Override
  public ClassTypeLatticeElement asClassTypeLatticeElement() {
    return this;
  }

  @Override
  public ClassTypeLatticeElement asMeetWithNotNull() {
    return getOrCreateVariant(nullability.meet(Nullability.definitelyNotNull()));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(nullability);
    builder.append(" ");
    builder.append(type);
    builder.append(" {");
    Set<DexType> interfaces = getInterfaces();
    if (interfaces != null) {
      builder.append(interfaces.stream().map(DexType::toString).collect(Collectors.joining(", ")));
    }
    builder.append("}");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    // The interfaces of a type do not contribute to its hashCode as they are lazily computed.
    return Objects.hash(nullability, type);
  }

  @Override
  public ClassTypeLatticeElement fixupClassTypeReferences(
      Function<DexType, DexType> mapping, AppView<? extends AppInfoWithSubtyping> appView) {
    DexType mappedType = mapping.apply(type);
    if (mappedType != type) {
      return create(mappedType, nullability, appView);
    }
    // If the mapped type is not object and no computation of interfaces, we can return early.
    if (mappedType != appView.dexItemFactory().objectType && lazyInterfaces == null) {
      return this;
    }

    // For most types there will not have been a change thus we iterate without allocating a new
    // set for holding modified interfaces.
    boolean hasChangedInterfaces = false;
    DexClass interfaceToClassChange = null;
    for (DexType iface : getInterfaces()) {
      DexType substitutedType = mapping.apply(iface);
      if (iface != substitutedType) {
        hasChangedInterfaces = true;
        DexClass mappedClass = appView.definitionFor(substitutedType);
        if (!mappedClass.isInterface()) {
          if (interfaceToClassChange != null && mappedClass != interfaceToClassChange) {
            throw new CompilationError(
                "More than one interface has changed to a class: "
                    + interfaceToClassChange
                    + " and "
                    + mappedClass);
          }
          interfaceToClassChange = mappedClass;
        }
      }
    }
    if (hasChangedInterfaces) {
      if (interfaceToClassChange != null) {
        assert !interfaceToClassChange.isInterface();
        assert type == appView.dexItemFactory().objectType;
        return create(interfaceToClassChange.type, nullability, appView);
      } else {
        Set<DexType> newInterfaces = new HashSet<>();
        for (DexType iface : lazyInterfaces) {
          newInterfaces.add(mapping.apply(iface));
        }
        return create(mappedType, nullability, newInterfaces);
      }
    }
    return this;
  }

  ClassTypeLatticeElement join(ClassTypeLatticeElement other, AppView<?> appView) {
    Nullability nullability = nullability().join(other.nullability());
    if (!appView.appInfo().hasSubtyping()) {
      assert lazyInterfaces != null && lazyInterfaces.isEmpty();
      assert other.lazyInterfaces != null && other.lazyInterfaces.isEmpty();
      return ClassTypeLatticeElement.create(
          getClassType() == other.getClassType()
              ? getClassType()
              : appView.dexItemFactory().objectType,
          nullability,
          Collections.emptySet());
    }
    DexType lubType =
        appView
            .appInfo()
            .withSubtyping()
            .computeLeastUpperBoundOfClasses(getClassType(), other.getClassType());
    Set<DexType> c1lubItfs = getInterfaces();
    Set<DexType> c2lubItfs = other.getInterfaces();
    Set<DexType> lubItfs = null;
    if (c1lubItfs.size() == c2lubItfs.size() && c1lubItfs.containsAll(c2lubItfs)) {
      lubItfs = c1lubItfs;
    }
    if (lubItfs == null) {
      lubItfs = computeLeastUpperBoundOfInterfaces(appView.withSubtyping(), c1lubItfs, c2lubItfs);
    }
    return ClassTypeLatticeElement.create(lubType, nullability, lubItfs);
  }

  private enum InterfaceMarker {
    LEFT,
    RIGHT
  }

  private static class InterfaceWithMarker {
    final DexType itf;
    final InterfaceMarker marker;

    InterfaceWithMarker(DexType itf, InterfaceMarker marker) {
      this.itf = itf;
      this.marker = marker;
    }
  }

  // TODO(b/130636783): inconsistent location
  public static Set<DexType> computeLeastUpperBoundOfInterfaces(
      AppView<? extends AppInfoWithSubtyping> appView, Set<DexType> s1, Set<DexType> s2) {
    if (s1.isEmpty() || s2.isEmpty()) {
      return Collections.emptySet();
    }
    Set<DexType> cached = appView.dexItemFactory().leastUpperBoundOfInterfacesTable.get(s1, s2);
    if (cached != null) {
      return cached;
    }
    cached = appView.dexItemFactory().leastUpperBoundOfInterfacesTable.get(s2, s1);
    if (cached != null) {
      return cached;
    }
    Map<DexType, Set<InterfaceMarker>> seen = new IdentityHashMap<>();
    Queue<InterfaceWithMarker> worklist = new ArrayDeque<>();
    for (DexType itf1 : s1) {
      worklist.add(new InterfaceWithMarker(itf1, InterfaceMarker.LEFT));
    }
    for (DexType itf2 : s2) {
      worklist.add(new InterfaceWithMarker(itf2, InterfaceMarker.RIGHT));
    }
    while (!worklist.isEmpty()) {
      InterfaceWithMarker item = worklist.poll();
      DexType itf = item.itf;
      InterfaceMarker marker = item.marker;
      Set<InterfaceMarker> markers = seen.computeIfAbsent(itf, k -> new HashSet<>());
      // If this interface is a lower one in this set, skip.
      if (markers.contains(marker)) {
        continue;
      }
      // If this interface is already visited by the other set, add marker for this set and skip.
      if (markers.size() == 1) {
        markers.add(marker);
        continue;
      }
      // Otherwise, this type is freshly visited.
      markers.add(marker);
      // Put super interfaces into the worklist.
      DexClass itfClass = appView.definitionFor(itf);
      if (itfClass != null) {
        for (DexType superItf : itfClass.interfaces.values) {
          markers = seen.computeIfAbsent(superItf, k -> new HashSet<>());
          if (!markers.contains(marker)) {
            worklist.add(new InterfaceWithMarker(superItf, marker));
          }
        }
      }
    }

    ImmutableSet.Builder<DexType> commonBuilder = ImmutableSet.builder();
    for (Map.Entry<DexType, Set<InterfaceMarker>> entry : seen.entrySet()) {
      // Keep commonly visited interfaces only
      if (entry.getValue().size() < 2) {
        continue;
      }
      commonBuilder.add(entry.getKey());
    }
    Set<DexType> commonlyVisited = commonBuilder.build();

    ImmutableSet.Builder<DexType> lubBuilder = ImmutableSet.builder();
    for (DexType itf : commonlyVisited) {
      // If there is a strict sub interface of this interface, it is not the least element.
      boolean notTheLeast = false;
      for (DexType other : commonlyVisited) {
        if (appView.appInfo().isStrictSubtypeOf(other, itf)) {
          notTheLeast = true;
          break;
        }
      }
      if (notTheLeast) {
        continue;
      }
      lubBuilder.add(itf);
    }
    Set<DexType> lub = lubBuilder.build();
    // Cache the computation result only if the given two sets of interfaces are different.
    if (s1.size() != s2.size() || !s1.containsAll(s2)) {
      synchronized (appView.dexItemFactory().leastUpperBoundOfInterfacesTable) {
        appView.dexItemFactory().leastUpperBoundOfInterfacesTable.put(s1, s2, lub);
      }
    }
    return lub;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClassTypeLatticeElement)) {
      return false;
    }
    ClassTypeLatticeElement other = (ClassTypeLatticeElement) o;
    if (nullability() != other.nullability()) {
      return false;
    }
    if (!type.equals(other.type)) {
      return false;
    }
    Set<DexType> thisInterfaces = getInterfaces();
    Set<DexType> otherInterfaces = other.getInterfaces();
    if (thisInterfaces == otherInterfaces) {
      return true;
    }
    if (thisInterfaces.size() != otherInterfaces.size()) {
      return false;
    }
    return thisInterfaces.containsAll(otherInterfaces);
  }
}
