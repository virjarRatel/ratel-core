// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.sideeffect;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ValueMayDependOnEnvironmentAnalysis;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeNewArray;
import com.android.tools.r8.ir.code.NewArrayFilledData;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.OptionalBool;

public class ClassInitializerSideEffectAnalysis {

  public enum ClassInitializerSideEffect {
    SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED,
    SIDE_EFFECTS_THAT_CAN_BE_POSTPONED,
    NONE;

    public boolean canBePostponed() {
      return this != SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
    }

    public boolean isNone() {
      return this == NONE;
    }
  }

  /**
   * A class initializer cannot be postponed if it writes a static field of another class, or if any
   * non-static-put instructions may have side effects.
   */
  public static ClassInitializerSideEffect classInitializerCanBePostponed(
      AppView<?> appView, IRCode code) {
    DexType context = code.method.method.holder;
    OptionalBool controlFlowMayDependOnEnvironment = OptionalBool.unknown();
    boolean mayHaveSideEffects = false;

    ValueMayDependOnEnvironmentAnalysis environmentAnalysis =
        new ValueMayDependOnEnvironmentAnalysis(appView, code);
    for (Instruction instruction : code.instructions()) {
      // Array stores to a newly created array are only observable if they may throw, or if the
      // array content may depend on the environment.
      if (instruction.isArrayPut()) {
        ArrayPut arrayPut = instruction.asArrayPut();
        Value array = arrayPut.array().getAliasedValue();
        if (array.isPhi()
            || !array.definition.isCreatingArray()
            || environmentAnalysis.valueMayDependOnEnvironment(arrayPut.index())
            || environmentAnalysis.valueMayDependOnEnvironment(arrayPut.value())
            || arrayPut.instructionInstanceCanThrow(appView, context).isThrowing()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        if (controlFlowMayDependOnEnvironment.isUnknown()) {
          controlFlowMayDependOnEnvironment =
              OptionalBool.of(code.controlFlowMayDependOnEnvironment(environmentAnalysis));
        }
        if (controlFlowMayDependOnEnvironment.isTrue()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        continue;
      }

      // NewArrayFilledData is handled similarly to ArrayPut.
      if (instruction.isNewArrayFilledData()) {
        NewArrayFilledData newArrayFilledData = instruction.asNewArrayFilledData();
        Value array = newArrayFilledData.src();
        if (array.isPhi()
            || !array.definition.isCreatingArray()
            || newArrayFilledData.instructionInstanceCanThrow(appView, context).isThrowing()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        if (controlFlowMayDependOnEnvironment.isUnknown()) {
          controlFlowMayDependOnEnvironment =
              OptionalBool.of(code.controlFlowMayDependOnEnvironment(environmentAnalysis));
        }
        if (controlFlowMayDependOnEnvironment.isTrue()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        continue;
      }

      // Array creations are only observable if they may throw, or if the array content may depend
      // on the environment.
      if (instruction.isInvokeNewArray()) {
        InvokeNewArray invokeNewArray = instruction.asInvokeNewArray();
        if (invokeNewArray.instructionInstanceCanThrow(appView, context).isThrowing()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        for (Value argument : invokeNewArray.arguments()) {
          if (environmentAnalysis.valueMayDependOnEnvironment(argument)) {
            return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
          }
        }
        continue;
      }

      if (instruction.isNewArrayEmpty()) {
        if (instruction.instructionInstanceCanThrow(appView, context).isThrowing()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        continue;
      }

      if (instruction.isStaticPut()) {
        StaticPut staticPut = instruction.asStaticPut();
        DexEncodedField field = appView.appInfo().resolveField(staticPut.getField());
        if (field == null
            || field.field.holder != context
            || environmentAnalysis.valueMayDependOnEnvironment(staticPut.value())
            || instruction.instructionInstanceCanThrow(appView, context).isThrowing()) {
          return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
        }
        mayHaveSideEffects = true;
        continue;
      }

      // For other instructions, bail out if they may have side effects.
      if (instruction.instructionMayHaveSideEffects(appView, context)) {
        return ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CANNOT_BE_POSTPONED;
      }
    }

    return mayHaveSideEffects
        ? ClassInitializerSideEffect.SIDE_EFFECTS_THAT_CAN_BE_POSTPONED
        : ClassInitializerSideEffect.NONE;
  }
}
