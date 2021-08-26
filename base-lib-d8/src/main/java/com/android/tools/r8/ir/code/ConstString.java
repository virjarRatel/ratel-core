// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfConstString;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.io.UTFDataFormatException;

public class ConstString extends ConstInstruction {

  private final DexString value;
  private final ThrowingInfo throwingInfo;

  public ConstString(Value dest, DexString value, ThrowingInfo throwingInfo) {
    super(dest);
    this.value = value;
    this.throwingInfo = throwingInfo;
  }

  @Override
  public int opcode() {
    return Opcodes.CONST_STRING;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public static ConstString copyOf(IRCode code, ConstString original) {
    Value newValue =
        new Value(code.valueNumberGenerator.next(),
            original.outValue().getTypeLattice(),
            original.getLocalInfo());
    return copyOf(newValue, original);
  }

  public static ConstString copyOf(Value newValue, ConstString original) {
    assert newValue != original.outValue();
    return new ConstString(newValue, original.getValue(), original.throwingInfo);
  }

  public Value dest() {
    return outValue;
  }

  public DexString getValue() {
    return value;
  }

  @Override
  public boolean instructionTypeCanBeCanonicalized() {
    return true;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int dest = builder.allocatedRegister(dest(), getNumber());
    builder.add(this, new com.android.tools.r8.code.ConstString(dest, value));
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isConstString() && other.asConstString().value == value;
  }

  @Override
  public int maxInValueRegister() {
    assert false : "ConstString has no register arguments.";
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    return super.toString() + " \"" + value + "\"";
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
  public boolean isConstString() {
    return true;
  }

  @Override
  public ConstString asConstString() {
    return this;
  }

  @Override
  public boolean instructionInstanceCanThrow() {
    if (throwingInfo == ThrowingInfo.NO_THROW) {
      return false;
    }
    // The const-string instruction can be a throwing instruction in DEX, if decode() fails.
    try {
      value.toString();
    } catch (RuntimeException e) {
      if (e.getCause() instanceof UTFDataFormatException) {
        return true;
      } else {
        throw e;
      }
    }
    return false;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // No side-effect, such as throwing an exception, in CF.
    return appView.options().isGeneratingClassFiles() || !instructionInstanceCanThrow();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfConstString(value));
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
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }

  @Override
  public AbstractValue getAbstractValue(AppView<?> appView, DexType context) {
    if (!instructionInstanceCanThrow()) {
      return appView.abstractValueFactory().createSingleStringValue(value);
    }
    return UnknownValue.getInstance();
  }
}
