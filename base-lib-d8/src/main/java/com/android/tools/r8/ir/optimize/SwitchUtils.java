// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.ArrayGet;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.AppInfoWithLiveness.EnumValueInfo;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.Map;

public class SwitchUtils {

  public static final class EnumSwitchInfo {
    public final DexType enumClass;
    public final Instruction ordinalInvoke;
    public final Instruction arrayGet;
    public final Instruction staticGet;
    public final Int2ReferenceMap<DexField> indexMap;
    public final Map<DexField, EnumValueInfo> valueInfoMap;

    private EnumSwitchInfo(DexType enumClass,
        Instruction ordinalInvoke,
        Instruction arrayGet, Instruction staticGet,
        Int2ReferenceMap<DexField> indexMap,
        Map<DexField, EnumValueInfo> valueInfoMap) {
      this.enumClass = enumClass;
      this.ordinalInvoke = ordinalInvoke;
      this.arrayGet = arrayGet;
      this.staticGet = staticGet;
      this.indexMap = indexMap;
      this.valueInfoMap = valueInfoMap;
    }
  }

  /**
   * Looks for a switch statement over the enum companion class of the form
   *
   * <blockquote>
   *
   * <pre>
   * switch(CompanionClass.$switchmap$field[enumValue.ordinal()]) {
   *   ...
   * }
   * </pre>
   *
   * </blockquote>
   *
   * and extracts the components and the index and ordinal maps. See {@link EnumInfoMapCollector}
   * and {@link SwitchMapCollector} for details.
   */
  public static EnumSwitchInfo analyzeSwitchOverEnum(
      Instruction switchInsn, AppView<AppInfoWithLiveness> appView) {
    AppInfoWithLiveness appInfo = appView.appInfo();
    Instruction input = switchInsn.inValues().get(0).definition;
    if (input == null || !input.isArrayGet()) {
      return null;
    }
    ArrayGet arrayGet = input.asArrayGet();
    Instruction index = arrayGet.index().definition;
    if (index == null || !index.isInvokeVirtual()) {
      return null;
    }
    InvokeVirtual ordinalInvoke = index.asInvokeVirtual();
    DexMethod ordinalMethod = ordinalInvoke.getInvokedMethod();
    DexClass enumClass = appInfo.definitionFor(ordinalMethod.holder);
    DexItemFactory dexItemFactory = appInfo.dexItemFactory();
    // After member rebinding, enumClass will be the actual java.lang.Enum class.
    if (enumClass == null
        || (!enumClass.accessFlags.isEnum() && enumClass.type != dexItemFactory.enumType)
        || ordinalMethod.name != dexItemFactory.ordinalMethodName
        || ordinalMethod.proto.returnType != dexItemFactory.intType
        || !ordinalMethod.proto.parameters.isEmpty()) {
      return null;
    }
    Instruction array = arrayGet.array().definition;
    if (array == null || !array.isStaticGet()) {
      return null;
    }
    StaticGet staticGet = array.asStaticGet();
    Int2ReferenceMap<DexField> indexMap = appInfo.getSwitchMapFor(staticGet.getField());
    if (indexMap == null || indexMap.isEmpty()) {
      return null;
    }
    // Due to member rebinding, only the fields are certain to provide the actual enums
    // class.
    DexType enumType = indexMap.values().iterator().next().holder;
    Map<DexField, EnumValueInfo> valueInfoMap = appInfo.getEnumValueInfoMapFor(enumType);
    if (valueInfoMap == null) {
      return null;
    }
    return new EnumSwitchInfo(enumType, ordinalInvoke, arrayGet, staticGet, indexMap,
        valueInfoMap);
  }


}
