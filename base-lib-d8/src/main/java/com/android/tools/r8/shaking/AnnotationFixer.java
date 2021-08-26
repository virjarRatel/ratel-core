// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.utils.ArrayUtils;

public class AnnotationFixer {

  private final GraphLense lense;

  public AnnotationFixer(GraphLense lense) {
    this.lense = lense;
  }

  public void run(Iterable<DexProgramClass> classes) {
    for (DexProgramClass clazz : classes) {
      clazz.annotations = clazz.annotations.rewrite(this::rewriteAnnotation);
      clazz.forEachMethod(this::processMethod);
      clazz.forEachField(this::processField);
    }
  }

  private void processMethod(DexEncodedMethod method) {
    method.annotations = method.annotations.rewrite(this::rewriteAnnotation);
    method.parameterAnnotationsList =
        method.parameterAnnotationsList.rewrite(
            dexAnnotationSet -> dexAnnotationSet.rewrite(this::rewriteAnnotation));
  }

  private void processField(DexEncodedField field) {
    field.annotations = field.annotations.rewrite(this::rewriteAnnotation);
  }

  private DexAnnotation rewriteAnnotation(DexAnnotation original) {
    return original.rewrite(this::rewriteEncodedAnnotation);
  }

  private DexEncodedAnnotation rewriteEncodedAnnotation(DexEncodedAnnotation original) {
    DexEncodedAnnotation rewritten =
        original.rewrite(lense::lookupType, this::rewriteAnnotationElement);
    assert rewritten != null;
    return rewritten;
  }

  private DexAnnotationElement rewriteAnnotationElement(DexAnnotationElement original) {
    DexValue rewrittenValue = rewriteValue(original.value);
    if (rewrittenValue != original.value) {
      return new DexAnnotationElement(original.name, rewrittenValue);
    }
    return original;
  }

  private DexValue rewriteValue(DexValue value) {
    if (value.isDexValueType()) {
      DexType originalType = value.asDexValueType().value;
      DexType rewrittenType = lense.lookupType(originalType);
      if (rewrittenType != originalType) {
        return new DexValueType(rewrittenType);
      }
    } else if (value.isDexValueArray()) {
      DexValue[] originalValues = value.asDexValueArray().getValues();
      DexValue[] rewrittenValues =
          ArrayUtils.map(DexValue[].class, originalValues, this::rewriteValue);
      if (rewrittenValues != originalValues) {
        return new DexValueArray(rewrittenValues);
      }
    }
    return value;
  }
}
