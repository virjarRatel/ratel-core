// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A common interface for {@link DexType}, {@link DexField}, and {@link DexMethod}.
 */
public abstract class DexReference extends IndexedDexItem {

  @Override
  public boolean isDexReference() {
    return true;
  }

  @Override
  public DexReference asDexReference() {
    return this;
  }

  public boolean isDexType() {
    return false;
  }

  public DexType asDexType() {
    return null;
  }

  public boolean isDescriptor() {
    return false;
  }

  public Descriptor asDescriptor() {
    return null;
  }

  public boolean isDexField() {
    return false;
  }

  public DexField asDexField() {
    return null;
  }

  public boolean isDexMethod() {
    return false;
  }

  public DexMethod asDexMethod() {
    return null;
  }

  public static Stream<DexReference> filterDexReference(Stream<DexItem> stream) {
    return DexItem.filter(stream, DexReference.class);
  }

  public DexDefinition toDefinition(AppInfo appInfo) {
    if (isDexType()) {
      return appInfo.definitionFor(asDexType());
    } else if (isDexField()) {
      return appInfo.definitionFor(asDexField());
    } else {
      assert isDexMethod();
      return appInfo.definitionFor(asDexMethod());
    }
  }

  public static Stream<DexDefinition> mapToDefinition(
      Stream<DexReference> references, AppInfo appInfo) {
    return references.map(r -> r.toDefinition(appInfo)).filter(Objects::nonNull);
  }

  private static <T extends DexReference> Stream<T> filter(
      Stream<DexReference> stream,
      Predicate<DexReference> pred,
      Function<DexReference, T> f) {
    return stream.filter(pred).map(f);
  }

  public static Stream<DexType> filterDexType(Stream<DexReference> stream) {
    return filter(stream, DexReference::isDexType, DexReference::asDexType);
  }

  public static Stream<DexField> filterDexField(Stream<DexReference> stream) {
    return filter(stream, DexReference::isDexField, DexReference::asDexField);
  }

  public static Stream<DexMethod> filterDexMethod(Stream<DexReference> stream) {
    return filter(stream, DexReference::isDexMethod, DexReference::asDexMethod);
  }
}
