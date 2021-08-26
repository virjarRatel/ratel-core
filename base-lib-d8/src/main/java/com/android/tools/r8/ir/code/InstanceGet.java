// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.code.Iget;
import com.android.tools.r8.code.IgetBoolean;
import com.android.tools.r8.code.IgetByte;
import com.android.tools.r8.code.IgetChar;
import com.android.tools.r8.code.IgetObject;
import com.android.tools.r8.code.IgetShort;
import com.android.tools.r8.code.IgetWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import java.util.Set;

public class InstanceGet extends FieldInstruction {

  public InstanceGet(Value dest, Value object, DexField field) {
    super(field, dest, object);
  }

  @Override
  public int opcode() {
    return Opcodes.INSTANCE_GET;
  }

  @Override
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return getField().type.isBooleanType();
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value dest() {
    return outValue;
  }

  public Value object() {
    assert inValues.size() == 1;
    return inValues.get(0);
  }

  @Override
  public Value value() {
    return outValue;
  }

  @Override
  public boolean couldIntroduceAnAlias(AppView<?> appView, Value root) {
    assert root != null && root.getTypeLattice().isReference();
    assert outValue != null;
    return outValue.getTypeLattice().isReference();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int destRegister = builder.allocatedRegister(dest(), getNumber());
    int objectRegister = builder.allocatedRegister(object(), getNumber());
    com.android.tools.r8.code.Instruction instruction;
    DexField field = getField();
    switch (getType()) {
      case INT:
      case FLOAT:
        instruction = new Iget(destRegister, objectRegister, field);
        break;
      case LONG:
      case DOUBLE:
        instruction = new IgetWide(destRegister, objectRegister, field);
        break;
      case OBJECT:
        instruction = new IgetObject(destRegister, objectRegister, field);
        break;
      case BOOLEAN:
        instruction = new IgetBoolean(destRegister, objectRegister, field);
        break;
      case BYTE:
        instruction = new IgetByte(destRegister, objectRegister, field);
        break;
      case CHAR:
        instruction = new IgetChar(destRegister, objectRegister, field);
        break;
      case SHORT:
        instruction = new IgetShort(destRegister, objectRegister, field);
        break;
      default:
        throw new Unreachable("Unexpected type: " + getType());
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    return instructionInstanceCanThrow(appView, context).isThrowing();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // instance-get can be dead code as long as it cannot have any of the following:
    // * NoSuchFieldError (resolution failure)
    // * IncompatibleClassChangeError (instance-* instruction for static fields)
    // * IllegalAccessError (not visible from the access context)
    // * NullPointerException (null receiver)
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
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
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isInstanceGet()) {
      return false;
    }
    InstanceGet o = other.asInstanceGet();
    return o.getField() == getField() && o.getType() == getType();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInstanceGet(getField(), invocationContext);
  }

  @Override
  public boolean isInstanceGet() {
    return true;
  }

  @Override
  public InstanceGet asInstanceGet() {
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + "; field: " + getField().toSourceString();
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.fromDexType(getField().type, Nullability.maybeNull(), appView);
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return getField().type;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(
        new CfFieldInstruction(
            org.objectweb.asm.Opcodes.GETFIELD, getField(), builder.resolveField(getField())));
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
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forInstanceGet(
        this, clazz, appView, mode, assumption);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
