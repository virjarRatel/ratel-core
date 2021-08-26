// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Keep
public interface DataEntryResource extends DataResource {

  /** Get the bytes of the data entry resource. */
  InputStream getByteStream() throws ResourceException;

  static DataEntryResource fromBytes(byte[] bytes, String name, Origin origin) {
    return new ByteDataEntryResource(bytes, name, origin);
  }

  static DataEntryResource fromFile(Path dir, Path file) {
    return new LocalDataEntryResource(dir.resolve(file).toFile(),
        file.toString().replace(File.separatorChar, SEPARATOR));
  }

  static DataEntryResource fromZip(ZipFile zip, ZipEntry entry) {
    return new ZipDataEntryResource(zip, entry);
  }

  default DataEntryResource withName(String name) {
    return new NestedDataEntryResource(name, null, this);
  }

  class ByteDataEntryResource implements DataEntryResource {

    private final byte[] bytes;
    private final String name;
    private final Origin origin;

    public ByteDataEntryResource(byte[] bytes, String name, Origin origin) {
      this.bytes = bytes;
      this.name = name;
      this.origin = origin;
    }

    @Override
    public InputStream getByteStream() {
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }
  }

  class ZipDataEntryResource implements DataEntryResource {
    private final ZipFile zip;
    private final ZipEntry entry;

    private ZipDataEntryResource(ZipFile zip, ZipEntry entry) {
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

    @Override
    public InputStream getByteStream() throws ResourceException {
      try {
        return zip.getInputStream(entry);
      } catch (IOException e) {
        throw new ResourceException(getOrigin(), e);
      }
    }
  }

  class LocalDataEntryResource implements DataEntryResource {
    private final File file;
    private final String relativePath;

    private LocalDataEntryResource(File file, String relativePath) {
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

    @Override
    public InputStream getByteStream() throws ResourceException {
      try {
        return new FileInputStream(file);
      } catch (IOException e) {
        throw new ResourceException(getOrigin(), e);
      }
    }
  }

  /**
   * A resource that has the same contents as another resource, but with a different name or origin.
   */
  class NestedDataEntryResource implements DataEntryResource {

    private final String name;
    private final Origin origin;
    private final DataEntryResource resource;

    public NestedDataEntryResource(String name, Origin origin, DataEntryResource resource) {
      this.name = name;
      this.origin = origin;
      this.resource = resource;
    }

    @Override
    public InputStream getByteStream() throws ResourceException {
      return resource.getByteStream();
    }

    @Override
    public String getName() {
      return name != null ? name : resource.getName();
    }

    @Override
    public Origin getOrigin() {
      return origin != null ? origin : resource.getOrigin();
    }
  }
}
