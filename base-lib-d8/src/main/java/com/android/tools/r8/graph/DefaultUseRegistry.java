// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

public class DefaultUseRegistry extends UseRegistry {

  public DefaultUseRegistry(DexItemFactory factory) {
    super(factory);
  }

  @Override
  public boolean registerInvokeVirtual(DexMethod method) {
    return true;
  }

  @Override
  public boolean registerInvokeDirect(DexMethod method) {
    return true;
  }

  @Override
  public boolean registerInvokeStatic(DexMethod method) {
    return true;
  }

  @Override
  public boolean registerInvokeInterface(DexMethod method) {
    return true;
  }

  @Override
  public boolean registerInvokeSuper(DexMethod method) {
    return true;
  }

  @Override
  public boolean registerInstanceFieldWrite(DexField field) {
    return true;
  }

  @Override
  public boolean registerInstanceFieldRead(DexField field) {
    return true;
  }

  @Override
  public boolean registerNewInstance(DexType type) {
    return true;
  }

  @Override
  public boolean registerStaticFieldRead(DexField field) {
    return true;
  }

  @Override
  public boolean registerStaticFieldWrite(DexField field) {
    return true;
  }

  @Override
  public boolean registerTypeReference(DexType type) {
    return true;
  }
}
