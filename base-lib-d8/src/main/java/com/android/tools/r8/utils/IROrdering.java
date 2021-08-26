// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface IROrdering {

  Iterable<DexEncodedMethod> order(Iterable<DexEncodedMethod> methods);

  Collection<DexEncodedMethod> order(Collection<DexEncodedMethod> methods);

  class IdentityIROrdering implements IROrdering {

    private static final IdentityIROrdering INSTANCE = new IdentityIROrdering();

    private IdentityIROrdering() {}

    public static IdentityIROrdering getInstance() {
      return INSTANCE;
    }

    @Override
    public Iterable<DexEncodedMethod> order(Iterable<DexEncodedMethod> methods) {
      return methods;
    }

    @Override
    public Collection<DexEncodedMethod> order(Collection<DexEncodedMethod> methods) {
      return methods;
    }
  }

  class NondeterministicIROrdering implements IROrdering {

    private static final NondeterministicIROrdering INSTANCE = new NondeterministicIROrdering();

    private NondeterministicIROrdering() {}

    public static NondeterministicIROrdering getInstance() {
      return INSTANCE;
    }

    @Override
    public List<DexEncodedMethod> order(Iterable<DexEncodedMethod> methods) {
      List<DexEncodedMethod> toShuffle = Lists.newArrayList(methods);
      Collections.shuffle(toShuffle);
      return toShuffle;
    }

    @Override
    public List<DexEncodedMethod> order(Collection<DexEncodedMethod> methods) {
      return order((Iterable<DexEncodedMethod>) methods);
    }
  }
}
