// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.value;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.TypeAndLocalInfoSupplier;
import com.android.tools.r8.ir.code.Value;

public class SingleNumberValue extends SingleValue {

  private final long value;

  /** Intentionally package private, use {@link AbstractValueFactory} instead. */
  SingleNumberValue(long value) {
    this.value = value;
  }

  @Override
  public boolean isSingleNumberValue() {
    return true;
  }

  @Override
  public SingleNumberValue asSingleNumberValue() {
    return this;
  }

  public boolean getBooleanValue() {
    assert value == 0 || value == 1;
    return value != 0;
  }

  public long getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "SingleNumberValue(" + value + ")";
  }

  @Override
  public Instruction createMaterializingInstruction(
      AppView<? extends AppInfoWithSubtyping> appView, IRCode code, TypeAndLocalInfoSupplier info) {
    TypeLatticeElement typeLattice = info.getTypeLattice();
    DebugLocalInfo debugLocalInfo = info.getLocalInfo();
    assert !typeLattice.isReference() || value == 0;
    Value returnedValue =
        code.createValue(
            typeLattice.isReference() ? TypeLatticeElement.NULL : typeLattice, debugLocalInfo);
    return new ConstNumber(returnedValue, value);
  }

  @Override
  public boolean isMaterializableInContext(AppView<?> appView, DexType context) {
    return true;
  }
}
