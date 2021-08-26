// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

/**
 * A DiagnosticsHandler can be provided to customize handling of diagnostics information.
 *
 * <p>During compilation the warning and info methods will be called.
 */
@Keep
public interface DiagnosticsHandler {

  /**
   * Handle error diagnostics.
   *
   * @param error Diagnostic containing error information.
   */
  default void error(Diagnostic error) {
    if (error.getOrigin() != Origin.unknown()) {
      System.err.print("Error in " + error.getOrigin());
      if (error.getPosition() != Position.UNKNOWN) {
        System.err.print(" at " + error.getPosition().getDescription());
      }
      System.err.println(":");
    } else {
      System.err.print("Error: ");
    }
    System.err.println(error.getDiagnosticMessage());
  }

  /**
   * Handle warning diagnostics.
   *
   * @param warning Diagnostic containing warning information.
   */
  default void warning(Diagnostic warning) {
    if (warning.getOrigin() != Origin.unknown()) {
      System.err.print("Warning in " + warning.getOrigin() + ":\n  ");
    } else {
      System.err.print("Warning: ");
    }
    System.err.println(warning.getDiagnosticMessage());
  }

  /**
   * Handle info diagnostics.
   *
   * @param info Diagnostic containing the information.
   */
  default void info(Diagnostic info) {
    if (info.getOrigin() != Origin.unknown()) {
      System.out.print("In " + info.getOrigin() + ":\n  ");
    }
    System.out.println(info.getDiagnosticMessage());
  }
}
