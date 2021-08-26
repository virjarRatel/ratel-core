// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.code.Sput;
import com.android.tools.r8.code.SputBoolean;
import com.android.tools.r8.code.SputByte;
import com.android.tools.r8.code.SputChar;
import com.android.tools.r8.code.SputObject;
import com.android.tools.r8.code.SputShort;
import com.android.tools.r8.code.SputWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
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
import com.android.tools.r8.shaking.ProguardMemberRule;

public class StaticPut extends FieldInstruction {

  public StaticPut(Value source, DexField field) {
    super(field, null, source);
  }

  @Override
  public int opcode() {
    return Opcodes.STATIC_PUT;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public Value value() {
    assert inValues.size() == 1;
    return inValues.get(0);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int src = builder.allocatedRegister(value(), getNumber());
    DexField field = getField();
    switch (getType()) {
      case INT:
      case FLOAT:
        instruction = new Sput(src, field);
        break;
      case LONG:
      case DOUBLE:
        instruction = new SputWide(src, field);
        break;
      case OBJECT:
        instruction = new SputObject(src, field);
        break;
      case BOOLEAN:
        instruction = new SputBoolean(src, field);
        break;
      case BYTE:
        instruction = new SputByte(src, field);
        break;
      case CHAR:
        instruction = new SputChar(src, field);
        break;
      case SHORT:
        instruction = new SputShort(src, field);
        break;
      default:
        throw new Unreachable("Unexpected type: " + getType());
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean instructionTypeCanThrow() {
    // This can cause <clinit> to run.
    return true;
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    if (appView.appInfo().hasLiveness()) {
      AppInfoWithLiveness appInfoWithLiveness = appView.appInfo().withLiveness();
      // MemberValuePropagation will replace the field read only if the target field has bound
      // -assumevalues rule whose return value is *single*.
      //
      // Note that, in principle, class initializer of the field's holder may have side effects.
      // However, with -assumevalues, we assume that the developer wants to remove field accesses.
      ProguardMemberRule rule = appInfoWithLiveness.assumedValues.get(getField());
      if (rule != null && rule.getReturnValue().isSingleValue()) {
        return false;
      }

      if (instructionInstanceCanThrow(appView, context).isThrowing()) {
        return true;
      }

      DexEncodedField encodedField = appInfoWithLiveness.resolveField(getField());
      assert encodedField != null : "NoSuchFieldError (resolution failure) should be caught.";

      boolean isDeadProtoExtensionField =
          appView.withGeneratedExtensionRegistryShrinker(
              shrinker -> shrinker.isDeadProtoExtensionField(encodedField.field), false);
      if (isDeadProtoExtensionField) {
        return false;
      }

      return appInfoWithLiveness.isFieldRead(encodedField)
          || isStoringObjectWithFinalizer(appInfoWithLiveness);
    }

    // In D8, we always have to assume that the field can be read, and thus have side effects.
    return true;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // static-put can be dead as long as it cannot have any of the following:
    // * NoSuchFieldError (resolution failure)
    // * IncompatibleClassChangeError (static-* instruction for instance fields)
    // * IllegalAccessError (not visible from the access context)
    // * side-effects in <clinit>
    // * not read _globally_
    boolean haveSideEffects = instructionMayHaveSideEffects(appView, code.method.method.holder);
    assert appView.enableWholeProgramOptimizations() || haveSideEffects
        : "Expected static-put instruction to have side effects in D8";
    return !haveSideEffects;
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "StaticPut instructions define no values.";
    return 0;
  }

  @Override
  public boolean identicalAfterRegisterAllocation(Instruction other, RegisterAllocator allocator) {
    if (!super.identicalAfterRegisterAllocation(other, allocator)) {
      return false;
    }

    if (allocator.options().canHaveIncorrectJoinForArrayOfInterfacesBug()) {
      StaticPut staticPut = other.asStaticPut();

      // If the value being written by this instruction is an array, then make sure that the value
      // being written by the other instruction is the exact same value. Otherwise, the verifier
      // may incorrectly join the types of these arrays to Object[].
      if (value().getTypeLattice().isArrayType() && value() != staticPut.value()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    if (!other.isStaticPut()) {
      return false;
    }
    StaticPut o = other.asStaticPut();
    return o.getField() == getField() && o.getType() == getType();
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forStaticPut(getField(), invocationContext);
  }

  @Override
  public String toString() {
    return super.toString() + "; field: " + getField().toSourceString();
  }

  @Override
  public boolean isStaticPut() {
    return true;
  }

  @Override
  public StaticPut asStaticPut() {
    return this;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(
        new CfFieldInstruction(
            org.objectweb.asm.Opcodes.PUTSTATIC, getField(), builder.resolveField(getField())));
  }

  @Override
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return ClassInitializationAnalysis.InstructionUtils.forStaticPut(
        this, clazz, appView, mode, assumption);
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
