// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.shaking.AppInfoWithLiveness;

// Determine whether the given method generates the deterministic output for the given same input.
// For now, this analysis is very conservative in the sense that it only allows methods that depend
// on arguments. To that end, the followings are not allowed.
//   1) any non-argument access, e.g., static-get, may result in different outputs.
//   2) even for argument access, states of instance or array can be different in any way.
//   3) if the current method invokes another method that is not deterministic, neither is it.
public class DeterminismAnalysis {

  public static boolean returnValueOnlyDependsOnArguments(
      AppView<AppInfoWithLiveness> appView, IRCode code) {
    for (Instruction instr : code.instructions()) {
      if (instr.outValue() == null || !instr.outValue().isUsed()) {
        continue;
      }
      if (instr.isStaticGet()) {
        // Unless we model the heap and take into account the snapshot per call site.
        return false;
      }
      if (instr.isInstanceGet()) {
        // Unless we trace the state of the instance.
        return false;
      }
      if (instr.isArrayGet() || instr.isArrayLength()) {
        // Unless we trace the state of the array.
        return false;
      }
      if (instr.isInvokeMethod()) {
        DexEncodedMethod target =
            instr.asInvokeMethod().lookupSingleTarget(appView, code.method.method.holder);
        if (target != null && target.getOptimizationInfo().returnValueOnlyDependsOnArguments()) {
          continue;
        }
        return false;
      }
      if (instr.isInvokeCustom() || instr.isInvokePolymorphic()) {
        // Unless we can nail down the single target for these invocations.
        return false;
      }
      if (instr.isCreatingInstanceOrArray()) {
        // Unless we determine the new instance/array is local.
        return false;
      }
      if (instr.isMoveException()) {
        return false;
      }
      // isArrayPut and isFieldPut are missed as they don't have out value.
      assert instr.isArgument()
          || instr.isAssume()
          || instr.isBinop()
          || instr.isUnop()
          || instr.isMonitor()
          || instr.isMove()
          || instr.isCheckCast()
          || instr.isInstanceOf()
          || instr.isConstInstruction()
          || instr.isJumpInstruction()
          || instr.isDebugInstruction()
          : "Instruction that impacts determinism: " + instr;
    }
    return true;
  }
}
