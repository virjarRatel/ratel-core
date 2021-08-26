// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.isClassFile;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Lazy resource provider returning class file resources based
 * on filesystem directory content.
 */
@Keep
public final class DirectoryClassFileProvider implements ClassFileResourceProvider {
  private final Path root;

  /** Create resource provider from directory path. */
  public static ClassFileResourceProvider fromDirectory(Path dir) {
    return new DirectoryClassFileProvider(dir.toAbsolutePath());
  }

  private DirectoryClassFileProvider(Path root) {
    this.root = root;
  }

  @Override
  public Set<String> getClassDescriptors() {
    HashSet<String> result = Sets.newHashSet();
    collectClassDescriptors(root, result);
    return result;
  }

  private void collectClassDescriptors(Path dir, Set<String> result) {
    File file = dir.toFile();
    if (file.exists()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File child : files) {
          if (child.isDirectory()) {
            collectClassDescriptors(child.toPath(), result);
          } else {
            Path relative = root.relativize(child.toPath());
            if (isClassFile(relative)) {
              result.add(DescriptorUtils.guessTypeDescriptor(relative));
            }
          }
        }
      }
    }
  }

  @Override
  public ProgramResource getProgramResource(String descriptor) {
    assert DescriptorUtils.isClassDescriptor(descriptor);
    // Build expected file path based on type descriptor.
    String classBinaryName = DescriptorUtils.getClassBinaryNameFromDescriptor(descriptor);
    Path file = root.resolve(classBinaryName + CLASS_EXTENSION);
    return (Files.exists(file) && !Files.isDirectory(file))
        ? ProgramResource.fromFile(Kind.CF, file)
        : null;
  }

  public Path getRoot() {
    return root;
  }
}
