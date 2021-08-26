// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;

/** Consumer for receiving dependency edges for desugaring. */
@KeepForSubclassing
public interface DesugarGraphConsumer {

  /**
   * Callback indicating that code originating from {@code src} was used to correctly desugar some
   * code originating from {@code dst}.
   *
   * <p>In other words, {@code src} is a dependency for the desugaring of {@code dst}.
   *
   * <p>Note: this callback may be called on multiple threads.
   *
   * <p>Note: this callback places no guarantees on order of calls or on duplicate calls.
   *
   * @param src Origin of some code input that is needed to desugar {@code dst}.
   * @param dst Origin of some code that was dependent on code in {@code src}.
   */
  void accept(Origin src, Origin dst);
}
