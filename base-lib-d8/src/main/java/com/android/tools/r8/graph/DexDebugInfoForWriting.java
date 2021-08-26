// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.graph.DexDebugEvent.SetInlineFrame;
import java.util.Arrays;

/**
 * Wraps DexDebugInfo to make comparison and hashcode not consider
 * the SetInlineFrames
 */
public class DexDebugInfoForWriting extends DexDebugInfo {

  public DexDebugInfoForWriting(DexDebugInfo dexDebugInfo) {
    super(dexDebugInfo.startLine, dexDebugInfo.parameters,
        Arrays.stream(dexDebugInfo.events)
            .filter(d -> !(d instanceof SetInlineFrame))
            .toArray(DexDebugEvent[]::new));
  }

}
