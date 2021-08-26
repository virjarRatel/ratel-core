// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isAarFile;
import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;
import static com.android.tools.r8.utils.FileUtils.isDexFile;
import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;
import static com.android.tools.r8.utils.ZipUtils.writeToZipStream;

import com.android.tools.r8.ClassFileConsumer;
import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DataResource;
import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.DataResourceProvider.Visitor;
import com.android.tools.r8.DexFilePerClassFileConsumer;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DirectoryClassFileProvider;
import com.android.tools.r8.OutputMode;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.ProgramResourceProvider;
import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.StringResource;
import com.android.tools.r8.Version;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.shaking.FilteredClassPath;
import com.android.tools.r8.shaking.ProguardConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassVisitor;

/**
 * Collection of program files needed for processing.
 *
 * <p>This abstraction is the main input and output container for a given application.
 */
public class AndroidApp {

  private static final String dumpVersionFileName = "r8-version";
  private static final String dumpProgramFileName = "program.jar";
  private static final String dumpClasspathFileName = "classpath.jar";
  private static final String dumpLibraryFileName = "library.jar";
  private static final String dumpConfigFileName = "proguard.config";

  private final ImmutableList<ProgramResourceProvider> programResourceProviders;
  private final ImmutableMap<Resource, String> programResourcesMainDescriptor;
  private final ImmutableList<ClassFileResourceProvider> classpathResourceProviders;
  private final ImmutableList<ClassFileResourceProvider> libraryResourceProviders;

  // List of internally added archive providers for which we must close their resources.
  private final ImmutableList<InternalArchiveClassFileProvider> archiveProvidersToClose;

  private final StringResource proguardMapOutputData;
  private final List<StringResource> mainDexListResources;
  private final List<String> mainDexClasses;

  public void closeInternalArchiveProviders() throws IOException {
    for (InternalArchiveClassFileProvider provider : archiveProvidersToClose) {
      provider.close();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    try {
      if (!programResourceProviders.isEmpty()) {
        builder.append("  Program resources:").append(System.lineSeparator());
        printProgramResourceProviders(builder, programResourceProviders);
      }
      if (!classpathResourceProviders.isEmpty()) {
        builder.append("  Classpath resources:").append(System.lineSeparator());
        printClassFileProviders(builder, classpathResourceProviders);
      }
      if (!libraryResourceProviders.isEmpty()) {
        builder.append("  Library resources:").append(System.lineSeparator());
        printClassFileProviders(builder, libraryResourceProviders);
      }
    } catch (ResourceException e) {
      e.printStackTrace();
    }
    return builder.toString();
  }

  private static void printProgramResourceProviders(
      StringBuilder builder, Collection<ProgramResourceProvider> providers)
      throws ResourceException {
    for (ProgramResourceProvider provider : providers) {
      for (ProgramResource resource : provider.getProgramResources()) {
        printProgramResource(builder, resource);
      }
    }
  }

  private static void printClassFileProviders(
      StringBuilder builder, Collection<ClassFileResourceProvider> providers) {
    for (ClassFileResourceProvider provider : providers) {
      for (String descriptor : provider.getClassDescriptors()) {
        ProgramResource resource = provider.getProgramResource(descriptor);
        printProgramResource(builder, resource);
      }
    }
  }

  private static void printProgramResource(StringBuilder builder, ProgramResource resource) {
    builder.append("    ").append(resource.getOrigin());
    Set<String> descriptors = resource.getClassDescriptors();
    if (descriptors != null && !descriptors.isEmpty()) {
      builder.append(" contains ");
      StringUtils.append(builder, descriptors);
    }
    builder.append(System.lineSeparator());
  }

  // See factory methods and AndroidApp.Builder below.
  private AndroidApp(
      ImmutableList<ProgramResourceProvider> programResourceProviders,
      ImmutableMap<Resource, String> programResourcesMainDescriptor,
      ImmutableList<ClassFileResourceProvider> classpathResourceProviders,
      ImmutableList<ClassFileResourceProvider> libraryResourceProviders,
      ImmutableList<InternalArchiveClassFileProvider> archiveProvidersToClose,
      StringResource proguardMapOutputData,
      List<StringResource> mainDexListResources,
      List<String> mainDexClasses) {
    this.programResourceProviders = programResourceProviders;
    this.programResourcesMainDescriptor = programResourcesMainDescriptor;
    this.classpathResourceProviders = classpathResourceProviders;
    this.libraryResourceProviders = libraryResourceProviders;
    this.archiveProvidersToClose = archiveProvidersToClose;
    this.proguardMapOutputData = proguardMapOutputData;
    this.mainDexListResources = mainDexListResources;
    this.mainDexClasses = mainDexClasses;
    assert verifyInternalProvidersInCloseSet(classpathResourceProviders, archiveProvidersToClose);
    assert verifyInternalProvidersInCloseSet(libraryResourceProviders, archiveProvidersToClose);
  }

  private static boolean verifyInternalProvidersInCloseSet(
      ImmutableList<ClassFileResourceProvider> providers,
      ImmutableList<InternalArchiveClassFileProvider> providersToClose) {
    return providers.stream()
        .allMatch(
            p -> !(p instanceof InternalArchiveClassFileProvider) || providersToClose.contains(p));
  }

  static Reporter defaultReporter() {
    return new Reporter();
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return builder(defaultReporter());
  }

  /** Create a new empty builder. */
  public static Builder builder(Reporter reporter) {
    return new Builder(reporter);
  }

  /**
   * Create a new builder initialized with the resources from @code{app}.
   */
  public static Builder builder(AndroidApp app) {
    return builder(app, defaultReporter());
  }

  /** Create a new builder initialized with the resources from @code{app}. */
  public static Builder builder(AndroidApp app, Reporter reporter) {
    return new Builder(reporter, app);
  }

  /** Get full collection of all program resources from all program providers. */
  public Collection<ProgramResource> computeAllProgramResources() throws ResourceException {
    List<ProgramResource> resources = new ArrayList<>();
    for (ProgramResourceProvider provider : programResourceProviders) {
      resources.addAll(provider.getProgramResources());
    }
    return resources;
  }

  // TODO(zerny): Remove this method.
  public List<ProgramResource> getDexProgramResourcesForTesting() throws IOException {
    try {
      return filter(programResourceProviders, Kind.DEX);
    } catch (ResourceException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw new InternalCompilerError("Unexpected resource error", e);
      }
    }
  }

  // TODO(zerny): Remove this method.
  public List<ProgramResource> getClassProgramResourcesForTesting() throws IOException {
    try {
      return filter(programResourceProviders, Kind.CF);
    } catch (ResourceException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw new InternalCompilerError("Unexpected resource error", e);
      }
    }
  }

  public Set<DataEntryResource> getDataEntryResourcesForTesting() throws ResourceException {
    Set<DataEntryResource> out = new TreeSet<>(Comparator.comparing(DataResource::getName));
    for (ProgramResourceProvider programResourceProvider : getProgramResourceProviders()) {
      DataResourceProvider dataResourceProvider = programResourceProvider.getDataResourceProvider();
      if (dataResourceProvider != null) {
        dataResourceProvider.accept(
            new Visitor() {

              @Override
              public void visit(DataDirectoryResource directory) {
                // Ignore.
              }

              @Override
              public void visit(DataEntryResource file) {
                try {
                  byte[] bytes = ByteStreams.toByteArray(file.getByteStream());
                  DataEntryResource copy =
                      DataEntryResource.fromBytes(bytes, file.getName(), file.getOrigin());
                  out.add(copy);
                } catch (IOException | ResourceException e) {
                  throw new RuntimeException(e);
                }
              }
            });
      }
    }
    return out;
  }

  /** Get program resource providers. */
  public List<ProgramResourceProvider> getProgramResourceProviders() {
    return programResourceProviders;
  }

  /** Get classpath resource providers. */
  public List<ClassFileResourceProvider> getClasspathResourceProviders() {
    return classpathResourceProviders;
  }

  /** Get library resource providers. */
  public List<ClassFileResourceProvider> getLibraryResourceProviders() {
    return libraryResourceProviders;
  }

  private List<ProgramResource> filter(List<ProgramResourceProvider> providers, Kind kind)
      throws ResourceException {
    List<ProgramResource> out = new ArrayList<>();
    for (ProgramResourceProvider provider : providers) {
      for (ProgramResource code : provider.getProgramResources()) {
        if (code.getKind() == kind) {
          out.add(code);
        }
      }
    }
    return out;
  }

  /**
   * Get the proguard-map associated with an output "app" if it exists.
   *
   * <p>Note: this should never be used as the input to a compilation. See proguards ApplyMapping
   * for such use cases.
   */
  public StringResource getProguardMapOutputData() {
    return proguardMapOutputData;
  }

  /**
   * True if the main dex list resources exists.
   */
  public boolean hasMainDexList() {
    return !(mainDexListResources.isEmpty() && mainDexClasses.isEmpty());
  }

  /**
   * True if the main dex list resources exists.
   */
  public boolean hasMainDexListResources() {
    return !mainDexListResources.isEmpty();
  }

  /**
   * Get the main dex list resources if any.
   */
  public List<StringResource> getMainDexListResources() {
    return mainDexListResources;
  }

  /**
   * Get the main dex classes if any.
   */
  public List<String> getMainDexClasses() {
    return mainDexClasses;
  }

  /** Returns a copy of this AndroidApp that does not have a main dex list. */
  public AndroidApp withoutMainDexList() {
    return new AndroidApp(
        programResourceProviders,
        programResourcesMainDescriptor,
        classpathResourceProviders,
        libraryResourceProviders,
        archiveProvidersToClose,
        proguardMapOutputData,
        ImmutableList.of(),
        ImmutableList.of());
  }

  /**
   * Write the dex program resources and proguard resource to @code{output}.
   */
  public void write(Path output, OutputMode outputMode) throws IOException {
    if (isArchive(output)) {
      writeToZip(output, outputMode);
    } else {
      writeToDirectory(output, outputMode);
    }
  }

  /**
   * Write the dex program resources and proguard resource to @code{directory}.
   */
  public void writeToDirectory(Path directory, OutputMode outputMode) throws IOException {
    List<ProgramResource> dexProgramSources = getDexProgramResourcesForTesting();
    try {
      if (outputMode == OutputMode.DexIndexed) {
        DexIndexedConsumer.DirectoryConsumer.writeResources(directory, dexProgramSources);
      } else {
        DexFilePerClassFileConsumer.DirectoryConsumer.writeResources(
            directory, dexProgramSources, programResourcesMainDescriptor);
      }
    } catch (ResourceException e) {
      throw new IOException("Resource Error", e);
    }
  }

  /** Write the dex program resources to @code{archive}. */
  public void writeToZip(Path archive, OutputMode outputMode) throws IOException {
    try {
      if (outputMode == OutputMode.DexIndexed) {
        DexIndexedConsumer.ArchiveConsumer.writeResources(
            archive, getDexProgramResourcesForTesting(), getDataEntryResourcesForTesting());
      } else if (outputMode == OutputMode.DexFilePerClassFile) {
        List<ProgramResource> resources = getDexProgramResourcesForTesting();
        DexFilePerClassFileConsumer.ArchiveConsumer.writeResources(
            archive, resources, programResourcesMainDescriptor);
      } else if (outputMode == OutputMode.ClassFile) {
        ClassFileConsumer.ArchiveConsumer.writeResources(
            archive, getClassProgramResourcesForTesting(), getDataEntryResourcesForTesting());
      } else {
        throw new Unreachable("Unsupported output-mode for writing: " + outputMode);
      }
    } catch (ResourceException e) {
      throw new IOException("Resource Error", e);
    }
  }

  // Public for testing.
  public String getPrimaryClassDescriptor(Resource resource) {
    assert resource instanceof ProgramResource;
    return programResourcesMainDescriptor.get(resource);
  }

  public void dump(Path output, ProguardConfiguration configuration, Reporter reporter) {
    int nextDexIndex = 0;
    OpenOption[] openOptions =
        new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
    try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output, openOptions))) {
      writeToZipStream(
          out, dumpVersionFileName, Version.getVersionString().getBytes(), ZipEntry.DEFLATED);
      if (configuration != null) {
        String proguardConfig = configuration.getParsedConfiguration();
        writeToZipStream(out, dumpConfigFileName, proguardConfig.getBytes(), ZipEntry.DEFLATED);
      }
      nextDexIndex = dumpProgramResources(dumpProgramFileName, nextDexIndex, out);
      nextDexIndex = dumpClasspathResources(nextDexIndex, out);
      nextDexIndex = dumpLibraryResources(nextDexIndex, out);
    } catch (IOException | ResourceException e) {
      reporter.fatalError(new StringDiagnostic("Failed to dump inputs"), e);
    }
  }

  private int dumpLibraryResources(int nextDexIndex, ZipOutputStream out)
      throws IOException, ResourceException {
    nextDexIndex =
        dumpClassFileResources(dumpLibraryFileName, nextDexIndex, out, libraryResourceProviders);
    return nextDexIndex;
  }

  private int dumpClasspathResources(int nextDexIndex, ZipOutputStream out)
      throws IOException, ResourceException {
    nextDexIndex =
        dumpClassFileResources(
            dumpClasspathFileName, nextDexIndex, out, classpathResourceProviders);
    return nextDexIndex;
  }

  private static ClassFileResourceProvider createClassFileResourceProvider(
      Map<String, ProgramResource> classPathResources) {
    return new ClassFileResourceProvider() {
      @Override
      public Set<String> getClassDescriptors() {
        return classPathResources.keySet();
      }

      @Override
      public ProgramResource getProgramResource(String descriptor) {
        return classPathResources.get(descriptor);
      }
    };
  }

  private Consumer<ProgramResource> createClassFileResourceConsumer(
      Map<String, ProgramResource> classPathResources) {
    return programResource -> {
      assert programResource.getClassDescriptors().size() == 1;
      String descriptor = programResource.getClassDescriptors().iterator().next();
      classPathResources.put(descriptor, programResource);
    };
  }

  private int dumpProgramResources(String archiveName, int nextDexIndex, ZipOutputStream out)
      throws IOException, ResourceException {
    try (ByteArrayOutputStream archiveByteStream = new ByteArrayOutputStream()) {
      try (ZipOutputStream archiveOutputStream = new ZipOutputStream(archiveByteStream)) {
        Object2IntMap<String> seen = new Object2IntOpenHashMap<>();
        Set<DataEntryResource> dataEntries = getDataEntryResourcesForTesting();
        for (DataEntryResource dataResource : dataEntries) {
          String entryName = dataResource.getName();
          try (InputStream dataStream = dataResource.getByteStream()) {
            byte[] bytes = ByteStreams.toByteArray(dataStream);
            writeToZipStream(archiveOutputStream, entryName, bytes, ZipEntry.DEFLATED);
          }
        }
        for (ProgramResourceProvider provider : programResourceProviders) {
          for (ProgramResource programResource : provider.getProgramResources()) {
            nextDexIndex =
                dumpProgramResource(seen, nextDexIndex, archiveOutputStream, programResource);
          }
        }
      }
      writeToZipStream(out, archiveName, archiveByteStream.toByteArray(), ZipEntry.DEFLATED);
    }
    return nextDexIndex;
  }

  private static int dumpClassFileResources(
      String archiveName,
      int nextDexIndex,
      ZipOutputStream out,
      ImmutableList<ClassFileResourceProvider> classpathResourceProviders)
      throws IOException, ResourceException {
    try (ByteArrayOutputStream archiveByteStream = new ByteArrayOutputStream()) {
      try (ZipOutputStream archiveOutputStream = new ZipOutputStream(archiveByteStream)) {
        Object2IntMap<String> seen = new Object2IntOpenHashMap<>();
        for (ClassFileResourceProvider provider : classpathResourceProviders) {
          for (String descriptor : provider.getClassDescriptors()) {
            ProgramResource programResource = provider.getProgramResource(descriptor);
            int oldDexIndex = nextDexIndex;
            nextDexIndex =
                dumpProgramResource(seen, nextDexIndex, archiveOutputStream, programResource);
            assert nextDexIndex == oldDexIndex;
          }
        }
      }
      writeToZipStream(out, archiveName, archiveByteStream.toByteArray(), ZipEntry.DEFLATED);
    }
    return nextDexIndex;
  }

  private static int dumpProgramResource(
      Object2IntMap<String> seen,
      int nextDexIndex,
      ZipOutputStream archiveOutputStream,
      ProgramResource programResource)
      throws ResourceException, IOException {
    byte[] bytes = ByteStreams.toByteArray(programResource.getByteStream());
    String entryName;
    if (programResource.getKind() == Kind.CF) {
      Set<String> classDescriptors = programResource.getClassDescriptors();
      String classDescriptor =
          (classDescriptors == null || classDescriptors.size() != 1)
              ? extractClassDescriptor(bytes)
              : classDescriptors.iterator().next();
      String classFileName = DescriptorUtils.getClassFileName(classDescriptor);
      int dupCount = seen.getOrDefault(classDescriptor, 0);
      seen.put(classDescriptor, dupCount + 1);
      entryName = dupCount == 0 ? classFileName : (classFileName + "." + dupCount + ".dup");
    } else {
      assert programResource.getKind() == Kind.DEX;
      entryName = "classes" + nextDexIndex++ + ".dex";
    }
    writeToZipStream(archiveOutputStream, entryName, bytes, ZipEntry.DEFLATED);
    return nextDexIndex;
  }

  private static String extractClassDescriptor(byte[] bytes) {
    class ClassNameExtractor extends ClassVisitor {
      private String className;

      private ClassNameExtractor() {
        super(ASM_VERSION);
      }

      @Override
      public void visit(
          int version,
          int access,
          String name,
          String signature,
          String superName,
          String[] interfaces) {
        className = name;
      }

      String getDescriptor() {
        return "L" + className + ";";
      }
    }

    org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
    ClassNameExtractor extractor = new ClassNameExtractor();
    reader.accept(
        extractor,
        org.objectweb.asm.ClassReader.SKIP_CODE
            | org.objectweb.asm.ClassReader.SKIP_DEBUG
            | org.objectweb.asm.ClassReader.SKIP_FRAMES);
    return extractor.getDescriptor();
  }

  /**
   * Builder interface for constructing an AndroidApp.
   */
  public static class Builder {

    private final List<ProgramResourceProvider> programResourceProviders = new ArrayList<>();
    private final List<ProgramResource> programResources = new ArrayList<>();
    private final List<DataResource> dataResources = new ArrayList<>();
    private final Map<ProgramResource, String> programResourcesMainDescriptor = new HashMap<>();
    private final List<ClassFileResourceProvider> classpathResourceProviders = new ArrayList<>();
    private final List<ClassFileResourceProvider> libraryResourceProviders = new ArrayList<>();
    private final List<InternalArchiveClassFileProvider> archiveProvidersToClose =
        new ArrayList<>();
    private List<StringResource> mainDexListResources = new ArrayList<>();
    private List<String> mainDexListClasses = new ArrayList<>();
    private boolean ignoreDexInArchive = false;

    // Proguard map data is output only data. This should never be used as input to a compilation.
    private StringResource proguardMapOutputData;

    private final Reporter reporter;

    // See AndroidApp::builder().
    private Builder(Reporter reporter) {
      this.reporter = reporter;
    }

    // See AndroidApp::builder(AndroidApp).
    private Builder(Reporter reporter, AndroidApp app) {
      this(reporter);
      programResourceProviders.addAll(app.programResourceProviders);
      classpathResourceProviders.addAll(app.classpathResourceProviders);
      libraryResourceProviders.addAll(app.libraryResourceProviders);
      archiveProvidersToClose.addAll(app.archiveProvidersToClose);
      mainDexListResources = app.mainDexListResources;
      mainDexListClasses = app.mainDexClasses;
    }

    public Reporter getReporter() {
      return reporter;
    }

    public Builder addDump(Path dumpFile) throws IOException {
      System.out.println("Reading dump from file: " + dumpFile);
      Origin origin = new PathOrigin(dumpFile);
      ZipUtils.iter(
          dumpFile.toString(),
          (entry, input) -> {
            String name = entry.getName();
            if (name.equals(dumpVersionFileName)) {
              String content = new String(ByteStreams.toByteArray(input), StandardCharsets.UTF_8);
              System.out.println("Dump produced by R8 version: " + content);
            } else if (name.equals(dumpProgramFileName)) {
              readProgramDump(origin, input);
            } else if (name.equals(dumpClasspathFileName)) {
              readClassFileDump(origin, input, this::addClasspathResourceProvider, "classpath");
            } else if (name.equals(dumpLibraryFileName)) {
              readClassFileDump(origin, input, this::addLibraryResourceProvider, "library");
            } else {
              System.out.println("WARNING: Unexpected dump file entry: " + entry.getName());
            }
          });
      return this;
    }

    private void readClassFileDump(
        Origin origin,
        InputStream input,
        Consumer<ClassFileResourceProvider> addProvider,
        String inputType)
        throws IOException {
      Map<String, ProgramResource> resources = new HashMap<>();
      try (ZipInputStream stream = new ZipInputStream(input)) {
        ZipEntry entry;
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
            resources.put(descriptor, resource);
          } else if (name.endsWith(".dup")) {
            System.out.println("WARNING: Duplicate " + inputType + " resource: " + name);
          } else {
            System.out.println("WARNING: Unexpected " + inputType + " resource: " + name);
          }
        }
      }
      if (!resources.isEmpty()) {
        addProvider.accept(createClassFileResourceProvider(resources));
      }
    }

    private void readProgramDump(Origin origin, InputStream input) throws IOException {
      List<ProgramResource> programResources = new ArrayList<>();
      List<DataEntryResource> dataResources = new ArrayList<>();
      try (ZipInputStream stream = new ZipInputStream(input)) {
        ZipEntry entry;
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
            programResources.add(resource);
          } else if (ZipUtils.isDexFile(name)) {
            Origin entryOrigin = new ArchiveEntryOrigin(name, origin);
            ProgramResource resource =
                OneShotByteResource.create(
                    Kind.DEX, entryOrigin, ByteStreams.toByteArray(stream), null);
            programResources.add(resource);
          } else if (name.endsWith(".dup")) {
            System.out.println("WARNING: Duplicate program resource: " + name);
          } else {
            dataResources.add(
                DataEntryResource.fromBytes(ByteStreams.toByteArray(stream), name, origin));
          }
        }
      }
      if (!programResources.isEmpty() || !dataResources.isEmpty()) {
        addProgramResourceProvider(
            new ProgramResourceProvider() {
              @Override
              public Collection<ProgramResource> getProgramResources() throws ResourceException {
                return programResources;
              }

              @Override
              public DataResourceProvider getDataResourceProvider() {
                return dataResources.isEmpty()
                    ? null
                    : new DataResourceProvider() {
                      @Override
                      public void accept(Visitor visitor) throws ResourceException {
                        for (DataEntryResource dataResource : dataResources) {
                          visitor.visit(dataResource);
                        }
                      }
                    };
              }
            });
      }
    }

    /** Add program file resources. */
    public Builder addProgramFiles(Path... files) {
      return addProgramFiles(Arrays.asList(files));
    }

    /** Add program file resources. */
    public Builder addProgramFiles(Collection<Path> files) {
      for (Path file : files) {
        addProgramFile(file);
      }
      return this;
    }

    /** Add filtered archives of program resources. */
    public Builder addFilteredProgramArchives(Collection<FilteredClassPath> filteredArchives) {
      for (FilteredClassPath archive : filteredArchives) {
        if (isArchive(archive.getPath())) {
          ArchiveResourceProvider archiveResourceProvider =
              new ArchiveResourceProvider(archive, ignoreDexInArchive);
          addProgramResourceProvider(archiveResourceProvider);
        } else {
          reporter.error(
              new StringDiagnostic(
                  "Unexpected input type. Only archive types are supported, e.g., .jar, .zip, etc.",
                  archive.getOrigin(),
                  archive.getPosition()));
        }
      }
      return this;
    }

    public Builder addProgramResourceProvider(ProgramResourceProvider provider) {
      assert provider != null;
      programResourceProviders.add(provider);
      return this;
    }

    /** Add classpath file resources. */
    public Builder addClasspathFiles(Path... files) {
      return addClasspathFiles(Arrays.asList(files));
    }

    /** Add classpath file resources. */
    public Builder addClasspathFiles(Collection<Path> files) {
      for (Path file : files) {
        addClasspathFile(file);
      }
      return this;
    }

    /** Add classpath file resources. */
    public Builder addClasspathFile(Path file) {
      addClasspathOrLibraryProvider(file, classpathResourceProviders);
      return this;
    }

    /**
     * Add classpath resource provider.
     */
    public Builder addClasspathResourceProvider(ClassFileResourceProvider provider) {
      classpathResourceProviders.add(provider);
      return this;
    }

    /** Add library file resources. */
    public Builder addLibraryFiles(Path... files) {
      return addLibraryFiles(Arrays.asList(files));
    }

    /** Add library file resources. */
    public Builder addLibraryFiles(Collection<Path> files) {
      for (Path file : files) {
        addClasspathOrLibraryProvider(file, libraryResourceProviders);
      }
      return this;
    }

    /** Add library file resource. */
    public Builder addLibraryFile(Path file) {
      addClasspathOrLibraryProvider(file, libraryResourceProviders);
      return this;
    }

    /** Add library file resources. */
    public Builder addFilteredLibraryArchives(Collection<FilteredClassPath> filteredArchives) {
      for (FilteredClassPath archive : filteredArchives) {
        if (isArchive(archive.getPath())) {
          try {
            FilteredArchiveClassFileProvider provider =
                new FilteredArchiveClassFileProvider(archive);
            archiveProvidersToClose.add(provider);
            libraryResourceProviders.add(provider);
          } catch (IOException e) {
            reporter.error(new ExceptionDiagnostic(e, new PathOrigin(archive.getPath())));
          }
        } else {
          reporter.error(
              new StringDiagnostic(
                  "Unexpected input type. Only archive types are supported, e.g., .jar, .zip, etc.",
                  archive.getOrigin(),
                  archive.getPosition()));
        }
      }
      return this;
    }

    /**
     * Add library resource provider.
     */
    public Builder addLibraryResourceProvider(ClassFileResourceProvider provider) {
      if (provider instanceof InternalArchiveClassFileProvider) {
        archiveProvidersToClose.add((InternalArchiveClassFileProvider) provider);
      }
      libraryResourceProviders.add(provider);
      return this;
    }

    /**
     * Add dex program-data with class descriptor.
     */
    public Builder addDexProgramData(byte[] data, Set<String> classDescriptors) {
      addProgramResources(
          ProgramResource.fromBytes(Origin.unknown(), Kind.DEX, data, classDescriptors));
      return this;
    }

    /**
     * Add dex program-data with class descriptor and primary class.
     */
    public Builder addDexProgramData(
        byte[] data,
        Set<String> classDescriptors,
        String primaryClassDescriptor) {
      ProgramResource resource = ProgramResource.fromBytes(
          Origin.unknown(), Kind.DEX, data, classDescriptors);
      programResources.add(resource);
      programResourcesMainDescriptor.put(resource, primaryClassDescriptor);
      return this;
    }

    /**
     * Add dex program-data.
     */
    public Builder addDexProgramData(byte[] data, Origin origin) {
      addProgramResources(ProgramResource.fromBytes(origin, Kind.DEX, data, null));
      return this;
    }

    /**
     * Add dex program-data.
     */
    public Builder addDexProgramData(Collection<byte[]> data) {
      for (byte[] datum : data) {
        addProgramResources(ProgramResource.fromBytes(Origin.unknown(), Kind.DEX, datum, null));
      }
      return this;
    }

    /**
     * Add Java-bytecode program data.
     */
    public Builder addClassProgramData(Collection<byte[]> data) {
      for (byte[] datum : data) {
        addClassProgramData(datum, Origin.unknown());
      }
      return this;
    }

    /**
     * Add Java-bytecode program data.
     */
    public Builder addClassProgramData(byte[] data, Origin origin) {
      return addClassProgramData(data, origin, null);
    }

    public Builder addClassProgramData(byte[] data, Origin origin, Set<String> classDescriptors) {
      addProgramResources(ProgramResource.fromBytes(origin, Kind.CF, data, classDescriptors));
      return this;
    }

    /** Add resource data. */
    public Builder addDataResource(DataResource dataResource) {
      addDataResources(dataResource);
      return this;
    }

    /**
     * Set proguard-map output data.
     *
     * <p>Note: this should not be used as inputs to compilation!
     */
    public Builder setProguardMapOutputData(String content) {
      proguardMapOutputData =
          content == null ? null : StringResource.fromString(content, Origin.unknown());
      return this;
    }

    /**
     * Add a main-dex list file.
     */
    public Builder addMainDexListFiles(Path... files) throws NoSuchFileException {
      return addMainDexListFiles(Arrays.asList(files));
    }

    public Builder addMainDexListFiles(Collection<Path> files) throws NoSuchFileException {
      for (Path file : files) {
        if (!Files.exists(file)) {
          throw new NoSuchFileException(file.toString());
        }
        // TODO(sgjesse): Should we just read the file here? This will sacrifice the parallelism
        // in ApplicationReader where all input resources are read in parallel.
        mainDexListResources.add(StringResource.fromFile(file));
      }
      return this;
    }

    /**
     * Add main-dex classes.
     */
    public Builder addMainDexClasses(String... classes) {
      return addMainDexClasses(Arrays.asList(classes));
    }

    /**
     * Add main-dex classes.
     */
    public Builder addMainDexClasses(Collection<String> classes) {
      mainDexListClasses.addAll(classes);
      return this;
    }

    public boolean hasMainDexList() {
      return !(mainDexListResources.isEmpty() && mainDexListClasses.isEmpty());
    }

    /**
     * Ignore dex resources in input archives.
     *
     * In some situations (e.g. AOSP framework build) the input archives include both class and
     * dex resources. Setting this flag ignores the dex resources and reads the class resources
     * only.
     */
    public Builder setIgnoreDexInArchive(boolean value) {
      ignoreDexInArchive = value;
      return this;
    }

    /**
     * Build final AndroidApp.
     */
    public AndroidApp build() {
      if (!programResources.isEmpty() || !dataResources.isEmpty()) {
        // If there are individual program resources move them to a dedicated provider.
        final List<ProgramResource> finalProgramResources = ImmutableList.copyOf(programResources);
        final List<DataResource> finalDataResources = ImmutableList.copyOf(dataResources);
        programResourceProviders.add(
            new ProgramResourceProvider() {
              @Override
              public Collection<ProgramResource> getProgramResources() {
                return finalProgramResources;
              }

              @Override
              public DataResourceProvider getDataResourceProvider() {
                if (!finalDataResources.isEmpty()) {
                  return new DataResourceProvider() {
                    @Override
                    public void accept(Visitor visitor) {
                      for (DataResource dataResource : finalDataResources) {
                        if (dataResource instanceof DataEntryResource) {
                          visitor.visit((DataEntryResource) dataResource);
                        } else {
                          assert dataResource instanceof DataDirectoryResource;
                          visitor.visit((DataDirectoryResource) dataResource);
                        }
                      }
                    }
                  };
                }
                return null;
              }
            });
        programResources.clear();
        dataResources.clear();
      }
      return new AndroidApp(
          ImmutableList.copyOf(programResourceProviders),
          ImmutableMap.copyOf(programResourcesMainDescriptor),
          ImmutableList.copyOf(classpathResourceProviders),
          ImmutableList.copyOf(libraryResourceProviders),
          ImmutableList.copyOf(archiveProvidersToClose),
          proguardMapOutputData,
          mainDexListResources,
          mainDexListClasses);
    }

    public Builder addProgramFile(Path file) {
      if (!Files.exists(file)) {
        PathOrigin pathOrigin = new PathOrigin(file);
        NoSuchFileException noSuchFileException = new NoSuchFileException(file.toString());
        reporter.error(new ExceptionDiagnostic(noSuchFileException, pathOrigin));
      }
      if (isDexFile(file)) {
        addProgramResources(ProgramResource.fromFile(Kind.DEX, file));
      } else if (isClassFile(file)) {
        addProgramResources(ProgramResource.fromFile(Kind.CF, file));
      } else if (isAarFile(file)) {
        addProgramResourceProvider(AarArchiveResourceProvider.fromArchive(file));
      } else if (isArchive(file)) {
        addProgramResourceProvider(ArchiveResourceProvider.fromArchive(file, ignoreDexInArchive));
      } else {
        throw new CompilationError("Unsupported source file type", new PathOrigin(file));
      }
      return this;
    }

    private void addProgramResources(ProgramResource... resources) {
      addProgramResources(Arrays.asList(resources));
    }

    private void addProgramResources(Collection<ProgramResource> resources) {
      programResources.addAll(resources);
    }

    private void addDataResources(DataResource... resources) {
      addDataResources(Arrays.asList(resources));
    }

    private void addDataResources(Collection<DataResource> resources) {
      this.dataResources.addAll(resources);
    }

    private void addClasspathOrLibraryProvider(
        Path file, List<ClassFileResourceProvider> providerList) {
      if (!Files.exists(file)) {
        reporter.error(
            new ExceptionDiagnostic(
                new NoSuchFileException(file.toString()), new PathOrigin(file)));
      }
      if (isArchive(file)) {
        try {
          InternalArchiveClassFileProvider provider = new InternalArchiveClassFileProvider(file);
          archiveProvidersToClose.add(provider);
          providerList.add(provider);
        } catch (IOException e) {
          reporter.error(new ExceptionDiagnostic(e, new PathOrigin(file)));
        }
      } else if (Files.isDirectory(file) ) {
        providerList.add(DirectoryClassFileProvider.fromDirectory(file));
      } else {
        throw new CompilationError("Unsupported source file type", new PathOrigin(file));
      }
    }

    public List<ProgramResourceProvider> getProgramResourceProviders() {
      return programResourceProviders;
    }
  }
}
