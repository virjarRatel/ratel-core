// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.constant;

abstract public class LatticeElement {
  abstract public LatticeElement meet(LatticeElement other);

  public boolean isConst() {
    return false;
  }

  public boolean isValueRange() {
    return false;
  }

  public ConstLatticeElement asConst() {
    return null;
  }

  public ConstRangeLatticeElement asConstRange() {
    return null;
  }

  public boolean isTop() {
    return false;
  }

  public boolean isBottom() {
    return false;
  }
}
