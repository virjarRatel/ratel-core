// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import com.android.tools.r8.cf.code.CfArrayStore;
import com.android.tools.r8.cf.code.CfConstNumber;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfNewArray;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.ValueType;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class ServiceLoaderSourceCode {

  // This is building the following implementation for service-loader rewriting:
  // public static <S> Iterator<S> loadS() {
  //   try {
  //     return Arrays.asList(X, Y, Z).iterator();
  //   } catch (Throwable t) {
  //     throw new ServiceConfigurationError(t.getMessage(), t);
  //   }
  // }
  public static CfCode generate(
      DexType serviceType, List<DexClass> classes, DexItemFactory factory) {
    ImmutableList.Builder<CfInstruction> builder = ImmutableList.builder();

    CfLabel tryCatchStart = new CfLabel();
    CfLabel tryCatchEnd = new CfLabel();

    builder.add(
        tryCatchStart,
        new CfConstNumber(classes.size(), ValueType.INT),
        new CfNewArray(factory.createArrayType(1, serviceType)));

    for (int i = 0; i < classes.size(); i++) {
      builder.add(
          new CfStackInstruction(CfStackInstruction.Opcode.Dup),
          new CfConstNumber(i, ValueType.INT),
          new CfNew(classes.get(i).type),
          new CfStackInstruction(CfStackInstruction.Opcode.Dup),
          new CfInvoke(
              INVOKESPECIAL,
              factory.createMethod(
                  classes.get(i).type,
                  factory.createProto(factory.voidType),
                  factory.constructorMethodName),
              false),
          new CfArrayStore(MemberType.OBJECT));
    }

    builder.add(
        new CfInvoke(INVOKESTATIC, factory.utilArraysMethods.asList, false),
        new CfInvoke(
            INVOKEINTERFACE,
            factory.createMethod(
                factory.listType,
                factory.createProto(factory.iteratorType),
                factory.createString("iterator")),
            true),
        tryCatchEnd,
        new CfReturn(ValueType.OBJECT));

    // Build the exception handler.
    CfLabel tryCatchHandler = new CfLabel();
    builder.add(
        tryCatchHandler,
        new CfStore(ValueType.OBJECT, 0),
        new CfNew(factory.serviceLoaderConfigurationErrorType),
        new CfStackInstruction(CfStackInstruction.Opcode.Dup),
        new CfLoad(ValueType.OBJECT, 0),
        new CfInvoke(INVOKEVIRTUAL, factory.throwableMethods.getMessage, false),
        new CfLoad(ValueType.OBJECT, 0),
        new CfInvoke(
            INVOKESPECIAL,
            factory.createMethod(
                factory.serviceLoaderConfigurationErrorType,
                factory.createProto(factory.voidType, factory.stringType, factory.throwableType),
                factory.constructorMethodName),
            false),
        new CfThrow());

    CfTryCatch cfTryCatch =
        new CfTryCatch(
            tryCatchStart,
            tryCatchEnd,
            ImmutableList.of(factory.throwableType),
            ImmutableList.of(tryCatchHandler));

    return new CfCode(
        null, 5, 1, builder.build(), ImmutableList.of(cfTryCatch), ImmutableList.of());
  }
}
