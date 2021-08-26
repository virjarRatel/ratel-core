// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// Helper class implementing bunch of default interface method handling operations.
final class DefaultMethodsHelper {
  // Current set of default interface methods, may overlap with `hidden`.
  private final Set<DexEncodedMethod> candidates = Sets.newIdentityHashSet();
  // Current set of known hidden default interface methods.
  private final Set<DexEncodedMethod> hidden = Sets.newIdentityHashSet();

  // Represents information about default interface methods of an
  // interface and its superinterfaces. Namely: a list of live (not hidden)
  // and hidden default interface methods in this interface's hierarchy.
  //
  // Note that it is assumes that these lists should never be big.
  final static class Collection {
    static final Collection EMPTY =
        new Collection(Collections.emptyList(), Collections.emptyList());

    // All live default interface methods in this interface's hierarchy.
    private final List<DexEncodedMethod> live;
    // All hidden default interface methods in this interface's hierarchy.
    private final List<DexEncodedMethod> hidden;

    private Collection(List<DexEncodedMethod> live, List<DexEncodedMethod> hidden) {
      this.live = live;
      this.hidden = hidden;
    }

    // If there is just one live method having specified
    // signature return it, otherwise return null.
    DexMethod getSingleCandidate(DexMethod method) {
      DexMethod candidate = null;
      for (DexEncodedMethod encodedMethod : live) {
        DexMethod current = encodedMethod.method;
        if (current.proto == method.proto && current.name == method.name) {
          if (candidate != null) {
            return null;
          }
          candidate = current;
        }
      }
      return candidate;
    }
  }

  final void merge(Collection collection) {
    candidates.addAll(collection.live);
    hidden.addAll(collection.hidden);
  }

  final void hideMatches(DexMethod method) {
    Iterator<DexEncodedMethod> it = candidates.iterator();
    while (it.hasNext()) {
      DexEncodedMethod candidate = it.next();
      if (method.match(candidate)) {
        hidden.add(candidate);
        it.remove();
      }
    }
  }

  final void addDefaultMethod(DexEncodedMethod encoded) {
    candidates.add(encoded);
  }

  // Create default interface collection based on collected information.
  final Collection wrapInCollection() {
    candidates.removeAll(hidden);
    return (candidates.isEmpty() && hidden.isEmpty()) ? Collection.EMPTY
        : new Collection(Lists.newArrayList(candidates), Lists.newArrayList(hidden));
  }
}
