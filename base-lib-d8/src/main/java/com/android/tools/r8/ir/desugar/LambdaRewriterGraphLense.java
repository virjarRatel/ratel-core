// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

class LambdaRewriterGraphLense extends NestedGraphLense {

  LambdaRewriterGraphLense(
      BiMap<DexMethod, DexMethod> methodMapping, GraphLense previous, DexItemFactory factory) {
    super(
        ImmutableMap.of(),
        methodMapping,
        ImmutableMap.of(),
        ImmutableBiMap.of(),
        methodMapping.inverse(),
        previous,
        factory);
  }

  @Override
  protected Type mapInvocationType(DexMethod newMethod, DexMethod originalMethod, Type type) {
    if (methodMap.get(originalMethod) == newMethod) {
      assert type == Type.VIRTUAL || type == Type.DIRECT;
      return Type.STATIC;
    }
    return super.mapInvocationType(newMethod, originalMethod, type);
  }

}
