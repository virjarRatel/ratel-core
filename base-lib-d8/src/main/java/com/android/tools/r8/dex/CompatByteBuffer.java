// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * In JDK 9 ByteBuffer ByteBuffer.position(int) started overriding Buffer Buffer.position(int) along
 * with various other methods defined on java.nio.Buffer. To avoid issues with running R8 on JDK 8
 * we internally wrap any use of ByteBuffer and make sure access to the overridden methods happens
 * only at the super type Buffer which is known to be in both JDKs.
 */
public class CompatByteBuffer {

  private final ByteBuffer buffer;

  public CompatByteBuffer(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public static CompatByteBuffer wrap(byte[] bytes) {
    return new CompatByteBuffer(ByteBuffer.wrap(bytes));
  }

  private Buffer asBuffer() {
    return buffer;
  }

  public ByteBuffer asByteBuffer() {
    return buffer;
  }

  // ----------------------------------------------------------------------------------------------
  // 1.8 compatible calls to java.nio.Buffer methods.
  // ----------------------------------------------------------------------------------------------

  // The position(int) became overridden with a ByteBuffer return value.
  public void position(int newPosition) {
    asBuffer().position(newPosition);
  }

  // The rewind() became overridden with a ByteBuffer return value.
  public void rewind() {
    asBuffer().rewind();
  }

  // ----------------------------------------------------------------------------------------------
  // Methods on java.nio.Buffer that are safely overridden or not non-overridden.
  // ----------------------------------------------------------------------------------------------

  // Note: array() overrides Buffer.array() in JDK 8 too, so that is safe for JDK 8 and 9.
  public byte[] array() {
    return asByteBuffer().array();
  }

  public int arrayOffset() {
    return asByteBuffer().arrayOffset();
  }

  public int capacity() {
    return asByteBuffer().capacity();
  }

  public boolean hasArray() {
    return asByteBuffer().hasArray();
  }

  public boolean hasRemaining() {
    return asByteBuffer().hasRemaining();
  }

  public int position() {
    return asByteBuffer().position();
  }

  public int remaining() {
    return asByteBuffer().remaining();
  }

  // ----------------------------------------------------------------------------------------------
  // Methods on java.nio.ByteBuffer.
  // ----------------------------------------------------------------------------------------------

  public ShortBuffer asShortBuffer() {
    return asByteBuffer().asShortBuffer();
  }

  public void order(ByteOrder bo) {
    asByteBuffer().order(bo);
  }

  public byte get() {
    return asByteBuffer().get();
  }

  public byte get(int index) {
    return asByteBuffer().get(index);
  }

  public void get(byte[] dst) {
    asByteBuffer().get(dst);
  }

  public int getInt() {
    return asByteBuffer().getInt();
  }

  public int getInt(int offset) {
    return asByteBuffer().getInt(offset);
  }

  public short getShort() {
    return asByteBuffer().getShort();
  }

  public void put(byte aByte) {
    asByteBuffer().put(aByte);
  }

  public void putShort(short aShort) {
    asByteBuffer().putShort(aShort);
  }

  public void putInt(int anInteger) {
    asByteBuffer().putInt(anInteger);
  }

  public void put(byte[] bytes) {
    asByteBuffer().put(bytes);
  }
}
