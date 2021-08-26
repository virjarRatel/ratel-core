// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import java.util.function.Consumer;

/** Provides immutable access to {@link FieldAccessInfoCollectionImpl}. */
public interface FieldAccessInfoCollection<T extends FieldAccessInfo> {

  T get(DexField field);

  void forEach(Consumer<T> consumer);
}
