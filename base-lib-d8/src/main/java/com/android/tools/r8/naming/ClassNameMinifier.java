// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static com.android.tools.r8.utils.DescriptorUtils.DESCRIPTOR_PACKAGE_SEPARATOR;
import static com.android.tools.r8.utils.DescriptorUtils.INNER_CLASS_SEPARATOR;
import static com.android.tools.r8.utils.DescriptorUtils.computeInnerClassSeparator;
import static com.android.tools.r8.utils.DescriptorUtils.getClassBinaryNameFromDescriptor;
import static com.android.tools.r8.utils.DescriptorUtils.getPackageBinaryNameFromJavaType;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.kotlin.KotlinMetadataRewriter;
import com.android.tools.r8.naming.signature.GenericSignatureRewriter;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.shaking.ProguardPackageNameList;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.InternalOptions.PackageObfuscationMode;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

class ClassNameMinifier {

  private final AppView<AppInfoWithLiveness> appView;
  private final ClassNamingStrategy classNamingStrategy;
  private final PackageNamingStrategy packageNamingStrategy;
  private final Iterable<? extends DexClass> classes;
  private final PackageObfuscationMode packageObfuscationMode;
  private final boolean isAccessModificationAllowed;
  private final Set<String> noObfuscationPrefixes = Sets.newHashSet();
  private final Set<String> usedPackagePrefixes = Sets.newHashSet();
  private final Set<DexString> usedTypeNames = Sets.newIdentityHashSet();

  private final Map<DexType, DexString> renaming = Maps.newIdentityHashMap();
  private final Map<String, Namespace> states = new HashMap<>();
  private final boolean keepInnerClassStructure;

  private final Namespace topLevelState;

  ClassNameMinifier(
      AppView<AppInfoWithLiveness> appView,
      ClassNamingStrategy classNamingStrategy,
      PackageNamingStrategy packageNamingStrategy,
      Iterable<? extends DexClass> classes) {
    this.appView = appView;
    this.classNamingStrategy = classNamingStrategy;
    this.packageNamingStrategy = packageNamingStrategy;
    this.classes = classes;
    InternalOptions options = appView.options();
    this.packageObfuscationMode = options.getProguardConfiguration().getPackageObfuscationMode();
    this.isAccessModificationAllowed =
        options.getProguardConfiguration().isAccessModificationAllowed();
    this.keepInnerClassStructure =
        options.getProguardConfiguration().getKeepAttributes().signature
            || options.getProguardConfiguration().getKeepAttributes().innerClasses;

    // Initialize top-level naming state.
    topLevelState = new Namespace(
        getPackageBinaryNameFromJavaType(options.getProguardConfiguration().getPackagePrefix()));

    states.put("", topLevelState);
  }

  static class ClassRenaming {
    protected final Map<String, String> packageRenaming;
    protected final Map<DexType, DexString> classRenaming;

    private ClassRenaming(
        Map<DexType, DexString> classRenaming, Map<String, String> packageRenaming) {
      this.classRenaming = classRenaming;
      this.packageRenaming = packageRenaming;
    }
  }

  ClassRenaming computeRenaming(Timing timing) {
    return computeRenaming(timing, Collections.emptyMap());
  }

  ClassRenaming computeRenaming(Timing timing, Map<DexType, DexString> syntheticClasses) {
    // Externally defined synthetic classes populate an initial renaming.
    renaming.putAll(syntheticClasses);

    // Collect names we have to keep.
    timing.begin("reserve");
    for (DexClass clazz : classes) {
      DexString descriptor = classNamingStrategy.reservedDescriptor(clazz.type);
      if (descriptor != null) {
        assert !renaming.containsKey(clazz.type);
        registerClassAsUsed(clazz.type, descriptor);
      }
    }
    timing.end();

    timing.begin("rename-classes");
    for (DexClass clazz : classes) {
      if (!renaming.containsKey(clazz.type)) {
        DexString renamed = computeName(clazz.type);
        renaming.put(clazz.type, renamed);
        KotlinMetadataRewriter.removeKotlinMetadataFromRenamedClass(appView, clazz);
        // If the class is a member class and it has used $ separator, its renamed name should have
        // the same separator (as long as inner-class attribute is honored).
        assert !keepInnerClassStructure
            || !clazz.isMemberClass()
            || !clazz.type.getInternalName().contains(String.valueOf(INNER_CLASS_SEPARATOR))
            || renamed.toString().contains(String.valueOf(INNER_CLASS_SEPARATOR))
            || classNamingStrategy.isRenamedByApplyMapping(clazz.type)
                : clazz.toSourceString() + " -> " + renamed;
      }
    }
    timing.end();

    timing.begin("rename-dangling-types");
    for (DexClass clazz : classes) {
      renameDanglingTypes(clazz);
    }
    timing.end();

    timing.begin("rename-generic");
    new GenericSignatureRewriter(appView, renaming).run(classes);
    timing.end();

    timing.begin("rename-arrays");
    appView.dexItemFactory().forAllTypes(this::renameArrayTypeIfNeeded);
    timing.end();

    return new ClassRenaming(Collections.unmodifiableMap(renaming), getPackageRenaming());
  }

  private Map<String, String> getPackageRenaming() {
    ImmutableMap.Builder<String, String> packageRenaming = ImmutableMap.builder();
    for (Entry<String, Namespace> entry : states.entrySet()) {
      String originalPackageName = entry.getKey();
      String minifiedPackageName = entry.getValue().getPackageName();
      if (!minifiedPackageName.equals(originalPackageName)) {
        packageRenaming.put(originalPackageName, minifiedPackageName);
      }
    }
    return packageRenaming.build();
  }

  private void renameDanglingTypes(DexClass clazz) {
    clazz.forEachMethod(this::renameDanglingTypesInMethod);
    clazz.forEachField(this::renameDanglingTypesInField);
  }

  private void renameDanglingTypesInField(DexEncodedField field) {
    renameDanglingType(field.field.type);
  }

  private void renameDanglingTypesInMethod(DexEncodedMethod method) {
    DexProto proto = method.method.proto;
    renameDanglingType(proto.returnType);
    for (DexType type : proto.parameters.values) {
      renameDanglingType(type);
    }
  }

  private void renameDanglingType(DexType type) {
    if (appView.appInfo().wasPruned(type) && !renaming.containsKey(type)) {
      // We have a type that is defined in the program source but is only used in a proto or
      // return type. As we don't need the class, we can rename it to anything as long as it is
      // unique.
      assert appView.definitionFor(type) == null;
      DexString descriptor = classNamingStrategy.reservedDescriptor(type);
      renaming.put(type, descriptor != null ? descriptor : topLevelState.nextTypeName(type));
    }
  }

  private void registerClassAsUsed(DexType type, DexString descriptor) {
    renaming.put(type, descriptor);
    registerPackagePrefixesAsUsed(
        getParentPackagePrefix(getClassBinaryNameFromDescriptor(descriptor.toSourceString())));
    usedTypeNames.add(descriptor);
    if (keepInnerClassStructure) {
      DexType outerClass = getOutClassForType(type);
      if (outerClass != null) {
        if (!renaming.containsKey(outerClass)
            && classNamingStrategy.reservedDescriptor(outerClass) == null) {
          // The outer class was not previously kept and will not be kept.
          // We have to force keep the outer class now.
          registerClassAsUsed(outerClass, outerClass.descriptor);
        }
      }
    }
  }

  /**
   * Registers the given package prefix and all of parent packages as used.
   */
  private void registerPackagePrefixesAsUsed(String packagePrefix) {
    // If -allowaccessmodification is not set, we may keep classes in their original packages,
    // accounting for package-private accesses.
    if (!isAccessModificationAllowed) {
      noObfuscationPrefixes.add(packagePrefix);
    }
    String usedPrefix = packagePrefix;
    while (usedPrefix.length() > 0) {
      usedPackagePrefixes.add(usedPrefix);
      usedPrefix = getParentPackagePrefix(usedPrefix);
    }
  }

  private DexType getOutClassForType(DexType type) {
    DexClass clazz = appView.definitionFor(type);
    if (clazz == null) {
      return null;
    }
    // For DEX inputs this could result in returning the outer class of a local class since we
    // can't distinguish it from a member class based on just the enclosing-class annotation.
    // We could filter out the local classes by looking for a corresponding entry in the
    // inner-classes attribute table which must exist only for member classes. Since DEX files
    // are not a supported input for R8 we just return the outer class in both cases.
    InnerClassAttribute attribute = clazz.getInnerClassAttributeForThisClass();
    if (attribute == null) {
      return null;
    }
    return attribute.getLiveContext(appView.appInfo());
  }

  private DexString computeName(DexType type) {
    Namespace state = null;
    if (keepInnerClassStructure) {
      // When keeping the nesting structure of inner classes, bind this type to the live context.
      // Note that such live context might not be always the enclosing class. E.g., a local class
      // does not have a direct enclosing class, but we use the holder of the enclosing method here.
      DexType outerClass = getOutClassForType(type);
      if (outerClass != null) {
        DexClass clazz = appView.definitionFor(type);
        assert clazz != null;
        InnerClassAttribute attribute = clazz.getInnerClassAttributeForThisClass();
        assert attribute != null;
        // Note that, to be consistent with the way inner-class attribute is written via minifier
        // lense, we are using attribute's outer-class, not the live context.
        String separator =
            computeInnerClassSeparator(attribute.getOuter(), type, attribute.getInnerName());
        if (separator == null) {
          separator = String.valueOf(INNER_CLASS_SEPARATOR);
        }
        state = getStateForOuterClass(outerClass, separator);
      }
    }
    if (state == null) {
      state = getStateForClass(type);
    }
    return state.nextTypeName(type);
  }

  private Namespace getStateForClass(DexType type) {
    String packageName = getPackageBinaryNameFromJavaType(type.getPackageDescriptor());
    // Check whether the given class should be kept.
    // or check whether the given class belongs to a package that is kept for another class.
    ProguardPackageNameList keepPackageNames =
        appView.options().getProguardConfiguration().getKeepPackageNamesPatterns();
    if (noObfuscationPrefixes.contains(packageName) || keepPackageNames.matches(type)) {
      return states.computeIfAbsent(packageName, Namespace::new);
    }
    Namespace state = topLevelState;
    switch (packageObfuscationMode) {
      case NONE:
        // For general obfuscation, rename all the involved package prefixes.
        state = getStateForPackagePrefix(packageName);
        break;
      case REPACKAGE:
        // For repackaging, all classes are repackaged to a single package.
        state = topLevelState;
        break;
      case FLATTEN:
        // For flattening, all packages are repackaged to a single package.
        state = states.computeIfAbsent(packageName, k -> {
          String renamedPackagePrefix = topLevelState.nextPackagePrefix();
          return new Namespace(renamedPackagePrefix);
        });
        break;
    }
    return state;
  }

  private Namespace getStateForPackagePrefix(String prefix) {
    Namespace state = states.get(prefix);
    if (state == null) {
      // Calculate the parent package prefix, e.g., La/b/c -> La/b
      String parentPackage = getParentPackagePrefix(prefix);
      Namespace superState;
      if (noObfuscationPrefixes.contains(parentPackage)) {
        // Restore a state for parent package prefix if it should be kept.
        superState = states.computeIfAbsent(parentPackage, Namespace::new);
      } else {
        // Create a state for parent package prefix, if necessary, in a recursive manner.
        // That recursion should end when the parent package hits the top-level, "".
        superState = getStateForPackagePrefix(parentPackage);
      }
      // From the super state, get a renamed package prefix for the current level.
      String renamedPackagePrefix = superState.nextPackagePrefix();
      // Create a new state, which corresponds to a new name space, for the current level.
      state = new Namespace(renamedPackagePrefix);
      states.put(prefix, state);
    }
    return state;
  }

  private Namespace getStateForOuterClass(DexType outer, String innerClassSeparator) {
    String prefix = getClassBinaryNameFromDescriptor(outer.toDescriptorString());
    Namespace state = states.get(prefix);
    if (state == null) {
      // Create a naming state with this classes renaming as prefix.
      DexString renamed = renaming.get(outer);
      if (renamed == null) {
        // The outer class has not been renamed yet, so rename the outer class first.
        // Note that here we proceed unconditionally---w/o regards to the existence of the outer
        // class: it could be the case that the outer class is not renamed because it's shrunk.
        // Even though that's the case, we can _implicitly_ assign a new name to the outer class
        // and then use that renamed name as a base prefix for the current inner class.
        renamed = computeName(outer);
        renaming.put(outer, renamed);
        KotlinMetadataRewriter.removeKotlinMetadataFromRenamedClass(appView, outer);
      }
      String binaryName = getClassBinaryNameFromDescriptor(renamed.toString());
      state = new Namespace(binaryName, innerClassSeparator);
      states.put(prefix, state);
    }
    return state;
  }

  private void renameArrayTypeIfNeeded(DexType type) {
    if (type.isArrayType()) {
      DexType base = type.toBaseType(appView.dexItemFactory());
      DexString value = renaming.get(base);
      if (value != null) {
        int dimensions = type.descriptor.numberOfLeadingSquareBrackets();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
          builder.append('[');
        }
        builder.append(value.toString());
        DexString descriptor = appView.dexItemFactory().createString(builder.toString());
        renaming.put(type, descriptor);
      }
    }
  }

  protected class Namespace implements InternalNamingState {

    private final String packageName;
    private final char[] packagePrefix;
    private int dictionaryIndex = 0;
    private int nameIndex = 1;

    Namespace(String packageName) {
      this(packageName, String.valueOf(DESCRIPTOR_PACKAGE_SEPARATOR));
    }

    Namespace(String packageName, String separator) {
      this.packageName = packageName;
      this.packagePrefix = ("L" + packageName
          // L or La/b/ (or La/b/C$)
          + (packageName.isEmpty() ? "" : separator))
          .toCharArray();
    }

    public String getPackageName() {
      return packageName;
    }

    DexString nextTypeName(DexType type) {
      DexString candidate =
          classNamingStrategy.next(type, packagePrefix, this, usedTypeNames::contains);
      assert !usedTypeNames.contains(candidate);
      usedTypeNames.add(candidate);
      return candidate;
    }

    String nextPackagePrefix() {
      String next = packageNamingStrategy.next(packagePrefix, this, usedPackagePrefixes::contains);
      assert !usedPackagePrefixes.contains(next);
      usedPackagePrefixes.add(next);
      return next;
    }

    @Override
    public int getDictionaryIndex() {
      return dictionaryIndex;
    }

    @Override
    public int incrementDictionaryIndex() {
      return dictionaryIndex++;
    }

    @Override
    public int incrementNameIndex(boolean isDirectMethodCall) {
      assert !isDirectMethodCall;
      return nameIndex++;
    }
  }

  protected interface ClassNamingStrategy {
    DexString next(
        DexType type, char[] packagePrefix, InternalNamingState state, Predicate<DexString> isUsed);

    /**
     * Returns the reserved descriptor for a type. If the type is not allowed to be obfuscated
     * (minified) it will return the original type descriptor. If applymapping is used, it will try
     * to return the applied name such that it can be reserved. Otherwise, if there are no
     * reservations, it will return null.
     *
     * @param type The type to find a reserved descriptor for
     * @return The reserved descriptor
     */
    DexString reservedDescriptor(DexType type);

    boolean isRenamedByApplyMapping(DexType type);
  }

  protected interface PackageNamingStrategy {
    String next(char[] packagePrefix, InternalNamingState state, Predicate<String> isUsed);
  }

  /**
   * Compute parent package prefix from the given package prefix.
   *
   * @param packagePrefix i.e. "Ljava/lang"
   * @return parent package prefix i.e. "Ljava"
   */
  static String getParentPackagePrefix(String packagePrefix) {
    int i = packagePrefix.lastIndexOf(DescriptorUtils.DESCRIPTOR_PACKAGE_SEPARATOR);
    if (i < 0) {
      return "";
    }
    return packagePrefix.substring(0, i);
  }
}
