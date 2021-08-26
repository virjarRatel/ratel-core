// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.position;

import com.android.tools.r8.Keep;

/**
 * A {@link Position} in a text file determined by line and column.
 * Line and column numbers start at 1.
 */
@Keep
public class TextPosition implements Position {

  /**
   * Column is unknown.
   */
  public static final int UNKNOWN_COLUMN = -1;

  /**
   * Char offset from the start of the text resource.
   */
  private final long offset;
  private final int line;
  private final int column;

  public TextPosition(long offset, int line, int column) {
    assert (offset >= 0)
        && (line >= 1)
        && (column >= 1 || column == UNKNOWN_COLUMN);
    this.offset = offset;
    this.line = line;
    this.column = column;
  }

  /**
   * Return the line of this position.
   */
  public int getLine() {
    return line;
  }

  /**
   * Return the column of this position.
   * @return May return {@link #UNKNOWN_COLUMN} if column information is not available.
   */
  public int getColumn() {
    return column;
  }

  public long getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return "offset: " + offset + ", line: " + line + ", column: " + column;
  }

  @Override
  public String getDescription() {
    return "line " + line + (column != UNKNOWN_COLUMN ? (", column " + column) : "");
  }

  @Override
  public int hashCode() {
    return Long.hashCode(offset) ^ line ^ (column << 16);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o != null  && o.getClass().equals(TextPosition.class)) {
      TextPosition other = (TextPosition) o;
      return offset == other.offset && line == other.line && column == other.column;
    }
    return false;
  }
}
