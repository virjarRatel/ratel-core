// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.shaking.AppInfoWithLiveness;

public class ArgumentRemovalUtils {

  // Returns true if this method is pinned from the perspective of optimizations that attempt to
  // remove method arguments.
  public static boolean isPinned(DexEncodedMethod method, AppView<AppInfoWithLiveness> appView) {
    return appView.appInfo().isPinned(method.method)
        || appView.appInfo().bootstrapMethods.contains(method.method)
        || appView.appInfo().brokenSuperInvokes.contains(method.method)
        || appView.appInfo().methodsTargetedByInvokeDynamic.contains(method.method)
        || method.accessFlags.isNative();
  }
}
