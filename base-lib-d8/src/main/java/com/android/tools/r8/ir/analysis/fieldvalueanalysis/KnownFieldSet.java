// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldvalueanalysis;

public interface KnownFieldSet {

  default boolean isConcreteFieldSet() {
    return false;
  }

  default ConcreteMutableFieldSet asConcreteFieldSet() {
    return null;
  }

  int size();
}
