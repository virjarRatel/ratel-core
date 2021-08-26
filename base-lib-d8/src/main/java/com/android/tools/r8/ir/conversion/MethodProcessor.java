// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.DexEncodedMethod;

public interface MethodProcessor {

  enum Phase {
    ONE_TIME,
    LAMBDA_PROCESSING,
    PRIMARY,
    POST
  }

  Phase getPhase();

  default boolean isPrimary() {
    return getPhase() == Phase.PRIMARY;
  }

  default CallSiteInformation getCallSiteInformation() {
    return CallSiteInformation.empty();
  }

  boolean isProcessedConcurrently(DexEncodedMethod method);
}
