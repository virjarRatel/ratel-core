// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.position.TextPosition;
import com.android.tools.r8.utils.IdentifierUtils;
import com.android.tools.r8.utils.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Parses a Proguard mapping file and produces mappings from obfuscated class names to the original
 * name and from obfuscated member signatures to the original members the obfuscated member
 * was formed of.
 * <p>
 * The expected format is as follows
 * <p>
 * original-type-name ARROW obfuscated-type-name COLON starts a class mapping
 * description and maps original to obfuscated.
 * <p>
 * followed by one or more of
 * <p>
 * signature ARROW name
 * <p>
 * which maps the member with the given signature to the new name. This mapping is not
 * bidirectional as member names are overloaded by signature. To make it bidirectional, we extend
 * the name with the signature of the original member.
 * <p>
 * Due to inlining, we might have the above prefixed with a range (two numbers separated by :).
 * <p>
 * range COLON signature ARROW name
 * <p>
 * This has the same meaning as the above but also encodes the line number range of the member. This
 * may be followed by multiple inline mappings of the form
 * <p>
 * range COLON signature COLON range ARROW name
 * <p>
 * to identify that signature was inlined from the second range to the new line numbers in the first
 * range. This is then followed by information on the call trace to where the member was inlined.
 * These entries have the form
 * <p>
 * range COLON signature COLON number ARROW name
 * <p>
 * and are currently only stored to be able to reproduce them later.
 */
public class ProguardMapReader implements AutoCloseable {

  private final BufferedReader reader;

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }

  ProguardMapReader(BufferedReader reader) {
    this.reader = reader;
  }

  // Internal parser state
  private int lineNo = 0;
  private int lineOffset = 0;
  private String line;

  private int peekCodePoint() {
    return lineOffset < line.length() ? line.codePointAt(lineOffset) : '\n';
  }

  private char peekChar(int distance) {
    return lineOffset + distance < line.length()
        ? line.charAt(lineOffset + distance)
        : '\n';
  }

  private boolean hasNext() {
    return lineOffset < line.length();
  }

  private int nextCodePoint() {
    try {
      int cp = line.codePointAt(lineOffset);
      lineOffset += Character.charCount(cp);
      return cp;
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ParseException("Unexpected end of line");
    }
  }

  private char nextChar() {
    assert hasNext();
    try {
      return line.charAt(lineOffset++);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ParseException("Unexpected end of line");
    }
  }

  private boolean nextLine() throws IOException {
    if (line.length() != lineOffset) {
      throw new ParseException("Expected end of line");
    }
    return skipLine();
  }

  private boolean isEmptyOrCommentLine(String line) {
    if (line == null) {
      return true;
    }
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (c == '#') {
        return true;
      } else if (!StringUtils.isWhitespace(c)) {
        return false;
      }
    }
    return true;
  }

  private boolean skipLine() throws IOException {
    lineOffset = 0;
    do {
      lineNo++;
      line = reader.readLine();
    } while (hasLine() && isEmptyOrCommentLine(line));
    return hasLine();
  }

  private boolean hasLine() {
    return line != null;
  }

  // Helpers for common pattern
  private void skipWhitespace() {
    while (hasNext() && StringUtils.isWhitespace(peekCodePoint())) {
      nextCodePoint();
    }
  }

  private void expectWhitespace() {
    boolean seen = false;
    while (hasNext() && StringUtils.isWhitespace(peekCodePoint())) {
      seen = seen || !StringUtils.isBOM(peekCodePoint());
      nextCodePoint();
    }
    if (!seen) {
      throw new ParseException("Expected whitespace", true);
    }
  }

  private void expect(char c) {
    if (!hasNext()) {
      throw new ParseException("Expected '" + c + "'", true);
    }
    if (nextChar() != c) {
      throw new ParseException("Expected '" + c + "'");
    }
  }

  void parse(ProguardMap.Builder mapBuilder) throws IOException {
    // Read the first line.
    do {
      line = reader.readLine();
      lineNo++;
    } while (hasLine() && isEmptyOrCommentLine(line));
    parseClassMappings(mapBuilder);
  }

  // Parsing of entries

  private void parseClassMappings(ProguardMap.Builder mapBuilder) throws IOException {
    while (hasLine()) {
      skipWhitespace();
      String before = parseType(false);
      skipWhitespace();
      // Workaround for proguard map files that contain entries for package-info.java files.
      assert IdentifierUtils.isDexIdentifierPart('-');
      if (before.endsWith("package-info")) {
        skipLine();
        continue;
      }
      if (before.endsWith("-") && acceptString(">")) {
        // With - as a legal identifier part the grammar is ambiguous, and we treat a->b as a -> b,
        // and not as a- > b (which would be a parse error).
        before = before.substring(0, before.length() - 1);
      } else {
        skipWhitespace();
        acceptArrow();
      }
      skipWhitespace();
      String after = parseType(false);
      skipWhitespace();
      expect(':');
      ClassNaming.Builder currentClassBuilder =
          mapBuilder.classNamingBuilder(after, before, getPosition());
      skipWhitespace();
      if (nextLine()) {
        parseMemberMappings(currentClassBuilder);
      }
    }
  }

  private void parseMemberMappings(ClassNaming.Builder classNamingBuilder) throws IOException {
    MemberNaming lastAddedNaming = null;
    MemberNaming activeMemberNaming = null;
    Range previousMappedRange = null;
    do {
      Object originalRange = null;
      Range mappedRange = null;

      // Parse the member line '  x:y:name:z:q -> renamedName'.
      if (!StringUtils.isWhitespace(peekCodePoint())) {
        break;
      }
      skipWhitespace();
      Object maybeRangeOrInt = maybeParseRangeOrInt();
      if (maybeRangeOrInt != null) {
        if (!(maybeRangeOrInt instanceof Range)) {
          throw new ParseException(
              String.format("Invalid obfuscated line number range (%s).", maybeRangeOrInt));
        }
        mappedRange = (Range) maybeRangeOrInt;
        skipWhitespace();
        expect(':');
      }
      skipWhitespace();
      Signature signature = parseSignature();
      skipWhitespace();
      if (peekChar(0) == ':') {
        // This is a mapping or inlining definition
        nextChar();
        skipWhitespace();
        originalRange = maybeParseRangeOrInt();
        if (originalRange == null) {
          throw new ParseException("No number follows the colon after the method signature.");
        }
      }
      if (mappedRange == null && originalRange != null) {
        throw new ParseException("No mapping for original range " + originalRange + ".");
      }

      skipWhitespace();
      skipArrow();
      skipWhitespace();
      String renamedName = parseMethodName();

      if (signature instanceof MethodSignature) {
        classNamingBuilder.addMappedRange(
            mappedRange, (MethodSignature) signature, originalRange, renamedName);
      }

      assert mappedRange == null || signature instanceof MethodSignature;

      // If this line refers to a member that should be added to classNamingBuilder (as opposed to
      // an inner inlined callee) and it's different from the the previous activeMemberNaming, then
      // flush (add) the current activeMemberNaming.
      if (activeMemberNaming != null) {
        boolean changedName = !activeMemberNaming.getRenamedName().equals(renamedName);
        boolean changedMappedRange = !Objects.equals(previousMappedRange, mappedRange);
        if (changedName || previousMappedRange == null || changedMappedRange) {
          if (lastAddedNaming == null
              || !lastAddedNaming.getOriginalSignature().equals(activeMemberNaming.signature)) {
            classNamingBuilder.addMemberEntry(activeMemberNaming);
            lastAddedNaming = activeMemberNaming;
          }
        }
      }
      activeMemberNaming = new MemberNaming(signature, renamedName, getPosition());
      previousMappedRange = mappedRange;
    } while (nextLine());

    if (activeMemberNaming != null) {
      boolean notAdded =
          lastAddedNaming == null
              || !lastAddedNaming.getOriginalSignature().equals(activeMemberNaming.signature);
      if (previousMappedRange == null || notAdded) {
        classNamingBuilder.addMemberEntry(activeMemberNaming);
      }
    }
  }

  private TextPosition getPosition() {
    return new TextPosition(0, lineNo, 1);
  }

  // Parsing of components

  private void skipIdentifier(boolean allowInit) {
    boolean isInit = false;
    if (allowInit && peekChar(0) == '<') {
      // swallow the leading < character
      nextChar();
      isInit = true;
    }
    // Progard sometimes outputs a ? as a method name. We have tools (dexsplitter) that depends
    // on being able to map class names back to the original, but does not care if methods are
    // correctly mapped. Using this on proguard output for anything else might not give correct
    // remappings.
    if (!IdentifierUtils.isDexIdentifierStart(peekCodePoint())
        && !IdentifierUtils.isQuestionMark(peekCodePoint())) {
      throw new ParseException("Identifier expected");
    }
    nextCodePoint();
    while (IdentifierUtils.isDexIdentifierPart(peekCodePoint())
        || IdentifierUtils.isQuestionMark(peekCodePoint())) {
      nextCodePoint();
    }
    if (isInit) {
      expect('>');
    }
    if (IdentifierUtils.isDexIdentifierPart(peekCodePoint())) {
      throw new ParseException(
          "End of identifier expected (was 0x" + Integer.toHexString(peekCodePoint()) + ")");
    }
  }

  // Cache for canonicalizing strings.
  // This saves 10% of heap space for large programs.
  final HashMap<String, String> cache = new HashMap<>();

  private String substring(int start) {
    String result = line.substring(start, lineOffset);
    if (cache.containsKey(result)) {
      return cache.get(result);
    }
    cache.put(result, result);
    return result;
  }

  private String parseMethodName() {
    int startPosition = lineOffset;
    skipIdentifier(true);
    while (peekChar(0) == '.') {
      nextChar();
      skipIdentifier(true);
    }
    return substring(startPosition);
  }

  private String parseType(boolean allowArray) {
    int startPosition = lineOffset;
    skipIdentifier(false);
    while (peekChar(0) == '.') {
      nextChar();
      skipIdentifier(false);
    }
    if (allowArray) {
      while (peekChar(0) == '[') {
        nextChar();
        expect(']');
      }
    }
    return substring(startPosition);
  }

  private Signature parseSignature() {
    String type = parseType(true);
    expectWhitespace();
    String name = parseMethodName();
    skipWhitespace();
    Signature signature;
    if (peekChar(0) == '(') {
      nextChar();
      skipWhitespace();
      String[] arguments;
      if (peekChar(0) == ')') {
        arguments = new String[0];
      } else {
        List<String> items = new LinkedList<>();
        items.add(parseType(true));
        skipWhitespace();
        while (peekChar(0) != ')') {
          skipWhitespace();
          expect(',');
          skipWhitespace();
          items.add(parseType(true));
        }
        arguments = items.toArray(StringUtils.EMPTY_ARRAY);
      }
      expect(')');
      signature = new MethodSignature(name, type, arguments);
    } else {
      signature = new FieldSignature(name, type);
    }
    return signature;
  }

  private void skipArrow() {
    expect('-');
    expect('>');
  }

  private boolean acceptArrow() {
    if (peekChar(0) == '-' && peekChar(1) == '>') {
      nextChar();
      nextChar();
      return true;
    }
    return false;
  }

  private boolean acceptString(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (peekChar(i) != s.charAt(i)) {
        return false;
      }
    }
    for (int i = 0; i < s.length(); i++) {
      nextChar();
    }
    return true;
  }

  private boolean isSimpleDigit(char c) {
    return '0' <= c && c <= '9';
  }

  private Object maybeParseRangeOrInt() {
    if (!isSimpleDigit(peekChar(0))) {
      return null;
    }
    int from = parseNumber();
    skipWhitespace();
    if (peekChar(0) != ':') {
      return from;
    }
    expect(':');
    skipWhitespace();
    int to = parseNumber();
    return new Range(from, to);
  }

  private int parseNumber() {
    int result = 0;
    if (!isSimpleDigit(peekChar(0))) {
      throw new ParseException("Number expected");
    }
    do {
      result *= 10;
      result += Character.getNumericValue(nextChar());
    } while (isSimpleDigit(peekChar(0)));
    return result;
  }

  private class ParseException extends RuntimeException {

    private final int lineNo;
    private final int lineOffset;
    private final boolean eol;
    private final String msg;

    ParseException(String msg) {
      this(msg, false);
    }

    ParseException(String msg, boolean eol) {
      lineNo = ProguardMapReader.this.lineNo;
      lineOffset = ProguardMapReader.this.lineOffset;
      this.eol = eol;
      this.msg = msg;
    }

    @Override
    public String toString() {
      if (eol) {
        return "Parse error [" + lineNo + ":eol] " + msg;
      } else {
        return "Parse error [" + lineNo + ":" + lineOffset + "] " + msg;
      }
    }
  }
}
