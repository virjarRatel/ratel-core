// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.position;

import com.android.tools.r8.Keep;

@Keep
public class TextRange implements Position {
  private final TextPosition start;
  private final TextPosition end;

  public TextRange(TextPosition start, TextPosition end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Return the start position of this range.
   */
  public TextPosition getStart() {
    return start;
  }

  /**
   * Return the end position of this range.
   */
  public TextPosition getEnd() {
    return end;
  }

  @Override
  public int hashCode() {
    return start.hashCode() ^ end.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o != null  && o.getClass().equals(getClass())) {
      TextRange other = (TextRange) o;
      return start.equals(other.getStart()) && end.equals(other.getEnd());
    }
    return false;
  }

  @Override
  public String toString() {
      return "Text range from: '" + getStart() + "', to: '" + getEnd() + "'";
  }

  @Override
  public String getDescription() {
    return start.getDescription();
  }
}
