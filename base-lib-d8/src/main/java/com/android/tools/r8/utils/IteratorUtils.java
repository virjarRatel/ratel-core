// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class IteratorUtils {

  public static <T, S extends T> Iterator<S> filter(Iterator<T> iterator, Predicate<T> predicate) {
    return new Iterator<S>() {

      private S next = advance();

      @SuppressWarnings("unchecked")
      private S advance() {
        while (iterator.hasNext()) {
          T element = iterator.next();
          if (predicate.test(element)) {
            return (S) element;
          }
        }
        return null;
      }

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public S next() {
        S current = next;
        if (current == null) {
          throw new NoSuchElementException();
        }
        next = advance();
        return current;
      }
    };
  }

  public static <T> T peekPrevious(ListIterator<T> iterator) {
    T previous = iterator.previous();
    T next = iterator.next();
    assert previous == next;
    return previous;
  }

  public static <T> T peekNext(ListIterator<T> iterator) {
    if (iterator.hasNext()) {
      T next = iterator.next();
      T previous = iterator.previous();
      assert previous == next;
      return next;
    }
    return null;
  }

  public static <T> T previousUntil(ListIterator<T> iterator, Predicate<T> predicate) {
    while (iterator.hasPrevious()) {
      T previous = iterator.previous();
      if (predicate.test(previous)) {
        return previous;
      }
    }
    throw new Unreachable();
  }

  public static <T> void removeIf(Iterator<T> iterator, Predicate<T> predicate) {
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (predicate.test(item)) {
        iterator.remove();
      }
    }
  }

  /** @deprecated Use {@link #removeIf(InstructionListIterator, Predicate)} instead. */
  @Deprecated
  public static void removeIf(InstructionIterator iterator, Predicate<Instruction> predicate) {
    throw new Unimplemented();
  }

  public static void removeIf(InstructionListIterator iterator, Predicate<Instruction> predicate) {
    removeIf((Iterator<Instruction>) iterator, predicate);
  }

  public static <T> boolean allRemainingMatch(ListIterator<T> iterator, Predicate<T> predicate) {
    return !anyRemainingMatch(iterator, remaining -> !predicate.test(remaining));
  }

  public static <T> boolean anyRemainingMatch(ListIterator<T> iterator, Predicate<T> predicate) {
    T state = peekNext(iterator);
    boolean result = false;
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (predicate.test(item)) {
        result = true;
        break;
      }
    }
    while (iterator.hasPrevious() && iterator.previous() != state) {
      // Restore the state of the iterator.
    }
    assert peekNext(iterator) == state;
    return result;
  }
}
