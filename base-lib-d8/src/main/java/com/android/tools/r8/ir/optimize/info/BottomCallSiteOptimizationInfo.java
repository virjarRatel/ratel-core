// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.info;

// Nothing is known about arguments at call sites.
public class BottomCallSiteOptimizationInfo extends CallSiteOptimizationInfo {
  static BottomCallSiteOptimizationInfo INSTANCE = new BottomCallSiteOptimizationInfo();

  private BottomCallSiteOptimizationInfo() {}

  @Override
  public boolean isBottom() {
    return true;
  }
}
