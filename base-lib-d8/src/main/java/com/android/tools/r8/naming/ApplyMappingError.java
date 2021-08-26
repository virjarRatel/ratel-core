// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.utils.StringDiagnostic;

public class ApplyMappingError extends StringDiagnostic {

  private static final String EXISTING_MESSAGE_START =
      "'%s' cannot be mapped to '%s' because it is in conflict with an existing ";
  private static final String EXISTING_MESSAGE_END =
      ". This usually happens when compiling a test application against a source application and "
          + "having short generic names in the test application. Try giving '%s' a more specific "
          + "name or add a keep rule to keep '%s'.";

  protected static final String EXISTING_CLASS_MESSAGE =
      EXISTING_MESSAGE_START + "class with the same name" + EXISTING_MESSAGE_END;
  protected static final String EXISTING_MEMBER_MESSAGE =
      EXISTING_MESSAGE_START + "member with the same signature" + EXISTING_MESSAGE_END;

  private ApplyMappingError(String message, Position position) {
    super(message, Origin.unknown(), position);
  }

  static ApplyMappingError mapToExistingClass(
      String originalName, String renamedName, Position position) {
    return new ApplyMappingError(
        String.format(EXISTING_CLASS_MESSAGE, originalName, renamedName, renamedName, originalName),
        position);
  }

  static ApplyMappingError mapToExistingMember(
      String originalName, String renamedName, Position position) {
    return new ApplyMappingError(
        String.format(
            EXISTING_MEMBER_MESSAGE, originalName, renamedName, renamedName, originalName),
        position);
  }
}
