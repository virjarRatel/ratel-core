// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.Invoke;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A graph lens that will not lead to any code rewritings in the {@link
 * com.android.tools.r8.ir.conversion.LensCodeRewriter}, or parameter removals in the {@link
 * com.android.tools.r8.ir.conversion.IRBuilder}.
 *
 * <p>The mappings from the original program to the generated program are kept, though.
 */
public class AppliedGraphLens extends GraphLense {

  private final AppView<?> appView;

  private final BiMap<DexType, DexType> originalTypeNames = HashBiMap.create();
  private final BiMap<DexField, DexField> originalFieldSignatures = HashBiMap.create();
  private final BiMap<DexMethod, DexMethod> originalMethodSignatures = HashBiMap.create();
  private final Map<DexMethod, DexMethod> originalMethodSignaturesForBridges =
      new IdentityHashMap<>();

  public AppliedGraphLens(
      AppView<? extends AppInfoWithSubtyping> appView, List<DexProgramClass> classes) {
    this.appView = appView;

    for (DexProgramClass clazz : classes) {
      // Record original type names.
      {
        DexType type = clazz.type;
        if (appView.verticallyMergedClasses() != null
            && !appView.verticallyMergedClasses().hasBeenMergedIntoSubtype(type)) {
          DexType original = appView.graphLense().getOriginalType(type);
          if (original != type) {
            DexType existing = originalTypeNames.forcePut(type, original);
            assert existing == null;
          }
        }
      }

      // Record original field signatures.
      for (DexEncodedField encodedField : clazz.fields()) {
        DexField field = encodedField.field;
        DexField original = appView.graphLense().getOriginalFieldSignature(field);
        if (original != field) {
          DexField existing = originalFieldSignatures.forcePut(field, original);
          assert existing == null;
        }
      }

      // Record original method signatures.
      for (DexEncodedMethod encodedMethod : clazz.methods()) {
        DexMethod method = encodedMethod.method;
        DexMethod original = appView.graphLense().getOriginalMethodSignature(method);
        if (original != method) {
          DexMethod existing = originalMethodSignatures.inverse().get(original);
          if (existing == null) {
            originalMethodSignatures.put(method, original);
          } else {
            DexMethod renamed = getRenamedMethodSignature(original);
            if (renamed == existing) {
              originalMethodSignaturesForBridges.put(method, original);
            } else {
              originalMethodSignatures.forcePut(method, original);
              originalMethodSignaturesForBridges.put(existing, original);
            }
          }
        }
      }
    }
  }

  @Override
  public DexType getOriginalType(DexType type) {
    return originalTypeNames.getOrDefault(type, type);
  }

  @Override
  public DexField getOriginalFieldSignature(DexField field) {
    return originalFieldSignatures.getOrDefault(field, field);
  }

  @Override
  public DexMethod getOriginalMethodSignature(DexMethod method) {
    if (originalMethodSignaturesForBridges.containsKey(method)) {
      return originalMethodSignaturesForBridges.get(method);
    }
    return originalMethodSignatures.getOrDefault(method, method);
  }

  @Override
  public DexField getRenamedFieldSignature(DexField originalField) {
    return originalFieldSignatures.inverse().getOrDefault(originalField, originalField);
  }

  @Override
  public DexMethod getRenamedMethodSignature(DexMethod originalMethod) {
    return originalMethodSignatures.inverse().getOrDefault(originalMethod, originalMethod);
  }

  @Override
  public DexType lookupType(DexType type) {
    if (appView.verticallyMergedClasses() != null
        && appView.verticallyMergedClasses().hasBeenMergedIntoSubtype(type)) {
      return lookupType(appView.verticallyMergedClasses().getTargetFor(type));
    }
    return originalTypeNames.inverse().getOrDefault(type, type);
  }

  @Override
  public GraphLenseLookupResult lookupMethod(
      DexMethod method, DexMethod context, Invoke.Type type) {
    return new GraphLenseLookupResult(method, type);
  }

  @Override
  public RewrittenPrototypeDescription lookupPrototypeChanges(DexMethod method) {
    return RewrittenPrototypeDescription.none();
  }

  @Override
  public DexField lookupField(DexField field) {
    return field;
  }

  @Override
  public boolean isContextFreeForMethods() {
    return true;
  }
}
