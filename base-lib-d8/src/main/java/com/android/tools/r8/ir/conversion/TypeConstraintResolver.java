// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.analysis.type.ArrayTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.ImpreciseMemberTypeInstruction;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueTypeConstraint;
import com.android.tools.r8.position.MethodPosition;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Type constraint resolver that ensures that all SSA values have a "precise" type, ie, every value
 * must be an element of exactly one of: object, int, float, long or double.
 *
 * <p>The resolution is a union-find over the SSA values, linking any type with an imprecise type to
 * a parent value that has either the same imprecise type or a precise one. SSA values are linked if
 * there type is constrained to be the same. This happens in two places:
 *
 * <ul>
 *   <li>For phis, the out value and all operand values must have the same type.
 *   <li>For if-{eq,ne} instructions, the input values must have the same type.
 *   <li>For array-{get,put} instructions, the array value must have member type compatible with the
 *       type of output/input value.
 * </ul>
 *
 * <p>All other constraints on types have been computed during IR construction where every call to
 * readRegister(ValueTypeConstraint) will constrain the type of the SSA value that the read resolves
 * to.
 */
public class TypeConstraintResolver {

  private final AppView<?> appView;
  private final IRBuilder builder;
  private final Map<Value, Value> unificationParents = new HashMap<>();

  public TypeConstraintResolver(AppView<?> appView, IRBuilder builder) {
    this.appView = appView;
    this.builder = builder;
  }

  public static ValueTypeConstraint constraintForType(TypeLatticeElement type) {
    // During constraint resolution the type bottom denotes references of not-yet-computed types.
    return type.isBottom() ? ValueTypeConstraint.OBJECT : ValueTypeConstraint.fromTypeLattice(type);
  }

  public static TypeLatticeElement typeForConstraint(ValueTypeConstraint constraint) {
    switch (constraint) {
      case INT_OR_FLOAT_OR_OBJECT:
        return TypeLatticeElement.TOP;
      case OBJECT:
        // If the constraint is object the concrete lattice type will need to be computed.
        // We mark the object type as bottom for now, with the implication that it is of type
        // reference but that it should not contribute to the computation of its join
        // (in potentially self-referencing phis).
        return TypeLatticeElement.BOTTOM;
      case INT:
        return TypeLatticeElement.INT;
      case FLOAT:
        return TypeLatticeElement.FLOAT;
      case INT_OR_FLOAT:
        return TypeLatticeElement.SINGLE;
      case LONG:
        return TypeLatticeElement.LONG;
      case DOUBLE:
        return TypeLatticeElement.DOUBLE;
      case LONG_OR_DOUBLE:
        return TypeLatticeElement.WIDE;
      default:
        throw new Unreachable("Unexpected constraint type: " + constraint);
    }
  }

  public void resolve(
      List<ImpreciseMemberTypeInstruction> impreciseInstructions,
      IRCode code,
      DexEncodedMethod method,
      DexEncodedMethod context) {
    // Round one will resolve at least all object vs single types.
    List<Value> remainingImpreciseValues = resolveRoundOne(code);
    // Round two will resolve any remaining single and wide types. These can depend on the types
    // of array instructions, thus we need to complete the type fixed point prior to resolving.
    new TypeAnalysis(appView, true).widening(context, method, code);
    // Round two resolves any remaining imprecision and finally selects a final precise type for
    // any unconstrained imprecise type.
    resolveRoundTwo(code, impreciseInstructions, remainingImpreciseValues);
  }

  private List<Value> resolveRoundOne(IRCode code) {
    List<Value> impreciseValues = new ArrayList<>();
    for (BasicBlock block : code.blocks) {
      for (Phi phi : block.getPhis()) {
        if (!phi.getTypeLattice().isPreciseType()) {
          impreciseValues.add(phi);
        }
        for (Value value : phi.getOperands()) {
          merge(phi, value);
        }
      }
      for (Instruction instruction : block.getInstructions()) {
        if (instruction.outValue() != null
            && !instruction.outValue().getTypeLattice().isPreciseType()) {
          impreciseValues.add(instruction.outValue());
        }

        if (instruction.isIf() && instruction.inValues().size() == 2) {
          If ifInstruction = instruction.asIf();
          assert !ifInstruction.isZeroTest();
          If.Type type = ifInstruction.getType();
          if (type == If.Type.EQ || type == If.Type.NE) {
            merge(ifInstruction.inValues().get(0), ifInstruction.inValues().get(1));
          }
        }
      }
    }
    return constrainValues(false, impreciseValues);
  }

  private void resolveRoundTwo(
      IRCode code,
      List<ImpreciseMemberTypeInstruction> impreciseInstructions,
      List<Value> remainingImpreciseValues) {
    if (impreciseInstructions != null) {
      for (ImpreciseMemberTypeInstruction impreciseInstruction : impreciseInstructions) {
        impreciseInstruction.constrainType(this);
      }
    }
    ArrayList<Value> stillImprecise = constrainValues(true, remainingImpreciseValues);
    if (!stillImprecise.isEmpty()) {
      throw appView
          .options()
          .reporter
          .fatalError(
              new StringDiagnostic(
                  "Cannot determine precise type for value: "
                      + stillImprecise.get(0)
                      + ", its imprecise type is: "
                      + stillImprecise.get(0).getTypeLattice(),
                  code.origin,
                  new MethodPosition(code.method.method)));
    }
  }

  private ArrayList<Value> constrainValues(boolean finished, List<Value> impreciseValues) {
    ArrayList<Value> stillImprecise = new ArrayList<>(impreciseValues.size());
    for (Value value : impreciseValues) {
      builder.constrainType(value, getCanonicalTypeConstraint(value, finished));
      if (!value.getTypeLattice().isPreciseType()) {
        stillImprecise.add(value);
      }
    }
    return stillImprecise;
  }

  public void constrainArrayMemberType(
      MemberType type, Value value, Value array, Consumer<MemberType> setter) {
    assert !type.isPrecise();
    Value canonical = canonical(value);
    ValueTypeConstraint constraint;
    if (array.getTypeLattice().isArrayType()) {
      // If the array type is known it uniquely defines the actual member type.
      ArrayTypeLatticeElement arrayType = array.getTypeLattice().asArrayTypeLatticeElement();
      constraint = ValueTypeConstraint.fromTypeLattice(arrayType.getArrayMemberTypeAsValueType());
    } else {
      // If not, e.g., the array input is null, the canonical value determines the final type.
      constraint = getCanonicalTypeConstraint(canonical, true);
    }
    // Constrain the canonical value by the final and precise type constraint and set the member.
    // A refinement of the value type will then be propagated in constrainValues of "round two".
    builder.constrainType(canonical, constraint);
    setter.accept(MemberType.constrainedType(type, constraint));
  }

  private void merge(Value value1, Value value2) {
    link(canonical(value1), canonical(value2));
  }

  private ValueTypeConstraint getCanonicalTypeConstraint(Value value, boolean finished) {
    ValueTypeConstraint type = constraintForType(canonical(value).getTypeLattice());
    switch (type) {
      case INT_OR_FLOAT_OR_OBJECT:
        // There is never a second round for resolving object vs single.
        assert !finished;
        return ValueTypeConstraint.INT_OR_FLOAT;
      case INT_OR_FLOAT:
        assert !finished || verifyNoConstrainedUses(value);
        return finished ? ValueTypeConstraint.INT : type;
      case LONG_OR_DOUBLE:
        assert !finished || verifyNoConstrainedUses(value);
        return finished ? ValueTypeConstraint.LONG : type;
      default:
        return type;
    }
  }

  private static boolean verifyNoConstrainedUses(Value value) {
    return verifyNoConstrainedUses(value, ImmutableSet.of());
  }

  private static boolean verifyNoConstrainedUses(Value value, Set<Value> assumeNoConstrainedUses) {
    for (Instruction user : value.uniqueUsers()) {
      if (user.isIf()) {
        If ifInstruction = user.asIf();
        if (ifInstruction.isZeroTest()) {
          continue;
        }
        Value other = ifInstruction.inValues().get(1 - ifInstruction.inValues().indexOf(value));
        if (assumeNoConstrainedUses.contains(other)) {
          continue;
        }
        assert verifyNoConstrainedUses(
            other,
            ImmutableSet.<Value>builder().addAll(assumeNoConstrainedUses).add(value).build());
      } else if (user.isArrayPut()) {
        ArrayPut put = user.asArrayPut();
        assert value == put.value();
        assert !put.getMemberType().isPrecise();
        assert put.array().getTypeLattice().isDefinitelyNull();
      } else {
        assert false;
      }
    }
    return true;
  }

  // Link two values as having the same type.
  private void link(Value canonical1, Value canonical2) {
    if (canonical1 == canonical2) {
      return;
    }
    TypeLatticeElement type1 = canonical1.getTypeLattice();
    TypeLatticeElement type2 = canonical2.getTypeLattice();
    if (type1.isPreciseType() && type2.isPreciseType()) {
      if (type1 != type2 && constraintForType(type1) != constraintForType(type2)) {
        throw new CompilationError(
            "Cannot unify types for values "
                + canonical1
                + ":"
                + type1
                + " and "
                + canonical2
                + ":"
                + type2);
      }
      return;
    }
    if (type1.isPreciseType()) {
      unificationParents.put(canonical2, canonical1);
    } else {
      unificationParents.put(canonical1, canonical2);
    }
  }

  // Find root with path-compression.
  private Value canonical(Value value) {
    Value parent = value;
    while (parent != null) {
      Value grandparent = unificationParents.get(parent);
      if (grandparent != null) {
        unificationParents.put(value, grandparent);
      }
      value = parent;
      parent = grandparent;
    }
    return value;
  }
}
