// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoEnqueuerExtension;
import com.android.tools.r8.shaking.DefaultEnqueuerUseRegistry;
import com.android.tools.r8.shaking.Enqueuer;
import com.android.tools.r8.shaking.EnqueuerUseRegistryFactory;

public class ProtoEnqueuerUseRegistry extends DefaultEnqueuerUseRegistry {

  private static final EnqueuerUseRegistryFactory FACTORY = ProtoEnqueuerUseRegistry::new;

  private final ProtoReferences references;

  public ProtoEnqueuerUseRegistry(
      AppView<?> appView,
      DexProgramClass currentHolder,
      DexEncodedMethod currentMethod,
      Enqueuer enqueuer) {
    super(appView, currentHolder, currentMethod, enqueuer);
    this.references = appView.protoShrinker().references;
  }

  public static EnqueuerUseRegistryFactory getFactory() {
    return FACTORY;
  }

  /**
   * Unlike {@link DefaultEnqueuerUseRegistry#registerConstClass(DexType)}, this method does not
   * trace any const-class instructions in every implementation of dynamicMethod().
   *
   * <p>The const-class instructions that remain after the proto schema has been optimized will be
   * traced manually by {@link ProtoEnqueuerExtension#tracePendingInstructionsInDynamicMethods}.
   */
  @Override
  public boolean registerConstClass(DexType type) {
    if (references.isDynamicMethod(currentMethod)) {
      return false;
    }
    return super.registerConstClass(type);
  }

  /**
   * Unlike {@link DefaultEnqueuerUseRegistry#registerStaticFieldRead(DexField)}, this method does
   * not trace any static-get instructions in every implementation of dynamicMethod().
   *
   * <p>The static-get instructions that remain after the proto schema has been optimized will be
   * traced manually by {@link ProtoEnqueuerExtension#tracePendingInstructionsInDynamicMethods}.
   */
  @Override
  public boolean registerStaticFieldRead(DexField field) {
    if (references.isDynamicMethod(currentMethod)) {
      return false;
    }
    return super.registerStaticFieldRead(field);
  }
}
