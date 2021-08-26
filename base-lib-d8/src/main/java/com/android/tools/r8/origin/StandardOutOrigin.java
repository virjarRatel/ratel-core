// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.origin;

public class StandardOutOrigin extends Origin {

  private static final StandardOutOrigin INSTANCE = new StandardOutOrigin();

  public static StandardOutOrigin instance() {
    return INSTANCE;
  }

  private StandardOutOrigin() {
    super(Origin.root());
  }

  @Override
  public String part() {
    return "stdout";
  }
}
