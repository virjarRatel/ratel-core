// Copyright (c) 2017, the Rex project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.dex.VDexParser;
import com.android.tools.r8.dex.VDexReader;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

@Keep
public class ExtractMarker {
  public static class VdexOrigin extends Origin {

    private final int index;

    public VdexOrigin(Origin vdexOrigin, int index) {
      super(vdexOrigin);
      this.index = index;
    }

    @Override
    public String part() {
      return Integer.toString(index);
    }
  }

  public static Collection<Marker> extractMarkerFromDexFile(Path file)
      throws IOException, ExecutionException, ResourceException {
    AndroidApp.Builder appBuilder = AndroidApp.builder();
    addDexResources(appBuilder, file);
    return extractMarker(appBuilder.build());
  }

  public static int extractDexSize(Path file) throws IOException, ResourceException {
    AndroidApp.Builder appBuilder = AndroidApp.builder();
    addDexResources(appBuilder, file);
    int size = 0;
    for (ProgramResource resource : appBuilder.build().computeAllProgramResources()) {
      if (resource.getKind() == Kind.DEX) {
        try (InputStream input = resource.getByteStream()) {
          size += ByteStreams.toByteArray(input).length;
        }
      }
    }
    return size;
  }

  public static Collection<Marker> extractMarkerFromDexProgramData(byte[] data)
      throws IOException, ExecutionException {
    AndroidApp app = AndroidApp.builder().addDexProgramData(data, Origin.unknown()).build();
    return extractMarker(app);
  }

  public static Collection<Marker> extractMarkerFromClassProgramData(byte[] data)
      throws IOException, ExecutionException {
    AndroidApp app = AndroidApp.builder().addClassProgramData(data, Origin.unknown()).build();
    return extractMarker(app);
  }

  private static void addDexResources(AndroidApp.Builder appBuilder, Path file)
      throws IOException, ResourceException {
    if (FileUtils.isVDexFile(file)) {
      PathOrigin vdexOrigin = new PathOrigin(file);
      try (InputStream fileInputStream = Files.newInputStream(file)) {
        VDexReader vdexReader = new VDexReader(vdexOrigin, fileInputStream);
        VDexParser vDexParser = new VDexParser(vdexReader);
        int index = 0;
        for (byte[] bytes : vDexParser.getDexFiles()) {
          appBuilder.addDexProgramData(bytes, new VdexOrigin(vdexOrigin, index));
          index++;
        }
      }
    } else {
      appBuilder.addProgramFiles(file);
    }
  }

  private static Collection<Marker> extractMarker(AndroidApp app)
      throws IOException, ExecutionException {
    InternalOptions options = new InternalOptions();
    options.skipReadingDexCode = true;
    options.minApiLevel = AndroidApiLevel.P.getLevel();
    DexApplication dexApp =
        new ApplicationReader(app, options, new Timing("ExtractMarker")).read();
    return dexApp.dexItemFactory.extractMarkers();
  }

  public static void main(String[] args)
      throws IOException, ExecutionException, ResourceException {
    ExtractMarkerCommand.Builder builder = ExtractMarkerCommand.parse(args);
    ExtractMarkerCommand command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(ExtractMarkerCommand.USAGE_MESSAGE);
      return;
    }

    Path cwd = Paths.get(System.getProperty("user.dir"));
    // Dex code is not needed for getting the marker. VDex files typically contains quickened byte
    // codes which cannot be read, and we want to get the marker from vdex files as well.
    int d8Count = 0;
    int r8Count = 0;
    int otherCount = 0;
    for (Path programFile : command.getProgramFiles()) {
      Collection<Marker> markers;
      try {
        markers = extractMarkerFromDexFile(programFile);
      } catch (CompilationError e) {
        System.out.println(
            "Failed to read dex/vdex file `" + programFile + "`: '" + e.getMessage() + "'");
        continue;
      }
      System.out.print("In file: " + cwd.toAbsolutePath().relativize(programFile.toAbsolutePath()));
      System.out.println(", " + extractDexSize(programFile) + " bytes:");
      for (Marker marker : markers) {
        if (marker == null) {
          otherCount++;
          if (!command.getIncludeOther()) {
            continue;
          }
        } else {
          if (marker.isD8()) {
            d8Count++;
          } else {
            r8Count++;
          }
        }
        if (command.getCSV()) {
          System.out.print("\"" + programFile + "\"");
          System.out.print(", ");
          if (marker == null) {
            System.out.print("\"no marker\"");
          } else {
            System.out.print("\"" + (marker.isD8() ? "D8" : "R8") + "\"");
          }
          System.out.print(", ");
          System.out.print(extractDexSize(programFile));
        } else {
          if (command.getVerbose()) {
            System.out.print(programFile);
            System.out.print(": ");
          }
          System.out.print(marker == null ? "D8/R8 marker not found" : marker);
        }
        System.out.println();
      }
    }
    if (command.getSummary()) {
      System.out.println("D8: " + d8Count);
      System.out.println("R8: " + r8Count);
      System.out.println("Other: " + otherCount);
      System.out.println("Total: " + (d8Count + r8Count + otherCount));
    }
  }
}
