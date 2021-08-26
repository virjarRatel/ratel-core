// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import java.util.Collection;

/** Program resource provider. */
@KeepForSubclassing
public interface ProgramResourceProvider {

  Collection<ProgramResource> getProgramResources() throws ResourceException;

  default DataResourceProvider getDataResourceProvider() {
    return null;
  }
}
