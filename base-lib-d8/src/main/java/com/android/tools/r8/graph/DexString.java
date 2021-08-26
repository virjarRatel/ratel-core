// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.IdentifierUtils;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.ThrowingCharIterator;
import java.io.UTFDataFormatException;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class DexString extends IndexedDexItem implements PresortedComparable<DexString> {

  public static final DexString[] EMPTY_ARRAY = {};
  private static final int ARRAY_CHARACTER = '[';

  public final int size;  // size of this string, in UTF-16
  public final byte[] content;

  DexString(int size, byte[] content) {
    this.size = size;
    this.content = content;
  }

  DexString(String string) {
    this.size = string.length();
    this.content = encodeToMutf8(string);
  }

  public ThrowingCharIterator<UTFDataFormatException> iterator() {
    return new ThrowingCharIterator<UTFDataFormatException>() {

      private int i = 0;

      @Override
      public char nextChar() throws UTFDataFormatException {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        char a = (char) (content[i++] & 0xff);
        assert a != 0;
        if (a < '\u0080') {
          return a;
        }
        if ((a & 0xe0) == 0xc0) {
          int b = content[i++] & 0xff;
          if ((b & 0xC0) == 0x80) {
            return (char) (((a & 0x1F) << 6) | (b & 0x3F));
          }
          throw new UTFDataFormatException("bad second byte");
        }
        if ((a & 0xf0) == 0xe0) {
          int b = content[i++] & 0xff;
          int c = content[i++] & 0xff;
          if ((b & 0xC0) == 0x80 && (c & 0xC0) == 0x80) {
            return (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
          }
          throw new UTFDataFormatException("bad second or third byte");
        }
        throw new UTFDataFormatException("bad byte");
      }

      @Override
      public boolean hasNext() {
        return i < content.length && (content[i] & 0xff) != 0;
      }
    };
  }

  @Override
  public int computeHashCode() {
    return size * 7 + Arrays.hashCode(content);
  }

  @Override
  public boolean computeEquals(Object other) {
    if (other instanceof DexString) {
      DexString o = (DexString) other;
      return size == o.size && Arrays.equals(content, o.content);
    }
    return false;
  }

  @Override
  public String toString() {
    try {
      return decode();
    } catch (UTFDataFormatException e) {
      throw new RuntimeException("Bad format", e);
    }
  }

  public String toASCIIString() {
    try {
      return StringUtils.toASCIIString(decode());
    } catch (UTFDataFormatException e) {
      throw new RuntimeException("Bad format", e);
    }
  }

  public int numberOfLeadingSquareBrackets() {
    int result = 0;
    while (content.length > result && content[result] == ((byte) '[')) {
      result++;
    }
    return result;
  }

  private String decode() throws UTFDataFormatException {
    char[] out = new char[size];
    int decodedLength = decodePrefix(out);
    return new String(out, 0, decodedLength);
  }

  // Inspired from /dex/src/main/java/com/android/dex/Mutf8.java
  public int decodePrefix(char[] out) throws UTFDataFormatException {
    int s = 0;
    int p = 0;
    int prefixLength = out.length;
    while (true) {
      char a = (char) (content[p++] & 0xff);
      if (a == 0) {
        return s;
      }
      out[s] = a;
      if (a < '\u0080') {
        if (++s == prefixLength) {
          return s;
        }
      } else if ((a & 0xe0) == 0xc0) {
        int b = content[p++] & 0xff;
        if ((b & 0xC0) != 0x80) {
          throw new UTFDataFormatException("bad second byte");
        }
        out[s] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
        if (++s == prefixLength) {
          return s;
        }
      } else if ((a & 0xf0) == 0xe0) {
        int b = content[p++] & 0xff;
        int c = content[p++] & 0xff;
        if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) {
          throw new UTFDataFormatException("bad second or third byte");
        }
        out[s] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
        if (++s == prefixLength) {
          return s;
        }
      } else {
        throw new UTFDataFormatException("bad byte");
      }
    }
  }

  public int decodedHashCode() throws UTFDataFormatException {
    if (size == 0) {
      assert decode().hashCode() == 0;
      return 0;
    }
    int h = 0;
    int p = 0;
    while (true) {
      char a = (char) (content[p++] & 0xff);
      if (a == 0) {
        break;
      }
      if (a < '\u0080') {
        h = 31 * h + a;
      } else if ((a & 0xe0) == 0xc0) {
        int b = content[p++] & 0xff;
        if ((b & 0xC0) != 0x80) {
          throw new UTFDataFormatException("bad second byte");
        }
        h = 31 * h + (char) (((a & 0x1F) << 6) | (b & 0x3F));
      } else if ((a & 0xf0) == 0xe0) {
        int b = content[p++] & 0xff;
        int c = content[p++] & 0xff;
        if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) {
          throw new UTFDataFormatException("bad second or third byte");
        }
        h = 31 * h + (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
      } else {
        throw new UTFDataFormatException("bad byte");
      }
    }

    assert h == decode().hashCode();
    return h;
  }

  // Inspired from /dex/src/main/java/com/android/dex/Mutf8.java
  private static int countBytes(String string) {
    // We need an extra byte for the terminating '0'.
    int result = 1;
    for (int i = 0; i < string.length(); ++i) {
      result += countBytes(string.charAt(i));
      assert result > 0;
    }
    return result;
  }

  public static int countBytes(char ch) {
    if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
      return 1;
    }
    if (ch <= 2047) {
      return 2;
    }
    return 3;
  }

  // Inspired from /dex/src/main/java/com/android/dex/Mutf8.java
  public static byte[] encodeToMutf8(String string) {
    byte[] result = new byte[countBytes(string)];
    int offset = 0;
    for (int i = 0; i < string.length(); i++) {
      offset = encodeToMutf8(string.charAt(i), result, offset);
    }
    result[offset] = 0;
    return result;
  }

  public static int encodeToMutf8(char ch, byte[] array, int offset) {
    if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
      array[offset++] = (byte) ch;
    } else if (ch <= 2047) {
      array[offset++] = (byte) (0xc0 | (0x1f & (ch >> 6)));
      array[offset++] = (byte) (0x80 | (0x3f & ch));
    } else {
      array[offset++] = (byte) (0xe0 | (0x0f & (ch >> 12)));
      array[offset++] = (byte) (0x80 | (0x3f & (ch >> 6)));
      array[offset++] = (byte) (0x80 | (0x3f & ch));
    }
    return offset;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    indexedItems.addString(this);
  }

  @Override
  public int getOffset(ObjectToOffsetMapping mapping) {
    return mapping.getOffsetFor(this);
  }

  @Override
  public int compareTo(DexString other) {
    return sortedCompareTo(other.getSortedIndex());
  }

  @Override
  public int slowCompareTo(DexString other) {
    // Compare the bytes, as comparing UTF-8 encoded strings as strings of unsigned bytes gives
    // the same result as comparing the corresponding Unicode strings lexicographically by
    // codepoint. The only complication is the MUTF-8 encoding have the two byte encoding c0 80 of
    // the null character (U+0000) to allow embedded null characters.
    // Supplementary characters (unicode code points above U+FFFF) are always represented as
    // surrogate pairs and are compared using UTF-16 code units as per Java string semantics.
    int index = 0;
    while (true) {
      char b1 = (char) (content[index] & 0xff);
      char b2 = (char) (other.content[index] & 0xff);
      int diff = b1 - b2;
      if (diff != 0) {
        // Check if either string ends here.
        if (b1 == 0 || b2 == 0) {
          return diff;
        }
        // If either of the strings have the null character starting here, the null character
        // sort lowest.
        if ((b1 == 0xc0 && (content[index + 1] & 0xff) == 0x80) ||
            (b2 == 0xc0 && (other.content[index + 1] & 0xff) == 0x80)) {
          return b1 == 0xc0 && (content[index + 1] & 0xff) == 0x80 ? -1 : 1;
        }
        return diff;
      } else if (b1 == 0) {
        // Reached the end in both strings.
        return 0;
      }
      index++;
    }
  }

  @Override
  public int slowCompareTo(DexString other, NamingLens lens) {
    // The naming lens cannot affect strings.
    return slowCompareTo(other);
  }

  @Override
  public int layeredCompareTo(DexString other, NamingLens lens) {
    // Strings have no subparts that are already sorted.
    return slowCompareTo(other);
  }

  private static boolean isValidClassDescriptor(String string) {
    if (string.length() < 3
        || string.charAt(0) != 'L'
        || string.charAt(string.length() - 1) != ';') {
      return false;
    }
    if (string.charAt(1) == '/' || string.charAt(string.length() - 2) == '/') {
      return false;
    }
    int cp;
    for (int i = 1; i < string.length() - 1; i += Character.charCount(cp)) {
      cp = string.codePointAt(i);
      if (cp != '/' && !IdentifierUtils.isRelaxedDexIdentifierPart(cp)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidMethodName(String string) {
    if (string.isEmpty()) {
      return false;
    }
    // According to https://source.android.com/devices/tech/dalvik/dex-format#membername
    // '<' SimpleName '>' should be valid. However, the art verifier only allows <init>
    // and <clinit> which is reasonable.
    if ((string.charAt(0) == '<') &&
        (string.equals(Constants.INSTANCE_INITIALIZER_NAME) ||
            string.equals(Constants.CLASS_INITIALIZER_NAME))) {
      return true;
    }
    int cp;
    for (int i = 0; i < string.length(); i += Character.charCount(cp)) {
      cp = string.codePointAt(i);
      if (!IdentifierUtils.isRelaxedDexIdentifierPart(cp)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidFieldName(String string) {
    if (string.isEmpty()) {
      return false;
    }
    int start = 0;
    int end = string.length();
    if (string.charAt(0) == '<') {
      if (string.charAt(end - 1) == '>') {
        start = 1;
        --end;
      } else {
        return false;
      }
    }
    int cp;
    for (int i = start; i < end; i += Character.charCount(cp)) {
      cp = string.codePointAt(i);
      if (!IdentifierUtils.isRelaxedDexIdentifierPart(cp)) {
        return false;
      }
    }
    return true;
  }

  public boolean isValidMethodName() {
    try {
      return isValidMethodName(decode());
    } catch (UTFDataFormatException e) {
      return false;
    }
  }

  public boolean isValidFieldName() {
    try {
      return isValidFieldName(decode());
    } catch (UTFDataFormatException e) {
      return false;
    }
  }

  public boolean isValidClassDescriptor() {
    try {
      return isValidClassDescriptor(decode());
    } catch (UTFDataFormatException e) {
      return false;
    }
  }

  public static boolean isValidSimpleName(int apiLevel, String string) {
    // space characters are not allowed prior to Android R
    if (apiLevel < AndroidApiLevel.R.getLevel()) {
      int cp;
      for (int i = 0; i < string.length(); ) {
        cp = string.codePointAt(i);
        if (IdentifierUtils.isUnicodeSpace(cp)) {
          return false;
        }
        i += Character.charCount(cp);
      }
    }
    return true;
  }

  public boolean isValidSimpleName(int apiLevel) {
    // space characters are not allowed prior to Android R
    if (apiLevel < AndroidApiLevel.R.getLevel()) {
      try {
        return isValidSimpleName(apiLevel, decode());
      } catch (UTFDataFormatException e) {
        return false;
      }
    }
    return true;
  }

  public String dump() {
    StringBuilder builder = new StringBuilder();
    builder.append(toString());
    builder.append(" [");
    for (int i = 0; i < content.length; i++) {
      if (i > 0) {
        builder.append(" ");
      }
      builder.append(Integer.toHexString(content[i] & 0xff));
    }
    builder.append("]");
    return builder.toString();
  }

  public boolean startsWith(DexString prefix) {
    if (content.length < prefix.content.length) {
      return false;
    }
    for (int i = 0; i < prefix.content.length - 1; i++) {
      if (content[i] != prefix.content[i]) {
        return false;
      }
    }
    return true;
  }

  public boolean endsWith(DexString suffix) {
    if (content.length < suffix.content.length) {
      return false;
    }
    for (int i = content.length - suffix.content.length, j = 0; i < content.length; i++, j++) {
      if (content[i] != suffix.content[j]) {
        return false;
      }
    }
    return true;
  }

  public DexString withNewPrefix(
      DexString prefix, DexString rewrittenPrefix, DexItemFactory factory) {
    // Copy bytes over to avoid decoding/encoding cost.
    // Each string ends with a 0 terminating byte, hence the +/- 1.
    // Maintain the [[ at the beginning for array dimensions.
    int arrayDim = getArrayDim();
    int newSize = rewrittenPrefix.size + this.size - prefix.size;
    byte[] newContent =
        new byte[rewrittenPrefix.content.length + this.content.length - prefix.content.length];
    // Write array dim.
    for (int i = 0; i < arrayDim; i++) {
      newContent[i] = ARRAY_CHARACTER;
    }
    // Write new prefix.
    System.arraycopy(
        rewrittenPrefix.content, 0, newContent, arrayDim, rewrittenPrefix.content.length - 1);
    // Write existing name - old prefix.
    System.arraycopy(
        this.content,
        prefix.content.length - 1,
        newContent,
        rewrittenPrefix.content.length - 1,
        this.content.length - prefix.content.length + 1);
    return factory.createString(newSize, newContent);
  }

  public DexString withoutArray(DexItemFactory factory) {
    int arrayDim = getArrayDim();
    if (arrayDim == 0) {
      return this;
    }
    byte[] newContent = new byte[content.length - arrayDim];
    System.arraycopy(this.content, arrayDim, newContent, 0, newContent.length);
    return factory.createString(this.size - arrayDim, newContent);
  }

  private int getArrayDim() {
    int arrayDim = 0;
    while (content[arrayDim] == ARRAY_CHARACTER) {
      arrayDim++;
    }
    return arrayDim;
  }
}
