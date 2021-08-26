// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.optimize.InvokeSingleTargetExtractor.InvokeKind;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class VisibilityBridgeRemover {

  private final AppView<AppInfoWithLiveness> appView;
  private final Consumer<DexEncodedMethod> unneededVisibilityBridgeConsumer;

  public VisibilityBridgeRemover(AppView<AppInfoWithLiveness> appView) {
    this(appView, null);
  }

  public VisibilityBridgeRemover(
      AppView<AppInfoWithLiveness> appView,
      Consumer<DexEncodedMethod> unneededVisibilityBridgeConsumer) {
    this.appView = appView;
    this.unneededVisibilityBridgeConsumer = unneededVisibilityBridgeConsumer;
  }

  private void removeUnneededVisibilityBridgesFromClass(DexProgramClass clazz) {
    DexEncodedMethod[] newDirectMethods = removeUnneededVisibilityBridges(clazz.directMethods());
    if (newDirectMethods != null) {
      clazz.setDirectMethods(newDirectMethods);
    }
    DexEncodedMethod[] newVirtualMethods = removeUnneededVisibilityBridges(clazz.virtualMethods());
    if (newVirtualMethods != null) {
      clazz.setVirtualMethods(newVirtualMethods);
    }
  }

  private DexEncodedMethod[] removeUnneededVisibilityBridges(List<DexEncodedMethod> methods) {
    Set<DexEncodedMethod> methodsToBeRemoved = null;
    for (DexEncodedMethod method : methods) {
      if (isUnneededVisibilityBridge(method)) {
        if (methodsToBeRemoved == null) {
          methodsToBeRemoved = Sets.newIdentityHashSet();
        }
        methodsToBeRemoved.add(method);
        if (unneededVisibilityBridgeConsumer != null) {
          unneededVisibilityBridgeConsumer.accept(method);
        }
      }
    }
    if (methodsToBeRemoved != null) {
      Set<DexEncodedMethod> finalMethodsToBeRemoved = methodsToBeRemoved;
      return methods.stream()
          .filter(method -> !finalMethodsToBeRemoved.contains(method))
          .toArray(DexEncodedMethod[]::new);
    }
    return null;
  }

  private boolean isUnneededVisibilityBridge(DexEncodedMethod method) {
    if (appView.appInfo().isPinned(method.method)) {
      return false;
    }
    MethodAccessFlags accessFlags = method.accessFlags;
    if (!accessFlags.isBridge() || accessFlags.isAbstract()) {
      return false;
    }
    InvokeSingleTargetExtractor targetExtractor =
        new InvokeSingleTargetExtractor(appView.dexItemFactory());
    method.getCode().registerCodeReferences(method, targetExtractor);
    DexMethod target = targetExtractor.getTarget();
    InvokeKind kind = targetExtractor.getKind();
    // javac-generated visibility forward bridge method has same descriptor (name, signature and
    // return type).
    if (target != null && target.hasSameProtoAndName(method.method)) {
      assert !accessFlags.isPrivate() && !accessFlags.isConstructor();
      if (kind == InvokeKind.SUPER) {
        // This is a visibility forward, so check for the direct target.
        DexEncodedMethod targetMethod =
            appView.appInfo().resolveMethod(target.holder, target).getSingleTarget();
        if (targetMethod != null && targetMethod.accessFlags.isPublic()) {
          if (Log.ENABLED) {
            Log.info(
                getClass(),
                "Removing visibility forwarding %s -> %s",
                method.method,
                targetMethod.method);
          }
          return true;
        }
      }
    }
    return false;
  }

  public void run() {
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      removeUnneededVisibilityBridgesFromClass(clazz);
    }
  }
}
