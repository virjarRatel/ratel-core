// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.references.FieldReference;
import com.android.tools.r8.references.MethodReference;
import com.android.tools.r8.references.TypeReference;
import com.android.tools.r8.utils.Box;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.util.Set;

public class RetraceBaseImpl implements RetraceBase {

  private static final Set<String> UNKNOWN_SOURCEFILE_NAMES =
      Sets.newHashSet("", "SourceFile", "Unknown", "Unknown Source");

  private final ClassNameMapper classNameMapper;

  RetraceBaseImpl(ClassNameMapper classNameMapper) {
    this.classNameMapper = classNameMapper;
  }

  @Override
  public RetraceMethodResult retrace(MethodReference methodReference) {
    return retrace(methodReference.getHolderClass()).lookupMethod(methodReference.getMethodName());
  }

  @Override
  public RetraceFieldResult retrace(FieldReference fieldReference) {
    return retrace(fieldReference.getHolderClass()).lookupField(fieldReference.getFieldName());
  }

  @Override
  public RetraceClassResult retrace(ClassReference classReference) {
    return RetraceClassResult.create(
        classReference, classNameMapper.getClassNaming(classReference.getTypeName()));
  }

  @Override
  public String retraceSourceFile(ClassReference classReference, String sourceFile) {
    Box<String> retracedSourceFile = new Box<>();
    retrace(classReference)
        .forEach(element -> retracedSourceFile.set(element.retraceSourceFile(sourceFile, this)));
    return retracedSourceFile.get();
  }

  @Override
  public RetraceTypeResult retrace(TypeReference typeReference) {
    return new RetraceTypeResult(typeReference, this);
  }

  @Override
  public String retraceSourceFile(
      ClassReference obfuscatedClass,
      String sourceFile,
      ClassReference retracedClassReference,
      boolean hasRetraceResult) {
    boolean fileNameProbablyChanged =
        hasRetraceResult
            && !retracedClassReference.getTypeName().startsWith(obfuscatedClass.getTypeName());
    if (!UNKNOWN_SOURCEFILE_NAMES.contains(sourceFile) && !fileNameProbablyChanged) {
      // We have no new information, only rewrite filename if it is unknown.
      // PG-retrace will always rewrite the filename, but that seems a bit to harsh to do.
      return sourceFile;
    }
    if (!hasRetraceResult) {
      // We have no mapping but but the file name is unknown, so the best we can do is take the
      // name of the obfuscated clazz.
      assert obfuscatedClass.getTypeName().equals(retracedClassReference.getTypeName());
      return getClassSimpleName(obfuscatedClass.getTypeName()) + ".java";
    }
    String newFileName = getClassSimpleName(retracedClassReference.getTypeName());
    String extension = Files.getFileExtension(sourceFile);
    if (extension.isEmpty()) {
      extension = "java";
    }
    return newFileName + "." + extension;
  }

  private static String getClassSimpleName(String clazz) {
    int lastIndexOfPeriod = clazz.lastIndexOf('.');
    // Check if we can find a subclass separator.
    int endIndex = clazz.lastIndexOf('$');
    if (lastIndexOfPeriod > endIndex || endIndex < 0) {
      endIndex = clazz.length();
    }
    return clazz.substring(lastIndexOfPeriod + 1, endIndex);
  }
}
