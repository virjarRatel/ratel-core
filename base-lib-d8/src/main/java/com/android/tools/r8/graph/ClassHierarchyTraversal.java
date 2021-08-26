// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.errors.Unreachable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

abstract class ClassHierarchyTraversal<
    T extends DexClass, CHT extends ClassHierarchyTraversal<T, CHT>> {

  enum Scope {
    ALL_CLASSES,
    ONLY_LIBRARY_CLASSES,
    ONLY_LIBRARY_AND_CLASSPATH_CLASSES,
    ONLY_PROGRAM_CLASSES;

    public boolean shouldBePassedToVisitor(DexClass clazz) {
      switch (this) {
        case ALL_CLASSES:
          return true;

        case ONLY_LIBRARY_CLASSES:
          return clazz.isLibraryClass();

        case ONLY_LIBRARY_AND_CLASSPATH_CLASSES:
          return clazz.isLibraryClass() || clazz.isClasspathClass();

        case ONLY_PROGRAM_CLASSES:
          return clazz.isProgramClass();

        default:
          throw new Unreachable();
      }
    }
  }

  final AppView<? extends AppInfoWithSubtyping> appView;
  final Scope scope;

  final Set<DexClass> visited = new HashSet<>();
  final Deque<T> worklist = new ArrayDeque<>();

  boolean excludeInterfaces = false;

  ClassHierarchyTraversal(AppView<? extends AppInfoWithSubtyping> appView, Scope scope) {
    this.appView = appView;
    this.scope = scope;
  }

  abstract CHT self();

  public CHT excludeInterfaces() {
    this.excludeInterfaces = true;
    return self();
  }

  public void visit(Iterable<DexProgramClass> sources, Consumer<T> visitor) {
    Iterator<DexProgramClass> sourceIterator = sources.iterator();

    // Visit the program classes in the order that is implemented by addDependentsToWorklist().
    while (sourceIterator.hasNext() || !worklist.isEmpty()) {
      if (worklist.isEmpty()) {
        // Add the dependents of the next source (including the source itself) to the worklist.
        // The order in which the dependents are added to the worklist is used to determine the
        // traversal order (e.g., top-down, bottom-up).
        addDependentsToWorklist(sourceIterator.next());
        if (worklist.isEmpty()) {
          continue;
        }
      }

      T clazz = worklist.removeFirst();
      if (visited.add(clazz)) {
        assert scope != Scope.ONLY_PROGRAM_CLASSES || clazz.isProgramClass();
        visitor.accept(clazz);
      }
    }

    visited.clear();
  }

  // The traversal order is dependent on the order in which addDependentsToWorklist() enqueues
  // classes into the worklist. For example, to implement a top-down class hierarchy, this method
  // should add all super types of `clazz` to the worklist before adding `clazz` itself to the
  // worklist.
  abstract void addDependentsToWorklist(DexClass clazz);
}
