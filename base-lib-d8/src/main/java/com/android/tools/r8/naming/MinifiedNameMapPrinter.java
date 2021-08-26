// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MinifiedNameMapPrinter {

  private static final String NEW_LINE = "\n";
  private final DexApplication application;
  private final NamingLens namingLens;
  private final Set<DexType> seenTypes = Sets.newIdentityHashSet();

  public MinifiedNameMapPrinter(DexApplication application, NamingLens namingLens) {
    this.application = application;
    this.namingLens = namingLens;
  }

  private <T> T[] sortedCopy(T[] source, Comparator<? super T> comparator) {
    T copy[] = Arrays.copyOf(source, source.length);
    Arrays.sort(copy, comparator);
    return copy;
  }

  private <T> List<T> sortedCopy(List<T> source, Comparator<? super T> comparator) {
    List<T> copy = new ArrayList<>(source);
    Collections.sort(copy, comparator);
    return copy;
  }

  private void writeClass(DexProgramClass clazz, StringBuilder out) {
    seenTypes.add(clazz.type);
    DexString descriptor = namingLens.lookupDescriptor(clazz.type);
    out.append(DescriptorUtils.descriptorToJavaType(clazz.type.descriptor.toSourceString()));
    out.append(" -> ");
    out.append(DescriptorUtils.descriptorToJavaType(descriptor.toSourceString()));
    out.append(":").append(NEW_LINE);
    writeFields(sortedCopy(
        clazz.instanceFields(), Comparator.comparing(DexEncodedField::toSourceString)), out);
    writeFields(sortedCopy(
        clazz.staticFields(), Comparator.comparing(DexEncodedField::toSourceString)), out);
    writeMethods(sortedCopy(
        clazz.directMethods(), Comparator.comparing(DexEncodedMethod::toSourceString)), out);
    writeMethods(sortedCopy(
        clazz.virtualMethods(), Comparator.comparing(DexEncodedMethod::toSourceString)), out);
  }

  private void writeType(DexType type, StringBuilder out) {
    if (type.isClassType() && seenTypes.add(type)) {
      DexString descriptor = namingLens.lookupDescriptor(type);
      out.append(DescriptorUtils.descriptorToJavaType(type.descriptor.toSourceString()));
      out.append(" -> ");
      out.append(DescriptorUtils.descriptorToJavaType(descriptor.toSourceString()));
      out.append(":").append(NEW_LINE);
    }
  }

  private void writeFields(List<DexEncodedField> fields, StringBuilder out) {
    for (DexEncodedField encodedField : fields) {
      DexField field = encodedField.field;
      DexString renamed = namingLens.lookupName(field);
      if (renamed != field.name) {
        out.append("    ");
        out.append(field.type.toSourceString());
        out.append(" ");
        out.append(field.name.toSourceString());
        out.append(" -> ");
        out.append(renamed.toSourceString()).append(NEW_LINE);
      }
    }
  }

  private void writeMethod(MethodSignature signature, String renamed, StringBuilder out) {
    out.append("    ");
    out.append(signature.toString());
    out.append(" -> ");
    out.append(renamed).append(NEW_LINE);
  }

  private void writeMethods(List<DexEncodedMethod> methods, StringBuilder out) {
    for (DexEncodedMethod encodedMethod : methods) {
      DexMethod method = encodedMethod.method;
      DexString renamed = namingLens.lookupName(method);
      if (renamed != method.name) {
        MethodSignature signature = MethodSignature.fromDexMethod(method);
        String renamedSourceString = renamed.toSourceString();
        writeMethod(signature, renamedSourceString, out);
      }
    }
  }

  public void write(StringBuilder out) {
    // First write out all classes that have been renamed.
    List<DexProgramClass> classes = new ArrayList<>(application.classes());
    classes.sort(Comparator.comparing(DexProgramClass::toSourceString));
    classes.forEach(clazz -> writeClass(clazz, out));
    // Now write out all types only mentioned in descriptors that have been renamed.
    namingLens.forAllRenamedTypes(type -> writeType(type, out));
  }
}
