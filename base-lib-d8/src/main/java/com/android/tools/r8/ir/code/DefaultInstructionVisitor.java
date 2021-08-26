// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

public abstract class DefaultInstructionVisitor<T> implements InstructionVisitor<T> {

  public T handleFieldInstruction(FieldInstruction instruction) {
    return null;
  }

  public T handleInvoke(Invoke instruction) {
    return null;
  }

  @Override
  public T visit(Add instruction) {
    return null;
  }

  @Override
  public T visit(AlwaysMaterializingDefinition instruction) {
    return null;
  }

  @Override
  public T visit(AlwaysMaterializingNop instruction) {
    return null;
  }

  @Override
  public T visit(AlwaysMaterializingUser instruction) {
    return null;
  }

  @Override
  public T visit(Assume<?> instruction) {
    return null;
  }

  @Override
  public T visit(And instruction) {
    return null;
  }

  @Override
  public T visit(Argument instruction) {
    return null;
  }

  @Override
  public T visit(ArrayGet instruction) {
    return null;
  }

  @Override
  public T visit(ArrayLength instruction) {
    return null;
  }

  @Override
  public T visit(ArrayPut instruction) {
    return null;
  }

  @Override
  public T visit(CheckCast instruction) {
    return null;
  }

  @Override
  public T visit(Cmp instruction) {
    return null;
  }

  @Override
  public T visit(ConstClass instruction) {
    return null;
  }

  @Override
  public T visit(ConstMethodHandle instruction) {
    return null;
  }

  @Override
  public T visit(ConstMethodType instruction) {
    return null;
  }

  @Override
  public T visit(ConstNumber instruction) {
    return null;
  }

  @Override
  public T visit(ConstString instruction) {
    return null;
  }

  @Override
  public T visit(DebugLocalRead instruction) {
    return null;
  }

  @Override
  public T visit(DebugLocalsChange instruction) {
    return null;
  }

  @Override
  public T visit(DebugLocalUninitialized instruction) {
    return null;
  }

  @Override
  public T visit(DebugLocalWrite instruction) {
    return null;
  }

  @Override
  public T visit(DebugPosition instruction) {
    return null;
  }

  @Override
  public T visit(DexItemBasedConstString instruction) {
    return null;
  }

  @Override
  public T visit(Div instruction) {
    return null;
  }

  @Override
  public T visit(Dup instruction) {
    return null;
  }

  @Override
  public T visit(Dup2 instruction) {
    return null;
  }

  @Override
  public T visit(Goto instruction) {
    return null;
  }

  @Override
  public T visit(If instruction) {
    return null;
  }

  @Override
  public T visit(Inc instruction) {
    return null;
  }

  @Override
  public T visit(InstanceGet instruction) {
    return handleFieldInstruction(instruction);
  }

  @Override
  public T visit(InstanceOf instruction) {
    return null;
  }

  @Override
  public T visit(InstancePut instruction) {
    return handleFieldInstruction(instruction);
  }

  @Override
  public T visit(InvokeCustom instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeDirect instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeInterface instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeMultiNewArray instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeNewArray instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokePolymorphic instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeStatic instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeSuper instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(InvokeVirtual instruction) {
    return handleInvoke(instruction);
  }

  @Override
  public T visit(Load instruction) {
    return null;
  }

  @Override
  public T visit(Monitor instruction) {
    return null;
  }

  @Override
  public T visit(Move instruction) {
    return null;
  }

  @Override
  public T visit(MoveException instruction) {
    return null;
  }

  @Override
  public T visit(Mul instruction) {
    return null;
  }

  @Override
  public T visit(Neg instruction) {
    return null;
  }

  @Override
  public T visit(NewArrayEmpty instruction) {
    return null;
  }

  @Override
  public T visit(NewArrayFilledData instruction) {
    return null;
  }

  @Override
  public T visit(NewInstance instruction) {
    return null;
  }

  @Override
  public T visit(Not instruction) {
    return null;
  }

  @Override
  public T visit(NumberConversion instruction) {
    return null;
  }

  @Override
  public T visit(Or instruction) {
    return null;
  }

  @Override
  public T visit(Pop instruction) {
    return null;
  }

  @Override
  public T visit(Rem instruction) {
    return null;
  }

  @Override
  public T visit(Return instruction) {
    return null;
  }

  @Override
  public T visit(Shl instruction) {
    return null;
  }

  @Override
  public T visit(Shr instruction) {
    return null;
  }

  @Override
  public T visit(StaticGet instruction) {
    return handleFieldInstruction(instruction);
  }

  @Override
  public T visit(StaticPut instruction) {
    return handleFieldInstruction(instruction);
  }

  @Override
  public T visit(Store instruction) {
    return null;
  }

  @Override
  public T visit(Sub instruction) {
    return null;
  }

  @Override
  public T visit(Swap instruction) {
    return null;
  }

  @Override
  public T visit(IntSwitch instruction) {
    return null;
  }

  @Override
  public T visit(StringSwitch instruction) {
    return null;
  }

  @Override
  public T visit(Throw instruction) {
    return null;
  }

  @Override
  public T visit(Ushr instruction) {
    return null;
  }

  @Override
  public T visit(Xor instruction) {
    return null;
  }
}
