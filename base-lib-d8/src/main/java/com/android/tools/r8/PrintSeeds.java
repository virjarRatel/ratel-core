// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.utils.ExceptionUtils.unwrapExecutionException;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppServices;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.Enqueuer;
import com.android.tools.r8.shaking.EnqueuerFactory;
import com.android.tools.r8.shaking.RootSetBuilder;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * PrintSeeds prints the classes, interfaces, methods and fields selected by a given ProGuard
 * configuration &lt;pg-conf.txt&gt; when compiling a given program &lt;r8.jar&gt; alongside a given
 * library &lt;rt.jar&gt;.
 *
 * <p>The output format is identical to what is printed when {@code -printseeds} is specified in
 * &lt;pg-conf.txt&gt;, but running PrintSeeds can be faster than running R8 with {@code
 * -printseeds}. See also the {@link PrintUses} program in R8.
 */
public class PrintSeeds {

  private static final String USAGE =
      "Arguments: <rt.jar> <r8.jar> <pg-conf.txt>\n"
          + "\n"
          + "PrintSeeds prints the classes, interfaces, methods and fields selected by\n"
          + "<pg-conf.txt> when compiling <r8.jar> alongside <rt.jar>.\n"
          + "\n"
          + "The output format is identical to what is printed when -printseeds is specified in\n"
          + "<pg-conf.txt>, but running PrintSeeds can be faster than running R8 with \n"
          + "-printseeds. See also the "
          + PrintUses.class.getSimpleName()
          + " program in R8.";

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println(USAGE.replace("\n", System.lineSeparator()));
      System.exit(1);
    }
    Path rtJar = Paths.get(args[0]);
    Path r8Jar = Paths.get(args[1]);
    Path pgConf = Paths.get(args[2]);
    R8Command command =
        R8Command.builder()
            .addLibraryFiles(rtJar)
            .addProgramFiles(r8Jar)
            .addProguardConfigurationFiles(pgConf)
            .setProgramConsumer(ClassFileConsumer.emptyConsumer())
            .build();
    Set<String> descriptors = new ArchiveClassFileProvider(r8Jar).getClassDescriptors();
    InternalOptions options = command.getInternalOptions();
    ExecutorService executorService = ThreadUtils.getExecutorService(options);
    ExceptionUtils.withR8CompilationHandler(
        command.getReporter(),
        () -> {
          try {
            run(command, descriptors, options, executorService);
          } finally {
            executorService.shutdown();
          }
        });
  }

  private static void run(
      R8Command command, Set<String> descriptors, InternalOptions options, ExecutorService executor)
      throws IOException {
    assert !options.forceProguardCompatibility;
    Timing timing = new Timing("PrintSeeds");
    try {
      DexApplication application =
          new ApplicationReader(command.getInputApp(), options, timing).read(executor).toDirect();
      AppView<? extends AppInfoWithSubtyping> appView =
          AppView.createForR8(new AppInfoWithSubtyping(application), options);
      appView.setAppServices(AppServices.builder(appView).build());
      RootSet rootSet =
          new RootSetBuilder(appView, application, options.getProguardConfiguration().getRules())
              .run(executor);
      Enqueuer enqueuer = EnqueuerFactory.createForInitialTreeShaking(appView);
      AppInfoWithLiveness appInfo =
          enqueuer.traceApplication(
              rootSet, options.getProguardConfiguration().getDontWarnPatterns(), executor, timing);
      RootSetBuilder.writeSeeds(
          appInfo, System.out, type -> descriptors.contains(type.toDescriptorString()));
    } catch (ExecutionException e) {
      throw unwrapExecutionException(e);
    }
  }
}
