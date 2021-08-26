// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.Value;

public class ProtoObjectFromStaticGet extends ProtoObject {

  private final DexField field;

  public ProtoObjectFromStaticGet(DexField field) {
    this.field = field;
  }

  public DexField getField() {
    return field;
  }

  @Override
  public Instruction buildIR(AppView<?> appView, IRCode code) {
    Value value =
        code.createValue(
            TypeLatticeElement.fromDexType(field.type, Nullability.maybeNull(), appView));
    return new StaticGet(value, field);
  }

  @Override
  public boolean isProtoObjectFromStaticGet() {
    return true;
  }

  @Override
  public ProtoObjectFromStaticGet asProtoObjectFromStaticGet() {
    return this;
  }
}
