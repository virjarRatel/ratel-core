// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.origin.Origin;

public class CfState {

  private abstract static class SlotType {

    public abstract DexType getPrecise();

    public abstract ValueType getImprecise();

    private static class Precise extends SlotType {
      private final DexType type;

      Precise(DexType type) {
        this.type = type;
      }

      @Override
      public DexType getPrecise() {
        return type;
      }

      @Override
      public ValueType getImprecise() {
        return ValueType.fromDexType(type);
      }

      @Override
      public String toString() {
        return "Precise(" + type + ")";
      }
    }

    private static class Imprecise extends SlotType {

      private final ValueType type;

      Imprecise(ValueType type) {
        this.type = type;
      }

      @Override
      public DexType getPrecise() {
        return null;
      }

      @Override
      public ValueType getImprecise() {
        return type;
      }

      @Override
      public String toString() {
        return "Imprecise(" + type + ")";
      }
    }
  }

  private final Origin origin;
  private Snapshot current;
  private Position position;

  public CfState(Origin origin) {
    this.origin = origin;
  }

  private static final int MAX_UPDATES = 4;

  public void buildPrelude(Position preamblePosition) {
    current = new BaseSnapshot();
    position = preamblePosition;
  }

  public void clear() {
    current = null;
  }

  public void reset(Snapshot snapshot, boolean isMethodEntry, Position position) {
    assert !isMethodEntry || snapshot != null : "Must have snapshot for method entry.";
    current = snapshot;
    this.position = position;
  }

  public void setStateFromFrame(DexType[] locals, DexType[] stack, Position position) {
    assert current == null || stackHeight() == stack.length;
    current = new BaseSnapshot(locals, stack, position);
  }

  public void merge(Snapshot snapshot) {
    if (current == null) {
      current = snapshot == null ? new BaseSnapshot() : snapshot;
    } else {
      current = merge(current, snapshot, origin);
    }
  }

  public Snapshot getSnapshot() {
    return current;
  }

  public static Snapshot merge(Snapshot current, Snapshot update, Origin origin) {
    assert update != null;
    if (current == null) {
      return update;
    }
    return merge(current.asBase(), update.asBase(), origin);
  }

  private static Snapshot merge(BaseSnapshot current, BaseSnapshot update, Origin origin) {
    if (current.stack.length != update.stack.length) {
      throw new CompilationError(
          "Different stack heights at jump target: "
              + current.stack.length
              + " != "
              + update.stack.length,
          origin);
    }
    // At this point, JarState checks if `current` has special "NULL" or "BYTE/BOOL" types
    // that `update` does not have, and if so it computes a refinement.
    // For now, let's just check that we didn't mix single/wide/reference types.
    for (int i = 0; i < current.stack.length; i++) {
      ValueType currentType = current.stack[i].getImprecise();
      ValueType updateType = update.stack[i].getImprecise();
      if (currentType != updateType) {
        throw new CompilationError(
            "Incompatible types in stack position "
                + i
                + ": "
                + current.stack[i]
                + " and "
                + update.stack[i],
            origin);
      }
    }
    // We could check that locals are compatible, but that doesn't make sense since locals can be
    // dead at this point.
    return current;
  }

  public int stackHeight() {
    return current.stackHeight();
  }

  public Slot push(Slot fromSlot) {
    return push(fromSlot.slotType);
  }

  public Slot push(DexType type) {
    return push(new SlotType.Precise(type));
  }

  public Slot push(ValueType type) {
    return push(new SlotType.Imprecise(type));
  }

  private Slot push(SlotType slotType) {
    Push newSnapshot = new Push(this.current, slotType);
    updateState(newSnapshot);
    return current.peek();
  }

  private void updateState(Snapshot newSnapshot) {
    current = newSnapshot.updates >= MAX_UPDATES ? new BaseSnapshot(newSnapshot) : newSnapshot;
  }

  public Slot pop() {
    Slot top = current.peek();
    updateState(new Pop(current));
    return top;
  }

  public int[] popReverse(int count) {
    int[] registers = new int[count];
    for (int i = count - 1; i >= 0; i--) {
      registers[i] = pop().register;
    }
    return registers;
  }

  public Slot peek() {
    return current.peek();
  }

  public Slot peek(int skip) {
    return current.getStack(current.stackHeight() - 1 - skip);
  }

  public Slot read(int localIndex) {
    return current.getLocal(localIndex);
  }

  public Slot write(int localIndex, DexType type) {
    return write(localIndex, new SlotType.Precise(type));
  }

  public Slot write(int localIndex, Slot src) {
    return write(localIndex, src.slotType);
  }

  private Slot write(int localIndex, SlotType slotType) {
    updateState(new Write(current, localIndex, slotType));
    return current.getLocal(localIndex);
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    assert position != null;
    this.position = position;
  }

  @Override
  public String toString() {
    return new BaseSnapshot(current).toString();
  }

  public static class Slot {

    public static final int STACK_OFFSET = 100000;
    public final int register;
    public final ValueType type;
    public final DexType preciseType;
    private final SlotType slotType;

    private Slot(int register, DexType preciseType) {
      this(register, new SlotType.Precise(preciseType));
    }

    private Slot(int register, SlotType type) {
      this.register = register;
      this.slotType = type;
      this.type = type.getImprecise();
      this.preciseType = type.getPrecise();
    }

    private static Slot stackSlot(int stackPosition, SlotType type) {
      return new Slot(stackPosition + STACK_OFFSET, type);
    }

    private int stackPosition() {
      assert register >= STACK_OFFSET;
      return register - STACK_OFFSET;
    }

    @Override
    public String toString() {
      return register < STACK_OFFSET
          ? register + "=" + slotType
          : "s" + (register - STACK_OFFSET) + "=" + slotType;
    }
  }

  public abstract static class Snapshot {
    final Snapshot parent;
    final int updates;

    private Snapshot(Snapshot parent, int updates) {
      this.parent = parent;
      this.updates = updates;
    }

    public int stackHeight() {
      return parent.stackHeight();
    }

    public int maxLocal() {
      return parent.maxLocal();
    }

    public Slot getStack(int i) {
      return parent.getStack(i);
    }

    public Slot peek() {
      return parent.peek();
    }

    public Slot getLocal(int i) {
      return parent.getLocal(i);
    }

    void build(BaseSnapshot base) {
      parent.build(base);
    }

    BaseSnapshot asBase() {
      return new BaseSnapshot(this);
    }

    public Snapshot exceptionTransfer(DexType throwableType) {
      BaseSnapshot result = new BaseSnapshot(maxLocal() + 1, 1);
      build(result);
      result.stack[0] = new SlotType.Precise(throwableType);
      return result;
    }
  }

  private static class BaseSnapshot extends Snapshot {
    final SlotType[] locals;
    final SlotType[] stack;

    BaseSnapshot() {
      this(0, 0);
    }

    BaseSnapshot(int locals, int stack) {
      super(null, 0);
      this.locals = new SlotType[locals];
      this.stack = new SlotType[stack];
    }

    BaseSnapshot(Snapshot newSnapshot) {
      this(newSnapshot.maxLocal() + 1, newSnapshot.stackHeight());
      newSnapshot.build(this);
    }

    BaseSnapshot(DexType[] locals, DexType[] stack, Position position) {
      super(null, 0);
      assert position != null;
      this.locals = new SlotType[locals.length];
      this.stack = new SlotType[stack.length];
      for (int i = 0; i < locals.length; i++) {
        this.locals[i] = locals[i] == null ? null : getSlotType(locals[i]);
      }
      for (int i = 0; i < stack.length; i++) {
        assert stack[i] != null;
        this.stack[i] = getSlotType(stack[i]);
      }
    }

    private SlotType getSlotType(DexType local) {
      return local.toDescriptorString().equals("NULL")
          ? new SlotType.Imprecise(ValueType.OBJECT)
          : new SlotType.Precise(local);
    }

    @Override
    public int stackHeight() {
      return stack.length;
    }

    @Override
    public int maxLocal() {
      return locals.length - 1;
    }

    @Override
    public Slot getStack(int i) {
      return Slot.stackSlot(i, stack[i]);
    }

    @Override
    public Slot peek() {
      assert stackHeight() > 0;
      return getStack(stackHeight() - 1);
    }

    @Override
    public Slot getLocal(int i) {
      if (i >= locals.length) {
        return null;
      }
      SlotType local = locals[i];
      return local == null ? null : new Slot(i, local);
    }

    @Override
    void build(BaseSnapshot dest) {
      for (int i = 0; i < locals.length && i < dest.locals.length; i++) {
        dest.locals[i] = locals[i];
      }
      for (int i = 0; i < stack.length && i < dest.stack.length; i++) {
        dest.stack[i] = stack[i];
      }
    }

    @Override
    BaseSnapshot asBase() {
      return this;
    }

    @Override
    public String toString() {
      StringBuilder stringBuilder = new StringBuilder().append("stack: [");
      String sep = "";
      for (SlotType type : stack) {
        stringBuilder.append(sep).append(type);
        sep = ", ";
      }
      stringBuilder.append("] locals: [");
      sep = "";
      for (int i = 0; i < locals.length; i++) {
        if (locals[i] != null) {
          stringBuilder.append(sep).append(i).append(':').append(locals[i]);
          sep = ", ";
        }
      }
      return stringBuilder.append(']').toString();
    }
  }

  private static class Push extends Snapshot {

    private final Slot slot;

    Push(Snapshot parent, SlotType type) {
      super(parent, parent.updates + 1);
      slot = Slot.stackSlot(parent.stackHeight(), type);
      assert 100000 <= slot.register && slot.register < 200000;
    }

    @Override
    public int stackHeight() {
      return slot.stackPosition() + 1;
    }

    @Override
    public Slot getStack(int i) {
      return i == slot.stackPosition() ? peek() : parent.getStack(i);
    }

    @Override
    public Slot peek() {
      return slot;
    }

    @Override
    void build(BaseSnapshot base) {
      parent.build(base);
      if (slot.stackPosition() < base.stack.length) {
        base.stack[slot.stackPosition()] = slot.slotType;
      }
    }

    @Override
    public String toString() {
      return parent.toString() + "; push(" + slot.slotType + ")";
    }
  }

  private static class Pop extends Snapshot {

    private final int stackHeight;

    Pop(Snapshot parent) {
      super(parent, parent.updates + 1);
      stackHeight = parent.stackHeight() - 1;
      assert stackHeight >= 0;
    }

    @Override
    public int stackHeight() {
      return stackHeight;
    }

    @Override
    public Slot getStack(int i) {
      assert i < stackHeight;
      return parent.getStack(i);
    }

    @Override
    public Slot peek() {
      return parent.getStack(stackHeight - 1);
    }

    @Override
    public String toString() {
      return parent.toString() + "; pop";
    }
  }

  private static class Write extends Snapshot {

    private final Slot slot;

    Write(Snapshot parent, int slotIndex, SlotType type) {
      super(parent, parent.updates + 1);
      slot = new Slot(slotIndex, type);
      assert 0 <= slotIndex && slotIndex < 100000;
    }

    @Override
    public int maxLocal() {
      return Math.max(slot.register, parent.maxLocal());
    }

    @Override
    public Slot getLocal(int i) {
      return i == slot.register ? slot : parent.getLocal(i);
    }

    @Override
    void build(BaseSnapshot base) {
      parent.build(base);
      base.locals[slot.register] = slot.slotType;
    }

    @Override
    public String toString() {
      return parent.toString() + "; write " + slot.register + " := " + slot.slotType;
    }
  }
}
