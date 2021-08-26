// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.AndroidAppConsumers;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DexRoundTrip {

  public static void process(D8Command.Builder builder) throws CompilationFailedException {
    builder.setDisableDesugaring(true);
    D8Command command = builder.build();
    InternalOptions options = command.getInternalOptions();
    AndroidApp app = command.getInputApp();
    options.passthroughDexCode = false;
    D8.runForTesting(app, options);
  }

  public static void main(String[] args) throws CompilationFailedException {
    D8Command.Builder builder = D8Command.builder();
    for (String arg : args) {
      Path file = Paths.get(arg);
      if (!FileUtils.isDexFile(file)) {
        throw new IllegalArgumentException(
            "Only DEX files are supported as inputs. Invalid file: " + file);
      }
      builder.addProgramFiles(file);
    }
    AndroidAppConsumers consumer = new AndroidAppConsumers();
    builder.setProgramConsumer(consumer.wrapDexIndexedConsumer(null));
    process(builder);
    process(
        D8Command.builder(consumer.build()).setProgramConsumer(DexIndexedConsumer.emptyConsumer()));
  }
}
