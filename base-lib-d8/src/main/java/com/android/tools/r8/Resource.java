// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;

/**
 * Base interface for application resources.
 *
 * Resources are inputs to the compilation that are provided from outside sources, e.g., the
 * command-line interface or API clients such as gradle. Each resource has an associated
 * {@link Origin} which is some opaque description of where the resource comes from. The D8/R8
 * compiler does not assume any particular structure of origin and does not rely on it for
 * compilation. The origin will be provided to diagnostics handlers so that they may detail what
 * resource was cause of some particular error.
 *
 * The D8/R8 compilers uses default implementations for various file-system resources, but the
 * client is free to provide their own.
 */
@KeepForSubclassing
public interface Resource {
  /**
   * Get the origin of the resource.
   *
   * The origin is a description of where the resource originates from. The client is free to define
   * what that means for a particular resource.
   */
  Origin getOrigin();
}
