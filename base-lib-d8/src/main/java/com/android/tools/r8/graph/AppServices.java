// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.DataDirectoryResource;
import com.android.tools.r8.DataEntryResource;
import com.android.tools.r8.DataResourceProvider;
import com.android.tools.r8.DataResourceProvider.Visitor;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

/** A description of the services and their implementations found in META-INF/services/. */
public class AppServices {

  public static final String SERVICE_DIRECTORY_NAME = "META-INF/services/";

  private final AppView<?> appView;

  // Mapping from service types to service implementation types.
  private final Map<DexType, List<DexType>> services;

  private AppServices(AppView<?> appView, Map<DexType, List<DexType>> services) {
    this.appView = appView;
    this.services = services;
  }

  public boolean isEmpty() {
    return services.isEmpty();
  }

  public Set<DexType> allServiceTypes() {
    assert verifyRewrittenWithLens();
    return services.keySet();
  }

  public List<DexType> serviceImplementationsFor(DexType serviceType) {
    assert verifyRewrittenWithLens();
    assert services.containsKey(serviceType);
    List<DexType> serviceImplementationTypes = services.get(serviceType);
    if (serviceImplementationTypes == null) {
      assert false
          : "Unexpected attempt to get service implementations for non-service type `"
              + serviceType.toSourceString()
              + "`";
      return ImmutableList.of();
    }
    return serviceImplementationTypes;
  }

  public AppServices rewrittenWithLens(GraphLense graphLens) {
    ImmutableMap.Builder<DexType, List<DexType>> rewrittenServices = ImmutableMap.builder();
    for (Entry<DexType, List<DexType>> entry : services.entrySet()) {
      DexType rewrittenServiceType = graphLens.lookupType(entry.getKey());
      ImmutableList.Builder<DexType> rewrittenServiceImplementationTypes = ImmutableList.builder();
      for (DexType serviceImplementationType : entry.getValue()) {
        rewrittenServiceImplementationTypes.add(graphLens.lookupType(serviceImplementationType));
      }
      rewrittenServices.put(rewrittenServiceType, rewrittenServiceImplementationTypes.build());
    }
    return new AppServices(appView, rewrittenServices.build());
  }

  public AppServices prunedCopy(Collection<DexType> removedClasses) {
    ImmutableMap.Builder<DexType, List<DexType>> rewrittenServicesBuilder = ImmutableMap.builder();
    for (Entry<DexType, List<DexType>> entry : services.entrySet()) {
      if (!removedClasses.contains(entry.getKey())) {
        DexType serviceType = entry.getKey();
        ImmutableList.Builder<DexType> rewrittenServiceImplementationTypesBuilder =
            ImmutableList.builder();
        for (DexType serviceImplementationType : entry.getValue()) {
          if (!removedClasses.contains(serviceImplementationType)) {
            rewrittenServiceImplementationTypesBuilder.add(serviceImplementationType);
          }
        }
        List<DexType> rewrittenServiceImplementationTypes =
            rewrittenServiceImplementationTypesBuilder.build();
        if (rewrittenServiceImplementationTypes.size() > 0) {
          rewrittenServicesBuilder.put(
              serviceType, rewrittenServiceImplementationTypesBuilder.build());
        }
      }
    }
    return new AppServices(appView, rewrittenServicesBuilder.build());
  }

  private boolean verifyRewrittenWithLens() {
    for (Entry<DexType, List<DexType>> entry : services.entrySet()) {
      assert entry.getKey() == appView.graphLense().lookupType(entry.getKey());
      for (DexType type : entry.getValue()) {
        assert type == appView.graphLense().lookupType(type);
      }
    }
    return true;
  }

  public void visit(BiConsumer<DexType, List<DexType>> consumer) {
    services.forEach(consumer);
  }

  public static Builder builder(AppView<?> appView) {
    return new Builder(appView);
  }

  public static class Builder {

    private final AppView<?> appView;
    private final Map<DexType, List<DexType>> services = new IdentityHashMap<>();

    private Builder(AppView<?> appView) {
      this.appView = appView;
    }

    public AppServices build() {
      for (DataResourceProvider provider : appView.appInfo().app().dataResourceProviders) {
        readServices(provider);
      }
      return new AppServices(appView, services);
    }

    private void readServices(DataResourceProvider dataResourceProvider) {
      try {
        dataResourceProvider.accept(new DataResourceProviderVisitor());
      } catch (ResourceException e) {
        throw new CompilationError(e.getMessage(), e);
      }
    }

    private class DataResourceProviderVisitor implements Visitor {

      @Override
      public void visit(DataDirectoryResource directory) {
        // Ignore.
      }

      @Override
      public void visit(DataEntryResource file) {
        try {
          String name = file.getName();
          if (name.startsWith(SERVICE_DIRECTORY_NAME)) {
            String serviceName = name.substring(SERVICE_DIRECTORY_NAME.length());
            if (DescriptorUtils.isValidJavaType(serviceName)) {
              String serviceDescriptor = DescriptorUtils.javaTypeToDescriptor(serviceName);
              DexType serviceType = appView.dexItemFactory().createType(serviceDescriptor);
              byte[] bytes = ByteStreams.toByteArray(file.getByteStream());
              String contents = new String(bytes, Charset.defaultCharset());
              List<DexType> serviceImplementations =
                  services.computeIfAbsent(serviceType, (key) -> new ArrayList<>());
              readServiceImplementationsForService(
                  contents, file.getOrigin(), serviceImplementations);
            }
          }
        } catch (IOException | ResourceException e) {
          throw new CompilationError(e.getMessage(), e);
        }
      }

      private void readServiceImplementationsForService(
          String contents, Origin origin, List<DexType> serviceImplementations) {
        if (contents != null) {
          Arrays.stream(contents.split(System.lineSeparator()))
              .map(String::trim)
              .map(this::prefixUntilCommentChar)
              .filter(line -> !line.isEmpty())
              .filter(DescriptorUtils::isValidJavaType)
              .map(DescriptorUtils::javaTypeToDescriptor)
              .map(appView.dexItemFactory()::createType)
              .filter(
                  serviceImplementationType -> {
                    if (!serviceImplementationType.isClassType()) {
                      // Should never happen.
                      appView
                          .options()
                          .reporter
                          .warning(
                              new StringDiagnostic(
                                  "Unexpected service implementation found in META-INF/services/: `"
                                      + serviceImplementationType.toSourceString()
                                      + "`.",
                                  origin));
                      return false;
                    }
                    // Only keep one of each implementation type in the list.
                    return !serviceImplementations.contains(serviceImplementationType);
                  })
              .forEach(serviceImplementations::add);
        }
      }

      private String prefixUntilCommentChar(String line) {
        int commentCharIndex = line.indexOf('#');
        return commentCharIndex > -1 ? line.substring(0, commentCharIndex) : line;
      }
    }
  }
}
