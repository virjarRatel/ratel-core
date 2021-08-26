// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.conversion.CfBuilder;
import java.util.ArrayList;
import java.util.List;

public class CfTryCatch {
  public final CfLabel start;
  public final CfLabel end;
  public final List<DexType> guards;
  public final List<CfLabel> targets;

  public CfTryCatch(CfLabel start, CfLabel end, List<DexType> guards, List<CfLabel> targets) {
    this.start = start;
    this.end = end;
    this.guards = guards;
    this.targets = targets;
    assert verifyAllNonNull(guards);
  }

  private static boolean verifyAllNonNull(List<DexType> types) {
    for (DexType type : types) {
      assert type != null;
    }
    return true;
  }

  public static CfTryCatch fromBuilder(
      CfLabel start,
      CfLabel end,
      CatchHandlers<BasicBlock> handlers,
      CfBuilder builder) {
    List<DexType> guards = handlers.getGuards();
    ArrayList<CfLabel> targets = new ArrayList<>(handlers.getAllTargets().size());
    for (BasicBlock block : handlers.getAllTargets()) {
      targets.add(builder.getLabel(block));
    }
    return new CfTryCatch(start, end, guards, targets);
  }
}
