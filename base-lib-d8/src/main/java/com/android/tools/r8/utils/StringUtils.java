// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.Unreachable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class StringUtils {
  public static char[] EMPTY_CHAR_ARRAY = {};
  public static final String[] EMPTY_ARRAY = {};
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final char BOM = '\uFEFF';

  public enum BraceType {
    PARENS,
    SQUARE,
    TUBORG,
    NONE;

    public String left() {
      switch (this) {
        case PARENS: return "(";
        case SQUARE: return "[";
        case TUBORG: return "{";
        case NONE: return "";
        default: throw new Unreachable("Invalid brace type: " + this);
      }
    }

    public String right() {
      switch (this) {
        case PARENS: return ")";
        case SQUARE: return "]";
        case TUBORG: return "}";
        case NONE: return "";
        default: throw new Unreachable("Invalid brace type: " + this);
      }
    }
  }

  public static String toASCIIString(String s) {
    StringBuilder builder = new StringBuilder();
    for (char ch : s.toCharArray()) {
      if (0x1f < ch && ch < 0x7f) {  // 0 - 0x1f and 0x7f are control characters.
        builder.append(ch);
      } else {
        builder.append("\\u").append(StringUtils.hexString(ch, 4, false));
      }
    }
    return builder.toString();
  }

  public static boolean appendNonEmpty(
      StringBuilder builder, String pre, Object item, String post) {
    if (item == null) {
      return false;
    }
    String text = item.toString();
    if (!text.isEmpty()) {
      if (pre != null) {
        builder.append(pre);
      }
      builder.append(text);
      if (post != null) {
        builder.append(post);
      }
      return true;
    }
    return false;
  }

  public static StringBuilder appendIndent(StringBuilder builder, String subject, int indent) {
    for (int i = 0; i < indent; i++) {
      builder.append(" ");
    }
    builder.append(subject);
    return builder;
  }

  public static StringBuilder appendLeftPadded(StringBuilder builder, String subject, int width) {
    for (int i = subject.length(); i < width; i++) {
      builder.append(" ");
    }
    builder.append(subject);
    return builder;
  }

  public static StringBuilder appendRightPadded(StringBuilder builder, String subject, int width) {
    builder.append(subject);
    for (int i = subject.length(); i < width; i++) {
      builder.append(" ");
    }
    return builder;
  }

  public static <T> StringBuilder append(StringBuilder builder, Collection<T> collection) {
    return append(builder, collection, ", ", BraceType.PARENS);
  }

  public static <T> StringBuilder append(StringBuilder builder, Collection<T> collection,
      String seperator, BraceType brace) {
    builder.append(brace.left());
    boolean first = true;
    for (T element : collection) {
      if (first) {
        first = false;
      } else {
        builder.append(seperator);
      }
      builder.append(element);
    }
    builder.append(brace.right());
    return builder;
  }

  public static <T> String join(Collection<T> collection, String separator) {
    return join(collection, separator, BraceType.NONE);
  }

  public static String join(String separator, String... strings) {
    return join(Arrays.asList(strings), separator, BraceType.NONE);
  }

  public static <T> String join(Collection<T> collection, String separator, BraceType brace) {
    return join(collection, separator, brace, Object::toString);
  }

  public static <T> String join(Collection<T> collection, String separator, BraceType brace,
      Function<T, String> fn) {
    StringBuilder builder = new StringBuilder();
    append(builder, ListUtils.map(collection, fn), separator, brace);
    return builder.toString();
  }

  public static String lines(List<String> lines) {
    StringBuilder builder = new StringBuilder();
    for (String line : lines) {
      builder.append(line).append(LINE_SEPARATOR);
    }
    return builder.toString();
  }

  public static String lines(String... lines) {
    return lines(Arrays.asList(lines));
  }

  public static String withNativeLineSeparator(String s) {
    s = s.replace("\r\n", "\n");
    if (LINE_SEPARATOR.equals("\r\n")) {
      return s.replace("\n", "\r\n");
    } else {
      assert LINE_SEPARATOR.equals("\n");
      return s;
    }
  }

  public static String joinLines(String... lines) {
    return join(LINE_SEPARATOR, lines);
  }

  public static <T> String joinLines(Collection<T> collection) {
    return join(collection, LINE_SEPARATOR, BraceType.NONE);
  }

  public static List<String> splitLines(String content) {
    int length = content.length();
    List<String> lines = new ArrayList<>();
    int start = 0;
    for (int i = 0; i < length; i++) {
      char c = content.charAt(i);
      int end = i;
      if (c == '\r' && i + 1 < length && content.charAt(i + 1) == '\n') {
        ++i;
      } else if (c != '\n') {
        continue;
      }
      lines.add(content.substring(start, end));
      start = i + 1;
    }
    if (start < length) {
      String line = content.substring(start);
      if (!line.isEmpty()) {
        lines.add(line);
      }
    }
    return lines;
  }

  public static String zeroPrefix(int i, int width) {
    return zeroPrefixString(Integer.toString(i), width);
  }

  private static String zeroPrefixString(String s, int width) {
    String prefix = "0000000000000000";
    assert(width <= prefix.length());
    int prefixLength = width - s.length();
    if (prefixLength > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append(prefix, 0, prefixLength);
      builder.append(s);
      return builder.toString();
    } else {
      return s;
    }
  }

  public static String hexString(int value, int width) {
    return hexString(value, width, true);
  }

  public static String hexString(int value, int width, boolean zeroXPrefix) {
    assert(0 <= width && width <= 8);
    String prefix = zeroXPrefix ? "0x" : "";
    String hex = Integer.toHexString(value);
    if (value >= 0) {
      return prefix + zeroPrefixString(hex, width);
    } else {
      // Negative ints are always formatted as 8 characters.
      assert(hex.length() == 8);
      return prefix + hex;
    }
  }

  public static String hexString(long value, int width) {
    return hexString(value, width, true);
  }

  public static String hexString(long value, int width, boolean zeroXPrefix) {
    assert(0 <= width && width <= 16);
    String prefix = zeroXPrefix ? "0x" : "";
    String hex = Long.toHexString(value);
    if (value >= 0) {
      return prefix + zeroPrefixString(hex, width);
    } else {
      // Negative longs are always formatted as 16 characters.
      assert(hex.length() == 16);
      return prefix + hex;
    }
  }

  public static String computeMD5Hash(String name) {
    byte[] digest = null;
    try {
      MessageDigest m = MessageDigest.getInstance("MD5");
      m.reset();
      m.update(name.getBytes());
      digest = m.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return Arrays.toString(digest);
  }


  public static String times(String string, int count) {
    StringBuilder builder = new StringBuilder();
    while (--count >= 0) {
      builder.append(string);
    }
    return builder.toString();
  }

  public static boolean isBOM(int codePoint) {
    return codePoint == BOM;
  }

  public static boolean isWhitespace(int codePoint) {
    return Character.isWhitespace(codePoint) || isBOM(codePoint);
  }

  public static String stripLeadingBOM(String s) {
    if (s.length() > 0 && s.charAt(0) == StringUtils.BOM) {
      return s.substring(1);
    } else {
      return s;
    }
  }

  public static String trim(String s) {
    int beginIndex = 0;
    int endIndex = s.length();
    while (beginIndex < endIndex && isWhitespace(s.charAt(beginIndex))) {
      beginIndex++;
    }
    while (endIndex - 1 > beginIndex && isWhitespace(s.charAt(endIndex - 1))) {
      endIndex--;
    }
    if (beginIndex > 0 || endIndex < s.length()) {
      return s.substring(beginIndex, endIndex);
    } else {
      return s;
    }
  }

  /** Returns true if {@param s} only contains the characters [0-9]. */
  public static boolean onlyContainsDigits(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }

  public static char lastChar(String s) {
    return charFromEnd(s, 0);
  }

  public static char charFromEnd(String s, int charsFromEnd) {
    assert s.length() > charsFromEnd;
    return s.charAt(s.length() - (charsFromEnd + 1));
  }
}
