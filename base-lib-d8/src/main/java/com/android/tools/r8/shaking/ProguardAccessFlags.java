// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AccessFlags;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ProguardAccessFlags {

  private final static int PPP_MASK =
      new ProguardAccessFlags().setPublic().setProtected().setPrivate().flags;

  private int flags = 0;

  // Ordered list of flag names. Must be consistent with getPredicates.
  private static final List<String> NAMES = ImmutableList.of(
      "public",
      "private",
      "protected",
      "static",
      "final",
      "abstract",
      "volatile",
      "transient",
      "synchronized",
      "native",
      "strictfp",
      "synthetic",
      "bridge"
  );

  // Get ordered list of flag predicates. Must be consistent with getNames.
  private List<BooleanSupplier> getPredicates() {
    return ImmutableList.of(
        this::isPublic,
        this::isPrivate,
        this::isProtected,
        this::isStatic,
        this::isFinal,
        this::isAbstract,
        this::isVolatile,
        this::isTransient,
        this::isSynchronized,
        this::isNative,
        this::isStrict,
        this::isSynthetic,
        this::isBridge);
  }

  private boolean containsAll(int other) {
    // ppp flags are the flags public, protected and private.
    return
        // All non-ppp flags set must match (a 0 in non-ppp flags means don't care).
        (((flags & ~PPP_MASK) & (other & ~PPP_MASK)) == (flags & ~PPP_MASK))
            // With no ppp flags any flags match, with ppp flags there must be an overlap to match.
            && (((flags & PPP_MASK) == 0) || ((flags & PPP_MASK) & (other & PPP_MASK)) != 0);
  }

  private boolean containsNone(int other) {
    return (flags & other) == 0;
  }

  public boolean containsAll(AccessFlags other) {
    return containsAll(other.getOriginalAccessFlags());
  }

  public boolean containsNone(AccessFlags other) {
    return containsNone(other.getOriginalAccessFlags());
  }

  public void setFlags(AccessFlags other) {
    this.flags = other.getOriginalAccessFlags();
  }

  public ProguardAccessFlags setPublic() {
    set(Constants.ACC_PUBLIC);
    return this;
  }

  public boolean isPublic() {
    return isSet(Constants.ACC_PUBLIC);
  }

  public ProguardAccessFlags setPrivate() {
    set(Constants.ACC_PRIVATE);
    return this;
  }

  public boolean isPrivate() {
    return isSet(Constants.ACC_PRIVATE);
  }

  public ProguardAccessFlags setProtected() {
    set(Constants.ACC_PROTECTED);
    return this;
  }

  public boolean isProtected() {
    return isSet(Constants.ACC_PROTECTED);
  }

  public void setVisibility(ClassAccessFlags classAccessFlags) {
    if (classAccessFlags.isPublic()) {
      setPublic();
    } else if (classAccessFlags.isProtected()) {
      setProtected();
    } else if (classAccessFlags.isPrivate()) {
      setPrivate();
    }
  }

  public void setStatic() {
    set(Constants.ACC_STATIC);
  }

  public boolean isStatic() {
    return isSet(Constants.ACC_STATIC);
  }

  public void setFinal() {
    set(Constants.ACC_FINAL);
  }

  public boolean isFinal() {
    return isSet(Constants.ACC_FINAL);
  }

  public void setAbstract() {
    set(Constants.ACC_ABSTRACT);
  }

  public boolean isAbstract() {
    return isSet(Constants.ACC_ABSTRACT);
  }

  public void setVolatile() {
    set(Constants.ACC_VOLATILE);
  }

  public boolean isVolatile() {
    return isSet(Constants.ACC_VOLATILE);
  }

  public void setTransient() {
    set(Constants.ACC_TRANSIENT);
  }

  public boolean isTransient() {
    return isSet(Constants.ACC_TRANSIENT);
  }

  public void setSynchronized() {
    set(Constants.ACC_SYNCHRONIZED);
  }

  public boolean isSynchronized() {
    return isSet(Constants.ACC_SYNCHRONIZED);
  }

  public void setNative() {
    set(Constants.ACC_NATIVE);
  }

  public boolean isNative() {
    return isSet(Constants.ACC_NATIVE);
  }

  public void setStrict() {
    set(Constants.ACC_STRICT);
  }

  public boolean isStrict() {
    return isSet(Constants.ACC_STRICT);
  }

  public void setSynthetic() {
    set(Constants.ACC_SYNTHETIC);
  }

  public boolean isSynthetic() {
    return isSet(Constants.ACC_SYNTHETIC);
  }

  public void setBridge() {
    set(Constants.ACC_BRIDGE);
  }

  public boolean isBridge() {
    return isSet(Constants.ACC_BRIDGE);
  }

  private boolean isSet(int flag) {
    return (flags & flag) != 0;
  }

  private void set(int flag) {
    flags |= flag;
  }

  @Override
  public String toString() {
    List<BooleanSupplier> predicates = getPredicates();
    StringBuilder builder = new StringBuilder();
    boolean space = false;
    for (int i = 0; i < NAMES.size(); i++) {
      if (predicates.get(i).getAsBoolean()) {
        if (space) {
          builder.append(' ');
        } else {
          space = true;
        }
        builder.append(NAMES.get(i));
      }
    }
    return builder.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProguardAccessFlags)) {
      return false;
    }
    return this.flags == ((ProguardAccessFlags) obj).flags;
  }

  @Override
  public int hashCode() {
    return flags;
  }
}
