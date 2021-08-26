// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.utils.BooleanUtils;

public class ProtoOneOfFieldType extends ProtoFieldType {

  static final int FIRST_ONE_OF_ID = MAP_ID + 1;

  ProtoOneOfFieldType(
      int id,
      boolean isRequired,
      boolean needsIsInitializedCheck,
      boolean isMapFieldWithProto2EnumValue) {
    super(id, isRequired, needsIsInitializedCheck, isMapFieldWithProto2EnumValue);
  }

  public ProtoFieldType getActualFieldType(ProtoFieldTypeFactory factory) {
    return factory.createField(serialize() - FIRST_ONE_OF_ID);
  }

  @Override
  public boolean hasAuxData(boolean isProto2) {
    return true;
  }

  @Override
  public boolean isOneOf() {
    return true;
  }

  @Override
  public ProtoOneOfFieldType asOneOf() {
    return this;
  }

  @Override
  public boolean isRepeated() {
    return false;
  }

  @Override
  public boolean isSingular() {
    return true;
  }

  @Override
  public boolean isValid() {
    assert id() >= FIRST_ONE_OF_ID;
    return true;
  }

  @Override
  public int numberOfObjects(boolean isProto2, ProtoFieldTypeFactory factory) {
    ProtoFieldType actualFieldType = getActualFieldType(factory);
    switch (actualFieldType.id()) {
      case MESSAGE_ID:
      case GROUP_ID:
        return 1;
      case ENUM_ID:
        return BooleanUtils.intValue(isProto2);
      default:
        return 0;
    }
  }
}
