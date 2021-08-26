// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.JarClassFileReader;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StreamUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Command-line program to compare two JARs. Given two JARs as input, the program first outputs a
 * list of classes only in one of the input JARs. Then, for each common class, the program outputs a
 * list of methods only in one of the input JARs. For each common method, the output of
 * CfInstruction.toString() on each instruction is compared to find instruction-level differences. A
 * simple diffing algorithm is used that simply removes the common prefix and common suffix and
 * prints everything from the first difference to the last difference in the method code.
 */
public class JarDiff {

  private static final String USAGE =
      "Arguments: <input1.jar> <input2.jar>\n"
          + "\n"
          + "JarDiff computes the difference between two JAR files that contain Java classes.\n"
          + "\n"
          + "Only method codes are compared. Fields, parameters, annotations, generic\n"
          + "signatures etc. are ignored.\n"
          + "\n"
          + "Note: Jump targets are ignored, so if two methods differ only in what label an\n"
          + "IF or GOTO instruction jumps to, no difference is output.";

  public static void main(String[] args) throws Exception {
    JarDiff jarDiff = JarDiff.parse(args);
    if (jarDiff == null) {
      System.out.println(USAGE);
    } else {
      jarDiff.run();
    }
  }

  public static JarDiff parse(String[] args) {
    int arg = 0;
    int before = 3;
    int after = 3;
    while (arg + 1 < args.length) {
      if (args[arg].equals("-B")) {
        before = Integer.parseInt(args[arg + 1]);
        arg += 2;
      } else if (args[arg].equals("-A")) {
        after = Integer.parseInt(args[arg + 1]);
        arg += 2;
      } else {
        break;
      }
    }
    if (args.length != arg + 2) {
      return null;
    }
    return new JarDiff(Paths.get(args[arg]), Paths.get(args[arg + 1]), before, after);
  }

  private final Path path1;
  private final Path path2;
  private final int before;
  private final int after;
  private final JarApplicationReader applicationReader;
  private ArchiveClassFileProvider archive1;
  private ArchiveClassFileProvider archive2;

  public JarDiff(Path path1, Path path2, int before, int after) {
    this.path1 = path1;
    this.path2 = path2;
    this.before = before;
    this.after = after;
    InternalOptions options = new InternalOptions();
    applicationReader = new JarApplicationReader(options);
  }

  public void run() throws Exception {
    archive1 = new ArchiveClassFileProvider(path1);
    archive2 = new ArchiveClassFileProvider(path2);
    for (String descriptor : getCommonDescriptors()) {
      byte[] bytes1 = getClassAsBytes(archive1, descriptor);
      byte[] bytes2 = getClassAsBytes(archive2, descriptor);
      if (Arrays.equals(bytes1, bytes2)) {
        continue;
      }
      DexProgramClass class1 = getDexProgramClass(path1, bytes1);
      DexProgramClass class2 = getDexProgramClass(path2, bytes2);
      compareMethods(class1, class2);
    }
  }

  private List<String> getCommonDescriptors() {
    List<String> descriptors1 = getSortedDescriptorList(archive1);
    List<String> descriptors2 = getSortedDescriptorList(archive2);
    if (descriptors1.equals(descriptors2)) {
      return descriptors1;
    }
    List<String> only1 = setMinus(descriptors1, descriptors2);
    List<String> only2 = setMinus(descriptors2, descriptors1);
    if (!only1.isEmpty()) {
      System.out.println("Only in " + path1 + ": " + only1);
    }
    if (!only2.isEmpty()) {
      System.out.println("Only in " + path2 + ": " + only2);
    }
    return setIntersection(descriptors1, descriptors2);
  }

  private List<String> getSortedDescriptorList(ArchiveClassFileProvider inputJar) {
    ArrayList<String> descriptorList = new ArrayList<>(inputJar.getClassDescriptors());
    Collections.sort(descriptorList);
    return descriptorList;
  }

  private byte[] getClassAsBytes(ArchiveClassFileProvider inputJar, String descriptor)
      throws Exception {
    return StreamUtils.StreamToByteArrayClose(
        inputJar.getProgramResource(descriptor).getByteStream());
  }

  private DexProgramClass getDexProgramClass(Path path, byte[] bytes) throws IOException {

    class Collector implements Consumer<DexClass> {

      private DexClass dexClass;

      @Override
      public void accept(DexClass dexClass) {
        this.dexClass = dexClass;
      }

      public DexClass get() {
        assert dexClass != null;
        return dexClass;
      }
    }

    Collector collector = new Collector();
    JarClassFileReader reader = new JarClassFileReader(applicationReader, collector);
    reader.read(new PathOrigin(path), ClassKind.PROGRAM, new ByteArrayInputStream(bytes));
    return collector.get().asProgramClass();
  }

  private void compareMethods(DexProgramClass class1, DexProgramClass class2) {
    class1.forEachMethod(
        method1 -> {
          DexEncodedMethod method2 = class2.lookupMethod(method1.method);
          compareMethods(method1, method2);
        });
    class2.forEachMethod(
        method2 -> {
          DexEncodedMethod method1 = class1.lookupMethod(method2.method);
          compareMethods(method1, method2);
        });
  }

  private void compareMethods(DexEncodedMethod m1, DexEncodedMethod m2) {
    if (m1 == null) {
      System.out.println("Only in " + path2 + ": " + m2.method.toSourceString());
      return;
    }
    if (m2 == null) {
      System.out.println("Only in " + path1 + ": " + m1.method.toSourceString());
      return;
    }
    List<String> code1 = getInstructionStrings(m1);
    List<String> code2 = getInstructionStrings(m2);
    if (code1.equals(code2)) {
      return;
    }
    int i = getCommonPrefix(code1, code2);
    int j = getCommonSuffix(code1, code2);
    int length1 = code1.size() - i - j;
    int length2 = code2.size() - i - j;
    int before = Math.min(i, this.before);
    int after = Math.min(j, this.after);
    int context = before + after;
    System.out.println("--- " + path1 + "/" + m1.method.toSmaliString());
    System.out.println("+++ " + path2 + "/" + m2.method.toSmaliString());
    System.out.println(
        "@@ -" + (i - before) + "," + (length1 + context)
            + " +" + (i - before) + "," + (length2 + context) + " @@ "
            + m1.method.toSourceString());
    for (int k = 0; k < before; k++) {
      System.out.println(" " + code1.get(i - before + k));
    }
    for (int k = 0; k < length1; k++) {
      System.out.println("-" + code1.get(i + k));
    }
    for (int k = 0; k < length2; k++) {
      System.out.println("+" + code2.get(i + k));
    }
    for (int k = 0; k < after; k++) {
      System.out.println(" " + code1.get(i + length1 + k));
    }
  }

  private static List<String> getInstructionStrings(DexEncodedMethod method) {
    List<CfInstruction> instructions = method.getCode().asCfCode().getInstructions();
    return instructions.stream().map(CfInstruction::toString).collect(Collectors.toList());
  }

  private static List<String> setIntersection(List<String> set1, List<String> set2) {
    ArrayList<String> result = new ArrayList<>(set1);
    result.retainAll(new HashSet<>(set2));
    return result;
  }

  private static List<String> setMinus(List<String> set, List<String> toRemove) {
    ArrayList<String> result = new ArrayList<>(set);
    result.removeAll(new HashSet<>(toRemove));
    return result;
  }

  private static int getCommonPrefix(List<String> code1, List<String> code2) {
    int i = 0;
    while (i < code1.size() && i < code2.size()) {
      if (code1.get(i).equals(code2.get(i))) {
        i++;
      } else {
        break;
      }
    }
    return i;
  }

  private static int getCommonSuffix(List<String> code1, List<String> code2) {
    int j = 0;
    while (j < code1.size() && j < code2.size()) {
      if (code1.get(code1.size() - j - 1).equals(code2.get(code2.size() - j - 1))) {
        j++;
      } else {
        break;
      }
    }
    return j;
  }
}
