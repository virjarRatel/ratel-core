// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.cf.code.CfArrayStore;
import com.android.tools.r8.cf.code.CfConstNumber;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfNewArray;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.objectweb.asm.Opcodes;

public final class CollectionMethodGenerators {

  private CollectionMethodGenerators() {}

  public static CfCode generateListOf(InternalOptions options, DexMethod method, int formalCount) {
    return generateFixedMethods(options, method, formalCount, options.itemFactory.listType);
  }

  public static CfCode generateSetOf(InternalOptions options, DexMethod method, int formalCount) {
    return generateFixedMethods(options, method, formalCount, options.itemFactory.setType);
  }

  private static CfCode generateFixedMethods(
      InternalOptions options, DexMethod method, int formalCount, DexType returnType) {
    Builder<CfInstruction> builder = ImmutableList.builder();
    builder.add(
        new CfConstNumber(formalCount, ValueType.INT),
        new CfNewArray(options.itemFactory.objectArrayType));

    for (int i = 0; i < formalCount; i++) {
      builder.add(
          new CfStackInstruction(CfStackInstruction.Opcode.Dup),
          new CfConstNumber(i, ValueType.INT),
          new CfLoad(ValueType.OBJECT, i),
          new CfArrayStore(MemberType.OBJECT));
    }

    builder.add(
        new CfInvoke(
            Opcodes.INVOKESTATIC,
            options.itemFactory.createMethod(
                returnType,
                options.itemFactory.createProto(returnType, options.itemFactory.objectArrayType),
                options.itemFactory.createString("of")),
            false),
        new CfReturn(ValueType.OBJECT));

    return new CfCode(
        method.holder,
        4,
        formalCount,
        builder.build(),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static CfCode generateMapOf(
      InternalOptions options, DexMethod method, int formalCount) {
    DexType mapEntryArray =
        options.itemFactory.createArrayType(1, options.itemFactory.mapEntryType);
    DexType simpleEntry = options.itemFactory.createType("Ljava/util/AbstractMap$SimpleEntry;");
    DexMethod simpleEntryConstructor = options.itemFactory.createMethod(
        simpleEntry,
        options.itemFactory.createProto(
            options.itemFactory.voidType,
            options.itemFactory.objectType,
            options.itemFactory.objectType),
        Constants.INSTANCE_INITIALIZER_NAME);

    Builder<CfInstruction> builder = ImmutableList.builder();
    builder.add(
        new CfConstNumber(formalCount, ValueType.INT),
        new CfNewArray(mapEntryArray));

    for (int i = 0; i < formalCount; i++) {
      builder.add(
          new CfStackInstruction(CfStackInstruction.Opcode.Dup),
          new CfConstNumber(i, ValueType.INT),
          new CfNew(simpleEntry),
          new CfStackInstruction(CfStackInstruction.Opcode.Dup),
          new CfLoad(ValueType.OBJECT, i * 2),
          new CfLoad(ValueType.OBJECT, i * 2 + 1),
          new CfInvoke(Opcodes.INVOKESPECIAL, simpleEntryConstructor, false),
          new CfArrayStore(MemberType.OBJECT));
    }

    builder.add(
        new CfInvoke(
            Opcodes.INVOKESTATIC,
            options.itemFactory.createMethod(
                options.itemFactory.mapType,
                options.itemFactory.createProto(
                    options.itemFactory.mapType,
                    mapEntryArray),
                options.itemFactory.createString("ofEntries")),
            false),
        new CfReturn(ValueType.OBJECT));

    return new CfCode(
        method.holder,
        7,
        formalCount * 2,
        builder.build(),
        ImmutableList.of(),
        ImmutableList.of());
  }
}
