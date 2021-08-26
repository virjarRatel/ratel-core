// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.ClassWriter;

public class NestMemberClassAttribute {

  private final DexType nestMember;

  public NestMemberClassAttribute(DexType nestMember) {
    this.nestMember = nestMember;
  }

  public DexType getNestMember() {
    return nestMember;
  }

  public void write(ClassWriter writer, NamingLens lens) {
    assert nestMember != null;
    writer.visitNestMember(lens.lookupInternalName(nestMember));
  }
}
