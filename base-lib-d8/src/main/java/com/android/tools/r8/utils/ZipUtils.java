// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.DEX_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.MODULE_INFO_CLASS;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

  public static void writeResourcesToZip(
      List<ProgramResource> resources,
      Set<DataEntryResource> dataResources,
      Closer closer,
      ZipOutputStream out)
      throws IOException, ResourceException {
    for (ProgramResource resource : resources) {
      assert resource.getClassDescriptors().size() == 1;
      Iterator<String> iterator = resource.getClassDescriptors().iterator();
      String className = iterator.next();
      String entryName = DescriptorUtils.getClassFileName(className);
      byte[] bytes = ByteStreams.toByteArray(closer.register(resource.getByteStream()));
      writeToZipStream(out, entryName, bytes, ZipEntry.DEFLATED);
    }
    for (DataEntryResource dataResource : dataResources) {
      String entryName = dataResource.getName();
      byte[] bytes = ByteStreams.toByteArray(closer.register(dataResource.getByteStream()));
      writeToZipStream(out, entryName, bytes, ZipEntry.DEFLATED);
    }
  }

  public interface OnEntryHandler {
    void onEntry(ZipEntry entry, InputStream input) throws IOException;
  }

  public static void iter(String zipFileStr, OnEntryHandler handler) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFileStr, StandardCharsets.UTF_8)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream entryStream = zipFile.getInputStream(entry)) {
          handler.onEntry(entry, entryStream);
        }
      }
    }
  }

  public static void zip(Path zipFile, Path inputDirectory) throws IOException {
    try (ZipOutputStream stream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      List<Path> files =
          Files.walk(inputDirectory)
              .filter(path -> !Files.isDirectory(path))
              .collect(Collectors.toList());
      for (Path path : files) {
        ZipEntry zipEntry = new ZipEntry(inputDirectory.relativize(path).toString());
        stream.putNextEntry(zipEntry);
        Files.copy(path, stream);
        stream.closeEntry();
      }
    }
  }

  public static List<File> unzip(String zipFile, File outDirectory) throws IOException {
    return unzip(zipFile, outDirectory, (entry) -> true);
  }

  public static List<File> unzip(String zipFile, File outDirectory, Predicate<ZipEntry> filter)
      throws IOException {
    final Path outDirectoryPath = outDirectory.toPath();
    final List<File> outFiles = new ArrayList<>();
      iter(zipFile, (entry, input) -> {
        String name = entry.getName();
        if (!entry.isDirectory() && filter.test(entry)) {
          if (name.contains("..")) {
            // Protect against malicious archives.
            throw new CompilationError("Invalid entry name \"" + name + "\"");
          }
          Path outPath = outDirectoryPath.resolve(name);
          File outFile = outPath.toFile();
          outFile.getParentFile().mkdirs();
          try (OutputStream output = new FileOutputStream(outFile)) {
            ByteStreams.copy(input, output);
          }
          outFiles.add(outFile);
        }
      });
    return outFiles;
  }

  public static void writeToZipStream(
      ZipOutputStream stream, String entry, byte[] content, int compressionMethod)
      throws IOException {
    writeToZipStream(stream, entry, ByteDataView.of(content), compressionMethod);
  }

  public static void writeToZipStream(
      ZipOutputStream stream, String entry, ByteDataView content, int compressionMethod)
      throws IOException {
    byte[] buffer = content.getBuffer();
    int offset = content.getOffset();
    int length = content.getLength();
    CRC32 crc = new CRC32();
    crc.update(buffer, offset, length);
    ZipEntry zipEntry = new ZipEntry(entry);
    zipEntry.setMethod(compressionMethod);
    zipEntry.setSize(length);
    zipEntry.setCrc(crc.getValue());
    zipEntry.setTime(0);
    stream.putNextEntry(zipEntry);
    stream.write(buffer, offset, length);
    stream.closeEntry();
  }

  public static boolean isDexFile(String entry) {
    String name = entry.toLowerCase();
    return name.endsWith(DEX_EXTENSION);
  }

  public static boolean isClassFile(String entry) {
    String name = entry.toLowerCase();
    if (name.endsWith(MODULE_INFO_CLASS)) {
      return false;
    }
    if (name.startsWith("meta-inf") || name.startsWith("/meta-inf")) {
      return false;
    }
    return name.endsWith(CLASS_EXTENSION);
  }
}
