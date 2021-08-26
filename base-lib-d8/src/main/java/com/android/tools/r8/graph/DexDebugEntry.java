// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class DexDebugEntry {

  public final boolean lineEntry;
  public final int address;
  public final int line;
  public final DexString sourceFile;
  public final boolean prologueEnd;
  public final boolean epilogueBegin;
  public final Map<Integer, DebugLocalInfo> locals;
  public final DexMethod method;
  public final Position callerPosition;

  public DexDebugEntry(
      boolean lineEntry,
      int address,
      int line,
      DexString sourceFile,
      boolean prologueEnd,
      boolean epilogueBegin,
      ImmutableMap<Integer, DebugLocalInfo> locals,
      DexMethod method,
      Position callerPosition) {
    this.lineEntry = lineEntry;
    this.address = address;
    this.line = line;
    this.sourceFile = sourceFile;
    this.prologueEnd = prologueEnd;
    this.epilogueBegin = epilogueBegin;
    this.locals = locals;
    this.method = method;
    assert method != null;
    this.callerPosition = callerPosition;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withPcPrefix) {
    StringBuilder builder = new StringBuilder();
    if (withPcPrefix) {
      builder.append("pc ");
    }
    builder.append(StringUtils.hexString(address, 2));
    if (sourceFile != null) {
      builder.append(", file ").append(sourceFile);
    }
    builder.append(", line ").append(line);
    if (callerPosition != null) {
      builder.append(":").append(method.name);
      Position caller = callerPosition;
      while (caller != null) {
        builder.append(";").append(caller.line).append(":").append(caller.method.name);
        caller = caller.callerPosition;
      }
    }
    if (prologueEnd) {
      builder.append(", prologue_end = true");
    }
    if (epilogueBegin) {
      builder.append(", epilogue_begin = true");
    }
    if (!locals.isEmpty()) {
      builder.append(", locals: [");
      SortedSet<Integer> keys = new TreeSet<>(locals.keySet());
      boolean first = true;
      for (Integer register : keys) {
        if (first) {
          first = false;
        } else {
          builder.append(", ");
        }
        builder.append(register).append(" -> ").append(locals.get(register));
      }
      builder.append("]");
    }
    return builder.toString();
  }
}
