// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Access flags common to classes, methods and fields. */
public abstract class AccessFlags<T extends AccessFlags<T>> {

  protected static final int BASE_FLAGS
      = Constants.ACC_PUBLIC
      | Constants.ACC_PRIVATE
      | Constants.ACC_PROTECTED
      | Constants.ACC_STATIC
      | Constants.ACC_FINAL
      | Constants.ACC_SYNTHETIC;

  // Ordered list of flag names. Must be consistent with getPredicates.
  private static final List<String> NAMES = ImmutableList.of(
      "public",
      "private",
      "protected",
      "static",
      "final",
      "synthetic"
  );

  // Get ordered list of flag predicates. Must be consistent with getNames.
  protected List<BooleanSupplier> getPredicates() {
    return ImmutableList.of(
        this::isPublic,
        this::isPrivate,
        this::isProtected,
        this::isStatic,
        this::isFinal,
        this::isSynthetic);
  }

  // Get ordered list of flag names. Must be consistent with getPredicates.
  protected List<String> getNames() {
    return NAMES;
  }

  protected int originalFlags;
  protected int modifiedFlags;

  protected AccessFlags(int originalFlags, int modifiedFlags) {
    this.originalFlags = originalFlags;
    this.modifiedFlags = modifiedFlags;
  }

  public abstract T copy();

  public abstract T self();

  public int materialize() {
    return modifiedFlags;
  }

  public abstract int getAsCfAccessFlags();

  public abstract int getAsDexAccessFlags();

  public final int getOriginalAccessFlags() {
    return originalFlags;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof AccessFlags) {
      AccessFlags other = (AccessFlags) object;
      return originalFlags == other.originalFlags && modifiedFlags == other.modifiedFlags;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return originalFlags | modifiedFlags;
  }

  public boolean isMoreVisibleThan(
      AccessFlags other, String packageNameThis, String packageNameOther) {
    int visibilityOrdinal = visibilityOrdinal();
    if (visibilityOrdinal > other.visibilityOrdinal()) {
      return true;
    }
    if (visibilityOrdinal == other.visibilityOrdinal()
        && isVisibilityDependingOnPackage()
        && !packageNameThis.equals(packageNameOther)) {
      return true;
    }
    return false;
  }

  public boolean isAtLeastAsVisibleAs(AccessFlags other) {
    return visibilityOrdinal() >= other.visibilityOrdinal();
  }

  public boolean isSameVisiblity(AccessFlags other) {
    return visibilityOrdinal() == other.visibilityOrdinal();
  }

  private int visibilityOrdinal() {
    // public > protected > package > private
    if (isPublic()) {
      return 3;
    }
    if (isProtected()) {
      return 2;
    }
    if (isPrivate()) {
      return 0;
    }
    // Package-private
    return 1;
  }

  public boolean isVisibilityDependingOnPackage() {
    return visibilityOrdinal() == 1 || visibilityOrdinal() == 2;
  }

  public boolean isPublic() {
    return isSet(Constants.ACC_PUBLIC);
  }

  public void setPublic() {
    assert !isPrivate() && !isProtected();
    set(Constants.ACC_PUBLIC);
  }

  public void unsetPublic() {
    unset(Constants.ACC_PUBLIC);
  }

  public boolean isPrivate() {
    return isSet(Constants.ACC_PRIVATE);
  }

  public void setPrivate() {
    assert !isPublic() && !isProtected();
    set(Constants.ACC_PRIVATE);
  }

  public void unsetPrivate() {
    unset(Constants.ACC_PRIVATE);
  }

  public boolean isProtected() {
    return isSet(Constants.ACC_PROTECTED);
  }

  public void setProtected() {
    assert !isPublic() && !isPrivate();
    set(Constants.ACC_PROTECTED);
  }

  public void unsetProtected() {
    unset(Constants.ACC_PROTECTED);
  }

  public boolean isStatic() {
    return isSet(Constants.ACC_STATIC);
  }

  public void setStatic() {
    set(Constants.ACC_STATIC);
  }

  public boolean isFinal() {
    return isSet(Constants.ACC_FINAL);
  }

  public void setFinal() {
    set(Constants.ACC_FINAL);
  }

  public void unsetFinal() {
    unset(Constants.ACC_FINAL);
  }

  public boolean isSynthetic() {
    return isSet(Constants.ACC_SYNTHETIC);
  }

  public void setSynthetic() {
    set(Constants.ACC_SYNTHETIC);
  }

  public void unsetSynthetic() {
    unset(Constants.ACC_SYNTHETIC);
  }

  public void promoteToFinal() {
    promote(Constants.ACC_FINAL);
  }

  public void demoteFromFinal() {
    demote(Constants.ACC_FINAL);
  }

  public boolean isPromotedToPublic() {
    return isPromoted(Constants.ACC_PUBLIC);
  }

  public void promoteToPublic() {
    demote(Constants.ACC_PRIVATE | Constants.ACC_PROTECTED);
    promote(Constants.ACC_PUBLIC);
  }

  public void promoteToStatic() {
    promote(Constants.ACC_STATIC);
  }

  private boolean wasSet(int flag) {
    return (originalFlags & flag) != 0;
  }

  protected boolean isSet(int flag) {
    return (modifiedFlags & flag) != 0;
  }

  protected void set(int flag) {
    originalFlags |= flag;
    modifiedFlags |= flag;
  }

  protected void unset(int flag) {
    originalFlags &= ~flag;
    modifiedFlags &= ~flag;
  }

  protected boolean isPromoted(int flag) {
    return !wasSet(flag) && isSet(flag);
  }

  protected void promote(int flag) {
    modifiedFlags |= flag;
  }

  protected void demote(int flag) {
    modifiedFlags &= ~flag;
  }

  public String toSmaliString() {
    return toStringInternal(true);
  }

  @Override
  public String toString() {
    return toStringInternal(false);
  }

  private String toStringInternal(boolean ignoreSuper) {
    List<String> names = getNames();
    List<BooleanSupplier> predicates = getPredicates();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < names.size(); i++) {
      if (predicates.get(i).getAsBoolean()) {
        if (!ignoreSuper || !names.get(i).equals("super")) {
          if (builder.length() > 0) {
            builder.append(' ');
          }
          builder.append(names.get(i));
        }
      }
    }
    return builder.toString();
  }
}
