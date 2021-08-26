// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.kotlin;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

// Provides access to kotlin information.
public abstract class KotlinInfo<MetadataKind extends KotlinClassMetadata> {
  final MetadataKind metadata;
  final DexClass clazz;
  boolean isProcessed;

  KotlinInfo(MetadataKind metadata) {
    this(metadata, null);
  }

  KotlinInfo(MetadataKind metadata, DexClass clazz) {
    this.metadata = metadata;
    this.clazz = clazz;
    processMetadata();
  }

  // Subtypes will define how to process the given metadata.
  abstract void processMetadata();

  // Subtypes will define how to rewrite metadata after shrinking and minification.
  abstract void rewrite(AppView<AppInfoWithLiveness> appView, NamingLens lens);

  abstract KotlinClassHeader createHeader();

  public enum Kind {
    Class, File, Synthetic, Part, Facade
  }

  public abstract Kind getKind();

  public boolean isClass() {
    return false;
  }

  public KotlinClass asClass() {
    return null;
  }

  public boolean isFile() {
    return false;
  }

  public KotlinFile asFile() {
    return null;
  }

  public boolean isSyntheticClass() {
    return false;
  }

  public KotlinSyntheticClass asSyntheticClass() {
    return null;
  }

  public boolean isClassPart() {
    return false;
  }

  public KotlinClassPart asClassPart() {
    return null;
  }

  public boolean isClassFacade() {
    return false;
  }

  public KotlinClassFacade asClassFacade() {
    return null;
  }

  @Override
  public String toString() {
    return clazz.toSourceString() + ": " + metadata.toString();
  }
}
