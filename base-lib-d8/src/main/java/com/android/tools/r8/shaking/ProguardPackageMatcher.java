// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexType;

public class ProguardPackageMatcher {
  private final String pattern;

  public ProguardPackageMatcher(String pattern) {
    this.pattern = pattern;
  }

  public boolean matches(DexType type) {
    return matchPackageNameImpl(pattern, 0, type.getPackageName(), 0);
  }

  private static boolean matchPackageNameImpl(
      String pattern, int patternIndex, String name, int nameIndex) {
    for (int i = patternIndex; i < pattern.length(); i++) {
      char patternChar = pattern.charAt(i);
      switch (patternChar) {
        case '*':
          int nextPatternIndex = i + 1;
          // Check for **.
          boolean includeSeparators =
              pattern.length() > (nextPatternIndex) && pattern.charAt(nextPatternIndex) == '*';
          if (includeSeparators) {
            nextPatternIndex += 1;
          }

          // Fast cases for the common case where a pattern ends with '*' or '**'.
          if (nextPatternIndex == pattern.length()) {
            if (includeSeparators) {
              return true;
            }
            boolean hasSeparators = containsSeparatorsStartingAt(name, nameIndex);
            return !hasSeparators;
          }

          // Match the rest of the pattern against the (non-empty) rest of the class name.
          for (int nextNameIndex = nameIndex; nextNameIndex < name.length(); nextNameIndex++) {
            if (!includeSeparators) {
              // Stop at the first separator for just *.
              if (name.charAt(nextNameIndex) == '.') {
                return matchPackageNameImpl(pattern, nextPatternIndex, name, nextNameIndex);
              }
            }
            if (matchPackageNameImpl(pattern, nextPatternIndex, name, nextNameIndex)) {
              return true;
            }
          }

          // Finally, check the case where the '*' or '**' eats all of the package name.
          return matchPackageNameImpl(pattern, nextPatternIndex, name, name.length());

        case '?':
          if (nameIndex == name.length() || name.charAt(nameIndex) == '.') {
            return false;
          }
          nameIndex++;
          break;

        default:
          if (nameIndex == name.length() || patternChar != name.charAt(nameIndex++)) {
            return false;
          }
          break;
      }
    }
    return nameIndex == name.length();
  }

  private static boolean containsSeparatorsStartingAt(String className, int nameIndex) {
    return className.indexOf('.', nameIndex) != -1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProguardPackageMatcher)) {
      return false;
    }
    ProguardPackageMatcher other = (ProguardPackageMatcher) o;
    return pattern.equals(other.pattern);
  }

  @Override
  public int hashCode() {
    return pattern.hashCode();
  }
}
