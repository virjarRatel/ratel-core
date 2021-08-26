// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import static com.android.tools.r8.dex.Constants.U8BIT_MAX;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfMonitor;
import com.android.tools.r8.code.MonitorEnter;
import com.android.tools.r8.code.MonitorExit;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class Monitor extends Instruction {

  public enum Type {
    ENTER, EXIT
  }

  private final Type type;

  public Monitor(Type type, Value object) {
    super(null, object);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.MONITOR;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value object() {
    return inValues.get(0);
  }

  public boolean isEnter() {
    return type == Type.ENTER;
  }

  public boolean isExit() {
    return type == Type.EXIT;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    // If the monitor object is an argument, we use the argument register for all the monitor
    // enters and exits in order to not confuse the Art verifier lock verification code.
    // This is best effort. If the argument happens to be in a very high register we cannot
    // do it and the lock verification can hit a case where it gets confused. Not much we
    // can do about that, but this should avoid it in the most common cases.
    int object = builder.argumentOrAllocateRegister(object(), getNumber());
    if (object > maxInValueRegister()) {
      object = builder.allocatedRegister(object(), getNumber());
    }
    if (type == Type.ENTER) {
      builder.add(this, new MonitorEnter(object));
    } else {
      builder.add(this, new MonitorExit(object));
    }
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isMonitor() && other.asMonitor().type == type;
  }

  @Override
  public int maxInValueRegister() {
    return U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "Monitor defines no values.";
    return 0;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean isMonitor() {
    return true;
  }

  @Override
  public boolean isMonitorEnter() {
    return isEnter();
  }

  @Override
  public Monitor asMonitor() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forMonitor();
  }

  @Override
  public String getInstructionName() {
    switch (type) {
      case ENTER:
        return "MonitorEnter";
      case EXIT:
        return "MonitorExit";
      default:
        throw new Unreachable("Unknown monitor type:" + type);
    }
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfMonitor(type));
  }

  @Override
  public boolean throwsNpeIfValueIsNull(Value value, DexItemFactory dexItemFactory) {
    return object() == value;
  }

  @Override
  public boolean throwsOnNullInput() {
    return true;
  }

  @Override
  public Value getNonNullInput() {
    return object();
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
