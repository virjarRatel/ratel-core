// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.base.Predicates;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class CallGraphBuilder extends CallGraphBuilderBase {

  CallGraphBuilder(AppView<AppInfoWithLiveness> appView) {
    super(appView);
  }

  @Override
  void process(ExecutorService executorService) throws ExecutionException {
    ThreadUtils.processItems(appView.appInfo().classes(), this::processClass, executorService);
  }

  private void processClass(DexProgramClass clazz) {
    clazz.forEachMethod(this::processMethod);
  }

  private void processMethod(DexEncodedMethod method) {
    if (method.hasCode()) {
      method.registerCodeReferences(
          new InvokeExtractor(getOrCreateNode(method), Predicates.alwaysTrue()));
    }
  }

  @Override
  boolean verifyAllMethodsWithCodeExists() {
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      for (DexEncodedMethod method : clazz.methods()) {
        assert !method.hasCode() || nodes.get(method.method) != null;
      }
    }
    return true;
  }
}
