// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph.classmerging;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.Set;

public class HorizontallyMergedLambdaClasses implements MergedClasses {

  private final Set<DexType> sources;

  public HorizontallyMergedLambdaClasses(Set<DexType> sources) {
    this.sources = sources;
  }

  @Override
  public boolean verifyAllSourcesPruned(AppView<AppInfoWithLiveness> appView) {
    for (DexType source : sources) {
      assert appView.appInfo().wasPruned(source)
          : "Expected horizontally merged lambda class `"
              + source.toSourceString()
              + "` to be absent";
    }
    return true;
  }
}
