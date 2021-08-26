// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.kotlin;

import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Class provides basic information about symbols related to Kotlin support. */
public final class Kotlin {

  // Simply "kotlin", but to avoid being renamed by Shadow.relocate in build.gradle.
  public static final String NAME = String.join("", ImmutableList.of("k", "o", "t", "l", "i", "n"));

  // Simply "Lkotlin/", but to avoid being renamed by Shadow.relocate in build.gradle.
  private static final String KOTLIN =
      String.join("", ImmutableList.of("L", "k", "o", "t", "l", "i", "n", "/"));

  static String addKotlinPrefix(String str) {
    return KOTLIN + str;
  }

  public final DexItemFactory factory;

  public final Functional functional;
  public final Intrinsics intrinsics;
  public final Metadata metadata;

  public Kotlin(DexItemFactory factory) {
    this.factory = factory;

    this.functional = new Functional();
    this.intrinsics = new Intrinsics();
    this.metadata = new Metadata();
  }

  public final class Functional {
    // NOTE: Kotlin stdlib defines interface Function0 till Function22 explicitly, see:
    //    https://github.com/JetBrains/kotlin/blob/master/libraries/
    //                    stdlib/jvm/runtime/kotlin/jvm/functions/Functions.kt
    //
    // For functions with arity bigger that 22 it is supposed to use FunctionN as described
    // in https://github.com/JetBrains/kotlin/blob/master/spec-docs/function-types.md,
    // but in current implementation (v1.2.21) defining such a lambda results in:
    //   > "Error: A JNI error has occurred, please check your installation and try again"
    //
    // This implementation just ignores lambdas with arity > 22.
    private final Object2IntMap<DexType> functions = new Object2IntArrayMap<>(
        IntStream.rangeClosed(0, 22).boxed().collect(
            Collectors.toMap(
                i -> factory.createType(addKotlinPrefix("jvm/functions/Function") + i + ";"),
                Function.identity()))
    );

    private Functional() {
    }

    public final DexString kotlinStyleLambdaInstanceName = factory.createString("INSTANCE");

    public final DexType functionBase =
        factory.createType(addKotlinPrefix("jvm/internal/FunctionBase;"));
    public final DexType lambdaType = factory.createType(addKotlinPrefix("jvm/internal/Lambda;"));

    public final DexMethod lambdaInitializerMethod = factory.createMethod(
        lambdaType,
        factory.createProto(factory.voidType, factory.intType),
        factory.constructorMethodName);

    public boolean isFunctionInterface(DexType type) {
      return functions.containsKey(type);
    }

    public int getArity(DexType type) {
      assert isFunctionInterface(type)
          : "Request to retrieve the arity from non-Kotlin-Function: " + type.toSourceString();
      return functions.getInt(type);
    }
  }

  public final class Metadata {
    public final DexType kotlinMetadataType = factory.createType(addKotlinPrefix("Metadata;"));
    public final DexString kind = factory.createString("k");
    public final DexString metadataVersion = factory.createString("mv");
    public final DexString bytecodeVersion = factory.createString("bv");
    public final DexString data1 = factory.createString("d1");
    public final DexString data2 = factory.createString("d2");
    public final DexString extraString = factory.createString("xs");
    public final DexString packageName = factory.createString("pn");
    public final DexString extraInt = factory.createString("xi");
  }

  // kotlin.jvm.internal.Intrinsics class
  public final class Intrinsics {
    public final DexType type = factory.createType(addKotlinPrefix("jvm/internal/Intrinsics;"));
    public final DexMethod throwParameterIsNullException = factory.createMethod(type,
        factory.createProto(factory.voidType, factory.stringType), "throwParameterIsNullException");
    public final DexMethod checkParameterIsNotNull = factory.createMethod(type,
        factory.createProto(factory.voidType, factory.objectType, factory.stringType),
        "checkParameterIsNotNull");
    public final DexMethod throwNpe = factory.createMethod(
        type, factory.createProto(factory.voidType), "throwNpe");
  }

  // Calculates kotlin info for a class.
  public KotlinInfo getKotlinInfo(DexClass clazz, DiagnosticsHandler reporter) {
    return KotlinClassMetadataReader.getKotlinInfo(this, clazz, reporter);
  }
}
