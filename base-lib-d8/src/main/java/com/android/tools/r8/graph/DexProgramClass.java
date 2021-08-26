// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.kotlin.KotlinInfo;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class DexProgramClass extends DexClass implements Supplier<DexProgramClass> {

  @FunctionalInterface
  public interface ChecksumSupplier {
    long getChecksum(DexProgramClass programClass);
  }

  public static final DexProgramClass[] EMPTY_ARRAY = {};

  private static final DexEncodedArray SENTINEL_NOT_YET_COMPUTED =
      new DexEncodedArray(DexValue.EMPTY_ARRAY);

  private final ProgramResource.Kind originKind;
  private DexEncodedArray staticValues = SENTINEL_NOT_YET_COMPUTED;
  private final Collection<DexProgramClass> synthesizedFrom;
  private int initialClassFileVersion = -1;
  private KotlinInfo kotlinInfo = null;

  private final ChecksumSupplier checksumSupplier;

  public DexProgramClass(
      DexType type,
      Kind originKind,
      Origin origin,
      ClassAccessFlags accessFlags,
      DexType superType,
      DexTypeList interfaces,
      DexString sourceFile,
      NestHostClassAttribute nestHost,
      List<NestMemberClassAttribute> nestMembers,
      EnclosingMethodAttribute enclosingMember,
      List<InnerClassAttribute> innerClasses,
      DexAnnotationSet classAnnotations,
      DexEncodedField[] staticFields,
      DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods,
      DexEncodedMethod[] virtualMethods,
      boolean skipNameValidationForTesting,
      ChecksumSupplier checksumSupplier) {
    this(
        type,
        originKind,
        origin,
        accessFlags,
        superType,
        interfaces,
        sourceFile,
        nestHost,
        nestMembers,
        enclosingMember,
        innerClasses,
        classAnnotations,
        staticFields,
        instanceFields,
        directMethods,
        virtualMethods,
        skipNameValidationForTesting,
        checksumSupplier,
        Collections.emptyList());
  }

  public DexProgramClass(
      DexType type,
      Kind originKind,
      Origin origin,
      ClassAccessFlags accessFlags,
      DexType superType,
      DexTypeList interfaces,
      DexString sourceFile,
      NestHostClassAttribute nestHost,
      List<NestMemberClassAttribute> nestMembers,
      EnclosingMethodAttribute enclosingMember,
      List<InnerClassAttribute> innerClasses,
      DexAnnotationSet classAnnotations,
      DexEncodedField[] staticFields,
      DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods,
      DexEncodedMethod[] virtualMethods,
      boolean skipNameValidationForTesting,
      ChecksumSupplier checksumSupplier,
      Collection<DexProgramClass> synthesizedDirectlyFrom) {
    super(
        sourceFile,
        interfaces,
        accessFlags,
        superType,
        type,
        staticFields,
        instanceFields,
        directMethods,
        virtualMethods,
        nestHost,
        nestMembers,
        enclosingMember,
        innerClasses,
        classAnnotations,
        origin,
        skipNameValidationForTesting);
    assert checksumSupplier != null;
    assert classAnnotations != null;
    this.originKind = originKind;
    this.checksumSupplier = checksumSupplier;
    this.synthesizedFrom = new HashSet<>();
    synthesizedDirectlyFrom.forEach(this::addSynthesizedFrom);
  }

  public boolean originatesFromDexResource() {
    return originKind == Kind.DEX;
  }

  public boolean originatesFromClassResource() {
    return originKind == Kind.CF;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    if (indexedItems.addClass(this)) {
      type.collectIndexedItems(indexedItems, method, instructionOffset);
      if (superType != null) {
        superType.collectIndexedItems(indexedItems, method, instructionOffset);
      } else {
        assert type.toDescriptorString().equals("Ljava/lang/Object;");
      }
      if (sourceFile != null) {
        sourceFile.collectIndexedItems(indexedItems, method, instructionOffset);
      }
      if (annotations != null) {
        annotations.collectIndexedItems(indexedItems, method, instructionOffset);
      }
      if (interfaces != null) {
        interfaces.collectIndexedItems(indexedItems, method, instructionOffset);
      }
      if (getEnclosingMethod() != null) {
        getEnclosingMethod().collectIndexedItems(indexedItems);
      }
      for (InnerClassAttribute attribute : getInnerClasses()) {
        attribute.collectIndexedItems(indexedItems);
      }
      synchronizedCollectAll(indexedItems, staticFields);
      synchronizedCollectAll(indexedItems, instanceFields);
      synchronizedCollectAll(indexedItems, directMethods);
      synchronizedCollectAll(indexedItems, virtualMethods);
    }
  }

  private static <T extends DexItem> void synchronizedCollectAll(IndexedItemCollection collection,
      T[] items) {
    synchronized (items) {
      collectAll(collection, items);
    }
  }

  public Collection<DexProgramClass> getSynthesizedFrom() {
    return synthesizedFrom;
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    assert getEnclosingMethod() == null;
    assert getInnerClasses().isEmpty();
    if (hasAnnotations()) {
      mixedItems.setAnnotationsDirectoryForClass(this, new DexAnnotationDirectory(this));
    }
  }

  @Override
  public void addDependencies(MixedSectionCollection collector) {
    assert getEnclosingMethod() == null;
    assert getInnerClasses().isEmpty();
    // We only have a class data item if there are methods or fields.
    if (hasMethodsOrFields()) {
      collector.add(this);

      // The ordering of methods and fields may not be deterministic due to concurrency
      // (see b/116027780).
      sortMembers();
      synchronizedCollectAll(collector, directMethods);
      synchronizedCollectAll(collector, virtualMethods);
      synchronizedCollectAll(collector, staticFields);
      synchronizedCollectAll(collector, instanceFields);
    }
    if (annotations != null) {
      annotations.collectMixedSectionItems(collector);
    }
    if (interfaces != null) {
      interfaces.collectMixedSectionItems(collector);
    }
    annotations.collectMixedSectionItems(collector);
  }

  private static <T extends DexItem> void synchronizedCollectAll(MixedSectionCollection collection,
      T[] items) {
    synchronized (items) {
      collectAll(collection, items);
    }
  }

  @Override
  public String toString() {
    return type.toString();
  }

  @Override
  public String toSourceString() {
    return type.toSourceString();
  }

  /**
   * Returns true if this class is final, or it is a non-pinned program class with no instantiated
   * subtypes.
   */
  @Override
  public boolean isEffectivelyFinal(AppView<?> appView) {
    if (isFinal()) {
      return true;
    }
    if (appView.enableWholeProgramOptimizations()) {
      assert appView.appInfo().hasLiveness();
      AppInfoWithLiveness appInfo = appView.appInfo().withLiveness();
      if (appInfo.isPinned(type)) {
        return false;
      }
      return !appInfo.hasSubtypes(type) || !appInfo.isInstantiatedIndirectly(this);
    }
    return false;
  }

  @Override
  public boolean isProgramClass() {
    return true;
  }

  @Override
  public DexProgramClass asProgramClass() {
    return this;
  }

  public static DexProgramClass asProgramClassOrNull(DexClass clazz) {
    return clazz != null ? clazz.asProgramClass() : null;
  }

  @Override
  public boolean isNotProgramClass() {
    return false;
  }

  @Override
  public KotlinInfo getKotlinInfo() {
    return kotlinInfo;
  }

  public void setKotlinInfo(KotlinInfo kotlinInfo) {
    assert this.kotlinInfo == null || kotlinInfo == null;
    this.kotlinInfo = kotlinInfo;
  }

  public boolean hasFields() {
    return instanceFields.length + staticFields.length > 0;
  }

  public boolean hasMethods() {
    return directMethods.length + virtualMethods.length > 0;
  }

  public boolean hasMethodsOrFields() {
    return hasMethods() || hasFields();
  }

  public boolean hasAnnotations() {
    return !annotations.isEmpty()
        || hasAnnotations(virtualMethods)
        || hasAnnotations(directMethods)
        || hasAnnotations(staticFields)
        || hasAnnotations(instanceFields);
  }

  boolean hasOnlyInternalizableAnnotations() {
    return !hasAnnotations(virtualMethods)
        && !hasAnnotations(directMethods)
        && !hasAnnotations(staticFields)
        && !hasAnnotations(instanceFields);
  }

  private boolean hasAnnotations(DexEncodedField[] fields) {
    synchronized (fields) {
      return Arrays.stream(fields).anyMatch(DexEncodedField::hasAnnotation);
    }
  }

  private boolean hasAnnotations(DexEncodedMethod[] methods) {
    synchronized (methods) {
      return Arrays.stream(methods).anyMatch(DexEncodedMethod::hasAnnotation);
    }
  }

  public void addSynthesizedFrom(DexProgramClass clazz) {
    if (clazz.synthesizedFrom.isEmpty()) {
      synthesizedFrom.add(clazz);
    } else {
      clazz.synthesizedFrom.forEach(this::addSynthesizedFrom);
    }
  }

  public void computeStaticValues() {
    // It does not actually hurt to compute this multiple times. So racing on staticValues is OK.
    if (staticValues == SENTINEL_NOT_YET_COMPUTED) {
      synchronized (staticFields) {
        assert PresortedComparable.isSorted(Arrays.asList(staticFields));
        DexEncodedField[] fields = staticFields;
        int length = 0;
        List<DexValue> values = new ArrayList<>(fields.length);
        for (int i = 0; i < fields.length; i++) {
          DexEncodedField field = fields[i];
          DexValue staticValue = field.getStaticValue();
          assert staticValue != null;
          values.add(staticValue);
          if (!staticValue.isDefault(field.field.type)) {
            length = i + 1;
          }
        }
        staticValues =
            length > 0
                ? new DexEncodedArray(values.subList(0, length).toArray(DexValue.EMPTY_ARRAY))
                : null;
      }
    }
  }

  public boolean isSorted() {
    return isSorted(virtualMethods)
        && isSorted(directMethods)
        && isSorted(staticFields)
        && isSorted(instanceFields);
  }

  private static <T extends KeyedDexItem<S>, S extends PresortedComparable<S>> boolean isSorted(
      T[] items) {
    synchronized (items) {
      T[] sorted = items.clone();
      Arrays.sort(sorted, Comparator.comparing(KeyedDexItem::getKey));
      return Arrays.equals(items, sorted);
    }
  }
  public DexEncodedArray getStaticValues() {
    // The sentinel value is left over for classes that actually have no fields.
    if (staticValues == SENTINEL_NOT_YET_COMPUTED) {
      assert !hasMethodsOrFields();
      return null;
    }
    return staticValues;
  }

  public void addMethod(DexEncodedMethod method) {
    if (method.accessFlags.isStatic()
        || method.accessFlags.isPrivate()
        || method.accessFlags.isConstructor()) {
      addDirectMethod(method);
    } else {
      addVirtualMethod(method);
    }
  }

  public void addVirtualMethod(DexEncodedMethod virtualMethod) {
    assert !virtualMethod.accessFlags.isStatic();
    assert !virtualMethod.accessFlags.isPrivate();
    assert !virtualMethod.accessFlags.isConstructor();
    virtualMethods = Arrays.copyOf(virtualMethods, virtualMethods.length + 1);
    virtualMethods[virtualMethods.length - 1] = virtualMethod;
  }

  public void addDirectMethod(DexEncodedMethod staticMethod) {
    assert staticMethod.accessFlags.isStatic() || staticMethod.accessFlags.isPrivate()
        || staticMethod.accessFlags.isConstructor();
    directMethods = Arrays.copyOf(directMethods, directMethods.length + 1);
    directMethods[directMethods.length - 1] = staticMethod;
  }

  public void sortMembers() {
    sortEncodedFields(staticFields);
    sortEncodedFields(instanceFields);
    sortEncodedMethods(directMethods);
    sortEncodedMethods(virtualMethods);
  }

  private void sortEncodedFields(DexEncodedField[] fields) {
    synchronized (fields) {
      Arrays.sort(fields, Comparator.comparing(a -> a.field));
    }
  }

  private void sortEncodedMethods(DexEncodedMethod[] methods) {
    synchronized (methods) {
      Arrays.sort(methods, Comparator.comparing(a -> a.method));
    }
  }

  @Override
  public DexProgramClass get() {
    return this;
  }

  public void setInitialClassFileVersion(int initialClassFileVersion) {
    assert this.initialClassFileVersion == -1 && initialClassFileVersion > 0;
    this.initialClassFileVersion = initialClassFileVersion;
  }

  public boolean hasClassFileVersion() {
    return initialClassFileVersion > -1;
  }

  public int getInitialClassFileVersion() {
    assert initialClassFileVersion > -1;
    return initialClassFileVersion;
  }

  /**
   * Is the class reachability sensitive.
   *
   * <p>A class is reachability sensitive if the
   * dalvik.annotation.optimization.ReachabilitySensitive annotation is on any field or method. When
   * that is the case, dead reference elimination is disabled and locals are kept alive for their
   * entire scope.
   */
  public boolean hasReachabilitySensitiveAnnotation(DexItemFactory factory) {
    for (DexEncodedMethod directMethod : directMethods) {
      for (DexAnnotation annotation : directMethod.annotations.annotations) {
        if (annotation.annotation.type == factory.annotationReachabilitySensitive) {
          return true;
        }
      }
    }
    for (DexEncodedMethod virtualMethod : virtualMethods) {
      for (DexAnnotation annotation : virtualMethod.annotations.annotations) {
        if (annotation.annotation.type == factory.annotationReachabilitySensitive) {
          return true;
        }
      }
    }
    for (DexEncodedField staticField : staticFields) {
      for (DexAnnotation annotation : staticField.annotations.annotations) {
        if (annotation.annotation.type == factory.annotationReachabilitySensitive) {
          return true;
        }
      }
    }
    for (DexEncodedField instanceField : instanceFields) {
      for (DexAnnotation annotation : instanceField.annotations.annotations) {
        if (annotation.annotation.type == factory.annotationReachabilitySensitive) {
          return true;
        }
      }
    }
    return false;
  }

  public static Iterable<DexProgramClass> asProgramClasses(
      Iterable<DexType> types, DexDefinitionSupplier definitions) {
    return () ->
        new Iterator<DexProgramClass>() {

          private final Iterator<DexType> iterator = types.iterator();

          private DexProgramClass next = findNext();

          @Override
          public boolean hasNext() {
            return next != null;
          }

          @Override
          public DexProgramClass next() {
            DexProgramClass current = next;
            next = findNext();
            return current;
          }

          private DexProgramClass findNext() {
            while (iterator.hasNext()) {
              DexType next = iterator.next();
              DexClass clazz = definitions.definitionFor(next);
              if (clazz != null && clazz.isProgramClass()) {
                return clazz.asProgramClass();
              }
            }
            return null;
          }
        };
  }

  public static long invalidChecksumRequest(DexProgramClass clazz) {
    throw new CompilationError(
        clazz + " has no checksum information while checksum encoding is requested", clazz.origin);
  }

  public static long checksumFromType(DexProgramClass clazz) {
    return clazz.type.hashCode();
  }

  public long getChecksum() {
    return checksumSupplier.getChecksum(this);
  }
}
