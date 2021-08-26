// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.kotlin.KotlinInfo;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OptionalBool;
import com.android.tools.r8.utils.PredicateUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class DexClass extends DexDefinition {

  public interface FieldSetter {
    void setField(int index, DexEncodedField field);
  }

  public interface MethodSetter {
    void setMethod(int index, DexEncodedMethod method);
  }

  private Optional<DexEncodedMethod> cachedClassInitializer = null;

  public final Origin origin;
  public DexType type;
  public final ClassAccessFlags accessFlags;
  public DexType superType;
  public DexTypeList interfaces;
  public DexString sourceFile;

  private OptionalBool isResolvable = OptionalBool.unknown();

  /** Access has to be synchronized during concurrent collection/writing phase. */
  protected DexEncodedField[] staticFields = DexEncodedField.EMPTY_ARRAY;

  /** Access has to be synchronized during concurrent collection/writing phase. */
  protected DexEncodedField[] instanceFields = DexEncodedField.EMPTY_ARRAY;

  /** Access has to be synchronized during concurrent collection/writing phase. */
  protected DexEncodedMethod[] directMethods = DexEncodedMethod.EMPTY_ARRAY;

  /** Access has to be synchronized during concurrent collection/writing phase. */
  protected DexEncodedMethod[] virtualMethods = DexEncodedMethod.EMPTY_ARRAY;

  /** Enclosing context of this class if it is an inner class, null otherwise. */
  private EnclosingMethodAttribute enclosingMethod;

  /** InnerClasses table. If this class is an inner class, it will have an entry here. */
  private final List<InnerClassAttribute> innerClasses;

  private NestHostClassAttribute nestHost;
  private final List<NestMemberClassAttribute> nestMembers;

  public DexAnnotationSet annotations;

  public DexClass(
      DexString sourceFile,
      DexTypeList interfaces,
      ClassAccessFlags accessFlags,
      DexType superType,
      DexType type,
      DexEncodedField[] staticFields,
      DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods,
      DexEncodedMethod[] virtualMethods,
      NestHostClassAttribute nestHost,
      List<NestMemberClassAttribute> nestMembers,
      EnclosingMethodAttribute enclosingMethod,
      List<InnerClassAttribute> innerClasses,
      DexAnnotationSet annotations,
      Origin origin,
      boolean skipNameValidationForTesting) {
    assert origin != null;
    this.origin = origin;
    this.sourceFile = sourceFile;
    this.interfaces = interfaces;
    this.accessFlags = accessFlags;
    this.superType = superType;
    this.type = type;
    setStaticFields(staticFields);
    setInstanceFields(instanceFields);
    setDirectMethods(directMethods);
    setVirtualMethods(virtualMethods);
    this.nestHost = nestHost;
    this.nestMembers = nestMembers;
    assert nestMembers != null;
    this.enclosingMethod = enclosingMethod;
    this.innerClasses = innerClasses;
    this.annotations = annotations;
    if (type == superType) {
      throw new CompilationError("Class " + type.toString() + " cannot extend itself");
    }
    for (DexType interfaceType : interfaces.values) {
      if (type == interfaceType) {
        throw new CompilationError("Interface " + type.toString() + " cannot implement itself");
      }
    }
    if (!skipNameValidationForTesting && !type.descriptor.isValidClassDescriptor()) {
      throw new CompilationError(
          "Class descriptor '"
              + type.descriptor.toString()
              + "' cannot be represented in dex format.");
    }
  }

  public Iterable<DexEncodedField> fields() {
    return fields(Predicates.alwaysTrue());
  }

  public Iterable<DexEncodedField> fields(final Predicate<? super DexEncodedField> predicate) {
    return Iterables.concat(
        Iterables.filter(Arrays.asList(instanceFields), predicate::test),
        Iterables.filter(Arrays.asList(staticFields), predicate::test));
  }

  public Iterable<DexEncodedMethod> methods() {
    return methods(Predicates.alwaysTrue());
  }

  public Iterable<DexEncodedMethod> methods(Predicate<? super DexEncodedMethod> predicate) {
    return Iterables.concat(
        Iterables.filter(Arrays.asList(directMethods), predicate::test),
        Iterables.filter(Arrays.asList(virtualMethods), predicate::test));
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    throw new Unreachable();
  }

  public List<DexEncodedMethod> directMethods() {
    assert directMethods != null;
    if (InternalOptions.assertionsEnabled()) {
      return Collections.unmodifiableList(Arrays.asList(directMethods));
    }
    return Arrays.asList(directMethods);
  }

  public void appendDirectMethod(DexEncodedMethod method) {
    cachedClassInitializer = null;
    DexEncodedMethod[] newMethods = new DexEncodedMethod[directMethods.length + 1];
    System.arraycopy(directMethods, 0, newMethods, 0, directMethods.length);
    newMethods[directMethods.length] = method;
    directMethods = newMethods;
    assert verifyCorrectnessOfMethodHolder(method);
    assert verifyNoDuplicateMethods();
  }

  public void appendDirectMethods(Collection<DexEncodedMethod> methods) {
    cachedClassInitializer = null;
    DexEncodedMethod[] newMethods = new DexEncodedMethod[directMethods.length + methods.size()];
    System.arraycopy(directMethods, 0, newMethods, 0, directMethods.length);
    int i = directMethods.length;
    for (DexEncodedMethod method : methods) {
      newMethods[i] = method;
      i++;
    }
    directMethods = newMethods;
    assert verifyCorrectnessOfMethodHolders(methods);
    assert verifyNoDuplicateMethods();
  }

  public void removeDirectMethod(int index) {
    cachedClassInitializer = null;
    DexEncodedMethod[] newMethods = new DexEncodedMethod[directMethods.length - 1];
    System.arraycopy(directMethods, 0, newMethods, 0, index);
    System.arraycopy(directMethods, index + 1, newMethods, index, directMethods.length - index - 1);
    directMethods = newMethods;
  }

  public void setDirectMethod(int index, DexEncodedMethod method) {
    cachedClassInitializer = null;
    directMethods[index] = method;
    assert verifyCorrectnessOfMethodHolder(method);
    assert verifyNoDuplicateMethods();
  }

  public void setDirectMethods(DexEncodedMethod[] methods) {
    cachedClassInitializer = null;
    directMethods = MoreObjects.firstNonNull(methods, DexEncodedMethod.EMPTY_ARRAY);
    assert verifyCorrectnessOfMethodHolders(directMethods());
    assert verifyNoDuplicateMethods();
  }

  public List<DexEncodedMethod> virtualMethods() {
    assert virtualMethods != null;
    if (InternalOptions.assertionsEnabled()) {
      return Collections.unmodifiableList(Arrays.asList(virtualMethods));
    }
    return Arrays.asList(virtualMethods);
  }

  public void appendVirtualMethod(DexEncodedMethod method) {
    DexEncodedMethod[] newMethods = new DexEncodedMethod[virtualMethods.length + 1];
    System.arraycopy(virtualMethods, 0, newMethods, 0, virtualMethods.length);
    newMethods[virtualMethods.length] = method;
    virtualMethods = newMethods;
    assert verifyCorrectnessOfMethodHolder(method);
    assert verifyNoDuplicateMethods();
  }

  public void appendVirtualMethods(Collection<DexEncodedMethod> methods) {
    DexEncodedMethod[] newMethods = new DexEncodedMethod[virtualMethods.length + methods.size()];
    System.arraycopy(virtualMethods, 0, newMethods, 0, virtualMethods.length);
    int i = virtualMethods.length;
    for (DexEncodedMethod method : methods) {
      newMethods[i] = method;
      i++;
    }
    virtualMethods = newMethods;
    assert verifyCorrectnessOfMethodHolders(methods);
    assert verifyNoDuplicateMethods();
  }

  public void removeVirtualMethod(int index) {
    DexEncodedMethod[] newMethods = new DexEncodedMethod[virtualMethods.length - 1];
    System.arraycopy(virtualMethods, 0, newMethods, 0, index);
    System.arraycopy(
        virtualMethods, index + 1, newMethods, index, virtualMethods.length - index - 1);
    virtualMethods = newMethods;
  }

  public void setVirtualMethod(int index, DexEncodedMethod method) {
    virtualMethods[index] = method;
    assert verifyCorrectnessOfMethodHolder(method);
    assert verifyNoDuplicateMethods();
  }

  public void setVirtualMethods(DexEncodedMethod[] methods) {
    virtualMethods = MoreObjects.firstNonNull(methods, DexEncodedMethod.EMPTY_ARRAY);
    assert verifyCorrectnessOfMethodHolders(virtualMethods());
    assert verifyNoDuplicateMethods();
  }

  private boolean verifyCorrectnessOfMethodHolder(DexEncodedMethod method) {
    assert method.method.holder == type
        : "Expected method `"
            + method.method.toSourceString()
            + "` to have holder `"
            + type.toSourceString()
            + "`";
    return true;
  }

  private boolean verifyCorrectnessOfMethodHolders(Iterable<DexEncodedMethod> methods) {
    for (DexEncodedMethod method : methods) {
      assert verifyCorrectnessOfMethodHolder(method);
    }
    return true;
  }

  private boolean verifyNoAbstractMethodsOnNonAbstractClasses(
      Iterable<DexEncodedMethod> methods, InternalOptions options) {
    if (options.canHaveDalvikAbstractMethodOnNonAbstractClassVerificationBug()) {
      if (!isAbstract()) {
        for (DexEncodedMethod method : methods) {
          assert !method.isAbstract()
              : "Non-abstract method on abstract class: `" + method.method.toSourceString() + "`";
        }
      }
    }
    return true;
  }

  private boolean verifyNoDuplicateMethods() {
    Set<DexMethod> unique = Sets.newIdentityHashSet();
    for (DexEncodedMethod method : methods()) {
      boolean changed = unique.add(method.method);
      assert changed : "Duplicate method `" + method.method.toSourceString() + "`";
    }
    return true;
  }

  public void forEachMethod(Consumer<DexEncodedMethod> consumer) {
    for (DexEncodedMethod method : directMethods()) {
      consumer.accept(method);
    }
    for (DexEncodedMethod method : virtualMethods()) {
      consumer.accept(method);
    }
  }

  public DexEncodedMethod[] allMethodsSorted() {
    int vLen = virtualMethods.length;
    int dLen = directMethods.length;
    DexEncodedMethod[] result = new DexEncodedMethod[vLen + dLen];
    System.arraycopy(virtualMethods, 0, result, 0, vLen);
    System.arraycopy(directMethods, 0, result, vLen, dLen);
    Arrays.sort(result,
        (DexEncodedMethod a, DexEncodedMethod b) -> a.method.slowCompareTo(b.method));
    return result;
  }

  public DexEncodedMethod[] directMethodsSorted() {
    DexEncodedMethod[] result = new DexEncodedMethod[directMethods.length];
    System.arraycopy(directMethods, 0, result, 0, directMethods.length);
    Arrays.sort(
        result, (DexEncodedMethod a, DexEncodedMethod b) -> a.method.slowCompareTo(b.method));
    return result;
  }

  public DexEncodedMethod[] virtualMethodsSorted() {
    DexEncodedMethod[] result = new DexEncodedMethod[virtualMethods.length];
    System.arraycopy(virtualMethods, 0, result, 0, virtualMethods.length);
    Arrays.sort(
        result, (DexEncodedMethod a, DexEncodedMethod b) -> a.method.slowCompareTo(b.method));
    return result;
  }

  public void virtualizeMethods(Set<DexEncodedMethod> privateInstanceMethods) {
    int vLen = virtualMethods.length;
    int dLen = directMethods.length;
    int mLen = privateInstanceMethods.size();
    assert mLen <= dLen;

    DexEncodedMethod[] newDirectMethods = new DexEncodedMethod[dLen - mLen];
    int index = 0;
    for (int i = 0; i < dLen; i++) {
      DexEncodedMethod encodedMethod = directMethods[i];
      if (!privateInstanceMethods.contains(encodedMethod)) {
        newDirectMethods[index++] = encodedMethod;
      }
    }
    assert index == dLen - mLen;
    setDirectMethods(newDirectMethods);

    DexEncodedMethod[] newVirtualMethods = new DexEncodedMethod[vLen + mLen];
    System.arraycopy(virtualMethods, 0, newVirtualMethods, 0, vLen);
    index = vLen;
    for (DexEncodedMethod encodedMethod : privateInstanceMethods) {
      newVirtualMethods[index++] = encodedMethod;
    }
    setVirtualMethods(newVirtualMethods);
  }

  /**
   * For all annotations on the class and all annotations on its methods and fields apply the
   * specified consumer.
   */
  public void forEachAnnotation(Consumer<DexAnnotation> consumer) {
    for (DexAnnotation annotation : annotations.annotations) {
      consumer.accept(annotation);
    }
    for (DexEncodedMethod method : directMethods()) {
      for (DexAnnotation annotation : method.annotations.annotations) {
        consumer.accept(annotation);
      }
      method.parameterAnnotationsList.forEachAnnotation(consumer);
    }
    for (DexEncodedMethod method : virtualMethods()) {
      for (DexAnnotation annotation : method.annotations.annotations) {
        consumer.accept(annotation);
      }
      method.parameterAnnotationsList.forEachAnnotation(consumer);
    }
    for (DexEncodedField field : instanceFields()) {
      for (DexAnnotation annotation : field.annotations.annotations) {
        consumer.accept(annotation);
      }
    }
    for (DexEncodedField field : staticFields()) {
      for (DexAnnotation annotation : field.annotations.annotations) {
        consumer.accept(annotation);
      }
    }
  }

  public void forEachField(Consumer<DexEncodedField> consumer) {
    for (DexEncodedField field : staticFields()) {
      consumer.accept(field);
    }
    for (DexEncodedField field : instanceFields()) {
      consumer.accept(field);
    }
  }

  public List<DexEncodedField> staticFields() {
    assert staticFields != null;
    if (InternalOptions.assertionsEnabled()) {
      return Collections.unmodifiableList(Arrays.asList(staticFields));
    }
    return Arrays.asList(staticFields);
  }

  public void appendStaticField(DexEncodedField field) {
    DexEncodedField[] newFields = new DexEncodedField[staticFields.length + 1];
    System.arraycopy(staticFields, 0, newFields, 0, staticFields.length);
    newFields[staticFields.length] = field;
    staticFields = newFields;
    assert verifyCorrectnessOfFieldHolder(field);
    assert verifyNoDuplicateFields();
  }

  public void appendStaticFields(Collection<DexEncodedField> fields) {
    DexEncodedField[] newFields = new DexEncodedField[staticFields.length + fields.size()];
    System.arraycopy(staticFields, 0, newFields, 0, staticFields.length);
    int i = staticFields.length;
    for (DexEncodedField field : fields) {
      newFields[i] = field;
      i++;
    }
    staticFields = newFields;
    assert verifyCorrectnessOfFieldHolders(fields);
    assert verifyNoDuplicateFields();
  }

  public void removeStaticField(int index) {
    DexEncodedField[] newFields = new DexEncodedField[staticFields.length - 1];
    System.arraycopy(staticFields, 0, newFields, 0, index);
    System.arraycopy(staticFields, index + 1, newFields, index, staticFields.length - index - 1);
    staticFields = newFields;
  }

  public void setStaticField(int index, DexEncodedField field) {
    staticFields[index] = field;
    assert verifyCorrectnessOfFieldHolder(field);
    assert verifyNoDuplicateFields();
  }

  public void setStaticFields(DexEncodedField[] fields) {
    staticFields = MoreObjects.firstNonNull(fields, DexEncodedField.EMPTY_ARRAY);
    assert verifyCorrectnessOfFieldHolders(staticFields());
    assert verifyNoDuplicateFields();
  }

  public boolean definesStaticField(DexField field) {
    for (DexEncodedField encodedField : staticFields()) {
      if (encodedField.field == field) {
        return true;
      }
    }
    return false;
  }

  public List<DexEncodedField> instanceFields() {
    assert instanceFields != null;
    if (InternalOptions.assertionsEnabled()) {
      return Collections.unmodifiableList(Arrays.asList(instanceFields));
    }
    return Arrays.asList(instanceFields);
  }

  public void appendInstanceField(DexEncodedField field) {
    DexEncodedField[] newFields = new DexEncodedField[instanceFields.length + 1];
    System.arraycopy(instanceFields, 0, newFields, 0, instanceFields.length);
    newFields[instanceFields.length] = field;
    instanceFields = newFields;
    assert verifyCorrectnessOfFieldHolder(field);
    assert verifyNoDuplicateFields();
  }

  public void appendInstanceFields(Collection<DexEncodedField> fields) {
    DexEncodedField[] newFields = new DexEncodedField[instanceFields.length + fields.size()];
    System.arraycopy(instanceFields, 0, newFields, 0, instanceFields.length);
    int i = instanceFields.length;
    for (DexEncodedField field : fields) {
      newFields[i] = field;
      i++;
    }
    instanceFields = newFields;
    assert verifyCorrectnessOfFieldHolders(fields);
    assert verifyNoDuplicateFields();
  }

  public void removeInstanceField(int index) {
    DexEncodedField[] newFields = new DexEncodedField[instanceFields.length - 1];
    System.arraycopy(instanceFields, 0, newFields, 0, index);
    System.arraycopy(
        instanceFields, index + 1, newFields, index, instanceFields.length - index - 1);
    instanceFields = newFields;
  }

  public void setInstanceField(int index, DexEncodedField field) {
    instanceFields[index] = field;
    assert verifyCorrectnessOfFieldHolder(field);
    assert verifyNoDuplicateFields();
  }

  public void setInstanceFields(DexEncodedField[] fields) {
    instanceFields = MoreObjects.firstNonNull(fields, DexEncodedField.EMPTY_ARRAY);
    assert verifyCorrectnessOfFieldHolders(instanceFields());
    assert verifyNoDuplicateFields();
  }

  private boolean verifyCorrectnessOfFieldHolder(DexEncodedField field) {
    assert field.field.holder == type
        : "Expected field `"
            + field.field.toSourceString()
            + "` to have holder `"
            + type.toSourceString()
            + "`";
    return true;
  }

  private boolean verifyCorrectnessOfFieldHolders(Iterable<DexEncodedField> fields) {
    for (DexEncodedField field : fields) {
      assert verifyCorrectnessOfFieldHolder(field);
    }
    return true;
  }

  private boolean verifyNoDuplicateFields() {
    Set<DexField> unique = Sets.newIdentityHashSet();
    for (DexEncodedField field : fields()) {
      boolean changed = unique.add(field.field);
      assert changed : "Duplicate field `" + field.field.toSourceString() + "`";
    }
    return true;
  }

  /** Find static field in this class matching {@param field}. */
  public DexEncodedField lookupStaticField(DexField field) {
    return lookupTarget(staticFields, field);
  }

  /** Find instance field in this class matching {@param field}. */
  public DexEncodedField lookupInstanceField(DexField field) {
    return lookupTarget(instanceFields, field);
  }

  public DexField lookupUniqueInstanceFieldWithName(DexString name) {
    DexField field = null;
    for (DexEncodedField encodedField : instanceFields()) {
      if (encodedField.field.name == name) {
        if (field != null) {
          return null;
        }
        field = encodedField.field;
      }
    }
    return field;
  }

  /** Find field in this class matching {@param field}. */
  public DexEncodedField lookupField(DexField field) {
    DexEncodedField result = lookupInstanceField(field);
    return result == null ? lookupStaticField(field) : result;
  }

  /** Find direct method in this class matching {@param method}. */
  public DexEncodedMethod lookupDirectMethod(DexMethod method) {
    return lookupTarget(directMethods, method);
  }

  /** Find direct method in this class matching {@param predicate}. */
  public DexEncodedMethod lookupDirectMethod(Predicate<DexEncodedMethod> predicate) {
    return PredicateUtils.findFirst(directMethods, predicate);
  }

  /** Find virtual method in this class matching {@param method}. */
  public DexEncodedMethod lookupVirtualMethod(DexMethod method) {
    return lookupTarget(virtualMethods, method);
  }

  /** Find virtual method in this class matching {@param predicate}. */
  public DexEncodedMethod lookupVirtualMethod(Predicate<DexEncodedMethod> predicate) {
    return PredicateUtils.findFirst(virtualMethods, predicate);
  }

  /** Find method in this class matching {@param method}. */
  public DexEncodedMethod lookupMethod(DexMethod method) {
    DexEncodedMethod result = lookupDirectMethod(method);
    return result == null ? lookupVirtualMethod(method) : result;
  }

  public DexEncodedMethod lookupSignaturePolymorphicMethod(
      DexString methodName, DexItemFactory factory) {
    if (type != factory.methodHandleType && type != factory.varHandleType) {
      return null;
    }
    DexEncodedMethod matchingName = null;
    DexEncodedMethod signaturePolymorphicMethod = null;
    for (DexEncodedMethod method : virtualMethods) {
      if (method.method.name == methodName) {
        if (matchingName != null) {
          // The jvm spec, section 5.4.3.3 details that there must be exactly one method with the
          // given name only.
          return null;
        }
        matchingName = method;
        if (isSignaturePolymorphicMethod(method, factory)) {
          signaturePolymorphicMethod = method;
        }
      }
    }
    return signaturePolymorphicMethod;
  }

  private boolean isSignaturePolymorphicMethod(DexEncodedMethod method, DexItemFactory factory) {
    assert method.method.holder == factory.methodHandleType
        || method.method.holder == factory.varHandleType;
    return method.accessFlags.isVarargs()
        && method.accessFlags.isNative()
        && method.method.proto.parameters.size() == 1
        && method.method.proto.parameters.values[0] != factory.objectArrayType;
  }

  private <T extends DexItem, S extends Descriptor<T, S>> T lookupTarget(T[] items, S descriptor) {
    for (T entry : items) {
      if (descriptor.match(entry)) {
        return entry;
      }
    }
    return null;
  }

  public boolean isAbstract() {
    return accessFlags.isAbstract();
  }

  public boolean isAnnotation() {
    return accessFlags.isAnnotation();
  }

  public boolean isFinal() {
    return accessFlags.isFinal();
  }

  public boolean isEffectivelyFinal(AppView<?> appView) {
    return isFinal();
  }

  public boolean isInterface() {
    return accessFlags.isInterface();
  }

  public boolean isEnum() {
    return accessFlags.isEnum();
  }

  public abstract void addDependencies(MixedSectionCollection collector);

  @Override
  public DexReference toReference() {
    return getType();
  }

  @Override
  public boolean isDexClass() {
    return true;
  }

  @Override
  public DexClass asDexClass() {
    return this;
  }

  public boolean isProgramClass() {
    return false;
  }

  public DexProgramClass asProgramClass() {
    return null;
  }

  public boolean isClasspathClass() {
    return false;
  }

  public DexClasspathClass asClasspathClass() {
    return null;
  }

  public abstract boolean isNotProgramClass();

  public boolean isLibraryClass() {
    return false;
  }

  public DexLibraryClass asLibraryClass() {
    return null;
  }

  @Override
  public boolean isStatic() {
    return accessFlags.isStatic();
  }

  @Override
  public boolean isStaticMember() {
    return false;
  }

  public synchronized DexEncodedMethod getClassInitializer() {
    if (cachedClassInitializer == null) {
      cachedClassInitializer = Optional.empty();
      for (DexEncodedMethod directMethod : directMethods) {
        if (directMethod.isClassInitializer()) {
          cachedClassInitializer = Optional.of(directMethod);
          break;
        }
      }
    }
    return cachedClassInitializer.orElse(null);
  }

  public Origin getOrigin() {
    return this.origin;
  }

  public DexType getType() {
    return this.type;
  }

  public boolean hasClassInitializer() {
    return getClassInitializer() != null;
  }

  public boolean hasDefaultInitializer() {
    return getDefaultInitializer() != null;
  }

  public DexEncodedMethod getInitializer(DexType[] parameters) {
    for (DexEncodedMethod method : directMethods()) {
      if (method.isInstanceInitializer()
          && Arrays.equals(method.method.proto.parameters.values, parameters)) {
        return method;
      }
    }
    return null;
  }

  public DexEncodedMethod getDefaultInitializer() {
    return getInitializer(DexType.EMPTY_ARRAY);
  }

  public boolean hasMissingSuperType(AppInfoWithSubtyping appInfo) {
    if (superType != null && appInfo.isMissingOrHasMissingSuperType(superType)) {
      return true;
    }
    for (DexType interfaceType : interfaces.values) {
      if (appInfo.isMissingOrHasMissingSuperType(interfaceType)) {
        return true;
      }
    }
    return false;
  }

  public boolean isResolvable(AppView<?> appView) {
    if (isResolvable.isUnknown()) {
      boolean resolvable;
      if (!isProgramClass()) {
        resolvable = appView.dexItemFactory().libraryTypesAssumedToBePresent.contains(type);
      } else {
        resolvable = true;
        for (DexType supertype : allImmediateSupertypes()) {
          resolvable &= supertype.isResolvable(appView);
          if (!resolvable) {
            break;
          }
        }
      }
      isResolvable = OptionalBool.of(resolvable);
    }
    assert !isResolvable.isUnknown();
    return isResolvable.isTrue();
  }

  public boolean isSerializable(AppView<? extends AppInfoWithSubtyping> appView) {
    return appView.appInfo().isSerializable(type);
  }

  public boolean isExternalizable(AppView<? extends AppInfoWithSubtyping> appView) {
    return appView.appInfo().isExternalizable(type);
  }

  public boolean classInitializationMayHaveSideEffects(AppView<?> appView) {
    return classInitializationMayHaveSideEffects(appView, Predicates.alwaysFalse());
  }

  public boolean classInitializationMayHaveSideEffects(
      AppView<?> appView, Predicate<DexType> ignore) {
    if (ignore.test(type)) {
      return false;
    }
    if (isLibraryClass()) {
      if (isInterface()) {
        return appView.options().libraryInterfacesMayHaveStaticInitialization;
      }
      if (appView.dexItemFactory().libraryClassesWithoutStaticInitialization.contains(type)) {
        return false;
      }
    }
    if (hasClassInitializerThatCannotBePostponed()) {
      return true;
    }
    if (defaultValuesForStaticFieldsMayTriggerAllocation()) {
      return true;
    }
    return initializationOfParentTypesMayHaveSideEffects(appView, ignore);
  }

  private boolean hasClassInitializerThatCannotBePostponed() {
    if (isLibraryClass()) {
      // We don't know for library classes in general but assume that java.lang.Object is safe.
      return superType != null;
    }
    DexEncodedMethod clinit = getClassInitializer();
    if (clinit == null || clinit.getCode() == null) {
      return false;
    }
    return !clinit.getOptimizationInfo().classInitializerMayBePostponed();
  }

  public Iterable<DexType> allImmediateSupertypes() {
    Iterator<DexType> iterator =
        superType != null
            ? Iterators.concat(
                Iterators.singletonIterator(superType), Iterators.forArray(interfaces.values))
            : Iterators.forArray(interfaces.values);
    return () -> iterator;
  }

  public boolean initializationOfParentTypesMayHaveSideEffects(AppView<?> appView) {
    return initializationOfParentTypesMayHaveSideEffects(appView, Predicates.alwaysFalse());
  }

  public boolean initializationOfParentTypesMayHaveSideEffects(
      AppView<?> appView, Predicate<DexType> ignore) {
    for (DexType iface : interfaces.values) {
      if (iface.classInitializationMayHaveSideEffects(appView, ignore)) {
        return true;
      }
    }
    if (superType != null && superType.classInitializationMayHaveSideEffects(appView, ignore)) {
      return true;
    }
    return false;
  }

  public boolean definesFinalizer(DexItemFactory factory) {
    return lookupVirtualMethod(factory.objectMethods.finalize) != null;
  }

  public boolean defaultValuesForStaticFieldsMayTriggerAllocation() {
    return staticFields().stream()
        .anyMatch(field -> field.getStaticValue().mayHaveSideEffects());
  }

  public List<InnerClassAttribute> getInnerClasses() {
    return innerClasses;
  }

  public EnclosingMethodAttribute getEnclosingMethod() {
    return enclosingMethod;
  }

  public void clearEnclosingMethod() {
    enclosingMethod = null;
  }

  public void removeEnclosingMethod(Predicate<EnclosingMethodAttribute> predicate) {
    if (enclosingMethod != null && predicate.test(enclosingMethod)) {
      enclosingMethod = null;
    }
  }

  public void clearInnerClasses() {
    innerClasses.clear();
  }

  public void removeInnerClasses(Predicate<InnerClassAttribute> predicate) {
    innerClasses.removeIf(predicate::test);
  }

  public InnerClassAttribute getInnerClassAttributeForThisClass() {
    for (InnerClassAttribute innerClassAttribute : getInnerClasses()) {
      if (type == innerClassAttribute.getInner()) {
        return innerClassAttribute;
      }
    }
    return null;
  }

  public void replaceInnerClassAttributeForThisClass(InnerClassAttribute newInnerClassAttribute) {
    ListIterator<InnerClassAttribute> iterator = getInnerClasses().listIterator();
    while (iterator.hasNext()) {
      InnerClassAttribute innerClassAttribute = iterator.next();
      if (type == innerClassAttribute.getInner()) {
        iterator.set(newInnerClassAttribute);
        return;
      }
    }
    throw new Unreachable();
  }

  public boolean isLocalClass() {
    InnerClassAttribute innerClass = getInnerClassAttributeForThisClass();
    // The corresponding enclosing-method attribute might be not available, e.g., CF version 50.
    return innerClass != null && innerClass.getOuter() == null && innerClass.isNamed();
  }

  public boolean isMemberClass() {
    InnerClassAttribute innerClass = getInnerClassAttributeForThisClass();
    boolean isMember = innerClass != null && innerClass.getOuter() != null && innerClass.isNamed();
    assert !isMember || getEnclosingMethod() == null;
    return isMember;
  }

  public boolean isAnonymousClass() {
    InnerClassAttribute innerClass = getInnerClassAttributeForThisClass();
    // The corresponding enclosing-method attribute might be not available, e.g., CF version 50.
    // We can't rely on outer type either because it's not null prior to 51 and null since 51.
    return innerClass != null && innerClass.isAnonymous();
  }

  public boolean isInANest() {
    return isNestHost() || isNestMember();
  }

  public void clearNestHost() {
    nestHost = null;
  }

  public void setNestHost(DexType type) {
    assert type != null;
    this.nestHost = new NestHostClassAttribute(type);
  }

  public boolean isNestHost() {
    return !nestMembers.isEmpty();
  }

  public boolean isNestMember() {
    return nestHost != null;
  }

  public DexType getNestHost() {
    if (isNestMember()) {
      return nestHost.getNestHost();
    }
    if (isNestHost()) {
      return type;
    }
    return null;
  }

  public NestHostClassAttribute getNestHostClassAttribute() {
    return nestHost;
  }

  public List<NestMemberClassAttribute> getNestMembersClassAttributes() {
    return nestMembers;
  }

  /** Returns kotlin class info if the class is synthesized by kotlin compiler. */
  public abstract KotlinInfo getKotlinInfo();

  public final boolean hasKotlinInfo() {
    return getKotlinInfo() != null;
  }

  public boolean hasInstanceFields() {
    return instanceFields.length > 0;
  }

  public boolean hasInstanceFieldsDirectlyOrIndirectly(AppView<?> appView) {
    if (superType == null || type == appView.dexItemFactory().objectType) {
      return false;
    }
    if (hasInstanceFields()) {
      return true;
    }
    DexClass superClass = appView.definitionFor(superType);
    return superClass == null || superClass.hasInstanceFieldsDirectlyOrIndirectly(appView);
  }

  public boolean isValid(InternalOptions options) {
    assert verifyNoAbstractMethodsOnNonAbstractClasses(virtualMethods(), options);
    assert !isInterface() || virtualMethods().stream().noneMatch(DexEncodedMethod::isFinal);
    assert verifyCorrectnessOfFieldHolders(fields());
    assert verifyNoDuplicateFields();
    assert verifyCorrectnessOfMethodHolders(methods());
    assert verifyNoDuplicateMethods();
    return true;
  }

  public boolean hasStaticSynchronizedMethods() {
    for (DexEncodedMethod encodedMethod : directMethods()) {
      if (encodedMethod.accessFlags.isStatic() && encodedMethod.accessFlags.isSynchronized()) {
        return true;
      }
    }
    return false;
  }
}
