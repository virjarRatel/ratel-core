// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;

/**
 * Value that represents a shared physical location defined by the phi value.
 *
 * <p>This value is introduced to represent the store instructions used to unify the location of
 * in-flowing values to phi's. After introducing this fixed location the graph is no longer in SSA
 * since the fixed location signifies a place that can be written to from multiple places.
 */
public class FixedLocalValue extends Value {

  private final Phi phi;

  public FixedLocalValue(Phi phi) {
    super(phi.getNumber(), phi.getTypeLattice(), phi.getLocalInfo());
    this.phi = phi;
  }

  public int getRegister(CfRegisterAllocator allocator) {
    return allocator.getRegisterForValue(phi, -1);
  }

  public Phi getPhi() {
    return phi;
  }

  @Override
  public boolean needsRegister() {
    return true;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public String toString() {
    return "fixed:v" + phi.getNumber();
  }
}
