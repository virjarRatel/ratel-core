// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import java.util.Arrays;

/** Byte data view of a buffer that is possibly larger than the data content. */
@Keep
public final class ByteDataView {
  private byte[] buffer;
  private final int offset;
  private final int length;

  /** Create a view spanning an entire byte array. */
  public static ByteDataView of(byte[] data) {
    return new ByteDataView(data, 0, data.length);
  }

  /**
   * Create a view of a byte array.
   *
   * <p>The view starts at {@param offset} and runs {@param length} bytes.
   */
  public ByteDataView(byte[] buffer, int offset, int length) {
    assert offset >= 0;
    assert length >= 0;
    assert offset + length <= buffer.length;
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;
  }

  /** Get the full backing buffer of the byte data view. */
  public byte[] getBuffer() {
    assert buffer != null;
    return buffer;
  }

  /** Get the offset at which the view starts. */
  public int getOffset() {
    assert buffer != null;
    return offset;
  }

  /**
   * Get the length of the data content.
   *
   * <p>Note that this does not include the offset.
   */
  public int getLength() {
    assert buffer != null;
    return length;
  }

  /** Get a copy of the data truncated to the actual data view. */
  public byte[] copyByteData() {
    return Arrays.copyOfRange(buffer, offset, offset + length);
  }

  public void invalidate() {
    buffer = null;
  }
}
