// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.AppInfoWithLiveness.EnumValueInfo;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Extracts the ordinal values and any anonymous subtypes for all Enum classes from their static
 * initializer.
 * <p>
 * An Enum class has a field for each value. In the class initializer, each field is initialized
 * to a singleton object that represents the value. This code matches on the corresponding call
 * to the constructor (instance initializer) and extracts the value of the second argument, which
 * is the ordinal and the holder which is the concrete type.
 */
public class EnumInfoMapCollector {

  private final AppView<AppInfoWithLiveness> appView;

  private final Map<DexType, Map<DexField, EnumValueInfo>> valueInfoMaps = new IdentityHashMap<>();

  public EnumInfoMapCollector(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
  }

  public AppInfoWithLiveness run() {
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      processClasses(clazz);
    }
    if (!valueInfoMaps.isEmpty()) {
      return appView.appInfo().addEnumValueInfoMaps(valueInfoMaps);
    }
    return appView.appInfo();
  }

  private void processClasses(DexProgramClass clazz) {
    // Enum classes are flagged as such. Also, for library classes, the ordinals are not known.
    if (!clazz.accessFlags.isEnum() || clazz.isNotProgramClass() || !clazz.hasClassInitializer()) {
      return;
    }
    DexEncodedMethod initializer = clazz.getClassInitializer();
    IRCode code = initializer.getCode().buildIR(initializer, appView, clazz.origin);
    Map<DexField, EnumValueInfo> valueInfoMap = new IdentityHashMap<>();
    for (Instruction insn : code.instructions()) {
      if (!insn.isStaticPut()) {
        continue;
      }
      StaticPut staticPut = insn.asStaticPut();
      if (staticPut.getField().type != clazz.type) {
        continue;
      }
      Instruction newInstance = staticPut.value().definition;
      if (newInstance == null || !newInstance.isNewInstance()) {
        continue;
      }
      Instruction ordinal = null;
      DexType type = null;
      for (Instruction ctorCall : newInstance.outValue().uniqueUsers()) {
        if (!ctorCall.isInvokeDirect()) {
          continue;
        }
        InvokeDirect invoke = ctorCall.asInvokeDirect();
        if (!appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())
            || invoke.arguments().size() < 3) {
          continue;
        }
        ordinal = invoke.arguments().get(2).definition;
        type = invoke.getInvokedMethod().holder;
        break;
      }
      if (ordinal == null || !ordinal.isConstNumber() || type == null) {
        return;
      }

      EnumValueInfo info = new EnumValueInfo(type, ordinal.asConstNumber().getIntValue());
      if (valueInfoMap.put(staticPut.getField(), info) != null) {
        return;
      }
    }
    valueInfoMaps.put(clazz.type, valueInfoMap);
  }
}
