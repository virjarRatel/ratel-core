// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isArchive;

import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.Keep;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.ProgramResourceProvider;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.shaking.FilteredClassPath;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@Keep // TODO(b/121121779) Remove keep-annotation.
public class ArchiveResourceProvider implements ProgramResourceProvider, DataResourceProvider {

  private final Origin origin;
  private final FilteredClassPath archive;
  private final boolean ignoreDexInArchive;

  public static ArchiveResourceProvider fromArchive(Path archive, boolean ignoreDexInArchive) {
    return new ArchiveResourceProvider(FilteredClassPath.unfiltered(archive), ignoreDexInArchive);
  }

  ArchiveResourceProvider(FilteredClassPath archive, boolean ignoreDexInArchive) {
    assert isArchive(archive.getPath());
    origin = new PathOrigin(archive.getPath());
    this.archive = archive;
    this.ignoreDexInArchive = ignoreDexInArchive;
  }

  private List<ProgramResource> readArchive() throws IOException {
    List<ProgramResource> dexResources = new ArrayList<>();
    List<ProgramResource> classResources = new ArrayList<>();
    try (ZipFile zipFile =
        FileUtils.createZipFile(archive.getPath().toFile(), StandardCharsets.UTF_8)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream stream = zipFile.getInputStream(entry)) {
          String name = entry.getName();
          Origin entryOrigin = new ArchiveEntryOrigin(name, origin);
          if (archive.matchesFile(name)) {
            if (ZipUtils.isDexFile(name)) {
              if (!ignoreDexInArchive) {
                ProgramResource resource =
                    OneShotByteResource.create(
                        Kind.DEX, entryOrigin, ByteStreams.toByteArray(stream), null);
                dexResources.add(resource);
              }
            } else if (ZipUtils.isClassFile(name)) {
              String descriptor = DescriptorUtils.guessTypeDescriptor(name);
              ProgramResource resource =
                  OneShotByteResource.create(
                      Kind.CF,
                      entryOrigin,
                      ByteStreams.toByteArray(stream),
                      Collections.singleton(descriptor));
              classResources.add(resource);
            }
          }
        }
      }
    } catch (ZipException e) {
      throw new CompilationError(
          "Zip error while reading '" + archive + "': " + e.getMessage(), e);
    }
    if (!dexResources.isEmpty() && !classResources.isEmpty()) {
      throw new CompilationError(
          "Cannot create android app from an archive '" + archive
              + "' containing both DEX and Java-bytecode content");
    }
    return !dexResources.isEmpty() ? dexResources : classResources;
  }

  @Override
  public Collection<ProgramResource> getProgramResources() throws ResourceException {
    try {
      return readArchive();
    } catch (IOException e) {
      throw new ResourceException(origin, e);
    }
  }

  @Override
  public DataResourceProvider getDataResourceProvider() {
    return this;
  }

  @Override
  public void accept(Visitor resourceBrowser) throws ResourceException {
    try (ZipFile zipFile =
        FileUtils.createZipFile(archive.getPath().toFile(), StandardCharsets.UTF_8)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        if (archive.matchesFile(name) && !isProgramResourceName(name)) {
          if (entry.isDirectory()) {
            resourceBrowser.visit(DataDirectoryResource.fromZip(zipFile, entry));
          } else {
            resourceBrowser.visit(DataEntryResource.fromZip(zipFile, entry));
          }
        }
      }
    } catch (ZipException e) {
      throw new ResourceException(origin, new CompilationError(
          "Zip error while reading '" + archive + "': " + e.getMessage(), e));
    } catch (IOException e) {
      throw new ResourceException(origin, new CompilationError(
          "I/O exception while reading '" + archive + "': " + e.getMessage(), e));
    }
  }

  private boolean isProgramResourceName(String name) {
    return ZipUtils.isClassFile(name) || (ZipUtils.isDexFile(name) && !ignoreDexInArchive);
  }
}
