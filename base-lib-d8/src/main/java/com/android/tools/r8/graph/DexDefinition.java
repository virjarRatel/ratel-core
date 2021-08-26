// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A common interface for {@link DexClass}, {@link DexEncodedField}, and {@link DexEncodedMethod}.
 */
public abstract class DexDefinition extends DexItem {

  @Override
  public boolean isDexDefinition() {
    return true;
  }

  @Override
  public DexDefinition asDexDefinition() {
    return this;
  }

  public boolean isDexClass() {
    return false;
  }

  public DexClass asDexClass() {
    return null;
  }

  public boolean isDexEncodedField() {
    return false;
  }

  public DexEncodedField asDexEncodedField() {
    return null;
  }

  public boolean isDexEncodedMethod() {
    return false;
  }

  public DexEncodedMethod asDexEncodedMethod() {
    return null;
  }

  public static Stream<DexDefinition> filterDexDefinition(Stream<DexItem> stream) {
    return DexItem.filter(stream, DexDefinition.class);
  }

  public abstract DexReference toReference();

  public static Stream<DexReference> mapToReference(Stream<DexDefinition> definitions) {
    return definitions.map(DexDefinition::toReference);
  }

  private static <T extends DexDefinition> Stream<T> filter(
      Stream<DexDefinition> stream,
      Predicate<DexDefinition> pred,
      Function<DexDefinition, T> f) {
    return stream.filter(pred).map(f);
  }

  public static Stream<DexClass> filterDexClass(Stream<DexDefinition> stream) {
    return filter(stream, DexDefinition::isDexClass, DexDefinition::asDexClass);
  }

  public static Stream<DexEncodedField> filterDexEncodedField(Stream<DexDefinition> stream) {
    return filter(stream, DexDefinition::isDexEncodedField, DexDefinition::asDexEncodedField);
  }

  public static Stream<DexEncodedMethod> filterDexEncodedMethod(Stream<DexDefinition> stream) {
    return filter(stream, DexDefinition::isDexEncodedMethod, DexDefinition::asDexEncodedMethod);
  }

  public abstract boolean isStatic();

  public abstract boolean isStaticMember();
}
