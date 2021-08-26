// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.position.TextPosition;
import com.android.tools.r8.position.TextRange;
import com.android.tools.r8.shaking.ProguardKeepRule;
import com.android.tools.r8.shaking.ProguardKeepRuleBase;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

// Note: this could potentially be merged with ConditionalKeepRuleGraphNode
// and an empty precondition set.
@Keep
public final class KeepRuleGraphNode extends GraphNode {

  private final Origin origin;
  private final Position position;
  private final String content;
  private final Set<GraphNode> preconditions;

  public KeepRuleGraphNode(ProguardKeepRule rule) {
    this(rule, Collections.emptySet());
  }

  public KeepRuleGraphNode(ProguardKeepRuleBase rule, Set<GraphNode> preconditions) {
    super(false);
    assert rule != null;
    assert preconditions != null;
    origin = rule.getOrigin();
    position = rule.getPosition();
    content = rule.getSource();
    this.preconditions = preconditions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KeepRuleGraphNode)) {
      return false;
    }
    KeepRuleGraphNode other = (KeepRuleGraphNode) o;
    return origin.equals(other.getOrigin())
        && position.equals(other.getPosition())
        && Objects.equals(content, other.getContent())
        && preconditions.equals(other.getPreconditions());
  }

  @Override
  public int hashCode() {
    return Objects.hash(origin, position, content, preconditions);
  }

  public Origin getOrigin() {
    return origin;
  }

  public Position getPosition() {
    return position;
  }

  public String getContent() {
    return content;
  }

  public Set<GraphNode> getPreconditions() {
    return preconditions;
  }

  /**
   * Get an identity string determining this keep rule.
   *
   * <p>The identity string is typically the source-file (if present) followed by the line number.
   * {@code <keep-rule-file>:<keep-rule-start-line>:<keep-rule-start-column>}.
   */
  @Override
  public String toString() {
    return (getOrigin() == Origin.unknown() ? getContent() : getOrigin())
        + ":"
        + shortPositionInfo(getPosition());
  }

  private static String shortPositionInfo(Position position) {
    if (position instanceof TextRange) {
      TextPosition start = ((TextRange) position).getStart();
      return start.getLine() + ":" + start.getColumn();
    }
    if (position instanceof TextPosition) {
      TextPosition start = (TextPosition) position;
      return start.getLine() + ":" + start.getColumn();
    }
    return position.getDescription();
  }
}
