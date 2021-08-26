// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class DebugLocalRead extends Instruction {
  private static final String ERROR_MESSAGE = "Unexpected attempt to emit debug-local read.";

  public DebugLocalRead() {
    super(null);
  }

  @Override
  public int opcode() {
    return Opcodes.DEBUG_LOCAL_READ;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isDebugLocalRead() {
    return true;
  }

  @Override
  public DebugLocalRead asDebugLocalRead() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable(ERROR_MESSAGE);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    throw new Unreachable(ERROR_MESSAGE);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isDebugLocalRead();
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
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forDebugLocalRead();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // Reads are never dead code.
    // They should also have a non-empty set of debug values (see RegAlloc::computeDebugInfo)
    return false;
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    // Non-materializing so no stack values are needed.
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
