// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.utils.StringUtils;
import java.util.function.Consumer;

class UsagePrinter {
  private static final String INDENT = "    ";

  static final UsagePrinter DONT_PRINT = new NoOpUsagePrinter();

  private final Consumer<String> consumer;
  private DexProgramClass enclosingClazz = null;
  private boolean clazzPrefixPrinted = false;

  UsagePrinter(Consumer<String> consumer) {
    this.consumer = consumer;
  }

  void append(String string) {
    consumer.accept(string);
  }

  void printUnusedClass(DexProgramClass clazz) {
    append(clazz.toSourceString());
    append(StringUtils.LINE_SEPARATOR);
  }

  // Visiting methods and fields of the given clazz.
  void visiting(DexProgramClass clazz) {
    assert enclosingClazz == null;
    enclosingClazz = clazz;
  }

  // Visited methods and fields of the top at the clazz stack.
  void visited() {
    enclosingClazz = null;
    clazzPrefixPrinted = false;
  }

  private void printClazzPrefixIfNecessary() {
    assert enclosingClazz != null;
    if (!clazzPrefixPrinted) {
      append(enclosingClazz.toSourceString());
      append(":");
      append(StringUtils.LINE_SEPARATOR);
      clazzPrefixPrinted = true;
    }
  }

  void printUnusedMethod(DexEncodedMethod method) {
    printClazzPrefixIfNecessary();
    append(INDENT);
    String accessFlags = method.accessFlags.toString();
    if (!accessFlags.isEmpty()) {
      append(accessFlags);
      append(" ");
    }
    append(method.method.proto.returnType.toSourceString());
    append(" ");
    append(method.method.name.toSourceString());
    append("(");
    for (int i = 0; i < method.method.proto.parameters.values.length; i++) {
      if (i != 0) {
        append(",");
      }
      append(method.method.proto.parameters.values[i].toSourceString());
    }
    append(")");
    append(StringUtils.LINE_SEPARATOR);
  }

  void printUnusedField(DexEncodedField field) {
    printClazzPrefixIfNecessary();
    append(INDENT);
    String accessFlags = field.accessFlags.toString();
    if (!accessFlags.isEmpty()) {
      append(accessFlags);
      append(" ");
    }
    append(field.field.type.toSourceString());
    append(" ");
    append(field.field.name.toSourceString());
    append(StringUtils.LINE_SEPARATOR);
  }

  // Empty implementation to silently ignore printing dead code.
  private static class NoOpUsagePrinter extends UsagePrinter {

    public NoOpUsagePrinter() {
      super(null);
    }

    @Override
    void printUnusedClass(DexProgramClass clazz) {
      // Intentionally left empty.
    }

    @Override
    void visiting(DexProgramClass clazz) {
      // Intentionally left empty.
    }

    @Override
    void visited() {
      // Intentionally left empty.
    }

    @Override
    void printUnusedMethod(DexEncodedMethod method) {
      // Intentionally left empty.
    }

    @Override
    void printUnusedField(DexEncodedField field) {
      // Intentionally left empty.
    }
  }
}
