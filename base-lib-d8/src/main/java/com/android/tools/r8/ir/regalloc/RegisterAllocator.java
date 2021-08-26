// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.InternalOptions;
import java.util.List;

public interface RegisterAllocator {
  void allocateRegisters();
  int registersUsed();
  int getRegisterForValue(Value value, int instructionNumber);
  int getArgumentOrAllocateRegisterForValue(Value value, int instructionNumber);

  InternalOptions options();

  void mergeBlocks(BasicBlock kept, BasicBlock removed);

  boolean hasEqualTypesAtEntry(BasicBlock first, BasicBlock second);

  // Call before removing the suffix from the preds. Block is used only as a key.
  void addNewBlockToShareIdenticalSuffix(
      BasicBlock block, int suffixSize, List<BasicBlock> predsBeforeSplit);
}
