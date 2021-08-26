// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.kotlin;

import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.utils.StringDiagnostic;
import java.util.IdentityHashMap;
import java.util.Map;
import kotlinx.metadata.InconsistentKotlinMetadataException;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

final class KotlinClassMetadataReader {

  static KotlinInfo getKotlinInfo(
      Kotlin kotlin,
      DexClass clazz,
      DiagnosticsHandler reporter) {
    if (clazz.annotations.isEmpty()) {
      return null;
    }
    DexAnnotation meta = clazz.annotations.getFirstMatching(kotlin.metadata.kotlinMetadataType);
    if (meta != null) {
      try {
        return createKotlinInfo(kotlin, clazz, meta);
      } catch (ClassCastException | InconsistentKotlinMetadataException | MetadataError e) {
        reporter.info(
            new StringDiagnostic("Class " + clazz.type.toSourceString()
                + " has malformed kotlin.Metadata: " + e.getMessage()));
      } catch (Throwable e) {
        reporter.info(
            new StringDiagnostic("Unexpected error while reading " + clazz.type.toSourceString()
                + "'s kotlin.Metadata: " + e.getMessage()));
      }
    }
    return null;
  }

  private static KotlinInfo createKotlinInfo(
      Kotlin kotlin,
      DexClass clazz,
      DexAnnotation meta) {
    Map<DexString, DexAnnotationElement> elementMap = new IdentityHashMap<>();
    for (DexAnnotationElement element : meta.annotation.elements) {
      elementMap.put(element.name, element);
    }

    DexAnnotationElement kind = elementMap.get(kotlin.metadata.kind);
    if (kind == null) {
      throw new MetadataError("element 'k' is missing.");
    }
    Integer k = (Integer) kind.value.getBoxedValue();
    DexAnnotationElement metadataVersion = elementMap.get(kotlin.metadata.metadataVersion);
    int[] mv = metadataVersion == null ? null : getUnboxedIntArray(metadataVersion.value, "mv");
    DexAnnotationElement bytecodeVersion = elementMap.get(kotlin.metadata.bytecodeVersion);
    int[] bv = bytecodeVersion == null ? null : getUnboxedIntArray(bytecodeVersion.value, "bv");
    DexAnnotationElement data1 = elementMap.get(kotlin.metadata.data1);
    String[] d1 = data1 == null ? null : getUnboxedStringArray(data1.value, "d1");
    DexAnnotationElement data2 = elementMap.get(kotlin.metadata.data2);
    String[] d2 = data2 == null ? null : getUnboxedStringArray(data2.value, "d2");
    DexAnnotationElement extraString = elementMap.get(kotlin.metadata.extraString);
    String xs = extraString == null ? null : getUnboxedString(extraString.value, "xs");
    DexAnnotationElement packageName = elementMap.get(kotlin.metadata.packageName);
    String pn = packageName == null ? null : getUnboxedString(packageName.value, "pn");
    DexAnnotationElement extraInt = elementMap.get(kotlin.metadata.extraInt);
    Integer xi = extraInt == null ? null : (Integer) extraInt.value.getBoxedValue();

    KotlinClassHeader header = new KotlinClassHeader(k, mv, bv, d1, d2, xs, pn, xi);
    KotlinClassMetadata kMetadata = KotlinClassMetadata.read(header);

    if (kMetadata instanceof KotlinClassMetadata.Class) {
      return KotlinClass.fromKotlinClassMetadata(kMetadata, clazz);
    } else if (kMetadata instanceof KotlinClassMetadata.FileFacade) {
      return KotlinFile.fromKotlinClassMetadata(kMetadata, clazz);
    } else if (kMetadata instanceof KotlinClassMetadata.MultiFileClassFacade) {
      return KotlinClassFacade.fromKotlinClassMetadata(kMetadata);
    } else if (kMetadata instanceof KotlinClassMetadata.MultiFileClassPart) {
      return KotlinClassPart.fromKotlinClassMetadata(kMetadata);
    } else if (kMetadata instanceof KotlinClassMetadata.SyntheticClass) {
      return KotlinSyntheticClass.fromKotlinClassMetadata(kMetadata, kotlin, clazz);
    } else {
      throw new MetadataError("unsupported 'k' value: " + k);
    }
  }

  private static int[] getUnboxedIntArray(DexValue v, String elementName) {
    if (!(v instanceof DexValueArray)) {
      throw new MetadataError("invalid '" + elementName + "' value: " + v.toSourceString());
    }
    DexValueArray intArrayValue = (DexValueArray) v;
    DexValue[] values = intArrayValue.getValues();
    int[] result = new int [values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = (Integer) values[i].getBoxedValue();
    }
    return result;
  }

  private static String[] getUnboxedStringArray(DexValue v, String elementName) {
    if (!(v instanceof DexValueArray)) {
      throw new MetadataError("invalid '" + elementName + "' value: " + v.toSourceString());
    }
    DexValueArray stringArrayValue = (DexValueArray) v;
    DexValue[] values = stringArrayValue.getValues();
    String[] result = new String [values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = getUnboxedString(values[i], elementName + "[" + i + "]");
    }
    return result;
  }

  private static String getUnboxedString(DexValue v, String elementName) {
    if (!(v instanceof DexValueString)) {
      throw new MetadataError("invalid '" + elementName + "' value: " + v.toSourceString());
    }
    return ((DexValueString) v).getValue().toString();
  }

  private static class MetadataError extends RuntimeException {
    MetadataError(String cause) {
      super(cause);
    }
  }

}
