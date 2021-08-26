// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.errors;

import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

@Keep
public class IncompleteNestNestDesugarDiagnosic extends NestDesugarDiagnostic {

  public IncompleteNestNestDesugarDiagnosic(Origin origin, Position position, String message) {
    super(origin, position, message);
  }
}
