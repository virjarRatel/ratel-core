// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

public class BitAccessInfo {

  private static final int ALL_BITS_READ = -1;
  private static final int NO_BITS_READ = 0;

  private BitAccessInfo() {}

  public static int getAllBitsReadValue() {
    return ALL_BITS_READ;
  }

  public static int getNoBitsReadValue() {
    return NO_BITS_READ;
  }

  public static boolean allBitsRead(int readBits) {
    return readBits == ALL_BITS_READ;
  }
}
