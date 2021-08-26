// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class ProguardPathList {

  public static Builder builder() {
    return new Builder();
  }

  public static ProguardPathList emptyList() {
    return new EmptyPathList();
  }

  abstract boolean matches(String path);

  public static class Builder {

    private final ImmutableList.Builder<FileNameMatcher> matchers = ImmutableList.builder();

    private Builder() {
    }

    public Builder addFileName(String path) {
      return addFileName(path, false);
    }

    public Builder addFileName(String path, boolean isNegated) {
      matchers.add(new FileNameMatcher(isNegated, path));
      return this;
    }

    ProguardPathList build() {
      List<FileNameMatcher> matchers = this.matchers.build();
      if (matchers. size() > 0) {
        return new PathList(matchers);
      } else {
        return emptyList();
      }
    }
  }

  private static class FileNameMatcher {
    public final boolean negated;
    public final String pattern;

    FileNameMatcher(boolean negated, String pattern) {
      this.negated = negated;
      this.pattern = pattern;
    }

    private boolean match(String path) {
      return matchImpl(pattern, 0, path, 0);
    }

    private boolean matchImpl(String pattern, int patternIndex, String path, int pathIndex) {
      for (int i = patternIndex; i < pattern.length(); i++) {
        char patternChar = pattern.charAt(i);
        switch (patternChar) {
          case '*':
            boolean includeSeparators = pattern.length() > (i + 1) && pattern.charAt(i + 1) == '*';
            int nextPatternIndex = i + (includeSeparators ? 2 : 1);
            // Fast cases for the common case where a pattern ends with '**' or '*'.
            if (nextPatternIndex == pattern.length()) {
              return includeSeparators || !containsSeparatorsStartingAt(path, pathIndex);
            }
            // Match the rest of the pattern against the (non-empty) rest of the class name.
            for (int nextPathIndex = pathIndex; nextPathIndex < path.length(); nextPathIndex++) {
              if (!includeSeparators && path.charAt(nextPathIndex) == '/') {
                return matchImpl(pattern, nextPatternIndex, path, nextPathIndex);
              }
              if (matchImpl(pattern, nextPatternIndex, path, nextPathIndex)) {
                return true;
              }
            }
            break;
          case '?':
            if (pathIndex == path.length() || path.charAt(pathIndex++) == '/') {
              return false;
            }
            break;
          default:
            if (pathIndex == path.length() || patternChar != path.charAt(pathIndex++)) {
              return false;
            }
            break;
        }
      }
      return pathIndex == path.length();
    }

    private boolean containsSeparatorsStartingAt(String path, int pathIndex) {
      return path.indexOf('/', pathIndex) != -1;
    }

  }

  private static class PathList extends ProguardPathList {
    private final List<FileNameMatcher> matchers;

    private PathList(List<FileNameMatcher> matchers) {
      this.matchers = matchers;
    }

    @Override
    boolean matches(String path) {
      for (FileNameMatcher matcher : matchers) {
        if (matcher.match(path)) {
          // If we match a negation, abort as non-match. If we match a positive, return true.
          return !matcher.negated;
        }
      }
      return false;
    }
  }

  private static class EmptyPathList extends ProguardPathList {

    private EmptyPathList() {
    }

    @Override
    boolean matches(String path) {
      return true;
    }
  }
}
