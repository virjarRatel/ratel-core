// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jar;

import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.graph.UseRegistry.MethodHandleUse;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JarRegisterEffectsVisitor extends MethodVisitor {
  private final DexType clazz;
  private final UseRegistry registry;
  private final JarApplicationReader application;

  public JarRegisterEffectsVisitor(DexType clazz, UseRegistry registry,
      JarApplicationReader application) {
    super(ASM_VERSION);
    this.clazz = clazz;
    this.registry = registry;
    this.application = application;
  }

  @Override
  public void visitTypeInsn(int opcode, String name) {
    DexType type = application.getTypeFromName(name);
    if (opcode == org.objectweb.asm.Opcodes.NEW) {
      registry.registerNewInstance(type);
    } else if (opcode == Opcodes.CHECKCAST) {
      registry.registerCheckCast(type);
    } else {
      registry.registerTypeReference(type);
    }
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    registry.registerTypeReference(application.getTypeFromDescriptor(desc));
  }

  @Override
  public void visitLdcInsn(Object cst) {
    if (cst instanceof Type) {
      if (((Type) cst).getSort() == Type.METHOD) {
        String descriptor = ((Type) cst).getDescriptor();
        assert descriptor.charAt(0) == '(';
        registry.registerProto(application.getProto(descriptor));
      } else {
        registry.registerConstClass(application.getType((Type) cst));
      }
    } else if (cst instanceof ConstantDynamic) {
      throw new CompilationError("Unsupported dynamic constant: " + cst.toString());
    } else if (cst instanceof Handle) {
      DexMethodHandle handle = DexMethodHandle.fromAsmHandle((Handle) cst, application, clazz);
      registry.registerMethodHandle(
          handle, MethodHandleUse.NOT_ARGUMENT_TO_LAMBDA_METAFACTORY);
    }
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    DexType ownerType = application.getTypeFromName(owner);
    DexMethod method = application.getMethod(ownerType, name, desc);
    switch (opcode) {
      case Opcodes.INVOKEVIRTUAL:
        registry.registerInvokeVirtual(method);
        break;
      case Opcodes.INVOKESTATIC:
        registry.registerInvokeStatic(method);
        break;
      case Opcodes.INVOKEINTERFACE:
        registry.registerInvokeInterface(method);
        break;
      case Opcodes.INVOKESPECIAL:
        if (name.equals(Constants.INSTANCE_INITIALIZER_NAME) || ownerType == clazz) {
          registry.registerInvokeDirect(method);
        } else {
          registry.registerInvokeSuper(method);
        }
        break;
      default:
        throw new Unreachable("Unexpected opcode " + opcode);
    }
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    DexField field = application.getField(owner, name, desc);
    switch (opcode) {
      case Opcodes.GETFIELD:
        registry.registerInstanceFieldRead(field);
        break;
      case Opcodes.PUTFIELD:
        registry.registerInstanceFieldWrite(field);
        break;
      case Opcodes.GETSTATIC:
        registry.registerStaticFieldRead(field);
        break;
      case Opcodes.PUTSTATIC:
        registry.registerStaticFieldWrite(field);
        break;
      default:
        throw new Unreachable("Unexpected opcode " + opcode);
    }
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    registry.registerCallSite(
        DexCallSite.fromAsmInvokeDynamic(application, clazz, name, desc, bsm, bsmArgs));
  }

}
