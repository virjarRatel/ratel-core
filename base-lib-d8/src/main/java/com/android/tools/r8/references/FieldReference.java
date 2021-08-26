// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.references;

import com.android.tools.r8.Keep;
import java.util.Objects;

/**
 * Reference to a field.
 *
 * <p>A field reference is always fully qualified with both a qualified holder type as well as the
 * type of the field.
 */
@Keep
public class FieldReference {
  private final ClassReference holderClass;
  private final String fieldName;
  private final TypeReference fieldType;

  boolean isUnknown() {
    return false;
  }

  FieldReference(ClassReference holderClass, String fieldName, TypeReference fieldType) {
    assert holderClass != null;
    assert fieldName != null;
    assert fieldType != null || isUnknown();
    this.holderClass = holderClass;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  public ClassReference getHolderClass() {
    return holderClass;
  }

  public String getFieldName() {
    return fieldName;
  }

  public TypeReference getFieldType() {
    return fieldType;
  }

  // Field references must implement full equality and hashcode since they are used as
  // canonicalization keys.

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FieldReference)) {
      return false;
    }
    FieldReference other = (FieldReference) o;
    return holderClass.equals(other.holderClass)
        && fieldName.equals(other.fieldName)
        && fieldType.equals(other.fieldType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(holderClass, fieldName, fieldType);
  }

  @Override
  public String toString() {
    return getHolderClass().toString() + getFieldName() + ":" + getFieldType().getDescriptor();
  }

  public static final class UnknownFieldReference extends FieldReference {

    public UnknownFieldReference(ClassReference holderClass, String fieldName) {
      super(holderClass, fieldName, null);
    }

    @Override
    boolean isUnknown() {
      return true;
    }
  }
}
