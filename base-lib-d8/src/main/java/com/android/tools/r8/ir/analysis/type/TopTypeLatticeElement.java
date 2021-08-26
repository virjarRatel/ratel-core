// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

public class TopTypeLatticeElement extends TypeLatticeElement {
  private static final TopTypeLatticeElement INSTANCE = new TopTypeLatticeElement();

  @Override
  public Nullability nullability() {
    return Nullability.maybeNull();
  }

  static TopTypeLatticeElement getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isTop() {
    return true;
  }

  @Override
  public String toString() {
    return "TOP (everything)";
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(INSTANCE);
  }
}
