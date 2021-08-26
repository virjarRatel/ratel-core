// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;

/**
 * Implements class path filtering as per
 * <a href="https://www.guardsquare.com/en/proguard/manual/usage#classpath">ProGuards classpath</a>
 * documentation.
 * <p>
 * Some of the implementation details are derived from the examples. For example the implict
 * catch-all positive filter after a trailing negative filter.
 */
public class FilteredClassPath {

  private final Path path;
  private final ImmutableList<String> pattern;
  private final Origin origin;
  private final Position position;

  public FilteredClassPath(Path path, List<String> pattern, Origin origin, Position position) {
    this.path = path;
    this.pattern = ImmutableList.copyOf(pattern);
    this.origin = origin;
    this.position = position;
  }

  private FilteredClassPath(Path path) {
    this(path, ImmutableList.of(), Origin.unknown(), Position.UNKNOWN);
  }

  public static FilteredClassPath unfiltered(Path path) {
    return new FilteredClassPath(path);
  }

  public Path getPath() {
    return path;
  }

  public Origin getOrigin() {
    return origin;
  }

  public Position getPosition() {
    return position;
  }

  public boolean matchesFile(String name) {
    if (isUnfiltered()) {
      return true;
    }
    boolean isNegated = false;
    for (String pattern : pattern) {
      isNegated = pattern.charAt(0) == '!';
      boolean matches = matchAgainstFileName(name, 0, pattern, isNegated ? 1 : 0);
      if (matches) {
        return !isNegated;
      }
    }
    // If the last filter was a negated one, we do catch all positive thereafter.
    return isNegated;
  }

  private boolean containsFileSeparator(String string) {
    return string.indexOf('/') != -1;
  }

  private boolean matchAgainstFileName(String fileName, int namePos, String pattern,
      int patternPos) {
    if (patternPos >= pattern.length()) {
      // We have exhausted the pattern before the filename.
      return namePos == fileName.length();
    }
    char currentPattern = pattern.charAt(patternPos);
    if (currentPattern == '*') {
      boolean includeFileSeparators =
          pattern.length() > patternPos + 1 && pattern.charAt(patternPos + 1) == '*';
      if (includeFileSeparators) {
        patternPos++;
      }
      // Common case where the end is a file name suffix without further wildcards.
      String remainingPattern = pattern.substring(patternPos + 1);
      if (remainingPattern.indexOf('*') == -1) {
        // The pattern contains no multi-char wildcards, so only the postfix has to match.
        int remaining = remainingPattern.length();
        if (namePos + remaining > fileName.length()) {
          // Exhausted the name too early.
          return false;
        }
        if (includeFileSeparators
            || !containsFileSeparator(fileName.substring(namePos, fileName.length() - remaining))) {
          return matchAgainstFileName(fileName, fileName.length() - remaining, pattern,
              patternPos + 1);
        }
      } else {
        for (int i = namePos; i < fileName.length(); i++) {
          if (!includeFileSeparators && fileName.charAt(i) == '/') {
            return false;
          }
          if (matchAgainstFileName(fileName, i, pattern, patternPos + 1)) {
            return true;
          }
        }
      }
    } else {
      if (namePos >= fileName.length()) {
        return false;
      }
      if (currentPattern == '?' || currentPattern == fileName.charAt(namePos)) {
        return matchAgainstFileName(fileName, namePos + 1, pattern, patternPos + 1);
      }
    }
    return false;
  }

  public boolean isUnfiltered() {
    return pattern.isEmpty();
  }

  @Override
  public String toString() {
    if (isUnfiltered()) {
      return path.toString();
    }
    StringBuilder builder = new StringBuilder();
    builder.append(path);
    builder.append('(');
    boolean first = true;
    for (String pattern : pattern) {
      if (!first) {
        builder.append(',');
      }
      builder.append(pattern);
      first = false;
    }
    builder.append(')');
    return builder.toString();
  }
}
