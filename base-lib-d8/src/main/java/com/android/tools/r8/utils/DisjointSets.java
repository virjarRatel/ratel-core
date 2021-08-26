// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Disjoint sets of instances of type T. Each of the sets will be represented by one of the
 * instances in the set.
 */
public class DisjointSets<T> {
  // Forrest represented in a set. Each element maps to its parent. A root of a tree in
  // the forest maps to itself. Each tree in the forrest represent a set in the disjoint sets.
  private final Map<T, T> parent = new HashMap<>();

  /**
   * Create a new set containing only <code>element</code>.
   *
   * <p>The <code>element</code> must not be present in any existing sets.
   */
  public T makeSet(T element) {
    assert !parent.containsKey(element);
    parent.put(element, element);
    assert findSet(element) == element;
    return element;
  }

  /**
   * Returns the representative for the set containing <code>element</code>.
   *
   * <p>Returns null if <code>element</code> is not in any sets.
   */
  public T findSet(T element) {
    T candidate = parent.get(element);
    if (candidate == null) {
      return null;
    }
    T candidateParent = parent.get(candidate);
    if (candidate == candidateParent) {
      return candidate;
    }
    // If not at the representative recurse and compress path.
    T representative = findSet(candidateParent);
    parent.put(element, representative);
    return representative;
  }

  /**
   * Check if <code>element</code> is the representative for a set or not present at all.
   *
   * <p>Returns <code>true</code> if that is the case.
   */
  public boolean isRepresentativeOrNotPresent(T element) {
    T representative = findSet(element);
    return representative == null || representative.equals(element);
  }

  /**
   * Returns the set containing <code>element</code>.
   *
   * <p>Returns null if <code>element</code> is not in any sets.
   */
  public Set<T> collectSet(T element) {
    T representative = findSet(element);
    if (representative == null) {
      return null;
    }
    Set<T> result = new HashSet<>();
    for (T t : parent.keySet()) {
      // Find root with path-compression.
      if (findSet(t).equals(representative)) {
        result.add(t);
      }
    }
    return result;
  }

  /**
   * Returns the representative for the set containing <code>element</code> while creating a set
   * containing <code>element</code> if <code>element</code> is not present in any of the existing
   * sets.
   *
   * <p>Returns the representative for the set containing <code>element</code>.
   */
  public T findOrMakeSet(T element) {
    T representative = findSet(element);
    return representative != null ? representative : makeSet(element);
  }

  /**
   * Union the two sets represented by <code>representative1</code> and <code>representative2</code>
   * .
   *
   * <p>Both <code>representative1</code> and <code>representative2</code> must be actual
   * representatives of a set, and not just any member of a set.
   *
   * <p>Returns the representative for the union set.
   */
  public T union(T representative1, T representative2) {
    // The two representatives must be roots in different trees.
    assert representative1 != null;
    assert representative2 != null;
    if (representative1 == representative2) {
      return representative1;
    }
    assert parent.get(representative1) == representative1;
    assert parent.get(representative2) == representative2;
    // Join the trees.
    parent.put(representative2, representative1);
    assert findSet(representative1) == representative1;
    assert findSet(representative2) == representative1;
    return representative1;
  }

  /**
   * Union the two sets containing by <code>element1</code> and <code>element2</code> while creating
   * a set containing <code>element1</code> and <code>element2</code> if one of them is not present
   * in any of the existing sets.
   *
   * <p>Returns the representative for the union set.
   */
  public T unionWithMakeSet(T element1, T element2) {
    if (element1.toString().contains("Enum") || element2.toString().contains("Enum")) {
      System.out.println();
    }
    if (element1 == element2) {
      return findOrMakeSet(element1);
    }
    return union(findOrMakeSet(element1), findOrMakeSet(element2));
  }

  /** Returns the sets currently represented. */
  public Map<T, Set<T>> collectSets() {
    Map<T, Set<T>> unification = new HashMap<>();
    for (T element : parent.keySet()) {
      // Find root with path-compression.
      T representative = findSet(element);
      unification.computeIfAbsent(representative, k -> new HashSet<>()).add(element);
    }
    return unification;
  }

  @Override
  public String toString() {
    Map<T, Set<T>> sets = collectSets();
    StringBuilder sb =
        new StringBuilder()
            .append("Number of sets: ")
            .append(sets.keySet().size())
            .append(System.lineSeparator());
    sets.forEach(
        (representative, set) -> {
          sb.append("Representative: ").append(representative).append(System.lineSeparator());
          set.forEach(v -> sb.append("    ").append(v).append(System.lineSeparator()));
        });
    return sb.toString();
  }
}
