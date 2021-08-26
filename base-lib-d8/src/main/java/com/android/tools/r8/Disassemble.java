// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.AssemblyWriter;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexByteCodeWriter;
import com.android.tools.r8.graph.SmaliWriter;
import com.android.tools.r8.origin.CommandLineOrigin;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Disassemble {
  public static class DisassembleCommand extends BaseCommand {

    private final Path outputPath;
    private final StringResource proguardMap;

    public static class Builder extends BaseCommand.Builder<DisassembleCommand, Builder> {

      private Path outputPath = null;
      private Path proguardMapFile = null;
      private boolean useSmali = false;
      private boolean allInfo = false;
      private boolean useIr;

      @Override
      Builder self() {
        return this;
      }

      public Builder setProguardMapFile(Path path) {
        proguardMapFile = path;
        return this;
      }

      public Path getOutputPath() {
        return outputPath;
      }

      public Builder setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
      }

      public Builder setAllInfo(boolean allInfo) {
        this.allInfo = allInfo;
        return this;
      }

      public Builder setUseSmali(boolean useSmali) {
        this.useSmali = useSmali;
        return this;
      }

      public Builder setUseIr(boolean useIr) {
        this.useIr = useIr;
        return this;
      }

      @Override
      protected DisassembleCommand makeCommand() {
        // If printing versions ignore everything else.
        if (isPrintHelp() || isPrintVersion()) {
          return new DisassembleCommand(isPrintHelp(), isPrintVersion());
        }
        return new DisassembleCommand(
            getAppBuilder().build(),
            getOutputPath(),
            proguardMapFile == null ? null : StringResource.fromFile(proguardMapFile),
            allInfo,
            useSmali,
            useIr);
      }
    }

    static final String USAGE_MESSAGE =
        "Usage: disasm [options] <input-files>\n"
            + " where <input-files> are dex files\n"
            + " and options are:\n"
            + "  --all                       # Include all information in disassembly.\n"
            + "  --smali                     # Disassemble using smali syntax.\n"
            + "  --ir                        # Print IR before and after optimization.\n"
            + "  --pg-map <file>             # Proguard map <file> for mapping names.\n"
            + "  --pg-map-charset <charset>  # Charset for Proguard map file.\n"
            + "  --output                    # Specify a file or directory to write to.\n"
            + "  --version                   # Print the version of r8.\n"
            + "  --help                      # Print this message.";

    private final boolean allInfo;
    private final boolean useSmali;
    private final boolean useIr;

    public static Builder builder() {
      return new Builder();
    }

    public static Builder parse(String[] args) {
      Builder builder = builder();
      parse(args, builder);
      return builder;
    }

    private static void parse(String[] args, Builder builder) {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i].trim();
        if (arg.length() == 0) {
          continue;
        } else if (arg.equals("--help")) {
          builder.setPrintHelp(true);
        } else if (arg.equals("--version")) {
          builder.setPrintVersion(true);
        } else if (arg.equals("--all")) {
          builder.setAllInfo(true);
        } else if (arg.equals("--smali")) {
          builder.setUseSmali(true);
        } else if (arg.equals("--ir")) {
          builder.setUseIr(true);
        } else if (arg.equals("--pg-map")) {
          builder.setProguardMapFile(Paths.get(args[++i]));
        } else if (arg.equals("--pg-map-charset")) {
          String charset = args[++i];
          try {
            Charset.forName(charset);
          } catch (UnsupportedCharsetException e) {
            builder.getReporter().error(
                new StringDiagnostic(
                    "Unsupported charset: " + charset + "." + System.lineSeparator()
                        + "Supported charsets are: "
                        + String.join(", ", Charset.availableCharsets().keySet()),
                CommandLineOrigin.INSTANCE));
          }
        } else if (arg.equals("--output")) {
          String outputPath = args[++i];
          builder.setOutputPath(Paths.get(outputPath));
        } else {
          if (arg.startsWith("--")) {
            builder.getReporter().error(new StringDiagnostic("Unknown option: " + arg,
                CommandLineOrigin.INSTANCE));
          }
          builder.addProgramFiles(Paths.get(arg));
        }
      }
    }

    private DisassembleCommand(
        AndroidApp inputApp,
        Path outputPath,
        StringResource proguardMap,
        boolean allInfo,
        boolean useSmali,
        boolean useIr) {
      super(inputApp);
      this.outputPath = outputPath;
      this.proguardMap = proguardMap;
      this.allInfo = allInfo;
      this.useSmali = useSmali;
      this.useIr = useIr;
    }

    private DisassembleCommand(boolean printHelp, boolean printVersion) {
      super(printHelp, printVersion);
      outputPath = null;
      proguardMap = null;
      allInfo = false;
      useSmali = false;
      useIr = false;
    }

    public Path getOutputPath() {
      return outputPath;
    }

    public boolean useSmali() {
      return useSmali;
    }

    public boolean useIr() {
      return useIr;
    }

    @Override
    InternalOptions getInternalOptions() {
      InternalOptions internal = new InternalOptions();
      internal.useSmaliSyntax = useSmali;
      return internal;
    }
  }

  public static void main(String[] args)
      throws IOException, ExecutionException, CompilationFailedException {
    DisassembleCommand.Builder builder = DisassembleCommand.parse(args);
    DisassembleCommand command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(DisassembleCommand.USAGE_MESSAGE);
      return;
    }
    if (command.isPrintVersion()) {
      System.out.println("Disassemble (R8) " + Version.LABEL);
      return;
    }
    disassemble(command);
  }

  public static void disassemble(DisassembleCommand command)
      throws IOException, ExecutionException {
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    Timing timing = new Timing("disassemble");
    try {
      DexApplication application =
          new ApplicationReader(app, options, timing).read(command.proguardMap, executor);
      DexByteCodeWriter writer =
          command.useSmali()
              ? new SmaliWriter(application, options)
              : new AssemblyWriter(application, options, command.allInfo, command.useIr());
      if (command.getOutputPath() != null) {
        writer.write(command.getOutputPath());
      } else {
        writer.write(System.out);
      }
    } finally {
      executor.shutdown();
    }
  }
}
