// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.synthetic.ForwardMethodSourceCode;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import com.google.common.base.Predicates;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// Responsible for processing the annotations dalvik.annotation.codegen.CovariantReturnType and
// dalvik.annotation.codegen.CovariantReturnType$CovariantReturnTypes.
//
// Consider the following class:
//   public class B extends A {
//     @CovariantReturnType(returnType = B.class, presentAfter = 25)
//     @Override
//     public A m(...) { ... return new B(); }
//   }
//
// The annotation is used to indicate that the compiler should insert a synthetic method that is
// equivalent to method m, but has return type B instead of A. Thus, for this example, this
// component is responsible for inserting the following method in class B (in addition to the
// existing method m):
//   public B m(...) { A result = "invoke B.m(...)A;"; return (B) result; }
//
// Note that a method may be annotated with more than one CovariantReturnType annotation. In this
// case there will be a CovariantReturnType$CovariantReturnTypes annotation on the method that wraps
// several CovariantReturnType annotations. In this case, a new method is synthesized for each of
// the contained CovariantReturnType annotations.
public final class CovariantReturnTypeAnnotationTransformer {
  private final IRConverter converter;
  private final DexItemFactory factory;

  public CovariantReturnTypeAnnotationTransformer(IRConverter converter, DexItemFactory factory) {
    this.converter = converter;
    this.factory = factory;
  }

  public void process(DexApplication.Builder<?> builder) {
    // List of methods that should be added to the next class.
    List<DexEncodedMethod> methodsWithCovariantReturnTypeAnnotation = new LinkedList<>();
    List<DexEncodedMethod> covariantReturnTypeMethods = new LinkedList<>();
    for (DexClass clazz : builder.getProgramClasses()) {
      // Construct the methods that should be added to clazz.
      buildCovariantReturnTypeMethodsForClass(
          clazz, methodsWithCovariantReturnTypeAnnotation, covariantReturnTypeMethods);
      if (covariantReturnTypeMethods.isEmpty()) {
        continue;
      }
      updateClass(clazz, methodsWithCovariantReturnTypeAnnotation, covariantReturnTypeMethods);
      // Reset lists for the next class that will have a CovariantReturnType or
      // CovariantReturnType$CovariantReturnTypes annotation.
      methodsWithCovariantReturnTypeAnnotation.clear();
      covariantReturnTypeMethods.clear();
    }
  }

  private void updateClass(
      DexClass clazz,
      List<DexEncodedMethod> methodsWithCovariantReturnTypeAnnotation,
      List<DexEncodedMethod> covariantReturnTypeMethods) {
    // It is a compilation error if the class already has a method with a signature similar to one
    // of the methods in covariantReturnTypeMethods.
    for (DexEncodedMethod syntheticMethod : covariantReturnTypeMethods) {
      if (hasVirtualMethodWithSignature(clazz, syntheticMethod)) {
        throw new CompilationError(
            String.format(
                "Cannot process CovariantReturnType annotation: Class %s already "
                    + "has a method \"%s\"",
                clazz.getType(), syntheticMethod.toSourceString()));
      }
    }
    // Remove the CovariantReturnType annotations.
    for (DexEncodedMethod method : methodsWithCovariantReturnTypeAnnotation) {
      method.annotations =
          method.annotations.keepIf(x -> !isCovariantReturnTypeAnnotation(x.annotation));
    }
    // Add the newly constructed methods to the class.
    clazz.appendVirtualMethods(covariantReturnTypeMethods);
  }

  // Processes all the dalvik.annotation.codegen.CovariantReturnType and dalvik.annotation.codegen.
  // CovariantReturnTypes annotations in the given DexClass. Adds the newly constructed, synthetic
  // methods to the list covariantReturnTypeMethods.
  private void buildCovariantReturnTypeMethodsForClass(
      DexClass clazz,
      List<DexEncodedMethod> methodsWithCovariantReturnTypeAnnotation,
      List<DexEncodedMethod> covariantReturnTypeMethods) {
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      if (methodHasCovariantReturnTypeAnnotation(method)) {
        methodsWithCovariantReturnTypeAnnotation.add(method);
        buildCovariantReturnTypeMethodsForMethod(clazz, method, covariantReturnTypeMethods);
      }
    }
  }

  private boolean methodHasCovariantReturnTypeAnnotation(DexEncodedMethod method) {
    for (DexAnnotation annotation : method.annotations.annotations) {
      if (isCovariantReturnTypeAnnotation(annotation.annotation)) {
        return true;
      }
    }
    return false;
  }

  // Processes all the dalvik.annotation.codegen.CovariantReturnType and dalvik.annotation.Co-
  // variantReturnTypes annotations on the given method. Adds the newly constructed, synthetic
  // methods to the list covariantReturnTypeMethods.
  private void buildCovariantReturnTypeMethodsForMethod(
      DexClass clazz, DexEncodedMethod method, List<DexEncodedMethod> covariantReturnTypeMethods) {
    assert methodHasCovariantReturnTypeAnnotation(method);
    for (DexType covariantReturnType : getCovariantReturnTypes(clazz, method)) {
      DexEncodedMethod covariantReturnTypeMethod =
          buildCovariantReturnTypeMethod(clazz, method, covariantReturnType);
      covariantReturnTypeMethods.add(covariantReturnTypeMethod);
    }
  }

  // Builds a synthetic method that invokes the given method, casts the result to
  // covariantReturnType, and then returns the result. The newly created method will have return
  // type covariantReturnType.
  //
  // Note: any "synchronized" or "strictfp" modifier could be dropped safely.
  private DexEncodedMethod buildCovariantReturnTypeMethod(
      DexClass clazz, DexEncodedMethod method, DexType covariantReturnType) {
    DexProto newProto =
        factory.createProto(
            covariantReturnType, method.method.proto.parameters, method.method.proto.shorty);
    MethodAccessFlags newAccessFlags = method.accessFlags.copy();
    newAccessFlags.setBridge();
    newAccessFlags.setSynthetic();
    DexMethod newMethod = factory.createMethod(method.method.holder, newProto, method.method.name);
    ForwardMethodSourceCode.Builder forwardSourceCodeBuilder =
        ForwardMethodSourceCode.builder(newMethod);
    forwardSourceCodeBuilder
        .setReceiver(clazz.type)
        .setTargetReceiver(method.method.holder)
        .setTarget(method.method)
        .setInvokeType(Invoke.Type.VIRTUAL)
        .setCastResult();
    DexEncodedMethod newVirtualMethod =
        new DexEncodedMethod(
            newMethod,
            newAccessFlags,
            method.annotations.keepIf(x -> !isCovariantReturnTypeAnnotation(x.annotation)),
            method.parameterAnnotationsList.keepIf(Predicates.alwaysTrue()),
            new SynthesizedCode(forwardSourceCodeBuilder::build));
    // Optimize to generate DexCode instead of SynthesizedCode.
    converter.optimizeSynthesizedMethod(newVirtualMethod);
    return newVirtualMethod;
  }

  // Returns the set of covariant return types for method.
  //
  // If the method is:
  //   @dalvik.annotation.codegen.CovariantReturnType(returnType=SubOfFoo, presentAfter=25)
  //   @dalvik.annotation.codegen.CovariantReturnType(returnType=SubOfSubOfFoo, presentAfter=28)
  //   @Override
  //   public Foo foo() { ... return new SubOfSubOfFoo(); }
  // then this method returns the set { SubOfFoo, SubOfSubOfFoo }.
  private Set<DexType> getCovariantReturnTypes(DexClass clazz, DexEncodedMethod method) {
    Set<DexType> covariantReturnTypes = new HashSet<>();
    for (DexAnnotation annotation : method.annotations.annotations) {
      if (isCovariantReturnTypeAnnotation(annotation.annotation)) {
        getCovariantReturnTypesFromAnnotation(
            clazz, method, annotation.annotation, covariantReturnTypes);
      }
    }
    return covariantReturnTypes;
  }

  private void getCovariantReturnTypesFromAnnotation(
      DexClass clazz,
      DexEncodedMethod method,
      DexEncodedAnnotation annotation,
      Set<DexType> covariantReturnTypes) {
    assert isCovariantReturnTypeAnnotation(annotation);
    boolean hasPresentAfterElement = false;
    for (DexAnnotationElement element : annotation.elements) {
      String name = element.name.toString();
      if (annotation.type == factory.annotationCovariantReturnType) {
        if (name.equals("returnType")) {
          if (!(element.value instanceof DexValue.DexValueType)) {
            throw new CompilationError(
                String.format(
                    "Expected element \"returnType\" of CovariantReturnType annotation to "
                        + "reference a type (method: \"%s\", was: %s)",
                    method.toSourceString(), element.value.getClass().getCanonicalName()));
          }

          DexValue.DexValueType dexValueType = (DexValue.DexValueType) element.value;
          covariantReturnTypes.add(dexValueType.value);
        } else if (name.equals("presentAfter")) {
          hasPresentAfterElement = true;
        }
      } else {
        if (name.equals("value")) {
          if (!(element.value instanceof DexValue.DexValueArray)) {
            throw new CompilationError(
                String.format(
                    "Expected element \"value\" of CovariantReturnTypes annotation to "
                        + "be an array (method: \"%s\", was: %s)",
                    method.toSourceString(), element.value.getClass().getCanonicalName()));
          }

          DexValue.DexValueArray array = (DexValue.DexValueArray) element.value;
          // Handle the inner dalvik.annotation.codegen.CovariantReturnType annotations recursively.
          for (DexValue value : array.getValues()) {
            assert value instanceof DexValue.DexValueAnnotation;
            DexValue.DexValueAnnotation innerAnnotation = (DexValue.DexValueAnnotation) value;
            getCovariantReturnTypesFromAnnotation(
                clazz, method, innerAnnotation.value, covariantReturnTypes);
          }
        }
      }
    }

    if (annotation.type == factory.annotationCovariantReturnType && !hasPresentAfterElement) {
      throw new CompilationError(
          String.format(
              "CovariantReturnType annotation for method \"%s\" is missing mandatory element "
                  + "\"presentAfter\" (class %s)",
              clazz.getType(), method.toSourceString()));
    }
  }

  private boolean isCovariantReturnTypeAnnotation(DexEncodedAnnotation annotation) {
    return isCovariantReturnTypeAnnotation(annotation.type, factory);
  }

  public static boolean isCovariantReturnTypeAnnotation(DexType type, DexItemFactory factory) {
    return type == factory.annotationCovariantReturnType
        || type == factory.annotationCovariantReturnTypes;
  }

  private static boolean hasVirtualMethodWithSignature(DexClass clazz, DexEncodedMethod method) {
    for (DexEncodedMethod existingMethod : clazz.virtualMethods()) {
      if (existingMethod.method.equals(method.method)) {
        return true;
      }
    }
    return false;
  }
}
