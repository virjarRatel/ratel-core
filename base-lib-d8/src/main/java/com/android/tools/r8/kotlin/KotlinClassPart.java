// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.kotlin;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import kotlinx.metadata.KmPackage;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

public final class KotlinClassPart extends KotlinInfo<KotlinClassMetadata.MultiFileClassPart> {

  private KmPackage kmPackage;

  static KotlinClassPart fromKotlinClassMetadata(KotlinClassMetadata kotlinClassMetadata) {
    assert kotlinClassMetadata instanceof KotlinClassMetadata.MultiFileClassPart;
    KotlinClassMetadata.MultiFileClassPart multiFileClassPart =
        (KotlinClassMetadata.MultiFileClassPart) kotlinClassMetadata;
    return new KotlinClassPart(multiFileClassPart);
  }

  private KotlinClassPart(KotlinClassMetadata.MultiFileClassPart metadata) {
    super(metadata);
  }

  @Override
  void processMetadata() {
    assert !isProcessed;
    isProcessed = true;
    kmPackage = metadata.toKmPackage();
  }

  @Override
  void rewrite(AppView<AppInfoWithLiveness> appView, NamingLens lens) {
    // TODO(b/70169921): no idea yet!
    assert lens.lookupType(clazz.type, appView.dexItemFactory()) == clazz.type
        : toString();
  }

  @Override
  KotlinClassHeader createHeader() {
    // TODO(b/70169921): may need to update if `rewrite` is implemented.
    return metadata.getHeader();
  }

  @Override
  public Kind getKind() {
    return Kind.Part;
  }

  @Override
  public boolean isClassPart() {
    return true;
  }

  @Override
  public KotlinClassPart asClassPart() {
    return this;
  }

}
