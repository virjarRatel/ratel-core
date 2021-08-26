// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.dexitembasedstring.NameComputationInfo;
import java.nio.ShortBuffer;

public class DexItemBasedConstString extends Format21c {

  public static final String NAME = "DexItemBasedConstString";
  public static final String SMALI_NAME = "const-string*";

  private final NameComputationInfo<?> nameComputationInfo;

  public DexItemBasedConstString(
      int register, DexReference string, NameComputationInfo<?> nameComputationInfo) {
    super(register, string);
    this.nameComputationInfo = nameComputationInfo;
  }

  public DexReference getItem() {
    return (DexReference) BBBB;
  }

  public NameComputationInfo<?> getNameComputationInfo() {
    return nameComputationInfo;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getSmaliName() {
    return SMALI_NAME;
  }

  @Override
  public int getOpcode() {
    throw new Unreachable(
        "DexItemBasedConstString instructions should always be rewritten into ConstString");
  }

  @Override
  public DexItemBasedConstString asDexItemBasedConstString() {
    return this;
  }

  @Override
  public boolean isDexItemBasedConstString() {
    return true;
  }

  @Override
  public String toString(ClassNameMapper naming) {
    // TODO(christofferqa): Apply mapping to item.
    return formatString("v" + AA + ", \"" + BBBB.toString() + "\"");
  }

  @Override
  public String toSmaliString(ClassNameMapper naming) {
    // TODO(christofferqa): Apply mapping to item.
    return formatSmaliString("v" + AA + ", \"" + BBBB.toString() + "\"");
  }

  @Override
  public void write(ShortBuffer dest, ObjectToOffsetMapping mapping) {
    throw new Unreachable(
        "DexItemBasedConstString instructions should always be rewritten into ConstString");
  }

  @Override
  public void registerUse(UseRegistry registry) {
    if (nameComputationInfo.needsToRegisterReference()) {
      assert getItem().isDexType();
      registry.registerTypeReference(getItem().asDexType());
    }
  }

  @Override
  public void buildIR(IRBuilder builder) {
    builder.addDexItemBasedConstString(AA, (DexReference) BBBB, nameComputationInfo);
  }

  @Override
  public boolean canThrow() {
    return true;
  }
}
