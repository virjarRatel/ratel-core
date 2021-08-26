// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class DexByteCodeWriter {

  private interface OutputStreamProvider {
    PrintStream get(DexClass clazz) throws IOException;
  }

  final DexApplication application;
  final InternalOptions options;

  DexByteCodeWriter(DexApplication application,
      InternalOptions options) {
    this.application = application;
    this.options = options;
  }

  private void ensureParentExists(Path path) throws IOException {
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }

  private OutputStreamProvider oneFilePerClass(Path path) {
    return (clazz) -> {
      String className = DescriptorUtils.descriptorToJavaType(clazz.type.toDescriptorString(),
          application.getProguardMap());
      Path classOutput = path.resolve(className.replace('.', File.separatorChar)
          + getFileEnding());
      ensureParentExists(classOutput);
      return new PrintStream(Files.newOutputStream(classOutput));
    };
  }

  public void write(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      write(oneFilePerClass(path), PrintStream::close);
    } else {
      ensureParentExists(path);
      try (PrintStream ps = new PrintStream(Files.newOutputStream(path))) {
        write(ps);
      }
    }
  }

  public void write(PrintStream output) throws IOException {
    write(x -> output, x -> {
    });
  }

  private void write(OutputStreamProvider outputStreamProvider, Consumer<PrintStream> closer)
      throws IOException {
    Iterable<DexProgramClass> classes = application.classesWithDeterministicOrder();
    for (DexProgramClass clazz : classes) {
      if (anyMethodMatches(clazz)) {
        PrintStream ps = outputStreamProvider.get(clazz);
        try {
          writeClass(clazz, ps);
        } finally {
          closer.accept(ps);
        }
      }
    }
  }

  private boolean anyMethodMatches(DexClass clazz) {
    return !options.hasMethodsFilter()
        || clazz.virtualMethods().stream().anyMatch(options::methodMatchesFilter)
        || clazz.directMethods().stream().anyMatch(options::methodMatchesFilter);
  }

  private void writeClass(DexProgramClass clazz, PrintStream ps) {
    writeClassHeader(clazz, ps);
    writeFieldsHeader(clazz, ps);
    clazz.forEachField(field -> writeField(field, ps));
    writeFieldsFooter(clazz, ps);
    writeMethodsHeader(clazz, ps);
    clazz.forEachMethod(method -> writeMethod(method, ps));
    writeMethodsFooter(clazz, ps);
    writeClassFooter(clazz, ps);
  }

  abstract String getFileEnding();

  abstract void writeClassHeader(DexProgramClass clazz, PrintStream ps);

  void writeFieldsHeader(DexProgramClass clazz, PrintStream ps) {
    // Do nothing.
  }

  abstract void writeField(DexEncodedField field, PrintStream ps);

  void writeFieldsFooter(DexProgramClass clazz, PrintStream ps) {
    // Do nothing.
  }

  void writeMethodsHeader(DexProgramClass clazz, PrintStream ps) {
    // Do nothing.
  }

  abstract void writeMethod(DexEncodedMethod method, PrintStream ps);

  void writeMethodsFooter(DexProgramClass clazz, PrintStream ps) {
    // Do nothing.
  }

  abstract void writeClassFooter(DexProgramClass clazz, PrintStream ps);
}
