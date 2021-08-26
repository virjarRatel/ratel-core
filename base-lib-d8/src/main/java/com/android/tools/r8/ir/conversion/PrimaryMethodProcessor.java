// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.conversion.CallGraph.Node;
import com.android.tools.r8.ir.conversion.CallGraphBuilderBase.CycleEliminator;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Action;
import com.android.tools.r8.utils.IROrdering;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.ThrowingConsumer;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A {@link MethodProcessor} that processes methods in the whole program in a bottom-up manner,
 * i.e., from leaves to roots.
 */
class PrimaryMethodProcessor implements MethodProcessor {

  interface WaveStartAction {

    void notifyWaveStart(Collection<DexEncodedMethod> wave, ExecutorService executorService)
        throws ExecutionException;
  }

  private final CallSiteInformation callSiteInformation;
  private final PostMethodProcessor.Builder postMethodProcessorBuilder;
  private final Deque<Collection<DexEncodedMethod>> waves;
  private Collection<DexEncodedMethod> wave;

  private PrimaryMethodProcessor(
      AppView<AppInfoWithLiveness> appView,
      PostMethodProcessor.Builder postMethodProcessorBuilder,
      CallGraph callGraph) {
    this.callSiteInformation = callGraph.createCallSiteInformation(appView);
    this.postMethodProcessorBuilder = postMethodProcessorBuilder;
    this.waves = createWaves(appView, callGraph, callSiteInformation);
  }

  static PrimaryMethodProcessor create(
      AppView<AppInfoWithLiveness> appView,
      PostMethodProcessor.Builder postMethodProcessorBuilder,
      ExecutorService executorService,
      Timing timing)
      throws ExecutionException {
    CallGraph callGraph = CallGraph.builder(appView).build(executorService, timing);
    return new PrimaryMethodProcessor(appView, postMethodProcessorBuilder, callGraph);
  }

  @Override
  public Phase getPhase() {
    return Phase.PRIMARY;
  }

  @Override
  public CallSiteInformation getCallSiteInformation() {
    return callSiteInformation;
  }

  private Deque<Collection<DexEncodedMethod>> createWaves(
      AppView<?> appView, CallGraph callGraph, CallSiteInformation callSiteInformation) {
    IROrdering shuffle = appView.options().testing.irOrdering;
    Deque<Collection<DexEncodedMethod>> waves = new ArrayDeque<>();

    Set<Node> nodes = callGraph.nodes;
    Set<DexEncodedMethod> reprocessing = Sets.newIdentityHashSet();
    int waveCount = 1;
    while (!nodes.isEmpty()) {
      Set<DexEncodedMethod> wave = Sets.newIdentityHashSet();
      extractLeaves(
          nodes,
          leaf -> {
            wave.add(leaf.method);

            // Reprocess methods that invoke a method with a single call site.
            if (callSiteInformation.hasSingleCallSite(leaf.method.method)) {
              callGraph.cycleEliminationResult.forEachRemovedCaller(
                  leaf, caller -> reprocessing.add(caller.method));
            }
          });
      waves.addLast(shuffle.order(wave));
      if (Log.ENABLED && Log.isLoggingEnabledFor(PrimaryMethodProcessor.class)) {
        Log.info(getClass(), "Wave #%d: %d", waveCount++, wave.size());
      }
    }
    if (!reprocessing.isEmpty()) {
      postMethodProcessorBuilder.put(reprocessing);
    }
    return waves;
  }

  /**
   * Extract the next set of leaves (nodes with an outgoing call degree of 0) if any.
   *
   * <p>All nodes in the graph are extracted if called repeatedly until null is returned. Please
   * note that there are no cycles in this graph (see {@link CycleEliminator#breakCycles}).
   */
  static void extractLeaves(Set<Node> nodes, Consumer<Node> fn) {
    Set<Node> removed = Sets.newIdentityHashSet();
    Iterator<Node> nodeIterator = nodes.iterator();
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.next();
      if (node.isLeaf()) {
        fn.accept(node);
        nodeIterator.remove();
        removed.add(node);
      }
    }
    removed.forEach(Node::cleanCallersForRemoval);
  }

  @Override
  public boolean isProcessedConcurrently(DexEncodedMethod method) {
    return wave != null && wave.contains(method);
  }

  /**
   * Applies the given method to all leaf nodes of the graph.
   *
   * <p>As second parameter, a predicate that can be used to decide whether another method is
   * processed at the same time is passed. This can be used to avoid races in concurrent processing.
   */
  <E extends Exception> void forEachMethod(
      ThrowingConsumer<DexEncodedMethod, E> consumer,
      WaveStartAction waveStartAction,
      Action waveDone,
      ExecutorService executorService)
      throws ExecutionException {
    while (!waves.isEmpty()) {
      wave = waves.removeFirst();
      assert wave.size() > 0;
      waveStartAction.notifyWaveStart(wave, executorService);
      ThreadUtils.processItems(wave, consumer, executorService);
      waveDone.execute();
    }
  }
}
