// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;

// While mapping fields representing lambda captures we rearrange fields to make sure
// lambdas having different order of the fields still can be merged. We also store
// all captures of reference types in fields of java.lang.Objects class.
//
// This allows us to use same lambda groups class for these two different lambdas:
//
//      Lambda Group Class         Lambda$1               Lambda$2
//          Object $c0        int      foo -> $c1     char foo -> $c2
//          int    $c1        String[] bar -> $c0     int  bar -> $c1
//          char   $c2        char     baz -> $c2     List baz -> $c0
//
// Capture signature is represented by a string with sorted shorties of field
// types, such as "CIL" for both Lambda$1 and Lambda$2 in the above example.
//
public final class CaptureSignature {
  private static final IntList EMPTY_LIST = IntLists.EMPTY_LIST;
  private static final IntList SINGLE_LIST = IntLists.singleton(0);

  private CaptureSignature() {
  }

  // Returns an array representing mapping of fields of the normalized capture
  // into fields of original capture such that:
  //
  //   mapping = getReverseCaptureMapping(...)
  //   <original-capture-index> = mapping[<normalized-capture-index>]
  public static IntList getReverseCaptureMapping(DexType[] types) {
    if (types.length == 0) {
      return EMPTY_LIST;
    }

    if (types.length == 1) {
      return SINGLE_LIST;
    }

    IntList result = new IntArrayList(types.length);
    for (int i = 0; i < types.length; i++) {
      result.add(i);
    }
    // Sort the indices by shorties (sorting is stable).
    result.sort(Comparator.comparingInt(i -> types[i].toShorty()));
    assert verifyMapping(result);
    return result;
  }

  // Given a capture signature and an index returns the type of the field.
  public static DexType fieldType(DexItemFactory factory, String capture, int index) {
    switch (capture.charAt(index)) {
      case 'L':
        return factory.objectType;
      case 'Z':
        return factory.booleanType;
      case 'B':
        return factory.byteType;
      case 'S':
        return factory.shortType;
      case 'C':
        return factory.charType;
      case 'I':
        return factory.intType;
      case 'F':
        return factory.floatType;
      case 'J':
        return factory.longType;
      case 'D':
        return factory.doubleType;
      default:
        throw new Unreachable("Invalid capture character: " + capture.charAt(index));
    }
  }

  private static String getCaptureSignature(int size, IntFunction<DexType> type) {
    if (size == 0) {
      return "";
    }
    if (size == 1) {
      return Character.toString(type.apply(0).toShorty());
    }

    char[] chars = new char[size];
    for (int i = 0; i < size; i++) {
      chars[i] = type.apply(i).toShorty();
    }
    Arrays.sort(chars);
    return new String(chars);
  }

  // Compute capture signature based on lambda class capture fields.
  public static String getCaptureSignature(List<DexEncodedField> fields) {
    return getCaptureSignature(fields.size(), i -> fields.get(i).field.type);
  }

  // Compute capture signature based on type list.
  public static String getCaptureSignature(DexTypeList types) {
    return getCaptureSignature(types.values.length, i -> types.values[i]);
  }

  // Having a list of fields of lambda captured values, maps one of them into
  // an index of the appropriate field in normalized capture signature.
  public static int mapFieldIntoCaptureIndex(
      String capture, List<DexEncodedField> lambdaFields, DexField fieldToMap) {
    char fieldKind = fieldToMap.type.toShorty();
    int numberOfSameCaptureKind = 0;
    int result = -1;

    for (DexEncodedField encodedField : lambdaFields) {
      if (encodedField.field == fieldToMap) {
        result = numberOfSameCaptureKind;
        break;
      }
      if (encodedField.field.type.toShorty() == fieldKind) {
        numberOfSameCaptureKind++;
      }
    }

    assert result >= 0 : "Field was not found in the lambda class.";

    for (int index = 0; index < capture.length(); index++) {
      if (capture.charAt(index) == fieldKind) {
        if (result == 0) {
          return index;
        }
        result--;
      }
    }

    throw new Unreachable("Were not able to map lambda field into capture index");
  }

  private static boolean verifyMapping(IntList mapping) {
    BitSet bits = new BitSet();
    for (Integer i : mapping) {
      assert i >= 0 && i < mapping.size();
      assert !bits.get(i);
      bits.set(i);
    }
    return true;
  }
}
