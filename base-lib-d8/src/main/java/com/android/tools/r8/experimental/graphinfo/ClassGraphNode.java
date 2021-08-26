// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.Keep;
import com.android.tools.r8.references.ClassReference;

@Keep
public final class ClassGraphNode extends GraphNode {

  private final ClassReference reference;

  public ClassGraphNode(boolean isLibraryNode, ClassReference reference) {
    super(isLibraryNode);
    assert reference != null;
    this.reference = reference;
  }

  public ClassReference getReference() {
    return reference;
  }

  @Override
  public boolean equals(Object o) {
    return this == o
        || (o instanceof ClassGraphNode && ((ClassGraphNode) o).reference == reference);
  }

  @Override
  public int hashCode() {
    return reference.hashCode();
  }

  @Override
  public String toString() {
    return reference.getDescriptor();
  }
}
