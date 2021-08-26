// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// ***********************************************************************************
// GENERATED FILE. DO NOT EDIT! Changes should be made to GenerateBackportMethods.java
// ***********************************************************************************

package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.cf.code.CfArithmeticBinop;
import com.android.tools.r8.cf.code.CfArrayLength;
import com.android.tools.r8.cf.code.CfArrayLoad;
import com.android.tools.r8.cf.code.CfArrayStore;
import com.android.tools.r8.cf.code.CfCheckCast;
import com.android.tools.r8.cf.code.CfCmp;
import com.android.tools.r8.cf.code.CfConstNumber;
import com.android.tools.r8.cf.code.CfConstString;
import com.android.tools.r8.cf.code.CfGoto;
import com.android.tools.r8.cf.code.CfIf;
import com.android.tools.r8.cf.code.CfIfCmp;
import com.android.tools.r8.cf.code.CfIinc;
import com.android.tools.r8.cf.code.CfInstanceOf;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfLogicalBinop;
import com.android.tools.r8.cf.code.CfNeg;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfNewArray;
import com.android.tools.r8.cf.code.CfNumberConversion;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfReturnVoid;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.code.Cmp;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;

public final class BackportedMethods {

  public static CfCode BooleanMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfIfCmp(If.Type.NE, ValueType.INT, label1),
            new CfConstNumber(0, ValueType.INT),
            new CfGoto(label3),
            label1,
            new CfLoad(ValueType.INT, 0),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label3),
            label2,
            new CfConstNumber(-1, ValueType.INT),
            label3,
            new CfReturn(ValueType.INT),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode BooleanMethods_hashCode(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfIf(If.Type.EQ, ValueType.INT, label1),
            new CfConstNumber(1231, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfConstNumber(1237, ValueType.INT),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ByteMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ByteMethods_compareUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(255, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfLoad(ValueType.INT, 1),
            new CfConstNumber(255, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ByteMethods_toUnsignedInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(255, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ByteMethods_toUnsignedLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(255, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CharacterMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CharacterMethods_toStringCodepoint(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        1,
        ImmutableList.of(
            label0,
            new CfNew(options.itemFactory.createType("Ljava/lang/String;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.INT, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Character;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("[C"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("toChars")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("[C")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfReturn(ValueType.OBJECT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CloseResourceMethod_closeResourceImpl(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    CfLabel label16 = new CfLabel();
    CfLabel label17 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("Ljava/lang/AutoCloseable;")),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("Ljava/lang/AutoCloseable;")),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/AutoCloseable;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("close")),
                true),
            new CfGoto(label11),
            label2,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Class;")),
                    options.itemFactory.createString("getClass")),
                false),
            new CfConstString(options.itemFactory.createString("close")),
            new CfConstNumber(0, ValueType.INT),
            new CfNewArray(options.itemFactory.createType("[Ljava/lang/Class;")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Class;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/reflect/Method;"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("[Ljava/lang/Class;")),
                    options.itemFactory.createString("getMethod")),
                false),
            new CfStore(ValueType.OBJECT, 2),
            label3,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 1),
            new CfConstNumber(0, ValueType.INT),
            new CfNewArray(options.itemFactory.createType("[Ljava/lang/Object;")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/reflect/Method;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("[Ljava/lang/Object;")),
                    options.itemFactory.createString("invoke")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label4,
            new CfGoto(label11),
            label5,
            new CfStore(ValueType.OBJECT, 2),
            label6,
            new CfNew(options.itemFactory.createType("Ljava/lang/AssertionError;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Class;")),
                    options.itemFactory.createString("getClass")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(" does not have a close() method.")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/AssertionError;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("Ljava/lang/Throwable;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label7,
            new CfStore(ValueType.OBJECT, 2),
            label8,
            new CfNew(options.itemFactory.createType("Ljava/lang/AssertionError;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("Fail to call close() on ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Class;")),
                    options.itemFactory.createString("getClass")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/AssertionError;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("Ljava/lang/Throwable;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label9,
            new CfStore(ValueType.OBJECT, 2),
            label10,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/reflect/InvocationTargetException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Throwable;")),
                    options.itemFactory.createString("getCause")),
                false),
            new CfThrow(),
            label11,
            new CfGoto(label16),
            label12,
            new CfStore(ValueType.OBJECT, 2),
            label13,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label14),
            new CfLoad(ValueType.OBJECT, 0),
            new CfGoto(label15),
            label14,
            new CfLoad(ValueType.OBJECT, 2),
            label15,
            new CfThrow(),
            label16,
            new CfReturnVoid(),
            label17),
        ImmutableList.of(
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(
                    options.itemFactory.createType("Ljava/lang/NoSuchMethodException;")),
                ImmutableList.of(label5)),
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(options.itemFactory.createType("Ljava/lang/SecurityException;")),
                ImmutableList.of(label5)),
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(
                    options.itemFactory.createType("Ljava/lang/IllegalAccessException;")),
                ImmutableList.of(label7)),
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(
                    options.itemFactory.createType("Ljava/lang/IllegalArgumentException;")),
                ImmutableList.of(label7)),
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(
                    options.itemFactory.createType("Ljava/lang/ExceptionInInitializerError;")),
                ImmutableList.of(label7)),
            new CfTryCatch(
                label2,
                label4,
                ImmutableList.of(
                    options.itemFactory.createType(
                        "Ljava/lang/reflect/InvocationTargetException;")),
                ImmutableList.of(label9)),
            new CfTryCatch(
                label0,
                label11,
                ImmutableList.of(options.itemFactory.createType("Ljava/lang/Throwable;")),
                ImmutableList.of(label12))),
        ImmutableList.of());
  }

  public static CfCode CollectionMethods_listOfArray(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        6,
        ImmutableList.of(
            label0,
            new CfNew(options.itemFactory.createType("Ljava/util/ArrayList;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 0),
            new CfArrayLength(),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/ArrayList;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfStore(ValueType.OBJECT, 1),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfStore(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 2),
            new CfArrayLength(),
            new CfStore(ValueType.INT, 3),
            new CfConstNumber(0, ValueType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.INT, 3),
            new CfIfCmp(If.Type.GE, ValueType.INT, label5),
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.INT, 4),
            new CfArrayLoad(MemberType.OBJECT),
            new CfStore(ValueType.OBJECT, 5),
            label3,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 5),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/ArrayList;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("add")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label4,
            new CfIinc(4, 1),
            new CfGoto(label2),
            label5,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/List;"),
                        options.itemFactory.createType("Ljava/util/List;")),
                    options.itemFactory.createString("unmodifiableList")),
                false),
            new CfReturn(ValueType.OBJECT),
            label6),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionMethods_mapEntry(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        2,
        ImmutableList.of(
            label0,
            new CfNew(
                options.itemFactory.createType("Ljava/util/AbstractMap$SimpleImmutableEntry;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 0),
            label1,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfLoad(ValueType.OBJECT, 1),
            label2,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/AbstractMap$SimpleImmutableEntry;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("<init>")),
                false),
            label3,
            new CfReturn(ValueType.OBJECT),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionMethods_mapOfEntries(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        8,
        ImmutableList.of(
            label0,
            new CfNew(options.itemFactory.createType("Ljava/util/HashMap;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 0),
            new CfArrayLength(),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/HashMap;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfStore(ValueType.OBJECT, 1),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfStore(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 2),
            new CfArrayLength(),
            new CfStore(ValueType.INT, 3),
            new CfConstNumber(0, ValueType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.INT, 3),
            new CfIfCmp(If.Type.GE, ValueType.INT, label8),
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.INT, 4),
            new CfArrayLoad(MemberType.OBJECT),
            new CfStore(ValueType.OBJECT, 5),
            label3,
            new CfLoad(ValueType.OBJECT, 5),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Map$Entry;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("getKey")),
                true),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfStore(ValueType.OBJECT, 6),
            label4,
            new CfLoad(ValueType.OBJECT, 5),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Map$Entry;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("getValue")),
                true),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfStore(ValueType.OBJECT, 7),
            label5,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 6),
            new CfLoad(ValueType.OBJECT, 7),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/HashMap;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("put")),
                false),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label7),
            label6,
            new CfNew(options.itemFactory.createType("Ljava/lang/IllegalArgumentException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("duplicate key: ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.OBJECT, 6),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/IllegalArgumentException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label7,
            new CfIinc(4, 1),
            new CfGoto(label2),
            label8,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/Map;"),
                        options.itemFactory.createType("Ljava/util/Map;")),
                    options.itemFactory.createString("unmodifiableMap")),
                false),
            new CfReturn(ValueType.OBJECT),
            label9),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionMethods_setOfArray(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        6,
        ImmutableList.of(
            label0,
            new CfNew(options.itemFactory.createType("Ljava/util/HashSet;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 0),
            new CfArrayLength(),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/HashSet;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfStore(ValueType.OBJECT, 1),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfStore(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 2),
            new CfArrayLength(),
            new CfStore(ValueType.INT, 3),
            new CfConstNumber(0, ValueType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.INT, 3),
            new CfIfCmp(If.Type.GE, ValueType.INT, label6),
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.INT, 4),
            new CfArrayLoad(MemberType.OBJECT),
            new CfStore(ValueType.OBJECT, 5),
            label3,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 5),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/HashSet;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("add")),
                false),
            new CfIf(If.Type.NE, ValueType.INT, label5),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/IllegalArgumentException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("duplicate element: ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.OBJECT, 5),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/IllegalArgumentException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5,
            new CfIinc(4, 1),
            new CfGoto(label2),
            label6,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/Set;"),
                        options.itemFactory.createType("Ljava/util/Set;")),
                    options.itemFactory.createString("unmodifiableSet")),
                false),
            new CfReturn(ValueType.OBJECT),
            label7),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionsMethods_emptyEnumeration(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        0,
        ImmutableList.of(
            label0,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/List;")),
                    options.itemFactory.createString("emptyList")),
                false),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/Enumeration;"),
                        options.itemFactory.createType("Ljava/util/Collection;")),
                    options.itemFactory.createString("enumeration")),
                false),
            new CfReturn(ValueType.OBJECT)),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionsMethods_emptyIterator(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        0,
        ImmutableList.of(
            label0,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/List;")),
                    options.itemFactory.createString("emptyList")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/List;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/Iterator;")),
                    options.itemFactory.createString("iterator")),
                true),
            new CfReturn(ValueType.OBJECT)),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode CollectionsMethods_emptyListIterator(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        0,
        ImmutableList.of(
            label0,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Collections;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/List;")),
                    options.itemFactory.createString("emptyList")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/List;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/ListIterator;")),
                    options.itemFactory.createString("listIterator")),
                true),
            new CfReturn(ValueType.OBJECT)),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode DoubleMethods_hashCode(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.DOUBLE, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Double;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"), options.itemFactory.createType("D")),
                    options.itemFactory.createString("doubleToLongBits")),
                false),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(32, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label2),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode DoubleMethods_isFinite(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.DOUBLE, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Double;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"), options.itemFactory.createType("D")),
                    options.itemFactory.createString("isInfinite")),
                false),
            new CfIf(If.Type.NE, ValueType.INT, label1),
            new CfLoad(ValueType.DOUBLE, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Double;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"), options.itemFactory.createType("D")),
                    options.itemFactory.createString("isNaN")),
                false),
            new CfIf(If.Type.NE, ValueType.INT, label1),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfConstNumber(0, ValueType.INT),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode FloatMethods_isFinite(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.FLOAT, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Float;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"), options.itemFactory.createType("F")),
                    options.itemFactory.createString("isInfinite")),
                false),
            new CfIf(If.Type.NE, ValueType.INT, label1),
            new CfLoad(ValueType.FLOAT, 0),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Float;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"), options.itemFactory.createType("F")),
                    options.itemFactory.createString("isNaN")),
                false),
            new CfIf(If.Type.NE, ValueType.INT, label1),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfConstNumber(0, ValueType.INT),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfIfCmp(If.Type.NE, ValueType.INT, label1),
            new CfConstNumber(0, ValueType.INT),
            new CfGoto(label3),
            label1,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfIfCmp(If.Type.GE, ValueType.INT, label2),
            new CfConstNumber(-1, ValueType.INT),
            new CfGoto(label3),
            label2,
            new CfConstNumber(1, ValueType.INT),
            label3,
            new CfReturn(ValueType.INT),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_compareUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(-2147483648, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.INT),
            new CfStore(ValueType.INT, 2),
            label1,
            new CfLoad(ValueType.INT, 1),
            new CfConstNumber(-2147483648, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.INT),
            new CfStore(ValueType.INT, 3),
            label2,
            new CfLoad(ValueType.INT, 2),
            new CfLoad(ValueType.INT, 3),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Integer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("compare")),
                false),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_divideUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        6,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.LONG, 4),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_parseUnsignedInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstNumber(10, ValueType.INT),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Integer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("parseUnsignedInt")),
                false),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_parseUnsignedIntWithRadix(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(options.itemFactory.createType("I")),
                    options.itemFactory.createString("length")),
                false),
            new CfConstNumber(1, ValueType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label2),
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstNumber(0, ValueType.INT),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("charAt")),
                false),
            new CfConstNumber(43, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("substring")),
                false),
            new CfStore(ValueType.OBJECT, 0),
            label2,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("parseLong")),
                false),
            new CfStore(ValueType.LONG, 2),
            label3,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfLoad(ValueType.LONG, 2),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.EQ, ValueType.INT, label5),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/NumberFormatException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("Input ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(" in base ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(
                options.itemFactory.createString(" is not in the range of an unsigned integer")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NumberFormatException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5,
            new CfLoad(ValueType.LONG, 2),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label6),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_remainderUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        6,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.LONG, 4),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Rem, NumericType.LONG),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_toUnsignedLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_toUnsignedString(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(10, ValueType.INT),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Integer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("toUnsignedString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode IntegerMethods_toUnsignedStringWithRadix(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(4294967295L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("toString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label2),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_compareUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        8,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label2,
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 6),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("compare")),
                false),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_divideUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    return new CfCode(
        method.holder,
        6,
        12,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label6),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label3,
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 6),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label5),
            label4,
            new CfConstNumber(0, ValueType.LONG),
            new CfReturn(ValueType.LONG),
            label5,
            new CfConstNumber(1, ValueType.LONG),
            new CfReturn(ValueType.LONG),
            label6,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label8),
            label7,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label8,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shl, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label9,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label10,
            new CfLoad(ValueType.LONG, 6),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 8),
            label11,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 10),
            label12,
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 8),
            new CfLoad(ValueType.LONG, 10),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label13),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label14),
            label13,
            new CfConstNumber(0, ValueType.INT),
            label14,
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label15),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_hashCode(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(32, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_parseUnsignedLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstNumber(10, ValueType.INT),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("parseUnsignedLong")),
                false),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_parseUnsignedLongWithRadix(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    CfLabel label16 = new CfLabel();
    CfLabel label17 = new CfLabel();
    CfLabel label18 = new CfLabel();
    CfLabel label19 = new CfLabel();
    CfLabel label20 = new CfLabel();
    CfLabel label21 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        10,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(options.itemFactory.createType("I")),
                    options.itemFactory.createString("length")),
                false),
            new CfStore(ValueType.INT, 2),
            label1,
            new CfLoad(ValueType.INT, 2),
            new CfIf(If.Type.NE, ValueType.INT, label3),
            label2,
            new CfNew(options.itemFactory.createType("Ljava/lang/NumberFormatException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfConstString(options.itemFactory.createString("empty string")),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NumberFormatException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label3,
            new CfLoad(ValueType.INT, 1),
            new CfConstNumber(2, ValueType.INT),
            new CfIfCmp(If.Type.LT, ValueType.INT, label4),
            new CfLoad(ValueType.INT, 1),
            new CfConstNumber(36, ValueType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label5),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/NumberFormatException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfConstString(options.itemFactory.createString("illegal radix: ")),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("valueOf")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("concat")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NumberFormatException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5,
            new CfConstNumber(-1, ValueType.LONG),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("divideUnsigned")),
                false),
            new CfStore(ValueType.LONG, 3),
            label6,
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstNumber(0, ValueType.INT),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("charAt")),
                false),
            new CfConstNumber(43, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label7),
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label7),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label8),
            label7,
            new CfConstNumber(0, ValueType.INT),
            label8,
            new CfStore(ValueType.INT, 5),
            label9,
            new CfConstNumber(0, ValueType.LONG),
            new CfStore(ValueType.LONG, 6),
            label10,
            new CfLoad(ValueType.INT, 5),
            new CfStore(ValueType.INT, 8),
            label11,
            new CfLoad(ValueType.INT, 8),
            new CfLoad(ValueType.INT, 2),
            new CfIfCmp(If.Type.GE, ValueType.INT, label20),
            label12,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.INT, 8),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("charAt")),
                false),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Character;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("C"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("digit")),
                false),
            new CfStore(ValueType.INT, 9),
            label13,
            new CfLoad(ValueType.INT, 9),
            new CfConstNumber(-1, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label15),
            label14,
            new CfNew(options.itemFactory.createType("Ljava/lang/NumberFormatException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NumberFormatException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label15,
            new CfLoad(ValueType.LONG, 6),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label17),
            new CfLoad(ValueType.LONG, 6),
            new CfLoad(ValueType.LONG, 3),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GT, ValueType.INT, label17),
            new CfLoad(ValueType.LONG, 6),
            new CfLoad(ValueType.LONG, 3),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label18),
            new CfLoad(ValueType.INT, 9),
            new CfConstNumber(-1, ValueType.LONG),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            label16,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("remainderUnsigned")),
                false),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label18),
            label17,
            new CfNew(options.itemFactory.createType("Ljava/lang/NumberFormatException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfConstString(options.itemFactory.createString("Too large for unsigned long: ")),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("concat")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NumberFormatException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label18,
            new CfLoad(ValueType.LONG, 6),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfLoad(ValueType.INT, 9),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label19,
            new CfIinc(8, 1),
            new CfGoto(label11),
            label20,
            new CfLoad(ValueType.LONG, 6),
            new CfReturn(ValueType.LONG),
            label21),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_remainderUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    return new CfCode(
        method.holder,
        6,
        12,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label6),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label3,
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 6),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label5),
            label4,
            new CfLoad(ValueType.LONG, 0),
            new CfReturn(ValueType.LONG),
            label5,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label6,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label8),
            label7,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Rem, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label8,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shl, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label9,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label10,
            new CfLoad(ValueType.LONG, 6),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 8),
            label11,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfStore(ValueType.LONG, 10),
            label12,
            new CfLoad(ValueType.LONG, 6),
            new CfLoad(ValueType.LONG, 8),
            new CfLoad(ValueType.LONG, 10),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label13),
            new CfLoad(ValueType.LONG, 2),
            new CfGoto(label14),
            label13,
            new CfConstNumber(0, ValueType.LONG),
            label14,
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label15),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_toUnsignedString(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(10, ValueType.INT),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("toUnsignedString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode LongMethods_toUnsignedStringWithRadix(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    CfLabel label16 = new CfLabel();
    CfLabel label17 = new CfLabel();
    CfLabel label18 = new CfLabel();
    CfLabel label19 = new CfLabel();
    CfLabel label20 = new CfLabel();
    CfLabel label21 = new CfLabel();
    CfLabel label22 = new CfLabel();
    CfLabel label23 = new CfLabel();
    CfLabel label24 = new CfLabel();
    CfLabel label25 = new CfLabel();
    CfLabel label26 = new CfLabel();
    return new CfCode(
        method.holder,
        6,
        9,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfConstString(options.itemFactory.createString("0")),
            new CfReturn(ValueType.OBJECT),
            label2,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("toString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label4,
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(2, ValueType.INT),
            new CfIfCmp(If.Type.LT, ValueType.INT, label5),
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(36, ValueType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label6),
            label5,
            new CfConstNumber(10, ValueType.INT),
            new CfStore(ValueType.INT, 2),
            label6,
            new CfConstNumber(64, ValueType.INT),
            new CfNewArray(options.itemFactory.createType("[C")),
            new CfStore(ValueType.OBJECT, 3),
            label7,
            new CfLoad(ValueType.OBJECT, 3),
            new CfArrayLength(),
            new CfStore(ValueType.INT, 4),
            label8,
            new CfLoad(ValueType.INT, 2),
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfIf(If.Type.NE, ValueType.INT, label15),
            label9,
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Integer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("numberOfTrailingZeros")),
                false),
            new CfStore(ValueType.INT, 5),
            label10,
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfStore(ValueType.INT, 6),
            label11,
            new CfLoad(ValueType.OBJECT, 3),
            new CfIinc(4, -1),
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.LONG, 0),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfLoad(ValueType.INT, 6),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Character;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("forDigit")),
                false),
            new CfArrayStore(MemberType.CHAR),
            label12,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 5),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfStore(ValueType.LONG, 0),
            label13,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label11),
            label14,
            new CfGoto(label25),
            label15,
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfIf(If.Type.NE, ValueType.INT, label18),
            label16,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.LONG),
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Ushr, NumericType.INT),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfStore(ValueType.LONG, 5),
            label17,
            new CfGoto(label19),
            label18,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("divideUnsigned")),
                false),
            new CfStore(ValueType.LONG, 5),
            label19,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 5),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 7),
            label20,
            new CfLoad(ValueType.OBJECT, 3),
            new CfIinc(4, -1),
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.LONG, 7),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Character;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("forDigit")),
                false),
            new CfArrayStore(MemberType.CHAR),
            label21,
            new CfLoad(ValueType.LONG, 5),
            new CfStore(ValueType.LONG, 0),
            label22,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LE, ValueType.INT, label25),
            label23,
            new CfLoad(ValueType.OBJECT, 3),
            new CfIinc(4, -1),
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Rem, NumericType.LONG),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Character;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("C"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("forDigit")),
                false),
            new CfArrayStore(MemberType.CHAR),
            label24,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfStore(ValueType.LONG, 0),
            new CfGoto(label22),
            label25,
            new CfNew(options.itemFactory.createType("Ljava/lang/String;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 3),
            new CfLoad(ValueType.INT, 4),
            new CfLoad(ValueType.OBJECT, 3),
            new CfArrayLength(),
            new CfLoad(ValueType.INT, 4),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/String;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("[C"),
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfReturn(ValueType.OBJECT),
            label26),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_addExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        5,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.INT, 4),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.INT, 4),
            new CfReturn(ValueType.INT),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_addExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        6,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label2),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label3),
            label2,
            new CfConstNumber(0, ValueType.INT),
            label3,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 4),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label4),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label5),
            label4,
            new CfConstNumber(0, ValueType.INT),
            label5,
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.INT),
            new CfIf(If.Type.EQ, ValueType.INT, label7),
            label6,
            new CfLoad(ValueType.LONG, 4),
            new CfReturn(ValueType.LONG),
            label7,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label8),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_decrementExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(-2147483648, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_decrementExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(1, ValueType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorDivInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        5,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.INT),
            new CfStore(ValueType.INT, 2),
            label1,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfLoad(ValueType.INT, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfStore(ValueType.INT, 3),
            label2,
            new CfLoad(ValueType.INT, 3),
            new CfIf(If.Type.NE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.INT, 2),
            new CfReturn(ValueType.INT),
            label4,
            new CfConstNumber(1, ValueType.INT),
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.INT),
            new CfConstNumber(31, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shr, NumericType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.INT),
            new CfStore(ValueType.INT, 4),
            label5,
            new CfLoad(ValueType.INT, 4),
            new CfIf(If.Type.GE, ValueType.INT, label6),
            new CfLoad(ValueType.INT, 2),
            new CfConstNumber(1, ValueType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfGoto(label7),
            label6,
            new CfLoad(ValueType.INT, 2),
            label7,
            new CfReturn(ValueType.INT),
            label8),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorDivLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    return new CfCode(
        method.holder,
        6,
        10,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.LONG, 4),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label2,
            new CfLoad(ValueType.LONG, 6),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.LONG, 4),
            new CfReturn(ValueType.LONG),
            label4,
            new CfConstNumber(1, ValueType.LONG),
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(63, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shr, NumericType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.LONG),
            new CfStore(ValueType.LONG, 8),
            label5,
            new CfLoad(ValueType.LONG, 8),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.GE, ValueType.INT, label6),
            new CfLoad(ValueType.LONG, 4),
            new CfConstNumber(1, ValueType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfGoto(label7),
            label6,
            new CfLoad(ValueType.LONG, 4),
            label7,
            new CfReturn(ValueType.LONG),
            label8),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorDivLongInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Math;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("floorDiv")),
                false),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorModInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Rem, NumericType.INT),
            new CfStore(ValueType.INT, 2),
            label1,
            new CfLoad(ValueType.INT, 2),
            new CfIf(If.Type.NE, ValueType.INT, label3),
            label2,
            new CfConstNumber(0, ValueType.INT),
            new CfReturn(ValueType.INT),
            label3,
            new CfConstNumber(1, ValueType.INT),
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.INT),
            new CfConstNumber(31, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shr, NumericType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.INT),
            new CfStore(ValueType.INT, 3),
            label4,
            new CfLoad(ValueType.INT, 3),
            new CfIf(If.Type.LE, ValueType.INT, label5),
            new CfLoad(ValueType.INT, 2),
            new CfGoto(label6),
            label5,
            new CfLoad(ValueType.INT, 2),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.INT),
            label6,
            new CfReturn(ValueType.INT),
            label7),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorModLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    return new CfCode(
        method.holder,
        6,
        8,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Rem, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label1,
            new CfLoad(ValueType.LONG, 4),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label3),
            label2,
            new CfConstNumber(0, ValueType.LONG),
            new CfReturn(ValueType.LONG),
            label3,
            new CfConstNumber(1, ValueType.LONG),
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(63, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Shr, NumericType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.LONG),
            new CfStore(ValueType.LONG, 6),
            label4,
            new CfLoad(ValueType.LONG, 6),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LE, ValueType.INT, label5),
            new CfLoad(ValueType.LONG, 4),
            new CfGoto(label6),
            label5,
            new CfLoad(ValueType.LONG, 4),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            label6,
            new CfReturn(ValueType.LONG),
            label7),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_floorModLongInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Math;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("floorMod")),
                false),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_incrementExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(2147483647, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(1, ValueType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.INT),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_incrementExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(9223372036854775807L, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(1, ValueType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_multiplyExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        5,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.INT, 4),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.INT, 4),
            new CfReturn(ValueType.INT),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_multiplyExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    CfLabel label16 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        7,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            label1,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"), options.itemFactory.createType("J")),
                    options.itemFactory.createString("numberOfLeadingZeros")),
                false),
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-1, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            label2,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"), options.itemFactory.createType("J")),
                    options.itemFactory.createString("numberOfLeadingZeros")),
                false),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.INT),
            new CfLoad(ValueType.LONG, 2),
            label3,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"), options.itemFactory.createType("J")),
                    options.itemFactory.createString("numberOfLeadingZeros")),
                false),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.INT),
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-1, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            label4,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Long;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"), options.itemFactory.createType("J")),
                    options.itemFactory.createString("numberOfLeadingZeros")),
                false),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Add, NumericType.INT),
            new CfStore(ValueType.INT, 4),
            label5,
            new CfLoad(ValueType.INT, 4),
            new CfConstNumber(65, ValueType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label7),
            label6,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label7,
            new CfLoad(ValueType.INT, 4),
            new CfConstNumber(64, ValueType.INT),
            new CfIfCmp(If.Type.LT, ValueType.INT, label15),
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label8),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label9),
            label8,
            new CfConstNumber(0, ValueType.INT),
            label9,
            new CfLoad(ValueType.LONG, 2),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.EQ, ValueType.INT, label10),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label11),
            label10,
            new CfConstNumber(0, ValueType.INT),
            label11,
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.INT),
            new CfIf(If.Type.EQ, ValueType.INT, label15),
            label12,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Mul, NumericType.LONG),
            new CfStore(ValueType.LONG, 5),
            label13,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.EQ, ValueType.INT, label14),
            new CfLoad(ValueType.LONG, 5),
            new CfLoad(ValueType.LONG, 0),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Div, NumericType.LONG),
            new CfLoad(ValueType.LONG, 2),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label15),
            label14,
            new CfLoad(ValueType.LONG, 5),
            new CfReturn(ValueType.LONG),
            label15,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label16),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_multiplyExactLongInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Math;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J"),
                        options.itemFactory.createType("J")),
                    options.itemFactory.createString("multiplyExact")),
                false),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_negateExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(-2147483648, ValueType.INT),
            new CfIfCmp(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfNeg(NumericType.INT),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_negateExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfConstNumber(-9223372036854775808L, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.LONG, 0),
            new CfNeg(NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_nextDownDouble(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.DOUBLE, 0),
            new CfNeg(NumericType.DOUBLE),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Math;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("D"), options.itemFactory.createType("D")),
                    options.itemFactory.createString("nextUp")),
                false),
            new CfNeg(NumericType.DOUBLE),
            new CfReturn(ValueType.DOUBLE),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_nextDownFloat(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.FLOAT, 0),
            new CfNeg(NumericType.FLOAT),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Math;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("F"), options.itemFactory.createType("F")),
                    options.itemFactory.createString("nextUp")),
                false),
            new CfNeg(NumericType.FLOAT),
            new CfReturn(ValueType.FLOAT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_subtractExactInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        5,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfLoad(ValueType.INT, 1),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 2),
            label1,
            new CfLoad(ValueType.LONG, 2),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfStore(ValueType.INT, 4),
            label2,
            new CfLoad(ValueType.LONG, 2),
            new CfLoad(ValueType.INT, 4),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.NE, ValueType.INT, label4),
            label3,
            new CfLoad(ValueType.INT, 4),
            new CfReturn(ValueType.INT),
            label4,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label5),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_subtractExactLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    return new CfCode(
        method.holder,
        5,
        6,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.LONG),
            new CfStore(ValueType.LONG, 4),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 2),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label2),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label3),
            label2,
            new CfConstNumber(0, ValueType.INT),
            label3,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.LONG, 4),
            new CfLogicalBinop(CfLogicalBinop.Opcode.Xor, NumericType.LONG),
            new CfConstNumber(0, ValueType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.LT, ValueType.INT, label4),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label5),
            label4,
            new CfConstNumber(0, ValueType.INT),
            label5,
            new CfLogicalBinop(CfLogicalBinop.Opcode.Or, NumericType.INT),
            new CfIf(If.Type.EQ, ValueType.INT, label7),
            label6,
            new CfLoad(ValueType.LONG, 4),
            new CfReturn(ValueType.LONG),
            label7,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label8),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode MathMethods_toIntExact(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.LONG, 0),
            new CfNumberConversion(NumericType.LONG, NumericType.INT),
            new CfStore(ValueType.INT, 2),
            label1,
            new CfLoad(ValueType.LONG, 0),
            new CfLoad(ValueType.INT, 2),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfCmp(Cmp.Bias.NONE, NumericType.LONG),
            new CfIf(If.Type.EQ, ValueType.INT, label3),
            label2,
            new CfNew(options.itemFactory.createType("Ljava/lang/ArithmeticException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/ArithmeticException;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label3,
            new CfLoad(ValueType.INT, 2),
            new CfReturn(ValueType.INT),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_checkFromIndexSize(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfIf(If.Type.LT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 1),
            new CfIf(If.Type.LT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 2),
            new CfIf(If.Type.LT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 2),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfIfCmp(If.Type.LE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("Range [")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(", ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(" + ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(") out of bounds for length ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_checkFromToIndex(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfIf(If.Type.LT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfIfCmp(If.Type.GT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 1),
            new CfLoad(ValueType.INT, 2),
            new CfIfCmp(If.Type.LE, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("Range [")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(", ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(") out of bounds for length ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 2),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_checkIndex(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfIf(If.Type.LT, ValueType.INT, label1),
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfIfCmp(If.Type.LT, ValueType.INT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfConstString(options.itemFactory.createString("Index ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfConstString(options.itemFactory.createString(" out of bounds for length ")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("append")),
                false),
            new CfLoad(ValueType.INT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("I")),
                    options.itemFactory.createString("append")),
                false),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/IndexOutOfBoundsException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.INT, 0),
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfIfCmp(If.Type.NE, ValueType.OBJECT, label1),
            new CfConstNumber(0, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Comparator;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("I"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("compare")),
                true),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_deepEquals(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    CfLabel label11 = new CfLabel();
    CfLabel label12 = new CfLabel();
    CfLabel label13 = new CfLabel();
    CfLabel label14 = new CfLabel();
    CfLabel label15 = new CfLabel();
    CfLabel label16 = new CfLabel();
    CfLabel label17 = new CfLabel();
    CfLabel label18 = new CfLabel();
    CfLabel label19 = new CfLabel();
    CfLabel label20 = new CfLabel();
    CfLabel label21 = new CfLabel();
    CfLabel label22 = new CfLabel();
    CfLabel label23 = new CfLabel();
    CfLabel label24 = new CfLabel();
    CfLabel label25 = new CfLabel();
    CfLabel label26 = new CfLabel();
    CfLabel label27 = new CfLabel();
    CfLabel label28 = new CfLabel();
    CfLabel label29 = new CfLabel();
    CfLabel label30 = new CfLabel();
    CfLabel label31 = new CfLabel();
    CfLabel label32 = new CfLabel();
    CfLabel label33 = new CfLabel();
    CfLabel label34 = new CfLabel();
    CfLabel label35 = new CfLabel();
    CfLabel label36 = new CfLabel();
    CfLabel label37 = new CfLabel();
    CfLabel label38 = new CfLabel();
    CfLabel label39 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfIfCmp(If.Type.NE, ValueType.OBJECT, label1),
            new CfConstNumber(1, ValueType.INT),
            new CfReturn(ValueType.INT),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label2),
            new CfConstNumber(0, ValueType.INT),
            new CfReturn(ValueType.INT),
            label2,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[Z")),
            new CfIf(If.Type.EQ, ValueType.INT, label6),
            label3,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[Z")),
            new CfIf(If.Type.EQ, ValueType.INT, label4),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[Z")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[Z")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[Z"),
                        options.itemFactory.createType("[Z")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label4),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label5),
            label4,
            new CfConstNumber(0, ValueType.INT),
            label5,
            new CfReturn(ValueType.INT),
            label6,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[B")),
            new CfIf(If.Type.EQ, ValueType.INT, label10),
            label7,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[B")),
            new CfIf(If.Type.EQ, ValueType.INT, label8),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[B")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[B")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[B"),
                        options.itemFactory.createType("[B")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label8),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label9),
            label8,
            new CfConstNumber(0, ValueType.INT),
            label9,
            new CfReturn(ValueType.INT),
            label10,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[C")),
            new CfIf(If.Type.EQ, ValueType.INT, label14),
            label11,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[C")),
            new CfIf(If.Type.EQ, ValueType.INT, label12),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[C")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[C")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[C"),
                        options.itemFactory.createType("[C")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label12),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label13),
            label12,
            new CfConstNumber(0, ValueType.INT),
            label13,
            new CfReturn(ValueType.INT),
            label14,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[D")),
            new CfIf(If.Type.EQ, ValueType.INT, label18),
            label15,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[D")),
            new CfIf(If.Type.EQ, ValueType.INT, label16),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[D")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[D")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[D"),
                        options.itemFactory.createType("[D")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label16),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label17),
            label16,
            new CfConstNumber(0, ValueType.INT),
            label17,
            new CfReturn(ValueType.INT),
            label18,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[F")),
            new CfIf(If.Type.EQ, ValueType.INT, label22),
            label19,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[F")),
            new CfIf(If.Type.EQ, ValueType.INT, label20),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[F")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[F")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[F"),
                        options.itemFactory.createType("[F")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label20),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label21),
            label20,
            new CfConstNumber(0, ValueType.INT),
            label21,
            new CfReturn(ValueType.INT),
            label22,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[I")),
            new CfIf(If.Type.EQ, ValueType.INT, label26),
            label23,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[I")),
            new CfIf(If.Type.EQ, ValueType.INT, label24),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[I")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[I")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[I"),
                        options.itemFactory.createType("[I")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label24),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label25),
            label24,
            new CfConstNumber(0, ValueType.INT),
            label25,
            new CfReturn(ValueType.INT),
            label26,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[J")),
            new CfIf(If.Type.EQ, ValueType.INT, label30),
            label27,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[J")),
            new CfIf(If.Type.EQ, ValueType.INT, label28),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[J")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[J")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[J"),
                        options.itemFactory.createType("[J")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label28),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label29),
            label28,
            new CfConstNumber(0, ValueType.INT),
            label29,
            new CfReturn(ValueType.INT),
            label30,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[S")),
            new CfIf(If.Type.EQ, ValueType.INT, label34),
            label31,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[S")),
            new CfIf(If.Type.EQ, ValueType.INT, label32),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[S")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[S")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[S"),
                        options.itemFactory.createType("[S")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label32),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label33),
            label32,
            new CfConstNumber(0, ValueType.INT),
            label33,
            new CfReturn(ValueType.INT),
            label34,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInstanceOf(options.itemFactory.createType("[Ljava/lang/Object;")),
            new CfIf(If.Type.EQ, ValueType.INT, label38),
            label35,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInstanceOf(options.itemFactory.createType("[Ljava/lang/Object;")),
            new CfIf(If.Type.EQ, ValueType.INT, label36),
            new CfLoad(ValueType.OBJECT, 0),
            new CfCheckCast(options.itemFactory.createType("[Ljava/lang/Object;")),
            new CfLoad(ValueType.OBJECT, 1),
            new CfCheckCast(options.itemFactory.createType("[Ljava/lang/Object;")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Arrays;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("[Ljava/lang/Object;"),
                        options.itemFactory.createType("[Ljava/lang/Object;")),
                    options.itemFactory.createString("deepEquals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label36),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label37),
            label36,
            new CfConstNumber(0, ValueType.INT),
            label37,
            new CfReturn(ValueType.INT),
            label38,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("equals")),
                false),
            new CfReturn(ValueType.INT),
            label39),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_equals(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfIfCmp(If.Type.EQ, ValueType.OBJECT, label1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label2),
            new CfLoad(ValueType.OBJECT, 0),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Z"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("equals")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label3),
            label2,
            new CfConstNumber(0, ValueType.INT),
            label3,
            new CfReturn(ValueType.INT),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_hashCode(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label1),
            new CfConstNumber(0, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(options.itemFactory.createType("I")),
                    options.itemFactory.createString("hashCode")),
                false),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_isNull(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label1),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfConstNumber(0, ValueType.INT),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_nonNull(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label1),
            new CfConstNumber(1, ValueType.INT),
            new CfGoto(label2),
            label1,
            new CfConstNumber(0, ValueType.INT),
            label2,
            new CfReturn(ValueType.INT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_requireNonNullElse(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfReturn(ValueType.OBJECT),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfConstString(options.itemFactory.createString("defaultObj")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfReturn(ValueType.OBJECT),
            label2),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_requireNonNullElseGet(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.EQ, ValueType.OBJECT, label1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfReturn(ValueType.OBJECT),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfConstString(options.itemFactory.createString("supplier")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfCheckCast(options.itemFactory.createType("Ljava/util/function/Supplier;")),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/Supplier;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("get")),
                true),
            new CfStore(ValueType.OBJECT, 2),
            label2,
            new CfLoad(ValueType.OBJECT, 2),
            new CfConstString(options.itemFactory.createString("supplier.get()")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfReturn(ValueType.OBJECT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_requireNonNullMessage(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label2),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/NullPointerException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NullPointerException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label2,
            new CfLoad(ValueType.OBJECT, 0),
            new CfReturn(ValueType.OBJECT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_toString(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfConstString(options.itemFactory.createString("null")),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;"),
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ObjectsMethods_toStringDefault(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label1),
            new CfLoad(ValueType.OBJECT, 1),
            new CfGoto(label2),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Object;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            label2,
            new CfReturn(ValueType.OBJECT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_ifPresentOrElse(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Optional;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Optional;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("get")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/Consumer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("accept")),
                true),
            new CfGoto(label3),
            label2,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Runnable;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("run")),
                true),
            label3,
            new CfReturnVoid(),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_ifPresentOrElseDouble(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalDouble;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalDouble;"),
                    options.itemFactory.createProto(options.itemFactory.createType("D")),
                    options.itemFactory.createString("getAsDouble")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/DoubleConsumer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("D")),
                    options.itemFactory.createString("accept")),
                true),
            new CfGoto(label3),
            label2,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Runnable;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("run")),
                true),
            label3,
            new CfReturnVoid(),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_ifPresentOrElseInt(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalInt;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalInt;"),
                    options.itemFactory.createProto(options.itemFactory.createType("I")),
                    options.itemFactory.createString("getAsInt")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/IntConsumer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("I")),
                    options.itemFactory.createString("accept")),
                true),
            new CfGoto(label3),
            label2,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Runnable;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("run")),
                true),
            label3,
            new CfReturnVoid(),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_ifPresentOrElseLong(
      InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalLong;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/OptionalLong;"),
                    options.itemFactory.createProto(options.itemFactory.createType("J")),
                    options.itemFactory.createString("getAsLong")),
                false),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/LongConsumer;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"), options.itemFactory.createType("J")),
                    options.itemFactory.createString("accept")),
                true),
            new CfGoto(label3),
            label2,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Runnable;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("run")),
                true),
            label3,
            new CfReturnVoid(),
            label4),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_or(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        3,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Optional;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label3),
            label2,
            new CfLoad(ValueType.OBJECT, 0),
            new CfReturn(ValueType.OBJECT),
            label3,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/function/Supplier;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("get")),
                true),
            new CfCheckCast(options.itemFactory.createType("Ljava/util/Optional;")),
            new CfStore(ValueType.OBJECT, 2),
            label4,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Objects;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("requireNonNull")),
                false),
            new CfCheckCast(options.itemFactory.createType("Ljava/util/Optional;")),
            new CfReturn(ValueType.OBJECT),
            label5),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode OptionalMethods_stream(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    return new CfCode(
        method.holder,
        1,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Optional;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("isPresent")),
                false),
            new CfIf(If.Type.EQ, ValueType.INT, label2),
            label1,
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Optional;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("get")),
                false),
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/stream/Stream;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/stream/Stream;"),
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("of")),
                true),
            new CfReturn(ValueType.OBJECT),
            label2,
            new CfInvoke(
                184,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/stream/Stream;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/stream/Stream;")),
                    options.itemFactory.createString("empty")),
                true),
            new CfReturn(ValueType.OBJECT),
            label3),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ShortMethods_compare(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfLoad(ValueType.INT, 1),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ShortMethods_compareUnsigned(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        2,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(65535, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfLoad(ValueType.INT, 1),
            new CfConstNumber(65535, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfArithmeticBinop(CfArithmeticBinop.Opcode.Sub, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ShortMethods_toUnsignedInt(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        2,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfConstNumber(65535, ValueType.INT),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.INT),
            new CfReturn(ValueType.INT),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode ShortMethods_toUnsignedLong(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    return new CfCode(
        method.holder,
        4,
        1,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.INT, 0),
            new CfNumberConversion(NumericType.INT, NumericType.LONG),
            new CfConstNumber(65535, ValueType.LONG),
            new CfLogicalBinop(CfLogicalBinop.Opcode.And, NumericType.LONG),
            new CfReturn(ValueType.LONG),
            label1),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode StringMethods_joinArray(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    CfLabel label10 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label1),
            new CfNew(options.itemFactory.createType("Ljava/lang/NullPointerException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfConstString(options.itemFactory.createString("delimiter")),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NullPointerException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfStore(ValueType.OBJECT, 2),
            label2,
            new CfLoad(ValueType.OBJECT, 1),
            new CfArrayLength(),
            new CfIf(If.Type.LE, ValueType.INT, label9),
            label3,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 1),
            new CfConstNumber(0, ValueType.INT),
            new CfArrayLoad(MemberType.OBJECT),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label4,
            new CfConstNumber(1, ValueType.INT),
            new CfStore(ValueType.INT, 3),
            label5,
            new CfLoad(ValueType.INT, 3),
            new CfLoad(ValueType.OBJECT, 1),
            new CfArrayLength(),
            new CfIfCmp(If.Type.GE, ValueType.INT, label9),
            label6,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label7,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 1),
            new CfLoad(ValueType.INT, 3),
            new CfArrayLoad(MemberType.OBJECT),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label8,
            new CfIinc(3, 1),
            new CfGoto(label5),
            label9,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label10),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode StringMethods_joinIterable(InternalOptions options, DexMethod method) {
    CfLabel label0 = new CfLabel();
    CfLabel label1 = new CfLabel();
    CfLabel label2 = new CfLabel();
    CfLabel label3 = new CfLabel();
    CfLabel label4 = new CfLabel();
    CfLabel label5 = new CfLabel();
    CfLabel label6 = new CfLabel();
    CfLabel label7 = new CfLabel();
    CfLabel label8 = new CfLabel();
    CfLabel label9 = new CfLabel();
    return new CfCode(
        method.holder,
        3,
        4,
        ImmutableList.of(
            label0,
            new CfLoad(ValueType.OBJECT, 0),
            new CfIf(If.Type.NE, ValueType.OBJECT, label1),
            new CfNew(options.itemFactory.createType("Ljava/lang/NullPointerException;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfConstString(options.itemFactory.createString("delimiter")),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/NullPointerException;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("V"),
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfThrow(),
            label1,
            new CfNew(options.itemFactory.createType("Ljava/lang/StringBuilder;")),
            new CfStackInstruction(CfStackInstruction.Opcode.Dup),
            new CfInvoke(
                183,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(options.itemFactory.createType("V")),
                    options.itemFactory.createString("<init>")),
                false),
            new CfStore(ValueType.OBJECT, 2),
            label2,
            new CfLoad(ValueType.OBJECT, 1),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/Iterable;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/util/Iterator;")),
                    options.itemFactory.createString("iterator")),
                true),
            new CfStore(ValueType.OBJECT, 3),
            label3,
            new CfLoad(ValueType.OBJECT, 3),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Iterator;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("hasNext")),
                true),
            new CfIf(If.Type.EQ, ValueType.INT, label8),
            label4,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 3),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Iterator;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("next")),
                true),
            new CfCheckCast(options.itemFactory.createType("Ljava/lang/CharSequence;")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label5,
            new CfLoad(ValueType.OBJECT, 3),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Iterator;"),
                    options.itemFactory.createProto(options.itemFactory.createType("Z")),
                    options.itemFactory.createString("hasNext")),
                true),
            new CfIf(If.Type.EQ, ValueType.INT, label8),
            label6,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 0),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            label7,
            new CfLoad(ValueType.OBJECT, 2),
            new CfLoad(ValueType.OBJECT, 3),
            new CfInvoke(
                185,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/util/Iterator;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/Object;")),
                    options.itemFactory.createString("next")),
                true),
            new CfCheckCast(options.itemFactory.createType("Ljava/lang/CharSequence;")),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                        options.itemFactory.createType("Ljava/lang/CharSequence;")),
                    options.itemFactory.createString("append")),
                false),
            new CfStackInstruction(CfStackInstruction.Opcode.Pop),
            new CfGoto(label5),
            label8,
            new CfLoad(ValueType.OBJECT, 2),
            new CfInvoke(
                182,
                options.itemFactory.createMethod(
                    options.itemFactory.createType("Ljava/lang/StringBuilder;"),
                    options.itemFactory.createProto(
                        options.itemFactory.createType("Ljava/lang/String;")),
                    options.itemFactory.createString("toString")),
                false),
            new CfReturn(ValueType.OBJECT),
            label9),
        ImmutableList.of(),
        ImmutableList.of());
  }
}
