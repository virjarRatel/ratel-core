// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.graph.DexMethod;
import com.google.common.base.Equivalence;

/**
 * Implements an equivalence on {@link DexMethod} that does not take the holder nor return type into
 * account.
 *
 * <p>Useful when deciding whether methods shadow each other wrt. Java semantics.
 */
public class MethodJavaSignatureEquivalence extends Equivalence<DexMethod> {

  private static final MethodJavaSignatureEquivalence INSTANCE =
      new MethodJavaSignatureEquivalence(false);

  private static final MethodJavaSignatureEquivalence INSTANCE_IGNORE_NAME =
      new MethodJavaSignatureEquivalence(true);

  private final boolean ignoreName;

  private MethodJavaSignatureEquivalence(boolean ignoreName) {
    this.ignoreName = ignoreName;
  }

  public static MethodJavaSignatureEquivalence get() {
    return INSTANCE;
  }

  public static MethodJavaSignatureEquivalence getEquivalenceIgnoreName() {
    return INSTANCE_IGNORE_NAME;
  }

  @Override
  protected boolean doEquivalent(DexMethod a, DexMethod b) {
    if (ignoreName) {
      return a.proto.parameters.equals(b.proto.parameters);
    }
    return a.name.equals(b.name) && a.proto.parameters.equals(b.proto.parameters);
  }

  @Override
  protected int doHash(DexMethod method) {
    if (ignoreName) {
      return method.proto.parameters.hashCode();
    }
    return method.name.hashCode() * 31 + method.proto.parameters.hashCode();
  }
}
