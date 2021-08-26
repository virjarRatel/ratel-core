// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.Value;
import java.util.Set;

public class InlinerUtils {

  public static void collectAllMonitorEnterValues(
      IRCode code,
      Set<DexType> constantMonitorEnterValues,
      Set<Value> nonConstantMonitorEnterValues) {
    assert code.metadata().mayHaveMonitorInstruction();
    for (Monitor monitor : code.<Monitor>instructions(Instruction::isMonitorEnter)) {
      Value monitorEnterValue = monitor.object().getAliasedValue();
      addMonitorEnterValue(
          monitorEnterValue, constantMonitorEnterValues, nonConstantMonitorEnterValues);
    }
  }

  public static void addMonitorEnterValue(
      Value monitorEnterValue,
      Set<DexType> constantMonitorEnterValues,
      Set<Value> nonConstantMonitorEnterValues) {
    assert !monitorEnterValue.hasAliasedValue();
    if (monitorEnterValue.isPhi() || !monitorEnterValue.definition.isConstClass()) {
      nonConstantMonitorEnterValues.add(monitorEnterValue);
    } else {
      constantMonitorEnterValues.add(monitorEnterValue.definition.asConstClass().getValue());
    }
  }
}
