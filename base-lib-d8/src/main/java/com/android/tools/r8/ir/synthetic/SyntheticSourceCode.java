// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.conversion.SourceCode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class SyntheticSourceCode implements SourceCode {
  protected final static Predicate<IRBuilder> doesNotEndBlock = x -> false;
  protected final static Predicate<IRBuilder> endsBlock = x -> true;

  protected final DexType receiver;
  protected final DexMethod method;
  protected final DexProto proto;

  // The next free register, note that we always
  // assign each value a new (next available) register.
  private int nextRegister = 0;

  // Registers for receiver and parameters
  private final int receiverRegister;
  private int[] paramRegisters;

  // Instruction constructors
  private List<Consumer<IRBuilder>> constructors = new ArrayList<>();
  private List<Predicate<IRBuilder>> traceEvents = new ArrayList<>();

  private final Position position;

  protected SyntheticSourceCode(DexType receiver, DexMethod method, Position callerPosition) {
    this(receiver, method, callerPosition, method);
  }

  protected SyntheticSourceCode(
      DexType receiver, DexMethod method, Position callerPosition, DexMethod originalMethod) {
    assert method != null;
    this.receiver = receiver;
    this.method = method;
    this.proto = method.proto;

    // Initialize register values for receiver and arguments
    this.receiverRegister = receiver != null ? nextRegister(ValueType.OBJECT) : -1;

    DexType[] params = proto.parameters.values;
    int paramCount = params.length;
    this.paramRegisters = new int[paramCount];
    for (int i = 0; i < paramCount; i++) {
      this.paramRegisters[i] = nextRegister(ValueType.fromDexType(params[i]));
    }

    position = Position.synthetic(0, originalMethod, callerPosition);
  }

  protected final void add(Consumer<IRBuilder> constructor) {
    add(constructor, doesNotEndBlock);
  }

  protected final void add(Consumer<IRBuilder> constructor, Predicate<IRBuilder> traceEvent) {
    constructors.add(constructor);
    traceEvents.add(traceEvent);
  }

  protected final int nextRegister(ValueType type) {
    int value = nextRegister;
    nextRegister += type.requiredRegisters();
    return value;
  }

  protected final int getReceiverRegister() {
    assert receiver != null;
    assert receiverRegister >= 0;
    return receiverRegister;
  }

  protected final int getParamRegister(int paramIndex) {
    assert paramIndex >= 0;
    assert paramIndex < paramRegisters.length;
    return paramRegisters[paramIndex];
  }

  protected abstract void prepareInstructions();

  @Override
  public final int instructionCount() {
    return constructors.size();
  }

  protected final int lastInstructionIndex() {
    return constructors.size() - 1;
  }

  protected final int nextInstructionIndex() {
    return constructors.size();
  }

  @Override
  public final int instructionIndex(int instructionOffset) {
    return instructionOffset;
  }

  @Override
  public final int instructionOffset(int instructionIndex) {
    return instructionIndex;
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
  public final int traceInstruction(int instructionIndex, IRBuilder builder) {
    return (traceEvents.get(instructionIndex).test(builder) ||
        (instructionIndex == constructors.size() - 1)) ? instructionIndex : -1;
  }

  @Override
  public final void setUp() {
    assert constructors.isEmpty();
    prepareInstructions();
    assert !constructors.isEmpty();
  }

  @Override
  public final void clear() {
    constructors = null;
    traceEvents = null;
    paramRegisters = null;
  }

  @Override
  public final void buildPrelude(IRBuilder builder) {
    if (receiver != null) {
      builder.addThisArgument(
          receiverRegister,
          TypeLatticeElement.fromDexType(
              receiver, Nullability.definitelyNotNull(), builder.appView));
    }
    // Fill in the Argument instructions in the argument block.
    DexType[] parameters = proto.parameters.values;
    for (int i = 0; i < parameters.length; i++) {
      TypeLatticeElement typeLattice =
          TypeLatticeElement.fromDexType(parameters[i], Nullability.maybeNull(), builder.appView);
      builder.addNonThisArgument(paramRegisters[i], typeLattice);
    }
  }

  @Override
  public final void buildPostlude(IRBuilder builder) {
    // Intentionally left empty.
  }

  @Override
  public final void buildInstruction(
      IRBuilder builder, int instructionIndex, boolean firstBlockInstruction) {
    constructors.get(instructionIndex).accept(builder);
  }

  @Override
  public void buildBlockTransfer(
      IRBuilder builder, int predecessorOffset, int successorOffset, boolean isExceptional) {
    // Intensionally empty as synthetic code does not contain locals information.
  }

  @Override
  public final void resolveAndBuildSwitch(
      int value, int fallthroughOffset, int payloadOffset, IRBuilder builder) {
    throw new Unreachable("Unexpected call to resolveAndBuildSwitch");
  }

  @Override
  public final void resolveAndBuildNewArrayFilledData(
      int arrayRef, int payloadOffset, IRBuilder builder) {
    throw new Unreachable("Unexpected call to resolveAndBuildNewArrayFilledData");
  }

  @Override
  public final CatchHandlers<Integer> getCurrentCatchHandlers(IRBuilder builder) {
    return null;
  }

  @Override
  public int getMoveExceptionRegister(int instructionIndex) {
    throw new Unreachable();
  }

  @Override
  public Position getCanonicalDebugPositionAtOffset(int offset) {
    throw new Unreachable();
  }

  @Override
  public Position getCurrentPosition() {
    return position;
  }

  @Override
  public final boolean verifyCurrentInstructionCanThrow() {
    return true;
  }

  @Override
  public boolean verifyLocalInScope(DebugLocalInfo local) {
    return true;
  }

  @Override
  public final boolean verifyRegister(int register) {
    return true;
  }

  // To be used as a tracing event for switch instruction.,
  protected boolean endsSwitch(
      IRBuilder builder, int switchIndex, int fallthrough, int[] offsets) {
    // ensure successors of switch instruction
    for (int offset : offsets) {
      builder.ensureNormalSuccessorBlock(switchIndex, offset);
    }
    builder.ensureNormalSuccessorBlock(switchIndex, fallthrough);
    return true;
  }
}
