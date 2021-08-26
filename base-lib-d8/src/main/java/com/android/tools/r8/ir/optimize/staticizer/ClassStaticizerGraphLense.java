// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.staticizer;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;

class ClassStaticizerGraphLense extends NestedGraphLense {

  ClassStaticizerGraphLense(
      AppView<?> appView,
      BiMap<DexField, DexField> fieldMapping,
      BiMap<DexMethod, DexMethod> methodMapping) {
    super(
        ImmutableMap.of(),
        methodMapping,
        fieldMapping,
        fieldMapping.inverse(),
        methodMapping.inverse(),
        appView.graphLense(),
        appView.dexItemFactory());
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
