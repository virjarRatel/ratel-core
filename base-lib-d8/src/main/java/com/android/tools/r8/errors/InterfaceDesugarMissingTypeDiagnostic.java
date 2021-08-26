// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.errors;

import com.android.tools.r8.Keep;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.references.ClassReference;

/**
 * Diagnostic for missing types needed for correct desugaring of default/static interface methods.
 */
@Keep
public class InterfaceDesugarMissingTypeDiagnostic implements DesugarDiagnostic {

  private final Origin origin;
  private final Position position;
  private final ClassReference missingType;
  private final ClassReference contextType;

  // Note: the implementing context is not yet made part of the public API as the context could be
  // both in the implements clause or from a lambda class in a member.
  private final ClassReference implementingContextType;

  public InterfaceDesugarMissingTypeDiagnostic(
      Origin origin,
      Position position,
      ClassReference missingType,
      ClassReference contextType,
      ClassReference implementingContextType) {
    assert origin != null;
    assert position != null;
    assert missingType != null;
    assert contextType != null;
    this.origin = origin;
    this.position = position;
    this.missingType = missingType;
    this.contextType = contextType;
    // The implementing context is optional.
    this.implementingContextType = implementingContextType;
  }

  /** Get the origin of a class leading to this warning. */
  @Override
  public Origin getOrigin() {
    return origin;
  }

  /** Get additional position information about the context leading to this warning. */
  @Override
  public Position getPosition() {
    return position;
  }

  /** Get the type that is missing. */
  public ClassReference getMissingType() {
    return missingType;
  }

  /** Get the type that requires knowledge of the missing type. */
  public ClassReference getContextType() {
    return contextType;
  }

  @Override
  public String getDiagnosticMessage() {
    StringBuilder builder =
        new StringBuilder()
            .append("Type `")
            .append(missingType.getTypeName())
            .append("` was not found, ")
            .append("it is required for default or static interface methods desugaring of `");
    if (position != Position.UNKNOWN) {
      builder.append(position.getDescription());
    } else {
      builder.append(contextType.getTypeName());
    }
    builder.append("`");
    if (implementingContextType != null) {
      builder
          .append(" This missing interface is declared in the direct hierarchy of `")
          .append(implementingContextType)
          .append("`");
    }
    return builder.toString();
  }
}
