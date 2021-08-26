// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isClassTypeVisibleFromContext;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.code.FilledNewArray;
import com.android.tools.r8.code.FilledNewArrayRange;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.AbstractError;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import java.util.List;

public class InvokeNewArray extends Invoke {

  private final DexType type;

  public InvokeNewArray(DexType type, Value result, List<Value> arguments) {
    super(result, arguments);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.INVOKE_NEW_ARRAY;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public DexType getReturnType() {
    return getArrayType();
  }

  public DexType getArrayType() {
    return type;
  }

  @Override
  public Type getType() {
    return Type.NEW_ARRAY;
  }

  @Override
  protected String getTypeString() {
    return "NewArray";
  }

  @Override
  public String toString() {
    return super.toString() + "; type: " + type.toSourceString();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new FilledNewArrayRange(firstRegister, argumentRegisters, type);
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new FilledNewArray(
          argumentRegistersCount,
          type,
          individualArgumentRegisters[0],  // C
          individualArgumentRegisters[1],  // D
          individualArgumentRegisters[2],  // E
          individualArgumentRegisters[3],  // F
          individualArgumentRegisters[4]); // G
    }
    addInvokeAndMoveResult(instruction, builder);
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isInvokeNewArray() && type == other.asInvokeNewArray().type;
  }

  @Override
  public boolean isInvokeNewArray() {
    return true;
  }

  @Override
  public InvokeNewArray asInvokeNewArray() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInvokeNewArray(type, invocationContext);
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.fromDexType(type, Nullability.definitelyNotNull(), appView);
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    throw cfUnsupported();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    throw cfUnsupported();
  }

  @Override
  public void buildCf(CfBuilder builder) {
    throw cfUnsupported();
  }

  private static Unreachable cfUnsupported() {
    throw new Unreachable("InvokeNewArray (non-empty) not supported when compiling to classfiles.");
  }

  @Override
  public AbstractError instructionInstanceCanThrow(AppView<?> appView, DexType context) {
    DexType baseType = type.isArrayType() ? type.toBaseType(appView.dexItemFactory()) : type;
    if (baseType.isPrimitiveType()) {
      // Primitives types are known to be present and accessible.
      assert !type.isWideType() : "The array's contents must be single-word";
      return AbstractError.bottom();
    }

    assert baseType.isReferenceType();

    if (baseType == context) {
      // The enclosing type is known to be present and accessible.
      return AbstractError.bottom();
    }

    if (!appView.enableWholeProgramOptimizations()) {
      // Conservatively bail-out in D8, because we require whole program knowledge to determine if
      // the type is present and accessible.
      return AbstractError.top();
    }

    // Check if the type is guaranteed to be present.
    DexClass clazz = appView.definitionFor(baseType);
    if (clazz == null) {
      return AbstractError.top();
    }

    if (clazz.isLibraryClass()) {
      if (!appView.dexItemFactory().libraryTypesAssumedToBePresent.contains(baseType)) {
        return AbstractError.top();
      }
    }

    // Check if the type is guaranteed to be accessible.
    if (!isClassTypeVisibleFromContext(appView, context, clazz)) {
      return AbstractError.top();
    }

    // Note: Implicitly assuming that all the arguments are of the right type, because the input
    // code must be valid.
    return AbstractError.bottom();
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    // Check if the instruction has a side effect on the locals environment.
    if (hasOutValue() && outValue().hasLocalInfo()) {
      assert appView.options().debug;
      return true;
    }

    return instructionInstanceCanThrow(appView, context).isThrowing();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
