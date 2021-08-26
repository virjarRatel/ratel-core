// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.Version;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.VersionProperties;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ProguardMapSupplier {

  public static final String MARKER_KEY_COMPILER = "compiler";
  public static final String MARKER_VALUE_COMPILER = "R8";
  public static final String MARKER_KEY_COMPILER_VERSION = "compiler_version";
  public static final String MARKER_KEY_COMPILER_HASH = "compiler_hash";
  public static final String MARKER_KEY_MIN_API = "min_api";
  public static final String MARKER_KEY_PG_MAP_ID = "pg_map_id";

  public static int PG_MAP_ID_LENGTH = 7;

  public static ProguardMapSupplier fromClassNameMapper(
      ClassNameMapper classNameMapper, InternalOptions options) {
    return new ProguardMapSupplier(true, classNameMapper, null, null, options);
  }

  public static ProguardMapSupplier fromNamingLens(
      NamingLens namingLens, DexApplication dexApplication, InternalOptions options) {
    return new ProguardMapSupplier(false, null, namingLens, dexApplication, options);
  }

  public static class ProguardMapAndId {
    public final String map;
    public final String id;

    ProguardMapAndId(String map, String id) {
      assert map != null && id != null;
      this.map = map;
      this.id = id;
    }
  }

  public ProguardMapSupplier(
      boolean useClassNameMapper,
      ClassNameMapper classNameMapper,
      NamingLens namingLens,
      DexApplication application,
      InternalOptions options) {
    this.useClassNameMapper = useClassNameMapper;
    this.classNameMapper = classNameMapper;
    this.namingLens = namingLens;
    this.application = application;
    this.minApiLevel = options.isGeneratingClassFiles() ? null : options.minApiLevel;
  }

  private final boolean useClassNameMapper;
  private final ClassNameMapper classNameMapper;
  private final NamingLens namingLens;
  private final DexApplication application;
  private final Integer minApiLevel;

  public ProguardMapAndId getProguardMapAndId() {
    String body = getBody();
    if (body == null || body.trim().length() == 0) {
      return null;
    }
    // Algorithm:
    // Hash of the non-whitespace codepoints of the input string.
    Hasher hasher = Hashing.murmur3_32().newHasher();
    body.codePoints().filter(c -> !Character.isWhitespace(c)).forEach(hasher::putInt);
    String proguardMapId = hasher.hash().toString().substring(0, PG_MAP_ID_LENGTH);

    StringBuilder builder = new StringBuilder();
    builder.append(
        "# "
            + MARKER_KEY_COMPILER
            + ": "
            + MARKER_VALUE_COMPILER
            + "\n"
            + "# "
            + MARKER_KEY_COMPILER_VERSION
            + ": "
            + Version.LABEL
            + "\n");
    if (minApiLevel != null) {
      builder.append("# " + MARKER_KEY_MIN_API + ": " + minApiLevel + "\n");
    }
    if (Version.isDevelopmentVersion()) {
      builder.append(
          "# " + MARKER_KEY_COMPILER_HASH + ": " + VersionProperties.INSTANCE.getSha() + "\n");
    }
    builder.append("# " + MARKER_KEY_PG_MAP_ID + ": " + proguardMapId + "\n");
    // Turn off linting of the mapping file in some build systems.
    builder.append("# common_typos_disable" + "\n");
    builder.append(body);

    return new ProguardMapAndId(builder.toString(), proguardMapId);
  }

  private String getBody() {
    if (useClassNameMapper) {
      assert classNameMapper != null;
      return classNameMapper.toString();
    }
    assert namingLens != null && application != null;
    // TODO(herhut): Should writing of the proguard-map file be split like this?
    if (!namingLens.isIdentityLens()) {
      StringBuilder map = new StringBuilder();
      new MinifiedNameMapPrinter(application, namingLens).write(map);
      return map.toString();
    }
    if (application.getProguardMap() != null) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      Writer writer = new PrintWriter(bytes);
      try {
        application.getProguardMap().write(writer);
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException("IOException while creating Proguard-map output: " + e);
      }
      return bytes.toString();
    }
    return null;
  }
}
