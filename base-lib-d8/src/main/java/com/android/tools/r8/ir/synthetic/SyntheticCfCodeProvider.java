// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class SyntheticCfCodeProvider {

  protected final AppView<?> appView;
  private final DexType holder;

  protected SyntheticCfCodeProvider(AppView<?> appView, DexType holder) {
    this.appView = appView;
    this.holder = holder;
  }

  public abstract CfCode generateCfCode();

  protected CfCode standardCfCodeFromInstructions(List<CfInstruction> instructions) {
    return new CfCode(
        holder,
        defaultMaxStack(),
        defaultMaxLocals(),
        instructions,
        defaultTryCatchs(),
        ImmutableList.of());
  }

  protected int defaultMaxStack() {
    return 16;
  }

  protected int defaultMaxLocals() {
    return 16;
  }

  protected List<CfTryCatch> defaultTryCatchs() {
    return ImmutableList.of();
  }
}
