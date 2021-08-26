// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.R8Command.USAGE_MESSAGE;
import static com.android.tools.r8.utils.ExceptionUtils.unwrapExecutionException;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.experimental.graphinfo.GraphConsumer;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppServices;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.AppliedGraphLens;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.analysis.ClassInitializerAssertionEnablingAnalysis;
import com.android.tools.r8.graph.analysis.InitializedClassesInInstanceMethodsAnalysis;
import com.android.tools.r8.ir.analysis.proto.GeneratedExtensionRegistryShrinker;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.desugar.R8NestBasedAccessDesugaring;
import com.android.tools.r8.ir.optimize.EnumInfoMapCollector;
import com.android.tools.r8.ir.optimize.MethodPoolCollection;
import com.android.tools.r8.ir.optimize.NestReducer;
import com.android.tools.r8.ir.optimize.SwitchMapCollector;
import com.android.tools.r8.ir.optimize.UninstantiatedTypeOptimization;
import com.android.tools.r8.ir.optimize.UnusedArgumentsCollector;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedbackSimple;
import com.android.tools.r8.jar.CfApplicationWriter;
import com.android.tools.r8.kotlin.Kotlin;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.Minifier;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.naming.PrefixRewritingNamingLens;
import com.android.tools.r8.naming.ProguardMapMinifier;
import com.android.tools.r8.naming.ProguardMapSupplier;
import com.android.tools.r8.naming.SeedMapper;
import com.android.tools.r8.naming.SourceFileRewriter;
import com.android.tools.r8.naming.signature.GenericSignatureRewriter;
import com.android.tools.r8.optimize.ClassAndMemberPublicizer;
import com.android.tools.r8.optimize.MemberRebindingAnalysis;
import com.android.tools.r8.optimize.VisibilityBridgeRemover;
import com.android.tools.r8.origin.CommandLineOrigin;
import com.android.tools.r8.shaking.AbstractMethodRemover;
import com.android.tools.r8.shaking.AnnotationRemover;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.DefaultTreePrunerConfiguration;
import com.android.tools.r8.shaking.DiscardedChecker;
import com.android.tools.r8.shaking.Enqueuer;
import com.android.tools.r8.shaking.Enqueuer.Mode;
import com.android.tools.r8.shaking.EnqueuerFactory;
import com.android.tools.r8.shaking.MainDexClasses;
import com.android.tools.r8.shaking.MainDexListBuilder;
import com.android.tools.r8.shaking.ProguardClassFilter;
import com.android.tools.r8.shaking.ProguardConfigurationRule;
import com.android.tools.r8.shaking.ProguardConfigurationUtils;
import com.android.tools.r8.shaking.RootSetBuilder;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.shaking.StaticClassMerger;
import com.android.tools.r8.shaking.TreePruner;
import com.android.tools.r8.shaking.TreePrunerConfiguration;
import com.android.tools.r8.shaking.VerticalClassMerger;
import com.android.tools.r8.shaking.WhyAreYouKeepingConsumer;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.CollectionUtils;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.InternalOptions.AssertionProcessing;
import com.android.tools.r8.utils.LineNumberOptimizer;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.SelfRetraceTest;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * The R8 compiler.
 *
 * <p>R8 performs whole-program optimizing compilation of Java bytecode. It supports compilation of
 * Java bytecode to Java bytecode or DEX bytecode. R8 supports tree-shaking the program to remove
 * unneeded code and it supports minification of the program names to reduce the size of the
 * resulting program.
 *
 * <p>The R8 API is intentionally limited and should "do the right thing" given a command. If this
 * API does not suffice please contact the D8/R8 team.
 *
 * <p>R8 supports some configuration using configuration files mostly compatible with the format of
 * the <a href="https://www.guardsquare.com/en/proguard">ProGuard</a> optimizer.
 *
 * <p>The compiler is invoked by calling {@link #run(R8Command) R8.run} with an appropriate {link
 * R8Command}. For example:
 *
 * <pre>
 *   R8.run(R8Command.builder()
 *       .addProgramFiles(inputPathA, inputPathB)
 *       .addLibraryFiles(androidJar)
 *       .setOutput(outputPath, OutputMode.DexIndexed)
 *       .build());
 * </pre>
 *
 * The above reads the input files denoted by {@code inputPathA} and {@code inputPathB}, compiles
 * them to DEX bytecode, using {@code androidJar} as the reference of the system runtime library,
 * and then writes the result to the directory or zip archive specified by {@code outputPath}.
 */
@Keep
public class R8 {

  private final Timing timing;
  private final InternalOptions options;

  private R8(InternalOptions options) {
    this.options = options;
    if (options.printMemory) {
      System.gc();
    }
    this.timing = new Timing("R8", options.printMemory);
    options.itemFactory.resetSortedIndices();
  }

  /**
   * Main API entry for the R8 compiler.
   *
   * <p>The R8 API is intentionally limited and should "do the right thing" given a command. If this
   * API does not suffice please contact the R8 team.
   *
   * @param command R8 command.
   */
  public static void run(R8Command command) throws CompilationFailedException {
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    runForTesting(app, options);
  }

  /**
   * Main API entry for the R8 compiler.
   *
   * <p>The R8 API is intentionally limited and should "do the right thing" given a command. If this
   * API does not suffice please contact the R8 team.
   *
   * @param command R8 command.
   * @param executor executor service from which to get threads for multi-threaded processing.
   */
  public static void run(R8Command command, ExecutorService executor)
      throws CompilationFailedException {
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    ExceptionUtils.withR8CompilationHandler(
        command.getReporter(),
        () -> {
          run(app, options, executor);
        });
  }

  static void writeApplication(
      ExecutorService executorService,
      DexApplication application,
      AppView<?> appView,
      GraphLense graphLense,
      NamingLens namingLens,
      InternalOptions options,
      ProguardMapSupplier proguardMapSupplier)
      throws ExecutionException {
    try {
      Marker marker = options.getMarker(Tool.R8);
      assert marker != null;
      if (options.isGeneratingClassFiles()) {
        new CfApplicationWriter(
                application, appView, options, marker, graphLense, namingLens, proguardMapSupplier)
            .write(options.getClassFileConsumer(), executorService);
      } else {
        new ApplicationWriter(
                application,
                appView,
                options,
                Collections.singletonList(marker),
                graphLense,
                namingLens,
                proguardMapSupplier)
            .write(executorService);
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot write application", e);
    }
  }

  private Set<DexType> filterMissingClasses(Set<DexType> missingClasses,
      ProguardClassFilter dontWarnPatterns) {
    Set<DexType> result = new HashSet<>(missingClasses);
    dontWarnPatterns.filterOutMatches(result);
    return result;
  }

  static void runForTesting(AndroidApp app, InternalOptions options)
      throws CompilationFailedException {
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    ExceptionUtils.withR8CompilationHandler(
        options.reporter,
        () -> {
          try {
            run(app, options, executor);
          } finally {
            executor.shutdown();
          }
        });
  }

  private static void run(AndroidApp app, InternalOptions options, ExecutorService executor)
      throws IOException {
    new R8(options).run(app, executor);
  }

  private void run(AndroidApp inputApp, ExecutorService executorService) throws IOException {
    assert options.programConsumer != null;
    if (options.quiet) {
      System.setOut(new PrintStream(ByteStreams.nullOutputStream()));
    }
    if (this.getClass().desiredAssertionStatus()) {
      options.reporter.info(
          new StringDiagnostic(
              "Running R8 version " + Version.LABEL + " with assertions enabled."));
    }
    try {
      DexApplication application =
          new ApplicationReader(inputApp, options, timing).read(executorService).toDirect();

      // Now that the dex-application is fully loaded, close any internal archive providers.
      inputApp.closeInternalArchiveProviders();

      AppView<AppInfoWithSubtyping> appView =
          AppView.createForR8(new AppInfoWithSubtyping(application), options);
      appView.setAppServices(AppServices.builder(appView).build());

      List<ProguardConfigurationRule> synthesizedProguardRules = new ArrayList<>();
      timing.begin("Strip unused code");
      Set<DexType> classesToRetainInnerClassAttributeFor = null;
      try {
        Set<DexType> missingClasses = appView.appInfo().getMissingClasses();
        missingClasses = filterMissingClasses(
            missingClasses, options.getProguardConfiguration().getDontWarnPatterns());
        if (!missingClasses.isEmpty()) {
          missingClasses.forEach(
              clazz -> {
                options.reporter.warning(
                    new StringDiagnostic("Missing class: " + clazz.toSourceString()));
              });
          if (!options.ignoreMissingClasses) {
            DexType missingClass = missingClasses.iterator().next();
            if (missingClasses.size() == 1) {
              throw new CompilationError(
                  "Compilation can't be completed because the class `"
                      + missingClass.toSourceString()
                      + "` is missing.");
            } else {
              throw new CompilationError(
                  "Compilation can't be completed because `" + missingClass.toSourceString()
                      + "` and " + (missingClasses.size() - 1) + " other classes are missing.");
            }
          }
        }

        // Compute kotlin info before setting the roots and before
        // kotlin metadata annotation is removed.
        computeKotlinInfoForProgramClasses(application, appView);

        // Add synthesized -assumenosideeffects from min api if relevant.
        if (options.isGeneratingDex()) {
          if (!ProguardConfigurationUtils.hasExplicitAssumeValuesOrAssumeNoSideEffectsRuleForMinSdk(
              options.itemFactory, options.getProguardConfiguration().getRules())) {
            synthesizedProguardRules.add(
                ProguardConfigurationUtils.buildAssumeNoSideEffectsRuleForApiLevel(
                    options.itemFactory, AndroidApiLevel.getAndroidApiLevel(options.minApiLevel)));
          }
        }

        appView.setRootSet(
            new RootSetBuilder(
                    appView,
                    application,
                    Iterables.concat(
                        options.getProguardConfiguration().getRules(), synthesizedProguardRules))
                .run(executorService));

        AppView<AppInfoWithLiveness> appViewWithLiveness = runEnqueuer(executorService, appView);
        assert appView.rootSet().verifyKeptFieldsAreAccessedAndLive(appViewWithLiveness.appInfo());
        assert appView.rootSet().verifyKeptMethodsAreTargetedAndLive(appViewWithLiveness.appInfo());
        assert appView.rootSet().verifyKeptTypesAreLive(appViewWithLiveness.appInfo());

        appView.rootSet().checkAllRulesAreUsed(options);
        if (options.proguardSeedsConsumer != null) {
          ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          PrintStream out = new PrintStream(bytes);
          RootSetBuilder.writeSeeds(appView.appInfo().withLiveness(), out, type -> true);
          out.flush();
          ExceptionUtils.withConsumeResourceHandler(
              options.reporter, options.proguardSeedsConsumer, bytes.toString());
          ExceptionUtils.withFinishedResourceHandler(
              options.reporter, options.proguardSeedsConsumer);
        }
        if (options.isShrinking()) {
          // Mark dead proto extensions fields as neither being read nor written. This step must
          // run prior to the tree pruner.
          appView.withGeneratedExtensionRegistryShrinker(
              shrinker -> {
                shrinker.run(Mode.INITIAL_TREE_SHAKING);
              });

          TreePruner pruner = new TreePruner(appViewWithLiveness);
          application = pruner.run(application);

          // Recompute the subtyping information.
          appView.setAppInfo(
              appView
                  .appInfo()
                  .withLiveness()
                  .prunedCopyFrom(
                      application,
                      pruner.getRemovedClasses(),
                      pruner.getMethodsToKeepForConfigurationDebugging()));
          appView.setAppServices(appView.appServices().prunedCopy(pruner.getRemovedClasses()));
          new AbstractMethodRemover(appView.appInfo().withLiveness()).run();
        }

        classesToRetainInnerClassAttributeFor =
            AnnotationRemover.computeClassesToRetainInnerClassAttributeFor(appView.withLiveness());
        new AnnotationRemover(appView.withLiveness(), classesToRetainInnerClassAttributeFor)
            .ensureValid()
            .run();

      } finally {
        timing.end();
      }

      assert appView.appInfo().hasLiveness();
      assert verifyNoJarApplicationReaders(application.classes());
      // Build conservative main dex content after first round of tree shaking. This is used
      // by certain optimizations to avoid introducing additional class references into main dex
      // classes, as that can cause the final number of main dex methods to grow.
      RootSet mainDexRootSet = null;
      MainDexClasses mainDexClasses = MainDexClasses.NONE;
      if (!options.mainDexKeepRules.isEmpty()) {
        assert appView.graphLense().isIdentityLense();
        // Find classes which may have code executed before secondary dex files installation.
        mainDexRootSet =
            new RootSetBuilder(appView, application, options.mainDexKeepRules).run(executorService);
        // Live types is the tracing result.
        Set<DexProgramClass> mainDexBaseClasses =
            EnqueuerFactory.createForMainDexTracing(appView)
                .traceMainDex(mainDexRootSet, executorService, timing);
        // Calculate the automatic main dex list according to legacy multidex constraints.
        mainDexClasses = new MainDexListBuilder(mainDexBaseClasses, application).run();
        appView.appInfo().unsetObsolete();
      }

      // The class type lattice elements include information about the interfaces that a class
      // implements. This information can change as a result of vertical class merging, so we need
      // to clear the cache, so that we will recompute the type lattice elements.
      appView.dexItemFactory().clearTypeLatticeElementsCache();

      if (options.getProguardConfiguration().isAccessModificationAllowed()) {
        GraphLense publicizedLense =
            ClassAndMemberPublicizer.run(
                executorService, timing, application, appView.withLiveness());
        boolean changed = appView.setGraphLense(publicizedLense);
        if (changed) {
          // We can now remove visibility bridges. Note that we do not need to update the
          // invoke-targets here, as the existing invokes will simply dispatch to the now
          // visible super-method. MemberRebinding, if run, will then dispatch it correctly.
          new VisibilityBridgeRemover(appView.withLiveness()).run();
        }
      }

      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      appView.setGraphLense(new MemberRebindingAnalysis(appViewWithLiveness).run());
      if (options.shouldDesugarNests()) {
        timing.begin("NestBasedAccessDesugaring");
        R8NestBasedAccessDesugaring analyzer = new R8NestBasedAccessDesugaring(appViewWithLiveness);
        boolean changed =
            appView.setGraphLense(analyzer.run(executorService, application.builder()));
        if (changed) {
          appViewWithLiveness.setAppInfo(
              appViewWithLiveness
                  .appInfo()
                  .rewrittenWithLense(application.asDirect(), appView.graphLense()));
        }
        timing.end();
      } else {
        timing.begin("NestReduction");
        // This pass attempts to reduce the number of nests and nest size
        // to allow further passes, specifically the class mergers, to do
        // a better job. This pass is better run before the class merger
        // but after the publicizer (cannot be run as part of the Enqueuer).
        new NestReducer(appViewWithLiveness).run(executorService);
        timing.end();
      }
      if (options.enableHorizontalClassMerging) {
        timing.begin("HorizontalStaticClassMerger");
        StaticClassMerger staticClassMerger =
            new StaticClassMerger(appViewWithLiveness, options, mainDexClasses);
        boolean changed = appView.setGraphLense(staticClassMerger.run());
        if (changed) {
          appViewWithLiveness.setAppInfo(
              appViewWithLiveness
                  .appInfo()
                  .rewrittenWithLense(application.asDirect(), appView.graphLense()));
        }
        timing.end();
      }
      if (options.enableVerticalClassMerging) {
        timing.begin("VerticalClassMerger");
        VerticalClassMerger verticalClassMerger =
            new VerticalClassMerger(
                application, appViewWithLiveness, executorService, timing, mainDexClasses);
        boolean changed = appView.setGraphLense(verticalClassMerger.run());
        if (changed) {
          appView.setVerticallyMergedClasses(verticalClassMerger.getMergedClasses());
          application = application.asDirect().rewrittenWithLense(appView.graphLense());
          appViewWithLiveness.setAppInfo(
              appViewWithLiveness
                  .appInfo()
                  .rewrittenWithLense(application.asDirect(), appView.graphLense()));
        }
        timing.end();
      }
      if (options.enableArgumentRemoval) {
        if (options.enableUnusedArgumentRemoval) {
          timing.begin("UnusedArgumentRemoval");
          boolean changed =
              appView.setGraphLense(
                  new UnusedArgumentsCollector(
                          appViewWithLiveness, new MethodPoolCollection(appView))
                      .run(executorService, timing));
          if (changed) {
            application = application.asDirect().rewrittenWithLense(appView.graphLense());
            appViewWithLiveness.setAppInfo(
                appViewWithLiveness
                    .appInfo()
                    .rewrittenWithLense(application.asDirect(), appView.graphLense()));
          }
          timing.end();
        }
        if (options.enableUninstantiatedTypeOptimization) {
          timing.begin("UninstantiatedTypeOptimization");
          boolean changed =
              appView.setGraphLense(
                  new UninstantiatedTypeOptimization(appViewWithLiveness)
                      .run(new MethodPoolCollection(appView), executorService, timing));
          if (changed) {
            application = application.asDirect().rewrittenWithLense(appView.graphLense());
            appViewWithLiveness.setAppInfo(
                appViewWithLiveness
                    .appInfo()
                    .rewrittenWithLense(application.asDirect(), appView.graphLense()));
          }
          timing.end();
        }
      }

      // None of the optimizations above should lead to the creation of type lattice elements.
      assert appView.dexItemFactory().verifyNoCachedTypeLatticeElements();

      // Collect switch maps and ordinals maps.
      if (options.enableEnumValueOptimization) {
        appViewWithLiveness.setAppInfo(new SwitchMapCollector(appViewWithLiveness).run());
        appViewWithLiveness.setAppInfo(new EnumInfoMapCollector(appViewWithLiveness).run());
      }

      appView.setAppServices(appView.appServices().rewrittenWithLens(appView.graphLense()));

      timing.begin("Create IR");
      Map<String, String> additionalRewritePrefix;
      Set<DexCallSite> desugaredCallSites;
      CfgPrinter printer = options.printCfg ? new CfgPrinter() : null;
      try {
        IRConverter converter = new IRConverter(appView, timing, printer, mainDexClasses);
        application = converter.optimize(executorService);
        desugaredCallSites = converter.getDesugaredCallSites();
      } finally {
        timing.end();
      }

      // Clear the reference type lattice element cache to reduce memory pressure.
      appView.dexItemFactory().clearTypeLatticeElementsCache();

      // At this point all code has been mapped according to the graph lens. We cannot remove the
      // graph lens entirely, though, since it is needed for mapping all field and method signatures
      // back to the original program.
      timing.begin("AppliedGraphLens construction");
      appView.setGraphLense(new AppliedGraphLens(appView, application.classes()));
      timing.end();

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

      // Overwrite SourceFile if specified. This step should be done after IR conversion.
      timing.begin("Rename SourceFile");
      new SourceFileRewriter(appView).run();
      timing.end();

      // Collect the already pruned types before creating a new app info without liveness.
      Set<DexType> prunedTypes = appView.withLiveness().appInfo().getPrunedTypes();

      if (!options.mainDexKeepRules.isEmpty()) {
        appView.setAppInfo(new AppInfoWithSubtyping(application));
        // No need to build a new main dex root set
        assert mainDexRootSet != null;
        GraphConsumer mainDexKeptGraphConsumer = options.mainDexKeptGraphConsumer;
        WhyAreYouKeepingConsumer whyAreYouKeepingConsumer = null;
        if (!mainDexRootSet.reasonAsked.isEmpty()) {
          whyAreYouKeepingConsumer = new WhyAreYouKeepingConsumer(mainDexKeptGraphConsumer);
          mainDexKeptGraphConsumer = whyAreYouKeepingConsumer;
        }

        Enqueuer enqueuer =
            EnqueuerFactory.createForMainDexTracing(appView, mainDexKeptGraphConsumer);
        // Find classes which may have code executed before secondary dex files installation.
        // Live types is the tracing result.
        Set<DexProgramClass> mainDexBaseClasses =
            enqueuer.traceMainDex(mainDexRootSet, executorService, timing);
        // Calculate the automatic main dex list according to legacy multidex constraints.
        mainDexClasses = new MainDexListBuilder(mainDexBaseClasses, application).run();
        final MainDexClasses finalMainDexClasses = mainDexClasses;

        processWhyAreYouKeepingAndCheckDiscarded(
            mainDexRootSet,
            () -> {
              ArrayList<DexProgramClass> classes = new ArrayList<>();
              // TODO(b/131668850): This is not a deterministic order!
              finalMainDexClasses
                  .getClasses()
                  .forEach(
                      type -> {
                        DexClass clazz = appView.definitionFor(type);
                        assert clazz.isProgramClass();
                        classes.add(clazz.asProgramClass());
                      });
              return classes;
            },
            whyAreYouKeepingConsumer,
            appView,
            enqueuer,
            true,
            options,
            timing,
            executorService);
      }

      appView.setAppInfo(new AppInfoWithSubtyping(application));

      if (options.isShrinking()
          || options.isMinifying()
          || options.getProguardConfiguration().hasApplyMappingFile()) {
        timing.begin("Post optimization code stripping");
        try {
          GraphConsumer keptGraphConsumer = null;
          WhyAreYouKeepingConsumer whyAreYouKeepingConsumer = null;
          if (options.isShrinking()) {
            keptGraphConsumer = options.keptGraphConsumer;
            if (!appView.rootSet().reasonAsked.isEmpty()) {
              whyAreYouKeepingConsumer = new WhyAreYouKeepingConsumer(keptGraphConsumer);
              keptGraphConsumer = whyAreYouKeepingConsumer;
            }
          }

          Enqueuer enqueuer = EnqueuerFactory.createForFinalTreeShaking(appView, keptGraphConsumer);
          appView.setAppInfo(
              enqueuer.traceApplication(
                  appView.rootSet(),
                  options.getProguardConfiguration().getDontWarnPatterns(),
                  executorService,
                  timing));

          if (Log.ENABLED && Log.isLoggingEnabledFor(GeneratedExtensionRegistryShrinker.class)) {
            appView.withGeneratedExtensionRegistryShrinker(
                GeneratedExtensionRegistryShrinker::logRemainingProtoExtensionFields);
          }

          if (options.isShrinking()) {
            // Mark dead proto extensions fields as neither being read nor written. This step must
            // run prior to the tree pruner.
            TreePrunerConfiguration treePrunerConfiguration =
                appView.withGeneratedExtensionRegistryShrinker(
                    shrinker -> shrinker.run(enqueuer.getMode()),
                    DefaultTreePrunerConfiguration.getInstance());

            TreePruner pruner = new TreePruner(appViewWithLiveness, treePrunerConfiguration);
            application = pruner.run(application);
            if (options.usageInformationConsumer != null) {
              ExceptionUtils.withFinishedResourceHandler(
                  options.reporter, options.usageInformationConsumer);
            }
            appViewWithLiveness.setAppInfo(
                appViewWithLiveness
                    .appInfo()
                    .prunedCopyFrom(
                        application,
                        CollectionUtils.mergeSets(prunedTypes, pruner.getRemovedClasses()),
                        pruner.getMethodsToKeepForConfigurationDebugging()));
            appView.setAppServices(appView.appServices().prunedCopy(pruner.getRemovedClasses()));

            // TODO(b/130721661): Enable this assert.
            // assert Inliner.verifyNoMethodsInlinedDueToSingleCallSite(appView);

            assert appView.allMergedClasses().verifyAllSourcesPruned(appViewWithLiveness);

            processWhyAreYouKeepingAndCheckDiscarded(
                appView.rootSet(),
                () -> appView.appInfo().app().classesWithDeterministicOrder(),
                whyAreYouKeepingConsumer,
                appView,
                enqueuer,
                false,
                options,
                timing,
                executorService);

            // Remove annotations that refer to types that no longer exist.
            assert classesToRetainInnerClassAttributeFor != null;
            new AnnotationRemover(appView.withLiveness(), classesToRetainInnerClassAttributeFor)
                .run();
            if (!mainDexClasses.isEmpty()) {
              // Remove types that no longer exists from the computed main dex list.
              mainDexClasses = mainDexClasses.prunedCopy(appView.appInfo().withLiveness());
            }
          }
        } finally {
          timing.end();
        }

        if (appView.options().protoShrinking().isProtoShrinkingEnabled()) {
          IRConverter converter = new IRConverter(appView, timing, null, mainDexClasses);

          // If proto shrinking is enabled, we need to reprocess every dynamicMethod(). This ensures
          // that proto fields that have been removed by the second round of tree shaking are also
          // removed from the proto schemas in the bytecode.
          // TODO(b/112437944): Avoid iterating the entire application to post-process every
          //  dynamicMethod() method.
          appView.withGeneratedMessageLiteShrinker(
              shrinker -> shrinker.postOptimizeDynamicMethods(converter));

          // If proto shrinking is enabled, we need to post-process every
          // findLiteExtensionByNumber() method. This ensures that there are no references to dead
          // extensions that have been removed by the second round of tree shaking.
          // TODO(b/112437944): Avoid iterating the entire application to post-process every
          //  findLiteExtensionByNumber() method.
          appView.withGeneratedExtensionRegistryShrinker(
              shrinker -> shrinker.postOptimizeGeneratedExtensionRegistry(converter));
        }
      }

      // Add automatic main dex classes to an eventual manual list of classes.
      if (!options.mainDexKeepRules.isEmpty()) {
        application = application.builder().addToMainDexList(mainDexClasses.getClasses()).build();
      }

      // Perform minification.
      NamingLens namingLens;
      if (options.getProguardConfiguration().hasApplyMappingFile()) {
        SeedMapper seedMapper =
            SeedMapper.seedMapperFromFile(
                options.reporter, options.getProguardConfiguration().getApplyMappingFile());
        timing.begin("apply-mapping");
        namingLens =
            new ProguardMapMinifier(appView.withLiveness(), seedMapper, desugaredCallSites)
                .run(executorService, timing);
        timing.end();
      } else if (options.isMinifying()) {
        timing.begin("Minification");
        namingLens =
            new Minifier(appView.withLiveness(), desugaredCallSites).run(executorService, timing);
        timing.end();
      } else {
        // Rewrite signature annotations for applications that are not minified.
        if (appView.appInfo().hasLiveness()) {
          // TODO(b/124726014): Rewrite signature annotations in lens rewriting instead of here?
          new GenericSignatureRewriter(appView.withLiveness()).run(appView.appInfo().classes());
        }
        namingLens = NamingLens.getIdentityLens();
      }

      ProguardMapSupplier proguardMapSupplier;

      timing.begin("Line number remapping");
      // When line number optimization is turned off the identity mapping for line numbers is
      // used. We still run the line number optimizer to collect line numbers and inline frame
      // information for the mapping file.
      ClassNameMapper classNameMapper = LineNumberOptimizer.run(appView, application, namingLens);
      timing.end();
      proguardMapSupplier = ProguardMapSupplier.fromClassNameMapper(classNameMapper, options);

      // If a method filter is present don't produce output since the application is likely partial.
      if (options.hasMethodsFilter()) {
        System.out.println("Finished compilation with method filter: ");
        options.methodsFilter.forEach(m -> System.out.println("  - " + m));
        return;
      }

      // Remove unneeded visibility bridges that have been inserted for member rebinding.
      // This can only be done if we have AppInfoWithLiveness.
      if (appView.appInfo().hasLiveness()) {
        ImmutableSet.Builder<DexMethod> unneededVisibilityBridgeMethods = ImmutableSet.builder();
        new VisibilityBridgeRemover(
                appView.withLiveness(),
                unneededVisibilityBridgeMethod ->
                    unneededVisibilityBridgeMethods.add(unneededVisibilityBridgeMethod.method))
            .run();
        appView.setUnneededVisibilityBridgeMethods(unneededVisibilityBridgeMethods.build());
      } else {
        // If we don't have AppInfoWithLiveness here, it must be because we are not shrinking. When
        // we are not shrinking, we can't move visibility bridges. In principle, though, it would be
        // possible to remove visibility bridges that have been synthesized by R8, but we currently
        // do not have this information.
        assert !options.isShrinking();
      }

      // Validity checks.
      assert application.classes().stream().allMatch(clazz -> clazz.isValid(options));
      if (options.isShrinking()
          || options.isMinifying()
          || options.getProguardConfiguration().hasApplyMappingFile()) {
        assert appView.rootSet().verifyKeptItemsAreKept(application, appView.appInfo());
      }
      assert appView
          .graphLense()
          .verifyMappingToOriginalProgram(
              application.classesWithDeterministicOrder(),
              new ApplicationReader(inputApp.withoutMainDexList(), options, timing)
                  .read(executorService),
              appView.dexItemFactory());

      // Report synthetic rules (only for testing).
      // TODO(b/120959039): Move this to being reported through the graph consumer.
      if (options.syntheticProguardRulesConsumer != null) {
        options.syntheticProguardRulesConsumer.accept(synthesizedProguardRules);
      }

      // Generate the resulting application resources.
      writeApplication(
          executorService,
          application,
          appView,
          appView.graphLense(),
          PrefixRewritingNamingLens.createPrefixRewritingNamingLens(
              options, appView.rewritePrefix, namingLens),
          options,
          proguardMapSupplier);

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

  private AppView<AppInfoWithLiveness> runEnqueuer(ExecutorService executorService,
      AppView<AppInfoWithSubtyping> appView) throws ExecutionException {
    Enqueuer enqueuer = EnqueuerFactory.createForInitialTreeShaking(appView);

    if (appView.options().enableInitializedClassesInInstanceMethodsAnalysis) {
      enqueuer.registerAnalysis(new InitializedClassesInInstanceMethodsAnalysis(appView));
    }
    if (appView.options().assertionProcessing != AssertionProcessing.LEAVE) {
      enqueuer.registerAnalysis(
          new ClassInitializerAssertionEnablingAnalysis(
              appView.dexItemFactory(), OptimizationFeedbackSimple.getInstance()));
    }

    return appView.setAppInfo(
        enqueuer.traceApplication(
            appView.rootSet(),
            options.getProguardConfiguration().getDontWarnPatterns(),
            executorService,
            timing));
  }

  static void processWhyAreYouKeepingAndCheckDiscarded(
      RootSet rootSet,
      Supplier<Iterable<DexProgramClass>> classes,
      WhyAreYouKeepingConsumer whyAreYouKeepingConsumer,
      AppView<? extends AppInfoWithSubtyping> appView,
      Enqueuer enqueuer,
      boolean forMainDex,
      InternalOptions options,
      Timing timing,
      ExecutorService executorService)
      throws ExecutionException {
    if (whyAreYouKeepingConsumer != null) {
      for (DexReference reference : rootSet.reasonAsked) {
        whyAreYouKeepingConsumer.printWhyAreYouKeeping(
            enqueuer.getGraphReporter().getGraphNode(reference), System.out);
      }
    }
    if (rootSet.checkDiscarded.isEmpty()
        || appView.options().testing.dontReportFailingCheckDiscarded) {
      return;
    }
    List<DexDefinition> failed = new DiscardedChecker(rootSet, classes.get()).run();
    if (failed.isEmpty()) {
      return;
    }
    // If there is no kept-graph info, re-run the enqueueing to compute it.
    if (whyAreYouKeepingConsumer == null) {
      whyAreYouKeepingConsumer = new WhyAreYouKeepingConsumer(null);
      if (forMainDex) {
        enqueuer = EnqueuerFactory.createForMainDexTracing(appView, whyAreYouKeepingConsumer);
        enqueuer.traceMainDex(rootSet, executorService, timing);
      } else {
        enqueuer = EnqueuerFactory.createForWhyAreYouKeeping(appView, whyAreYouKeepingConsumer);
        enqueuer.traceApplication(
            rootSet,
            options.getProguardConfiguration().getDontWarnPatterns(),
            executorService,
            timing);
      }
    }
    for (DexDefinition definition : failed) {
      if (!failed.isEmpty()) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        whyAreYouKeepingConsumer.printWhyAreYouKeeping(
            enqueuer.getGraphReporter().getGraphNode(definition.toReference()),
            new PrintStream(baos));
        options.reporter.info(
            new StringDiagnostic(
                "Item " + definition.toSourceString() + " was not discarded.\n" + baos.toString()));
      }
    }
    throw new CompilationError("Discard checks failed.");
  }

  private void computeKotlinInfoForProgramClasses(DexApplication application, AppView<?> appView) {
    if (appView.options().kotlinOptimizationOptions().disableKotlinSpecificOptimizations) {
      return;
    }
    Kotlin kotlin = appView.dexItemFactory().kotlin;
    Reporter reporter = options.reporter;
    for (DexProgramClass programClass : application.classes()) {
      programClass.setKotlinInfo(kotlin.getKotlinInfo(programClass, reporter));
    }
  }

  private static boolean verifyNoJarApplicationReaders(List<DexProgramClass> classes) {
    for (DexProgramClass clazz : classes) {
      for (DexEncodedMethod method : clazz.methods()) {
        if (method.getCode() != null) {
          assert method.getCode().verifyNoInputReaders();
        }
      }
    }
    return true;
  }

  private static void run(String[] args) throws CompilationFailedException {
    R8Command command = R8Command.parse(args, CommandLineOrigin.INSTANCE).build();
    if (command.isPrintHelp()) {
      SelfRetraceTest.test();
      System.out.println(USAGE_MESSAGE);
      return;
    }
    if (command.isPrintVersion()) {
      System.out.println("R8 " + Version.getVersionString());
      return;
    }
    InternalOptions options = command.getInternalOptions();
    ExecutorService executorService = ThreadUtils.getExecutorService(options);
    try {
      ExceptionUtils.withR8CompilationHandler(options.reporter, () ->
          run(command.getInputApp(), options, executorService));
    } finally {
      executorService.shutdown();
    }
  }

  /**
   * Command-line entry to R8.
   *
   * See {@link R8Command#USAGE_MESSAGE} or run {@code r8 --help} for usage information.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println(USAGE_MESSAGE);
      System.exit(ExceptionUtils.STATUS_ERROR);
    }
    ExceptionUtils.withMainProgramHandler(() -> run(args));
  }
}
