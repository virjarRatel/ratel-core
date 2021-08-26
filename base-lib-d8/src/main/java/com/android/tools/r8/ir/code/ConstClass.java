// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isClassTypeVisibleFromContext;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfConstClass;
import com.android.tools.r8.dex.Constants;
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

public class ConstClass extends ConstInstruction {

  private final DexType clazz;

  public ConstClass(Value dest, DexType clazz) {
    super(dest);
    this.clazz = clazz;
  }

  @Override
  public int opcode() {
    return Opcodes.CONST_CLASS;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public static ConstClass copyOf(IRCode code, ConstClass original) {
    Value newValue =
        new Value(
            code.valueNumberGenerator.next(),
            original.outValue().getTypeLattice(),
            original.getLocalInfo());
    return copyOf(newValue, original);
  }

  public static ConstClass copyOf(Value newValue, ConstClass original) {
    assert newValue != original.outValue();
    return new ConstClass(newValue, original.getValue());
  }

  public Value dest() {
    return outValue;
  }

  public DexType getValue() {
    return clazz;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int dest = builder.allocatedRegister(dest(), getNumber());
    builder.add(this, new com.android.tools.r8.code.ConstClass(dest, clazz));
  }

  @Override
  public int maxInValueRegister() {
    assert false : "ConstClass has no register arguments.";
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    return super.toString() + clazz.toSourceString();
  }

  @Override
  public boolean instructionTypeCanBeCanonicalized() {
    return true;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean instructionInstanceCanThrow() {
    return true;
  }

  @Override
  public AbstractError instructionInstanceCanThrow(AppView<?> appView, DexType context) {
    DexType baseType = getValue().toBaseType(appView.dexItemFactory());
    if (baseType.isPrimitiveType()) {
      return AbstractError.bottom();
    }

    // Not applicable for D8.
    if (!appView.enableWholeProgramOptimizations()) {
      // Unless the type of interest is same as the context.
      if (baseType == context) {
        return AbstractError.bottom();
      }
      return AbstractError.top();
    }

    DexClass clazz = appView.definitionFor(baseType);

    if (clazz == null) {
      return AbstractError.specific(appView.dexItemFactory().noClassDefFoundErrorType);
    }
    // * NoClassDefFoundError (resolution failure).
    if (!clazz.isResolvable(appView)) {
      return AbstractError.specific(appView.dexItemFactory().noClassDefFoundErrorType);
    }
    // * IllegalAccessError (not visible from the access context).
    if (!isClassTypeVisibleFromContext(appView, context, clazz)) {
      return AbstractError.specific(appView.dexItemFactory().illegalAccessErrorType);
    }

    return AbstractError.bottom();
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    return instructionInstanceCanThrow(appView, context).isThrowing();
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
  }

  @Override
  public boolean isOutConstant() {
    return true;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isConstClass() && other.asConstClass().clazz == clazz;
  }

  @Override
  public boolean isConstClass() {
    return true;
  }

  @Override
  public ConstClass asConstClass() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forConstClass(clazz, invocationContext);
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.classClassType(appView, Nullability.definitelyNotNull());
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return appView.dexItemFactory().classType;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfConstClass(clazz));
  }
}
