// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.escape;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.Box;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Escape analysis that collects instructions where the value of interest can escape from the given
 * method, i.e., can be stored outside, passed as arguments, etc.
 *
 * <p>Note: at this point, the analysis always returns a non-empty list for values that are defined
 * by a new-instance IR, since they could escape via the corresponding direct call to `<init>()`.
 */
public class EscapeAnalysis {

  private final AppView<?> appView;
  private final EscapeAnalysisConfiguration configuration;

  // Data structure to ensure termination in case of cycles in the data flow graph.
  private final Set<Value> trackedValues = Sets.newIdentityHashSet();

  // Worklist containing values that are alias of the value of interest.
  private final Deque<Value> valuesToTrack = new ArrayDeque<>();

  public EscapeAnalysis(AppView<?> appView) {
    this(appView, DefaultEscapeAnalysisConfiguration.getInstance());
  }

  public EscapeAnalysis(AppView<?> appView, EscapeAnalysisConfiguration configuration) {
    this.appView = appView;
    this.configuration = configuration;
  }

  /**
   * Main entry point for running the escape analysis. This method is used to determine if
   * `valueOfInterest` escapes from the given method. Returns true as soon as the value is known to
   * escape, hence it is more efficient to use this method over {@link #computeEscapeRoutes(IRCode,
   * Value)}.
   */
  public boolean isEscaping(IRCode code, Value valueOfInterest) {
    Box<Instruction> firstEscapeRoute = new Box<>();
    run(
        code,
        valueOfInterest,
        escapeRoute -> {
          firstEscapeRoute.set(escapeRoute);
          return true;
        });
    return firstEscapeRoute.isSet();
  }

  /**
   * Main entry point for running the escape analysis. This method is used to determine if
   * `valueOfInterest` escapes from the given method. Returns the set of instructions from which the
   * value may escape. Clients that only need to know if the value escapes or not should use {@link
   * #isEscaping(IRCode, Value)} for better performance.
   */
  public Set<Instruction> computeEscapeRoutes(IRCode code, Value valueOfInterest) {
    ImmutableSet.Builder<Instruction> escapeRoutes = ImmutableSet.builder();
    run(
        code,
        valueOfInterest,
        escapeRoute -> {
          escapeRoutes.add(escapeRoute);
          return false;
        });
    return escapeRoutes.build();
  }

  // Returns the set of instructions where the value of interest can escape from the code.
  private void run(IRCode code, Value valueOfInterest, Predicate<Instruction> stoppingCriterion) {
    assert valueOfInterest.getTypeLattice().isReference();
    assert trackedValues.isEmpty();
    assert valuesToTrack.isEmpty();

    // Sanity check: value (or its containing block) belongs to the code.
    BasicBlock block =
        valueOfInterest.isPhi()
            ? valueOfInterest.asPhi().getBlock()
            : valueOfInterest.definition.getBlock();
    assert code.blocks.contains(block);
    List<Value> arguments = code.collectArguments();

    addToWorklist(valueOfInterest);

    while (!valuesToTrack.isEmpty()) {
      Value alias = valuesToTrack.poll();
      assert alias != null;

      // Make sure we are not tracking values over and over again.
      assert trackedValues.contains(alias);

      boolean stopped =
          processValue(valueOfInterest, alias, block, code, arguments, stoppingCriterion);
      if (stopped) {
        break;
      }
    }

    trackedValues.clear();
    valuesToTrack.clear();
  }

  private boolean processValue(
      Value root,
      Value alias,
      BasicBlock block,
      IRCode code,
      List<Value> arguments,
      Predicate<Instruction> stoppingCriterion) {
    for (Value phi : alias.uniquePhiUsers()) {
      addToWorklist(phi);
    }
    for (Instruction user : alias.uniqueUsers()) {
      // Users in the same block need one more filtering.
      if (user.getBlock() == block) {
        // When the value of interest has the definition
        if (!root.isPhi()) {
          List<Instruction> instructions = block.getInstructions();
          // Make sure we're not considering instructions prior to the value of interest.
          if (instructions.indexOf(user) < instructions.indexOf(root.definition)) {
            continue;
          }
        }
      }
      if (!configuration.isLegitimateEscapeRoute(appView, this, user, code.method.method)
          && isDirectlyEscaping(user, code.method.method, arguments)) {
        if (stoppingCriterion.test(user)) {
          return true;
        }
      }
      // Track aliased value.
      boolean couldIntroduceTrackedValueAlias = false;
      for (Value trackedValue : trackedValues) {
        if (user.couldIntroduceAnAlias(appView, trackedValue)) {
          couldIntroduceTrackedValueAlias = true;
          break;
        }
      }
      if (couldIntroduceTrackedValueAlias) {
        Value outValue = user.outValue();
        assert outValue != null && outValue.getTypeLattice().isReference();
        addToWorklist(outValue);
      }
      // Track propagated values through which the value of interest can escape indirectly.
      Value propagatedValue = getPropagatedSubject(alias, user);
      if (propagatedValue != null && propagatedValue != alias) {
        assert propagatedValue.getTypeLattice().isReference();
        addToWorklist(propagatedValue);
      }
    }
    return false;
  }

  private void addToWorklist(Value value) {
    assert value != null;
    if (trackedValues.add(value)) {
      valuesToTrack.push(value);
    }
  }

  private boolean isDirectlyEscaping(Instruction instr, DexMethod context, List<Value> arguments) {
    // As return value.
    if (instr.isReturn()) {
      return true;
    }
    // Throwing an exception.
    if (instr.isThrow()) {
      return true;
    }
    // Storing to the static field.
    if (instr.isStaticPut()) {
      return true;
    }
    // Passing as arguments.
    if (instr.isInvokeMethod()) {
      DexMethod invokedMethod = instr.asInvokeMethod().getInvokedMethod();
      // Filter out the recursion with exactly same arguments.
      if (invokedMethod == context) {
        return !instr.inValues().equals(arguments);
      }
      return true;
    }
    // Storing to non-local array.
    if (instr.isArrayPut()) {
      Value array = instr.asArrayPut().array().getAliasedValue();
      return array.isPhi() || !array.definition.isCreatingArray();
    }
    // Storing to non-local instance.
    if (instr.isInstancePut()) {
      Value instance = instr.asInstancePut().object().getAliasedValue();
      return instance.isPhi() || !instance.definition.isNewInstance();
    }
    return false;
  }

  public boolean isValueOfInterestOrAlias(Value value) {
    return trackedValues.contains(value);
  }

  private static Value getPropagatedSubject(Value src, Instruction instr) {
    if (instr.isArrayPut()) {
      return instr.asArrayPut().array();
    }
    if (instr.isInstancePut()) {
      return instr.asInstancePut().object();
    }
    return null;
  }
}
