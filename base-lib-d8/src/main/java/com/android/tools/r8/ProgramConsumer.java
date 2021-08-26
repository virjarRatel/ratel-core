// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

/**
 * Base for all program consumers to allow abstracting which concrete consumer is provided to D8/R8.
 */
@KeepForSubclassing
public interface ProgramConsumer {

  /**
   * Returns a {@link DataResourceConsumer} that will receive data resources. If this
   * returns <code>null</code> no data resources will be processed.
   */
  default DataResourceConsumer getDataResourceConsumer() {
    return null;
  }

  /**
   * Callback signifying that compilation of program resources has finished.
   *
   * <p>Called only once after all program outputs have been generated and consumed.
   *
   * @param handler Diagnostics handler for reporting.
   */
  void finished(DiagnosticsHandler handler);

}
