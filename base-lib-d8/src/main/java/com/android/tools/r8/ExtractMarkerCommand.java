// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.errors.CompilationError;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ExtractMarkerCommand {

  public static class Builder {
    private boolean printHelp = false;
    private boolean includeOther = true;
    private boolean verbose;
    private boolean summary;
    private boolean csv;
    private final List<Path> programFiles = new ArrayList<>();

    public Builder setPrintHelp(boolean printHelp) {
      this.printHelp = printHelp;
      return this;
    }

    public boolean isPrintHelp() {
      return printHelp;
    }

    public Builder setIncludeOther(boolean includeOther) {
      this.includeOther = includeOther;
      return this;
    }

    public Builder setVerbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public Builder setSummary(boolean summary) {
      this.summary = summary;
      return this;
    }

    public Builder setCSV(boolean csv) {
      this.csv = csv;
      return this;
    }

    public Builder addProgramFile(Path programFile) {
      programFiles.add(programFile);
      return this;
    }

    public ExtractMarkerCommand build() throws IOException {
      // If printing versions ignore everything else.
      if (isPrintHelp()) {
        return new ExtractMarkerCommand(isPrintHelp());
      }
      return new ExtractMarkerCommand(includeOther, verbose, summary, csv, programFiles);
    }
  }

  static final String USAGE_MESSAGE = String.join("\n", ImmutableList.of(
      "Usage: extractmarker [options] <input-files>",
      " where <input-files> are dex or vdex files",
      "  --no-other              # Only show information for D8 or R8 processed files.",
      "  --verbose               # More verbose output.",
      "  --summary               # Print summary at the end.",
      "  --csv                   # Output in CSV format.",
      "  --help                  # Print this message."));

  public static Builder builder() {
    return new Builder();
  }

  public static Builder parse(String[] args) throws IOException {
    Builder builder = builder();
    parse(args, builder);
    return builder;
  }

  private static void parse(String[] args, Builder builder) throws IOException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i].trim();
      if (arg.length() == 0) {
        continue;
      } else if (arg.equals("--no-other")) {
        builder.setIncludeOther(false);
      } else if (arg.equals("--verbose")) {
        builder.setVerbose(true);
      } else if (arg.equals("--summary")) {
        builder.setSummary(true);
      } else if (arg.equals("--csv")) {
        builder.setCSV(true);
      } else if (arg.equals("--help")) {
        builder.setPrintHelp(true);
      } else {
        if (arg.startsWith("--")) {
          throw new CompilationError("Unknown option: " + arg);
        }
        builder.addProgramFile(Paths.get(arg));
      }
    }
  }

  private final boolean printHelp;
  private final boolean includeOther;
  private final boolean verbose;
  private final boolean summary;
  private final boolean csv;
  private final List<Path> programFiles;

  private ExtractMarkerCommand(boolean includeOther, boolean verbose, boolean summary,
      boolean csv, List<Path> programFiles) {
    this.printHelp = false;
    this.includeOther = includeOther;
    this.verbose = verbose;
    this.summary = summary;
    this.csv = csv;
    this.programFiles = programFiles;
  }

  private ExtractMarkerCommand(boolean printHelp) {
    this.printHelp = printHelp;
    this.includeOther = true;
    this.verbose = false;
    this.summary = false;
    this.csv = false;
    programFiles = ImmutableList.of();
  }

  public boolean isPrintHelp() {
    return printHelp;
  }

  public List<Path> getProgramFiles() {
    return programFiles;
  }

  public boolean getIncludeOther() {
    return includeOther;
  }

  public boolean getVerbose() {
    return verbose;
  }

  public boolean getSummary() {
    return summary;
  }

  public boolean getCSV() {
    return csv;
  }
}
