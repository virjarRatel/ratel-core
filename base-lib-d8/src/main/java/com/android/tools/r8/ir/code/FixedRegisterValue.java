// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;

// Value that has a fixed register allocated. These are used for inserting spill, restore, and phi
// moves in the spilling register allocator.
public class FixedRegisterValue extends Value {
  private final int register;

  public FixedRegisterValue(TypeLatticeElement moveType, int register) {
    // Set local info to null since these values are never representatives of live-ranges.
    super(-1, moveType, null);
    setNeedsRegister(true);
    this.register = register;
  }

  @Override
  public ValueType outType() {
    TypeLatticeElement type = getTypeLattice();
    if (type.isPrimitive()) {
      if (type.isSinglePrimitive()) {
        if (type.isInt()) {
          return ValueType.INT;
        }
        if (type.isFloat()) {
          return ValueType.FLOAT;
        }
      } else {
        assert type.isWidePrimitive();
        if (type.isDouble()) {
          return ValueType.DOUBLE;
        }
        if (type.isLong()) {
          return ValueType.LONG;
        }
      }
    } else {
      assert type.isReference();
      return ValueType.OBJECT;
    }
    throw new Unreachable("Unexpected imprecise type: " + type);
  }

  public int getRegister() {
    return register;
  }

  @Override
  public boolean isFixedRegisterValue() {
    return true;
  }

  @Override
  public FixedRegisterValue asFixedRegisterValue() {
    return this;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public String toString() {
    return "r" + register;
  }
}
