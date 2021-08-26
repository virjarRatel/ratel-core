// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.ByteBufferProvider;
import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DataResourceConsumer;
import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.DataResourceProvider.Visitor;
import com.android.tools.r8.DexFilePerClassFileConsumer;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.ProgramConsumer;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.dex.FileWriter.ByteBufferResult;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.features.FeatureSplitConfiguration.DataResourceProvidersAndConsumer;
import com.android.tools.r8.graph.AppServices;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationDirectory;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedArray;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.EnclosingMethodAttribute;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.naming.ProguardMapSupplier;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ApplicationWriter {

  public final DexApplication application;
  public final AppView<?> appView;
  public final GraphLense graphLense;
  public final NamingLens namingLens;
  public final InternalOptions options;
  public List<Marker> markers;
  public List<DexString> markerStrings;

  public DexIndexedConsumer programConsumer;
  public final ProguardMapSupplier proguardMapSupplier;

  private static class SortAnnotations extends MixedSectionCollection {

    @Override
    public boolean add(DexAnnotationSet dexAnnotationSet) {
      // Annotation sets are sorted by annotation types.
      dexAnnotationSet.sort();
      return true;
    }

    @Override
    public boolean add(DexAnnotation annotation) {
      // The elements of encoded annotation must be sorted by name.
      annotation.annotation.sort();
      return true;
    }

    @Override
    public boolean add(DexEncodedArray dexEncodedArray) {
      // Dex values must potentially be sorted, eg, for DexValueAnnotation.
      for (DexValue value : dexEncodedArray.values) {
        value.sort();
      }
      return true;
    }

    @Override
    public boolean add(DexProgramClass dexClassData) {
      return true;
    }

    @Override
    public boolean add(DexCode dexCode) {
      return true;
    }

    @Override
    public boolean add(DexDebugInfo dexDebugInfo) {
      return true;
    }

    @Override
    public boolean add(DexTypeList dexTypeList) {
      return true;
    }

    @Override
    public boolean add(ParameterAnnotationsList parameterAnnotationsList) {
      return true;
    }

    @Override
    public boolean setAnnotationsDirectoryForClass(DexProgramClass clazz,
        DexAnnotationDirectory annotationDirectory) {
      return true;
    }
  }

  public ApplicationWriter(
      DexApplication application,
      AppView<?> appView,
      InternalOptions options,
      List<Marker> markers,
      GraphLense graphLense,
      NamingLens namingLens,
      ProguardMapSupplier proguardMapSupplier) {
    this(
        application,
        appView,
        options,
        markers,
        graphLense,
        namingLens,
        proguardMapSupplier,
        null);
  }

  public ApplicationWriter(
      DexApplication application,
      AppView<?> appView,
      InternalOptions options,
      List<Marker> markers,
      GraphLense graphLense,
      NamingLens namingLens,
      ProguardMapSupplier proguardMapSupplier,
      DexIndexedConsumer consumer) {
    assert application != null;
    this.application = application;
    this.appView = appView;
    assert options != null;
    this.options = options;
    this.markers = markers;
    this.graphLense = graphLense;
    this.namingLens = namingLens;
    this.proguardMapSupplier = proguardMapSupplier;
    this.programConsumer = consumer;
  }

  private Iterable<VirtualFile> distribute(ExecutorService executorService)
      throws ExecutionException, IOException {
    // Distribute classes into dex files.
    VirtualFile.Distributor distributor;
    if (options.isGeneratingDexFilePerClassFile()) {
      distributor = new VirtualFile.FilePerInputClassDistributor(this,
          options.getDexFilePerClassFileConsumer().combineSyntheticClassesWithPrimaryClass());
    } else if (!options.canUseMultidex()
        && options.mainDexKeepRules.isEmpty()
        && application.mainDexList.isEmpty()
        && options.enableMainDexListCheck) {
      distributor = new VirtualFile.MonoDexDistributor(this, options);
    } else {
      distributor = new VirtualFile.FillFilesDistributor(this, options, executorService);
    }

    Iterable<VirtualFile> result = distributor.run();
    return result;
  }

  /**
   * For each class within a virtual file, this function insert a string that contains the
   * checksum information about that class.
   *
   * This needs to be done after distribute but before dex string sorting.
   */
  private void encodeChecksums(Iterable<VirtualFile> files) {
    List<DexProgramClass> classes = application.classes();
    Object2LongMap<String> inputChecksums = new Object2LongOpenHashMap<>(classes.size());
    for (DexProgramClass clazz : classes) {
      inputChecksums.put(clazz.getType().descriptor.toASCIIString(), clazz.getChecksum());
    }
    for (VirtualFile file : files) {
      ClassesChecksum toWrite = new ClassesChecksum();
      for (String desc : file.getClassDescriptors()) {
        toWrite.addChecksum(desc, inputChecksums.getLong(desc));
      }
      file.injectString(application.dexItemFactory.createString(toWrite.toJsonString()));
    }
  }

  public void write(ExecutorService executorService) throws IOException, ExecutionException {
    application.timing.begin("DexApplication.write");
    ProguardMapSupplier.ProguardMapAndId proguardMapAndId = null;
    if (proguardMapSupplier != null && options.proguardMapConsumer != null) {
      proguardMapAndId = proguardMapSupplier.getProguardMapAndId();
    }

    // If we do have a map then we're called from R8. In that case we have exactly one marker.
    assert proguardMapAndId == null || (markers != null && markers.size() == 1);

    if (markers != null && !markers.isEmpty()) {
      if (proguardMapAndId != null) {
        markers.get(0).setPgMapId(proguardMapAndId.id);
      }
      markerStrings = new ArrayList<>(markers.size());
      for (Marker marker : markers) {
        markerStrings.add(application.dexItemFactory.createString(marker.toString()));
      }
    }
    try {
      insertAttributeAnnotations();

      // Generate the dex file contents.
      List<Future<Boolean>> dexDataFutures = new ArrayList<>();
      Iterable<VirtualFile> virtualFiles = distribute(executorService);
      if (options.encodeChecksums) {
        encodeChecksums(virtualFiles);
      }
      application.dexItemFactory.sort(namingLens);
      assert markers == null
          || markers.isEmpty()
          || application.dexItemFactory.extractMarkers() != null;

      SortAnnotations sortAnnotations = new SortAnnotations();
      application.classes().forEach((clazz) -> clazz.addDependencies(sortAnnotations));

      for (VirtualFile virtualFile : virtualFiles) {
        if (virtualFile.isEmpty()) {
          continue;
        }
        dexDataFutures.add(
            executorService.submit(
                () -> {
                  ProgramConsumer consumer;
                  ByteBufferProvider byteBufferProvider;
                  if (programConsumer != null) {
                    consumer = programConsumer;
                    byteBufferProvider = programConsumer;
                  } else if (virtualFile.getPrimaryClassDescriptor() != null) {
                    consumer = options.getDexFilePerClassFileConsumer();
                    byteBufferProvider = options.getDexFilePerClassFileConsumer();
                  } else {
                    if (virtualFile.getFeatureSplit() != null) {
                      ProgramConsumer featureConsumer =
                          virtualFile.getFeatureSplit().getProgramConsumer();
                      assert featureConsumer instanceof DexIndexedConsumer;
                      consumer = featureConsumer;
                      byteBufferProvider = (DexIndexedConsumer) featureConsumer;
                    } else {
                      consumer = options.getDexIndexedConsumer();
                      byteBufferProvider = options.getDexIndexedConsumer();
                    }
                  }
                  ObjectToOffsetMapping objectMapping = virtualFile.computeMapping(application);
                  MethodToCodeObjectMapping codeMapping =
                      rewriteCodeWithJumboStrings(
                          objectMapping, virtualFile.classes(), application);
                  ByteBufferResult result =
                      writeDexFile(objectMapping, codeMapping, byteBufferProvider);
                  ByteDataView data =
                      new ByteDataView(
                          result.buffer.array(), result.buffer.arrayOffset(), result.length);
                  if (consumer instanceof DexFilePerClassFileConsumer) {
                    ((DexFilePerClassFileConsumer) consumer)
                        .accept(
                            virtualFile.getPrimaryClassDescriptor(),
                            data,
                            virtualFile.getClassDescriptors(),
                            options.reporter);
                  } else {
                    ((DexIndexedConsumer) consumer)
                        .accept(
                            virtualFile.getId(),
                            data,
                            virtualFile.getClassDescriptors(),
                            options.reporter);
                  }
                  // Release use of the backing buffer now that accept has returned.
                  data.invalidate();
                  byteBufferProvider.releaseByteBuffer(result.buffer.asByteBuffer());
                  return true;
                }));
      }
      // Wait for all files to be processed before moving on.
      ThreadUtils.awaitFutures(dexDataFutures);
      // Fail if there are pending errors, e.g., the program consumers may have reported errors.
      options.reporter.failIfPendingErrors();
      // Supply info to all additional resource consumers.
      supplyAdditionalConsumers(
          application,
          appView,
          graphLense,
          namingLens,
          options,
          proguardMapAndId == null ? null : proguardMapAndId.map);
    } finally {
      application.timing.end();
    }
  }

  public static void supplyAdditionalConsumers(
      DexApplication application,
      AppView<?> appView,
      GraphLense graphLense,
      NamingLens namingLens,
      InternalOptions options,
      String proguardMapContent) {
    if (options.configurationConsumer != null) {
      ExceptionUtils.withConsumeResourceHandler(
          options.reporter, options.configurationConsumer,
          options.getProguardConfiguration().getParsedConfiguration());
      ExceptionUtils.withFinishedResourceHandler(options.reporter, options.configurationConsumer);
    }
    if (proguardMapContent != null) {
      assert validateProguardMapParses(proguardMapContent);
      ExceptionUtils.withConsumeResourceHandler(
          options.reporter, options.proguardMapConsumer, proguardMapContent);
      ExceptionUtils.withFinishedResourceHandler(options.reporter, options.proguardMapConsumer);
    }
    if (options.mainDexListConsumer != null) {
      ExceptionUtils.withConsumeResourceHandler(
          options.reporter, options.mainDexListConsumer, writeMainDexList(application, namingLens));
      ExceptionUtils.withFinishedResourceHandler(options.reporter, options.mainDexListConsumer);
    }

    DataResourceConsumer dataResourceConsumer = options.dataResourceConsumer;
    if (dataResourceConsumer != null) {
      ImmutableList<DataResourceProvider> dataResourceProviders = application.dataResourceProviders;
      ResourceAdapter resourceAdapter =
          new ResourceAdapter(appView, application.dexItemFactory, graphLense, namingLens, options);

      adaptAndPassDataResources(
          options, dataResourceConsumer, dataResourceProviders, resourceAdapter);

      // Write the META-INF/services resources. Sort on service names and keep the order from
      // the input for the implementation lines for deterministic output.
      if (!appView.appServices().isEmpty()) {
        appView
            .appServices()
            .visit(
                (DexType service, List<DexType> implementations) -> {
                  String serviceName =
                      DescriptorUtils.descriptorToJavaType(
                          namingLens.lookupDescriptor(service).toString());
                  dataResourceConsumer.accept(
                      DataEntryResource.fromBytes(
                          StringUtils.lines(
                                  implementations.stream()
                                      .map(namingLens::lookupDescriptor)
                                      .map(DexString::toString)
                                      .map(DescriptorUtils::descriptorToJavaType)
                                      .collect(Collectors.toList()))
                              .getBytes(),
                          AppServices.SERVICE_DIRECTORY_NAME + serviceName,
                          Origin.unknown()),
                      options.reporter);
                });
      }
    }

    if (options.featureSplitConfiguration != null) {
      for (DataResourceProvidersAndConsumer entry :
          options.featureSplitConfiguration.getDataResourceProvidersAndConsumers()) {
        ResourceAdapter resourceAdapter =
            new ResourceAdapter(
                appView, application.dexItemFactory, graphLense, namingLens, options);
        adaptAndPassDataResources(
            options, entry.getConsumer(), entry.getProviders(), resourceAdapter);
      }
    }
  }

  private static void adaptAndPassDataResources(
      InternalOptions options,
      DataResourceConsumer dataResourceConsumer,
      Collection<DataResourceProvider> dataResourceProviders,
      ResourceAdapter resourceAdapter) {
    Set<String> generatedResourceNames = new HashSet<>();

    for (DataResourceProvider dataResourceProvider : dataResourceProviders) {
      try {
        dataResourceProvider.accept(
            new Visitor() {
              @Override
              public void visit(DataDirectoryResource directory) {
                DataDirectoryResource adapted = resourceAdapter.adaptIfNeeded(directory);
                if (adapted != null) {
                  dataResourceConsumer.accept(adapted, options.reporter);
                  options.reporter.failIfPendingErrors();
                }
              }

              @Override
              public void visit(DataEntryResource file) {
                if (resourceAdapter.isService(file)) {
                  // META-INF/services resources are handled below.
                  return;
                }

                DataEntryResource adapted = resourceAdapter.adaptIfNeeded(file);
                if (generatedResourceNames.add(adapted.getName())) {
                  dataResourceConsumer.accept(adapted, options.reporter);
                } else {
                  options.reporter.warning(
                      new StringDiagnostic("Resource '" + file.getName() + "' already exists."));
                }
                options.reporter.failIfPendingErrors();
              }
            });
      } catch (ResourceException e) {
        throw new CompilationError(e.getMessage(), e);
      }
    }
  }

  private static boolean validateProguardMapParses(String content) {
    try {
      ClassNameMapper.mapperFromString(content);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private void insertAttributeAnnotations() {
    // Convert inner-class attributes to DEX annotations
    for (DexProgramClass clazz : application.classes()) {
      EnclosingMethodAttribute enclosingMethod = clazz.getEnclosingMethod();
      List<InnerClassAttribute> innerClasses = clazz.getInnerClasses();
      if (enclosingMethod == null && innerClasses.isEmpty()) {
        continue;
      }

      // EnclosingMember translates directly to an enclosing class/method if present.
      List<DexAnnotation> annotations = new ArrayList<>(1 + innerClasses.size());
      if (enclosingMethod != null) {
        if (enclosingMethod.getEnclosingMethod() != null) {
          annotations.add(
              DexAnnotation.createEnclosingMethodAnnotation(
                  enclosingMethod.getEnclosingMethod(), options.itemFactory));
        } else {
          // At this point DEX can't distinguish between local classes and member classes based on
          // the enclosing class annotation itself.
          annotations.add(
              DexAnnotation.createEnclosingClassAnnotation(
                  enclosingMethod.getEnclosingClass(), options.itemFactory));
        }
      }

      // Each inner-class entry becomes a inner-class (or inner-class & enclosing-class pair) if
      // it relates to the present class. If it relates to the outer-type (and is named) it becomes
      // part of the member-classes annotation.
      if (!innerClasses.isEmpty()) {
        List<DexType> memberClasses = new ArrayList<>(innerClasses.size());
        for (InnerClassAttribute innerClass : innerClasses) {
          if (clazz.type == innerClass.getInner()) {
            if (enclosingMethod == null
                && (innerClass.getOuter() == null || innerClass.isAnonymous())) {
              options.warningMissingEnclosingMember(
                  clazz.type, clazz.origin, clazz.getInitialClassFileVersion());
            } else {
              annotations.add(
                  DexAnnotation.createInnerClassAnnotation(
                      namingLens.lookupInnerName(innerClass, options),
                      innerClass.getAccess(),
                      options.itemFactory));
              if (innerClass.getOuter() != null && innerClass.isNamed()) {
                annotations.add(
                    DexAnnotation.createEnclosingClassAnnotation(
                        innerClass.getOuter(), options.itemFactory));
              }
            }
          } else if (clazz.type == innerClass.getOuter() && innerClass.isNamed()) {
            memberClasses.add(innerClass.getInner());
          }
        }
        if (!memberClasses.isEmpty()) {
          annotations.add(
              DexAnnotation.createMemberClassesAnnotation(memberClasses, options.itemFactory));
        }
      }

      if (!annotations.isEmpty()) {
        // Append the annotations to annotations array of the class.
        DexAnnotation[] copy =
            ObjectArrays.concat(
                clazz.annotations.annotations,
                annotations.toArray(DexAnnotation.EMPTY_ARRAY),
                DexAnnotation.class);
        clazz.annotations = new DexAnnotationSet(copy);
      }

      // Clear the attribute structures now that they are represented in annotations.
      clazz.clearEnclosingMethod();
      clazz.clearInnerClasses();
    }
  }

  /**
   * Rewrites the code for all methods in the given file so that they use JumboString for at least
   * the strings that require it in mapping.
   *
   * <p>If run multiple times on a class, the lowest index that is required to be a JumboString will
   * be used.
   */
  private MethodToCodeObjectMapping rewriteCodeWithJumboStrings(
      ObjectToOffsetMapping mapping,
      Collection<DexProgramClass> classes,
      DexApplication application) {
    // Do not bail out early if forcing jumbo string processing.
    if (!options.testing.forceJumboStringProcessing) {
      // If there are no strings with jumbo indices at all this is a no-op.
      if (!mapping.hasJumboStrings()) {
        return MethodToCodeObjectMapping.fromMethodBacking();
      }
      // If the globally highest sorting string is not a jumbo string this is also a no-op.
      if (application.highestSortingString != null &&
          application.highestSortingString.slowCompareTo(mapping.getFirstJumboString()) < 0) {
        return MethodToCodeObjectMapping.fromMethodBacking();
      }
    }
    // At least one method needs a jumbo string in which case we construct a thread local mapping
    // for all code objects and write the processed results into that map.
    Map<DexEncodedMethod, DexCode> codeMapping = new IdentityHashMap<>();
    for (DexProgramClass clazz : classes) {
      boolean isSharedSynthetic = clazz.getSynthesizedFrom().size() > 1;
      clazz.forEachMethod(
          method -> {
            DexCode code =
                method.rewriteCodeWithJumboStrings(
                    mapping,
                    application.dexItemFactory,
                    options.testing.forceJumboStringProcessing);
            codeMapping.put(method, code);
            if (!isSharedSynthetic) {
              // If the class is not a shared class the mapping now has ownership of the methods
              // code object. This ensures freeing of code resources once the map entry is cleared
              // and also ensures that we don't end up using the incorrect code pointer again later!
              method.removeCode();
            }
          });
    }
    return MethodToCodeObjectMapping.fromMapBacking(codeMapping);
  }

  private ByteBufferResult writeDexFile(
      ObjectToOffsetMapping objectMapping,
      MethodToCodeObjectMapping codeMapping,
      ByteBufferProvider provider) {
    FileWriter fileWriter =
        new FileWriter(provider, objectMapping, codeMapping, application, options, namingLens);
    // Collect the non-fixed sections.
    fileWriter.collect();
    // Generate and write the bytes.
    return fileWriter.generate();
  }

  private static String mapMainDexListName(DexType type, NamingLens namingLens) {
    return DescriptorUtils.descriptorToJavaType(namingLens.lookupDescriptor(type).toString())
        .replace('.', '/') + ".class";
  }

  private static String writeMainDexList(DexApplication application, NamingLens namingLens) {
    StringBuilder builder = new StringBuilder();
    List<DexType> list = new ArrayList<>(application.mainDexList);
    list.sort(DexType::slowCompareTo);
    list.forEach(
        type -> builder.append(mapMainDexListName(type, namingLens)).append('\n'));
    return builder.toString();
  }
}
