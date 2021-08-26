// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

// Summary:
// - Computes all the live nests reachable from Program Classes (Sequential), each time a
//  nest is computed, its processing starts concurrently.
// - Add bridges to be processed by further passes and create the maps
// for the lens (Sequential)
public class R8NestBasedAccessDesugaring extends NestBasedAccessDesugaring {

  private final Map<DexMethod, DexMethod> lensBridges = new IdentityHashMap<>();
  private final Map<DexField, DexMethod> lensGetFieldBridges = new IdentityHashMap<>();
  private final Map<DexField, DexMethod> lensPutFieldBridges = new IdentityHashMap<>();

  public R8NestBasedAccessDesugaring(AppView<?> appView) {
    super(appView);
  }

  public GraphLense run(ExecutorService executorService, DexApplication.Builder<?> appBuilder)
      throws ExecutionException {
    assert !appView.options().canUseNestBasedAccess()
        || appView.options().testing.enableForceNestBasedAccessDesugaringForTest;
    computeAndProcessNestsConcurrently(executorService);
    addDeferredBridgesAndMapMethods();
    clearNestAttributes();
    if (nothingToMap()) {
      return appView.graphLense();
    }
    synthesizeNestConstructor(appBuilder);
    return new NestedPrivateMethodLense(
        appView,
        getNestConstructorType(),
        lensBridges,
        lensGetFieldBridges,
        lensPutFieldBridges,
        appView.graphLense());
  }

  private boolean nothingToMap() {
    return lensBridges.isEmpty() && lensGetFieldBridges.isEmpty() && lensPutFieldBridges.isEmpty();
  }

  private void addDeferredBridgesAndMapMethods() {
    // Here we add the bridges and we fill the lens map.
    // The lens map are different than the original map since
    // they refer DexMethod and not DexEncodedMethod (so they can be long lived without issues),
    // and since they do not require synchronization (they are only read in the lens).
    // We cannot easily do this concurrently since methods are added to classes.
    addDeferredBridgesAndMapMethods(bridges, lensBridges);
    addDeferredBridgesAndMapMethods(getFieldBridges, lensGetFieldBridges);
    addDeferredBridgesAndMapMethods(putFieldBridges, lensPutFieldBridges);
  }

  private <E> void addDeferredBridgesAndMapMethods(
      Map<E, DexEncodedMethod> bridges, Map<E, DexMethod> map) {
    for (Map.Entry<E, DexEncodedMethod> entry : bridges.entrySet()) {
      DexClass holder = definitionFor(entry.getValue().method.holder);
      assert holder != null && holder.isProgramClass();
      holder.asProgramClass().addMethod(entry.getValue());
      map.put(entry.getKey(), entry.getValue().method);
    }
    bridges.clear();
  }

  private void clearNestAttributes() {
    // Clearing nest attributes is required to allow class merging to be performed across nests
    // and to forbid other optimizations to introduce nest based accesses.
    for (DexClass clazz : appView.appInfo().classes()) {
      clazz.clearNestHost();
      clazz.getNestMembersClassAttributes().clear();
    }
  }

  private void computeAndProcessNestsConcurrently(ExecutorService executorService)
      throws ExecutionException {
    Set<DexType> nestHosts = Sets.newIdentityHashSet();
    ;
    List<Future<?>> futures = new ArrayList<>();
    // It is possible that a nest member is on the program path but its nest host
    // is only in the class path (or missing, raising an error).
    // Nests are therefore computed the first time a nest member is met, host or not.
    // The computedNestHosts list is there to avoid processing multiple times the same nest.
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      if (clazz.isInANest()) {
        DexType hostType = clazz.getNestHost();
        if (!nestHosts.contains(hostType)) {
          nestHosts.add(hostType);
          futures.add(asyncProcessNest(clazz, executorService));
        }
      }
    }
    ThreadUtils.awaitFutures(futures);
  }

  // In R8, all classes are processed ahead of time.
  @Override
  protected boolean shouldProcessClassInNest(DexClass clazz, List<DexType> nest) {
    return true;
  }

  @Override
  void reportMissingNestHost(DexClass clazz) {
    if (appView.options().ignoreMissingClasses) {
      appView.options().nestDesugaringWarningMissingNestHost(clazz);
    } else {
      appView.options().errorMissingClassMissingNestHost(clazz);
    }
  }

  @Override
  void reportIncompleteNest(List<DexType> nest) {
    if (appView.options().ignoreMissingClasses) {
      appView.options().nestDesugaringWarningIncompleteNest(nest, appView);
    } else {
      appView.options().errorMissingClassIncompleteNest(nest, appView);
    }
  }
}
