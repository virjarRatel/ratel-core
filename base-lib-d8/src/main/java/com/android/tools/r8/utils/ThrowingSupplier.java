// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

/**
 * Similar to a {@link java.util.function.Supplier} but throws a single {@link Throwable}.
 *
 * @param <T> the type of the result
 * @param <E> the type of the {@link Throwable}
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {

  T get() throws E;
}
