// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfCheckCast;
import com.android.tools.r8.code.MoveObject;
import com.android.tools.r8.code.MoveObjectFrom16;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;

public class CheckCast extends Instruction {

  private final DexType type;

  // A CheckCast dex instruction takes only one register containing a value and changes
  // the associated type information for that value. In the IR we let the CheckCast
  // instruction define a new value. During register allocation we then need to arrange it
  // so that the source and destination are assigned the same register.
  public CheckCast(Value dest, Value value, DexType type) {
    super(dest, value);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.CHECK_CAST;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public DexType getType() {
    return type;
  }

  public Value object() {
    return inValues().get(0);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    // The check cast instruction in dex doesn't write a new register. Therefore,
    // if the register allocator could not put input and output in the same register
    // we have to insert a move before the check cast instruction.
    int inRegister = builder.allocatedRegister(inValues.get(0), getNumber());
    if (outValue == null) {
      builder.add(this, new com.android.tools.r8.code.CheckCast(inRegister, type));
    } else {
      int outRegister = builder.allocatedRegister(outValue, getNumber());
      if (inRegister == outRegister) {
        builder.add(this, new com.android.tools.r8.code.CheckCast(outRegister, type));
      } else {
        com.android.tools.r8.code.CheckCast cast =
            new com.android.tools.r8.code.CheckCast(outRegister, type);
        if (outRegister <= Constants.U4BIT_MAX && inRegister <= Constants.U4BIT_MAX) {
          builder.add(this, new MoveObject(outRegister, inRegister), cast);
        } else {
          builder.add(this, new MoveObjectFrom16(outRegister, inRegister), cast);
        }
      }
    }
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isCheckCast() && other.asCheckCast().type == type;
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean isCheckCast() {
    return true;
  }

  @Override
  public CheckCast asCheckCast() {
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + "; " + type;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forCheckCast(type, invocationContext);
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.fromDexType(type, object().getTypeLattice().nullability(), appView);
  }

  @Override
  public boolean verifyTypes(AppView<?> appView) {
    assert super.verifyTypes(appView);

    TypeLatticeElement inType = object().getTypeLattice();

    assert inType.isPreciseType();

    TypeLatticeElement outType = outValue().getTypeLattice();
    TypeLatticeElement castType =
        TypeLatticeElement.fromDexType(getType(), inType.nullability(), appView);

    if (inType.lessThanOrEqual(castType, appView)) {
      // Cast can be removed. Check that it is sound to replace all users of the out-value by the
      // in-value.
      assert inType.lessThanOrEqual(outType, appView);

      // TODO(b/72693244): Consider checking equivalence. This requires that the types are always
      // as precise as possible, though, meaning that almost all changes to the IR must be followed
      // by a fix-point analysis.
      // assert outType.equals(inType);
    } else {
      // We don't have enough information to remove the cast. Check that the out-value does not
      // have a more precise type than the cast-type.
      assert outType.equalUpToNullability(castType);

      // Check soundness of null information.
      assert inType.nullability().lessThanOrEqual(outType.nullability());

      // Since we cannot remove the cast the in-value must be different from null.
      assert !inType.isNullType();

      // TODO(b/72693244): Consider checking equivalence. This requires that the types are always
      // as precise as possible, though, meaning that almost all changes to the IR must be followed
      // by a fix-point analysis.
      // assert outType.equals(castType);
    }
    return true;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    helper.storeOutValue(this, it);
  }

  @Override
  public boolean hasInvariantOutType() {
    // Nullability of in-value can be refined.
    return false;
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return type;
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfCheckCast(type));
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
