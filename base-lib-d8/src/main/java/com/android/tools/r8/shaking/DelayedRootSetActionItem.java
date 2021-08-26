// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexEncodedMethod;
import java.util.function.Consumer;

public abstract class DelayedRootSetActionItem {

  public boolean isInterfaceMethodSyntheticBridgeAction() {
    return false;
  }

  public InterfaceMethodSyntheticBridgeAction asInterfaceMethodSyntheticBridgeAction() {
    return null;
  }

  public static class InterfaceMethodSyntheticBridgeAction extends DelayedRootSetActionItem {
    private final DexEncodedMethod methodToKeep;
    private final DexEncodedMethod singleTarget;
    private final Consumer<RootSetBuilder> action;

    InterfaceMethodSyntheticBridgeAction(
        DexEncodedMethod methodToKeep,
        DexEncodedMethod singleTarget,
        Consumer<RootSetBuilder> action) {
      this.methodToKeep = methodToKeep;
      this.singleTarget = singleTarget;
      this.action = action;
    }

    public DexEncodedMethod getMethodToKeep() {
      return methodToKeep;
    }

    public DexEncodedMethod getSingleTarget() {
      return singleTarget;
    }

    public Consumer<RootSetBuilder> getAction() {
      return action;
    }

    @Override
    public boolean isInterfaceMethodSyntheticBridgeAction() {
      return true;
    }

    @Override
    public InterfaceMethodSyntheticBridgeAction asInterfaceMethodSyntheticBridgeAction() {
      return this;
    }
  }
}
