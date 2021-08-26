// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.MAX_VDEX_VERSION;
import static com.android.tools.r8.dex.Constants.MIN_VDEX_VERSION;
import static com.android.tools.r8.dex.Constants.VDEX_FILE_MAGIC_PREFIX;
import static com.android.tools.r8.dex.Constants.VDEX_FILE_VERSION_LENGTH;
import static com.android.tools.r8.dex.Constants.VDEX_NUMBER_OF_DEX_FILES_OFFSET;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * See runtime/vdex_file.h and runtime/vdex_file.cc in the Art code for the vdex file format.
 */
public class VDexReader extends BinaryReader {

  private final int version;

  public VDexReader(Origin origin, InputStream stream) throws IOException {
    super(origin, ByteStreams.toByteArray(stream));
    version = parseMagic(buffer);
    if (!supportedVersion(version)) {
      throw new CompilationError("Unsupported vdex file version " + version, origin);
    }
  }

  private static boolean supportedVersion(int versionNumber) {
    return MIN_VDEX_VERSION <= versionNumber && versionNumber <= MAX_VDEX_VERSION;
  }

  // Parse the magic header and determine the dex file version.
  private int parseMagic(CompatByteBuffer buffer) {
    int index = 0;
    for (byte prefixByte : VDEX_FILE_MAGIC_PREFIX) {
      if (buffer.get(index++) != prefixByte) {
        throw new CompilationError("VDex file has invalid header", origin);
      }
    }
    byte[] version = new byte[VDEX_FILE_VERSION_LENGTH];
    for (int i = 0; i < VDEX_FILE_VERSION_LENGTH; i++) {
      if (!buffer.hasRemaining()) {
        throw new CompilationError("Truncated VDex file - unable to read version", origin);
      }
      version[i] = buffer.get(index++);
    }
    if (version[VDEX_FILE_VERSION_LENGTH - 1] != '\0') {
      throw new CompilationError("VDex file has invalid version number", origin);
    }
    int versionNumber = 0;
    for (int i = 0; i < VDEX_FILE_VERSION_LENGTH - 1;i++) {
      if (0x30 <= version[i] && version[i] <= 0x39) {
        versionNumber = versionNumber * 10 + version[i] - 0x30;
      } else {
        throw new CompilationError("VDex file has invalid version number", origin);
      }
    }
    return versionNumber;
  }

  public static int firstDexOffset(int numberOfDexFiles) {
    return Constants.VDEX_CHECKSUM_SECTION_OFFSET
        + numberOfDexFiles * Constants.VDEX_DEX_CHECKSUM_SIZE;
  }

  @Override
  void setByteOrder() {
    // Make sure we set the right endian for reading.
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    int dexFiles = buffer.getInt(VDEX_NUMBER_OF_DEX_FILES_OFFSET);
    // Reading a strange number of dex files indicate reading with the wrong endian.
    if (dexFiles < 0 || dexFiles > 1000) {
      buffer.order(ByteOrder.BIG_ENDIAN);
      dexFiles = buffer.getInt(VDEX_NUMBER_OF_DEX_FILES_OFFSET);
      assert dexFiles < 0 || dexFiles > 1000;
    }

    // Make sure we did set the right endian for reading.
    int endian = buffer.getInt(firstDexOffset(dexFiles) + Constants.ENDIAN_TAG_OFFSET);
    if (endian != Constants.ENDIAN_CONSTANT) {
      throw new CompilationError("Unable to determine endianess for reading vdex file.");
    }
  }

  int getVDexVersion() {
    return version;
  }
}
