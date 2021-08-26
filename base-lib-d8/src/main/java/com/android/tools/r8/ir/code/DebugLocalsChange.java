// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.utils.StringUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;

public class DebugLocalsChange extends Instruction {

  private final Int2ReferenceMap<DebugLocalInfo> ending;
  private final Int2ReferenceMap<DebugLocalInfo> starting;

  public DebugLocalsChange(
      Int2ReferenceMap<DebugLocalInfo> ending, Int2ReferenceMap<DebugLocalInfo> starting) {
    super(null);
    assert !ending.isEmpty() || !starting.isEmpty();
    this.ending = ending;
    this.starting = starting;
  }

  @Override
  public int opcode() {
    return Opcodes.DEBUG_LOCALS_CHANGE;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Int2ReferenceMap<DebugLocalInfo> getEnding() {
    return ending;
  }

  public Int2ReferenceMap<DebugLocalInfo> getStarting() {
    return starting;
  }

  @Override
  public boolean isDebugLocalsChange() {
    return true;
  }

  @Override
  public DebugLocalsChange asDebugLocalsChange() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.addNothing(this);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isDebugLocalsChange()) {
      return false;
    }
    DebugLocalsChange o = other.asDebugLocalsChange();
    return DebugLocalInfo.localsInfoMapsEqual(ending, o.ending)
        && DebugLocalInfo.localsInfoMapsEqual(starting, o.starting);
  }

  @Override
  public int maxInValueRegister() {
    throw new Unreachable();
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(super.toString());
    builder.append("ending: ");
    StringUtils.append(builder, ending.int2ReferenceEntrySet());
    builder.append(", starting: ");
    StringUtils.append(builder, starting.int2ReferenceEntrySet());
    return builder.toString();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forDebugLocalsChange();
  }

  public boolean apply(Int2ReferenceMap<DebugLocalInfo> locals) {
    boolean changed = false;
    for (Entry<DebugLocalInfo> end : getEnding().int2ReferenceEntrySet()) {
      assert locals.get(end.getIntKey()) == end.getValue();
      if (locals.remove(end.getIntKey()) != null) {
        changed = true;
      }
    }
    for (Entry<DebugLocalInfo> start : getStarting().int2ReferenceEntrySet()) {
      assert !locals.containsKey(start.getIntKey());
      DebugLocalInfo old = locals.put(start.getIntKey(), start.getValue());
      changed |= old == null || old != start.getValue();
    }
    return changed;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    throw new Unreachable();
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public void buildCf(CfBuilder builder) {
    throw new Unreachable();
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }

  @Override
  public boolean isAllowedAfterThrowingInstruction() {
    return true;
  }
}
