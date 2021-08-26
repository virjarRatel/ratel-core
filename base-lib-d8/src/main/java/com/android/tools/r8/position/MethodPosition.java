// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.position;

import com.android.tools.r8.Keep;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** A {@link Position} denoting a method. */
@Keep
public class MethodPosition implements Position {

  // Do not expose the internal dex method structure.
  private DexMethod method;

  public MethodPosition(DexMethod method) {
    this.method = method;
  }

  /** The unqualified name of the method. */
  public String getName() {
    return method.name.toString();
  }

  /** The type descriptor of the method holder. */
  public String getHolder() {
    return method.holder.toDescriptorString();
  }

  /** The type descriptor of the methods return type. */
  public String getReturnType() {
    return method.proto.returnType.toDescriptorString();
  }

  /** The type descriptors for the methods formal parameter types. */
  public List<String> getParameterTypes() {
    return Arrays.stream(method.proto.parameters.values)
        .map(DexType::toDescriptorString)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return method.toSourceString();
  }

  @Override
  public String getDescription() {
    return toString();
  }

  @Override
  public int hashCode() {
    return method.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof MethodPosition) {
      return method.equals(((MethodPosition) o).method);
    }
    return false;
  }
}
