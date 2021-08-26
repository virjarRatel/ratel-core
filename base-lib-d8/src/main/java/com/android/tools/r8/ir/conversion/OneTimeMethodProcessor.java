// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.ThrowingConsumer;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * A {@link MethodProcessor} that doesn't persist; rather just processes the given methods one-time,
 * along with a default abstraction of concurrent processing.
 */
public class OneTimeMethodProcessor implements MethodProcessor {

  private Collection<DexEncodedMethod> wave;

  private OneTimeMethodProcessor(Collection<DexEncodedMethod> methodsToProcess) {
    this.wave = methodsToProcess;
  }

  public static OneTimeMethodProcessor getInstance() {
    return new OneTimeMethodProcessor(null);
  }

  static OneTimeMethodProcessor getInstance(Collection<DexEncodedMethod> methodsToProcess) {
    return new OneTimeMethodProcessor(methodsToProcess);
  }

  @Override
  public Phase getPhase() {
    return Phase.ONE_TIME;
  }

  @Override
  public boolean isProcessedConcurrently(DexEncodedMethod method) {
    return wave != null && wave.contains(method);
  }

  <E extends Exception> void forEachWave(
      ThrowingConsumer<DexEncodedMethod, E> consumer,
      ExecutorService executorService)
      throws ExecutionException {
    ThreadUtils.processItems(wave, consumer, executorService);
  }
}
