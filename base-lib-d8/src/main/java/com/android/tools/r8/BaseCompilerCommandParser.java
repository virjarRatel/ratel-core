// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.StringDiagnostic;

public class BaseCompilerCommandParser {

  static void parseMinApi(BaseCompilerCommand.Builder builder, String minApiString, Origin origin) {
    int minApi;
    try {
      minApi = Integer.valueOf(minApiString);
    } catch (NumberFormatException e) {
      builder.error(new StringDiagnostic("Invalid argument to --min-api: " + minApiString, origin));
      return;
    }
    if (minApi < 1) {
      builder.error(new StringDiagnostic("Invalid argument to --min-api: " + minApiString, origin));
      return;
    }
    builder.setMinApiLevel(minApi);
  }
}
