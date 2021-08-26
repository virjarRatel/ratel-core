// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.Keep;

@Keep
public abstract class GraphNode {

  private static final GraphNode CYCLE =
      new GraphNode(false) {
        @Override
        public boolean equals(Object o) {
          return o == this;
        }

        @Override
        public int hashCode() {
          return 0;
        }

        @Override
        public String toString() {
          return "cycle";
        }
      };

  private final boolean isLibraryNode;

  public GraphNode(boolean isLibraryNode) {
    this.isLibraryNode = isLibraryNode;
  }

  public static GraphNode cycle() {
    return CYCLE;
  }

  public final boolean isCycle() {
    return this == cycle();
  }

  public boolean isLibraryNode() {
    return isLibraryNode;
  }

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
