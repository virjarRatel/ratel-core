// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import static com.android.tools.r8.ir.regalloc.LiveIntervals.NO_REGISTER;

import com.android.tools.r8.cf.TypeVerificationHelper.TypeInfo;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DebugLocalWrite;
import com.android.tools.r8.ir.code.Dup;
import com.android.tools.r8.ir.code.Dup2;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.IRCode.LiveAtEntrySets;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.Load;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Pop;
import com.android.tools.r8.ir.code.StackValue;
import com.android.tools.r8.ir.code.StackValues;
import com.android.tools.r8.ir.code.Store;
import com.android.tools.r8.ir.code.Swap;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator;
import com.android.tools.r8.ir.regalloc.LiveIntervals;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Allocator for assigning local-indexes to non-stack values.
 *
 * <p>This is mostly a very simple variant of {@link
 * com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator} since for CF there are no register
 * constraints and thus no need for spilling.
 */
public class CfRegisterAllocator implements RegisterAllocator {

  private final AppView<?> appView;
  private final IRCode code;
  private final TypeVerificationHelper typeHelper;

  // Mapping from basic blocks to the set of values live at entry to that basic block.
  private Map<BasicBlock, LiveAtEntrySets> liveAtEntrySets;

  public static class TypesAtBlockEntry {
    public final Int2ReferenceMap<TypeInfo> registers;
    public final List<TypeInfo> stack; // Last item is the top.

    TypesAtBlockEntry(Int2ReferenceMap<TypeInfo> registers, List<TypeInfo> stack) {
      this.registers = registers;
      this.stack = stack;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("regs[");
      for (Int2ReferenceMap.Entry<TypeInfo> kv : registers.int2ReferenceEntrySet()) {
        sb.append(kv.getIntKey()).append(":").append(kv.getValue()).append(", ");
      }
      sb.append("], stack[");
      for (TypeInfo t : stack) {
        sb.append(t).append(", ");
      }
      sb.append("]");
      return sb.toString();
    }
  };

  private final Map<BasicBlock, TypesAtBlockEntry> lazyTypeInfoAtBlockEntry = new HashMap<>();

  // List of all top-level live intervals for all SSA values.
  private final List<LiveIntervals> liveIntervals = new ArrayList<>();

  // List of active intervals.
  private final List<LiveIntervals> active = new LinkedList<>();

  // List of intervals where the current instruction falls into one of their live range holes.
  private final List<LiveIntervals> inactive = new LinkedList<>();

  // List of intervals that no register has been allocated to sorted by first live range.
  private final PriorityQueue<LiveIntervals> unhandled = new PriorityQueue<>();

  // The set of registers that are free for allocation.
  private NavigableSet<Integer> freeRegisters = new TreeSet<>();

  private int nextUnusedRegisterNumber = 0;

  private int maxRegisterNumber = 0;

  private int maxArgumentRegisterNumber = -1;

  public CfRegisterAllocator(AppView<?> appView, IRCode code, TypeVerificationHelper typeHelper) {
    this.appView = appView;
    this.code = code;
    this.typeHelper = typeHelper;
  }

  @Override
  public int registersUsed() {
    return maxRegisterNumber + 1;
  }

  @Override
  public int getRegisterForValue(Value value, int instructionNumber) {
    return getRegisterForValue(value);
  }

  public int getRegisterForValue(Value value) {
    if (value instanceof FixedLocalValue) {
      return ((FixedLocalValue) value).getRegister(this);
    }
    assert !value.getLiveIntervals().hasSplits();
    return value.getLiveIntervals().getRegister();
  }

  @Override
  public int getArgumentOrAllocateRegisterForValue(Value value, int instructionNumber) {
    return getRegisterForValue(value);
  }

  @Override
  public InternalOptions options() {
    return appView.options();
  }

  @Override
  public void allocateRegisters() {
    computeNeedsRegister();
    ImmutableList<BasicBlock> blocks = computeLivenessInformation();
    performLinearScan();
    // Even if the method is reachability sensitive, we do not compute debug information after
    // register allocation. We just treat the method as being in debug mode in order to keep
    // locals alive for their entire live range. In release mode the liveness is all that matters
    // and we do not actually want locals information in the output.
    if (appView.options().debug) {
      LinearScanRegisterAllocator.computeDebugInfo(
          code, blocks, liveIntervals, this, liveAtEntrySets);
    }
  }

  private void computeNeedsRegister() {
    for (Instruction instruction : code.instructions()) {
      Value outValue = instruction.outValue();
      if (outValue != null) {
        boolean isStackValue =
            (outValue instanceof StackValue) || (outValue instanceof StackValues);
        outValue.setNeedsRegister(!isStackValue);
      }
    }
  }

  private ImmutableList<BasicBlock> computeLivenessInformation() {
    ImmutableList<BasicBlock> blocks = code.numberInstructions();
    liveAtEntrySets = code.computeLiveAtEntrySets();
    LinearScanRegisterAllocator.computeLiveRanges(
        appView.options(), code, liveAtEntrySets, liveIntervals);
    return blocks;
  }

  private void performLinearScan() {
    unhandled.addAll(liveIntervals);

    while (!unhandled.isEmpty() && unhandled.peek().getValue().isArgument()) {
      LiveIntervals argument = unhandled.poll();
      assignRegisterToUnhandledInterval(argument, getNextFreeRegister(argument.getType().isWide()));
    }

    maxArgumentRegisterNumber = nextUnusedRegisterNumber - 1;

    while (!unhandled.isEmpty()) {
      LiveIntervals unhandledInterval = unhandled.poll();
      assert !unhandledInterval.getValue().isArgument();
      int start = unhandledInterval.getStart();
      {
        // Check for active intervals that expired or became inactive.
        Iterator<LiveIntervals> it = active.iterator();
        while (it.hasNext()) {
          LiveIntervals activeIntervals = it.next();
          if (start >= activeIntervals.getEnd()) {
            it.remove();
            freeRegistersForIntervals(activeIntervals);
          } else if (!activeIntervals.overlapsPosition(start)) {
            assert activeIntervals.getRegister() != NO_REGISTER;
            it.remove();
            inactive.add(activeIntervals);
            freeRegistersForIntervals(activeIntervals);
          }
        }
      }
      {
        // Check for inactive intervals that expired or become active.
        Iterator<LiveIntervals> it = inactive.iterator();
        while (it.hasNext()) {
          LiveIntervals inactiveIntervals = it.next();
          if (start >= inactiveIntervals.getEnd()) {
            it.remove();
          } else if (inactiveIntervals.overlapsPosition(start)) {
            it.remove();
            assert inactiveIntervals.getRegister() != NO_REGISTER;
            active.add(inactiveIntervals);
            takeRegistersForIntervals(inactiveIntervals);
          }
        }
      }

      // Find a free register that is not used by an inactive interval that overlaps with
      // unhandledInterval.
      if (tryHint(unhandledInterval)) {
        assignRegisterToUnhandledInterval(unhandledInterval, unhandledInterval.getHint());
      } else {
        boolean wide = unhandledInterval.getType().isWide();
        int register;
        NavigableSet<Integer> previousFreeRegisters = new TreeSet<Integer>(freeRegisters);
        while (true) {
          register = getNextFreeRegister(wide);
          boolean overlapsInactiveInterval = false;
          for (LiveIntervals inactiveIntervals : inactive) {
            if (inactiveIntervals.usesRegister(register, wide)
                && unhandledInterval.overlaps(inactiveIntervals)) {
              overlapsInactiveInterval = true;
              break;
            }
          }
          if (!overlapsInactiveInterval) {
            break;
          }
          // Remove so that next invocation of getNextFreeRegister does not consider this.
          freeRegisters.remove(register);
          // For wide types, register + 1 and 2 might be good even though register + 0 and 1
          // weren't, so don't remove register+1 from freeRegisters.
        }
        freeRegisters = previousFreeRegisters;
        assignRegisterToUnhandledInterval(unhandledInterval, register);
      }
    }
  }

  // Note that getNextFreeRegister will not take the register before it is committed to an interval.
  private int getNextFreeRegister(boolean isWide) {
    if (!freeRegisters.isEmpty()) {
      if (isWide) {
        // In case of a wide local we don't allow it to start at maxArgumentRegisterNumber, that is
        // either at the last, non-wide argument or the second register of the last, wide argument.
        // This is a workaround for a jacoco bug (see b/117593305) where the jacoco instrumentation
        // algorithm assumes that we never reuse argument registers so it is safe to insert the
        // instrumentation registers starting with the first register after the arguments.
        //
        // Without this workaround we may create a wide local whose first register overlaps the
        // last argument and whose second register will overlap the first instrumentation register.
        // The result is a Java verification error.
        for (Integer register : freeRegisters) {
          if ((freeRegisters.contains(register + 1) || nextUnusedRegisterNumber == register + 1)
              && register != maxArgumentRegisterNumber) {
            return register;
          }
        }
        return nextUnusedRegisterNumber;
      }
      return freeRegisters.first();
    }
    return nextUnusedRegisterNumber;
  }

  private void freeRegistersForIntervals(LiveIntervals intervals) {
    int register = intervals.getRegister();
    freeRegisters.add(register);
    if (intervals.getType().isWide()) {
      freeRegisters.add(register + 1);
    }
  }

  private void takeRegistersForIntervals(LiveIntervals intervals) {
    int register = intervals.getRegister();
    freeRegisters.remove(register);
    if (intervals.getType().isWide()) {
      freeRegisters.remove(register + 1);
    }
  }

  private void updateHints(LiveIntervals intervals) {
    for (Phi phi : intervals.getValue().uniquePhiUsers()) {
      if (!phi.isValueOnStack()) {
        phi.getLiveIntervals().setHint(intervals, unhandled);
        for (Value value : phi.getOperands()) {
          value.getLiveIntervals().setHint(intervals, unhandled);
        }
      }
    }
  }

  private boolean tryHint(LiveIntervals unhandled) {
    if (unhandled.getHint() == null) {
      return false;
    }
    boolean isWide = unhandled.getType().isWide();
    int hintRegister = unhandled.getHint();
    if (freeRegisters.contains(hintRegister)
        && (!isWide || freeRegisters.contains(hintRegister + 1))) {
      for (LiveIntervals inactive : inactive) {
        if (inactive.usesRegister(hintRegister, isWide) && inactive.overlaps(unhandled)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void assignRegisterToUnhandledInterval(LiveIntervals unhandledInterval, int register) {
    assignRegister(unhandledInterval, register);
    takeRegistersForIntervals(unhandledInterval);
    updateRegisterState(register, unhandledInterval.getType().isWide());
    updateHints(unhandledInterval);
    active.add(unhandledInterval);
  }

  private void updateRegisterState(int register, boolean needsRegisterPair) {
    int maxRegister = register + (needsRegisterPair ? 1 : 0);
    if (maxRegister >= nextUnusedRegisterNumber) {
      nextUnusedRegisterNumber = maxRegister + 1;
    }
    maxRegisterNumber = Math.max(maxRegisterNumber, maxRegister);
  }

  private void assignRegister(LiveIntervals intervals, int register) {
    intervals.setRegister(register);
  }

  public void addToLiveAtEntrySet(BasicBlock block, Collection<Phi> phis) {
    for (Phi phi : phis) {
      if (phi.isValueOnStack()) {
        liveAtEntrySets.get(block).liveStackValues.addLast(phi);
      } else {
        liveAtEntrySets.get(block).liveValues.add(phi);
      }
    }
  }

  public TypesAtBlockEntry getTypesAtBlockEntry(BasicBlock block) {
    return lazyTypeInfoAtBlockEntry.computeIfAbsent(
        block,
        k -> {
          Set<Value> liveValues = liveAtEntrySets.get(k).liveValues;
          Int2ReferenceMap<TypeInfo> registers = new Int2ReferenceOpenHashMap<>(liveValues.size());
          for (Value liveValue : liveValues) {
            registers.put(getRegisterForValue(liveValue), typeHelper.getTypeInfo(liveValue));
          }

          Deque<Value> liveStackValues = liveAtEntrySets.get(k).liveStackValues;
          List<TypeInfo> stack = new ArrayList<>(liveStackValues.size());
          for (Value value : liveStackValues) {
            stack.add(typeHelper.getTypeInfo(value));
          }
          return new TypesAtBlockEntry(registers, stack);
        });
  }

  @Override
  public void mergeBlocks(BasicBlock kept, BasicBlock removed) {
    TypesAtBlockEntry typesOfKept = getTypesAtBlockEntry(kept);
    TypesAtBlockEntry typesOfRemoved = getTypesAtBlockEntry(removed);

    // Join types of registers (= JVM local variables).
    assert typesOfKept.registers.size() == typesOfRemoved.registers.size();
    // Updating the reference into lazyTypeInfoAtBlockEntry directly.
    updateFirstRegisterMapByJoiningTheSecond(typesOfKept.registers, typesOfRemoved.registers);

    // Join types of stack elements.
    assert typesOfKept.stack.size() == typesOfRemoved.stack.size();
    // Updating the reference into lazyTypeInfoAtBlockEntry directly.
    updateFirstStackByJoiningTheSecond(typesOfKept.stack, typesOfRemoved.stack);
  }

  @Override
  public boolean hasEqualTypesAtEntry(BasicBlock first, BasicBlock second) {
    if (!java.util.Objects.equals(first.getLocalsAtEntry(), second.getLocalsAtEntry())) {
      return false;
    }
    // Check that stack at entry is same.
    List<TypeInfo> firstStack = getTypesAtBlockEntry(first).stack;
    List<TypeInfo> secondStack = getTypesAtBlockEntry(second).stack;
    if (firstStack.size() != secondStack.size()) {
      return false;
    }
    for (int i = 0; i < firstStack.size(); i++) {
      if (firstStack.get(i).getDexType() != secondStack.get(i).getDexType()) {
        return false;
      }
    }
    // Check if the blocks are catch-handlers, which implies that the exception is transferred
    // through the stack.
    if (first.entry().isMoveException() != second.entry().isMoveException()) {
      return false;
    }
    // If both blocks are catch handlers, we require that the exceptions are the same type.
    if (first.entry().isMoveException()
        && typeHelper.getTypeInfo(first.entry().outValue()).getDexType()
            != typeHelper.getTypeInfo(second.entry().outValue()).getDexType()) {
      return false;
    }

    return true;
  }

  // Return false if this is not an instruction with the type of outvalue dependent on types of
  // invalues.
  private boolean tryApplyInstructionWithDependentOutType(
      Instruction instruction, Int2ReferenceMap<TypeInfo> registers, Deque<TypeInfo> stack) {
    if (instruction.outValue() == null || instruction.inValues().isEmpty()) {
      return false;
    }
    if (instruction instanceof Load) {
      int register = getRegisterForValue(instruction.inValues().get(0));
      assert registers.containsKey(register);
      stack.addLast(registers.get(register));
    } else if (instruction instanceof Store) {
      int register = getRegisterForValue(instruction.outValue());
      registers.put(register, stack.removeLast());
    } else if (instruction instanceof Pop) {
      stack.removeLast();
    } else if (instruction instanceof Dup) {
      stack.addLast(stack.getLast());
    } else if (instruction instanceof Dup2) {
      TypeInfo wasTop = stack.removeLast();
      TypeInfo wasBelowTop = stack.getLast();
      stack.addLast(wasTop);
      stack.addLast(wasBelowTop);
      stack.addLast(wasTop);
    } else if (instruction instanceof Swap) {
      TypeInfo wasTop = stack.removeLast();
      TypeInfo wasBelowTop = stack.removeLast();
      stack.addLast(wasTop);
      stack.addLast(wasBelowTop);
    } else if (instruction instanceof DebugLocalWrite) {
      int dstRegister = getRegisterForValue(instruction.outValue());
      registers.put(dstRegister, stack.removeLast());
    } else {
      return false;
    }
    return true;
  }

  private void applyInstructionsToTypes(
      BasicBlock block,
      Int2ReferenceMap<TypeInfo> registers,
      Deque<TypeInfo> stack,
      int prefixSize) {
    InstructionIterator it = block.iterator();
    int instructionsLeft = prefixSize;
    while (--instructionsLeft >= 0 && it.hasNext()) {
      Instruction current = it.next();

      if (tryApplyInstructionWithDependentOutType(current, registers, stack)) {
        continue;
      }
      for (int i = current.inValues().size() - 1; i >= 0; --i) {
        Value inValue = current.inValues().get(i);
        if (inValue.isValueOnStack()) {
          stack.removeLast();
        }
      }

      Value outValue = current.outValue();
      if (outValue == null) {
        continue;
      }

      TypeInfo outType = typeHelper.getTypeInfo(outValue);
      assert outType != null;
      if (outValue.needsRegister()) {
        int register = getRegisterForValue(outValue);
        registers.put(register, outType);
      } else if (outValue.isValueOnStack()) {
        stack.addLast(outType);
      } else {
        throw new Unreachable();
      }
    }
  }

  private void applyInstructionsBackwardsToRegisterLiveness(
      BasicBlock block, IntSet liveRegisters, int suffixSize) {
    InstructionIterator iterator = block.iterator(block.getInstructions().size());
    int instructionsLeft = suffixSize;
    while (--instructionsLeft >= 0 && iterator.hasPrevious()) {
      Instruction current = iterator.previous();
      Value outValue = current.outValue();
      if (outValue != null && outValue.needsRegister()) {
        int register = getRegisterForValue(outValue);
        assert liveRegisters.contains(register);
        liveRegisters.remove(register);
      }
      for (Value inValue : current.inValues()) {
        if (inValue.needsRegister()) {
          int register = getRegisterForValue(inValue);
          liveRegisters.add(register);
        }
      }
    }
  }

  @Override
  public void addNewBlockToShareIdenticalSuffix(
      BasicBlock block, int suffixSize, List<BasicBlock> predsBeforeSplit) {
    Int2ReferenceMap<TypeInfo> joinedRegistersBeforeSuffix = new Int2ReferenceOpenHashMap<>();
    List<TypeInfo> joinedStackBeforeSuffix = new ArrayList<>(); // Last item is the top.

    assert !predsBeforeSplit.isEmpty();
    for (BasicBlock pred : predsBeforeSplit) {
      // Replay instructions of prefix on registers and stack at block entry.
      TypesAtBlockEntry types = getTypesAtBlockEntry(pred);
      Int2ReferenceMap<TypeInfo> registersFromPrefix =
          new Int2ReferenceOpenHashMap<>(types.registers);
      Deque<TypeInfo> stackFromPrefix = new ArrayDeque<>(types.stack);
      applyInstructionsToTypes(
          pred, registersFromPrefix, stackFromPrefix, pred.getInstructions().size() - suffixSize);

      // Replay instructions of suffix on registers at block end to compute liveness between prefix
      // and suffix.
      Int2ReferenceMap<TypeInfo> registersAtExit;
      if (pred.getSuccessors().isEmpty()) {
        registersAtExit = new Int2ReferenceOpenHashMap<>();
      } else {
        assert pred.getSuccessors().size() == 1;
        registersAtExit = getTypesAtBlockEntry(pred.getSuccessors().get(0)).registers;
      }
      IntSet registersLiveFromSuffix = new IntOpenHashSet(registersAtExit.size() * 2);
      registersLiveFromSuffix.addAll(registersAtExit.keySet());
      applyInstructionsBackwardsToRegisterLiveness(pred, registersLiveFromSuffix, suffixSize);

      // We have two sets of registers between the prefix and suffix:
      // - 'registersFromPrefix', which is computed from block entry forwards, and
      // - 'registersLiveFromSuffix', which is computed from block exit backwards.
      // Keep only the registers from the prefix which are live as computed from the suffix.
      Int2ReferenceMap<TypeInfo> registersBeforeSuffix =
          new Int2ReferenceOpenHashMap<>(registersFromPrefix.size());
      for (int register : registersLiveFromSuffix) {
        assert registersFromPrefix.containsKey(register);
        registersBeforeSuffix.put(register, registersFromPrefix.get(register));
      }

      updateFirstRegisterMapByJoiningTheSecond(joinedRegistersBeforeSuffix, registersBeforeSuffix);
      updateFirstStackByJoiningTheSecond(
          joinedStackBeforeSuffix, Arrays.asList(stackFromPrefix.toArray(new TypeInfo[0])));
    }

    assert !lazyTypeInfoAtBlockEntry.containsKey(block);
    lazyTypeInfoAtBlockEntry.put(
        block, new TypesAtBlockEntry(joinedRegistersBeforeSuffix, joinedStackBeforeSuffix));
  }

  // First registermap can be empty, otherwise they must have identical keysets
  private void updateFirstRegisterMapByJoiningTheSecond(
      Int2ReferenceMap<TypeInfo> map1, Int2ReferenceMap<TypeInfo> map2) {
    if (map1.isEmpty()) {
      map1.putAll(map2);
      return;
    }
    assert map1.size() == map2.size();
    for (Int2ReferenceMap.Entry<TypeInfo> entry1 : map1.int2ReferenceEntrySet()) {
      int register = entry1.getIntKey();
      TypeInfo type1 = entry1.getValue();
      assert map2.containsKey(register);
      TypeInfo joinedType = typeHelper.join(type1, map2.get(register));
      if (joinedType != type1) {
        map1.put(register, joinedType);
      }
    }
  }

  // First stack can be empty, otherwise they must have the same size.
  private void updateFirstStackByJoiningTheSecond(List<TypeInfo> stack1, List<TypeInfo> stack2) {
    if (stack1.isEmpty()) {
      stack1.addAll(stack2);
      return;
    }
    assert stack1.size() == stack2.size();
    for (int i = 0; i < stack1.size(); ++i) {
      TypeInfo type1 = stack1.get(i);
      TypeInfo joinedType = typeHelper.join(type1, stack2.get(i));
      if (joinedType != type1) {
        stack1.set(i, joinedType);
      }
    }
  }
}
