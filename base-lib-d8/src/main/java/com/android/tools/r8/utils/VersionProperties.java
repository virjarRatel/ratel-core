// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A class describing version properties.
 */
public class VersionProperties {

  public static final VersionProperties INSTANCE = get();

  private static final String VERSION_CODE_KEY = "version-file.version.code";
  private static final String SHA_KEY = "version.sha";
  private static final String RELEASER_KEY = "releaser";

  private static final String RESOURCE_NAME = "r8-version.properties";

  private String sha;
  private String releaser;

  private static VersionProperties get() {
    ClassLoader loader = VersionProperties.class.getClassLoader();
    try (InputStream resourceStream = loader.getResourceAsStream(RESOURCE_NAME)) {
      return resourceStream == null
          ? new VersionProperties()
          : new VersionProperties(resourceStream);
    } catch (IOException e) {
      return new VersionProperties();
    }
  }

  private VersionProperties() {
  }

  private VersionProperties(InputStream resourceStream) throws IOException {
    Properties prop = new Properties();
    prop.load(resourceStream);

    long versionFileVersion = Long.parseLong(prop.getProperty(VERSION_CODE_KEY));
    assert versionFileVersion >= 1;

    sha = prop.getProperty(SHA_KEY);
    releaser = prop.getProperty(RELEASER_KEY);
  }

  public String getDescription() {
    return "build " + getSha() + (releaser != null ? " from " + releaser : "");
  }

  public String getSha() {
    return isEngineering() ? "engineering" : sha;
  }

  @Override
  public String toString() {
    return sha + " from " + releaser;
  }

  public boolean isEngineering() {
    return sha == null || sha.trim().isEmpty();
  }
}
