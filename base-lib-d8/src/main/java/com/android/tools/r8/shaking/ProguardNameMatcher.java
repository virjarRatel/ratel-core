// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.shaking.ProguardConfigurationParser.IdentifierPatternWithWildcards;
import com.android.tools.r8.shaking.ProguardWildcard.BackReference;
import com.android.tools.r8.shaking.ProguardWildcard.Pattern;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ProguardNameMatcher {

  private static final ProguardNameMatcher MATCH_ALL_NAMES = new MatchAllNames();

  private ProguardNameMatcher() {
  }

  public static ProguardNameMatcher create(
      IdentifierPatternWithWildcards identifierPatternWithWildcards) {
    if (identifierPatternWithWildcards.isMatchAllNames()) {
      return MATCH_ALL_NAMES;
    } else if (identifierPatternWithWildcards.wildcards.isEmpty()) {
      return new MatchSpecificName(identifierPatternWithWildcards.pattern);
    } else {
      return new MatchNamePattern(identifierPatternWithWildcards);
    }
  }

  private static boolean matchFieldOrMethodNameImpl(
      String pattern, int patternIndex,
      String name, int nameIndex,
      List<ProguardWildcard> wildcards, int wildcardIndex) {
    ProguardWildcard wildcard;
    Pattern wildcardPattern;
    BackReference backReference;
    for (int i = patternIndex; i < pattern.length(); i++) {
      char patternChar = pattern.charAt(i);
      switch (patternChar) {
        case '*':
          wildcard = wildcards.get(wildcardIndex);
          assert wildcard.isPattern();
          wildcardPattern = wildcard.asPattern();
          // Match the rest of the pattern against the rest of the name.
          for (int nextNameIndex = nameIndex; nextNameIndex <= name.length(); nextNameIndex++) {
            wildcardPattern.setCaptured(name.substring(nameIndex, nextNameIndex));
            if (matchFieldOrMethodNameImpl(
                pattern, i + 1, name, nextNameIndex, wildcards, wildcardIndex + 1)) {
              return true;
            }
          }
          return false;
        case '?':
          wildcard = wildcards.get(wildcardIndex);
          assert wildcard.isPattern();
          if (nameIndex == name.length()) {
            return false;
          }
          wildcardPattern = wildcard.asPattern();
          wildcardPattern.setCaptured(name.substring(nameIndex, nameIndex + 1));
          nameIndex++;
          wildcardIndex++;
          break;
        case '<':
          wildcard = wildcards.get(wildcardIndex);
          assert wildcard.isBackReference();
          backReference = wildcard.asBackReference();
          String captured = backReference.getCaptured();
          if (captured == null
              || name.length() < nameIndex + captured.length()
              || !captured.equals(name.substring(nameIndex, nameIndex + captured.length()))) {
            return false;
          }
          nameIndex = nameIndex + captured.length();
          wildcardIndex++;
          i = pattern.indexOf(">", i);
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

  public abstract boolean matches(String name);

  protected Iterable<ProguardWildcard> getWildcards() {
    return Collections::emptyIterator;
  }

  static Iterable<ProguardWildcard> getWildcardsOrEmpty(ProguardNameMatcher nameMatcher) {
    return nameMatcher == null ? Collections::emptyIterator : nameMatcher.getWildcards();
  }

  protected ProguardNameMatcher materialize() {
    return this;
  }

  private static class MatchAllNames extends ProguardNameMatcher {
    private final ProguardWildcard wildcard;

    MatchAllNames() {
      this(new Pattern("*"));
    }

    private MatchAllNames(ProguardWildcard wildcard) {
      this.wildcard = wildcard;
    }

    @Override
    public boolean matches(String name) {
      wildcard.setCaptured(name);
      return true;
    }

    @Override
    protected Iterable<ProguardWildcard> getWildcards() {
      return ImmutableList.of(wildcard);
    }

    @Override
    protected MatchAllNames materialize() {
      return new MatchAllNames(wildcard.materialize());
    }

    @Override
    public String toString() {
      return "*";
    }
  }

  private static class MatchNamePattern extends ProguardNameMatcher {

    private final String pattern;
    private final List<ProguardWildcard> wildcards;

    MatchNamePattern(IdentifierPatternWithWildcards identifierPatternWithWildcards) {
      this.pattern = identifierPatternWithWildcards.pattern;
      this.wildcards = identifierPatternWithWildcards.wildcards;
    }

    @Override
    public boolean matches(String name) {
      boolean matched = matchFieldOrMethodNameImpl(pattern, 0, name, 0, wildcards, 0);
      if (!matched) {
        wildcards.forEach(ProguardWildcard::clearCaptured);
      }
      return matched;
    }

    @Override
    protected Iterable<ProguardWildcard> getWildcards() {
      return wildcards;
    }

    @Override
    protected MatchNamePattern materialize() {
      List<ProguardWildcard> materializedWildcards =
          wildcards.stream().map(ProguardWildcard::materialize).collect(Collectors.toList());
      IdentifierPatternWithWildcards identifierPatternWithMaterializedWildcards =
          new IdentifierPatternWithWildcards(pattern, materializedWildcards);
      return new MatchNamePattern(identifierPatternWithMaterializedWildcards);
    }

    @Override
    public String toString() {
      return pattern;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      return o instanceof MatchNamePattern && pattern.equals(((MatchNamePattern) o).pattern);
    }

    @Override
    public int hashCode() {
      return pattern.hashCode();
    }
  }

  private static class MatchSpecificName extends ProguardNameMatcher {

    private final String name;

    MatchSpecificName(String name) {
      this.name = name;
    }

    @Override
    public boolean matches(String name) {
      return this.name.equals(name);
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof MatchSpecificName && name.equals(((MatchSpecificName) o).name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
}
