// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.isArchive;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Internal-only provider for providing class files when clients use the addClasspathFiles and
 * addLibraryFiles API.
 *
 * <p>The purpose of the internal provider is that for the API use above we know it is safe to use
 * the zip-file descriptor throughout compilation and close at the end of reading. It must also be
 * safe to reopen it as currently our own tests reuse AndroidApp structures.
 */
class InternalArchiveClassFileProvider implements ClassFileResourceProvider, AutoCloseable {
  private final Path path;
  private final Origin origin;
  private final Set<String> descriptors = new HashSet<>();

  private ZipFile openedZipFile = null;

  /**
   * Creates a lazy class-file program-resource provider.
   *
   * @param archive Zip archive to provide resources from.
   */
  public InternalArchiveClassFileProvider(Path archive) throws IOException {
    this(archive, entry -> true);
  }

  /**
   * Creates a lazy class-file program-resource provider with an include filter.
   *
   * @param archive Zip archive to provide resources from.
   * @param include Predicate deciding if a given class-file entry should be provided.
   */
  public InternalArchiveClassFileProvider(Path archive, Predicate<String> include)
      throws IOException {
    assert isArchive(archive);
    path = archive;
    origin = new PathOrigin(archive);
    final Enumeration<? extends ZipEntry> entries = getOpenZipFile().entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      String name = entry.getName();
      if (ZipUtils.isClassFile(name) && include.test(name)) {
        descriptors.add(DescriptorUtils.guessTypeDescriptor(name));
      }
    }
  }

  @Override
  public Set<String> getClassDescriptors() {
    return Collections.unmodifiableSet(descriptors);
  }

  @Override
  public ProgramResource getProgramResource(String descriptor) {
    if (!descriptors.contains(descriptor)) {
      return null;
    }
    try {
      ZipEntry zipEntry = getZipEntryFromDescriptor(descriptor);
      try (InputStream inputStream = getOpenZipFile().getInputStream(zipEntry)) {
        return ProgramResource.fromBytes(
            new ArchiveEntryOrigin(zipEntry.getName(), origin),
            Kind.CF,
            ByteStreams.toByteArray(inputStream),
            Collections.singleton(descriptor));
      }
    } catch (IOException e) {
      throw new CompilationError("Failed to read '" + descriptor, origin);
    }
  }

  private ZipFile getOpenZipFile() throws IOException {
    if (openedZipFile == null) {
      try {
        openedZipFile = FileUtils.createZipFile(path.toFile(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        if (!Files.exists(path)) {
          throw new NoSuchFileException(path.toString());
        } else {
          throw e;
        }
      }
    }
    return openedZipFile;
  }

  @Override
  public void close() throws IOException {
    openedZipFile.close();
    openedZipFile = null;
  }

  private ZipEntry getZipEntryFromDescriptor(String descriptor) throws IOException {
    return getOpenZipFile()
        .getEntry(descriptor.substring(1, descriptor.length() - 1) + CLASS_EXTENSION);
  }
}
