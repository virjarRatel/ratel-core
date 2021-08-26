// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import java.util.Set;

/**
 * Program resource provider for program resources of class-file kind.
 *
 * <p>Note that the classes are generally lazily requested and the corresponding class structures
 * only created on-demand when they are needed by the compiler. If never needed, the resource will
 * never be loaded.
 */
@KeepForSubclassing
public interface ClassFileResourceProvider {

  /**
   * Get the set of all class descriptors that can be provided.
   *
   * <p>For user program classes (eg, not class-path or library resources), this set must be
   * defined.
   *
   * @return Set of all class descriptors that can be provided or null if the set is unknown.
   */
  Set<String> getClassDescriptors();

  /**
   * Get the program resource associated with the descriptor, or null if this provider does not have
   * a resource for the descriptor.
   *
   * <p>Method may be called several times for the same resource, and should support concurrent
   * calls from different threads.
   */
  ProgramResource getProgramResource(String descriptor);
}
