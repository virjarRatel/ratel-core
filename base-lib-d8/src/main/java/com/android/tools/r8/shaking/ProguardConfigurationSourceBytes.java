// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProguardConfigurationSourceBytes implements ProguardConfigurationSource {
  private final byte[] bytes;
  private final Origin origin;

  public ProguardConfigurationSourceBytes(byte[] bytes, Origin origin) {
    this.bytes = bytes;
    this.origin = origin;
  }

  public ProguardConfigurationSourceBytes(InputStream in, Origin origin) throws IOException {
    this(ByteStreams.toByteArray(in), origin);
  }

  @Override
  public String get() throws IOException {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public Path getBaseDirectory() {
    // Options with relative names (including -include) is not supported.
    return null;
  }

  @Override
  public String getName() {
    return origin.toString();
  }

  @Override
  public Origin getOrigin() {
    return origin;
  }
}
