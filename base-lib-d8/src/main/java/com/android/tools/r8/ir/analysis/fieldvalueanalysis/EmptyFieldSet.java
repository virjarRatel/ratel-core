// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldvalueanalysis;

import com.android.tools.r8.graph.DexEncodedField;

public class EmptyFieldSet extends AbstractFieldSet implements KnownFieldSet {

  private static final EmptyFieldSet INSTANCE = new EmptyFieldSet();

  private EmptyFieldSet() {}

  public static EmptyFieldSet getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isKnownFieldSet() {
    return true;
  }

  @Override
  public EmptyFieldSet asKnownFieldSet() {
    return this;
  }

  @Override
  public boolean contains(DexEncodedField field) {
    return false;
  }

  @Override
  public boolean isBottom() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }
}
