// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.errors;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.Keep;
import com.android.tools.r8.dex.VirtualFile;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;

/**
 * Diagnostic information about errors when classes cannot fit in a DEX file.
 *
 * <p>This can happen when compiling to a single DEX file but not all classes can fit in it; or when
 * compiling for legacy multidex but there are too many classes that need to fit in the main DEX
 * file, e.g., classes.dex.
 */
@Keep
public class DexFileOverflowDiagnostic implements Diagnostic {
  private final boolean hasMainDexSpecification;
  private final long numOfMethods;
  private final long numOfFields;

  public DexFileOverflowDiagnostic(
      boolean hasMainDexSpecification, long numOfMethods, long numOfFields) {
    this.hasMainDexSpecification = hasMainDexSpecification;
    this.numOfMethods = numOfMethods;
    this.numOfFields = numOfFields;
  }

  /** The number of fields that the application needs to include in the main DEX file. */
  public long getNumberOfFields() {
    return numOfFields;
  }

  /** The number of methods that the application needs to include in the main DEX file. */
  public long getNumberOfMethods() {
    return numOfMethods;
  }

  /** The maximum number of fields that can be included in a DEX file. */
  public long getMaximumNumberOfFields() {
    return VirtualFile.MAX_ENTRIES;
  }

  /** The maximum number of methods that can be included in a DEX file. */
  public long getMaximumNumberOfMethods() {
    return VirtualFile.MAX_ENTRIES;
  }

  /** True if the application has specified lists and/or rules for computing the main DEX file. */
  public boolean hasMainDexSpecification() {
    return hasMainDexSpecification;
  }

  /** The origin of a main DEX file overflow is not unique. (The whole app is to blame.) */
  @Override
  public Origin getOrigin() {
    return Origin.unknown();
  }

  /** The position of the main DEX error is not specified. */
  @Override
  public Position getPosition() {
    return null;
  }

  @Override
  public String getDiagnosticMessage() {
    StringBuilder builder = new StringBuilder();
    // General message: Cannot fit.
    builder
        .append("Cannot fit requested classes in ")
        .append(hasMainDexSpecification() ? "the main-" : "a single ")
        .append("dex file")
        .append(" (");
    // Show the numbers of methods and/or fields that exceed the limit.
    if (getNumberOfMethods() > getMaximumNumberOfMethods()) {
      builder
          .append("# methods: ")
          .append(getNumberOfMethods())
          .append(" > ")
          .append(getMaximumNumberOfMethods());
      if (getNumberOfFields() > getMaximumNumberOfFields()) {
        builder.append(" ; ");
      }
    }
    if (getNumberOfFields() > getMaximumNumberOfFields()) {
      builder
          .append("# fields: ")
          .append(getNumberOfFields())
          .append(" > ")
          .append(getMaximumNumberOfFields());
    }
    return builder.append(")").toString();
  }
}
