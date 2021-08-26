// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public interface StringResource extends Resource {

  /**
   * Create a string resource from a string.
   *
   * <p>The origin of the resource must be supplied upon construction. If no reasonable origin
   * exits, use {@link Origin#unknown()}.
   *
   * @param content String content for the string resource.
   * @param origin Origin of the string resource.
   */
  static StringResource fromString(String content, Origin origin) {
    return new StringContentResource(origin, content);
  }

  /**
   * Create a string resource for the contents of a given UTF-8 encoded text file.
   *
   * <p>The origin of the resource is the path of the file.
   *
   * @param file Path of file with UTF-8 encoded text.
   */
  static StringResource fromFile(Path file) {
    return fromFile(file, StandardCharsets.UTF_8);
  }

  /**
   * Create a string resource for the contents of a given file.
   *
   * <p>The origin of the resource is the path of the file.
   *
   * @param file Path of file.
   * @param charset Charset coding of file.
   */
  static StringResource fromFile(Path file, Charset charset) {
    return new FileResource(file, charset);
  }

  /**
   * Get the string content of the string resource.
   *
   * @return The string content of the resource.
   * @throws ResourceException Exception thrown if the resource fails to produce its content.
   */
  String getString() throws ResourceException;

  class StringContentResource implements StringResource {
    private final Origin origin;
    private final String content;

    private StringContentResource(Origin origin, String content) {
      assert origin != null;
      assert content != null;
      this.origin = origin;
      this.content = content;
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }

    @Override
    public String getString() throws ResourceException {
      return content;
    }
  }

  class FileResource implements StringResource {
    private final Path file;
    private final Charset charset;
    private final Origin origin;

    private FileResource(Path file, Charset charset) {
      assert file != null;
      assert charset != null;
      this.file = file;
      this.charset = charset;
      origin = new PathOrigin(file);
    }

    @Override
    public Origin getOrigin() {
      return origin;
    }

    @Override
    public String getString() throws ResourceException {
      try {
        return FileUtils.readTextFile(file, charset);
      } catch (IOException e) {
        throw new ResourceException(origin, e);
      }
    }
  }
}
