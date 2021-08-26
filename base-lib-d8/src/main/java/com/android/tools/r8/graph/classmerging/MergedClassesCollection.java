// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph.classmerging;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.ArrayList;
import java.util.List;

public class MergedClassesCollection implements MergedClasses {

  private List<MergedClasses> collection = new ArrayList<>();

  public void add(MergedClasses mergedClasses) {
    collection.add(mergedClasses);
  }

  @Override
  public boolean verifyAllSourcesPruned(AppView<AppInfoWithLiveness> appView) {
    for (MergedClasses mergedClasses : collection) {
      assert mergedClasses.verifyAllSourcesPruned(appView);
    }
    return true;
  }
}
