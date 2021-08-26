// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.errors;

import com.android.tools.r8.KeepForSubclassing;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

@KeepForSubclassing
public class NestDesugarDiagnostic implements DesugarDiagnostic {

  private final Origin origin;
  private final Position position;
  private final String message;

  public NestDesugarDiagnostic(Origin origin, Position position, String message) {
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
