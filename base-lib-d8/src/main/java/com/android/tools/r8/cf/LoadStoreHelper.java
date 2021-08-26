// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.ConstInstruction;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Pop;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.StackValue;
import com.android.tools.r8.ir.code.Store;
import com.android.tools.r8.ir.code.Value;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class LoadStoreHelper {

  private final AppView<?> appView;
  private final IRCode code;
  private final TypeVerificationHelper typesHelper;

  private Map<Value, ConstInstruction> clonableConstants = null;
  private ListIterator<BasicBlock> blockIterator = null;

  public LoadStoreHelper(AppView<?> appView, IRCode code, TypeVerificationHelper typesHelper) {
    this.appView = appView;
    this.code = code;
    this.typesHelper = typesHelper;
  }

  private static boolean hasLocalInfoOrUsersOutsideThisBlock(Value value, BasicBlock block) {
    if (value.hasLocalInfo()) {
      return true;
    }
    if (value.numberOfPhiUsers() > 0) {
      return true;
    }
    for (Instruction instruction : value.uniqueUsers()) {
      if (instruction.getBlock() != block) {
        return true;
      }
    }
    return false;
  }

  private static boolean isConstInstructionAlwaysThreeBytes(ConstInstruction instr) {
    if (instr.isConstNumber()) {
      ConstNumber constNumber = instr.asConstNumber();
      switch (instr.outType()) {
        case OBJECT:
        case INT:
        case FLOAT:
          return false;
        case LONG:
          {
            long number = constNumber.getLongValue();
            return number != 0 && number != 1;
          }
        case DOUBLE:
          {
            double number = constNumber.getDoubleValue();
            return number != 0.0f && number != 1.0f;
          }
        default:
          throw new Unreachable();
      }
    }
    assert instr.isConstClass()
        || instr.isConstMethodHandle()
        || instr.isConstMethodType()
        || instr.isConstString()
        || instr.isDexItemBasedConstString();
    return false;
  }

  private static boolean canRemoveConstInstruction(ConstInstruction instr, BasicBlock block) {
    Value value = instr.outValue();
    return !hasLocalInfoOrUsersOutsideThisBlock(value, block)
        && (value.numberOfUsers() <= 1 || !isConstInstructionAlwaysThreeBytes(instr));
  }

  public void insertLoadsAndStores() {
    clonableConstants = new IdentityHashMap<>();
    blockIterator = code.listIterator();
    while (blockIterator.hasNext()) {
      InstructionListIterator it = blockIterator.next().listIterator(code);
      while (it.hasNext()) {
        it.next().insertLoadAndStores(it, this);
      }
      clonableConstants.clear();
    }
    clonableConstants = null;
    blockIterator = null;
  }

  public void insertPhiMoves(CfRegisterAllocator allocator) {
    // Insert phi stores in all predecessors.
    for (BasicBlock block : code.blocks) {
      if (!block.getPhis().isEmpty()) {
        // TODO(zerny): Phi's at an exception block must be dealt with at block entry.
        assert !block.entry().isMoveException();
        for (int predIndex = 0; predIndex < block.getPredecessors().size(); predIndex++) {
          BasicBlock pred = block.getPredecessors().get(predIndex);
          List<Phi> phis = block.getPhis();
          List<PhiMove> moves = new ArrayList<>(phis.size());
          for (Phi phi : phis) {
            if (!phi.needsRegister()) {
              continue;
            }
            Value value = phi.getOperand(predIndex);
            if (allocator.getRegisterForValue(phi) != allocator.getRegisterForValue(value)) {
              moves.add(new PhiMove(phi, value));
            }
          }
          InstructionListIterator it = pred.listIterator(code, pred.getInstructions().size());
          Instruction exit = it.previous();
          assert pred.exit() == exit;
          movePhis(moves, it, exit.getPosition());
        }
        allocator.addToLiveAtEntrySet(block, block.getPhis());
      }
    }
    code.blocks.forEach(BasicBlock::clearUserInfo);
  }

  private StackValue createStackValue(Value value, int height) {
    return StackValue.create(typesHelper.getTypeInfo(value), height, appView);
  }

  private StackValue createStackValue(DexType type, int height) {
    return StackValue.create(typesHelper.createInitializedType(type), height, appView);
  }

  public void loadInValues(Instruction instruction, InstructionListIterator it) {
    int topOfStack = 0;
    it.previous();
    for (int i = 0; i < instruction.inValues().size(); i++) {
      Value value = instruction.inValues().get(i);
      StackValue stackValue = createStackValue(value, topOfStack++);
      assert clonableConstants != null;
      ConstInstruction constInstruction = clonableConstants.get(value);
      if (constInstruction != null) {
        ConstInstruction clonedConstInstruction =
            ConstInstruction.copyOf(stackValue, constInstruction);
        add(clonedConstInstruction, instruction, it);
      } else {
        add(load(stackValue, value), instruction, it);
      }
      instruction.replaceValue(i, stackValue);
    }
    it.next();
  }

  public void storeOutValue(Instruction instruction, InstructionListIterator it) {
    if (!instruction.hasOutValue()) {
      return;
    }
    assert !(instruction.outValue() instanceof StackValue);
    if (instruction.isConstInstruction()) {
      ConstInstruction constInstruction = instruction.asConstInstruction();
      if (canRemoveConstInstruction(constInstruction, instruction.getBlock())) {
        assert !constInstruction.isDexItemBasedConstString()
            || constInstruction.outValue().numberOfUsers() == 1;
        clonableConstants.put(instruction.outValue(), constInstruction);
        instruction.outValue().clearUsers();
        it.removeOrReplaceByDebugLocalRead();
        return;
      }
      assert instruction.outValue().isUsed(); // Should have removed it above.
    }
    if (!instruction.outValue().isUsed()) {
      popOutValue(instruction.outValue(), instruction, it);
      return;
    }
    StackValue newOutValue = createStackValue(instruction.outValue(), 0);
    Value oldOutValue = instruction.swapOutValue(newOutValue);
    Store store = new Store(oldOutValue, newOutValue);
    // Move the debugging-locals liveness pertaining to the instruction to the store.
    instruction.moveDebugValues(store);
    BasicBlock storeBlock = instruction.getBlock();
    // If the block has catch-handlers we are not allowed to modify the locals of the block. If the
    // instruction is throwing, the action should be moved to a new block - otherwise, the store
    // should be inserted and the remaining instructions should be moved along with the handlers to
    // the new block.
    boolean hasCatchHandlers = instruction.getBlock().hasCatchHandlers();
    if (hasCatchHandlers && instruction.instructionTypeCanThrow()) {
      storeBlock = it.split(this.code, this.blockIterator);
      it = storeBlock.listIterator(code);
    }
    add(store, storeBlock, instruction.getPosition(), it);
    if (hasCatchHandlers && !instruction.instructionTypeCanThrow()) {
      it.split(this.code, this.blockIterator);
      this.blockIterator.previous();
    }
  }

  public void popOutType(DexType type, Instruction instruction, InstructionListIterator it) {
    popOutValue(createStackValue(type, 0), instruction, it);
  }

  private void popOutValue(Value value, Instruction instruction, InstructionListIterator it) {
    popOutValue(createStackValue(value, 0), instruction, it);
  }

  private void popOutValue(
      StackValue newOutValue, Instruction instruction, InstructionListIterator it) {
    BasicBlock insertBlock = instruction.getBlock();
    if (insertBlock.hasCatchHandlers() && instruction.instructionTypeCanThrow()) {
      insertBlock = it.split(this.code, this.blockIterator);
      it = insertBlock.listIterator(code);
    }
    instruction.swapOutValue(newOutValue);
    add(new Pop(newOutValue), insertBlock, instruction.getPosition(), it);
  }

  private static class PhiMove {
    final Phi phi;
    final Value operand;

    public PhiMove(Phi phi, Value operand) {
      this.phi = phi;
      this.operand = operand;
    }
  }

  private void movePhis(List<PhiMove> moves, InstructionListIterator it, Position position) {
    // TODO(zerny): Accounting for non-interfering phis would lower the max stack size.
    int topOfStack = 0;
    List<StackValue> temps = new ArrayList<>(moves.size());
    for (PhiMove move : moves) {
      StackValue tmp = createStackValue(move.phi, topOfStack++);
      add(load(tmp, move.operand), move.phi.getBlock(), position, it);
      temps.add(tmp);
      move.operand.removePhiUser(move.phi);
    }
    for (int i = moves.size() - 1; i >= 0; i--) {
      PhiMove move = moves.get(i);
      StackValue tmp = temps.get(i);
      FixedLocalValue out = new FixedLocalValue(move.phi);
      add(new Store(out, tmp), move.phi.getBlock(), position, it);
      move.phi.replaceUsers(out);
    }
  }

  private Instruction load(StackValue stackValue, Value value) {
    if (value.isConstant()) {
      ConstInstruction constant = value.getConstInstruction();
      if (constant.isConstNumber()) {
        return new ConstNumber(stackValue, constant.asConstNumber().getRawValue());
      } else if (constant.isConstString()) {
        return new ConstString(
            stackValue, constant.asConstString().getValue(), ThrowingInfo.NO_THROW);
      } else if (constant.isDexItemBasedConstString()) {
        DexItemBasedConstString computedConstant = constant.asDexItemBasedConstString();
        return new DexItemBasedConstString(
            stackValue,
            computedConstant.getItem(),
            computedConstant.getNameComputationInfo(),
            ThrowingInfo.NO_THROW);
      } else if (constant.isConstClass()) {
        return new ConstClass(stackValue, constant.asConstClass().getValue());
      } else {
        throw new Unreachable("Unexpected constant value: " + value);
      }
    }
    return new Load(stackValue, value);
  }

  private static void add(
      Instruction newInstruction, Instruction existingInstruction, InstructionListIterator it) {
    add(newInstruction, existingInstruction.getBlock(), existingInstruction.getPosition(), it);
  }

  private static void add(
      Instruction newInstruction, BasicBlock block, Position position, InstructionListIterator it) {
    newInstruction.setBlock(block);
    newInstruction.setPosition(position);
    it.add(newInstruction);
  }
}
