// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.experimental.graphinfo.GraphConsumer;
import com.android.tools.r8.experimental.graphinfo.GraphEdgeInfo;
import com.android.tools.r8.experimental.graphinfo.GraphNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Base implementation of a graph consumer that collects all of graph nodes and edges. */
public class CollectingGraphConsumer implements GraphConsumer {

  // Possible sub-consumer that is also inspecting the kept-graph.
  private final GraphConsumer subConsumer;

  // Directional map backwards from targets to direct sources.
  private final Map<GraphNode, Map<GraphNode, Set<GraphEdgeInfo>>> target2sources = new HashMap<>();

  public CollectingGraphConsumer(GraphConsumer subConsumer) {
    this.subConsumer = subConsumer;
  }

  @Override
  public void acceptEdge(GraphNode source, GraphNode target, GraphEdgeInfo info) {
    target2sources
        .computeIfAbsent(target, k -> new HashMap<>())
        .computeIfAbsent(source, k -> new HashSet<>())
        .add(info);
    if (subConsumer != null) {
      subConsumer.acceptEdge(source, target, info);
    }
  }

  public Set<GraphNode> getTargets() {
    return target2sources.keySet();
  }

  public Map<GraphNode, Set<GraphEdgeInfo>> getSourcesTargeting(GraphNode target) {
    return target2sources.get(target);
  }
}
