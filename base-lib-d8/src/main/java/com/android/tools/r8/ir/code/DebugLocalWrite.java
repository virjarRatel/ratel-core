// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.CfBuilder;

/**
 * Instruction introducing an SSA value with attached local information.
 *
 * <p>All instructions may have attached local information (defined as the local information of
 * their outgoing value). This instruction is needed to mark a transition of an existing value (with
 * a possible local attached) to a new value that has a local (possibly the same one). Even if the
 * debug info of the ingoing value is equal to that of the outgoing value, the write may still be
 * needed since an explicit end may have ended the visibility range of the local which now becomes
 * visible again.
 *
 * <p>For valid debug info, this instruction should have at least one debug user, denoting the end
 * of its range, and thus it should be live.
 */
public class DebugLocalWrite extends Move {

  public DebugLocalWrite(Value dest, Value src) {
    super(dest, src);
    assert dest.hasLocalInfo();
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean isDebugLocalWrite() {
    return true;
  }

  @Override
  public DebugLocalWrite asDebugLocalWrite() {
    return this;
  }

  @Override
  public boolean isOutConstant() {
    return false;
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isDebugLocalWrite();
  }

  @Override
  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    return helper.getDexType(src());
  }

  @Override
  public void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper) {
    helper.loadInValues(this, it);
    // A local-write does not have an outgoing stack value, but in writes directly to the local.
  }

  @Override
  public void buildCf(CfBuilder builder) {
    builder.add(new CfStore(outType(), builder.getLocalRegister(outValue())));
  }

  @Override
  public boolean isAllowedAfterThrowingInstruction() {
    return true;
  }

  @Override
  public boolean verifyTypes(AppView<?> appView) {
    super.verifyTypes(appView);
    assert src().getTypeLattice().lessThanOrEqual(outValue().getTypeLattice(), appView);
    return true;
  }
}
