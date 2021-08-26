// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProguardConfigurationSourceStrings implements ProguardConfigurationSource {

  private final Path basePath;
  private final List<String> config;
  private final Origin origin;

  /**
   * Creates {@link ProguardConfigurationSource} with raw {@param config}, along with
   * {@param basePath}, which allows all other options that use a relative path to reach out
   * to desired paths appropriately.
   */
  public ProguardConfigurationSourceStrings(List<String> config, Path basePath, Origin origin) {
    this.basePath = basePath;
    this.config = config;
    this.origin = origin;
  }

  private ProguardConfigurationSourceStrings(List<String> config) {
    this(config, Paths.get(""), Origin.unknown());
  }

  @VisibleForTesting
  public static ProguardConfigurationSourceStrings createConfigurationForTesting(
      List<String> config) {
    return new ProguardConfigurationSourceStrings(config);
  }

  @Override
  public String get() {
    return String.join(System.lineSeparator(), config);
  }

  @Override
  public Path getBaseDirectory() {
    return basePath;
  }

  @Override
  public String getName() {
    return "<no file>";
  }

  @Override
  public Origin getOrigin() {
    return origin;
  }

}
