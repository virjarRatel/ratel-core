// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.naming.ClassNameMinifier.ClassRenaming;
import com.android.tools.r8.naming.FieldNameMinifier.FieldRenaming;
import com.android.tools.r8.naming.MethodNameMinifier.MethodRenaming;
import com.android.tools.r8.optimize.MemberRebindingAnalysis;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

class MinifiedRenaming extends NamingLens {

  private final AppView<?> appView;
  private final Map<String, String> packageRenaming;
  private final Map<DexItem, DexString> renaming = new IdentityHashMap<>();

  MinifiedRenaming(
      AppView<?> appView,
      ClassRenaming classRenaming,
      MethodRenaming methodRenaming,
      FieldRenaming fieldRenaming) {
    this.appView = appView;
    this.packageRenaming = classRenaming.packageRenaming;
    renaming.putAll(classRenaming.classRenaming);
    renaming.putAll(methodRenaming.renaming);
    renaming.putAll(methodRenaming.callSiteRenaming);
    renaming.putAll(fieldRenaming.renaming);
  }

  @Override
  public String lookupPackageName(String packageName) {
    return packageRenaming.getOrDefault(packageName, packageName);
  }

  @Override
  public DexString lookupDescriptor(DexType type) {
    return renaming.getOrDefault(type, type.descriptor);
  }

  @Override
  public DexString lookupInnerName(InnerClassAttribute attribute, InternalOptions options) {
    if (attribute.getInnerName() == null) {
      return null;
    }
    DexType innerType = attribute.getInner();
    String inner = DescriptorUtils.descriptorToInternalName(innerType.descriptor.toString());
    // At this point we assume the input was of the form: <OuterType>$<index><InnerName>
    // Find the mapped type and if it remains the same return that, otherwise split at
    // either the input separator ($, $$, or anything that starts with $) or $ (that we recover
    // while minifying, e.g., empty separator, under bar, etc.).
    String innerTypeMapped =
        DescriptorUtils.descriptorToInternalName(lookupDescriptor(innerType).toString());
    if (inner.equals(innerTypeMapped)) {
      return attribute.getInnerName();
    }
    String separator = DescriptorUtils.computeInnerClassSeparator(
        attribute.getOuter(), innerType, attribute.getInnerName());
    if (separator == null) {
      separator = String.valueOf(DescriptorUtils.INNER_CLASS_SEPARATOR);
    }
    int index = innerTypeMapped.lastIndexOf(separator);
    if (index < 0) {
      assert options.getProguardConfiguration().hasApplyMappingFile()
          : innerType + " -> " + innerTypeMapped;
      String descriptor = lookupDescriptor(innerType).toString();
      return options.itemFactory.createString(
          DescriptorUtils.getUnqualifiedClassNameFromDescriptor(descriptor));
    }
    return options.itemFactory.createString(innerTypeMapped.substring(index + separator.length()));
  }

  @Override
  public DexString lookupName(DexMethod method) {
    DexString renamed = renaming.get(method);
    if (renamed != null) {
      return renamed;
    }
    // If the method does not have a direct renaming, return the resolutions mapping.
    ResolutionResult resolutionResult = appView.appInfo().resolveMethod(method.holder, method);
    if (resolutionResult.isSingleResolution()) {
      return renaming.getOrDefault(resolutionResult.getSingleTarget().method, method.name);
    }
    // If resolution fails, the method must be renamed consistently with the targets that give rise
    // to the failure.
    if (resolutionResult.isFailedResolution()) {
      List<DexEncodedMethod> targets = new ArrayList<>();
      resolutionResult.asFailedResolution().forEachFailureDependency(targets::add);
      if (!targets.isEmpty()) {
        DexString firstRename = renaming.get(targets.get(0).method);
        assert targets.stream().allMatch(target -> renaming.get(target.method) == firstRename);
        if (firstRename != null) {
          return firstRename;
        }
      }
    }
    // If no renaming can be found the default is the methods name.
    return method.name;
  }

  @Override
  public DexString lookupMethodName(DexCallSite callSite) {
    return renaming.getOrDefault(callSite, callSite.methodName);
  }

  @Override
  public DexString lookupName(DexField field) {
    return renaming.getOrDefault(field, field.name);
  }

  @Override
  public boolean verifyNoOverlap(Map<DexType, DexString> map) {
    for (DexType alreadyRenamedInOtherLens : map.keySet()) {
      assert !renaming.containsKey(alreadyRenamedInOtherLens);
      assert !renaming.containsKey(
          appView.dexItemFactory().createType(map.get(alreadyRenamedInOtherLens)));
    }
    return true;
  }

  @Override
  void forAllRenamedTypes(Consumer<DexType> consumer) {
    DexReference.filterDexType(DexReference.filterDexReference(renaming.keySet().stream()))
        .forEach(consumer);
  }

  @Override
  <T extends DexItem> Map<String, T> getRenamedItems(
      Class<T> clazz, Predicate<T> predicate, Function<T, String> namer) {
    return renaming.keySet().stream()
        .filter(item -> (clazz.isInstance(item) && predicate.test(clazz.cast(item))))
        .map(clazz::cast)
        .collect(ImmutableMap.toImmutableMap(namer, i -> i));
  }

  /**
   * Checks whether the target is precise enough to be translated,
   *
   * <p>We only track the renaming of actual definitions, Thus, if we encounter a method id that
   * does not directly point at a definition, we won't find the actual renaming. To avoid
   * dispatching on every lookup, we assume that the tree has been fully dispatched by {@link
   * MemberRebindingAnalysis}.
   *
   * <p>Library methods are excluded from this check, as those are never renamed.
   */
  @Override
  public boolean checkTargetCanBeTranslated(DexMethod item) {
    if (item.holder.isArrayType()) {
      // Array methods are never renamed, so do not bother to check.
      return true;
    }
    DexClass holder = appView.definitionFor(item.holder);
    if (holder == null || holder.isNotProgramClass()) {
      return true;
    }
    // We don't know which invoke type this method is used for, so checks that it has been
    // rebound either way.
    DexEncodedMethod staticTarget = appView.appInfo().lookupStaticTarget(item);
    DexEncodedMethod directTarget = appView.appInfo().lookupDirectTarget(item);
    DexEncodedMethod virtualTarget = appView.appInfo().lookupVirtualTarget(item.holder, item);
    DexClass staticTargetHolder =
        staticTarget != null ? appView.definitionFor(staticTarget.method.holder) : null;
    DexClass directTargetHolder =
        directTarget != null ? appView.definitionFor(directTarget.method.holder) : null;
    DexClass virtualTargetHolder =
        virtualTarget != null ? appView.definitionFor(virtualTarget.method.holder) : null;
    assert (directTarget == null && staticTarget == null && virtualTarget == null)
        || (virtualTarget != null && virtualTarget.method == item)
        || (directTarget != null && directTarget.method == item)
        || (staticTarget != null && staticTarget.method == item)
        || (directTargetHolder != null && directTargetHolder.isNotProgramClass())
        || (virtualTargetHolder != null && virtualTargetHolder.isNotProgramClass())
        || (staticTargetHolder != null && staticTargetHolder.isNotProgramClass())
        || appView.unneededVisibilityBridgeMethods().contains(item);
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    renaming.forEach(
        (item, str) -> {
          if (item instanceof DexType) {
            builder.append("[c] ");
          } else if (item instanceof DexMethod) {
            builder.append("[m] ");
          } else if (item instanceof DexField) {
            builder.append("[f] ");
          }
          builder.append(item.toSourceString());
          builder.append(" -> ");
          builder.append(str.toSourceString());
          builder.append('\n');
        });
    return builder.toString();
  }
}
