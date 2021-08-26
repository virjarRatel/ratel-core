// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.references;

import com.android.tools.r8.Keep;
import com.android.tools.r8.errors.Unreachable;

@Keep
public abstract class PrimitiveReference implements TypeReference {

  static final PrimitiveReference BOOL =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "Z";
        }
      };

  static final PrimitiveReference BYTE =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "B";
        }
      };

  static final PrimitiveReference CHAR =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "C";
        }
      };

  static final PrimitiveReference SHORT =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "S";
        }
      };

  static final PrimitiveReference INT =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "I";
        }
      };

  static final PrimitiveReference FLOAT =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "F";
        }
      };

  static final PrimitiveReference LONG =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "J";
        }
      };

  static final PrimitiveReference DOUBLE =
      new PrimitiveReference() {
        @Override
        public String getDescriptor() {
          return "D";
        }
      };

  private PrimitiveReference() {}

  static PrimitiveReference fromDescriptor(String descriptor) {
    assert descriptor.length() == 1;
    switch (descriptor.charAt(0)) {
      case 'Z':
        return BOOL;
      case 'B':
        return BYTE;
      case 'C':
        return CHAR;
      case 'S':
        return SHORT;
      case 'I':
        return INT;
      case 'F':
        return FLOAT;
      case 'J':
        return LONG;
      case 'D':
        return DOUBLE;
      default:
        throw new Unreachable("Invalid primitive descriptor: " + descriptor);
    }
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public PrimitiveReference asPrimitive() {
    return this;
  }

  @Override
  public abstract String getDescriptor();

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
