// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.library;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import java.util.Set;

public class NopLibraryMethodModelCollection implements LibraryMethodModelCollection {

  private static final NopLibraryMethodModelCollection INSTANCE =
      new NopLibraryMethodModelCollection();

  private NopLibraryMethodModelCollection() {}

  public static NopLibraryMethodModelCollection getInstance() {
    return INSTANCE;
  }

  @Override
  public DexType getType() {
    throw new Unreachable();
  }

  @Override
  public void optimize(
      IRCode code,
      InstructionListIterator instructionIterator,
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      Set<Value> affectedValues) {}
}
