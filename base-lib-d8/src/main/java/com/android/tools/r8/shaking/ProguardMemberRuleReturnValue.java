// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.utils.LongInterval;

public class ProguardMemberRuleReturnValue {
  public enum Type {
    BOOLEAN,
    VALUE_RANGE,
    FIELD,
    NULL
  }

  private final Type type;
  private final boolean booleanValue;
  private final LongInterval longInterval;
  private final DexField field;

  ProguardMemberRuleReturnValue(boolean value) {
    this.type = Type.BOOLEAN;
    this.booleanValue = value;
    this.longInterval = null;
    this.field = null;
  }

  ProguardMemberRuleReturnValue(LongInterval value) {
    this.type = Type.VALUE_RANGE;
    this.booleanValue = false;
    this.longInterval = value;
    this.field = null;
  }

  ProguardMemberRuleReturnValue(DexField field) {
    this.type = Type.FIELD;
    this.booleanValue = false;
    this.longInterval = null;
    this.field = field;
  }

  ProguardMemberRuleReturnValue() {
    this.type = Type.NULL;
    this.booleanValue = false;
    this.longInterval = null;
    this.field = null;
  }

  public boolean isBoolean() {
    return type == Type.BOOLEAN;
  }

  public boolean isValueRange() {
    return type == Type.VALUE_RANGE;
  }

  public boolean isField() {
    return type == Type.FIELD;
  }

  public boolean isNull() {
    return type == Type.NULL;
  }

  public boolean getBoolean() {
    assert isBoolean();
    return booleanValue;
  }

  /**
   * Returns if this return value is a single value.
   *
   * Boolean values and null are considered a single value.
   */
  public boolean isSingleValue() {
    return isBoolean() || isNull() || (isValueRange() && longInterval.isSingleValue());
  }

  /**
   * Returns the return value.
   *
   * Boolean values are returned as 0 for <code>false</code> and 1 for <code>true</code>.
   *
   * Reference value <code>null</code> is returned as 0.
   */
  public long getSingleValue() {
    assert isSingleValue();
    if (isBoolean()) {
      return booleanValue ? 1 : 0;
    }
    if (isNull()) {
      return 0;
    }
    return longInterval.getSingleValue();
  }

  public LongInterval getValueRange() {
    assert isValueRange();
    return longInterval;
  }

  public DexField getField() {
    assert isField();
    return field;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(" return ");
    if (isBoolean()) {
      result.append(booleanValue ? "true" : "false");
    } else if (isNull()) {
      result.append("null");
    } else if (isValueRange()) {
      result.append(longInterval.getMin());
      if (!isSingleValue()) {
        result.append("..");
        result.append(longInterval.getMax());
      }
    } else {
      assert isField();
      result.append(field.holder.toSourceString() + '.' + field.name);
    }
    return result.toString();
  }
}
