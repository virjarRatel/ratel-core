// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.desugar.BackportedMethodRewriter;
import com.android.tools.r8.utils.AndroidApiLevel;

public class GenerateBackportedMethodList {

  public static void main(String[] args) {
    BackportedMethodRewriter.generateListOfBackportedMethods(AndroidApiLevel.B).stream()
        .map(DexMethod::toSourceString)
        .sorted()
        .forEach(System.out::println);
  }
}
