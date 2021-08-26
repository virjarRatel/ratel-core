// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.references;

import com.android.tools.r8.Keep;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.StringUtils.BraceType;
import java.util.List;
import java.util.Objects;

/**
 * Reference to a method.
 *
 * <p>A method reference is always fully qualified with both a qualified holder type as well as its
 * full list of formal parameters.
 */
@Keep
public class MethodReference {
  private final ClassReference holderClass;
  private final String methodName;
  private final List<TypeReference> formalTypes;
  private final TypeReference returnType;

  MethodReference(
      ClassReference holderClass,
      String methodName,
      List<TypeReference> formalTypes,
      TypeReference returnType) {
    assert holderClass != null;
    assert methodName != null;
    assert formalTypes != null || isUnknown();
    this.holderClass = holderClass;
    this.methodName = methodName;
    this.formalTypes = formalTypes;
    this.returnType = returnType;
  }

  public boolean isUnknown() {
    return false;
  }

  public ClassReference getHolderClass() {
    return holderClass;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<TypeReference> getFormalTypes() {
    return formalTypes;
  }

  public TypeReference getReturnType() {
    return returnType;
  }

  // Method references must implement full equality and hashcode since they are used as
  // canonicalization keys.

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MethodReference)) {
      return false;
    }
    MethodReference other = (MethodReference) o;
    return holderClass.equals(other.holderClass)
        && methodName.equals(other.methodName)
        && formalTypes.equals(other.formalTypes)
        && Objects.equals(returnType, other.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(holderClass, methodName, formalTypes, returnType);
  }

  public String getMethodDescriptor() {
    return StringUtils.join(
            ListUtils.map(getFormalTypes(), TypeReference::getDescriptor), "", BraceType.PARENS)
        + (getReturnType() == null ? "V" : getReturnType().getDescriptor());
  }

  @Override
  public String toString() {
    return getHolderClass().toString() + getMethodName() + getMethodDescriptor();
  }

  public static final class UnknownMethodReference extends MethodReference {

    @Override
    public boolean isUnknown() {
      return true;
    }

    public UnknownMethodReference(ClassReference holderClass, String methodName) {
      super(holderClass, methodName, null, null);
    }
  }
}
