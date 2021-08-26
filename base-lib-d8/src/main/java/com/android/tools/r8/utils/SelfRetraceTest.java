// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

// NOTE: If you change this file you may need to update the expected line numbers in
// tools/test_self_retrace.py
public class SelfRetraceTest {
  private static String SELFRETRACETEST_ENVIRONMENT_VAR = "R8_THROW_EXCEPTION_FOR_TESTING_RETRACE";

  private void foo3() {
    throw new RuntimeException("Intentional exception for testing retrace.");
  }

  private void foo2() {
    foo3();
  }

  private void foo1() {
    foo2();
  }

  public static void test() {
    if (System.getenv(SELFRETRACETEST_ENVIRONMENT_VAR) != null) {
      new SelfRetraceTest().foo1();
    }
  }
}
