// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;
import static com.android.tools.r8.ir.analysis.type.Nullability.maybeNull;
import static com.android.tools.r8.ir.analysis.type.TypeLatticeElement.fromDexType;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.Lists;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public class TypeAnalysis {

  private enum Mode {
    UNSET,
    WIDENING,  // initial analysis, including fixed-point iteration for phis and updating with less
               // specific info, e.g., removing assume nodes.
    NARROWING, // updating with more specific info, e.g., passing the return value of the inlinee.
    NO_CHANGE  // utility to ensure types are up to date
  }

  private final boolean mayHaveImpreciseTypes;

  private Mode mode = Mode.UNSET;

  private final AppView<?> appView;

  private final Deque<Value> worklist = new ArrayDeque<>();

  public TypeAnalysis(AppView<?> appView) {
    this(appView, false);
  }

  public TypeAnalysis(AppView<?> appView, boolean mayHaveImpreciseTypes) {
    this.appView = appView;
    this.mayHaveImpreciseTypes = mayHaveImpreciseTypes;
  }

  private void analyze() {
    while (!worklist.isEmpty()) {
      analyzeValue(worklist.poll());
    }
  }

  public void widening(DexEncodedMethod context, DexEncodedMethod encodedMethod, IRCode code) {
    mode = Mode.WIDENING;
    assert worklist.isEmpty();
    code.topologicallySortedBlocks().forEach(b -> analyzeBasicBlock(context, encodedMethod, b));
    analyze();
  }

  public void widening(Iterable<Value> values) {
    analyzeValues(values, Mode.WIDENING);
  }

  public void narrowing(Iterable<? extends Value> values) {
    // TODO(b/125492155) Not sorting causes us to have non-deterministic behaviour. This should be
    //  removed when the bug is fixed.
    List<Value> sortedValues = Lists.newArrayList(values);
    sortedValues.sort(Comparator.comparingInt(Value::getNumber));
    analyzeValues(sortedValues, Mode.NARROWING);
  }

  public boolean verifyValuesUpToDate(Iterable<? extends Value> values) {
    analyzeValues(values, Mode.NO_CHANGE);
    return true;
  }

  private void analyzeValues(Iterable<? extends Value> values, Mode mode) {
    this.mode = mode;
    assert worklist.isEmpty();
    values.forEach(this::enqueue);
    analyze();
  }

  private void enqueue(Value v) {
    assert v != null;
    if (!worklist.contains(v)) {
      worklist.add(v);
    }
  }

  private void analyzeBasicBlock(
      DexEncodedMethod context, DexEncodedMethod encodedMethod, BasicBlock block) {
    int argumentsSeen = encodedMethod.accessFlags.isStatic() ? 0 : -1;
    for (Instruction instruction : block.getInstructions()) {
      Value outValue = instruction.outValue();
      if (outValue == null) {
        continue;
      }
      // The type for Argument, a quasi instruction, can be inferred from the method signature.
      if (instruction.isArgument()) {
        TypeLatticeElement derived;
        if (argumentsSeen < 0) {
          // Receiver
          derived =
              fromDexType(
                  encodedMethod.method.holder,
                  // Now we try inlining even when the receiver could be null.
                  encodedMethod == context ? definitelyNotNull() : maybeNull(),
                  appView);
        } else {
          DexType argType = encodedMethod.method.proto.parameters.values[argumentsSeen];
          derived = fromDexType(argType, maybeNull(), appView);
        }
        argumentsSeen++;
        updateTypeOfValue(outValue, derived);
        // Note that we don't need to enqueue the out value of arguments here because it's constant.
      } else if (instruction.hasInvariantOutType()) {
        TypeLatticeElement derived = instruction.evaluate(appView);
        updateTypeOfValue(outValue, derived);
      } else {
        enqueue(outValue);
      }
    }
    for (Phi phi : block.getPhis()) {
      enqueue(phi);
    }
  }

  private void analyzeValue(Value value) {
    TypeLatticeElement previous = value.getTypeLattice();
    TypeLatticeElement derived =
        value.isPhi() ? value.asPhi().computePhiType(appView) : value.definition.evaluate(appView);
    assert mayHaveImpreciseTypes || derived.isPreciseType();
    assert !previous.isPreciseType() || derived.isPreciseType();
    updateTypeOfValue(value, derived);
  }

  private void updateTypeOfValue(Value value, TypeLatticeElement type) {
    assert mode != Mode.UNSET;

    TypeLatticeElement current = value.getTypeLattice();
    if (current.equals(type)) {
      return;
    }

    assert mode != Mode.NO_CHANGE;

    if (type.isBottom()) {
      return;
    }

    if (mode == Mode.WIDENING) {
      value.widening(appView, type);
    } else {
      assert mode == Mode.NARROWING;
      value.narrowing(appView, type);
    }

    // propagate the type change to (instruction) users if any.
    for (Instruction instruction : value.uniqueUsers()) {
      Value outValue = instruction.outValue();
      if (outValue != null) {
        enqueue(outValue);
      }
    }
    // Propagate the type change to phi users if any.
    for (Phi phi : value.uniquePhiUsers()) {
      enqueue(phi);
    }
  }

  public static DexType getRefinedReceiverType(
      AppView<? extends AppInfoWithSubtyping> appView, InvokeMethodWithReceiver invoke) {
    Value receiver = invoke.getReceiver();
    TypeLatticeElement lattice = receiver.getDynamicUpperBoundType(appView);
    DexType staticReceiverType = invoke.getInvokedMethod().holder;
    if (lattice.isClassType()) {
      ClassTypeLatticeElement classType = lattice.asClassTypeLatticeElement();
      DexType refinedType = classType.getClassType();
      if (refinedType == appView.dexItemFactory().objectType) {
        Set<DexType> interfaces = classType.getInterfaces();
        if (interfaces.size() == 1) {
          refinedType = interfaces.iterator().next();
        }
      }
      if (appView.appInfo().isSubtype(refinedType, staticReceiverType)) {
        return refinedType;
      }
    }
    return staticReceiverType;
  }
}
