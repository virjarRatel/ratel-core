// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.references;

import com.android.tools.r8.Keep;
import com.android.tools.r8.utils.DescriptorUtils;

@Keep
public interface TypeReference {

  /**
   * Get the JVM descriptor for this type.
   *
   * @return The descriptor for the type.
   */
  String getDescriptor();

  /** Predicate that is true iff the TypeReference is an instance of ClassTypeReference. */
  default boolean isClass() {
    return false;
  }

  /** Predicate that is true iff the TypeReference is an instance of ArrayTypeReference. */
  default boolean isArray() {
    return false;
  }

  /** Predicate that is true iff the TypeReference is an instance of PrimitiveTypeReference. */
  default boolean isPrimitive() {
    return false;
  }

  /**
   * Return a non-null ClassTypeReference if this type is ClassTypeReference
   *
   * @return this with static type of ClassTypeReference
   */
  default ClassReference asClass() {
    return null;
  }

  /**
   * Return a non-null ArrayReference if this type is ArrayReference
   *
   * @return this with static type of ArrayReference
   */
  default ArrayReference asArray() {
    return null;
  }

  /**
   * Return a non-null PrimitiveReference if this type is PrimitiveReference
   *
   * @return this with static type of PrimitiveReference
   */
  default PrimitiveReference asPrimitive() {
    return null;
  }

  default String getTypeName() {
    return DescriptorUtils.descriptorToJavaType(getDescriptor());
  }
}
