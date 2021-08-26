// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;

public abstract class FieldOptimizationInfo {

  public abstract MutableFieldOptimizationInfo mutableCopy();

  public abstract boolean cannotBeKept();

  public abstract AbstractValue getAbstractValue();

  /**
   * This should only be used once all methods in the program have been processed. Until then the
   * value returned by this method may not be sound.
   */
  public abstract int getReadBits();

  public abstract ClassTypeLatticeElement getDynamicLowerBoundType();

  public abstract TypeLatticeElement getDynamicUpperBoundType();

  public abstract boolean valueHasBeenPropagated();

  public boolean isDefaultFieldOptimizationInfo() {
    return false;
  }

  public DefaultFieldOptimizationInfo asDefaultFieldOptimizationInfo() {
    return null;
  }

  public boolean isMutableFieldOptimizationInfo() {
    return false;
  }

  public MutableFieldOptimizationInfo asMutableFieldOptimizationInfo() {
    return null;
  }
}
