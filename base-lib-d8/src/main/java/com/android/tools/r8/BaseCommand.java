// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.AbortException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base class for commands and command builders for applications/tools which take an Android
 * application sources (and optional main-dex list) as input.
 *
 * <p>For concrete builders, see for example {@link D8Command.Builder} and {@link
 * R8Command.Builder}.
 */
@Keep
public abstract class BaseCommand {

  private final boolean printHelp;
  private final boolean printVersion;

  private final AndroidApp app;

  BaseCommand(boolean printHelp, boolean printVersion) {
    this.printHelp = printHelp;
    this.printVersion = printVersion;
    // All other fields are initialized with stub/invalid values.
    this.app = null;
  }

  BaseCommand(AndroidApp app) {
    assert app != null;
    this.app = app;
    // Print options are not set.
    printHelp = false;
    printVersion = false;
  }

  public boolean isPrintHelp() {
    return printHelp;
  }

  public boolean isPrintVersion() {
    return printVersion;
  }

  // Internal access to the input resources.
  AndroidApp getInputApp() {
    return app;
  }

  // Internal access to the internal options.
  abstract InternalOptions getInternalOptions();

  abstract static class InputFileOrigin extends PathOrigin {
    private final String inputType;

    public InputFileOrigin(String inputType, Path file) {
      super(file);
      this.inputType = inputType;
    }

    @Override
    public String part() {
      return inputType + " '" + super.part() + "'";
    }
  }

  private static class ProgramInputOrigin extends InputFileOrigin {

    public ProgramInputOrigin(Path file) {
      super("program input", file);
    }
  }

  private static class LibraryInputOrigin extends InputFileOrigin {

    public LibraryInputOrigin(Path file) {
      super("library input", file);
    }
  }

  /**
   * Base builder for commands.
   *
   * @param <C> Command the builder is building, e.g., {@link R8Command} or {@link D8Command}.
   * @param <B> Concrete builder extending this base, e.g., {@link R8Command.Builder} or {@link
   *     D8Command.Builder}.
   */
  @Keep
  public abstract static class Builder<C extends BaseCommand, B extends Builder<C, B>> {

    private final Reporter reporter;
    private boolean printHelp = false;
    private boolean printVersion = false;
    private final AndroidApp.Builder app;

    List<Path> programFiles = new ArrayList<>();

    Builder() {
      this(AndroidApp.builder());
    }

    Builder(DiagnosticsHandler handler) {
      this(AndroidApp.builder(new Reporter(handler)));
    }

    Builder(AndroidApp.Builder builder) {
      this.app = builder;
      this.reporter = builder.getReporter();
    }

    abstract B self();

    /**
     * Build the final command.
     *
     * <p>Building the command will complete the validation of builders state and build the final
     * command. If any errors occur during validation or building the errors are reported to the
     * associated diagnostics handler and a {@link CompilationFailedException} exception is thrown.
     */
    public final C build() throws CompilationFailedException {
      try {
        validate();
        C c = makeCommand();
        reporter.failIfPendingErrors();
        return c;
      } catch (AbortException e) {
        throw new CompilationFailedException(e);
      }
    }

    // Helper to construct the actual command. Called as part of {@link build()}.
    abstract C makeCommand();

    // Internal accessor for the application resources.
    AndroidApp.Builder getAppBuilder() {
      return app;
    }

    /** Add program file resources. */
    public B addProgramFiles(Path... files) {
      addProgramFiles(Arrays.asList(files));
      return self();
    }

    Reporter getReporter() {
      return reporter;
    }

    /** Add program file resources. */
    public B addProgramFiles(Collection<Path> files) {
      guard(
          () -> {
            for (Path path : files) {
              try {
                app.addProgramFile(path);
                programFiles.add(path);
              } catch (CompilationError e) {
                error(new ProgramInputOrigin(path), e);
              }
            }
          });
      return self();
    }

    /** Add a resource provider for program resources. */
    public B addProgramResourceProvider(ProgramResourceProvider programProvider) {
      app.addProgramResourceProvider(programProvider);
      return self();
    }

    /** Add library file resource provider. */
    public B addLibraryResourceProvider(ClassFileResourceProvider provider) {
      guard(() -> getAppBuilder().addLibraryResourceProvider(provider));
      return self();
    }

    /** Add library file resources. */
    public B addLibraryFiles(Path... files) {
      addLibraryFiles(Arrays.asList(files));
      return self();
    }

    /** Add library file resources. */
    public B addLibraryFiles(Collection<Path> files) {
      guard(
          () -> {
            for (Path path : files) {
              try {
                app.addLibraryFile(path);
              } catch (CompilationError e) {
                error(new LibraryInputOrigin(path), e);
              }
            }
          });
      return self();
    }

    /** Add classpath file resources. */
    public B addClasspathFiles(Path... files) {
      guard(() -> Arrays.stream(files).forEach(this::addClasspathFile));
      return self();
    }

    /** Add classpath file resources. */
    public B addClasspathFiles(Collection<Path> files) {
      guard(() -> files.forEach(this::addClasspathFile));
      return self();
    }

    private void addClasspathFile(Path file) {
      guard(() -> getAppBuilder().addClasspathFile(file));
    }

    /** Add classfile resources provider for class-path resources. */
    public B addClasspathResourceProvider(ClassFileResourceProvider provider) {
      guard(() -> getAppBuilder().addClasspathResourceProvider(provider));
      return self();
    }

    /** Add Java-bytecode program-data. */
    public B addClassProgramData(byte[] data, Origin origin) {
      guard(() -> app.addClassProgramData(data, origin));
      return self();
    }

    /** Add Java-bytecode program-data. */
    B addDexProgramData(byte[] data, Origin origin) {
      guard(() -> app.addDexProgramData(data, origin));
      return self();
    }

    /**
     * Add main-dex list files.
     *
     * Each line in each of the files specifies one class to keep in the primary dex file
     * (<code>classes.dex</code>).
     *
     * A class is specified using the following format: "com/example/MyClass.class". That is
     * "/" as separator between package components, and a trailing ".class".
     */
    public B addMainDexListFiles(Path... files) {
      guard(() -> {
        try {
          app.addMainDexListFiles(files);
        } catch (NoSuchFileException e) {
          reporter.error(new StringDiagnostic(
              "Main-dex-list file does not exist", new PathOrigin(Paths.get(e.getFile()))));
        }
      });
      return self();
    }

    /**
     * Add main-dex list files.
     *
     * @see #addMainDexListFiles(Path...)
     */
    public B addMainDexListFiles(Collection<Path> files) {
      guard(() -> {
        try {
          app.addMainDexListFiles(files);
        } catch (NoSuchFileException e) {
          reporter.error(new StringDiagnostic(
              "Main-dex-ist file does not exist", new PathOrigin(Paths.get(e.getFile()))));
        }
      });
      return self();
    }

    /**
     * Add main-dex classes.
     *
     * Add classes to keep in the primary dex file (<code>classes.dex</code>).
     *
     * NOTE: The name of the classes is specified using the Java fully qualified names format
     * (e.g. "com.example.MyClass"), and <i>not</i> the format used by the main-dex list file.
     */
    public B addMainDexClasses(String... classes) {
      guard(() -> app.addMainDexClasses(classes));
      return self();
    }

    /**
     * Add main-dex classes.
     *
     * Add classes to keep in the primary dex file (<code>classes.dex</code>).
     *
     * NOTE: The name of the classes is specified using the Java fully qualified names format
     * (e.g. "com.example.MyClass"), and <i>not</i> the format used by the main-dex list file.
     */
    public B addMainDexClasses(Collection<String> classes) {
      guard(() -> app.addMainDexClasses(classes));
      return self();
    }

    /** True if the print-help flag is enabled. */
    public boolean isPrintHelp() {
      return printHelp;
    }

    /** Set the value of the print-help flag. */
    public B setPrintHelp(boolean printHelp) {
      this.printHelp = printHelp;
      return self();
    }

    /** True if the print-version flag is enabled. */
    public boolean isPrintVersion() {
      return printVersion;
    }

    /** Set the value of the print-version flag. */
    public B setPrintVersion(boolean printVersion) {
      this.printVersion = printVersion;
      return self();
    }

    /** Signal an error. */
    public void error(Diagnostic diagnostic) {
      reporter.error(diagnostic);
    }

    /**
     * Signal an error and throw {@link AbortException}.
     *
     * @throws AbortException always.
     */
    public RuntimeException fatalError(Diagnostic diagnostic) {
      return reporter.fatalError(diagnostic);
    }

    // Internal helper for compat tools to make them ignore DEX code in input archives.
    void setIgnoreDexInArchive(boolean value) {
      guard(() -> app.setIgnoreDexInArchive(value));
    }

    // Helper that validates the command content. Called as part of {@link build()}.
    void validate() {}

    // Helper to signify an error.
    void error(Origin origin, Throwable throwable) {
      reporter.error(new ExceptionDiagnostic(throwable, origin));
    }

    // Helper to guard and handle exceptions.
    void guard(Runnable action) {
      try {
        action.run();
      } catch (CompilationError e) {
        reporter.error(e.toStringDiagnostic());
      } catch (AbortException e) {
        // Error was reported and exception will be thrown by build.
      }
    }

  }
}
