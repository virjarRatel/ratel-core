// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.classinliner.ClassInlinerEligibilityInfo;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.BitSet;
import java.util.Set;

public abstract class OptimizationFeedbackIgnore extends OptimizationFeedback {

  private static final OptimizationFeedbackIgnore INSTANCE = new OptimizationFeedbackIgnore() {};

  protected OptimizationFeedbackIgnore() {}

  public static OptimizationFeedbackIgnore getInstance() {
    return INSTANCE;
  }

  // FIELD OPTIMIZATION INFO:

  @Override
  public void markFieldCannotBeKept(DexEncodedField field) {}

  @Override
  public void markFieldAsPropagated(DexEncodedField field) {}

  @Override
  public void markFieldHasDynamicLowerBoundType(
      DexEncodedField field, ClassTypeLatticeElement type) {}

  @Override
  public void markFieldHasDynamicUpperBoundType(DexEncodedField field, TypeLatticeElement type) {}

  @Override
  public void markFieldBitsRead(DexEncodedField field, int bitsRead) {}

  @Override
  public void recordFieldHasAbstractValue(
      DexEncodedField field, AppView<AppInfoWithLiveness> appView, AbstractValue abstractValue) {}

  // METHOD OPTIMIZATION INFO:

  @Override
  public void markForceInline(DexEncodedMethod method) {}

  @Override
  public void markInlinedIntoSingleCallSite(DexEncodedMethod method) {}

  @Override
  public void markMethodCannotBeKept(DexEncodedMethod method) {}

  @Override
  public void methodInitializesClassesOnNormalExit(
      DexEncodedMethod method, Set<DexType> initializedClasses) {}

  @Override
  public void methodReturnsArgument(DexEncodedMethod method, int argument) {}

  @Override
  public void methodReturnsAbstractValue(
      DexEncodedMethod method, AppView<AppInfoWithLiveness> appView, AbstractValue value) {}

  @Override
  public void methodReturnsObjectWithUpperBoundType(
      DexEncodedMethod method, AppView<?> appView, TypeLatticeElement type) {}

  @Override
  public void methodReturnsObjectWithLowerBoundType(
      DexEncodedMethod method, ClassTypeLatticeElement type) {}

  @Override
  public void methodMayNotHaveSideEffects(DexEncodedMethod method) {}

  @Override
  public void methodReturnValueOnlyDependsOnArguments(DexEncodedMethod method) {}

  @Override
  public void methodNeverReturnsNull(DexEncodedMethod method) {}

  @Override
  public void methodNeverReturnsNormally(DexEncodedMethod method) {}

  @Override
  public void markAsPropagated(DexEncodedMethod method) {}

  @Override
  public void markProcessed(DexEncodedMethod method, ConstraintWithTarget state) {}

  @Override
  public void markUseIdentifierNameString(DexEncodedMethod method) {}

  @Override
  public void markCheckNullReceiverBeforeAnySideEffect(DexEncodedMethod method, boolean mark) {}

  @Override
  public void markTriggerClassInitBeforeAnySideEffect(DexEncodedMethod method, boolean mark) {}

  @Override
  public void setClassInlinerEligibility(
      DexEncodedMethod method, ClassInlinerEligibilityInfo eligibility) {}

  @Override
  public void setInstanceInitializerInfo(
      DexEncodedMethod method, InstanceInitializerInfo instanceInitializerInfo) {}

  @Override
  public void setInitializerEnablingJavaAssertions(DexEncodedMethod method) {
  }

  @Override
  public void setParameterUsages(DexEncodedMethod method, ParameterUsagesInfo parameterUsagesInfo) {
  }

  @Override
  public void setNonNullParamOrThrow(DexEncodedMethod method, BitSet facts) {
  }

  @Override
  public void setNonNullParamOnNormalExits(DexEncodedMethod method, BitSet facts) {
  }

  @Override
  public void classInitializerMayBePostponed(DexEncodedMethod method) {}
}
