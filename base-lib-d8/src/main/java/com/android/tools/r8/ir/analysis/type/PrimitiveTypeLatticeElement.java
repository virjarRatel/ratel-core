// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.NumericType;

/**
 * A {@link TypeLatticeElement} that abstracts primitive types.
 */
public abstract class PrimitiveTypeLatticeElement extends TypeLatticeElement {

  @Override
  public Nullability nullability() {
    return Nullability.definitelyNotNull();
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public PrimitiveTypeLatticeElement asPrimitiveTypeLatticeElement() {
    return this;
  }

  static PrimitiveTypeLatticeElement fromDexType(DexType type, boolean asArrayElementType) {
    assert type.isPrimitiveType();
    return fromTypeDescriptorChar((char) type.descriptor.content[0], asArrayElementType);
  }

  public DexType toDexType(DexItemFactory factory) {
    if (isBoolean()) {
      return factory.booleanType;
    }
    if (isByte()) {
      return factory.byteType;
    }
    if (isShort()) {
      return factory.shortType;
    }
    if (isChar()) {
      return factory.charType;
    }
    if (isInt()) {
      return factory.intType;
    }
    if (isFloat()) {
      return factory.floatType;
    }
    if (isLong()) {
      return factory.longType;
    }
    if (isDouble()) {
      return factory.doubleType;
    }
    throw new Unreachable("Imprecise primitive type '" + toString() + "'");
  }

  public boolean hasDexType() {
    return isBoolean()
        || isByte()
        || isShort()
        || isChar()
        || isInt()
        || isFloat()
        || isLong()
        || isDouble();
  }

  private static PrimitiveTypeLatticeElement fromTypeDescriptorChar(
      char descriptor, boolean asArrayElementType) {
    switch (descriptor) {
      case 'Z':
        if (asArrayElementType) {
          return TypeLatticeElement.BOOLEAN;
        }
        // fall through
      case 'B':
        if (asArrayElementType) {
          return TypeLatticeElement.BYTE;
        }
        // fall through
      case 'S':
        if (asArrayElementType) {
          return TypeLatticeElement.SHORT;
        }
        // fall through
      case 'C':
        if (asArrayElementType) {
          return TypeLatticeElement.CHAR;
        }
        // fall through
      case 'I':
        return TypeLatticeElement.INT;
      case 'F':
        return TypeLatticeElement.FLOAT;
      case 'J':
        return TypeLatticeElement.LONG;
      case 'D':
        return TypeLatticeElement.DOUBLE;
      case 'V':
        throw new InternalCompilerError("No value type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static PrimitiveTypeLatticeElement fromNumericType(NumericType numericType) {
    switch(numericType) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return TypeLatticeElement.INT;
      case FLOAT:
        return TypeLatticeElement.FLOAT;
      case LONG:
        return TypeLatticeElement.LONG;
      case DOUBLE:
        return TypeLatticeElement.DOUBLE;
      default:
        throw new Unreachable("Invalid numeric type '" + numericType + "'");
    }
  }

  TypeLatticeElement join(PrimitiveTypeLatticeElement other) {
    if (this == other) {
      return this;
    }
    if (isSinglePrimitive()) {
      if (other.isSinglePrimitive()) {
        return TypeLatticeElement.SINGLE;
      }
      assert other.isWidePrimitive();
      return TypeLatticeElement.TOP;
    }
    assert isWidePrimitive();
    if (other.isWidePrimitive()) {
      return TypeLatticeElement.WIDE;
    }
    assert other.isSinglePrimitive();
    return TypeLatticeElement.TOP;
  }
}
