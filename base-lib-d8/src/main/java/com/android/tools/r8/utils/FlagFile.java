// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.BaseCommand;
import com.android.tools.r8.origin.Origin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FlagFile {

  private static class FlagFileOrigin extends Origin {
    private final Path path;

    protected FlagFileOrigin(Path path) {
      super(Origin.root());
      this.path = path;
    }

    @Override
    public String part() {
      return "flag file argument: '@" + path + "'";
    }
  }

  public static String[] expandFlagFiles(String[] args, BaseCommand.Builder builder) {
    List<String> flags = new ArrayList<>(args.length);
    for (String arg : args) {
      if (arg.startsWith("@")) {
        Path flagFilePath = Paths.get(arg.substring(1));
        try {
          flags.addAll(Files.readAllLines(flagFilePath));
        } catch (IOException e) {
          Origin origin = new FlagFileOrigin(flagFilePath);
          builder.error(new ExceptionDiagnostic(e, origin));
        }
      } else {
        flags.add(arg);
      }
    }
    return flags.toArray(StringUtils.EMPTY_ARRAY);
  }
}
