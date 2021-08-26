// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.constant;

public class Top extends LatticeElement {
  private static final Top INSTANCE = new Top();

  private Top() {
  }

  public static Top getInstance() {
    return INSTANCE;
  }

  @Override
  public LatticeElement meet(LatticeElement other) {
    return other;
  }

  @Override
  public boolean isTop() {
    return true;
  }

  @Override
  public String toString() {
    return "TOP";
  }
}
