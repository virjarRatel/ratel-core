// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.utils.ExceptionUtils.unwrapExecutionException;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.desugar.PrefixRewritingMapper;
import com.android.tools.r8.jar.CfApplicationWriter;
import com.android.tools.r8.naming.PrefixRewritingNamingLens;
import com.android.tools.r8.shaking.AnnotationRemover;
import com.android.tools.r8.shaking.L8TreePruner;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * The L8 compiler.
 */
@Keep
public class L8 {

  /**
   * Main API entry for the L8 compiler.
   *
   * @param command L8 command.
   */
  public static void run(L8Command command) throws CompilationFailedException {
    runForTesting(
        command.getInputApp(),
        command.getInternalOptions(),
        command.isShrinking(),
        command.getD8Command(),
        command.getR8Command());
  }

  /**
   * Main API entry for the L8 compiler.
   *
   * @param command L8 command.
   * @param executor executor service from which to get threads for multi-threaded processing.
   */
  public static void run(L8Command command, ExecutorService executor)
      throws CompilationFailedException {
    run(
        command.getInputApp(),
        command.getInternalOptions(),
        command.isShrinking(),
        command.getD8Command(),
        command.getR8Command(),
        executor);
  }

  static void runForTesting(
      AndroidApp app,
      InternalOptions options,
      boolean shrink,
      D8Command d8Command,
      R8Command r8Command)
      throws CompilationFailedException {
    ExecutorService executorService = ThreadUtils.getExecutorService(options);
    run(app, options, shrink, d8Command, r8Command, executorService);
  }

  private static void run(
      AndroidApp app,
      InternalOptions options,
      boolean shrink,
      D8Command d8Command,
      R8Command r8Command,
      ExecutorService executorService)
      throws CompilationFailedException {
    try {
      ExceptionUtils.withD8CompilationHandler(
          options.reporter,
          () -> {
            desugar(app, options, executorService);
          });
      if (shrink) {
        R8.run(r8Command);
      } else {
        D8.run(d8Command, executorService);
      }
    } finally {
      executorService.shutdown();
    }
  }

  private static void desugar(
      AndroidApp inputApp, InternalOptions options, ExecutorService executor) throws IOException {
    Timing timing = new Timing("L8 desugaring");
    try {
      // Disable global optimizations.
      options.disableGlobalOptimizations();
      // Since L8 Cf representation is temporary, just disable long running back-end optimizations
      // on it.
      options.enableLoadStoreOptimization = false;

      DexApplication app = new ApplicationReader(inputApp, options, timing).read(executor);
      PrefixRewritingMapper rewritePrefix =
          options.desugaredLibraryConfiguration.createPrefixRewritingMapper(options.itemFactory);

      app = new L8TreePruner(options).prune(app, rewritePrefix);
      AppInfo appInfo = new AppInfo(app);

      AppView<?> appView = AppView.createForL8(appInfo, options, rewritePrefix);
      IRConverter converter = new IRConverter(appView, timing);

      if (!options.testing.disableL8AnnotationRemoval) {
        AnnotationRemover.clearAnnotations(appView);
      }
      app = converter.convert(app, executor);
      assert appView.appInfo() == appInfo;

      new CfApplicationWriter(
              app,
              appView,
              options,
              options.getMarker(Tool.L8),
              GraphLense.getIdentityLense(),
              PrefixRewritingNamingLens.createPrefixRewritingNamingLens(options, rewritePrefix),
              null)
          .write(options.getClassFileConsumer(), executor);
      options.printWarnings();
    } catch (ExecutionException e) {
      throw unwrapExecutionException(e);
    } finally {
      options.signalFinishedToConsumers();
      // Dump timings.
      if (options.printTimes) {
        timing.report();
      }
    }
  }
}
