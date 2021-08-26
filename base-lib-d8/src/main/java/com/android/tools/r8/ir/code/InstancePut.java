// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.code.Iput;
import com.android.tools.r8.code.IputBoolean;
import com.android.tools.r8.code.IputByte;
import com.android.tools.r8.code.IputChar;
import com.android.tools.r8.code.IputObject;
import com.android.tools.r8.code.IputShort;
import com.android.tools.r8.code.IputWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.Arrays;

public class InstancePut extends FieldInstruction {

  public InstancePut(DexField field, Value object, Value value) {
    super(field, null, Arrays.asList(object, value));
    assert object().verifyCompatible(ValueType.OBJECT);
    assert value().verifyCompatible(ValueType.fromDexType(field.type));
  }

  @Override
  public int opcode() {
    return Opcodes.INSTANCE_PUT;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value object() {
    return inValues.get(0);
  }

  @Override
  public Value value() {
    return inValues.get(1);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int valueRegister = builder.allocatedRegister(value(), getNumber());
    int objectRegister = builder.allocatedRegister(object(), getNumber());
    DexField field = getField();
    switch (getType()) {
      case INT:
      case FLOAT:
        instruction = new Iput(valueRegister, objectRegister, field);
        break;
      case LONG:
      case DOUBLE:
        instruction = new IputWide(valueRegister, objectRegister, field);
        break;
      case OBJECT:
        instruction = new IputObject(valueRegister, objectRegister, field);
        break;
      case BOOLEAN:
        instruction = new IputBoolean(valueRegister, objectRegister, field);
        break;
      case BYTE:
        instruction = new IputByte(valueRegister, objectRegister, field);
        break;
      case CHAR:
        instruction = new IputChar(valueRegister, objectRegister, field);
        break;
      case SHORT:
        instruction = new IputShort(valueRegister, objectRegister, field);
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
    if (appView.appInfo().hasLiveness()) {
      AppInfoWithLiveness appInfoWithLiveness = appView.appInfo().withLiveness();

      if (instructionInstanceCanThrow(appView, context).isThrowing()) {
        return true;
      }

      DexEncodedField encodedField = appInfoWithLiveness.resolveField(getField());
      assert encodedField != null : "NoSuchFieldError (resolution failure) should be caught.";
      return appInfoWithLiveness.isFieldRead(encodedField)
          || isStoringObjectWithFinalizer(appInfoWithLiveness);
    }

    // In D8, we always have to assume that the field can be read, and thus have side effects.
    return true;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // instance-put can be dead as long as it cannot have any of the following:
    // * NoSuchFieldError (resolution failure)
    // * IncompatibleClassChangeError (static-* instruction for instance fields)
    // * IllegalAccessError (not visible from the access context)
    // * NullPointerException (null receiver)
    // * not read at all
    boolean haveSideEffects = instructionMayHaveSideEffects(appView, code.method.method.holder);
    assert appView.enableWholeProgramOptimizations() || haveSideEffects
        : "Expected instance-put instruction to have side effects in D8";
    return !haveSideEffects;
  }

  @Override
  public boolean identicalAfterRegisterAllocation(Instruction other, RegisterAllocator allocator) {
    if (!super.identicalAfterRegisterAllocation(other, allocator)) {
      return false;
    }

    if (allocator.options().canHaveIncorrectJoinForArrayOfInterfacesBug()) {
      InstancePut instancePut = other.asInstancePut();

      // If the value being written by this instruction is an array, then make sure that the value
      // being written by the other instruction is the exact same value. Otherwise, the verifier
      // may incorrectly join the types of these arrays to Object[].
      if (value().getTypeLattice().isArrayType() && value() != instancePut.value()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isInstancePut()) {
      return false;
    }
    InstancePut o = other.asInstancePut();
    return o.getField() == getField() && o.getType() == getType();
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "InstancePut instructions define no values.";
    return 0;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInstancePut(getField(), invocationContext);
  }

  @Override
  public boolean isInstancePut() {
    return true;
  }

  @Override
  public InstancePut asInstancePut() {
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + "; field: " + getField().toSourceString();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(
        new CfFieldInstruction(
            org.objectweb.asm.Opcodes.PUTFIELD, getField(), builder.resolveField(getField())));
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
    return ClassInitializationAnalysis.InstructionUtils.forInstancePut(
        this, clazz, appView, mode, assumption);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
