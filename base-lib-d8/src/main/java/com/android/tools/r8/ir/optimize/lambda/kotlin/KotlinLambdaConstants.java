// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.FieldAccessFlags;
import com.android.tools.r8.graph.MethodAccessFlags;

interface KotlinLambdaConstants {
  // Default lambda class flags.
  ClassAccessFlags LAMBDA_CLASS_FLAGS =
      ClassAccessFlags.fromDexAccessFlags(Constants.ACC_FINAL);
  // Access-relaxed lambda class flags.
  ClassAccessFlags PUBLIC_LAMBDA_CLASS_FLAGS =
      ClassAccessFlags.fromDexAccessFlags(Constants.ACC_PUBLIC + Constants.ACC_FINAL);

  // Default lambda class initializer flags.
  MethodAccessFlags CLASS_INITIALIZER_FLAGS =
      MethodAccessFlags.fromSharedAccessFlags(Constants.ACC_STATIC, true);
  // Default lambda class constructor flags.
  MethodAccessFlags CONSTRUCTOR_FLAGS =
      MethodAccessFlags.fromSharedAccessFlags(0, true);
  // Access-relaxed lambda class constructor flags.
  MethodAccessFlags CONSTRUCTOR_FLAGS_RELAXED =
      MethodAccessFlags.fromSharedAccessFlags(Constants.ACC_PUBLIC, true);

  // Default main lambda method flags.
  MethodAccessFlags MAIN_METHOD_FLAGS =
      MethodAccessFlags.fromSharedAccessFlags(
          Constants.ACC_PUBLIC + Constants.ACC_FINAL, false);
  // Default bridge lambda method flags.
  MethodAccessFlags BRIDGE_METHOD_FLAGS =
      MethodAccessFlags.fromSharedAccessFlags(
          Constants.ACC_PUBLIC + Constants.ACC_SYNTHETIC + Constants.ACC_BRIDGE, false);
  // Bridge lambda method flags after inliner.
  MethodAccessFlags BRIDGE_METHOD_FLAGS_FIXED =
      MethodAccessFlags.fromSharedAccessFlags(Constants.ACC_PUBLIC, false);

  // Default singleton instance folding field flags.
  FieldAccessFlags SINGLETON_FIELD_FLAGS =
      FieldAccessFlags.fromSharedAccessFlags(
          Constants.ACC_PUBLIC + Constants.ACC_STATIC + Constants.ACC_FINAL);
  // Default instance (lambda capture) field flags.
  FieldAccessFlags CAPTURE_FIELD_FLAGS =
      FieldAccessFlags.fromSharedAccessFlags(
          Constants.ACC_FINAL + Constants.ACC_SYNTHETIC);
  // access-relaxed instance (lambda capture) field flags.
  FieldAccessFlags CAPTURE_FIELD_FLAGS_RELAXED =
      FieldAccessFlags.fromSharedAccessFlags(
          Constants.ACC_PUBLIC + Constants.ACC_FINAL + Constants.ACC_SYNTHETIC);
}
