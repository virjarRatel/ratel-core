// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.IdentifierNameStringUtils;

public class ConstantValueUtils {

  /**
   * If the given value is a constant class, then returns the corresponding {@link DexType}.
   * Otherwise returns null.
   */
  public static DexType getDexTypeRepresentedByValue(
      Value value, DexDefinitionSupplier definitions) {
    Value alias = value.getAliasedValue();
    if (alias.isPhi()) {
      return null;
    }

    if (alias.definition.isConstClass()) {
      return alias.definition.asConstClass().getValue();
    }

    if (alias.definition.isInvokeStatic()) {
      InvokeStatic invoke = alias.definition.asInvokeStatic();
      if (definitions.dexItemFactory().classMethods
          .isReflectiveClassLookup(invoke.getInvokedMethod())) {
        return getDexTypeFromClassForName(invoke, definitions);
      }
    }

    return null;
  }

  public static DexType getDexTypeFromClassForName(
      InvokeStatic invoke, DexDefinitionSupplier definitions) {
    assert definitions.dexItemFactory().classMethods
        .isReflectiveClassLookup(invoke.getInvokedMethod());
    if (invoke.arguments().size() == 1 || invoke.arguments().size() == 3) {
      Value argument = invoke.arguments().get(0);
      if (argument.isConstString()) {
        ConstString constStringInstruction = argument.getConstInstruction().asConstString();
        return IdentifierNameStringUtils.inferTypeFromNameString(
            definitions, constStringInstruction.getValue());
      }
      if (argument.isDexItemBasedConstString()) {
        DexItemBasedConstString constStringInstruction =
            argument.getConstInstruction().asDexItemBasedConstString();
        DexReference item = constStringInstruction.getItem();
        if (item.isDexType()) {
          return item.asDexType();
        }
      }
    }
    return null;
  }
}
