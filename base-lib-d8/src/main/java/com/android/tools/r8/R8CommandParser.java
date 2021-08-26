// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.FlagFile;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class R8CommandParser extends BaseCompilerCommandParser {

  private static final Set<String> OPTIONS_WITH_PARAMETER =
      ImmutableSet.of(
          "--output",
          "--lib",
          "--classpath",
          "--min-api",
          "--main-dex-rules",
          "--main-dex-list",
          "--main-dex-list-output",
          "--pg-conf",
          "--pg-map-output");

  public static void main(String[] args) throws CompilationFailedException {
    R8Command command = parse(args, Origin.root()).build();
    if (command.isPrintHelp()) {
      System.out.println(USAGE_MESSAGE);
      System.exit(1);
    }
    R8.run(command);
  }

  // Internal state to verify parsing properties not enforced by the builder.
  private static class ParseState {
    CompilationMode mode = null;
    OutputMode outputMode = null;
    Path outputPath = null;
    boolean hasDefinedApiLevel = false;
    private boolean includeDataResources = true;
  }

  static final String USAGE_MESSAGE =
      String.join(
          "\n",
          Arrays.asList(
              "Usage: r8 [options] <input-files>",
              " where <input-files> are any combination of dex, class, zip, jar, or apk files",
              " and options are:",
              "  --release                # Compile without debugging information (default).",
              "  --debug                  # Compile with debugging information.",
              "  --dex                    # Compile program to DEX file format (default).",
              "  --classfile              # Compile program to Java classfile format.",
              "  --output <file>          # Output result in <file>.",
              "                           # <file> must be an existing directory or a zip file.",
              "  --lib <file>             # Add <file> as a library resource.",
              "  --classpath <file>       # Add <file> as a classpath resource.",
              "  --min-api <number>       # Minimum Android API level compatibility, default: "
                  + AndroidApiLevel.getDefault().getLevel()
                  + ".",
              "  --pg-conf <file>         # Proguard configuration <file>.",
              "  --pg-map-output <file>   # Output the resulting name and line mapping to <file>.",
              "  --no-tree-shaking        # Force disable tree shaking of unreachable classes.",
              "  --no-minification        # Force disable minification of names.",
              "  --no-data-resources      # Ignore all data resources.",
              "  --no-desugaring          # Force disable desugaring.",
              "  --main-dex-rules <file>  # Proguard keep rules for classes to place in the",
              "                           # primary dex file.",
              "  --main-dex-list <file>   # List of classes to place in the primary dex file.",
              "  --main-dex-list-output <file>  ",
              "                           # Output the full main-dex list in <file>.",
              "  --version                # Print the version of r8.",
              "  --help                   # Print this message."));
  /**
   * Parse the R8 command-line.
   *
   * <p>Parsing will set the supplied options or their default value if they have any.
   *
   * @param args Command-line arguments array.
   * @param origin Origin description of the command-line arguments.
   * @return R8 command builder with state set up according to parsed command line.
   */
  public static R8Command.Builder parse(String[] args, Origin origin) {
    return new R8CommandParser().parse(args, origin, R8Command.builder());
  }

  /**
   * Parse the R8 command-line.
   *
   * <p>Parsing will set the supplied options or their default value if they have any.
   *
   * @param args Command-line arguments array.
   * @param origin Origin description of the command-line arguments.
   * @param handler Custom defined diagnostics handler.
   * @return R8 command builder with state set up according to parsed command line.
   */
  public static R8Command.Builder parse(String[] args, Origin origin, DiagnosticsHandler handler) {
    return new R8CommandParser().parse(args, origin, R8Command.builder(handler));
  }

  private R8Command.Builder parse(String[] args, Origin origin, R8Command.Builder builder) {
    ParseState state = new ParseState();
    parse(args, origin, builder, state);
    if (state.mode != null) {
      builder.setMode(state.mode);
    }
    Path outputPath = state.outputPath != null ? state.outputPath : Paths.get(".");
    OutputMode outputMode = state.outputMode != null ? state.outputMode : OutputMode.DexIndexed;
    builder.setOutput(outputPath, outputMode, state.includeDataResources);
    return builder;
  }

  private void parse(
      String[] args, Origin argsOrigin, R8Command.Builder builder, ParseState state) {
    String[] expandedArgs = FlagFile.expandFlagFiles(args, builder);
    for (int i = 0; i < expandedArgs.length; i++) {
      String arg = expandedArgs[i].trim();
      String nextArg = null;
      if (OPTIONS_WITH_PARAMETER.contains(arg)) {
        if (++i < expandedArgs.length) {
          nextArg = expandedArgs[i];
        } else {
          builder.error(
              new StringDiagnostic(
                  "Missing parameter for " + expandedArgs[i - 1] + ".", argsOrigin));
          break;
        }
      }
      if (arg.length() == 0) {
        continue;
      } else if (arg.equals("--help")) {
        builder.setPrintHelp(true);
      } else if (arg.equals("--version")) {
        builder.setPrintVersion(true);
      } else if (arg.equals("--debug")) {
        if (state.mode == CompilationMode.RELEASE) {
          builder.error(
              new StringDiagnostic(
                  "Cannot compile in both --debug and --release mode.", argsOrigin));
        }
        state.mode = CompilationMode.DEBUG;
      } else if (arg.equals("--release")) {
        if (state.mode == CompilationMode.DEBUG) {
          builder.error(
              new StringDiagnostic(
                  "Cannot compile in both --debug and --release mode.", argsOrigin));
        }
        state.mode = CompilationMode.RELEASE;
      } else if (arg.equals("--dex")) {
        if (state.outputMode == OutputMode.ClassFile) {
          builder.error(
              new StringDiagnostic(
                  "Cannot compile in both --dex and --classfile output mode.", argsOrigin));
        }
        state.outputMode = OutputMode.DexIndexed;
      } else if (arg.equals("--classfile")) {
        if (state.outputMode == OutputMode.DexIndexed) {
          builder.error(
              new StringDiagnostic(
                  "Cannot compile in both --dex and --classfile output mode.", argsOrigin));
        }
        state.outputMode = OutputMode.ClassFile;
      } else if (arg.equals("--output")) {
        if (state.outputPath != null) {
          builder.error(
              new StringDiagnostic(
                  "Cannot output both to '"
                      + state.outputPath.toString()
                      + "' and '"
                      + nextArg
                      + "'",
                  argsOrigin));
        }
        state.outputPath = Paths.get(nextArg);
      } else if (arg.equals("--lib")) {
        builder.addLibraryFiles(Paths.get(nextArg));
      } else if (arg.equals("--classpath")) {
        builder.addClasspathFiles(Paths.get(nextArg));
      } else if (arg.equals("--min-api")) {
        if (state.hasDefinedApiLevel) {
          builder.error(new StringDiagnostic("Cannot set multiple --min-api options", argsOrigin));
        } else {
          parseMinApi(builder, nextArg, argsOrigin);
          state.hasDefinedApiLevel = true;
        }
      } else if (arg.equals("--no-tree-shaking")) {
        builder.setDisableTreeShaking(true);
      } else if (arg.equals("--no-minification")) {
        builder.setDisableMinification(true);
      } else if (arg.equals("--no-desugaring")) {
        builder.setDisableDesugaring(true);
      } else if (arg.equals("--main-dex-rules")) {
        builder.addMainDexRulesFiles(Paths.get(nextArg));
      } else if (arg.equals("--main-dex-list")) {
        builder.addMainDexListFiles(Paths.get(nextArg));
      } else if (arg.equals("--main-dex-list-output")) {
        builder.setMainDexListOutputPath(Paths.get(nextArg));
      } else if (arg.equals("--optimize-multidex-for-linearalloc")) {
        builder.setOptimizeMultidexForLinearAlloc(true);
      } else if (arg.equals("--pg-conf")) {
        builder.addProguardConfigurationFiles(Paths.get(nextArg));
      } else if (arg.equals("--pg-map-output")) {
        builder.setProguardMapOutputPath(Paths.get(nextArg));
      } else if (arg.equals("--no-data-resources")) {
        state.includeDataResources = false;
      } else {
        if (arg.startsWith("--")) {
          builder.error(new StringDiagnostic("Unknown option: " + arg, argsOrigin));
        }
        builder.addProgramFiles(Paths.get(arg));
      }
    }
  }
}
