// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.kotlin.KotlinInfo;
import com.android.tools.r8.origin.Origin;
import java.util.List;
import java.util.function.Supplier;

public class DexClasspathClass extends DexClass implements Supplier<DexClasspathClass> {

  public DexClasspathClass(
      DexType type,
      ProgramResource.Kind kind,
      Origin origin,
      ClassAccessFlags accessFlags,
      DexType superType,
      DexTypeList interfaces,
      DexString sourceFile,
      NestHostClassAttribute nestHost,
      List<NestMemberClassAttribute> nestMembers,
      EnclosingMethodAttribute enclosingMember,
      List<InnerClassAttribute> innerClasses,
      DexAnnotationSet annotations,
      DexEncodedField[] staticFields,
      DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods,
      DexEncodedMethod[] virtualMethods,
      boolean skipNameValidationForTesting) {
    super(
        sourceFile,
        interfaces,
        accessFlags,
        superType,
        type,
        staticFields,
        instanceFields,
        directMethods,
        virtualMethods,
        nestHost,
        nestMembers,
        enclosingMember,
        innerClasses,
        annotations,
        origin,
        skipNameValidationForTesting);
    assert kind == Kind.CF : "Invalid kind " + kind + " for class-path class " + type;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    throw new Unreachable();
  }

  @Override
  public String toString() {
    return type.toString() + "(classpath class)";
  }

  @Override
  public void addDependencies(MixedSectionCollection collector) {
    // Should never happen but does not harm.
    assert false;
  }

  @Override
  public boolean isClasspathClass() {
    return true;
  }

  @Override
  public DexClasspathClass asClasspathClass() {
    return this;
  }

  @Override
  public boolean isNotProgramClass() {
    return true;
  }

  @Override
  public KotlinInfo getKotlinInfo() {
    throw new Unreachable("Kotlin into n classpath class is not supported yet.");
  }

  @Override
  public DexClasspathClass get() {
    return this;
  }
}
