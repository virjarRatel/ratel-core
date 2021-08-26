// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

/**
 * Exception thrown when compilation failed to complete because of errors previously reported
 * through {@link com.android.tools.r8.DiagnosticsHandler}.
 */
@Keep
public class CompilationFailedException extends Exception {

  public CompilationFailedException() {
    super("Compilation failed to complete");
  }

  public CompilationFailedException(Throwable cause) {
    super("Compilation failed to complete", cause);
  }

  public CompilationFailedException(String message) {
    super(message);
  }
}
