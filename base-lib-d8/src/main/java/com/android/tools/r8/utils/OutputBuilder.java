// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.origin.Origin;
import java.nio.file.Path;

public interface OutputBuilder {
  char NAME_SEPARATOR = '/';

  void open();

  void close(DiagnosticsHandler handler);

  void addDirectory(String name, DiagnosticsHandler handler);

  void addFile(String name, DataEntryResource content, DiagnosticsHandler handler);

  void addFile(String name, ByteDataView content, DiagnosticsHandler handler);

  void addIndexedClassFile(
      int index, String name, ByteDataView content, DiagnosticsHandler handler);

  Path getPath();

  Origin getOrigin();
}
