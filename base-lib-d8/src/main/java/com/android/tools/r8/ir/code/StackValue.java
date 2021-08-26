// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.TypeVerificationHelper.TypeInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;

public class StackValue extends Value {

  private final int height;
  private final TypeInfo typeInfo;

  private StackValue(TypeInfo typeInfo, TypeLatticeElement typeLattice, int height) {
    super(Value.UNDEFINED_NUMBER, typeLattice, null);
    this.height = height;
    this.typeInfo = typeInfo;
    assert height >= 0;
  }

  public static StackValue create(TypeInfo typeInfo, int height, AppView<?> appView) {
    return new StackValue(
        typeInfo,
        TypeLatticeElement.fromDexType(typeInfo.getDexType(), Nullability.maybeNull(), appView),
        height);
  }

  public int getHeight() {
    return height;
  }

  public TypeInfo getTypeInfo() {
    return typeInfo;
  }

  public StackValue duplicate(int height) {
    return new StackValue(typeInfo, getTypeLattice(), height);
  }

  @Override
  public boolean isValueOnStack() {
    return true;
  }

  @Override
  public boolean needsRegister() {
    return false;
  }

  @Override
  public void setNeedsRegister(boolean value) {
    assert !value;
  }

  @Override
  public String toString() {
    return "s" + height;
  }
}
