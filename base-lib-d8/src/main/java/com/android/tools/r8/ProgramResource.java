// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Represents program application resources.
 *
 * The content kind or format of a program resource can be either a Java class-file or an Android
 * DEX file. In both cases, the resource must be able to provide the content as a byte stream.
 * A resource may optionally include a set describing the class descriptors for each type that is
 * defined by the resource.
 */
@KeepForSubclassing
public interface ProgramResource extends Resource {

  /** Type of program-format kinds. */
  @Keep
  enum Kind {
    /** Format-kind for Java class-file resources. */
    CF,
    /** Format-kind for Android dex-file resources. */
    DEX,
  }

  /**
   * Create a program resource for a given file.
   *
   * <p>The origin of a file resource is the path of the file.
   */
  static ProgramResource fromFile(Kind kind, Path file) {
    return new FileResource(kind, file, null);
  }

  /**
   * Create a program resource for a given type, content and type descriptor.
   *
   * <p>The origin must be supplied upon construction. If no reasonable origin
   * exits, use {@code Origin.unknown()}.
   */
  static ProgramResource fromBytes(
      Origin origin, Kind kind, byte[] bytes, Set<String> typeDescriptors) {
    return new ByteResource(origin, kind, bytes, typeDescriptors);
  }

  /** Get the program format-kind of the resource. */
  Kind getKind();

  /** Get the bytes of the program resource. */
  InputStream getByteStream() throws ResourceException;

  /**
   * Get the set of class descriptors for classes defined by this resource.
   *
   * <p>This is not deprecated and will remain after Resource::getClassDescriptors is removed.
   *
   * @return Set of class descriptors defined by the resource or null if unknown.
   */
  Set<String> getClassDescriptors();

  /** File-based program resource. */
  @Keep
  class FileResource implements ProgramResource {
    private final Origin origin;
    private final Kind kind;
    private final Path file;
    private final Set<String> classDescriptors;

    private FileResource(Kind kind, Path file, Set<String> classDescriptors) {
      this.origin = new PathOrigin(file);
      this.kind = kind;
      this.file = file;
      this.classDescriptors = classDescriptors;
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }

    @Override
    public Kind getKind() {
      return kind;
    }

    @Override
    public InputStream getByteStream() throws ResourceException {
      try {
        return Files.newInputStream(file);
      } catch (IOException e) {
        throw new ResourceException(getOrigin(), e);
      }
    }

    @Override
    public Set<String> getClassDescriptors() {
      return classDescriptors;
    }
  }

  /** Byte-content based program resource. */
  @Keep
  class ByteResource implements ProgramResource {
    private final Origin origin;
    private final Kind kind;
    private final byte[] bytes;
    private final Set<String> classDescriptors;

    private ByteResource(Origin origin, Kind kind, byte[] bytes, Set<String> classDescriptors) {
      assert bytes != null;
      this.origin = origin;
      this.kind = kind;
      this.bytes = bytes;
      this.classDescriptors = classDescriptors;
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }

    @Override
    public Kind getKind() {
      return kind;
    }

    @Override
    public InputStream getByteStream() throws ResourceException {
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public Set<String> getClassDescriptors() {
      return classDescriptors;
    }
  }
}
