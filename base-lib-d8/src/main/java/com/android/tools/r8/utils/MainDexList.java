// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.DescriptorUtils.JAVA_PACKAGE_SEPARATOR;
import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;

import com.android.tools.r8.ResourceException;
import com.android.tools.r8.StringResource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.position.TextPosition;
import com.google.common.collect.Sets;
import java.util.Set;

public class MainDexList {

  public static DexType parseEntry(String clazz, DexItemFactory itemFactory) {
    if (!clazz.endsWith(CLASS_EXTENSION)) {
      throw new CompilationError("Illegal main-dex-list entry '" + clazz + "'.");
    }
    String name = clazz.substring(0, clazz.length() - CLASS_EXTENSION.length());
    if (name.contains("" + JAVA_PACKAGE_SEPARATOR)) {
      throw new CompilationError("Illegal main-dex-list entry '" + clazz + "'.");
    }
    String descriptor = "L" + name + ";";
    return itemFactory.createType(descriptor);
  }

  public static Set<DexType> parseList(StringResource resource, DexItemFactory itemFactory) {
    String lines;
    try {
      lines = resource.getString();
    } catch (ResourceException e) {
      throw new CompilationError("Failed to parse main-dex resource", e, resource.getOrigin());
    }
    Set<DexType> result = Sets.newIdentityHashSet();
    int lineNumber = 0;
    for (int offset = 0; offset < lines.length(); ) {
      ++lineNumber;
      int newLineIndex = lines.indexOf('\n', offset);
      int lineEnd = newLineIndex == -1 ? lines.length() : newLineIndex;
      String line = StringUtils.trim(lines.substring(offset, lineEnd));
      if (!line.isEmpty()) {
        try {
          result.add(parseEntry(line, itemFactory));
        } catch (CompilationError e) {
          throw new CompilationError(e.getMessage(), e, resource.getOrigin(),
              new TextPosition(offset, lineNumber, TextPosition.UNKNOWN_COLUMN));
        }
      }
      offset = lineEnd + 1;
    }
    return result;
  }
}
