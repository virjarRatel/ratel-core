// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MemberRebindingLense extends NestedGraphLense {

  public static class Builder extends NestedGraphLense.Builder {

    private final AppView<?> appView;

    protected Builder(AppView<?> appView) {
      this.appView = appView;
    }

    public GraphLense build(GraphLense previousLense) {
      assert typeMap.isEmpty();
      if (methodMap.isEmpty() && fieldMap.isEmpty()) {
        return previousLense;
      }
      return new MemberRebindingLense(appView, methodMap, fieldMap, previousLense);
    }
  }

  private final AppView<?> appView;

  public MemberRebindingLense(
      AppView<?> appView,
      Map<DexMethod, DexMethod> methodMap,
      Map<DexField, DexField> fieldMap,
      GraphLense previousLense) {
    super(
        ImmutableMap.of(),
        methodMap,
        fieldMap,
        null,
        null,
        previousLense,
        appView.dexItemFactory());
    this.appView = appView;
  }

  public static Builder builder(AppView<?> appView) {
    return new Builder(appView);
  }

  @Override
  protected Type mapInvocationType(DexMethod newMethod, DexMethod originalMethod, Type type) {
    return super.mapVirtualInterfaceInvocationTypes(appView, newMethod, originalMethod, type);
  }
}
