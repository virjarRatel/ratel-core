// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Throw;

/**
 * Utility to determine if a given IR code object type checks.
 *
 * <p>NOTE: This is incomplete! The primary motivation for type checking the code is to be able to
 * prune code that does not type check in R8. Such code does not verify, and we therefore assume
 * that it is dead.
 *
 * <p>Pruning code that does not verify is necessary in order to be able to assert that the types
 * are sound using {@link Instruction#verifyTypes(AppView)}.
 */
public class TypeChecker {

  private final AppView<? extends AppInfoWithSubtyping> appView;

  public TypeChecker(AppView<? extends AppInfoWithSubtyping> appView) {
    this.appView = appView;
  }

  public boolean check(IRCode code) {
    for (Instruction instruction : code.instructions()) {
      if (instruction.isInstancePut()) {
        if (!check(instruction.asInstancePut())) {
          return false;
        }
      } else if (instruction.isReturn()) {
        if (!check(instruction.asReturn(), code.method)) {
          return false;
        }
      } else if (instruction.isStaticPut()) {
        if (!check(instruction.asStaticPut())) {
          return false;
        }
      } else if (instruction.isThrow()) {
        if (!check(instruction.asThrow())) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean check(InstancePut instruction) {
    return checkFieldPut(instruction);
  }

  public boolean check(Return instruction, DexEncodedMethod method) {
    if (instruction.isReturnVoid()) {
      return true;
    }
    TypeLatticeElement valueType = instruction.returnValue().getTypeLattice();
    TypeLatticeElement returnType =
        TypeLatticeElement.fromDexType(
            method.method.proto.returnType, Nullability.maybeNull(), appView);
    if (isSubtypeOf(valueType, returnType)) {
      return true;
    }

    if (returnType.isClassType() && valueType.isReference()) {
      // Interface types are treated like Object according to the JVM spec.
      // https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.10.1.2-100
      DexClass clazz = appView.definitionFor(method.method.proto.returnType);
      return clazz != null && clazz.isInterface();
    }

    return false;
  }

  public boolean check(StaticPut instruction) {
    return checkFieldPut(instruction);
  }

  public boolean checkFieldPut(FieldInstruction instruction) {
    assert instruction.isFieldPut();
    TypeLatticeElement valueType = instruction.value().getTypeLattice();
    TypeLatticeElement fieldType =
        TypeLatticeElement.fromDexType(
            instruction.getField().type, valueType.nullability(), appView);
    if (isSubtypeOf(valueType, fieldType)) {
      return true;
    }

    if (fieldType.isClassType() && valueType.isReference()) {
      // Interface types are treated like Object according to the JVM spec.
      // https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.10.1.2-100
      DexClass clazz = appView.definitionFor(instruction.getField().type);
      return clazz != null && clazz.isInterface();
    }

    return false;
  }

  public boolean check(Throw instruction) {
    TypeLatticeElement valueType = instruction.exception().getTypeLattice();
    TypeLatticeElement throwableType =
        TypeLatticeElement.fromDexType(
            appView.dexItemFactory().throwableType, valueType.nullability(), appView);
    return isSubtypeOf(valueType, throwableType);
  }

  private boolean isSubtypeOf(
      TypeLatticeElement expectedSubtype, TypeLatticeElement expectedSupertype) {
    return (expectedSubtype.isNullType() && expectedSupertype.isReference())
        || expectedSubtype.lessThanOrEqual(expectedSupertype, appView)
        || expectedSubtype.isBasedOnMissingClass(appView);
  }
}
