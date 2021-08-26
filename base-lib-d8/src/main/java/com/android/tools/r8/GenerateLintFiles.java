// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.cf.code.CfConstNull;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProgramClass.ChecksumSupplier;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DirectMappedDexApplication;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.ir.desugar.DesugaredLibraryConfiguration;
import com.android.tools.r8.ir.desugar.DesugaredLibraryConfigurationParser;
import com.android.tools.r8.jar.CfApplicationWriter;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class GenerateLintFiles {

  private static final String ANDROID_JAR_PATTERN = "third_party/android_jar/lib-v%d/android.jar";

  private final DexItemFactory factory = new DexItemFactory();
  private final Reporter reporter = new Reporter();
  private final InternalOptions options = new InternalOptions(factory, reporter);

  private final DesugaredLibraryConfiguration desugaredLibraryConfiguration;
  private final String outputDirectory;

  private final Set<DexMethod> parallelMethods = Sets.newIdentityHashSet();

  private GenerateLintFiles(String desugarConfigurationPath, String outputDirectory) {
    this.desugaredLibraryConfiguration =
        readDesugaredLibraryConfiguration(desugarConfigurationPath);
    this.outputDirectory =
        outputDirectory.endsWith("/") ? outputDirectory : outputDirectory + File.separator;

    DexType streamType = factory.createType(factory.createString("Ljava/util/stream/Stream;"));
    DexMethod parallelMethod =
        factory.createMethod(
            factory.collectionType,
            factory.createProto(streamType),
            factory.createString("parallelStream"));
    parallelMethods.add(parallelMethod);
    DexType baseStreamType =
        factory.createType(factory.createString("Ljava/util/stream/BaseStream;"));
    for (String typePrefix : new String[] {"Base", "Double", "Int", "Long"}) {
      streamType =
          factory.createType(factory.createString("Ljava/util/stream/" + typePrefix + "Stream;"));
      parallelMethod =
          factory.createMethod(
              streamType, factory.createProto(streamType), factory.createString("parallel"));
      parallelMethods.add(parallelMethod);
      // Also filter out the generated bridges for the covariant return type.
      parallelMethod =
          factory.createMethod(
              streamType, factory.createProto(baseStreamType), factory.createString("parallel"));
      parallelMethods.add(parallelMethod);
    }
  }

  private static Path getAndroidJarPath(AndroidApiLevel apiLevel) {
    String jar = String.format(ANDROID_JAR_PATTERN, apiLevel.getLevel());
    return Paths.get(jar);
  }

  private DesugaredLibraryConfiguration readDesugaredLibraryConfiguration(
      String desugarConfigurationPath) {
    return new DesugaredLibraryConfigurationParser(
            factory, reporter, false, AndroidApiLevel.B.getLevel())
        .parse(StringResource.fromFile(Paths.get(desugarConfigurationPath)));
  }

  private CfCode buildEmptyThrowingCfCode(DexMethod method) {
    CfInstruction insn[] = {new CfConstNull(), new CfThrow()};
    return new CfCode(
        method.holder,
        1,
        method.proto.parameters.size() + 1,
        Arrays.asList(insn),
        Collections.emptyList(),
        Collections.emptyList());
  }

  private void addMethodsToHeaderJar(
      DexApplication.Builder builder, DexClass clazz, List<DexEncodedMethod> methods) {
    if (methods.size() == 0) {
      return;
    }

    List<DexEncodedMethod> directMethods = new ArrayList<>();
    List<DexEncodedMethod> virtualMethods = new ArrayList<>();
    for (DexEncodedMethod method : methods) {
      assert method.method.holder == clazz.type;
      CfCode code = null;
      if (!method.accessFlags.isAbstract() /*&& !method.accessFlags.isNative()*/) {
        code = buildEmptyThrowingCfCode(method.method);
      }
      DexEncodedMethod throwingMethod =
          new DexEncodedMethod(
              method.method,
              method.accessFlags,
              DexAnnotationSet.empty(),
              ParameterAnnotationsList.empty(),
              code,
              50);
      if (method.accessFlags.isStatic()) {
        directMethods.add(throwingMethod);
      } else {
        virtualMethods.add(throwingMethod);
      }
    }

    DexEncodedMethod[] directMethodsArray = new DexEncodedMethod[directMethods.size()];
    DexEncodedMethod[] virtualMethodsArray = new DexEncodedMethod[virtualMethods.size()];
    directMethods.toArray(directMethodsArray);
    virtualMethods.toArray(virtualMethodsArray);
    assert !options.encodeChecksums;
    ChecksumSupplier checksumSupplier = DexProgramClass::invalidChecksumRequest;
    DexProgramClass programClass =
        new DexProgramClass(
            clazz.type,
            null,
            Origin.unknown(),
            clazz.accessFlags,
            clazz.superType,
            clazz.interfaces,
            null,
            null,
            Collections.emptyList(),
            null,
            Collections.emptyList(),
            DexAnnotationSet.empty(),
            DexEncodedField.EMPTY_ARRAY,
            DexEncodedField.EMPTY_ARRAY,
            directMethodsArray,
            virtualMethodsArray,
            false,
            checksumSupplier);
    builder.addProgramClass(programClass);
  }

  public static class SupportedMethods {
    public final Set<DexClass> classesWithAllMethodsSupported;
    public final Map<DexClass, List<DexEncodedMethod>> supportedMethods;

    public SupportedMethods(
        Set<DexClass> classesWithAllMethodsSupported,
        Map<DexClass, List<DexEncodedMethod>> supportedMethods) {
      this.classesWithAllMethodsSupported = classesWithAllMethodsSupported;
      this.supportedMethods = supportedMethods;
    }
  }

  private SupportedMethods collectSupportedMethods(
      AndroidApiLevel compilationApiLevel, Predicate<DexEncodedMethod> supported)
      throws IOException, ExecutionException {

    // Read the android.jar for the compilation API level.
    AndroidApp library =
        AndroidApp.builder().addLibraryFiles(getAndroidJarPath(compilationApiLevel)).build();
    DirectMappedDexApplication dexApplication =
        new ApplicationReader(library, options, new Timing()).read().toDirect();

    // Collect all the methods that the library desugar configuration adds support for.
    Set<DexClass> classesWithAllMethodsSupported = Sets.newIdentityHashSet();
    Map<DexClass, List<DexEncodedMethod>> supportedMethods = new LinkedHashMap<>();
    for (DexLibraryClass clazz : dexApplication.libraryClasses()) {
      String className = clazz.toSourceString();
      // All the methods with the rewritten prefix are supported.
      for (String prefix : desugaredLibraryConfiguration.getRewritePrefix().keySet()) {
        if (clazz.accessFlags.isPublic() && className.startsWith(prefix)) {
          boolean allMethodsAddad = true;
          for (DexEncodedMethod method : clazz.methods()) {
            if (supported.test(method)) {
              supportedMethods.computeIfAbsent(clazz, k -> new ArrayList<>()).add(method);
            } else {
              allMethodsAddad = false;
            }
          }
          if (allMethodsAddad) {
            classesWithAllMethodsSupported.add(clazz);
          }
        }
      }

      // All retargeted methods are supported.
      for (DexEncodedMethod method : clazz.methods()) {
        if (desugaredLibraryConfiguration
            .getRetargetCoreLibMember()
            .keySet()
            .contains(method.method.name)) {
          if (desugaredLibraryConfiguration
              .getRetargetCoreLibMember()
              .get(method.method.name)
              .containsKey(clazz.type)) {
            if (supported.test(method)) {
              supportedMethods.computeIfAbsent(clazz, k -> new ArrayList<>()).add(method);
            }
          }
        }
      }

      // All emulated interfaces static and default methods are supported.
      if (desugaredLibraryConfiguration.getEmulateLibraryInterface().containsKey(clazz.type)) {
        assert clazz.isInterface();
        for (DexEncodedMethod method : clazz.methods()) {
          if (!method.isDefaultMethod() && !method.isStatic()) {
            continue;
          }
          if (supported.test(method)) {
            supportedMethods.computeIfAbsent(clazz, k -> new ArrayList<>()).add(method);
          }
        }
      }
    }

    return new SupportedMethods(classesWithAllMethodsSupported, supportedMethods);
  }

  private String lintBaseFileName(
      AndroidApiLevel compilationApiLevel, AndroidApiLevel minApiLevel) {
    return "desugared_apis_" + compilationApiLevel.getLevel() + "_" + minApiLevel.getLevel();
  }

  private Path lintFile(
      AndroidApiLevel compilationApiLevel, AndroidApiLevel minApiLevel, String extension)
      throws Exception {
    Path directory =
        Paths.get(outputDirectory + "compile_api_level_" + compilationApiLevel.getLevel());
    Files.createDirectories(directory);
    return Paths.get(
        directory
            + File.separator
            + lintBaseFileName(compilationApiLevel, minApiLevel)
            + extension);
  }

  private void writeLintFiles(
      AndroidApiLevel compilationApiLevel,
      AndroidApiLevel minApiLevel,
      SupportedMethods supportedMethods)
      throws Exception {
    // Build a plain text file with the desugared APIs.
    List<String> desugaredApisSignatures = new ArrayList<>();

    DexApplication.Builder builder = DexApplication.builder(options, new Timing());
    supportedMethods.supportedMethods.forEach(
        (clazz, methods) -> {
          String classBinaryName =
              DescriptorUtils.getClassBinaryNameFromDescriptor(clazz.type.descriptor.toString());
          if (!supportedMethods.classesWithAllMethodsSupported.contains(clazz)) {
            for (DexEncodedMethod method : methods) {
              desugaredApisSignatures.add(
                  classBinaryName
                      + '#'
                      + method.method.name
                      + method.method.proto.toDescriptorString());
            }
          } else {
            desugaredApisSignatures.add(classBinaryName);
          }

          addMethodsToHeaderJar(builder, clazz, methods);
        });
    DexApplication app = builder.build();

    // Write a plain text file with the desugared APIs.
    desugaredApisSignatures.sort(Comparator.naturalOrder());
    FileUtils.writeTextFile(
        lintFile(compilationApiLevel, minApiLevel, ".txt"), desugaredApisSignatures);

    // Write a header jar with the desugared APIs.
    AppInfo appInfo = new AppInfo(app);
    AppView<?> appView = AppView.createForD8(appInfo, options);
    CfApplicationWriter writer =
        new CfApplicationWriter(
            builder.build(),
            appView,
            options,
            options.getMarker(Tool.L8),
            GraphLense.getIdentityLense(),
            NamingLens.getIdentityLens(),
            null);
    ClassFileConsumer consumer =
        new ClassFileConsumer.ArchiveConsumer(
            lintFile(compilationApiLevel, minApiLevel, FileUtils.JAR_EXTENSION));
    writer.write(consumer, ThreadUtils.getExecutorService(options));
    consumer.finished(options.reporter);
  }

  private void generateLintFiles(
      AndroidApiLevel compilationApiLevel,
      Predicate<AndroidApiLevel> generateForThisMinApiLevel,
      BiPredicate<AndroidApiLevel, DexEncodedMethod> supportedForMinApiLevel)
      throws Exception {
    System.out.print("  - generating for min API:");
    for (AndroidApiLevel minApiLevel : AndroidApiLevel.values()) {
      if (!generateForThisMinApiLevel.test(minApiLevel)) {
        continue;
      }

      System.out.print(" " + minApiLevel);

      SupportedMethods supportedMethods =
          collectSupportedMethods(
              compilationApiLevel, (method -> supportedForMinApiLevel.test(minApiLevel, method)));
      writeLintFiles(compilationApiLevel, minApiLevel, supportedMethods);
    }
    System.out.println();
  }

  private void run() throws Exception {
    // Run over all the API levels that the desugared library can be compiled with.
    for (int apiLevel = AndroidApiLevel.Q.getLevel();
        apiLevel >= desugaredLibraryConfiguration.getRequiredCompilationApiLevel().getLevel();
        apiLevel--) {
      System.out.println("Generating lint files for compile API " + apiLevel);
      generateLintFiles(
          AndroidApiLevel.getAndroidApiLevel(apiLevel),
          minApiLevel -> minApiLevel == AndroidApiLevel.L || minApiLevel == AndroidApiLevel.B,
          (minApiLevel, method) -> {
            assert minApiLevel == AndroidApiLevel.L || minApiLevel == AndroidApiLevel.B;
            if (minApiLevel == AndroidApiLevel.L) {
              return true;
            }
            assert minApiLevel == AndroidApiLevel.B;
            return !parallelMethods.contains(method.method);
          });
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: GenerateLineFiles <desuage configuration> <output directory>");
      System.exit(1);
    }
    new GenerateLintFiles(args[0], args[1]).run();
  }
}
