// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.naming.NamingLens;

public class DexField extends Descriptor<DexEncodedField, DexField> implements
    PresortedComparable<DexField> {

  public final DexType holder;
  public final DexType type;
  public final DexString name;

  DexField(DexType holder, DexType type, DexString name, boolean skipNameValidationForTesting) {
    this.holder = holder;
    this.type = type;
    this.name = name;
    if (!skipNameValidationForTesting && !name.isValidFieldName()) {
      throw new CompilationError(
          "Field name '" + name.toString() + "' cannot be represented in dex format.");
    }
  }

  @Override
  public int computeHashCode() {
    return holder.hashCode()
        + type.hashCode() * 7
        + name.hashCode() * 31;
  }

  @Override
  public boolean computeEquals(Object other) {
    if (other instanceof DexField) {
      DexField o = (DexField) other;
      return holder.equals(o.holder)
          && type.equals(o.type)
          && name.equals(o.name);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Field " + type + " " + holder + "." + name;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    if (indexedItems.addField(this)) {
      holder.collectIndexedItems(indexedItems, method, instructionOffset);
      type.collectIndexedItems(indexedItems, method, instructionOffset);
      indexedItems.getRenamedName(this).collectIndexedItems(
          indexedItems, method, instructionOffset);
    }
  }

  @Override
  public int getOffset(ObjectToOffsetMapping mapping) {
    return mapping.getOffsetFor(this);
  }

  @Override
  public boolean isDexField() {
    return true;
  }

  @Override
  public DexField asDexField() {
    return this;
  }

  @Override
  public int compareTo(DexField other) {
    return sortedCompareTo(other.getSortedIndex());
  }

  @Override
  public int slowCompareTo(DexField other) {
    int result = holder.slowCompareTo(other.holder);
    if (result != 0) {
      return result;
    }
    result = name.slowCompareTo(other.name);
    if (result != 0) {
      return result;
    }
    return type.slowCompareTo(other.type);
  }

  @Override
  public int slowCompareTo(DexField other, NamingLens namingLens) {
    int result = holder.slowCompareTo(other.holder, namingLens);
    if (result != 0) {
      return result;
    }
    result = namingLens.lookupName(this).slowCompareTo(namingLens.lookupName(other));
    if (result != 0) {
      return result;
    }
    return type.slowCompareTo(other.type, namingLens);
  }

  @Override
  public int layeredCompareTo(DexField other, NamingLens namingLens) {
    int result = holder.compareTo(other.holder);
    if (result != 0) {
      return result;
    }
    result = namingLens.lookupName(this).compareTo(namingLens.lookupName(other));
    if (result != 0) {
      return result;
    }
    return type.compareTo(other.type);
  }

  @Override
  public boolean match(DexField field) {
    return field.name == name && field.type == type;
  }

  @Override
  public boolean match(DexEncodedField encodedField) {
    return match(encodedField.field);
  }

  public String qualifiedName() {
    return holder + "." + name;
  }

  @Override
  public String toSmaliString() {
    return holder.toSmaliString() + "->" + name + ":" + type.toSmaliString();
  }

  @Override
  public String toSourceString() {
    return type.toSourceString() + " " + holder.toSourceString() + "." + name.toSourceString();
  }
}
