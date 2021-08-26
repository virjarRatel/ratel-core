// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.DexParser;
import com.android.tools.r8.dex.DexSection;
import com.android.tools.r8.origin.CommandLineOrigin;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class DexSegments {
  private static class Command extends BaseCommand {

    public static class Builder
        extends BaseCommand.Builder<Command, Builder> {

      @Override
      Command.Builder self() {
        return this;
      }

      @Override
      protected Command makeCommand() {
        // If printing versions ignore everything else.
        if (isPrintHelp()) {
          return new Command(isPrintHelp());
        }
        return new Command(getAppBuilder().build());
      }
    }

    static final String USAGE_MESSAGE = String.join("\n", ImmutableList.of(
        "Usage: dexsegments [options] <input-files>",
        " where <input-files> are dex files",
        "  --version               # Print the version of r8.",
        "  --help                  # Print this message."));

    public static Command.Builder builder() {
      return new Command.Builder();
    }

    public static Command.Builder parse(String[] args) {
      Command.Builder builder = builder();
      parse(args, builder);
      return builder;
    }

    private static void parse(String[] args, Command.Builder builder) {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i].trim();
        if (arg.length() == 0) {
          continue;
        } else if (arg.equals("--help")) {
          builder.setPrintHelp(true);
        } else {
          if (arg.startsWith("--")) {
            builder.getReporter().error(new StringDiagnostic("Unknown option: " + arg,
                CommandLineOrigin.INSTANCE));
          }
          builder.addProgramFiles(Paths.get(arg));
        }
      }
    }

    private Command(AndroidApp inputApp) {
      super(inputApp);
    }

    private Command(boolean printHelp) {
      super(printHelp, false);
    }

    @Override
    InternalOptions getInternalOptions() {
      return new InternalOptions();
    }
  }

  public static void main(String[] args)
      throws IOException, CompilationFailedException, ResourceException {
    Command.Builder builder = Command.parse(args);
    Command command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(Command.USAGE_MESSAGE);
      return;
    }
    AndroidApp app = command.getInputApp();

    Map<String, SegmentInfo> result = new LinkedHashMap<>();
    // Fill the results with all benchmark items otherwise golem may report missing benchmarks.
    int[] benchmarks =
        new int[] {
          Constants.TYPE_ENCODED_ARRAY_ITEM,
          Constants.TYPE_HEADER_ITEM,
          Constants.TYPE_DEBUG_INFO_ITEM,
          Constants.TYPE_FIELD_ID_ITEM,
          Constants.TYPE_ANNOTATION_SET_REF_LIST,
          Constants.TYPE_STRING_ID_ITEM,
          Constants.TYPE_MAP_LIST,
          Constants.TYPE_PROTO_ID_ITEM,
          Constants.TYPE_METHOD_ID_ITEM,
          Constants.TYPE_TYPE_ID_ITEM,
          Constants.TYPE_STRING_DATA_ITEM,
          Constants.TYPE_CLASS_DATA_ITEM,
          Constants.TYPE_TYPE_LIST,
          Constants.TYPE_ANNOTATIONS_DIRECTORY_ITEM,
          Constants.TYPE_ANNOTATION_ITEM,
          Constants.TYPE_ANNOTATION_SET_ITEM,
          Constants.TYPE_CLASS_DEF_ITEM
        };
    for (int benchmark : benchmarks) {
      result.computeIfAbsent(DexSection.typeName(benchmark), (key) -> new SegmentInfo());
    }
    try (Closer closer = Closer.create()) {
      for (ProgramResource resource : app.computeAllProgramResources()) {
        if (resource.getKind() == Kind.DEX) {
          for (DexSection dexSection :
              DexParser.parseMapFrom(
                  closer.register(resource.getByteStream()), resource.getOrigin())) {
            SegmentInfo info =
                result.computeIfAbsent(dexSection.typeName(), (key) -> new SegmentInfo());
            info.increment(dexSection.length, dexSection.size());
          }
        }
      }
    }
    System.out.println("Segments in dex application (name: size / items):");
    // This output is parsed by tools/test_framework.py. Check the parsing there when updating.
    result.forEach(
        (key, value) -> System.out.println(" - " + key + ": " + value.size + " / " + value.items));
  }

  private static class SegmentInfo {
    private int items;
    private int size;

    SegmentInfo() {
      this.items = 0;
      this.size = 0;
    }

    void increment(int items, int size) {
      this.items += items;
      this.size += size;
    }
  }
}
