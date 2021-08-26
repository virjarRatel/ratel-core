// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

@KeepForSubclassing
public interface DataResourceConsumer {

  void accept(DataDirectoryResource directory, DiagnosticsHandler diagnosticsHandler);
  void accept(DataEntryResource file, DiagnosticsHandler diagnosticsHandler);
  void finished(DiagnosticsHandler handler);

}
