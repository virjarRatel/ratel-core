// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
  /**
   * Read all data from the stream into a byte[], close the stream and return the bytes.
   * @return The bytes of the stream
   */
  public static byte[] StreamToByteArrayClose(InputStream inputStream) throws IOException {
    byte[] result = ByteStreams.toByteArray(inputStream);
    inputStream.close();
    return result;
  }
}
