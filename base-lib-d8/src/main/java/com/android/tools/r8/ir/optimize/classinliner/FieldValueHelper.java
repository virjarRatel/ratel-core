// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.classinliner;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;
import static com.android.tools.r8.ir.analysis.type.Nullability.maybeNull;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Phi.RegisterReadType;
import com.android.tools.r8.ir.code.Value;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// Describes and caches what values are supposed to be used instead of field reads.
final class FieldValueHelper {
  private final DexField field;
  private final IRCode code;
  private final Instruction root;
  private final AppView<?> appView;

  private Value defaultValue = null;
  private final Map<BasicBlock, Value> ins = new IdentityHashMap<>();
  private final Map<BasicBlock, Value> outs = new IdentityHashMap<>();

  FieldValueHelper(DexField field, IRCode code, Instruction root, AppView<?> appView) {
    this.field = field;
    this.code = code;
    this.root = root;
    this.appView = appView;
    // Verify that `root` is not aliased.
    assert root.hasOutValue();
    assert root.outValue() == root.outValue().getAliasedValue();
  }

  void replaceValue(Value oldValue, Value newValue) {
    for (Entry<BasicBlock, Value> entry : ins.entrySet()) {
      if (entry.getValue() == oldValue) {
        entry.setValue(newValue);
      }
    }
    for (Entry<BasicBlock, Value> entry : outs.entrySet()) {
      if (entry.getValue() == oldValue) {
        entry.setValue(newValue);
      }
    }
  }

  Value getValueForFieldRead(BasicBlock block, Instruction valueUser) {
    assert valueUser != null;
    Value value = getValueDefinedInTheBlock(block, valueUser);
    return value != null ? value : getOrCreateInValue(block);
  }

  private Value getOrCreateOutValue(BasicBlock block) {
    Value value = outs.get(block);
    if (value != null) {
      return value;
    }

    value = getValueDefinedInTheBlock(block, null);
    if (value == null) {
      // No value defined in the block.
      value = getOrCreateInValue(block);
    }

    assert value != null;
    outs.put(block, value);
    return value;
  }

  private Value getOrCreateInValue(BasicBlock block) {
    Value value = ins.get(block);
    if (value != null) {
      return value;
    }

    List<BasicBlock> predecessors = block.getPredecessors();
    if (predecessors.size() == 1) {
      value = getOrCreateOutValue(predecessors.get(0));
      ins.put(block, value);
    } else {
      // Create phi, add it to the block, cache in ins map for future use.
      Phi phi =
          new Phi(
              code.valueNumberGenerator.next(),
              block,
              TypeLatticeElement.fromDexType(field.type, maybeNull(), appView),
              null,
              RegisterReadType.NORMAL);
      ins.put(block, phi);

      List<Value> operands = new ArrayList<>();
      for (BasicBlock predecessor : block.getPredecessors()) {
        operands.add(getOrCreateOutValue(predecessor));
      }
      // Add phi, but don't remove trivial phis; since we cache the phi
      // we just created for future use we should delay removing trivial
      // phis until we are done with replacing fields reads.
      phi.addOperands(operands, false);

      TypeLatticeElement phiType = phi.computePhiType(appView);
      assert phiType.lessThanOrEqual(phi.getTypeLattice(), appView);
      phi.setTypeLattice(phiType);

      value = phi;
    }

    assert value != null;
    return value;
  }

  private Value getValueDefinedInTheBlock(BasicBlock block, Instruction stopAt) {
    InstructionIterator iterator =
        stopAt == null ? block.iterator(block.getInstructions().size()) : block.iterator(stopAt);

    Instruction valueProducingInsn = null;
    while (iterator.hasPrevious()) {
      Instruction instruction = iterator.previous();
      assert instruction != null;

      if (instruction == root
          || (instruction.isInstancePut()
              && instruction.asInstancePut().getField() == field
              && instruction.asInstancePut().object().getAliasedValue() == root.outValue())) {
        valueProducingInsn = instruction;
        break;
      }
    }

    if (valueProducingInsn == null) {
      return null;
    }
    if (valueProducingInsn.isInstancePut()) {
      return valueProducingInsn.asInstancePut().value();
    }

    assert root == valueProducingInsn;
    if (defaultValue == null) {
      InstructionListIterator it = block.listIterator(code, root);
      // If we met newInstance it means that default value is supposed to be used.
      if (field.type.isPrimitiveType()) {
        defaultValue = code.createValue(
            TypeLatticeElement.fromDexType(field.type, definitelyNotNull(), appView));
        ConstNumber defaultValueInsn = new ConstNumber(defaultValue, 0);
        defaultValueInsn.setPosition(root.getPosition());
        it.add(defaultValueInsn);
      } else {
        defaultValue = it.insertConstNullInstruction(code, appView.options());
      }
    }
    return defaultValue;
  }
}
