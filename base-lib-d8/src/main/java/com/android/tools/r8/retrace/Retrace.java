// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import static com.android.tools.r8.utils.ExceptionUtils.STATUS_ERROR;

import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.Keep;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.retrace.RetraceCommand.Builder;
import com.android.tools.r8.retrace.RetraceCommand.ProguardMapProducer;
import com.android.tools.r8.utils.OptionsParsing;
import com.android.tools.r8.utils.OptionsParsing.ParseContext;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A retrace tool for obfuscated stack traces.
 *
 * <p>This is the interface for getting de-obfuscating stack traces, similar to the proguard retrace
 * tool.
 */
@Keep
public class Retrace {

  public static final String USAGE_MESSAGE =
      StringUtils.lines(
          "Usage: retrace <proguard-map> <stacktrace-file>",
          "  where <proguard-map> is an r8 generated mapping file.");

  private static Builder parseArguments(String[] args, DiagnosticsHandler diagnosticsHandler) {
    ParseContext context = new ParseContext(args);
    Builder builder = RetraceCommand.builder(diagnosticsHandler);
    boolean hasSetProguardMap = false;
    boolean hasSetStackTrace = false;
    while (context.head() != null) {
      Boolean help = OptionsParsing.tryParseBoolean(context, "--help");
      if (help != null) {
        return null;
      }
      Boolean verbose = OptionsParsing.tryParseBoolean(context, "--verbose");
      if (verbose != null) {
        // TODO(b/132850880): Enable support for verbose.
        diagnosticsHandler.error(new StringDiagnostic("Currently no support for --verbose"));
        continue;
      }
      String regex = OptionsParsing.tryParseSingle(context, "--regex", "r");
      if (regex != null && !regex.isEmpty()) {
        // TODO(b/132850880): Enable support for regex.
        diagnosticsHandler.error(new StringDiagnostic("Currently no support for --regex"));
        continue;
      }
      if (!hasSetProguardMap) {
        builder.setProguardMapProducer(getMappingSupplier(context.head(), diagnosticsHandler));
        context.next();
        hasSetProguardMap = true;
      } else if (!hasSetStackTrace) {
        builder.setStackTrace(getStackTraceFromFile(context.head(), diagnosticsHandler));
        context.next();
        hasSetStackTrace = true;
      } else {
        diagnosticsHandler.error(
            new StringDiagnostic(
                String.format("Too many arguments specified for builder at '%s'", context.head())));
        diagnosticsHandler.error(new StringDiagnostic(USAGE_MESSAGE));
        throw new RetraceAbortException();
      }
    }
    if (!hasSetProguardMap) {
      diagnosticsHandler.error(new StringDiagnostic("Mapping file not specified"));
      throw new RetraceAbortException();
    }
    if (!hasSetStackTrace) {
      builder.setStackTrace(getStackTraceFromStandardInput());
    }
    return builder;
  }

  private static ProguardMapProducer getMappingSupplier(
      String mappingPath, DiagnosticsHandler diagnosticsHandler) {
    Path path = Paths.get(mappingPath);
    if (!Files.exists(path)) {
      diagnosticsHandler.error(
          new StringDiagnostic(String.format("Could not find mapping file '%s'.", mappingPath)));
      throw new RetraceAbortException();
    }
    return () -> new String(Files.readAllBytes(path));
  }

  private static List<String> getStackTraceFromFile(
      String stackTracePath, DiagnosticsHandler diagnostics) {
    try {
      return Files.readAllLines(Paths.get(stackTracePath));
    } catch (IOException e) {
      diagnostics.error(new StringDiagnostic("Could not find stack trace file: " + stackTracePath));
      throw new RetraceAbortException();
    }
  }

  /**
   * The main entry point for running the retrace.
   *
   * @param command The command that describes the desired behavior of this retrace invocation.
   */
  public static void run(RetraceCommand command) {
    try {
      ClassNameMapper classNameMapper =
          ClassNameMapper.mapperFromString(command.proguardMapProducer.get());
      RetraceBase retraceBase = new RetraceBaseImpl(classNameMapper);
      RetraceCommandLineResult result;
      if (command.regularExpression != null) {
        result =
            new RetraceRegularExpression(
                    retraceBase,
                    command.stackTrace,
                    command.diagnosticsHandler,
                    command.regularExpression)
                .retrace();
      } else {
        result =
            new RetraceStackTrace(retraceBase, command.stackTrace, command.diagnosticsHandler)
                .retrace();
      }
      command.retracedStackTraceConsumer.accept(result.getNodes());
    } catch (IOException ex) {
      command.diagnosticsHandler.error(
          new StringDiagnostic("Could not open mapping input stream: " + ex.getMessage()));
      throw new RetraceAbortException();
    }
  }

  static void run(String[] args) {
    DiagnosticsHandler diagnosticsHandler = new DiagnosticsHandler() {};
    Builder builder = parseArguments(args, diagnosticsHandler);
    if (builder == null) {
      // --help was an argument to list
      assert Arrays.asList(args).contains("--help");
      System.out.print(USAGE_MESSAGE);
      return;
    }
    builder.setRetracedStackTraceConsumer(
        retraced -> System.out.print(StringUtils.lines(retraced)));
    run(builder.build());
  }
  /**
   * The main entry point for running a legacy compatible retrace from the command line.
   *
   * @param args The argument that describes this command.
   */
  public static void main(String... args) {
    withMainProgramHandler(() -> run(args));
  }

  private static List<String> getStackTraceFromStandardInput() {
    Scanner sc = new Scanner(System.in);
    List<String> readLines = new ArrayList<>();
    while (sc.hasNext()) {
      readLines.add(sc.nextLine());
    }
    return readLines;
  }

  static class RetraceAbortException extends RuntimeException {}

  private interface MainAction {
    void run() throws RetraceAbortException;
  }

  private static void withMainProgramHandler(MainAction action) {
    try {
      action.run();
    } catch (RetraceAbortException e) {
      // Detail of the errors were already reported
      System.exit(STATUS_ERROR);
    } catch (RuntimeException e) {
      System.err.println("Retrace failed with an internal error.");
      Throwable cause = e.getCause() == null ? e : e.getCause();
      cause.printStackTrace();
      System.exit(STATUS_ERROR);
    }
  }
}
