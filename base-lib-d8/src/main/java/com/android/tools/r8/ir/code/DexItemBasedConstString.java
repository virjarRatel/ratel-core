// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfDexItemBasedConstString;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.dexitembasedstring.NameComputationInfo;

public class DexItemBasedConstString extends ConstInstruction {

  private final DexReference item;
  private final NameComputationInfo<?> nameComputationInfo;
  private final ThrowingInfo throwingInfo;

  public DexItemBasedConstString(
      Value dest,
      DexReference item,
      NameComputationInfo<?> nameComputationInfo,
      ThrowingInfo throwingInfo) {
    super(dest);
    this.item = item;
    this.nameComputationInfo = nameComputationInfo;
    this.throwingInfo = throwingInfo;
  }

  @Override
  public int opcode() {
    return Opcodes.DEX_ITEM_BASED_CONST_STRING;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public static DexItemBasedConstString copyOf(IRCode code, DexItemBasedConstString original) {
    Value newValue =
        new Value(code.valueNumberGenerator.next(),
            original.outValue().getTypeLattice(),
            original.getLocalInfo());
    return copyOf(newValue, original);
  }

  public static DexItemBasedConstString copyOf(Value newValue, DexItemBasedConstString original) {
    assert newValue != original.outValue();
    return new DexItemBasedConstString(
        newValue, original.getItem(), original.nameComputationInfo, original.throwingInfo);
  }

  public DexReference getItem() {
    return item;
  }

  public NameComputationInfo<?> getNameComputationInfo() {
    return nameComputationInfo;
  }

  @Override
  public boolean instructionTypeCanBeCanonicalized() {
    return true;
  }

  @Override
  public boolean isDexItemBasedConstString() {
    return true;
  }

  @Override
  public DexItemBasedConstString asDexItemBasedConstString() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int dest = builder.allocatedRegister(outValue(), getNumber());
    builder.add(
        this,
        new com.android.tools.r8.code.DexItemBasedConstString(dest, item, nameComputationInfo));
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isDexItemBasedConstString()
        && other.asDexItemBasedConstString().item == item
        && other.asDexItemBasedConstString().nameComputationInfo.equals(nameComputationInfo);
  }

  @Override
  public int maxInValueRegister() {
    assert false : "DexItemBasedConstString has no register arguments.";
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    return super.toString() + " \"" + item.toSourceString() + "\"";
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return throwingInfo == ThrowingInfo.CAN_THROW;
  }

  @Override
  public boolean isOutConstant() {
    return true;
  }

  @Override
  public boolean instructionInstanceCanThrow() {
    // A const-string instruction can usually throw an exception if the decoding of the string
    // fails. Since this string corresponds to a type or member name, though, decoding cannot fail.
    return false;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // No side-effect, such as throwing an exception, in CF.
    return true;
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfDexItemBasedConstString(item, nameComputationInfo));
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return appView.dexItemFactory().stringType;
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    return TypeLatticeElement.stringClassType(appView, Nullability.definitelyNotNull());
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forDexItemBasedConstString(item, invocationContext);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
