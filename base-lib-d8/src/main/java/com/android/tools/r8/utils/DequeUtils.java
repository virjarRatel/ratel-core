// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class DequeUtils {

  public static <T> Deque<T> newArrayDeque(T element) {
    Deque<T> deque = new ArrayDeque<>();
    deque.add(element);
    return deque;
  }
}
