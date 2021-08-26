// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;

public class DeadProtoFieldObject extends ProtoFieldObject {

  // For debugging purposes only.
  private final DexType holder;
  private final DexString name;

  public DeadProtoFieldObject(DexType holder, DexString name) {
    this.holder = holder;
    this.name = name;
  }

  @Override
  public Instruction buildIR(AppView<?> appView, IRCode code) {
    throw new Unreachable();
  }

  @Override
  public boolean isDeadProtoFieldObject() {
    return true;
  }

  @Override
  public String toString() {
    return "DeadProtoFieldObject(" + holder.toSourceString() + "." + name.toSourceString() + ")";
  }
}
