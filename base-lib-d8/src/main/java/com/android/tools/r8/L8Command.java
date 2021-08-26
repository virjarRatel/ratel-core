// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.errors.DexFileOverflowDiagnostic;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.desugar.DesugaredLibraryConfiguration;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Pair;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Immutable command structure for an invocation of the {@link L8} library compiler. */
@Keep
public final class L8Command extends BaseCompilerCommand {

  private final D8Command d8Command;
  private final R8Command r8Command;
  private final com.android.tools.r8.ir.desugar.DesugaredLibraryConfiguration libraryConfiguration;
  private final DexItemFactory factory;

  boolean isShrinking() {
    return r8Command != null;
  }

  D8Command getD8Command() {
    return d8Command;
  }

  R8Command getR8Command() {
    return r8Command;
  }

  private L8Command(
      R8Command r8Command,
      D8Command d8Command,
      AndroidApp inputApp,
      CompilationMode mode,
      ProgramConsumer programConsumer,
      StringConsumer mainDexListConsumer,
      int minApiLevel,
      Reporter diagnosticsHandler,
      DesugaredLibraryConfiguration libraryConfiguration,
      DexItemFactory factory) {
    super(
        inputApp,
        mode,
        programConsumer,
        mainDexListConsumer,
        minApiLevel,
        diagnosticsHandler,
        true,
        false,
        false,
        (name, checksum) -> true);
    this.d8Command = d8Command;
    this.r8Command = r8Command;
    this.libraryConfiguration = libraryConfiguration;
    this.factory = factory;
  }

  private L8Command(boolean printHelp, boolean printVersion) {
    super(printHelp, printVersion);
    r8Command = null;
    d8Command = null;
    libraryConfiguration = null;
    factory = null;
  }

  protected static class DefaultL8DiagnosticsHandler implements DiagnosticsHandler {

    @Override
    public void error(Diagnostic error) {
      if (error instanceof DexFileOverflowDiagnostic) {
        DexFileOverflowDiagnostic overflowDiagnostic = (DexFileOverflowDiagnostic) error;
        DiagnosticsHandler.super.error(
            new StringDiagnostic(
                overflowDiagnostic.getDiagnosticMessage()
                    + ". Library too large. L8 can only produce a single .dex file"));
        return;
      }
      DiagnosticsHandler.super.error(error);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(DiagnosticsHandler diagnosticsHandler) {
    return new Builder(diagnosticsHandler);
  }

  @Override
  InternalOptions getInternalOptions() {
    InternalOptions internal = new InternalOptions(factory, getReporter());
    assert !internal.debug;
    internal.debug = getMode() == CompilationMode.DEBUG;
    assert internal.mainDexListConsumer == null;
    assert !internal.minimalMainDex;
    internal.minApiLevel = getMinApiLevel();
    assert !internal.intermediate;
    assert internal.readCompileTimeAnnotations;
    internal.programConsumer = getProgramConsumer();
    assert internal.programConsumer instanceof ClassFileConsumer;

    // Assert and fixup defaults.
    assert !internal.isShrinking();
    assert !internal.isMinifying();
    assert !internal.passthroughDexCode;

    // Assert some of R8 optimizations are disabled.
    assert !internal.enableDynamicTypeOptimization;
    assert !internal.enableInlining;
    assert !internal.enableClassInlining;
    assert !internal.enableHorizontalClassMerging;
    assert !internal.enableVerticalClassMerging;
    assert !internal.enableClassStaticizer;
    assert !internal.enableEnumValueOptimization;
    assert !internal.outline.enabled;
    assert !internal.enableValuePropagation;
    assert !internal.enableLambdaMerging;
    assert !internal.enableTreeShakingOfLibraryMethodOverrides;

    // TODO(b/137168535) Disable non-null tracking for now.
    internal.enableNonNullTracking = false;
    assert internal.enableDesugaring;
    assert internal.enableInheritanceClassInDexDistributor;
    internal.enableInheritanceClassInDexDistributor = false;

    // TODO(134732760): This is still work in progress.
    internal.desugaredLibraryConfiguration = libraryConfiguration;

    return internal;
  }

  /**
   * Builder for constructing a L8Command.
   *
   * <p>A builder is obtained by calling {@link L8Command#builder}.
   */
  @Keep
  public static class Builder extends BaseCompilerCommand.Builder<L8Command, Builder> {

    private final List<Pair<List<String>, Origin>> proguardConfigStrings = new ArrayList<>();
    private final List<Path> proguardConfigFiles = new ArrayList<>();

    private Builder() {
      this(new DefaultL8DiagnosticsHandler());
    }

    private Builder(DiagnosticsHandler diagnosticsHandler) {
      super(diagnosticsHandler);
    }

    public boolean isShrinking() {
      // Answers true if keep rules, even empty, are provided.
      return !proguardConfigStrings.isEmpty() || !proguardConfigFiles.isEmpty();
    }

    @Override
    Builder self() {
      return this;
    }

    @Override
    CompilationMode defaultCompilationMode() {
      return CompilationMode.DEBUG;
    }

    /** Add proguard configuration-file resources. */
    public Builder addProguardConfigurationFiles(Path... paths) {
      Collections.addAll(proguardConfigFiles, paths);
      return self();
    }

    /** Add proguard configuration-file resources. */
    public Builder addProguardConfigurationFiles(List<Path> paths) {
      proguardConfigFiles.addAll(paths);
      return self();
    }

    /** Add proguard configuration. */
    public Builder addProguardConfiguration(List<String> lines, Origin origin) {
      proguardConfigStrings.add(new Pair<>(lines, origin));
      return self();
    }

    @Override
    void validate() {
      Reporter reporter = getReporter();
      if (!hasDesugaredLibraryConfiguration()) {
        reporter.error("L8 requires a desugared library configuration");
      }
      if (getProgramConsumer() instanceof ClassFileConsumer) {
        reporter.error("L8 does not support compiling to class files");
      }
      if (getProgramConsumer() instanceof DexFilePerClassFileConsumer) {
        reporter.error("L8 does not support compiling to dex per class");
      }
      if (getAppBuilder().hasMainDexList()) {
        reporter.error("L8 does not support a main dex list");
      } else if (getMainDexListConsumer() != null) {
        reporter.error("L8 does not support generating a main dex list");
      }
      super.validate();
    }

    @Override
    L8Command makeCommand() {
      if (isPrintHelp() || isPrintVersion()) {
        return new L8Command(isPrintHelp(), isPrintVersion());
      }

      if (getMode() == null) {
        setMode(defaultCompilationMode());
      }

      DexItemFactory factory = new DexItemFactory();
      DesugaredLibraryConfiguration libraryConfiguration =
          getDesugaredLibraryConfiguration(factory, true);

      R8Command r8Command = null;
      D8Command d8Command = null;

      AndroidApp inputs = getAppBuilder().build();
      DesugaredLibrary desugaredLibrary = new DesugaredLibrary();

      if (isShrinking()) {
        R8Command.Builder r8Builder =
            R8Command.builder(getReporter())
                .addProgramResourceProvider(desugaredLibrary)
                .setMinApiLevel(getMinApiLevel())
                .setMode(getMode())
                .setProgramConsumer(getProgramConsumer());
        for (ClassFileResourceProvider libraryResourceProvider :
            inputs.getLibraryResourceProviders()) {
          r8Builder.addLibraryResourceProvider(libraryResourceProvider);
        }
        for (Pair<List<String>, Origin> proguardConfig : proguardConfigStrings) {
          r8Builder.addProguardConfiguration(proguardConfig.getFirst(), proguardConfig.getSecond());
        }
        r8Builder.addProguardConfiguration(
            libraryConfiguration.getExtraKeepRules(), Origin.unknown());
        r8Builder.addProguardConfigurationFiles(proguardConfigFiles);
        r8Command = r8Builder.makeCommand();
      } else {
        D8Command.Builder d8Builder =
            D8Command.builder(getReporter())
                .addProgramResourceProvider(desugaredLibrary)
                .setMinApiLevel(getMinApiLevel())
                .setMode(getMode())
                .setProgramConsumer(getProgramConsumer());
        for (ClassFileResourceProvider libraryResourceProvider :
            inputs.getLibraryResourceProviders()) {
          d8Builder.addLibraryResourceProvider(libraryResourceProvider);
        }
        d8Command = d8Builder.makeCommand();
      }
      return new L8Command(
          r8Command,
          d8Command,
          inputs,
          getMode(),
          desugaredLibrary,
          getMainDexListConsumer(),
          getMinApiLevel(),
          getReporter(),
          libraryConfiguration,
          factory);
    }
  }

  static class DesugaredLibrary implements ClassFileConsumer, ProgramResourceProvider {

    private final List<ProgramResource> resources = new ArrayList<>();

    @Override
    public synchronized void accept(
        ByteDataView data, String descriptor, DiagnosticsHandler handler) {
      // TODO(b/139273544): Map Origin information.
      resources.add(
          ProgramResource.fromBytes(
              Origin.unknown(), Kind.CF, data.copyByteData(), Collections.singleton(descriptor)));
    }

    @Override
    public Collection<ProgramResource> getProgramResources() throws ResourceException {
      return resources;
    }

    @Override
    public void finished(DiagnosticsHandler handler) {}
  }
}
