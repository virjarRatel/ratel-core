// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.classmerging.VerticallyMergedClasses;
import com.android.tools.r8.ir.analysis.TypeChecker;
import com.android.tools.r8.ir.analysis.ValueMayDependOnEnvironmentAnalysis;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Phi.RegisterReadType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.DequeUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.IteratorUtils;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IRCode {

  private static final int MAX_MARKING_COLOR = 0x40000000;

  public static class LiveAtEntrySets {
    // Set of live SSA values (regardless of whether they denote a local variable).
    public final Set<Value> liveValues;

    // Subset of live local-variable values.
    public final Set<Value> liveLocalValues;

    public final Deque<Value> liveStackValues;

    public LiveAtEntrySets(
        Set<Value> liveValues, Set<Value> liveLocalValues, Deque<Value> liveStackValues) {
      assert liveValues.containsAll(liveLocalValues);
      this.liveValues = liveValues;
      this.liveLocalValues = liveLocalValues;
      this.liveStackValues = liveStackValues;
    }

    @Override
    public int hashCode() {
      throw new Unreachable();
    }

    @Override
    public boolean equals(Object o) {
      LiveAtEntrySets other = (LiveAtEntrySets) o;
      return liveValues.equals(other.liveValues) && liveLocalValues.equals(other.liveLocalValues);
    }

    public boolean isEmpty() {
      return liveValues.isEmpty() && liveLocalValues.isEmpty();
    }
  }

  // Stack marker to denote when all successors of a block have been processed when topologically
  // sorting.
  private static class BlockMarker {
    final BasicBlock block;
    BlockMarker(BasicBlock block) {
      this.block = block;
    }
  }

  // When numbering instructions we number instructions only with even numbers. This allows us to
  // use odd instruction numbers for the insertion of moves during spilling.
  public static final int INSTRUCTION_NUMBER_DELTA = 2;

  public final DexEncodedMethod method;

  public LinkedList<BasicBlock> blocks;
  public final ValueNumberGenerator valueNumberGenerator;
  private int usedMarkingColors = 0;

  private boolean numbered = false;
  private int nextInstructionNumber = 0;

  // Initial value indicating if the code does have actual positions on all throwing instructions.
  // If this is the case, which holds for javac code, then we want to ensure that it remains so.
  private boolean allThrowingInstructionsHavePositions;

  private final IRMetadata metadata;
  private final InternalOptions options;

  public final Origin origin;

  public IRCode(
      InternalOptions options,
      DexEncodedMethod method,
      LinkedList<BasicBlock> blocks,
      ValueNumberGenerator valueNumberGenerator,
      IRMetadata metadata,
      Origin origin) {
    assert metadata != null;
    assert options != null;
    this.options = options;
    this.method = method;
    this.blocks = blocks;
    this.valueNumberGenerator = valueNumberGenerator;
    this.metadata = metadata;
    this.origin = origin;
    // TODO(zerny): Remove or update this property now that all instructions have positions.
    allThrowingInstructionsHavePositions = computeAllThrowingInstructionsHavePositions();
  }

  public IRMetadata metadata() {
    return metadata;
  }

  public BasicBlock entryBlock() {
    return blocks.getFirst();
  }

  /**
   * Compute the set of live values at the entry to each block using a backwards data-flow analysis.
   */
  public Map<BasicBlock, LiveAtEntrySets> computeLiveAtEntrySets() {
    Map<BasicBlock, LiveAtEntrySets> liveAtEntrySets = new IdentityHashMap<>();
    Queue<BasicBlock> worklist = new ArrayDeque<>();
    // Since this is a backwards data-flow analysis we process the blocks in reverse
    // topological order to reduce the number of iterations.
    ImmutableList<BasicBlock> sorted = topologicallySortedBlocks();
    worklist.addAll(sorted.reverse());
    while (!worklist.isEmpty()) {
      BasicBlock block = worklist.poll();
      // Note that the iteration order of live values matters when inserting spill/restore moves.
      Set<Value> live = new LinkedHashSet<>();
      Set<Value> liveLocals = Sets.newIdentityHashSet();
      Deque<Value> liveStack = new ArrayDeque<>();
      Set<BasicBlock> exceptionalSuccessors = block.getCatchHandlers().getUniqueTargets();
      for (BasicBlock succ : block.getSuccessors()) {
        LiveAtEntrySets liveAtSucc = liveAtEntrySets.get(succ);
        if (liveAtSucc != null) {
          live.addAll(liveAtSucc.liveValues);
          liveLocals.addAll(liveAtSucc.liveLocalValues);
          // The stack is only allowed to be non-empty in the case of linear-flow (so-far).
          // If succ is an exceptional successor the successor stack should be empty
          // otherwise only one successor must have a non-empty stack.
          if (exceptionalSuccessors.contains(succ)) {
            assert liveAtSucc.liveStackValues.size() == 0;
          } else {
            assert liveStack.isEmpty();
            liveStack = new ArrayDeque<>(liveAtSucc.liveStackValues);
          }
        }
        int predIndex = succ.getPredecessors().indexOf(block);
        for (Phi phi : succ.getPhis()) {
          Value operand = phi.getOperand(predIndex);
          if (operand.isValueOnStack()) {
            liveStack.addLast(operand);
          } else {
            live.add(operand);
            if (phi.hasLocalInfo()) {
              // If the phi has local information that implies that the local *must* be live at
              // entry to the block (ie, phis can't end a local explicitly only instructions can).
              // The graph also requires that any local live at entry to a block is at the latest
              // started in the split edge prior to that block (ie, phis cannot start a local
              // range).
              // Therefore, if the phi has local information, that local is live and the operand
              // must be live at block exit.
              assert phi.getLocalInfo() == operand.getLocalInfo();
              liveLocals.add(operand);
            }
          }
        }
      }
      // Sanity-check, making sure that if the stack is non-empty only in the case of a single
      // normal successor.
      assert liveStack.isEmpty()
          || block.getSuccessors().size() - exceptionalSuccessors.size() == 1;
      InstructionIterator iterator = block.listIterator(this, block.getInstructions().size());
      while (iterator.hasPrevious()) {
        Instruction instruction = iterator.previous();
        Value outValue = instruction.outValue();
        if (outValue != null) {
          if (outValue instanceof StackValue) {
            Value pop = liveStack.removeLast();
            assert pop == outValue;
          } else if (outValue instanceof StackValues) {
            StackValue[] values = ((StackValues) outValue).getStackValues();
            for (int i = values.length - 1; i >= 0; i--) {
              Value pop = liveStack.removeLast();
              assert pop == values[i];
            }
          } else {
            live.remove(outValue);
            assert outValue.hasLocalInfo() || !liveLocals.contains(outValue);
            if (outValue.hasLocalInfo()) {
              liveLocals.remove(outValue);
            }
          }
        }
        for (Value use : instruction.inValues()) {
          if (use.needsRegister()) {
            live.add(use);
          } else if (use.isValueOnStack()) {
            liveStack.addLast(use);
          }
        }
        assert instruction.getDebugValues().stream().allMatch(Value::needsRegister);
        assert instruction.getDebugValues().stream().allMatch(Value::hasLocalInfo);
        live.addAll(instruction.getDebugValues());
        liveLocals.addAll(instruction.getDebugValues());
      }
      for (Phi phi : block.getPhis()) {
        if (phi.isValueOnStack()) {
          liveStack.remove(phi);
        } else {
          live.remove(phi);
        }
        assert phi.hasLocalInfo() || !liveLocals.contains(phi);
        if (phi.hasLocalInfo()) {
          liveLocals.remove(phi);
        }
      }
      LiveAtEntrySets liveAtEntry = new LiveAtEntrySets(live, liveLocals, liveStack);
      LiveAtEntrySets previousLiveAtEntry = liveAtEntrySets.put(block, liveAtEntry);
      // If the live-at-entry set changed, add the predecessors to the worklist if they are not
      // already there.
      if (previousLiveAtEntry == null || !previousLiveAtEntry.equals(liveAtEntry)) {
        for (BasicBlock pred : block.getPredecessors()) {
          if (!worklist.contains(pred)) {
            worklist.add(pred);
          }
        }
      }
    }
    assert liveAtEntrySets.get(sorted.get(0)).isEmpty()
        : "Unexpected values live at entry to first block: "
        + liveAtEntrySets.get(sorted.get(0)).liveValues;
    return liveAtEntrySets;
  }

  public boolean controlFlowMayDependOnEnvironment(
      ValueMayDependOnEnvironmentAnalysis environmentAnalysis) {
    for (BasicBlock block : blocks) {
      if (block.hasCatchHandlers()) {
        // Whether an instruction throws may generally depend on the environment.
        return true;
      }
      if (block.exit().isIf()) {
        If ifInstruction = block.exit().asIf();
        if (environmentAnalysis.valueMayDependOnEnvironment(ifInstruction.lhs())) {
          return true;
        }
        if (!ifInstruction.isZeroTest()
            && environmentAnalysis.valueMayDependOnEnvironment(ifInstruction.rhs())) {
          return true;
        }
      } else if (block.exit().isSwitch()) {
        Switch switchInstruction = block.exit().asSwitch();
        if (environmentAnalysis.valueMayDependOnEnvironment(switchInstruction.value())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Prepares every block for getting catch handlers. This involves splitting blocks until each
   * block has only one throwing instruction, and all instructions after the throwing instruction
   * are allowed to follow a throwing instruction.
   *
   * <p>It is also a requirement that the entry block does not have any catch handlers, thus the
   * entry block is split to ensure that it contains no throwing instructions.
   *
   * <p>This method also inserts a split block between each block with more than two predecessors
   * and the predecessors that have a throwing instruction. This is necessary because adding catch
   * handlers to a predecessor would otherwise lead to critical edges.
   */
  public void prepareBlocksForCatchHandlers() {
    BasicBlock entryBlock = entryBlock();
    ListIterator<BasicBlock> blockIterator = listIterator();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      InstructionListIterator instructionIterator = block.listIterator(this);
      boolean hasSeenThrowingInstruction = false;
      while (instructionIterator.hasNext()) {
        Instruction instruction = instructionIterator.next();
        boolean instructionTypeCanThrow = instruction.instructionTypeCanThrow();
        if ((hasSeenThrowingInstruction && !instruction.isAllowedAfterThrowingInstruction())
            || (instructionTypeCanThrow && block == entryBlock)) {
          instructionIterator.previous();
          instructionIterator.split(this, blockIterator);
          blockIterator.previous();
          break;
        }
        if (instructionTypeCanThrow) {
          hasSeenThrowingInstruction = true;
        }
      }
      if (hasSeenThrowingInstruction) {
        List<BasicBlock> successors = block.getSuccessors();
        if (successors.size() == 1 && ListUtils.first(successors).getPredecessors().size() > 1) {
          BasicBlock splitBlock = block.createSplitBlock(getHighestBlockNumber() + 1, true);
          Goto newGoto = new Goto(block);
          newGoto.setPosition(Position.none());
          splitBlock.listIterator(this).add(newGoto);
          blockIterator.add(splitBlock);
        }
      }
    }
    assert blocks.stream().allMatch(block -> block.numberOfThrowingInstructions() <= 1);
  }

  public void splitCriticalEdges() {
    List<BasicBlock> newBlocks = new ArrayList<>();
    int nextBlockNumber = getHighestBlockNumber() + 1;
    for (BasicBlock block : blocks) {
      // We are using a spilling register allocator that might need to insert moves at
      // all critical edges, so we always split them all.
      List<BasicBlock> predecessors = block.getMutablePredecessors();
      if (predecessors.size() <= 1) {
        continue;
      }

      // Exceptional edges are given unique header blocks and can have at most one predecessor.
      assert !block.entry().isMoveException();

      for (int predIndex = 0; predIndex < predecessors.size(); predIndex++) {
        BasicBlock pred = predecessors.get(predIndex);
        if (!pred.hasOneNormalExit()) {
          // Critical edge: split it and inject a new block into which the
          // phi moves can be inserted. The new block is created with the
          // correct predecessor and successor structure. It is inserted
          // at the end of the list of blocks disregarding branching
          // structure.
          BasicBlock newBlock =
              BasicBlock.createGotoBlock(
                  nextBlockNumber++, pred.exit().getPosition(), metadata, block);
          newBlocks.add(newBlock);
          pred.replaceSuccessor(block, newBlock);
          newBlock.getMutablePredecessors().add(pred);
          predecessors.set(predIndex, newBlock);
        }
      }
    }
    blocks.addAll(newBlocks);
  }

  public boolean verifySplitCriticalEdges() {
    for (BasicBlock block : blocks) {
      // If there are multiple incoming edges, check each has a split block.
      List<BasicBlock> predecessors = block.getPredecessors();
      if (predecessors.size() > 1) {
        for (BasicBlock predecessor : predecessors) {
          assert predecessor.hasOneNormalExit();
          assert predecessor.getSuccessors().get(0) == block;
        }
      }
      // If there are outgoing exceptional edges, check that each has a split block.
      if (block.hasCatchHandlers()) {
        for (BasicBlock handler : block.getCatchHandlers().getUniqueTargets()) {
          assert handler.getPredecessors().size() == 1;
          assert handler.getPredecessors().get(0) == block;
        }
      }
    }
    return true;
  }

  /**
   * Trace blocks and attempt to put fallthrough blocks immediately after the block that
   * falls through. When we fail to do that we create a new fallthrough block with an explicit
   * goto to the actual fallthrough block.
   */
  public void traceBlocks() {
    // Get the blocks first, as calling topologicallySortedBlocks also sets marks.
    ImmutableList<BasicBlock> sorted = topologicallySortedBlocks();
    int color = reserveMarkingColor();
    int nextBlockNumber = blocks.size();
    LinkedList<BasicBlock> tracedBlocks = new LinkedList<>();
    for (BasicBlock block : sorted) {
      if (!block.isMarked(color)) {
        block.mark(color);
        tracedBlocks.add(block);
        BasicBlock current = block;
        BasicBlock fallthrough = block.exit().fallthroughBlock();
        while (fallthrough != null && !fallthrough.isMarked(color)) {
          fallthrough.mark(color);
          tracedBlocks.add(fallthrough);
          current = fallthrough;
          fallthrough = fallthrough.exit().fallthroughBlock();
        }
        if (fallthrough != null) {
          BasicBlock newFallthrough =
              BasicBlock.createGotoBlock(
                  nextBlockNumber++, current.exit().getPosition(), metadata, fallthrough);
          current.exit().setFallthroughBlock(newFallthrough);
          newFallthrough.getMutablePredecessors().add(current);
          fallthrough.replacePredecessor(current, newFallthrough);
          newFallthrough.mark(color);
          tracedBlocks.add(newFallthrough);
        }
      }
    }
    blocks = tracedBlocks;
    returnMarkingColor(color);
    assert noColorsInUse();
  }

  private void ensureBlockNumbering() {
    if (!numbered) {
      numbered = true;
      int blockNumber = 0;
      for (BasicBlock block : topologicallySortedBlocks()) {
        block.setNumber(blockNumber++);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("blocks:\n");
    for (BasicBlock block : blocks) {
      builder.append(block.toDetailedString());
      builder.append("\n");
    }
    return builder.toString();
  }

  public void clearMarks(int color) {
    for (BasicBlock block : blocks) {
      block.clearMark(color);
    }
  }

  public void removeMarkedBlocks(int color) {
    ListIterator<BasicBlock> blockIterator = listIterator();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (block.isMarked(color)) {
        blockIterator.remove();
      }
    }
  }

  public boolean verifyNoBlocksMarked(int color) {
    for (BasicBlock block : blocks) {
      assert !block.isMarked(color);
    }
    return true;
  }

  public void removeBlocks(Collection<BasicBlock> blocksToRemove) {
    blocks.removeAll(blocksToRemove);
  }

  /**
   * Compute quasi topologically sorted list of the basic blocks using depth first search.
   *
   * TODO(ager): We probably want to compute strongly connected components and topologically
   * sort strongly connected components instead. However, this is much better than having
   * no sorting.
   */
  public ImmutableList<BasicBlock> topologicallySortedBlocks() {
    ImmutableList<BasicBlock> ordered = depthFirstSorting();
    return options.testing.placeExceptionalBlocksLast
        ? reorderExceptionalBlocksLastForTesting(ordered)
        : ordered;
  }

  private ImmutableList<BasicBlock> depthFirstSorting() {
    ArrayList<BasicBlock> reverseOrdered = new ArrayList<>(blocks.size());
    Set<BasicBlock> visitedBlocks = new HashSet<>(blocks.size());
    Deque<Object> worklist = new ArrayDeque<>(blocks.size());
    worklist.addLast(entryBlock());
    while (!worklist.isEmpty()) {
      Object item = worklist.removeLast();
      if (item instanceof BlockMarker) {
        reverseOrdered.add(((BlockMarker) item).block);
        continue;
      }
      BasicBlock block = (BasicBlock) item;
      if (!visitedBlocks.contains(block)) {
        visitedBlocks.add(block);
        worklist.addLast(new BlockMarker(block));
        for (int i = block.getSuccessors().size() - 1; i >= 0; i--) {
          worklist.addLast(block.getSuccessors().get(i));
        }
      }
    }
    ImmutableList.Builder<BasicBlock> builder = ImmutableList.builder();
    for (int i = reverseOrdered.size() - 1; i >= 0; i--) {
      builder.add(reverseOrdered.get(i));
    }
    return builder.build();
  }

  // Reorder the blocks forcing all exceptional blocks to be at the end.
  private static ImmutableList<BasicBlock> reorderExceptionalBlocksLastForTesting(
      ImmutableList<BasicBlock> blocks) {
    ImmutableList.Builder<BasicBlock> reordered = ImmutableList.builder();
    for (BasicBlock block : blocks) {
      if (!block.entry().isMoveException()) {
        reordered.add(block);
      }
    }
    for (BasicBlock block : blocks) {
      if (block.entry().isMoveException()) {
        reordered.add(block);
      }
    }
    return reordered.build();
  }

  public void print(CfgPrinter printer) {
    ensureBlockNumbering();
    for (BasicBlock block : blocks) {
      block.print(printer);
    }
  }

  public boolean isConsistentSSA() {
    assert isConsistentGraph();
    assert consistentDefUseChains();
    assert validThrowingInstructions();
    assert noCriticalEdges();
    assert verifyNoImpreciseOrBottomTypes();
    assert verifyNoValueWithOnlyAssumeInstructionAsUsers();
    return true;
  }

  public boolean hasNoVerticallyMergedClasses(AppView<? extends AppInfoWithSubtyping> appView) {
    VerticallyMergedClasses verticallyMergedClasses = appView.verticallyMergedClasses();
    if (verticallyMergedClasses == null) {
      return true;
    }
    for (Instruction instruction : instructions()) {
      if (instruction.outValue != null && instruction.outValue.getTypeLattice().isClassType()) {
        ClassTypeLatticeElement classTypeLattice =
            instruction.outValue.getTypeLattice().asClassTypeLatticeElement();
        assert !verticallyMergedClasses.hasBeenMergedIntoSubtype(classTypeLattice.getClassType());
        for (DexType itf : classTypeLattice.getInterfaces()) {
          assert !verticallyMergedClasses.hasBeenMergedIntoSubtype(itf);
        }
      }
    }
    return true;
  }

  public boolean isConsistentGraph() {
    assert noColorsInUse();
    assert consistentBlockNumbering();
    assert consistentPredecessorSuccessors();
    assert consistentCatchHandlers();
    assert consistentBlockInstructions();
    assert consistentMetadata();
    assert !allThrowingInstructionsHavePositions || computeAllThrowingInstructionsHavePositions();
    return true;
  }

  public boolean verifyTypes(AppView<?> appView) {
    // We can only type check the program if we have subtyping information. Therefore, we do not
    // require that the program type checks in D8.
    if (appView.enableWholeProgramOptimizations()) {
      assert validAssumeDynamicTypeInstructions(appView);
      assert new TypeChecker(appView.withLiveness()).check(this);
    }
    assert blocks.stream().allMatch(block -> block.verifyTypes(appView));
    return true;
  }

  private boolean validAssumeDynamicTypeInstructions(AppView<?> appView) {
    for (BasicBlock block : blocks) {
      for (Instruction instruction : block.getInstructions()) {
        if (instruction.isAssumeDynamicType()) {
          assert instruction.asAssumeDynamicType().verifyInstructionIsNeeded(appView);
        }
      }
    }
    return true;
  }

  private boolean noCriticalEdges() {
    for (BasicBlock block : blocks) {
      List<BasicBlock> predecessors = block.getPredecessors();
      if (predecessors.size() <= 1) {
        continue;
      }
      if (block.entry() instanceof MoveException) {
        assert false;
        return false;
      }
      for (int predIndex = 0; predIndex < predecessors.size(); predIndex++) {
        if (!predecessors.get(predIndex).hasOneNormalExit()) {
          assert false;
          return false;
        }
      }
    }
    return true;
  }

  public boolean hasCatchHandlers() {
    for (BasicBlock block : blocks) {
      if (block.hasCatchHandlers()) {
        return true;
      }
    }
    return false;
  }

  private boolean consistentDefUseChains() {
    Set<Value> values = Sets.newIdentityHashSet();

    for (BasicBlock block : blocks) {
      int predecessorCount = block.getPredecessors().size();
      // Check that all phi uses are consistent.
      for (Phi phi : block.getPhis()) {
        assert !phi.isTrivialPhi();
        assert phi.getOperands().size() == predecessorCount;
        values.add(phi);
        for (Value value : phi.getOperands()) {
          values.add(value);
          assert value.uniquePhiUsers().contains(phi);
          assert !phi.hasLocalInfo() || phi.getLocalInfo() == value.getLocalInfo();
        }
      }
      for (Instruction instruction : block.getInstructions()) {
        assert instruction.getBlock() == block;
        Value outValue = instruction.outValue();
        if (outValue != null) {
          values.add(outValue);
          assert outValue.definition == instruction;
        }
        for (Value value : instruction.inValues()) {
          values.add(value);
          assert value.uniqueUsers().contains(instruction);
        }
        for (Value value : instruction.getDebugValues()) {
          values.add(value);
          assert value.debugUsers().contains(instruction);
        }
      }
    }

    for (Value value : values) {
      assert verifyValue(value);
      assert consistentValueUses(value);
    }

    return true;
  }

  private boolean verifyValue(Value value) {
    assert value.isPhi() ? verifyPhi(value.asPhi()) : verifyDefinition(value);
    return true;
  }

  private boolean verifyPhi(Phi phi) {
    assert phi.getBlock().getPhis().contains(phi);
    return true;
  }

  private boolean verifyDefinition(Value value) {
    Value outValue = value.definition.outValue();
    assert outValue == value
        || (value instanceof StackValue
            && Arrays.asList(((StackValues) outValue).getStackValues()).contains(value));
    return true;
  }

  private boolean consistentValueUses(Value value) {
    for (Instruction user : value.uniqueUsers()) {
      assert user.inValues().contains(value);
    }
    for (Phi phiUser : value.uniquePhiUsers()) {
      assert phiUser.getOperands().contains(value);
      assert phiUser.getBlock().getPhis().contains(phiUser);
    }
    if (value.hasLocalInfo()) {
      for (Instruction debugUser : value.debugUsers()) {
        assert debugUser.getDebugValues().contains(value);
      }
    }
    return true;
  }

  private boolean consistentPredecessorSuccessors() {
    for (BasicBlock block : blocks) {
      // Check that all successors are distinct.
      assert new HashSet<>(block.getSuccessors()).size() == block.getSuccessors().size();
      for (BasicBlock succ : block.getSuccessors()) {
        // Check that successors are in the block list.
        assert blocks.contains(succ);
        // Check that successors have this block as a predecessor.
        assert succ.getPredecessors().contains(block);
      }
      // Check that all predecessors are distinct.
      assert new HashSet<>(block.getPredecessors()).size() == block.getPredecessors().size();
      for (BasicBlock pred : block.getPredecessors()) {
        // Check that predecessors are in the block list.
        assert blocks.contains(pred);
        // Check that predecessors have this block as a successor.
        assert pred.getSuccessors().contains(block);
      }
    }
    return true;
  }

  private boolean consistentCatchHandlers() {
    for (BasicBlock block : blocks) {
      assert block.consistentCatchHandlers();
    }
    return true;
  }

  public boolean consistentBlockNumbering() {
    blocks
        .stream()
        .collect(Collectors.groupingBy(BasicBlock::getNumber, Collectors.counting()))
        .forEach(
            (key, value) -> {
              assert value == 1;
            });
    return true;
  }

  private boolean consistentBlockInstructions() {
    boolean argumentsAllowed = true;
    for (BasicBlock block : blocks) {
      assert block.consistentBlockInstructions(
          argumentsAllowed,
          options.debug || method.getOptimizationInfo().isReachabilitySensitive());
      argumentsAllowed = false;
    }
    return true;
  }

  private boolean consistentMetadata() {
    for (Instruction instruction : instructions()) {
      if (instruction.isAdd()) {
        assert metadata.mayHaveAdd() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has an add";
      } else if (instruction.isAnd()) {
        assert metadata.mayHaveAnd() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has an and";
      } else if (instruction.isCheckCast()) {
        assert metadata.mayHaveCheckCast()
            : "IR metadata should indicate that code has a check-cast";
      } else if (instruction.isConstNumber()) {
        assert metadata.mayHaveConstNumber()
            : "IR metadata should indicate that code has a const-number";
      } else if (instruction.isConstString()) {
        assert metadata.mayHaveConstString()
            : "IR metadata should indicate that code has a const-string";
      } else if (instruction.isDebugPosition()) {
        assert metadata.mayHaveDebugPosition()
            : "IR metadata should indicate that code has a debug position";
      } else if (instruction.isDexItemBasedConstString()) {
        assert metadata.mayHaveDexItemBasedConstString()
            : "IR metadata should indicate that code has a dex-item-based-const-string";
      } else if (instruction.isDiv()) {
        assert metadata.mayHaveDiv() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a div";
      } else if (instruction.isInstanceGet()) {
        assert metadata.mayHaveInstanceGet()
            : "IR metadata should indicate that code has an instance-get";
      } else if (instruction.isInstancePut()) {
        assert metadata.mayHaveInstancePut()
            : "IR metadata should indicate that code has an instance-put";
      } else if (instruction.isInstanceOf()) {
        assert metadata.mayHaveInstanceOf()
            : "IR metadata should indicate that code has an instance-of";
      } else if (instruction.isIntSwitch()) {
        assert metadata.mayHaveIntSwitch()
            : "IR metadata should indicate that code has an int-switch";
      } else if (instruction.isInvokeDirect()) {
        assert metadata.mayHaveInvokeDirect()
            : "IR metadata should indicate that code has an invoke-direct";
      } else if (instruction.isInvokeInterface()) {
        assert metadata.mayHaveInvokeInterface()
            : "IR metadata should indicate that code has an invoke-interface";
      } else if (instruction.isInvokePolymorphic()) {
        assert metadata.mayHaveInvokePolymorphic()
            : "IR metadata should indicate that code has an invoke-polymorphic";
      } else if (instruction.isInvokeStatic()) {
        assert metadata.mayHaveInvokeStatic()
            : "IR metadata should indicate that code has an invoke-static";
      } else if (instruction.isInvokeSuper()) {
        assert metadata.mayHaveInvokeSuper()
            : "IR metadata should indicate that code has an invoke-super";
      } else if (instruction.isInvokeVirtual()) {
        assert metadata.mayHaveInvokeVirtual()
            : "IR metadata should indicate that code has an invoke-virtual";
      } else if (instruction.isOr()) {
        assert metadata.mayHaveOr() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has an or";
      } else if (instruction.isMonitor()) {
        assert metadata.mayHaveMonitorInstruction()
            : "IR metadata should indicate that code has a monitor instruction";
      } else if (instruction.isMul()) {
        assert metadata.mayHaveMul() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a mul";
      } else if (instruction.isRem()) {
        assert metadata.mayHaveRem() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a rem";
      } else if (instruction.isShl()) {
        assert metadata.mayHaveShl() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a shl";
      } else if (instruction.isShr()) {
        assert metadata.mayHaveShr() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a shr";
      } else if (instruction.isStaticGet()) {
        assert metadata.mayHaveStaticGet()
            : "IR metadata should indicate that code has a static-get";
      } else if (instruction.isStaticPut()) {
        assert metadata.mayHaveStaticPut()
            : "IR metadata should indicate that code has a static-put";
      } else if (instruction.isStringSwitch()) {
        assert metadata.mayHaveStringSwitch()
            : "IR metadata should indicate that code has a string-switch";
      } else if (instruction.isSub()) {
        assert metadata.mayHaveSub() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has a sub";
      } else if (instruction.isUshr()) {
        assert metadata.mayHaveUshr() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has an ushr";
      } else if (instruction.isXor()) {
        assert metadata.mayHaveXor() && metadata.mayHaveArithmeticOrLogicalBinop()
            : "IR metadata should indicate that code has an xor";
      }
    }
    return true;
  }

  private boolean validThrowingInstructions() {
    for (BasicBlock block : blocks) {
      if (block.hasCatchHandlers()) {
        assert block != entryBlock();
        for (BasicBlock handler : block.getCatchHandlers().getUniqueTargets()) {
          // Ensure that catch handlers are always split edges for a well-formed SSA graph.
          assert handler.getPredecessors().size() == 1;
        }
        boolean seenThrowing = false;
        for (Instruction instruction : block.getInstructions()) {
          if (instruction.instructionTypeCanThrow()) {
            assert !seenThrowing;
            seenThrowing = true;
            continue;
          }
          // After the throwing instruction only debug instructions and the final jump instruction
          // is allowed.
          assert !seenThrowing || instruction.isAllowedAfterThrowingInstruction();
        }
      }
    }
    return true;
  }

  public boolean verifyNoImpreciseOrBottomTypes() {
    Predicate<Value> verifyValue =
        v -> {
          assert v.getTypeLattice().isPreciseType();
          assert !v.getTypeLattice().isFineGrainedType();
          // For now we assume no bottom types on IR values. We may want to reconsider this for
          // representing unreachable code.
          assert !v.getTypeLattice().isBottom();
          assert !(v.definition instanceof ImpreciseMemberTypeInstruction)
              || ((ImpreciseMemberTypeInstruction) v.definition).getMemberType().isPrecise();
          return true;
        };
    return verifySSATypeLattice(wrapSSAVerifierWithStackValueHandling(verifyValue));
  }

  public boolean verifyNoNullabilityBottomTypes() {
    Predicate<Value> verifyValue =
        v -> {
          assert v.getTypeLattice().isPrimitive()
              || v.getTypeLattice().asReferenceTypeLatticeElement().nullability()
                  != Nullability.bottom();
          return true;
        };
    return verifySSATypeLattice(wrapSSAVerifierWithStackValueHandling(verifyValue));
  }

  private boolean verifyNoValueWithOnlyAssumeInstructionAsUsers() {
    Predicate<Value> verifyValue =
        v -> {
          assert !v.hasUsers()
                  || v.uniqueUsers().stream().anyMatch(i -> !i.isAssume())
                  || (!v.isPhi() && v.definition.isArgument())
                  || !v.hasDebugUsers()
                  || v.debugUsers().stream().anyMatch(i -> !i.isAssume())
                  || v.numberOfPhiUsers() > 0
              : StringUtils.join(v.uniqueUsers(), System.lineSeparator());
          return true;
        };
    return verifySSATypeLattice(wrapSSAVerifierWithStackValueHandling(verifyValue));
  }

  private Predicate<Value> wrapSSAVerifierWithStackValueHandling(Predicate<Value> tester) {
    return v -> {
      // StackValues is an artificial type created to allow returning multiple values from an
      // instruction.
      if (v instanceof StackValues) {
        return Stream.of(((StackValues) v).getStackValues()).allMatch(tester);
      } else {
        return tester.test(v);
      }
    };
  }

  private boolean verifySSATypeLattice(Predicate<Value> tester) {
    for (BasicBlock block : blocks) {
      for (Instruction instruction : block.getInstructions()) {
        if (instruction.hasOutValue()) {
          assert tester.test(instruction.outValue());
        }
      }
      for (Phi phi : block.getPhis()) {
        assert tester.test(phi);
      }
    }
    return true;
  }

  public Iterable<Instruction> instructions() {
    return this::instructionIterator;
  }

  public <T extends Instruction> Iterable<T> instructions(Predicate<Instruction> predicate) {
    return () -> IteratorUtils.filter(instructionIterator(), predicate);
  }

  public InstructionIterator instructionIterator() {
    return new IRCodeInstructionIterator(this);
  }

  public InstructionListIterator instructionListIterator() {
    return new IRCodeInstructionListIterator(this);
  }

  public List<BasicBlock> computeNormalExitBlocks() {
    ImmutableList.Builder<BasicBlock> builder = ImmutableList.builder();
    for (BasicBlock block : blocks) {
      if (block.exit().isReturn()) {
        builder.add(block);
      }
    }
    return builder.build();
  }

  public ListIterator<BasicBlock> listIterator() {
    return new BasicBlockIterator(this);
  }

  public ListIterator<BasicBlock> listIterator(int index) {
    return new BasicBlockIterator(this, index);
  }

  public ImmutableList<BasicBlock> numberInstructions() {
    ImmutableList<BasicBlock> blocks = topologicallySortedBlocks();
    for (BasicBlock block : blocks) {
      nextInstructionNumber = block.numberInstructions(nextInstructionNumber);
    }
    return blocks;
  }

  public int numberRemainingInstructions() {
    for (Instruction instruction : instructions()) {
      if (instruction.getNumber() == -1) {
        instruction.setNumber(nextInstructionNumber);
        nextInstructionNumber += INSTRUCTION_NUMBER_DELTA;
      }
    }
    return nextInstructionNumber;
  }

  public int getNextInstructionNumber() {
    return nextInstructionNumber;
  }

  public List<Value> collectArguments() {
    return collectArguments(false);
  }

  public List<Value> collectArguments(boolean ignoreReceiver) {
    final List<Value> arguments = new ArrayList<>();
    InstructionIterator iterator = entryBlock().iterator();
    while (iterator.hasNext()) {
      Instruction instruction = iterator.next();
      if (instruction.isArgument()) {
        Value out = instruction.asArgument().outValue();
        if (!ignoreReceiver || !out.isThis()) {
          arguments.add(out);
        }
      }
    }
    assert arguments.size()
        == method.method.getArity() + ((method.accessFlags.isStatic() || ignoreReceiver) ? 0 : 1);
    return arguments;
  }

  public Value getThis() {
    if (method.accessFlags.isStatic()) {
      return null;
    }
    Instruction firstArg = entryBlock().iterator().nextUntil(Instruction::isArgument);
    assert firstArg != null;
    Value thisValue = firstArg.asArgument().outValue();
    assert thisValue.isThis();
    return thisValue;
  }

  public Value createValue(TypeLatticeElement typeLattice, DebugLocalInfo local) {
    return new Value(valueNumberGenerator.next(), typeLattice, local);
  }

  public Value createValue(TypeLatticeElement typeLattice) {
    return createValue(typeLattice, null);
  }

  public ConstNumber createDoubleConstant(double value, DebugLocalInfo local) {
    Value out = createValue(TypeLatticeElement.DOUBLE, local);
    return new ConstNumber(out, Double.doubleToLongBits(value));
  }

  public ConstNumber createFloatConstant(float value, DebugLocalInfo local) {
    Value out = createValue(TypeLatticeElement.FLOAT, local);
    return new ConstNumber(out, Float.floatToIntBits(value));
  }

  public ConstNumber createIntConstant(int value) {
    return createIntConstant(value, null);
  }

  public ConstNumber createIntConstant(int value, DebugLocalInfo local) {
    Value out = createValue(TypeLatticeElement.INT, local);
    return new ConstNumber(out, value);
  }

  public ConstNumber createLongConstant(long value, DebugLocalInfo local) {
    Value out = createValue(TypeLatticeElement.LONG, local);
    return new ConstNumber(out, value);
  }

  public Phi createPhi(BasicBlock block, TypeLatticeElement type) {
    return new Phi(valueNumberGenerator.next(), block, type, null, RegisterReadType.NORMAL);
  }

  public final int getHighestBlockNumber() {
    return blocks.stream().max(Comparator.comparingInt(BasicBlock::getNumber)).get().getNumber();
  }

  public ConstClass createConstClass(AppView<?> appView, DexType type) {
    Value out =
        createValue(TypeLatticeElement.fromDexType(type, Nullability.definitelyNotNull(), appView));
    return new ConstClass(out, type);
  }

  public ConstNumber createConstNull() {
    Value out = createValue(TypeLatticeElement.NULL);
    return new ConstNumber(out, 0);
  }

  public ConstNumber createConstNull(DebugLocalInfo local) {
    Value out = createValue(TypeLatticeElement.NULL, local);
    return new ConstNumber(out, 0);
  }

  public boolean doAllThrowingInstructionsHavePositions() {
    return allThrowingInstructionsHavePositions;
  }

  public void setAllThrowingInstructionsHavePositions(boolean value) {
    this.allThrowingInstructionsHavePositions = value;
  }

  private boolean computeAllThrowingInstructionsHavePositions() {
    for (Instruction instruction : instructions()) {
      if (instruction.instructionTypeCanThrow()
          && !instruction.isConstString()
          && !instruction.isDexItemBasedConstString()
          && instruction.getPosition().isNone()
          && !instruction.getPosition().isSyntheticNone()) {
        return false;
      }
    }
    return true;
  }

  public void removeAllTrivialPhis() {
    removeAllTrivialPhis(null, null);
  }

  public void removeAllTrivialPhis(IRBuilder builder) {
    removeAllTrivialPhis(builder, null);
  }

  public void removeAllTrivialPhis(Set<Value> affectedValues) {
    removeAllTrivialPhis(null, affectedValues);
  }

  public void removeAllTrivialPhis(IRBuilder builder, Set<Value> affectedValues) {
    for (BasicBlock block : blocks) {
      List<Phi> phis = new ArrayList<>(block.getPhis());
      for (Phi phi : phis) {
        phi.removeTrivialPhi(builder, affectedValues);
      }
    }
  }

  public int reserveMarkingColor() {
    assert anyMarkingColorAvailable();
    int color = 1;
    while ((usedMarkingColors & color) == color) {
      assert color <= MAX_MARKING_COLOR;
      color <<= 1;
    }
    usedMarkingColors |= color;
    assert isMarkingColorInUse(color);
    assert verifyNoBlocksMarked(color);
    return color;
  }

  public boolean anyMarkingColorAvailable() {
    int color = 1;
    while ((usedMarkingColors & color) == color) {
      if (color > MAX_MARKING_COLOR) {
        return false;
      }
      color <<= 1;
    }
    return true;
  }

  public void returnMarkingColor(int color) {
    assert isMarkingColorInUse(color);
    clearMarks(color);
    usedMarkingColors &= ~color;
  }

  public boolean isMarkingColorInUse(int color) {
    return (usedMarkingColors & color) != 0;
  }

  public boolean anyBlocksMarkedWithColor(int color) {
    for (BasicBlock block : blocks) {
      if (block.isMarked(color)) {
        return true;
      }
    }
    return false;
  }

  public boolean noColorsInUse() {
    return usedMarkingColors == 0;
  }

  public Iterable<Instruction> getInstructionsReachableFrom(Instruction instruction) {
    BasicBlock source = instruction.getBlock();
    Set<BasicBlock> blocksReachableFromSource = getBlocksReachableFromExclusive(source);
    if (blocksReachableFromSource.contains(source)) {
      Iterable<Instruction> result = null;
      for (BasicBlock block : blocksReachableFromSource) {
        result =
            result != null
                ? Iterables.concat(result, block.getInstructions())
                : block.getInstructions();
      }
      return result;
    } else {
      Iterable<Instruction> result = () -> source.iterator(instruction);
      for (BasicBlock block : blocksReachableFromSource) {
        result = Iterables.concat(result, block.getInstructions());
      }
      return result;
    }
  }

  /**
   * Returns the set of blocks that are reachable from the given source. The source itself is only
   * included if there is a path from the given block to itself.
   */
  public Set<BasicBlock> getBlocksReachableFromExclusive(BasicBlock source) {
    Set<BasicBlock> result = Sets.newIdentityHashSet();
    int color = reserveMarkingColor();
    markTransitiveSuccessors(new ArrayDeque<>(source.getSuccessors()), color);
    for (BasicBlock block : blocks) {
      if (block.isMarked(color)) {
        result.add(block);
      }
    }
    returnMarkingColor(color);
    return result;
  }

  public Set<BasicBlock> getUnreachableBlocks() {
    Set<BasicBlock> unreachableBlocks = Sets.newIdentityHashSet();
    int color = reserveMarkingColor();
    markTransitiveSuccessors(entryBlock(), color);
    for (BasicBlock block : blocks) {
      if (!block.isMarked(color)) {
        unreachableBlocks.add(block);
      }
    }
    returnMarkingColor(color);
    return unreachableBlocks;
  }

  public Set<Value> removeUnreachableBlocks() {
    ImmutableSet.Builder<Value> affectedValueBuilder = ImmutableSet.builder();
    int color = reserveMarkingColor();
    markTransitiveSuccessors(entryBlock(), color);
    ListIterator<BasicBlock> blockIterator = listIterator();
    while (blockIterator.hasNext()) {
      BasicBlock current = blockIterator.next();
      if (!current.isMarked(color)) {
        affectedValueBuilder.addAll(current.cleanForRemoval());
        blockIterator.remove();
      }
    }
    returnMarkingColor(color);
    return affectedValueBuilder.build();
  }

  // Note: It is the responsibility of the caller to return the marking color.
  private void markTransitiveSuccessors(BasicBlock subject, int color) {
    markTransitiveSuccessors(DequeUtils.newArrayDeque(subject), color);
  }

  private void markTransitiveSuccessors(Deque<BasicBlock> worklist, int color) {
    assert isMarkingColorInUse(color) && !anyBlocksMarkedWithColor(color);
    while (!worklist.isEmpty()) {
      BasicBlock block = worklist.poll();
      if (block.isMarked(color)) {
        continue;
      }
      block.mark(color);
      for (BasicBlock successor : block.getSuccessors()) {
        if (!successor.isMarked(color)) {
          worklist.add(successor);
        }
      }
    }
  }

  /**
   * Marks the transitive predecessors of the given block, including the block itself.
   *
   * <p>Note: It is the responsibility of the caller to return the marking color.
   */
  public void markTransitivePredecessors(BasicBlock subject, int color) {
    assert isMarkingColorInUse(color);
    Queue<BasicBlock> worklist = new ArrayDeque<>();
    worklist.add(subject);
    while (!worklist.isEmpty()) {
      BasicBlock block = worklist.poll();
      if (block.isMarked(color)) {
        continue;
      }
      block.mark(color);
      for (BasicBlock predecessor : block.getPredecessors()) {
        if (!predecessor.isMarked(color)) {
          worklist.add(predecessor);
        }
      }
    }
  }

  public Position findFirstNonNonePosition() {
    return findFirstNonNonePosition(Position.none());
  }

  public Position findFirstNonNonePosition(Position orElse) {
    BasicBlock current = entryBlock();
    Set<BasicBlock> visitedBlocks = Sets.newIdentityHashSet();
    do {
      boolean changed = visitedBlocks.add(current);
      assert changed;

      for (Instruction instruction : current.getInstructions()) {
        if (instruction.isArgument() || instruction.isGoto()) {
          continue;
        }
        if (instruction.getPosition().isSome()) {
          return instruction.getPosition();
        }
      }

      // The very first non-argument instruction can be chained via goto.
      if (current.exit().isGoto()) {
        current = current.exit().asGoto().getTarget();
      } else {
        break;
      }
    } while (!visitedBlocks.contains(current));
    return orElse;
  }
}
