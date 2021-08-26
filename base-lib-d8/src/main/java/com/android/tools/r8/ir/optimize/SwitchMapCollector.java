// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Extracts the mapping from ordinal values to switch case constants.
 * <p>
 * This is done by pattern-matching on the class initializer of the synthetic switch map class.
 * For a switch
 *
 * <blockquote><pre>
 * switch (day) {
 *   case WEDNESDAY:
 *   case FRIDAY:
 *     System.out.println("3 or 5");
 *     break;
 *   case SUNDAY:
 *     System.out.println("7");
 *     break;
 *   default:
 *     System.out.println("other");
 * }
 * </pre></blockquote>
 *
 * the generated companing class initializer will have the form
 *
 * <blockquote><pre>
 * class Switches$1 {
 *   static {
 *   $SwitchMap$switchmaps$Days[Days.WEDNESDAY.ordinal()] = 1;
 *   $SwitchMap$switchmaps$Days[Days.FRIDAY.ordinal()] = 2;
 *   $SwitchMap$switchmaps$Days[Days.SUNDAY.ordinal()] = 3;
 * }
 * </pre></blockquote>
 *
 * Note that one map per class is generated, so the map might contain additional entries as used
 * by other switches in the class.
 */
public class SwitchMapCollector {

  private final AppView<AppInfoWithLiveness> appView;
  private final DexString switchMapPrefix;
  private final DexString kotlinSwitchMapPrefix;
  private final DexType intArrayType;

  private final Map<DexField, Int2ReferenceMap<DexField>> switchMaps = new IdentityHashMap<>();

  public SwitchMapCollector(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;

    DexItemFactory dexItemFactory = appView.dexItemFactory();
    switchMapPrefix = dexItemFactory.createString("$SwitchMap$");
    kotlinSwitchMapPrefix = dexItemFactory.createString("$EnumSwitchMapping$");
    intArrayType = dexItemFactory.createType("[I");
  }

  public AppInfoWithLiveness run() {
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      processClasses(clazz);
    }
    if (!switchMaps.isEmpty()) {
      return appView.appInfo().addSwitchMaps(switchMaps);
    }
    return appView.appInfo();
  }

  private void processClasses(DexProgramClass clazz) {
    // Switchmap classes are synthetic and have a class initializer.
    if (!clazz.accessFlags.isSynthetic() && !clazz.hasClassInitializer()) {
      return;
    }
    List<DexEncodedField> switchMapFields = clazz.staticFields().stream()
        .filter(this::maybeIsSwitchMap).collect(Collectors.toList());
    if (!switchMapFields.isEmpty()) {
      IRCode initializer = clazz.getClassInitializer().buildIR(appView, clazz.origin);
      switchMapFields.forEach(field -> extractSwitchMap(field, initializer));
    }
  }

  private void extractSwitchMap(DexEncodedField encodedField, IRCode initializer) {
    DexField field = encodedField.field;
    Int2ReferenceMap<DexField> switchMap = new Int2ReferenceArrayMap<>();

    // Find each array-put instruction that updates an entry of the array that is stored in
    // `encodedField`.
    //
    // We do this by finding each instruction that reads the array, and iterating the users of the
    // out-value:
    //
    //   int[] x = static-get <encodedField>
    //   x[index] = value
    //
    // A new array is assigned into `encodedField` in the beginning of `initializer`. For
    // completeness, we also need to visit the users of the array being stored:
    //
    //   int[] x = new int[size];
    //   static-put <encodedField>, x
    //   x[index] = value
    Predicate<Instruction> predicate =
        i -> (i.isStaticGet() || i.isStaticPut()) && i.asFieldInstruction().getField() == field;
    for (Instruction instruction : initializer.instructions(predicate)) {
      Value valueOfInterest =
          instruction.isStaticGet() ? instruction.outValue() : instruction.asStaticPut().value();
      for (Instruction use : valueOfInterest.uniqueUsers()) {
        if (use.isArrayPut()) {
          Instruction index = use.asArrayPut().value().definition;
          if (index == null || !index.isConstNumber()) {
            return;
          }
          int integerIndex = index.asConstNumber().getIntValue();
          Instruction value = use.asArrayPut().index().definition;
          if (value == null || !value.isInvokeVirtual()) {
            return;
          }
          InvokeVirtual invoke = value.asInvokeVirtual();
          DexClass holder = appView.definitionFor(invoke.getInvokedMethod().holder);
          if (holder == null
              || (!holder.accessFlags.isEnum()
                  && holder.type != appView.dexItemFactory().enumType)) {
            return;
          }
          Instruction enumGet = invoke.arguments().get(0).definition;
          if (enumGet == null || !enumGet.isStaticGet()) {
            return;
          }
          DexField enumField = enumGet.asStaticGet().getField();
          if (!appView.definitionFor(enumField.holder).accessFlags.isEnum()) {
            return;
          }
          if (switchMap.put(integerIndex, enumField) != null) {
            return;
          }
        } else if (use != instruction) {
          return;
        }
      }
    }
    switchMaps.put(field, switchMap);
  }

  private boolean maybeIsSwitchMap(DexEncodedField dexEncodedField) {
    // We are looking for synthetic fields of type int[].
    DexField field = dexEncodedField.field;
    return dexEncodedField.accessFlags.isSynthetic()
        && (field.name.startsWith(switchMapPrefix) || field.name.startsWith(kotlinSwitchMapPrefix))
        && field.type == intArrayType;
  }
}
