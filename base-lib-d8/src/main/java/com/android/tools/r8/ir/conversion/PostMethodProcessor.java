// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.IROrdering;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

class PostMethodProcessor implements MethodProcessor {

  private final AppView<AppInfoWithLiveness> appView;
  private final Map<DexEncodedMethod, Collection<CodeOptimization>> methodsMap;
  private final Deque<Collection<DexEncodedMethod>> waves;
  private Collection<DexEncodedMethod> wave;

  private PostMethodProcessor(
      AppView<AppInfoWithLiveness> appView,
      Map<DexEncodedMethod, Collection<CodeOptimization>> methodsMap,
      CallGraph callGraph) {
    this.appView = appView;
    this.methodsMap = methodsMap;
    this.waves = createWaves(appView, callGraph);
  }

  @Override
  public Phase getPhase() {
    return Phase.POST;
  }

  static class Builder {
    private final Collection<CodeOptimization> defaultCodeOptimizations;
    private final Map<DexEncodedMethod, Collection<CodeOptimization>> methodsMap =
        Maps.newIdentityHashMap();

    Builder(Collection<CodeOptimization> defaultCodeOptimizations) {
      this.defaultCodeOptimizations = defaultCodeOptimizations;
    }

    private void put(
        Set<DexEncodedMethod> methodsToRevisit, Collection<CodeOptimization> codeOptimizations) {
      if (codeOptimizations.isEmpty()) {
        // Nothing to conduct.
        return;
      }
      for (DexEncodedMethod method : methodsToRevisit) {
        methodsMap
            .computeIfAbsent(
                method,
                // Optimization order might matter, hence a collection that preserves orderings.
                k -> new LinkedHashSet<>())
            .addAll(codeOptimizations);
      }
    }

    void put(Set<DexEncodedMethod> methodsToRevisit) {
      put(methodsToRevisit, defaultCodeOptimizations);
    }

    void put(PostOptimization postOptimization) {
      Collection<CodeOptimization> codeOptimizations =
          postOptimization.codeOptimizationsForPostProcessing();
      if (codeOptimizations == null) {
        codeOptimizations = defaultCodeOptimizations;
      }
      put(postOptimization.methodsToRevisit(), codeOptimizations);
    }

    PostMethodProcessor build(
        AppView<AppInfoWithLiveness> appView, ExecutorService executorService, Timing timing)
        throws ExecutionException {
      if (methodsMap.keySet().isEmpty()) {
        // Nothing to revisit.
        return null;
      }
      CallGraph callGraph =
          new PartialCallGraphBuilder(appView, methodsMap.keySet())
              .build(executorService, timing);
      return new PostMethodProcessor(appView, methodsMap, callGraph);
    }
  }

  private Deque<Collection<DexEncodedMethod>> createWaves(AppView<?> appView, CallGraph callGraph) {
    IROrdering shuffle = appView.options().testing.irOrdering;
    Deque<Collection<DexEncodedMethod>> waves = new ArrayDeque<>();

    int waveCount = 1;
    while (!callGraph.isEmpty()) {
      Set<DexEncodedMethod> wave = callGraph.extractRoots();
      waves.addLast(shuffle.order(wave));
      if (Log.ENABLED && Log.isLoggingEnabledFor(PostMethodProcessor.class)) {
        Log.info(getClass(), "Wave #%d: %d", waveCount++, wave.size());
      }
    }

    return waves;
  }

  @Override
  public boolean isProcessedConcurrently(DexEncodedMethod method) {
    return wave != null && wave.contains(method);
  }

  void forEachWave(OptimizationFeedback feedback, ExecutorService executorService)
      throws ExecutionException {
    while (!waves.isEmpty()) {
      wave = waves.removeFirst();
      assert wave.size() > 0;
      ThreadUtils.processItems(
          wave,
          method -> {
            Collection<CodeOptimization> codeOptimizations = methodsMap.get(method);
            assert codeOptimizations != null && !codeOptimizations.isEmpty();
            forEachMethod(method, codeOptimizations, feedback);
          },
          executorService);
    }
  }

  private void forEachMethod(
      DexEncodedMethod method,
      Collection<CodeOptimization> codeOptimizations,
      OptimizationFeedback feedback) {
    // TODO(b/140766440): Make IRConverter#process receive a list of CodeOptimization to conduct.
    //   Then, we can share IRCode creation there.
    Origin origin = appView.appInfo().originFor(method.method.holder);
    if (appView.options().skipIR) {
      feedback.markProcessed(method, ConstraintWithTarget.NEVER);
      return;
    }
    IRCode code = method.buildIR(appView, origin);
    if (code == null) {
      feedback.markProcessed(method, ConstraintWithTarget.NEVER);
      return;
    }
    // TODO(b/140768815): Reprocessing may trigger more methods to revisit. Update waves on-the-fly.
    for (CodeOptimization codeOptimization : codeOptimizations) {
      codeOptimization.optimize(appView, code, feedback, this);
    }
  }
}
