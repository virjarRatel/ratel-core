// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.AccessFlags;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.ir.optimize.lambda.CaptureSignature;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroup.LambdaStructureError;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroupId;
import com.android.tools.r8.kotlin.Kotlin;
import com.android.tools.r8.utils.InternalOptions;
import java.util.List;

public abstract class KotlinLambdaGroupIdFactory implements KotlinLambdaConstants {
  KotlinLambdaGroupIdFactory() {
  }

  // Creates a lambda group id for kotlin style lambda. Should never return null, if the lambda
  // does not pass pre-requirements (mostly by not meeting high-level structure expectations)
  // should throw LambdaStructureError leaving the caller to decide if/how it needs to be reported.
  //
  // At this point we only perform high-level checks before qualifying the lambda as a candidate
  // for merging and assigning lambda group id. We can NOT perform checks on method bodies since
  // they may not be converted yet, we'll do that in KStyleLambdaClassValidator.
  public static LambdaGroupId create(Kotlin kotlin, DexClass lambda, InternalOptions options)
      throws LambdaStructureError {

    assert lambda.hasKotlinInfo() && lambda.getKotlinInfo().isSyntheticClass();
    if (lambda.getKotlinInfo().asSyntheticClass().isKotlinStyleLambda()) {
      return KStyleLambdaGroupIdFactory.INSTANCE.validateAndCreate(kotlin, lambda, options);
    }

    assert lambda.getKotlinInfo().asSyntheticClass().isJavaStyleLambda();
    return JStyleLambdaGroupIdFactory.INSTANCE.validateAndCreate(kotlin, lambda, options);
  }

  abstract LambdaGroupId validateAndCreate(Kotlin kotlin, DexClass lambda, InternalOptions options)
      throws LambdaStructureError;

  abstract void validateSuperclass(Kotlin kotlin, DexClass lambda) throws LambdaStructureError;

  abstract DexType validateInterfaces(Kotlin kotlin, DexClass lambda) throws LambdaStructureError;

  DexEncodedMethod validateVirtualMethods(DexClass lambda) throws LambdaStructureError {
    DexEncodedMethod mainMethod = null;

    for (DexEncodedMethod method : lambda.virtualMethods()) {
      if (method.accessFlags.materialize() == MAIN_METHOD_FLAGS.materialize()) {
        if (mainMethod != null) {
          throw new LambdaStructureError("more than one main method found");
        }
        mainMethod = method;
      } else {
        checkAccessFlags("unexpected virtual method access flags",
            method.accessFlags, BRIDGE_METHOD_FLAGS, BRIDGE_METHOD_FLAGS_FIXED);
        checkDirectMethodAnnotations(method);
      }
    }

    if (mainMethod == null) {
      // Missing main method may be a result of tree shaking.
      throw new LambdaStructureError("no main method found", false);
    }
    return mainMethod;
  }

  InnerClassAttribute validateInnerClasses(DexClass lambda) throws LambdaStructureError {
    List<InnerClassAttribute> innerClasses = lambda.getInnerClasses();
    if (innerClasses != null) {
      for (InnerClassAttribute inner : innerClasses) {
        if (inner.getInner() == lambda.type) {
          if (!inner.isAnonymous()) {
            throw new LambdaStructureError("is not anonymous");
          }
          return inner;
        }
      }
    }
    return null;
  }

  public static boolean hasValidAnnotations(Kotlin kotlin, DexClass lambda) {
    if (!lambda.annotations.isEmpty()) {
      for (DexAnnotation annotation : lambda.annotations.annotations) {
        if (DexAnnotation.isSignatureAnnotation(annotation, kotlin.factory)) {
          continue;
        }
        if (annotation.annotation.type == kotlin.metadata.kotlinMetadataType) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  String validateAnnotations(Kotlin kotlin, DexClass lambda) throws LambdaStructureError {
    String signature = null;
    if (!lambda.annotations.isEmpty()) {
      for (DexAnnotation annotation : lambda.annotations.annotations) {
        if (DexAnnotation.isSignatureAnnotation(annotation, kotlin.factory)) {
          signature = DexAnnotation.getSignature(annotation);
          continue;
        }

        if (annotation.annotation.type == kotlin.metadata.kotlinMetadataType) {
          // Ignore kotlin metadata on lambda classes. Metadata on synthetic
          // classes exists but is not used in the current Kotlin version (1.2.21)
          // and newly generated lambda _group_ class is not exactly a kotlin class.
          continue;
        }

        assert !hasValidAnnotations(kotlin, lambda);
        throw new LambdaStructureError(
            "unexpected annotation: " + annotation.annotation.type.toSourceString());
      }
    }
    assert hasValidAnnotations(kotlin, lambda);
    return signature;
  }

  void validateStaticFields(Kotlin kotlin, DexClass lambda) throws LambdaStructureError {
    List<DexEncodedField> staticFields = lambda.staticFields();
    if (staticFields.size() == 1) {
      DexEncodedField field = staticFields.get(0);
      if (field.field.name != kotlin.functional.kotlinStyleLambdaInstanceName ||
          field.field.type != lambda.type || !field.accessFlags.isPublic() ||
          !field.accessFlags.isFinal() || !field.accessFlags.isStatic()) {
        throw new LambdaStructureError("unexpected static field " + field.toSourceString());
      }
      // No state if the lambda is a singleton.
      if (lambda.instanceFields().size() > 0) {
        throw new LambdaStructureError("has instance fields along with INSTANCE");
      }
      checkAccessFlags("static field access flags", field.accessFlags, SINGLETON_FIELD_FLAGS);
      checkFieldAnnotations(field);

    } else if (staticFields.size() > 1) {
      throw new LambdaStructureError(
          "only one static field max expected, found " + staticFields.size());
    }
  }

  String validateInstanceFields(DexClass lambda, boolean accessRelaxed)
      throws LambdaStructureError {
    List<DexEncodedField> instanceFields = lambda.instanceFields();
    for (DexEncodedField field : instanceFields) {
      checkAccessFlags("capture field access flags", field.accessFlags,
          accessRelaxed ? CAPTURE_FIELD_FLAGS_RELAXED : CAPTURE_FIELD_FLAGS);
      checkFieldAnnotations(field);
    }
    return CaptureSignature.getCaptureSignature(instanceFields);
  }

  void validateDirectMethods(DexClass lambda) throws LambdaStructureError {
    for (DexEncodedMethod method : lambda.directMethods()) {
      if (method.isClassInitializer()) {
        // We expect to see class initializer only if there is a singleton field.
        if (lambda.staticFields().size() != 1) {
          throw new LambdaStructureError("has static initializer, but no singleton field");
        }
        checkAccessFlags(
            "unexpected static initializer access flags",
            method.accessFlags.getOriginalAccessFlags(),
            CLASS_INITIALIZER_FLAGS);
        checkDirectMethodAnnotations(method);
      } else if (method.isStatic()) {
        throw new LambdaStructureError(
            "unexpected static method: " + method.method.toSourceString());
      } else if (method.isInstanceInitializer()) {
        // Lambda class is expected to have one constructor
        // with parameters matching capture signature.
        DexType[] parameters = method.method.proto.parameters.values;
        List<DexEncodedField> instanceFields = lambda.instanceFields();
        if (parameters.length != instanceFields.size()) {
          throw new LambdaStructureError("constructor parameters don't match captured values.");
        }
        for (int i = 0; i < parameters.length; i++) {
          // Kotlin compiler sometimes reshuffles the parameters so that their order
          // in the constructor don't match order of capture fields. We could add
          // support for it, but it happens quite rarely so don't bother for now.
          if (parameters[i] != instanceFields.get(i).field.type) {
            throw new LambdaStructureError(
                "constructor parameters don't match captured values.", false);
          }
        }
        checkAccessFlags("unexpected constructor access flags",
            method.accessFlags, CONSTRUCTOR_FLAGS, CONSTRUCTOR_FLAGS_RELAXED);
        checkDirectMethodAnnotations(method);

      } else if (method.isPrivateMethod()) {
        // TODO(b/135975229)
        throw new LambdaStructureError("private method: " + method.method.toSourceString());
      } else {
        assert false;
        throw new LambdaStructureError(
            "unexpected method encountered: " + method.method.toSourceString());
      }
    }
  }

  void checkDirectMethodAnnotations(DexEncodedMethod method) throws LambdaStructureError {
    if (!method.annotations.isEmpty()) {
      throw new LambdaStructureError("unexpected method annotations [" +
          method.annotations.toSmaliString() + "] on " + method.method.toSourceString());
    }
    if (!method.parameterAnnotationsList.isEmpty()) {
      throw new LambdaStructureError("unexpected method parameters annotations [" +
          method.annotations.toSmaliString() + "] on " + method.method.toSourceString());
    }
  }

  private static void checkFieldAnnotations(DexEncodedField field) throws LambdaStructureError {
    if (!field.annotations.isEmpty()) {
      throw new LambdaStructureError("unexpected field annotations [" +
          field.annotations.toSmaliString() + "] on " + field.field.toSourceString());
    }
  }

  @SafeVarargs
  static <T extends AccessFlags> void checkAccessFlags(
      String message, T actual, T... expected) throws LambdaStructureError {
    checkAccessFlags(message, actual.materialize(), expected);
  }

  @SafeVarargs
  static <T extends AccessFlags> void checkAccessFlags(String message, int actual, T... expected)
      throws LambdaStructureError {
    for (T flag : expected) {
      if (actual == flag.materialize()) {
        return;
      }
    }
    throw new LambdaStructureError(message);
  }
}
