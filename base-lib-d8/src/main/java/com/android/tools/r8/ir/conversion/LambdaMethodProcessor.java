// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Action;
import com.android.tools.r8.utils.IROrdering;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.ThrowingConsumer;
import com.android.tools.r8.utils.Timing;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

class LambdaMethodProcessor implements MethodProcessor {

  private final Deque<Collection<DexEncodedMethod>> waves;
  private Collection<DexEncodedMethod> wave;

  LambdaMethodProcessor(
      AppView<AppInfoWithLiveness> appView,
      Set<DexEncodedMethod> methods,
      ExecutorService executorService,
      Timing timing)
      throws ExecutionException {
    CallGraph callGraph =
        new PartialCallGraphBuilder(appView, methods).build(executorService, timing);
    this.waves = createWaves(appView, callGraph);
  }

  @Override
  public Phase getPhase() {
    return Phase.LAMBDA_PROCESSING;
  }

  private Deque<Collection<DexEncodedMethod>> createWaves(AppView<?> appView, CallGraph callGraph) {
    IROrdering shuffle = appView.options().testing.irOrdering;
    Deque<Collection<DexEncodedMethod>> waves = new ArrayDeque<>();
    while (!callGraph.isEmpty()) {
      waves.addLast(shuffle.order(callGraph.extractLeaves()));
    }
    return waves;
  }

  @Override
  public boolean isProcessedConcurrently(DexEncodedMethod method) {
    return wave.contains(method);
  }

  <E extends Exception> void forEachMethod(
      ThrowingConsumer<DexEncodedMethod, E> consumer,
      Action waveDone,
      ExecutorService executorService)
      throws ExecutionException {
    while (!waves.isEmpty()) {
      wave = waves.removeFirst();
      assert wave.size() > 0;
      ThreadUtils.processItems(wave, consumer, executorService);
      waveDone.execute();
    }
  }
}
