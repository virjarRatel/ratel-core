// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.utils.VersionProperties;

/** Version of the D8/R8 library. */
public final class Version {

  // This field is accessed from release scripts using simple pattern matching.
  // Therefore, changing this field could break our release scripts.
  public static final String LABEL = "master";

  private Version() {
  }

  /** Returns current R8 version (with additional info) as a string. */
  public static String getVersionString() {
    return LABEL + " (" + VersionProperties.INSTANCE.getDescription() + ")";
  }

  /**
   * Returns the major version number of the compiler.
   *
   * @return Major version or -1 for an unreleased build.
   */
  public static int getMajorVersion() {
    return getMajorVersion(LABEL);
  }

  static int getMajorVersion(String label) {
    if (label.equals("master")) {
      return -1;
    }
    int start = 0;
    int end = label.indexOf('.');
    return Integer.parseInt(label.substring(start, end));
  }

  /**
   * Returns the minor version number of the compiler.
   *
   * @return Minor version or -1 for an unreleased build.
   */
  public static int getMinorVersion() {
    return getMinorVersion(LABEL);
  }

  static int getMinorVersion(String label) {
    if (label.equals("master")) {
      return -1;
    }
    int start = label.indexOf('.') + 1;
    int end = label.indexOf('.', start);
    return Integer.parseInt(label.substring(start, end));
  }

  /**
   * Returns the patch version number of the compiler.
   *
   * @return Patch version or -1 for an unreleased build.
   */
  public static int getPatchVersion() {
    return getPatchVersion(LABEL);
  }

  static int getPatchVersion(String label) {
    if (label.equals("master")) {
      return -1;
    }
    int skip = label.indexOf('.') + 1;
    int start = label.indexOf('.', skip) + 1;
    int end = label.indexOf('-', start);
    return Integer.parseInt(label.substring(start, end != -1 ? end : label.length()));
  }

  /**
   * Returns the pre-release version information of the compiler.
   *
   * @return Pre-release information if present, the empty string if absent, and null for an
   *     unreleased build.
   */
  public static String getPreReleaseString() {
    return getPreReleaseString(LABEL);
  }

  static String getPreReleaseString(String label) {
    if (label.equals("master")) {
      return null;
    }
    int start = label.indexOf('-') + 1;
    if (start > 0) {
      return label.substring(start);
    }
    return "";
  }

  /**
   * Is this a development version of the D8/R8 library.
   *
   * @return True if the build is not a release or if it is a development release.
   */
  public static boolean isDevelopmentVersion() {
    return isDevelopmentVersion(LABEL, VersionProperties.INSTANCE.isEngineering());
  }

  static boolean isDevelopmentVersion(String label, boolean isEngineering) {
    return label.equals("master") || label.endsWith("-dev") || isEngineering;
  }
}
