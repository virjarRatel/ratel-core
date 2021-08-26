// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.code.FillArrayData;
import com.android.tools.r8.code.FillArrayDataPayload;
import com.android.tools.r8.code.FilledNewArray;
import com.android.tools.r8.code.FilledNewArrayRange;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.InvokeCustom;
import com.android.tools.r8.code.InvokeCustomRange;
import com.android.tools.r8.code.InvokeDirect;
import com.android.tools.r8.code.InvokeDirectRange;
import com.android.tools.r8.code.InvokeInterface;
import com.android.tools.r8.code.InvokeInterfaceRange;
import com.android.tools.r8.code.InvokePolymorphic;
import com.android.tools.r8.code.InvokePolymorphicRange;
import com.android.tools.r8.code.InvokeStatic;
import com.android.tools.r8.code.InvokeStaticRange;
import com.android.tools.r8.code.InvokeSuper;
import com.android.tools.r8.code.InvokeSuperRange;
import com.android.tools.r8.code.InvokeVirtual;
import com.android.tools.r8.code.InvokeVirtualRange;
import com.android.tools.r8.code.MoveException;
import com.android.tools.r8.code.MoveResult;
import com.android.tools.r8.code.MoveResultObject;
import com.android.tools.r8.code.MoveResultWide;
import com.android.tools.r8.code.SwitchPayload;
import com.android.tools.r8.code.Throw;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexCode.Try;
import com.android.tools.r8.graph.DexCode.TryHandler;
import com.android.tools.r8.graph.DexCode.TryHandler.TypeAddrPair;
import com.android.tools.r8.graph.DexDebugEntry;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription.RemovedArgumentInfo;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription.RemovedArgumentsInfo;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.CanonicalPositions;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class DexSourceCode implements SourceCode {

  private final DexCode code;
  private final DexEncodedMethod method;

  // Mapping from instruction offset to instruction index in the DexCode instruction array.
  private final Map<Integer, Integer> offsetToInstructionIndex = new HashMap<>();

  private final SwitchPayloadResolver switchPayloadResolver = new SwitchPayloadResolver();
  private final ArrayFilledDataPayloadResolver arrayFilledDataPayloadResolver =
      new ArrayFilledDataPayloadResolver();

  private Try currentTryRange = null;
  private CatchHandlers<Integer> currentCatchHandlers = null;
  private Instruction currentDexInstruction = null;

  private Position currentPosition = null;
  private final CanonicalPositions canonicalPositions;

  private List<DexDebugEntry> debugEntries = null;
  // In case of inlining the position of the invoke in the caller.
  private final DexMethod originalMethod;

  public DexSourceCode(
      DexCode code, DexEncodedMethod method, DexMethod originalMethod, Position callerPosition) {
    this.code = code;
    this.method = method;
    this.originalMethod = originalMethod;
    DexDebugInfo info = code.getDebugInfo();
    if (info != null) {
      debugEntries = info.computeEntries(originalMethod);
    }
    canonicalPositions =
        new CanonicalPositions(
            callerPosition,
            debugEntries == null ? 0 : debugEntries.size(),
            originalMethod);
  }

  @Override
  public boolean verifyRegister(int register) {
    return register < code.registerSize;
  }

  @Override
  public int instructionCount() {
    return code.instructions.length;
  }

  @Override
  public DebugLocalInfo getIncomingLocalAtBlock(int register, int blockOffset) {
    return null;
  }

  @Override
  public DebugLocalInfo getIncomingLocal(int register) {
    return null;
  }

  @Override
  public DebugLocalInfo getOutgoingLocal(int register) {
    return null;
  }

  @Override
  public void setUp() {
    // Collect all payloads in the instruction stream.
    for (int index = 0; index < code.instructions.length; index++) {
      Instruction insn = code.instructions[index];
      offsetToInstructionIndex.put(insn.getOffset(), index);
      if (insn.isPayload()) {
        if (insn.isSwitchPayload()) {
          switchPayloadResolver.resolve((SwitchPayload) insn);
        } else {
          arrayFilledDataPayloadResolver.resolve((FillArrayDataPayload) insn);
        }
      }
    }
  }

  @Override
  public void buildPrelude(IRBuilder builder) {
    currentPosition = canonicalPositions.getPreamblePosition();
    if (code.incomingRegisterSize == 0) {
      return;
    }
    buildArgumentsWithUnusedArgumentStubs(
        builder,
        code.registerSize - code.incomingRegisterSize,
        method,
        DexSourceCode::doNothingWriteConsumer);
  }

  public static void doNothingWriteConsumer(Integer register, DexType type) {
    // Intentionally empty.
  }

  public static void buildArgumentsWithUnusedArgumentStubs(
      IRBuilder builder,
      int register,
      DexEncodedMethod method,
      BiConsumer<Integer, DexType> writeCallback) {
    RemovedArgumentsInfo removedArgumentsInfo = builder.prototypeChanges.getRemovedArgumentsInfo();
    ListIterator<RemovedArgumentInfo> removedArgumentIterator = removedArgumentsInfo.iterator();
    RemovedArgumentInfo nextRemovedArgument =
        removedArgumentIterator.hasNext() ? removedArgumentIterator.next() : null;

    // Fill in the Argument instructions (incomingRegisterSize last registers) in the argument
    // block.
    int argumentIndex = 0;

    if (!method.isStatic()) {
      writeCallback.accept(register, method.method.holder);
      builder.addThisArgument(register);
      ++argumentIndex;
      ++register;
    }

    int numberOfArguments =
        method.method.proto.parameters.values.length
            + removedArgumentsInfo.numberOfRemovedArguments()
            + (method.isStatic() ? 0 : 1);

    for (int usedArgumentIndex = 0; argumentIndex < numberOfArguments; ++argumentIndex) {
      TypeLatticeElement type;
      if (nextRemovedArgument != null && nextRemovedArgument.getArgumentIndex() == argumentIndex) {
        writeCallback.accept(register, nextRemovedArgument.getType());
        type =
            TypeLatticeElement.fromDexType(
                nextRemovedArgument.getType(), Nullability.maybeNull(), builder.appView);
        builder.addConstantOrUnusedArgument(register);
        nextRemovedArgument =
            removedArgumentIterator.hasNext() ? removedArgumentIterator.next() : null;
      } else {
        DexType dexType = method.method.proto.parameters.values[usedArgumentIndex++];
        writeCallback.accept(register, dexType);
        type =
            TypeLatticeElement.fromDexType(
                dexType,
                Nullability.maybeNull(),
                builder.appView);
        if (dexType.isBooleanType()) {
          builder.addBooleanNonThisArgument(register);
        } else {
          builder.addNonThisArgument(register, type);
        }
      }
      register += type.requiredRegisters();
    }
    builder.flushArgumentInstructions();
  }

  @Override
  public void buildPostlude(IRBuilder builder) {
    // Intentionally left empty. (Needed in the Java bytecode frontend for synchronization support.)
  }

  @Override
  public void buildBlockTransfer(
      IRBuilder builder, int predecessorOffset, int successorOffset, boolean isExceptional) {
    // Intentionally empty. Dex front-end does not support debug locals so no transfer info needed.
  }

  @Override
  public void buildInstruction(
      IRBuilder builder, int instructionIndex, boolean firstBlockInstruction) {
    updateCurrentCatchHandlers(instructionIndex, builder.appView.dexItemFactory());
    updateDebugPosition(instructionIndex, builder);
    currentDexInstruction = code.instructions[instructionIndex];
    currentDexInstruction.buildIR(builder);
  }

  @Override
  public CatchHandlers<Integer> getCurrentCatchHandlers(IRBuilder builder) {
    return currentCatchHandlers;
  }

  @Override
  public int getMoveExceptionRegister(int instructionIndex) {
    Instruction instruction = code.instructions[instructionIndex];
    if (instruction instanceof MoveException) {
      MoveException moveException = (MoveException) instruction;
      return moveException.AA;
    }
    return -1;
  }

  @Override
  public Position getCanonicalDebugPositionAtOffset(int offset) {
    DexDebugEntry entry = getDebugEntryAtOffset(offset);
    return entry == null
        ? canonicalPositions.getPreamblePosition()
        : getCanonicalPositionAppendCaller(entry);
  }

  @Override
  public Position getCurrentPosition() {
    return currentPosition;
  }

  @Override
  public boolean verifyCurrentInstructionCanThrow() {
    return currentDexInstruction.canThrow();
  }

  @Override
  public boolean verifyLocalInScope(DebugLocalInfo local) {
    return true;
  }

  private void updateCurrentCatchHandlers(int instructionIndex, DexItemFactory factory) {
    Try tryRange = getTryForOffset(instructionOffset(instructionIndex));
    if (tryRange == currentTryRange) {
      return;
    }
    currentTryRange = tryRange;
    if (tryRange == null) {
      currentCatchHandlers = null;
    } else {
      currentCatchHandlers = getCurrentCatchHandlers(factory, tryRange);
    }
  }

  private DexDebugEntry getDebugEntryAtOffset(int offset) {
    DexDebugEntry current = null;
    if (debugEntries != null) {
      for (DexDebugEntry entry : debugEntries) {
        if (entry.address > offset) {
          break;
        }
        current = entry;
      }
    }
    return current;
  }

  private void updateDebugPosition(int instructionIndex, IRBuilder builder) {
    if (debugEntries == null || debugEntries.isEmpty()) {
      return;
    }
    int offset = instructionOffset(instructionIndex);
    DexDebugEntry entry = getDebugEntryAtOffset(offset);
    if (entry == null) {
      currentPosition = canonicalPositions.getPreamblePosition();
    } else {
      currentPosition = getCanonicalPositionAppendCaller(entry);
      if (entry.lineEntry && entry.address == offset) {
        builder.addDebugPosition(currentPosition);
      }
    }
  }

  private Position getCanonicalPositionAppendCaller(DexDebugEntry entry) {
    // If this instruction has already been inlined then this.method must be the outermost caller.
    assert entry.callerPosition == null
        || entry.callerPosition.getOutermostCaller().method == originalMethod;

    return canonicalPositions.getCanonical(
        new Position(
            entry.line,
            entry.sourceFile,
            entry.method,
            canonicalPositions.canonicalizeCallerPosition(entry.callerPosition)));
  }

  @Override
  public void clear() {
    switchPayloadResolver.clear();
    arrayFilledDataPayloadResolver.clear();
  }

  @Override
  public int instructionIndex(int instructionOffset) {
    return offsetToInstructionIndex.get(instructionOffset);
  }

  @Override
  public int instructionOffset(int instructionIndex) {
    return code.instructions[instructionIndex].getOffset();
  }

  @Override
  public void resolveAndBuildSwitch(int value, int fallthroughOffset, int payloadOffset,
      IRBuilder builder) {
    builder.addSwitch(value, switchPayloadResolver.getKeys(payloadOffset), fallthroughOffset,
        switchPayloadResolver.absoluteTargets(payloadOffset));
  }

  @Override
  public void resolveAndBuildNewArrayFilledData(int arrayRef, int payloadOffset,
      IRBuilder builder) {
    builder.addNewArrayFilledData(arrayRef,
        arrayFilledDataPayloadResolver.getElementWidth(payloadOffset),
        arrayFilledDataPayloadResolver.getSize(payloadOffset),
        arrayFilledDataPayloadResolver.getData(payloadOffset));
  }

  private boolean isInvoke(Instruction dex) {
    return dex instanceof InvokeCustom
        || dex instanceof InvokeCustomRange
        || dex instanceof InvokeDirect
        || dex instanceof InvokeDirectRange
        || dex instanceof InvokeVirtual
        || dex instanceof InvokeVirtualRange
        || dex instanceof InvokeInterface
        || dex instanceof InvokeInterfaceRange
        || dex instanceof InvokeStatic
        || dex instanceof InvokeStaticRange
        || dex instanceof InvokeSuper
        || dex instanceof InvokeSuperRange
        || dex instanceof InvokePolymorphic
        || dex instanceof InvokePolymorphicRange
        || dex instanceof FilledNewArray
        || dex instanceof FilledNewArrayRange;
  }

  private boolean isMoveResult(Instruction dex) {
    return dex instanceof MoveResult
        || dex instanceof MoveResultObject
        || dex instanceof MoveResultWide;
  }

  @Override
  public int traceInstruction(int index, IRBuilder builder) {
    Instruction dex = code.instructions[index];
    int offset = dex.getOffset();
    assert !dex.isPayload();
    int[] targets = dex.getTargets();
    if (targets != Instruction.NO_TARGETS) {
      // Check that we don't ever have instructions that can throw and have targets.
      assert !dex.canThrow();
      for (int relativeOffset : targets) {
        builder.ensureNormalSuccessorBlock(offset, offset + relativeOffset);
      }
      return index;
    }
    if (dex.canThrow()) {
      // TODO(zerny): Remove this from block computation.
      if (dex.hasPayload()) {
        arrayFilledDataPayloadResolver.addPayloadUser((FillArrayData) dex);
      }
      // If the instruction can throw and is in a try block, add edges to its catch successors.
      Try tryRange = getTryForOffset(offset);
      if (tryRange != null) {
        // Ensure the block starts at the start of the try-range (don't enqueue, not a target).
        int tryRangeStartAddress = tryRange.startAddress;
        if (isMoveResult(code.instructions[offsetToInstructionIndex.get(tryRangeStartAddress)])) {
          // If a handler range starts at a move result instruction it is safe to start it at
          // the following instruction since the move-result cannot throw an exception. Doing so
          // makes sure that we do not split an invoke and its move result instruction across
          // two blocks.
          ++tryRangeStartAddress;
        }
        builder.ensureBlockWithoutEnqueuing(tryRangeStartAddress);
        // Edge to exceptional successors.
        for (Integer handlerOffset :
            getUniqueTryHandlerOffsets(tryRange, builder.appView.dexItemFactory())) {
          builder.ensureExceptionalSuccessorBlock(offset, handlerOffset);
        }
        // If the following instruction is a move-result include it in this (the invokes) block.
        if (index + 1 < code.instructions.length && isMoveResult(code.instructions[index + 1])) {
          assert isInvoke(dex);
          ++index;
          dex = code.instructions[index];
        }
        // Edge to normal successor if any (fallthrough).
        if (!(dex instanceof Throw)) {
          builder.ensureNormalSuccessorBlock(offset, dex.getOffset() + dex.getSize());
        }
        return index;
      }
      // Close the block if the instruction is a throw, otherwise the block remains open.
      return dex instanceof Throw ? index : -1;
    }
    if (dex.isIntSwitch()) {
      // TODO(zerny): Remove this from block computation.
      switchPayloadResolver.addPayloadUser(dex);

      for (int target : switchPayloadResolver.absoluteTargets(dex)) {
        builder.ensureNormalSuccessorBlock(offset, target);
      }
      builder.ensureNormalSuccessorBlock(offset, offset + dex.getSize());
      return index;
    }
    // This instruction does not close the block.
    return -1;
  }

  private boolean inTryRange(Try tryItem, int offset) {
    return tryItem.startAddress <= offset
        && offset < tryItem.startAddress + tryItem.instructionCount;
  }

  private Try getTryForOffset(int offset) {
    for (Try tryRange : code.tries) {
      if (inTryRange(tryRange, offset)) {
        return tryRange;
      }
    }
    return null;
  }

  private CatchHandlers<Integer> getCurrentCatchHandlers(DexItemFactory factory, Try tryRange) {
    List<DexType> handlerGuards = new ArrayList<>();
    List<Integer> handlerOffsets = new ArrayList<>();
    forEachTryRange(
        tryRange,
        factory,
        (type, addr) -> {
          handlerGuards.add(type);
          handlerOffsets.add(addr);
        });
    return new CatchHandlers<>(handlerGuards, handlerOffsets);
  }

  private void forEachTryRange(
      Try tryRange, DexItemFactory factory, BiConsumer<DexType, Integer> fn) {
    TryHandler handler = code.handlers[tryRange.handlerIndex];
    for (TypeAddrPair pair : handler.pairs) {
      fn.accept(pair.type, pair.addr);
      if (pair.type == factory.throwableType) {
        return;
      }
    }
    if (handler.catchAllAddr != TryHandler.NO_HANDLER) {
      fn.accept(factory.throwableType, handler.catchAllAddr);
    }

  }

  private Set<Integer> getUniqueTryHandlerOffsets(Try tryRange, DexItemFactory factory) {
    return new HashSet<>(getTryHandlerOffsets(tryRange, factory));
  }

  private List<Integer> getTryHandlerOffsets(Try tryRange, DexItemFactory factory) {
    List<Integer> handlerOffsets = new ArrayList<>();
    forEachTryRange(tryRange, factory, (type, addr) -> handlerOffsets.add(addr));
    return handlerOffsets;
  }
}
