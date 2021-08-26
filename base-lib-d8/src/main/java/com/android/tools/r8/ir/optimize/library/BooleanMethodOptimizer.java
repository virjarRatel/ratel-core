// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.library;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.Value;
import java.util.Set;

public class BooleanMethodOptimizer implements LibraryMethodModelCollection {

  private final AppView<? extends AppInfoWithSubtyping> appView;
  private final DexItemFactory dexItemFactory;

  BooleanMethodOptimizer(AppView<? extends AppInfoWithSubtyping> appView) {
    this.appView = appView;
    this.dexItemFactory = appView.dexItemFactory();
  }

  @Override
  public DexType getType() {
    return dexItemFactory.boxedBooleanType;
  }

  @Override
  public void optimize(
      IRCode code,
      InstructionListIterator instructionIterator,
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      Set<Value> affectedValues) {
    if (singleTarget.method == dexItemFactory.booleanMembers.booleanValue) {
      optimizeBooleanValue(code, instructionIterator, invoke);
    } else if (singleTarget.method == dexItemFactory.booleanMembers.valueOf) {
      optimizeValueOf(code, instructionIterator, invoke, affectedValues);
    }
  }

  private void optimizeBooleanValue(
      IRCode code, InstructionListIterator instructionIterator, InvokeMethod invoke) {
    Value argument = invoke.arguments().get(0).getAliasedValue();
    if (!argument.isPhi()) {
      Instruction definition = argument.definition;
      if (definition.isStaticGet()) {
        StaticGet staticGet = definition.asStaticGet();
        DexField field = staticGet.getField();
        if (field == dexItemFactory.booleanMembers.TRUE) {
          instructionIterator.replaceCurrentInstructionWithConstInt(appView, code, 1);
        } else if (field == dexItemFactory.booleanMembers.FALSE) {
          instructionIterator.replaceCurrentInstructionWithConstInt(appView, code, 0);
        }
      }
    }
  }

  private void optimizeValueOf(
      IRCode code,
      InstructionListIterator instructionIterator,
      InvokeMethod invoke,
      Set<Value> affectedValues) {
    Value argument = invoke.arguments().get(0);
    AbstractValue abstractValue = argument.getAbstractValue(appView, code.method.method.holder);
    if (abstractValue.isSingleNumberValue()) {
      instructionIterator.replaceCurrentInstructionWithStaticGet(
          appView,
          code,
          abstractValue.asSingleNumberValue().getBooleanValue()
              ? dexItemFactory.booleanMembers.TRUE
              : dexItemFactory.booleanMembers.FALSE,
          affectedValues);
    }
  }
}
