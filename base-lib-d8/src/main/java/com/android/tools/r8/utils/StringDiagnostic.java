// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

/**
 * A diagnostic message.
 *
 * <p>This string diagnostic is used to report various error messages from the compiler. There is
 * no guarantee that the message will remain the same or that the issue will continue to be reported
 * by way of a {@link StringDiagnostic}. If there is a need to reliably identify a particular kind
 * of error which is currently reported via a {@link StringDiagnostic}, then please file a feature
 * request on D8/R8.
 */
@Keep
public class StringDiagnostic implements Diagnostic {

  private final Origin origin;
  private final Position position;
  private final String message;

  public StringDiagnostic(String message) {
    this(message, Origin.unknown());
  }

  public StringDiagnostic(String message, Origin origin) {
    this(message, origin, Position.UNKNOWN);
  }

  public StringDiagnostic(String message, Origin origin, Position position) {
    this.origin = origin;
    this.position = position;
    this.message = message;
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
    return message;
  }
}
