// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.shaking.ProguardConfigurationParser;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.Timing;
import java.nio.file.Paths;

/**
 * Benchmark for testing ability to and speed of parsing Proguard keep files.
 */
public class ReadKeepFile {

  private static final String DEFAULT_KEEP_FILE_NAME = "build/proguard.cfg";

  final Timing timing = new Timing("ReadKeepFile");

  private void readProguardKeepFile(String fileName) {
    System.out.println("  - reading " + fileName);
    timing.begin("Reading " + fileName);
    new ProguardConfigurationParser(new DexItemFactory(), new Reporter())
        .parse(Paths.get(fileName));
    timing.end();
  }

  public static void main(String[] args) {
    new ReadKeepFile().run(args);
  }

  private void run(String[] args) {
    System.out.println("ProguardKeepRuleParser benchmark.");
    if (args.length == 0) {
      readProguardKeepFile(DEFAULT_KEEP_FILE_NAME);
    } else {
      for (String name : args) {
        readProguardKeepFile(name);
      }
    }
    timing.report();
  }
}
