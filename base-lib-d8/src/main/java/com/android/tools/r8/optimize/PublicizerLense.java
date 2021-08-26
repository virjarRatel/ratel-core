// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Set;

final class PublicizerLense extends NestedGraphLense {

  private final AppView appView;
  private final Set<DexMethod> publicizedMethods;

  private PublicizerLense(AppView appView, Set<DexMethod> publicizedMethods) {
    super(
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        null,
        null,
        appView.graphLense(),
        appView.dexItemFactory());
    this.appView = appView;
    this.publicizedMethods = publicizedMethods;
  }

  @Override
  protected boolean isLegitimateToHaveEmptyMappings() {
    // This lense does not map any DexItem's at all.
    // It will just tweak invoke type for publicized methods from invoke-direct to invoke-virtual.
    return true;
  }

  @Override
  public GraphLenseLookupResult lookupMethod(DexMethod method, DexMethod context, Type type) {
    GraphLenseLookupResult previous = previousLense.lookupMethod(method, context, type);
    method = previous.getMethod();
    type = previous.getType();
    if (type == Type.DIRECT && publicizedMethods.contains(method)) {
      assert publicizedMethodIsPresentOnHolder(method, context);
      return new GraphLenseLookupResult(method, Type.VIRTUAL);
    }
    return super.lookupMethod(method, context, type);
  }

  private boolean publicizedMethodIsPresentOnHolder(DexMethod method, DexMethod context) {
    GraphLenseLookupResult lookup =
        appView.graphLense().lookupMethod(method, context, Type.VIRTUAL);
    DexMethod signatureInCurrentWorld = lookup.getMethod();
    DexClass clazz = appView.definitionFor(signatureInCurrentWorld.holder);
    assert clazz != null;
    DexEncodedMethod actualEncodedTarget = clazz.lookupVirtualMethod(signatureInCurrentWorld);
    assert actualEncodedTarget != null;
    assert actualEncodedTarget.isPublicized();
    return true;
  }

  static PublicizedLenseBuilder createBuilder() {
    return new PublicizedLenseBuilder();
  }

  static class PublicizedLenseBuilder {
    private final Set<DexMethod> publicizedMethods = Sets.newIdentityHashSet();

    private PublicizedLenseBuilder() {
    }

    public GraphLense build(AppView appView) {
      if (publicizedMethods.isEmpty()) {
        return appView.graphLense();
      }
      return new PublicizerLense(appView, publicizedMethods);
    }

    public void add(DexMethod publicizedMethod) {
      publicizedMethods.add(publicizedMethod);
    }
  }
}
