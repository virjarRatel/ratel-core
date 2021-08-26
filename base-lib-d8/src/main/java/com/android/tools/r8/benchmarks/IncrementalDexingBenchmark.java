// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.benchmarks;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.utils.ThreadUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class IncrementalDexingBenchmark {
  private static final int ITERATIONS = 1000;

  public static void compile(ExecutorService executor)
      throws IOException, CompilationFailedException {
    D8.run(
        D8Command.builder()
            .addProgramFiles(Paths.get("build/test/examples/arithmetic.jar"))
            .setMode(CompilationMode.DEBUG)
            .setDisableDesugaring(true)
            .setProgramConsumer(
                new DexIndexedConsumer.ForwardingConsumer(null) {
                  @Override
                  public void accept(
                      int fileIndex,
                      ByteDataView data,
                      Set<String> descriptors,
                      DiagnosticsHandler handler) {
                    if (fileIndex != 0) {
                      throw new RuntimeException("WAT");
                    }
                  }
                })
            .build(),
        executor);
  }

  public static void main(String[] args) throws IOException, CompilationFailedException {
    int threads = Integer.min(Runtime.getRuntime().availableProcessors(), 16) / 2;
    ExecutorService executor = ThreadUtils.getExecutorService(threads);
    try {
      long start = System.nanoTime();
      for (int i = 0; i < ITERATIONS; i++) {
        compile(executor);
      }
      double elapsedMs = (System.nanoTime() - start) / 1000000.0;
      BenchmarkUtils.printRuntimeMilliseconds("IncrementalDexing", elapsedMs);
    } finally {
      executor.shutdown();
    }
  }
}
