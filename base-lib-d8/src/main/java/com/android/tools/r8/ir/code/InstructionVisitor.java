// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

public interface InstructionVisitor<T> {

  T visit(Add instruction);

  T visit(AlwaysMaterializingDefinition instruction);

  T visit(AlwaysMaterializingNop instruction);

  T visit(AlwaysMaterializingUser instruction);

  T visit(And instruction);

  T visit(Argument instruction);

  T visit(ArrayGet instruction);

  T visit(ArrayLength instruction);

  T visit(ArrayPut instruction);

  T visit(Assume<?> instruction);

  T visit(CheckCast instruction);

  T visit(Cmp instruction);

  T visit(ConstClass instruction);

  T visit(ConstMethodHandle instruction);

  T visit(ConstMethodType instruction);

  T visit(ConstNumber instruction);

  T visit(ConstString instruction);

  T visit(DebugLocalRead instruction);

  T visit(DebugLocalsChange instruction);

  T visit(DebugLocalUninitialized instruction);

  T visit(DebugLocalWrite instruction);

  T visit(DebugPosition instruction);

  T visit(DexItemBasedConstString instruction);

  T visit(Div instruction);

  T visit(Dup instruction);

  T visit(Dup2 instruction);

  T visit(Goto instruction);

  T visit(If instruction);

  T visit(Inc instruction);

  T visit(InstanceGet instruction);

  T visit(InstanceOf instruction);

  T visit(InstancePut instruction);

  T visit(InvokeCustom instruction);

  T visit(InvokeDirect instruction);

  T visit(InvokeInterface instruction);

  T visit(InvokeMultiNewArray instruction);

  T visit(InvokeNewArray instruction);

  T visit(InvokePolymorphic instruction);

  T visit(InvokeStatic instruction);

  T visit(InvokeSuper instruction);

  T visit(InvokeVirtual instruction);

  T visit(Load instruction);

  T visit(Monitor instruction);

  T visit(Move instruction);

  T visit(MoveException instruction);

  T visit(Mul instruction);

  T visit(Neg instruction);

  T visit(NewArrayEmpty instruction);

  T visit(NewArrayFilledData instruction);

  T visit(NewInstance instruction);

  T visit(Not instruction);

  T visit(NumberConversion instruction);

  T visit(Or instruction);

  T visit(Pop instruction);

  T visit(Rem instruction);

  T visit(Return instruction);

  T visit(Shl instruction);

  T visit(Shr instruction);

  T visit(StaticGet instruction);

  T visit(StaticPut instruction);

  T visit(Store instruction);

  T visit(Sub instruction);

  T visit(Swap instruction);

  T visit(IntSwitch instruction);

  T visit(StringSwitch instruction);

  T visit(Throw instruction);

  T visit(Ushr instruction);

  T visit(Xor instruction);
}
