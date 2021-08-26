// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph.analysis;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import java.util.IdentityHashMap;
import java.util.Map;

public class InitializedClassesInInstanceMethodsAnalysis extends EnqueuerAnalysis {

  // A simple structure that stores the result of the analysis.
  public static class InitializedClassesInInstanceMethods {

    private final AppView<? extends AppInfoWithSubtyping> appView;
    private final Map<DexType, DexType> mapping;

    private InitializedClassesInInstanceMethods(
        AppView<? extends AppInfoWithSubtyping> appView, Map<DexType, DexType> mapping) {
      this.appView = appView;
      this.mapping = mapping;
    }

    public boolean isClassDefinitelyLoadedInInstanceMethodsOn(DexType subject, DexType context) {
      // If `subject` is kept, then it is instantiated by reflection, which means that the analysis
      // has not seen all allocation sites. In that case, we conservatively return false.
      AppInfoWithSubtyping appInfo = appView.appInfo();
      if (appInfo.hasLiveness() && appInfo.withLiveness().isPinned(subject)) {
        return false;
      }

      // Check that `subject` is guaranteed to be initialized in all instance methods of `context`.
      DexType guaranteedToBeInitializedInContext =
          mapping.getOrDefault(context, appView.dexItemFactory().objectType);
      if (!appInfo.isSubtype(guaranteedToBeInitializedInContext, subject)) {
        return false;
      }

      // Also check that `subject` is not an interface, since interfaces are not initialized
      // transitively.
      DexClass clazz = appView.definitionFor(subject);
      return clazz != null && !clazz.isInterface();
    }
  }

  private final AppView<? extends AppInfoWithSubtyping> appView;

  // If the mapping contains an entry `X -> Y`, then the type Y is guaranteed to be initialized in
  // all instance methods of X.
  private final Map<DexType, DexType> mapping = new IdentityHashMap<>();

  public InitializedClassesInInstanceMethodsAnalysis(
      AppView<? extends AppInfoWithSubtyping> appView) {
    this.appView = appView;
  }

  @Override
  public void processNewlyInstantiatedClass(DexProgramClass clazz, DexEncodedMethod context) {
    DexType key = clazz.type;
    DexType objectType = appView.dexItemFactory().objectType;
    if (context == null) {
      // Record that we don't know anything about the set of classes that are guaranteed to be
      // initialized in the instance methods of `clazz`.
      mapping.put(key, objectType);
      return;
    }

    // Record that the enclosing class is guaranteed to be initialized at the allocation site.
    AppInfoWithSubtyping appInfo = appView.appInfo();
    DexType guaranteedToBeInitialized = context.method.holder;
    DexType existingGuaranteedToBeInitialized =
        mapping.getOrDefault(key, guaranteedToBeInitialized);
    mapping.put(
        key,
        appInfo.computeLeastUpperBoundOfClasses(
            guaranteedToBeInitialized, existingGuaranteedToBeInitialized));
  }

  @Override
  public void done() {
    appView.setInitializedClassesInInstanceMethods(
        new InitializedClassesInInstanceMethods(appView, mapping));
  }
}
