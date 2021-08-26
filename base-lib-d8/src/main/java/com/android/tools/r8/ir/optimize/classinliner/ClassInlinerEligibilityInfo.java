// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.classinliner;

import com.android.tools.r8.utils.OptionalBool;

public class ClassInlinerEligibilityInfo {

  /**
   * Set to {@link OptionalBool#TRUE} if the method is guaranteed to return the receiver, {@link
   * OptionalBool#FALSE} if the method is guaranteed not to return the receiver, and {@link
   * OptionalBool#UNKNOWN} if the method may return the receiver.
   */
  final OptionalBool returnsReceiver;

  public ClassInlinerEligibilityInfo(OptionalBool returnsReceiver) {
    this.returnsReceiver = returnsReceiver;
  }
}
