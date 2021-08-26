// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.ArchiveBuilder;
import com.android.tools.r8.utils.DirectoryBuilder;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.OutputBuilder;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.ZipUtils;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Consumer for DEX encoded programs.
 *
 * <p>This consumer receives DEX file content using standard indexed-multidex for programs larger
 * than a single DEX file. This is the default consumer for DEX programs.
 */
@KeepForSubclassing
public interface DexIndexedConsumer extends ProgramConsumer, ByteBufferProvider {

  /**
   * Callback to receive DEX data for a compilation output.
   *
   * <p>This is the equivalent to writing out the files classes.dex, classes2.dex, etc., where
   * fileIndex gives the current file count (with the first file having index zero).
   *
   * <p>There is no guaranteed order and files might be written concurrently.
   *
   * <p>The consumer is expected not to throw, but instead report any errors via the diagnostics
   * {@param handler}. If an error is reported via {@param handler} and no exceptions are thrown,
   * then the compiler guaranties to exit with an error.
   *
   * <p>The {@link ByteDataView} {@param data} object can only be assumed valid during the duration
   * of the accept. If the bytes are needed beyond that, a copy must be made elsewhere.
   *
   * @param fileIndex Index of the DEX file for multi-dexing. Files are zero-indexed.
   * @param data DEX encoded data in a ByteDataView wrapper.
   * @param descriptors Class descriptors for all classes defined in the DEX data.
   * @param handler Diagnostics handler for reporting.
   */
  default void accept(
      int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
    // To avoid breaking binary compatiblity, old consumers not implementing the new API will be
    // forwarded to. New consumers must implement the accept on ByteDataView.
    accept(fileIndex, data.copyByteData(), descriptors, handler);
  }

  // Any new implementation should not use or call the deprecated accept method.
  @Deprecated
  default void accept(
      int fileIndex, byte[] data, Set<String> descriptors, DiagnosticsHandler handler) {
    handler.error(
        new StringDiagnostic("Deprecated use of DexIndexedConsumer::accept(..., byte[], ...)"));
  }

  /** Empty consumer to request the production of the resource but ignore its value. */
  static DexIndexedConsumer emptyConsumer() {
    return ForwardingConsumer.EMPTY_CONSUMER;
  }

  /** Forwarding consumer to delegate to an optional existing consumer. */
  @Keep
  class ForwardingConsumer implements DexIndexedConsumer {

    private static final DexIndexedConsumer EMPTY_CONSUMER = new ForwardingConsumer(null);

    private final DexIndexedConsumer consumer;

    public ForwardingConsumer(DexIndexedConsumer consumer) {
      this.consumer = consumer;
    }

    protected static String getDefaultDexFileName(int fileIndex) {
      return fileIndex == 0
          ? "classes" + FileUtils.DEX_EXTENSION
          : ("classes" + (fileIndex + 1) + FileUtils.DEX_EXTENSION);
    }

    protected String getDexFileName(int fileIndex) {
      return getDefaultDexFileName(fileIndex);
    }

    @Override
    public DataResourceConsumer getDataResourceConsumer() {
      return consumer != null ? consumer.getDataResourceConsumer() : null;
    }

    @Override
    public void accept(
        int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
      if (consumer != null) {
        consumer.accept(fileIndex, data, descriptors, handler);
      }
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      if (consumer != null) {
        consumer.finished(handler);
      }
    }
  }

  /** Consumer to write program resources to an output. */
  @Keep
  class ArchiveConsumer extends ForwardingConsumer
      implements DataResourceConsumer, InternalProgramOutputPathConsumer {
    protected final OutputBuilder outputBuilder;
    protected final boolean consumeDataResources;

    public ArchiveConsumer(Path archive) {
      this(archive, null, false);
    }

    public ArchiveConsumer(Path archive, boolean consumeDataResouces) {
      this(archive, null, consumeDataResouces);
    }

    public ArchiveConsumer(Path archive, DexIndexedConsumer consumer) {
      this(archive, consumer, false);
    }

    public ArchiveConsumer(Path archive, DexIndexedConsumer consumer, boolean consumeDataResouces) {
      super(consumer);
      this.outputBuilder = new ArchiveBuilder(archive);
      this.consumeDataResources = consumeDataResouces;
      this.outputBuilder.open();
      if (getDataResourceConsumer() != null) {
        this.outputBuilder.open();
      }
    }

    public Origin getOrigin() {
      return outputBuilder.getOrigin();
    }

    @Override
    public DataResourceConsumer getDataResourceConsumer() {
      return consumeDataResources ? this : null;
    }

    @Override
    public void accept(
        int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
      super.accept(fileIndex, data, descriptors, handler);
      outputBuilder.addIndexedClassFile(fileIndex, getDexFileName(fileIndex), data, handler);
    }

    @Override
    public void accept(DataDirectoryResource directory, DiagnosticsHandler handler) {
      outputBuilder.addDirectory(directory.getName(), handler);
    }

    @Override
    public void accept(DataEntryResource file, DiagnosticsHandler handler) {
      outputBuilder.addFile(file.getName(), file, handler);
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      super.finished(handler);
      outputBuilder.close(handler);
    }

    public static void writeResources(
        Path archive, List<ProgramResource> resources, Set<DataEntryResource> dataResources)
        throws IOException, ResourceException {
      OpenOption[] options =
          new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
      try (Closer closer = Closer.create()) {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive, options))) {
          for (int i = 0; i < resources.size(); i++) {
            ProgramResource resource = resources.get(i);
            String entryName = getDefaultDexFileName(i);
            byte[] bytes = ByteStreams.toByteArray(closer.register(resource.getByteStream()));
            ZipUtils.writeToZipStream(out, entryName, bytes, ZipEntry.STORED);
          }
          for (DataEntryResource dataResource : dataResources) {
            String entryName = dataResource.getName();
            byte[] bytes = ByteStreams.toByteArray(closer.register(dataResource.getByteStream()));
            ZipUtils.writeToZipStream(out, entryName, bytes, ZipEntry.STORED);
          }
        }
      }
    }

    @Override
    public Path internalGetOutputPath() {
      return outputBuilder.getPath();
    }
  }

  @Keep
  class DirectoryConsumer extends ForwardingConsumer
      implements DataResourceConsumer, InternalProgramOutputPathConsumer {
    private final Path directory;
    private boolean preparedDirectory = false;
    private final OutputBuilder outputBuilder;
    protected final boolean consumeDataResouces;

    public DirectoryConsumer(Path directory) {
      this(directory, null, false);
    }

    public DirectoryConsumer(Path directory, boolean consumeDataResouces) {
      this(directory, null, consumeDataResouces);
    }

    public DirectoryConsumer(Path directory, DexIndexedConsumer consumer) {
      this(directory, consumer, false);
    }

    public DirectoryConsumer(
        Path directory, DexIndexedConsumer consumer, boolean consumeDataResouces) {
      super(consumer);
      this.directory = directory;
      this.outputBuilder = new DirectoryBuilder(directory);
      this.consumeDataResouces = consumeDataResouces;
    }

    @Override
    public DataResourceConsumer getDataResourceConsumer() {
      return consumeDataResouces ? this : null;
    }

    @Override
    public void accept(
        int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
      super.accept(fileIndex, data, descriptors, handler);
      try {
        prepareDirectory();
      } catch (IOException e) {
        handler.error(new ExceptionDiagnostic(e, new PathOrigin(directory)));
      }
      outputBuilder.addFile(getDexFileName(fileIndex), data, handler);
    }

    @Override
    public void accept(DataDirectoryResource directory, DiagnosticsHandler handler) {
      outputBuilder.addDirectory(directory.getName(), handler);
    }

    @Override
    public void accept(DataEntryResource file, DiagnosticsHandler handler) {
      outputBuilder.addFile(file.getName(), file, handler);
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
      super.finished(handler);
      outputBuilder.close(handler);
    }

    private synchronized void prepareDirectory() throws IOException {
      if (preparedDirectory) {
        return;
      }
      preparedDirectory = true;
      deleteClassesDexFiles(directory);
    }

    static void deleteClassesDexFiles(Path directory) throws IOException {
      try (Stream<Path> filesInDir = Files.list(directory)) {
        for (Path path : filesInDir.collect(Collectors.toList())) {
          if (FileUtils.isClassesDexFile(path)) {
            Files.delete(path);
          }
        }
      }
    }

    public static void writeResources(Path directory, List<ProgramResource> resources)
        throws IOException, ResourceException {
      deleteClassesDexFiles(directory);
      try (Closer closer = Closer.create()) {
        for (int i = 0; i < resources.size(); i++) {
          ProgramResource resource = resources.get(i);
          Path target = getTargetDexFile(directory, i);
          writeFile(ByteStreams.toByteArray(closer.register(resource.getByteStream())), target);
        }
      }
    }

    private static Path getTargetDexFile(Path directory, int fileIndex) {
      return directory.resolve(ForwardingConsumer.getDefaultDexFileName(fileIndex));
    }

    private static void writeFile(byte[] contents, Path target) throws IOException {
      Files.createDirectories(target.getParent());
      FileUtils.writeToFile(target, null, contents);
    }

    @Override
    public Path internalGetOutputPath() {
      return outputBuilder.getPath();
    }
  }
}
