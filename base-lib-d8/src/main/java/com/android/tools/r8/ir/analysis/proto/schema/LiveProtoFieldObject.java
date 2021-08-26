// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.naming.dexitembasedstring.FieldNameComputationInfo;

public class LiveProtoFieldObject extends ProtoFieldObject {

  private final DexField field;

  public LiveProtoFieldObject(DexField field) {
    this.field = field;
  }

  public DexField getField() {
    return field;
  }

  @Override
  public Instruction buildIR(AppView<?> appView, IRCode code) {
    Value value =
        code.createValue(
            TypeLatticeElement.stringClassType(appView, Nullability.definitelyNotNull()));
    ThrowingInfo throwingInfo = ThrowingInfo.defaultForConstString(appView.options());
    if (appView.options().isMinifying()) {
      return new DexItemBasedConstString(
          value, field, FieldNameComputationInfo.forFieldName(), throwingInfo);
    }
    return new ConstString(value, field.name, throwingInfo);
  }

  @Override
  public boolean isLiveProtoFieldObject() {
    return true;
  }

  @Override
  public LiveProtoFieldObject asLiveProtoFieldObject() {
    return this;
  }

  @Override
  public String toString() {
    return "LiveProtoFieldObject(" + field.toSourceString() + ")";
  }
}
