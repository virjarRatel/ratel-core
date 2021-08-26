// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import java.nio.ByteBuffer;

/** Interface to enable manual memory management for a pool of byte buffers. */
@KeepForSubclassing
public interface ByteBufferProvider {

  /**
   * Acquire unique ownership of a {@link ByteBuffer}.
   *
   * <p>The buffer must have an array backing and must have capacity of at least the requested
   * {@param capacity} value. The buffer must be positioned at position zero.
   *
   * <p>The buffer is owned by the caller until it is explicitly given back by a call to {@link
   * ByteBufferProvider::releaseByteBuffer}.
   *
   * <p>Requests for byte buffers can happen in parallel with no guarantees of thread or order.
   */
  default ByteBuffer acquireByteBuffer(int capacity) {
    return ByteBuffer.allocate(capacity);
  }

  /**
   * Release ownership of a previously acquired byte buffer.
   *
   * <p>After a byte buffer is released it is free to be reclaimed or reused by other requests.
   *
   * <p>The release of a buffer will only happen once for each acquired buffer and it will happen
   * only on the same thread that acquired it.
   */
  default void releaseByteBuffer(ByteBuffer buffer) {
    // Implicitly reclaimed by GC.
  }
}
