// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexItemFactory;

public abstract class ReferenceTypeLatticeElement extends TypeLatticeElement {

  private static class NullLatticeElement extends ReferenceTypeLatticeElement {

    NullLatticeElement(Nullability nullability) {
      super(nullability);
    }

    @Override
    public ReferenceTypeLatticeElement getOrCreateVariant(Nullability nullability) {
      return nullability.isNullable() ? NULL_INSTANCE : NULL_BOTTOM_INSTANCE;
    }

    private static NullLatticeElement create() {
      return new NullLatticeElement(Nullability.definitelyNull());
    }

    private static NullLatticeElement createBottom() {
      return new NullLatticeElement(Nullability.bottom());
    }

    @Override
    public boolean isNullType() {
      return true;
    }

    @Override
    public String toString() {
      return nullability.toString() + " " + DexItemFactory.nullValueType.toString();
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof NullLatticeElement)) {
        return false;
      }
      return true;
    }
  }

  private static final ReferenceTypeLatticeElement NULL_INSTANCE = NullLatticeElement.create();
  private static final ReferenceTypeLatticeElement NULL_BOTTOM_INSTANCE =
      NullLatticeElement.createBottom();

  final Nullability nullability;

  ReferenceTypeLatticeElement(Nullability nullability) {
    this.nullability = nullability;
  }

  @Override
  public Nullability nullability() {
    return nullability;
  }

  static ReferenceTypeLatticeElement getNullTypeLatticeElement() {
    return NULL_INSTANCE;
  }

  public abstract ReferenceTypeLatticeElement getOrCreateVariant(Nullability nullability);

  public TypeLatticeElement asMeetWithNotNull() {
    return getOrCreateVariant(nullability.meet(Nullability.definitelyNotNull()));
  }

  public TypeLatticeElement asDefinitelyNotNull() {
    return getOrCreateVariant(Nullability.definitelyNotNull());
  }

  @Override
  public boolean isReference() {
    return true;
  }

  @Override
  public ReferenceTypeLatticeElement asReferenceTypeLatticeElement() {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    throw new Unreachable("Should be implemented on each sub type");
  }

  @Override
  public int hashCode() {
    throw new Unreachable("Should be implemented on each sub type");
  }
}
