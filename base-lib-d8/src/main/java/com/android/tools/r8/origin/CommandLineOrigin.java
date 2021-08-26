// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.origin;

public class CommandLineOrigin extends Origin {

  public static final CommandLineOrigin INSTANCE = new CommandLineOrigin();

  private CommandLineOrigin() {
    super(root());
  }

  @Override
  public String part() {
    return "Command line";
  }
}
