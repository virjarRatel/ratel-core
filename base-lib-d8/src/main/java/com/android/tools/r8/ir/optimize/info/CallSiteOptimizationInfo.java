// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.ir.optimize.CallSiteOptimizationInfoPropagator;

// A flat lattice structure:
//   BOTTOM, TOP, and a lattice element that holds accumulated argument info.
public abstract class CallSiteOptimizationInfo {
  public static BottomCallSiteOptimizationInfo BOTTOM = BottomCallSiteOptimizationInfo.INSTANCE;
  public static TopCallSiteOptimizationInfo TOP = TopCallSiteOptimizationInfo.INSTANCE;

  public boolean isBottom() {
    return false;
  }

  public boolean isTop() {
    return false;
  }

  public boolean isConcreteCallSiteOptimizationInfo() {
    return false;
  }

  public ConcreteCallSiteOptimizationInfo asConcreteCallSiteOptimizationInfo() {
    return null;
  }

  public CallSiteOptimizationInfo join(
      CallSiteOptimizationInfo other, AppView<?> appView, DexEncodedMethod encodedMethod) {
    if (isBottom()) {
      return other;
    }
    if (other.isBottom()) {
      return this;
    }
    if (isTop() || other.isTop()) {
      return TOP;
    }
    assert isConcreteCallSiteOptimizationInfo() && other.isConcreteCallSiteOptimizationInfo();
    return asConcreteCallSiteOptimizationInfo()
        .join(other.asConcreteCallSiteOptimizationInfo(), appView, encodedMethod);
  }

  /**
   * {@link CallSiteOptimizationInfoPropagator} will reprocess the call target if its collected call
   * site optimization info has something useful that can trigger more optimizations. For example,
   * if a certain argument is guaranteed to be definitely not null for all call sites, null-check on
   * that argument can be simplified during the reprocessing of the method.
   */
  public boolean hasUsefulOptimizationInfo(AppView<?> appView, DexEncodedMethod encodedMethod) {
    return false;
  }

  // The index exactly matches with in values of invocation, i.e., even including receiver.
  public TypeLatticeElement getDynamicUpperBoundType(int argIndex) {
    return null;
  }

  // TODO(b/139246447): dynamic lower bound type?

  // TODO(b/69963623): we need to re-run unused argument removal?

  // The index exactly matches with in values of invocation, i.e., even including receiver.
  public AbstractValue getAbstractArgumentValue(int argIndex) {
    return UnknownValue.getInstance();
  }

  // TODO(b/139249918): propagate classes that are guaranteed to be initialized.
}
