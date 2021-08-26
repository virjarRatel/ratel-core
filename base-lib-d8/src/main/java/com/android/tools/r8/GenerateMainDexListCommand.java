// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.experimental.graphinfo.GraphConsumer;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.origin.CommandLineOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.ProguardConfigurationParser;
import com.android.tools.r8.shaking.ProguardConfigurationRule;
import com.android.tools.r8.shaking.ProguardConfigurationSource;
import com.android.tools.r8.shaking.ProguardConfigurationSourceFile;
import com.android.tools.r8.shaking.ProguardConfigurationSourceStrings;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Keep
public class GenerateMainDexListCommand extends BaseCommand {

  private final List<ProguardConfigurationRule> mainDexKeepRules;
  private final StringConsumer mainDexListConsumer;
  private final GraphConsumer mainDexKeptGraphConsumer;
  private final DexItemFactory factory;
  private final Reporter reporter;

  @Keep
  public static class Builder extends BaseCommand.Builder<GenerateMainDexListCommand, Builder> {

    private final DexItemFactory factory = new DexItemFactory();
    private final List<ProguardConfigurationSource> mainDexRules = new ArrayList<>();
    private StringConsumer mainDexListConsumer = null;
    private GraphConsumer mainDexKeptGraphConsumer = null;

    private Builder() {
    }

    private Builder(DiagnosticsHandler diagnosticsHandler) {
      super(diagnosticsHandler);
    }


    @Override
    GenerateMainDexListCommand.Builder self() {
      return this;
    }

    /**
     * Add proguard configuration file resources for automatic main dex list calculation.
     */
    public GenerateMainDexListCommand.Builder addMainDexRulesFiles(Path... paths) {
      guard(() -> {
        for (Path path : paths) {
          mainDexRules.add(new ProguardConfigurationSourceFile(path));
        }
      });
      return self();
    }

    /**
     * Add proguard configuration file resources for automatic main dex list calculation.
     */
    public GenerateMainDexListCommand.Builder addMainDexRulesFiles(List<Path> paths) {
      guard(() -> {
        for (Path path : paths) {
          mainDexRules.add(new ProguardConfigurationSourceFile(path));
        }
      });
      return self();
    }

    /**
     * Add proguard configuration for automatic main dex list calculation.
     */
    public GenerateMainDexListCommand.Builder addMainDexRules(List<String> lines, Origin origin) {
      guard(() -> mainDexRules.add(
          new ProguardConfigurationSourceStrings(lines, Paths.get("."), origin)));
      return self();
    }

    /**
     * Set the output file for the main-dex list.
     *
     * If the file exists it will be overwritten.
     */
    public GenerateMainDexListCommand.Builder setMainDexListOutputPath(Path mainDexListOutputPath) {
      mainDexListConsumer = new StringConsumer.FileConsumer(mainDexListOutputPath);
      return self();
    }

    public GenerateMainDexListCommand.Builder setMainDexListConsumer(
        StringConsumer mainDexListConsumer) {
      this.mainDexListConsumer = mainDexListConsumer;
      return self();
    }

    @Override
    protected GenerateMainDexListCommand makeCommand() {
      // If printing versions ignore everything else.
      if (isPrintHelp() || isPrintVersion()) {
        return new GenerateMainDexListCommand(isPrintHelp(), isPrintVersion());
      }

      List<ProguardConfigurationRule> mainDexKeepRules;
      if (this.mainDexRules.isEmpty()) {
        mainDexKeepRules = ImmutableList.of();
      } else {
        ProguardConfigurationParser parser =
            new ProguardConfigurationParser(factory, getReporter());
        parser.parse(mainDexRules);
        mainDexKeepRules = parser.getConfig().getRules();
      }

      return new GenerateMainDexListCommand(
          factory,
          getAppBuilder().build(),
          mainDexKeepRules,
          mainDexListConsumer,
          mainDexKeptGraphConsumer,
          getReporter());
    }

    public GenerateMainDexListCommand.Builder setMainDexKeptGraphConsumer(
        GraphConsumer graphConsumer) {
      this.mainDexKeptGraphConsumer = graphConsumer;
      return self();
    }
  }

  static final String USAGE_MESSAGE = String.join("\n", ImmutableList.of(
      "Usage: maindex [options] <input-files>",
      " where <input-files> are JAR files",
      " and options are:",
      "  --lib <file>             # Add <file> as a library resource.",
      "  --main-dex-rules <file>  # Proguard keep rules for classes to place in the",
      "                           # primary dex file.",
      "  --main-dex-list <file>   # List of classes to place in the primary dex file.",
      "  --main-dex-list-output <file>  # Output the full main-dex list in <file>.",
      "  --version                # Print the version.",
      "  --help                   # Print this message."));


  public static GenerateMainDexListCommand.Builder builder() {
    return new GenerateMainDexListCommand.Builder();
  }

  public static GenerateMainDexListCommand.Builder builder(DiagnosticsHandler diagnosticsHandler) {
    return new GenerateMainDexListCommand.Builder(diagnosticsHandler);
  }

  public static GenerateMainDexListCommand.Builder parse(String[] args) {
    GenerateMainDexListCommand.Builder builder = builder();
    parse(args, builder);
    return builder;
  }

  public StringConsumer getMainDexListConsumer() {
    return mainDexListConsumer;
  }

  Reporter getReporter() {
    return reporter;
  }

  private static void parse(String[] args, GenerateMainDexListCommand.Builder builder) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i].trim();
      if (arg.length() == 0) {
        continue;
      } else if (arg.equals("--help")) {
        builder.setPrintHelp(true);
      } else if (arg.equals("--version")) {
        builder.setPrintVersion(true);
      } else if (arg.equals("--lib")) {
        builder.addLibraryFiles(Paths.get(args[++i]));
      } else if (arg.equals("--main-dex-rules")) {
        builder.addMainDexRulesFiles(Paths.get(args[++i]));
      } else if (arg.equals("--main-dex-list")) {
        builder.addMainDexListFiles(Paths.get(args[++i]));
      } else if (arg.equals("--main-dex-list-output")) {
        builder.setMainDexListOutputPath(Paths.get(args[++i]));
      } else {
        if (arg.startsWith("--")) {
          builder.getReporter().error(new StringDiagnostic("Unknown option: " + arg,
              CommandLineOrigin.INSTANCE));
        }
        builder.addProgramFiles(Paths.get(arg));
      }
    }
  }

  private GenerateMainDexListCommand(
      DexItemFactory factory,
      AndroidApp inputApp,
      List<ProguardConfigurationRule> mainDexKeepRules,
      StringConsumer mainDexListConsumer,
      GraphConsumer mainDexKeptGraphConsumer,
      Reporter reporter) {
    super(inputApp);
    this.factory = factory;
    this.mainDexKeepRules = mainDexKeepRules;
    this.mainDexListConsumer = mainDexListConsumer;
    this.mainDexKeptGraphConsumer = mainDexKeptGraphConsumer;
    this.reporter = reporter;
  }

  private GenerateMainDexListCommand(boolean printHelp, boolean printVersion) {
    super(printHelp, printVersion);
    this.factory = new DexItemFactory();
    this.mainDexKeepRules = ImmutableList.of();
    this.mainDexListConsumer = null;
    this.mainDexKeptGraphConsumer = null;
    this.reporter = new Reporter();
  }

  @Override
  InternalOptions getInternalOptions() {
    InternalOptions internal = new InternalOptions(factory, reporter);
    internal.programConsumer = ClassFileConsumer.emptyConsumer();
    internal.mainDexKeepRules = mainDexKeepRules;
    internal.mainDexListConsumer = mainDexListConsumer;
    internal.mainDexKeptGraphConsumer = mainDexKeptGraphConsumer;
    internal.minimalMainDex = internal.debug;
    internal.enableEnumValueOptimization = false;
    internal.enableInlining = false;
    return internal;
  }
}

