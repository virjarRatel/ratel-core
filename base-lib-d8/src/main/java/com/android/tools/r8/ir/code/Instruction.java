// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.LoadStoreHelper;
import com.android.tools.r8.cf.TypeVerificationHelper;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.AbstractError;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.AnalysisAssumption;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis.Query;
import com.android.tools.r8.ir.analysis.constant.Bottom;
import com.android.tools.r8.ir.analysis.constant.ConstRangeLatticeElement;
import com.android.tools.r8.ir.analysis.constant.LatticeElement;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.AbstractFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.EmptyFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.UnknownFieldSet;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.ir.code.Assume.DynamicTypeAssumption;
import com.android.tools.r8.ir.code.Assume.NoAssumption;
import com.android.tools.r8.ir.code.Assume.NonNullAssumption;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.StringUtils.BraceType;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class Instruction implements InstructionOrPhi, TypeAndLocalInfoSupplier {

  protected Value outValue = null;
  protected final List<Value> inValues = new ArrayList<>();
  private BasicBlock block = null;
  private int number = -1;
  private LinkedHashSet<Value> debugValues = null;
  private Position position = null;

  protected Instruction(Value outValue) {
    setOutValue(outValue);
  }

  protected Instruction(Value outValue, Value inValue) {
    addInValue(inValue);
    setOutValue(outValue);
  }

  protected Instruction(Value outValue, List<? extends Value> inValues) {
    if (inValues != null) {
      for (Value v : inValues) {
        addInValue(v);
      }
    }
    setOutValue(outValue);
  }

  public abstract int opcode();

  public abstract <T> T accept(InstructionVisitor<T> visitor);

  final boolean hasPosition() {
    return position != null;
  }

  public final Position getPosition() {
    assert position != null;
    return position;
  }

  public void setPosition(Position position) {
    assert this.position == null;
    this.position = position;
  }

  public String getPositionAsString() {
    return position == null ? "???" : position.toString();
  }

  public List<Value> inValues() {
    return inValues;
  }

  protected void addInValue(Value value) {
    if (value != null) {
      inValues.add(value);
      assert value.hasUsersInfo();
      if (value.hasUsersInfo()) {
        value.addUser(this);
      }
    }
  }

  public boolean hasInValueWithLocalInfo() {
    for (Value inValue : inValues()) {
      if (inValue.hasLocalInfo()) {
        return true;
      }
    }
    return false;
  }

  public boolean hasOutValue() {
    return outValue != null;
  }

  public Value outValue() {
    return outValue;
  }

  public void setOutValue(Value value) {
    assert outValue == null || !outValue.hasUsersInfo() || !outValue.isUsed();
    outValue = value;
    if (outValue != null) {
      outValue.definition = this;
    }
  }

  public Value swapOutValue(Value newOutValue) {
    Value oldOutValue = outValue;
    outValue = null;
    setOutValue(newOutValue);
    if (oldOutValue != null) {
      oldOutValue.definition = null;
    }
    return oldOutValue;
  }

  public AbstractValue getAbstractValue(AppView<?> appView, DexType context) {
    assert hasOutValue();
    return UnknownValue.getInstance();
  }

  @Override
  public TypeLatticeElement getTypeLattice() {
    if (hasOutValue()) {
      return outValue().getTypeLattice();
    }
    return null;
  }

  public void addDebugValue(Value value) {
    assert value.hasLocalInfo();
    if (debugValues == null) {
      debugValues = new LinkedHashSet<>();
    }
    if (debugValues.add(value)) {
      value.addDebugUser(this);
    }
  }

  public static void clearUserInfo(Instruction instruction) {
    if (instruction.outValue != null) {
      instruction.outValue.clearUsersInfo();
    }
    instruction.inValues.forEach(Value::clearUsersInfo);
    if (instruction.debugValues != null) {
      instruction.debugValues.forEach(Value::clearUsersInfo);
      instruction.debugValues = null;
    }
  }

  public final ValueType outType() {
    return outValue.outType();
  }

  public abstract void buildDex(DexBuilder builder);

  public abstract void buildCf(CfBuilder builder);

  public void replaceValue(Value oldValue, Value newValue) {
    for (int i = 0; i < inValues.size(); i++) {
      if (oldValue == inValues.get(i)) {
        inValues.set(i, newValue);
        newValue.addUser(this);
      }
    }
  }

  public void replaceValue(int index, Value newValue) {
    Value oldValue = inValues.get(index);
    inValues.set(index, newValue);
    newValue.addUser(this);
    oldValue.removeUser(this);
  }

  public void replaceDebugValue(Value oldValue, Value newValue) {
    if (debugValues.remove(oldValue)) {
      // TODO(mathiasr): Enable this assertion when BasicBlock has current position so trivial phi
      // removal can take local info into account.
      // assert newValue.getLocalInfo() == oldValue.getLocalInfo()
      //     : "Replacing debug values with inconsistent locals " +
      //       oldValue.getLocalInfo() + " and " + newValue.getLocalInfo() +
      //       ". This is likely a code transformation bug " +
      //       "that has not taken local information into account";
      if (newValue.hasLocalInfo()) {
        addDebugValue(newValue);
      }
    }
  }

  public void moveDebugValues(Instruction target) {
    if (debugValues == null) {
      return;
    }
    for (Value value : debugValues) {
      value.replaceDebugUser(this, target);
    }
    debugValues.clear();
  }

  public void moveDebugValue(Value value, Instruction target) {
    assert debugValues.contains(value);
    value.replaceDebugUser(this, target);
    debugValues.remove(value);
  }

  public void removeDebugValue(Value value) {
    assert value.hasLocalInfo();
    if (debugValues != null) {
      assert debugValues.contains(value);
      if (debugValues.remove(value)) {
        value.removeDebugUser(this);
      }
      return;
    }
    assert false;
  }

  public Value removeDebugValue(DebugLocalInfo localInfo) {
    if (debugValues != null) {
      Iterator<Value> it = debugValues.iterator();
      while (it.hasNext()) {
        Value value = it.next();
        if (value.hasLocalInfo() && value.getLocalInfo() == localInfo) {
          it.remove();
          value.removeDebugUser(this);
          return value;
        }
      }
    }
    return null;
  }

  public void clearDebugValues() {
    if (debugValues != null) {
      for (Value debugValue : debugValues) {
        debugValue.removeDebugUser(this);
      }
      debugValues.clear();
    }
  }

  /**
   * Returns the basic block containing this instruction.
   */
  @Override
  public BasicBlock getBlock() {
    assert block != null;
    return block;
  }

  /**
   * Set the basic block of this instruction. See IRBuilder.
   */
  public void setBlock(BasicBlock block) {
    assert block != null;
    this.block = block;
  }

  /**
   * Clear the basic block of this instruction. Use when removing an instruction from a block.
   */
  public void clearBlock() {
    assert block != null;
    block = null;
  }

  public void removeOrReplaceByDebugLocalRead(IRCode code) {
    getBlock().listIterator(code, this).removeOrReplaceByDebugLocalRead();
  }

  public void replace(Instruction newInstruction, IRCode code) {
    getBlock().listIterator(code, this).replaceCurrentInstruction(newInstruction);
  }

  /**
   * Returns true if the instruction is in the IR and therefore has a block.
   */
  public boolean hasBlock() {
    return block != null;
  }

  public String getInstructionName() {
    return getClass().getSimpleName();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getInstructionName());
    for (int i = builder.length(); i < 20; i++) {
      builder.append(" ");
    }
    builder.append(" ");
    if (outValue != null) {
      builder.append(outValue);
      builder.append(" <- ");
    }
    if (!inValues.isEmpty()) {
      StringUtils.append(builder, inValues, ", ", BraceType.NONE);
    }
    return builder.toString();
  }

  public void print(CfgPrinter printer) {
    int uses = 0;
    String value;
    if (outValue == null) {
      value = printer.makeUnusedValue();
    } else {
      if (outValue.hasUsersInfo()) {
        uses = outValue.uniqueUsers().size() + outValue.uniquePhiUsers().size();
      }
      value = "v" + outValue.getNumber();
    }
    printer
        .print(0)           // bci
        .sp().append(uses)  // use
        .sp().append(value) // tid
        .sp().append(getClass().getSimpleName());
    for (Value in : inValues) {
      printer.append(" v").append(in.getNumber());
    }
  }

  public void printLIR(CfgPrinter printer) {
    // TODO(ager): Improve the instruction printing. Use different name for values so that the
    // HIR and LIR values are not confused in the c1 visualizer.
    printer.print(number).sp().append(toString());
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    assert number != -1;
    this.number = number;
  }

  public void clearNumber() {
    this.number = -1;
  }

  /**
   * Compare equality of two class-equivalent instructions modulo their values and positions.
   */
  public abstract boolean identicalNonValueNonPositionParts(Instruction other);

  public boolean identicalNonValueParts(Instruction other) {
    assert getClass() == other.getClass();
    return position.equals(other.position) && identicalNonValueNonPositionParts(other);
  }

  private boolean identicalInputAfterRegisterAllocation(
      Value a,
      int aInstrNumber,
      Instruction bInstr,
      Value b,
      int bInstrNumber,
      RegisterAllocator allocator) {
    boolean aIsStackValue = a instanceof StackValue;
    boolean aIsStackValues = a instanceof StackValues;

    if (aIsStackValue != b instanceof StackValue || aIsStackValues != b instanceof StackValues) {
      return false;
    }

    if (aIsStackValue) {
      return identicalStackValuePair((StackValue) a, (StackValue) b);
    }

    if (aIsStackValues) {
      return identicalStackValuesPair((StackValues) a, (StackValues) b);
    }

    if (needsValueInRegister(a) != bInstr.needsValueInRegister(b)) {
      return false;
    }

    // If the value is needed in a register or one of the instructions is a two-addr instruction,
    // the register for the value is used and it needs to be the same.
    if (needsValueInRegister(a) || isTwoAddr(allocator) || bInstr.isTwoAddr(allocator)) {
      // Only one of the instructions have a register assigned and one of them will use a
      // register, so the instructions are not identical.
      if (!a.needsRegister() || !b.needsRegister()) {
        return false;
      }
      // Check if the allocated registers are identical.
      if (allocator.getRegisterForValue(a, aInstrNumber)
          != allocator.getRegisterForValue(b, bInstrNumber)) {
        return false;
      }
    } else {
      ConstNumber aNum = a.getConstInstruction().asConstNumber();
      ConstNumber bNum = b.getConstInstruction().asConstNumber();
      if (!aNum.identicalNonValueNonPositionParts(bNum)) {
        return false;
      }
    }

    return a.outType() == b.outType();
  }

  private boolean identicalOutputAfterRegisterAllocation(
      Value a, int aInstrNumber, Value b, int bInstrNumber, RegisterAllocator allocator) {
    boolean aIsStackValue = a instanceof StackValue;
    boolean aIsStackValues = a instanceof StackValues;

    if (aIsStackValue != b instanceof StackValue || aIsStackValues != b instanceof StackValues) {
      return false;
    }

    if (aIsStackValue) {
      return identicalStackValuePair((StackValue) a, (StackValue) b);
    }

    if (aIsStackValues) {
      return identicalStackValuesPair((StackValues) a, (StackValues) b);
    }

    if (a.needsRegister() != b.needsRegister()) {
      return false;
    }

    if (a.needsRegister()) {
      if (allocator.getRegisterForValue(a, aInstrNumber)
          != allocator.getRegisterForValue(b, bInstrNumber)) {
        return false;
      }
    } else {
      ConstNumber aNum = a.getConstInstruction().asConstNumber();
      ConstNumber bNum = b.getConstInstruction().asConstNumber();
      if (!aNum.identicalNonValueNonPositionParts(bNum)) {
        return false;
      }
    }

    return a.outType() == b.outType();
  }

  public boolean identicalAfterRegisterAllocation(Instruction other, RegisterAllocator allocator) {
    if (other.getClass() != getClass()) {
      return false;
    }
    // In debug mode or if the instruction can throw we must account for positions, in release mode
    // we do want to share non-throwing instructions even if their positions differ.
    if (instructionTypeCanThrow() || allocator.options().debug) {
      if (!identicalNonValueParts(other)) {
        return false;
      }
    } else if (!identicalNonValueNonPositionParts(other)) {
      return false;
    }
    if (isInvokeDirect() && !asInvokeDirect().sameConstructorReceiverValue(other.asInvoke())) {
      return false;
    }
    if (outValue != null) {
      if (other.outValue == null) {
        return false;
      }
      if (!identicalOutputAfterRegisterAllocation(
          outValue, getNumber(), other.outValue, other.getNumber(), allocator)) {
        return false;
      }
    } else if (other.outValue != null) {
      return false;
    }
    // Check that all input values have the same type and allocated registers.
    if (inValues.size() != other.inValues.size()) {
      return false;
    }
    for (int j = 0; j < inValues.size(); j++) {
      Value in0 = inValues.get(j);
      Value in1 = other.inValues.get(j);
      if (!identicalInputAfterRegisterAllocation(in0, getNumber(), other, in1, other.getNumber(),
          allocator)) {
        return false;
      }
    }
    // Finally check that the dex instructions for the generated code actually are the same.
    if (allocator.options().isGeneratingDex()
        && !DexBuilder.identicalInstructionsAfterBuildingDexCode(this, other, allocator)) {
      return false;
    }
    return true;
  }

  private boolean identicalStackValuePair(StackValue a, StackValue b) {
    return a.getHeight() == b.getHeight() && a.outType() == b.outType();
  }

  private boolean identicalStackValuesPair(StackValues a, StackValues b) {
    StackValue[] aStackValues = a.getStackValues();
    StackValue[] bStackValues = b.getStackValues();
    if (aStackValues.length != bStackValues.length) {
      return false;
    }
    for (int i = 0; i < aStackValues.length; ++i) {
      if (!identicalStackValuePair(aStackValues[i], bStackValues[i])) {
        return false;
      }
    }
    return true;
  }

  public boolean isAllowedAfterThrowingInstruction() {
    return false;
  }

  public boolean instructionTypeCanBeCanonicalized() {
    return false;
  }

  /**
   * Returns true if this instruction may throw an exception.
   */
  public boolean instructionTypeCanThrow() {
    return false;
  }

  public boolean instructionInstanceCanThrow() {
    return instructionTypeCanThrow();
  }

  public boolean instructionMayHaveSideEffects(AppView<?> appView, DexType context) {
    return instructionInstanceCanThrow();
  }

  /**
   * Returns true if this instruction may trigger another method to execute either directly or
   * indirectly (e.g., via class initialization).
   */
  public abstract boolean instructionMayTriggerMethodInvocation(
      AppView<?> appView, DexType context);

  public AbstractError instructionInstanceCanThrow(AppView<?> appView, DexType context) {
    return instructionInstanceCanThrow() ? AbstractError.top() : AbstractError.bottom();
  }

  /** Returns true is this instruction can be treated as dead code if its outputs are not used. */
  public boolean canBeDeadCode(AppView<?> appView, IRCode code) {
    // TODO(b/129530569): instructions with fine-grained side effect analysis may use:
    // return !instructionMayHaveSideEffects(appView, code.method.method.holder);
    return !instructionInstanceCanThrow();
  }

  /**
   * Returns an abstraction of the set of fields that may possibly be read as a result of executing
   * this instruction.
   */
  public AbstractFieldSet readSet(AppView<?> appView, DexType context) {
    if (instructionMayTriggerMethodInvocation(appView, context)
        && instructionMayHaveSideEffects(appView, context)) {
      return UnknownFieldSet.getInstance();
    }
    return EmptyFieldSet.getInstance();
  }

  /**
   * Returns true if this instruction need this value in a register.
   */
  public boolean needsValueInRegister(Value value) {
    return true;
  }

  public boolean isTwoAddr(RegisterAllocator allocator) {
    return false;
  }

  /**
   * Returns true if the out value of this instruction is a constant.
   *
   * @return whether the out value of this instruction is a constant.
   */
  public boolean isOutConstant() {
    return false;
  }

  /**
   * Returns the ConstInstruction defining the constant out value if the out value is constant.
   *
   * @return ConstInstruction or null.
   */
  public ConstInstruction getOutConstantConstInstruction() {
    return null;
  }

  public abstract int maxInValueRegister();

  public abstract int maxOutValueRegister();

  @Override
  public DebugLocalInfo getLocalInfo() {
    return outValue == null ? null : outValue.getLocalInfo();
  }

  public Set<Value> getDebugValues() {
    return debugValues != null ? debugValues : ImmutableSet.of();
  }

  @Override
  public boolean isInstruction() {
    return true;
  }

  @Override
  public Instruction asInstruction() {
    return this;
  }

  public boolean isArrayGet() {
    return false;
  }

  public ArrayGet asArrayGet() {
    return null;
  }

  public boolean isArrayLength() {
    return false;
  }

  public ArrayLength asArrayLength() {
    return null;
  }

  public boolean isArrayPut() {
    return false;
  }

  public ArrayPut asArrayPut() {
    return null;
  }

  public boolean isArgument() {
    return false;
  }

  public Argument asArgument() {
    return null;
  }

  public boolean isArithmeticBinop() {
    return false;
  }

  public ArithmeticBinop asArithmeticBinop() {
    return null;
  }

  public boolean isAssume() {
    return false;
  }

  public Assume<?> asAssume() {
    return null;
  }

  public boolean isAssumeNone() {
    return false;
  }

  public Assume<NoAssumption> asAssumeNone() {
    return null;
  }

  public boolean isAssumeDynamicType() {
    return false;
  }

  public Assume<DynamicTypeAssumption> asAssumeDynamicType() {
    return null;
  }

  public boolean isAssumeNonNull() {
    return false;
  }

  public Assume<NonNullAssumption> asAssumeNonNull() {
    return null;
  }

  public boolean isBinop() {
    return false;
  }

  public Binop asBinop() {
    return null;
  }

  public boolean isUnop() {
    return false;
  }

  public Unop asUnop() {
    return null;
  }

  public boolean isCheckCast() {
    return false;
  }

  public CheckCast asCheckCast() {
    return null;
  }

  public boolean isConstNumber() {
    return false;
  }

  public ConstNumber asConstNumber() {
    return null;
  }

  public boolean isConstInstruction() {
    return false;
  }

  public ConstInstruction asConstInstruction() {
    return null;
  }

  public boolean isConstClass() {
    return false;
  }

  public ConstClass asConstClass() {
    return null;
  }

  public boolean isConstMethodHandle() {
    return false;
  }

  public ConstMethodHandle asConstMethodHandle() {
    return null;
  }

  public boolean isConstMethodType() {
    return false;
  }

  public ConstMethodType asConstMethodType() {
    return null;
  }

  public boolean isConstString() {
    return false;
  }

  public ConstString asConstString() {
    return null;
  }

  public boolean isDexItemBasedConstString() {
    return false;
  }

  public DexItemBasedConstString asDexItemBasedConstString() {
    return null;
  }

  public boolean isCmp() {
    return false;
  }

  public Cmp asCmp() {
    return null;
  }

  public boolean isDup() {
    return false;
  }

  public Dup asDup() {
    return null;
  }

  public boolean isDup2() {
    return false;
  }

  public Dup2 asDup2() {
    return null;
  }

  public boolean isJumpInstruction() {
    return false;
  }

  public JumpInstruction asJumpInstruction() {
    return null;
  }

  public boolean isFieldInstruction() {
    return false;
  }

  public FieldInstruction asFieldInstruction() {
    return null;
  }

  public boolean isGoto() {
    return false;
  }

  public Goto asGoto() {
    return null;
  }

  public boolean isIf() {
    return false;
  }

  public If asIf() {
    return null;
  }

  public boolean isSwitch() {
    return false;
  }

  public Switch asSwitch() {
    return null;
  }

  public boolean isIntSwitch() {
    return false;
  }

  public IntSwitch asIntSwitch() {
    return null;
  }

  public boolean isStringSwitch() {
    return false;
  }

  public StringSwitch asStringSwitch() {
    return null;
  }

  public boolean isInstanceGet() {
    return false;
  }

  public InstanceGet asInstanceGet() {
    return null;
  }

  public boolean isInstanceOf() {
    return false;
  }

  public InstanceOf asInstanceOf() {
    return null;
  }

  public final boolean isFieldGet() {
    return isInstanceGet() || isStaticGet();
  }

  public final boolean isFieldPut() {
    return isInstancePut() || isStaticPut();
  }

  public boolean isInstancePut() {
    return false;
  }

  public InstancePut asInstancePut() {
    return null;
  }

  public boolean isInvoke() {
    return false;
  }

  public Invoke asInvoke() {
    return null;
  }

  public boolean isMonitor() {
    return false;
  }

  public boolean isMonitorEnter() {
    return false;
  }

  public Monitor asMonitor() {
    return null;
  }

  public boolean isMove() {
    return false;
  }

  public Move asMove() {
    return null;
  }

  public boolean isNewArrayEmpty() {
    return false;
  }

  public NewArrayEmpty asNewArrayEmpty() {
    return null;
  }

  public boolean isNewArrayFilledData() {
    return false;
  }

  public NewArrayFilledData asNewArrayFilledData() {
    return null;
  }

  public boolean isNeg() {
    return false;
  }

  public Neg asNeg() {
    return null;
  }

  public boolean isNewInstance() {
    return false;
  }

  public NewInstance asNewInstance() {
    return null;
  }

  public boolean isNot() {
    return false;
  }

  public Not asNot() {
    return null;
  }

  public boolean isNumberConversion() {
    return false;
  }

  public NumberConversion asNumberConversion() {
    return null;
  }

  public boolean isReturn() {
    return false;
  }

  public Return asReturn() {
    return null;
  }

  public boolean isThrow() {
    return false;
  }

  public Throw asThrow() {
    return null;
  }

  public boolean isStaticGet() {
    return false;
  }

  public StaticGet asStaticGet() {
    return null;
  }

  public boolean isStaticPut() {
    return false;
  }

  public StaticPut asStaticPut() {
    return null;
  }

  public boolean isAdd() {
    return false;
  }

  public Add asAdd() {
    return null;
  }

  public boolean isSub() {
    return false;
  }

  public Sub asSub() {
    return null;
  }

  public boolean isMul() {
    return false;
  }

  public Mul asMul() {
    return null;
  }

  public boolean isDiv() {
    return false;
  }

  public Div asDiv() {
    return null;
  }

  public boolean isRem() {
    return false;
  }

  public Rem asRem() {
    return null;
  }

  public boolean isLogicalBinop() {
    return false;
  }

  public LogicalBinop asLogicalBinop() {
    return null;
  }

  public boolean isShl() {
    return false;
  }

  public Shl asShl() {
    return null;
  }

  public boolean isShr() {
    return false;
  }

  public Shr asShr() {
    return null;
  }

  public boolean isUshr() {
    return false;
  }

  public Ushr asUshr() {
    return null;
  }

  public boolean isAnd() {
    return false;
  }

  public And asAnd() {
    return null;
  }

  public boolean isOr() {
    return false;
  }

  public Or asOr() {
    return null;
  }

  public boolean isXor() {
    return false;
  }

  public Xor asXor() {
    return null;
  }

  public boolean isMoveException() {
    return false;
  }

  public MoveException asMoveException() {
    return null;
  }

  public boolean isDebugInstruction() {
    return isDebugPosition()
        || isDebugLocalsChange()
        || isDebugLocalRead()
        || isDebugLocalWrite()
        || isDebugLocalUninitialized();
  }

  public boolean isDebugPosition() {
    return false;
  }

  public DebugPosition asDebugPosition() {
    return null;
  }

  public boolean isDebugLocalsChange() {
    return false;
  }

  public DebugLocalsChange asDebugLocalsChange() {
    return null;
  }

  public boolean isDebugLocalUninitialized() {
    return false;
  }

  public DebugLocalUninitialized asDebugLocalUninitialized() {
    return null;
  }

  public boolean isDebugLocalWrite() {
    return false;
  }

  public DebugLocalWrite asDebugLocalWrite() {
    return null;
  }

  public boolean isInvokeMethodWithDynamicDispatch() {
    return isInvokeInterface() || isInvokeVirtual();
  }

  public boolean isInvokeMethod() {
    return false;
  }

  public InvokeMethod asInvokeMethod() {
    return null;
  }

  public boolean isInvokeMethodWithReceiver() {
    return false;
  }

  public InvokeMethodWithReceiver asInvokeMethodWithReceiver() {
    return null;
  }

  public boolean isInvokeNewArray() {
    return false;
  }

  public InvokeNewArray asInvokeNewArray() {
    return null;
  }

  public boolean isInvokeMultiNewArray() {
    return false;
  }

  public InvokeMultiNewArray asInvokeMultiNewArray() {
    return null;
  }

  public boolean isInvokeCustom() {
    return false;
  }

  public InvokeCustom asInvokeCustom() {
    return null;
  }

  public boolean isInvokeDirect() {
    return false;
  }

  public InvokeDirect asInvokeDirect() {
    return null;
  }

  public boolean isInvokeInterface() {
    return false;
  }

  public InvokeInterface asInvokeInterface() {
    return null;
  }

  public boolean isInvokeStatic() {
    return false;
  }

  public InvokeStatic asInvokeStatic() {
    return null;
  }

  public boolean isInvokeSuper() {
    return false;
  }

  public InvokeSuper asInvokeSuper() {
    return null;
  }

  public boolean isInvokeVirtual() {
    return false;
  }

  public InvokeVirtual asInvokeVirtual() {
    return null;
  }

  public boolean isInvokePolymorphic() {
    return false;
  }

  public InvokePolymorphic asInvokePolymorphic() {
    return null;
  }

  public boolean isDebugLocalRead() {
    return false;
  }

  public DebugLocalRead asDebugLocalRead() {
    return null;
  }

  public boolean isPop() {
    return false;
  }

  public Pop asPop() {
    return null;
  }

  public boolean isStore() {
    return false;
  }

  public Store asStore() {
    return null;
  }

  public boolean isSwap() {
    return false;
  }

  public Swap asSwap() {
    return null;
  }

  public boolean isLoad() {
    return false;
  }

  public Load asLoad() {
    return null;
  }

  public boolean canBeFolded() {
    return false;
  }

  /** Returns true if the out-value could be an alias of an in-value.
   *
   * <p>This is a conservative version of {@link #isIntroducingAnAlias()} so that other analyses,
   * e.g., escape analysis, can propagate or track aliased values in a conservative manner.
   */
  public boolean couldIntroduceAnAlias(AppView<?> appView, Value root) {
    return false;
  }

  /** Returns true if the out-value is an alias of an in-value. */
  public boolean isIntroducingAnAlias() {
    return false;
  }

  /** Returns the in-value that is an alias for the out-value. */
  public Value getAliasForOutValue() {
    assert isIntroducingAnAlias();
    return null;
  }

  public boolean isCreatingArray() {
    return isNewArrayEmpty()
        || isNewArrayFilledData()
        || isInvokeNewArray()
        || isInvokeMultiNewArray();
  }

  public boolean isCreatingInstanceOrArray() {
    return isNewInstance() || isCreatingArray();
  }

  /**
   * Returns the inlining constraint for this method when used in the context of the given type.
   *
   * <p>The type is used to judge visibility constraints and also for dispatch decisions.
   */
  public abstract ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext);

  public abstract void insertLoadAndStores(InstructionListIterator it, LoadStoreHelper helper);

  public DexType computeVerificationType(AppView<?> appView, TypeVerificationHelper helper) {
    assert outValue == null || !outValue.getTypeLattice().isReference();
    throw new Unreachable("Instruction without object outValue cannot compute verification type");
  }

  public abstract boolean hasInvariantOutType();

  public LatticeElement evaluate(IRCode code, Function<Value, LatticeElement> getLatticeElement) {
    if (outValue.hasValueRange()) {
      return new ConstRangeLatticeElement(outValue);
    }
    return Bottom.getInstance();
  }

  // TODO(b/72693244): maybe rename to computeOutType once TypeVerificationHelper is gone?
  public TypeLatticeElement evaluate(AppView<?> appView) {
    assert outValue == null;
    throw new Unimplemented(
        "Implement type lattice evaluation for: " + getInstructionName());
  }

  public boolean verifyTypes(AppView<?> appView) {
    // TODO(b/72693244): for instructions with invariant out type, we can verify type directly here.
    if (outValue != null) {
      TypeLatticeElement outTypeLatticeElement = outValue.getTypeLattice();
      if (outTypeLatticeElement.isArrayType()) {
        DexType outBaseType =
            outTypeLatticeElement
                .asArrayTypeLatticeElement()
                .getArrayType(appView.dexItemFactory())
                .toBaseType(appView.dexItemFactory());
        assert appView.graphLense().lookupType(outBaseType) == outBaseType;
      } else if (outTypeLatticeElement.isClassType()) {
        DexType outType = outTypeLatticeElement.asClassTypeLatticeElement().getClassType();
        assert appView.graphLense().lookupType(outType) == outType;
      }
    }
    return true;
  }

  /**
   * Indicates whether the instruction throws a NullPointerException if the object denoted by the
   * given value is null at runtime execution.
   *
   * @param value the value representing an object that may be null at runtime execution.
   * @param dexItemFactory where pre-defined descriptors are retrieved
   * @return true if the instruction throws NullPointerException if value is null at runtime, false
   *     otherwise.
   */
  public boolean throwsNpeIfValueIsNull(Value value, DexItemFactory dexItemFactory) {
    return false;
  }

  // Any instructions that override `throwsNpeIfValueIsNull` should override this too.
  // `throw` instruction is also throwing if the input is null. However, this util and the below
  // one are used to insert Assume instruction if input is known to be non-null or replace the
  // instruction with `throw null`. In either case, nullability of `throw` does not add anything
  // new: `throw X` with null reference X has the same effect as `throw null`; `throw X` with
  // non-null reference X ends, hence having non-null X after that instruction is non-sense.
  public boolean throwsOnNullInput() {
    return false;
  }

  // Any instructions that override `throwsOnNullInput` should override this too.
  public Value getNonNullInput() {
    throw new Unreachable("Should conform to throwsOnNullInput.");
  }

  /**
   * Indicates whether the instruction triggers the class initialization (i.e. the <clinit> method)
   * of the given class at runtime execution.
   *
   * @return true if the instruction triggers intialization of the class at runtime, false
   *     otherwise.
   */
  public boolean definitelyTriggersClassInitialization(
      DexType clazz,
      DexType context,
      AppView<?> appView,
      Query mode,
      AnalysisAssumption assumption) {
    return false;
  }

  public boolean verifyValidPositionInfo(boolean debug) {
    assert position != null;
    assert !debug || getPosition().isSome();
    assert !instructionTypeCanThrow()
        || isConstString()
        || isDexItemBasedConstString()
        || getPosition().isSome()
        || getPosition().isSyntheticNone();
    return true;
  }

  public boolean outTypeKnownToBeBoolean(Set<Phi> seen) {
    return false;
  }
}
