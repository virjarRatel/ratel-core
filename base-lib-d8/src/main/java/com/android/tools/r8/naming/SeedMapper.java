// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static com.android.tools.r8.utils.DescriptorUtils.descriptorToInternalName;
import static com.android.tools.r8.utils.DescriptorUtils.descriptorToJavaType;
import static com.android.tools.r8.utils.DescriptorUtils.javaTypeToDescriptor;

import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.utils.Reporter;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mappings read from the given ProGuard map.
 * <p>
 * The main differences of this against {@link ClassNameMapper} and
 * {@link ClassNameMapper#getObfuscatedToOriginalMapping()} are:
 *   1) the key is the original descriptor, not the obfuscated java name. Thus, it is much easier
 *   to look up what mapping to apply while traversing {@link DexType}s; and
 *   2) the value is {@link ClassNamingForMapApplier}, another variant of {@link ClassNaming},
 *   which also uses original {@link Signature} as a key, instead of renamed {@link Signature}.
 */
public class SeedMapper implements ProguardMap {

  static class Builder extends ProguardMap.Builder {
    final Map<String, ClassNamingForMapApplier.Builder> map = new HashMap<>();
    private final Reporter reporter;

    private Builder(Reporter reporter) {
      this.reporter = reporter;
    }

    @Override
    ClassNamingForMapApplier.Builder classNamingBuilder(
        String renamedName, String originalName, Position position) {
      String originalDescriptor = javaTypeToDescriptor(originalName);
      ClassNamingForMapApplier.Builder classNamingBuilder =
          ClassNamingForMapApplier.builder(
              javaTypeToDescriptor(renamedName), originalDescriptor, position, reporter);
      if (map.put(originalDescriptor, classNamingBuilder) != null) {
        reporter.error(ProguardMapError.duplicateSourceClass(originalName, position));
      }
      return classNamingBuilder;
    }

    @Override
    SeedMapper build() {
      reporter.failIfPendingErrors();
      return new SeedMapper(ImmutableMap.copyOf(map), reporter);
    }
  }

  static Builder builder(Reporter reporter) {
    return new Builder(reporter);
  }

  private static SeedMapper seedMapperFromInputStream(Reporter reporter, InputStream in)
      throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    try (ProguardMapReader proguardReader = new ProguardMapReader(reader)) {
      SeedMapper.Builder builder = SeedMapper.builder(reporter);
      proguardReader.parse(builder);
      return builder.build();
    }
  }

  public static SeedMapper seedMapperFromFile(Reporter reporter, Path path) throws IOException {
    return seedMapperFromInputStream(reporter, Files.newInputStream(path));
  }

  private final ImmutableMap<String, ClassNamingForMapApplier> mappings;
  private final Reporter reporter;

  private SeedMapper(Map<String, ClassNamingForMapApplier.Builder> mappings, Reporter reporter) {
    this.reporter = reporter;
    ImmutableMap.Builder<String, ClassNamingForMapApplier> builder = ImmutableMap.builder();
    for(Map.Entry<String, ClassNamingForMapApplier.Builder> entry : mappings.entrySet()) {
      builder.put(entry.getKey(), entry.getValue().build());
    }
    this.mappings = builder.build();
    verifyMappingsAreConflictFree();
  }

  private void verifyMappingsAreConflictFree() {
    Map<String, String> seenMappings = new HashMap<>();
    for (String key : mappings.keySet()) {
      ClassNamingForMapApplier classNaming = mappings.get(key);
      String existing = seenMappings.put(classNaming.renamedName, key);
      if (existing != null) {
        reporter.error(
            ProguardMapError.duplicateTargetClass(
                descriptorToJavaType(key),
                descriptorToJavaType(existing),
                descriptorToInternalName(classNaming.renamedName),
                classNaming.position));
      }
      // TODO(b/136694827) Enable when we have proper support
      // Map<Signature, MemberNaming> seenMembers = new HashMap<>();
      // classNaming.forAllMemberNaming(
      //     memberNaming -> {
      //       MemberNaming existingMember =
      //           seenMembers.put(memberNaming.renamedSignature, memberNaming);
      //       if (existingMember != null) {
      //         reporter.error(
      //             ProguardMapError.duplicateTargetSignature(
      //                 existingMember.signature,
      //                 memberNaming.signature,
      //                 memberNaming.getRenamedName(),
      //                 memberNaming.position));
      //       }
      //     });
    }
    reporter.failIfPendingErrors();
  }

  @Override
  public boolean hasMapping(DexType type) {
    return mappings.containsKey(type.descriptor.toString());
  }

  @Override
  public ClassNamingForMapApplier getClassNaming(DexType type) {
    return mappings.get(type.descriptor.toString());
  }

  public Set<String> getKeyset() {
    return mappings.keySet();
  }

  public ClassNamingForMapApplier getMapping(String key) {
    return mappings.get(key);
  }
}
