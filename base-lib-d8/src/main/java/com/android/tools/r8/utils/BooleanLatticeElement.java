// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

public abstract class BooleanLatticeElement {

  public static final BooleanLatticeElement BOTTOM =
      new BooleanLatticeElement() {

        @Override
        public OptionalBool asOptionalBool() {
          throw new IllegalStateException("BooleanLatticeElement.BOTTOM is not an OptionalBool");
        }

        @Override
        public boolean isBottom() {
          return true;
        }

        @Override
        public String toString() {
          return "bottom";
        }
      };

  BooleanLatticeElement() {}

  public abstract OptionalBool asOptionalBool();

  public boolean isBottom() {
    return false;
  }

  public boolean isTrue() {
    return false;
  }

  public boolean isFalse() {
    return false;
  }

  public boolean isUnknown() {
    return false;
  }

  public boolean isPossiblyTrue() {
    return isTrue() || isUnknown();
  }

  public boolean isPossiblyFalse() {
    return isFalse() || isUnknown();
  }

  public BooleanLatticeElement join(BooleanLatticeElement other) {
    if (this == other || other.isBottom() || isUnknown()) {
      return this;
    }
    if (isBottom() || other.isUnknown()) {
      return other;
    }
    assert isTrue() || isFalse();
    assert other.isTrue() || other.isFalse();
    return OptionalBool.UNKNOWN;
  }

  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  // Force all subtypes to implement toString().
  @Override
  public abstract String toString();
}
