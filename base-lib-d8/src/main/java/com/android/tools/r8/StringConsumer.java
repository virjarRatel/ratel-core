// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Interface for receiving String resource. */
@KeepForSubclassing
public interface StringConsumer {

  /**
   * Callback to receive part of a string resource.
   *
   * <p>The consumer is expected not to throw, but instead report any errors via the diagnostics
   * {@param handler}. If an error is reported via {@param handler} and no exceptions are thrown,
   * then the compiler guaranties to exit with an error.
   *
   * <p>Note: prior to the addition of 'finished' consumers could expect all content to be reported
   * in one call to accept. That is no longer guaranteed.
   *
   * @param string Part of the string resource.
   * @param handler Diagnostics handler for reporting.
   */
  void accept(String string, DiagnosticsHandler handler);

  /**
   * Callback when no further content will be provided for the string resource.
   *
   * <p>The consumer is expected not to throw, but instead report any errors via the diagnostics
   * {@param handler}. If an error is reported via {@param handler} and no exceptions are thrown,
   * then the compiler guaranties to exit with an error.
   *
   * @param handler Diagnostics handler for reporting.
   */
  default void finished(DiagnosticsHandler handler) {}

  static EmptyConsumer emptyConsumer() {
    return EmptyConsumer.EMPTY_CONSUMER;
  }

  /** Empty consumer to request the production of the resource but ignore its value. */
  class EmptyConsumer implements StringConsumer {

    private static final EmptyConsumer EMPTY_CONSUMER = new EmptyConsumer();

    @Override
    public void accept(String string, DiagnosticsHandler handler) {
      // Ignore content.
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      // No content so, nothing to do.
    }
  }

  /** Forwarding consumer to delegate to an optional existing consumer. */
  class ForwardingConsumer implements StringConsumer {

    private final StringConsumer consumer;

    /** @param consumer Consumer to forward to, if null, nothing will be forwarded. */
    public ForwardingConsumer(StringConsumer consumer) {
      this.consumer = consumer;
    }

    @Override
    public void accept(String string, DiagnosticsHandler handler) {
      if (consumer != null) {
        consumer.accept(string, handler);
      }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      if (consumer != null) {
        consumer.finished(handler);
      }
    }
  }

  /** File consumer to write contents to a file-system file. */
  @Keep // TODO(b/121121779) Consider deprecating the R8 provided file writing.
  class FileConsumer extends ForwardingConsumer {

    private final Path outputPath;
    private Charset encoding = StandardCharsets.UTF_8;
    private WriterConsumer delegate = null;
    private boolean failedToCreateDelegate = false;

    /** Consumer that writes to {@param outputPath}. */
    public FileConsumer(Path outputPath) {
      this(outputPath, null);
    }

    /** Consumer that forwards to {@param consumer} and also writes to {@param outputPath}. */
    public FileConsumer(Path outputPath, StringConsumer consumer) {
      super(consumer);
      this.outputPath = outputPath;
    }

    /** Get the output path that the consumer will write to. */
    public Path getOutputPath() {
      return outputPath;
    }

    /** Set the output encoding. Defaults to UTF8. */
    public void setEncoding(Charset encoding) {
      assert encoding != null;
      if (delegate != null) {
        throw new IllegalStateException("Invalid call to set encoding after file stream is opened");
      }
      this.encoding = encoding;
    }

    /** Get the output encoding. Defaults to UTF8. */
    public Charset getEncoding() {
      return encoding;
    }

    @Override
    public void accept(String string, DiagnosticsHandler handler) {
      super.accept(string, handler);
      if (failedToCreateDelegate) {
        return;
      }
      ensureDelegate(handler);
      if (delegate != null) {
        delegate.accept(string, handler);
      }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      super.finished(handler);
      if (failedToCreateDelegate) {
        return;
      }
      if (delegate != null) {
        delegate.finished(handler);
        delegate = null;
      }
    }

    private void ensureDelegate(DiagnosticsHandler handler) {
      if (delegate != null) {
        return;
      }
      PathOrigin origin = new PathOrigin(outputPath);
      try {
        Path parent = outputPath.getParent();
        if (parent != null && !parent.toFile().exists()) {
          Files.createDirectories(parent);
        }
        delegate = new WriterConsumer(origin, Files.newBufferedWriter(outputPath, encoding));
      } catch (IOException e) {
        failedToCreateDelegate = true;
        handler.error(new ExceptionDiagnostic(e, origin));
      }
    }
  }

  /**
   * String consumer to write contents to a Writer.
   *
   * <p>Note: The writer is closed when the consumer receives its 'finished' callback.
   */
  class WriterConsumer extends ForwardingConsumer {

    private final Origin origin;
    private Writer writer;

    /** Consumer that writes to {@param writer}. */
    public WriterConsumer(Origin origin, Writer writer) {
      this(origin, writer, null);
    }

    /** Consumer that forwards to {@param consumer} and also writes to {@param writer}. */
    public WriterConsumer(Origin origin, Writer writer, StringConsumer consumer) {
      super(consumer);
      this.origin = origin;
      this.writer = writer;
    }

    @Override
    public void accept(String string, DiagnosticsHandler handler) {
      super.accept(string, handler);
      try {
        writer.write(string);
        writer.flush();
      } catch (IOException e) {
        handler.error(new ExceptionDiagnostic(e, origin));
      }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      super.finished(handler);
      try {
        writer.close();
      } catch (IOException e) {
        handler.error(new ExceptionDiagnostic(e, origin));
      }
    }
  }
}
