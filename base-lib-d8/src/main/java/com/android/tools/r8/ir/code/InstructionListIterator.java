// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.Sets;
import java.util.ListIterator;
import java.util.Set;

public interface InstructionListIterator
    extends InstructionIterator, ListIterator<Instruction>, PreviousUntilIterator<Instruction> {

  /**
   * Replace the current instruction (aka the {@link Instruction} returned by the previous call to
   * {@link #next} with the passed in <code>newInstruction</code>.
   *
   * <p>The current instruction will be completely detached from the instruction stream with uses of
   * its in-values removed.
   *
   * <p>If the current instruction produces an out-value the new instruction must also produce an
   * out-value, and all uses of the current instructions out-value will be replaced by the new
   * instructions out-value.
   *
   * <p>The debug information of the current instruction will be attached to the new instruction.
   *
   * @param newInstruction the instruction to insert instead of the current.
   */
  void replaceCurrentInstruction(Instruction newInstruction);

  // Do not show a deprecation warning for InstructionListIterator.remove().
  @SuppressWarnings("deprecation")
  @Override
  void remove();

  // Removes the current instruction, even if it has an out-value that is used.
  default void removeInstructionIgnoreOutValue() {
    throw new Unimplemented();
  }

  /**
   * Safe removal function that will insert a DebugLocalRead to take over the debug values if any
   * are associated with the current instruction.
   */
  void removeOrReplaceByDebugLocalRead();

  default void setInsertionPosition(Position position) {
    // Intentionally empty.
  }

  Value insertConstNullInstruction(IRCode code, InternalOptions options);

  Value insertConstIntInstruction(IRCode code, InternalOptions options, int value);

  void replaceCurrentInstructionWithConstInt(
      AppView<? extends AppInfoWithSubtyping> appView, IRCode code, int value);

  void replaceCurrentInstructionWithStaticGet(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      DexField field,
      Set<Value> affectedValues);

  /**
   * Replace the current instruction with null throwing instructions.
   *
   * @param appView with subtype info through which we can test if the guard is subtype of NPE.
   * @param code the IR code for the block this iterator originates from.
   * @param blockIterator basic block iterator used to iterate the blocks.
   * @param blocksToRemove set passed where blocks that were detached from the graph, but not
   *     removed yet are added. When inserting `throw null`, catch handlers whose guard does not
   *     catch NPE will be removed, but not yet removed using the passed block
   *     <code>blockIterator</code>. When iterating using <code>blockIterator</code> after then
   *     method returns the blocks in this set must be skipped when iterating with the active
   *     <code>blockIterator</code> and ultimately removed.
   * @param affectedValues set passed where values depending on detached blocks will be added.
   */
  void replaceCurrentInstructionWithThrowNull(
      AppView<? extends AppInfoWithSubtyping> appView,
      IRCode code,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      Set<Value> affectedValues);

  /**
   * Split the block into two blocks at the point of the {@link ListIterator} cursor. The existing
   * block will have all the instructions before the cursor, and the new block all the
   * instructions after the cursor.
   *
   * If the current block has catch handlers these catch handlers will be attached to the block
   * containing the throwing instruction after the split.
   *
   * @param code the IR code for the block this iterator originates from.
   * @param blockIterator basic block iterator used to iterate the blocks. This must be positioned
   * just after the block for which this is the instruction iterator. After this method returns it
   * will be positioned just after the basic block returned. Calling {@link #remove} without
   * further navigation will remove that block.
   * @return Returns the new block with the instructions after the cursor.
   */
  BasicBlock split(IRCode code, ListIterator<BasicBlock> blockIterator);

  default BasicBlock split(IRCode code) {
    return split(code, null);
  }

  /**
   * Split the block into three blocks. The first split is at the point of the {@link ListIterator}
   * cursor and the second split is <code>instructions</code> after the cursor. The existing
   * block will have all the instructions before the cursor, and the two new blocks all the
   * instructions after the cursor.
   *
   * If the current block have catch handlers these catch handlers will be attached to the block
   * containing the throwing instruction after the split.
   *
   * @param code the IR code for the block this iterator originates from.
   * @param instructions the number of instructions to include in the second block.
   * @param blockIterator basic block iterator used to iterate the blocks. This must be positioned
   * just after the block for this is the instruction iterator. After this method returns it will be
   * positioned just after the second block inserted. Calling {@link #remove} without further
   * navigation will remove that block.
   * @return Returns the new block with the instructions after the cursor.
   */
  // TODO(sgjesse): Refactor to avoid the need for passing code and blockIterator.
  BasicBlock split(IRCode code, int instructions, ListIterator<BasicBlock> blockIterator);

  /**
   * See {@link #split(IRCode, int, ListIterator)}.
   */
  default BasicBlock split(IRCode code, int instructions) {
    return split(code, instructions, null);
  }

  /**
   * Inline the code in {@code inlinee} into {@code code}, replacing the invoke instruction at the
   * position after the cursor.
   *
   * <p>The instruction at the position after cursor must be an invoke that matches the signature
   * for the code in {@code inlinee}.
   *
   * <p>With one exception (see below) both the calling code and the inlinee can have catch
   * handlers.
   *
   * <p><strong>EXCEPTION:</strong> If the invoke instruction is covered by catch handlers, and the
   * code for {@code inlinee} always throws (does not have a normal return) inlining is currently
   * <strong>NOT</strong> supported.
   *
   * @param appView {@link AppView} to retrieve class definition.
   * @param code the IR code for the block this iterator originates from.
   * @param inlinee the IR code for the block this iterator originates from.
   * @param blockIterator basic block iterator used to iterate the blocks. This must be positioned
   *     just after the block for which this is the instruction iterator. After this method returns
   *     it will be positioned just after the basic block returned.
   * @param blocksToRemove list passed where blocks that were detached from the graph, but not
   *     removed are added. When inlining an inlinee that always throws blocks in the <code>code
   *     </code> can be detached, and not simply removed using the passed <code>blockIterator
   *     </code>. When iterating using <code>blockIterator</code> after then method returns the
   *     blocks in this list must be skipped when iterating with the active <code>blockIterator
   *     </code> and ultimately removed.
   * @param downcast tells the inliner to issue a check cast operation.
   * @return the basic block with the instructions right after the inlining. This can be a block
   *     which can also be in the <code>blocksToRemove</code> list.
   */
  // TODO(sgjesse): Refactor to avoid the need for passing code.
  // TODO(sgjesse): Refactor to avoid the need for passing blocksToRemove.
  // TODO(sgjesse): Maybe don't return a BasicBlock, as it can be in blocksToRemove.
  // TODO(sgjesse): Maybe find a better place for this method.
  // TODO(sgjesse): Support inlinee with throwing instructions for invokes with existing handlers.
  BasicBlock inlineInvoke(
      AppView<?> appView,
      IRCode code,
      IRCode inlinee,
      ListIterator<BasicBlock> blockIterator,
      Set<BasicBlock> blocksToRemove,
      DexType downcast);

  /** See {@link #inlineInvoke(AppView, IRCode, IRCode, ListIterator, Set, DexType)}. */
  default BasicBlock inlineInvoke(AppView<?> appView, IRCode code, IRCode inlinee) {
    Set<BasicBlock> blocksToRemove = Sets.newIdentityHashSet();
    BasicBlock result = inlineInvoke(appView, code, inlinee, null, blocksToRemove, null);
    code.removeBlocks(blocksToRemove);
    return result;
  }
}
