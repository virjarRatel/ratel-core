// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import static com.android.tools.r8.ir.code.DominatorTree.Assumption.MAY_HAVE_UNREACHABLE_BLOCKS;

import com.android.tools.r8.ir.code.BasicBlock.BasicBlockChangeListener;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DominatorTree implements BasicBlockChangeListener {

  public enum Assumption {
    NO_UNREACHABLE_BLOCKS,
    MAY_HAVE_UNREACHABLE_BLOCKS
  }

  public enum Inclusive {
    YES,
    NO
  }

  private final BasicBlock[] sorted;
  private BasicBlock[] doms;
  private final BasicBlock normalExitBlock = new BasicBlock();

  private final int unreachableStartIndex;

  private boolean obsolete = false;

  public DominatorTree(IRCode code) {
    this(code, Assumption.NO_UNREACHABLE_BLOCKS);
  }

  public DominatorTree(IRCode code, Assumption assumption) {
    assert assumption != null;
    assert assumption == MAY_HAVE_UNREACHABLE_BLOCKS || code.getUnreachableBlocks().isEmpty();

    ImmutableList<BasicBlock> blocks = code.topologicallySortedBlocks();
    // Add the internal exit block to the block list.
    for (BasicBlock block : blocks) {
      if (block.exit().isReturn()) {
        normalExitBlock.getMutablePredecessors().add(block);
      }
    }
    int numberOfBlocks = code.blocks.size();
    if (assumption == MAY_HAVE_UNREACHABLE_BLOCKS) {
      // Unreachable blocks have been removed implicitly by the topological sort.
      sorted = new BasicBlock[numberOfBlocks + 1];
      // Move topologically sorted blocks into `sorted`.
      int color = code.reserveMarkingColor();
      int i = 0;
      for (BasicBlock block : blocks) {
        sorted[i] = block;
        block.mark(color);
        i++;
      }
      sorted[i] = normalExitBlock;
      i++;
      // Move unreachable blocks into the end of `sorted`.
      unreachableStartIndex = i;
      for (BasicBlock block : code.blocks) {
        if (!block.isMarked(color)) {
          sorted[i] = block;
          i++;
        }
      }
      code.returnMarkingColor(color);
    } else {
      sorted = blocks.toArray(new BasicBlock[numberOfBlocks + 1]);
      sorted[numberOfBlocks] = normalExitBlock;
      unreachableStartIndex = numberOfBlocks + 1;
    }
    numberBlocks();
    build();

    // This is intentionally implemented via an `assert` so that we do not attach listeners to all
    // basic blocks when running without assertions.
    assert recordChangesToControlFlowEdges(code.blocks);
  }

  /**
   * Get the immediate dominator block for a block.
   */
  public BasicBlock immediateDominator(BasicBlock block) {
    assert !obsolete;
    return doms[block.getNumber()];
  }

  /**
   * Check if one basic block is dominated by another basic block.
   *
   * @param subject subject to check for domination by {@param dominator}
   * @param dominator dominator to check against
   * @return whether {@param subject} is dominated by {@param dominator}
   */
  public boolean dominatedBy(BasicBlock subject, BasicBlock dominator) {
    assert !obsolete;
    if (subject == dominator) {
      return true;
    }
    return strictlyDominatedBy(subject, dominator);
  }

  /**
   * Checks if one basic block dominates a collection of other basic blocks.
   *
   * @param dominator dominator to check against
   * @param subjects subjects to check for domination by {@param dominator}
   * @return whether {@param subjects} are all dominated by {@param dominator}
   */
  public boolean dominatesAllOf(BasicBlock dominator, Iterable<BasicBlock> subjects) {
    for (BasicBlock subject : subjects) {
      if (!dominatedBy(subject, dominator)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if one basic block is strictly dominated by another basic block.
   *
   * @param subject subject to check for domination by {@param dominator}
   * @param dominator dominator to check against
   * @return whether {@param subject} is strictly dominated by {@param dominator}
   */
  public boolean strictlyDominatedBy(BasicBlock subject, BasicBlock dominator) {
    assert !obsolete;
    if (subject.getNumber() == 0 || subject == normalExitBlock) {
      return false;
    }
    while (true) {
      BasicBlock idom = immediateDominator(subject);
      if (idom.getNumber() < dominator.getNumber()) {
        return false;
      }
      if (idom == dominator) {
        return true;
      }
      subject = idom;
    }
  }

  /**
   * Use the dominator tree to find the dominating block that is closest to a set of blocks.
   *
   * @param blocks the block for which to find a dominator
   * @return the closest dominator for the collection of blocks
   */
  public BasicBlock closestDominator(Collection<BasicBlock> blocks) {
    assert !obsolete;
    if (blocks.size() == 0) {
      return null;
    }
    Iterator<BasicBlock> it = blocks.iterator();
    BasicBlock dominator = it.next();
    while (it.hasNext()) {
      dominator = intersect(dominator, it.next());
    }
    return dominator;
  }

  /** Returns the blocks dominated by dominator, including dominator itself. */
  public List<BasicBlock> dominatedBlocks(BasicBlock dominator) {
    assert !obsolete;
    List<BasicBlock> dominatedBlocks = new ArrayList<>();
    for (int i = dominator.getNumber(); i < unreachableStartIndex; ++i) {
      BasicBlock block = sorted[i];
      if (dominatedBy(block, dominator)) {
        dominatedBlocks.add(block);
      }
    }
    return dominatedBlocks;
  }

  /**
   * Returns an iterator over all dominator blocks of <code>dominated</code>.
   *
   * Iteration order is always the immediate dominator of the previously returned block. The
   * iteration starts by returning <code>dominated</code>.
   */
  public Iterable<BasicBlock> dominatorBlocks(BasicBlock dominated, Inclusive inclusive) {
    assert !obsolete;
    return () -> {
      Iterator<BasicBlock> iterator =
          new Iterator<BasicBlock>() {
            private BasicBlock current = dominated;

            @Override
            public boolean hasNext() {
              return current != null;
            }

            @Override
            public BasicBlock next() {
              if (!hasNext()) {
                return null;
              } else {
                BasicBlock result = current;
                if (current.getNumber() == 0) {
                  current = null;
                } else {
                  current = immediateDominator(current);
                  assert current != result;
                }
                return result;
              }
            }
          };
      if (inclusive == Inclusive.NO) {
        BasicBlock block = iterator.next();
        assert block == dominated;
      }
      return iterator;
    };
  }

  public Iterable<BasicBlock> normalExitDominatorBlocks() {
    assert !obsolete;
    // Do not include the `normalExitBlock`, since this is a synthetic block created here in the
    // DominatorTree.
    return dominatorBlocks(normalExitBlock, Inclusive.NO);
  }

  public BasicBlock[] getSortedBlocks() {
    return sorted;
  }

  private void numberBlocks() {
    for (int i = 0; i < sorted.length; i++) {
      sorted[i].setNumber(i);
    }
  }

  private boolean postorderCompareLess(BasicBlock b1, BasicBlock b2) {
    // The topological sort is reverse postorder.
    return b1.getNumber() > b2.getNumber();
  }

  // Build dominator tree based on the algorithm described in this paper:
  //
  // A Simple, Fast Dominance Algorithm
  // Cooper, Keith D.; Harvey, Timothy J.; and Kennedy, Ken (2001).
  // http://www.cs.rice.edu/~keith/EMBED/dom.pdf
  private void build() {
    doms = new BasicBlock[sorted.length];
    doms[0] = sorted[0];
    boolean changed = true;
    while (changed) {
      changed = false;
      // Run through all nodes in reverse postorder (except start node).
      for (int i = 1; i < sorted.length; i++) {
        BasicBlock b = sorted[i];
        // Pick one processed predecessor.
        BasicBlock newIDom = null;
        int picked = -1;
        for (int j = 0; newIDom == null && j < b.getPredecessors().size(); j++) {
          BasicBlock p = b.getPredecessors().get(j);
          if (doms[p.getNumber()] != null) {
            picked = j;
            newIDom = p;
          }
        }
        // Run through all other predecessors.
        for (int j = 0; j < b.getPredecessors().size(); j++) {
          BasicBlock p = b.getPredecessors().get(j);
          if (j == picked) {
            continue;
          }
          if (doms[p.getNumber()] != null) {
            newIDom = intersect(p, newIDom);
          }
        }
        if (doms[b.getNumber()] != newIDom) {
          doms[b.getNumber()] = newIDom;
          changed = true;
        }
      }
    }
  }

  private BasicBlock intersect(BasicBlock b1, BasicBlock b2) {
    BasicBlock finger1 = b1;
    BasicBlock finger2 = b2;
    while (finger1 != finger2) {
      while (postorderCompareLess(finger1, finger2)) {
        finger1 = doms[finger1.getNumber()];
      }
      while (postorderCompareLess(finger2, finger1)) {
        finger2 = doms[finger2.getNumber()];
      }
    }
    return finger1;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Dominators\n");
    for (BasicBlock block : sorted) {
      builder.append(block.getNumber());
      builder.append(": ");
      builder.append(doms[block.getNumber()].getNumber());
      builder.append("\n");
    }
    return builder.toString();
  }

  private boolean recordChangesToControlFlowEdges(List<BasicBlock> blocks) {
    for (BasicBlock block : blocks) {
      block.addControlFlowEdgesMayChangeListener(this);
    }
    return true;
  }

  @Override
  public void onSuccessorsMayChange(BasicBlock block) {
    obsolete = true;
  }

  @Override
  public void onPredecessorsMayChange(BasicBlock block) {
    obsolete = true;
  }
}
