// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.utils.ArrayUtils;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * List of parameter annotations.
 *
 * <p>Due to a javac bug that went unfixed for multiple Java versions, the JVM specification does
 * not require that the number of entries in the ParameterAnnotations attribute of a method matches
 * the number of parameters in the method prototype; the number of ParameterAnnotations entries may
 * be less than the number of prototype parameters for methods on inner classes.
 *
 * <p>There are two ways of accessing the parameter annotations:
 *
 * <ul>
 *   <li>Using {@link ParameterAnnotationsList#forEachAnnotation(Consumer)}
 *   <li>Using {@link ParameterAnnotationsList#size()}, {@link
 *       ParameterAnnotationsList#isMissing(int)} and {@link ParameterAnnotationsList#get(int)}
 * </ul>
 *
 * <p>The {@link ParameterAnnotationsList#forEachAnnotation(Consumer)} method visits all the {@link
 * DexAnnotation}s specified in the ParameterAnnotations attribute. In contrast, the {@link
 * ParameterAnnotationsList#size()} and {@link ParameterAnnotationsList#get(int)} methods may be
 * used to access the annotations on individual parameters; these methods automatically shift
 * parameter annotations up to mitigate the javac bug. The {@link
 * ParameterAnnotationsList#isMissing(int)} accessor is used to determine whether a given parameter
 * is missing in the ParameterAnnotations attribute.
 */
public class ParameterAnnotationsList extends DexItem {

  private static final ParameterAnnotationsList EMPTY_PARAMETER_ANNOTATIONS_LIST =
      new ParameterAnnotationsList();

  private final DexAnnotationSet[] values;
  private final int missingParameterAnnotations;

  public static ParameterAnnotationsList empty() {
    return EMPTY_PARAMETER_ANNOTATIONS_LIST;
  }

  private ParameterAnnotationsList() {
    this.values = DexAnnotationSet.EMPTY_ARRAY;
    this.missingParameterAnnotations = 0;
  }

  public ParameterAnnotationsList(DexAnnotationSet[] values) {
    this(values, 0);
  }

  public ParameterAnnotationsList(DexAnnotationSet[] values, int missingParameterAnnotations) {
    assert values != null && values.length > 0;
    this.values = values;
    this.missingParameterAnnotations = missingParameterAnnotations;
  }

  public int getAnnotableParameterCount() {
    return size();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof ParameterAnnotationsList) {
      return Arrays.equals(values, ((ParameterAnnotationsList) other).values);
    }
    return false;
  }

  @Override
  public void collectIndexedItems(
      IndexedItemCollection indexedItems, DexMethod method, int instructionOffset) {
    collectAll(indexedItems, values);
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    // Collect values first so that the annotation sets have sorted themselves before adding this.
    collectAll(mixedItems, values);
    mixedItems.add(this);
  }

  public boolean isEmpty() {
    return values.length == 0;
  }

  /** Iterate over the {@link DexAnnotation}s of all parameters. */
  public void forEachAnnotation(Consumer<DexAnnotation> consumer) {
    for (DexAnnotationSet parameterAnnotations : values) {
      for (DexAnnotation annotation : parameterAnnotations.annotations) {
        consumer.accept(annotation);
      }
    }
  }

  /**
   * Return the number of parameters in the method prototype, or zero if the method's parameters
   * have no annotations.
   */
  public int size() {
    return missingParameterAnnotations + values.length;
  }

  /**
   * Return the number of parameters specified in the ParameterAnnotations attribute, that is, the
   * number of parameters for which {@link ParameterAnnotationsList#isMissing(int)} returns false.
   */
  public int countNonMissing() {
    return values.length;
  }

  /**
   * Return true if the ParameterAnnotations attribute is missing an entry for this parameter. This
   * is sometimes the case for the first parameter in a method on an inner class.
   *
   * @param i Index of the parameter in the method prototype.
   */
  public boolean isMissing(int i) {
    assert i >= 0;
    return i < missingParameterAnnotations;
  }

  /**
   * Return the annotations on the {@code i}th parameter (indexed according to the method
   * prototype). If the parameter's annotation list is missing, or {@code i} is not less than the
   * number of parameters (see {@link ParameterAnnotationsList#isMissing(int)}), {@link
   * DexAnnotationSet#empty()} is returned.
   *
   * @param i Index of the parameter in the method prototype.
   */
  public DexAnnotationSet get(int i) {
    assert i >= 0;
    int adjustedIndex = i - missingParameterAnnotations;
    return (0 <= adjustedIndex && adjustedIndex < values.length)
        ? values[adjustedIndex]
        : DexAnnotationSet.empty();
  }

  /** Return a ParameterAnnotationsList extended to the given number of parameters. */
  public ParameterAnnotationsList withParameterCount(int parameterCount) {
    if (this == EMPTY_PARAMETER_ANNOTATIONS_LIST || parameterCount == size()) {
      return this;
    }
    if (parameterCount < size()) {
      // Generally, it should never be the case that parameterCount < size(). However, it may be
      // that the input has already been optimized (e.g., by Proguard), and that some optimization
      // has removed formal parameters without removing the corresponding parameters annotations.
      // In this case, we remove the excess annotations.
      DexAnnotationSet[] trimmedValues = new DexAnnotationSet[parameterCount];
      System.arraycopy(values, 0, trimmedValues, 0, parameterCount);
      return new ParameterAnnotationsList(trimmedValues, 0);
    }
    return new ParameterAnnotationsList(values, parameterCount - values.length);
  }

  /**
   * Return a new ParameterAnnotationsList that keeps only the annotations matched by {@code
   * filter}.
   */
  public ParameterAnnotationsList keepIf(Predicate<DexAnnotation> filter) {
    DexAnnotationSet[] filtered = null;
    boolean allEmpty = true;
    for (int i = 0; i < values.length; i++) {
      DexAnnotationSet updated = values[i].keepIf(filter);
      if (updated != values[i]) {
        if (filtered == null) {
          filtered = values.clone();
        }
        filtered[i] = updated;
      }
      if (!updated.isEmpty()) {
        allEmpty = false;
      }
    }
    if (filtered == null) {
      return this;
    }
    if (allEmpty) {
      return ParameterAnnotationsList.empty();
    }
    return new ParameterAnnotationsList(filtered, missingParameterAnnotations);
  }

  public ParameterAnnotationsList rewrite(Function<DexAnnotationSet, DexAnnotationSet> mapper) {
    DexAnnotationSet[] rewritten = ArrayUtils.map(DexAnnotationSet[].class, values, mapper);
    if (rewritten != values) {
      return new ParameterAnnotationsList(rewritten, missingParameterAnnotations);
    }
    return this;
  }
}
