// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.FlagFile;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class D8CommandParser extends BaseCompilerCommandParser {

  private static final Set<String> OPTIONS_WITH_PARAMETER =
      ImmutableSet.of(
          "--output",
          "--lib",
          "--classpath",
          "--min-api",
          "--main-dex-list",
          "--main-dex-list-output");

  private static final String APK_EXTENSION = ".apk";
  private static final String JAR_EXTENSION = ".jar";
  private static final String ZIP_EXTENSION = ".zip";

  private static boolean isArchive(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(APK_EXTENSION)
        || name.endsWith(JAR_EXTENSION)
        || name.endsWith(ZIP_EXTENSION);
  }

  static class OrderedClassFileResourceProvider implements ClassFileResourceProvider {
    static class Builder {
      private final ImmutableList.Builder<ClassFileResourceProvider> builder =
          ImmutableList.builder();
      boolean empty = true;

      OrderedClassFileResourceProvider build() {
        return new OrderedClassFileResourceProvider(builder.build());
      }

      Builder addClassFileResourceProvider(ClassFileResourceProvider provider) {
        builder.add(provider);
        empty = false;
        return this;
      }

      boolean isEmpty() {
        return empty;
      }
    }

    final List<ClassFileResourceProvider> providers;
    final Set<String> descriptors = Sets.newHashSet();

    private OrderedClassFileResourceProvider(ImmutableList<ClassFileResourceProvider> providers) {
      this.providers = providers;
      // Collect all descriptors that can be provided.
      this.providers.forEach(provider -> this.descriptors.addAll(provider.getClassDescriptors()));
    }

    static Builder builder() {
      return new Builder();
    }

    @Override
    public Set<String> getClassDescriptors() {
      return descriptors;
    }

    @Override
    public ProgramResource getProgramResource(String descriptor) {
      // Search the providers in order. Return the program resource from the first provider that
      // can provide it.
      for (ClassFileResourceProvider provider : providers) {
        if (provider.getClassDescriptors().contains(descriptor)) {
          return provider.getProgramResource(descriptor);
        }
      }
      return null;
    }
  }

  public static void main(String[] args) throws CompilationFailedException {
    D8Command command = parse(args, Origin.root()).build();
    if (command.isPrintHelp()) {
      System.out.println(USAGE_MESSAGE);
      System.exit(1);
    }
    D8.run(command);
  }

  static final String USAGE_MESSAGE =
      String.join(
          "\n",
          Arrays.asList(
              "Usage: d8 [options] <input-files>",
              " where <input-files> are any combination of dex, class, zip, jar, or apk files",
              " and options are:",
              "  --debug                 # Compile with debugging information (default).",
              "  --release               # Compile without debugging information.",
              "  --output <file>         # Output result in <outfile>.",
              "                          # <file> must be an existing directory or a zip file.",
              "  --lib <file>            # Add <file> as a library resource.",
              "  --classpath <file>      # Add <file> as a classpath resource.",
              "  --min-api <number>      # Minimum Android API level compatibility, default: "
                  + AndroidApiLevel.getDefault().getLevel()
                  + ".",
              "  --intermediate          # Compile an intermediate result intended for later",
              "                          # merging.",
              "  --file-per-class        # Produce a separate dex file per input class",
              "  --no-desugaring         # Force disable desugaring.",
              "  --main-dex-list <file>  # List of classes to place in the primary dex file.",
              "  --main-dex-list-output <file>",
              "                          # Output resulting main dex list in <file>.",
              "  --version               # Print the version of d8.",
              "  --help                  # Print this message."));

  /**
   * Parse the D8 command-line.
   *
   * <p>Parsing will set the supplied options or their default value if they have any.
   *
   * @param args Command-line arguments array.
   * @param origin Origin description of the command-line arguments.
   * @return D8 command builder with state set up according to parsed command line.
   */
  public static D8Command.Builder parse(String[] args, Origin origin) {
    return parse(args, origin, D8Command.builder());
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
  public static D8Command.Builder parse(String[] args, Origin origin, DiagnosticsHandler handler) {
    return parse(args, origin, D8Command.builder(handler));
  }

  private static D8Command.Builder parse(String[] args, Origin origin, D8Command.Builder builder) {
    CompilationMode compilationMode = null;
    Path outputPath = null;
    OutputMode outputMode = null;
    boolean hasDefinedApiLevel = false;
    OrderedClassFileResourceProvider.Builder classpathBuilder =
        OrderedClassFileResourceProvider.builder();
    String[] expandedArgs = FlagFile.expandFlagFiles(args, builder);
    for (int i = 0; i < expandedArgs.length; i++) {
      String arg = expandedArgs[i].trim();
      String nextArg = null;
      if (OPTIONS_WITH_PARAMETER.contains(arg)) {
        if (++i < expandedArgs.length) {
          nextArg = expandedArgs[i];
        } else {
          builder.error(
              new StringDiagnostic("Missing parameter for " + expandedArgs[i - 1] + ".", origin));
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
        if (compilationMode == CompilationMode.RELEASE) {
          builder.error(
              new StringDiagnostic("Cannot compile in both --debug and --release mode.", origin));
          continue;
        }
        compilationMode = CompilationMode.DEBUG;
      } else if (arg.equals("--release")) {
        if (compilationMode == CompilationMode.DEBUG) {
          builder.error(
              new StringDiagnostic("Cannot compile in both --debug and --release mode.", origin));
          continue;
        }
        compilationMode = CompilationMode.RELEASE;
      } else if (arg.equals("--file-per-class")) {
        outputMode = OutputMode.DexFilePerClass;
      } else if (arg.equals("--output")) {
        if (outputPath != null) {
          builder.error(
              new StringDiagnostic(
                  "Cannot output both to '" + outputPath.toString() + "' and '" + nextArg + "'",
                  origin));
          continue;
        }
        outputPath = Paths.get(nextArg);
      } else if (arg.equals("--lib")) {
        builder.addLibraryFiles(Paths.get(nextArg));
      } else if (arg.equals("--classpath")) {
        Path file = Paths.get(nextArg);
        try {
          if (!Files.exists(file)) {
            throw new NoSuchFileException(file.toString());
          }
          if (isArchive(file)) {
            classpathBuilder.addClassFileResourceProvider(new ArchiveClassFileProvider(file));
          } else if (Files.isDirectory(file)) {
            classpathBuilder.addClassFileResourceProvider(
                DirectoryClassFileProvider.fromDirectory(file));
          } else {
            builder.error(
                new StringDiagnostic("Unsupported classpath file type", new PathOrigin(file)));
          }
        } catch (IOException e) {
          builder.error(new ExceptionDiagnostic(e, new PathOrigin(file)));
        }
      } else if (arg.equals("--main-dex-list")) {
        builder.addMainDexListFiles(Paths.get(nextArg));
      } else if (arg.equals("--main-dex-list-output")) {
        builder.setMainDexListOutputPath(Paths.get(nextArg));
      } else if (arg.equals("--optimize-multidex-for-linearalloc")) {
        builder.setOptimizeMultidexForLinearAlloc(true);
      } else if (arg.equals("--min-api")) {
        if (hasDefinedApiLevel) {
          builder.error(new StringDiagnostic("Cannot set multiple --min-api options", origin));
        } else {
          parseMinApi(builder, nextArg, origin);
          hasDefinedApiLevel = true;
        }
      } else if (arg.equals("--intermediate")) {
        builder.setIntermediate(true);
      } else if (arg.equals("--no-desugaring")) {
        builder.setDisableDesugaring(true);
      } else {
        if (arg.startsWith("--")) {
          builder.error(new StringDiagnostic("Unknown option: " + arg, origin));
          continue;
        }
        builder.addProgramFiles(Paths.get(arg));
      }
    }
    if (!classpathBuilder.isEmpty()) {
      builder.addClasspathResourceProvider(classpathBuilder.build());
    }
    if (compilationMode != null) {
      builder.setMode(compilationMode);
    }
    if (outputMode == null) {
      outputMode = OutputMode.DexIndexed;
    }
    if (outputPath == null) {
      outputPath = Paths.get(".");
    }
    return builder.setOutput(outputPath, outputMode);
  }
}
