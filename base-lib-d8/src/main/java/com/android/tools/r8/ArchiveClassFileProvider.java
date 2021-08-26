// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.isArchive;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.ZipUtils;
import com.google.common.io.ByteStreams;
import java.io.Closeable;
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
 * Lazy Java class file resource provider loading class files from a zip archive.
 *
 * <p>The descriptor index is built eagerly upon creating the provider and subsequent requests for
 * resources in the descriptor set will then force the read of zip entry contents.
 */
@Keep
public class ArchiveClassFileProvider implements ClassFileResourceProvider, Closeable {
  private final Origin origin;
  private final ZipFile zipFile;
  private final Set<String> descriptors = new HashSet<>();

  /**
   * Creates a lazy class-file program-resource provider.
   *
   * @param archive Zip archive to provide resources from.
   */
  public ArchiveClassFileProvider(Path archive) throws IOException {
    this(archive, entry -> true);
  }

  /**
   * Creates a lazy class-file program-resource provider with an include filter.
   *
   * @param archive Zip archive to provide resources from.
   * @param include Predicate deciding if a given class-file entry should be provided.
   */
  public ArchiveClassFileProvider(Path archive, Predicate<String> include) throws IOException {
    assert isArchive(archive);
    origin = new PathOrigin(archive);
    try {
      zipFile = FileUtils.createZipFile(archive.toFile(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      if (!Files.exists(archive)) {
        throw new NoSuchFileException(archive.toString());
      } else {
        throw e;
      }
    }
    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
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
    ZipEntry zipEntry = getZipEntryFromDescriptor(descriptor);
    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
      return ProgramResource.fromBytes(
          new ArchiveEntryOrigin(zipEntry.getName(), origin),
          Kind.CF,
          ByteStreams.toByteArray(inputStream),
          Collections.singleton(descriptor));
    } catch (IOException e) {
      throw new CompilationError("Failed to read '" + descriptor, origin);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public void close() throws IOException {
    zipFile.close();
  }

  private ZipEntry getZipEntryFromDescriptor(String descriptor) {
    return zipFile.getEntry(descriptor.substring(1, descriptor.length() - 1) + CLASS_EXTENSION);
  }
}
