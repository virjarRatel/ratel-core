// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InvalidDebugInfoException;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock.EdgeType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.conversion.TypeConstraintResolver;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.DequeUtils;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.SetUtils;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Phi extends Value implements InstructionOrPhi {

  public enum RegisterReadType {
    NORMAL,
    DEBUG,
  }

  private final BasicBlock block;
  private final List<Value> operands = new ArrayList<>();
  private RegisterReadType readType;
  private boolean isStackPhi;

  // Trivial phis are eliminated during IR construction. When a trivial phi is eliminated
  // we need to update all references to it. A phi can be referenced from phis, instructions
  // and current definition mappings. This list contains the current definitions mappings that
  // contain this phi.
  private List<Map<Integer, Value>> definitionUsers = new ArrayList<>();

  public Phi(
      int number,
      BasicBlock block,
      TypeLatticeElement typeLattice,
      DebugLocalInfo local,
      RegisterReadType readType) {
    super(number, typeLattice, local);
    this.block = block;
    this.readType = readType;
    block.addPhi(this);
  }

  @Override
  public boolean isPhi() {
    return true;
  }

  @Override
  public Phi asPhi() {
    return this;
  }

  @Override
  public BasicBlock getBlock() {
    return block;
  }

  @Override
  public void constrainType(
      ValueTypeConstraint constraint, DexMethod method, Origin origin, Reporter reporter) {
    if (readType == RegisterReadType.DEBUG) {
      abortOnInvalidDebugInfo(constraint);
    }
    super.constrainType(constraint, method, origin, reporter);
  }

  private void abortOnInvalidDebugInfo(ValueTypeConstraint constraint) {
    if (constrainedType(constraint) == null) {
      // If the phi has been requested from local info, throw out locals and retry compilation.
      throw new InvalidDebugInfoException(
          "Type information in locals-table is inconsistent."
              + " Cannot constrain type: "
              + typeLattice
              + " for value: "
              + this
              + " by constraint "
              + constraint
              + ".");
    }
  }

  public void addOperands(IRBuilder builder, int register) {
    // Phi operands are only filled in once to complete the phi. Some phis are incomplete for a
    // period of time to break cycles. When the cycle has been resolved they are completed
    // exactly once by adding the operands.
    assert operands.isEmpty();
    if (block.getPredecessors().size() == 0) {
      throwUndefinedValueError();
    }

    ValueTypeConstraint readConstraint = TypeConstraintResolver.constraintForType(typeLattice);
    List<Value> operands = new ArrayList<>(block.getPredecessors().size());
    for (BasicBlock pred : block.getPredecessors()) {
      EdgeType edgeType = pred.getEdgeType(block);
      Value operand = builder.readRegister(register, readConstraint, pred, edgeType, readType);
      operands.add(operand);
    }

    if (readType != RegisterReadType.NORMAL) {
      for (Value operand : operands) {
        TypeLatticeElement type = operand.getTypeLattice();
        ValueTypeConstraint constraint = TypeConstraintResolver.constraintForType(type);
        abortOnInvalidDebugInfo(constraint);
      }
    }

    for (Value operand : operands) {
      builder.constrainType(operand, readConstraint);
      appendOperand(operand);
    }
    removeTrivialPhi(builder, null);
  }

  public void addOperands(List<Value> operands) {
    addOperands(operands, true);
  }

  public void addOperands(List<Value> operands, boolean removeTrivialPhi) {
    // Phi operands are only filled in once to complete the phi. Some phis are incomplete for a
    // period of time to break cycles. When the cycle has been resolved they are completed
    // exactly once by adding the operands.
    assert this.operands.isEmpty();
    if (operands.size() == 0) {
      throwUndefinedValueError();
    }
    for (Value operand : operands) {
      appendOperand(operand);
    }
    if (removeTrivialPhi) {
      removeTrivialPhi();
    }
  }

  @Override
  public void markNonDebugLocalRead() {
    readType = RegisterReadType.NORMAL;
  }

  private void throwUndefinedValueError() {
    throw new CompilationError(
        "Undefined value encountered during compilation. "
            + "This is typically caused by invalid dex input that uses a register "
            + "that is not defined on all control-flow paths leading to the use.");
  }

  private void appendOperand(Value operand) {
    operands.add(operand);
    operand.addPhiUser(this);
  }

  public Value getOperand(int predIndex) {
    return operands.get(predIndex);
  }

  public List<Value> getOperands() {
    return operands;
  }

  public void removeOperand(int index) {
    operands.get(index).removePhiUser(this);
    operands.remove(index);
  }

  public void removeOperandsByIndex(List<Integer> operandsToRemove) {
    if (operandsToRemove.isEmpty()) {
      return;
    }
    List<Value> copy = new ArrayList<>(operands);
    operands.clear();
    int current = 0;
    for (int i : operandsToRemove) {
      operands.addAll(copy.subList(current, i));
      copy.get(i).removePhiUser(this);
      current = i + 1;
    }
    operands.addAll(copy.subList(current, copy.size()));
  }

  public void replaceOperandAt(int predIndex, Value newValue) {
    Value current = operands.get(predIndex);
    operands.set(predIndex, newValue);
    newValue.addPhiUser(this);
    current.removePhiUser(this);
  }

  void replaceOperand(Value current, Value newValue) {
    for (int i = 0; i < operands.size(); i++) {
      if (operands.get(i) == current) {
        operands.set(i, newValue);
        newValue.addPhiUser(this);
      }
    }
  }

  public boolean isTrivialPhi() {
    Value same = null;
    for (Value op : operands) {
      if (op == same || op == this) {
        // Have only seen one value other than this.
        continue;
      }
      if (same != null) {
        // Merged at least two values and is therefore not trivial.
        return false;
      }
      same = op;
    }
    return true;
  }

  public boolean removeTrivialPhi() {
    return removeTrivialPhi(null, null);
  }

  public boolean removeTrivialPhi(IRBuilder builder, Set<Value> affectedValues) {
    Value same = null;
    for (Value op : operands) {
      if (op == same || op == this) {
        // Have only seen one value other than this.
        continue;
      }
      if (same != null) {
        // Merged at least two values and is therefore not trivial.
        assert !isTrivialPhi();
        return false;
      }
      same = op;
    }
    assert isTrivialPhi();
    if (same == null) {
      // When doing if-simplification we remove blocks and we can end up with cyclic phis
      // of the form v1 = phi(v1, v1) in dead blocks. If we encounter that case we just
      // leave the phi in there and check at the end that there are no trivial phis.
      return false;
    }
    // Ensure that the value that replaces this phi is constrained to the type of this phi.
    if (builder != null && typeLattice.isPreciseType() && !typeLattice.isBottom()) {
      builder.constrainType(same, ValueTypeConstraint.fromTypeLattice(typeLattice));
    }
    if (affectedValues != null) {
      affectedValues.addAll(this.affectedValues());
    }
    // Removing this phi, so get rid of it as a phi user from all of the operands to avoid
    // recursively getting back here with the same phi. If the phi has itself as an operand
    // that also removes the self-reference.
    for (Value op : operands) {
      op.removePhiUser(this);
    }
    // If IR construction is taking place, update the definition users.
    if (definitionUsers != null) {
      for (Map<Integer, Value> user : definitionUsers) {
        for (Entry<Integer, Value> entry : user.entrySet()) {
          if (entry.getValue() == this) {
            entry.setValue(same);
            if (same.isPhi()) {
              same.asPhi().addDefinitionsUser(user);
            }
          }
        }
      }
    }
    {
      Set<Phi> phiUsersToSimplify = uniquePhiUsers();
      // Replace this phi with the unique value in all users.
      replaceUsers(same);
      // Try to simplify phi users that might now have become trivial.
      for (Phi user : phiUsersToSimplify) {
        user.removeTrivialPhi(builder, affectedValues);
      }
    }
    // Get rid of the phi itself.
    block.removePhi(this);
    return true;
  }

  public void removeDeadPhi() {
    // First, make sure it is indeed dead, i.e., no non-phi users.
    assert !hasUsers();
    // Then, manually clean up this from all of the operands.
    for (Value operand : getOperands()) {
      operand.removePhiUser(this);
    }
    // And remove it from the containing block.
    getBlock().removePhi(this);
  }

  public String printPhi() {
    StringBuilder builder = new StringBuilder();
    builder.append("v");
    builder.append(number);
    if (hasLocalInfo()) {
      builder.append("(").append(getLocalInfo()).append(")");
    }
    builder.append(" <- phi");
    StringUtils.append(builder, ListUtils.map(operands, Value::toString));
    builder.append(" : ").append(getTypeLattice());
    return builder.toString();
  }

  public void print(CfgPrinter printer) {
    int uses = numberOfPhiUsers() + numberOfUsers();
    printer
        .print("0 ")                 // bci
        .append(uses)                // use
        .append(" v").append(number) // tid
        .append(" Phi");
    for (Value operand : operands) {
      printer.append(" v").append(operand.number);
    }
  }

  public void addDefinitionsUser(Map<Integer, Value> currentDefinitions) {
    definitionUsers.add(currentDefinitions);
  }

  public void removeDefinitionsUser(Map<Integer, Value> currentDefinitions) {
    definitionUsers.remove(currentDefinitions);
  }

  public void clearDefinitionsUsers() {
    definitionUsers = null;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public boolean isValueOnStack() {
    assert verifyIsStackPhi(Sets.newIdentityHashSet());
    return isStackPhi;
  }

  public void setIsStackPhi(boolean isStackPhi) {
    this.isStackPhi = isStackPhi;
  }

  private boolean verifyIsStackPhi(Set<Phi> seenPhis) {
    seenPhis.add(this);
    operands.forEach(
        v -> {
          if (v.isPhi()) {
            assert seenPhis.contains(v) || v.asPhi().verifyIsStackPhi(seenPhis);
          } else {
            assert v.isValueOnStack() == isStackPhi;
          }
        });
    return true;
  }

  @Override
  public boolean needsRegister() {
    return !isValueOnStack();
  }

  public boolean usesValueOneTime(Value usedValue) {
    return operands.indexOf(usedValue) == operands.lastIndexOf(usedValue);
  }

  public DexType computeVerificationType(TypeVerificationHelper helper) {
    assert outType().isObject();
    Set<DexType> operandTypes = new HashSet<>(operands.size());
    for (Value operand : operands) {
      DexType operandType = helper.getDexType(operand);
      if (operandType != null) {
        operandTypes.add(operandType);
      }
    }
    return helper.join(operandTypes);
  }

  // Type of phi(v1, v2, ..., vn) is the least upper bound of all those n operands.
  public TypeLatticeElement computePhiType(AppView<?> appView) {
    TypeLatticeElement result = TypeLatticeElement.BOTTOM;
    for (Value operand : getOperands()) {
      result = result.join(operand.getTypeLattice(), appView);
    }
    return result;
  }

  @Override
  public TypeLatticeElement getDynamicUpperBoundType(
      AppView<? extends AppInfoWithSubtyping> appView) {
    Set<Phi> reachablePhis = SetUtils.newIdentityHashSet(this);
    Deque<Phi> worklist = DequeUtils.newArrayDeque(this);
    while (!worklist.isEmpty()) {
      Phi phi = worklist.removeFirst();
      assert reachablePhis.contains(phi);
      for (Value operand : phi.getOperands()) {
        Phi candidate = operand.getAliasedValue().asPhi();
        if (candidate != null && reachablePhis.add(candidate)) {
          worklist.addLast(candidate);
        }
      }
    }
    Set<Value> visitedOperands = Sets.newIdentityHashSet();
    TypeLatticeElement result = TypeLatticeElement.BOTTOM;
    for (Phi phi : reachablePhis) {
      for (Value operand : phi.getOperands()) {
        if (!operand.getAliasedValue().isPhi() && visitedOperands.add(operand)) {
          result = result.join(operand.getDynamicUpperBoundType(appView), appView);
        }
      }
    }
    return result;
  }
}
