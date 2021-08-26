// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

public interface DexDefinitionSupplier {

  DexDefinition definitionFor(DexReference reference);

  DexEncodedField definitionFor(DexField field);

  DexEncodedMethod definitionFor(DexMethod method);

  DexClass definitionFor(DexType type);

  DexProgramClass definitionForProgramType(DexType type);

  DexItemFactory dexItemFactory();
}
