// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

public class BooleanUtils {

  private static final Boolean[] VALUES = new Boolean[] { Boolean.TRUE, Boolean.FALSE };

  public static int intValue(boolean value) {
    return value ? 1 : 0;
  }

  public static Boolean[] values() {
    return VALUES;
  }
}
