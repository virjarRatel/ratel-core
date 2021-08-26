// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.value;

import static com.android.tools.r8.ir.analysis.type.Nullability.maybeNull;
import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.TypeAndLocalInfoSupplier;
import com.android.tools.r8.ir.code.Value;

public class SingleEnumValue extends SingleValue {

  private final DexField field;

  /** Intentionally package private, use {@link AbstractValueFactory} instead. */
  SingleEnumValue(DexField field) {
    this.field = field;
  }

  @Override
  public boolean isSingleEnumValue() {
    return true;
  }

  @Override
  public SingleEnumValue asSingleEnumValue() {
    return this;
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
    return "SingleEnumValue(" + field.toSourceString() + ")";
  }

  @Override
  public Instruction createMaterializingInstruction(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      TypeAndLocalInfoSupplier info) {
    TypeLatticeElement type = TypeLatticeElement.fromDexType(field.type, maybeNull(), appView);
    assert type.lessThanOrEqual(info.getTypeLattice(), appView);
    Value outValue = code.createValue(type, info.getLocalInfo());
    return new StaticGet(outValue, field);
  }

  @Override
  public boolean isMaterializableInContext(AppView<?> appView, DexType context) {
    DexEncodedField encodedField = appView.appInfo().resolveField(field);
    return isMemberVisibleFromOriginalContext(
        appView, context, encodedField.field.holder, encodedField.accessFlags);
  }
}
