// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.type;

import java.util.function.BiFunction;
import java.util.function.Function;

public class NullabilityVariants<T extends ReferenceTypeLatticeElement> {

  private T maybeNullVariant;
  private T definitelyNullVariant;
  private T definitelyNotNullVariant;
  private T bottomVariant;

  public static <T extends ReferenceTypeLatticeElement> T create(
      Nullability nullability, Function<NullabilityVariants<T>, T> callback) {
    NullabilityVariants<T> variants = new NullabilityVariants<>();
    T newElement = callback.apply(variants);
    variants.set(nullability, newElement);
    return newElement;
  }

  private void set(Nullability nullability, T element) {
    if (nullability == Nullability.maybeNull()) {
      maybeNullVariant = element;
    } else if (nullability == Nullability.definitelyNull()) {
      definitelyNullVariant = element;
    } else if (nullability == Nullability.definitelyNotNull()) {
      definitelyNotNullVariant = element;
    } else {
      assert nullability == Nullability.bottom();
      bottomVariant = element;
    }
  }

  T get(Nullability nullability) {
    if (nullability == Nullability.maybeNull()) {
      return maybeNullVariant;
    } else if (nullability == Nullability.definitelyNull()) {
      return definitelyNullVariant;
    } else if (nullability == Nullability.definitelyNotNull()) {
      return definitelyNotNullVariant;
    } else {
      assert nullability == Nullability.bottom();
      return bottomVariant;
    }
  }

  T getOrCreateElement(
      Nullability nullability, BiFunction<Nullability, NullabilityVariants<T>, T> creator) {
    T element = get(nullability);
    if (element != null) {
      return element;
    }
    synchronized (this) {
      element = get(nullability);
      if (element != null) {
        return element;
      }
      element = creator.apply(nullability, this);
      assert element != null;
      set(nullability, element);
      return element;
    }
  }
}
