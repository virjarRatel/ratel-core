// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.EnclosingMethodAttribute;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroup;
import com.android.tools.r8.ir.optimize.lambda.LambdaGroupId;

abstract class KotlinLambdaGroupId implements LambdaGroupId {
  private static final int MISSING_INNER_CLASS_ATTRIBUTE = -1;

  private final int hash;

  // Capture signature.
  final String capture;
  // Kotlin functional interface.
  final DexType iface;
  // Package (in case access relaxation is enabled root package is used).
  final String pkg;
  // Generic signature of the lambda class.
  final String signature;

  // Characteristics of the main lambda method. Kotlin generates one main method with
  // the signature matching the lambda signature, and a bridge method (if needed)
  // forwarding the call via interface to the main method. Main method may have
  // generic signature and parameter annotations defining nullability.
  //
  // TODO: address cases when main method is removed after inlining.
  // (the main method if created is supposed to be inlined, since it is always
  //  only called from the bridge, and removed. In this case the method with
  //  signature and annotations is removed allowing more lambda to be merged.)
  final DexString mainMethodName;
  final DexProto mainMethodProto;
  final DexAnnotationSet mainMethodAnnotations;
  final ParameterAnnotationsList mainMethodParamAnnotations;

  final EnclosingMethodAttribute enclosing;

  // Note that lambda classes are always created as _anonymous_ inner classes. We only
  // need to store the fact that the class had this attribute and if it had store the
  // access from InnerClassAttribute.
  final int innerClassAccess;

  KotlinLambdaGroupId(String capture, DexType iface, String pkg, String signature,
      DexEncodedMethod mainMethod, InnerClassAttribute inner, EnclosingMethodAttribute enclosing) {
    assert capture != null && iface != null && pkg != null && mainMethod != null;
    assert inner == null || (inner.isAnonymous() && inner.getOuter() == null);
    this.capture = capture;
    this.iface = iface;
    this.pkg = pkg;
    this.signature = signature;
    this.mainMethodName = mainMethod.method.name;
    this.mainMethodProto = mainMethod.method.proto;
    this.mainMethodAnnotations = mainMethod.annotations;
    this.mainMethodParamAnnotations = mainMethod.parameterAnnotationsList;
    this.innerClassAccess = inner != null ? inner.getAccess() : MISSING_INNER_CLASS_ATTRIBUTE;
    this.enclosing = enclosing;
    this.hash = computeHashCode();
  }

  final boolean hasInnerClassAttribute() {
    return innerClassAccess != MISSING_INNER_CLASS_ATTRIBUTE;
  }

  @Override
  public final int hashCode() {
    return hash;
  }

  private int computeHashCode() {
    int hash = capture.hashCode() * 7;
    hash += iface.hashCode() * 17;
    hash += pkg.hashCode() * 37;
    hash += signature != null ? signature.hashCode() * 47 : 0;
    hash += mainMethodName.hashCode() * 71;
    hash += mainMethodProto.hashCode() * 89;
    hash += mainMethodAnnotations != null ? mainMethodAnnotations.hashCode() * 101 : 0;
    hash += mainMethodParamAnnotations != null ? mainMethodParamAnnotations.hashCode() * 113 : 0;
    hash += innerClassAccess * 131;
    hash += enclosing != null ? enclosing.hashCode() * 211 : 0;
    return hash;
  }

  @Override
  public abstract boolean equals(Object obj);

  boolean computeEquals(KotlinLambdaGroupId other) {
    return capture.equals(other.capture) &&
        iface == other.iface &&
        pkg.equals(other.pkg) &&
        mainMethodName == other.mainMethodName &&
        mainMethodProto == other.mainMethodProto &&
        (mainMethodAnnotations == null ? other.mainMethodAnnotations == null
            : mainMethodAnnotations.equals(other.mainMethodAnnotations)) &&
        (mainMethodParamAnnotations == null ? other.mainMethodParamAnnotations == null
            : mainMethodParamAnnotations.equals(other.mainMethodParamAnnotations)) &&
        (signature == null ? other.signature == null : signature.equals(other.signature)) &&
        innerClassAccess == other.innerClassAccess &&
        (enclosing == null ? other.enclosing == null : enclosing.equals(other.enclosing));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getLambdaKindDescriptor())
        .append("\n  capture: ").append(capture)
        .append("\n  interface: ").append(iface.descriptor)
        .append("\n  package: ").append(pkg)
        .append("\n  signature: ").append(signature)
        .append("\n  main method name: ").append(mainMethodName.toString())
        .append("\n  main method: ").append(mainMethodProto.toSourceString())
        .append("\n  main annotations: ").append(mainMethodAnnotations)
        .append("\n  main param annotations: ").append(mainMethodParamAnnotations)
        .append("\n  inner: ")
        .append(innerClassAccess == MISSING_INNER_CLASS_ATTRIBUTE ? "none" : innerClassAccess);
    if (enclosing != null) {
      if (enclosing.getEnclosingClass() != null) {
        builder.append("\n  enclosingClass: ")
            .append(enclosing.getEnclosingClass().descriptor);
      } else {
        builder.append("\n  enclosingMethod: ")
            .append(enclosing.getEnclosingMethod().toSourceString());
      }
    }
    return builder.toString();
  }

  abstract String getLambdaKindDescriptor();

  @Override
  public abstract LambdaGroup createGroup();
}
