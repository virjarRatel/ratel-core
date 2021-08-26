// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.bisect;

import com.android.tools.r8.OutputMode;
import com.android.tools.r8.ProgramConsumer;
import com.android.tools.r8.StringConsumer;
import com.android.tools.r8.bisect.BisectOptions.Result;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.AndroidAppConsumers;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bisect {

  private final BisectOptions options;
  private final Timing timing = new Timing("bisect");

  public interface Command {

    Result apply(DexApplication application) throws Exception;
  }

  private static class StreamReader implements Runnable {

    private final InputStream stream;
    private String result;

    public StreamReader(InputStream stream) {
      this.stream = stream;
    }

    public String getResult() {
      return result;
    }

    @Override
    public void run() {
      try {
        result = CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
        stream.close();
      } catch (IOException e) {
        result = "Failed reading result for stream " + stream;
      }
    }
  }

  public Bisect(BisectOptions options) {
    this.options = options;
  }

  public static DexProgramClass run(BisectState state, Command command, Path output,
      ExecutorService executor)
      throws Exception {
    while (true) {
      DexApplication app = state.bisect();
      state.write();
      if (app == null) {
        return state.getFinalClass();
      }
      if (command == null) {
        writeApp(app, output, executor);
        System.out.println("Bisecting completed with build in " + output + "/");
        System.out.println("Continue bisection by passing either --"
            + BisectOptions.RESULT_GOOD_FLAG + " or --"
            + BisectOptions.RESULT_BAD_FLAG);
        return null;
      }
      state.setPreviousResult(command.apply(app));
      app.options.itemFactory.resetSortedIndices();
    }
  }

  public DexProgramClass run() throws Exception {
    // Setup output directory (or write to a temp dir).
    Path output;
    if (options.output != null) {
      output = options.output;
    } else {
      output = Files.createTempDirectory("bisect");
    }

    ExecutorService executor = Executors.newWorkStealingPool();
    try {
      InternalOptions internal = new InternalOptions();
      DexApplication goodApp = readApp(options.goodBuild, internal, executor);
      DexApplication badApp = readApp(options.badBuild, internal, executor);

      Path stateFile =
          options.stateFile != null ? options.stateFile : output.resolve("bisect.state");

      // Setup initial (or saved) bisection state.
      BisectState state = new BisectState(goodApp, badApp, stateFile);
      if (options.stateFile != null) {
        state.read();
      }

      // If a "previous" result is supplied on the command line, record it.
      if (options.result != Result.UNKNOWN) {
        state.setPreviousResult(options.result);
      }

      // Setup post-build command.
      Command command = null;
      if (options.command != null) {
        command =
            (application) -> {
              writeApp(application, output, executor);
              return runCommand(options.command, options.goodBuild, options.badBuild, output);
            };
      }

      // Run bisection.
      return run(state, command, output, executor);
    } finally {
      executor.shutdown();
    }
  }

  private static Result runCommand(Path command, Path good, Path bad, Path output)
      throws IOException {
    List<String> args = new ArrayList<>();
    args.add("/bin/bash");
    args.add(command.toString());
    args.addAll(ImmutableList.of(good.toString(), bad.toString(), output.toString()));
    System.out.println("Running cmd: " + String.join(" ", args));
    ProcessBuilder builder = new ProcessBuilder(args);
    Process process = builder.start();
    StreamReader stdoutReader = new StreamReader(process.getInputStream());
    StreamReader stderrReader = new StreamReader(process.getErrorStream());
    Thread stdoutThread = new Thread(stdoutReader);
    Thread stderrThread = new Thread(stderrReader);
    stdoutThread.start();
    stderrThread.start();
    try {
      process.waitFor();
      stdoutThread.join();
      stderrThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException("Execution interrupted", e);
    }
    System.out.print("OUT:\n" + stdoutReader.getResult());
    System.out.print("ERR:\n" + stderrReader.getResult());
    int result = process.exitValue();
    if (result == 0) {
      return Result.GOOD;
    } else if (result == 1) {
      return Result.BAD;
    }
    System.out.println("Failed to run command " + args);
    System.out.println("Exit code: " + result + " (expected 0 for good, 1 for bad)");
    throw new CompilationError("Failed to run command " + args);
  }

  private DexApplication readApp(Path apk, InternalOptions options, ExecutorService executor)
      throws IOException, ExecutionException {
    AndroidApp app = AndroidApp.builder().addProgramFiles(apk).build();
    return new ApplicationReader(app, options, timing).read(executor);
  }

  private static void writeApp(DexApplication app, Path output, ExecutorService executor)
      throws IOException, ExecutionException {
    InternalOptions options = app.options;
    // Save the original consumers so they can be unwrapped after write.
    ProgramConsumer programConsumer = options.programConsumer;
    StringConsumer proguardMapConsumer = options.proguardMapConsumer;
    AndroidAppConsumers compatSink = new AndroidAppConsumers(options);
    ApplicationWriter writer =
        new ApplicationWriter(app, null, options, null, null, NamingLens.getIdentityLens(), null);
    writer.write(executor);
    options.signalFinishedToConsumers();
    compatSink.build().writeToDirectory(output, OutputMode.DexIndexed);
    // Restore original consumers.
    options.programConsumer = programConsumer;
    options.proguardMapConsumer = proguardMapConsumer;
  }

  public static DexProgramClass run(BisectOptions options) throws Exception {
    return new Bisect(options).run();
  }

  public static void main(String[] args) throws Exception {
    BisectOptions options;
    try {
      options = BisectOptions.parse(args);
    } catch (CompilationError e) {
      System.err.println(e.getMessage());
      BisectOptions.printHelp(System.err);
      return;
    }
    if (options == null) {
      return;
    }
    DexProgramClass clazz = Bisect.run(options);
    if (clazz != null) {
      System.out.println("Bisection found final bad class " + clazz);
    }
  }
}
