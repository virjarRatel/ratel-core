// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import java.util.List;

public class RetraceCommandLineResult {

  private final List<String> nodes;

  RetraceCommandLineResult(List<String> nodes) {
    this.nodes = nodes;
  }

  public List<String> getNodes() {
    return nodes;
  }
}
