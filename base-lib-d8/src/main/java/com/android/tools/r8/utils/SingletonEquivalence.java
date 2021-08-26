// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.base.Equivalence;

/** Implements an equivalence that considers all objects to be the same. */
public class SingletonEquivalence<T> extends Equivalence<T> {

  public SingletonEquivalence() {}

  @Override
  protected boolean doEquivalent(T a, T b) {
    return true;
  }

  @Override
  protected int doHash(T a) {
    return 0;
  }
}
