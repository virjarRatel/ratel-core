// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph.classmerging;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerticallyMergedClasses implements MergedClasses {

  private final Map<DexType, DexType> mergedClasses;
  private final Map<DexType, List<DexType>> sources;

  public VerticallyMergedClasses(Map<DexType, DexType> mergedClasses) {
    Map<DexType, List<DexType>> sources = Maps.newIdentityHashMap();
    mergedClasses.forEach(
        (source, target) -> sources.computeIfAbsent(target, key -> new ArrayList<>()).add(source));
    this.mergedClasses = mergedClasses;
    this.sources = sources;
  }

  public List<DexType> getSourcesFor(DexType type) {
    return sources.getOrDefault(type, ImmutableList.of());
  }

  public DexType getTargetFor(DexType type) {
    assert mergedClasses.containsKey(type);
    return mergedClasses.get(type);
  }

  public boolean hasBeenMergedIntoSubtype(DexType type) {
    return mergedClasses.containsKey(type);
  }

  @Override
  public boolean verifyAllSourcesPruned(AppView<AppInfoWithLiveness> appView) {
    for (List<DexType> sourcesForTarget : sources.values()) {
      for (DexType source : sourcesForTarget) {
        assert appView.appInfo().wasPruned(source)
            : "Expected vertically merged class `" + source.toSourceString() + "` to be absent";
      }
    }
    return true;
  }
}
