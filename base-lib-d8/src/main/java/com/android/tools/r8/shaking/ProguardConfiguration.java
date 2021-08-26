// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.naming.DictionaryReader;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.utils.InternalOptions.PackageObfuscationMode;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProguardConfiguration {

  public static class Builder {

    private final List<String> parsedConfiguration = new ArrayList<>();
    private final List<FilteredClassPath> injars = new ArrayList<>();
    private final List<FilteredClassPath> libraryjars = new ArrayList<>();
    private final Reporter reporter;
    private PackageObfuscationMode packageObfuscationMode = PackageObfuscationMode.NONE;
    private String packagePrefix = "";
    private boolean allowAccessModification;
    private boolean ignoreWarnings;
    private boolean optimizing = true;
    private boolean obfuscating = true;
    private boolean shrinking = true;
    private boolean printConfiguration;
    private Path printConfigurationFile;
    private boolean printUsage;
    private Path printUsageFile;
    private boolean printMapping;
    private Path printMappingFile;
    private Path applyMappingFile;
    private boolean verbose;
    private String renameSourceFileAttribute;
    private final List<String> keepAttributePatterns = new ArrayList<>();
    private final ProguardPackageNameList.Builder keepPackageNamesPatterns =
        ProguardPackageNameList.builder();
    private final ProguardClassFilter.Builder dontWarnPatterns = ProguardClassFilter.builder();
    private final ProguardClassFilter.Builder dontNotePatterns = ProguardClassFilter.builder();
    protected final Set<ProguardConfigurationRule> rules = Sets.newLinkedHashSet();
    private final DexItemFactory dexItemFactory;
    private boolean printSeeds;
    private Path seedFile;
    private Path obfuscationDictionary;
    private Path classObfuscationDictionary;
    private Path packageObfuscationDictionary;
    private boolean keepParameterNames;
    private Origin keepParameterNamesOptionOrigin;
    private Position keepParameterNamesOptionPosition;
    private final ProguardClassFilter.Builder adaptClassStrings = ProguardClassFilter.builder();
    private final ProguardPathFilter.Builder adaptResourceFilenames =
        ProguardPathFilter.builder()
            .addPattern(ProguardPathList.builder().addFileName("META-INF/services/*").build());
    private final ProguardPathFilter.Builder adaptResourceFileContents =
        ProguardPathFilter.builder()
            .addPattern(ProguardPathList.builder().addFileName("META-INF/services/*").build());
    private final ProguardPathFilter.Builder keepDirectories =
        ProguardPathFilter.builder().disable();
    private boolean forceProguardCompatibility = false;
    private boolean overloadAggressively;
    private boolean keepRuleSynthesisForRecompilation = false;
    private boolean configurationDebugging = false;
    private boolean dontUseMixedCaseClassnames = false;

    private Builder(DexItemFactory dexItemFactory, Reporter reporter) {
      this.dexItemFactory = dexItemFactory;
      this.reporter = reporter;
    }

    public void addParsedConfiguration(String source) {
      parsedConfiguration.add(source);
    }

    public void addInjars(List<FilteredClassPath> injars) {
      this.injars.addAll(injars);
    }

    public void addLibraryJars(List<FilteredClassPath> libraryJars) {
      this.libraryjars.addAll(libraryJars);
    }

    public PackageObfuscationMode getPackageObfuscationMode() {
      return packageObfuscationMode;
    }

    public void setPackagePrefix(String packagePrefix) {
      packageObfuscationMode = PackageObfuscationMode.REPACKAGE;
      this.packagePrefix = packagePrefix;
    }

    public void setFlattenPackagePrefix(String packagePrefix) {
      packageObfuscationMode = PackageObfuscationMode.FLATTEN;
      this.packagePrefix = packagePrefix;
    }

    public void setAllowAccessModification(boolean allowAccessModification) {
      this.allowAccessModification = allowAccessModification;
    }

    public void setIgnoreWarnings(boolean ignoreWarnings) {
      this.ignoreWarnings = ignoreWarnings;
    }

    public Builder disableOptimization() {
      this.optimizing = false;
      return this;
    }

    public Builder disableObfuscation() {
      this.obfuscating = false;
      return this;
    }

    boolean isObfuscating() {
      return obfuscating;
    }

    public boolean isOptimizing() {
      return optimizing;
    }

    public boolean isShrinking() {
      return shrinking;
    }

    public Builder disableShrinking() {
      shrinking = false;
      return this;
    }

    public void setPrintConfiguration(boolean printConfiguration) {
      this.printConfiguration = printConfiguration;
    }

    public void setPrintConfigurationFile(Path file) {
      assert printConfiguration;
      this.printConfigurationFile = file;
    }

    public void setPrintUsage(boolean printUsage) {
      this.printUsage = printUsage;
    }

    public void setPrintUsageFile(Path printUsageFile) {
      this.printUsageFile = printUsageFile;
    }

    public void setPrintMapping(boolean printMapping) {
      this.printMapping = printMapping;
    }

    public void setPrintMappingFile(Path file) {
      assert printMapping;
      this.printMappingFile = file;
    }

    public void setApplyMappingFile(Path file) {
      this.applyMappingFile = file;
    }

    public boolean hasApplyMappingFile() {
      return applyMappingFile != null;
    }

    public void setVerbose(boolean verbose) {
      this.verbose = verbose;
    }

    public void setRenameSourceFileAttribute(String renameSourceFileAttribute) {
      this.renameSourceFileAttribute = renameSourceFileAttribute;
    }

    public Builder addKeepAttributePatterns(List<String> keepAttributePatterns) {
      this.keepAttributePatterns.addAll(keepAttributePatterns);
      return this;
    }

    public void addRule(ProguardConfigurationRule rule) {
      this.rules.add(rule);
    }

    public void addKeepPackageNamesPattern(boolean isNegated, ProguardPackageMatcher pattern) {
      keepPackageNamesPatterns.addPackageName(isNegated, pattern);
    }

    public void addDontWarnPattern(ProguardClassNameList pattern) {
      dontWarnPatterns.addPattern(pattern);
    }

    public void addDontNotePattern(ProguardClassNameList pattern) {
      dontNotePatterns.addPattern(pattern);
    }

    public void setSeedFile(Path seedFile) {
      this.seedFile = seedFile;
    }

    public void setPrintSeeds(boolean printSeeds) {
      this.printSeeds = printSeeds;
    }

    public void setObfuscationDictionary(Path obfuscationDictionary) {
      this.obfuscationDictionary = obfuscationDictionary;
    }

    public void setClassObfuscationDictionary(Path classObfuscationDictionary) {
      this.classObfuscationDictionary = classObfuscationDictionary;
    }

    public void setPackageObfuscationDictionary(Path packageObfuscationDictionary) {
      this.packageObfuscationDictionary = packageObfuscationDictionary;
    }

    boolean isOverloadAggressively() {
      return overloadAggressively;
    }

    public void setKeepParameterNames(boolean keepParameterNames, Origin optionOrigin,
        Position optionPosition) {
      assert optionOrigin != null || !keepParameterNames;
      this.keepParameterNames = keepParameterNames;
      this.keepParameterNamesOptionOrigin = optionOrigin;
      this.keepParameterNamesOptionPosition = optionPosition;
    }

    boolean isKeepParameterNames() {
      return keepParameterNames;
    }

    Origin getKeepParameterNamesOptionOrigin() {
      return keepParameterNamesOptionOrigin;
    }

    Position getKeepParameterNamesOptionPosition() {
      return keepParameterNamesOptionPosition;
    }

    public void addAdaptClassStringsPattern(ProguardClassNameList pattern) {
      adaptClassStrings.addPattern(pattern);
    }

    public void addAdaptResourceFilenames(ProguardPathList pattern) {
      adaptResourceFilenames.addPattern(pattern);
    }

    public void addAdaptResourceFileContents(ProguardPathList pattern) {
      adaptResourceFileContents.addPattern(pattern);
    }

    public void enableKeepDirectories() {
      keepDirectories.enable();
    }

    public void addKeepDirectories(ProguardPathList pattern) {
      keepDirectories.addPattern(pattern);
    }

    public void setForceProguardCompatibility(boolean forceProguardCompatibility) {
      this.forceProguardCompatibility = forceProguardCompatibility;
    }

    public void setOverloadAggressively(boolean overloadAggressively) {
      this.overloadAggressively = overloadAggressively;
    }

    public void enableKeepRuleSynthesisForRecompilation() {
      this.keepRuleSynthesisForRecompilation = true;
    }

    public void setConfigurationDebugging(boolean configurationDebugging) {
      this.configurationDebugging = configurationDebugging;
    }

    boolean isConfigurationDebugging() {
      return configurationDebugging;
    }

    public void setDontUseMixedCaseClassnames(boolean dontUseMixedCaseClassnames) {
      this.dontUseMixedCaseClassnames = dontUseMixedCaseClassnames;
    }

    /**
     * This synthesizes a set of keep rules that are necessary in order to be able to successfully
     * recompile the generated dex files with the same keep rules.
     */
    public void synthesizeKeepRulesForRecompilation() {
      List<ProguardConfigurationRule> synthesizedKeepRules = new ArrayList<>();
      for (ProguardConfigurationRule rule : rules) {
        ProguardConfigurationUtils.synthesizeKeepRulesForRecompilation(rule, synthesizedKeepRules);
      }
      if (rules.addAll(synthesizedKeepRules)) {
        parsedConfiguration.add(
            StringUtils.lines(
                synthesizedKeepRules.stream()
                    .map(ProguardClassSpecification::toString)
                    .toArray(String[]::new)));
      }
    }

    public ProguardConfiguration buildRaw() {
      ProguardConfiguration configuration =
          new ProguardConfiguration(
              String.join(System.lineSeparator(), parsedConfiguration),
              dexItemFactory,
              injars,
              libraryjars,
              packageObfuscationMode,
              packagePrefix,
              allowAccessModification,
              ignoreWarnings,
              optimizing,
              obfuscating,
              shrinking,
              printConfiguration,
              printConfigurationFile,
              printUsage,
              printUsageFile,
              printMapping,
              printMappingFile,
              applyMappingFile,
              verbose,
              renameSourceFileAttribute,
              ProguardKeepAttributes.fromPatterns(keepAttributePatterns),
              keepPackageNamesPatterns.build(),
              dontWarnPatterns.build(),
              dontNotePatterns.build(),
              rules,
              printSeeds,
              seedFile,
              overloadAggressively,
              DictionaryReader.readAllNames(obfuscationDictionary, reporter),
              DictionaryReader.readAllNames(classObfuscationDictionary, reporter),
              DictionaryReader.readAllNames(packageObfuscationDictionary, reporter),
              keepParameterNames,
              adaptClassStrings.build(),
              adaptResourceFilenames.build(),
              adaptResourceFileContents.build(),
              keepDirectories.build(),
              configurationDebugging,
              dontUseMixedCaseClassnames);

      reporter.failIfPendingErrors();

      return configuration;
    }

    public ProguardConfiguration build() {
      if (forceProguardCompatibility && !isObfuscating()) {
        // For Proguard -keepattributes are only applicable when obfuscating.
        keepAttributePatterns.addAll(ProguardKeepAttributes.KEEP_ALL);
      }
      // If either of the flags -dontshrink, -dontobfuscate or -dontoptimize is passed, or
      // shrinking or minification is turned off through the API, then add a match all rule
      // which will apply that.
      if (!isShrinking() || !isObfuscating() || !isOptimizing()) {
        addRule(ProguardKeepRule.defaultKeepAllRule(modifiers -> {
          modifiers.setAllowsShrinking(isShrinking());
          modifiers.setAllowsOptimization(isOptimizing());
          modifiers.setAllowsObfuscation(isObfuscating());
        }));
      }

      if (keepRuleSynthesisForRecompilation) {
        synthesizeKeepRulesForRecompilation();
      }

      return buildRaw();
    }
  }

  private final String parsedConfiguration;
  private final DexItemFactory dexItemFactory;
  private final ImmutableList<FilteredClassPath> injars;
  private final ImmutableList<FilteredClassPath> libraryjars;
  private final PackageObfuscationMode packageObfuscationMode;
  private final String packagePrefix;
  private final boolean allowAccessModification;
  private final boolean ignoreWarnings;
  private final boolean optimizing;
  private final boolean obfuscating;
  private final boolean shrinking;
  private final boolean printConfiguration;
  private final Path printConfigurationFile;
  private final boolean printUsage;
  private final Path printUsageFile;
  private final boolean printMapping;
  private final Path printMappingFile;
  private final Path applyMappingFile;
  private final boolean verbose;
  private final String renameSourceFileAttribute;
  private final ProguardKeepAttributes keepAttributes;
  private final ProguardPackageNameList keepPackageNamesPatterns;
  private final ProguardClassFilter dontWarnPatterns;
  private final ProguardClassFilter dontNotePatterns;
  protected final ImmutableList<ProguardConfigurationRule> rules;
  private final boolean printSeeds;
  private final Path seedFile;
  private final boolean overloadAggressively;
  private final ImmutableList<String> obfuscationDictionary;
  private final ImmutableList<String> classObfuscationDictionary;
  private final ImmutableList<String> packageObfuscationDictionary;
  private final boolean keepParameterNames;
  private final ProguardClassFilter adaptClassStrings;
  private final ProguardPathFilter adaptResourceFilenames;
  private final ProguardPathFilter adaptResourceFileContents;
  private final ProguardPathFilter keepDirectories;
  private final boolean configurationDebugging;
  private final boolean dontUseMixedCaseClassnames;

  private ProguardConfiguration(
      String parsedConfiguration,
      DexItemFactory factory,
      List<FilteredClassPath> injars,
      List<FilteredClassPath> libraryjars,
      PackageObfuscationMode packageObfuscationMode,
      String packagePrefix,
      boolean allowAccessModification,
      boolean ignoreWarnings,
      boolean optimizing,
      boolean obfuscating,
      boolean shrinking,
      boolean printConfiguration,
      Path printConfigurationFile,
      boolean printUsage,
      Path printUsageFile,
      boolean printMapping,
      Path printMappingFile,
      Path applyMappingFile,
      boolean verbose,
      String renameSourceFileAttribute,
      ProguardKeepAttributes keepAttributes,
      ProguardPackageNameList keepPackageNamesPatterns,
      ProguardClassFilter dontWarnPatterns,
      ProguardClassFilter dontNotePatterns,
      Set<ProguardConfigurationRule> rules,
      boolean printSeeds,
      Path seedFile,
      boolean overloadAggressively,
      ImmutableList<String> obfuscationDictionary,
      ImmutableList<String> classObfuscationDictionary,
      ImmutableList<String> packageObfuscationDictionary,
      boolean keepParameterNames,
      ProguardClassFilter adaptClassStrings,
      ProguardPathFilter adaptResourceFilenames,
      ProguardPathFilter adaptResourceFileContents,
      ProguardPathFilter keepDirectories,
      boolean configurationDebugging,
      boolean dontUseMixedCaseClassnames) {
    this.parsedConfiguration = parsedConfiguration;
    this.dexItemFactory = factory;
    this.injars = ImmutableList.copyOf(injars);
    this.libraryjars = ImmutableList.copyOf(libraryjars);
    this.packageObfuscationMode = packageObfuscationMode;
    this.packagePrefix = packagePrefix;
    this.allowAccessModification = allowAccessModification;
    this.ignoreWarnings = ignoreWarnings;
    this.optimizing = optimizing;
    this.obfuscating = obfuscating;
    this.shrinking = shrinking;
    this.printConfiguration = printConfiguration;
    this.printConfigurationFile = printConfigurationFile;
    this.printUsage = printUsage;
    this.printUsageFile = printUsageFile;
    this.printMapping = printMapping;
    this.printMappingFile = printMappingFile;
    this.applyMappingFile = applyMappingFile;
    this.verbose = verbose;
    this.renameSourceFileAttribute = renameSourceFileAttribute;
    this.keepAttributes = keepAttributes;
    this.keepPackageNamesPatterns = keepPackageNamesPatterns;
    this.dontWarnPatterns = dontWarnPatterns;
    this.dontNotePatterns = dontNotePatterns;
    this.rules = ImmutableList.copyOf(rules);
    this.printSeeds = printSeeds;
    this.seedFile = seedFile;
    this.overloadAggressively = overloadAggressively;
    this.obfuscationDictionary = obfuscationDictionary;
    this.classObfuscationDictionary = classObfuscationDictionary;
    this.packageObfuscationDictionary = packageObfuscationDictionary;
    this.keepParameterNames = keepParameterNames;
    this.adaptClassStrings = adaptClassStrings;
    this.adaptResourceFilenames = adaptResourceFilenames;
    this.adaptResourceFileContents = adaptResourceFileContents;
    this.keepDirectories = keepDirectories;
    this.configurationDebugging = configurationDebugging;
    this.dontUseMixedCaseClassnames = dontUseMixedCaseClassnames;
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder(DexItemFactory dexItemFactory,
      Reporter reporter) {
    return new Builder(dexItemFactory, reporter);
  }

  public String getParsedConfiguration() {
    return parsedConfiguration;
  }

  public DexItemFactory getDexItemFactory() {
    return dexItemFactory;
  }

  public List<FilteredClassPath> getInjars() {
    return injars;
  }

  public List<FilteredClassPath> getLibraryjars() {
    return libraryjars;
  }

  public PackageObfuscationMode getPackageObfuscationMode() {
    return packageObfuscationMode;
  }

  public String getPackagePrefix() {
    return packagePrefix;
  }

  public boolean isAccessModificationAllowed() {
    return allowAccessModification;
  }

  public boolean isPrintMapping() {
    return printMapping;
  }

  public Path getPrintMappingFile() {
    return printMappingFile;
  }

  public boolean hasApplyMappingFile() {
    return applyMappingFile != null;
  }

  public Path getApplyMappingFile() {
    return applyMappingFile;
  }

  public boolean isIgnoreWarnings() {
    return ignoreWarnings;
  }

  public boolean isOptimizing() {
    return optimizing;
  }

  public boolean isObfuscating() {
    return obfuscating;
  }

  public boolean isShrinking() {
    return shrinking;
  }

  public boolean isPrintConfiguration() {
    return printConfiguration;
  }

  public Path getPrintConfigurationFile() {
    return printConfigurationFile;
  }

  public boolean isPrintUsage() {
    return printUsage;
  }

  public Path getPrintUsageFile() {
    return printUsageFile;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public String getRenameSourceFileAttribute() {
    return renameSourceFileAttribute;
  }

  public ProguardKeepAttributes getKeepAttributes() {
    return keepAttributes;
  }

  public ProguardPackageNameList getKeepPackageNamesPatterns() {
    return keepPackageNamesPatterns;
  }

  public ProguardClassFilter getDontWarnPatterns() {
    return dontWarnPatterns;
  }

  public ProguardClassFilter getDontNotePatterns() {
    return dontNotePatterns;
  }

  public List<ProguardConfigurationRule> getRules() {
    return rules;
  }

  public boolean isOverloadAggressively() {
    return overloadAggressively;
  }

  public List<String> getObfuscationDictionary() {
    return obfuscationDictionary;
  }

  public List<String> getClassObfuscationDictionary() {
    return classObfuscationDictionary;
  }

  public List<String> getPackageObfuscationDictionary() {
    return packageObfuscationDictionary;
  }

  public boolean isKeepParameterNames() {
    return keepParameterNames;
  }

  public ProguardClassFilter getAdaptClassStrings() {
    return adaptClassStrings;
  }

  public ProguardPathFilter getAdaptResourceFilenames() {
    return adaptResourceFilenames;
  }

  public ProguardPathFilter getAdaptResourceFileContents() {
    return adaptResourceFileContents;
  }

  public ProguardPathFilter getKeepDirectories() {
    return keepDirectories;
  }

  public boolean isPrintSeeds() {
    return printSeeds;
  }

  public Path getSeedFile() {
    return seedFile;
  }

  public boolean isConfigurationDebugging() {
    return configurationDebugging;
  }

  public boolean hasDontUseMixedCaseClassnames() {
    return dontUseMixedCaseClassnames;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!keepAttributes.isEmpty()) {
      keepAttributes.append(builder);
      builder.append('\n');
    }
    for (ProguardConfigurationRule rule : rules) {
      rule.append(builder, true);
      builder.append('\n');
    }
    return builder.toString();
  }
}
