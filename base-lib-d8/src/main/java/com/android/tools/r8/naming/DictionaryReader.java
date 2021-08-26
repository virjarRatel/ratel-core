// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static com.android.tools.r8.position.TextPosition.UNKNOWN_COLUMN;

import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.position.TextPosition;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DictionaryReader implements AutoCloseable {

  private final BufferedReader reader;
  private final Path path;

  public DictionaryReader(Path path) throws IOException {
    this.path = path;
    this.reader = Files.newBufferedReader(path);
  }

  public String readName(Reporter reporter) throws IOException {
    assert reader != null;

    StringBuilder name = new StringBuilder();
    int readCharAsInt;
    int lineNumber = 1;

    while ((readCharAsInt = reader.read()) != -1) {
      char readChar = (char) readCharAsInt;

      if ((name.length() != 0 && Character.isJavaIdentifierPart(readChar))
          || (name.length() == 0 && Character.isJavaIdentifierStart(readChar))) {
        name.append(readChar);
      } else {
        boolean isCommentChar = readChar == '#';
        boolean isValidEndOfLineInput = readChar == '\n' || readChar == '\r';
        if (isCommentChar || isValidEndOfLineInput) {
          if (isCommentChar) {
            reader.readLine();
          }
          lineNumber++;
        }
        if (isValidEndOfLineInput && name.length() != 0) {
          return name.toString();
        }
        name = new StringBuilder();
        // An illegal character was in the input. We discard the entire line instead of returning as
        // much as possible, to ensure us not throwing an error in the case of duplicates.
        if (!isValidEndOfLineInput) {
          reporter.info(
              new StringDiagnostic(
                  "Invalid character in dictionary '" + readChar + "'",
                  new PathOrigin(path),
                  new TextPosition(0, lineNumber, UNKNOWN_COLUMN)));
          reader.readLine();
          lineNumber++;
        }
      }
    }

    return name.toString();
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }

  public static ImmutableList<String> readAllNames(Path path, Reporter reporter) {
    if (path != null) {
      Set<String> seenNames = new HashSet<>();
      Builder<String> namesBuilder = new ImmutableList.Builder<String>();
      try (DictionaryReader reader = new DictionaryReader(path);) {
        String name = reader.readName(reporter);
        while (!name.isEmpty()) {
          if (!seenNames.add(name)) {
            reporter.error(
                new StringDiagnostic(
                    "Duplicate entry for '" + name + "' in dictionary", new PathOrigin(path)));
          }
          namesBuilder.add(name);
          name = reader.readName(reporter);
        }
      } catch (IOException e) {
        reporter.error(new ExceptionDiagnostic(e, new PathOrigin(path)));
      }
      return namesBuilder.build();
    } else {
      return ImmutableList.of();
    }
  }
}
