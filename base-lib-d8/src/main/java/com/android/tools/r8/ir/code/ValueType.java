// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.PrimitiveTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;

public enum ValueType {
  OBJECT,
  INT,
  FLOAT,
  LONG,
  DOUBLE;

  public boolean isObject() {
    return this == OBJECT;
  }

  public boolean isSingle() {
    return this == INT || this == FLOAT;
  }

  public boolean isWide() {
    return this == LONG || this == DOUBLE;
  }

  public int requiredRegisters() {
    return isWide() ? 2 : 1;
  }

  public static ValueType fromMemberType(MemberType type) {
    switch (type) {
      case BOOLEAN_OR_BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return ValueType.INT;
      case FLOAT:
        return ValueType.FLOAT;
      case LONG:
        return ValueType.LONG;
      case DOUBLE:
        return ValueType.DOUBLE;
      case OBJECT:
        return ValueType.OBJECT;
      case INT_OR_FLOAT:
      case LONG_OR_DOUBLE:
        throw new Unreachable("Unexpected imprecise type: " + type);
      default:
        throw new Unreachable("Unexpected member type: " + type);
    }
  }

  public static ValueType fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':
      case '[':
        return ValueType.OBJECT;
      case 'Z':
      case 'B':
      case 'S':
      case 'C':
      case 'I':
        return ValueType.INT;
      case 'F':
        return ValueType.FLOAT;
      case 'J':
        return ValueType.LONG;
      case 'D':
        return ValueType.DOUBLE;
      case 'V':
        throw new InternalCompilerError("No value type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static ValueType fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }

  public static ValueType fromNumericType(NumericType type) {
    switch (type) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return ValueType.INT;
      case FLOAT:
        return ValueType.FLOAT;
      case LONG:
        return ValueType.LONG;
      case DOUBLE:
        return ValueType.DOUBLE;
      default:
        throw new Unreachable("Invalid numeric type '" + type + "'");
    }
  }

  public static ValueType fromTypeLattice(TypeLatticeElement typeLatticeElement) {
    if (typeLatticeElement.isReference()) {
      return OBJECT;
    }
    if (typeLatticeElement.isInt()) {
      return INT;
    }
    if (typeLatticeElement.isFloat()) {
      return FLOAT;
    }
    if (typeLatticeElement.isLong()) {
      return LONG;
    }
    if (typeLatticeElement.isDouble()) {
      return DOUBLE;
    }
    throw new Unreachable("Unexpected conversion of imprecise type: " + typeLatticeElement);
  }

  public PrimitiveTypeLatticeElement toPrimitiveTypeLattice() {
    switch (this) {
      case INT:
        return TypeLatticeElement.INT;
      case FLOAT:
        return TypeLatticeElement.FLOAT;
      case LONG:
        return TypeLatticeElement.LONG;
      case DOUBLE:
        return TypeLatticeElement.DOUBLE;
      default:
        throw new Unreachable("Unexpected type in conversion to primitive: " + this);
    }
  }
}
