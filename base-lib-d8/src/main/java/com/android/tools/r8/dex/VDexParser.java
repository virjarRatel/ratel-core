// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.VDEX_NUMBER_OF_DEX_FILES_OFFSET;

import com.android.tools.r8.errors.CompilationError;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a VDEX and isolate the different dex files it contains.
 */
public class VDexParser {

  private VDexReader vDexReader;
  private List<byte[]> dexFiles = new ArrayList<>();

  public void close() {
    vDexReader = null;
    dexFiles = ImmutableList.of();
  }

  public VDexParser(VDexReader vDexReader) {
    this.vDexReader = vDexReader;
    vDexReader.setByteOrder();
    parseDexFiles();
  }

  public List<byte[]> getDexFiles() {
    return dexFiles;
  }

  private void parseDexFiles() {
    vDexReader.position(VDEX_NUMBER_OF_DEX_FILES_OFFSET);
    int numberOfDexFiles = vDexReader.getUint();
    int dexSize = vDexReader.getUint();
    int verifierDepsSize = vDexReader.getUint();
    int quickeningInfoSize = vDexReader.getUint();

    int offset = VDexReader.firstDexOffset(numberOfDexFiles);
    int totalDexSize = 0;
    for (int i = 0; i < numberOfDexFiles; i++) {
      int size = vDexReader.getUint(offset + Constants.FILE_SIZE_OFFSET);
      vDexReader.position(offset);
      dexFiles.add(vDexReader.getByteArray(size));
      totalDexSize += size;
      offset += size;
    }
    if (totalDexSize != dexSize) {
      throw new CompilationError("Invalid vdex file. Mismatch in total dex files size");
    }
  }
}
