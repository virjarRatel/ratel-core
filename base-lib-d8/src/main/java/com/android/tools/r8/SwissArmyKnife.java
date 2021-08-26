// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.bisect.Bisect;
import com.android.tools.r8.compatdx.CompatDx;
import com.android.tools.r8.compatproguard.CompatProguard;
import com.android.tools.r8.dexfilemerger.DexFileMerger;
import com.android.tools.r8.dexsplitter.DexSplitter;
import java.util.Arrays;

/**
 * Common entry point to everything in the R8 project.
 *
 * <p>This class is used as the main class in {@code r8.jar}. It checks the first command-line
 * argument to find the tool to run, or runs {@link R8} if the first argument is not a recognized
 * tool name.
 *
 * <p>The set of tools recognized by this class is defined by a switch statement in {@link
 * SwissArmyKnife#main(String[])}.
 */
public class SwissArmyKnife {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      runDefault(args);
      return;
    }
    switch (args[0]) {
      case "bisect":
        Bisect.main(shift(args));
        break;
      case "compatdx":
        CompatDx.main(shift(args));
        break;
      case "compatproguard":
        CompatProguard.main(shift(args));
        break;
      case "d8":
        D8.main(shift(args));
        break;
      case "d8logger":
        D8Logger.main(shift(args));
        break;
      case "dexfilemerger":
        DexFileMerger.main(shift(args));
        break;
      case "dexsegments":
        DexSegments.main(shift(args));
        break;
      case "dexsplitter":
        DexSplitter.main(shift(args));
        break;
      case "disasm":
        Disassemble.main(shift(args));
        break;
      case "extractmarker":
        ExtractMarker.main(shift(args));
        break;
      case "jardiff":
        JarDiff.main(shift(args));
        break;
      case "jarsizecompare":
        JarSizeCompare.main(shift(args));
        break;
      case "maindex":
        GenerateMainDexList.main(shift(args));
        break;
      case "printseeds":
        PrintSeeds.main(shift(args));
        break;
      case "printuses":
        PrintUses.main(shift(args));
        break;
      case "r8":
        R8.main(shift(args));
        break;
      default:
        runDefault(args);
        break;
    }
  }

  private static void runDefault(String[] args) {
    R8.main(args);
  }

  private static String[] shift(String[] args) {
    return Arrays.copyOfRange(args, 1, args.length);
  }
}
