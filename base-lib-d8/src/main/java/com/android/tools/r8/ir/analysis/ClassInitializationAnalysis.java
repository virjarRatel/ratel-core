// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CatchHandlers.CatchHandler;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.DominatorTree.Assumption;
import com.android.tools.r8.ir.code.DominatorTree.Inclusive;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeInterface;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeSuper;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.OptionalBool;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Analysis that given an instruction determines if a given type is guaranteed to be class
 * initialized prior the given instruction.
 */
public class ClassInitializationAnalysis {

  public enum AnalysisAssumption {
    INSTRUCTION_DOES_NOT_THROW,
    NONE
  }

  public enum Query {
    DIRECTLY,
    DIRECTLY_OR_INDIRECTLY
  }

  private static final ClassInitializationAnalysis TRIVIAL =
      new ClassInitializationAnalysis() {

        @Override
        public boolean isClassDefinitelyLoadedBeforeInstruction(
            DexType type, Instruction instruction) {
          return false;
        }
      };

  private final AppView<AppInfoWithLiveness> appView;
  private final IRCode code;
  private final DexItemFactory dexItemFactory;

  private DominatorTree dominatorTree = null;
  private int markingColor = -1;

  private ClassInitializationAnalysis() {
    this.appView = null;
    this.code = null;
    this.dexItemFactory = null;
  }

  public ClassInitializationAnalysis(AppView<AppInfoWithLiveness> appView, IRCode code) {
    this.appView = appView;
    this.code = code;
    this.dexItemFactory = appView.dexItemFactory();
  }

  // Returns a trivial, conservative analysis that always returns false.
  public static ClassInitializationAnalysis trivial() {
    return TRIVIAL;
  }

  public boolean isClassDefinitelyLoadedBeforeInstruction(DexType type, Instruction instruction) {
    DexType context = code.method.method.holder;
    BasicBlock block = instruction.getBlock();

    // Visit the instructions in `block` prior to `instruction`.
    for (Instruction previous : block.getInstructions()) {
      if (previous == instruction) {
        break;
      }
      if (previous.definitelyTriggersClassInitialization(
          type,
          context,
          appView,
          Query.DIRECTLY_OR_INDIRECTLY,
          // The given instruction is only reached if none of the instructions in the same
          // basic block throws, so we can safely assume that they will not.
          AnalysisAssumption.INSTRUCTION_DOES_NOT_THROW)) {
        return true;
      }
    }

    if (dominatorTree == null) {
      dominatorTree = new DominatorTree(code, Assumption.MAY_HAVE_UNREACHABLE_BLOCKS);
    }

    // Visit all the instructions in all the blocks that dominate `block`.
    for (BasicBlock dominator : dominatorTree.dominatorBlocks(block, Inclusive.NO)) {
      AnalysisAssumption assumption = getAssumptionForDominator(dominator, block);
      InstructionIterator instructionIterator = dominator.iterator();
      while (instructionIterator.hasNext()) {
        Instruction previous = instructionIterator.next();
        if (previous.definitelyTriggersClassInitialization(
            type, context, appView, Query.DIRECTLY_OR_INDIRECTLY, assumption)) {
          return true;
        }
        if (dominator.hasCatchHandlers() && previous.instructionTypeCanThrow()) {
          // All of the instructions that follow the first instruction that may throw are
          // guaranteed to be non-throwing. Hence they cannot cause any class initializations.
          assert Streams.stream(instructionIterator)
              .noneMatch(Instruction::instructionTypeCanThrow);
          break;
        }
      }
    }
    return false;
  }

  /**
   * Returns the analysis assumption to use when analyzing the instructions in the given dominator
   * block.
   *
   * <p>If the given block has no catch handlers, then we can safely assume that the instruction
   * does not throw, because execution would otherwise exit the method.
   *
   * <p>As a simple example, consider the method below. In order for the execution to get from the
   * call to A.foo() to the call to A.bar() the call A.foo() must not throw.
   *
   * <pre>
   *   public static void method() {
   *     A.foo();
   *     A.bar();
   *   }
   * </pre>
   *
   * This assumption cannot be made in the presence of intraprocedural exceptional control flow.
   * Consider the following example.
   *
   * <pre>
   *   public static void method(A instance) {
   *     try {
   *       instance.field = 42;
   *     } catch (Exception e) {
   *       A.foo();
   *       return;
   *     }
   *     A.bar();
   *   }
   * </pre>
   *
   * <p>At the call to A.foo() it is not guaranteed that the class A has been initialized, since
   * `instance` could always be null.
   *
   * <p>At the call to A.bar() it is guaranteed, since the instance field assignment succeeded.
   */
  private AnalysisAssumption getAssumptionForDominator(BasicBlock dominator, BasicBlock block) {
    if (!dominator.hasCatchHandlers()) {
      return AnalysisAssumption.INSTRUCTION_DOES_NOT_THROW;
    }

    Instruction exceptionalExit = dominator.exceptionalExit();
    if (exceptionalExit == null) {
      // The block cannot throw after all.
      return AnalysisAssumption.INSTRUCTION_DOES_NOT_THROW;
    }

    if (markingColor < 0) {
      markingColor = code.reserveMarkingColor();
      code.markTransitivePredecessors(block, markingColor);
    }

    for (CatchHandler<BasicBlock> catchHandler : dominator.getCatchHandlers()) {
      if (!catchHandler.target.isMarked(markingColor)) {
        // There is no path from this catch handler to the instruction of interest, so we can
        // ignore it.
        continue;
      }

      DexType guard = catchHandler.guard;
      if (exceptionalExit.isInstanceGet()
          || exceptionalExit.isInstancePut()
          || exceptionalExit.isInvokeMethodWithReceiver()) {
        // If an instance-get, instance-put, or instance-invoke instruction does not fail with a
        // NullPointerException, then the receiver class must have been initialized.
        OptionalBool isCatchingNPE = appView.isSubtype(dexItemFactory.npeType, guard);
        if (isCatchingNPE.isPossiblyTrue()) {
          return AnalysisAssumption.NONE;
        }
      }
      if (exceptionalExit.isStaticGet()
          || exceptionalExit.isStaticPut()
          || exceptionalExit.isInvokeStatic()) {
        // If a static-get, static-put, or invoke-static does not fail with an ExceptionIn-
        // InitializerError, then the holder class must have been initialized.
        OptionalBool isCatchingInitError =
            appView.isSubtype(dexItemFactory.exceptionInInitializerErrorType, guard);
        if (isCatchingInitError.isPossiblyTrue()) {
          return AnalysisAssumption.NONE;
        }
      }
    }

    // There are no paths from any of the catch handlers to the instruction of interest, so we can
    // assume that no instructions in the given block will throw (otherwise, the instruction of
    // interest will not be reached).
    return AnalysisAssumption.INSTRUCTION_DOES_NOT_THROW;
  }

  /**
   * The analysis reuses the dominator tree and basic block markings. If the underlying structure of
   * the IR changes, then this method must be called to reset the dominator tree, return the current
   * marking color, and clear all marks.
   */
  public void notifyCodeHasChanged() {
    dominatorTree = null;
    returnMarkingColor();
  }

  /** Returns the marking color, if any, and clears all marks. */
  public void finish() {
    returnMarkingColor();
  }

  private void returnMarkingColor() {
    if (markingColor >= 0) {
      code.returnMarkingColor(markingColor);
      markingColor = -1;
    }
  }

  public static class InstructionUtils {

    public static boolean forInstanceGet(
        InstanceGet instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      return forInstanceGetOrPut(instruction, type, appView, mode, assumption);
    }

    public static boolean forInstancePut(
        InstancePut instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      return forInstanceGetOrPut(instruction, type, appView, mode, assumption);
    }

    private static boolean forInstanceGetOrPut(
        FieldInstruction instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      assert instruction.isInstanceGet() || instruction.isInstancePut();
      if (assumption == AnalysisAssumption.NONE) {
        Value object =
            instruction.isInstanceGet()
                ? instruction.asInstanceGet().object()
                : instruction.asInstancePut().object();
        if (object.getTypeLattice().isNullable()) {
          // If the receiver is null we cannot be sure that the holder has been initialized.
          return false;
        }
      }
      DexEncodedField field = appView.appInfo().resolveField(instruction.getField());
      return field != null && isTypeInitializedBy(type, field, appView, mode);
    }

    public static boolean forInvokeDirect(
        InvokeDirect instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        if (instruction.getReceiver().getTypeLattice().isNullable()) {
          // If the receiver is null we cannot be sure that the holder has been initialized.
          return false;
        }
      }
      DexEncodedMethod method = appView.definitionFor(instruction.getInvokedMethod());
      return method != null && isTypeInitializedBy(type, method, appView, mode);
    }

    public static boolean forInvokeInterface(
        InvokeInterface instruction,
        DexType type,
        DexType context,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        if (instruction.getReceiver().getTypeLattice().isNullable()) {
          // If the receiver is null we cannot be sure that the holder has been initialized.
          return false;
        }
      }
      if (mode == Query.DIRECTLY) {
        // We cannot ensure exactly which class is being loaded because it depends on the runtime
        // type of the receiver.
        // TODO(christofferqa): We can do better if there is a unique target.
        return false;
      }
      if (appView.appInfo().hasLiveness()) {
        DexEncodedMethod singleTarget =
            instruction.lookupSingleTarget(appView.withLiveness(), context);
        if (singleTarget != null) {
          return isTypeInitializedBy(type, singleTarget, appView, mode);
        }
      }
      DexMethod method = instruction.getInvokedMethod();
      ResolutionResult resolutionResult =
          appView.appInfo().resolveMethodOnInterface(method.holder, method);
      if (!resolutionResult.isSingleResolution()) {
        return false;
      }
      DexType holder = resolutionResult.getSingleTarget().method.holder;
      return appView.isSubtype(holder, type).isTrue();
    }

    public static boolean forInvokeStatic(
        InvokeStatic instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        // Class initialization may fail with ExceptionInInitializerError.
        return false;
      }
      DexEncodedMethod method = appView.definitionFor(instruction.getInvokedMethod());
      return method != null && isTypeInitializedBy(type, method, appView, mode);
    }

    public static boolean forInvokeSuper(
        InvokeSuper instruction,
        DexType type,
        DexType context,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        if (instruction.getReceiver().getTypeLattice().isNullable()) {
          // If the receiver is null we cannot be sure that the holder has been initialized.
          return false;
        }
      }
      if (mode == Query.DIRECTLY) {
        // We cannot ensure exactly which class is being loaded because it depends on the runtime
        // type of the receiver.
        // TODO(christofferqa): We can do better if there is a unique target.
        return false;
      }
      if (appView.appInfo().hasLiveness()) {
        DexEncodedMethod singleTarget =
            instruction.lookupSingleTarget(appView.withLiveness(), context);
        if (singleTarget != null) {
          return isTypeInitializedBy(type, singleTarget, appView, mode);
        }
      }
      DexMethod method = instruction.getInvokedMethod();
      DexClass enclosingClass = appView.definitionFor(method.holder);
      if (enclosingClass == null) {
        return false;
      }
      DexType superType = enclosingClass.superType;
      if (superType == null) {
        return false;
      }
      ResolutionResult resolutionResult =
          appView.appInfo().resolveMethod(superType, method, instruction.itf);
      if (!resolutionResult.isSingleResolution()) {
        return false;
      }
      DexType holder = resolutionResult.getSingleTarget().method.holder;
      return appView.isSubtype(holder, type).isTrue();
    }

    public static boolean forInvokeVirtual(
        InvokeVirtual instruction,
        DexType type,
        DexType context,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        if (instruction.getReceiver().getTypeLattice().isNullable()) {
          // If the receiver is null we cannot be sure that the holder has been initialized.
          return false;
        }
      }
      if (mode == Query.DIRECTLY) {
        // We cannot ensure exactly which class is being loaded because it depends on the runtime
        // type of the receiver.
        // TODO(christofferqa): We can do better if there is a unique target.
        return false;
      }
      if (appView.appInfo().hasLiveness()) {
        DexEncodedMethod singleTarget =
            instruction.lookupSingleTarget(appView.withLiveness(), context);
        if (singleTarget != null) {
          return isTypeInitializedBy(type, singleTarget, appView, mode);
        }
      }
      DexMethod method = instruction.getInvokedMethod();
      ResolutionResult resolutionResult =
          appView.appInfo().resolveMethodOnClass(method.holder, method);
      if (!resolutionResult.isSingleResolution()) {
        return false;
      }
      DexType holder = resolutionResult.getSingleTarget().method.holder;
      return appView.isSubtype(holder, type).isTrue();
    }

    public static boolean forNewInstance(
        NewInstance instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      if (assumption == AnalysisAssumption.NONE) {
        // Instruction may throw.
        return false;
      }
      DexClass clazz = appView.definitionFor(instruction.clazz);
      return clazz != null && isTypeInitializedBy(type, clazz, appView, mode);
    }

    public static boolean forStaticGet(
        StaticGet instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      return forStaticGetOrPut(instruction, type, appView, mode, assumption);
    }

    public static boolean forStaticPut(
        StaticPut instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      return forStaticGetOrPut(instruction, type, appView, mode, assumption);
    }

    private static boolean forStaticGetOrPut(
        FieldInstruction instruction,
        DexType type,
        AppView<?> appView,
        Query mode,
        AnalysisAssumption assumption) {
      assert instruction.isStaticGet() || instruction.isStaticPut();
      if (assumption == AnalysisAssumption.NONE) {
        // Class initialization may fail with ExceptionInInitializerError.
        return false;
      }
      DexEncodedField field = appView.appInfo().resolveField(instruction.getField());
      return field != null && isTypeInitializedBy(type, field, appView, mode);
    }

    private static boolean isTypeInitializedBy(
        DexType typeToBeInitialized, DexDefinition definition, AppView<?> appView, Query mode) {
      if (mode == Query.DIRECTLY) {
        if (definition.isDexClass()) {
          return definition.asDexClass().type == typeToBeInitialized;
        }
        if (definition.isDexEncodedField()) {
          return definition.asDexEncodedField().field.holder == typeToBeInitialized;
        }
        if (definition.isDexEncodedMethod()) {
          return definition.asDexEncodedMethod().method.holder == typeToBeInitialized;
        }
        assert false;
        return false;
      }

      Set<DexType> visited = Sets.newIdentityHashSet();
      Deque<DexType> worklist = new ArrayDeque<>();

      if (definition.isDexClass()) {
        DexClass clazz = definition.asDexClass();
        enqueue(clazz.type, visited, worklist);
      } else if (definition.isDexEncodedField()) {
        DexEncodedField field = definition.asDexEncodedField();
        enqueue(field.field.holder, visited, worklist);
      } else if (definition.isDexEncodedMethod()) {
        DexEncodedMethod method = definition.asDexEncodedMethod();
        enqueue(method.method.holder, visited, worklist);
        enqueueInitializedClassesOnNormalExit(method, visited, worklist);
      } else {
        assert false;
      }

      while (!worklist.isEmpty()) {
        DexType typeKnownToBeInitialized = worklist.removeFirst();
        assert visited.contains(typeKnownToBeInitialized);

        if (appView.isSubtype(typeKnownToBeInitialized, typeToBeInitialized).isTrue()) {
          return true;
        }

        DexClass clazz = appView.definitionFor(typeKnownToBeInitialized);
        if (clazz != null) {
          DexEncodedMethod classInitializer = clazz.getClassInitializer();
          if (classInitializer != null) {
            enqueueInitializedClassesOnNormalExit(classInitializer, visited, worklist);
          }
        }
      }

      return false;
    }

    private static void enqueue(DexType type, Set<DexType> visited, Deque<DexType> worklist) {
      if (type.isClassType() && visited.add(type)) {
        worklist.add(type);
      }
    }

    private static void enqueueInitializedClassesOnNormalExit(
        DexEncodedMethod method, Set<DexType> visited, Deque<DexType> worklist) {
      for (DexType type : method.getOptimizationInfo().getInitializedClassesOnNormalExit()) {
        enqueue(type, visited, worklist);
      }
    }
  }
}
