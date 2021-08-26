// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.InternalOptions;
import org.objectweb.asm.ClassWriter;

/** Representation of an entry in the Java InnerClasses attribute table. */
public class InnerClassAttribute {

  // Access flags to the inner class as declared in the program source.
  private final int access;

  // Type of the inner class.
  private final DexType inner;

  // Type of the enclosing class, null if the inner class is top-level, local or anonymous.
  private final DexType outer;

  // Short name of the inner class, null if the inner class is anonymous.
  private final DexString innerName;

  // Create a named inner-class attribute, but with some arbitrary/unknown name.
  // This is needed to partially map back to the Java attribute structures when reading DEX inputs.
  public static InnerClassAttribute createUnknownNamedInnerClass(DexType inner, DexType outer) {
    return new InnerClassAttribute(0, inner, outer, DexItemFactory.unknownTypeName);
  }

  public InnerClassAttribute(int access, DexType inner, DexType outer, DexString innerName) {
    assert inner != null;
    this.access = access;
    this.inner = inner;
    this.outer = outer;
    this.innerName = innerName;
  }

  public boolean isNamed() {
    return innerName != null;
  }

  public boolean isAnonymous() {
    return innerName == null;
  }

  public int getAccess() {
    return access;
  }

  public DexType getInner() {
    return inner;
  }

  public DexType getOuter() {
    return outer;
  }

  public DexString getInnerName() {
    return innerName;
  }

  public void write(ClassWriter writer, NamingLens lens, InternalOptions options) {
    String internalName = lens.lookupInternalName(inner);
    writer.visitInnerClass(
        internalName,
        outer == null ? null : lens.lookupInternalName(outer),
        innerName == null ? null : lens.lookupInnerName(this, options).toString(),
        access);
  }

  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    inner.collectIndexedItems(indexedItems);
    if (outer != null) {
      outer.collectIndexedItems(indexedItems);
    }
    if (innerName != null) {
      innerName.collectIndexedItems(indexedItems);
    }
  }

  public DexType getLiveContext(AppInfoWithLiveness appInfo) {
    DexType context = getOuter();
    if (context == null) {
      DexClass inner = appInfo.definitionFor(getInner());
      if (inner != null && inner.getEnclosingMethod() != null) {
        EnclosingMethodAttribute enclosingMethodAttribute = inner.getEnclosingMethod();
        if (enclosingMethodAttribute.getEnclosingClass() != null) {
          context = enclosingMethodAttribute.getEnclosingClass();
        } else {
          DexMethod enclosingMethod = enclosingMethodAttribute.getEnclosingMethod();
          if (!appInfo.liveMethods.contains(enclosingMethod)) {
            // EnclosingMethodAttribute will be pruned as it references the pruned method.
            // Hence, the current InnerClassAttribute will be removed too. No live context.
            return null;
          }
          context = enclosingMethod.holder;
        }
      }
    }
    return context;
  }

  @Override
  public String toString() {
    return "[access : " + Integer.toHexString(access)
        + ", inner: " + inner.toDescriptorString()
        + ", outer: " + (outer == null ? "null" : outer.toDescriptorString())
        + ", innerName: " + (innerName == null ? "(anonymous)" : innerName.toString())
        + "]";
  }
}
