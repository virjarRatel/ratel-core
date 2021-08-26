// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.desugar.PrefixRewritingMapper;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// In L8, all classes not rewritten or emulated interfaces are pruned away.
// This avoids having non rewritten classes in the output when compiled with D8.
// This also avoids having non rewritten classes in the output.
public class L8TreePruner {

  private final InternalOptions options;
  private final Set<DexType> emulatedInterfaces = Sets.newIdentityHashSet();
  private final Set<DexType> backports = Sets.newIdentityHashSet();
  private final List<DexType> pruned = new ArrayList<>();

  public L8TreePruner(InternalOptions options) {
    this.options = options;
    backports.addAll(options.desugaredLibraryConfiguration.getBackportCoreLibraryMember().keySet());
    emulatedInterfaces.addAll(
        options.desugaredLibraryConfiguration.getEmulateLibraryInterface().keySet());
  }

  public DexApplication prune(DexApplication app, PrefixRewritingMapper rewritePrefix) {
    Map<DexType, DexProgramClass> typeMap = new IdentityHashMap<>();
    for (DexProgramClass aClass : app.classes()) {
      typeMap.put(aClass.type, aClass);
    }
    List<DexProgramClass> toKeep = new ArrayList<>();
    for (DexProgramClass aClass : app.classes()) {
      if (rewritePrefix.hasRewrittenType(aClass.type)
          || emulatedInterfaces.contains(aClass.type)
          || interfaceImplementsEmulatedInterface(aClass, typeMap)) {
        toKeep.add(aClass);
      } else {
        pruned.add(aClass.type);
      }
    }
    typeMap.clear();
    // TODO(b/134732760): Would be nice to add pruned type to the appView removedClasses instead
    // of just doing nothing with it.
    return app.builder().replaceProgramClasses(toKeep).build();
  }

  private boolean interfaceImplementsEmulatedInterface(
      DexClass itf, Map<DexType, DexProgramClass> typeMap) {
    if (!itf.isInterface()) {
      return false;
    }
    LinkedList<DexType> workList = new LinkedList<>();
    Collections.addAll(workList, itf.interfaces.values);
    while (!workList.isEmpty()) {
      DexType dexType = workList.removeFirst();
      if (emulatedInterfaces.contains(dexType)) {
        return true;
      }
      if (typeMap.containsKey(dexType)) {
        DexProgramClass dexProgramClass = typeMap.get(dexType);
        Collections.addAll(workList, dexProgramClass.interfaces.values);
      }
    }
    return false;
  }
}
