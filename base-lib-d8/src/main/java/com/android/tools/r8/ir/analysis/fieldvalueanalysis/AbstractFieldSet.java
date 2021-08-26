// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldvalueanalysis;

import com.android.tools.r8.graph.DexEncodedField;

/**
 * Implements a lifted subset lattice for fields.
 *
 * <p>An element can either be:
 *
 * <ol>
 *   <li>bottom, represented by {@link EmptyFieldSet},
 *   <li>a (possibly empty) set of fields, represented by {@link ConcreteMutableFieldSet}, or
 *   <li>top, represented by {@link UnknownFieldSet}.
 * </ol>
 *
 * <p>Note that this class currently does not contain a {@code join()} method. Instead, the {@link
 * ConcreteMutableFieldSet} has an {@link ConcreteMutableFieldSet#add(DexEncodedField)} and {@link
 * ConcreteMutableFieldSet#addAll(ConcreteMutableFieldSet)} method. This is intentional, since the
 * use of {@code join()} could lead to an excessive amount of set copying under the hood.
 */
public abstract class AbstractFieldSet {

  public boolean isConcreteFieldSet() {
    return false;
  }

  public ConcreteMutableFieldSet asConcreteFieldSet() {
    return null;
  }

  public boolean isKnownFieldSet() {
    return false;
  }

  public KnownFieldSet asKnownFieldSet() {
    return null;
  }

  public abstract boolean contains(DexEncodedField field);

  public boolean isBottom() {
    return false;
  }

  public boolean isTop() {
    return false;
  }

  public final boolean lessThanOrEqual(AbstractFieldSet other) {
    if (isBottom() || other.isTop()) {
      return true;
    }
    if (isTop() || other.isBottom()) {
      return false;
    }
    assert isConcreteFieldSet();
    assert other.isConcreteFieldSet();
    return other.asConcreteFieldSet().getFields().containsAll(asConcreteFieldSet().getFields());
  }

  public final boolean strictlyLessThan(AbstractFieldSet other) {
    return lessThanOrEqual(other) && !equals(other);
  }
}
