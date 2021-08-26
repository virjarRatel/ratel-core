// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.references;

import com.android.tools.r8.Keep;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Reference provider/factory.
 *
 * <p>The reference class provides a single point for creating and managing reference objects that
 * represent types, methods and fields in a JVM/DEX application. The objects are interned/shared so
 * that allocation is reduced and equality is constant time. Internally, the objects are weakly
 * stored to avoid memory pressure.
 *
 * <p>All reference objects are immutable and can be compared for equality using physical identity.
 */
@Keep
public final class Reference {

  public static PrimitiveReference BOOL = PrimitiveReference.BOOL;
  public static PrimitiveReference BYTE = PrimitiveReference.BYTE;
  public static PrimitiveReference CHAR = PrimitiveReference.CHAR;
  public static PrimitiveReference SHORT = PrimitiveReference.SHORT;
  public static PrimitiveReference INT = PrimitiveReference.INT;
  public static PrimitiveReference FLOAT = PrimitiveReference.FLOAT;
  public static PrimitiveReference LONG = PrimitiveReference.LONG;
  public static PrimitiveReference DOUBLE = PrimitiveReference.DOUBLE;

  private static Reference instance;

  // Weak map of classes. The values are weak and the descriptor is used as key.
  private final ConcurrentMap<String, ClassReference> classes =
      new MapMaker().weakValues().makeMap();

  // Weak map of arrays. The values are weak and the descriptor is used as key.
  private final ConcurrentMap<String, ArrayReference> arrays =
      new MapMaker().weakValues().makeMap();

  // Weak map of method references. Keys are strong and must not be identical to the value.
  private final ConcurrentMap<MethodReference, MethodReference> methods =
      new MapMaker().weakValues().makeMap();

  // Weak map of field references. Keys are strong and must not be identical to the value.
  private final ConcurrentMap<FieldReference, FieldReference> fields =
      new MapMaker().weakValues().makeMap();

  private Reference() {
    // Intentionally hidden.
  }

  private static Reference getInstance() {
    if (instance == null) {
      instance = new Reference();
    }
    return instance;
  }

  public static TypeReference returnTypeFromDescriptor(String descriptor) {
    return descriptor.equals("V") ? null : typeFromDescriptor(descriptor);
  }

  public static TypeReference typeFromDescriptor(String descriptor) {
    switch (descriptor.charAt(0)) {
      case 'L':
        return classFromDescriptor(descriptor);
      case '[':
        return arrayFromDescriptor(descriptor);
      default:
        return primitiveFromDescriptor(descriptor);
    }
  }

  public static TypeReference typeFromTypeName(String typeName) {
    return typeFromDescriptor(DescriptorUtils.javaTypeToDescriptor(typeName));
  }

  // Internal helper to convert Class<?> for primitive/array types too.
  private static TypeReference typeFromClass(Class<?> clazz) {
    return typeFromDescriptor(DescriptorUtils.javaTypeToDescriptor(clazz.getTypeName()));
  }

  public static PrimitiveReference primitiveFromDescriptor(String descriptor) {
    return PrimitiveReference.fromDescriptor(descriptor);
  }

  /** Get a class reference from a JVM descriptor. */
  public static ClassReference classFromDescriptor(String descriptor) {
    return getInstance().classes.computeIfAbsent(descriptor, ClassReference::fromDescriptor);
  }

  /**
   * Get a class reference from a JVM binary name.
   *
   * <p>See JVM SE 9 Specification, Section 4.2.1. Binary Class and Interface Names.
   */
  public static ClassReference classFromBinaryName(String binaryName) {
    return classFromDescriptor(DescriptorUtils.getDescriptorFromClassBinaryName(binaryName));
  }

  /**
   * Get a class reference from a Java type name.
   *
   * <p>See Class.getTypeName() from Java 1.8.
   */
  public static ClassReference classFromTypeName(String typeName) {
    return classFromDescriptor(DescriptorUtils.javaTypeToDescriptor(typeName));
  }

  /** Get a class reference from a Java java.lang.Class object. */
  public static ClassReference classFromClass(Class<?> clazz) {
    return classFromTypeName(clazz.getTypeName());
  }

  /** Get an array reference from a JVM descriptor. */
  public static ArrayReference arrayFromDescriptor(String descriptor) {
    return getInstance().arrays.computeIfAbsent(descriptor, ArrayReference::fromDescriptor);
  }

  /** Get an array reference from a base type and dimensions. */
  public static ArrayReference array(TypeReference baseType, int dimensions) {
    String arrayDescriptor =
        DescriptorUtils.toArrayDescriptor(dimensions, baseType.getDescriptor());
    return getInstance()
        .arrays
        .computeIfAbsent(
            arrayDescriptor, descriptor -> ArrayReference.fromBaseType(baseType, dimensions));
  }

  /** Get a method reference from its full reference specification. */
  public static MethodReference method(
      ClassReference holderClass,
      String methodName,
      List<TypeReference> formalTypes,
      TypeReference returnType) {
    MethodReference key = new MethodReference(
        holderClass, methodName, ImmutableList.copyOf(formalTypes), returnType);
    return getInstance().methods.computeIfAbsent(key,
        // Allocate a distinct value for the canonical reference so the key is not a strong pointer.
        k -> new MethodReference(
            k.getHolderClass(),
            k.getMethodName(),
            ImmutableList.copyOf(k.getFormalTypes()),
            k.getReturnType()));
  }

  /** Get a method reference from a Java reflection method. */
  public static MethodReference methodFromMethod(Method method) {
    String methodName = method.getName();
    Class<?> holderClass = method.getDeclaringClass();
    Class<?>[] parameterTypes = method.getParameterTypes();
    Class<?> returnType = method.getReturnType();
    ImmutableList.Builder<TypeReference> builder = ImmutableList.builder();
    for (Class<?> parameterType : parameterTypes) {
      builder.add(typeFromClass(parameterType));
    }
    return method(
        classFromClass(holderClass),
        methodName,
        builder.build(),
        returnType == Void.TYPE ? null : typeFromClass(returnType));
  }

  /** Get a method reference from a Java reflection constructor. */
  public static MethodReference methodFromMethod(Constructor<?> method) {
    Class<?> holderClass = method.getDeclaringClass();
    Class<?>[] parameterTypes = method.getParameterTypes();
    ImmutableList.Builder<TypeReference> builder = ImmutableList.builder();
    for (Class<?> parameterType : parameterTypes) {
      builder.add(typeFromClass(parameterType));
    }
    return method(classFromClass(holderClass), "<init>", builder.build(), null);
  }

  public static MethodReference classConstructor(ClassReference type) {
    return method(type, "<clinit>", Collections.emptyList(), null);
  }

  /** Get a field reference from its full reference specification. */
  public static FieldReference field(
      ClassReference holderClass, String fieldName, TypeReference fieldType) {
    FieldReference key = new FieldReference(holderClass, fieldName, fieldType);
    return getInstance().fields.computeIfAbsent(key,
        // Allocate a distinct value for the canonical reference so the key is not a strong pointer.
        k -> new FieldReference(k.getHolderClass(), k.getFieldName(), k.getFieldType()));
  }

  /** Get a field reference from a Java reflection field. */
  public static FieldReference fieldFromField(Field field) {
    Class<?> holderClass = field.getDeclaringClass();
    String fieldName = field.getName();
    Class<?> fieldType = field.getType();
    return field(classFromClass(holderClass), fieldName, typeFromClass(fieldType));
  }
}
