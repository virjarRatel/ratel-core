// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming.dexitembasedstring;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.NamingLens;

public class FieldNameComputationInfo extends NameComputationInfo<DexField> {

  private static final FieldNameComputationInfo FIELD_NAME_INSTANCE =
      new FieldNameComputationInfo();

  private FieldNameComputationInfo() {}

  public static FieldNameComputationInfo forFieldName() {
    return FIELD_NAME_INSTANCE;
  }

  public boolean isForFieldName() {
    return true;
  }

  @Override
  public DexString internalComputeNameFor(
      DexField field, DexDefinitionSupplier definitions, NamingLens namingLens) {
    // Should be dead since needsToComputeName() returns false.
    throw new Unreachable();
  }

  @Override
  public boolean needsToComputeName() {
    return false;
  }

  @Override
  public boolean needsToRegisterReference() {
    return false;
  }

  @Override
  public boolean isFieldNameComputationInfo() {
    return true;
  }

  @Override
  public FieldNameComputationInfo asFieldNameComputationInfo() {
    return this;
  }
}
