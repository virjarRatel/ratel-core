// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf.code;

import com.android.tools.r8.cf.CfPrinter;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.CfSourceCode;
import com.android.tools.r8.ir.conversion.CfState;
import com.android.tools.r8.ir.conversion.CfState.Slot;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.naming.NamingLens;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CfStackInstruction extends CfInstruction {

  public enum Opcode {
    Pop(Opcodes.POP),
    Pop2(Opcodes.POP2),
    Dup(Opcodes.DUP),
    DupX1(Opcodes.DUP_X1),
    DupX2(Opcodes.DUP_X2),
    Dup2(Opcodes.DUP2),
    Dup2X1(Opcodes.DUP2_X1),
    Dup2X2(Opcodes.DUP2_X2),
    Swap(Opcodes.SWAP);

    private final int opcode;

    Opcode(int opcode) {
      this.opcode = opcode;
    }
  }

  private final Opcode opcode;

  public static CfStackInstruction fromAsm(int opcode) {
    switch (opcode) {
      case Opcodes.POP:
        return new CfStackInstruction(Opcode.Pop);
      case Opcodes.POP2:
        return new CfStackInstruction(Opcode.Pop2);
      case Opcodes.DUP:
        return new CfStackInstruction(Opcode.Dup);
      case Opcodes.DUP_X1:
        return new CfStackInstruction(Opcode.DupX1);
      case Opcodes.DUP_X2:
        return new CfStackInstruction(Opcode.DupX2);
      case Opcodes.DUP2:
        return new CfStackInstruction(Opcode.Dup2);
      case Opcodes.DUP2_X1:
        return new CfStackInstruction(Opcode.Dup2X1);
      case Opcodes.DUP2_X2:
        return new CfStackInstruction(Opcode.Dup2X2);
      case Opcodes.SWAP:
        return new CfStackInstruction(Opcode.Swap);
      default:
        throw new Unreachable("Invalid opcode for CfStackInstruction");
    }
  }

  public static CfStackInstruction popType(ValueType type) {
    return new CfStackInstruction(type.isWide() ? Opcode.Pop2 : Opcode.Pop);
  }

  public CfStackInstruction(Opcode opcode) {
    this.opcode = opcode;
  }

  @Override
  public void write(MethodVisitor visitor, NamingLens lens) {
    visitor.visitInsn(opcode.opcode);
  }

  @Override
  public void print(CfPrinter printer) {
    printer.print(this);
  }

  public Opcode getOpcode() {
    return opcode;
  }

  @Override
  public void buildIR(IRBuilder builder, CfState state, CfSourceCode code) {
    switch (opcode) {
      case Pop:
        {
          Slot pop = state.pop();
          assert !pop.type.isWide();
          break;
        }
      case Pop2:
        {
          Slot pop1 = state.pop();
          if (!pop1.type.isWide()) {
            Slot pop2 = state.pop();
            assert !pop2.type.isWide();
          }
          break;
        }
      case Dup:
        {
          Slot dupValue = state.peek();
          assert !dupValue.type.isWide();
          builder.addMove(dupValue.type, state.push(dupValue).register, dupValue.register);
          break;
        }
      case DupX1:
        {
          Slot value1 = state.pop();
          Slot value2 = state.pop();
          assert !value1.type.isWide();
          assert !value2.type.isWide();
          dup1x1(builder, state, value1, value2);
          break;
        }
      case DupX2:
        {
          Slot value1 = state.pop();
          Slot value2 = state.pop();
          assert !value1.type.isWide();
          if (value2.type.isWide()) {
            dup1x1(builder, state, value1, value2);
          } else {
            Slot value3 = state.pop();
            assert !value3.type.isWide();
            dup1x2(builder, state, value1, value2, value3);
          }
          break;
        }
      case Dup2:
        {
          Slot value1 = state.peek();
          if (value1.type.isWide()) {
            builder.addMove(value1.type, state.push(value1).register, value1.register);
          } else {
            Slot value2 = state.peek(1);
            builder.addMove(value2.type, state.push(value2).register, value2.register);
            builder.addMove(value1.type, state.push(value1).register, value1.register);
          }
          break;
        }
      case Dup2X1:
        {
          Slot value1 = state.pop();
          Slot value2 = state.pop();
          assert !value2.type.isWide();
          if (value1.type.isWide()) {
            dup1x1(builder, state, value1, value2);
          } else {
            Slot value3 = state.pop();
            assert !value3.type.isWide();
            dup2x1(builder, state, value1, value2, value3);
          }
          break;
        }
      case Dup2X2:
        {
          Slot value1 = state.pop();
          Slot value2 = state.pop();
          if (value1.type.isWide() && value2.type.isWide()) {
            // Input stack: ..., value2, value1
            // Output stack: ..., value1, value2, value1
            dup1x1(builder, state, value1, value2);
            break;
          }
          // Remaining valid cases are:
          // - single, single, wide
          // - wide, single, single
          // - single, single, single, single
          Slot value3 = state.pop();
          if (value1.type.isWide()) {
            if (value3.type.isWide()) {
              throw new CompilationError("Invalid dup2x2 with types: wide, single, wide");
            }
            // Input stack: ..., value3, value2, value1
            // Output stack: ..., value1, value3, value2, value1
            dup1x2(builder, state, value1, value2, value3);
            break;
          }
          if (value2.type.isWide()) {
            throw new CompilationError("Invalid dup2x2 with types: ..., wide, single");
          }
          if (value3.type.isWide()) {
            dup2x1(builder, state, value1, value2, value3);
            break;
          } else {
            Slot value4 = state.pop();
            if (value4.type.isWide()) {
              throw new CompilationError("Invalid dup2x2 with types: wide, single, single, single");
            }
            // Input stack: ..., value4, value3, value2, value1
            // Output stack: ..., value2, value1, value4, value3, value2, value1
            dup2x2(builder, state, value1, value2, value3, value4);
            break;
          }
        }
      case Swap:
        {
          Slot value1 = state.pop();
          Slot value2 = state.pop();
          assert !value1.type.isWide();
          assert !value2.type.isWide();
          // Input stack: ..., value2, value1
          dup1x1(builder, state, value1, value2);
          // Current stack: ..., value1, value2, value1copy
          state.pop();
          // Output stack: ..., value1, value2
          break;
        }
    }
  }

  // Dup top of stack across/X one other value (width independent).
  private void dup1x1(IRBuilder builder, CfState state, Slot inValue1, Slot inValue2) {
    // Input stack: ..., A:inValue2, B:inValue1 (values already popped)
    Slot outValue1 = state.push(inValue1);
    Slot outValue2 = state.push(inValue2);
    Slot outValue1Copy = state.push(inValue1);
    // Output stack: ..., A:outValue1, B:outValue2, C:outValue1Copy
    // Move C(outValue1Copy) <- B(inValue1)
    builder.addMove(inValue1.type, outValue1Copy.register, inValue1.register);
    // Move B(outValue2) <- A(inValue2)
    builder.addMove(inValue2.type, outValue2.register, inValue2.register);
    // Move A(outValue1) <- C(outValue1Copy)
    builder.addMove(outValue1Copy.type, outValue1.register, outValue1Copy.register);
  }

  // Dup top of stack across/X two other values.
  // Width independent, but the two cross values can only be single for valid CF inputs.
  private void dup1x2(
      IRBuilder builder, CfState state, Slot inValue1, Slot inValue2, Slot inValue3) {
    // Input stack: ..., A:inValue3, B:inValue2, C:inValue1 (values already popped)
    Slot outValue1 = state.push(inValue1);
    Slot outValue3 = state.push(inValue3);
    Slot outValue2 = state.push(inValue2);
    Slot outValue1Copy = state.push(inValue1);
    // Output stack: ..., A:outValue1, B:outValue3, C:outValue2, D:outValue1Copy
    // Move D(outValue1Copy) <- C(inValue1)
    builder.addMove(inValue1.type, outValue1Copy.register, inValue1.register);
    // Move C(outValue2) <- B(inValue2)
    builder.addMove(inValue2.type, outValue2.register, inValue2.register);
    // Move B(outValue3) <- A(inValue3)
    builder.addMove(inValue3.type, outValue3.register, inValue3.register);
    // Move A(outValue1) <- D(outValue1Copy)
    builder.addMove(outValue1Copy.type, outValue1.register, outValue1Copy.register);
  }

  // Dup top 2 elements of the stack across/X one other value (width independent).
  private void dup2x1(
      IRBuilder builder, CfState state, Slot inValue1, Slot inValue2, Slot inValue3) {
    // Input stack: ..., A:inValue3, B:inValue2, C:inValue1 (already popped).
    Slot outValue2 = state.push(inValue2);
    Slot outValue1 = state.push(inValue1);
    Slot outValue3 = state.push(inValue3);
    Slot outValue2Copy = state.push(inValue2);
    Slot outValue1Copy = state.push(inValue1);
    // Output: ..., A:outValue2, B:outValue1, C:outValue3, D:outValue2Copy, E:outValue1Copy
    // Move E(outValue1Copy) <- C(value1)
    builder.addMove(inValue1.type, outValue1Copy.register, inValue1.register);
    // Move D(outValue2Copy) <- B(value2)
    builder.addMove(inValue2.type, outValue2Copy.register, inValue2.register);
    // Move C(outValue3) <- A(value3)
    builder.addMove(inValue3.type, outValue3.register, inValue3.register);
    // Move B(outValue1) <- E(outValue1Copy)
    builder.addMove(inValue1.type, outValue1.register, outValue1Copy.register);
    // Move A(outValue2) <- D(outValue2Copy)
    builder.addMove(inValue2.type, outValue2.register, outValue2Copy.register);
  }

  // Dup top two elements of the stack across/X two other values.
  // Width independent, but the four values can only be single for valid CF inputs.
  private void dup2x2(
      IRBuilder builder,
      CfState state,
      Slot inValue1,
      Slot inValue2,
      Slot inValue3,
      Slot inValue4) {
    // Input stack: ..., A:inValue4, B:inValue3, C:inValue2, D:inValue1 (values already popped)
    Slot outValue2 = state.push(inValue2);
    Slot outValue1 = state.push(inValue1);
    Slot outValue4 = state.push(inValue4);
    Slot outValue3 = state.push(inValue3);
    Slot outValue2Copy = state.push(inValue2);
    Slot outValue1Copy = state.push(inValue1);
    // Output stack:
    // ..., A:outValue2, B:outValue1, C:outValue4, D:outValue3, E:outValue2Copy, F:outValue1Copy
    // Move F(outValue1Copy) <- D(inValue1)
    builder.addMove(inValue1.type, outValue1Copy.register, inValue1.register);
    // Move E(outValue2Copy) <- C(inValue2)
    builder.addMove(inValue2.type, outValue2Copy.register, inValue2.register);
    // Move D(outValue3) <- B(inValue3)
    builder.addMove(inValue3.type, outValue3.register, inValue3.register);
    // Move C(outValue4) <- A(inValue4)
    builder.addMove(inValue4.type, outValue4.register, inValue4.register);
    // Move B(outValue1) <- F(outValue1Copy)
    builder.addMove(outValue1Copy.type, outValue1.register, outValue1Copy.register);
    // Move A(outValue2) <- E(outValue2Copy)
    builder.addMove(outValue2Copy.type, outValue2.register, outValue2Copy.register);
  }

  @Override
  public boolean emitsIR() {
    return false;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints,
      DexType invocationContext,
      GraphLense graphLense,
      AppView<?> appView) {
    return ConstraintWithTarget.ALWAYS;
  }
}
