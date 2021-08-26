// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import static it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMaps.emptyMap;

import com.android.tools.r8.cf.code.CfFrame;
import com.android.tools.r8.cf.code.CfFrame.FrameType;
import com.android.tools.r8.cf.code.CfGoto;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfPosition;
import com.android.tools.r8.cf.code.CfSwitch;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.errors.InvalidDebugInfoException;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.CfCode.LocalVariableInfo;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DebugLocalInfo.PrintLevel;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.CanonicalPositions;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.CfState.Slot;
import com.android.tools.r8.ir.conversion.CfState.Snapshot;
import com.android.tools.r8.ir.conversion.IRBuilder.BlockInfo;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.InternalOutputMode;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CfSourceCode implements SourceCode {

  private BlockInfo currentBlockInfo;
  private boolean hasExitingInstruction = false;

  private static final int EXCEPTIONAL_SYNC_EXIT_OFFSET = -2;
  private final boolean needsGeneratedMethodSynchronization;
  private boolean currentlyGeneratingMethodSynchronization = false;
  private Monitor monitorEnter = null;

  private static class TryHandlerList {

    public final int startOffset;
    public final int endOffset;
    public final List<DexType> guards;
    public final IntList offsets;

    TryHandlerList(int startOffset, int endOffset, List<DexType> guards, IntList offsets) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.guards = guards;
      this.offsets = offsets;
    }

    boolean validFor(int instructionOffset) {
      return startOffset <= instructionOffset && instructionOffset < endOffset;
    }

    boolean isEmpty() {
      assert guards.isEmpty() == offsets.isEmpty();
      return guards.isEmpty();
    }

    static TryHandlerList computeTryHandlers(
        int instructionOffset,
        List<CfTryCatch> tryCatchRanges,
        Reference2IntMap<CfLabel> labelOffsets,
        boolean needsGeneratedMethodSynchronization,
        DexItemFactory factory) {
      int startOffset = 0;
      int endOffset = Integer.MAX_VALUE;
      List<DexType> guards = new ArrayList<>();
      IntList offsets = new IntArrayList();
      ReferenceSet<DexType> seen = new ReferenceOpenHashSet<>();
      boolean seenCatchAll = false;
      for (CfTryCatch tryCatch : tryCatchRanges) {
        int start = labelOffsets.getInt(tryCatch.start);
        int end = labelOffsets.getInt(tryCatch.end);
        if (start > instructionOffset) {
          endOffset = Math.min(endOffset, start);
          continue;
        } else if (instructionOffset >= end) {
          startOffset = Math.max(startOffset, end);
          continue;
        }
        startOffset = Math.max(startOffset, start);
        endOffset = Math.min(endOffset, end);
        for (int i = 0; i < tryCatch.guards.size() && !seenCatchAll; i++) {
          DexType guard = tryCatch.guards.get(i);
          if (seen.add(guard)) {
            guards.add(guard);
            offsets.add(labelOffsets.getInt(tryCatch.targets.get(i)));
            seenCatchAll = guard == factory.throwableType;
          }
        }
        if (seenCatchAll) {
          break;
        }
      }
      if (needsGeneratedMethodSynchronization && !seenCatchAll) {
        guards.add(factory.throwableType);
        offsets.add(EXCEPTIONAL_SYNC_EXIT_OFFSET);
      }
      return new TryHandlerList(startOffset, endOffset, guards, offsets);
    }
  }

  private static class LocalVariableList {

    public static final LocalVariableList EMPTY = new LocalVariableList(0, 0, emptyMap());
    public final int startOffset;
    public final int endOffset;
    public final Int2ReferenceMap<DebugLocalInfo> locals;

    private LocalVariableList(
        int startOffset, int endOffset, Int2ReferenceMap<DebugLocalInfo> locals) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.locals = locals;
    }

    static LocalVariableList compute(
        int instructionOffset,
        List<CfCode.LocalVariableInfo> locals,
        Reference2IntMap<CfLabel> labelOffsets) {
      int startOffset = Integer.MIN_VALUE;
      int endOffset = Integer.MAX_VALUE;
      Int2ReferenceMap<DebugLocalInfo> currentLocals = null;
      for (LocalVariableInfo local : locals) {
        int start = labelOffsets.getInt(local.getStart());
        int end = labelOffsets.getInt(local.getEnd());
        if (start > instructionOffset) {
          endOffset = Math.min(endOffset, start);
          continue;
        } else if (instructionOffset >= end) {
          startOffset = Math.max(startOffset, end);
          continue;
        }
        if (currentLocals == null) {
          currentLocals = new Int2ReferenceOpenHashMap<>();
        }
        startOffset = Math.max(startOffset, start);
        endOffset = Math.min(endOffset, end);
        currentLocals.put(local.getIndex(), local.getLocal());
      }
      return new LocalVariableList(
          startOffset, endOffset, currentLocals == null ? emptyMap() : currentLocals);
    }

    boolean validFor(int instructionOffset) {
      return startOffset <= instructionOffset && instructionOffset < endOffset;
    }

    public DebugLocalInfo getLocal(int register) {
      return locals.get(register);
    }

    public Int2ReferenceOpenHashMap<DebugLocalInfo> merge(LocalVariableList other) {
      return merge(this, other);
    }

    private static Int2ReferenceOpenHashMap<DebugLocalInfo> merge(
        LocalVariableList a, LocalVariableList b) {
      if (a.locals.size() > b.locals.size()) {
        return merge(b, a);
      }
      Int2ReferenceOpenHashMap<DebugLocalInfo> result = new Int2ReferenceOpenHashMap<>();
      for (Entry<DebugLocalInfo> local : a.locals.int2ReferenceEntrySet()) {
        if (local.getValue().equals(b.getLocal(local.getIntKey()))) {
          result.put(local.getIntKey(), local.getValue());
        }
      }
      return result;
    }
  }

  private CfState state;
  private final List<CfCode.LocalVariableInfo> localVariables;
  private final CfCode code;
  private final DexEncodedMethod method;
  private final Origin origin;
  private final AppView<?> appView;

  private final Reference2IntMap<CfLabel> labelOffsets = new Reference2IntOpenHashMap<>();
  private TryHandlerList cachedTryHandlerList;
  private LocalVariableList cachedLocalVariableList;
  private int currentInstructionIndex;
  private boolean inPrelude;
  private Int2ReferenceMap<DebugLocalInfo> incomingLocals;
  private Int2ReferenceMap<DebugLocalInfo> outgoingLocals;
  private Int2ReferenceMap<CfState.Snapshot> incomingState = new Int2ReferenceOpenHashMap<>();
  private final CanonicalPositions canonicalPositions;
  private final InternalOutputMode internalOutputMode;

  public CfSourceCode(
      CfCode code,
      List<CfCode.LocalVariableInfo> localVariables,
      DexEncodedMethod method,
      DexMethod originalMethod,
      Position callerPosition,
      Origin origin,
      AppView<?> appView) {
    this.code = code;
    this.localVariables = localVariables;
    this.method = method;
    this.origin = origin;
    this.appView = appView;
    int cfPositionCount = 0;
    for (int i = 0; i < code.getInstructions().size(); i++) {
      CfInstruction instruction = code.getInstructions().get(i);
      if (instruction instanceof CfLabel) {
        labelOffsets.put((CfLabel) instruction, instructionOffset(i));
      }
      if (instruction instanceof CfPosition) {
        ++cfPositionCount;
      }
    }
    this.state = new CfState(origin);
    canonicalPositions = new CanonicalPositions(callerPosition, cfPositionCount, originalMethod);
    internalOutputMode = appView.options().getInternalOutputMode();

    needsGeneratedMethodSynchronization =
        !method.isProcessed()
            && internalOutputMode.isGeneratingDex()
            && method.accessFlags.isSynchronized();
  }

  public Origin getOrigin() {
    return origin;
  }

  public DexType getOriginalHolder() {
    return code.getOriginalHolder();
  }

  @Override
  public int instructionCount() {
    return code.getInstructions().size();
  }

  @Override
  public int instructionIndex(int instructionOffset) {
    return instructionOffset;
  }

  @Override
  public int instructionOffset(int instructionIndex) {
    return instructionIndex;
  }

  @Override
  public boolean verifyRegister(int register) {
    return true;
  }

  @Override
  public void setUp() {}

  @Override
  public void clear() {}

  // Utility method that treats constant strings as not throwing in the case of having CF output.
  // This is the only instruction that differ in throwing between DEX and CF. If we find more
  // consider rewriting CfInstruction.canThrow to take in options.
  private boolean canThrowHelper(CfInstruction instruction) {
    if (internalOutputMode.isGeneratingClassFiles()
        && (instruction.isConstString() || instruction.isDexItemBasedConstString())) {
      return false;
    }
    return instruction.canThrow();
  }

  @Override
  public int traceInstruction(int instructionIndex, IRBuilder builder) {
    CfInstruction instruction = code.getInstructions().get(instructionIndex);
    AppView<?> appView = builder.appView;
    assert appView.options().isGeneratingClassFiles()
        == internalOutputMode.isGeneratingClassFiles();
    if (canThrowHelper(instruction)) {
      TryHandlerList tryHandlers = getTryHandlers(instructionIndex, appView.dexItemFactory());
      if (!tryHandlers.isEmpty()) {
        // Ensure the block starts at the start of the try-range (don't enqueue, not a target).
        builder.ensureBlockWithoutEnqueuing(tryHandlers.startOffset);
        IntSet seen = new IntOpenHashSet();
        for (int offset : tryHandlers.offsets) {
          if (seen.add(offset)) {
            builder.ensureExceptionalSuccessorBlock(instructionIndex, offset);
          }
        }
        if (!(instruction instanceof CfThrow)) {
          builder.ensureNormalSuccessorBlock(instructionIndex, instructionIndex + 1);
        }
        return instructionIndex;
      }
      // If the throwable instruction is "throw" it closes the block.
      hasExitingInstruction |= instruction instanceof CfThrow;
      return (instruction instanceof CfThrow) ? instructionIndex : -1;
    }
    if (isControlFlow(instruction)) {
      for (int target : getTargets(instructionIndex)) {
        builder.ensureNormalSuccessorBlock(instructionIndex, target);
      }
      hasExitingInstruction |= instruction.isReturn();
      return instructionIndex;
    }
    return -1;
  }

  private TryHandlerList getTryHandlers(int instructionOffset, DexItemFactory factory) {
    if (cachedTryHandlerList == null || !cachedTryHandlerList.validFor(instructionOffset)) {
      cachedTryHandlerList =
          TryHandlerList.computeTryHandlers(
              instructionOffset,
              code.getTryCatchRanges(),
              labelOffsets,
              needsGeneratedMethodSynchronization,
              factory);
    }
    return cachedTryHandlerList;
  }

  private LocalVariableList getLocalVariables(int instructionOffset) {
    if (cachedLocalVariableList == null || !cachedLocalVariableList.validFor(instructionOffset)) {
      cachedLocalVariableList =
          LocalVariableList.compute(instructionOffset, localVariables, labelOffsets);
    }
    return cachedLocalVariableList;
  }

  private int[] getTargets(int instructionIndex) {
    CfInstruction instruction = code.getInstructions().get(instructionIndex);
    assert isControlFlow(instruction);
    CfLabel target = instruction.getTarget();
    if (instruction.isReturn() || instruction instanceof CfThrow) {
      assert target == null;
      return new int[] {};
    }
    assert instruction instanceof CfSwitch || target != null
        : "getTargets(): Non-control flow instruction " + instruction.getClass();
    if (instruction instanceof CfSwitch) {
      CfSwitch cfSwitch = (CfSwitch) instruction;
      List<CfLabel> targets = cfSwitch.getSwitchTargets();
      int[] res = new int[targets.size() + 1];
      for (int i = 0; i < targets.size(); i++) {
        res[i] = labelOffsets.getInt(targets.get(i));
      }
      res[targets.size()] = labelOffsets.getInt(cfSwitch.getDefaultTarget());
      return res;
    }
    int targetIndex = labelOffsets.getInt(target);
    if (instruction instanceof CfGoto) {
      return new int[] {targetIndex};
    }
    assert instruction.isConditionalJump();
    return new int[] {targetIndex, instructionIndex + 1};
  }

  @Override
  public void buildPrelude(IRBuilder builder) {
    assert !inPrelude;
    inPrelude = true;
    state.buildPrelude(canonicalPositions.getPreamblePosition());
    setLocalVariableLists();
    DexSourceCode.buildArgumentsWithUnusedArgumentStubs(builder, 0, method, state::write);
    // Add debug information for all locals at the initial label.
    Int2ReferenceMap<DebugLocalInfo> locals = getLocalVariables(0).locals;
    if (!locals.isEmpty()) {
      int firstLocalIndex = 0;
      if (!method.isStatic()) {
        firstLocalIndex++;
      }
      for (DexType value : method.method.proto.parameters.values) {
        firstLocalIndex++;
        if (value.isLongType() || value.isDoubleType()) {
          firstLocalIndex++;
        }
      }
      for (Entry<DebugLocalInfo> entry : locals.int2ReferenceEntrySet()) {
        if (firstLocalIndex <= entry.getIntKey()) {
          builder.addDebugLocalStart(entry.getIntKey(), entry.getValue());
        }
      }
    }
    if (needsGeneratedMethodSynchronization) {
      buildMethodEnterSynchronization(builder);
    }
    recordStateForTarget(0, state.getSnapshot());
    inPrelude = false;
  }

  private boolean isStatic() {
    return method.accessFlags.isStatic();
  }

  private boolean isCurrentlyGeneratingMethodSynchronization() {
    return currentlyGeneratingMethodSynchronization;
  }

  private boolean isExceptionalExitForMethodSynchronization(int instructionIndex) {
    return instructionIndex == EXCEPTIONAL_SYNC_EXIT_OFFSET;
  }

  private void buildMethodEnterSynchronization(IRBuilder builder) {
    assert needsGeneratedMethodSynchronization;
    currentlyGeneratingMethodSynchronization = true;
    DexType type = method.method.holder;
    int monitorRegister;
    if (isStatic()) {
      monitorRegister = state.push(type).register;
      state.pop();
      builder.addConstClass(monitorRegister, type);
    } else {
      monitorRegister = state.read(0).register;
    }
    // Build the monitor enter and save it for when generating exits later.
    monitorEnter = builder.addMonitor(Monitor.Type.ENTER, monitorRegister);
    currentlyGeneratingMethodSynchronization = false;
  }

  private void buildExceptionalExitMethodSynchronization(IRBuilder builder) {
    assert needsGeneratedMethodSynchronization;
    currentlyGeneratingMethodSynchronization = true;
    state.setPosition(getCanonicalDebugPositionAtOffset(EXCEPTIONAL_SYNC_EXIT_OFFSET));
    builder.add(new Monitor(Monitor.Type.EXIT, monitorEnter.inValues().get(0)));
    builder.addThrow(getMoveExceptionRegister(0));
    currentlyGeneratingMethodSynchronization = false;
  }

  @Override
  public void buildPostlude(IRBuilder builder) {
    if (needsGeneratedMethodSynchronization) {
      currentlyGeneratingMethodSynchronization = true;
      builder.add(new Monitor(Monitor.Type.EXIT, monitorEnter.inValues().get(0)));
      currentlyGeneratingMethodSynchronization = false;
    }
  }

  @Override
  public void buildBlockTransfer(
      IRBuilder builder, int predecessorOffset, int successorOffset, boolean isExceptional) {
    if (predecessorOffset == IRBuilder.INITIAL_BLOCK_OFFSET
        || isExceptionalExitForMethodSynchronization(successorOffset)) {
      return;
    }
    // The transfer has not yet taken place, so the current position is that of the predecessor,
    // except for exceptional edges where the transfer has already taken place.
    state.setPosition(
        getCanonicalDebugPositionAtOffset(isExceptional ? successorOffset : predecessorOffset));

    // Manually compute the local variable change for the block transfer.
    Int2ReferenceMap<DebugLocalInfo> atSource = getLocalVariables(predecessorOffset).locals;
    Int2ReferenceMap<DebugLocalInfo> atTarget = getLocalVariables(successorOffset).locals;
    if (!isExceptional) {
      for (Entry<DebugLocalInfo> entry : atSource.int2ReferenceEntrySet()) {
        if (atTarget.get(entry.getIntKey()) != entry.getValue()) {
          builder.addDebugLocalEnd(entry.getIntKey(), entry.getValue());
        }
      }
    }
    for (Entry<DebugLocalInfo> entry : atTarget.int2ReferenceEntrySet()) {
      if (atSource.get(entry.getIntKey()) != entry.getValue()) {
        builder.addDebugLocalStart(entry.getIntKey(), entry.getValue());
      }
    }

    // If there are no explicit exits from the method (ie, this method is a loop without an explict
    // return or an unhandled throw) then we cannot guarentee that a local live in a successor will
    // ensure it is marked as such (via an explict 'end' marker) and thus be live in predecessors.
    // In this case we insert an 'end' point on all explicit goto instructions ensuring that any
    // back-edge will explicitly keep locals live at that point.
    if (!hasExitingInstruction && code.getInstructions().get(predecessorOffset) instanceof CfGoto) {
      assert !isExceptional;
      for (Entry<DebugLocalInfo> entry : atSource.int2ReferenceEntrySet()) {
        if (atTarget.get(entry.getIntKey()) == entry.getValue()) {
          builder.addDebugLocalEnd(entry.getIntKey(), entry.getValue());
        }
      }
    }
  }

  @Override
  public void buildInstruction(
      IRBuilder builder, int instructionIndex, boolean firstBlockInstruction) {
    if (isExceptionalExitForMethodSynchronization(instructionIndex)) {
      buildExceptionalExitMethodSynchronization(builder);
      return;
    }
    CfInstruction instruction = code.getInstructions().get(instructionIndex);
    currentInstructionIndex = instructionIndex;
    if (firstBlockInstruction) {
      currentBlockInfo = builder.getCFG().get(instructionIndex);
      if (instructionIndex == 0 && currentBlockInfo == null) {
        // If the entry block is also a target the actual entry block is at offset -1.
        currentBlockInfo = builder.getCFG().get(IRBuilder.INITIAL_BLOCK_OFFSET);
      }
      state.reset(
          incomingState.get(instructionIndex),
          instructionIndex == 0,
          getCanonicalDebugPositionAtOffset(instructionIndex));
    }

    assert currentBlockInfo != null;
    setLocalVariableLists();

    if (canThrowHelper(instruction)) {
      Snapshot exceptionTransfer =
          state.getSnapshot().exceptionTransfer(builder.appView.dexItemFactory().throwableType);
      for (int target : currentBlockInfo.exceptionalSuccessors) {
        recordStateForTarget(target, exceptionTransfer);
      }
    }

    boolean localsChanged = localsChanged();
    boolean hasNextInstructionInCurrentBlock =
        instructionIndex + 1 != instructionCount()
            && !builder.getCFG().containsKey(instructionIndex + 1);

    if (instruction.isReturn() || instruction instanceof CfThrow) {
      // Ensure that all live locals are marked as ending on method exits.
      assert currentBlockInfo.normalSuccessors.isEmpty();
      if (currentBlockInfo.exceptionalSuccessors.isEmpty()) {
        incomingLocals.forEach(builder::addDebugLocalEnd);
      } else if (!incomingLocals.isEmpty()) {
        // If the throw instruction does not exit the method, we must end (ensure liveness of) all
        // locals that are not kept live by at least one of the exceptional successors.
        Int2ReferenceMap<DebugLocalInfo> live = new Int2ReferenceOpenHashMap<>();
        for (int successorOffset : currentBlockInfo.exceptionalSuccessors) {
          live.putAll(getLocalVariables(successorOffset).locals);
        }
        for (Entry<DebugLocalInfo> entry : incomingLocals.int2ReferenceEntrySet()) {
          if (live.get(entry.getIntKey()) != entry.getValue()) {
            builder.addDebugLocalEnd(entry.getIntKey(), entry.getValue());
          }
        }
      }
    } else if (localsChanged && hasNextInstructionInCurrentBlock) {
      endLocals(builder);
    }

    build(instruction, builder);
    if (!hasNextInstructionInCurrentBlock) {
      Snapshot stateSnapshot = state.getSnapshot();
      if (isControlFlow(instruction)) {
        for (int target : getTargets(instructionIndex)) {
          recordStateForTarget(target, stateSnapshot);
        }
      } else {
        recordStateForTarget(instructionIndex + 1, stateSnapshot);
      }
    } else if (localsChanged) {
      startLocals(builder);
    }
  }

  private void build(CfInstruction instruction, IRBuilder builder) {
    instruction.buildIR(builder, state, this);
  }

  private void recordStateForTarget(int target, Snapshot snapshot) {
    Snapshot existing = incomingState.get(target);
    Snapshot merged = CfState.merge(existing, snapshot, origin);
    if (merged != existing) {
      incomingState.put(target, merged);
    }
  }

  public int getCurrentInstructionIndex() {
    return currentInstructionIndex;
  }

  public int getLabelOffset(CfLabel label) {
    assert labelOffsets.containsKey(label);
    return labelOffsets.getInt(label);
  }

  public void setStateFromFrame(CfFrame frame) {
    Int2ReferenceSortedMap<FrameType> frameLocals = frame.getLocals();
    DexType[] locals = new DexType[frameLocals.isEmpty() ? 0 : frameLocals.lastIntKey() + 1];
    DexType[] stack = new DexType[frame.getStack().size()];
    for (Int2ReferenceMap.Entry<FrameType> entry : frameLocals.int2ReferenceEntrySet()) {
      locals[entry.getIntKey()] = convertUninitialized(entry.getValue());
    }
    for (int i = 0; i < stack.length; i++) {
      stack[i] = convertUninitialized(frame.getStack().get(i));
    }
    state.setStateFromFrame(
        locals, stack, getCanonicalDebugPositionAtOffset(currentInstructionIndex));
  }

  private DexType convertUninitialized(FrameType type) {
    if (type.isInitialized()) {
      return type.getInitializedType();
    }
    if (type.isUninitializedNew()) {
      int labelOffset = getLabelOffset(type.getUninitializedLabel());
      int insnOffset = labelOffset + 1;
      while (insnOffset < code.getInstructions().size()) {
        CfInstruction instruction = code.getInstructions().get(insnOffset);
        if (!(instruction instanceof CfLabel)
            && !(instruction instanceof CfFrame)
            && !(instruction instanceof CfPosition)) {
          assert instruction instanceof CfNew;
          break;
        }
        insnOffset += 1;
      }
      CfInstruction instruction = code.getInstructions().get(insnOffset);
      assert instruction instanceof CfNew;
      return ((CfNew) instruction).getType();
    }
    if (type.isUninitializedThis()) {
      return method.method.holder;
    }
    assert type.isTop();
    return null;
  }

  @Override
  public void resolveAndBuildSwitch(
      int value, int fallthroughOffset, int payloadOffset, IRBuilder builder) {}

  @Override
  public void resolveAndBuildNewArrayFilledData(
      int arrayRef, int payloadOffset, IRBuilder builder) {}

  @Override
  public DebugLocalInfo getIncomingLocalAtBlock(int register, int blockOffset) {
    return getLocalVariables(blockOffset).locals.get(register);
  }

  @Override
  public DebugLocalInfo getIncomingLocal(int register) {
    return isCurrentlyGeneratingMethodSynchronization() ? null : incomingLocals.get(register);
  }

  @Override
  public DebugLocalInfo getOutgoingLocal(int register) {
    if (isCurrentlyGeneratingMethodSynchronization()) {
      return null;
    }
    if (inPrelude) {
      return getIncomingLocal(register);
    }
    assert !isControlFlow(code.getInstructions().get(currentInstructionIndex))
        : "Outgoing local is undefined for control-flow instructions";
    return outgoingLocals.get(register);
  }

  private void setLocalVariableLists() {
    incomingLocals = getLocalVariables(currentInstructionIndex).locals;
    if (inPrelude) {
      outgoingLocals = incomingLocals;
      return;
    }
    CfInstruction currentInstruction = code.getInstructions().get(currentInstructionIndex);
    outgoingLocals =
        !isControlFlow(currentInstruction)
            ? getLocalVariables(currentInstructionIndex + 1).locals
            : emptyMap();
  }

  private boolean localsChanged() {
    return !incomingLocals.equals(outgoingLocals);
  }

  private void endLocals(IRBuilder builder) {
    assert localsChanged();
    for (Entry<DebugLocalInfo> entry : incomingLocals.int2ReferenceEntrySet()) {
      if (!entry.getValue().equals(outgoingLocals.get(entry.getIntKey()))) {
        builder.addDebugLocalEnd(entry.getIntKey(), entry.getValue());
      }
    }
  }

  private void startLocals(IRBuilder builder) {
    assert localsChanged();
    for (Entry<DebugLocalInfo> entry : outgoingLocals.int2ReferenceEntrySet()) {
      if (!entry.getValue().equals(incomingLocals.get(entry.getIntKey()))) {
        Slot slot = state.read(entry.getIntKey());
        if (slot != null && slot.type != ValueType.fromDexType(entry.getValue().type)) {
          throw new InvalidDebugInfoException(
              "Attempt to define local of type "
                  + prettyType(slot.type)
                  + " as "
                  + entry.getValue().toString(PrintLevel.FULL));
        }
        builder.addDebugLocalStart(entry.getIntKey(), entry.getValue());
      }
    }
  }

  private String prettyType(ValueType type) {
    switch (type) {
      case OBJECT:
        return "reference";
      case INT:
        return "int";
      case FLOAT:
        return "float";
      case LONG:
        return "long";
      case DOUBLE:
        return "double";
      default:
        throw new Unreachable();
    }
  }

  private boolean isControlFlow(CfInstruction currentInstruction) {
    return currentInstruction.isReturn()
        || currentInstruction.getTarget() != null
        || currentInstruction instanceof CfSwitch
        || currentInstruction instanceof CfThrow;
  }

  @Override
  public CatchHandlers<Integer> getCurrentCatchHandlers(IRBuilder builder) {
    if (isCurrentlyGeneratingMethodSynchronization()) {
      return null;
    }
    TryHandlerList tryHandlers =
        getTryHandlers(
            instructionOffset(currentInstructionIndex), builder.appView.dexItemFactory());
    if (tryHandlers.isEmpty()) {
      return null;
    }
    return new CatchHandlers<>(tryHandlers.guards, tryHandlers.offsets);
  }

  @Override
  public int getMoveExceptionRegister(int instructionIndex) {
    return CfState.Slot.STACK_OFFSET;
  }

  @Override
  public boolean verifyCurrentInstructionCanThrow() {
    return isCurrentlyGeneratingMethodSynchronization()
        || canThrowHelper(code.getInstructions().get(currentInstructionIndex));
  }

  @Override
  public boolean verifyLocalInScope(DebugLocalInfo local) {
    return false;
  }

  @Override
  public Position getCanonicalDebugPositionAtOffset(int offset) {
    if (offset == EXCEPTIONAL_SYNC_EXIT_OFFSET) {
      return canonicalPositions.getExceptionalExitPosition(
          appView.options().debug,
          () ->
              code.instructions.stream()
                  .filter(insn -> insn instanceof CfPosition)
                  .map(insn -> ((CfPosition) insn).getPosition())
                  .collect(Collectors.toList()),
          method.method);
    }
    while (offset + 1 < code.getInstructions().size()) {
      CfInstruction insn = code.getInstructions().get(offset);
      if (!(insn instanceof CfLabel) && !(insn instanceof CfFrame)) {
        break;
      }
      offset += 1;
    }
    while (offset >= 0 && !(code.getInstructions().get(offset) instanceof CfPosition)) {
      offset -= 1;
    }
    if (offset < 0) {
      return canonicalPositions.getPreamblePosition();
    }
    return getCanonicalPosition(((CfPosition) code.getInstructions().get(offset)).getPosition());
  }

  @Override
  public Position getCurrentPosition() {
    return state.getPosition();
  }

  public Position getCanonicalPosition(Position position) {
    return canonicalPositions.getCanonical(
        new Position(
            position.line,
            position.file,
            position.method,
            canonicalPositions.canonicalizeCallerPosition(position.callerPosition)));
  }
}
