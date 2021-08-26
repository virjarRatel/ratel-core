// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.ClassWriter;

public class NestHostClassAttribute {

  private final DexType nestHost;

  public NestHostClassAttribute(DexType nestHost) {
    this.nestHost = nestHost;
  }

  public DexType getNestHost() {
    return nestHost;
  }

  public void write(ClassWriter writer, NamingLens lens) {
    assert nestHost != null;
    writer.visitNestHost(lens.lookupInternalName(nestHost));
  }
}
