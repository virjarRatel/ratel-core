// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.ir.desugar.PrefixRewritingMapper;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Naming lens for rewriting type prefixes.
public class PrefixRewritingNamingLens extends NamingLens {

  final Map<DexType, DexString> classRenaming = new IdentityHashMap<>();
  final NamingLens namingLens;
  final InternalOptions options;

  public static NamingLens createPrefixRewritingNamingLens(
      InternalOptions options, PrefixRewritingMapper rewritePrefix) {
    return createPrefixRewritingNamingLens(options, rewritePrefix, NamingLens.getIdentityLens());
  }

  public static NamingLens createPrefixRewritingNamingLens(
      InternalOptions options, PrefixRewritingMapper rewritePrefix, NamingLens namingLens) {
    if (!rewritePrefix.isRewriting()) {
      return namingLens;
    }
    return new PrefixRewritingNamingLens(namingLens, options, rewritePrefix);
  }

  public PrefixRewritingNamingLens(
      NamingLens namingLens, InternalOptions options, PrefixRewritingMapper rewritePrefix) {
    this.namingLens = namingLens;
    this.options = options;
    DexItemFactory itemFactory = options.itemFactory;
    itemFactory.forAllTypes(
        type -> {
          if (rewritePrefix.hasRewrittenType(type)) {
            classRenaming.put(type, rewritePrefix.rewrittenType(type).descriptor);
          }
        });
    // Verify that no type would have been renamed by both lenses.
    assert namingLens.verifyNoOverlap(classRenaming);
  }

  @Override
  public boolean hasPrefixRewritingLogic() {
    return true;
  }

  @Override
  public DexString prefixRewrittenType(DexType type) {
    return classRenaming.get(type);
  }

  @Override
  public DexString lookupDescriptor(DexType type) {
    return classRenaming.getOrDefault(type, namingLens.lookupDescriptor(type));
  }

  @Override
  public DexString lookupInnerName(InnerClassAttribute attribute, InternalOptions options) {
    if (classRenaming.containsKey(attribute.getInner())) {
      // Prefix rewriting does not influence the inner name.
      return attribute.getInnerName();
    }
    return namingLens.lookupInnerName(attribute, options);
  }

  @Override
  public DexString lookupName(DexMethod method) {
    if (classRenaming.containsKey(method.holder)) {
      // Prefix rewriting does not influence the method name.
      return method.name;
    }
    return namingLens.lookupName(method);
  }

  @Override
  public DexString lookupMethodName(DexCallSite callSite) {
    if (classRenaming.containsKey(callSite.bootstrapMethod.rewrittenTarget.holder)) {
      // Prefix rewriting does not influence the inner name.
      return callSite.methodName;
    }
    return namingLens.lookupMethodName(callSite);
  }

  @Override
  public DexString lookupName(DexField field) {
    if (classRenaming.containsKey(field.holder)) {
      // Prefix rewriting does not influence the field name.
      return field.name;
    }
    return namingLens.lookupName(field);
  }

  @Override
  public boolean verifyNoOverlap(Map<DexType, DexString> map) {
    throw new Unreachable("Multiple prefix rewriting lens not supported.");
  }

  @Override
  public String lookupPackageName(String packageName) {
    // Used for resource shrinking.
    // Desugared libraries do not have resources.
    // Hence this call is necessarily for the minifyingLens.
    // TODO(b/134732760): This assertion does not hold with ressources with renamed prefixes.
    // Write a test where the assertion does not hold and fix it.
    assert verifyNotPrefixRewrittenPackage(packageName);
    return namingLens.lookupPackageName(packageName);
  }

  private boolean verifyNotPrefixRewrittenPackage(String packageName) {
    for (DexType dexType : classRenaming.keySet()) {
      assert !dexType.getPackageDescriptor().equals(packageName);
    }
    return true;
  }

  @Override
  public void forAllRenamedTypes(Consumer<DexType> consumer) {
    // Used for printing the applyMapping map.
    // If compiling the program using a desugared library, nothing needs to be printed.
    // If compiling the desugared library, the mapping needs to be printed.
    // When debugging the program, both mapping files need to be merged.
    if (options.isDesugaredLibraryCompilation()) {
      classRenaming.keySet().forEach(consumer);
    }
    namingLens.forAllRenamedTypes(consumer);
  }

  @Override
  public <T extends DexItem> Map<String, T> getRenamedItems(
      Class<T> clazz, Predicate<T> predicate, Function<T, String> namer) {
    Map<String, T> renamedItemsPrefixRewritting;
    if (clazz == DexType.class) {
      renamedItemsPrefixRewritting =
          classRenaming.keySet().stream()
              .filter(item -> predicate.test(clazz.cast(item)))
              .map(clazz::cast)
              .collect(ImmutableMap.toImmutableMap(namer, i -> i));
    } else {
      renamedItemsPrefixRewritting = ImmutableMap.of();
    }
    Map<String, T> renamedItemsMinifier = namingLens.getRenamedItems(clazz, predicate, namer);
    // The Collector throws an exception for duplicated keys.
    return Stream.concat(
            renamedItemsPrefixRewritting.entrySet().stream(),
            renamedItemsMinifier.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public boolean checkTargetCanBeTranslated(DexMethod item) {
    return namingLens.checkTargetCanBeTranslated(item);
  }
}
