// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

// This graph lense is instantiated during vertical class merging. The graph lense is context
// sensitive in the enclosing class of a given invoke *and* the type of the invoke (e.g., invoke-
// super vs invoke-virtual). This is illustrated by the following example.
//
// public class A {
//   public void m() { ... }
// }
// public class B extends A {
//   @Override
//   public void m() { invoke-super A.m(); ... }
//
//   public void m2() { invoke-virtual A.m(); ... }
// }
//
// Vertical class merging will merge class A into class B. Since class B already has a method with
// the signature "void B.m()", the method A.m will be given a fresh name and moved to class B.
// During this process, the method corresponding to A.m will be made private such that it can be
// called via an invoke-direct instruction.
//
// For the invocation "invoke-super A.m()" in B.m, this graph lense will return the newly created,
// private method corresponding to A.m (that is now in B.m with a fresh name), such that the
// invocation will hit the same implementation as the original super.m() call.
//
// For the invocation "invoke-virtual A.m()" in B.m2, this graph lense will return the method B.m.
public class VerticalClassMergerGraphLense extends NestedGraphLense {

  private final AppView<?> appView;

  private final Map<DexType, Map<DexMethod, GraphLenseLookupResult>>
      contextualVirtualToDirectMethodMaps;
  private Set<DexMethod> mergedMethods;
  private final Map<DexMethod, DexMethod> originalMethodSignaturesForBridges;

  private VerticalClassMergerGraphLense(
      AppView<?> appView,
      Map<DexType, DexType> typeMap,
      Map<DexField, DexField> fieldMap,
      Map<DexMethod, DexMethod> methodMap,
      Set<DexMethod> mergedMethods,
      Map<DexType, Map<DexMethod, GraphLenseLookupResult>> contextualVirtualToDirectMethodMaps,
      BiMap<DexField, DexField> originalFieldSignatures,
      BiMap<DexMethod, DexMethod> originalMethodSignatures,
      Map<DexMethod, DexMethod> originalMethodSignaturesForBridges,
      GraphLense previousLense) {
    super(
        typeMap,
        methodMap,
        fieldMap,
        originalFieldSignatures,
        originalMethodSignatures,
        previousLense,
        appView.dexItemFactory());
    this.appView = appView;
    this.contextualVirtualToDirectMethodMaps = contextualVirtualToDirectMethodMaps;
    this.mergedMethods = mergedMethods;
    this.originalMethodSignaturesForBridges = originalMethodSignaturesForBridges;
  }

  @Override
  public DexType getOriginalType(DexType type) {
    return previousLense.getOriginalType(type);
  }

  @Override
  public DexMethod getOriginalMethodSignature(DexMethod method) {
    return super.getOriginalMethodSignature(
        originalMethodSignaturesForBridges.getOrDefault(method, method));
  }

  @Override
  public GraphLenseLookupResult lookupMethod(DexMethod method, DexMethod context, Type type) {
    assert isContextFreeForMethod(method) || (context != null && type != null);
    DexMethod previousContext =
        originalMethodSignaturesForBridges.containsKey(context)
            ? originalMethodSignaturesForBridges.get(context)
            : originalMethodSignatures.getOrDefault(context, context);
    GraphLenseLookupResult previous = previousLense.lookupMethod(method, previousContext, type);
    if (previous.getType() == Type.SUPER && !mergedMethods.contains(context)) {
      Map<DexMethod, GraphLenseLookupResult> virtualToDirectMethodMap =
          contextualVirtualToDirectMethodMaps.get(context.holder);
      if (virtualToDirectMethodMap != null) {
        GraphLenseLookupResult lookup = virtualToDirectMethodMap.get(previous.getMethod());
        if (lookup != null) {
          // If the super class A of the enclosing class B (i.e., context.method.holder)
          // has been merged into B during vertical class merging, and this invoke-super instruction
          // was resolving to a method in A, then the target method has been changed to a direct
          // method and moved into B, so that we need to use an invoke-direct instruction instead of
          // invoke-super (or invoke-static, if the method was originally a default interface
          // method).
          return lookup;
        }
      }
    }
    return super.lookupMethod(previous.getMethod(), context, previous.getType());
  }

  @Override
  protected Type mapInvocationType(DexMethod newMethod, DexMethod originalMethod, Type type) {
    return super.mapVirtualInterfaceInvocationTypes(appView, newMethod, originalMethod, type);
  }

  @Override
  public Set<DexMethod> lookupMethodInAllContexts(DexMethod method) {
    ImmutableSet.Builder<DexMethod> builder = ImmutableSet.builder();
    for (DexMethod previous : previousLense.lookupMethodInAllContexts(method)) {
      builder.add(methodMap.getOrDefault(previous, previous));
      for (Map<DexMethod, GraphLenseLookupResult> virtualToDirectMethodMap :
          contextualVirtualToDirectMethodMaps.values()) {
        GraphLenseLookupResult lookup = virtualToDirectMethodMap.get(previous);
        if (lookup != null) {
          builder.add(lookup.getMethod());
        }
      }
    }
    return builder.build();
  }

  @Override
  public boolean isContextFreeForMethods() {
    return contextualVirtualToDirectMethodMaps.isEmpty() && previousLense.isContextFreeForMethods();
  }

  @Override
  public boolean isContextFreeForMethod(DexMethod method) {
    if (!previousLense.isContextFreeForMethod(method)) {
      return false;
    }
    DexMethod previous = previousLense.lookupMethod(method);
    for (Map<DexMethod, GraphLenseLookupResult> virtualToDirectMethodMap :
        contextualVirtualToDirectMethodMaps.values()) {
      if (virtualToDirectMethodMap.containsKey(previous)) {
        return false;
      }
    }
    return true;
  }

  public static class Builder {

    private final DexItemFactory dexItemFactory;

    protected final BiMap<DexField, DexField> fieldMap = HashBiMap.create();
    protected final Map<DexMethod, DexMethod> methodMap = new IdentityHashMap<>();
    private final ImmutableSet.Builder<DexMethod> mergedMethodsBuilder = ImmutableSet.builder();
    private final Map<DexType, Map<DexMethod, GraphLenseLookupResult>>
        contextualVirtualToDirectMethodMaps = new IdentityHashMap<>();

    private final BiMap<DexMethod, DexMethod> originalMethodSignatures = HashBiMap.create();
    private final Map<DexMethod, DexMethod> originalMethodSignaturesForBridges =
        new IdentityHashMap<>();

    private final Map<DexProto, DexProto> cache = new IdentityHashMap<>();

    Builder(DexItemFactory dexItemFactory) {
      this.dexItemFactory = dexItemFactory;
    }

    static Builder createBuilderForFixup(Builder builder, Map<DexType, DexType> mergedClasses) {
      Builder newBuilder = new Builder(builder.dexItemFactory);
      for (Map.Entry<DexField, DexField> entry : builder.fieldMap.entrySet()) {
        newBuilder.map(
            entry.getKey(),
            builder.getFieldSignatureAfterClassMerging(entry.getValue(), mergedClasses));
      }
      for (Map.Entry<DexMethod, DexMethod> entry : builder.methodMap.entrySet()) {
        newBuilder.map(
            entry.getKey(),
            builder.getMethodSignatureAfterClassMerging(entry.getValue(), mergedClasses));
      }
      for (DexMethod method : builder.mergedMethodsBuilder.build()) {
        newBuilder.markMethodAsMerged(
            builder.getMethodSignatureAfterClassMerging(method, mergedClasses));
      }
      for (Map.Entry<DexType, Map<DexMethod, GraphLenseLookupResult>> entry :
          builder.contextualVirtualToDirectMethodMaps.entrySet()) {
        DexType context = entry.getKey();
        assert context == builder.getTypeAfterClassMerging(context, mergedClasses);
        for (Map.Entry<DexMethod, GraphLenseLookupResult> innerEntry :
            entry.getValue().entrySet()) {
          DexMethod from = innerEntry.getKey();
          GraphLenseLookupResult rewriting = innerEntry.getValue();
          DexMethod to =
              builder.getMethodSignatureAfterClassMerging(rewriting.getMethod(), mergedClasses);
          newBuilder.mapVirtualMethodToDirectInType(
              from, new GraphLenseLookupResult(to, rewriting.getType()), context);
        }
      }
      for (Map.Entry<DexMethod, DexMethod> entry : builder.originalMethodSignatures.entrySet()) {
        newBuilder.recordMove(
            entry.getValue(),
            builder.getMethodSignatureAfterClassMerging(entry.getKey(), mergedClasses));
      }
      for (Map.Entry<DexMethod, DexMethod> entry :
          builder.originalMethodSignaturesForBridges.entrySet()) {
        newBuilder.recordCreationOfBridgeMethod(
            entry.getValue(),
            builder.getMethodSignatureAfterClassMerging(entry.getKey(), mergedClasses));
      }
      return newBuilder;
    }

    public GraphLense build(AppView<?> appView, Map<DexType, DexType> mergedClasses) {
      if (mergedClasses.isEmpty()) {
        return appView.graphLense();
      }
      BiMap<DexField, DexField> originalFieldSignatures = fieldMap.inverse();
      // Build new graph lense.
      return new VerticalClassMergerGraphLense(
          appView,
          mergedClasses,
          fieldMap,
          methodMap,
          mergedMethodsBuilder.build(),
          contextualVirtualToDirectMethodMaps,
          originalFieldSignatures,
          originalMethodSignatures,
          originalMethodSignaturesForBridges,
          appView.graphLense());
    }

    private DexField getFieldSignatureAfterClassMerging(
        DexField field, Map<DexType, DexType> mergedClasses) {
      assert !field.holder.isArrayType();

      DexType holder = field.holder;
      DexType newHolder = mergedClasses.getOrDefault(holder, holder);

      DexType type = field.type;
      DexType newType = getTypeAfterClassMerging(type, mergedClasses);

      if (holder == newHolder && type == newType) {
        return field;
      }
      return dexItemFactory.createField(newHolder, newType, field.name);
    }

    private DexMethod getMethodSignatureAfterClassMerging(
        DexMethod signature, Map<DexType, DexType> mergedClasses) {
      assert !signature.holder.isArrayType();

      DexType holder = signature.holder;
      DexType newHolder = mergedClasses.getOrDefault(holder, holder);

      DexProto proto = signature.proto;
      DexProto newProto =
          dexItemFactory.applyClassMappingToProto(
              proto, type -> getTypeAfterClassMerging(type, mergedClasses), cache);

      if (holder == newHolder && proto == newProto) {
        return signature;
      }
      return dexItemFactory.createMethod(newHolder, newProto, signature.name);
    }

    private DexType getTypeAfterClassMerging(DexType type, Map<DexType, DexType> mergedClasses) {
      if (type.isArrayType()) {
        DexType baseType = type.toBaseType(dexItemFactory);
        DexType newBaseType = mergedClasses.getOrDefault(baseType, baseType);
        if (newBaseType != baseType) {
          return type.replaceBaseType(newBaseType, dexItemFactory);
        }
        return type;
      }
      return mergedClasses.getOrDefault(type, type);
    }

    public boolean hasMappingForSignatureInContext(DexType context, DexMethod signature) {
      Map<DexMethod, GraphLenseLookupResult> virtualToDirectMethodMap =
          contextualVirtualToDirectMethodMaps.get(context);
      if (virtualToDirectMethodMap != null) {
        return virtualToDirectMethodMap.containsKey(signature);
      }
      return false;
    }

    public boolean hasOriginalSignatureMappingFor(DexField field) {
      return fieldMap.inverse().containsKey(field);
    }

    public boolean hasOriginalSignatureMappingFor(DexMethod method) {
      return originalMethodSignatures.containsKey(method)
          || originalMethodSignaturesForBridges.containsKey(method);
    }

    public void markMethodAsMerged(DexMethod method) {
      mergedMethodsBuilder.add(method);
    }

    public void map(DexField from, DexField to) {
      fieldMap.put(from, to);
    }

    public Builder map(DexMethod from, DexMethod to) {
      methodMap.put(from, to);
      return this;
    }

    public void recordMove(DexMethod from, DexMethod to) {
      originalMethodSignatures.put(to, from);
    }

    public void recordCreationOfBridgeMethod(DexMethod from, DexMethod to) {
      originalMethodSignaturesForBridges.put(to, from);
    }

    public void mapVirtualMethodToDirectInType(
        DexMethod from, GraphLenseLookupResult to, DexType type) {
      Map<DexMethod, GraphLenseLookupResult> virtualToDirectMethodMap =
          contextualVirtualToDirectMethodMaps.computeIfAbsent(type, key -> new IdentityHashMap<>());
      virtualToDirectMethodMap.put(from, to);
    }

    public void merge(VerticalClassMergerGraphLense.Builder builder) {
      fieldMap.putAll(builder.fieldMap);
      methodMap.putAll(builder.methodMap);
      mergedMethodsBuilder.addAll(builder.mergedMethodsBuilder.build());
      originalMethodSignatures.putAll(builder.originalMethodSignatures);
      originalMethodSignaturesForBridges.putAll(builder.originalMethodSignaturesForBridges);
      for (DexType context : builder.contextualVirtualToDirectMethodMaps.keySet()) {
        Map<DexMethod, GraphLenseLookupResult> current =
            contextualVirtualToDirectMethodMaps.get(context);
        Map<DexMethod, GraphLenseLookupResult> other =
            builder.contextualVirtualToDirectMethodMaps.get(context);
        if (current != null) {
          current.putAll(other);
        } else {
          contextualVirtualToDirectMethodMaps.put(context, other);
        }
      }
    }
  }
}
