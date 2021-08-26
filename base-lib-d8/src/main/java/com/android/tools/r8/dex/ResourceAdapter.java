// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dex;

import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppServices;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.ProguardPathFilter;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.ExceptionDiagnostic;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceAdapter {

  private final AppView<?> appView;
  private final DexItemFactory dexItemFactory;
  private final GraphLense graphLense;
  private final NamingLens namingLense;
  private final InternalOptions options;

  public ResourceAdapter(
      AppView<?> appView,
      DexItemFactory dexItemFactory,
      GraphLense graphLense,
      NamingLens namingLense,
      InternalOptions options) {
    this.appView = appView;
    this.dexItemFactory = dexItemFactory;
    this.graphLense = graphLense;
    this.namingLense = namingLense;
    this.options = options;
  }

  public DataEntryResource adaptIfNeeded(DataEntryResource file) {
    // Adapt name, if needed.
    ProguardPathFilter adaptResourceFileNamesFilter =
        options.getProguardConfiguration().getAdaptResourceFilenames();
    String name =
        adaptResourceFileNamesFilter.isEnabled()
                && !file.getName().toLowerCase().endsWith(FileUtils.CLASS_EXTENSION)
                && adaptResourceFileNamesFilter.matches(file.getName())
            ? adaptFileName(file)
            : file.getName();
    assert name != null;
    // Adapt contents, if needed.
    ProguardPathFilter adaptResourceFileContentsFilter =
        options.getProguardConfiguration().getAdaptResourceFileContents();
    byte[] contents =
        adaptResourceFileContentsFilter.isEnabled()
                && !file.getName().toLowerCase().endsWith(FileUtils.CLASS_EXTENSION)
                && adaptResourceFileContentsFilter.matches(file.getName())
            ? adaptFileContents(file)
            : null;
    // Return a new resource if the name or contents changed. Otherwise return the original
    // resource as it was.
    if (contents != null) {
      // File contents was adapted. Return a new resource that has the new contents, and a new name,
      // if the filename was adapted.
      return DataEntryResource.fromBytes(contents, name, file.getOrigin());
    }
    if (!name.equals(file.getName())) {
      // File contents was not adapted, but filename was.
      return file.withName(name);
    }
    // Neither file contents nor filename was adapted.
    return file;
  }

  public DataDirectoryResource adaptIfNeeded(DataDirectoryResource directory) {
    // First check if this directory should even be in the output.
    ProguardPathFilter keepDirectoriesFilter =
        options.getProguardConfiguration().getKeepDirectories();
    if (!keepDirectoriesFilter.matches(directory.getName())) {
      return null;
    }
    return DataDirectoryResource.fromName(adaptDirectoryName(directory), directory.getOrigin());
  }

  public boolean isService(DataEntryResource file) {
    return file.getName().startsWith(AppServices.SERVICE_DIRECTORY_NAME);
  }

  private String adaptFileName(DataEntryResource file) {
    FileNameAdapter adapter =
        file.getName().startsWith(AppServices.SERVICE_DIRECTORY_NAME)
            ? new ServiceFileNameAdapter(file.getName())
            : new DefaultFileNameAdapter(file.getName());
    if (adapter.run()) {
      return adapter.getResult();
    }
    return file.getName();
  }

  private String adaptDirectoryName(DataDirectoryResource file) {
    DirectoryNameAdapter adapter = new DirectoryNameAdapter(file.getName());
    if (adapter.run()) {
      return adapter.getResult();
    }
    return file.getName();
  }

  // According to the Proguard documentation, the resource files should be parsed and written using
  // the platform's default character set.
  private byte[] adaptFileContents(DataEntryResource file) {
    try (InputStream in = file.getByteStream()) {
      byte[] bytes = ByteStreams.toByteArray(in);
      String contents = new String(bytes, Charset.defaultCharset());

      FileContentsAdapter adapter = new FileContentsAdapter(contents);
      if (adapter.run()) {
        return adapter.getResult().getBytes(Charset.defaultCharset());
      }
    } catch (ResourceException e) {
      options.reporter.error(
          new StringDiagnostic("Failed to open input: " + e.getMessage(), file.getOrigin()));
    } catch (Exception e) {
      options.reporter.error(new ExceptionDiagnostic(e, file.getOrigin()));
    }
    // Return null to signal that the file contents did not change. Otherwise we would have to copy
    // the original file for no reason.
    return null;
  }

  private abstract class StringAdapter {

    protected final String contents;
    private final StringBuilder result = new StringBuilder();

    // If any type names in `contents` have been updated. If this flag is still true in the end,
    // then we can simply use the resource as it was.
    private boolean changed = false;
    private int outputFrom = 0;
    private int position = 0;

    // When renaming Java type names, the adapter always looks for the longest name to rewrite.
    // For example, if there is a resource with the name "foo/bar/C$X$Y.txt", then the adapter will
    // check if there is a renaming for the type "foo.bar.C$X$Y". If there is no such renaming, then
    // -adaptresourcefilenames works in such a way that "foo/bar/C$X" should be rewritten if there
    // is a renaming for the type "foo.bar.C$X". Therefore, when scanning forwards to read the
    // substring "foo/bar/C$X$Y", this adapter records the positions of the two '$' characters in
    // the stack `prefixEndPositionsExclusive`, such that it can easily backtrack to the previously
    // valid, but shorter Java type name.
    //
    // Note that there is no backtracking for -adaptresourcefilecontents.
    private final IntStack prefixEndPositionsExclusive;

    public StringAdapter(String contents) {
      this.contents = contents;
      this.prefixEndPositionsExclusive = allowRenamingOfPrefixes() ? new IntArrayList() : null;
    }

    public boolean run() {
      do {
        handleMisc();
        handleJavaType();
      } while (!eof());
      if (changed) {
        // At least one type was renamed. We need to flush all characters in `contents` that follow
        // the last type that was renamed.
        outputRangeFromInput(outputFrom, contents.length());
      } else {
        // No types were renamed. In this case the adapter should simply have scanned through
        // `contents`, without outputting anything to `result`.
        assert outputFrom == 0;
        assert result.toString().isEmpty();
      }
      return changed;
    }

    public String getResult() {
      assert changed;
      return result.toString();
    }

    // Forwards the cursor until the current character is a Java identifier part.
    private void handleMisc() {
      while (!eof() && !Character.isJavaIdentifierPart(contents.charAt(position))) {
        position++;
      }
    }

    // Reads a Java type from the current position in `contents`, and then checks if the given
    // type has been renamed.
    private void handleJavaType() {
      if (eof()) {
        return;
      }

      assert !allowRenamingOfPrefixes() || prefixEndPositionsExclusive.isEmpty();

      assert Character.isJavaIdentifierPart(contents.charAt(position));
      int start = position++;
      while (!eof()) {
        char currentChar = contents.charAt(position);
        if (Character.isJavaIdentifierPart(currentChar)) {
          if (allowRenamingOfPrefixes()
              && shouldRecordPrefix(currentChar)
              && isRenamingCandidate(start, position)) {
            prefixEndPositionsExclusive.push(position);
          }
          position++;
          continue;
        }

        if (currentChar == getClassNameSeparator()
            && !eof(position + 1)
            && Character.isJavaIdentifierPart(contents.charAt(position + 1))) {
          if (allowRenamingOfPrefixes()
              && shouldRecordPrefix(currentChar)
              && isRenamingCandidate(start, position)) {
            prefixEndPositionsExclusive.push(position);
          }
          // Consume the separator and the Java identifier part that follows the separator.
          position += 2;
          continue;
        }

        // Not a valid extension of the type name.
        break;
      }

      if (allowRenamingOfPrefixes() && eof() && isRenamingCandidate(start, position)) {
        prefixEndPositionsExclusive.push(position);
      }

      boolean renamingSucceeded =
          isRenamingCandidate(start, position) && renameJavaTypeInRange(start, position);
      if (!renamingSucceeded && allowRenamingOfPrefixes()) {
        while (!prefixEndPositionsExclusive.isEmpty() && !renamingSucceeded) {
          int prefixEndExclusive = prefixEndPositionsExclusive.popInt();
          assert isRenamingCandidate(start, prefixEndExclusive);
          renamingSucceeded = handlePrefix(start, prefixEndExclusive);
        }
      }

      if (allowRenamingOfPrefixes()) {
        while (!prefixEndPositionsExclusive.isEmpty()) {
          prefixEndPositionsExclusive.popInt();
        }
      }
    }

    // Returns true if the Java type in the range [from; toExclusive[ was renamed.
    protected boolean renameJavaTypeInRange(int from, int toExclusive) {
      String javaType = contents.substring(from, toExclusive);
      if (getClassNameSeparator() != '.') {
        javaType = javaType.replace(getClassNameSeparator(), '.');
      }
      DexString descriptor =
          dexItemFactory.lookupString(
              DescriptorUtils.javaTypeToDescriptorIgnorePrimitives(javaType));
      DexType dexType = descriptor != null ? dexItemFactory.lookupType(descriptor) : null;
      if (dexType != null) {
        DexString renamedDescriptor = namingLense.lookupDescriptor(graphLense.lookupType(dexType));
        if (!descriptor.equals(renamedDescriptor)) {
          String renamedJavaType =
              DescriptorUtils.descriptorToJavaType(renamedDescriptor.toSourceString());
          // Need to flush all changes up to and excluding 'from', and then output the renamed
          // type.
          outputRangeFromInput(outputFrom, from);
          outputJavaType(
              getClassNameSeparator() != '.'
                  ? renamedJavaType.replace('.', getClassNameSeparator())
                  : renamedJavaType);
          outputFrom = toExclusive;
          changed = true;
          return true;
        }
      }
      return false;
    }

    // Returns true if the Java package in the range [from; toExclusive[ was renamed.
    protected boolean renameJavaPackageInRange(int from, int toExclusive) {
      String javaPackage = contents.substring(from, toExclusive);
      if (getClassNameSeparator() != '/') {
        javaPackage = javaPackage.replace(getClassNameSeparator(), '/');
      }
      String minifiedJavaPackage = namingLense.lookupPackageName(javaPackage);
      if (!javaPackage.equals(minifiedJavaPackage)) {
        outputRangeFromInput(outputFrom, from);
        outputJavaType(
            getClassNameSeparator() != '/'
                ? minifiedJavaPackage.replace('/', getClassNameSeparator())
                : minifiedJavaPackage);
        outputFrom = toExclusive;
        changed = true;
        return true;
      }
      return false;
    }

    protected abstract char getClassNameSeparator();

    protected abstract boolean allowRenamingOfPrefixes();

    protected abstract boolean shouldRecordPrefix(char c);

    protected abstract boolean handlePrefix(int from, int toExclusive);

    protected abstract boolean isRenamingCandidate(int from, int toExclusive);

    private void outputRangeFromInput(int from, int toExclusive) {
      if (from < toExclusive) {
        result.append(contents, from, toExclusive);
      }
    }

    private void outputJavaType(String s) {
      result.append(s);
    }

    protected boolean eof() {
      return eof(position);
    }

    protected boolean eof(int position) {
      return position == contents.length();
    }
  }

  private class FileContentsAdapter extends StringAdapter {

    public FileContentsAdapter(String fileContents) {
      super(fileContents);
    }

    @Override
    public char getClassNameSeparator() {
      return '.';
    }

    @Override
    public boolean allowRenamingOfPrefixes() {
      return false;
    }

    @Override
    public boolean shouldRecordPrefix(char c) {
      throw new Unreachable();
    }

    @Override
    protected boolean handlePrefix(int from, int toExclusive) {
      throw new Unreachable();
    }

    @Override
    public boolean isRenamingCandidate(int from, int toExclusive) {
      // If the Java type starts with '-' or '.', it should not be renamed.
      return (from <= 0 || !isDashOrDot(contents.charAt(from - 1)))
          && (eof(toExclusive) || !isDashOrDot(contents.charAt(toExclusive)));
    }

    private boolean isDashOrDot(char c) {
      return c == '.' || c == '-';
    }
  }

  private abstract class FileNameAdapter extends StringAdapter {
    public FileNameAdapter(String filename) {
      super(filename);
    }

    @Override
    public char getClassNameSeparator() {
      return '/';
    }

    @Override
    public boolean allowRenamingOfPrefixes() {
      return true;
    }

    @Override
    public boolean shouldRecordPrefix(char c) {
      return !Character.isLetterOrDigit(c);
    }

    @Override
    protected boolean handlePrefix(int from, int toExclusive) {
      if (eof(toExclusive) || contents.charAt(toExclusive) == '/') {
        return renameJavaPackageInRange(from, toExclusive);
      }
      return renameJavaTypeInRange(from, toExclusive);
    }
  }

  private class DefaultFileNameAdapter extends FileNameAdapter {
    public DefaultFileNameAdapter(String filename) {
      super(filename);
    }

    @Override
    public boolean isRenamingCandidate(int from, int toExclusive) {
      return from == 0 && !eof(toExclusive);
    }
  }

  private class ServiceFileNameAdapter extends FileNameAdapter {
    public ServiceFileNameAdapter(String filename) {
      super(filename);
    }

    @Override
    public char getClassNameSeparator() {
      return '.';
    }

    @Override
    public boolean allowRenamingOfPrefixes() {
      return false;
    }

    @Override
    public boolean isRenamingCandidate(int from, int toExclusive) {
      return from == AppServices.SERVICE_DIRECTORY_NAME.length() && eof(toExclusive);
    }
  }

  private class DirectoryNameAdapter extends FileNameAdapter {
    public DirectoryNameAdapter(String filename) {
      super(filename);
    }

    @Override
    public boolean isRenamingCandidate(int from, int toExclusive) {
      return from == 0;
    }
  }
}
