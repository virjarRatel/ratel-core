// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldvalueanalysis;

import com.android.tools.r8.graph.DexEncodedField;

public class UnknownFieldSet extends AbstractFieldSet {

  private static final UnknownFieldSet INSTANCE = new UnknownFieldSet();

  private UnknownFieldSet() {}

  public static UnknownFieldSet getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean contains(DexEncodedField field) {
    return true;
  }

  @Override
  public boolean isTop() {
    return true;
  }
}
