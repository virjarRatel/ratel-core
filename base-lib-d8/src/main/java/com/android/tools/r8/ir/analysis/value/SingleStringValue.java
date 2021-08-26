// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.value;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;
import static com.android.tools.r8.ir.analysis.type.TypeLatticeElement.stringClassType;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.TypeAndLocalInfoSupplier;
import com.android.tools.r8.ir.code.Value;

public class SingleStringValue extends SingleValue {

  private final DexString string;

  /** Intentionally package private, use {@link AbstractValueFactory} instead. */
  SingleStringValue(DexString string) {
    this.string = string;
  }

  @Override
  public boolean isSingleStringValue() {
    return true;
  }

  @Override
  public SingleStringValue asSingleStringValue() {
    return this;
  }

  public DexString getDexString() {
    return string;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public String toString() {
    return "SingleStringValue(" + string + ")";
  }

  @Override
  public Instruction createMaterializingInstruction(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      TypeAndLocalInfoSupplier info) {
    TypeLatticeElement typeLattice = info.getTypeLattice();
    DebugLocalInfo debugLocalInfo = info.getLocalInfo();
    assert typeLattice.isClassType();
    assert appView
        .isSubtype(
            appView.dexItemFactory().stringType,
            typeLattice.asClassTypeLatticeElement().getClassType())
        .isTrue();
    Value returnedValue =
        code.createValue(stringClassType(appView, definitelyNotNull()), debugLocalInfo);
    ConstString instruction =
        new ConstString(
            returnedValue, string, ThrowingInfo.defaultForConstString(appView.options()));
    assert !instruction.instructionInstanceCanThrow();
    return instruction;
  }

  @Override
  public boolean isMaterializableInContext(AppView<?> appView, DexType context) {
    return true;
  }
}
