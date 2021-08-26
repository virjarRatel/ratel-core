// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroup.LambdaStructureError;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroupId;
import com.android.tools.r8.kotlin.Kotlin;
import com.android.tools.r8.utils.InternalOptions;

final class KStyleLambdaGroupIdFactory extends KotlinLambdaGroupIdFactory {
  static final KotlinLambdaGroupIdFactory INSTANCE = new KStyleLambdaGroupIdFactory();

  @Override
  LambdaGroupId validateAndCreate(Kotlin kotlin, DexClass lambda, InternalOptions options)
      throws LambdaStructureError {
    boolean accessRelaxed = options.getProguardConfiguration().isAccessModificationAllowed();

    assert lambda.hasKotlinInfo() && lambda.getKotlinInfo().isSyntheticClass();
    assert lambda.getKotlinInfo().asSyntheticClass().isKotlinStyleLambda();

    checkAccessFlags("class access flags", lambda.accessFlags,
        PUBLIC_LAMBDA_CLASS_FLAGS, LAMBDA_CLASS_FLAGS);

    // Class and interface.
    validateSuperclass(kotlin, lambda);
    DexType iface = validateInterfaces(kotlin, lambda);

    validateStaticFields(kotlin, lambda);
    String captureSignature = validateInstanceFields(lambda, accessRelaxed);
    validateDirectMethods(lambda);
    DexEncodedMethod mainMethod = validateVirtualMethods(lambda);
    String genericSignature = validateAnnotations(kotlin, lambda);
    InnerClassAttribute innerClass = validateInnerClasses(lambda);

    return new KStyleLambdaGroup.GroupId(captureSignature, iface,
        accessRelaxed ? "" : lambda.type.getPackageDescriptor(),
        genericSignature, mainMethod, innerClass, lambda.getEnclosingMethod());
  }

  @Override
  void validateSuperclass(Kotlin kotlin, DexClass lambda) throws LambdaStructureError {
    if (lambda.superType != kotlin.functional.lambdaType) {
      throw new LambdaStructureError("implements " + lambda.superType.toSourceString() +
          " instead of kotlin.jvm.internal.Lambda");
    }
  }

  @Override
  DexType validateInterfaces(Kotlin kotlin, DexClass lambda) throws LambdaStructureError {
    if (lambda.interfaces.size() == 0) {
      throw new LambdaStructureError("does not implement any interfaces");
    }
    if (lambda.interfaces.size() > 1) {
      throw new LambdaStructureError(
          "implements more than one interface: " + lambda.interfaces.size());
    }
    DexType iface = lambda.interfaces.values[0];
    if (!kotlin.functional.isFunctionInterface(iface)) {
      throw new LambdaStructureError("implements " + iface.toSourceString() +
          " instead of kotlin functional interface.");
    }
    return iface;
  }
}
