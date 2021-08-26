// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.BooleanUtils;

public class ProtoUtils {

  public static final int IS_PROTO_2_MASK = 0x1;

  public static Value getInfoValueFromMessageInfoConstructionInvoke(
      InvokeMethod invoke, ProtoReferences references) {
    assert references.isMessageInfoConstructionMethod(invoke.getInvokedMethod());
    int adjustment = BooleanUtils.intValue(invoke.isInvokeDirect());
    return invoke.inValues().get(1 + adjustment).getAliasedValue();
  }

  static Value getObjectsValueFromMessageInfoConstructionInvoke(
      InvokeMethod invoke, ProtoReferences references) {
    assert references.isMessageInfoConstructionMethod(invoke.getInvokedMethod());
    int adjustment = BooleanUtils.intValue(invoke.isInvokeDirect());
    return invoke.inValues().get(2 + adjustment).getAliasedValue();
  }

  static void setObjectsValueForMessageInfoConstructionInvoke(
      InvokeMethod invoke, Value newObjectsValue, ProtoReferences references) {
    assert references.isMessageInfoConstructionMethod(invoke.getInvokedMethod());
    int adjustment = BooleanUtils.intValue(invoke.isInvokeDirect());
    invoke.replaceValue(2 + adjustment, newObjectsValue);
  }

  public static boolean isProto2(int flags) {
    return (flags & IS_PROTO_2_MASK) != 0;
  }
}
