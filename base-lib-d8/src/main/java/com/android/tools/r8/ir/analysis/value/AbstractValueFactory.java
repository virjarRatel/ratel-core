// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.value;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexString;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractValueFactory {

  private ConcurrentHashMap<DexField, SingleEnumValue> singleEnumValues = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Long, SingleNumberValue> singleNumberValues = new ConcurrentHashMap<>();
  private ConcurrentHashMap<DexString, SingleStringValue> singleStringValues =
      new ConcurrentHashMap<>();

  public SingleEnumValue createSingleEnumValue(DexField field) {
    return singleEnumValues.computeIfAbsent(field, SingleEnumValue::new);
  }

  public SingleNumberValue createSingleNumberValue(long value) {
    return singleNumberValues.computeIfAbsent(value, SingleNumberValue::new);
  }

  public SingleStringValue createSingleStringValue(DexString string) {
    return singleStringValues.computeIfAbsent(string, SingleStringValue::new);
  }
}
