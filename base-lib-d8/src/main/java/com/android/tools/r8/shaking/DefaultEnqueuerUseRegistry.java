// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;

public class DefaultEnqueuerUseRegistry extends UseRegistry {

  private final DexProgramClass currentHolder;
  protected final DexEncodedMethod currentMethod;
  private final Enqueuer enqueuer;

  public DefaultEnqueuerUseRegistry(
      AppView<?> appView,
      DexProgramClass currentHolder,
      DexEncodedMethod currentMethod,
      Enqueuer enqueuer) {
    super(appView.dexItemFactory());
    assert currentHolder.type == currentMethod.method.holder;
    this.currentHolder = currentHolder;
    this.currentMethod = currentMethod;
    this.enqueuer = enqueuer;
  }

  @Override
  public boolean registerInvokeVirtual(DexMethod invokedMethod) {
    return enqueuer.traceInvokeVirtual(invokedMethod, currentHolder, currentMethod);
  }

  @Override
  public boolean registerInvokeDirect(DexMethod invokedMethod) {
    return enqueuer.traceInvokeDirect(invokedMethod, currentHolder, currentMethod);
  }

  @Override
  public boolean registerInvokeStatic(DexMethod invokedMethod) {
    return enqueuer.traceInvokeStatic(invokedMethod, currentHolder, currentMethod);
  }

  @Override
  public boolean registerInvokeInterface(DexMethod invokedMethod) {
    return enqueuer.traceInvokeInterface(invokedMethod, currentHolder, currentMethod);
  }

  @Override
  public boolean registerInvokeSuper(DexMethod invokedMethod) {
    return enqueuer.traceInvokeSuper(invokedMethod, currentHolder, currentMethod);
  }

  @Override
  public boolean registerInstanceFieldWrite(DexField field) {
    return enqueuer.traceInstanceFieldWrite(field, currentMethod);
  }

  @Override
  public boolean registerInstanceFieldRead(DexField field) {
    return enqueuer.traceInstanceFieldRead(field, currentMethod);
  }

  @Override
  public boolean registerNewInstance(DexType type) {
    return enqueuer.traceNewInstance(type, currentMethod);
  }

  @Override
  public boolean registerStaticFieldRead(DexField field) {
    return enqueuer.traceStaticFieldRead(field, currentMethod);
  }

  @Override
  public boolean registerStaticFieldWrite(DexField field) {
    return enqueuer.traceStaticFieldWrite(field, currentMethod);
  }

  @Override
  public boolean registerConstClass(DexType type) {
    return enqueuer.traceConstClass(type, currentMethod);
  }

  @Override
  public boolean registerCheckCast(DexType type) {
    return enqueuer.traceCheckCast(type, currentMethod);
  }

  @Override
  public boolean registerTypeReference(DexType type) {
    return enqueuer.traceTypeReference(type, currentMethod);
  }

  @Override
  public void registerMethodHandle(DexMethodHandle methodHandle, MethodHandleUse use) {
    super.registerMethodHandle(methodHandle, use);
    enqueuer.traceMethodHandle(methodHandle, use, currentMethod);
  }

  @Override
  public void registerCallSite(DexCallSite callSite) {
    super.registerCallSite(callSite);
    enqueuer.traceCallSite(callSite, currentMethod);
  }
}
