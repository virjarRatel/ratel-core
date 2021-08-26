// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming.dexitembasedstring;

import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.NamingLens;

public abstract class NameComputationInfo<T extends DexReference> {

  public final DexString computeNameFor(
      DexReference reference, DexDefinitionSupplier definitions, NamingLens namingLens) {
    if (needsToComputeName()) {
      if (isFieldNameComputationInfo()) {
        return asFieldNameComputationInfo()
            .internalComputeNameFor(reference.asDexField(), definitions, namingLens);
      }
      if (isClassNameComputationInfo()) {
        return asClassNameComputationInfo()
            .internalComputeNameFor(reference.asDexType(), definitions, namingLens);
      }
    }
    return namingLens.lookupName(reference, definitions.dexItemFactory());
  }

  abstract DexString internalComputeNameFor(
      T reference, DexDefinitionSupplier definitions, NamingLens namingLens);

  public abstract boolean needsToComputeName();

  public abstract boolean needsToRegisterReference();

  public boolean isFieldNameComputationInfo() {
    return false;
  }

  public FieldNameComputationInfo asFieldNameComputationInfo() {
    return null;
  }

  public boolean isClassNameComputationInfo() {
    return false;
  }

  public ClassNameComputationInfo asClassNameComputationInfo() {
    return null;
  }
}
