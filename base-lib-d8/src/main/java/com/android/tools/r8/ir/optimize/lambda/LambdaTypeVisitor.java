// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda;

import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueField;
import com.android.tools.r8.graph.DexValue.DexValueMethod;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueMethodType;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Encapsulates the logic of visiting all lambda classes.
final class LambdaTypeVisitor {
  private final DexItemFactory factory;
  private final Predicate<DexType> isLambdaType;
  private final Consumer<DexType> onLambdaType;

  LambdaTypeVisitor(DexItemFactory factory,
      Predicate<DexType> isLambdaType, Consumer<DexType> onLambdaType) {
    this.factory = factory;
    this.isLambdaType = isLambdaType;
    this.onLambdaType = onLambdaType;
  }

  void accept(DexCallSite callSite) {
    accept(callSite.methodProto);
    accept(callSite.bootstrapMethod);
    for (DexValue value : callSite.bootstrapArgs) {
      accept(value);
    }
  }

  private void accept(DexValue value) {
    if (value instanceof DexValueType) {
      accept(((DexValueType) value).value);
      return;
    }
    if (value instanceof DexValueArray) {
      for (DexValue subValue : ((DexValueArray) value).getValues()) {
        accept(subValue);
      }
      return;
    }
    if (value instanceof DexValueMethod) {
      accept(((DexValueMethod) value).value, null);
      return;
    }
    if (value instanceof DexValueMethodHandle) {
      accept(((DexValueMethodHandle) value).value);
      return;
    }
    if (value instanceof DexValueMethodType) {
      accept(((DexValueMethodType) value).value);
      return;
    }
    if (value instanceof DexValueField) {
      accept(((DexValueField) value).value, null);
    }
  }

  void accept(DexMethodHandle handle) {
    if (handle.isFieldHandle()) {
      accept(handle.asField(), null);
    } else {
      assert handle.isMethodHandle();
      accept(handle.asMethod(), null);
    }
  }

  void accept(DexField field, DexType holderToIgnore) {
    accept(field.type);
    if (holderToIgnore != field.holder) {
      accept(field.holder);
    }
  }

  void accept(DexMethod method, DexType holderToIgnore) {
    if (holderToIgnore != method.holder) {
      accept(method.holder);
    }
    accept(method.proto);
  }

  void accept(DexProto proto) {
    accept(proto.returnType);
    accept(proto.parameters);
  }

  void accept(DexTypeList types) {
    for (DexType type : types.values) {
      accept(type);
    }
  }

  void accept(DexAnnotationSet annotationSet) {
    for (DexAnnotation annotation : annotationSet.annotations) {
      accept(annotation);
    }
  }

  void accept(ParameterAnnotationsList parameterAnnotationsList) {
    parameterAnnotationsList.forEachAnnotation(this::accept);
  }

  private void accept(DexAnnotation annotation) {
    accept(annotation.annotation);
  }

  private void accept(DexEncodedAnnotation annotation) {
    accept(annotation.type);
    for (DexAnnotationElement element : annotation.elements) {
      accept(element);
    }
  }

  private void accept(DexAnnotationElement element) {
    accept(element.value);
  }

  void accept(DexType type) {
    if (type == null) {
      return;
    }
    if (type.isPrimitiveType() || type.isVoidType() || type.isPrimitiveArrayType()) {
      return;
    }
    if (type.isArrayType()) {
      accept(type.toArrayElementType(factory));
      return;
    }
    if (isLambdaType.test(type)) {
      onLambdaType.accept(type);
    }
  }
}
