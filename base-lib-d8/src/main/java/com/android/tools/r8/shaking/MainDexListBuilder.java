// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.SetUtils;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;

/**
 * Calculate the list of classes required in the main dex to allow legacy multidex loading.
 * Classes required in the main dex are:
 * <li> The classes with code executed before secondary dex files are installed.
 * <li> The "direct dependencies" of those classes, ie the classes required by dexopt.
 * <li> Annotation classes with a possible enum value and all classes annotated by them.
 */
public class MainDexListBuilder {

  private final Set<DexType> roots;
  private final AppInfoWithSubtyping appInfo;
  private final Map<DexType, Boolean> annotationTypeContainEnum;
  private final DexApplication dexApplication;
  private final MainDexClasses.Builder mainDexClassesBuilder;

  /**
   * @param roots Classes which code may be executed before secondary dex files loading.
   * @param application the dex appplication.
   */
  public MainDexListBuilder(Set<DexProgramClass> roots, DexApplication application) {
    this.dexApplication = application;
    this.appInfo = new AppInfoWithSubtyping(dexApplication);
    // Only consider program classes for the root set.
    this.roots = SetUtils.mapIdentityHashSet(roots, DexProgramClass::getType);
    mainDexClassesBuilder = MainDexClasses.builder(appInfo).addRoots(this.roots);
    DexClass enumType = appInfo.definitionFor(appInfo.dexItemFactory().enumType);
    if (enumType == null) {
      throw new CompilationError("Tracing for legacy multi dex is not possible without all"
          + " classpath libraries (java.lang.Enum is missing)");
    }
    DexClass annotationType = appInfo.definitionFor(appInfo.dexItemFactory().annotationType);
    if (annotationType == null) {
      throw new CompilationError("Tracing for legacy multi dex is not possible without all"
          + " classpath libraries (java.lang.annotation.Annotation is missing)");
    }
    annotationTypeContainEnum =
        Maps.newHashMapWithExpectedSize(
            appInfo.subtypes(appInfo.dexItemFactory().annotationType).size());
  }

  public MainDexClasses run() {
    traceMainDexDirectDependencies();
    traceRuntimeAnnotationsWithEnumForMainDex();
    return mainDexClassesBuilder.build();
  }

  private void traceRuntimeAnnotationsWithEnumForMainDex() {
    for (DexProgramClass clazz : dexApplication.classes()) {
      DexType dexType = clazz.type;
      if (mainDexClassesBuilder.contains(dexType)) {
        continue;
      }
      if (isAnnotation(dexType) && isAnnotationWithEnum(dexType)) {
        addAnnotationsWithEnum(clazz);
        continue;
      }
      // Classes with annotations must be in the same dex file as the annotation. As all
      // annotations with enums goes into the main dex, move annotated classes there as well.
      clazz.forEachAnnotation(
          annotation -> {
            if (!mainDexClassesBuilder.contains(dexType)
                && annotation.visibility == DexAnnotation.VISIBILITY_RUNTIME
                && isAnnotationWithEnum(annotation.annotation.type)) {
              addClassAnnotatedWithAnnotationWithEnum(dexType);
            }
          });
    }
  }

  private boolean isAnnotationWithEnum(DexType dexType) {
    Boolean value = annotationTypeContainEnum.get(dexType);
    if (value == null) {
      DexClass clazz = appInfo.definitionFor(dexType);
      if (clazz == null) {
        // Information is missing lets be conservative.
        value = true;
      } else {
        value = false;
        // Browse annotation values types in search for enum.
        // Each annotation value is represented by a virtual method.
        for (DexEncodedMethod method : clazz.virtualMethods()) {
          DexProto proto = method.method.proto;
          if (proto.parameters.isEmpty()) {
            DexType valueType = proto.returnType.toBaseType(appInfo.dexItemFactory());
            if (isEnum(valueType)) {
              value = true;
              break;
            } else if (isAnnotation(valueType) && isAnnotationWithEnum(valueType)) {
              value = true;
              break;
            }
          }
        }
      }
      annotationTypeContainEnum.put(dexType, value);
    }
    return value;
  }

  private boolean isEnum(DexType valueType) {
    return appInfo.isSubtype(valueType, appInfo.dexItemFactory().enumType);
  }

  private boolean isAnnotation(DexType valueType) {
    return appInfo.isSubtype(valueType, appInfo.dexItemFactory().annotationType);
  }

  private boolean isProgramClass(DexType dexType) {
    DexClass clazz = appInfo.definitionFor(dexType);
    return clazz != null && clazz.isProgramClass();
  }

  private void traceMainDexDirectDependencies() {
    new MainDexDirectReferenceTracer(appInfo, this::addDirectDependency)
        .run(roots);
  }

  private void addAnnotationsWithEnum(DexProgramClass clazz) {
    // Add the annotation class as a direct dependency.
    addDirectDependency(clazz);
    // Add enum classes used for values as direct dependencies.
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      DexProto proto = method.method.proto;
      if (proto.parameters.isEmpty()) {
        DexType valueType = proto.returnType.toBaseType(appInfo.dexItemFactory());
        if (isEnum(valueType)) {
          addDirectDependency(valueType);
        }
      }
    }
  }

  private void addClassAnnotatedWithAnnotationWithEnum(DexType type) {
    // Just add classes annotated with annotations with enum ad direct dependencies.
    addDirectDependency(type);
  }

  private void addDirectDependency(DexType type) {
    // Consider only component type of arrays
    type = type.toBaseType(appInfo.dexItemFactory());

    if (!type.isClassType() || mainDexClassesBuilder.contains(type)) {
      return;
    }

    DexClass clazz = appInfo.definitionFor(type);
    // No library classes in main-dex.
    if (clazz == null || clazz.isNotProgramClass()) {
      return;
    }
    addDirectDependency(clazz.asProgramClass());
  }

  private void addDirectDependency(DexProgramClass dexClass) {
    DexType type = dexClass.type;
    assert !mainDexClassesBuilder.contains(type);
    mainDexClassesBuilder.addDependency(type);
    if (dexClass.superType != null) {
      addDirectDependency(dexClass.superType);
    }
    for (DexType interfaze : dexClass.interfaces.values) {
      addDirectDependency(interfaze);
    }
  }
}
