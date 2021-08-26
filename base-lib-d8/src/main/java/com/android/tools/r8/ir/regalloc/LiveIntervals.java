// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

import static com.android.tools.r8.dex.Constants.U16BIT_MAX;
import static com.android.tools.r8.dex.Constants.U8BIT_MAX;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.utils.CfgPrinter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.function.IntConsumer;

public class LiveIntervals implements Comparable<LiveIntervals> {

  public static final int NO_REGISTER = Integer.MIN_VALUE;
  public static final int CHILDREN_SORTING_CUTOFF = 100;

  private final Value value;
  private LiveIntervals nextConsecutive;
  private LiveIntervals previousConsecutive;
  private final LiveIntervals splitParent;
  private final List<LiveIntervals> splitChildren = new ArrayList<>();
  private final IntArrayList sortedSplitChildrenEnds = new IntArrayList();
  private boolean sortedChildren = false;
  private List<LiveRange> ranges = new ArrayList<>();
  private final TreeSet<LiveIntervalsUse> uses = new TreeSet<>();
  private int numberOfConsecutiveRegisters = -1;
  private int register = NO_REGISTER;
  private Integer hint;
  private boolean spilled = false;
  private boolean usedInMonitorOperations = false;

  // Only registers up to and including the registerLimit are allowed for this interval.
  private int registerLimit = U16BIT_MAX;

  // Max register used for any of the non-spilled splits for these live intervals or for any of the
  // live intervals that this live interval is connected to by phi moves. This is used to
  // conservatively determine if it is safe to use rematerialization for this value.
  private int maxNonSpilledRegister = NO_REGISTER;
  private boolean isRematerializable = false;

  public LiveIntervals(Value value) {
    this.value = value;
    usedInMonitorOperations = value.usedInMonitorOperation();
    splitParent = this;
    value.setLiveIntervals(this);
  }

  public LiveIntervals(LiveIntervals splitParent) {
    this.splitParent = splitParent;
    value = splitParent.value;
    usedInMonitorOperations = splitParent.usedInMonitorOperations;
  }

  private int toInstructionPosition(int position) {
    return position % 2 == 0 ? position : position + 1;
  }

  private int toGapPosition(int position) {
    return position % 2 == 1 ? position : position - 1;
  }

  public Value getValue() {
    return value;
  }

  public ValueType getType() {
    return value.outType();
  }

  public int requiredRegisters() {
    return getType().requiredRegisters();
  }

  public void setHint(LiveIntervals intervals, PriorityQueue<LiveIntervals> unhandled) {
    // Do not set hints if they cannot be used anyway.
    if (!overlaps(intervals)) {
      // The hint is used in sorting the unhandled intervals. Therefore, if the hint changes
      // we have to remove and reinsert the interval to get the sorting updated.
      boolean removed = unhandled.remove(this);
      hint = intervals.getRegister();
      if (removed) {
        unhandled.add(this);
      }
    }
  }

  public Integer getHint() {
    return hint;
  }

  public void setSpilled(boolean value) {
    // Check that we always spill arguments to their original register.
    assert getRegister() != NO_REGISTER;
    assert !(value && isArgumentInterval()) || getRegister() == getSplitParent().getRegister();
    spilled = value;
  }

  public boolean isSpilled() {
    return spilled;
  }

  private boolean isRematerializable() {
    assert splitParent == this;
    return isRematerializable;
  }

  private boolean allSplitsAreSpilled() {
    assert isSpilled();
    for (LiveIntervals splitChild : splitChildren) {
      assert splitChild.isSpilled();
    }
    return true;
  }

  public boolean isSpilledAndRematerializable() {
    return isSpilled() && splitParent.isRematerializable();
  }

  public void link(LiveIntervals next) {
    assert numberOfConsecutiveRegisters == -1;
    nextConsecutive = next;
    next.previousConsecutive = this;
  }

  public boolean isLinked() {
    return splitParent.previousConsecutive != null || splitParent.nextConsecutive != null;
  }

  public boolean isArgumentInterval() {
    Instruction definition = this.splitParent.value.definition;
    return definition != null && definition.isArgument();
  }

  public LiveIntervals getStartOfConsecutive() {
    LiveIntervals current = this;
    while (current.previousConsecutive != null) {
      current = current.previousConsecutive;
    }
    return current;
  }

  public LiveIntervals getNextConsecutive() {
    return nextConsecutive;
  }

  public LiveIntervals getPreviousConsecutive() {
    return previousConsecutive;
  }

  public int numberOfConsecutiveRegisters() {
    LiveIntervals start = getStartOfConsecutive();
    if (start.numberOfConsecutiveRegisters != -1) {
      assert start.numberOfConsecutiveRegisters == computeNumberOfConsecutiveRegisters();
      return start.numberOfConsecutiveRegisters;
    }
    return computeNumberOfConsecutiveRegisters();
  }

  private int computeNumberOfConsecutiveRegisters() {
    LiveIntervals start = getStartOfConsecutive();
    int result = 0;
    for (LiveIntervals current = start;
        current != null;
        current = current.nextConsecutive) {
      result += current.requiredRegisters();
    }
    start.numberOfConsecutiveRegisters = result;
    return result;
  }

  public boolean hasSplits() {
    return splitChildren.size() != 0;
  }

  private void sortSplitChildrenIfNeeded() {
    if (!sortedChildren) {
      splitChildren.sort(Comparator.comparingInt(LiveIntervals::getEnd));
      sortedSplitChildrenEnds.clear();
      for (LiveIntervals splitChild : splitChildren) {
        sortedSplitChildrenEnds.add(splitChild.getEnd());
      }
      assert sortedChildrenConsistent();
      sortedChildren = true;
    }
  }

  private boolean sortedChildrenConsistent() {
    for (int i = 0; i < splitChildren.size(); i++) {
      assert splitChildren.get(i).getEnd() == sortedSplitChildrenEnds.getInt(i);
      assert i == 0 || sortedSplitChildrenEnds.getInt(i - 1) <= sortedSplitChildrenEnds.getInt(i);
    }
    return true;
  }

  public List<LiveIntervals> getSplitChildren() {
    return splitChildren;
  }

  public LiveIntervals getSplitParent() {
    return splitParent;
  }

  /**
   * Add a live range to the intervals.
   *
   * @param range the range to add
   */
  public void addRange(LiveRange range) {
    boolean added = tryAddRange(range);
    assert added;
  }

  private boolean tryAddRange(LiveRange range) {
    if (ranges.size() > 0) {
      LiveRange lastRange = ranges.get(ranges.size() - 1);
      if (lastRange.isInfinite()) {
        return false;
      }
      int rangeStartInstructionPosition = toInstructionPosition(range.start);
      int lastRangeEndInstructionPosition = toInstructionPosition(lastRange.end);
      if (lastRangeEndInstructionPosition > rangeStartInstructionPosition) {
        return false;
      }
      if (lastRangeEndInstructionPosition == rangeStartInstructionPosition) {
        lastRange.end = range.end;
        return true;
      }
    }
    ranges.add(range);
    return true;
  }

  /**
   * Record a use for this interval.
   */
  public void addUse(LiveIntervalsUse use) {
    uses.add(use);
    updateRegisterConstraint(use.getLimit());
  }

  public void updateRegisterConstraint(int constraint) {
    registerLimit = Math.min(registerLimit, constraint);
  }

  public TreeSet<LiveIntervalsUse> getUses() {
    return uses;
  }

  public List<LiveRange> getRanges() {
    return ranges;
  }

  public int getStart() {
    assert !ranges.isEmpty();
    return ranges.get(0).start;
  }

  public int getEnd() {
    assert !ranges.isEmpty();
    return ranges.get(ranges.size() - 1).end;
  }

  public int getRegister() {
    return register;
  }

  public int getRegisterLimit() {
    return registerLimit;
  }

  public void setRegister(int n) {
    assert register == NO_REGISTER || register == n;
    register = n;
  }

  private int computeMaxNonSpilledRegister() {
    assert splitParent == this;
    assert maxNonSpilledRegister == NO_REGISTER;
    if (!isSpilled()) {
      maxNonSpilledRegister = getRegister();
    }
    for (LiveIntervals child : splitChildren) {
      if (!child.isSpilled()) {
        maxNonSpilledRegister = Math.max(maxNonSpilledRegister, child.getRegister());
      }
    }
    return maxNonSpilledRegister;
  }

  public void setMaxNonSpilledRegister(int i) {
    assert i >= splitParent.maxNonSpilledRegister;
    splitParent.maxNonSpilledRegister = i;
  }

  public int getMaxNonSpilledRegister() {
    if (splitParent.maxNonSpilledRegister != NO_REGISTER) {
      return splitParent.maxNonSpilledRegister;
    }
    return splitParent.computeMaxNonSpilledRegister();
  }

  public boolean usesRegister(int n, boolean otherIsWide) {
    if (register == n) {
      return true;
    }
    if (getType().isWide() && register + 1 == n) {
      return true;
    }
    if (otherIsWide && register == n + 1) {
      return true;
    }
    return false;
  }

  public boolean hasConflictingRegisters(LiveIntervals other) {
    return other.usesRegister(register, getType().isWide());
  }

  public void clearRegisterAssignment() {
    register = NO_REGISTER;
    hint = null;
  }

  public boolean overlapsPosition(int position) {
    for (LiveRange range : ranges) {
      if (range.start > position) {
        // Ranges are sorted. When a range starts after position there is no overlap.
        return false;
      }
      if (position < range.end) {
        return true;
      }
    }
    return false;
  }

  public boolean overlaps(LiveIntervals other) {
    return nextOverlap(other) != -1;
  }

  public boolean anySplitOverlaps(LiveIntervals other) {
    LiveIntervals parent = getSplitParent();
    if (parent.overlaps(other)) {
      return true;
    }
    for (LiveIntervals child : parent.getSplitChildren()) {
      if (child.overlaps(other)) {
        return true;
      }
    }
    return false;
  }

  public int nextOverlap(LiveIntervals other) {
    Iterator<LiveRange> it = other.ranges.iterator();
    LiveRange otherRange = it.next();
    for (LiveRange range : ranges) {
      while (otherRange.end <= range.start) {
        if (!it.hasNext()) {
          return -1;
        }
        otherRange = it.next();
      }
      if (otherRange.start < range.end) {
        return otherRange.start;
      }
    }
    return -1;
  }

  public int firstUseAfter(int unhandledStart) {
    for (LiveIntervalsUse use : uses) {
      if (use.getPosition() >= unhandledStart) {
        return use.getPosition();
      }
    }
    return Integer.MAX_VALUE;
  }

  public int getFirstUse() {
    return uses.first().getPosition();
  }

  public LiveIntervalsUse firstUseWithConstraint() {
    for (LiveIntervalsUse use : uses) {
      if (use.hasConstraint()) {
        return use;
      }
    }
    return null;
  }

  public void forEachRegister(IntConsumer consumer) {
    assert register != NO_REGISTER;
    consumer.accept(register);
    if (getType().isWide()) {
      consumer.accept(register + 1);
    }
  }

  public LiveIntervals splitBefore(int start) {
    if (toInstructionPosition(start) == toInstructionPosition(getStart())) {
      assert uses.size() == 0 || getFirstUse() != start;
      register = NO_REGISTER;
      return this;
    }
    start = toGapPosition(start);
    LiveIntervals splitChild = new LiveIntervals(splitParent);
    splitParent.splitChildren.add(splitChild);
    splitParent.sortedChildren = false;
    List<LiveRange> beforeSplit = new ArrayList<>();
    List<LiveRange> afterSplit = new ArrayList<>();
    if (start == getEnd()) {
      beforeSplit = ranges;
      afterSplit.add(new LiveRange(start, start));
    } else {
      int rangeToSplitIndex = 0;
      for (; rangeToSplitIndex < ranges.size(); rangeToSplitIndex++) {
        LiveRange range = ranges.get(rangeToSplitIndex);
        if (range.start <= start && range.end > start) {
          break;
        }
        if (range.start > start) {
          break;
        }
      }
      LiveRange rangeToSplit = ranges.get(rangeToSplitIndex);
      beforeSplit.addAll(ranges.subList(0, rangeToSplitIndex));
      if (rangeToSplit.start < start) {
        beforeSplit.add(new LiveRange(rangeToSplit.start, start));
        afterSplit.add(new LiveRange(start, rangeToSplit.end));
      } else {
        afterSplit.add(rangeToSplit);
      }
      afterSplit.addAll(ranges.subList(rangeToSplitIndex + 1, ranges.size()));
    }
    splitChild.ranges = afterSplit;
    ranges = beforeSplit;
    while (!uses.isEmpty() && uses.last().getPosition() >= start) {
      splitChild.addUse(uses.pollLast());
    }
    // Recompute limit after having removed uses from this interval.
    recomputeLimit();
    assert !ranges.isEmpty();
    assert !splitChild.ranges.isEmpty();
    return splitChild;
  }

  public void undoSplits() {
    List<LiveRange> ranges = new ArrayList<>(this.ranges);
    for (LiveIntervals split : splitChildren) {
      ranges.addAll(split.ranges);
      for (LiveIntervalsUse use : split.uses) {
        addUse(use);
      }
    }
    Collections.sort(ranges);
    this.ranges.clear();
    for (LiveRange range : ranges) {
      addRange(range);
    }
    splitChildren.clear();
    recomputeLimit();
  }

  private void recomputeLimit() {
    registerLimit = U16BIT_MAX;
    for (LiveIntervalsUse use : uses) {
      updateRegisterConstraint(use.getLimit());
    }
  }

  public LiveIntervals getSplitCovering(int instructionNumber) {
    assert getSplitParent() == this;
    // Check if this interval itself is covering the instruction.
    if (getStart() <= instructionNumber && getEnd() > instructionNumber) {
      return this;
    }
    // If the instruction number is not in this intervals range, we go through all split children.
    // If we do not find a child that contains the instruction number we return the interval
    // whose end is the instruction number. This is needed when transitioning values across
    // control-flow boundaries.
    LiveIntervals matchingEnd = getEnd() == instructionNumber ? this : null;
    // When there are many children, avoid looking at the ones for which the end is before
    // the instruction number by doing a binary search for the first candidate whose end is after
    // the instruction number.
    int firstCandidate = 0;
    if (splitChildren.size() > CHILDREN_SORTING_CUTOFF) {
      sortSplitChildrenIfNeeded();
      firstCandidate = Collections.binarySearch(sortedSplitChildrenEnds, instructionNumber);
      if (firstCandidate < 0) {
        firstCandidate = -(firstCandidate + 1);
      }
    }
    for (int i = firstCandidate; i < splitChildren.size(); i++) {
      LiveIntervals splitChild = splitChildren.get(i);
      if (splitChild.getStart() <= instructionNumber && splitChild.getEnd() > instructionNumber) {
        return splitChild;
      }
      if (splitChild.getEnd() == instructionNumber) {
        matchingEnd = splitChild;
      }
    }
    if (matchingEnd != null) {
      return matchingEnd;
    }
    assert false : "Couldn't find split covering instruction position.";
    return null;
  }

  public boolean isConstantNumberInterval() {
    return value.definition != null && value.isConstNumber();
  }

  public boolean usedInMonitorOperation() {
    return usedInMonitorOperations;
  }

  public boolean isNewStringInstanceDisallowingSpilling() {
    // Due to b/80118070 some String new-instances must not be spilled.
    return value.definition != null
        && value.definition.isNewInstance()
        && !value.definition.asNewInstance().isSpillingAllowed();
  }

  public int numberOfUsesWithConstraint() {
    int count = 0;
    for (LiveIntervalsUse use : getUses()) {
      if (use.hasConstraint()) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int compareTo(LiveIntervals other) {
    // Sort by interval start instruction number.
    int startDiff = getStart() - other.getStart();
    if (startDiff != 0) return startDiff;
    // Then sort by register number of hints to make sure that a phi
    // does not take a low register that is the hint for another phi.
    if (hint != null && other.hint != null) {
      int registerDiff = hint - other.hint;
      if (registerDiff != 0) return registerDiff;
    }
    // Intervals with hints go first so intervals without hints
    // do not take registers from intervals with hints.
    if (hint != null && other.hint == null) return -1;
    if (hint == null && other.hint != null) return 1;
    // Tie-breaker: no values have equal numbers.
    int result = value.getNumber() - other.value.getNumber();
    assert result != 0;
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("(cons ");
    // Use the field here to avoid toString to have side effects.
    builder.append(numberOfConsecutiveRegisters);
    builder.append("): ");
    for (LiveRange range : getRanges()) {
      builder.append(range);
      builder.append(" ");
    }
    builder.append("\n");
    return builder.toString();
  }

  public String toAscciArtString() {
    StringBuilder builder = new StringBuilder();
    int current = 0;
    for (LiveRange range : getRanges()) {
      if (range.isInfinite()) {
        builder.append("--- infinite ---...");
        break;
      }
      for (; current < range.start; current++) {
        builder.append(" ");
      }
      for (; current < range.end; current++) {
        builder.append("-");
      }
    }
    return builder.toString();
  }

  public void print(CfgPrinter printer, int number, int parentNumber) {
    printer.append(number * 10000 + register) // range number
        .sp().append("object") // range type
        .sp().append(parentNumber * 10000 + getSplitParent().getRegister()) // split parent
        .sp().append(-1); // hint
    for (LiveRange range : getRanges()) {
      printer.sp().append(range.toString());
    }
    for (LiveIntervalsUse use : getUses()) {
      printer.sp().append(use.getPosition()).sp().append("M");
    }
    printer.append(" \"\"").ln();
    int delta = 0;
    for (LiveIntervals splitChild : splitChildren) {
      delta += 10000;
      splitChild.print(printer, number + delta, number);
    }
  }

  public void computeRematerializable(LinearScanRegisterAllocator allocator) {
    assert splitParent == this;
    if (value.isArgument()) {
      isRematerializable = true;
      return;
    }

    // TODO(ager): rematerialize const string as well. However, in that case we have to be very
    // careful with methods with synchronization. If we rematerialize in a block that has no
    // other throwing instructions we can end up with lock-level verification issues. The
    // rematerialized throwing const-string instruction is not covered by the catch range going
    // to the monitor-exit instruction and we can leave the method without unlocking the monitor.
    if (!value.isConstNumber()) {
      return;
    }

    // If the constant is spilled when flowing to a phi and the phi has a register higher than what
    // can be const rematerialized then this value is not rematerializable and needs a register even
    // when spilled.
    for (Phi phi : value.uniquePhiUsers()) {
      int reg = allocator.unadjustedRealRegisterFromAllocated(phi.getLiveIntervals().getRegister());
      if (reg >= U8BIT_MAX) {
        for (int predIndex = 0; predIndex < phi.getOperands().size(); predIndex++) {
          Value operand = phi.getOperand(predIndex);
          if (operand == value) {
            int predExit = phi.getBlock().getPredecessors().get(predIndex).exit().getNumber();
            if (getSplitCovering(predExit).isSpilled()) {
              return;
            }
          }
        }
      }
    }

    // If one of the non-spilled splits uses a register that is higher than U8BIT_MAX we cannot
    // rematerialize it using a ConstNumber instruction and we use spill moves instead of
    // rematerialization. We use this check both before and after we have computed the set
    // of unused registers. We therefore have to be careful to use the same max number for
    // these computations. We use the unadjusted real register number to make sure that
    // isRematerializable for the same intervals does not change from one phase of
    // compilation to the next.
    if (getMaxNonSpilledRegister() == NO_REGISTER) {
      assert allSplitsAreSpilled();
      isRematerializable = true;
      return;
    }
    int max = allocator.unadjustedRealRegisterFromAllocated(getMaxNonSpilledRegister());
    isRematerializable = max < U8BIT_MAX;
  }
}
