// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldaccess;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.code.And;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.LogicalBinop;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.info.BitAccessInfo;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;

public class FieldBitAccessAnalysis {

  private final AppView<? extends AppInfoWithSubtyping> appView;

  public FieldBitAccessAnalysis(AppView<? extends AppInfoWithSubtyping> appView) {
    assert appView.enableWholeProgramOptimizations();
    this.appView = appView;
  }

  public void recordFieldAccesses(IRCode code, OptimizationFeedback feedback) {
    if (!code.metadata().mayHaveFieldInstruction()) {
      return;
    }

    for (Instruction instruction : code.instructions()) {
      if (instruction.isFieldInstruction()) {
        FieldInstruction fieldInstruction = instruction.asFieldInstruction();
        DexField field = fieldInstruction.getField();
        if (!field.type.isIntType()) {
          continue;
        }

        DexEncodedField encodedField = appView.appInfo().resolveField(field);
        if (encodedField == null || !encodedField.isProgramField(appView)) {
          continue;
        }

        if (BitAccessInfo.allBitsRead(encodedField.getOptimizationInfo().getReadBits())) {
          continue;
        }

        if (fieldInstruction.isFieldGet()) {
          feedback.markFieldBitsRead(encodedField, computeBitsRead(fieldInstruction, encodedField));
        }
      }
    }
  }

  private int computeBitsRead(FieldInstruction instruction, DexEncodedField encodedField) {
    Value outValue = instruction.outValue();
    if (outValue.numberOfPhiUsers() > 0) {
      // No need to track aliases, just give up.
      return BitAccessInfo.getAllBitsReadValue();
    }

    int bitsRead = BitAccessInfo.getNoBitsReadValue();
    for (Instruction user : outValue.uniqueUsers()) {
      if (isOnlyUsedToUpdateFieldValue(user, encodedField)) {
        continue;
      }
      if (user.isAnd()) {
        And andInstruction = user.asAnd();
        Value other =
            andInstruction
                .inValues()
                .get(1 - andInstruction.inValues().indexOf(outValue))
                .getAliasedValue();
        if (other.isPhi() || !other.definition.isConstNumber()) {
          // Could potentially read all bits, give up.
          return BitAccessInfo.getAllBitsReadValue();
        }
        bitsRead |= other.definition.asConstNumber().getIntValue();
      } else {
        // Unknown usage, give up.
        return BitAccessInfo.getAllBitsReadValue();
      }
    }
    return bitsRead;
  }

  private boolean isOnlyUsedToUpdateFieldValue(Instruction user, DexEncodedField encodedField) {
    if (user.isLogicalBinop()) {
      LogicalBinop binop = user.asLogicalBinop();
      Value outValue = binop.outValue();
      if (outValue.numberOfPhiUsers() > 0) {
        // Not tracking aliased values, give up.
        return false;
      }
      for (Instruction indirectUser : outValue.uniqueUsers()) {
        if (!indirectUser.isFieldPut()) {
          return false;
        }
        if (indirectUser.asFieldInstruction().getField() != encodedField.field) {
          return false;
        }
      }
      return true;
    }
    if (user.isFieldPut()) {
      FieldInstruction fieldInstruction = user.asFieldInstruction();
      if (fieldInstruction.getField() == encodedField.field) {
        return true;
      }
    }
    return false;
  }
}
