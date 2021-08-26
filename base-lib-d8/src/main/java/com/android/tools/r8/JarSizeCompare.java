// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.ClassNamingForNameMapper;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.AndroidAppConsumers;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * JarSizeCompare outputs the class, method, field sizes of the given JAR files. For each input, a
 * ProGuard map can be passed that is used to resolve minified names.
 *
 * <p>By default, only shows methods where R8's DEX output is 5 or more instructions larger than
 * ProGuard+D8's output. Pass {@code --threshold 0} to display all methods.
 */
public class JarSizeCompare {

  private static final String USAGE =
      "Arguments:\n"
          + "    [--threshold <threshold>]\n"
          + "    [--lib <lib.jar>]\n"
          + "    --input <name1> <input1.jar> [<map1.txt>]\n"
          + "    --input <name2> <input2.jar> [<map2.txt>] ...\n"
          + "\n"
          + "JarSizeCompare outputs the class, method, field sizes of the given JAR files.\n"
          + "For each input, a ProGuard map can be passed that is used to resolve minified names.\n";

  private static final ImmutableMap<String, String> R8_RELOCATIONS =
      ImmutableMap.<String, String>builder()
          .put("com.google.common", "com.android.tools.r8.com.google.common")
          .put("com.google.gson", "com.android.tools.r8.com.google.gson")
          .put("com.google.thirdparty", "com.android.tools.r8.com.google.thirdparty")
          .put("joptsimple", "com.android.tools.r8.joptsimple")
          .put("org.apache.commons", "com.android.tools.r8.org.apache.commons")
          .put("org.objectweb.asm", "com.android.tools.r8.org.objectweb.asm")
          .put("it.unimi.dsi.fastutil", "com.android.tools.r8.it.unimi.dsi.fastutil")
          .build();

  private final List<Path> libraries;
  private final List<InputParameter> inputParameters;
  private final int threshold;
  private final InternalOptions options;
  private int pgIndex;
  private int r8Index;

  private JarSizeCompare(
      List<Path> libraries, List<InputParameter> inputParameters, int threshold) {
    this.libraries = libraries;
    this.inputParameters = inputParameters;
    this.threshold = threshold;
    options = new InternalOptions();
  }

  public void run() throws Exception {
    List<String> names = new ArrayList<>();
    List<InputApplication> inputApplicationList = new ArrayList<>();
    Timing timing = new Timing("JarSizeCompare");
    for (InputParameter inputParameter : inputParameters) {
      AndroidApp inputApp = inputParameter.getInputApp(libraries);
      DexApplication input = inputParameter.getReader(options, inputApp, timing);
      AndroidAppConsumers appConsumer = new AndroidAppConsumers();
      D8.run(
          D8Command.builder(inputApp)
              .setMinApiLevel(AndroidApiLevel.P.getLevel())
              .setProgramConsumer(appConsumer.wrapDexIndexedConsumer(null))
              .build());
      DexApplication d8Input = inputParameter.getReader(options, appConsumer.build(), timing);
      InputApplication inputApplication =
          new InputApplication(input, translateClassNames(input, input.classes()));
      InputApplication d8Classes =
          new InputApplication(input, translateClassNames(input, d8Input.classes()));
      names.add(inputParameter.name + "-input");
      inputApplicationList.add(inputApplication);
      names.add(inputParameter.name + "-d8");
      inputApplicationList.add(d8Classes);
    }
    if (threshold != 0) {
      pgIndex = names.indexOf("pg-d8");
      r8Index = names.indexOf("r8-d8");
    }
    Map<String, InputClass[]> inputClasses = new HashMap<>();
    for (int i = 0; i < names.size(); i++) {
      InputApplication classes = inputApplicationList.get(i);
      for (String className : classes.getClasses()) {
        inputClasses.computeIfAbsent(className, k -> new InputClass[names.size()])[i] =
            classes.getInputClass(className);
      }
    }
    for (Entry<String, Map<String, InputClass[]>> library : byLibrary(inputClasses)) {
      System.out.println("");
      System.out.println(Strings.repeat("=", 100));
      String commonPrefix = getCommonPrefix(library.getValue().keySet());
      if (library.getKey().isEmpty()) {
        System.out.println("PROGRAM (" + commonPrefix + ")");
      } else {
        System.out.println("LIBRARY: " + library.getKey() + " (" + commonPrefix + ")");
      }
      printLibrary(library.getValue(), commonPrefix);
    }
  }

  private Map<String, DexProgramClass> translateClassNames(
      DexApplication input, List<DexProgramClass> classes) {
    Map<String, DexProgramClass> result = new HashMap<>();
    ClassNameMapper classNameMapper = input.getProguardMap();
    for (DexProgramClass programClass : classes) {
      ClassNamingForNameMapper classNaming;
      if (classNameMapper == null) {
        classNaming = null;
      } else {
        classNaming = classNameMapper.getClassNaming(programClass.type);
      }
      String type =
          classNaming == null ? programClass.type.toSourceString() : classNaming.originalName;
      result.put(type, programClass);
    }
    return result;
  }

  private String getCommonPrefix(Set<String> classes) {
    if (classes.size() <= 1) {
      return "";
    }
    String commonPrefix = null;
    for (String clazz : classes) {
      if (clazz.equals("r8.GeneratedOutlineSupport")) {
        continue;
      }
      if (commonPrefix == null) {
        commonPrefix = clazz;
      } else {
        int i = 0;
        while (i < clazz.length()
            && i < commonPrefix.length()
            && clazz.charAt(i) == commonPrefix.charAt(i)) {
          i++;
        }
        commonPrefix = commonPrefix.substring(0, i);
      }
    }
    return commonPrefix;
  }

  private void printLibrary(Map<String, InputClass[]> classMap, String commonPrefix) {
    List<Entry<String, InputClass[]>> classes = new ArrayList<>(classMap.entrySet());
    classes.sort(Comparator.comparing(Entry::getKey));
    for (Entry<String, InputClass[]> clazz : classes) {
      printClass(
          clazz.getKey().substring(commonPrefix.length()), new ClassCompare(clazz.getValue()));
    }
  }

  private void printClass(String name, ClassCompare inputClasses) {
    List<MethodSignature> methods = getMethods(inputClasses);
    List<FieldSignature> fields = getFields(inputClasses);
    if (methods.isEmpty() && fields.isEmpty()) {
      return;
    }
    System.out.println(name);
    for (MethodSignature sig : methods) {
      printSignature(getMethodString(sig), inputClasses.sizes(sig));
    }
    for (FieldSignature sig : fields) {
      printSignature(getFieldString(sig), inputClasses.sizes(sig));
    }
  }

  private String getMethodString(MethodSignature sig) {
    StringBuilder builder = new StringBuilder().append('(');
    for (int i = 0; i < sig.parameters.length; i++) {
      builder.append(DescriptorUtils.javaTypeToShorty(sig.parameters[i]));
    }
    builder.append(')').append(DescriptorUtils.javaTypeToShorty(sig.type)).append(' ');
    return builder.append(sig.name).toString();
  }

  private String getFieldString(FieldSignature sig) {
    return DescriptorUtils.javaTypeToShorty(sig.type) + ' ' + sig.name;
  }

  private void printSignature(String key, int[] sizes) {
    System.out.print(padItem(key));
    for (int size : sizes) {
      System.out.print(padValue(size));
    }
    System.out.print('\n');
  }

  private List<MethodSignature> getMethods(ClassCompare inputClasses) {
    List<MethodSignature> methods = new ArrayList<>();
    for (MethodSignature methodSignature : inputClasses.getMethods()) {
      if (threshold == 0 || methodExceedsThreshold(inputClasses, methodSignature)) {
        methods.add(methodSignature);
      }
    }
    return methods;
  }

  private boolean methodExceedsThreshold(
      ClassCompare inputClasses, MethodSignature methodSignature) {
    assert threshold > 0;
    assert pgIndex != r8Index;
    int pgSize = inputClasses.size(methodSignature, pgIndex);
    int r8Size = inputClasses.size(methodSignature, r8Index);
    return pgSize != -1 && r8Size != -1 && pgSize + threshold <= r8Size;
  }

  private List<FieldSignature> getFields(ClassCompare inputClasses) {
    return threshold == 0 ? inputClasses.getFields() : Collections.emptyList();
  }

  private String padItem(String s) {
    return String.format("%-52s", s);
  }

  private String padValue(int v) {
    return String.format("%8s", v == -1 ? "---" : v);
  }

  private List<Map.Entry<String, Map<String, InputClass[]>>> byLibrary(
      Map<String, InputClass[]> inputClasses) {
    Map<String, Map<String, InputClass[]>> byLibrary = new HashMap<>();
    for (Entry<String, InputClass[]> entry : inputClasses.entrySet()) {
      Map<String, InputClass[]> library =
          byLibrary.computeIfAbsent(getLibraryName(entry.getKey()), k -> new HashMap<>());
      library.put(entry.getKey(), entry.getValue());
    }
    List<Entry<String, Map<String, InputClass[]>>> list = new ArrayList<>(byLibrary.entrySet());
    list.sort(Comparator.comparing(Entry::getKey));
    return list;
  }

  private String getLibraryName(String className) {
    for (Entry<String, String> relocation : R8_RELOCATIONS.entrySet()) {
      if (className.startsWith(relocation.getValue())) {
        return relocation.getKey();
      }
    }
    return "";
  }

  static class InputParameter {

    private final String name;
    private final Path jar;
    private final Path map;

    InputParameter(String name, Path jar, Path map) {
      this.name = name;
      this.jar = jar;
      this.map = map;
    }

    DexApplication getReader(InternalOptions options, AndroidApp inputApp, Timing timing)
        throws Exception {
      ApplicationReader applicationReader = new ApplicationReader(inputApp, options, timing);
      return applicationReader.read(map == null ? null : StringResource.fromFile(map)).toDirect();
    }

    AndroidApp getInputApp(List<Path> libraries) throws Exception {
      return AndroidApp.builder().addLibraryFiles(libraries).addProgramFiles(jar).build();
    }
  }

  static class InputApplication {

    private final DexApplication dexApplication;
    private final Map<String, DexProgramClass> classMap;

    private InputApplication(DexApplication dexApplication, Map<String, DexProgramClass> classMap) {
      this.dexApplication = dexApplication;
      this.classMap = classMap;
    }

    public Set<String> getClasses() {
      return classMap.keySet();
    }

    private InputClass getInputClass(String type) {
      DexProgramClass inputClass = classMap.get(type);
      ClassNameMapper proguardMap = dexApplication.getProguardMap();
      return new InputClass(inputClass, proguardMap);
    }
  }

  static class InputClass {
    private final DexProgramClass programClass;
    private final ClassNameMapper proguardMap;

    InputClass(DexClass dexClass, ClassNameMapper proguardMap) {
      this.programClass = dexClass == null ? null : dexClass.asProgramClass();
      this.proguardMap = proguardMap;
    }

    void forEachMethod(BiConsumer<MethodSignature, DexEncodedMethod> consumer) {
      if (programClass == null) {
        return;
      }
      programClass.forEachMethod(
          dexEncodedMethod -> {
            MethodSignature originalSignature =
                proguardMap == null
                    ? null
                    : proguardMap.originalSignatureOf(dexEncodedMethod.method);
            MethodSignature signature = MethodSignature.fromDexMethod(dexEncodedMethod.method);
            consumer.accept(
                originalSignature == null ? signature : originalSignature, dexEncodedMethod);
          });
    }

    void forEachField(BiConsumer<FieldSignature, DexEncodedField> consumer) {
      if (programClass == null) {
        return;
      }
      programClass.forEachField(
          dexEncodedField -> {
            FieldSignature originalSignature =
                proguardMap == null ? null : proguardMap.originalSignatureOf(dexEncodedField.field);
            FieldSignature signature = FieldSignature.fromDexField(dexEncodedField.field);
            consumer.accept(
                originalSignature == null ? signature : originalSignature, dexEncodedField);
          });
    }
  }

  private static class ClassCompare {
    final Map<MethodSignature, DexEncodedMethod[]> methods = new HashMap<>();
    final Map<FieldSignature, DexEncodedField[]> fields = new HashMap<>();
    final int classes;

    ClassCompare(InputClass[] inputs) {
      for (int i = 0; i < inputs.length; i++) {
        InputClass inputClass = inputs[i];
        int finalI = i;
        if (inputClass == null) {
          continue;
        }
        inputClass.forEachMethod(
            (sig, m) ->
                methods.computeIfAbsent(sig, o -> new DexEncodedMethod[inputs.length])[finalI] = m);
        inputClass.forEachField(
            (sig, f) ->
                fields.computeIfAbsent(sig, o -> new DexEncodedField[inputs.length])[finalI] = f);
      }
      classes = inputs.length;
    }

    List<MethodSignature> getMethods() {
      List<MethodSignature> methods = new ArrayList<>(this.methods.keySet());
      methods.sort(Comparator.comparing(MethodSignature::toString));
      return methods;
    }

    List<FieldSignature> getFields() {
      List<FieldSignature> fields = new ArrayList<>(this.fields.keySet());
      fields.sort(Comparator.comparing(FieldSignature::toString));
      return fields;
    }

    int size(MethodSignature method, int classIndex) {
      DexEncodedMethod dexEncodedMethod = methods.get(method)[classIndex];
      if (dexEncodedMethod == null) {
        return -1;
      }
      Code code = dexEncodedMethod.getCode();
      if (code == null) {
        return 0;
      }
      if (code.isCfCode()) {
        return code.asCfCode().getInstructions().size();
      }
      if (code.isDexCode()) {
        return code.asDexCode().instructions.length;
      }
      throw new Unreachable();
    }

    int[] sizes(MethodSignature method) {
      int[] result = new int[classes];
      for (int i = 0; i < classes; i++) {
        result[i] = size(method, i);
      }
      return result;
    }

    int size(FieldSignature field, int classIndex) {
      return fields.get(field)[classIndex] == null ? -1 : 1;
    }

    int[] sizes(FieldSignature field) {
      int[] result = new int[classes];
      for (int i = 0; i < classes; i++) {
        result[i] = size(field, i);
      }
      return result;
    }
  }

  public static void main(String[] args) throws Exception {
    JarSizeCompare program = JarSizeCompare.parse(args);
    if (program == null) {
      System.out.println(USAGE);
    } else {
      program.run();
    }
  }

  public static JarSizeCompare parse(String[] args) {
    int i = 0;
    int threshold = 0;
    List<Path> libraries = new ArrayList<>();
    List<InputParameter> inputs = new ArrayList<>();
    Set<String> names = new HashSet<>();
    while (i < args.length) {
      if (args[i].equals("--threshold") && i + 1 < args.length) {
        threshold = Integer.parseInt(args[i + 1]);
        i += 2;
      } else if (args[i].equals("--lib") && i + 1 < args.length) {
        libraries.add(Paths.get(args[i + 1]));
        i += 2;
      } else if (args[i].equals("--input") && i + 2 < args.length) {
        String name = args[i + 1];
        Path jar = Paths.get(args[i + 2]);
        Path map = null;
        if (i + 3 < args.length && !args[i + 3].startsWith("-")) {
          map = Paths.get(args[i + 3]);
          i += 4;
        } else {
          i += 3;
        }
        inputs.add(new InputParameter(name, jar, map));
        if (!names.add(name)) {
          System.out.println("Duplicate name: " + name);
          return null;
        }
      } else {
        return null;
      }
    }
    if (inputs.size() < 2) {
      return null;
    }
    if (threshold != 0 && (!names.contains("r8") || !names.contains("pg"))) {
      System.out.println(
          "You must either specify names \"pg\" and \"r8\" for input files "
              + "or use \"--threshold 0\".");
      return null;
    }
    return new JarSizeCompare(libraries, inputs, threshold);
  }
}
