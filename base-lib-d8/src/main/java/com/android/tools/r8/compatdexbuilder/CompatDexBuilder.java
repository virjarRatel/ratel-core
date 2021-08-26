// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.compatdexbuilder;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.CompatDxHelper;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class CompatDexBuilder {

  private static class DexConsumer extends DexIndexedConsumer.ForwardingConsumer {

    byte[] bytes;

    public DexConsumer() {
      super(null);
    }

    @Override
    public synchronized void accept(
        int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
      super.accept(fileIndex, data, descriptors, handler);
      assert bytes == null;
      bytes = data.copyByteData();
    }

    byte[] getBytes() {
      return bytes;
    }
  }

  private String input = null;
  private String output = null;
  private int numberOfThreads = 8;
  private boolean noLocals = false;

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    new CompatDexBuilder().run(args);
  }

  private void run(String[] args) throws IOException, InterruptedException, ExecutionException {
    List<String> flags = new ArrayList<>();

    for (String arg : args) {
      if (arg.startsWith("@")) {
        flags.addAll(Files.readAllLines(Paths.get(arg.substring(1))));
      } else {
        flags.add(arg);
      }
    }

    for (int i = 0; i < flags.size(); i++) {
      String flag = flags.get(i);
      if (flag.startsWith("--positions=")) {
        String positionsValue = flag.substring("--positions=".length());
        if (positionsValue.startsWith("throwing") || positionsValue.startsWith("important")) {
          noLocals = true;
        }
        continue;
      }
      if (flag.startsWith("--num-threads=")) {
        numberOfThreads = Integer.parseInt(flag.substring("--num-threads=".length()));
        continue;
      }
      switch (flag) {
        case "--input_jar":
          input = flags.get(++i);
          break;
        case "--output_zip":
          output = flags.get(++i);
          break;
        case "--verify-dex-file":
        case "--no-verify-dex-file":
        case "--show_flags":
        case "--no-optimize":
        case "--nooptimize":
        case "--help":
          // Ignore
          break;
        case "--nolocals":
          noLocals = true;
          break;
        default:
          System.err.println("Unsupported option: " + flag);
          System.exit(1);
      }
    }

    if (input == null) {
      System.err.println("No input jar specified");
      System.exit(1);
    }

    if (output == null) {
      System.err.println("No output jar specified");
      System.exit(1);
    }

    ExecutorService executor = ThreadUtils.getExecutorService(numberOfThreads);
    try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(Paths.get(output)))) {

      List<ZipEntry> toDex = new ArrayList<>();

      try (ZipFile zipFile = new ZipFile(input, StandardCharsets.UTF_8)) {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          if (!entry.getName().endsWith(".class")) {
            try (InputStream stream = zipFile.getInputStream(entry)) {
              addEntry(entry.getName(), stream, out);
            }
          } else {
            toDex.add(entry);
          }
        }

        List<Future<DexConsumer>> futures = new ArrayList<>(toDex.size());
        for (int i = 0; i < toDex.size(); i++) {
          ZipEntry classEntry = toDex.get(i);
          futures.add(executor.submit(() -> dexEntry(zipFile, classEntry, executor)));
        }
        for (int i = 0; i < futures.size(); i++) {
          ZipEntry entry = toDex.get(i);
          DexConsumer consumer = futures.get(i).get();
          addEntry(entry.getName() + ".dex", consumer.getBytes(), out);
        }
      }
    } finally {
      executor.shutdown();
    }
  }

  private DexConsumer dexEntry(ZipFile zipFile, ZipEntry classEntry, ExecutorService executor)
      throws IOException, CompilationFailedException {
    DexConsumer consumer = new DexConsumer();
    D8Command.Builder builder = D8Command.builder();
    CompatDxHelper.ignoreDexInArchive(builder);
    builder
        .setProgramConsumer(consumer)
        .setMode(noLocals ? CompilationMode.RELEASE : CompilationMode.DEBUG)
        .setMinApiLevel(AndroidApiLevel.H_MR2.getLevel())
        .setDisableDesugaring(true);
    try (InputStream stream = zipFile.getInputStream(classEntry)) {
      builder.addClassProgramData(
          ByteStreams.toByteArray(stream),
          new ArchiveEntryOrigin(
              classEntry.getName(), new PathOrigin(Paths.get(zipFile.getName()))));
    }
    D8.run(builder.build(), executor);
    return consumer;
  }

  private static void addEntry(String name, InputStream stream, ZipOutputStream out)
      throws IOException {
    addEntry(name, ByteStreams.toByteArray(stream), out);
  }

  private static void addEntry(String name, byte[] bytes, ZipOutputStream out) throws IOException {
    ZipEntry zipEntry = new ZipEntry(name);
    CRC32 crc32 = new CRC32();
    crc32.update(bytes);
    zipEntry.setSize(bytes.length);
    zipEntry.setMethod(ZipEntry.STORED);
    zipEntry.setCrc(crc32.getValue());
    zipEntry.setTime(0);
    out.putNextEntry(zipEntry);
    out.write(bytes);
    out.closeEntry();
  }
}
