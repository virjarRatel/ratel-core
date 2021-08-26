// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

public class ProguardRuleParserException extends Exception implements Diagnostic {

  private final String message;
  private final String snippet;
  private final Origin origin;
  private final Position position;

  public ProguardRuleParserException(String message, String snippet, Origin origin,
      Position position) {
    this.message = message;
    this.snippet = snippet;
    this.origin = origin;
    this.position = position;
  }

  public ProguardRuleParserException(String message, String snippet, Origin origin,
      Position position, Throwable cause) {
    this(message, snippet,origin, position);
    initCause(cause);
  }

  @Override
  public Origin getOrigin() {
    return origin;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public String getDiagnosticMessage() {
    return message + " at " + snippet;
  }

  @Override
  public String getMessage() {
    return message + " at " + snippet;
  }

  public String getParseError() {
    return message;
  }

  public String getSnippet() {
    return snippet;
  }
}
