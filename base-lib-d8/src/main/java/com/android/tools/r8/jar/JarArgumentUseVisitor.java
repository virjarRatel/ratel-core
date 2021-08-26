// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.jar;

import static org.objectweb.asm.Opcodes.ALOAD;
import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.LLOAD;

import com.android.tools.r8.graph.ArgumentUse;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.objectweb.asm.MethodVisitor;

public class JarArgumentUseVisitor extends MethodVisitor {
  private final ArgumentUse registry;
  private final Int2IntMap slotToArgument;
  private final int arguments;

  public JarArgumentUseVisitor(DexEncodedMethod method, ArgumentUse registry) {
    super(ASM_VERSION);
    this.registry = registry;

    DexProto proto = method.method.proto;
    boolean isStatic = method.accessFlags.isStatic();

    boolean hasLongOrDouble = false;
    for (DexType value : proto.parameters.values) {
      if (value.isLongType() || value.isDoubleType()) {
        hasLongOrDouble = true;
        break;
      }
    }
    if (hasLongOrDouble) {
      this.slotToArgument = new Int2IntArrayMap();
      int nextSlot = 0;
      int nextArgument = 0;
      if (!isStatic) {
        slotToArgument.put(nextSlot++, nextArgument++);
      }
      for (DexType value : proto.parameters.values) {
        slotToArgument.put(nextSlot++, nextArgument++);
        if (value.isLongType() || value.isDoubleType()) {
          nextSlot++;
        }
      }
      this.arguments = nextSlot;
    } else {
      this.slotToArgument = null;
      this.arguments = proto.parameters.values.length + (isStatic ? 0 : 1);
    }
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    if (var < arguments) {
      registry.register(slotToArgument == null ? var : slotToArgument.get(var));
    }
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    switch (opcode) {
      case ILOAD:
      case LLOAD:
      case FLOAD:
      case DLOAD:
      case ALOAD:
        if (var < arguments) {
          registry.register(slotToArgument == null ? var : slotToArgument.get(var));
        }
        break;

      default:
        // Ignore
    }
  }
}
