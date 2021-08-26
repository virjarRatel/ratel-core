// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.Diagnostic;

public abstract class DiagnosticWithThrowable implements Diagnostic {

  private final Throwable throwable;

  protected DiagnosticWithThrowable(Throwable throwable) {
    assert throwable != null;
    this.throwable = throwable;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
