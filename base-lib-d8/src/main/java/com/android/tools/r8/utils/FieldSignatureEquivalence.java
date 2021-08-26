// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.graph.DexField;
import com.google.common.base.Equivalence;

/**
 * Implements an equivalence on {@link DexField} that does not take the holder into account.
 *
 * <p>Useful when comparing method implementations by their signature only.
 */
public class FieldSignatureEquivalence extends Equivalence<DexField> {

  private static final FieldSignatureEquivalence INSTANCE = new FieldSignatureEquivalence(false);

  private static final FieldSignatureEquivalence INSTANCE_IGNORE_NAME =
      new FieldSignatureEquivalence(true);

  private final boolean ignoreName;

  private FieldSignatureEquivalence(boolean ignoreName) {
    this.ignoreName = ignoreName;
  }

  public static FieldSignatureEquivalence get() {
    return INSTANCE;
  }

  public static FieldSignatureEquivalence getEquivalenceIgnoreName() {
    return INSTANCE_IGNORE_NAME;
  }

  @Override
  protected boolean doEquivalent(DexField a, DexField b) {
    if (ignoreName) {
      return a.type.equals(b.type);
    }
    return a.name.equals(b.name) && a.type.equals(b.type);
  }

  @Override
  protected int doHash(DexField field) {
    if (ignoreName) {
      return field.type.hashCode();
    }
    return field.name.hashCode() * 31 + field.type.hashCode();
  }
}
