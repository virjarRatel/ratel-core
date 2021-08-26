// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.Unreachable;
import java.util.Optional;

/**
 * Android dex version
 */
public enum DexVersion {
  V35(35, new byte[]{'0', '3', '5'}),
  V37(37, new byte[]{'0', '3', '7'}),
  V38(38, new byte[]{'0', '3', '8'}),
  V39(39, new byte[]{'0', '3', '9'});

  private final int dexVersion;

  private final byte[] dexVersionBytes;

  DexVersion(int dexVersion, byte[] dexVersionBytes) {
    this.dexVersion = dexVersion;
    this.dexVersionBytes = dexVersionBytes;
  }

  public int getIntValue() {
    return dexVersion;
  }

  public byte[] getBytes() {
    return dexVersionBytes;
  }

  public boolean matchesApiLevel(AndroidApiLevel androidApiLevel) {
    return getDexVersion(androidApiLevel).dexVersion >= dexVersion;
  }

  public static DexVersion getDexVersion(AndroidApiLevel androidApiLevel) {
    switch (androidApiLevel) {
      case Q:
      case P:
        return DexVersion.V39;
      case O_MR1:
      case O:
        return DexVersion.V38;
      case N_MR1:
      case N:
        return DexVersion.V37;
      case B:
      case B_1_1:
      case C:
      case D:
      case E:
      case E_0_1:
      case E_MR1:
      case F:
      case G:
      case G_MR1:
      case H:
      case H_MR1:
      case H_MR2:
      case I:
      case I_MR1:
      case J:
      case J_MR1:
      case J_MR2:
      case K:
      case K_WATCH:
      case L:
      case L_MR1:
      case M:
        return DexVersion.V35;
      default :
        throw new Unreachable("Unsupported api level " + androidApiLevel);
    }
  }

  public static Optional<DexVersion> getDexVersion(int intValue) {
    switch (intValue) {
      case 35:
        return Optional.of(V35);
      case 37:
        return Optional.of(V37);
      case 38:
        return Optional.of(V38);
      case 39:
        return Optional.of(V39);
      default:
        return Optional.empty();
    }
  }

  public static Optional<DexVersion> getDexVersion(char b0, char b1, char b2) {
    if (b0 != '0' || b1 != '3' || b2 < '5' || '9' < b2) {
      return Optional.empty();
    }
    return getDexVersion(100 * (b0 - '0') + 10 * (b1 - '0') + (b2 - '0'));
  }
}
