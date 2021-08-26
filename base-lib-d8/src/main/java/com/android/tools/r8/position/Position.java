// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.position;

import com.android.tools.r8.Keep;

/**
 * Represent a position in a resource, it can for example be line in a text file or the byte offset
 * in a binary stream.
 */
@Keep
public interface Position {

  /**
   * Position is unknown.
   */
  Position UNKNOWN = new Position() {
    @Override
    public String getDescription() {
      return "Unknown";
    }
  };

  /**
   * A user friendly text representation of this position.
   */
  String getDescription();

}
