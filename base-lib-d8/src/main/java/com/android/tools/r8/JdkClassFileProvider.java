// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.FileUtils;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lazy Java class file resource provider loading class files from a JDK.
 *
 * <p>The descriptor index is built eagerly upon creating the provider and subsequent requests for
 * resources in the descriptor set will then force the read of JDK content.
 *
 * <p>Currently only JDK's of version 9 or higher with a lib/jrt-fs.jar file present is supported.
 */
@Keep
public class JdkClassFileProvider implements ClassFileResourceProvider, Closeable {
  private Origin origin;
  private final Set<String> descriptors = new HashSet<>();
  private final Map<String, String> descriptorToModule = new HashMap<>();
  private URLClassLoader jrtFsJarLoader;
  private FileSystem jrtFs;

  /**
   * Creates a lazy class-file program-resource provider for the system modules of the running Java
   * VM.
   *
   * <p>Only supported if the current VM is of version 9 or higher.
   */
  public static ClassFileResourceProvider fromSystemJdk() throws IOException {
    return new JdkClassFileProvider();
  }

  /**
   * Creates a lazy class-file program-resource provider for the system modules of a JDK.
   *
   * @param home Location of the JDK to read the program-resources from.
   *     <p>Only supported if the JDK to read is of version 9 or higher.
   */
  public static ClassFileResourceProvider fromSystemModulesJdk(Path home) throws IOException {
    Path jrtFsJar = home.resolve("lib").resolve("jrt-fs.jar");
    if (!Files.exists(jrtFsJar)) {
      throw new NoSuchFileException(jrtFsJar.toString());
    }
    return new JdkClassFileProvider(home);
  }

  /**
   * Creates a lazy class-file program-resource provider for the runtime of a JDK.
   *
   * <p>This will load the program-resources form the system modules for JDK of version 9 or higher.
   *
   * <p>This will load <code>rt.jar</code> for JDK of version 8 and lower.
   *
   * @param home Location of the JDK to read the program-resources from.
   */
  public static ClassFileResourceProvider fromJdkHome(Path home) throws IOException {
    Path jrtFsJar = home.resolve("lib").resolve("jrt-fs.jar");
    if (Files.exists(jrtFsJar)) {
      return JdkClassFileProvider.fromSystemModulesJdk(home);
    }
    // JDK has rt.jar in jre/lib/rt.jar.
    Path rtJar = home.resolve("jre").resolve("lib").resolve("rt.jar");
    if (Files.exists(rtJar)) {
      return fromJavaRuntimeJar(rtJar);
    }
    // JRE has rt.jar in lib/rt.jar.
    rtJar = home.resolve("lib").resolve("rt.jar");
    if (Files.exists(rtJar)) {
      return fromJavaRuntimeJar(rtJar);
    }
    throw new IOException("Path " + home + " does not look like a Java home");
  }

  public static ClassFileResourceProvider fromJavaRuntimeJar(Path archive) throws IOException {
    return new ArchiveClassFileProvider(archive);
  }

  private JdkClassFileProvider() throws IOException {
    origin = Origin.unknown();
    collectDescriptors(FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap()));
  }

  /**
   * Creates a lazy class-file program-resource provider for the system modules of a JDK.
   *
   * @param home Location of the JDK to read the program-resources from.
   *     <p>Only supported if the JDK to read is of version 9 or higher.
   */
  private JdkClassFileProvider(Path home) throws IOException {
    origin = new PathOrigin(home);
    Path jrtFsJar = home.resolve("lib").resolve("jrt-fs.jar");
    assert Files.exists(jrtFsJar);
    jrtFsJarLoader = new URLClassLoader(new URL[] {jrtFsJar.toUri().toURL()});
    FileSystem jrtFs =
        FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap(), jrtFsJarLoader);
    collectDescriptors(jrtFs);
  }

  private void collectDescriptors(FileSystem jrtFs) throws IOException {
    this.jrtFs = jrtFs;
    Files.walk(jrtFs.getPath("/modules"))
        .forEach(
            path -> {
              if (FileUtils.isClassFile(path)) {
                DescriptorUtils.ModuleAndDescriptor moduleAndDescriptor =
                    DescriptorUtils.guessJrtModuleAndTypeDescriptor(path.toString());
                descriptorToModule.put(
                    moduleAndDescriptor.getDescriptor(), moduleAndDescriptor.getModule());
                descriptors.add(moduleAndDescriptor.getDescriptor());
              }
            });
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
      return ProgramResource.fromBytes(
          Origin.unknown(),
          ProgramResource.Kind.CF,
          Files.readAllBytes(
              jrtFs.getPath(
                  "modules",
                  descriptorToModule.get(descriptor),
                  DescriptorUtils.getPathFromDescriptor(descriptor))),
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
    jrtFs.close();
    if (jrtFsJarLoader != null) {
      jrtFsJarLoader.close();
    }
  }
}
