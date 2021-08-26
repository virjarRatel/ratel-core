// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.graph.DexValue.DexValueDouble;
import com.android.tools.r8.graph.DexValue.DexValueFloat;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueLong;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueMethodType;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.graph.DexValue.DexValueType;

public abstract class UseRegistry {

  private DexItemFactory factory;

  public enum MethodHandleUse {
    ARGUMENT_TO_LAMBDA_METAFACTORY,
    NOT_ARGUMENT_TO_LAMBDA_METAFACTORY
  }

  public UseRegistry(DexItemFactory factory) {
    this.factory = factory;
  }

  public abstract boolean registerInvokeVirtual(DexMethod method);

  public abstract boolean registerInvokeDirect(DexMethod method);

  public abstract boolean registerInvokeStatic(DexMethod method);

  public abstract boolean registerInvokeInterface(DexMethod method);

  public abstract boolean registerInvokeSuper(DexMethod method);

  public abstract boolean registerInstanceFieldWrite(DexField field);

  public abstract boolean registerInstanceFieldRead(DexField field);

  public abstract boolean registerNewInstance(DexType type);

  public abstract boolean registerStaticFieldRead(DexField field);

  public abstract boolean registerStaticFieldWrite(DexField field);

  public abstract boolean registerTypeReference(DexType type);

  public boolean registerConstClass(DexType type) {
    return registerTypeReference(type);
  }

  public boolean registerCheckCast(DexType type) {
    return registerTypeReference(type);
  }

  public void registerMethodHandle(
      DexMethodHandle methodHandle, MethodHandleUse use) {
    switch (methodHandle.type) {
      case INSTANCE_GET:
        registerInstanceFieldRead(methodHandle.asField());
        break;
      case INSTANCE_PUT:
        registerInstanceFieldWrite(methodHandle.asField());
        break;
      case STATIC_GET:
        registerStaticFieldRead(methodHandle.asField());
        break;
      case STATIC_PUT:
        registerStaticFieldWrite(methodHandle.asField());
        break;
      case INVOKE_INSTANCE:
        registerInvokeVirtual(methodHandle.asMethod());
        break;
      case INVOKE_STATIC:
        registerInvokeStatic(methodHandle.asMethod());
        break;
      case INVOKE_CONSTRUCTOR:
        DexMethod method = methodHandle.asMethod();
        registerNewInstance(method.holder);
        registerInvokeDirect(method);
        break;
      case INVOKE_INTERFACE:
        registerInvokeInterface(methodHandle.asMethod());
        break;
      case INVOKE_SUPER:
        registerInvokeSuper(methodHandle.asMethod());
        break;
      case INVOKE_DIRECT:
        registerInvokeDirect(methodHandle.asMethod());
        break;
      default:
        throw new AssertionError();
    }
  }

  public void registerCallSite(DexCallSite callSite) {
    boolean isLambdaMetaFactory =
        factory.isLambdaMetafactoryMethod(callSite.bootstrapMethod.asMethod());

    registerMethodHandle(
        callSite.bootstrapMethod, MethodHandleUse.NOT_ARGUMENT_TO_LAMBDA_METAFACTORY);

    // Lambda metafactory will use this type as the main SAM
    // interface for the dynamically created lambda class.
    registerTypeReference(callSite.methodProto.returnType);

    // Register bootstrap method arguments.
    // Only Type, MethodHandle, and MethodType need to be registered.
    for (DexValue arg : callSite.bootstrapArgs) {
      if (arg instanceof DexValueType) {
        registerTypeReference(((DexValueType) arg).value);
      } else if (arg instanceof DexValueMethodHandle) {
        DexMethodHandle handle = ((DexValueMethodHandle) arg).value;
        MethodHandleUse use = isLambdaMetaFactory
            ? MethodHandleUse.ARGUMENT_TO_LAMBDA_METAFACTORY
            : MethodHandleUse.NOT_ARGUMENT_TO_LAMBDA_METAFACTORY;
        registerMethodHandle(handle, use);
      } else if (arg instanceof DexValueMethodType) {
        registerProto(((DexValueMethodType) arg).value);
      } else {
        assert (arg instanceof DexValueInt)
            || (arg instanceof DexValueLong)
            || (arg instanceof DexValueFloat)
            || (arg instanceof DexValueDouble)
            || (arg instanceof DexValueString);
      }
    }
  }

  public void registerProto(DexProto proto) {
    registerTypeReference(proto.returnType);
    for (DexType type : proto.parameters.values) {
      registerTypeReference(type);
    }
  }
}
