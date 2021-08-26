// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.graph.ClassKind.CLASSPATH;
import static com.android.tools.r8.graph.ClassKind.LIBRARY;
import static com.android.tools.r8.graph.ClassKind.PROGRAM;
import static com.android.tools.r8.utils.ExceptionUtils.unwrapExecutionException;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.ProgramResourceProvider;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.StringResource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexClasspathClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.JarClassFileReader;
import com.android.tools.r8.graph.LazyLoadedDexApplication;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ClassProvider;
import com.android.tools.r8.utils.ClasspathClassCollection;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.DexVersion;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LibraryClassCollection;
import com.android.tools.r8.utils.MainDexList;
import com.android.tools.r8.utils.ProgramClassCollection;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ApplicationReader {

  private final InternalOptions options;
  private final DexItemFactory itemFactory;
  private final Timing timing;
  private final AndroidApp inputApp;

  public interface ProgramClassConflictResolver {
    DexProgramClass resolveClassConflict(DexProgramClass a, DexProgramClass b);
  }

  public ApplicationReader(AndroidApp inputApp, InternalOptions options, Timing timing) {
    this.options = options;
    itemFactory = options.itemFactory;
    this.timing = timing;
    this.inputApp = inputApp;
  }

  public DexApplication read() throws IOException, ExecutionException {
    return read((StringResource) null);
  }

  public DexApplication read(StringResource proguardMap) throws IOException, ExecutionException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      return read(proguardMap, executor);
    } finally {
      executor.shutdown();
    }
  }

  public final DexApplication read(ExecutorService executorService)
      throws IOException, ExecutionException {
    return read(
        null, executorService, ProgramClassCollection.defaultConflictResolver(options.reporter));
  }

  public final DexApplication read(StringResource proguardMap, ExecutorService executorService)
      throws IOException, ExecutionException {
    return read(
        proguardMap,
        executorService,
        ProgramClassCollection.defaultConflictResolver(options.reporter));
  }

  public final DexApplication read(
      StringResource proguardMap,
      ExecutorService executorService,
      ProgramClassConflictResolver resolver)
      throws IOException, ExecutionException {
    assert verifyMainDexOptionsCompatible(inputApp, options);
    Path dumpOutput = null;
    boolean cleanDump = false;
    if (options.dumpInputToFile != null) {
      dumpOutput = Paths.get(options.dumpInputToFile);
    } else if (options.dumpInputToDirectory != null) {
      dumpOutput =
          Paths.get(options.dumpInputToDirectory).resolve("dump" + System.nanoTime() + ".zip");
    } else if (options.testing.dumpAll) {
      cleanDump = true;
      dumpOutput = Paths.get("/tmp").resolve("dump" + System.nanoTime() + ".zip");
    }
    if (dumpOutput != null) {
      timing.begin("ApplicationReader.dump");
      dumpInputToFile(inputApp, dumpOutput, options);
      if (cleanDump) {
        Files.delete(dumpOutput);
      }
      timing.end();
      Diagnostic message = new StringDiagnostic("Dumped compilation inputs to: " + dumpOutput);
      if (options.dumpInputToFile != null) {
        throw options.reporter.fatalError(message);
      } else if (!cleanDump) {
        options.reporter.info(message);
      }
    }
    timing.begin("DexApplication.read");
    final LazyLoadedDexApplication.Builder builder =
        DexApplication.builder(options, timing, resolver);
    try {
      List<Future<?>> futures = new ArrayList<>();
      // Still preload some of the classes, primarily for two reasons:
      // (a) class lazy loading is not supported for DEX files
      //     now and current implementation of parallel DEX file
      //     loading will be lost with on-demand class loading.
      // (b) some of the class file resources don't provide information
      //     about class descriptor.
      // TODO: try and preload less classes.
      readProguardMap(proguardMap, builder, executorService, futures);
      readMainDexList(builder, executorService, futures);
      ClassReader classReader = new ClassReader(executorService, futures);
      JarClassFileReader jcf = classReader.readSources();
      ThreadUtils.awaitFutures(futures);
      classReader.initializeLazyClassCollection(builder);
      for (ProgramResourceProvider provider : inputApp.getProgramResourceProviders()) {
        DataResourceProvider dataResourceProvider = provider.getDataResourceProvider();
        if (dataResourceProvider != null) {
          builder.addDataResourceProvider(dataResourceProvider);
        }
      }
    } catch (ExecutionException e) {
      throw unwrapExecutionException(e);
    } catch (ResourceException e) {
      throw options.reporter.fatalError(new StringDiagnostic(e.getMessage(), e.getOrigin()));
    } finally {
      timing.end();
    }
    return builder.build();
  }

  private static void dumpInputToFile(AndroidApp app, Path output, InternalOptions options) {
    app.dump(output, options.getProguardConfiguration(), options.reporter);
  }

  private static boolean verifyMainDexOptionsCompatible(
      AndroidApp inputApp, InternalOptions options) {
    if (!options.isGeneratingDex()) {
      return true;
    }
    AndroidApiLevel nativeMultiDex = AndroidApiLevel.L;
    if (options.minApiLevel < nativeMultiDex.getLevel()) {
      return true;
    }
    assert options.mainDexKeepRules.isEmpty();
    assert options.mainDexListConsumer == null;
    assert !inputApp.hasMainDexList();
    return true;
  }

  private int validateOrComputeMinApiLevel(int computedMinApiLevel, DexReader dexReader) {
    DexVersion version = dexReader.getDexVersion();
    if (options.minApiLevel == AndroidApiLevel.getDefault().getLevel()) {
      computedMinApiLevel = Math
          .max(computedMinApiLevel, AndroidApiLevel.getMinAndroidApiLevel(version).getLevel());
    } else if (!version
        .matchesApiLevel(AndroidApiLevel.getAndroidApiLevel(options.minApiLevel))) {
      throw new CompilationError("Dex file with version '" + version.getIntValue() +
          "' cannot be used with min sdk level '" + options.minApiLevel + "'.");
    }
    return computedMinApiLevel;
  }

  private void readProguardMap(
      StringResource map,
      DexApplication.Builder<?> builder,
      ExecutorService executorService,
      List<Future<?>> futures) {
    // Read the Proguard mapping file in parallel with DexCode and DexProgramClass items.
    if (map == null) {
      return;
    }
    futures.add(
        executorService.submit(
            () -> {
              try {
                String content = map.getString();
                builder.setProguardMap(ClassNameMapper.mapperFromString(content));
              } catch (IOException | ResourceException e) {
                throw new CompilationError("Failure to read proguard map file", e, map.getOrigin());
              }
            }));
  }

  private void readMainDexList(DexApplication.Builder<?> builder, ExecutorService executorService,
      List<Future<?>> futures) {
    if (inputApp.hasMainDexList()) {
      futures.add(executorService.submit(() -> {
        for (StringResource resource : inputApp.getMainDexListResources()) {
          builder.addToMainDexList(MainDexList.parseList(resource, itemFactory));
        }

        builder.addToMainDexList(
            inputApp.getMainDexClasses()
                .stream()
                .map(clazz -> itemFactory.createType(DescriptorUtils.javaTypeToDescriptor(clazz)))
                .collect(Collectors.toList()));
      }));
    }
  }

  private final class ClassReader {
    private final ExecutorService executorService;
    private final List<Future<?>> futures;

    // We use concurrent queues to collect classes
    // since the classes can be collected concurrently.
    private final Queue<DexProgramClass> programClasses = new ConcurrentLinkedQueue<>();
    private final Queue<DexClasspathClass> classpathClasses = new ConcurrentLinkedQueue<>();
    private final Queue<DexLibraryClass> libraryClasses = new ConcurrentLinkedQueue<>();
    // Jar application reader to share across all class readers.
    private final JarApplicationReader application = new JarApplicationReader(options);

    ClassReader(ExecutorService executorService, List<Future<?>> futures) {
      this.executorService = executorService;
      this.futures = futures;
    }

    private <T extends DexClass> void readDexSources(
        List<ProgramResource> dexSources, ClassKind classKind, Queue<T> classes)
        throws IOException, ResourceException {
      if (dexSources.size() > 0) {
        List<DexParser> dexParsers = new ArrayList<>(dexSources.size());
        int computedMinApiLevel = options.minApiLevel;
        for (ProgramResource input : dexSources) {
          DexReader dexReader = new DexReader(input);
          if (options.passthroughDexCode) {
            computedMinApiLevel = validateOrComputeMinApiLevel(computedMinApiLevel, dexReader);
          }
          dexParsers.add(new DexParser(dexReader, classKind, options));
        }

        options.minApiLevel = computedMinApiLevel;
        for (DexParser dexParser : dexParsers) {
          dexParser.populateIndexTables();
        }
        // Read the DexCode items and DexProgramClass items in parallel.
        if (!options.skipReadingDexCode) {
          for (DexParser dexParser : dexParsers) {
            futures.add(executorService.submit(() -> {
              dexParser.addClassDefsTo(
                  classKind.bridgeConsumer(classes::add)); // Depends on Methods, Code items etc.
            }));
          }
        }
      }
    }

    private <T extends DexClass> JarClassFileReader readClassSources(
        List<ProgramResource> classSources, ClassKind classKind, Queue<T> classes) {
      JarClassFileReader reader = new JarClassFileReader(
          application, classKind.bridgeConsumer(classes::add));
      // Read classes in parallel.
      for (ProgramResource input : classSources) {
        futures.add(
            executorService.submit(
                () -> {
                  try (InputStream is = input.getByteStream()) {
                    reader.read(input.getOrigin(), classKind, is);
                  }
                  // No other way to have a void callable, but we want the IOException from the
                  // previous
                  // line to be wrapped into an ExecutionException.
                  return null;
                }));
      }
      return reader;
    }

    JarClassFileReader readSources() throws IOException, ResourceException {
      Collection<ProgramResource> resources = inputApp.computeAllProgramResources();
      List<ProgramResource> dexResources = new ArrayList<>(resources.size());
      List<ProgramResource> cfResources = new ArrayList<>(resources.size());
      for (ProgramResource resource : resources) {
        if (resource.getKind() == Kind.DEX) {
          dexResources.add(resource);
        } else {
          assert resource.getKind() == Kind.CF;
          cfResources.add(resource);
        }
      }
      readDexSources(dexResources, PROGRAM, programClasses);
      return readClassSources(cfResources, PROGRAM, programClasses);
    }

    private <T extends DexClass> ClassProvider<T> buildClassProvider(ClassKind classKind,
        Queue<T> preloadedClasses, List<ClassFileResourceProvider> resourceProviders,
        JarApplicationReader reader) {
      List<ClassProvider<T>> providers = new ArrayList<>();

      // Preloaded classes.
      if (!preloadedClasses.isEmpty()) {
        providers.add(ClassProvider.forPreloadedClasses(classKind, preloadedClasses));
      }

      // Class file resource providers.
      for (ClassFileResourceProvider provider : resourceProviders) {
        providers.add(ClassProvider.forClassFileResources(classKind, provider, reader));
      }

      // Combine if needed.
      if (providers.isEmpty()) {
        return null;
      }
      return providers.size() == 1 ? providers.get(0)
          : ClassProvider.combine(classKind, providers);
    }

    void initializeLazyClassCollection(LazyLoadedDexApplication.Builder builder) {
      // Add all program classes to the builder.
      for (DexProgramClass clazz : programClasses) {
        builder.addProgramClass(clazz.asProgramClass());
      }

      // Create classpath class collection if needed.
      ClassProvider<DexClasspathClass> classpathClassProvider = buildClassProvider(CLASSPATH,
          classpathClasses, inputApp.getClasspathResourceProviders(), application);
      if (classpathClassProvider != null) {
        builder.setClasspathClassCollection(new ClasspathClassCollection(classpathClassProvider));
      }

      // Create library class collection if needed.
      ClassProvider<DexLibraryClass> libraryClassProvider = buildClassProvider(LIBRARY,
          libraryClasses, inputApp.getLibraryResourceProviders(), application);
      if (libraryClassProvider != null) {
        builder.setLibraryClassCollection(new LibraryClassCollection(libraryClassProvider));
      }
    }
  }
}
