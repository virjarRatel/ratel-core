// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ClassAccessFlags extends AccessFlags<ClassAccessFlags> {

  // List of valid flags for both DEX and Java.
  private static final int SHARED_FLAGS
      = AccessFlags.BASE_FLAGS
      | Constants.ACC_INTERFACE
      | Constants.ACC_ABSTRACT
      | Constants.ACC_ANNOTATION
      | Constants.ACC_ENUM;

  private static final int DEX_FLAGS
      = SHARED_FLAGS;

  private static final int CF_FLAGS
      = SHARED_FLAGS
      | Constants.ACC_SUPER;

  @Override
  protected List<String> getNames() {
    return new ImmutableList.Builder<String>()
        .addAll(super.getNames())
        .add("interface")
        .add("abstract")
        .add("annotation")
        .add("enum")
        .add("super")
        .build();
  }

  @Override
  protected List<BooleanSupplier> getPredicates() {
    return new ImmutableList.Builder<BooleanSupplier>()
        .addAll(super.getPredicates())
        .add(this::isInterface)
        .add(this::isAbstract)
        .add(this::isAnnotation)
        .add(this::isEnum)
        .add(this::isSuper)
        .build();
  }

  private ClassAccessFlags(int flags) {
    this(flags, flags);
  }

  private ClassAccessFlags(int originalFlags, int modifiedFlags) {
    super(originalFlags, modifiedFlags);
  }

  public static ClassAccessFlags fromSharedAccessFlags(int access) {
    assert (access & SHARED_FLAGS) == access;
    assert SHARED_FLAGS == DEX_FLAGS;
    return fromDexAccessFlags(access);
  }

  public static ClassAccessFlags fromDexAccessFlags(int access) {
    // Assume that the SUPER flag should be set (behaviour for Java versions > 1.1).
    return new ClassAccessFlags((access & DEX_FLAGS) | Constants.ACC_SUPER);
  }

  public static ClassAccessFlags fromCfAccessFlags(int access) {
    return new ClassAccessFlags(access & CF_FLAGS);
  }

  @Override
  public ClassAccessFlags copy() {
    return new ClassAccessFlags(originalFlags, modifiedFlags);
  }

  @Override
  public ClassAccessFlags self() {
    return this;
  }

  @Override
  public int getAsDexAccessFlags() {
    // We unset the super flag here, as it is meaningless in DEX. Furthermore, we add missing
    // abstract to interfaces to work around a javac bug when generating package-info classes.
    int flags = materialize() & ~Constants.ACC_SUPER;
    if (isInterface()) {
      return flags | Constants.ACC_ABSTRACT;
    }
    return flags;
  }

  @Override
  public int getAsCfAccessFlags() {
    return materialize();
  }

  /**
   * Checks whether the constraints from
   * https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1 are met.
   */
  public boolean areValid(int majorVersion, boolean isPackageInfo) {
    if (isInterface()) {
      // We ignore the super flags prior to JDK 9, as so did the VM.
      if ((majorVersion >= 53) && isSuper()) {
        return false;
      }
      // When not coming from DEX input we require interfaces to be abstract - except for
      // package-info classes - as both old versions of javac and other tools can produce
      // package-info classes that are interfaces but not abstract.
      if (((majorVersion > Constants.CORRESPONDING_CLASS_FILE_VERSION) && !isAbstract())
          && !isPackageInfo) {
        return false;
      }
      return !isFinal() && !isEnum();
    } else {
      return !isAnnotation() && (!isFinal() || !isAbstract());
    }
  }

  public boolean isInterface() {
    return isSet(Constants.ACC_INTERFACE);
  }

  public void setInterface() {
    set(Constants.ACC_INTERFACE);
  }

  public void unsetInterface() {
    unset(Constants.ACC_INTERFACE);
  }

  public boolean isAbstract() {
    return isSet(Constants.ACC_ABSTRACT);
  }

  public void setAbstract() {
    set(Constants.ACC_ABSTRACT);
  }

  public void unsetAbstract() {
    unset(Constants.ACC_ABSTRACT);
  }

  public boolean isAnnotation() {
    return isSet(Constants.ACC_ANNOTATION);
  }

  public void setAnnotation() {
    set(Constants.ACC_ANNOTATION);
  }

  public void unsetAnnotation() {
    unset(Constants.ACC_ANNOTATION);
  }

  public boolean isEnum() {
    return isSet(Constants.ACC_ENUM);
  }

  public void setEnum() {
    set(Constants.ACC_ENUM);
  }

  public boolean isSuper() {
    return isSet(Constants.ACC_SUPER);
  }

  public void setSuper() {
    set(Constants.ACC_SUPER);
  }

  public void unsetSuper() {
    unset(Constants.ACC_SUPER);
  }
}
