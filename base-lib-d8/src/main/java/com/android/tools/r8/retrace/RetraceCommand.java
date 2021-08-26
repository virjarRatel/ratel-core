// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.Keep;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class RetraceCommand {

  final boolean isVerbose;
  final String regularExpression;
  final DiagnosticsHandler diagnosticsHandler;
  final ProguardMapProducer proguardMapProducer;
  final List<String> stackTrace;
  final Consumer<List<String>> retracedStackTraceConsumer;

  private RetraceCommand(
      boolean isVerbose,
      String regularExpression,
      DiagnosticsHandler diagnosticsHandler,
      ProguardMapProducer proguardMapProducer,
      List<String> stackTrace,
      Consumer<List<String>> retracedStackTraceConsumer) {
    this.isVerbose = isVerbose;
    this.regularExpression = regularExpression;
    this.diagnosticsHandler = diagnosticsHandler;
    this.proguardMapProducer = proguardMapProducer;
    this.stackTrace = stackTrace;
    this.retracedStackTraceConsumer = retracedStackTraceConsumer;

    assert this.diagnosticsHandler != null;
    assert this.proguardMapProducer != null;
    assert this.stackTrace != null;
    assert this.retracedStackTraceConsumer != null;
  }

  /**
   * Utility method for obtaining a RetraceCommand builder.
   *
   * @param diagnosticsHandler The diagnostics handler for consuming messages.
   */
  public static Builder builder(DiagnosticsHandler diagnosticsHandler) {
    return new Builder(diagnosticsHandler);
  }

  /** Utility method for obtaining a RetraceCommand builder with a default diagnostics handler. */
  public static Builder builder() {
    return new Builder(new DiagnosticsHandler() {});
  }

  public static class Builder {

    private boolean isVerbose;
    private DiagnosticsHandler diagnosticsHandler;
    private ProguardMapProducer proguardMapProducer;
    private String regularExpression;
    private List<String> stackTrace;
    private Consumer<List<String>> retracedStackTraceConsumer;

    private Builder(DiagnosticsHandler diagnosticsHandler) {
      this.diagnosticsHandler = diagnosticsHandler;
    }

    /** Set if the produced stack trace should have additional information. */
    public Builder isVerbose() {
      this.isVerbose = true;
      return this;
    }

    /**
     * Set a producer for the proguard mapping contents.
     *
     * @param producer Producer for
     */
    public Builder setProguardMapProducer(ProguardMapProducer producer) {
      this.proguardMapProducer = producer;
      return this;
    }

    /**
     * Set a regular expression for parsing the incoming text. The Regular expression must not use
     * naming groups and has special wild cards according to proguard retrace.
     *
     * @param regularExpression The regular expression to use.
     */
    public Builder setRegularExpression(String regularExpression) {
      this.regularExpression = regularExpression;
      return this;
    }

    /**
     * Set the obfuscated stack trace that is to be retraced.
     *
     * @param stackTrace Stack trace having the top entry(the closest stack to the error) as the
     *     first line.
     */
    public Builder setStackTrace(List<String> stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    /**
     * Set a consumer for receiving the retraced stack trace.
     *
     * @param consumer Consumer for receiving the retraced stack trace.
     */
    public Builder setRetracedStackTraceConsumer(Consumer<List<String>> consumer) {
      this.retracedStackTraceConsumer = consumer;
      return this;
    }

    public RetraceCommand build() {
      if (this.diagnosticsHandler == null) {
        throw new RuntimeException("DiagnosticsHandler not specified");
      }
      if (this.proguardMapProducer == null) {
        throw new RuntimeException("ProguardMapSupplier not specified");
      }
      if (this.stackTrace == null) {
        throw new RuntimeException("StackTrace not specified");
      }
      if (this.retracedStackTraceConsumer == null) {
        throw new RuntimeException("RetracedStackConsumer not specified");
      }
      return new RetraceCommand(
          isVerbose,
          regularExpression,
          diagnosticsHandler,
          proguardMapProducer,
          stackTrace,
          retracedStackTraceConsumer);
    }
  }

  @Keep
  public interface ProguardMapProducer {
    String get() throws IOException;
  }
}
