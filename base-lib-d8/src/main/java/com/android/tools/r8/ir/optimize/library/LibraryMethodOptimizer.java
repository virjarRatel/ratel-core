// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.library;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.CodeOptimization;
import com.android.tools.r8.ir.conversion.MethodProcessor;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class LibraryMethodOptimizer implements CodeOptimization {

  private final Map<DexType, LibraryMethodModelCollection> libraryMethodModelCollections =
      new IdentityHashMap<>();

  public LibraryMethodOptimizer(AppView<? extends AppInfoWithSubtyping> appView) {
    register(new BooleanMethodOptimizer(appView));
  }

  private void register(LibraryMethodModelCollection optimizer) {
    LibraryMethodModelCollection existing =
        libraryMethodModelCollections.put(optimizer.getType(), optimizer);
    assert existing == null;
  }

  @Override
  public void optimize(
      AppView<?> appView,
      IRCode code,
      OptimizationFeedback feedback,
      MethodProcessor methodProcessor) {
    Set<Value> affectedValues = Sets.newIdentityHashSet();
    InstructionListIterator instructionIterator = code.instructionListIterator();
    while (instructionIterator.hasNext()) {
      Instruction instruction = instructionIterator.next();
      if (instruction.isInvokeMethod()) {
        InvokeMethod invoke = instruction.asInvokeMethod();
        DexEncodedMethod singleTarget =
            invoke.lookupSingleTarget(appView, code.method.method.holder);
        if (singleTarget != null) {
          optimizeInvoke(code, instructionIterator, invoke, singleTarget, affectedValues);
        }
      }
    }
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
  }

  private void optimizeInvoke(
      IRCode code,
      InstructionListIterator instructionIterator,
      InvokeMethod invoke,
      DexEncodedMethod singleTarget,
      Set<Value> affectedValues) {
    LibraryMethodModelCollection optimizer =
        libraryMethodModelCollections.getOrDefault(
            singleTarget.method.holder, NopLibraryMethodModelCollection.getInstance());
    optimizer.optimize(code, instructionIterator, invoke, singleTarget, affectedValues);
  }
}
