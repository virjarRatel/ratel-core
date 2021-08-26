// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.ArchiveClassFileProvider;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.Keep;
import com.android.tools.r8.dexsplitter.DexSplitter.FeatureJar;
import com.android.tools.r8.origin.PathOrigin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides a mappings of classes to modules. The structure of the input file is as follows:
 * packageOrClass:module
 *
 * <p>Lines with a # prefix are ignored.
 *
 * <p>We will do most specific matching, i.e.,
 * <pre>
 *   com.google.foobar.*:feature2
 *   com.google.*:base
 * </pre>
 * will put everything in the com.google namespace into base, except classes in com.google.foobar
 * that will go to feature2. Class based mappings takes precedence over packages (since they are
 * more specific):
 * <pre>
 *   com.google.A:feature2
 *   com.google.*:base
 *  </pre>
 * Puts A into feature2, and all other classes from com.google into base.
 *
 * <p>Note that this format does not allow specifying inter-module dependencies, this is simply a
 * placement tool.
 */
@Keep
public final class FeatureClassMapping {

  Map<String, String> parsedRules = new HashMap<>(); // Already parsed rules.
  Map<String, String> parseNonClassRules = new HashMap<>();
  boolean usesOnlyExactMappings = true;

  Set<FeaturePredicate> mappings = new HashSet<>();

  Path mappingFile;
  String baseName = DEFAULT_BASE_NAME;

  static final String DEFAULT_BASE_NAME = "base";

  static final String COMMENT = "#";
  static final String SEPARATOR = ":";

  public String getBaseName() {
    return baseName;
  }

  private static class SpecificationOrigin extends PathOrigin {

    public SpecificationOrigin(Path path) {
      super(path);
    }

    @Override
    public String part() {
      return "specification file '" + super.part() + "'";
    }
  }

  private static class JarFileOrigin extends PathOrigin {

    public JarFileOrigin(Path path) {
      super(path);
    }

    @Override
    public String part() {
      return "jar file '" + super.part() + "'";
    }
  }

  public static FeatureClassMapping fromSpecification(Path file) throws FeatureMappingException {
    return fromSpecification(file, new DiagnosticsHandler() {});
  }

  public static FeatureClassMapping fromSpecification(Path file, DiagnosticsHandler reporter)
      throws FeatureMappingException {
    FeatureClassMapping mapping = new FeatureClassMapping();
    List<String> lines = null;
    try {
      lines = FileUtils.readAllLines(file);
    } catch (IOException e) {
      reporter.error(new ExceptionDiagnostic(e, new SpecificationOrigin(file)));
      throw new AbortException();
    }
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      mapping.parseAndAdd(line, i);
    }
    return mapping;
  }

  public static class Internal {
    private static List<String> getClassFileDescriptors(String jar, DiagnosticsHandler reporter) {
      Path jarPath = Paths.get(jar);
      try {
        return new ArchiveClassFileProvider(jarPath).getClassDescriptors()
            .stream()
            .map(DescriptorUtils::descriptorToJavaType)
            .collect(Collectors.toList());
      } catch (IOException e) {
        reporter.error(new ExceptionDiagnostic(e, new JarFileOrigin(jarPath)));
        throw new AbortException();
      }
    }

    private static List<String> getNonClassFiles(String jar, DiagnosticsHandler reporter) {
      try (ZipFile zipfile = new ZipFile(jar, StandardCharsets.UTF_8)) {
          return zipfile.stream()
              .filter(entry -> !ZipUtils.isClassFile(entry.getName()))
              .map(ZipEntry::getName)
              .collect(Collectors.toList());
        } catch (IOException e) {
          reporter.error(new ExceptionDiagnostic(e, new JarFileOrigin(Paths.get(jar))));
          throw new AbortException();
        }
    }

    public static FeatureClassMapping fromJarFiles(
        List<FeatureJar> featureJars, List<String> baseJars, String baseName,
        DiagnosticsHandler reporter)
        throws FeatureMappingException {
      FeatureClassMapping mapping = new FeatureClassMapping();
      if (baseName != null) {
        mapping.baseName = baseName;
      }
      for (FeatureJar featureJar : featureJars) {
        for (String javaType : getClassFileDescriptors(featureJar.getJar(), reporter)) {
          mapping.addMapping(javaType, featureJar.getOutputName());
        }
        for (String nonClass : getNonClassFiles(featureJar.getJar(), reporter)) {
          mapping.addNonClassMapping(nonClass, featureJar.getOutputName());
        }
      }
      for (String baseJar : baseJars) {
        for (String javaType : getClassFileDescriptors(baseJar, reporter)) {
          mapping.addBaseMapping(javaType);
        }
        for (String nonClass : getNonClassFiles(baseJar, reporter)) {
          mapping.addBaseNonClassMapping(nonClass);
        }
      }
      assert mapping.usesOnlyExactMappings;
      return mapping;
    }

  }

  private FeatureClassMapping() {}

  public void addBaseMapping(String clazz) throws FeatureMappingException {
    addMapping(clazz, baseName);
  }

  public void addBaseNonClassMapping(String name) {
    addNonClassMapping(name, baseName);
  }

  public void addMapping(String clazz, String feature) throws FeatureMappingException {
    addRule(clazz, feature, 0);
  }

  public void addNonClassMapping(String name, String feature) {
    // If a non-class file is present in multiple features put the resource in the base.
    parseNonClassRules.put(name, parseNonClassRules.containsKey(name) ? baseName : feature);
  }

  FeatureClassMapping(List<String> lines) throws FeatureMappingException {
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      parseAndAdd(line, i);
    }
  }

  public String featureForClass(String clazz) {
    if (usesOnlyExactMappings) {
      return parsedRules.getOrDefault(clazz, baseName);
    } else {
      FeaturePredicate bestMatch = null;
      for (FeaturePredicate mapping : mappings) {
        if (mapping.match(clazz)) {
          if (bestMatch == null || bestMatch.predicate.length() < mapping.predicate.length()) {
            bestMatch = mapping;
          }
        }
      }
      if (bestMatch == null) {
        return baseName;
      }
      return bestMatch.feature;
    }
  }

  public String featureForNonClass(String nonClass) {
    return parseNonClassRules.getOrDefault(nonClass, baseName);
  }

  private void parseAndAdd(String line, int lineNumber) throws FeatureMappingException {
    if (line.startsWith(COMMENT)) {
      return; // Ignore comments
    }
    if (line.isEmpty()) {
      return; // Ignore blank lines
    }

    if (!line.contains(SEPARATOR)) {
      error("Mapping lines must contain a " + SEPARATOR, lineNumber);
    }
    String[] values = line.split(SEPARATOR);
    if (values.length != 2) {
      error("Mapping lines can only contain one " + SEPARATOR, lineNumber);
    }

    String predicate = values[0];
    String feature = values[1];
    addRule(predicate, feature, lineNumber);
  }

  private void addRule(String predicate, String feature, int lineNumber)
      throws FeatureMappingException {
    if (parsedRules.containsKey(predicate)) {
      if (!parsedRules.get(predicate).equals(feature)) {
        error("Redefinition of predicate " + predicate + "not allowed", lineNumber);
      }
      return; // Already have this rule.
    }
    parsedRules.put(predicate, feature);
    FeaturePredicate featurePredicate = new FeaturePredicate(predicate, feature);
    mappings.add(featurePredicate);
    usesOnlyExactMappings &= featurePredicate.isExactmapping();
  }

  private void error(String error, int line) throws FeatureMappingException {
    throw new FeatureMappingException(
        "Invalid mappings specification: " + error + "\n in file " + mappingFile + ":" + line);
  }

  @Keep
  public static class FeatureMappingException extends Exception {
    FeatureMappingException(String message) {
      super(message);
    }
  }

  /** A feature predicate can either be a wildcard or class predicate. */
  private static class FeaturePredicate {
    private static Pattern identifier = Pattern.compile("[A-Za-z_\\-][A-Za-z0-9_$\\-]*");
    final String predicate;
    final String feature;
    final boolean isCatchAll;
    // False implies class predicate.
    final boolean isWildcard;

    FeaturePredicate(String predicate, String feature) throws FeatureMappingException {
      isWildcard = predicate.endsWith(".*");
      isCatchAll =  predicate.equals("*");
      if (isCatchAll) {
        this.predicate = "";
      } else if (isWildcard) {
        String packageName = predicate.substring(0, predicate.length() - 2);
        if (!DescriptorUtils.isValidJavaType(packageName)) {
          throw new FeatureMappingException(packageName + " is not a valid identifier");
        }
        // Prefix of a fully-qualified class name, including a terminating dot.
        this.predicate = predicate.substring(0, predicate.length() - 1);
      } else {
        if (!DescriptorUtils.isValidJavaType(predicate)) {
          throw new FeatureMappingException(predicate + " is not a valid identifier");
        }
        this.predicate = predicate;
      }
      this.feature = feature;
    }

    boolean match(String className) {
      if (isCatchAll) {
        return true;
      } else if (isWildcard) {
        return className.startsWith(predicate);
      } else {
        return className.equals(predicate);
      }
    }

    boolean isExactmapping() {
      return !isWildcard && !isCatchAll;
    }
  }
}
