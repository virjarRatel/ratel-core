// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.inliner;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.optimize.Inliner.Reason;

public interface InliningReasonStrategy {

  Reason computeInliningReason(InvokeMethod invoke, DexEncodedMethod target);
}
