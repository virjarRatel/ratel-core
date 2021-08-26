// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.TopDownClassHierarchyTraversal;
import com.android.tools.r8.ir.analysis.escape.EscapeAnalysis;
import com.android.tools.r8.ir.analysis.escape.EscapeAnalysisConfiguration;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import com.android.tools.r8.logging.Log;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collections;
import java.util.Set;

public class LibraryMethodOverrideAnalysis {

  private final AppView<AppInfoWithLiveness> appView;

  // Note: Set is accessed concurrently and must be thread-safe.
  private final Set<DexType> nonEscapingClassesWithLibraryMethodOverrides;

  // Maps each instruction type to the number of times that kind of instruction has caused a type to
  // escape.
  private final Object2IntMap<Class<?>> escapeDebuggingCounters =
      Log.ENABLED ? new Object2IntLinkedOpenHashMap<>() : null;

  public LibraryMethodOverrideAnalysis(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
    this.nonEscapingClassesWithLibraryMethodOverrides =
        Collections.synchronizedSet(
            getInitialNonEscapingClassesWithLibraryMethodOverrides(appView));
  }

  private static Set<DexType> getInitialNonEscapingClassesWithLibraryMethodOverrides(
      AppView<AppInfoWithLiveness> appView) {
    Set<DexType> initialNonEscapingClassesWithLibraryMethodOverrides =
        getClassesWithLibraryMethodOverrides(appView);

    // Remove all types that are pinned from the initial set of non-escaping classes.
    DexReference.filterDexType(appView.appInfo().pinnedItems.stream())
        .forEach(initialNonEscapingClassesWithLibraryMethodOverrides::remove);

    return initialNonEscapingClassesWithLibraryMethodOverrides;
  }

  private static Set<DexType> getClassesWithLibraryMethodOverrides(
      AppView<AppInfoWithLiveness> appView) {
    Set<DexType> classesWithLibraryMethodOverrides = Sets.newIdentityHashSet();
    TopDownClassHierarchyTraversal.forProgramClasses(appView)
        .visit(
            appView.appInfo().classes(),
            clazz -> {
              if (hasLibraryMethodOverrideDirectlyOrIndirectly(
                  clazz, classesWithLibraryMethodOverrides)) {
                classesWithLibraryMethodOverrides.add(clazz.type);
              }
            });
    return classesWithLibraryMethodOverrides;
  }

  private static boolean hasLibraryMethodOverrideDirectlyOrIndirectly(
      DexProgramClass clazz, Set<DexType> classesWithLibraryMethodOverrides) {
    return hasLibraryMethodOverrideDirectly(clazz)
        || hasLibraryMethodOverrideIndirectly(clazz, classesWithLibraryMethodOverrides);
  }

  private static boolean hasLibraryMethodOverrideDirectly(DexProgramClass clazz) {
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      if (method.accessFlags.isAbstract()) {
        continue;
      }
      if (method.isLibraryMethodOverride().isPossiblyTrue()) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasLibraryMethodOverrideIndirectly(
      DexProgramClass clazz, Set<DexType> classesWithLibraryMethodOverrides) {
    if (classesWithLibraryMethodOverrides.contains(clazz.superType)) {
      return true;
    }
    for (DexType interfaceType : clazz.interfaces.values) {
      if (classesWithLibraryMethodOverrides.contains(interfaceType)) {
        return true;
      }
    }
    return false;
  }

  public void analyze(IRCode code) {
    if (nonEscapingClassesWithLibraryMethodOverrides.isEmpty()) {
      // No need to run escape analysis since all types have already escaped.
      return;
    }

    // Must be thread local.
    EscapeAnalysis escapeAnalysis =
        new EscapeAnalysis(appView, LibraryEscapeAnalysisConfiguration.getInstance());

    for (Instruction instruction : code.instructions()) {
      if (instruction.isNewInstance()) {
        DexType type = instruction.asNewInstance().clazz;
        DexClass clazz = appView.definitionFor(type);
        if (clazz == null || !clazz.isProgramClass()) {
          continue;
        }

        if (nonEscapingClassesWithLibraryMethodOverrides.contains(type)) {
          // We need to remove this instance from the set of non-escaping classes if it escapes.
          if (escapeAnalysis.isEscaping(code, instruction.outValue())) {
            nonEscapingClassesWithLibraryMethodOverrides.remove(type);

            if (Log.ENABLED) {
              Set<Instruction> escapeRoutes =
                  escapeAnalysis.computeEscapeRoutes(code, instruction.outValue());
              for (Instruction escapeRoute : escapeRoutes) {
                Class<?> instructionClass = escapeRoute.getClass();
                escapeDebuggingCounters.put(
                    instructionClass, escapeDebuggingCounters.getInt(instructionClass) + 1);
              }
            }
          }
        }
      }
    }
  }

  public void finish() {
    assert verifyNoUninstantiatedTypesEscapeIntoLibrary();
    appView.setClassesEscapingIntoLibrary(
        type -> !nonEscapingClassesWithLibraryMethodOverrides.contains(type));
  }

  private boolean verifyNoUninstantiatedTypesEscapeIntoLibrary() {
    Set<DexType> classesWithLibraryMethodOverrides = getClassesWithLibraryMethodOverrides(appView);
    for (DexProgramClass clazz : appView.appInfo().classes()) {
      assert appView.appInfo().isInstantiatedDirectlyOrIndirectly(clazz)
          || !classesWithLibraryMethodOverrides.contains(clazz.type)
          || nonEscapingClassesWithLibraryMethodOverrides.contains(clazz.type);
    }
    return true;
  }

  public void logResults() {
    assert Log.ENABLED;
    Log.info(
        getClass(),
        "# classes with library method overrides: %s",
        getClassesWithLibraryMethodOverrides(appView).size());
    Log.info(
        getClass(),
        "# non-escaping classes with library method overrides: %s",
        nonEscapingClassesWithLibraryMethodOverrides.size());
    escapeDebuggingCounters
        .keySet()
        .forEach(
            (instructionClass) ->
                Log.info(
                    getClass(),
                    "# classes that escaped via %s: %s",
                    instructionClass.getSimpleName(),
                    escapeDebuggingCounters.getInt(instructionClass)));
  }

  static class LibraryEscapeAnalysisConfiguration implements EscapeAnalysisConfiguration {

    private static final LibraryEscapeAnalysisConfiguration INSTANCE =
        new LibraryEscapeAnalysisConfiguration();

    private LibraryEscapeAnalysisConfiguration() {}

    public static LibraryEscapeAnalysisConfiguration getInstance() {
      return INSTANCE;
    }

    @Override
    public boolean isLegitimateEscapeRoute(
        AppView<?> appView,
        EscapeAnalysis escapeAnalysis,
        Instruction escapeRoute,
        DexMethod context) {
      if (appView.appInfo().hasLiveness()) {
        return isLegitimateConstructorInvocation(
            appView.withLiveness(), escapeAnalysis, escapeRoute, context);
      }
      return false;
    }

    private boolean isLegitimateConstructorInvocation(
        AppView<AppInfoWithLiveness> appView,
        EscapeAnalysis escapeAnalysis,
        Instruction instruction,
        DexMethod context) {
      if (!instruction.isInvokeDirect()) {
        return false;
      }

      InvokeDirect invoke = instruction.asInvokeDirect();
      if (!appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())) {
        return false;
      }

      for (int i = 1; i < invoke.arguments().size(); i++) {
        if (escapeAnalysis.isValueOfInterestOrAlias(invoke.arguments().get(i))) {
          return false;
        }
      }

      DexEncodedMethod singleTarget = invoke.lookupSingleTarget(appView, context.holder);
      if (singleTarget == null) {
        return false;
      }

      InstanceInitializerInfo initializerInfo =
          singleTarget.getOptimizationInfo().getInstanceInitializerInfo();
      return initializerInfo.receiverNeverEscapesOutsideConstructorChain();
    }
  }
}
