// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfArrayLoad;
import com.android.tools.r8.code.Aget;
import com.android.tools.r8.code.AgetBoolean;
import com.android.tools.r8.code.AgetByte;
import com.android.tools.r8.code.AgetChar;
import com.android.tools.r8.code.AgetObject;
import com.android.tools.r8.code.AgetShort;
import com.android.tools.r8.code.AgetWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
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
import java.util.Set;

public class ArrayGet extends Instruction implements ImpreciseMemberTypeInstruction {

  private MemberType type;

  public ArrayGet(MemberType type, Value dest, Value array, Value index) {
    super(dest, Arrays.asList(array, index));
    this.type = type;
  }

  @Override
  public int opcode() {
    return Opcodes.ARRAY_GET;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public Value dest() {
    return outValue;
  }

  public Value array() {
    return inValues.get(0);
  }

  public Value index() {
    return inValues.get(1);
  }

  @Override
  public MemberType getMemberType() {
    return type;
  }

  @Override
  public boolean couldIntroduceAnAlias(AppView<?> appView, Value root) {
    assert root != null && root.getTypeLattice().isReference();
    assert outValue != null;
    return outValue.getTypeLattice().isReference();
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int dest = builder.allocatedRegister(dest(), getNumber());
    int array = builder.allocatedRegister(array(), getNumber());
    int index = builder.allocatedRegister(index(), getNumber());
    com.android.tools.r8.code.Instruction instruction;
    switch (type) {
      case INT:
      case FLOAT:
        instruction = new Aget(dest, array, index);
        break;
      case LONG:
      case DOUBLE:
        assert builder.getOptions().canUseSameArrayAndResultRegisterInArrayGetWide()
            || dest != array;
        instruction = new AgetWide(dest, array, index);
        break;
      case OBJECT:
        instruction = new AgetObject(dest, array, index);
        break;
      case BOOLEAN_OR_BYTE:
        ArrayTypeLatticeElement arrayType = array().getTypeLattice().asArrayTypeLatticeElement();
        if (arrayType != null
            && arrayType.getArrayMemberTypeAsMemberType() == TypeLatticeElement.BOOLEAN) {
          instruction = new AgetBoolean(dest, array, index);
        } else {
          assert array().getTypeLattice().isDefinitelyNull()
              || arrayType.getArrayMemberTypeAsMemberType() == TypeLatticeElement.BYTE;
          instruction = new AgetByte(dest, array, index);
        }
        break;
      case CHAR:
        instruction = new AgetChar(dest, array, index);
        break;
      case SHORT:
        instruction = new AgetShort(dest, array, index);
        break;
      case INT_OR_FLOAT:
      case LONG_OR_DOUBLE:
        throw new Unreachable("Unexpected imprecise type: " + type);
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean identicalAfterRegisterAllocation(Instruction other, RegisterAllocator allocator) {
    // We cannot share ArrayGet instructions without knowledge of the type of the array input.
    // If multiple primitive array types flow to the same ArrayGet instruction the art verifier
    // gets confused.
    return false;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isArrayGet() && other.asArrayGet().type == type;
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
    // TODO: Determine if the array index out-of-bounds exception cannot happen.
    return true;
  }

  @Override
  public boolean isArrayGet() {
    return true;
  }

  @Override
  public ArrayGet asArrayGet() {
    return this;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forArrayGet();
  }

  @Override
  public boolean hasInvariantOutType() {
    return false;
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    // This method is not called for ArrayGet on primitive array.
    assert this.outValue.getTypeLattice().isReference();
    DexType arrayType = helper.getDexType(array());
    if (arrayType == DexItemFactory.nullValueType) {
      // JVM 8 §4.10.1.9.aaload: Array component type of null is null.
      return arrayType;
    }
    return arrayType.toArrayElementType(appView.dexItemFactory());
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    helper.storeOutValue(this, it);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfArrayLoad(type));
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    ArrayTypeLatticeElement arrayTypeLattice = array().getTypeLattice().isArrayType()
        ? array().getTypeLattice().asArrayTypeLatticeElement()
        : null;
    switch (getMemberType()) {
      case OBJECT:
        // If the out-type of the array is bottom (the input array must be definitely null), then
        // the instruction cannot return. For now we return NULL as the type to ensure we have a
        // type consistent witness for the out-value type. We could consider returning bottom in
        // this case as the value is indeed empty, i.e., the instruction will always fail.
        TypeLatticeElement valueType = arrayTypeLattice == null
            ? TypeLatticeElement.NULL
            : arrayTypeLattice.getArrayMemberTypeAsValueType();
        assert valueType.isReference();
        return valueType;
      case BOOLEAN_OR_BYTE:
      case CHAR:
      case SHORT:
      case INT:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isInt();
        return TypeLatticeElement.INT;
      case FLOAT:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isFloat();
        return TypeLatticeElement.FLOAT;
      case LONG:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isLong();
        return TypeLatticeElement.LONG;
      case DOUBLE:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isDouble();
        return TypeLatticeElement.DOUBLE;
      case INT_OR_FLOAT:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isSinglePrimitive();
        return checkConstraint(dest(), ValueTypeConstraint.INT_OR_FLOAT);
      case LONG_OR_DOUBLE:
        assert arrayTypeLattice == null
            || arrayTypeLattice.getArrayMemberTypeAsValueType().isWidePrimitive();
        return checkConstraint(dest(), ValueTypeConstraint.LONG_OR_DOUBLE);
      default:
        throw new Unreachable("Unexpected member type: " + getMemberType());
    }
  }

  private static TypeLatticeElement checkConstraint(Value value, ValueTypeConstraint constraint) {
    TypeLatticeElement latticeElement = value.constrainedType(constraint);
    if (latticeElement != null) {
      return latticeElement;
    }
    throw new CompilationError(
        "Failure to constrain value: " + value + " by constraint: " + constraint);
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
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return array().getTypeLattice().isArrayType()
        && array().getTypeLattice().asArrayTypeLatticeElement().getArrayMemberTypeAsMemberType()
        == TypeLatticeElement.BOOLEAN;
  }

  @Override
  public void constrainType(TypeConstraintResolver constraintResolver) {
    constraintResolver.constrainArrayMemberType(type, dest(), array(), t -> type = t);
  }

  @Override
  public boolean instructionMayTriggerMethodInvocation(AppView<?> appView, DexType context) {
    return false;
  }
}
