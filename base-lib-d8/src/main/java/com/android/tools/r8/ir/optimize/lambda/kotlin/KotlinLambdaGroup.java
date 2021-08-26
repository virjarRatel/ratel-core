// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.optimize.lambda.CaptureSignature;
import com.android.tools.r8.ir.optimize.lambda.CodeProcessor.Strategy;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroup;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroupId;

// Represents a lambda group created to combine several lambda classes generated
// by kotlin compiler for either regular kotlin lambda expressions (k-style lambdas)
// or lambda expressions created to implement java SAM interface.
abstract class KotlinLambdaGroup extends LambdaGroup {
  private final Strategy strategy = new KotlinLambdaGroupCodeStrategy(this);

  KotlinLambdaGroup(LambdaGroupId id) {
    super(id);
  }

  final KotlinLambdaGroupId id() {
    return (KotlinLambdaGroupId) id;
  }

  final boolean isStateless() {
    return id().capture.isEmpty();
  }

  final boolean hasAnySingletons() {
    assert isStateless();
    return anyLambda(info -> this.isSingletonLambda(info.clazz.type));
  }

  final boolean isSingletonLambda(DexType lambda) {
    assert isStateless();
    return lambdaSingletonField(lambda) != null;
  }

  // Field referencing singleton instance for a lambda with specified id.
  final DexField getSingletonInstanceField(DexItemFactory factory, int id) {
    return factory.createField(this.getGroupClassType(),
        this.getGroupClassType(), factory.createString("INSTANCE$" + id));
  }

  @Override
  protected String getTypePackage() {
    String pkg = id().pkg;
    return pkg.isEmpty() ? "" : (pkg + "/");
  }

  final DexProto createConstructorProto(DexItemFactory factory) {
    String capture = id().capture;
    DexType[] newParameters = new DexType[capture.length() + 1];
    newParameters[0] = factory.intType; // Lambda id.
    for (int i = 0; i < capture.length(); i++) {
      newParameters[i + 1] = CaptureSignature.fieldType(factory, capture, i);
    }
    return factory.createProto(factory.voidType, newParameters);
  }

  final DexField getLambdaIdField(DexItemFactory factory) {
    return factory.createField(this.getGroupClassType(), factory.intType, "$id$");
  }

  final int mapFieldIntoCaptureIndex(DexType lambda, DexField field) {
    return CaptureSignature.mapFieldIntoCaptureIndex(
        id().capture, lambdaCaptureFields(lambda), field);
  }

  final DexField getCaptureField(DexItemFactory factory, int index) {
    assert index >= 0 && index < id().capture.length();
    return factory.createField(this.getGroupClassType(),
        CaptureSignature.fieldType(factory, id().capture, index), "$capture$" + index);
  }

  @Override
  public Strategy getCodeStrategy() {
    return strategy;
  }
}
