// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;

/**
 * Checked exception for resource related failures.
 *
 * For example, this is the expected exception that must be thrown if a resource fails to produce
 * its content. See {@link ProgramResource#getByteStream()} and {@link StringResource#getString()}.
 */
@Keep
public class ResourceException extends Exception {

  private final Origin origin;

  public ResourceException(Origin origin, String message) {
    super(message);
    this.origin = origin;
  }

  public ResourceException(Origin origin, Throwable cause) {
    super(cause);
    this.origin = origin;
  }

  public ResourceException(Origin origin, String message, Throwable cause) {
    super(message, cause);
    this.origin = origin;
  }

  public Origin getOrigin() {
    return origin;
  }
}
