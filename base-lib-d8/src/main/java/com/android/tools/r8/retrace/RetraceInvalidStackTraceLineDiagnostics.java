// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.position.TextPosition;

@Keep
public class RetraceInvalidStackTraceLineDiagnostics implements Diagnostic {

  private static final String NULL_STACK_TRACE_LINE_MESSAGE = "The stack trace line is <null>";
  private static final String PARSE_STACK_TRACE_LINE_MESSAGE =
      "Could not parse the stack trace line '%s'";

  private final int lineNumber;
  private final String message;

  private RetraceInvalidStackTraceLineDiagnostics(int lineNumber, String message) {
    this.lineNumber = lineNumber;
    this.message = message;
  }

  @Override
  public Origin getOrigin() {
    return Origin.unknown();
  }

  @Override
  public Position getPosition() {
    return new TextPosition(0, lineNumber, TextPosition.UNKNOWN_COLUMN);
  }

  @Override
  public String getDiagnosticMessage() {
    return message;
  }

  public static RetraceInvalidStackTraceLineDiagnostics createNull(int lineNumber) {
    return new RetraceInvalidStackTraceLineDiagnostics(lineNumber, NULL_STACK_TRACE_LINE_MESSAGE);
  }

  public static RetraceInvalidStackTraceLineDiagnostics createParse(int lineNumber, String line) {
    return new RetraceInvalidStackTraceLineDiagnostics(
        lineNumber, String.format(PARSE_STACK_TRACE_LINE_MESSAGE, line));
  }
}
