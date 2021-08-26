// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;

public enum FieldMemberType {
  OBJECT,
  BOOLEAN,
  BYTE,
  CHAR,
  SHORT,
  INT,
  FLOAT,
  LONG,
  DOUBLE;

  public static FieldMemberType fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':
      case '[':
        return FieldMemberType.OBJECT;
      case 'Z':
        return FieldMemberType.BOOLEAN;
      case 'B':
        return FieldMemberType.BYTE;
      case 'S':
        return FieldMemberType.SHORT;
      case 'C':
        return FieldMemberType.CHAR;
      case 'I':
        return FieldMemberType.INT;
      case 'F':
        return FieldMemberType.FLOAT;
      case 'J':
        return FieldMemberType.LONG;
      case 'D':
        return FieldMemberType.DOUBLE;
      case 'V':
        throw new InternalCompilerError("No member type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static FieldMemberType fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }
}
