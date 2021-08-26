// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;

public class Position {

  // A no-position marker. Not having a position means the position is implicitly defined by the
  // context, e.g., the marker does not materialize anything concrete.
  private static final Position NO_POSITION = new Position(-1, null, null, null, false);

  // A synthetic marker position that should never materialize.
  // This is used specifically to mark exceptional exit blocks from synchronized methods in release.
  private static final Position NO_POSITION_SYNTHETIC = new Position(-1, null, null, null, true);

  // Fake position to use for representing an actual position in testing code.
  private static final Position TESTING_POSITION = new Position(0, null, null, null, true);

  public final int line;
  public final DexString file;
  public final boolean synthetic;

  // If there's no inlining, callerPosition is null.
  //
  // For an inlined instruction its Position contains the inlinee's line and method and
  // callerPosition is the position of the invoke instruction in the caller.

  public final DexMethod method;
  public final Position callerPosition;

  public Position(int line, DexString file, DexMethod method, Position callerPosition) {
    this(line, file, method, callerPosition, false);
    assert line >= 0;
    assert method != null;
  }

  private Position(
      int line, DexString file, DexMethod method, Position callerPosition, boolean synthetic) {
    this.line = line;
    this.file = file;
    this.synthetic = synthetic;
    this.method = method;
    this.callerPosition = callerPosition;
    assert callerPosition == null || callerPosition.method != null;
  }

  public static Position synthetic(int line, DexMethod method, Position callerPosition) {
    assert line >= 0;
    assert method != null;
    return new Position(line, null, method, callerPosition, true);
  }

  public static Position none() {
    return NO_POSITION;
  }

  public static Position syntheticNone() {
    return NO_POSITION_SYNTHETIC;
  }

  @VisibleForTesting
  public static Position testingPosition() {
    return TESTING_POSITION;
  }

  // This factory method is used by the Inliner to create Positions when the caller has no valid
  // positions. Since the callee still may have valid positions we need a non-null Position to set
  // it as the caller of the inlined Positions.
  public static Position noneWithMethod(DexMethod method, Position callerPosition) {
    assert method != null;
    return new Position(-1, null, method, callerPosition, false);
  }

  public static Position getPositionForInlining(
      AppView<?> appView, InvokeMethod invoke, DexEncodedMethod context) {
    Position position = invoke.getPosition();
    if (position.method == null) {
      assert position.isNone();
      position = Position.noneWithMethod(context.method, null);
    }
    assert position.callerPosition == null
        || position.getOutermostCaller().method
            == appView.graphLense().getOriginalMethodSignature(context.method);
    return position;
  }

  public boolean isNone() {
    return line == -1;
  }

  public boolean isSyntheticNone() {
    return this == NO_POSITION_SYNTHETIC;
  }

  public boolean isSome() {
    return !isNone();
  }

  // Follow the linked list of callerPositions and return the last.
  // Return this if no inliner.
  public Position getOutermostCaller() {
    Position lastPosition = this;
    while (lastPosition.callerPosition != null) {
      lastPosition = lastPosition.callerPosition;
    }
    return lastPosition;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof Position) {
      Position o = (Position) other;
      return line == o.line
          && file == o.file
          && method == o.method
          && synthetic == o.synthetic
          && Objects.equals(callerPosition, o.callerPosition);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = line;
    result = 31 * result + Objects.hashCode(file);
    result = 31 * result + (synthetic ? 1 : 0);
    result = 31 * result + Objects.hashCode(method);
    result = 31 * result + Objects.hashCode(callerPosition);
    return result;
  }

  private String toString(boolean forceMethod) {
    if (isNone()) {
      return "--";
    }
    StringBuilder builder = new StringBuilder();
    if (file != null) {
      builder.append(file).append(":");
    }
    builder.append("#").append(line);
    if (method != null && (forceMethod || callerPosition != null)) {
      builder.append(":").append(method.name);
    }
    if (callerPosition != null) {
      Position caller = callerPosition;
      while (caller != null) {
        builder.append(";").append(caller.line).append(":").append(caller.method.name);
        caller = caller.callerPosition;
      }
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return toString(false);
  }
}
