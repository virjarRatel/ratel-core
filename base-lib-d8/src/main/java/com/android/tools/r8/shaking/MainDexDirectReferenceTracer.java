// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.UseRegistry;
import java.util.Set;
import java.util.function.Consumer;

public class MainDexDirectReferenceTracer {
  private final AnnotationDirectReferenceCollector annotationDirectReferenceCollector =
      new AnnotationDirectReferenceCollector();
  private final DirectReferencesCollector codeDirectReferenceCollector;

  private final AppInfoWithSubtyping appInfo;
  private final Consumer<DexType> consumer;

  public MainDexDirectReferenceTracer(AppInfoWithSubtyping appInfo, Consumer<DexType> consumer) {
    this.codeDirectReferenceCollector = new DirectReferencesCollector(appInfo.dexItemFactory());
    this.appInfo = appInfo;
    this.consumer = consumer;
  }

  public void run(Set<DexType> roots) {
    for (DexType type : roots) {
      DexClass clazz = appInfo.definitionFor(type);
      // Should only happen for library classes, which are filtered out.
      assert clazz != null;
      consumer.accept(type);
      // Super and interfaces are live, no need to add them.
      traceAnnotationsDirectDependencies(clazz.annotations);
      clazz.forEachField(field -> consumer.accept(field.field.type));
      clazz.forEachMethod(method -> {
        traceMethodDirectDependencies(method.method, consumer);
        method.registerCodeReferences(codeDirectReferenceCollector);
      });
    }
  }

  public void runOnCode(DexEncodedMethod method) {
    method.registerCodeReferences(codeDirectReferenceCollector);
  }

  private static class BooleanBox {
    boolean value = false;
  }

  public static boolean hasReferencesOutsideFromCode(
      AppInfoWithSubtyping appInfo, DexEncodedMethod method, Set<DexType> classes) {

    BooleanBox result = new BooleanBox();

    new MainDexDirectReferenceTracer(
            appInfo,
            type -> {
              DexType baseType = type.toBaseType(appInfo.dexItemFactory());
              if (baseType.isClassType() && !classes.contains(baseType)) {
                DexClass cls = appInfo.definitionFor(baseType);
                if (cls != null && cls.isProgramClass()) {
                  result.value = true;
                }
              }
            })
        .runOnCode(method);

    return result.value;
  }

  private void traceAnnotationsDirectDependencies(DexAnnotationSet annotations) {
    annotations.collectIndexedItems(annotationDirectReferenceCollector);
  }

  private void traceMethodDirectDependencies(DexMethod method, Consumer<DexType> consumer) {
    DexProto proto = method.proto;
    consumer.accept(proto.returnType);
    for (DexType parameterType : proto.parameters.values) {
      consumer.accept(parameterType);
    }
  }

  private class DirectReferencesCollector extends UseRegistry {

    private DirectReferencesCollector(DexItemFactory factory) {
      super(factory);
    }

    @Override
    public boolean registerInvokeVirtual(DexMethod method) {
      return registerInvoke(method);
    }

    @Override
    public boolean registerInvokeDirect(DexMethod method) {
      return registerInvoke(method);
    }

    @Override
    public boolean registerInvokeStatic(DexMethod method) {
      return registerInvoke(method);
    }

    @Override
    public boolean registerInvokeInterface(DexMethod method) {
      return registerInvoke(method);
    }

    @Override
    public boolean registerInvokeSuper(DexMethod method) {
      return registerInvoke(method);
    }

    protected boolean registerInvoke(DexMethod method) {
      consumer.accept(method.holder);
      traceMethodDirectDependencies(method, consumer);
      return true;
    }

    @Override
    public boolean registerInstanceFieldWrite(DexField field) {
      return registerFieldAccess(field);
    }

    @Override
    public boolean registerInstanceFieldRead(DexField field) {
      return registerFieldAccess(field);
    }

    @Override
    public boolean registerStaticFieldRead(DexField field) {
      return registerFieldAccess(field);
    }

    @Override
    public boolean registerStaticFieldWrite(DexField field) {
      return registerFieldAccess(field);
    }

    protected boolean registerFieldAccess(DexField field) {
      consumer.accept(field.holder);
      consumer.accept(field.type);
      return true;
    }

    @Override
    public boolean registerNewInstance(DexType type) {
      consumer.accept(type);
      return true;
    }

    @Override
    public boolean registerTypeReference(DexType type) {
      consumer.accept(type);
      return true;
    }
  }

  private class AnnotationDirectReferenceCollector implements IndexedItemCollection {

    @Override
    public boolean addClass(DexProgramClass dexProgramClass) {
      consumer.accept(dexProgramClass.type);
      return false;
    }

    @Override
    public boolean addField(DexField field) {
      consumer.accept(field.holder);
      consumer.accept(field.type);
      return false;
    }

    @Override
    public boolean addMethod(DexMethod method) {
      consumer.accept(method.holder);
      addProto(method.proto);
      return false;
    }

    @Override
    public boolean addString(DexString string) {
      return false;
    }

    @Override
    public boolean addProto(DexProto proto) {
      consumer.accept(proto.returnType);
      for (DexType parameterType : proto.parameters.values) {
        consumer.accept(parameterType);
      }
      return false;
    }

    @Override
    public boolean addType(DexType type) {
      consumer.accept(type);
      return false;
    }

    @Override
    public boolean addCallSite(DexCallSite callSite) {
      throw new AssertionError("CallSite are not supported when tracing for legacy multi dex");
    }

    @Override
    public boolean addMethodHandle(DexMethodHandle methodHandle) {
      throw new AssertionError(
          "DexMethodHandle are not supported when tracing for legacy multi dex");
    }
  }
}
