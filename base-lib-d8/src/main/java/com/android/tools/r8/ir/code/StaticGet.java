// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.code.Sget;
import com.android.tools.r8.code.SgetBoolean;
import com.android.tools.r8.code.SgetByte;
import com.android.tools.r8.code.SgetChar;
import com.android.tools.r8.code.SgetObject;
import com.android.tools.r8.code.SgetShort;
import com.android.tools.r8.code.SgetWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
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

public class StaticGet extends FieldInstruction {

  public StaticGet(Value dest, DexField field) {
    super(field, dest, (Value) null);
  }

  public static StaticGet copyOf(IRCode code, StaticGet original) {
    Value newValue =
        new Value(
            code.valueNumberGenerator.next(),
            original.outValue().getTypeLattice(),
            original.getLocalInfo());
    return copyOf(newValue, original);
  }

  public static StaticGet copyOf(Value newValue, StaticGet original) {
    assert newValue != original.outValue();
    return new StaticGet(newValue, original.getField());
  }

  @Override
  public int opcode() {
    return Opcodes.STATIC_GET;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value dest() {
    return outValue;
  }

  @Override
  public Value value() {
    return outValue;
  }

  @Override
  public boolean couldIntroduceAnAlias(AppView<?> appView, Value root) {
    assert root != null && root.getTypeLattice().isReference();
    assert outValue != null;
    TypeLatticeElement outType = outValue.getTypeLattice();
    if (outType.isPrimitive()) {
      return false;
    }
    if (appView.appInfo().hasSubtyping()) {
      if (outType.isClassType()
          && root.getTypeLattice().isClassType()
          && appView.appInfo().withSubtyping().inDifferentHierarchy(
              outType.asClassTypeLatticeElement().getClassType(),
              root.getTypeLattice().asClassTypeLatticeElement().getClassType())) {
        return false;
      }
    }
    return outType.isReference();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int dest = builder.allocatedRegister(dest(), getNumber());
    DexField field = getField();
    switch (getType()) {
      case INT:
      case FLOAT:
        instruction = new Sget(dest, field);
        break;
      case LONG:
      case DOUBLE:
        instruction = new SgetWide(dest, field);
        break;
      case OBJECT:
        instruction = new SgetObject(dest, field);
        break;
      case BOOLEAN:
        instruction = new SgetBoolean(dest, field);
        break;
      case BYTE:
        instruction = new SgetByte(dest, field);
        break;
      case CHAR:
        instruction = new SgetChar(dest, field);
        break;
      case SHORT:
        instruction = new SgetShort(dest, field);
        break;
      default:
        throw new Unreachable("Unexpected type: " + getType());
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean instructionTypeCanBeCanonicalized() {
    return true;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    // This can cause <clinit> to run.
    return true;
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    return instructionInstanceCanThrow(appView, context).isThrowing();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // static-get can be dead as long as it cannot have any of the following:
    // * NoSuchFieldError (resolution failure)
    // * IncompatibleClassChangeError (static-* instruction for instance fields)
    // * IllegalAccessError (not visible from the access context)
    // * side-effects in <clinit>
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
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
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isStaticGet()) {
      return false;
    }
    StaticGet o = other.asStaticGet();
    return o.getField() == getField() && o.getType() == getType();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forStaticGet(getField(), invocationContext);
  }

  @Override
  public String toString() {
    return super.toString() + "; field: " + getField().toSourceString();
  }

  @Override
  public boolean isStaticGet() {
    return true;
  }

  @Override
  public StaticGet asStaticGet() {
    return this;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(
        new CfFieldInstruction(
            org.objectweb.asm.Opcodes.GETSTATIC, getField(), builder.resolveField(getField())));
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return getField().type;
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.fromDexType(getField().type, Nullability.maybeNull(), appView);
  }

  @Override
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forStaticGet(
        this, clazz, appView, mode, assumption);
  }

  @Override
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return getField().type.isBooleanType();
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    DexType holder = getField().holder;
    if (appView.enableWholeProgramOptimizations()) {
      // In R8, check if the class initialization of the holder or any of its ancestor types may
      // have side effects.
      return holder.classInitializationMayHaveSideEffects(
          appView,
          // Types that are a super type of `context` are guaranteed to be initialized already.
          type -> appView.isSubtype(context, type).isTrue());
    } else {
      // In D8, this instruction may trigger class initialization if the holder of the field is
      // different from the current context.
      return holder != context;
    }
  }
}
