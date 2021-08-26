// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.BaseCompilerCommand;
import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.ClassFileConsumer;
import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DataResourceConsumer;
import com.android.tools.r8.DexFilePerClassFileConsumer;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DexIndexedConsumer.ForwardingConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.ProgramConsumer;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.StringConsumer;
import com.android.tools.r8.origin.Origin;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class AndroidAppConsumers {

  private final AndroidApp.Builder builder = AndroidApp.builder();
  private boolean closed = false;

  private ProgramConsumer programConsumer = null;
  private StringConsumer proguardMapConsumer = null;

  public AndroidAppConsumers() {
    // Nothing to do.
  }

  public AndroidAppConsumers(BaseCompilerCommand.Builder builder) {
    builder.setProgramConsumer(wrapProgramConsumer(builder.getProgramConsumer()));
  }

  public AndroidAppConsumers(InternalOptions options) {
    options.programConsumer = wrapProgramConsumer(options.programConsumer);
    options.proguardMapConsumer = wrapProguardMapConsumer(options.proguardMapConsumer);
  }

  public ProgramConsumer wrapProgramConsumer(ProgramConsumer consumer) {
    assert programConsumer == null;
    if (consumer instanceof ClassFileConsumer) {
      wrapClassFileConsumer((ClassFileConsumer) consumer);
    } else if (consumer instanceof DexIndexedConsumer) {
      wrapDexIndexedConsumer((DexIndexedConsumer) consumer);
    } else if (consumer instanceof DexFilePerClassFileConsumer) {
      wrapDexFilePerClassFileConsumer((DexFilePerClassFileConsumer) consumer);
    } else {
      // TODO(zerny): Refine API to disallow running without a program consumer.
      assert consumer == null;
      wrapDexIndexedConsumer(null);
    }
    assert programConsumer != null;
    return programConsumer;
  }

  public StringConsumer wrapProguardMapConsumer(StringConsumer consumer) {
    assert proguardMapConsumer == null;
    if (consumer != null) {
      proguardMapConsumer =
          new StringConsumer.ForwardingConsumer(consumer) {
            StringBuilder stringBuilder = null;

            @Override
            public void accept(String string, DiagnosticsHandler handler) {
              super.accept(string, handler);
              if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
              }
              stringBuilder.append(string);
            }

            @Override
            public void finished(DiagnosticsHandler handler) {
              super.finished(handler);
              if (stringBuilder != null) {
                builder.setProguardMapOutputData(stringBuilder.toString());
              }
            }
          };
    }
    return proguardMapConsumer;
  }

  public DexIndexedConsumer wrapDexIndexedConsumer(DexIndexedConsumer consumer) {
    assert programConsumer == null;
    DexIndexedConsumer wrapped =
        new ForwardingConsumer(consumer) {

          // Sort the files by id so that their order is deterministic. Some tests depend on this.
          private Int2ReferenceSortedMap<DescriptorsWithContents> files =
              new Int2ReferenceAVLTreeMap<>();

          @Override
          public void accept(
              int fileIndex,
              ByteDataView data,
              Set<String> descriptors,
              DiagnosticsHandler handler) {
            super.accept(fileIndex, data, descriptors, handler);
            addDexFile(fileIndex, data.copyByteData(), descriptors);
          }

          @Override
          public void finished(DiagnosticsHandler handler) {
            super.finished(handler);
            if (!closed) {
              closed = true;
              files.forEach((v, d) -> builder.addDexProgramData(d.contents, d.descriptors));
              files = null;
            } else {
              assert getDataResourceConsumer() != null;
            }
          }

          @Override
          public DataResourceConsumer getDataResourceConsumer() {
            DataResourceConsumer dataResourceConsumer =
                consumer != null ? consumer.getDataResourceConsumer() : null;
            return new DataResourceConsumer() {

              @Override
              public void accept(
                  DataDirectoryResource directory, DiagnosticsHandler diagnosticsHandler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.accept(directory, diagnosticsHandler);
                }
              }

              @Override
              public void accept(DataEntryResource file, DiagnosticsHandler diagnosticsHandler) {
                try {
                  byte[] bytes = ByteStreams.toByteArray(file.getByteStream());
                  DataEntryResource copy =
                      DataEntryResource.fromBytes(bytes, file.getName(), file.getOrigin());
                  builder.addDataResource(copy);
                  if (dataResourceConsumer != null) {
                    dataResourceConsumer.accept(copy, diagnosticsHandler);
                  }
                } catch (IOException | ResourceException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public void finished(DiagnosticsHandler handler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.finished(handler);
                }
              }
            };
          }

          synchronized void addDexFile(int fileIndex, byte[] data, Set<String> descriptors) {
            files.put(fileIndex, new DescriptorsWithContents(descriptors, data));
          }
        };
    programConsumer = wrapped;
    return wrapped;
  }

  public DexFilePerClassFileConsumer wrapDexFilePerClassFileConsumer(
      DexFilePerClassFileConsumer consumer) {
    assert programConsumer == null;
    DexFilePerClassFileConsumer wrapped =
        new DexFilePerClassFileConsumer.ForwardingConsumer(consumer) {

          // Sort the files by their name for good measure.
          private TreeMap<String, DescriptorsWithContents> files = new TreeMap<>();

          @Override
          public void accept(
              String primaryClassDescriptor,
              ByteDataView data,
              Set<String> descriptors,
              DiagnosticsHandler handler) {
            super.accept(primaryClassDescriptor, data, descriptors, handler);
            addDexFile(primaryClassDescriptor, data.copyByteData(), descriptors);
          }

          synchronized void addDexFile(
              String primaryClassDescriptor, byte[] data, Set<String> descriptors) {
            files.put(primaryClassDescriptor, new DescriptorsWithContents(descriptors, data));
          }

          @Override
          public void finished(DiagnosticsHandler handler) {
            super.finished(handler);
            if (!closed) {
              closed = true;
              files.forEach((v, d) -> builder.addDexProgramData(d.contents, d.descriptors, v));
              files = null;
            } else {
              assert getDataResourceConsumer() != null;
            }
          }

          @Override
          public DataResourceConsumer getDataResourceConsumer() {
            DataResourceConsumer dataResourceConsumer =
                consumer != null ? consumer.getDataResourceConsumer() : null;
            return new DataResourceConsumer() {

              @Override
              public void accept(
                  DataDirectoryResource directory, DiagnosticsHandler diagnosticsHandler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.accept(directory, diagnosticsHandler);
                }
              }

              @Override
              public void accept(DataEntryResource file, DiagnosticsHandler diagnosticsHandler) {
                try {
                  byte[] bytes = ByteStreams.toByteArray(file.getByteStream());
                  DataEntryResource copy =
                      DataEntryResource.fromBytes(bytes, file.getName(), file.getOrigin());
                  builder.addDataResource(copy);
                  if (dataResourceConsumer != null) {
                    dataResourceConsumer.accept(copy, diagnosticsHandler);
                  }
                } catch (IOException | ResourceException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public void finished(DiagnosticsHandler handler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.finished(handler);
                }
              }
            };
          }
        };
    programConsumer = wrapped;
    return wrapped;
  }

  public ClassFileConsumer wrapClassFileConsumer(ClassFileConsumer consumer) {
    assert programConsumer == null;
    ClassFileConsumer wrapped =
        new ClassFileConsumer.ForwardingConsumer(consumer) {

          private List<DescriptorsWithContents> files = new ArrayList<>();

          @Override
          public void accept(ByteDataView data, String descriptor, DiagnosticsHandler handler) {
            super.accept(data, descriptor, handler);
            addClassFile(data.copyByteData(), descriptor);
          }

          synchronized void addClassFile(byte[] data, String descriptor) {
            files.add(new DescriptorsWithContents(Collections.singleton(descriptor), data));
          }

          @Override
          public void finished(DiagnosticsHandler handler) {
            super.finished(handler);
            if (!closed) {
              closed = true;
              files.forEach(
                  d -> builder.addClassProgramData(d.contents, Origin.unknown(), d.descriptors));
              files = null;
            } else {
              assert getDataResourceConsumer() != null;
            }
          }

          @Override
          public DataResourceConsumer getDataResourceConsumer() {
            DataResourceConsumer dataResourceConsumer =
                consumer != null ? consumer.getDataResourceConsumer() : null;
            return new DataResourceConsumer() {

              @Override
              public void accept(
                  DataDirectoryResource directory, DiagnosticsHandler diagnosticsHandler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.accept(directory, diagnosticsHandler);
                }
              }

              @Override
              public void accept(DataEntryResource file, DiagnosticsHandler diagnosticsHandler) {
                try {
                  byte[] bytes = ByteStreams.toByteArray(file.getByteStream());
                  DataEntryResource copy =
                      DataEntryResource.fromBytes(bytes, file.getName(), file.getOrigin());
                  builder.addDataResource(copy);
                  if (dataResourceConsumer != null) {
                    dataResourceConsumer.accept(copy, diagnosticsHandler);
                  }
                } catch (IOException | ResourceException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public void finished(DiagnosticsHandler handler) {
                if (dataResourceConsumer != null) {
                  dataResourceConsumer.finished(handler);
                }
              }
            };
          }
        };
    programConsumer = wrapped;
    return wrapped;
  }

  public AndroidApp build() {
    assert closed;
    return builder.build();
  }

  private static class DescriptorsWithContents {

    final Set<String> descriptors;
    final byte[] contents;

    private DescriptorsWithContents(Set<String> descriptors, byte[] contents) {
      this.descriptors = descriptors;
      this.contents = contents;
    }
  }
}
