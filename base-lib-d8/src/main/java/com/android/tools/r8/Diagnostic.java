// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

/**
 * Interface for all diagnostic message produced by D8 and R8.
 */
@Keep
public interface Diagnostic {

  /**
   * Origin of the resource causing the problem.
   * @return Origin.unknown() when origin is not available.
   */
  Origin getOrigin();

  /**
   * Position of the problem in the origin.
   * @return {@link Position#UNKNOWN} when position is not available.
   */
  Position getPosition();

  /**
   * User friendly description of the problem.
   */
  String getDiagnosticMessage();
}
