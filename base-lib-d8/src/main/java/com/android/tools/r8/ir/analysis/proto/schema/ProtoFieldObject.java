// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

public abstract class ProtoFieldObject extends ProtoObject {

  @Override
  public boolean isProtoFieldObject() {
    return true;
  }

  @Override
  public ProtoFieldObject asProtoFieldObject() {
    return this;
  }
}
