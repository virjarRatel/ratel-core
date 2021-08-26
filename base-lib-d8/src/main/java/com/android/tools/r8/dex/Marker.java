// Copyright (c) 2017, the Rex project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.graph.DexString;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * Abstraction for hidden dex marker intended for the main dex file.
 */
public class Marker {

  public static final String VERSION = "version";
  public static final String MIN_API = "min-api";
  public static final String SHA1 = "sha-1";
  public static final String COMPILATION_MODE = "compilation-mode";
  public static final String HAS_CHECKSUMS = "has-checksums";
  public static final String PG_MAP_ID = "pg-map-id";

  public enum Tool {
    D8,
    R8,
    L8;

    public static Tool[] valuesR8andD8() {
      return new Tool[] {Tool.D8, Tool.R8};
    }
  }

  private static final char PREFIX_CHAR = '~';
  private static final String PREFIX = "~~";
  private static final String D8_PREFIX = PREFIX + Tool.D8 + "{";
  private static final String R8_PREFIX = PREFIX + Tool.R8 + "{";
  private static final String L8_PREFIX = PREFIX + Tool.L8 + "{";

  private final JsonObject jsonObject;
  private final Tool tool;

  public Marker(Tool tool) {
    this(tool, new JsonObject());
  }

  private Marker(Tool tool, JsonObject jsonObject) {
    this.tool = tool;
    this.jsonObject = jsonObject;
  }

  public Tool getTool() {
    return tool;
  }

  public boolean isD8() {
    return tool == Tool.D8;
  }

  public boolean isR8() {
    return tool == Tool.R8;
  }

  public boolean isL8() {
    return tool == Tool.L8;
  }

  public String getVersion() {
    return jsonObject.get(VERSION).getAsString();
  }

  public Marker setVersion(String version) {
    assert !jsonObject.has(VERSION);
    jsonObject.addProperty(VERSION, version);
    return this;
  }

  public Long getMinApi() {
    return jsonObject.get(MIN_API).getAsLong();
  }

  public Marker setMinApi(long minApi) {
    assert !jsonObject.has(MIN_API);
    jsonObject.addProperty(MIN_API, minApi);
    return this;
  }

  public String getSha1() {
    return jsonObject.get(SHA1).getAsString();
  }

  public Marker setSha1(String sha1) {
    assert !jsonObject.has(SHA1);
    jsonObject.addProperty(SHA1, sha1);
    return this;
  }

  public String getCompilationMode() {
    return jsonObject.get(COMPILATION_MODE).getAsString();
  }

  public Marker setCompilationMode(CompilationMode mode) {
    assert !jsonObject.has(COMPILATION_MODE);
    jsonObject.addProperty(COMPILATION_MODE, mode.toString().toLowerCase());
    return this;
  }

  public boolean getHasChecksums() {
    return jsonObject.get(HAS_CHECKSUMS).getAsBoolean();
  }

  public Marker setHasChecksums(boolean hasChecksums) {
    assert !jsonObject.has(HAS_CHECKSUMS);
    jsonObject.addProperty(HAS_CHECKSUMS, hasChecksums);
    return this;
  }

  public String getPgMapId() {
    return jsonObject.get(PG_MAP_ID).getAsString();
  }

  public Marker setPgMapId(String pgMapId) {
    assert !jsonObject.has(PG_MAP_ID);
    jsonObject.addProperty(PG_MAP_ID, pgMapId);
    return this;
  }

  @Override
  public String toString() {
    // In order to make printing of markers deterministic we sort the entries by key.
    final JsonObject sortedJson = new JsonObject();
    jsonObject.entrySet()
        .stream()
        .sorted(Comparator.comparing(Entry::getKey))
        .forEach(entry -> sortedJson.add(entry.getKey(), entry.getValue()));
    return PREFIX + tool + sortedJson;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Marker) {
      Marker other = (Marker) obj;
      return (tool == other.tool) && jsonObject.equals(other.jsonObject);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return tool.hashCode() + 3 * jsonObject.hashCode();
  }

  // Try to parse str as a marker.
  // Returns null if parsing fails.
  public static Marker parse(DexString dexString) {
    if (dexString.size > 2
        && dexString.content[0] == PREFIX_CHAR
        && dexString.content[1] == PREFIX_CHAR) {
      String str = dexString.toString();
      if (str.startsWith(D8_PREFIX)) {
        return internalParse(Tool.D8, str.substring(D8_PREFIX.length() - 1));
      }
      if (str.startsWith(R8_PREFIX)) {
        return internalParse(Tool.R8, str.substring(R8_PREFIX.length() - 1));
      }
      if (str.startsWith(L8_PREFIX)) {
        return internalParse(Tool.L8, str.substring(L8_PREFIX.length() - 1));
      }
    }
    return null;
  }

  private static Marker internalParse(Tool tool, String str) {
    try {
      JsonElement result =  new JsonParser().parse(str);
      if (result.isJsonObject()) {
        return new Marker(tool, result.getAsJsonObject());
      }
    } catch (JsonSyntaxException e) {
      // Fall through.
    }
    return null;
  }
}
