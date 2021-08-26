// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;

/**
 * {@link StackValues} allow us to represent stack operations that produces two or more elements on
 * the stack while using the same logic for instructions.
 */
public class StackValues extends Value {

  private final StackValue[] stackValues;

  public StackValues(StackValue... stackValues) {
    super(Value.UNDEFINED_NUMBER, TypeLatticeElement.BOTTOM, null);
    this.stackValues = stackValues;
    assert stackValues.length >= 2;
  }

  public StackValue[] getStackValues() {
    return stackValues;
  }

  @Override
  public boolean needsRegister() {
    return false;
  }

  @Override
  public void setNeedsRegister(boolean value) {
    assert !value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (StackValue value : stackValues) {
      if (sb.length() > 1) {
        sb.append(", ");
      }
      sb.append(value);
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean isValueOnStack() {
    return true;
  }

  @Override
  public TypeLatticeElement getTypeLattice() {
    throw new Unreachable();
  }
}
