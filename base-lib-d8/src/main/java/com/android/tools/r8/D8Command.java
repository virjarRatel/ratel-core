// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.errors.DexFileOverflowDiagnostic;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.desugar.DesugaredLibraryConfiguration;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * Immutable command structure for an invocation of the {@link D8} compiler.
 *
 * <p>To build a D8 command use the {@link D8Command.Builder} class. For example:
 *
 * <pre>
 *   D8Command command = D8Command.builder()
 *     .addProgramFiles(path1, path2)
 *     .setMode(CompilationMode.RELEASE)
 *     .setOutput(Paths.get("output.zip", OutputMode.DexIndexed))
 *     .build();
 * </pre>
 */
@Keep
public final class D8Command extends BaseCompilerCommand {

  private static class ClasspathInputOrigin extends InputFileOrigin {

    public ClasspathInputOrigin(Path file) {
      super("classpath input", file);
    }
  }

  private static class DefaultD8DiagnosticsHandler implements DiagnosticsHandler {

    @Override
    public void error(Diagnostic error) {
      if (error instanceof DexFileOverflowDiagnostic) {
        DexFileOverflowDiagnostic overflowDiagnostic = (DexFileOverflowDiagnostic) error;
        if (!overflowDiagnostic.hasMainDexSpecification()) {
          DiagnosticsHandler.super.error(
              new StringDiagnostic(
                  overflowDiagnostic.getDiagnosticMessage() + ". Try supplying a main-dex list"));
          return;
        }
      }
      DiagnosticsHandler.super.error(error);
    }
  }

  /**
   * Builder for constructing a D8Command.
   *
   * <p>A builder is obtained by calling {@link D8Command#builder}.
   */
  @Keep
  public static class Builder extends BaseCompilerCommand.Builder<D8Command, Builder> {

    private boolean intermediate = false;
    private DesugarGraphConsumer desugarGraphConsumer = null;
    private StringConsumer desugaredLibraryKeepRuleConsumer = null;

    private Builder() {
      this(new DefaultD8DiagnosticsHandler());
    }

    private Builder(DiagnosticsHandler diagnosticsHandler) {
      super(diagnosticsHandler);
    }

    private Builder(AndroidApp app) {
      super(app);
    }

    /**
     * Add dex program-data.
     */
    @Override
    public Builder addDexProgramData(byte[] data, Origin origin) {
      guard(() -> getAppBuilder().addDexProgramData(data, origin));
      return self();
    }

    /**
     * Add classpath file resources. These have @Override to ensure binary compatibility.
     */
    @Override
    public Builder addClasspathFiles(Path... files) {
      return super.addClasspathFiles(files);
    }

    /**
     * Add classpath file resources.
     */
    @Override
    public Builder addClasspathFiles(Collection<Path> files) {
      return super.addClasspathFiles(files);
    }

    /**
     * Add classfile resources provider for class-path resources.
     */
    @Override
    public Builder addClasspathResourceProvider(ClassFileResourceProvider provider) {
      return super.addClasspathResourceProvider(provider);
    }

    /**
     * Indicate if compilation is to intermediate results, i.e., intended for later merging.
     *
     * <p>Intermediate mode is implied if compiling results to a "file-per-class-file".
     */
    public Builder setIntermediate(boolean value) {
      this.intermediate = value;
      return self();
    }

    /**
     * Set a consumer for receiving the keep rules to use when compiling the desugared library for
     * the program being compiled in this compilation.
     *
     * @param keepRuleConsumer Consumer to receive the content once produced.
     */
    public Builder setDesugaredLibraryKeepRuleConsumer(StringConsumer keepRuleConsumer) {
      this.desugaredLibraryKeepRuleConsumer = keepRuleConsumer;
      return self();
    }

    /**
     * Get the consumer that will receive dependency information for desugaring.
     */
    public DesugarGraphConsumer getDesugarGraphConsumer() {
      return desugarGraphConsumer;
    }

    /**
     * Set the consumer that will receive dependency information for desugaring.
     *
     * <p>Setting the consumer will clear any previously set consumer.
     */
    public Builder setDesugarGraphConsumer(DesugarGraphConsumer desugarGraphConsumer) {
      this.desugarGraphConsumer = desugarGraphConsumer;
      return self();
    }

    @Override
    Builder self() {
      return this;
    }

    @Override
    CompilationMode defaultCompilationMode() {
      return CompilationMode.DEBUG;
    }

    @Override
    void validate() {
      Reporter reporter = getReporter();
      if (getProgramConsumer() instanceof ClassFileConsumer) {
        reporter.error("D8 does not support compiling to Java class files");
      }
      if (getAppBuilder().hasMainDexList()) {
        if (intermediate) {
          reporter.error("Option --main-dex-list cannot be used with --intermediate");
        }
        if (getProgramConsumer() instanceof DexFilePerClassFileConsumer) {
          reporter.error("Option --main-dex-list cannot be used with --file-per-class");
        }
      } else if (getMainDexListConsumer() != null) {
        reporter.error("Option --main-dex-list-output require --main-dex-list");
      }
      if (getMinApiLevel() >= AndroidApiLevel.L.getLevel()) {
        if (getMainDexListConsumer() != null || getAppBuilder().hasMainDexList()) {
          reporter.error(
              "D8 does not support main-dex inputs and outputs when compiling to API level "
                  + AndroidApiLevel.L.getLevel()
                  + " and above");
        }
      }
      if (hasDesugaredLibraryConfiguration() && getDisableDesugaring()) {
        reporter.error("Using desugared library configuration requires desugaring to be enabled");
      }
      super.validate();
    }

    @Override
    D8Command makeCommand() {
      if (isPrintHelp() || isPrintVersion()) {
        return new D8Command(isPrintHelp(), isPrintVersion());
      }

      intermediate |= getProgramConsumer() instanceof DexFilePerClassFileConsumer;

      DexItemFactory factory = new DexItemFactory();
      DesugaredLibraryConfiguration libraryConfiguration =
          getDesugaredLibraryConfiguration(factory, false);

      return new D8Command(
          getAppBuilder().build(),
          getMode(),
          getProgramConsumer(),
          getMainDexListConsumer(),
          getMinApiLevel(),
          getReporter(),
          !getDisableDesugaring(),
          intermediate,
          isOptimizeMultidexForLinearAlloc(),
          getIncludeClassesChecksum(),
          getDexClassChecksumFilter(),
          getDesugarGraphConsumer(),
          desugaredLibraryKeepRuleConsumer,
          libraryConfiguration,
          factory);
    }
  }

  static final String USAGE_MESSAGE = D8CommandParser.USAGE_MESSAGE;

  private final boolean intermediate;
  private final DesugarGraphConsumer desugarGraphConsumer;
  private final StringConsumer desugaredLibraryKeepRuleConsumer;
  private final DesugaredLibraryConfiguration libraryConfiguration;
  private final DexItemFactory factory;

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(DiagnosticsHandler diagnosticsHandler) {
    return new Builder(diagnosticsHandler);
  }

  // Internal builder to start from an existing AndroidApp.
  static Builder builder(AndroidApp app) {
    return new Builder(app);
  }

  /**
   * Parse the D8 command-line.
   *
   * <p>Parsing will set the supplied options or their default value if they have any.
   *
   * @param args Command-line arguments array.
   * @param origin Origin description of the command-line arguments.
   * @return D8 command builder with state set up according to parsed command line.
   */
  public static Builder parse(String[] args, Origin origin) {
    return D8CommandParser.parse(args, origin);
  }

  /**
   * Parse the D8 command-line.
   *
   * <p>Parsing will set the supplied options or their default value if they have any.
   *
   * @param args Command-line arguments array.
   * @param origin Origin description of the command-line arguments.
   * @param handler Custom defined diagnostics handler.
   * @return D8 command builder with state set up according to parsed command line.
   */
  public static Builder parse(String[] args, Origin origin, DiagnosticsHandler handler) {
    return D8CommandParser.parse(args, origin, handler);
  }

  private D8Command(
      AndroidApp inputApp,
      CompilationMode mode,
      ProgramConsumer programConsumer,
      StringConsumer mainDexListConsumer,
      int minApiLevel,
      Reporter diagnosticsHandler,
      boolean enableDesugaring,
      boolean intermediate,
      boolean optimizeMultidexForLinearAlloc,
      boolean encodeChecksum,
      BiPredicate<String, Long> dexClassChecksumFilter,
      DesugarGraphConsumer desugarGraphConsumer,
      StringConsumer desugaredLibraryKeepRuleConsumer,
      DesugaredLibraryConfiguration libraryConfiguration,
      DexItemFactory factory) {
    super(
        inputApp,
        mode,
        programConsumer,
        mainDexListConsumer,
        minApiLevel,
        diagnosticsHandler,
        enableDesugaring,
        optimizeMultidexForLinearAlloc,
        encodeChecksum,
        dexClassChecksumFilter);
    this.intermediate = intermediate;
    this.desugarGraphConsumer = desugarGraphConsumer;
    this.desugaredLibraryKeepRuleConsumer = desugaredLibraryKeepRuleConsumer;
    this.libraryConfiguration = libraryConfiguration;
    this.factory = factory;
  }

  private D8Command(boolean printHelp, boolean printVersion) {
    super(printHelp, printVersion);
    intermediate = false;
    desugarGraphConsumer = null;
    desugaredLibraryKeepRuleConsumer = null;
    libraryConfiguration = null;
    factory = null;
  }

  @Override
  InternalOptions getInternalOptions() {
    InternalOptions internal = new InternalOptions(factory, getReporter());
    assert !internal.debug;
    internal.debug = getMode() == CompilationMode.DEBUG;
    internal.programConsumer = getProgramConsumer();
    internal.mainDexListConsumer = getMainDexListConsumer();
    internal.minimalMainDex = internal.debug;
    internal.minApiLevel = getMinApiLevel();
    internal.intermediate = intermediate;
    internal.readCompileTimeAnnotations = intermediate;
    internal.desugarGraphConsumer = desugarGraphConsumer;

    // Assert and fixup defaults.
    assert !internal.isShrinking();
    assert !internal.isMinifying();
    assert !internal.passthroughDexCode;
    internal.passthroughDexCode = true;
    assert internal.neverMergePrefixes.contains("j$.");

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
    internal.enableDesugaring = getEnableDesugaring();
    internal.encodeChecksums = getIncludeClassesChecksum();
    internal.dexClassChecksumFilter = getDexClassChecksumFilter();
    internal.enableInheritanceClassInDexDistributor = isOptimizeMultidexForLinearAlloc();

    // TODO(134732760): This is still work in progress.
    internal.desugaredLibraryConfiguration = libraryConfiguration;
    internal.desugaredLibraryKeepRuleConsumer = desugaredLibraryKeepRuleConsumer;

    return internal;
  }
}
