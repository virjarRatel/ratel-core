// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

  R apply(T t, U u, V v);
}
