// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionsParsing {

  /**
   * Try parsing the switch {@code name} and zero or more non-switch args after it. Also supports
   * the <name>=arg syntax.
   */
  public static List<String> tryParseMulti(ParseContext context, String name) {
    List<String> result = null;
    String head = context.head();
    if (head.equals(name)) {
      context.next();
      result = new ArrayList<>();
      while (context.head() != null && !context.head().startsWith("-")) {
        result.add(context.head());
        context.next();
      }
    } else if (head.startsWith(name) && head.charAt(name.length()) == '=') {
      result = Collections.singletonList(head.substring(name.length() + 1));
      context.next();
    }
    return result;
  }

  /**
   * Try parsing the switch {@code name} and one arg after it. Also supports the <name>=arg syntax.
   */
  public static String tryParseSingle(ParseContext context, String name, String shortName) {
    String head = context.head();
    if (head.equals(name) || head.equals(shortName)) {
      String next = context.next();
      if (next == null) {
        throw new RuntimeException(String.format("Missing argument for '%s'.", head));
      }
      context.next();
      return next;
    }

    if (head.startsWith(name) && head.charAt(name.length()) == '=') {
      context.next();
      return head.substring(name.length() + 1);
    }

    return null;
  }

  /**
   * Try parsing the switch {@code name} as a boolean switch or its negation, with a 'no' between
   * the dashes and the word.
   */
  public static Boolean tryParseBoolean(ParseContext context, String name) {
    if (context.head().equals(name)) {
      context.next();
      return true;
    }
    assert name.startsWith("--");
    if (context.head().equals("--no" + name.substring(2))) {
      context.next();
      return false;
    }
    return null;
  }

  public static class ParseContext {
    private final String[] args;
    private int nextIndex = 0;

    public ParseContext(String[] args) {
      this.args = args;
    }

    public String head() {
      return nextIndex < args.length ? args[nextIndex] : null;
    }

    public String next() {
      if (nextIndex < args.length) {
        ++nextIndex;
        return head();
      } else {
        throw new RuntimeException("Iterating over the end of argument list.");
      }
    }
  }
}
