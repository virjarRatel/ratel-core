// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.D8Command.USAGE_MESSAGE;
import static com.android.tools.r8.utils.ExceptionUtils.unwrapExecutionException;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.analysis.ClassInitializerAssertionEnablingAnalysis;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.desugar.PrefixRewritingMapper;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedbackSimple;
import com.android.tools.r8.naming.PrefixRewritingNamingLens;
import com.android.tools.r8.origin.CommandLineOrigin;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.InternalOptions.AssertionProcessing;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * The D8 dex compiler.
 *
 * <p>D8 performs modular compilation to DEX bytecode. It supports compilation of Java bytecode and
 * Android DEX bytecode to DEX bytecode including merging a mix of these input formats.
 *
 * <p>The D8 dexer API is intentionally limited and should "do the right thing" given a command. If
 * this API does not suffice please contact the D8/R8 team.
 *
 * <p>The compiler is invoked by calling {@link #run(D8Command) D8.run} with an appropriate {@link
 * D8Command}. For example:
 *
 * <pre>
 *   D8.run(D8Command.builder()
 *       .addProgramFiles(inputPathA, inputPathB)
 *       .setOutput(outputPath, OutputMode.DexIndexed)
 *       .build());
 * </pre>
 *
 * The above reads the input files denoted by {@code inputPathA} and {@code inputPathB}, compiles
 * them to DEX bytecode (compiling from Java bytecode for such inputs and merging for DEX inputs),
 * and then writes the result to the directory or zip archive specified by {@code outputPath}.
 */
@Keep
public final class D8 {

  private D8() {}

  /**
   * Main API entry for the D8 dexer.
   *
   * @param command D8 command.
   */
  public static void run(D8Command command) throws CompilationFailedException {
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    ExceptionUtils.withD8CompilationHandler(
        command.getReporter(),
        () -> {
          try {
            run(app, options, executor);
          } finally {
            executor.shutdown();
          }
        });
  }

  /**
   * Main API entry for the D8 dexer with a externally supplied executor service.
   *
   * @param command D8 command.
   * @param executor executor service from which to get threads for multi-threaded processing.
   */
  public static void run(D8Command command, ExecutorService executor)
      throws CompilationFailedException {
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    ExceptionUtils.withD8CompilationHandler(
        command.getReporter(),
        () -> {
          run(app, options, executor);
        });
  }

  private static void run(String[] args) throws CompilationFailedException {
    D8Command command = D8Command.parse(args, CommandLineOrigin.INSTANCE).build();
    if (command.isPrintHelp()) {
      System.out.println(USAGE_MESSAGE);
      return;
    }
    if (command.isPrintVersion()) {
      System.out.println("D8 " + Version.getVersionString());
      return;
    }
    InternalOptions options = command.getInternalOptions();
    AndroidApp app = command.getInputApp();
    runForTesting(app, options);
  }

  /**
   * Command-line entry to D8.
   *
   * See {@link D8Command#USAGE_MESSAGE} or run {@code d8 --help} for usage information.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println(USAGE_MESSAGE);
      System.exit(ExceptionUtils.STATUS_ERROR);
    }
    ExceptionUtils.withMainProgramHandler(() -> run(args));
  }

  static void runForTesting(AndroidApp inputApp, InternalOptions options)
      throws CompilationFailedException {
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    ExceptionUtils.withD8CompilationHandler(
        options.reporter,
        () -> {
          try {
            run(inputApp, options, executor);
          } finally {
            executor.shutdown();
          }
        });
  }

  private static void run(AndroidApp inputApp, InternalOptions options, ExecutorService executor)
      throws IOException {
    Timing timing = new Timing("D8");
    try {
      // Disable global optimizations.
      options.disableGlobalOptimizations();

      DexApplication app = new ApplicationReader(inputApp, options, timing).read(executor);
      PrefixRewritingMapper rewritePrefix =
          options.desugaredLibraryConfiguration.createPrefixRewritingMapper(options.itemFactory);
      AppInfo appInfo = new AppInfo(app);

      final CfgPrinter printer = options.printCfg ? new CfgPrinter() : null;

      if (options.assertionProcessing != AssertionProcessing.LEAVE) {
        // Run analysis to mark all <clinit> methods having the javac generated assertion
        // enabling code.
        ClassInitializerAssertionEnablingAnalysis analysis =
            new ClassInitializerAssertionEnablingAnalysis(
                appInfo.dexItemFactory(), OptimizationFeedbackSimple.getInstance());
        for (DexProgramClass clazz : appInfo.classes()) {
          if (clazz.hasClassInitializer()) {
            analysis.processNewlyLiveMethod(clazz.getClassInitializer());
          }
        }
      }

      IRConverter converter =
          new IRConverter(AppView.createForD8(appInfo, options, rewritePrefix), timing, printer);
      app = converter.convert(app, executor);

      if (options.printCfg) {
        if (options.printCfgFile == null || options.printCfgFile.isEmpty()) {
          System.out.print(printer.toString());
        } else {
          try (OutputStreamWriter writer =
              new OutputStreamWriter(
                  new FileOutputStream(options.printCfgFile), StandardCharsets.UTF_8)) {
            writer.write(printer.toString());
          }
        }
      }

      // Close any internal archive providers now the application is fully processed.
      inputApp.closeInternalArchiveProviders();

      // If a method filter is present don't produce output since the application is likely partial.
      if (options.hasMethodsFilter()) {
        System.out.println("Finished compilation with method filter: ");
        options.methodsFilter.forEach((m) -> System.out.println("  - " + m));
      }

      // Preserve markers from input dex code and add a marker with the current version
      // if there were class file inputs.
      boolean hasClassResources = false;
      for (DexProgramClass dexProgramClass : app.classes()) {
        if (dexProgramClass.originatesFromClassResource()) {
          hasClassResources = true;
          break;
        }
      }
      Marker marker = options.getMarker(Tool.D8);
      Set<Marker> markers = new HashSet<>(app.dexItemFactory.extractMarkers());
      if (marker != null && hasClassResources) {
        markers.add(marker);
      }

      new ApplicationWriter(
              app,
              null,
              options,
              marker == null ? null : ImmutableList.copyOf(markers),
              GraphLense.getIdentityLense(),
              PrefixRewritingNamingLens.createPrefixRewritingNamingLens(options, rewritePrefix),
              null)
          .write(executor);
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

  static DexApplication optimize(
      DexApplication application,
      AppInfo appInfo,
      InternalOptions options,
      Timing timing,
      ExecutorService executor)
      throws IOException, ExecutionException {
    final CfgPrinter printer = options.printCfg ? new CfgPrinter() : null;

    IRConverter converter = new IRConverter(appInfo, options, timing, printer);
    application = converter.convert(application, executor);

    if (options.printCfg) {
      if (options.printCfgFile == null || options.printCfgFile.isEmpty()) {
        System.out.print(printer.toString());
      } else {
        try (OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(options.printCfgFile),
            StandardCharsets.UTF_8)) {
          writer.write(printer.toString());
        }
      }
    }
    return application;
  }
}
