// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.MoveResult;
import com.android.tools.r8.code.MoveResultObject;
import com.android.tools.r8.code.MoveResultWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.List;
import java.util.Set;

public abstract class Invoke extends Instruction {

  public enum Type {
    DIRECT,
    INTERFACE,
    STATIC,
    SUPER,
    VIRTUAL,
    NEW_ARRAY,
    MULTI_NEW_ARRAY,
    CUSTOM,
    POLYMORPHIC;

    public MethodHandleType toMethodHandle(DexMethod targetMethod) {
      switch (this) {
        case STATIC:
          return MethodHandleType.INVOKE_STATIC;
        case VIRTUAL:
          return MethodHandleType.INVOKE_INSTANCE;
        case DIRECT:
          if (targetMethod.name.toString().equals("<init>")) {
            return MethodHandleType.INVOKE_CONSTRUCTOR;
          } else {
            return MethodHandleType.INVOKE_DIRECT;
          }
        case INTERFACE:
          return MethodHandleType.INVOKE_INTERFACE;
        case SUPER:
          return MethodHandleType.INVOKE_SUPER;
        default:
          throw new Unreachable(
              "Conversion to method handle with unexpected invoke type: " + this);
      }
    }
  }

  protected Invoke(Value result, List<Value> arguments) {
    super(result, arguments);
  }

  public static Invoke create(
      Type type, DexItem target, DexProto proto, Value result, List<Value> arguments) {
    return create(type, target, proto, result, arguments, false);
  }

  public static Invoke create(
      Type type, DexItem target, DexProto proto, Value result, List<Value> arguments, boolean itf) {
    switch (type) {
      case DIRECT:
        return new InvokeDirect((DexMethod) target, result, arguments, itf);
      case INTERFACE:
        return new InvokeInterface((DexMethod) target, result, arguments);
      case STATIC:
        return new InvokeStatic((DexMethod) target, result, arguments, itf);
      case SUPER:
        return new InvokeSuper((DexMethod) target, result, arguments, itf);
      case VIRTUAL:
        return new InvokeVirtual((DexMethod) target, result, arguments);
      case NEW_ARRAY:
        return new InvokeNewArray((DexType) target, result, arguments);
      case MULTI_NEW_ARRAY:
        return new InvokeMultiNewArray((DexType) target, result, arguments);
      case CUSTOM:
        throw new Unreachable("Use InvokeCustom constructor instead");
      case POLYMORPHIC:
        return new InvokePolymorphic((DexMethod) target, proto, result, arguments);
    }
    throw new Unreachable("Unknown invoke type: " + type);
  }

  abstract public Type getType();

  abstract public DexType getReturnType();

  public boolean hasReturnTypeVoid(DexItemFactory factory) {
    return getReturnType() == factory.voidType;
  }

  public List<Value> arguments() {
    return inValues;
  }

  public int requiredArgumentRegisters() {
    int registers = 0;
    for (Value inValue : inValues) {
      registers += inValue.requiredRegisters();
    }
    return registers;
  }

  protected int argumentRegisterValue(int i, DexBuilder builder) {
    assert needsRangedInvoke(builder);
    if (i < arguments().size()) {
      // If argument values flow into ranged invokes, all the ranged invoke arguments
      // are arguments to this method in order. Therefore, we use the incoming registers
      // for the ranged invoke arguments. We know that arguments are always available there.
      // If argument reuse is allowed there is no splitting and if argument reuse is disallowed
      // the argument registers are never overwritten.
      return builder.argumentOrAllocateRegister(arguments().get(i), getNumber());
    }
    return 0;
  }

  protected int fillArgumentRegisters(DexBuilder builder, int[] registers) {
    assert !needsRangedInvoke(builder);
    int i = 0;
    for (Value value : arguments()) {
      // If one of the arguments to the invoke instruction is an argument of the enclosing method
      // that has been spilled at this location, then we need to take the argument from its
      // original input register (because the register allocator never inserts moves from an
      // argument register to a spill register). Note that this is only a problem if an argument
      // has been spilled to a register that is not the argument's original register.
      //
      // For simplicity, we just use the original input register for all arguments if the register
      // fits in 4 bits.
      int register = builder.argumentOrAllocateRegister(value, getNumber());
      if (register + value.requiredRegisters() - 1 > Constants.U4BIT_MAX) {
        register = builder.allocatedRegister(value, getNumber());
      }
      assert register + value.requiredRegisters() - 1 <= Constants.U4BIT_MAX;
      for (int j = 0; j < value.requiredRegisters(); j++) {
        assert i < 5;
        registers[i++] = register++;
      }
    }
    return i;
  }

  protected boolean argumentsConsecutive(DexBuilder builder) {
    Value value = arguments().get(0);
    int next = builder.argumentOrAllocateRegister(value, getNumber()) + value.requiredRegisters();
    for (int i = 1; i < arguments().size(); i++) {
      value = arguments().get(i);
      assert next == builder.argumentOrAllocateRegister(value, getNumber());
      next += value.requiredRegisters();
    }
    return true;
  }

  protected void addInvokeAndMoveResult(
      com.android.tools.r8.code.Instruction instruction, DexBuilder builder) {
    if (outValue != null && outValue.needsRegister()) {
      TypeLatticeElement moveType = outValue.getTypeLattice();
      int register = builder.allocatedRegister(outValue, getNumber());
      com.android.tools.r8.code.Instruction moveResult;
      if (moveType.isSinglePrimitive()) {
        moveResult = new MoveResult(register);
      } else if (moveType.isWidePrimitive()) {
        moveResult = new MoveResultWide(register);
      } else if (moveType.isReference()) {
        moveResult = new MoveResultObject(register);
      } else {
        throw new Unreachable("Unexpected result type " + outType());
      }
      builder.add(this, instruction, moveResult);
    } else {
      builder.add(this, instruction);
    }
  }

  @Override
  public boolean couldIntroduceAnAlias(AppView<?> appView, Value root) {
    assert root != null && root.getTypeLattice().isReference();
    if (outValue == null) {
      return false;
    }
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
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public int maxInValueRegister() {
    if (arguments().size() == 1
        || requiredArgumentRegisters() > 5
        || argumentsAreConsecutiveInputArguments()) {
      return Constants.U16BIT_MAX;
    }
    return Constants.U4BIT_MAX;
  }

  private boolean argumentsAreConsecutiveInputArguments() {
    if (arguments().size() == 0) {
      return false;
    }
    Value current = arguments().get(0);
    if (!current.isArgument()) {
      return false;
    }
    for (int i = 1; i < arguments().size(); i++) {
      Value next = arguments().get(i);
      if (current.getNextConsecutive() != next) {
        return false;
      }
      current = next;
    }
    return true;
  }

  protected boolean needsRangedInvoke(DexBuilder builder) {
    if (requiredArgumentRegisters() > 5) {
      // No way around using an invoke-range instruction.
      return true;
    }
    // By using an invoke-range instruction when there is only one argument, we avoid having to
    // satisfy the constraint that the argument register(s) must fit in 4 bits.
    boolean registersGuaranteedToBeConsecutive =
        arguments().size() == 1 || argumentsAreConsecutiveInputArguments();
    if (!registersGuaranteedToBeConsecutive) {
      // No way that we will need an invoke-range.
      return false;
    }
    // If we could use an invoke-range instruction, but all the registers fit in 4 bits, then we
    // use a non-range invoke.
    assert argumentsConsecutive(builder);
    int registerStart = builder.argumentOrAllocateRegister(arguments().get(0), getNumber());
    int registerEnd = registerStart + requiredArgumentRegisters() - 1;
    return registerEnd > Constants.U4BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  abstract protected String getTypeString();

  @Override
  public String getInstructionName() {
    return "Invoke-" + getTypeString();
  }

  @Override
  public boolean isInvoke() {
    return true;
  }

  @Override
  public Invoke asInvoke() {
    return this;
  }

  @Override
  public TypeLatticeElement evaluate(AppView<?> appView) {
    DexType returnType = getReturnType();
    if (returnType.isVoidType()) {
      throw new Unreachable("void methods have no type.");
    }
    return TypeLatticeElement.fromDexType(returnType, Nullability.maybeNull(), appView);
  }

  @Override
  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return getReturnType().isBooleanType();
  }
}
