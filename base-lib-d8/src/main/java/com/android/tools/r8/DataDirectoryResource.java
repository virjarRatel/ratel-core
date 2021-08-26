// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Keep
public interface DataDirectoryResource extends DataResource {

  static DataDirectoryResource fromName(String name, Origin origin) {
    return new NamedDataDirectoryResource(name, origin);
  }

  static DataDirectoryResource fromFile(Path dir, Path file) {
    return new LocalDataDirectoryResource(dir.resolve(file).toFile(),
        file.toString().replace(File.separatorChar, SEPARATOR));
  }

  static DataDirectoryResource fromZip(ZipFile zip, ZipEntry entry) {
    return new ZipDataDirectoryResource(zip, entry);
  }

  class NamedDataDirectoryResource implements DataDirectoryResource {
    private final String name;
    private final Origin origin;

    private NamedDataDirectoryResource(String name, Origin origin) {
      assert name != null;
      assert origin != null;
      this.name = name;
      this.origin = origin;
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  class ZipDataDirectoryResource implements DataDirectoryResource {
    private final ZipFile zip;
    private final ZipEntry entry;

    private ZipDataDirectoryResource(ZipFile zip, ZipEntry entry) {
      assert zip != null;
      assert entry != null;
      this.zip = zip;
      this.entry = entry;
    }

    @Override
    public Origin getOrigin() {
      return new ArchiveEntryOrigin(entry.getName(), new PathOrigin(Paths.get(zip.getName())));
    }

    @Override
    public String getName() {
      return entry.getName();
    }
  }

  class LocalDataDirectoryResource implements DataDirectoryResource {
    private final File file;
    private final String relativePath;

    private LocalDataDirectoryResource(File file, String relativePath) {
      assert file != null;
      assert relativePath != null;
      this.file = file;
      this.relativePath = relativePath;
    }

    @Override
    public Origin getOrigin() {
      return new PathOrigin(file.toPath());
    }

    @Override
    public String getName() {
      return relativePath;
    }
  }
}
