// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class Throw extends JumpInstruction {

  public Throw(Value exception) {
    super(exception);
  }

  @Override
  public int opcode() {
    return Opcodes.THROW;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value exception() {
    return inValues.get(0);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.add(this, new com.android.tools.r8.code.Throw(builder.allocatedRegister(exception(), getNumber())));
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "Throw defines no values.";
    return 0;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isThrow();
  }

  @Override
  public boolean isThrow() {
    return true;
  }

  @Override
  public Throw asThrow() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forThrow();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfThrow());
  }

  @Override
  public boolean throwsNpeIfValueIsNull(Value value, DexItemFactory dexItemFactory) {
    if (exception() == value) {
      return true;
    }
    TypeLatticeElement exceptionType = exception().getTypeLattice();
    if (exceptionType.isNullType()) {
      // throw null
      return true;
    }
    if (exceptionType.isDefinitelyNull()) {
      // throw value, where value is null (if the throw instruction type checks, then the static
      // type of `value` must be a subtype of Throwable)
      return true;
    }
    Value aliasedValue = exception().getAliasedValue();
    if (!aliasedValue.isPhi()) {
      Instruction definition = aliasedValue.getDefinition();
      if (definition.isNewInstance()
          && definition.asNewInstance().clazz == dexItemFactory.npeType) {
        // throw new NullPointerException()
        return true;
      }
    }
    return false;
  }
}
