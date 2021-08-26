// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.KeepForSubclassing;

@KeepForSubclassing
public interface GraphConsumer {

  /**
   * Registers a directed edge in the graph.
   *
   * @param source The source node of the edge.
   * @param target The target node of the edge.
   * @param info Additional information about the edge.
   */
  void acceptEdge(GraphNode source, GraphNode target, GraphEdgeInfo info);
}
