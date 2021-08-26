// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.utils.BiMapContainer;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;

/**
 * Provides a translation between class names based on a source and target proguard map.
 *
 * A mapping consists of:
 *
 * <ul>
 *   <li> {@link #translation} a bidirectional mapping between obfuscated names on the source
 *   proguard map to the corresponding class in the target proguard map
 *   <li> {@link #newClasses} a set of the unobfuscated names of classes that are in the source but
 *   not the target map
 *   <li> {@link #unusedNames} a set of names in the target map that are not used by the source map
 * </ul>
 */
public class ClassRenamingMapper {

  public static ClassRenamingMapper from(ClassNameMapper originalMap, ClassNameMapper targetMap) {
    ImmutableBiMap.Builder<String, String> translationBuilder = ImmutableBiMap.builder();
    ImmutableSet.Builder<String> newClasses = ImmutableSet.builder();

    Map<String, String> sourceOriginalToObfuscated =
        originalMap.getObfuscatedToOriginalMapping().inverse;

    BiMapContainer<String, String> targetMapping = targetMap.getObfuscatedToOriginalMapping();
    Map<String, String> targetOriginalToObfuscated = targetMapping.inverse;

    for (String originalName : sourceOriginalToObfuscated.keySet()) {
      String sourceObfuscatedName = sourceOriginalToObfuscated.get(originalName);
      String targetObfuscatedName = targetOriginalToObfuscated.get(originalName);
      if (targetObfuscatedName == null) {
        newClasses.add(originalName);
        continue;
      }
      translationBuilder.put(sourceObfuscatedName, targetObfuscatedName);
    }

    ImmutableBiMap<String, String> translation = translationBuilder.build();
    ImmutableSet<String> unusedNames =
        ImmutableSet.copyOf(Sets.difference(targetMapping.original.keySet(), translation.values()));

    return new ClassRenamingMapper(translation, newClasses.build(), unusedNames);
  }

  /**
   * Mapping from obfuscated class names in the source map to their counterpart in the target name
   * map.
   */
  public final ImmutableBiMap<String, String> translation;

  /**
   * Set of (unobfuscated) class names that are present in the source map but not in the target map.
   */
  public final ImmutableSet<String> newClasses;

  /**
   * Set of (obfuscated) class names that are present in the target map but not in the source map.
   */
  public final ImmutableSet<String> unusedNames;

  private ClassRenamingMapper(ImmutableBiMap<String, String> translation,
      ImmutableSet<String> newClasses, ImmutableSet<String> unusedNames) {
    this.translation = translation;
    this.newClasses = newClasses;
    this.unusedNames = unusedNames;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Translation:\n\n");
    for (String name : translation.keySet()) {
      String newName = translation.get(name);
      builder.append(name.equals(newName) ? "    " : " --- ")
          .append(name)
          .append(" -> ")
          .append(newName)
          .append('\n');
    }
    builder.append("\nNew classes:\n\n");
    for (String name : newClasses) {
      builder.append("    ")
          .append(name)
          .append('\n');
    }
    builder.append("\nUnused names:\n\n");
    for (String unused : unusedNames) {
      builder.append("    ")
          .append(unused)
          .append('\n');
    }
    return builder.toString();
  }
}
