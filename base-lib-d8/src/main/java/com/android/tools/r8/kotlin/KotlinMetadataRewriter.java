// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.kotlin;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import kotlinx.metadata.jvm.KotlinClassHeader;

public class KotlinMetadataRewriter {

  private final AppView<AppInfoWithLiveness> appView;
  private final NamingLens lens;
  private final DexItemFactory factory;
  private final Kotlin kotlin;

  public KotlinMetadataRewriter(AppView<AppInfoWithLiveness> appView, NamingLens lens) {
    this.appView = appView;
    this.lens = lens;
    this.factory = appView.dexItemFactory();
    this.kotlin = factory.kotlin;
  }

  public static void removeKotlinMetadataFromRenamedClass(AppView<?> appView, DexType type) {
    DexClass clazz = appView.definitionFor(type);
    if (clazz == null) {
      return;
    }
    removeKotlinMetadataFromRenamedClass(appView, clazz);
  }

  public static void removeKotlinMetadataFromRenamedClass(AppView<?> appView, DexClass clazz) {
    // Remove @Metadata in DexAnnotation form if a class is renamed.
    clazz.annotations = clazz.annotations.keepIf(anno -> isNotKotlinMetadata(appView, anno));
    // Clear associated {@link KotlinInfo} to avoid accidentally deserialize it back to
    // DexAnnotation we've just removed above.
    if (clazz.isProgramClass()) {
      clazz.asProgramClass().setKotlinInfo(null);
    }
  }

  private static boolean isNotKotlinMetadata(AppView<?> appView, DexAnnotation annotation) {
    return annotation.annotation.type
        != appView.dexItemFactory().kotlin.metadata.kotlinMetadataType;
  }

  public void run(ExecutorService executorService) throws ExecutionException {
    ThreadUtils.processItems(
        appView.appInfo().classes(),
        clazz -> {
          KotlinInfo<?> kotlinInfo = clazz.getKotlinInfo();
          if (kotlinInfo != null) {
            assert kotlinInfo.isClass()
                || kotlinInfo.isSyntheticClass()
                || kotlinInfo.isFile(); // e.g., B.kt becomes class `BKt`
            // If @Metadata is still associated, this class should not be renamed
            // (by {@link ClassNameMinifier} of course).
            assert lens.lookupType(clazz.type, appView.dexItemFactory()) == clazz.type
                : clazz.toSourceString() + " != "
                    + lens.lookupType(clazz.type, appView.dexItemFactory());

            DexAnnotation oldMeta =
                clazz.annotations.getFirstMatching(kotlin.metadata.kotlinMetadataType);
            // If @Metadata is already gone, e.g., by {@link AnnotationRemover} if type Metadata is
            // determined as dead (e.g., due to no keep rule), nothing to do.
            if (oldMeta == null) {
              return;
            }

            kotlinInfo.rewrite(appView, lens);

            DexAnnotation newMeta = createKotlinMetadataAnnotation(kotlinInfo.createHeader());
            clazz.annotations = clazz.annotations.rewrite(anno -> anno == oldMeta ? newMeta : anno);
          }
        },
        executorService
    );
  }

  private DexAnnotation createKotlinMetadataAnnotation(KotlinClassHeader header) {
    List<DexAnnotationElement> elements = new ArrayList<>();
    elements.add(
        new DexAnnotationElement(kotlin.metadata.kind, DexValueInt.create(header.getKind())));
    elements.add(
        new DexAnnotationElement(
            kotlin.metadata.metadataVersion, createIntArray(header.getMetadataVersion())));
    elements.add(
        new DexAnnotationElement(
            kotlin.metadata.bytecodeVersion, createIntArray(header.getBytecodeVersion())));
    elements.add(
        new DexAnnotationElement(kotlin.metadata.data1, createStringArray(header.getData1())));
    elements.add(
        new DexAnnotationElement(kotlin.metadata.data2, createStringArray(header.getData2())));
    elements.add(
        new DexAnnotationElement(
            kotlin.metadata.extraString,
            new DexValueString(factory.createString(header.getExtraString()))));
    elements.add(
        new DexAnnotationElement(
            kotlin.metadata.packageName,
            new DexValueString(factory.createString(header.getPackageName()))));
    elements.add(
        new DexAnnotationElement(
            kotlin.metadata.extraInt, DexValueInt.create(header.getExtraInt())));
    DexEncodedAnnotation encodedAnnotation =
        new DexEncodedAnnotation(
            kotlin.metadata.kotlinMetadataType, elements.toArray(DexAnnotationElement.EMPTY_ARRAY));
    return new DexAnnotation(DexAnnotation.VISIBILITY_RUNTIME, encodedAnnotation);
  }

  private DexValueArray createIntArray(int[] data) {
    DexValue[] values = new DexValue[data.length];
    for (int i = 0; i < data.length; i++) {
      values[i] = DexValueInt.create(data[i]);
    }
    return new DexValueArray(values);
  }

  private DexValueArray createStringArray(String[] data) {
    DexValue[] values = new DexValue[data.length];
    for (int i = 0; i < data.length; i++) {
      values[i] = new DexValueString(factory.createString(data[i]));
    }
    return new DexValueArray(values);
  }
}
