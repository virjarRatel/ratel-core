// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.origin;

public class SynthesizedOrigin extends Origin {

  private final String reason;
  private final Class<?> from;

  public SynthesizedOrigin(String reason, Class<?> from) {
    super(root());
    this.reason = reason;
    this.from = from;
  }

  @Override
  public String part() {
    return "synthesized for " + reason;
  }

  public String getReason() {
    return reason;
  }

  public Class<?> getFrom() {
    return from;
  }
}
