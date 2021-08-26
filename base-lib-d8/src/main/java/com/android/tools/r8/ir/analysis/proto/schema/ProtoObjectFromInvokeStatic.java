// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.ImmutableList;

public class ProtoObjectFromInvokeStatic extends ProtoObject {

  private final DexMethod method;

  public ProtoObjectFromInvokeStatic(DexMethod method) {
    this.method = method;
  }

  @Override
  public Instruction buildIR(AppView<?> appView, IRCode code) {
    Value value =
        code.createValue(
            TypeLatticeElement.fromDexType(
                method.proto.returnType, Nullability.maybeNull(), appView));
    return new InvokeStatic(method, value, ImmutableList.of());
  }
}
