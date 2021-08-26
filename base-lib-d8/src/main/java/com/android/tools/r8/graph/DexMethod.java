// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.naming.NamingLens;
import com.google.common.collect.Maps;
import java.util.Map;

public class DexMethod extends Descriptor<DexEncodedMethod, DexMethod>
    implements PresortedComparable<DexMethod> {

  public final DexType holder;
  public final DexProto proto;
  public final DexString name;

  // Caches used during processing.
  private Map<DexType, DexEncodedMethod> singleTargetCache;

  DexMethod(DexType holder, DexProto proto, DexString name, boolean skipNameValidationForTesting) {
    this.holder = holder;
    this.proto = proto;
    this.name = name;
    if (!skipNameValidationForTesting && !name.isValidMethodName()) {
      throw new CompilationError(
          "Method name '" + name + "' in class '" + holder.toSourceString() +
              "' cannot be represented in dex format.");
    }
  }

  @Override
  public String toString() {
    return "Method " + holder + "." + name + " " + proto.toString();
  }

  public int getArity() {
    return proto.parameters.size();
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    if (collectIndexedItemsExceptName(indexedItems, method, instructionOffset)) {
      collectIndexedItemsName(indexedItems, method, instructionOffset);
    }
  }

  public boolean collectIndexedItemsExceptName(
      IndexedItemCollection indexedItems, DexMethod method, int instructionOffset) {
    if (indexedItems.addMethod(this)) {
      holder.collectIndexedItems(indexedItems, method, instructionOffset);
      proto.collectIndexedItems(indexedItems, method, instructionOffset);
      return true;
    }
    return false;
  }

  public void collectIndexedItemsName(
      IndexedItemCollection indexedItems, DexMethod method, int instructionOffset) {
    indexedItems.getRenamedName(this).collectIndexedItems(indexedItems, method, instructionOffset);
  }

  @Override
  public int getOffset(ObjectToOffsetMapping mapping) {
    return mapping.getOffsetFor(this);
  }

  @Override
  public boolean isDexMethod() {
    return true;
  }

  @Override
  public DexMethod asDexMethod() {
    return this;
  }

  @Override
  public int computeHashCode() {
    return holder.hashCode()
        + proto.hashCode() * 7
        + name.hashCode() * 31;
  }

  @Override
  public boolean computeEquals(Object other) {
    if (other instanceof DexMethod) {
      DexMethod o = (DexMethod) other;
      return holder.equals(o.holder)
          && name.equals(o.name)
          && proto.equals(o.proto);
    }
    return false;
  }

  /**
   * Returns true if the other method has the same name and prototype (including signature and
   * return type), false otherwise.
   */
  public boolean hasSameProtoAndName(DexMethod other) {
    return name == other.name && proto == other.proto;
  }

  @Override
  public int compareTo(DexMethod other) {
    return sortedCompareTo(other.getSortedIndex());
  }

  @Override
  public int slowCompareTo(DexMethod other) {
    int result = holder.slowCompareTo(other.holder);
    if (result != 0) {
      return result;
    }
    result = name.slowCompareTo(other.name);
    if (result != 0) {
      return result;
    }
    return proto.slowCompareTo(other.proto);
  }

  @Override
  public int slowCompareTo(DexMethod other, NamingLens namingLens) {
    int result = holder.slowCompareTo(other.holder, namingLens);
    if (result != 0) {
      return result;
    }
    result = namingLens.lookupName(this).slowCompareTo(namingLens.lookupName(other));
    if (result != 0) {
      return result;
    }
    return proto.slowCompareTo(other.proto, namingLens);
  }

  @Override
  public int layeredCompareTo(DexMethod other, NamingLens namingLens) {
    int result = holder.compareTo(other.holder);
    if (result != 0) {
      return result;
    }
    result = namingLens.lookupName(this).compareTo(namingLens.lookupName(other));
    if (result != 0) {
      return result;
    }
    return proto.compareTo(other.proto);
  }

  @Override
  public boolean match(DexMethod method) {
    return method.name == name && method.proto == proto;
  }

  @Override
  public boolean match(DexEncodedMethod encodedMethod) {
    return match(encodedMethod.method);
  }

  public String qualifiedName() {
    return holder + "." + name;
  }

  @Override
  public String toSmaliString() {
    return holder.toSmaliString() + "->" + name + proto.toSmaliString();
  }

  @Override
  public String toSourceString() {
    return toSourceString(true);
  }

  public String toSourceStringWithoutHolder() {
    return toSourceString(false);
  }

  private String toSourceString(boolean includeHolder) {
    StringBuilder builder = new StringBuilder(proto.returnType.toSourceString()).append(" ");
    if (includeHolder) {
      builder.append(holder.toSourceString()).append(".");
    }
    builder.append(name).append("(");
    for (int i = 0; i < getArity(); i++) {
      if (i != 0) {
        builder.append(", ");
      }
      builder.append(proto.parameters.values[i].toSourceString());
    }
    return builder.append(")").toString();
  }

  public boolean isLambdaDeserializeMethod(DexItemFactory dexItemFactory) {
    return name == dexItemFactory.deserializeLambdaMethodName
        && proto == dexItemFactory.deserializeLambdaMethodProto;
  }

  synchronized public void setSingleVirtualMethodCache(
      DexType receiverType, DexEncodedMethod method) {
    if (singleTargetCache == null) {
      singleTargetCache = Maps.newIdentityHashMap();
    }
    singleTargetCache.put(receiverType, method);
  }

  synchronized public boolean isSingleVirtualMethodCached(DexType receiverType) {
    return singleTargetCache != null && singleTargetCache.containsKey(receiverType);
  }

  synchronized public DexEncodedMethod getSingleVirtualMethodCache(DexType receiverType) {
    assert isSingleVirtualMethodCached(receiverType);
    return singleTargetCache.get(receiverType);
  }
}
