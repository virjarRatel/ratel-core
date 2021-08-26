// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

public class ShortTypeLatticeElement extends SinglePrimitiveTypeLatticeElement {

  private static final ShortTypeLatticeElement INSTANCE = new ShortTypeLatticeElement();

  static ShortTypeLatticeElement getInstance() {
    return INSTANCE;
  }

  @Override
  boolean isShort() {
    return true;
  }

  @Override
  public String toString() {
    return "SHORT";
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
