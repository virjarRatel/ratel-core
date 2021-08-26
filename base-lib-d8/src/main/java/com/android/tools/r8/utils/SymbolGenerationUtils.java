// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.StringUtils.EMPTY_CHAR_ARRAY;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public class SymbolGenerationUtils {

  public enum MixedCasing {
    USE_MIXED_CASE,
    DONT_USE_MIXED_CASE
  }

  public static Set<String> PRIMITIVE_TYPE_NAMES =
      Sets.newHashSet("boolean", "byte", "char", "double", "float", "int", "long", "short", "void");

  // These letters are used not creating fresh names to output and not for parsing dex/class files.
  private static final char[] IDENTIFIER_CHARACTERS =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final int NUMBER_OF_CHARACTERS = IDENTIFIER_CHARACTERS.length;
  private static final int NUMBER_OF_CHARACTERS_MINUS_CAPITAL_LETTERS = NUMBER_OF_CHARACTERS - 26;
  private static final int NON_ALLOWED_FIRST_CHARACTERS = 10;

  public static String numberToIdentifier(int nameCount, MixedCasing mixedCasing) {
    return numberToIdentifier(nameCount, mixedCasing, EMPTY_CHAR_ARRAY, false);
  }

  public static String numberToIdentifier(int nameCount, MixedCasing mixedCasing, char[] prefix) {
    return numberToIdentifier(nameCount, mixedCasing, prefix, false);
  }

  public static String numberToIdentifier(
      int nameCount, MixedCasing mixedCasing, char[] prefix, boolean addSemicolon) {
    int size = 1;
    int number = nameCount;
    int maximumNumberOfCharacters =
        mixedCasing == MixedCasing.USE_MIXED_CASE
            ? NUMBER_OF_CHARACTERS
            : NUMBER_OF_CHARACTERS_MINUS_CAPITAL_LETTERS;
    int firstNumberOfCharacters = maximumNumberOfCharacters - NON_ALLOWED_FIRST_CHARACTERS;
    int availableCharacters = firstNumberOfCharacters;
    // We first do an initial computation to find the size of the resulting string to allocate an
    // array that will fit in length.
    while (number > availableCharacters) {
      number = (number - 1) / availableCharacters;
      availableCharacters = maximumNumberOfCharacters;
      size++;
    }
    size += addSemicolon ? 1 : 0;
    char characters[] = Arrays.copyOfRange(prefix, 0, prefix.length + size);
    number = nameCount;

    int i = prefix.length;
    availableCharacters = firstNumberOfCharacters;
    int firstLetterPadding = NON_ALLOWED_FIRST_CHARACTERS;
    while (number > availableCharacters) {
      characters[i++] =
          IDENTIFIER_CHARACTERS[(number - 1) % availableCharacters + firstLetterPadding];
      number = (number - 1) / availableCharacters;
      availableCharacters = maximumNumberOfCharacters;
      firstLetterPadding = 0;
    }
    characters[i++] = IDENTIFIER_CHARACTERS[number - 1 + firstLetterPadding];
    if (addSemicolon) {
      characters[i++] = ';';
    }
    assert i == characters.length;
    assert !Character.isDigit(characters[prefix.length]);

    return new String(characters);
  }
}
