// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import static com.android.tools.r8.ir.analysis.proto.schema.ProtoOneOfFieldType.FIRST_ONE_OF_ID;
import static com.android.tools.r8.utils.BitUtils.isBitInMaskSet;

import com.android.tools.r8.utils.BooleanUtils;

public class ProtoFieldType {

  public static final int MESSAGE_ID = 9;
  public static final int ENUM_ID = 12;
  public static final int GROUP_ID = 17;
  public static final int MESSAGE_LIST_ID = 27;
  public static final int ENUM_LIST_ID = 30;
  public static final int ENUM_LIST_PACKAGED_ID = 44;
  public static final int GROUP_LIST_ID = 49;
  public static final int MAP_ID = 50;

  private static final int FIELD_ID_MASK = 0xFF;
  private static final int FIELD_IS_REQUIRED_MASK = 0x100;
  private static final int FIELD_NEEDS_IS_INITIALIZED_CHECK_MASK = 0x400;
  private static final int FIELD_IS_MAP_FIELD_WITH_PROTO_2_ENUM_VALUE_MASK = 0x800;

  private final int id;
  private final boolean isRequired;
  private final boolean needsIsInitializedCheck;
  private final boolean isMapFieldWithProto2EnumValue;

  ProtoFieldType(
      int id,
      boolean isRequired,
      boolean needsIsInitializedCheck,
      boolean isMapFieldWithProto2EnumValue) {
    this.id = id;
    this.isRequired = isRequired;
    this.needsIsInitializedCheck = needsIsInitializedCheck;
    this.isMapFieldWithProto2EnumValue = isMapFieldWithProto2EnumValue;
    assert isValid();
  }

  static ProtoFieldType fromFieldIdWithExtraBits(int fieldTypeWithExtraBits) {
    int fieldId = fieldTypeWithExtraBits & FIELD_ID_MASK;
    if (fieldId < FIRST_ONE_OF_ID) {
      return new ProtoFieldType(
          fieldTypeWithExtraBits & FIELD_ID_MASK,
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_IS_REQUIRED_MASK),
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_NEEDS_IS_INITIALIZED_CHECK_MASK),
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_IS_MAP_FIELD_WITH_PROTO_2_ENUM_VALUE_MASK));
    } else {
      return new ProtoOneOfFieldType(
          fieldTypeWithExtraBits & FIELD_ID_MASK,
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_IS_REQUIRED_MASK),
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_NEEDS_IS_INITIALIZED_CHECK_MASK),
          isBitInMaskSet(fieldTypeWithExtraBits, FIELD_IS_MAP_FIELD_WITH_PROTO_2_ENUM_VALUE_MASK));
    }
  }

  public boolean hasAuxData(boolean isProto2) {
    return isProto2 && isSingular();
  }

  public int id() {
    return id;
  }

  public boolean isGroup() {
    return id == GROUP_ID;
  }

  public boolean isGroupList() {
    return id == GROUP_LIST_ID;
  }

  public boolean isMap() {
    return id == MAP_ID;
  }

  public boolean isMapFieldWithProto2EnumValue() {
    return isMapFieldWithProto2EnumValue;
  }

  public boolean isMessage() {
    return id == MESSAGE_ID;
  }

  public boolean isMessageList() {
    return id == MESSAGE_LIST_ID;
  }

  public boolean isOneOf() {
    return false;
  }

  public ProtoOneOfFieldType asOneOf() {
    return null;
  }

  public boolean isRepeated() {
    return !isSingular() && !isMap();
  }

  public boolean isRequired() {
    return isRequired;
  }

  public boolean isSingular() {
    return id <= GROUP_ID;
  }

  public boolean isValid() {
    assert id < FIRST_ONE_OF_ID;
    return true;
  }

  public boolean needsIsInitializedCheck() {
    return needsIsInitializedCheck;
  }

  public int numberOfObjects(boolean isProto2, ProtoFieldTypeFactory factory) {
    switch (id) {
      case MESSAGE_LIST_ID:
      case GROUP_LIST_ID:
        return 2;
      case ENUM_ID:
      case ENUM_LIST_ID:
      case ENUM_LIST_PACKAGED_ID:
        return BooleanUtils.intValue(isProto2) + 1;
      case MAP_ID:
        return BooleanUtils.intValue(isMapFieldWithProto2EnumValue) + 2;
      default:
        return 1;
    }
  }

  public int serialize() {
    int result = id;
    if (isRequired) {
      result |= FIELD_IS_REQUIRED_MASK;
    }
    if (needsIsInitializedCheck) {
      result |= FIELD_NEEDS_IS_INITIALIZED_CHECK_MASK;
    }
    if (isMapFieldWithProto2EnumValue) {
      result |= FIELD_IS_MAP_FIELD_WITH_PROTO_2_ENUM_VALUE_MASK;
    }
    return result;
  }
}
