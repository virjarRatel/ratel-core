// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.errors.Unreachable;

public abstract class ProguardWildcard {

  abstract void setCaptured(String captured);
  abstract void clearCaptured();
  abstract String getCaptured();
  abstract ProguardWildcard materialize();

  boolean isPattern() {
    return false;
  }

  Pattern asPattern() {
    return null;
  }

  boolean isBackReference() {
    return false;
  }

  BackReference asBackReference() {
    return null;
  }

  static class Pattern extends ProguardWildcard {
    final String pattern;
    private String captured = null;

    Pattern(String pattern) {
      this.pattern = pattern;
    }

    @Override
    synchronized void setCaptured(String captured) {
      this.captured = captured;
    }

    @Override
    synchronized void clearCaptured() {
      captured = null;
    }

    @Override
    synchronized String getCaptured() {
      return captured;
    }

    @Override
    Pattern materialize() {
      if (captured == null) {
        return this;
      }
      Pattern copy = new Pattern(pattern);
      copy.setCaptured(captured);
      return copy;
    }

    @Override
    boolean isPattern() {
      return true;
    }

    @Override
    Pattern asPattern() {
      return this;
    }

    @Override
    public String toString() {
      return pattern;
    }
  }

  static class BackReference extends ProguardWildcard {
    // Back-reference is not referable, hence the other type, Pattern, here.
    Pattern reference;
    final int referenceIndex;

    BackReference(int referenceIndex) {
      this.referenceIndex = referenceIndex;
    }

    void setReference(Pattern reference) {
      this.reference = reference;
    }

    @Override
    void setCaptured(String captured) {
      throw new Unreachable("A back reference refers back to a previously matched wildcard.");
    }

    @Override
    void clearCaptured() {
      // no-op
    }

    @Override
    String getCaptured() {
      return reference != null ? reference.getCaptured() : null;
    }

    @Override
    BackReference materialize() {
      if (reference == null || reference.getCaptured() == null) {
        return this;
      }
      BackReference copy = new BackReference(referenceIndex);
      copy.setReference(reference.materialize());
      return copy;
    }

    @Override
    boolean isBackReference() {
      return true;
    }

    @Override
    BackReference asBackReference() {
      return this;
    }

    @Override
    public String toString() {
      return "<" + referenceIndex + ">";
    }
  }

}
