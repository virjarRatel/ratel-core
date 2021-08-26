// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dexfilemerger;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.DexFileMergerHelper;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.OptionsParsing;
import com.android.tools.r8.utils.OptionsParsing.ParseContext;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.ZipUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DexFileMerger {
  /** File name prefix of a {@code .dex} file automatically loaded in an archive. */
  private static final String DEX_PREFIX = "classes";

  private static final String DEFAULT_OUTPUT_ARCHIVE_FILENAME = "classes.dex.jar";

  private static final boolean PRINT_ARGS = false;

  /** Strategies for outputting multiple {@code .dex} files supported by {@link DexFileMerger}. */
  private enum MultidexStrategy {
    /** Create exactly one .dex file. The operation will fail if .dex limits are exceeded. */
    OFF,
    /** Create exactly one &lt;prefixN&gt;.dex file with N taken from the (single) input archive. */
    GIVEN_SHARD,
    /**
     * Assemble .dex files similar to {@link com.android.dx.command.dexer.Main dx}, with all but one
     * file as large as possible.
     */
    MINIMAL,
    /**
     * Allow some leeway and sometimes use additional .dex files to speed up processing. This option
     * exists to give flexibility but it often (or always) may be identical to {@link #MINIMAL}.
     */
    BEST_EFFORT;

    public boolean isMultidexAllowed() {
      switch (this) {
        case OFF:
        case GIVEN_SHARD:
          return false;
        case MINIMAL:
        case BEST_EFFORT:
          return true;
      }
      throw new AssertionError("Unknown: " + this);
    }

    public static MultidexStrategy parse(String value) {
      switch (value) {
        case "off":
          return OFF;
        case "given_shard":
          return GIVEN_SHARD;
        case "minimal":
          return MINIMAL;
        case "best_effort":
          return BEST_EFFORT;
        default:
          throw new RuntimeException(
              "Multidex argument must be either 'off', 'given_shard', 'minimal' or 'best_effort'.");
      }
    }
  }

  private static class Options {
    List<String> inputArchives = new ArrayList<>();
    String outputArchive = DEFAULT_OUTPUT_ARCHIVE_FILENAME;
    MultidexStrategy multidexMode = MultidexStrategy.OFF;
    String mainDexListFile = null;
    boolean minimalMainDex = false;
    boolean verbose = false;
    String dexPrefix = DEX_PREFIX;
  }


  private static Options parseArguments(String[] args) throws IOException {
    // We may have a single argument which is a parameter file path, prefixed with '@'.
    if (args.length == 1 && args[0].startsWith("@")) {
      // TODO(tamaskenez) Implement more sophisticated processing
      // which is aligned with Blaze's
      // com.google.devtools.common.options.ShellQuotedParamsFilePreProcessor
      Path paramsFile = Paths.get(args[0].substring(1));
      List<String> argsList = new ArrayList<>();
      for (String s : Files.readAllLines(paramsFile)) {
        s = s.trim();
        if (s.isEmpty()) {
          continue;
        }
        // Trim optional enclosing single quotes. Unescaping omitted for now.
        if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
          s = s.substring(1, s.length() - 1);
        }
        argsList.add(s);
      }
      args = argsList.toArray(new String[argsList.size()]);
    }

    Options options = new Options();
    ParseContext context = new ParseContext(args);
    List<String> strings;
    String string;
    Boolean b;
    while (context.head() != null) {
      if (context.head().startsWith("@")) {
        throw new RuntimeException("A params file must be the only argument: " + context.head());
      }
      strings = OptionsParsing.tryParseMulti(context, "--input");
      if (strings != null) {
        options.inputArchives.addAll(strings);
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--output", "-o");
      if (string != null) {
        options.outputArchive = string;
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--multidex", null);
      if (string != null) {
        options.multidexMode = MultidexStrategy.parse(string);
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--main-dex-list", null);
      if (string != null) {
        options.mainDexListFile = string;
        continue;
      }
      b = OptionsParsing.tryParseBoolean(context, "--minimal-main-dex");
      if (b != null) {
        options.minimalMainDex = b;
        continue;
      }
      b = OptionsParsing.tryParseBoolean(context, "--verbose");
      if (b != null) {
        options.verbose = b;
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--max-bytes-wasted-per-file", null);
      if (string != null) {
        System.err.println("Warning: '--max-bytes-wasted-per-file' is ignored.");
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--set-max-idx-number", null);
      if (string != null) {
        System.err.println("Warning: The '--set-max-idx-number' option is ignored.");
        continue;
      }
      b = OptionsParsing.tryParseBoolean(context, "--forceJumbo");
      if (b != null) {
        System.err.println(
            "Warning: '--forceJumbo' can be safely omitted. Strings will only use "
                + "jumbo-string indexing if necessary.");
        continue;
      }
      string = OptionsParsing.tryParseSingle(context, "--dex_prefix", null);
      if (string != null) {
        options.dexPrefix = string;
        continue;
      }
      throw new RuntimeException(String.format("Unknown options: '%s'.", context.head()));
    }
    return options;
  }

  /**
   * Implements a DexIndexedConsumer writing into a ZipStream with support for custom dex file name
   * prefix, reindexing a single dex output file to a nonzero index and reporting if any data has
   * been written.
   */
  private static class ArchiveConsumer implements DexIndexedConsumer {
    private final Path path;
    private final String prefix;
    private final Integer singleFixedFileIndex;
    private final Origin origin;
    private ZipOutputStream stream = null;

    private int highestIndexWritten = -1;
    private final Map<Integer, Runnable> writers = new TreeMap<>();
    private boolean hasWrittenSomething = false;

    /** If singleFixedFileIndex is not null then we expect only one output dex file */
    private ArchiveConsumer(Path path, String prefix, Integer singleFixedFileIndex) {
      this.path = path;
      this.prefix = prefix;
      this.singleFixedFileIndex = singleFixedFileIndex;
      this.origin = new PathOrigin(path);
    }

    private boolean hasWrittenSomething() {
      return hasWrittenSomething;
    }

    private String getDexFileName(int fileIndex) {
      if (singleFixedFileIndex != null) {
        fileIndex = singleFixedFileIndex;
      }
      return prefix + (fileIndex == 0 ? "" : (fileIndex + 1)) + FileUtils.DEX_EXTENSION;
    }

    @Override
    public synchronized void accept(
        int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
      if (singleFixedFileIndex != null && fileIndex != 0) {
        handler.error(new StringDiagnostic("Result does not fit into a single dex file."));
        return;
      }
      // Make a copy of the actual bytes as they will possibly be accessed later by the runner.
      final byte[] bytes = data.copyByteData();
      writers.put(fileIndex, () -> writeEntry(fileIndex, bytes, descriptors, handler));

      while (writers.containsKey(highestIndexWritten + 1)) {
        ++highestIndexWritten;
        writers.get(highestIndexWritten).run();
        writers.remove(highestIndexWritten);
      }
    }

    /** Get or open the zip output stream. */
    private synchronized ZipOutputStream getStream(DiagnosticsHandler handler) {
      if (stream == null) {
        try {
          stream =
              new ZipOutputStream(
                  Files.newOutputStream(
                      path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
        } catch (IOException e) {
          handler.error(new ExceptionDiagnostic(e, origin));
        }
      }
      return stream;
    }

    private void writeEntry(
        int fileIndex, byte[] data, Set<String> descriptors, DiagnosticsHandler handler) {
      try {
        ZipUtils.writeToZipStream(
            getStream(handler),
            getDexFileName(fileIndex),
            ByteDataView.of(data),
            ZipEntry.DEFLATED);
        hasWrittenSomething = true;
      } catch (IOException e) {
        handler.error(new ExceptionDiagnostic(e, origin));
      }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      if (!writers.isEmpty()) {
        handler.error(
            new StringDiagnostic(
                "Failed to write zip, for a multidex output some of the classes.dex files were"
                    + " not produced."));
      }
      try {
        if (stream != null) {
          stream.close();
          stream = null;
        }
      } catch (IOException e) {
        handler.error(new ExceptionDiagnostic(e, origin));
      }
    }
  }

  private static int parseFileIndexFromShardFilename(String inputArchive) {
    Pattern namingPattern = Pattern.compile("([0-9]+)\\..*");
    String name = new File(inputArchive).getName();
    Matcher matcher = namingPattern.matcher(name);
    if (!matcher.matches()) {
      throw new RuntimeException(
          String.format(
              "Expect input named <N>.xxx.zip for --multidex=given_shard but got %s.", name));
    }
    int shard = Integer.parseInt(matcher.group(1));
    if (shard <= 0) {
      throw new RuntimeException(
          String.format("Expect positive N in input named <N>.xxx.zip but got %d.", shard));
    }
    return shard;
  }

  public static void run(String[] args) throws CompilationFailedException, IOException {
    Options options = parseArguments(args);

    if (options.inputArchives.isEmpty()) {
      throw new RuntimeException("Need at least one --input");
    }

    if (options.mainDexListFile != null && options.inputArchives.size() != 1) {
      throw new RuntimeException(
          "--main-dex-list only supported with exactly one --input, use DexFileSplitter for more");
    }

    if (!options.multidexMode.isMultidexAllowed()) {
      if (options.mainDexListFile != null) {
        throw new RuntimeException(
            "--main-dex-list is only supported with multidex enabled, but mode is: "
                + options.multidexMode.toString());
      }
      if (options.minimalMainDex) {
        throw new RuntimeException(
            "--minimal-main-dex is only supported with multidex enabled, but mode is: "
                + options.multidexMode.toString());
      }
    }

    D8Command.Builder builder = D8Command.builder();

    Map<String, Integer> inputOrdering = new HashMap<>(options.inputArchives.size());
    int sequenceNumber = 0;
    for (String s : options.inputArchives) {
      builder.addProgramFiles(Paths.get(s));
      inputOrdering.put(s, sequenceNumber++);
    }

    // Determine enabling multidexing and file indexing.
    Integer singleFixedFileIndex = null;
    switch (options.multidexMode) {
      case OFF:
        singleFixedFileIndex = 0;
        break;
      case GIVEN_SHARD:
        if (options.inputArchives.size() != 1) {
          throw new RuntimeException("'--multidex=given_shard' requires exactly one --input.");
        }
        singleFixedFileIndex = parseFileIndexFromShardFilename(options.inputArchives.get(0)) - 1;
        break;
      case MINIMAL:
      case BEST_EFFORT:
        // Nothing to do.
        break;
      default:
        throw new Unreachable("Unexpected enum: " + options.multidexMode);
    }

    if (options.mainDexListFile != null) {
      builder.addMainDexListFiles(Paths.get(options.mainDexListFile));
    }

    ArchiveConsumer consumer =
        new ArchiveConsumer(
            Paths.get(options.outputArchive), options.dexPrefix, singleFixedFileIndex);
    builder.setProgramConsumer(consumer);

    DexFileMergerHelper.run(builder.build(), options.minimalMainDex, inputOrdering);

    // If input was empty we still need to write out an empty zip.
    if (!consumer.hasWrittenSomething()) {
      File f = new File(options.outputArchive);
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
      out.close();
    }
  }

  public static void main(String[] args) {
    try {
      if (PRINT_ARGS) {
        printArgs(args);
      }
      run(args);
    } catch (CompilationFailedException | IOException e) {
      System.err.println("Merge failed: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void printArgs(String[] args) {
    System.err.print("r8.DexFileMerger");
    for (String s : args) {
      System.err.printf(" %s", s);
    }
    System.err.println("");
  }
}
