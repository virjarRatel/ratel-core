// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiscardedChecker {

  private final Set<DexReference> checkDiscarded;
  private final Iterable<DexProgramClass> classes;

  public DiscardedChecker(RootSet rootSet, Iterable<DexProgramClass> classes) {
    this.checkDiscarded = new HashSet<>(rootSet.checkDiscarded);
    this.classes = classes;
  }

  public List<DexDefinition> run() {
    List<DexDefinition> failed = new ArrayList<>(checkDiscarded.size());
    // TODO(b/131668850): Lookup the definition based on the reference.
    for (DexProgramClass clazz : classes) {
      checkItem(clazz, failed);
      clazz.forEachMethod(method -> checkItem(method, failed));
      clazz.forEachField(field -> checkItem(field, failed));
    }
    return failed;
  }

  private void checkItem(DexDefinition item, List<DexDefinition> failed) {
    DexReference reference = item.toReference();
    if (checkDiscarded.contains(reference)) {
      failed.add(item);
    }
  }
}
