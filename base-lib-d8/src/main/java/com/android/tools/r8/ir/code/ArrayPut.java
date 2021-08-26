// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.code.CfArrayStore;
import com.android.tools.r8.code.Aput;
import com.android.tools.r8.code.AputBoolean;
import com.android.tools.r8.code.AputByte;
import com.android.tools.r8.code.AputChar;
import com.android.tools.r8.code.AputObject;
import com.android.tools.r8.code.AputShort;
import com.android.tools.r8.code.AputWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.ArrayTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.conversion.TypeConstraintResolver;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import java.util.Arrays;

public class ArrayPut extends Instruction implements ImpreciseMemberTypeInstruction {

  // Input values are ordered according to the stack order of the Java bytecode astore.
  private static final int ARRAY_INDEX = 0;
  private static final int INDEX_INDEX = 1;
  private static final int VALUE_INDEX = 2;

  private MemberType type;

  public ArrayPut(MemberType type, Value array, Value index, Value value) {
    super(null, Arrays.asList(array, index, value));
    assert type != null;
    assert array.verifyCompatible(ValueType.OBJECT);
    assert index.verifyCompatible(ValueType.INT);
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.ARRAY_PUT;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value array() {
    return inValues.get(ARRAY_INDEX);
  }

  public Value index() {
    return inValues.get(INDEX_INDEX);
  }

  public Value value() {
    return inValues.get(VALUE_INDEX);
  }

  @Override
  public MemberType getMemberType() {
    return type;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int value = builder.allocatedRegister(value(), getNumber());
    int array = builder.allocatedRegister(array(), getNumber());
    int index = builder.allocatedRegister(index(), getNumber());
    com.android.tools.r8.code.Instruction instruction;
    switch (type) {
      case INT:
      case FLOAT:
        instruction = new Aput(value, array, index);
        break;
      case LONG:
      case DOUBLE:
        instruction = new AputWide(value, array, index);
        break;
      case OBJECT:
        instruction = new AputObject(value, array, index);
        break;
      case BOOLEAN_OR_BYTE:
        ArrayTypeLatticeElement arrayType = array().getTypeLattice().asArrayTypeLatticeElement();
        if (arrayType != null
            && arrayType.getArrayMemberTypeAsMemberType() == TypeLatticeElement.BOOLEAN) {
          instruction = new AputBoolean(value, array, index);
        } else {
          assert array().getTypeLattice().isDefinitelyNull()
              || arrayType.getArrayMemberTypeAsMemberType() == TypeLatticeElement.BYTE;
          instruction = new AputByte(value, array, index);
        }
        break;
      case CHAR:
        instruction = new AputChar(value, array, index);
        break;
      case SHORT:
        instruction = new AputShort(value, array, index);
        break;
      case INT_OR_FLOAT:
      case LONG_OR_DOUBLE:
        throw new Unreachable("Unexpected imprecise type: " + type);
      default:
        throw new Unreachable("Unexpected type: " + type);
    }
    builder.add(this, instruction);
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "ArrayPut instructions define no values.";
    return 0;
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean instructionInstanceCanThrow() {
    if (index().isConstant() && !array().isPhi() && array().definition.isNewArrayEmpty()) {
      Value newArraySizeValue = array().definition.asNewArrayEmpty().size();
      if (newArraySizeValue.isConstant()) {
        int newArraySize = newArraySizeValue.getConstInstruction().asConstNumber().getIntValue();
        int index = index().getConstInstruction().asConstNumber().getIntValue();
        return newArraySize <= 0 || index < 0 || newArraySize <= index;
      }
    }
    return true;
  }

  @Override
  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    // In debug mode, ArrayPut has a side-effect on the locals.
    if (appView.options().debug) {
      return true;
    }

    // In release mode, ArrayPut has side-effects on the input array, unless the index is in bounds
    // and the array is never used.
    Value array = array().getAliasedValue();
    if (array.isPhi() || !array.definition.isNewArrayEmpty()) {
      return true;
    }

    NewArrayEmpty definition = array.definition.asNewArrayEmpty();
    Value sizeValue = definition.size().getAliasedValue();
    if (sizeValue.isPhi() || !sizeValue.definition.isConstNumber()) {
      return true;
    }

    Value indexValue = index().getAliasedValue();
    if (indexValue.isPhi() || !indexValue.definition.isConstNumber()) {
      return true;
    }

    long index = indexValue.definition.asConstNumber().getRawValue();
    long size = sizeValue.definition.asConstNumber().getRawValue();
    if (index < 0 || index >= size) {
      return true;
    }

    // Check for type errors.
    TypeLatticeElement arrayType = array.getTypeLattice();
    TypeLatticeElement valueType = value().getTypeLattice();
    if (!arrayType.isArrayType()) {
      return true;
    }
    TypeLatticeElement memberType =
        arrayType.asArrayTypeLatticeElement().getArrayMemberTypeAsValueType();
    if (!valueType.lessThanOrEqualUpToNullability(memberType, appView)) {
      return true;
    }

    // Check that all usages of the array are array stores.
    for (Instruction user : array.uniqueUsers()) {
      if (!user.isArrayPut() || user.asArrayPut().array() != array) {
        return true;
      }
    }

    if (array.numberOfPhiUsers() > 0) {
      // The array could be used indirectly.
      return true;
    }

    return false;
  }

  @Override
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    return !instructionMayHaveSideEffects(appView, code.method.method.holder);
  }

  @Override
  public boolean identicalAfterRegisterAllocation(Instruction other, RegisterAllocator allocator) {
    // We cannot share ArrayPut instructions without knowledge of the type of the array input.
    // If multiple primitive array types flow to the same ArrayPut instruction the art verifier
    // gets confused.
    return false;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isArrayPut() && other.asArrayPut().type == type;
  }

  @Override
  public boolean isArrayPut() {
    return true;
  }

  @Override
  public ArrayPut asArrayPut() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forArrayPut();
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
  }

  @Override
  public boolean hasInvariantOutType() {
    return true;
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfArrayStore(type));
  }

  @Override
  public boolean throwsNpeIfValueIsNull(Value value, DexItemFactory dexItemFactory) {
    return array() == value;
  }

  @Override
  public boolean throwsOnNullInput() {
    return true;
  }

  @Override
  public Value getNonNullInput() {
    return array();
  }

  @Override
  public void constrainType(TypeConstraintResolver constraintResolver) {
    constraintResolver.constrainArrayMemberType(type, value(), array(), t -> type = t);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
