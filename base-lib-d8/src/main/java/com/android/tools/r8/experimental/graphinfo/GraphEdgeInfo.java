// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.experimental.graphinfo;

public class GraphEdgeInfo {

  private static GraphEdgeInfo UNKNOWN = new GraphEdgeInfo(EdgeKind.Unknown);

  public static GraphEdgeInfo unknown() {
    return UNKNOWN;
  }

  // TODO(b/120959039): Simplify these. Most of the information is present in the source node.
  public enum EdgeKind {
    // Prioritized list of edge types.
    KeepRule,
    CompatibilityRule,
    ConditionalKeepRule,
    KeepRulePrecondition,
    InstantiatedIn,
    InvokedViaSuper,
    TargetedBySuper,
    InvokedFrom,
    InvokedFromLambdaCreatedIn,
    AnnotatedOn,
    ReferencedFrom,
    ReflectiveUseFrom,
    ReachableFromLiveType,
    ReferencedInAnnotation,
    IsLibraryMethod,
    OverridingMethod,
    MethodHandleUseFrom,
    CompanionClass,
    CompanionMethod,
    Unknown
  }

  private final EdgeKind kind;

  public GraphEdgeInfo(EdgeKind kind) {
    this.kind = kind;
  }

  public EdgeKind edgeKind() {
    return kind;
  }

  public String getInfoPrefix() {
    switch (edgeKind()) {
      case KeepRule:
      case CompatibilityRule:
      case ConditionalKeepRule:
        return "referenced in keep rule";
      case KeepRulePrecondition:
        return "satisfied with precondition";
      case InstantiatedIn:
        return "instantiated in";
      case InvokedViaSuper:
        return "invoked via super from";
      case TargetedBySuper:
        return "targeted by super from";
      case InvokedFrom:
        return "invoked from";
      case InvokedFromLambdaCreatedIn:
        return "invoked from lambda created in";
      case AnnotatedOn:
        return "annotated on";
      case ReferencedFrom:
        return "referenced from";
      case ReflectiveUseFrom:
        return "reflected from";
      case ReachableFromLiveType:
        return "reachable from";
      case ReferencedInAnnotation:
        return "referenced in annotation";
      case OverridingMethod:
        return "overriding method";
      case IsLibraryMethod:
        // TODO(b/120959039): It would be good to also surface the defining library type.
        return "defined in library method overridden by";
      case MethodHandleUseFrom:
        return "referenced by method handle";
      case CompanionClass:
        return "companion class for";
      case CompanionMethod:
        return "companion method for";
      default:
        assert false : "Unknown edge kind: " + edgeKind();
        // fall through
      case Unknown:
        return "kept for unknown reasons";
    }
  }

  @Override
  public String toString() {
    return "{edge-type:" + kind.toString() + "}";
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof GraphEdgeInfo && ((GraphEdgeInfo) o).kind == kind);
  }

  @Override
  public int hashCode() {
    return kind.hashCode();
  }
}
