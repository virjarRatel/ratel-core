// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.StringConsumer;
import com.android.tools.r8.Version;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.google.common.collect.ObjectArrays;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public abstract class ExceptionUtils {

  public static final int STATUS_ERROR = 1;

  public static void withConsumeResourceHandler(
      Reporter reporter, StringConsumer consumer, String data) {
    withConsumeResourceHandler(reporter, handler -> consumer.accept(data, handler));
  }

  public static void withFinishedResourceHandler(Reporter reporter, StringConsumer consumer) {
    withConsumeResourceHandler(reporter, consumer::finished);
  }

  public static void withConsumeResourceHandler(
      Reporter reporter, Consumer<DiagnosticsHandler> consumer) {
    // Unchecked exceptions simply propagate out, aborting the compilation forcefully.
    consumer.accept(reporter);
    // Fail fast for now. We might consider delaying failure since consumer failure does not affect
    // the compilation. We might need to be careful to correctly identify errors so as to exit
    // compilation with an error code.
    reporter.failIfPendingErrors();
  }

  public interface CompileAction {
    void run() throws IOException, CompilationError, ResourceException;
  }

  public static void withD8CompilationHandler(Reporter reporter, CompileAction action)
      throws CompilationFailedException {
    withCompilationHandler(reporter, action);
  }

  public static void withR8CompilationHandler(Reporter reporter, CompileAction action)
      throws CompilationFailedException {
    withCompilationHandler(reporter, action);
  }

  public static void withMainDexListHandler(
      Reporter reporter, CompileAction action) throws CompilationFailedException {
    withCompilationHandler(reporter, action);
  }

  public static void withCompilationHandler(Reporter reporter, CompileAction action)
      throws CompilationFailedException {
    try {
      try {
        action.run();
      } catch (IOException e) {
        throw reporter.fatalError(new ExceptionDiagnostic(e, extractIOExceptionOrigin(e)));
      } catch (CompilationError e) {
        throw reporter.fatalError(e.toStringDiagnostic());
      } catch (ResourceException e) {
        throw reporter.fatalError(new ExceptionDiagnostic(e, e.getOrigin()));
      } catch (AssertionError e) {
        throw reporter.fatalError(new ExceptionDiagnostic(e, Origin.unknown()));
      } catch (Exception e) {
        String filename = "Version_" + Version.LABEL + ".java";
        StackTraceElement versionElement = new StackTraceElement(
            Version.class.getSimpleName(), "fakeStackEntry", filename, 0);
        StackTraceElement[] withVersion = ObjectArrays.concat(versionElement, e.getStackTrace());
        e.setStackTrace(withVersion);
        throw e;
      }
      reporter.failIfPendingErrors();
    } catch (AbortException e) {
      throw new CompilationFailedException(e);
    }
  }

  public interface MainAction {
    void run() throws CompilationFailedException;
  }

  public static void withMainProgramHandler(MainAction action) {
    try {
      action.run();
    } catch (CompilationFailedException | AbortException e) {
      // Detail of the errors were already reported
      System.err.println("Compilation failed");
      System.exit(STATUS_ERROR);
    } catch (RuntimeException e) {
      System.err.println("Compilation failed with an internal error.");
      Throwable cause = e.getCause() == null ? e : e.getCause();
      cause.printStackTrace();
      System.exit(STATUS_ERROR);
    }
  }

  // We should try to avoid the use of this extraction as it signifies a point where we don't have
  // enough context to associate a specific origin with an IOException. Concretely, we should move
  // towards always catching IOException and rethrowing CompilationError with proper origins.
  public static Origin extractIOExceptionOrigin(IOException e) {
    if (e instanceof FileSystemException) {
      FileSystemException fse = (FileSystemException) e;
      if (fse.getFile() != null && !fse.getFile().isEmpty()) {
        return new PathOrigin(Paths.get(fse.getFile()));
      }
    }
    return Origin.unknown();
  }

  public static RuntimeException unwrapExecutionException(ExecutionException executionException) {
    Throwable cause = executionException.getCause();
    if (cause instanceof Error) {
      // add original exception as suppressed exception to provide the original stack trace
      cause.addSuppressed(executionException);
      throw (Error) cause;
    } else if (cause instanceof RuntimeException) {
      cause.addSuppressed(executionException);
      throw (RuntimeException) cause;
    } else {
      throw new RuntimeException(executionException.getMessage(), cause);
    }
  }
}
