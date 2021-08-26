// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.graph.ResolutionResult.ArrayCloneMethodResult;
import com.android.tools.r8.graph.ResolutionResult.ClassNotFoundResult;
import com.android.tools.r8.graph.ResolutionResult.IncompatibleClassResult;
import com.android.tools.r8.graph.ResolutionResult.NoSuchMethodResult;
import com.android.tools.r8.graph.ResolutionResult.SingleResolutionResult;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ListUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AppInfo implements DexDefinitionSupplier {

  private final DexApplication app;
  private final DexItemFactory dexItemFactory;
  private final ConcurrentHashMap<DexType, Map<Descriptor<?,?>, KeyedDexItem<?>>> definitions =
      new ConcurrentHashMap<>();
  // For some optimizations, e.g. optimizing synthetic classes, we may need to resolve the current
  // class being optimized.
  final ConcurrentHashMap<DexType, DexProgramClass> synthesizedClasses = new ConcurrentHashMap<>();

  // Set when a new AppInfo replaces a previous one. All public methods should verify that the
  // current instance is not obsolete, to ensure that we almost use the most recent AppInfo.
  private boolean obsolete;

  public AppInfo(DexApplication application) {
    this.app = application;
    this.dexItemFactory = app.dexItemFactory;
  }

  protected AppInfo(AppInfo previous) {
    assert !previous.isObsolete();
    this.app = previous.app;
    this.dexItemFactory = app.dexItemFactory;
    this.definitions.putAll(previous.definitions);
    copyMetadataFromPrevious(previous);
  }

  protected InternalOptions options() {
    return app.options;
  }

  public void copyMetadataFromPrevious(AppInfo previous) {
    this.synthesizedClasses.putAll(previous.synthesizedClasses);
  }

  public boolean isObsolete() {
    return obsolete;
  }

  public void markObsolete() {
    obsolete = true;
  }

  public void unsetObsolete() {
    obsolete = false;
  }

  public boolean checkIfObsolete() {
    assert !isObsolete();
    return true;
  }

  public DexApplication app() {
    assert checkIfObsolete();
    return app;
  }

  @Override
  public DexItemFactory dexItemFactory() {
    assert checkIfObsolete();
    return dexItemFactory;
  }

  public void addSynthesizedClass(DexProgramClass clazz) {
    assert checkIfObsolete();
    assert clazz.type.isD8R8SynthesizedClassType();
    DexProgramClass previous = synthesizedClasses.put(clazz.type, clazz);
    invalidateTypeCacheFor(clazz.type);
    assert previous == null || previous == clazz;
  }

  public Collection<DexProgramClass> getSynthesizedClassesForSanityCheck() {
    assert checkIfObsolete();
    return Collections.unmodifiableCollection(synthesizedClasses.values());
  }

  private Map<Descriptor<?,?>, KeyedDexItem<?>> computeDefinitions(DexType type) {
    Builder<Descriptor<?,?>, KeyedDexItem<?>> builder = ImmutableMap.builder();
    DexClass clazz = definitionFor(type);
    if (clazz != null) {
      clazz.forEachMethod(method -> builder.put(method.getKey(), method));
      clazz.forEachField(field -> builder.put(field.getKey(), field));
    }
    return builder.build();
  }

  public Iterable<DexProgramClass> classes() {
    assert checkIfObsolete();
    return app.classes();
  }

  public Iterable<DexProgramClass> classesWithDeterministicOrder() {
    assert checkIfObsolete();
    return app.classesWithDeterministicOrder();
  }

  @Override
  public DexDefinition definitionFor(DexReference reference) {
    assert checkIfObsolete();
    if (reference.isDexType()) {
      return definitionFor(reference.asDexType());
    }
    if (reference.isDexMethod()) {
      return definitionFor(reference.asDexMethod());
    }
    assert reference.isDexField();
    return definitionFor(reference.asDexField());
  }

  @Override
  public DexClass definitionFor(DexType type) {
    assert checkIfObsolete();
    DexProgramClass cached = synthesizedClasses.get(type);
    if (cached != null) {
      assert app.definitionFor(type) == null;
      return cached;
    }
    return app.definitionFor(type);
  }

  @Override
  public DexProgramClass definitionForProgramType(DexType type) {
    return app.programDefinitionFor(type);
  }

  public Origin originFor(DexType type) {
    assert checkIfObsolete();
    DexClass definition = app.definitionFor(type);
    return definition == null ? Origin.unknown() : definition.origin;
  }

  @Override
  public DexEncodedMethod definitionFor(DexMethod method) {
    assert checkIfObsolete();
    DexType holderType = method.holder;
    DexEncodedMethod cached = (DexEncodedMethod) getDefinitions(holderType).get(method);
    if (cached != null && cached.isObsolete()) {
      definitions.remove(holderType);
      cached = (DexEncodedMethod) getDefinitions(holderType).get(method);
    }
    return cached;
  }

  @Override
  public DexEncodedField definitionFor(DexField field) {
    assert checkIfObsolete();
    return (DexEncodedField) getDefinitions(field.holder).get(field);
  }

  private Map<Descriptor<?,?>, KeyedDexItem<?>> getDefinitions(DexType type) {
    Map<Descriptor<?,?>, KeyedDexItem<?>> typeDefinitions = definitions.get(type);
    if (typeDefinitions != null) {
      return typeDefinitions;
    }

    typeDefinitions = computeDefinitions(type);
    Map<Descriptor<?,?>, KeyedDexItem<?>> existing = definitions.putIfAbsent(type, typeDefinitions);
    return existing != null ? existing : typeDefinitions;
  }

  public void invalidateTypeCacheFor(DexType type) {
    definitions.remove(type);
  }

  /**
   * Lookup static method following the super chain from the holder of {@code method}.
   * <p>
   * This method will resolve the method on the holder of {@code method} and only return a non-null
   * value if the result of resolution was a static, non-abstract method.
   *
   * @param method the method to lookup
   * @return The actual target for {@code method} or {@code null} if none found.
   */
  public DexEncodedMethod lookupStaticTarget(DexMethod method) {
    assert checkIfObsolete();
    ResolutionResult resolutionResult = resolveMethod(method.holder, method);
    DexEncodedMethod target = resolutionResult.getSingleTarget();
    return target == null || target.isStatic() ? target : null;
  }

  /**
   * Lookup super method following the super chain from the holder of {@code method}.
   *
   * <p>This method will resolve the method on the holder of {@code method} and only return a
   * non-null value if the result of resolution was an instance (i.e. non-static) method.
   *
   * @param method the method to lookup
   * @param invocationContext the class the invoke is contained in, i.e., the holder of the caller.
   * @return The actual target for {@code method} or {@code null} if none found.
   */
  public DexEncodedMethod lookupSuperTarget(DexMethod method, DexType invocationContext) {
    assert checkIfObsolete();
    return resolveMethod(method.holder, method).lookupInvokeSuperTarget(invocationContext, this);
  }

  /**
   * Lookup direct method following the super chain from the holder of {@code method}.
   * <p>
   * This method will lookup private and constructor methods.
   *
   * @param method the method to lookup
   * @return The actual target for {@code method} or {@code null} if none found.
   */
  public DexEncodedMethod lookupDirectTarget(DexMethod method) {
    assert checkIfObsolete();
    ResolutionResult resolutionResult = resolveMethod(method.holder, method);
    DexEncodedMethod target = resolutionResult.getSingleTarget();
    return target == null || target.isDirectMethod() ? target : null;
  }

  /**
   * Lookup virtual method starting in type and following the super chain.
   * <p>
   * This method will resolve the method on the holder of {@code method} and only return a
   * non-null value if the result of resolution was a non-static, non-private method.
   */
  public DexEncodedMethod lookupVirtualTarget(DexType type, DexMethod method) {
    assert checkIfObsolete();
    assert type.isClassType() || type.isArrayType();
    ResolutionResult resolutionResult = resolveMethod(type, method);
    DexEncodedMethod target = resolutionResult.getSingleTarget();
    return target == null || target.isVirtualMethod() ? target : null;
  }

  /**
   * Implements resolution of a method descriptor against a target type.
   *
   * <p>This method will query the definition of the holder to decide on which resolution to use. If
   * the holder is an interface, it delegates to {@link #resolveMethodOnInterface(DexType,
   * DexMethod)}, otherwise {@link #resolveMethodOnClass(DexType, DexMethod)} is used.
   *
   * <p>This is to overcome the shortcoming of the DEX file format that does not allow to encode the
   * kind of a method reference.
   */
  public ResolutionResult resolveMethod(DexType holder, DexMethod method) {
    assert checkIfObsolete();
    if (holder.isArrayType()) {
      return resolveMethodOnArray(holder, method);
    }
    DexClass definition = definitionFor(holder);
    if (definition == null) {
      return ClassNotFoundResult.INSTANCE;
    }
    return resolveMethod(definition, method);
  }

  public ResolutionResult resolveMethod(DexClass holder, DexMethod method) {
    return holder.isInterface()
        ? resolveMethodOnInterface(holder, method)
        : resolveMethodOnClass(holder, method);
  }

  /**
   * Implements resolution of a method descriptor against a target type.
   *
   * <p>The boolean isInterface parameter denotes if the method reference is an interface method
   * reference, and if so method resolution is done according to interface method resolution.
   *
   * @param holder Type at which to initiate the resolution.
   * @param method Method descriptor for resolution (the field method.holder is ignored).
   * @param isInterface Indicates if resolution is to be done according to class or interface.
   * @return The result of resolution.
   */
  public ResolutionResult resolveMethod(DexType holder, DexMethod method, boolean isInterface) {
    assert checkIfObsolete();
    return isInterface
        ? resolveMethodOnInterface(holder, method)
        : resolveMethodOnClass(holder, method);
  }

  /**
   * Implements resolution of a method descriptor against an array type.
   *
   * <p>See <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-10.html#jls-10.7">Section
   * 10.7 of the Java Language Specification</a>. All invokations will have target java.lang.Object
   * except clone which has no target.
   */
  private ResolutionResult resolveMethodOnArray(DexType holder, DexMethod method) {
    assert checkIfObsolete();
    assert holder.isArrayType();
    if (method.name == dexItemFactory.cloneMethodName) {
      return ArrayCloneMethodResult.INSTANCE;
    } else {
      return resolveMethodOnClass(dexItemFactory.objectType, method);
    }
  }

  /**
   * Implements resolution of a method descriptor against a class type.
   * <p>
   * See <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3">
   * Section 5.4.3.3 of the JVM Spec</a>.
   * <p>
   * The resolved method is not the method that will actually be invoked. Which methods gets
   * invoked depends on the invoke instruction used. However, it is always safe to rewrite
   * any invoke on the given descriptor to a corresponding invoke on the resolved descriptor, as the
   * resolved method is used as basis for dispatch.
   */
  public ResolutionResult resolveMethodOnClass(DexType holder, DexMethod method) {
    assert checkIfObsolete();
    if (holder.isArrayType()) {
      return resolveMethodOnArray(holder, method);
    }
    DexClass clazz = definitionFor(holder);
    if (clazz == null) {
      return ClassNotFoundResult.INSTANCE;
    }
    // Step 1: If holder is an interface, resolution fails with an ICCE. We return null.
    if (clazz.isInterface()) {
      return IncompatibleClassResult.INSTANCE;
    }
    return resolveMethodOnClass(clazz, method);
  }

  public ResolutionResult resolveMethodOnClass(DexClass clazz, DexMethod method) {
    assert checkIfObsolete();
    assert !clazz.isInterface();
    // Step 2:
    SingleResolutionResult result = resolveMethodOnClassStep2(clazz, method, clazz);
    if (result != null) {
      return result;
    }
    // Finally Step 3:
    return resolveMethodStep3(clazz, method);
  }

  /**
   * Implements step 2 of method resolution on classes as per <a
   * href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3">Section
   * 5.4.3.3 of the JVM Spec</a>.
   */
  private SingleResolutionResult resolveMethodOnClassStep2(
      DexClass clazz, DexMethod method, DexClass initialResolutionHolder) {
    // Pt. 1: Signature polymorphic method check.
    // See also <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.9">
    // Section 2.9 of the JVM Spec</a>.
    DexEncodedMethod result = clazz.lookupSignaturePolymorphicMethod(method.name, dexItemFactory);
    if (result != null) {
      return new SingleResolutionResult(initialResolutionHolder, clazz, result);
    }
    // Pt 2: Find a method that matches the descriptor.
    result = clazz.lookupMethod(method);
    if (result != null) {
      return new SingleResolutionResult(initialResolutionHolder, clazz, result);
    }
    // Pt 3: Apply step two to direct superclass of holder.
    if (clazz.superType != null) {
      DexClass superClass = definitionFor(clazz.superType);
      if (superClass != null) {
        return resolveMethodOnClassStep2(superClass, method, initialResolutionHolder);
      }
    }
    return null;
  }

  /**
   * Helper method used for emulated interface resolution (not in JVM specifications). The result
   * may be abstract.
   */
  public ResolutionResult resolveMaximallySpecificMethods(DexClass clazz, DexMethod method) {
    assert !clazz.type.isArrayType();
    if (clazz.isInterface()) {
      // Look for exact method on interface.
      DexEncodedMethod result = clazz.lookupMethod(method);
      if (result != null) {
        return new SingleResolutionResult(clazz, clazz, result);
      }
    }
    return resolveMethodStep3(clazz, method);
  }

  /**
   * Implements step 3 of <a
   * href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3">Section
   * 5.4.3.3 of the JVM Spec</a>. As this is the same for interfaces and classes, we share one
   * implementation.
   */
  private ResolutionResult resolveMethodStep3(DexClass clazz, DexMethod method) {
    MaximallySpecificMethodsBuilder builder = new MaximallySpecificMethodsBuilder(clazz);
    resolveMethodStep3Helper(clazz, method, builder);
    return builder.resolve();
  }

  /** Helper method that builds the set of maximally specific methods. */
  private void resolveMethodStep3Helper(
      DexClass clazz, DexMethod method, MaximallySpecificMethodsBuilder builder) {
    for (DexType iface : clazz.interfaces.values) {
      DexClass definiton = definitionFor(iface);
      if (definiton == null) {
        // Ignore missing interface definitions.
        continue;
      }
      assert definiton.isInterface();
      DexEncodedMethod result = definiton.lookupMethod(method);
      if (isMaximallySpecificCandidate(result)) {
        // The candidate is added and doing so will prohibit shadowed methods from being in the set.
        builder.addCandidate(definiton, result, this);
      } else {
        // Look at the super-interfaces of this class and keep searching.
        resolveMethodStep3Helper(definiton, method, builder);
      }
    }
    // Now look at indirect super interfaces.
    if (clazz.superType != null) {
      DexClass superClass = definitionFor(clazz.superType);
      if (superClass != null) {
        resolveMethodStep3Helper(superClass, method, builder);
      }
    }
  }

  /**
   * A candidate for being a maximally specific method must have neither its private, nor its static
   * flag set. A candidate may still not be maximally specific, which entails that no subinterfaces
   * from also contribute with a candidate to the type. That is not determined by this method.
   */
  private boolean isMaximallySpecificCandidate(DexEncodedMethod method) {
    return method != null && !method.accessFlags.isPrivate() && !method.accessFlags.isStatic();
  }

  /**
   * Implements resolution of a method descriptor against an interface type.
   * <p>
   * See <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3">
   * Section 5.4.3.4 of the JVM Spec</a>.
   * <p>
   * The resolved method is not the method that will actually be invoked. Which methods gets
   * invoked depends on the invoke instruction used. However, it is always save to rewrite
   * any invoke on the given descriptor to a corresponding invoke on the resolved descriptor, as the
   * resolved method is used as basis for dispatch.
   */
  public ResolutionResult resolveMethodOnInterface(DexType holder, DexMethod desc) {
    assert checkIfObsolete();
    if (holder.isArrayType()) {
      return IncompatibleClassResult.INSTANCE;
    }
    // Step 1: Lookup interface.
    DexClass definition = definitionFor(holder);
    // If the definition is not an interface, resolution fails with an ICCE. We just return the
    // empty result here.
    if (definition == null) {
      return ClassNotFoundResult.INSTANCE;
    }
    if (!definition.isInterface()) {
      return IncompatibleClassResult.INSTANCE;
    }
    return resolveMethodOnInterface(definition, desc);
  }

  public ResolutionResult resolveMethodOnInterface(DexClass definition, DexMethod desc) {
    assert checkIfObsolete();
    assert definition.isInterface();
    // Step 2: Look for exact method on interface.
    DexEncodedMethod result = definition.lookupMethod(desc);
    if (result != null) {
      return new SingleResolutionResult(definition, definition, result);
    }
    // Step 3: Look for matching method on object class.
    DexClass objectClass = definitionFor(dexItemFactory.objectType);
    if (objectClass == null) {
      return ClassNotFoundResult.INSTANCE;
    }
    result = objectClass.lookupMethod(desc);
    if (result != null && result.accessFlags.isPublic() && !result.accessFlags.isAbstract()) {
      return new SingleResolutionResult(definition, objectClass, result);
    }
    // Step 3: Look for maximally-specific superinterface methods or any interface definition.
    //         This is the same for classes and interfaces.
    return resolveMethodStep3(definition, desc);
  }

  /**
   * Lookup instance field starting in type and following the interface and super chain.
   * <p>
   * The result is the field that will be hit at runtime, if such field is known. A result
   * of null indicates that the field is either undefined or not an instance field.
   */
  public DexEncodedField lookupInstanceTarget(DexType type, DexField field) {
    assert checkIfObsolete();
    assert type.isClassType();
    DexEncodedField result = resolveFieldOn(type, field);
    return result == null || result.accessFlags.isStatic() ? null : result;
  }

  /**
   * Lookup static field starting in type and following the interface and super chain.
   * <p>
   * The result is the field that will be hit at runtime, if such field is known. A result
   * of null indicates that the field is either undefined or not a static field.
   */
  public DexEncodedField lookupStaticTarget(DexType type, DexField field) {
    assert checkIfObsolete();
    assert type.isClassType();
    DexEncodedField result = resolveFieldOn(type, field);
    return result == null || !result.accessFlags.isStatic() ? null : result;
  }

  /**
   * Implements resolution of a field descriptor against the holder of the field. See also {@link
   * #resolveFieldOn}.
   */
  public DexEncodedField resolveField(DexField field) {
    assert checkIfObsolete();
    return resolveFieldOn(field.holder, field);
  }

  /**
   * Implements resolution of a field descriptor against a type.
   * <p>
   * See <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2">
   * Section 5.4.3.2 of the JVM Spec</a>.
   */
  public DexEncodedField resolveFieldOn(DexType type, DexField desc) {
    assert checkIfObsolete();
    DexClass holder = definitionFor(type);
    return holder != null ? resolveFieldOn(holder, desc) : null;
  }

  public DexEncodedField resolveFieldOn(DexClass holder, DexField desc) {
    assert checkIfObsolete();
    assert holder != null;
    // Step 1: Class declares the field.
    DexEncodedField result = holder.lookupField(desc);
    if (result != null) {
      return result;
    }
    // Step 2: Apply recursively to direct superinterfaces. First match succeeds.
    for (DexType iface : holder.interfaces.values) {
      result = resolveFieldOn(iface, desc);
      if (result != null) {
        return result;
      }
    }
    // Step 3: Apply recursively to superclass.
    if (holder.superType != null) {
      result = resolveFieldOn(holder.superType, desc);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public boolean hasSubtyping() {
    assert checkIfObsolete();
    return false;
  }

  public AppInfoWithSubtyping withSubtyping() {
    assert checkIfObsolete();
    return null;
  }

  public boolean hasLiveness() {
    assert checkIfObsolete();
    return false;
  }

  public AppInfoWithLiveness withLiveness() {
    assert checkIfObsolete();
    return null;
  }

  public boolean isInMainDexList(DexType type) {
    assert checkIfObsolete();
    return app.mainDexList.contains(type);
  }

  private static class MaximallySpecificMethodsBuilder {

    private final DexClass initialResolutionHolder;

    // The set of actual maximally specific methods.
    // This set is linked map so that in the case where a number of methods remain a deterministic
    // choice can be made. The map is from definition classes to their maximally specific method, or
    // in the case that a type has a candidate which is shadowed by a subinterface, the map will
    // map the class to a null entry, thus any addition to the map must check for key containment
    // prior to writing.
    LinkedHashMap<DexClass, DexEncodedMethod> maximallySpecificMethods = new LinkedHashMap<>();

    public MaximallySpecificMethodsBuilder(DexClass initialResolutionHolder) {
      this.initialResolutionHolder = initialResolutionHolder;
    }

    void addCandidate(DexClass holder, DexEncodedMethod method, AppInfo appInfo) {
      // If this candidate is already a candidate or it is shadowed, then no need to continue.
      if (maximallySpecificMethods.containsKey(holder)) {
        return;
      }
      maximallySpecificMethods.put(holder, method);
      // Prune exiting candidates and prohibit future candidates in the super hierarchy.
      assert holder.isInterface();
      assert holder.superType == appInfo.dexItemFactory.objectType;
      for (DexType iface : holder.interfaces.values) {
        markShadowed(iface, appInfo);
      }
    }

    private void markShadowed(DexType type, AppInfo appInfo) {
      if (type == null) {
        return;
      }
      DexClass clazz = appInfo.definitionFor(type);
      if (clazz == null) {
        return;
      }
      assert clazz.isInterface();
      assert clazz.superType == appInfo.dexItemFactory.objectType;
      // A null entry signifies that the candidate is shadowed blocking future candidates.
      // If the candidate is already shadowed at this type there is no need to shadow further up.
      if (maximallySpecificMethods.containsKey(clazz)
          && maximallySpecificMethods.get(clazz) == null) {
        return;
      }
      maximallySpecificMethods.put(clazz, null);
      for (DexType iface : clazz.interfaces.values) {
        markShadowed(iface, appInfo);
      }
    }

    ResolutionResult resolve() {
      if (maximallySpecificMethods.isEmpty()) {
        return NoSuchMethodResult.INSTANCE;
      }
      // Fast path in the common case of a single method.
      if (maximallySpecificMethods.size() == 1) {
        Entry<DexClass, DexEncodedMethod> first =
            maximallySpecificMethods.entrySet().iterator().next();
        return new SingleResolutionResult(
            initialResolutionHolder, first.getKey(), first.getValue());
      }
      Entry<DexClass, DexEncodedMethod> firstMaximallySpecificMethod = null;
      List<Entry<DexClass, DexEncodedMethod>> nonAbstractMethods =
          new ArrayList<>(maximallySpecificMethods.size());
      for (Entry<DexClass, DexEncodedMethod> entry : maximallySpecificMethods.entrySet()) {
        DexEncodedMethod method = entry.getValue();
        if (method == null) {
          // Ignore shadowed candidates.
          continue;
        }
        if (firstMaximallySpecificMethod == null) {
          firstMaximallySpecificMethod = entry;
        }
        if (method.isNonAbstractVirtualMethod()) {
          nonAbstractMethods.add(entry);
        }
      }
      // If there are no non-abstract methods, then any candidate will suffice as a target.
      // For deterministic resolution, we return the first mapped method (of the linked map).
      if (nonAbstractMethods.isEmpty()) {
        return new SingleResolutionResult(
            initialResolutionHolder,
            firstMaximallySpecificMethod.getKey(),
            firstMaximallySpecificMethod.getValue());
      }
      // If there is exactly one non-abstract method (a default method) it is the resolution target.
      if (nonAbstractMethods.size() == 1) {
        Entry<DexClass, DexEncodedMethod> entry = nonAbstractMethods.get(0);
        return new SingleResolutionResult(
            initialResolutionHolder, entry.getKey(), entry.getValue());
      }
      return IncompatibleClassResult.create(ListUtils.map(nonAbstractMethods, Entry::getValue));
    }
  }

}
