// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.desugar.PrefixRewritingMapper.DesugarPrefixRewritingMapper;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DesugaredLibraryConfiguration {

  public static final String FALL_BACK_SYNTHESIZED_CLASSES_PACKAGE_PREFIX = "j$/";

  // TODO(b/134732760): should use DexString, DexType, DexMethod or so on when possible.
  private final AndroidApiLevel requiredCompilationAPILevel;
  private final boolean libraryCompilation;
  private final String synthesizedLibraryClassesPackagePrefix;
  private final Map<String, String> rewritePrefix;
  private final Map<DexType, DexType> emulateLibraryInterface;
  private final Map<DexString, Map<DexType, DexType>> retargetCoreLibMember;
  private final Map<DexType, DexType> backportCoreLibraryMember;
  private final Map<DexType, DexType> customConversions;
  private final List<Pair<DexType, DexString>> dontRewriteInvocation;
  private final List<String> extraKeepRules;

  public static Builder builder(DexItemFactory dexItemFactory) {
    return new Builder(dexItemFactory);
  }

  public static DesugaredLibraryConfiguration withOnlyRewritePrefixForTesting(
      Map<String, String> prefix) {
    return new DesugaredLibraryConfiguration(
        AndroidApiLevel.B,
        false,
        FALL_BACK_SYNTHESIZED_CLASSES_PACKAGE_PREFIX,
        prefix,
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static DesugaredLibraryConfiguration empty() {
    return new DesugaredLibraryConfiguration(
        AndroidApiLevel.B,
        false,
        FALL_BACK_SYNTHESIZED_CLASSES_PACKAGE_PREFIX,
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public DesugaredLibraryConfiguration(
      AndroidApiLevel requiredCompilationAPILevel,
      boolean libraryCompilation,
      String packagePrefix,
      Map<String, String> rewritePrefix,
      Map<DexType, DexType> emulateLibraryInterface,
      Map<DexString, Map<DexType, DexType>> retargetCoreLibMember,
      Map<DexType, DexType> backportCoreLibraryMember,
      Map<DexType, DexType> customConversions,
      List<Pair<DexType, DexString>> dontRewriteInvocation,
      List<String> extraKeepRules) {
    this.requiredCompilationAPILevel = requiredCompilationAPILevel;
    this.libraryCompilation = libraryCompilation;
    this.synthesizedLibraryClassesPackagePrefix = packagePrefix;
    this.rewritePrefix = rewritePrefix;
    this.emulateLibraryInterface = emulateLibraryInterface;
    this.retargetCoreLibMember = retargetCoreLibMember;
    this.backportCoreLibraryMember = backportCoreLibraryMember;
    this.customConversions = customConversions;
    this.dontRewriteInvocation = dontRewriteInvocation;
    this.extraKeepRules = extraKeepRules;
  }

  public PrefixRewritingMapper createPrefixRewritingMapper(DexItemFactory factory) {
    return rewritePrefix.isEmpty()
        ? PrefixRewritingMapper.empty()
        : new DesugarPrefixRewritingMapper(rewritePrefix, factory);
  }

  public AndroidApiLevel getRequiredCompilationApiLevel() {
    return requiredCompilationAPILevel;
  }

  public boolean isLibraryCompilation() {
    return libraryCompilation;
  }

  public String getSynthesizedLibraryClassesPackagePrefix(AppView<?> appView) {
    return appView.options().isDesugaredLibraryCompilation()
        ? synthesizedLibraryClassesPackagePrefix
        : "";
  }

  public String getSynthesizedLibraryClassesPackagePrefix() {
    return synthesizedLibraryClassesPackagePrefix;
  }

  public Map<String, String> getRewritePrefix() {
    return rewritePrefix;
  }

  public Map<DexType, DexType> getEmulateLibraryInterface() {
    return emulateLibraryInterface;
  }

  public Map<DexString, Map<DexType, DexType>> getRetargetCoreLibMember() {
    return retargetCoreLibMember;
  }

  public Map<DexType, DexType> getBackportCoreLibraryMember() {
    return backportCoreLibraryMember;
  }

  public Map<DexType, DexType> getCustomConversions() {
    return customConversions;
  }

  public List<Pair<DexType, DexString>> getDontRewriteInvocation() {
    return dontRewriteInvocation;
  }

  public List<String> getExtraKeepRules() {
    return extraKeepRules;
  }

  public static class Builder {

    private final DexItemFactory factory;

    private AndroidApiLevel requiredCompilationAPILevel;
    private boolean libraryCompilation = false;
    private String synthesizedLibraryClassesPackagePrefix =
        FALL_BACK_SYNTHESIZED_CLASSES_PACKAGE_PREFIX;
    private Map<String, String> rewritePrefix = new HashMap<>();
    private Map<DexType, DexType> emulateLibraryInterface = new HashMap<>();
    private Map<DexString, Map<DexType, DexType>> retargetCoreLibMember = new IdentityHashMap<>();
    private Map<DexType, DexType> backportCoreLibraryMember = new HashMap<>();
    private Map<DexType, DexType> customConversions = new HashMap<>();
    private List<Pair<DexType, DexString>> dontRewriteInvocation = new ArrayList<>();
    private List<String> extraKeepRules = Collections.emptyList();

    public Builder(DexItemFactory dexItemFactory) {
      this.factory = dexItemFactory;
    }

    public Builder setSynthesizedLibraryClassesPackagePrefix(String prefix) {
      String replace = prefix.replace('.', '/');
      this.synthesizedLibraryClassesPackagePrefix = replace;
      return this;
    }

    public Builder setRequiredCompilationAPILevel(AndroidApiLevel requiredCompilationAPILevel) {
      this.requiredCompilationAPILevel = requiredCompilationAPILevel;
      return this;
    }

    public Builder setProgramCompilation() {
      libraryCompilation = false;
      return this;
    }

    public Builder setLibraryCompilation() {
      libraryCompilation = true;
      return this;
    }

    public Builder setExtraKeepRules(List<String> rules) {
      extraKeepRules = rules;
      return this;
    }

    public Builder putRewritePrefix(String prefix, String rewrittenPrefix) {
      rewritePrefix.put(prefix, rewrittenPrefix);
      return this;
    }

    public Builder putEmulateLibraryInterface(
        String emulateLibraryItf, String rewrittenEmulateLibraryItf) {
      DexType interfaceType = stringClassToDexType(emulateLibraryItf);
      DexType rewrittenType = stringClassToDexType(rewrittenEmulateLibraryItf);
      emulateLibraryInterface.put(interfaceType, rewrittenType);
      return this;
    }

    public Builder putCustomConversion(String type, String conversionHolder) {
      DexType dexType = stringClassToDexType(type);
      DexType conversionType = stringClassToDexType(conversionHolder);
      customConversions.put(dexType, conversionType);
      return this;
    }

    public Builder putRetargetCoreLibMember(String retarget, String rewrittenRetarget) {
      int index = sharpIndex(retarget, "retarget core library member");
      DexString methodName = factory.createString(retarget.substring(index + 1));
      retargetCoreLibMember.putIfAbsent(methodName, new IdentityHashMap<>());
      Map<DexType, DexType> typeMap = retargetCoreLibMember.get(methodName);
      DexType originalType = stringClassToDexType(retarget.substring(0, index));
      DexType finalType = stringClassToDexType(rewrittenRetarget);
      assert !typeMap.containsKey(originalType);
      typeMap.put(originalType, finalType);
      return this;
    }

    public Builder putBackportCoreLibraryMember(String backport, String rewrittenBackport) {
      DexType backportType = stringClassToDexType(backport);
      DexType rewrittenBackportType = stringClassToDexType(rewrittenBackport);
      backportCoreLibraryMember.put(backportType, rewrittenBackportType);
      return this;
    }

    public Builder addDontRewriteInvocation(String dontRewriteInvocation) {
      int index = sharpIndex(dontRewriteInvocation, "don't rewrite");
      this.dontRewriteInvocation.add(
          new Pair<>(
              stringClassToDexType(dontRewriteInvocation.substring(0, index)),
              factory.createString(dontRewriteInvocation.substring(index + 1))));
      return this;
    }

    private int sharpIndex(String typeAndSelector, String descr) {
      int index = typeAndSelector.lastIndexOf('#');
      if (index <= 0 || index >= typeAndSelector.length() - 1) {
        throw new CompilationError(
            "Invalid " + descr + " specification (# position) in " + typeAndSelector + ".");
      }
      return index;
    }

    private DexType stringClassToDexType(String stringClass) {
      return factory.createType(DescriptorUtils.javaTypeToDescriptor(stringClass));
    }

    public DesugaredLibraryConfiguration build() {
      return new DesugaredLibraryConfiguration(
          requiredCompilationAPILevel,
          libraryCompilation,
          synthesizedLibraryClassesPackagePrefix,
          ImmutableMap.copyOf(rewritePrefix),
          ImmutableMap.copyOf(emulateLibraryInterface),
          ImmutableMap.copyOf(retargetCoreLibMember),
          ImmutableMap.copyOf(backportCoreLibraryMember),
          ImmutableMap.copyOf(customConversions),
          ImmutableList.copyOf(dontRewriteInvocation),
          ImmutableList.copyOf(extraKeepRules));
    }
  }
}
