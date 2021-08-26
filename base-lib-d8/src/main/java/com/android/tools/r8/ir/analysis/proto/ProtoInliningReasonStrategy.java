// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.analysis.proto.ProtoReferences.MethodToInvokeMembers;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.inliner.InliningReasonStrategy;

/**
 * Equivalent to the {@link InliningReasonStrategy} in {@link #parent} except for invocations to
 * dynamicMethod().
 */
public class ProtoInliningReasonStrategy implements InliningReasonStrategy {

  private static final int METHOD_TO_INVOKE_ARGUMENT_POSITION_IN_DYNAMIC_METHOD = 1;

  private final InliningReasonStrategy parent;
  private final ProtoReferences references;

  public ProtoInliningReasonStrategy(AppView<?> appView, InliningReasonStrategy parent) {
    this.parent = parent;
    this.references = appView.protoShrinker().references;
  }

  @Override
  public Reason computeInliningReason(InvokeMethod invoke, DexEncodedMethod target) {
    return references.isDynamicMethod(target)
        ? computeInliningReasonForDynamicMethod(invoke)
        : parent.computeInliningReason(invoke, target);
  }

  private Reason computeInliningReasonForDynamicMethod(InvokeMethod invoke) {
    Value methodToInvokeValue =
        invoke
            .inValues()
            .get(METHOD_TO_INVOKE_ARGUMENT_POSITION_IN_DYNAMIC_METHOD)
            .getAliasedValue();
    if (methodToInvokeValue.isPhi()) {
      return Reason.NEVER;
    }

    Instruction methodToInvokeDefinition = methodToInvokeValue.definition;
    if (!methodToInvokeDefinition.isStaticGet()) {
      return Reason.NEVER;
    }

    DexField field = methodToInvokeDefinition.asStaticGet().getField();
    MethodToInvokeMembers methodToInvokeMembers = references.methodToInvokeMembers;
    if (methodToInvokeMembers.isMethodToInvokeWithSimpleBody(field)) {
      return Reason.ALWAYS;
    }

    assert field.holder != references.methodToInvokeType
        || methodToInvokeMembers.isMethodToInvokeWithNonSimpleBody(field);

    return Reason.NEVER;
  }
}
