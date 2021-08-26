// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DefaultInstructionVisitor;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.DominatorTree.Assumption;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An analysis that given a method returns a set of types that are guaranteed to be initialized by
 * the method on all normal exits of the given method.
 */
public class InitializedClassesOnNormalExitAnalysis {

  public static Set<DexType> computeInitializedClassesOnNormalExit(
      AppView<AppInfoWithLiveness> appView, IRCode code) {
    DominatorTree dominatorTree = new DominatorTree(code, Assumption.MAY_HAVE_UNREACHABLE_BLOCKS);
    Visitor visitor = new Visitor(appView, code.method.method.holder);
    for (BasicBlock dominator : dominatorTree.normalExitDominatorBlocks()) {
      if (dominator.hasCatchHandlers()) {
        // When determining which classes that are guaranteed to be initialized from a given
        // instruction, we assume that the given instruction does not throw. Therefore, we skip
        // blocks that have a catch handler.
        continue;
      }

      for (Instruction instruction : dominator.getInstructions()) {
        instruction.accept(visitor);
      }
    }
    return visitor.build();
  }

  private static class Visitor extends DefaultInstructionVisitor<Void> {

    private final AppView<AppInfoWithLiveness> appView;
    private final DexType context;
    private final Set<DexType> initializedClassesOnNormalExit = Sets.newIdentityHashSet();

    Visitor(AppView<AppInfoWithLiveness> appView, DexType context) {
      this.appView = appView;
      this.context = context;
    }

    Set<DexType> build() {
      return Collections.unmodifiableSet(initializedClassesOnNormalExit);
    }

    private void markInitializedOnNormalExit(Iterable<DexType> knownToBeInitialized) {
      knownToBeInitialized.forEach(this::markInitializedOnNormalExit);
    }

    private void markInitializedOnNormalExit(DexType knownToBeInitialized) {
      if (knownToBeInitialized == context) {
        // Do not record that the given method causes its own holder to be initialized, since this
        // is trivial.
        return;
      }
      DexClass clazz = appView.definitionFor(knownToBeInitialized);
      if (clazz == null) {
        return;
      }
      if (!clazz.isProgramClass()) {
        // Only mark program classes as being initialized on normal exits.
        return;
      }
      if (!clazz.classInitializationMayHaveSideEffects(appView)) {
        // Only mark classes that actually have side effects during class initialization.
        return;
      }
      List<DexType> subsumedByKnownToBeInitialized = null;
      for (DexType alreadyKnownToBeInitialized : initializedClassesOnNormalExit) {
        if (appView.isSubtype(alreadyKnownToBeInitialized, knownToBeInitialized).isTrue()) {
          // The fact that there is a subtype B of the given type A, which is known to be
          // initialized, implies that the given type A is also be initialized. Therefore, we do not
          // add this type into the set of classes that have been initialized on all normal exits.
          return;
        }

        if (appView.isSubtype(knownToBeInitialized, alreadyKnownToBeInitialized).isTrue()) {
          if (subsumedByKnownToBeInitialized == null) {
            subsumedByKnownToBeInitialized = new ArrayList<>();
          }
          subsumedByKnownToBeInitialized.add(alreadyKnownToBeInitialized);
        }
      }
      initializedClassesOnNormalExit.add(knownToBeInitialized);
      if (subsumedByKnownToBeInitialized != null) {
        initializedClassesOnNormalExit.removeAll(subsumedByKnownToBeInitialized);
      }
    }

    @Override
    public Void handleFieldInstruction(FieldInstruction instruction) {
      DexEncodedField field = appView.appInfo().resolveField(instruction.getField());
      if (field != null) {
        if (field.field.holder.isClassType()) {
          markInitializedOnNormalExit(field.field.holder);
        } else {
          assert false : "Expected holder of field type to be a class type";
        }
      }
      return null;
    }

    @Override
    public Void handleInvoke(Invoke instruction) {
      if (instruction.isInvokeMethod()) {
        InvokeMethod invoke = instruction.asInvokeMethod();
        DexMethod method = invoke.getInvokedMethod();
        if (method.holder.isClassType()) {
          DexEncodedMethod singleTarget = invoke.lookupSingleTarget(appView, context);
          if (singleTarget != null) {
            markInitializedOnNormalExit(singleTarget.method.holder);
            markInitializedOnNormalExit(
                singleTarget.getOptimizationInfo().getInitializedClassesOnNormalExit());
          } else {
            markInitializedOnNormalExit(method.holder);
          }
        }
      }
      return null;
    }

    @Override
    public Void visit(NewInstance instruction) {
      markInitializedOnNormalExit(instruction.clazz);
      return null;
    }
  }
}
