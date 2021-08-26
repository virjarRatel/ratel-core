// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isArchive;

import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.ProgramResourceProvider;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
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
import java.util.zip.ZipInputStream;

public class AarArchiveResourceProvider implements ProgramResourceProvider {

  private final Origin origin;
  private final Path archive;

  public static AarArchiveResourceProvider fromArchive(Path archive) {
    return new AarArchiveResourceProvider(archive);
  }

  AarArchiveResourceProvider(Path archive) {
    assert isArchive(archive);
    origin = new ArchiveEntryOrigin("classes.jar", new PathOrigin(archive));
    this.archive = archive;
  }

  private List<ProgramResource> readClassesJar(ZipInputStream stream) throws IOException {
    ZipEntry entry;
    List<ProgramResource> resources = new ArrayList<>();
    while (null != (entry = stream.getNextEntry())) {
      String name = entry.getName();
      if (ZipUtils.isClassFile(name)) {
        Origin entryOrigin = new ArchiveEntryOrigin(name, origin);
        String descriptor = DescriptorUtils.guessTypeDescriptor(name);
        ProgramResource resource =
            OneShotByteResource.create(
                Kind.CF,
                entryOrigin,
                ByteStreams.toByteArray(stream),
                Collections.singleton(descriptor));
        resources.add(resource);
      }
    }
    return resources;
  }

  private List<ProgramResource> readArchive() throws IOException {
    List<ProgramResource> classResources = null;
    try (ZipFile zipFile = FileUtils.createZipFile(archive.toFile(), StandardCharsets.UTF_8)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream stream = zipFile.getInputStream(entry)) {
          String name = entry.getName();
          if (name.equals("classes.jar")) {
            try (ZipInputStream classesStream = new ZipInputStream(stream)) {
              classResources = readClassesJar(classesStream);
            }
            break;
          }
        }
      }
    } catch (ZipException e) {
      throw new CompilationError("Zip error while reading '" + archive + "': " + e.getMessage(), e);
    }
    return classResources == null ? Collections.emptyList() : classResources;
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
    return null;
  }
}
