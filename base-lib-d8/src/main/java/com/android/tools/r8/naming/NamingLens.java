// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.optimize.MemberRebindingAnalysis;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements a translation of the Dex graph from original names to new names produced by the {@link
 * Minifier}.
 *
 * <p>The minifier does not actually rename classes and members but instead only produces a mapping
 * from original ids to renamed ids. When writing the file, the graph has to be interpreted with
 * that mapping in mind, i.e., it should be looked at only through this lens.
 *
 * <p>The translation relies on members being statically dispatched to actual definitions, as done
 * by the {@link MemberRebindingAnalysis} optimization.
 */
public abstract class NamingLens {

  public abstract String lookupPackageName(String packageName);

  public abstract DexString lookupDescriptor(DexType type);

  public abstract DexString lookupInnerName(InnerClassAttribute attribute, InternalOptions options);

  public abstract DexString lookupName(DexMethod method);

  public abstract DexString lookupMethodName(DexCallSite callSite);

  public abstract DexString lookupName(DexField field);

  public final DexString lookupName(DexReference reference, DexItemFactory dexItemFactory) {
    if (reference.isDexType()) {
      DexString renamed = lookupDescriptor(reference.asDexType());
      return dexItemFactory.createString(DescriptorUtils.descriptorToJavaType(renamed.toString()));
    }
    if (reference.isDexMethod()) {
      return lookupName(reference.asDexMethod());
    }
    assert reference.isDexField();
    return lookupName(reference.asDexField());
  }

  public final DexField lookupField(DexField field, DexItemFactory dexItemFactory) {
    return dexItemFactory.createField(
        lookupType(field.holder, dexItemFactory),
        lookupType(field.type, dexItemFactory),
        lookupName(field));
  }

  public final DexMethod lookupMethod(DexMethod method, DexItemFactory dexItemFactory) {
    return dexItemFactory.createMethod(
        lookupType(method.holder, dexItemFactory),
        lookupProto(method.proto, dexItemFactory),
        lookupName(method));
  }

  private DexProto lookupProto(DexProto proto, DexItemFactory dexItemFactory) {
    return dexItemFactory.createProto(
        lookupType(proto.returnType, dexItemFactory),
        Arrays.stream(proto.parameters.values)
            .map(type -> lookupType(type, dexItemFactory))
            .toArray(DexType[]::new));
  }

  public final DexType lookupType(DexType type, DexItemFactory dexItemFactory) {
    if (type.isPrimitiveType() || type.isVoidType()) {
      return type;
    }
    if (type.isArrayType()) {
      DexType newBaseType = lookupType(type.toBaseType(dexItemFactory), dexItemFactory);
      return type.replaceBaseType(newBaseType, dexItemFactory);
    }
    assert type.isClassType();
    return dexItemFactory.createType(lookupDescriptor(type));
  }

  public abstract boolean verifyNoOverlap(Map<DexType, DexString> map);

  public boolean hasPrefixRewritingLogic() {
    return false;
  }

  public DexString prefixRewrittenType(DexType type) {
    return null;
  }

  public static NamingLens getIdentityLens() {
    return new IdentityLens();
  }

  public final boolean isIdentityLens() {
    return this instanceof IdentityLens;
  }

  public String lookupInternalName(DexType type) {
    assert type.isClassType() || type.isArrayType();
    return DescriptorUtils.descriptorToInternalName(lookupDescriptor(type).toString());
  }

  abstract void forAllRenamedTypes(Consumer<DexType> consumer);

  abstract <T extends DexItem> Map<String, T> getRenamedItems(
      Class<T> clazz, Predicate<T> predicate, Function<T, String> namer);

  /**
   * Checks whether the target will be translated properly by this lense.
   *
   * <p>Normally, this means that the target corresponds to an actual definition that has been
   * renamed. For identity renamings, we are more relaxed, as no targets will be translated anyway.
   */
  public abstract boolean checkTargetCanBeTranslated(DexMethod item);

  public final boolean verifyNoCollisions(
      Iterable<DexProgramClass> classes, DexItemFactory dexItemFactory) {
    Set<DexReference> references = Sets.newIdentityHashSet();
    for (DexProgramClass clazz : classes) {
      {
        DexType newType = lookupType(clazz.type, dexItemFactory);
        boolean referencesChanged = references.add(newType);
        assert referencesChanged
            : "Duplicate definition of type `" + newType.toSourceString() + "`";
      }

      for (DexEncodedField field : clazz.fields()) {
        DexField newField = lookupField(field.field, dexItemFactory);
        boolean referencesChanged = references.add(newField);
        assert referencesChanged
            : "Duplicate definition of field `" + newField.toSourceString() + "`";
      }

      for (DexEncodedMethod method : clazz.methods()) {
        DexMethod newMethod = lookupMethod(method.method, dexItemFactory);
        boolean referencesChanged = references.add(newMethod);
        assert referencesChanged
            : "Duplicate definition of method `" + newMethod.toSourceString() + "`";
      }
    }
    return true;
  }

  private static class IdentityLens extends NamingLens {

    private IdentityLens() {
      // Intentionally left empty.
    }

    @Override
    public DexString lookupDescriptor(DexType type) {
      return type.descriptor;
    }

    @Override
    public DexString lookupInnerName(InnerClassAttribute attribute, InternalOptions options) {
      return attribute.getInnerName();
    }

    @Override
    public DexString lookupName(DexMethod method) {
      return method.name;
    }

    @Override
    public DexString lookupMethodName(DexCallSite callSite) {
      return callSite.methodName;
    }

    @Override
    public DexString lookupName(DexField field) {
      return field.name;
    }

    @Override
    public boolean verifyNoOverlap(Map<DexType, DexString> map) {
      return true;
    }

    @Override
    public String lookupPackageName(String packageName) {
      return packageName;
    }

    @Override
    void forAllRenamedTypes(Consumer<DexType> consumer) {
      // Intentionally left empty.
    }

    @Override
    <T extends DexItem> Map<String, T> getRenamedItems(
        Class<T> clazz, Predicate<T> predicate, Function<T, String> namer) {
      return ImmutableMap.of();
    }

    @Override
    public boolean checkTargetCanBeTranslated(DexMethod item) {
      return true;
    }
  }
}
