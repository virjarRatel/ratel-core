// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.errors;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.StringUtils;
import java.util.Collection;

@Keep
public class DuplicateTypesDiagnostic implements Diagnostic {

  private final ClassReference type;
  private final Collection<Origin> origins;

  public DuplicateTypesDiagnostic(ClassReference type, Collection<Origin> origins) {
    assert type != null;
    assert origins.size() > 1;
    this.type = type;
    this.origins = origins;
  }

  /** Get the type that is defined multiple times. */
  public ClassReference getType() {
    return type;
  }

  /**
   * Get a collection of origins that define the type.
   *
   * <p>Note that this returned collection is not complete, thus there may be other origins of the
   * type not contained within the returned collection. The collection will have at least two
   * elements.
   *
   * @return Collection of origins defining the type.
   */
  public Collection<Origin> getOrigins() {
    return origins;
  }

  /**
   * Returns one of the origins defining the duplicated type.
   *
   * <p>Note that there are no guarentees on which element of the collection this returns, only that
   * it will hold that <code>d.getOrigins().contains(d.getOrigin())</code>.
   *
   * @return An origin defining the duplicated type.
   */
  @Override
  public Origin getOrigin() {
    return origins.iterator().next();
  }

  @Override
  public Position getPosition() {
    return Position.UNKNOWN;
  }

  @Override
  public String getDiagnosticMessage() {
    String typeName = DescriptorUtils.descriptorToJavaType(type.getDescriptor());
    return "Type " + typeName + " is defined multiple times: " + StringUtils.join(origins, ", ");
  }
}
