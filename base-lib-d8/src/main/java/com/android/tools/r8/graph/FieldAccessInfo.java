// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Provides immutable access to {@link FieldAccessInfoImpl}. */
public interface FieldAccessInfo {

  FieldAccessInfoImpl asMutable();

  DexField getField();

  DexEncodedMethod getUniqueReadContext();

  void forEachIndirectAccess(Consumer<DexField> consumer);

  void forEachIndirectAccessWithContexts(BiConsumer<DexField, Set<DexEncodedMethod>> consumer);

  void forEachReadContext(Consumer<DexMethod> consumer);

  boolean isRead();

  boolean isReadOnlyIn(DexEncodedMethod method);

  boolean isWritten();

  boolean isWrittenInMethodSatisfying(Predicate<DexEncodedMethod> predicate);

  boolean isWrittenOutside(DexEncodedMethod method);
}
