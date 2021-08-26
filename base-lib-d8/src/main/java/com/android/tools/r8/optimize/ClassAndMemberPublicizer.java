// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import static com.android.tools.r8.dex.Constants.ACC_PRIVATE;
import static com.android.tools.r8.dex.Constants.ACC_PROTECTED;
import static com.android.tools.r8.dex.Constants.ACC_PUBLIC;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.ir.optimize.MethodPoolCollection;
import com.android.tools.r8.optimize.PublicizerLense.PublicizedLenseBuilder;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Timing;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public final class ClassAndMemberPublicizer {

  private final DexApplication application;
  private final AppView<AppInfoWithLiveness> appView;
  private final MethodPoolCollection methodPoolCollection;

  private final PublicizedLenseBuilder lenseBuilder = PublicizerLense.createBuilder();

  private ClassAndMemberPublicizer(
      DexApplication application, AppView<AppInfoWithLiveness> appView) {
    this.application = application;
    this.appView = appView;
    this.methodPoolCollection =
        // We will add private instance methods when we promote them.
        new MethodPoolCollection(appView, MethodPoolCollection::excludesPrivateInstanceMethod);
  }

  /**
   * Marks all package private and protected methods and fields as public. Makes all private static
   * methods public. Makes private instance methods public final instance methods, if possible.
   *
   * <p>This will destructively update the DexApplication passed in as argument.
   */
  public static GraphLense run(
      ExecutorService executorService,
      Timing timing,
      DexApplication application,
      AppView<AppInfoWithLiveness> appView)
      throws ExecutionException {
    return new ClassAndMemberPublicizer(application, appView).run(executorService, timing);
  }

  private GraphLense run(ExecutorService executorService, Timing timing)
      throws ExecutionException {
    // Phase 1: Collect methods to check if private instance methods don't have conflicts.
    methodPoolCollection.buildAll(executorService, timing);

    // Phase 2: Visit classes and promote class/member to public if possible.
    timing.begin("Phase 2: promoteToPublic");
    for (DexClass iface : appView.appInfo().computeReachableInterfaces(Collections.emptySet())) {
      publicizeType(iface.type);
    }
    publicizeType(appView.dexItemFactory().objectType);
    timing.end();

    return lenseBuilder.build(appView);
  }

  private void publicizeType(DexType type) {
    DexClass clazz = application.definitionFor(type);
    if (clazz != null && clazz.isProgramClass()) {
      clazz.accessFlags.promoteToPublic();

      // Publicize fields.
      clazz.forEachField(field -> field.accessFlags.promoteToPublic());

      // Publicize methods.
      Set<DexEncodedMethod> privateInstanceEncodedMethods = new LinkedHashSet<>();
      clazz.forEachMethod(encodedMethod -> {
        if (publicizeMethod(clazz, encodedMethod)) {
          privateInstanceEncodedMethods.add(encodedMethod);
        }
      });
      if (!privateInstanceEncodedMethods.isEmpty()) {
        clazz.virtualizeMethods(privateInstanceEncodedMethods);
      }

      // Publicize inner class attribute.
      InnerClassAttribute attr = clazz.getInnerClassAttributeForThisClass();
      if (attr != null) {
        int accessFlags = ((attr.getAccess() | ACC_PUBLIC) & ~ACC_PRIVATE) & ~ACC_PROTECTED;
        clazz.replaceInnerClassAttributeForThisClass(
            new InnerClassAttribute(
                accessFlags, attr.getInner(), attr.getOuter(), attr.getInnerName()));
      }
    }

    appView.appInfo().forAllImmediateExtendsSubtypes(type, this::publicizeType);
  }

  private boolean publicizeMethod(DexClass holder, DexEncodedMethod encodedMethod) {
    MethodAccessFlags accessFlags = encodedMethod.accessFlags;
    if (accessFlags.isPublic()) {
      return false;
    }
    if (!accessFlags.isPrivate() || appView.dexItemFactory().isConstructor(encodedMethod.method)) {
      accessFlags.promoteToPublic();
      return false;
    }

    if (!accessFlags.isStatic()) {
      // If this method is mentioned in keep rules, do not transform (rule applications changed).
      if (appView.appInfo().isPinned(encodedMethod.method)) {
        return false;
      }

      // We can't publicize private instance methods in interfaces or methods that are copied from
      // interfaces to lambda-desugared classes because this will be added as a new default method.
      // TODO(b/111118390): It might be possible to transform it into static methods, though.
      if (holder.isInterface() || accessFlags.isSynthetic()) {
        return false;
      }

      boolean wasSeen = methodPoolCollection.markIfNotSeen(holder, encodedMethod.method);
      if (wasSeen) {
        // We can't do anything further because even renaming is not allowed due to the keep rule.
        if (appView.rootSet().mayNotBeMinified(encodedMethod.method, appView)) {
          return false;
        }
        // TODO(b/111118390): Renaming will enable more private instance methods to be publicized.
        return false;
      }
      lenseBuilder.add(encodedMethod.method);
      accessFlags.promoteToFinal();
      accessFlags.promoteToPublic();
      // Although the current method became public, it surely has the single virtual target.
      encodedMethod.method.setSingleVirtualMethodCache(
          encodedMethod.method.holder, encodedMethod);
      return true;
    }

    // For private static methods we can just relax the access to private, since
    // even though JLS prevents from declaring static method in derived class if
    // an instance method with same signature exists in superclass, JVM actually
    // does not take into account access of the static methods.
    accessFlags.promoteToPublic();
    return false;
  }
}
