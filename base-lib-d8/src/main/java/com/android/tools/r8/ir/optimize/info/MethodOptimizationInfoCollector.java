// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.info;

import static com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query.DIRECTLY;
import static com.android.tools.r8.ir.code.DominatorTree.Assumption.MAY_HAVE_UNREACHABLE_BLOCKS;
import static com.android.tools.r8.ir.code.Opcodes.ARGUMENT;
import static com.android.tools.r8.ir.code.Opcodes.ASSUME;
import static com.android.tools.r8.ir.code.Opcodes.CHECK_CAST;
import static com.android.tools.r8.ir.code.Opcodes.CONST_CLASS;
import static com.android.tools.r8.ir.code.Opcodes.CONST_NUMBER;
import static com.android.tools.r8.ir.code.Opcodes.CONST_STRING;
import static com.android.tools.r8.ir.code.Opcodes.DEX_ITEM_BASED_CONST_STRING;
import static com.android.tools.r8.ir.code.Opcodes.GOTO;
import static com.android.tools.r8.ir.code.Opcodes.IF;
import static com.android.tools.r8.ir.code.Opcodes.INSTANCE_GET;
import static com.android.tools.r8.ir.code.Opcodes.INSTANCE_OF;
import static com.android.tools.r8.ir.code.Opcodes.INSTANCE_PUT;
import static com.android.tools.r8.ir.code.Opcodes.INVOKE_DIRECT;
import static com.android.tools.r8.ir.code.Opcodes.INVOKE_INTERFACE;
import static com.android.tools.r8.ir.code.Opcodes.INVOKE_STATIC;
import static com.android.tools.r8.ir.code.Opcodes.INVOKE_VIRTUAL;
import static com.android.tools.r8.ir.code.Opcodes.NEW_INSTANCE;
import static com.android.tools.r8.ir.code.Opcodes.RETURN;
import static com.android.tools.r8.ir.code.Opcodes.STATIC_GET;
import static com.android.tools.r8.ir.code.Opcodes.THROW;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.DeterminismAnalysis;
import com.android.tools.r8.ir.analysis.InitializedClassesOnNormalExitAnalysis;
import com.android.tools.r8.ir.analysis.sideeffect.ClassInitializerSideEffectAnalysis;
import com.android.tools.r8.ir.analysis.sideeffect.ClassInitializerSideEffectAnalysis.ClassInitializerSideEffect;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.DynamicTypeOptimization;
import com.android.tools.r8.ir.optimize.classinliner.ClassInlinerEligibilityInfo;
import com.android.tools.r8.ir.optimize.classinliner.ClassInlinerReceiverAnalysis;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsage;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsageBuilder;
import com.android.tools.r8.ir.optimize.info.initializer.DefaultInstanceInitializerInfo;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import com.android.tools.r8.ir.optimize.info.initializer.NonTrivialInstanceInitializerInfo;
import com.android.tools.r8.kotlin.Kotlin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class MethodOptimizationInfoCollector {
  private final AppView<AppInfoWithLiveness> appView;
  private final InternalOptions options;
  private final DexItemFactory dexItemFactory;

  public MethodOptimizationInfoCollector(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
    this.options = appView.options();
    this.dexItemFactory = appView.dexItemFactory();
  }

  public void collectMethodOptimizationInfo(
      DexEncodedMethod method,
      IRCode code,
      OptimizationFeedback feedback,
      DynamicTypeOptimization dynamicTypeOptimization) {
    identifyClassInlinerEligibility(method, code, feedback);
    identifyParameterUsages(method, code, feedback);
    identifyReturnsArgument(method, code, feedback);
    if (options.enableInlining) {
      identifyInvokeSemanticsForInlining(method, code, appView, feedback);
    }
    computeDynamicReturnType(dynamicTypeOptimization, feedback, method, code);
    computeInitializedClassesOnNormalExit(feedback, method, code);
    computeInstanceInitializerInfo(method, code, feedback);
    computeMayHaveSideEffects(feedback, method, code);
    computeReturnValueOnlyDependsOnArguments(feedback, method, code);
    computeNonNullParamOrThrow(feedback, method, code);
    computeNonNullParamOnNormalExits(feedback, code);
  }

  private void identifyClassInlinerEligibility(
      DexEncodedMethod method, IRCode code, OptimizationFeedback feedback) {
    // Method eligibility is calculated in similar way for regular method
    // and for the constructor. To be eligible method should only be using its
    // receiver in the following ways:
    //
    //  (1) as a receiver of reads/writes of instance fields of the holder class,
    //  (2) as a return value,
    //  (3) as a receiver of a call to the superclass initializer. Note that we don't
    //      check what is passed to superclass initializer as arguments, only check
    //      that it is not the instance being initialized,
    //  (4) as argument to a monitor instruction.
    //
    // Note that (4) can safely be removed as the receiver is guaranteed not to escape when we class
    // inline it, and hence any monitor instructions are no-ops.
    boolean instanceInitializer = method.isInstanceInitializer();
    if (method.accessFlags.isNative()
        || (!method.isNonAbstractVirtualMethod() && !instanceInitializer)) {
      return;
    }

    feedback.setClassInlinerEligibility(method, null);  // To allow returns below.

    Value receiver = code.getThis();
    if (receiver.numberOfPhiUsers() > 0) {
      return;
    }

    DexClass clazz = appView.definitionFor(method.method.holder);
    if (clazz == null) {
      return;
    }

    boolean seenSuperInitCall = false;
    for (Instruction insn : receiver.aliasedUsers()) {
      if (insn.isAssume()) {
        continue;
      }

      if (insn.isMonitor()) {
        continue;
      }

      if (insn.isInstanceGet() || insn.isInstancePut()) {
        if (insn.isInstancePut()) {
          InstancePut instancePutInstruction = insn.asInstancePut();
          // Only allow field writes to the receiver.
          if (instancePutInstruction.object().getAliasedValue() != receiver) {
            return;
          }
          // Do not allow the receiver to escape via a field write.
          if (instancePutInstruction.value().getAliasedValue() == receiver) {
            return;
          }
        }
        DexField field = insn.asFieldInstruction().getField();
        if (appView.appInfo().resolveFieldOn(clazz, field) != null) {
          // Require only accessing direct or indirect instance fields of the current class.
          continue;
        }
        return;
      }

      // If this is an instance initializer allow one call to superclass instance initializer.
      if (insn.isInvokeDirect()) {
        InvokeDirect invokedDirect = insn.asInvokeDirect();
        DexMethod invokedMethod = invokedDirect.getInvokedMethod();
        if (dexItemFactory.isConstructor(invokedMethod)
            && invokedMethod.holder == clazz.superType
            && ListUtils.lastIndexMatching(
                invokedDirect.inValues(), v -> v.getAliasedValue() == receiver) == 0
            && !seenSuperInitCall
            && instanceInitializer) {
          seenSuperInitCall = true;
          continue;
        }
        // We don't support other direct calls yet.
        return;
      }

      if (insn.isReturn()) {
        continue;
      }

      // Other receiver usages make the method not eligible.
      return;
    }

    if (instanceInitializer && !seenSuperInitCall) {
      // Call to super constructor not found?
      return;
    }

    feedback.setClassInlinerEligibility(
        method,
        new ClassInlinerEligibilityInfo(
            new ClassInlinerReceiverAnalysis(appView, method, code).computeReturnsReceiver()));
  }

  private void identifyParameterUsages(
      DexEncodedMethod method, IRCode code, OptimizationFeedback feedback) {
    List<ParameterUsage> usages = new ArrayList<>();
    List<Value> values = code.collectArguments();
    for (int i = 0; i < values.size(); i++) {
      Value value = values.get(i);
      if (value.numberOfPhiUsers() > 0) {
        continue;
      }
      ParameterUsage usage = collectParameterUsages(i, value);
      if (usage != null) {
        usages.add(usage);
      }
    }
    feedback.setParameterUsages(
        method,
        usages.isEmpty()
            ? DefaultMethodOptimizationInfo.UNKNOWN_PARAMETER_USAGE_INFO
            : new ParameterUsagesInfo(usages));
  }

  private ParameterUsage collectParameterUsages(int i, Value value) {
    ParameterUsageBuilder builder = new ParameterUsageBuilder(value, i);
    for (Instruction user : value.aliasedUsers()) {
      if (!builder.note(user)) {
        return null;
      }
    }
    return builder.build();
  }

  private void identifyReturnsArgument(
      DexEncodedMethod method, IRCode code, OptimizationFeedback feedback) {
    List<BasicBlock> normalExits = code.computeNormalExitBlocks();
    if (normalExits.isEmpty()) {
      feedback.methodNeverReturnsNormally(method);
      return;
    }
    Return firstExit = normalExits.get(0).exit().asReturn();
    if (firstExit.isReturnVoid()) {
      return;
    }
    Value returnValue = firstExit.returnValue();
    boolean isNeverNull = returnValue.getTypeLattice().isReference() && returnValue.isNeverNull();
    for (int i = 1; i < normalExits.size(); i++) {
      Return exit = normalExits.get(i).exit().asReturn();
      Value value = exit.returnValue();
      if (value != returnValue) {
        returnValue = null;
      }
      isNeverNull &= value.getTypeLattice().isReference() && value.isNeverNull();
    }
    if (returnValue != null) {
      Value aliasedValue = returnValue.getAliasedValue();
      if (!aliasedValue.isPhi()) {
        Instruction definition = aliasedValue.definition;
        if (definition.isArgument()) {
          // Find the argument number.
          int index = aliasedValue.computeArgumentPosition(code);
          assert index >= 0;
          feedback.methodReturnsArgument(method, index);
        }
        DexType context = method.method.holder;
        AbstractValue abstractReturnValue = definition.getAbstractValue(appView, context);
        if (abstractReturnValue.isNonTrivial()) {
          feedback.methodReturnsAbstractValue(method, appView, abstractReturnValue);
        }
      }
    }
    if (isNeverNull) {
      feedback.methodNeverReturnsNull(method);
    }
  }

  private void computeInstanceInitializerInfo(
      DexEncodedMethod method, IRCode code, OptimizationFeedback feedback) {
    assert !appView.appInfo().isPinned(method.method);

    if (!method.isInstanceInitializer()) {
      return;
    }

    if (method.accessFlags.isNative()) {
      return;
    }

    if (appView.appInfo().mayHaveSideEffects.containsKey(method.method)) {
      return;
    }

    DexClass clazz = appView.appInfo().definitionFor(method.method.holder);
    if (clazz == null) {
      assert false;
      return;
    }

    InstanceInitializerInfo instanceInitializerInfo = analyzeInstanceInitializer(code, clazz);
    feedback.setInstanceInitializerInfo(
        method,
        instanceInitializerInfo != null
            ? instanceInitializerInfo
            : DefaultInstanceInitializerInfo.getInstance());
  }

  // This method defines trivial instance initializer as follows:
  //
  // ** The holder class must not define a finalize method.
  //
  // ** The initializer may call the initializer of the base class, which
  //    itself must be trivial.
  //
  // ** java.lang.Object.<init>() is considered trivial.
  //
  // ** all arguments passed to a super-class initializer must be non-throwing
  //    constants or arguments.
  //
  // ** Assigns arguments or non-throwing constants to fields of this class.
  //
  // (Note that this initializer does not have to have zero arguments.)
  private InstanceInitializerInfo analyzeInstanceInitializer(IRCode code, DexClass clazz) {
    if (clazz.definesFinalizer(options.itemFactory)) {
      // Defining a finalize method can observe the side-effect of Object.<init> GC registration.
      return null;
    }

    NonTrivialInstanceInitializerInfo.Builder builder = NonTrivialInstanceInitializerInfo.builder();
    Value receiver = code.getThis();
    boolean hasCatchHandler = false;
    for (BasicBlock block : code.blocks) {
      if (block.hasCatchHandlers()) {
        hasCatchHandler = true;
      }

      for (Instruction instruction : block.getInstructions()) {
        switch (instruction.opcode()) {
          case ARGUMENT:
          case ASSUME:
          case CONST_NUMBER:
          case GOTO:
          case RETURN:
            break;

          case IF:
            builder.setInstanceFieldInitializationMayDependOnEnvironment();
            break;

          case CHECK_CAST:
          case CONST_CLASS:
          case CONST_STRING:
          case DEX_ITEM_BASED_CONST_STRING:
          case INSTANCE_OF:
          case THROW:
            // These instructions types may raise an exception, which is a side effect. None of the
            // instructions can trigger class initialization side effects, hence it is not necessary
            // to mark all fields as potentially being read. Also, none of the instruction types
            // can cause the receiver to escape.
            if (instruction.instructionMayHaveSideEffects(appView, clazz.type)) {
              builder.setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
            }
            break;

          case INSTANCE_GET:
          case STATIC_GET:
            {
              FieldInstruction fieldGet = instruction.asFieldInstruction();
              DexEncodedField field = appView.appInfo().resolveField(fieldGet.getField());
              if (field == null) {
                return null;
              }
              builder.markFieldAsRead(field);
              if (fieldGet.instructionMayHaveSideEffects(appView, clazz.type)) {
                builder.setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
                if (fieldGet.isStaticGet()) {
                  // It could trigger a class initializer.
                  builder.markAllFieldsAsRead();
                }
              }
            }
            break;

          case INSTANCE_PUT:
            {
              InstancePut instancePut = instruction.asInstancePut();
              DexEncodedField field = appView.appInfo().resolveField(instancePut.getField());
              if (field == null) {
                return null;
              }
              if (instancePut.object().getAliasedValue() != receiver
                  || instancePut.instructionInstanceCanThrow(appView, clazz.type).isThrowing()) {
                builder.setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
              }

              Value value = instancePut.value().getAliasedValue();
              // TODO(b/142762134): Replace the use of onlyDependsOnArgument() by
              //  ValueMayDependOnEnvironmentAnalysis.
              if (!value.onlyDependsOnArgument()) {
                builder.setInstanceFieldInitializationMayDependOnEnvironment();
              }
              if (value == receiver) {
                builder.setReceiverMayEscapeOutsideConstructorChain();
              }
            }
            break;

          case INVOKE_DIRECT:
            {
              InvokeDirect invoke = instruction.asInvokeDirect();
              DexMethod invokedMethod = invoke.getInvokedMethod();
              DexEncodedMethod singleTarget = appView.definitionFor(invokedMethod);
              if (singleTarget == null) {
                return null;
              }
              if (singleTarget.isInstanceInitializer() && invoke.getReceiver() == receiver) {
                if (builder.hasParent()) {
                  return null;
                }
                // java.lang.Enum.<init>() and java.lang.Object.<init>() are considered trivial.
                if (invokedMethod == dexItemFactory.enumMethods.constructor
                    || invokedMethod == dexItemFactory.objectMethods.constructor) {
                  builder.setParent(invokedMethod);
                  break;
                }
                builder.merge(singleTarget.getOptimizationInfo().getInstanceInitializerInfo());
                for (int i = 1; i < invoke.arguments().size(); i++) {
                  Value argument = invoke.arguments().get(i).getAliasedValue();
                  if (argument == receiver) {
                    // In the analysis of the parent constructor, we don't consider the non-receiver
                    // arguments as being aliases of the receiver. Therefore, we explicitly mark
                    // that the receiver escapes from this constructor.
                    builder.setReceiverMayEscapeOutsideConstructorChain();
                  }
                  if (!argument.onlyDependsOnArgument()) {
                    // If the parent constructor assigns this argument into a field, then the value
                    // of the field may depend on the environment.
                    builder.setInstanceFieldInitializationMayDependOnEnvironment();
                  }
                }
                builder.setParent(invokedMethod);
              } else {
                builder
                    .markAllFieldsAsRead()
                    .setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
                for (Value inValue : invoke.inValues()) {
                  if (inValue.getAliasedValue() == receiver) {
                    builder.setReceiverMayEscapeOutsideConstructorChain();
                    break;
                  }
                }
              }
            }
            break;

          case INVOKE_INTERFACE:
          case INVOKE_STATIC:
          case INVOKE_VIRTUAL:
            InvokeMethod invoke = instruction.asInvokeMethod();
            builder.markAllFieldsAsRead().setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
            for (Value inValue : invoke.inValues()) {
              if (inValue.getAliasedValue() == receiver) {
                builder.setReceiverMayEscapeOutsideConstructorChain();
                break;
              }
            }
            break;

          case NEW_INSTANCE:
            {
              NewInstance newInstance = instruction.asNewInstance();
              if (newInstance.instructionMayHaveSideEffects(appView, clazz.type)) {
                // It could trigger a class initializer.
                builder
                    .markAllFieldsAsRead()
                    .setMayHaveOtherSideEffectsThanInstanceFieldAssignments();
              }
            }
            break;

          default:
            builder
                .markAllFieldsAsRead()
                .setInstanceFieldInitializationMayDependOnEnvironment()
                .setMayHaveOtherSideEffectsThanInstanceFieldAssignments()
                .setReceiverMayEscapeOutsideConstructorChain();
            break;
        }
      }
    }

    // In presence of exceptional control flow, the assignments to the instance fields could depend
    // on the environment, if there is an instruction that could throw.
    //
    // Example:
    //   void <init>() {
    //     try {
    //       throwIfTrue(Environment.STATIC_FIELD);
    //     } catch (Exception e) {
    //       this.f = 42;
    //     }
    //   }
    if (hasCatchHandler && builder.mayHaveOtherSideEffectsThanInstanceFieldAssignments()) {
      builder.setInstanceFieldInitializationMayDependOnEnvironment();
    }

    return builder.build();
  }

  private void identifyInvokeSemanticsForInlining(
      DexEncodedMethod method, IRCode code, AppView<?> appView, OptimizationFeedback feedback) {
    if (method.isStatic()) {
      // Identifies if the method preserves class initialization after inlining.
      feedback.markTriggerClassInitBeforeAnySideEffect(
          method, triggersClassInitializationBeforeSideEffect(method.method.holder, code, appView));
    } else {
      // Identifies if the method preserves null check of the receiver after inlining.
      final Value receiver = code.getThis();
      feedback.markCheckNullReceiverBeforeAnySideEffect(
          method, receiver.isUsed() && checksNullBeforeSideEffect(code, receiver, appView));
    }
  }

  /**
   * Returns true if the given code unconditionally triggers class initialization before any other
   * side effecting instruction.
   *
   * <p>Note: we do not track phis so we may return false negative. This is a conservative approach.
   */
  private static boolean triggersClassInitializationBeforeSideEffect(
      DexType clazz, IRCode code, AppView<?> appView) {
    return alwaysTriggerExpectedEffectBeforeAnythingElse(
        code,
        (instruction, it) -> {
          DexType context = code.method.method.holder;
          if (instruction.definitelyTriggersClassInitialization(
              clazz, context, appView, DIRECTLY, AnalysisAssumption.INSTRUCTION_DOES_NOT_THROW)) {
            // In order to preserve class initialization semantic, the exception must not be caught
            // by any handler. Therefore, we must ignore this instruction if it is covered by a
            // catch handler.
            // Note: this is a conservative approach where we consider that any catch handler could
            // catch the exception, even if it cannot catch an ExceptionInInitializerError.
            if (!instruction.getBlock().hasCatchHandlers()) {
              // We found an instruction that preserves initialization of the class.
              return InstructionEffect.DESIRED_EFFECT;
            }
          } else if (instruction.instructionMayHaveSideEffects(appView, clazz)) {
            // We found a side effect before class initialization.
            return InstructionEffect.OTHER_EFFECT;
          }
          return InstructionEffect.NO_EFFECT;
        });
  }

  /**
   * Returns true if the given code unconditionally triggers an expected effect before anything
   * else, false otherwise.
   *
   * <p>Note: we do not track phis so we may return false negative. This is a conservative approach.
   */
  private static boolean alwaysTriggerExpectedEffectBeforeAnythingElse(
      IRCode code, BiFunction<Instruction, InstructionIterator, InstructionEffect> function) {
    final int color = code.reserveMarkingColor();
    try {
      ArrayDeque<BasicBlock> worklist = new ArrayDeque<>();
      final BasicBlock entry = code.entryBlock();
      worklist.add(entry);
      entry.mark(color);

      while (!worklist.isEmpty()) {
        BasicBlock currentBlock = worklist.poll();
        assert currentBlock.isMarked(color);

        InstructionEffect result = InstructionEffect.NO_EFFECT;
        InstructionIterator it = currentBlock.iterator();
        while (result == InstructionEffect.NO_EFFECT && it.hasNext()) {
          result = function.apply(it.next(), it);
        }
        if (result == InstructionEffect.OTHER_EFFECT) {
          // We found an instruction that is causing an unexpected side effect.
          return false;
        } else if (result == InstructionEffect.DESIRED_EFFECT) {
          // The current path is causing the expected effect. No need to go deeper in this path,
          // go to the next block in the work list.
          continue;
        } else if (result == InstructionEffect.CONDITIONAL_EFFECT) {
          assert !currentBlock.getNormalSuccessors().isEmpty();
          Instruction lastInstruction = currentBlock.getInstructions().getLast();
          assert lastInstruction.isIf();
          // The current path is checking if the value of interest is null. Go deeper into the path
          // that corresponds to the null value.
          BasicBlock targetIfReceiverIsNull = lastInstruction.asIf().targetFromCondition(0);
          if (!targetIfReceiverIsNull.isMarked(color)) {
            worklist.add(targetIfReceiverIsNull);
            targetIfReceiverIsNull.mark(color);
          }
        } else {
          assert result == InstructionEffect.NO_EFFECT;
          // The block did not cause any particular effect.
          if (currentBlock.getNormalSuccessors().isEmpty()) {
            // This is the end of the current non-exceptional path and we did not find any expected
            // effect. It means there is at least one path where the expected effect does not
            // happen.
            Instruction lastInstruction = currentBlock.getInstructions().getLast();
            assert lastInstruction.isReturn() || lastInstruction.isThrow();
            return false;
          } else {
            // Look into successors
            for (BasicBlock successor : currentBlock.getSuccessors()) {
              if (!successor.isMarked(color)) {
                worklist.add(successor);
                successor.mark(color);
              }
            }
          }
        }
      }
      // If we reach this point, we checked that the expected effect happens in every possible path.
      return true;
    } finally {
      code.returnMarkingColor(color);
    }
  }

  /**
   * Returns true if the given code unconditionally throws if value is null before any other side
   * effect instruction.
   *
   * <p>Note: we do not track phis so we may return false negative. This is a conservative approach.
   */
  private static boolean checksNullBeforeSideEffect(IRCode code, Value value, AppView<?> appView) {
    return alwaysTriggerExpectedEffectBeforeAnythingElse(
        code,
        (instr, it) -> {
          BasicBlock currentBlock = instr.getBlock();
          // If the code explicitly checks the nullability of the value, we should visit the next
          // block that corresponds to the null value where NPE semantic could be preserved.
          if (!currentBlock.hasCatchHandlers() && isNullCheck(instr, value)) {
            return InstructionEffect.CONDITIONAL_EFFECT;
          }
          if (isKotlinNullCheck(instr, value, appView)) {
            DexMethod invokedMethod = instr.asInvokeStatic().getInvokedMethod();
            // Kotlin specific way of throwing NPE: throwParameterIsNullException.
            // Similarly, combined with the above CONDITIONAL_EFFECT, the code checks on NPE on
            // the value.
            if (invokedMethod.name
                == appView.dexItemFactory().kotlin.intrinsics.throwParameterIsNullException.name) {
              // We found a NPE (or similar exception) throwing code.
              // Combined with the above CONDITIONAL_EFFECT, the code checks NPE on the value.
              for (BasicBlock predecessor : currentBlock.getPredecessors()) {
                if (isNullCheck(predecessor.exit(), value)) {
                  return InstructionEffect.DESIRED_EFFECT;
                }
              }
              // Hitting here means that this call might be used for other parameters. If we don't
              // bail out, it will be regarded as side effects for the current value.
              return InstructionEffect.NO_EFFECT;
            } else {
              // Kotlin specific way of checking parameter nullness: checkParameterIsNotNull.
              assert invokedMethod.name
                  == appView.dexItemFactory().kotlin.intrinsics.checkParameterIsNotNull.name;
              return InstructionEffect.DESIRED_EFFECT;
            }
          }
          if (isInstantiationOfNullPointerException(instr, it, appView.dexItemFactory())) {
            it.next(); // Skip call to NullPointerException.<init>.
            return InstructionEffect.NO_EFFECT;
          } else if (instr.throwsNpeIfValueIsNull(value, appView.dexItemFactory())) {
            // In order to preserve NPE semantic, the exception must not be caught by any handler.
            // Therefore, we must ignore this instruction if it is covered by a catch handler.
            // Note: this is a conservative approach where we consider that any catch handler could
            // catch the exception, even if it cannot catch a NullPointerException.
            if (!currentBlock.hasCatchHandlers()) {
              // We found a NPE check on the value.
              return InstructionEffect.DESIRED_EFFECT;
            }
          } else if (instr.instructionMayHaveSideEffects(appView, code.method.method.holder)) {
            // If the current instruction is const-string, this could load the parameter name.
            // Just make sure it is indeed not throwing.
            if (instr.isConstString() && !instr.instructionInstanceCanThrow()) {
              return InstructionEffect.NO_EFFECT;
            }
            // We found a side effect before a NPE check.
            return InstructionEffect.OTHER_EFFECT;
          }
          return InstructionEffect.NO_EFFECT;
        });
  }

  /**
   * An enum used to classify instructions according to a particular effect that they produce.
   *
   * The "effect" of an instruction can be seen as a program state change (or semantic change) at
   * runtime execution. For example, an instruction could cause the initialization of a class,
   * change the value of a field, ... while other instructions do not.
   *
   * This classification also depends on the type of analysis that is using it. For instance, an
   * analysis can look for instructions that cause class initialization while another look for
   * instructions that check nullness of a particular object.
   *
   * On the other hand, some instructions may provide a non desired effect which is a signal for
   * the analysis to stop.
   */
  private enum InstructionEffect {
    DESIRED_EFFECT,
    CONDITIONAL_EFFECT,
    OTHER_EFFECT,
    NO_EFFECT
  }

  // Note that this method may have false positives, since the application could in principle
  // declare a method called checkParameterIsNotNull(parameter, message) or
  // throwParameterIsNullException(parameterName) in a package that starts with "kotlin".
  private static boolean isKotlinNullCheck(Instruction instr, Value value, AppView<?> appView) {
    if (appView.options().kotlinOptimizationOptions().disableKotlinSpecificOptimizations) {
      return false;
    }
    if (!instr.isInvokeStatic()) {
      return false;
    }
    // We need to strip the holder, since Kotlin adds different versions of null-check machinery,
    // e.g., kotlin.collections.ArraysKt___ArraysKt... or kotlin.jvm.internal.ArrayIteratorKt...
    MethodSignatureEquivalence wrapper = MethodSignatureEquivalence.get();
    Wrapper<DexMethod> checkParameterIsNotNull =
        wrapper.wrap(appView.dexItemFactory().kotlin.intrinsics.checkParameterIsNotNull);
    Wrapper<DexMethod> throwParamIsNullException =
        wrapper.wrap(appView.dexItemFactory().kotlin.intrinsics.throwParameterIsNullException);
    DexMethod invokedMethod =
        appView.graphLense().getOriginalMethodSignature(instr.asInvokeStatic().getInvokedMethod());
    Wrapper<DexMethod> methodWrap = wrapper.wrap(invokedMethod);
    if (methodWrap.equals(throwParamIsNullException)
        || (methodWrap.equals(checkParameterIsNotNull) && instr.inValues().get(0).equals(value))) {
      if (invokedMethod.holder.getPackageDescriptor().startsWith(Kotlin.NAME)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isNullCheck(Instruction instr, Value value) {
    return instr.isIf() && instr.asIf().isZeroTest()
        && instr.inValues().get(0).equals(value)
        && (instr.asIf().getType() == If.Type.EQ || instr.asIf().getType() == If.Type.NE);
  }

  /**
   * Returns true if the given instruction is {@code v <- new-instance NullPointerException}, and
   * the next instruction is {@code invoke-direct v, NullPointerException.<init>()}.
   */
  private static boolean isInstantiationOfNullPointerException(
      Instruction instruction, InstructionIterator it, DexItemFactory dexItemFactory) {
    if (!instruction.isNewInstance()
        || instruction.asNewInstance().clazz != dexItemFactory.npeType) {
      return false;
    }
    Instruction next = it.peekNext();
    if (next == null
        || !next.isInvokeDirect()
        || next.asInvokeDirect().getInvokedMethod() != dexItemFactory.npeMethods.init) {
      return false;
    }
    return true;
  }

  private void computeDynamicReturnType(
      DynamicTypeOptimization dynamicTypeOptimization,
      OptimizationFeedback feedback,
      DexEncodedMethod method,
      IRCode code) {
    if (dynamicTypeOptimization != null) {
      DexType staticReturnTypeRaw = method.method.proto.returnType;
      if (!staticReturnTypeRaw.isReferenceType()) {
        return;
      }
      TypeLatticeElement dynamicReturnType =
          dynamicTypeOptimization.computeDynamicReturnType(method, code);
      if (dynamicReturnType != null) {
        TypeLatticeElement staticReturnType =
            TypeLatticeElement.fromDexType(staticReturnTypeRaw, Nullability.maybeNull(), appView);
        // If the dynamic return type is not more precise than the static return type there is no
        // need to record it.
        if (dynamicReturnType.strictlyLessThan(staticReturnType, appView)) {
          feedback.methodReturnsObjectWithUpperBoundType(method, appView, dynamicReturnType);
        }
      }
      ClassTypeLatticeElement exactReturnType =
          dynamicTypeOptimization.computeDynamicLowerBoundType(method, code);
      if (exactReturnType != null) {
        feedback.methodReturnsObjectWithLowerBoundType(method, exactReturnType);
      }
    }
  }

  private void computeInitializedClassesOnNormalExit(
      OptimizationFeedback feedback, DexEncodedMethod method, IRCode code) {
    if (options.enableInitializedClassesAnalysis && appView.appInfo().hasLiveness()) {
      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      Set<DexType> initializedClasses =
          InitializedClassesOnNormalExitAnalysis.computeInitializedClassesOnNormalExit(
              appViewWithLiveness, code);
      if (initializedClasses != null && !initializedClasses.isEmpty()) {
        feedback.methodInitializesClassesOnNormalExit(method, initializedClasses);
      }
    }
  }

  private void computeMayHaveSideEffects(
      OptimizationFeedback feedback, DexEncodedMethod method, IRCode code) {
    // If the method is native, we don't know what could happen.
    assert !method.accessFlags.isNative();
    if (!options.enableSideEffectAnalysis) {
      return;
    }
    if (appView.appInfo().mayHaveSideEffects.containsKey(method.method)) {
      return;
    }
    DexType context = method.method.holder;
    if (method.isClassInitializer()) {
      // For class initializers, we also wish to compute if the class initializer has observable
      // side effects.
      ClassInitializerSideEffect classInitializerSideEffect =
          ClassInitializerSideEffectAnalysis.classInitializerCanBePostponed(appView, code);
      if (classInitializerSideEffect.isNone()) {
        feedback.methodMayNotHaveSideEffects(method);
        feedback.classInitializerMayBePostponed(method);
      } else if (classInitializerSideEffect.canBePostponed()) {
        feedback.classInitializerMayBePostponed(method);
      } else {
        assert !context.isD8R8SynthesizedLambdaClassType()
                || options.debug
                || appView.appInfo().hasPinnedInstanceInitializer(context)
            : "Unexpected observable side effects from lambda `" + context.toSourceString() + "`";
      }
      return;
    }
    boolean mayHaveSideEffects;
    if (method.accessFlags.isSynchronized()) {
      // If the method is synchronized then it acquires a lock.
      mayHaveSideEffects = true;
    } else if (method.isInstanceInitializer() && hasNonTrivialFinalizeMethod(context)) {
      // If a class T overrides java.lang.Object.finalize(), then treat the constructor as having
      // side effects. This ensures that we won't remove instructions on the form `new-instance
      // {v0}, T`.
      mayHaveSideEffects = true;
    } else {
      // Otherwise, check if there is an instruction that has side effects.
      mayHaveSideEffects =
          Streams.stream(code.instructions())
              .anyMatch(instruction -> instruction.instructionMayHaveSideEffects(appView, context));
    }
    if (!mayHaveSideEffects) {
      feedback.methodMayNotHaveSideEffects(method);
    }
  }

  // Returns true if `method` is an initializer and the enclosing class overrides the method
  // `void java.lang.Object.finalize()`.
  private boolean hasNonTrivialFinalizeMethod(DexType type) {
    DexClass clazz = appView.definitionFor(type);
    if (clazz != null) {
      if (clazz.isProgramClass() && !clazz.isInterface()) {
        DexItemFactory dexItemFactory = appView.dexItemFactory();
        ResolutionResult resolutionResult =
            appView
                .appInfo()
                .resolveMethodOnClass(clazz, appView.dexItemFactory().objectMethods.finalize);

        DexEncodedMethod target = resolutionResult.getSingleTarget();
        if (target != null
            && target.method != dexItemFactory.enumMethods.finalize
            && target.method != dexItemFactory.objectMethods.finalize) {
            return true;
        }
        return false;
      } else {
        // Conservatively report that the library class could implement finalize().
        return true;
      }
    }
    return false;
  }

  private void computeReturnValueOnlyDependsOnArguments(
      OptimizationFeedback feedback, DexEncodedMethod method, IRCode code) {
    if (!options.enableDeterminismAnalysis) {
      return;
    }
    boolean returnValueOnlyDependsOnArguments =
        DeterminismAnalysis.returnValueOnlyDependsOnArguments(appView.withLiveness(), code);
    if (returnValueOnlyDependsOnArguments) {
      feedback.methodReturnValueOnlyDependsOnArguments(method);
    }
  }

  // Track usage of parameters and compute their nullability and possibility of NPE.
  private void computeNonNullParamOrThrow(
      OptimizationFeedback feedback, DexEncodedMethod method, IRCode code) {
    if (method.getOptimizationInfo().getNonNullParamOrThrow() != null) {
      return;
    }
    List<Value> arguments = code.collectArguments();
    BitSet paramsCheckedForNull = new BitSet();
    for (int index = 0; index < arguments.size(); index++) {
      Value argument = arguments.get(index);
      // This handles cases where the parameter is checked via Kotlin Intrinsics:
      //
      //   kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(param, message)
      //
      // or its inlined version:
      //
      //   if (param != null) return;
      //   invoke-static throwParameterIsNullException(msg)
      //
      // or some other variants, e.g., throw null or NPE after the direct null check.
      if (argument.isUsed() && checksNullBeforeSideEffect(code, argument, appView)) {
        paramsCheckedForNull.set(index);
      }
    }
    if (paramsCheckedForNull.length() > 0) {
      feedback.setNonNullParamOrThrow(method, paramsCheckedForNull);
    }
  }

  private void computeNonNullParamOnNormalExits(OptimizationFeedback feedback, IRCode code) {
    Set<BasicBlock> normalExits = Sets.newIdentityHashSet();
    normalExits.addAll(code.computeNormalExitBlocks());
    DominatorTree dominatorTree = new DominatorTree(code, MAY_HAVE_UNREACHABLE_BLOCKS);
    List<Value> arguments = code.collectArguments();
    BitSet facts = new BitSet();
    Set<BasicBlock> nullCheckedBlocks = Sets.newIdentityHashSet();
    for (int index = 0; index < arguments.size(); index++) {
      Value argument = arguments.get(index);
      // Consider reference-type parameter only.
      if (!argument.getTypeLattice().isReference()) {
        continue;
      }
      // The receiver is always non-null on normal exits.
      if (argument.isThis()) {
        facts.set(index);
        continue;
      }
      // Collect basic blocks that check nullability of the parameter.
      nullCheckedBlocks.clear();
      for (Instruction user : argument.uniqueUsers()) {
        if (user.isAssumeNonNull()) {
          nullCheckedBlocks.add(user.asAssumeNonNull().getBlock());
        }
        if (user.isIf()
            && user.asIf().isZeroTest()
            && (user.asIf().getType() == If.Type.EQ || user.asIf().getType() == If.Type.NE)) {
          nullCheckedBlocks.add(user.asIf().targetFromNonNullObject());
        }
      }
      if (!nullCheckedBlocks.isEmpty()) {
        boolean allExitsCovered = true;
        for (BasicBlock normalExit : normalExits) {
          if (!isNormalExitDominated(normalExit, code, dominatorTree, nullCheckedBlocks)) {
            allExitsCovered = false;
            break;
          }
        }
        if (allExitsCovered) {
          facts.set(index);
        }
      }
    }
    if (facts.length() > 0) {
      feedback.setNonNullParamOnNormalExits(code.method, facts);
    }
  }

  private boolean isNormalExitDominated(
      BasicBlock normalExit,
      IRCode code,
      DominatorTree dominatorTree,
      Set<BasicBlock> nullCheckedBlocks) {
    // Each normal exit should be...
    for (BasicBlock nullCheckedBlock : nullCheckedBlocks) {
      // A) ...directly dominated by any null-checked block.
      if (dominatorTree.dominatedBy(normalExit, nullCheckedBlock)) {
        return true;
      }
    }
    // B) ...or indirectly dominated by null-checked blocks.
    // Although the normal exit is not dominated by any of null-checked blocks (because of other
    // paths to the exit), it could be still the case that all possible paths to that exit should
    // pass some of null-checked blocks.
    Set<BasicBlock> visited = Sets.newIdentityHashSet();
    // Initial fan-out of predecessors.
    Deque<BasicBlock> uncoveredPaths = new ArrayDeque<>(normalExit.getPredecessors());
    while (!uncoveredPaths.isEmpty()) {
      BasicBlock uncoveredPath = uncoveredPaths.poll();
      // Stop traversing upwards if we hit the entry block: if the entry block has an non-null,
      // this case should be handled already by A) because the entry block surely dominates all
      // normal exits.
      if (uncoveredPath == code.entryBlock()) {
        return false;
      }
      // Make sure we're not visiting the same block over and over again.
      if (!visited.add(uncoveredPath)) {
        // But, if that block is the last one in the queue, the normal exit is not fully covered.
        if (uncoveredPaths.isEmpty()) {
          return false;
        } else {
          continue;
        }
      }
      boolean pathCovered = false;
      for (BasicBlock nullCheckedBlock : nullCheckedBlocks) {
        if (dominatorTree.dominatedBy(uncoveredPath, nullCheckedBlock)) {
          pathCovered = true;
          break;
        }
      }
      if (!pathCovered) {
        // Fan out predecessors one more level.
        // Note that remaining, unmatched null-checked blocks should cover newly added paths.
        uncoveredPaths.addAll(uncoveredPath.getPredecessors());
      }
    }
    // Reaching here means that every path to the given normal exit is covered by the set of
    // null-checked blocks.
    assert uncoveredPaths.isEmpty();
    return true;
  }
}
