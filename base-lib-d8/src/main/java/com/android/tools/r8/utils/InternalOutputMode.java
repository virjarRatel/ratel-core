// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

public enum InternalOutputMode {
  DexIndexed,
  DexFilePerClassFile,
  ClassFile;

  public boolean isGeneratingClassFiles() {
    return this == ClassFile;
  }

  public boolean isGeneratingDex() {
    return this != ClassFile;
  }
}
