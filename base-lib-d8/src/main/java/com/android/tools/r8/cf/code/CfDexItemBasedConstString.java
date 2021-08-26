// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.naming.dexitembasedstring.NameComputationInfo;
import org.objectweb.asm.MethodVisitor;

public class CfDexItemBasedConstString extends CfInstruction {

  private final DexReference item;
  private final NameComputationInfo<?> nameComputationInfo;

  public CfDexItemBasedConstString(DexReference item, NameComputationInfo<?> nameComputationInfo) {
    this.item = item;
    this.nameComputationInfo = nameComputationInfo;
  }

  public DexReference getItem() {
    return item;
  }

  public NameComputationInfo<?> getNameComputationInfo() {
    return nameComputationInfo;
  }

  @Override
  public CfDexItemBasedConstString asDexItemBasedConstString() {
    return this;
  }

  @Override
  public boolean isDexItemBasedConstString() {
    return true;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    throw new Unreachable(
        "CfDexItemBasedConstString instructions should always be rewritten into CfConstString");
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  @Override
  public boolean canThrow() {
    // const-string* may throw in dex. (Not in CF, see CfSourceCode.canThrowHelper)
    return true;
  }

  @Override
  public void registerUse(UseRegistry registry, DexType clazz) {
    if (nameComputationInfo.needsToRegisterReference()) {
      assert item.isDexType();
      registry.registerTypeReference(item.asDexType());
    }
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    builder.addDexItemBasedConstString(
        state.push(builder.appView.dexItemFactory().stringType).register,
        item,
        nameComputationInfo);
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return inliningConstraints.forDexItemBasedConstString(item, invocationContext);
  }
}
