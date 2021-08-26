// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.origin;

import com.android.tools.r8.KeepForSubclassing;
import java.util.ArrayList;
import java.util.List;

/**
 * Origin description of a resource.
 *
 * <p>An origin is a list of parts that describe where a resource originates from. The first part
 * is the most recent part and is associated with the present resource, each successive part is
 * then associated with the context of the previous part.
 *
 * <p>For example, for a class file, say {@code my/class/Foo.class}, that is contained within a
 * jar archive, say {@code myjar.jar}, the Origin of of this resource will be {@code
 * myjar.jar:my/class/Foo.class} where each part is separated by a colon.
 *
 * <p>There are two top-most origins which have no parent: {@code Origin.root()} and {@code
 * Origin.unknown()}. The former is the parent of any file path, while the latter is an unknown
 * origin (e.g., for generated resources of raw bytes).
 */
@KeepForSubclassing
public abstract class Origin implements Comparable<Origin> {

  private static final Origin ROOT =
      new Origin() {
        @Override
        public String part() {
          return "";
        }

        @Override
        List<String> buildParts(int size) {
          return new ArrayList<>(size);
        }
      };

  private static final Origin UNKNOWN =
      new Origin() {
        @Override
        public String part() {
          return "<unknown>";
        }

        @Override
        List<String> buildParts(int size) {
          List<String> parts = new ArrayList<>(size + 1);
          parts.add(part());
          return parts;
        }
      };

  public static Origin root() {
    return ROOT;
  }

  public static Origin unknown() {
    return UNKNOWN;
  }

  private final Origin parent;

  private Origin() {
    this.parent = null;
  }

  protected Origin(Origin parent) {
    assert parent != null;
    this.parent = parent;
  }

  public abstract String part();

  public Origin parent() {
    return parent;
  }

  public List<String> parts() {
    return buildParts(0);
  }

  List<String> buildParts(int size) {
    List<String> parts = parent().buildParts(size + 1);
    parts.add(part());
    return parts;
  }

  /**
   * Find first parent or this instance of the given class.
   * @return This {@link Origin} if it's an instance of the requested {@link Class} or the first
   * parent found matching the condition. May return null if no satisfying instance is found.
   */
  @SuppressWarnings("unchecked")
  public <T extends Origin> T getFromHierarchy(Class<T> type) {
    Origin origin = this;
    do {
      if (type.isInstance(origin)) {
        return (T) origin;
      }
      origin = origin.parent();
    } while (origin != null);
    return null;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Origin)) {
      return false;
    }
    Origin self = this;
    Origin other = (Origin) obj;
    while (self != null && other != null && self.part().equals(other.part())) {
      self = self.parent();
      other = other.parent();
    }
    return self == other;
  }

  @Override
  public int compareTo(Origin other) {
    // Lexicographic ordering from root to leaf.
    List<String> thisParts = parts();
    List<String> otherParts = other.parts();
    int len = Math.min(thisParts.size(), otherParts.size());
    for (int i = 0; i < len; i++) {
      int compare = thisParts.get(i).compareTo(otherParts.get(i));
      if (compare != 0) {
        return compare;
      }
    }
    return Integer.compare(thisParts.size(), otherParts.size());
  }

  @Override
  public int hashCode() {
    int hash = 1;
    for (String part : parts()) {
      hash = 31 * hash + part.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    return String.join(":", parts());
  }
}
