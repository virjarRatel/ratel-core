// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

abstract public class Unop extends Instruction {

  public Unop(Value dest, Value source) {
    super(dest, source);
  }

  public Value dest() {
    return outValue;
  }

  public Value source() {
    return inValues.get(0);
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public boolean isUnop() {
    return true;
  }

  @Override
  public Unop asUnop() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forUnop();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    helper.storeOutValue(this, it);
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return source().getTypeLattice();
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
