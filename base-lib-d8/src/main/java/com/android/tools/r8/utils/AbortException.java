// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

/**
 * Exception thrown to interrupt processing after a fatal error. The exception doesn't carry
 * directly information about the fatal error: the problem was already reported to the
 * {@link com.android.tools.r8.DiagnosticsHandler}.
 */
public class AbortException extends RuntimeException {
  public AbortException() {

  }

  public AbortException(String message) {
    super(message);
  }
}
