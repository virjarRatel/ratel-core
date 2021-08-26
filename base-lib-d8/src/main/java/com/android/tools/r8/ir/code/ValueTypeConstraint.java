// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.PrimitiveTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;

public enum ValueTypeConstraint {
  OBJECT,
  INT,
  FLOAT,
  INT_OR_FLOAT,
  INT_OR_FLOAT_OR_OBJECT,
  LONG,
  DOUBLE,
  LONG_OR_DOUBLE;

  public boolean isObject() {
    return this == OBJECT;
  }

  public boolean isSingle() {
    return this == INT || this == FLOAT || this == INT_OR_FLOAT;
  }

  public boolean isWide() {
    return this == LONG || this == DOUBLE || this == LONG_OR_DOUBLE;
  }

  public boolean isPrecise() {
    return this != ValueTypeConstraint.INT_OR_FLOAT
        && this != ValueTypeConstraint.LONG_OR_DOUBLE
        && this != ValueTypeConstraint.INT_OR_FLOAT_OR_OBJECT;
  }

  public int requiredRegisters() {
    return isWide() ? 2 : 1;
  }

  public static ValueTypeConstraint fromValueType(ValueType type) {
    switch (type) {
      case OBJECT:
        return OBJECT;
      case INT:
        return INT;
      case FLOAT:
        return FLOAT;
      case LONG:
        return LONG;
      case DOUBLE:
        return DOUBLE;
      default:
        throw new Unreachable("Unexpected value type: " + type);
    }
  }

  public static ValueTypeConstraint fromMemberType(MemberType type) {
    switch (type) {
      case BOOLEAN_OR_BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return ValueTypeConstraint.INT;
      case FLOAT:
        return ValueTypeConstraint.FLOAT;
      case INT_OR_FLOAT:
        return ValueTypeConstraint.INT_OR_FLOAT;
      case LONG:
        return ValueTypeConstraint.LONG;
      case DOUBLE:
        return ValueTypeConstraint.DOUBLE;
      case LONG_OR_DOUBLE:
        return ValueTypeConstraint.LONG_OR_DOUBLE;
      case OBJECT:
        return ValueTypeConstraint.OBJECT;
      default:
        throw new Unreachable("Unexpected member type: " + type);
    }
  }

  public static ValueTypeConstraint fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':
      case '[':
        return ValueTypeConstraint.OBJECT;
      case 'Z':
      case 'B':
      case 'S':
      case 'C':
      case 'I':
        return ValueTypeConstraint.INT;
      case 'F':
        return ValueTypeConstraint.FLOAT;
      case 'J':
        return ValueTypeConstraint.LONG;
      case 'D':
        return ValueTypeConstraint.DOUBLE;
      case 'V':
        throw new InternalCompilerError("No value type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static ValueTypeConstraint fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }

  public static ValueTypeConstraint fromNumericType(NumericType type) {
    switch (type) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return ValueTypeConstraint.INT;
      case FLOAT:
        return ValueTypeConstraint.FLOAT;
      case LONG:
        return ValueTypeConstraint.LONG;
      case DOUBLE:
        return ValueTypeConstraint.DOUBLE;
      default:
        throw new Unreachable("Invalid numeric type '" + type + "'");
    }
  }

  public static ValueTypeConstraint fromTypeLattice(TypeLatticeElement typeLatticeElement) {
    if (typeLatticeElement.isReference()) {
      return OBJECT;
    }
    if (typeLatticeElement.isFineGrainedType() || typeLatticeElement.isInt()) {
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
    if (typeLatticeElement.isSinglePrimitive()) {
      return INT_OR_FLOAT;
    }
    if (typeLatticeElement.isWidePrimitive()) {
      return LONG_OR_DOUBLE;
    }
    if (typeLatticeElement.isTop()) {
      return INT_OR_FLOAT_OR_OBJECT;
    }
    throw new Unreachable("Unexpected conversion of type: " + typeLatticeElement);
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
