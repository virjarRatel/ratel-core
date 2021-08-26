// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import java.util.Set;
import java.util.function.BooleanSupplier;

// TODO(b/112437944): Should remove the new Builder() instructions from each dynamicMethod() that
//  references a dead proto builder.
public class GeneratedMessageLiteBuilderShrinker {

  private final ProtoReferences references;

  GeneratedMessageLiteBuilderShrinker(ProtoReferences references) {
    this.references = references;
  }

  /** Returns true if an action was deferred. */
  public boolean deferDeadProtoBuilders(
      DexProgramClass clazz, DexEncodedMethod context, BooleanSupplier register) {
    if (references.isDynamicMethod(context) && references.isGeneratedMessageLiteBuilder(clazz)) {
      return register.getAsBoolean();
    }
    return false;
  }

  public static void addInliningHeuristicsForBuilderInlining(
      AppView<? extends AppInfoWithSubtyping> appView,
      Set<DexMethod> alwaysInline,
      Set<DexMethod> neverInline,
      Set<DexMethod> bypassClinitforInlining) {
    new RootSetExtension(appView, alwaysInline, neverInline, bypassClinitforInlining).extend();
  }

  private static class RootSetExtension {

    private final AppView<? extends AppInfoWithSubtyping> appView;
    private final ProtoReferences references;

    private final Set<DexMethod> alwaysInline;
    private final Set<DexMethod> neverInline;
    private final Set<DexMethod> bypassClinitforInlining;

    RootSetExtension(
        AppView<? extends AppInfoWithSubtyping> appView,
        Set<DexMethod> alwaysInline,
        Set<DexMethod> neverInline,
        Set<DexMethod> bypassClinitforInlining) {
      this.appView = appView;
      this.references = appView.protoShrinker().references;
      this.alwaysInline = alwaysInline;
      this.neverInline = neverInline;
      this.bypassClinitforInlining = bypassClinitforInlining;
    }

    void extend() {
      // GeneratedMessageLite heuristics.
      alwaysInlineCreateBuilderFromGeneratedMessageLite();
      neverInlineIsInitializedFromGeneratedMessageLite();

      // * extends GeneratedMessageLite heuristics.
      bypassClinitforInliningNewBuilderMethods();
      alwaysInlineDynamicMethodFromGeneratedMessageLiteImplementations();

      // GeneratedMessageLite$Builder heuristics.
      alwaysInlineBuildPartialFromGeneratedMessageLiteBuilder();
    }

    private void bypassClinitforInliningNewBuilderMethods() {
      for (DexType type : appView.appInfo().subtypes(references.generatedMessageLiteType)) {
        DexProgramClass clazz = appView.definitionFor(type).asProgramClass();
        if (clazz != null) {
          DexEncodedMethod newBuilderMethod =
              clazz.lookupDirectMethod(
                  method -> method.method.name == references.newBuilderMethodName);
          if (newBuilderMethod != null) {
            bypassClinitforInlining.add(newBuilderMethod.method);
          }
        }
      }
    }

    private void alwaysInlineBuildPartialFromGeneratedMessageLiteBuilder() {
      alwaysInline.add(references.generatedMessageLiteBuilderMethods.buildPartialMethod);
    }

    private void alwaysInlineCreateBuilderFromGeneratedMessageLite() {
      alwaysInline.add(references.generatedMessageLiteMethods.createBuilderMethod);
    }

    private void alwaysInlineDynamicMethodFromGeneratedMessageLiteImplementations() {
      // TODO(b/132600418): We should be able to determine that dynamicMethod() becomes 'SIMPLE'
      //  when the MethodToInvoke argument is MethodToInvoke.NEW_BUILDER.
      DexItemFactory dexItemFactory = appView.dexItemFactory();
      for (DexType type : appView.appInfo().subtypes(references.generatedMessageLiteType)) {
        alwaysInline.add(
            dexItemFactory.createMethod(
                type,
                dexItemFactory.createProto(
                    dexItemFactory.objectType,
                    references.methodToInvokeType,
                    dexItemFactory.objectType,
                    dexItemFactory.objectType),
                "dynamicMethod"));
      }
    }

    /**
     * Without this rule, GeneratedMessageLite$Builder.build() becomes too big for class inlining.
     * TODO(b/112437944): Maybe introduce a -neverinlineinto rule instead?
     */
    private void neverInlineIsInitializedFromGeneratedMessageLite() {
      neverInline.add(references.generatedMessageLiteMethods.isInitializedMethod);
    }
  }
}
