// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.graph.DexProgramClass.ChecksumSupplier;
import com.android.tools.r8.origin.Origin;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Kind of the application class. Can be program, classpath or library. */
public enum ClassKind {
  PROGRAM(DexProgramClass::new, DexClass::isProgramClass),
  CLASSPATH(
      (type,
          kind,
          origin,
          accessFlags,
          superType,
          interfaces,
          sourceFile,
          nestHost,
          nestMembers,
          enclosingMember,
          innerClasses,
          annotations,
          staticFields,
          instanceFields,
          directMethods,
          virtualMethods,
          skipNameValidationForTesting,
          checksumSupplier) -> {
        return new DexClasspathClass(
            type,
            kind,
            origin,
            accessFlags,
            superType,
            interfaces,
            sourceFile,
            nestHost,
            nestMembers,
            enclosingMember,
            innerClasses,
            annotations,
            staticFields,
            instanceFields,
            directMethods,
            virtualMethods,
            skipNameValidationForTesting);
      },
      DexClass::isClasspathClass),
  LIBRARY(
      (type,
          kind,
          origin,
          accessFlags,
          superType,
          interfaces,
          sourceFile,
          nestHost,
          nestMembers,
          enclosingMember,
          innerClasses,
          annotations,
          staticFields,
          instanceFields,
          directMethods,
          virtualMethods,
          skipNameValidationForTesting,
          checksumSupplier) -> {
        return new DexLibraryClass(
            type,
            kind,
            origin,
            accessFlags,
            superType,
            interfaces,
            sourceFile,
            nestHost,
            nestMembers,
            enclosingMember,
            innerClasses,
            annotations,
            staticFields,
            instanceFields,
            directMethods,
            virtualMethods,
            skipNameValidationForTesting);
      },
      DexClass::isLibraryClass);

  private interface Factory {
    DexClass create(
        DexType type,
        Kind kind,
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
        boolean skipNameValidationForTesting,
        ChecksumSupplier checksumSupplier);
  }

  private final Factory factory;
  private final Predicate<DexClass> check;

  ClassKind(Factory factory, Predicate<DexClass> check) {
    this.factory = factory;
    this.check = check;
  }

  public DexClass create(
      DexType type,
      Kind kind,
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
      boolean skipNameValidationForTesting,
      ChecksumSupplier checksumSupplier) {
    return factory.create(
        type,
        kind,
        origin,
        accessFlags,
        superType,
        interfaces,
        sourceFile,
        nestHost,
        nestMembers,
        enclosingMember,
        innerClasses,
        annotations,
        staticFields,
        instanceFields,
        directMethods,
        virtualMethods,
        skipNameValidationForTesting,
        checksumSupplier);
  }

  public boolean isOfKind(DexClass clazz) {
    return check.test(clazz);
  }

  public <T extends DexClass> Consumer<DexClass> bridgeConsumer(Consumer<T> consumer) {
    return clazz -> {
      assert isOfKind(clazz);
      @SuppressWarnings("unchecked") T specialized = (T) clazz;
      consumer.accept(specialized);
    };
  }
}
