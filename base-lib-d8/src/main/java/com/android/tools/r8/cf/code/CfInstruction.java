// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;

public abstract class CfInstruction {

  public abstract void write(MethodVisitor visitor, NamingLens lens);

  public abstract void print(CfPrinter printer);

  @Override
  public String toString() {
    CfPrinter printer = new CfPrinter();
    print(printer);
    return printer.toString();
  }

  public void registerUse(UseRegistry registry, DexType clazz) {
    // Intentionally empty.
  }

  public CfLabel getTarget() {
    return null;
  }

  public CfConstString asConstString() {
    return null;
  }

  public boolean isConstString() {
    return false;
  }

  public CfFieldInstruction asFieldInstruction() {
    return null;
  }

  public boolean isFieldInstruction() {
    return false;
  }

  public CfGoto asGoto() {
    return null;
  }

  public boolean isGoto() {
    return false;
  }

  public CfInvoke asInvoke() {
    return null;
  }

  public boolean isInvoke() {
    return false;
  }

  public CfLabel asLabel() {
    return null;
  }

  public boolean isLabel() {
    return false;
  }

  public CfLoad asLoad() {
    return null;
  }

  public boolean isLoad() {
    return false;
  }

  public CfStore asStore() {
    return null;
  }

  public boolean isStore() {
    return false;
  }

  public CfSwitch asSwitch() {
    return null;
  }

  public boolean isSwitch() {
    return false;
  }

  public CfDexItemBasedConstString asDexItemBasedConstString() {
    return null;
  }

  public boolean isDexItemBasedConstString() {
    return false;
  }

  /** Return true if this instruction is CfReturn or CfReturnVoid. */
  public boolean isReturn() {
    return false;
  }

  /** Return true if this instruction is CfIf or CfIfCmp. */
  public boolean isConditionalJump() {
    return false;
  }

  /** Return true if this instruction is CfIf, CfIfCmp, CfSwitch, CfGoto, CfThrow,
   * CfReturn or CfReturnVoid. */
  public boolean isJump() {
    return false;
  }

  /** Return true if this instruction or its DEX equivalent can throw. */
  public boolean canThrow() {
    return false;
  }

  public abstract void buildIR(IRBuilder builder, CfState state, CfSourceCode code);

  /** Return true if this instruction directly emits IR instructions. */
  public boolean emitsIR() {
    return true;
  }

  public abstract ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView);
}
