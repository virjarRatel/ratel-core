// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

public interface ClassHierarchy extends DexDefinitionSupplier {
  boolean hasSubtypes(DexType type);

  boolean isDirectSubtype(DexType subtype, DexType supertype);

  boolean isSubtype(DexType subtype, DexType supertype);
}
