// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.ir.code.DominatorTree.Assumption.MAY_HAVE_UNREACHABLE_BLOCKS;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.Assume.NonNullAssumption;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.info.FieldOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.MethodOptimizationInfo;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NonNullTracker implements Assumer {

  private final AppView<?> appView;
  private final DexItemFactory dexItemFactory;
  private final Consumer<BasicBlock> splitBlockConsumer;

  public NonNullTracker(AppView<?> appView) {
    this(appView, null);
  }

  public NonNullTracker(AppView<?> appView, Consumer<BasicBlock> splitBlockConsumer) {
    this.appView = appView;
    this.dexItemFactory = appView.dexItemFactory();
    this.splitBlockConsumer = splitBlockConsumer;
  }

  @Override
  public void insertAssumeInstructionsInBlocks(
      IRCode code, ListIterator<BasicBlock> blockIterator, Predicate<BasicBlock> blockTester) {
    Set<Value> affectedValues = Sets.newIdentityHashSet();
    Set<Value> knownToBeNonNullValues = Sets.newIdentityHashSet();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (!blockTester.test(block)) {
        continue;
      }
      // Add non-null after
      // 1) instructions that implicitly indicate receiver/array is not null.
      // 2) invocations that call non-overridable library methods that are known to return non null.
      // 3) invocations that are guaranteed to return a non-null value.
      // 4) parameters that are not null after the invocation.
      // 5) field-get instructions that are guaranteed to read a non-null value.
      InstructionListIterator iterator = block.listIterator(code);
      while (iterator.hasNext()) {
        Instruction current = iterator.next();
        Value outValue = current.outValue();

        // Case (1), instructions that implicitly indicate receiver/array is not null.
        if (current.throwsOnNullInput()) {
          Value couldBeNonNull = current.getNonNullInput();
          if (isNullableReferenceTypeWithUsers(couldBeNonNull)) {
            knownToBeNonNullValues.add(couldBeNonNull);
          }
        }

        if (current.isInvokeMethod()) {
          InvokeMethod invoke = current.asInvokeMethod();
          DexMethod invokedMethod = invoke.getInvokedMethod();

          // Case (2), invocations that call non-overridable library methods that are known to
          // return non null.
          if (dexItemFactory.libraryMethodsReturningNonNull.contains(invokedMethod)) {
            if (current.hasOutValue() && isNullableReferenceTypeWithUsers(outValue)) {
              knownToBeNonNullValues.add(outValue);
            }
          }

          DexEncodedMethod singleTarget =
              invoke.lookupSingleTarget(appView, code.method.method.holder);
          if (singleTarget != null) {
            MethodOptimizationInfo optimizationInfo = singleTarget.getOptimizationInfo();

            // Case (3), invocations that are guaranteed to return a non-null value.
            if (optimizationInfo.neverReturnsNull()) {
              if (invoke.hasOutValue() && isNullableReferenceTypeWithUsers(outValue)) {
                knownToBeNonNullValues.add(outValue);
              }
            }

            // Case (4), parameters that are not null after the invocation.
            BitSet nonNullParamOnNormalExits = optimizationInfo.getNonNullParamOnNormalExits();
            if (nonNullParamOnNormalExits != null) {
              for (int i = 0; i < current.inValues().size(); i++) {
                if (nonNullParamOnNormalExits.get(i)) {
                  Value knownToBeNonNullValue = current.inValues().get(i);
                  if (isNullableReferenceTypeWithUsers(knownToBeNonNullValue)) {
                    knownToBeNonNullValues.add(knownToBeNonNullValue);
                  }
                }
              }
            }
          }
        } else if (current.isFieldGet()) {
          // Case (5), field-get instructions that are guaranteed to read a non-null value.
          FieldInstruction fieldInstruction = current.asFieldInstruction();
          DexField field = fieldInstruction.getField();
          if (field.type.isClassType() && isNullableReferenceTypeWithUsers(outValue)) {
            DexEncodedField encodedField = appView.appInfo().resolveField(field);
            if (encodedField != null) {
              FieldOptimizationInfo optimizationInfo = encodedField.getOptimizationInfo();
              if (optimizationInfo.getDynamicUpperBoundType() != null
                  && optimizationInfo.getDynamicUpperBoundType().isDefinitelyNotNull()) {
                knownToBeNonNullValues.add(outValue);
              }
            }
          }
        }

        // This is to ensure that we do not add redundant non-null instructions.
        // Otherwise, we will have something like:
        //   y <- assume-not-null(x)
        //   ...
        //   z <- assume-not-null(y)
        assert knownToBeNonNullValues.stream()
            .allMatch(NonNullTracker::isNullableReferenceTypeWithUsers);

        if (!knownToBeNonNullValues.isEmpty()) {
          addNonNullForValues(
              code,
              blockIterator,
              block,
              iterator,
              current,
              knownToBeNonNullValues,
              affectedValues);
          knownToBeNonNullValues.clear();
        }
      }

      // Add non-null on top of the successor block if the current block ends with a null check.
      if (block.exit().isIf() && block.exit().asIf().isZeroTest()) {
        // if v EQ blockX
        // ... (fallthrough)
        // blockX: ...
        //
        //   ~>
        //
        // if v EQ blockX
        // non_null_value <- non-null(v)
        // ...
        // blockX: ...
        //
        // or
        //
        // if v NE blockY
        // ...
        // blockY: ...
        //
        //   ~>
        //
        // blockY: non_null_value <- non-null(v)
        // ...
        If theIf = block.exit().asIf();
        Value knownToBeNonNullValue = theIf.inValues().get(0);
        // Avoid adding redundant non-null instruction.
        if (isNullableReferenceTypeWithUsers(knownToBeNonNullValue)) {
          BasicBlock target = theIf.targetFromNonNullObject();
          // Ignore uncommon empty blocks.
          if (!target.isEmpty()) {
            DominatorTree dominatorTree = new DominatorTree(code, MAY_HAVE_UNREACHABLE_BLOCKS);
            // Make sure there are no paths to the target block without passing the current block.
            if (dominatorTree.dominatedBy(target, block)) {
              // Collect users of the original value that are dominated by the target block.
              Set<Instruction> dominatedUsers = Sets.newIdentityHashSet();
              Map<Phi, IntList> dominatedPhiUsersWithPositions = new IdentityHashMap<>();
              Set<BasicBlock> dominatedBlocks =
                  Sets.newHashSet(dominatorTree.dominatedBlocks(target));
              for (Instruction user : knownToBeNonNullValue.uniqueUsers()) {
                if (dominatedBlocks.contains(user.getBlock())) {
                  dominatedUsers.add(user);
                }
              }
              for (Phi user : knownToBeNonNullValue.uniquePhiUsers()) {
                IntList dominatedPredecessorIndexes = findDominatedPredecessorIndexesInPhi(
                    user, knownToBeNonNullValue, dominatedBlocks);
                if (!dominatedPredecessorIndexes.isEmpty()) {
                  dominatedPhiUsersWithPositions.put(user, dominatedPredecessorIndexes);
                }
              }
              // Avoid adding a non-null for the value without meaningful users.
              if (knownToBeNonNullValue.isArgument()
                  || !dominatedUsers.isEmpty()
                  || !dominatedPhiUsersWithPositions.isEmpty()) {
                TypeLatticeElement typeLattice = knownToBeNonNullValue.getTypeLattice();
                Value nonNullValue =
                    code.createValue(
                        typeLattice.asReferenceTypeLatticeElement().asMeetWithNotNull(),
                        knownToBeNonNullValue.getLocalInfo());
                affectedValues.addAll(knownToBeNonNullValue.affectedValues());
                Assume<NonNullAssumption> nonNull =
                    Assume.createAssumeNonNullInstruction(
                        nonNullValue, knownToBeNonNullValue, theIf, appView);
                InstructionListIterator targetIterator = target.listIterator(code);
                nonNull.setPosition(targetIterator.next().getPosition());
                targetIterator.previous();
                targetIterator.add(nonNull);
                knownToBeNonNullValue.replaceSelectiveUsers(
                    nonNullValue, dominatedUsers, dominatedPhiUsersWithPositions);
              }
            }
          }
        }
      }
    }
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
  }

  private void addNonNullForValues(
      IRCode code,
      ListIterator<BasicBlock> blockIterator,
      BasicBlock block,
      InstructionListIterator iterator,
      Instruction current,
      Set<Value> knownToBeNonNullValues,
      Set<Value> affectedValues) {
    // First, if the current block has catch handler, split into two blocks, e.g.,
    //
    // ...x
    // invoke(rcv, ...)
    // ...y
    //
    //   ~>
    //
    // ...x
    // invoke(rcv, ...)
    // goto A
    //
    // A: ...y // blockWithNonNullInstruction
    boolean split = block.hasCatchHandlers();
    BasicBlock blockWithNonNullInstruction;
    if (split) {
      blockWithNonNullInstruction = iterator.split(code, blockIterator);
      if (splitBlockConsumer != null) {
        splitBlockConsumer.accept(blockWithNonNullInstruction);
      }
    } else {
      blockWithNonNullInstruction = block;
    }

    DominatorTree dominatorTree = new DominatorTree(code, MAY_HAVE_UNREACHABLE_BLOCKS);
    for (Value knownToBeNonNullValue : knownToBeNonNullValues) {
      // Find all users of the original value that are dominated by either the current block
      // or the new split-off block. Since NPE can be explicitly caught, nullness should be
      // propagated through dominance.
      Set<Instruction> users = knownToBeNonNullValue.uniqueUsers();
      Set<Instruction> dominatedUsers = Sets.newIdentityHashSet();
      Map<Phi, IntList> dominatedPhiUsersWithPositions = new IdentityHashMap<>();
      Set<BasicBlock> dominatedBlocks = Sets.newIdentityHashSet();
      for (BasicBlock dominatee : dominatorTree.dominatedBlocks(blockWithNonNullInstruction)) {
        dominatedBlocks.add(dominatee);
        InstructionIterator dominateeIterator = dominatee.iterator();
        if (dominatee == blockWithNonNullInstruction && !split) {
          // In the block where the non null instruction will be inserted, skip instructions up to
          // and including the insertion point.
          dominateeIterator.nextUntil(instruction -> instruction == current);
        }
        while (dominateeIterator.hasNext()) {
          Instruction potentialUser = dominateeIterator.next();
          if (users.contains(potentialUser)) {
            dominatedUsers.add(potentialUser);
          }
        }
      }
      for (Phi user : knownToBeNonNullValue.uniquePhiUsers()) {
        IntList dominatedPredecessorIndexes =
            findDominatedPredecessorIndexesInPhi(user, knownToBeNonNullValue, dominatedBlocks);
        if (!dominatedPredecessorIndexes.isEmpty()) {
          dominatedPhiUsersWithPositions.put(user, dominatedPredecessorIndexes);
        }
      }

      // Only insert non-null instruction if it is ever used.
      // Exception: if it is an argument, non-null IR can be used to compute non-null parameter.
      if (knownToBeNonNullValue.isArgument()
          || !dominatedUsers.isEmpty()
          || !dominatedPhiUsersWithPositions.isEmpty()) {
        // Add non-null fake IR, e.g.,
        // ...x
        // invoke(rcv, ...)
        // goto A
        // ...
        // A: non_null_rcv <- non-null(rcv)
        // ...y
        TypeLatticeElement typeLattice = knownToBeNonNullValue.getTypeLattice();
        assert typeLattice.isReference();
        Value nonNullValue =
            code.createValue(
                typeLattice.asReferenceTypeLatticeElement().asMeetWithNotNull(),
                knownToBeNonNullValue.getLocalInfo());
        affectedValues.addAll(knownToBeNonNullValue.affectedValues());
        Assume<NonNullAssumption> nonNull =
            Assume.createAssumeNonNullInstruction(
                nonNullValue, knownToBeNonNullValue, current, appView);
        nonNull.setPosition(current.getPosition());
        if (blockWithNonNullInstruction != block) {
          // If we split, add non-null IR on top of the new split block.
          blockWithNonNullInstruction.listIterator(code).add(nonNull);
        } else {
          // Otherwise, just add it to the current block at the position of the iterator.
          iterator.add(nonNull);
        }

        // Replace all users of the original value that are dominated by either the current block
        // or the new split-off block.
        knownToBeNonNullValue.replaceSelectiveUsers(
            nonNullValue, dominatedUsers, dominatedPhiUsersWithPositions);
      }
    }
  }

  private IntList findDominatedPredecessorIndexesInPhi(
      Phi user, Value knownToBeNonNullValue, Set<BasicBlock> dominatedBlocks) {
    assert user.getOperands().contains(knownToBeNonNullValue);
    List<Value> operands = user.getOperands();
    List<BasicBlock> predecessors = user.getBlock().getPredecessors();
    assert operands.size() == predecessors.size();

    IntList predecessorIndexes = new IntArrayList();
    int index = 0;
    Iterator<Value> operandIterator = operands.iterator();
    Iterator<BasicBlock> predecessorIterator = predecessors.iterator();
    while (operandIterator.hasNext() && predecessorIterator.hasNext()) {
      Value operand = operandIterator.next();
      BasicBlock predecessor = predecessorIterator.next();
      // When this phi is chosen to be known-to-be-non-null value,
      // check if the corresponding predecessor is dominated by the block where non-null is added.
      if (operand == knownToBeNonNullValue && dominatedBlocks.contains(predecessor)) {
        predecessorIndexes.add(index);
      }

      index++;
    }
    return predecessorIndexes;
  }

  private static boolean isNullableReferenceTypeWithUsers(Value value) {
    TypeLatticeElement type = value.getTypeLattice();
    return type.isReference()
        && type.asReferenceTypeLatticeElement().isNullable()
        && value.numberOfAllUsers() > 0;
  }
}
