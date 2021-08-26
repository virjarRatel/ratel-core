// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;

public enum MemberType {
  OBJECT,
  BOOLEAN_OR_BYTE,
  CHAR,
  SHORT,
  INT,
  FLOAT,
  LONG,
  DOUBLE,
  INT_OR_FLOAT,
  LONG_OR_DOUBLE;

  public boolean isPrecise() {
    return this != INT_OR_FLOAT && this != LONG_OR_DOUBLE;
  }

  public static MemberType constrainedType(MemberType type, ValueTypeConstraint constraint) {
    switch (constraint) {
      case OBJECT:
        if (type == OBJECT) {
          return OBJECT;
        }
        break;
      case INT:
        if (type == INT || type == INT_OR_FLOAT) {
          return INT;
        }
        break;
      case FLOAT:
        if (type == FLOAT || type == INT_OR_FLOAT) {
          return FLOAT;
        }
        break;
      case INT_OR_FLOAT:
        if (type == INT || type == FLOAT || type == INT_OR_FLOAT) {
          return type;
        }
        break;
      case INT_OR_FLOAT_OR_OBJECT:
        if (type == INT || type == FLOAT || type == OBJECT || type == INT_OR_FLOAT) {
          return type;
        }
        break;
      case LONG:
        if (type == LONG || type == LONG_OR_DOUBLE) {
          return LONG;
        }
        break;
      case DOUBLE:
        if (type == DOUBLE || type == LONG_OR_DOUBLE) {
          return DOUBLE;
        }
        break;
      case LONG_OR_DOUBLE:
        if (type == LONG || type == DOUBLE || type == LONG_OR_DOUBLE) {
          return type;
        }
        break;
      default:
        throw new Unreachable("Unexpected type constraint: " + constraint);
    }
    return null;
  }

  public static MemberType fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':
      case '[':
        return MemberType.OBJECT;
      case 'Z':
      case 'B':
        return MemberType.BOOLEAN_OR_BYTE;
      case 'S':
        return MemberType.SHORT;
      case 'C':
        return MemberType.CHAR;
      case 'I':
        return MemberType.INT;
      case 'F':
        return MemberType.FLOAT;
      case 'J':
        return MemberType.LONG;
      case 'D':
        return MemberType.DOUBLE;
      case 'V':
        throw new InternalCompilerError("No member type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static MemberType fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }
}
