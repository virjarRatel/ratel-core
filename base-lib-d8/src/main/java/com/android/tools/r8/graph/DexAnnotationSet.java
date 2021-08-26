// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.utils.ArrayUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class DexAnnotationSet extends CachedHashValueDexItem {

  public static final DexAnnotationSet[] EMPTY_ARRAY = {};

  private static final int UNSORTED = 0;
  private static final DexAnnotationSet THE_EMPTY_ANNOTATIONS_SET =
      new DexAnnotationSet(DexAnnotation.EMPTY_ARRAY);

  public final DexAnnotation[] annotations;
  private int sorted = UNSORTED;

  public DexAnnotationSet(DexAnnotation[] annotations) {
    this.annotations = annotations;
  }

  public static DexType findDuplicateEntryType(DexAnnotation[] annotations) {
    return findDuplicateEntryType(Arrays.asList(annotations));
  }

  public static DexType findDuplicateEntryType(List<DexAnnotation> annotations) {
    Set<DexType> seenTypes = new HashSet<>();
    for (DexAnnotation annotation : annotations) {
      if (!seenTypes.add(annotation.annotation.type)) {
        return annotation.annotation.type;
      }
    }
    return null;
  }

  public static DexAnnotationSet empty() {
    return THE_EMPTY_ANNOTATIONS_SET;
  }

  public int size() {
    return annotations.length;
  }

  @Override
  public int computeHashCode() {
    return Arrays.hashCode(annotations);
  }

  @Override
  public boolean computeEquals(Object other) {
    if (other instanceof DexAnnotationSet) {
      DexAnnotationSet o = (DexAnnotationSet) other;
      return Arrays.equals(annotations, o.annotations);
    }
    return false;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    collectAll(indexedItems, annotations);
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    mixedItems.add(this);
    collectAll(mixedItems, annotations);
  }

  public boolean isEmpty() {
    return annotations.length == 0;
  }

  public void sort() {
    if (sorted != UNSORTED) {
      assert sorted == sortedHashCode();
      return;
    }
    Arrays.sort(annotations, (a, b) -> a.annotation.type.compareTo(b.annotation.type));
    for (DexAnnotation annotation : annotations) {
      annotation.annotation.sort();
    }
    sorted = hashCode();
  }

  public DexAnnotation getFirstMatching(DexType type) {
    for (DexAnnotation annotation : annotations) {
      if (annotation.annotation.type == type) {
        return annotation;
      }
    }
    return null;
  }

  public DexAnnotationSet getWithout(DexType annotationType) {
    int index = 0;
    for (DexAnnotation annotation : annotations) {
      if (annotation.annotation.type == annotationType) {
        DexAnnotation[] reducedArray = new DexAnnotation[annotations.length - 1];
        System.arraycopy(annotations, 0, reducedArray, 0, index);
        if (index < reducedArray.length) {
          System.arraycopy(annotations, index + 1, reducedArray, index, reducedArray.length - index);
        }
        return new DexAnnotationSet(reducedArray);
      }
      ++index;
    }
    return this;
  }

  private int sortedHashCode() {
    int hashCode = hashCode();
    return hashCode == UNSORTED ? 1 : hashCode;
  }

  public DexAnnotationSet getWithAddedOrReplaced(DexAnnotation newAnnotation) {

    // Check existing annotation for replacement.
    int index = 0;
    for (DexAnnotation annotation : annotations) {
      if (annotation.annotation.type == newAnnotation.annotation.type) {
        DexAnnotation[] modifiedArray = annotations.clone();
        modifiedArray[index] = newAnnotation;
        return new DexAnnotationSet(modifiedArray);
      }
      ++index;
    }

    // No existing annotation, append.
    DexAnnotation[] extendedArray = new DexAnnotation[annotations.length + 1];
    System.arraycopy(annotations, 0, extendedArray, 0, annotations.length);
    extendedArray[annotations.length] = newAnnotation;
    return new DexAnnotationSet(extendedArray);
  }

  public DexAnnotationSet keepIf(Predicate<DexAnnotation> filter) {
    return rewrite(anno -> filter.test(anno) ? anno : null);
  }

  public DexAnnotationSet rewrite(Function<DexAnnotation, DexAnnotation> rewriter) {
    if (isEmpty()) {
      return this;
    }
    DexAnnotation[] rewritten = ArrayUtils.map(DexAnnotation[].class, annotations, rewriter);
    if (rewritten == annotations) {
      return this;
    }
    if (rewritten.length == 0) {
      return DexAnnotationSet.empty();
    }
    return new DexAnnotationSet(rewritten);
  }

  @Override
  public String toString() {
    return Arrays.toString(annotations);
  }
}
