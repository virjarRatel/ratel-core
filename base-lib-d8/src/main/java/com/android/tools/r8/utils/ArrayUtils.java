// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtils {

  /**
   * Copies the input array and then applies specified sparse changes.
   *
   * @param clazz target type's Class to cast
   * @param original an array of original elements
   * @param changedElements sparse changes to apply
   * @param <T> target type
   * @return a copy of original arrays while sparse changes are applied
   */
  public static <T> T[] copyWithSparseChanges(
      Class<T[]> clazz, T[] original, Map<Integer, T> changedElements) {
    T[] results = clazz.cast(Array.newInstance(clazz.getComponentType(), original.length));
    int pos = 0;
    for (Map.Entry<Integer, T> entry : changedElements.entrySet()) {
      int i = entry.getKey();
      System.arraycopy(original, pos, results, pos, i - pos);
      results[i] = entry.getValue();
      pos = i + 1;
    }
    if (pos < original.length) {
      System.arraycopy(original, pos, results, pos, original.length - pos);
    }
    return results;
  }

  /**
   * Filters the input array based on the given predicate.
   *
   * @param clazz target type's Class to cast
   * @param original an array of original elements
   * @param filter a predicate that tells us what to keep
   * @param <T> target type
   * @return a partial copy of the original array
   */
  public static <T> T[] filter(Class<T[]> clazz, T[] original, Predicate<T> filter) {
    ArrayList<T> filtered = null;
    for (int i = 0; i < original.length; i++) {
      T elt = original[i];
      if (filter.test(elt)) {
        if (filtered != null) {
          filtered.add(elt);
        }
      } else {
        if (filtered == null) {
          filtered = new ArrayList<>(original.length);
          for (int j = 0; j < i; j++) {
            filtered.add(original[j]);
          }
        }
      }
    }
    if (filtered == null) {
      return original;
    }
    return filtered.toArray(
        clazz.cast(Array.newInstance(clazz.getComponentType(), filtered.size())));
  }

  /**
   * Rewrites the input array based on the given function.
   *
   * @param clazz target type's Class to cast
   * @param original an array of original elements
   * @param mapper a mapper that rewrites an original element to a new one, maybe `null`
   * @param <T> target type
   * @return an array with written elements
   */
  public static <T> T[] map(Class<T[]> clazz, T[] original, Function<T, T> mapper) {
    ArrayList<T> results = null;
    for (int i = 0; i < original.length; i++) {
      T oldOne = original[i];
      T newOne = mapper.apply(oldOne);
      if (newOne == oldOne) {
        if (results != null) {
          results.add(oldOne);
        }
      } else {
        if (results == null) {
          results = new ArrayList<>(original.length);
          for (int j = 0; j < i; j++) {
            results.add(original[j]);
          }
        }
        if (newOne != null) {
          results.add(newOne);
        }
      }
    }
    if (results == null) {
      return original;
    }
    return results.toArray(
        clazz.cast(Array.newInstance(clazz.getComponentType(), results.size())));
  }

  public static <T> boolean contains(T[] elements, T elementToLookFor) {
    for (Object element : elements) {
      if (element.equals(elementToLookFor)) {
        return true;
      }
    }
    return false;
  }
}
