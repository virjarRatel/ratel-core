// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;

public class ProtoTypeObject extends ProtoObject {

  private final DexType type;

  public ProtoTypeObject(DexType type) {
    this.type = type;
  }

  public DexType getType() {
    return type;
  }

  @Override
  public Instruction buildIR(AppView<?> appView, IRCode code) {
    return code.createConstClass(appView, type);
  }

  @Override
  public boolean isProtoTypeObject() {
    return true;
  }

  @Override
  public ProtoTypeObject asProtoTypeObject() {
    return this;
  }
}
