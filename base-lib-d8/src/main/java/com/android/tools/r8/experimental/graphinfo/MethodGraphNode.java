// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.Keep;
import com.android.tools.r8.references.MethodReference;

@Keep
public final class MethodGraphNode extends GraphNode {

  private final MethodReference reference;

  public MethodGraphNode(boolean isLibraryNode, MethodReference reference) {
    super(isLibraryNode);
    assert reference != null;
    this.reference = reference;
  }

  public MethodReference getReference() {
    return reference;
  }

  @Override
  public boolean equals(Object o) {
    return this == o
        || (o instanceof MethodGraphNode && ((MethodGraphNode) o).reference == reference);
  }

  @Override
  public int hashCode() {
    return reference.hashCode();
  }

  @Override
  public String toString() {
    return reference.toString();
  }
}
