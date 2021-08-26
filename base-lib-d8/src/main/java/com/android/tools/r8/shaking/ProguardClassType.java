// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexClass;

public enum ProguardClassType {
  ANNOTATION_INTERFACE {
    @Override
    public boolean matches(DexClass clazz) {
      return clazz.accessFlags.isInterface() && clazz.accessFlags.isAnnotation();
    }
  },
  CLASS {
    @Override
    public boolean matches(DexClass clazz) {
      return true;
    }
  },
  ENUM {
    @Override
    public boolean matches(DexClass clazz) {
      return clazz.accessFlags.isEnum();
    }
  },
  INTERFACE {
    @Override
    public boolean matches(DexClass clazz) {
      return clazz.accessFlags.isInterface() && !clazz.accessFlags.isAnnotation();
    }
  },
  UNSPECIFIED {
    @Override
    public boolean matches(DexClass clazz) {
      return true;
    }
  };

  @Override
  public String toString() {
    switch (this) {
      case ANNOTATION_INTERFACE: return "@interface";
      case CLASS: return "class";
      case ENUM: return "enum";
      case INTERFACE: return "interface";
      case UNSPECIFIED:
        return "";
      default:
        throw new Unreachable("Invalid proguard class type '" + this + "'");
    }
  }

  public abstract boolean matches(DexClass clazz);
}
