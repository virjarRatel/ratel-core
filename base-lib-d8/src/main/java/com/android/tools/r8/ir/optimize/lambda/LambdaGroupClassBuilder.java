// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.EnclosingMethodAttribute;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.origin.SynthesizedOrigin;
import java.util.Collections;
import java.util.List;

// Encapsulates lambda group class building logic and separates
// it from the rest of lambda group functionality.
public abstract class LambdaGroupClassBuilder<T extends LambdaGroup> {
  protected final T group;
  protected final DexItemFactory factory;
  protected final String origin;

  protected LambdaGroupClassBuilder(T group, DexItemFactory factory, String origin) {
    this.group = group;
    this.factory = factory;
    this.origin = origin;
  }

  public final DexProgramClass synthesizeClass(
      AppView<? extends AppInfoWithSubtyping> appView, OptimizationFeedback feedback) {
    DexType groupClassType = group.getGroupClassType();
    DexType superClassType = getSuperClassType();
    DexProgramClass programClass =
        new DexProgramClass(
            groupClassType,
            null,
            new SynthesizedOrigin(origin, getClass()),
            buildAccessFlags(),
            superClassType,
            buildInterfaces(),
            factory.createString(origin),
            null,
            Collections.emptyList(),
            buildEnclosingMethodAttribute(),
            buildInnerClasses(),
            buildAnnotations(),
            buildStaticFields(appView, feedback),
            buildInstanceFields(),
            buildDirectMethods(),
            buildVirtualMethods(),
            factory.getSkipNameValidationForTesting(),
            // The name of the class is based on the hash of the content.
            DexProgramClass::checksumFromType);
    return programClass;
  }

  protected abstract DexType getSuperClassType();

  protected abstract ClassAccessFlags buildAccessFlags();

  protected abstract EnclosingMethodAttribute buildEnclosingMethodAttribute();

  protected abstract List<InnerClassAttribute> buildInnerClasses();

  protected abstract DexAnnotationSet buildAnnotations();

  protected abstract DexEncodedMethod[] buildVirtualMethods();

  protected abstract DexEncodedMethod[] buildDirectMethods();

  protected abstract DexEncodedField[] buildInstanceFields();

  protected abstract DexEncodedField[] buildStaticFields(
      AppView<? extends AppInfoWithSubtyping> appView, OptimizationFeedback feedback);

  protected abstract DexTypeList buildInterfaces();
}
