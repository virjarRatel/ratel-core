// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.kotlin;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

public final class KotlinClassFacade extends KotlinInfo<KotlinClassMetadata.MultiFileClassFacade> {

  static KotlinClassFacade fromKotlinClassMetadata(KotlinClassMetadata kotlinClassMetadata) {
    assert kotlinClassMetadata instanceof KotlinClassMetadata.MultiFileClassFacade;
    KotlinClassMetadata.MultiFileClassFacade multiFileClassFacade =
        (KotlinClassMetadata.MultiFileClassFacade) kotlinClassMetadata;
    return new KotlinClassFacade(multiFileClassFacade);
  }

  private KotlinClassFacade(KotlinClassMetadata.MultiFileClassFacade metadata) {
    super(metadata);
  }

  @Override
  void processMetadata() {
    assert !isProcessed;
    isProcessed = true;
    // No API to explore metadata details, hence nothing to do further.
  }

  @Override
  void rewrite(AppView<AppInfoWithLiveness> appView, NamingLens lens) {
    throw new Unreachable(toString());
  }

  @Override
  KotlinClassHeader createHeader() {
    throw new Unreachable(toString());
  }

  @Override
  public Kind getKind() {
    return Kind.Facade;
  }

  @Override
  public boolean isClassFacade() {
    return true;
  }

  @Override
  public KotlinClassFacade asClassFacade() {
    return this;
  }

}
