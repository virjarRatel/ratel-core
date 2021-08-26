// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.optimize.classinliner.ClassInlinerEligibilityInfo;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsage;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import java.util.BitSet;
import java.util.Set;

public interface MethodOptimizationInfo {

  enum InlinePreference {
    NeverInline,
    ForceInline,
    Default
  }

  boolean isDefaultMethodOptimizationInfo();

  boolean isUpdatableMethodOptimizationInfo();

  UpdatableMethodOptimizationInfo asUpdatableMethodOptimizationInfo();

  boolean cannotBeKept();

  boolean classInitializerMayBePostponed();

  TypeLatticeElement getDynamicUpperBoundType();

  ClassTypeLatticeElement getDynamicLowerBoundType();

  ParameterUsage getParameterUsages(int parameter);

  BitSet getNonNullParamOrThrow();

  BitSet getNonNullParamOnNormalExits();

  boolean hasBeenInlinedIntoSingleCallSite();

  boolean isReachabilitySensitive();

  boolean returnsArgument();

  int getReturnedArgument();

  boolean neverReturnsNull();

  boolean neverReturnsNormally();

  ClassInlinerEligibilityInfo getClassInlinerEligibility();

  Set<DexType> getInitializedClassesOnNormalExit();

  InstanceInitializerInfo getInstanceInitializerInfo();

  boolean isInitializerEnablingJavaAssertions();

  AbstractValue getAbstractReturnValue();

  boolean forceInline();

  boolean neverInline();

  boolean useIdentifierNameString();

  boolean checksNullReceiverBeforeAnySideEffect();

  boolean triggersClassInitBeforeAnySideEffect();

  boolean mayHaveSideEffects();

  boolean returnValueOnlyDependsOnArguments();

  boolean returnValueHasBeenPropagated();

  UpdatableMethodOptimizationInfo mutableCopy();
}
