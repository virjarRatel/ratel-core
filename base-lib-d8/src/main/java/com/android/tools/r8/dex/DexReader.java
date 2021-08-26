// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.DEX_FILE_MAGIC_PREFIX;

import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.DexVersion;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Optional;

/**
 * {@link BinaryReader} for Dex content.
 */
public class DexReader extends BinaryReader {

  private final DexVersion version;

  public DexReader(ProgramResource resource) throws ResourceException, IOException {
    super(resource);
    version = parseMagic(buffer);
  }

  /**
   * Returns a File that contains the bytes provided as argument. Used for testing.
   *
   * @param bytes contents of the file
   */
  DexReader(Origin origin, byte[] bytes) {
    super(origin, bytes);
    version = parseMagic(buffer);
  }

  // Parse the magic header and determine the dex file version.
  private DexVersion parseMagic(CompatByteBuffer buffer) {
    try {
      buffer.get();
      buffer.rewind();
    } catch (BufferUnderflowException e) {
      throw new CompilationError("Dex file is empty", origin);
    }
    int index = 0;
    for (byte prefixByte : DEX_FILE_MAGIC_PREFIX) {
      if (buffer.get(index++) != prefixByte) {
        throw new CompilationError("Dex file has invalid header", origin);
      }
    }

    char versionByte0 = (char) buffer.get(index++);
    char versionByte1 = (char) buffer.get(index++);
    char versionByte2 = (char) buffer.get(index++);
    Optional<DexVersion> maybeVersion =
        DexVersion.getDexVersion(versionByte0, versionByte1, versionByte2);
    if (!maybeVersion.isPresent()) {
      throw new CompilationError(
          "Unsupported DEX file version: "
              + versionByte0
              + versionByte1
              + versionByte2,
          origin);
    }
    if (buffer.get(index++) != '\0') {
      throw new CompilationError("Dex file has invalid header", origin);
    }
    return maybeVersion.get();
  }

  @Override
  void setByteOrder() {
    // Make sure we set the right endian for reading.
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    int endian = buffer.getInt(Constants.ENDIAN_TAG_OFFSET);
    if (endian == Constants.REVERSE_ENDIAN_CONSTANT) {
      buffer.order(ByteOrder.BIG_ENDIAN);
    } else {
      if (endian != Constants.ENDIAN_CONSTANT) {
        throw new CompilationError("Unable to determine endianess for reading dex file.");
      }
    }
  }

  DexVersion getDexVersion() {
    return version;
  }
}
