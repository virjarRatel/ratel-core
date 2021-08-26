// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

public class FloatTypeLatticeElement extends SinglePrimitiveTypeLatticeElement {
  private static final FloatTypeLatticeElement INSTANCE = new FloatTypeLatticeElement();

  static FloatTypeLatticeElement getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isFloat() {
    return true;
  }

  @Override
  public String toString() {
    return "FLOAT";
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(INSTANCE);
  }
}
