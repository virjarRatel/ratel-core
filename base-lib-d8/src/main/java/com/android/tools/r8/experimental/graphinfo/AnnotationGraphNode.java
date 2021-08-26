// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.Keep;

@Keep
public final class AnnotationGraphNode extends GraphNode {

  private final GraphNode annotatedNode;

  public AnnotationGraphNode(GraphNode annotatedNode) {
    super(annotatedNode.isLibraryNode());
    this.annotatedNode = annotatedNode;
  }

  public GraphNode getAnnotatedNode() {
    return annotatedNode;
  }

  @Override
  public boolean equals(Object o) {
    return this == o
        || (o instanceof AnnotationGraphNode
            && ((AnnotationGraphNode) o).annotatedNode.equals(annotatedNode));
  }

  @Override
  public int hashCode() {
    return 7 * annotatedNode.hashCode();
  }

  @Override
  public String toString() {
    return "annotated " + annotatedNode.toString();
  }
}
