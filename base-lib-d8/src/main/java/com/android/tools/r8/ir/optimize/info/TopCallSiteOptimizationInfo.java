// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.info;

// Nothing should not be assumed about arguments at call sites.
class TopCallSiteOptimizationInfo extends CallSiteOptimizationInfo {
  static TopCallSiteOptimizationInfo INSTANCE = new TopCallSiteOptimizationInfo();

  private TopCallSiteOptimizationInfo() {}

  @Override
  public boolean isTop() {
    return true;
  }
}
