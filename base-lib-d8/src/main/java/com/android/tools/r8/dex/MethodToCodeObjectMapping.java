// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import java.util.Collection;
import java.util.Map;

public abstract class MethodToCodeObjectMapping {

  public abstract DexCode getCode(DexEncodedMethod method);

  public abstract void clearCode(DexEncodedMethod method, boolean isSharedSynthetic);

  public abstract boolean verifyCodeObjects(Collection<DexCode> codes);

  public static MethodToCodeObjectMapping fromMethodBacking() {
    return MethodBacking.INSTANCE;
  }

  public static MethodToCodeObjectMapping fromMapBacking(Map<DexEncodedMethod, DexCode> map) {
    return new MapBacking(map);
  }

  private static class MethodBacking extends MethodToCodeObjectMapping {

    private static final MethodBacking INSTANCE = new MethodBacking();

    @Override
    public DexCode getCode(DexEncodedMethod method) {
      Code code = method.getCode();
      assert code == null || code.isDexCode();
      return code == null ? null : code.asDexCode();
    }

    @Override
    public void clearCode(DexEncodedMethod method, boolean isSharedSynthetic) {
      // When using methods directly any shared class needs to maintain its methods as read-only.
      if (!isSharedSynthetic) {
        method.removeCode();
      }
    }

    @Override
    public boolean verifyCodeObjects(Collection<DexCode> codes) {
      return true;
    }
  }

  private static class MapBacking extends MethodToCodeObjectMapping {

    private final Map<DexEncodedMethod, DexCode> codes;

    public MapBacking(Map<DexEncodedMethod, DexCode> codes) {
      this.codes = codes;
    }

    @Override
    public DexCode getCode(DexEncodedMethod method) {
      return codes.get(method);
    }

    @Override
    public void clearCode(DexEncodedMethod method, boolean isSharedSynthetic) {
      // We can safely clear the thread local pointer to even shared methods.
      codes.put(method, null);
    }

    @Override
    public boolean verifyCodeObjects(Collection<DexCode> codes) {
      assert this.codes.values().containsAll(codes);
      return true;
    }
  }
}
