// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.NestMemberClassAttribute;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

// This pass
// - cleans the nests: it removes missing nest host/members from the input and
// report them (warning or error).
// - clears nests which do not use nest based access control to allow other
// optimizations such as class merging to perform better.
public class NestReducer {

  private AppView<?> appView;

  public NestReducer(AppView<?> appView) {
    this.appView = appView;
  }

  private DexClass definitionFor(DexType type) {
    assert appView.graphLense().lookupType(type) == type;
    return appView.definitionFor(appView.graphLense().lookupType(type));
  }

  public void run(ExecutorService executorService) throws ExecutionException {
    Set<DexType> nestHosts = Sets.newIdentityHashSet();
    List<Future<?>> futures = new ArrayList<>();
    // It is possible that a nest member is on the program path but its nest host
    // is only in the class path.
    // Nests are therefore computed the first time a nest member is met, host or not.
    // The computedNestHosts list is there to avoid processing multiple times the same nest.
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      DexType hostType = clazz.getNestHost();
      if (hostType != null && !nestHosts.contains(hostType)) {
        nestHosts.add(hostType);
        futures.add(
            executorService.submit(
                () -> {
                  processNestFrom(clazz);
                  return null; // we want a Callable not a Runnable to be able to throw
                }));
      }
    }
    ThreadUtils.awaitFutures(futures);
  }

  private void processNestFrom(DexClass clazz) {
    DexClass nestHost = definitionFor(clazz.getNestHost());
    if (nestHost == null) {
      reportMissingNestHost(clazz);
      clazz.clearNestHost();
      return;
    }
    boolean hasPrivateMembers = hasPrivateMembers(nestHost);
    Iterator<NestMemberClassAttribute> iterator =
        nestHost.getNestMembersClassAttributes().iterator();
    boolean reported = false;
    while (iterator.hasNext()) {
      DexClass member = definitionFor(iterator.next().getNestMember());
      if (member == null) {
        if (!reported) {
          reported = true;
          reportIncompleteNest(nestHost);
        }
        iterator.remove();
      } else {
        hasPrivateMembers = hasPrivateMembers || hasPrivateMembers(member);
      }
    }
    if (!hasPrivateMembers && appView.options().enableNestReduction) {
      clearNestAttributes(nestHost);
    }
  }

  private void reportMissingNestHost(DexClass clazz) {
    if (appView.options().ignoreMissingClasses) {
      appView.options().warningMissingClassMissingNestHost(clazz);
    } else {
      appView.options().errorMissingClassMissingNestHost(clazz);
    }
  }

  private void reportIncompleteNest(DexClass nestHost) {
    List<DexType> nest = new ArrayList<>(nestHost.getNestMembersClassAttributes().size() + 1);
    for (NestMemberClassAttribute attr : nestHost.getNestMembersClassAttributes()) {
      nest.add(attr.getNestMember());
    }
    nest.add(nestHost.type);
    if (appView.options().ignoreMissingClasses) {
      appView.options().warningMissingClassIncompleteNest(nest, appView);
    } else {
      appView.options().errorMissingClassIncompleteNest(nest, appView);
    }
  }

  private void clearNestAttributes(DexClass nestHost) {
    nestHost.getNestMembersClassAttributes().clear();
    for (NestMemberClassAttribute attr : nestHost.getNestMembersClassAttributes()) {
      DexClass member =
          appView.definitionFor(appView.graphLense().lookupType(attr.getNestMember()));
      member.clearNestHost();
    }
  }

  private boolean hasPrivateMembers(DexClass clazz) {
    for (DexEncodedMethod method : clazz.methods()) {
      if (method.accessFlags.isPrivate()) {
        return true;
      }
    }
    for (DexEncodedField field : clazz.fields()) {
      if (field.accessFlags.isPrivate()) {
        return true;
      }
    }
    return false;
  }
}
