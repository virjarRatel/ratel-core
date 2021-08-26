// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

public class BitUtils {

  public static boolean isBitSet(int value, int which) {
    return (value & (1 << (which - 1))) != 0;
  }

  public static boolean isBitInMaskSet(int value, int mask) {
    return (value & mask) != 0;
  }
}
