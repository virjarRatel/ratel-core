// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.ValueType;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builder to construct a "per position" representation of the debug information.
 *
 * <p>This builder is relatively relaxed about the stream of build operations and should accept any
 * stream from any input file we expect to process correctly.
 */
public class DexDebugEntryBuilder implements DexDebugEventVisitor {

  private static class LocalEntry {
    DebugLocalInfo current;
    DebugLocalInfo last;

    void set(DebugLocalInfo value) {
      current = value;
      last = value;
    }

    void unset() {
      current = null;
    }

    void reset() {
      current = last;
    }
  }

  // The variables of the state machine.
  private boolean prologueEnd = false;
  private boolean epilogueBegin = false;
  private final Map<Integer, LocalEntry> locals = new HashMap<>();
  private final Int2ReferenceMap<DebugLocalInfo> arguments = new Int2ReferenceArrayMap<>();
  private final DexMethod method;

  // Delayed construction of an entry. Is finalized once locals information has been collected.
  private DexDebugEntry pending = null;

  // Canonicalization of locals (the IR/Dex builders assume identity of locals).
  private final Map<DebugLocalInfo, DebugLocalInfo> canonicalizedLocals = new HashMap<>();

  // Resulting debug entries.
  private List<DexDebugEntry> entries = new ArrayList<>();
  private final DexDebugPositionState positionState;

  public DexDebugEntryBuilder(int startLine, DexMethod method) {
    assert method != null;
    this.method = method;
    positionState = new DexDebugPositionState(startLine, method);
  }

  public DexDebugEntryBuilder(DexEncodedMethod method, DexItemFactory factory) {
    assert method != null && method.method != null;
    this.method = method.method;
    positionState =
        new DexDebugPositionState(
            method.getCode().asDexCode().getDebugInfo().startLine, method.method);
    DexCode code = method.getCode().asDexCode();
    DexDebugInfo info = code.getDebugInfo();
    int argumentRegister = code.registerSize - code.incomingRegisterSize;
    if (!method.accessFlags.isStatic()) {
      DexString name = factory.thisName;
      DexType type = method.method.holder;
      startArgument(argumentRegister, name, type);
      argumentRegister += ValueType.fromDexType(type).requiredRegisters();
    }
    DexType[] types = method.method.proto.parameters.values;
    DexString[] names = info.parameters;
    for (int i = 0; i < types.length; i++) {
      // If null, the parameter has a parameterized type and the local is introduced in the stream.
      // TODO(114704754): The check 'i < names.length' is a bug workaround which should be removed.
      if (i < names.length && names[i] != null) {
        startArgument(argumentRegister, names[i], types[i]);
      }
      argumentRegister += ValueType.fromDexType(types[i]).requiredRegisters();
    }
    for (DexDebugEvent event : info.events) {
      event.accept(this);
    }
  }

  public Int2ReferenceMap<DebugLocalInfo> getArguments() {
    return arguments;
  }

  @Override
  public void visit(DexDebugEvent.AdvancePC advancePC) {
    positionState.visit(advancePC);
    entryEventReceived(false);
  }

  @Override
  public void visit(DexDebugEvent.AdvanceLine advanceLine) {
    positionState.visit(advanceLine);
  }

  @Override
  public void visit(DexDebugEvent.SetInlineFrame setInlineFrame) {
    positionState.visit(setInlineFrame);
  }

  @Override
  public void visit(DexDebugEvent.Default defaultEvent) {
    positionState.visit(defaultEvent);
    entryEventReceived(true);
  }

  @Override
  public void visit(DexDebugEvent.SetFile setFile) {
    positionState.visit(setFile);
  }

  @Override
  public void visit(DexDebugEvent.SetPrologueEnd setPrologueEnd) {
    prologueEnd = true;
  }

  @Override
  public void visit(DexDebugEvent.SetEpilogueBegin setEpilogueBegin) {
    epilogueBegin = true;
  }

  public void startArgument(int register, DexString name, DexType type) {
    DebugLocalInfo argument = canonicalize(name, type, null);
    arguments.put(register, argument);
    getEntry(register).set(argument);
  }

  @Override
  public void visit(DexDebugEvent.StartLocal setStartLocal) {
    getEntry(setStartLocal.registerNum)
        .set(canonicalize(setStartLocal.name, setStartLocal.type, setStartLocal.signature));
  }

  @Override
  public void visit(DexDebugEvent.EndLocal endLocal) {
    getEntry(endLocal.registerNum).unset();
  }

  @Override
  public void visit(DexDebugEvent.RestartLocal restartLocal) {
    getEntry(restartLocal.registerNum).reset();
  }

  private void entryEventReceived(boolean lineEntry) {
    if (pending != null) {
      // Local changes contribute to the pending position entry.
      entries.add(
          new DexDebugEntry(
              pending.lineEntry,
              pending.address,
              pending.line,
              pending.sourceFile,
              pending.prologueEnd,
              pending.epilogueBegin,
              getLocals(),
              pending.method,
              pending.callerPosition));
    }
    pending =
        new DexDebugEntry(
            lineEntry,
            positionState.getCurrentPc(),
            positionState.getCurrentLine(),
            positionState.getCurrentFile(),
            prologueEnd,
            epilogueBegin,
            null,
            positionState.getCurrentMethod(),
            positionState.getCurrentCallerPosition());
    prologueEnd = false;
    epilogueBegin = false;
  }

  public List<DexDebugEntry> build() {
    // Flush any pending entry.
    if (pending != null) {
      entryEventReceived(false); // To flush 'pending'.
      pending = null;
    }
    List<DexDebugEntry> result = entries;
    entries = null;
    return result;
  }

  private DebugLocalInfo canonicalize(DexString name, DexType type, DexString signature) {
    DebugLocalInfo local = new DebugLocalInfo(name, type, signature);
    DebugLocalInfo canonical = canonicalizedLocals.putIfAbsent(local, local);
    return canonical != null ? canonical : local;
  }

  private LocalEntry getEntry(int register) {
    LocalEntry entry = locals.get(register);
    if (entry == null) {
      entry = new LocalEntry();
      locals.put(register, entry);
    }
    return entry;
  }

  private ImmutableMap<Integer, DebugLocalInfo> getLocals() {
    ImmutableMap.Builder<Integer, DebugLocalInfo> builder = ImmutableMap.builder();
    for (Entry<Integer, LocalEntry> mapEntry : locals.entrySet()) {
      Integer register = mapEntry.getKey();
      LocalEntry entry = mapEntry.getValue();
      if (entry.current != null) {
        builder.put(register, entry.current);
      }
    }
    return builder.build();
  }
}
