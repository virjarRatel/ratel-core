// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.ClassWriter;

/**
 * Representation of the Java EnclosingMethod attribute.
 *
 * <p>A class with this attribute is either a local class or an anonymous class. It might be
 * declared in the context of a constructor or method, in which case the method field will be
 * non-null. Otherwise, i.e., if the class is declared in an initializer, method is null.
 */
public final class EnclosingMethodAttribute {

  // Enclosing class of the inner class.
  // Null if the inner class is declared inside of a method or constructor.
  private DexType enclosingClass;

  // Enclosing method of the inner class.
  // Null if the inner class is declared outside of a method or constructor.
  private DexMethod enclosingMethod;

  public EnclosingMethodAttribute(DexType enclosingClass) {
    this.enclosingClass = enclosingClass;
  }

  public EnclosingMethodAttribute(DexMethod enclosingMethod) {
    this.enclosingMethod = enclosingMethod;
  }

  public void write(ClassWriter writer, NamingLens lens) {
    if (enclosingMethod != null) {
      writer.visitOuterClass(
          lens.lookupInternalName(enclosingMethod.holder),
          lens.lookupName(enclosingMethod).toString(),
          enclosingMethod.proto.toDescriptorString(lens));
    } else {
      writer.visitOuterClass(lens.lookupInternalName(enclosingClass), null, null);
    }
  }

  public DexMethod getEnclosingMethod() {
    return enclosingMethod;
  }

  public DexType getEnclosingClass() {
    return enclosingClass;
  }

  @Override
  public int hashCode() {
    assert (enclosingClass == null) != (enclosingMethod == null);
    return System.identityHashCode(enclosingClass) + System.identityHashCode(enclosingMethod);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof EnclosingMethodAttribute &&
        enclosingClass == ((EnclosingMethodAttribute) obj).enclosingClass &&
        enclosingMethod == ((EnclosingMethodAttribute) obj).enclosingMethod;
  }

  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    if (enclosingClass != null) {
      enclosingClass.collectIndexedItems(indexedItems);
    }
    if (enclosingMethod != null) {
      enclosingMethod.collectIndexedItems(indexedItems);
    }
  }

  @Override
  public String toString() {
    return "[enclosingClass: "
        + (enclosingClass == null ? "null" : enclosingClass.toDescriptorString())
        + ", enclosingMethod: "
        + (enclosingMethod == null ? "null" : enclosingMethod.toSourceString())
        + "]";
  }
}
