// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info.initializer;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.AbstractFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.UnknownFieldSet;

public class DefaultInstanceInitializerInfo extends InstanceInitializerInfo {

  private static final DefaultInstanceInitializerInfo INSTANCE =
      new DefaultInstanceInitializerInfo();

  private DefaultInstanceInitializerInfo() {}

  public static DefaultInstanceInitializerInfo getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isDefaultInfo() {
    return true;
  }

  @Override
  public DexMethod getParent() {
    return null;
  }

  @Override
  public AbstractFieldSet readSet() {
    return UnknownFieldSet.getInstance();
  }

  @Override
  public boolean instanceFieldInitializationMayDependOnEnvironment() {
    return true;
  }

  @Override
  public boolean mayHaveOtherSideEffectsThanInstanceFieldAssignments() {
    return true;
  }

  @Override
  public boolean receiverNeverEscapesOutsideConstructorChain() {
    return false;
  }
}
