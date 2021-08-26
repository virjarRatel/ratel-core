// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

@FunctionalInterface
public interface Consumer3<F1, F2, F3> {
  void accept(F1 f1, F2 f2, F3 f3);
}
