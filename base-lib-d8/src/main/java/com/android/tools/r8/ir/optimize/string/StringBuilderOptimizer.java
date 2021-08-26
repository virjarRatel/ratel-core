// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.string;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.escape.EscapeAnalysis;
import com.android.tools.r8.ir.analysis.escape.EscapeAnalysisConfiguration;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Assume;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.DominatorTree.Assumption;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.NumberConversion;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// This optimization attempts to replace all builder.toString() calls with a constant string.
// TODO(b/114002137): for now, the analysis depends on rewriteMoveResult.
// Consider the following example:
//
//   StringBuilder builder;
//   if (...) {
//     builder.append("X");
//   } else {
//     builder.append("Y");
//   }
//   builder.toString();
//
// Its corresponding IR looks like:
//   block0:
//     b <- new-instance StringBuilder
//     if ... block2 // Otherwise, fallthrough
//   block1:
//     c1 <- "X"
//     b1 <- invoke-virtual b, c1, ...append
//     goto block3
//   block2:
//     c2 <- "Y"
//     b2 <- invoke-virtual b, c2, ...append
//     goto block3
//   block3:
//     invoke-virtual b, ...toString
//
// After rewriteMoveResult, aliased out values, b1 and b2, are gone. So the analysis can focus on
// single SSA values, assuming it's flow-sensitive (which is not true in general).
public class StringBuilderOptimizer {

  private final AppView<?> appView;
  private final DexItemFactory factory;
  private final ThrowingInfo throwingInfo;
  @VisibleForTesting
  StringConcatenationAnalysis analysis;
  final StringBuilderOptimizationConfiguration optimizationConfiguration;

  private int numberOfBuildersWithMultipleToString = 0;
  private int numberOfBuildersWithoutToString = 0;
  private int numberOfBuildersThatEscape = 0;
  private int numberOfBuildersWhoseResultIsInterned = 0;
  private int numberOfBuildersWithNonTrivialStateChange = 0;
  private int numberOfBuildersWithUnsupportedArg = 0;
  private int numberOfBuildersWithMergingPoints = 0;
  private int numberOfBuildersWithNonDeterministicArg = 0;
  private int numberOfDeadBuilders = 0;
  private int numberOfBuildersSimplified = 0;
  private final Object2IntMap<Integer> histogramOfLengthOfAppendChains;
  private final Object2IntMap<Integer> histogramOfLengthOfEndResult;
  private final Object2IntMap<Integer> histogramOfLengthOfPartialAppendChains;
  private final Object2IntMap<Integer> histogramOfLengthOfPartialResult;

  public StringBuilderOptimizer(AppView<? extends AppInfo> appView) {
    this.appView = appView;
    this.factory = appView.dexItemFactory();
    this.throwingInfo = ThrowingInfo.defaultForConstString(appView.options());
    this.optimizationConfiguration = new DefaultStringBuilderOptimizationConfiguration();
    if (Log.ENABLED && Log.isLoggingEnabledFor(StringBuilderOptimizer.class)) {
      histogramOfLengthOfAppendChains = new Object2IntArrayMap<>();
      histogramOfLengthOfEndResult = new Object2IntArrayMap<>();
      histogramOfLengthOfPartialAppendChains = new Object2IntArrayMap<>();
      histogramOfLengthOfPartialResult = new Object2IntArrayMap<>();
    } else {
      histogramOfLengthOfAppendChains = null;
      histogramOfLengthOfEndResult = null;
      histogramOfLengthOfPartialAppendChains = null;
      histogramOfLengthOfPartialResult = null;
    }
  }

  public void logResults() {
    assert Log.ENABLED;
    Log.info(getClass(),
        "# builders w/ multiple toString(): %s", numberOfBuildersWithMultipleToString);
    Log.info(getClass(),
        "# builders w/o toString(): %s", numberOfBuildersWithoutToString);
    Log.info(getClass(),
        "# builders that escape: %s", numberOfBuildersThatEscape);
    Log.info(getClass(),
        "# builders whose result is interned: %s", numberOfBuildersWhoseResultIsInterned);
    Log.info(getClass(),
        "# builders w/ non-trivial state change: %s", numberOfBuildersWithNonTrivialStateChange);
    Log.info(getClass(),
        "# builders w/ unsupported arg: %s", numberOfBuildersWithUnsupportedArg);
    Log.info(getClass(),
        "# builders w/ merging points: %s", numberOfBuildersWithMergingPoints);
    Log.info(getClass(),
        "# builders w/ non-deterministic arg: %s", numberOfBuildersWithNonDeterministicArg);
    Log.info(getClass(), "# dead builders : %s", numberOfDeadBuilders);
    Log.info(getClass(), "# builders simplified: %s", numberOfBuildersSimplified);
    if (histogramOfLengthOfAppendChains != null) {
      Log.info(getClass(), "------ histogram of StringBuilder append chain lengths ------");
      histogramOfLengthOfAppendChains.forEach((chainSize, count) -> {
        Log.info(getClass(),
            "%s: %s (%s)", chainSize, StringUtils.times("*", Math.min(count, 53)), count);
      });
    }
    if (histogramOfLengthOfEndResult != null) {
      Log.info(getClass(), "------ histogram of StringBuilder result lengths ------");
      histogramOfLengthOfEndResult.forEach((length, count) -> {
        Log.info(getClass(),
            "%s: %s (%s)", length, StringUtils.times("*", Math.min(count, 53)), count);
      });
    }
    if (histogramOfLengthOfPartialAppendChains != null) {
      Log.info(getClass(),
          "------ histogram of StringBuilder append chain lengths (partial) ------");
      histogramOfLengthOfPartialAppendChains.forEach((chainSize, count) -> {
        Log.info(getClass(),
            "%s: %s (%s)", chainSize, StringUtils.times("*", Math.min(count, 53)), count);
      });
    }
    if (histogramOfLengthOfPartialResult != null) {
      Log.info(getClass(), "------ histogram of StringBuilder partial result lengths ------");
      histogramOfLengthOfPartialResult.forEach((length, count) -> {
        Log.info(getClass(),
            "%s: %s (%s)", length, StringUtils.times("*", Math.min(count, 53)), count);
      });
    }
  }

  public void computeTrivialStringConcatenation(IRCode code) {
    StringConcatenationAnalysis analysis = new StringConcatenationAnalysis(code);
    // Only for testing purpose, where we ran the analysis for only one method.
    // Using `this.analysis` is not thread-safe, of course.
    this.analysis = analysis;
    Set<Value> candidateBuilders =
        analysis.findAllLocalBuilders()
            .stream()
            .filter(analysis::canBeOptimized)
            .collect(Collectors.toSet());
    if (candidateBuilders.isEmpty()) {
      return;
    }
    analysis
        .buildBuilderStateGraph(candidateBuilders)
        .applyConcatenationResults(candidateBuilders)
        .removeTrivialBuilders();
  }

  class StringConcatenationAnalysis {

    // Inspired by {@link JumboStringTest}. Some code intentionally may have too many append(...).
    private static final int CONCATENATION_THRESHOLD = 200;
    private static final String ANY_STRING = "*";
    private static final String DUMMY = "$dummy$";

    private final IRCode code;

    // A map from SSA Value of StringBuilder type to its toString() counts.
    // Reused (e.g., concatenated, toString, concatenated more, toString) builders are out of scope.
    // TODO(b/114002137): some of those toString could have constant string states.
    final Object2IntMap<Value> builderToStringCounts = new Object2IntArrayMap<>();

    StringConcatenationAnalysis(IRCode code) {
      this.code = code;
    }

    // This optimization focuses on builders that are created and used locally.
    // In the first step, we collect builders that are created in the current method.
    // In the next step, we will filter out builders that cannot be optimized. To avoid multiple
    // iterations per builder, we're collecting # of uses of those builders by iterating the code
    // twice in this step.
    private Set<Value> findAllLocalBuilders() {
      // During the first iteration, collect builders that are locally created.
      // TODO(b/114002137): Make sure new-instance is followed by <init> before any other calls.
      for (Instruction instr : code.instructions()) {
        if (instr.isNewInstance()
            && optimizationConfiguration.isBuilderType(instr.asNewInstance().clazz)) {
          Value builder = instr.asNewInstance().dest();
          assert !builderToStringCounts.containsKey(builder);
          builderToStringCounts.put(builder, 0);
        }
      }
      if (builderToStringCounts.isEmpty()) {
        return ImmutableSet.of();
      }
      int concatenationCount = 0;
      // During the second iteration, count builders' usage.
      for (Instruction instr : code.instructions()) {
        if (!instr.isInvokeVirtual()) {
          continue;
        }
        InvokeVirtual invoke = instr.asInvokeVirtual();
        DexMethod invokedMethod = invoke.getInvokedMethod();
        if (optimizationConfiguration.isAppendMethod(invokedMethod)) {
          concatenationCount++;
          // The analysis might be overwhelmed.
          if (concatenationCount > CONCATENATION_THRESHOLD) {
            return ImmutableSet.of();
          }
        } else if (optimizationConfiguration.isToStringMethod(invokedMethod)) {
          assert invoke.inValues().size() == 1;
          Value receiver = invoke.getReceiver().getAliasedValue();
          for (Value builder : collectAllLinkedBuilders(receiver)) {
            if (builderToStringCounts.containsKey(builder)) {
              int count = builderToStringCounts.getInt(builder);
              builderToStringCounts.put(builder, count + 1);
            }
          }
        }
      }
      return builderToStringCounts.keySet();
    }

    private Set<Value> collectAllLinkedBuilders(Value builder) {
      Set<Value> builders = Sets.newIdentityHashSet();
      Set<Value> visited = Sets.newIdentityHashSet();
      collectAllLinkedBuilders(builder, builders, visited);
      return builders;
    }

    private void collectAllLinkedBuilders(Value builder, Set<Value> builders, Set<Value> visited) {
      if (!visited.add(builder)) {
        return;
      }
      if (builder.isPhi()) {
        for (Value operand : builder.asPhi().getOperands()) {
          collectAllLinkedBuilders(operand, builders, visited);
        }
      } else {
        builders.add(builder);
      }
    }

    private boolean canBeOptimized(Value builder) {
      // If the builder is definitely null, it may be handled by other optimizations.
      // E.g., any further operations, such as append, will raise NPE.
      // But, as we collect local builders, it should never be null.
      assert !builder.isAlwaysNull(appView);
      // Before checking the builder usage, make sure we have its usage count.
      assert builderToStringCounts.containsKey(builder);
      // If a builder is reused, chances are the code is not trivial, e.g., building a prefix
      // at some point; appending different suffices in different conditions; and building again.
      if (builderToStringCounts.getInt(builder) > 1) {
        numberOfBuildersWithMultipleToString++;
        return false;
      }
      // If a builder is not used, i.e., never converted to string, it doesn't make sense to
      // attempt to compute its compile-time constant string.
      if (builderToStringCounts.getInt(builder) < 1) {
        numberOfBuildersWithoutToString++;
        return false;
      }
      // Make sure builder is neither phi nor coming from outside of the method.
      assert !builder.isPhi() && builder.definition.isNewInstance();
      assert builder.getTypeLattice().isClassType();
      DexType builderType = builder.getTypeLattice().asClassTypeLatticeElement().getClassType();
      assert optimizationConfiguration.isBuilderType(builderType);
      EscapeAnalysis escapeAnalysis =
          new EscapeAnalysis(
              appView, new StringBuilderOptimizerEscapeAnalysisConfiguration(builder));
      return !escapeAnalysis.isEscaping(code, builder);
    }

    // A map from SSA Value of StringBuilder type to its internal state per instruction.
    final Map<Value, Map<Instruction, BuilderState>> builderStates = new HashMap<>();

    // Create a builder state, only used when visiting new-instance instructions.
    private Map<Instruction, BuilderState> createBuilderState(Value builder) {
      // By using LinkedHashMap, we want to visit instructions in the order of their insertions.
      return builderStates.computeIfAbsent(builder, ignore -> new LinkedHashMap<>());
    }

    // Get a builder state, used for all other cases except for new-instance instructions.
    private Map<Instruction, BuilderState> getBuilderState(Value builder) {
      return builderStates.get(builder);
    }

    // Suppose a simple, trivial chain:
    //   new StringBuilder().append("a").append("b").toString();
    //
    // the resulting graph would be:
    //   [s1, root, "", {s2}],
    //   [s2, s1, "a", {s3}],
    //   [s3, s2, "b", {}].
    //
    // For each meaningful IR (init, append, toString), the corresponding state will be bound:
    // <init> -> s1, 1st append -> s2, 2nd append -> s3, toString -> s3.
    //
    // Suppose an example with a phi:
    //   StringBuilder b = flag ? new StringBuilder("x") : new StringBuilder("y");
    //   b.append("z").toString();
    //
    // the resulting graph would be:
    //   [s1, root, "", {s2, s3, s4}],
    //   [s2, s1, "x", {}],
    //   [s3, s1, "y", {}],
    //   [s4, s1, "z", {}].
    //
    // Note that neither s2 nor s3 can dominate s4, and thus all of s{2..4} are linked to s1.
    // An alternative graph shape would be: [s4, {s2, s3}, "z", {}] (and proper successor update in
    // s2 and s3, of course). But, from the point of the view of finding the trivial chain, there is
    // no difference. The current graph construction relies on and resembles dominator tree.
    private StringConcatenationAnalysis buildBuilderStateGraph(Set<Value> candidateBuilders) {
      DominatorTree dominatorTree = new DominatorTree(code, Assumption.MAY_HAVE_UNREACHABLE_BLOCKS);
      for (BasicBlock block : code.topologicallySortedBlocks()) {
        for (Instruction instr : block.getInstructions()) {
          if (instr.isNewInstance()
              && optimizationConfiguration.isBuilderType(instr.asNewInstance().clazz)) {
            Value builder = instr.asNewInstance().dest();
            if (!candidateBuilders.contains(builder)) {
              continue;
            }
            Map<Instruction, BuilderState> perInstrState = createBuilderState(builder);
            perInstrState.put(instr, BuilderState.createRoot());
            continue;
          }

          if (instr.isInvokeDirect()
              && optimizationConfiguration.isBuilderInitWithInitialValue(instr.asInvokeDirect())) {
            InvokeDirect invoke = instr.asInvokeDirect();
            Value builder = invoke.getReceiver().getAliasedValue();
            if (!candidateBuilders.contains(builder)) {
              continue;
            }
            assert invoke.inValues().size() == 2;
            Value arg = invoke.inValues().get(1).getAliasedValue();
            DexType argType = invoke.getInvokedMethod().proto.parameters.values[0];
            String addition = extractConstantArgument(arg, argType);
            Map<Instruction, BuilderState> perInstrState = getBuilderState(builder);
            BuilderState dominantState = findDominantState(dominatorTree, perInstrState, instr);
            if (dominantState != null) {
              BuilderState currentState = dominantState.createChild(addition);
              perInstrState.put(instr, currentState);
            } else {
              // TODO(b/114002137): if we want to utilize partial results, don't remove it here.
              candidateBuilders.remove(builder);
            }
            continue;
          }

          if (!instr.isInvokeMethodWithReceiver()) {
            continue;
          }

          InvokeMethodWithReceiver invoke = instr.asInvokeMethodWithReceiver();
          if (optimizationConfiguration.isAppendMethod(invoke.getInvokedMethod())) {
            Value builder = invoke.getReceiver().getAliasedValue();
            // If `builder` is phi, itself and predecessors won't be tracked.
            // Not being tracked means that they won't be optimized, which is intentional.
            if (!candidateBuilders.contains(builder)) {
              continue;
            }
            Value arg = invoke.inValues().get(1).getAliasedValue();
            DexType argType = invoke.getInvokedMethod().proto.parameters.values[0];
            String addition = extractConstantArgument(arg, argType);
            Map<Instruction, BuilderState> perInstrState = getBuilderState(builder);
            BuilderState dominantState = findDominantState(dominatorTree, perInstrState, instr);
            if (dominantState != null) {
              BuilderState currentState = dominantState.createChild(addition);
              perInstrState.put(instr, currentState);
            } else {
              // TODO(b/114002137): if we want to utilize partial results, don't remove it here.
              candidateBuilders.remove(builder);
            }
          }
          if (optimizationConfiguration.isToStringMethod(invoke.getInvokedMethod())) {
            Value builder = invoke.getReceiver().getAliasedValue();
            // If `builder` is phi, itself and predecessors won't be tracked.
            // Not being tracked means that they won't be optimized, which is intentional.
            if (!candidateBuilders.contains(builder)) {
              continue;
            }
            Map<Instruction, BuilderState> perInstrState = getBuilderState(builder);
            BuilderState dominantState = findDominantState(dominatorTree, perInstrState, instr);
            if (dominantState != null) {
              perInstrState.put(instr, dominantState);
            } else {
              // TODO(b/114002137): if we want to utilize partial results, don't remove it here.
              candidateBuilders.remove(builder);
            }
          }
        }
      }

      return this;
    }

    private String extractConstantArgument(Value arg, DexType argType) {
      String addition = ANY_STRING;
      if (arg.isPhi()) {
        return addition;
      }
      if (arg.definition.isConstString()) {
        addition = arg.definition.asConstString().getValue().toString();
      } else if (arg.definition.isConstNumber() || arg.definition.isNumberConversion()) {
        Number number = extractConstantNumber(arg);
        if (number == null) {
          return addition;
        }
        if (arg.getTypeLattice().isPrimitive()) {
          if (argType == factory.booleanType) {
            addition = String.valueOf(number.intValue() != 0);
          } else if (argType == factory.byteType) {
            addition = String.valueOf(number.byteValue());
          } else if (argType == factory.shortType) {
            addition = String.valueOf(number.shortValue());
          } else if (argType == factory.charType) {
            addition = String.valueOf((char) number.intValue());
          } else if (argType == factory.intType) {
            addition = String.valueOf(number.intValue());
          } else if (argType == factory.longType) {
            addition = String.valueOf(number.longValue());
          } else if (argType == factory.floatType) {
            addition = String.valueOf(number.floatValue());
          } else if (argType == factory.doubleType) {
            addition = String.valueOf(number.doubleValue());
          }
        } else if (arg.getTypeLattice().isNullType()) {
          assert number.intValue() == 0;
          addition = "null";
        }
      }
      return addition;
    }

    private Number extractConstantNumber(Value arg) {
      if (arg.isPhi()) {
        return null;
      }
      if (arg.definition.isConstNumber()) {
        ConstNumber cst = arg.definition.asConstNumber();
        if (cst.outType() == ValueType.LONG) {
          return cst.getLongValue();
        } else if (cst.outType() == ValueType.FLOAT) {
          return cst.getFloatValue();
        } else if (cst.outType() == ValueType.DOUBLE) {
          return cst.getDoubleValue();
        } else {
          assert cst.outType() == ValueType.INT || cst.outType() == ValueType.OBJECT;
          return cst.getIntValue();
        }
      } else if (arg.definition.isNumberConversion()) {
        NumberConversion conversion = arg.definition.asNumberConversion();
        assert conversion.inValues().size() == 1;
        Number temp = extractConstantNumber(conversion.inValues().get(0));
        if (temp == null) {
          return null;
        }
        DexType conversionType = conversion.to.dexTypeFor(factory);
        if (conversionType == factory.booleanType) {
          return temp.intValue() != 0 ? 1 : 0;
        } else if (conversionType == factory.byteType) {
          return temp.byteValue();
        } else if (conversionType == factory.shortType) {
          return temp.shortValue();
        } else if (conversionType == factory.charType) {
          return temp.intValue();
        } else if (conversionType == factory.intType) {
          return temp.intValue();
        } else if (conversionType == factory.longType) {
          return temp.longValue();
        } else if (conversionType == factory.floatType) {
          return temp.floatValue();
        } else if (conversionType == factory.doubleType) {
          return temp.doubleValue();
        }
      }
      return null;
    }

    private BuilderState findDominantState(
        DominatorTree dominatorTree,
        Map<Instruction, BuilderState> perInstrState,
        Instruction current) {
      BuilderState result = null;
      BasicBlock lastDominantBlock = null;
      for (Instruction instr : perInstrState.keySet()) {
        BasicBlock block = instr.getBlock();
        if (!dominatorTree.dominatedBy(current.getBlock(), block)) {
          continue;
        }
        if (lastDominantBlock == null
            || dominatorTree.dominatedBy(block, lastDominantBlock)) {
          result = perInstrState.get(instr);
          lastDominantBlock = block;
        }
      }
      return result;
    }

    private void logHistogramOfChains(List<String> contents, boolean isPartial) {
      if (!Log.ENABLED || !Log.isLoggingEnabledFor(StringOptimizer.class)) {
        return;
      }
      if (contents == null || contents.isEmpty()) {
        return;
      }
      String result = StringUtils.join(contents, "");
      Integer size = Integer.valueOf(contents.size());
      Integer length = Integer.valueOf(result.length());
      if (isPartial) {
        assert histogramOfLengthOfPartialAppendChains != null;
        synchronized (histogramOfLengthOfPartialAppendChains) {
          int count = histogramOfLengthOfPartialAppendChains.getOrDefault(size, 0);
          histogramOfLengthOfPartialAppendChains.put(size, count + 1);
        }
        assert histogramOfLengthOfPartialResult != null;
        synchronized (histogramOfLengthOfPartialResult) {
          int count = histogramOfLengthOfPartialResult.getOrDefault(length, 0);
          histogramOfLengthOfPartialResult.put(length, count + 1);
        }
      } else {
        assert histogramOfLengthOfAppendChains != null;
        synchronized (histogramOfLengthOfAppendChains) {
          int count = histogramOfLengthOfAppendChains.getOrDefault(size, 0);
          histogramOfLengthOfAppendChains.put(size, count + 1);
        }
        assert histogramOfLengthOfEndResult != null;
        synchronized (histogramOfLengthOfEndResult) {
          int count = histogramOfLengthOfEndResult.getOrDefault(length, 0);
          histogramOfLengthOfEndResult.put(length, count + 1);
        }
      }
    }

    // StringBuilders that are simplified by this analysis or simply dead (e.g., after applying
    // -assumenosideeffects to logging calls, then logging messages built by builders are dead).
    // Will be used to clean up uses of the builders, such as creation, <init>, and append calls.
    final Set<Value> deadBuilders = Sets.newIdentityHashSet();
    final Set<Value> simplifiedBuilders = Sets.newIdentityHashSet();

    private StringConcatenationAnalysis applyConcatenationResults(Set<Value> candidateBuilders) {
      Set<Value> affectedValues = Sets.newIdentityHashSet();
      InstructionListIterator it = code.instructionListIterator();
      while (it.hasNext()) {
        Instruction instr = it.nextUntil(i -> isToStringOfInterest(candidateBuilders, i));
        if (instr == null) {
          break;
        }
        InvokeVirtual invoke = instr.asInvokeVirtual();
        assert invoke.inValues().size() == 1;
        Value builder = invoke.getReceiver().getAliasedValue();
        Value outValue = invoke.outValue();
        if (outValue == null || outValue.isDead(appView, code)) {
          // If the out value is still used but potentially dead, replace it with a dummy string.
          if (outValue != null && outValue.isUsed()) {
            Value dummy =
                code.createValue(
                    TypeLatticeElement.stringClassType(appView, definitelyNotNull()),
                    invoke.getLocalInfo());
            it.replaceCurrentInstruction(
                new ConstString(dummy, factory.createString(DUMMY), throwingInfo));
          } else {
            it.removeOrReplaceByDebugLocalRead();
          }
          // Although this builder is not simplified by this analysis, add that to the set so that
          // it can be removed at the final clean-up phase.
          deadBuilders.add(builder);
          numberOfDeadBuilders++;
          continue;
        }
        Map<Instruction, BuilderState> perInstrState = builderStates.get(builder);
        assert perInstrState != null;
        BuilderState builderState = perInstrState.get(instr);
        assert builderState != null;
        String element = toCompileTimeString(builderState);
        assert element != null;
        Value stringValue =
            code.createValue(
                TypeLatticeElement.stringClassType(appView, definitelyNotNull()),
                invoke.getLocalInfo());
        affectedValues.addAll(outValue.affectedValues());
        it.replaceCurrentInstruction(
            new ConstString(stringValue, factory.createString(element), throwingInfo));
        simplifiedBuilders.add(builder);
        numberOfBuildersSimplified++;
      }
      // Concatenation results are not null, and thus propagate that information.
      if (!affectedValues.isEmpty()) {
        new TypeAnalysis(appView).narrowing(affectedValues);
      }
      return this;
    }

    private boolean isToStringOfInterest(Set<Value> candidateBuilders, Instruction instr) {
      if (!instr.isInvokeVirtual()) {
        return false;
      }
      InvokeVirtual invoke = instr.asInvokeVirtual();
      DexMethod invokedMethod = invoke.getInvokedMethod();
      if (!optimizationConfiguration.isToStringMethod(invokedMethod)) {
        return false;
      }
      assert invoke.inValues().size() == 1;
      Value builder = invoke.getReceiver().getAliasedValue();
      if (!candidateBuilders.contains(builder)) {
        return false;
      }
      // If the result of toString() is no longer used, computing the compile-time constant is
      // even not necessary.
      if (!invoke.hasOutValue() || invoke.outValue().isDead(appView, code)) {
        return true;
      }
      Map<Instruction, BuilderState> perInstrState = builderStates.get(builder);
      if (perInstrState == null) {
        return false;
      }
      BuilderState builderState = perInstrState.get(instr);
      if (builderState == null) {
        return false;
      }
      String element = toCompileTimeString(builderState);
      if (element == null) {
        return false;
      }
      return true;
    }

    // Find the trivial chain of builder-append*-toString.
    // Note that we can't determine a compile-time constant string if there are any ambiguity.
    @VisibleForTesting
    String toCompileTimeString(BuilderState state) {
      boolean continuedForLogging = false;
      LinkedList<String> contents = new LinkedList<>();
      while (state != null) {
        // Not a single chain if there are multiple successors.
        if (state.nexts != null && state.nexts.size() > 1) {
          numberOfBuildersWithMergingPoints++;
          if (Log.ENABLED && Log.isLoggingEnabledFor(StringBuilderOptimizer.class)) {
            logHistogramOfChains(contents, true);
            continuedForLogging = true;
            contents.clear();
            state = state.previous;
            continue;
          }
          return null;
        }
        // Reaching the root.
        if (state.addition == null) {
          break;
        }
        // A non-deterministic argument is appended.
        // TODO(b/129200243): Even though it's ambiguous, if the chain length is 2, and the 1st one
        //   is definitely non-null, we can convert it to String#concat.
        if (state.addition.equals(ANY_STRING)) {
          numberOfBuildersWithNonDeterministicArg++;
          if (Log.ENABLED && Log.isLoggingEnabledFor(StringBuilderOptimizer.class)) {
            logHistogramOfChains(contents, true);
            continuedForLogging = true;
            contents.clear();
            state = state.previous;
            continue;
          }
          return null;
        }
        contents.push(state.addition);
        state = state.previous;
      }
      if (continuedForLogging) {
        logHistogramOfChains(contents, true);
        return null;
      }
      if (Log.ENABLED && Log.isLoggingEnabledFor(StringBuilderOptimizer.class)) {
        logHistogramOfChains(contents, false);
      }
      if (contents.isEmpty()) {
        return null;
      }
      String result = StringUtils.join(contents, "");
      int estimate = estimateSizeReduction(contents);
      return estimate > result.length() ? result : null;
    }

    private int estimateSizeReduction(List<String> contents) {
      int result = 8; // builder initialization
      for (String content : contents) {
        result += 4; // builder append()
        // If a certain string is only used as part of the resulting string, it will be gone.
        result += (int) (content.length() * 0.5); // Magic number of that chance: 50%.
      }
      result += 4; // builder toString()
      return result;
    }

    void removeTrivialBuilders() {
      if (deadBuilders.isEmpty() && simplifiedBuilders.isEmpty()) {
        return;
      }
      Set<Value> buildersToRemove = Sets.union(deadBuilders, simplifiedBuilders);
      // All instructions that refer to dead/simplified builders are dead.
      // Here, we remove toString() calls, append(...) calls, <init>, and new-instance in order.
      InstructionListIterator it = code.instructionListIterator();
      while (it.hasNext()) {
        Instruction instr = it.next();
        if (instr.isInvokeVirtual()) {
          InvokeVirtual invoke = instr.asInvokeVirtual();
          DexMethod invokedMethod = invoke.getInvokedMethod();
          if (optimizationConfiguration.isToStringMethod(invokedMethod)
              && buildersToRemove.contains(invoke.getReceiver().getAliasedValue())) {
            it.removeOrReplaceByDebugLocalRead();
          }
        }
      }
      // append(...) and <init> don't have out values, so removing them won't bother each other.
      it = code.instructionListIterator();
      while (it.hasNext()) {
        Instruction instr = it.next();
        if (instr.isInvokeVirtual()) {
          InvokeVirtual invoke = instr.asInvokeVirtual();
          DexMethod invokedMethod = invoke.getInvokedMethod();
          if (optimizationConfiguration.isAppendMethod(invokedMethod)
              && buildersToRemove.contains(invoke.getReceiver().getAliasedValue())) {
            it.removeOrReplaceByDebugLocalRead();
          }
        }
        if (instr.isInvokeDirect()) {
          InvokeDirect invoke = instr.asInvokeDirect();
          DexMethod invokedMethod = invoke.getInvokedMethod();
          if (optimizationConfiguration.isBuilderInit(invokedMethod)
              && buildersToRemove.contains(invoke.getReceiver().getAliasedValue())) {
            it.removeOrReplaceByDebugLocalRead();
          }
        }
        // If there are aliasing instructions, they should be removed before new-instance.
        if (instr.isAssume() && buildersToRemove.contains(instr.outValue().getAliasedValue())) {
          Assume<?> assumeInstruction = instr.asAssume();
          Value src = assumeInstruction.src();
          Value dest = assumeInstruction.outValue();
          dest.replaceUsers(src);
          it.remove();
        }
      }
      // new-instance should be removed at last, since it will check the out value, builder, is not
      // used anywhere, which we've removed so far.
      it = code.instructionListIterator();
      while (it.hasNext()) {
        Instruction instr = it.next();
        if (instr.isNewInstance()
            && optimizationConfiguration.isBuilderType(instr.asNewInstance().clazz)
            && instr.hasOutValue()
            && buildersToRemove.contains(instr.outValue())) {
          it.removeOrReplaceByDebugLocalRead();
        }
      }
      assert code.isConsistentSSA();
    }
  }

  class DefaultStringBuilderOptimizationConfiguration
      implements StringBuilderOptimizationConfiguration {
    @Override
    public boolean isBuilderType(DexType type) {
      return type == factory.stringBuilderType
          || type == factory.stringBufferType;
    }

    @Override
    public boolean isBuilderInit(DexMethod method, DexType builderType) {
      return builderType == method.holder
          && factory.isConstructor(method);
    }

    @Override
    public boolean isBuilderInit(DexMethod method) {
      return isBuilderType(method.holder)
          && factory.isConstructor(method);
    }

    @Override
    public boolean isBuilderInitWithInitialValue(InvokeMethod invoke) {
      return isBuilderInit(invoke.getInvokedMethod())
          && invoke.inValues().size() == 2
          && !invoke.inValues().get(1).getTypeLattice().isPrimitive();
    }

    @Override
    public boolean isAppendMethod(DexMethod method) {
      return factory.stringBuilderMethods.isAppendMethod(method)
          || factory.stringBufferMethods.isAppendMethod(method);
    }

    @Override
    public boolean isSupportedAppendMethod(InvokeMethod invoke) {
      DexMethod invokedMethod = invoke.getInvokedMethod();
      assert isAppendMethod(invokedMethod);
      // Any methods other than append(arg) are not trivial since they may change the builder
      // state not monotonically.
      if (invoke.inValues().size() > 2) {
        numberOfBuildersWithNonTrivialStateChange++;
        return false;
      }
      assert invoke.inValues().size() == 2;
      TypeLatticeElement argType = invoke.inValues().get(1).getTypeLattice();
      if (!argType.isPrimitive() && !argType.isClassType() && !argType.isNullType()) {
        numberOfBuildersWithUnsupportedArg++;
        return false;
      }
      if (argType.isClassType()) {
        DexType argClassType = argType.asClassTypeLatticeElement().getClassType();
        return canHandleArgumentType(argClassType);
      }
      return true;
    }

    @Override
    public boolean isToStringMethod(DexMethod method) {
      return method == factory.stringBuilderMethods.toString
          || method == factory.stringBufferMethods.toString;
    }

    private boolean canHandleArgumentType(DexType argType) {
      // TODO(b/113859361): passed to another builder should be an eligible case.
      return argType == factory.stringType || argType == factory.charSequenceType;
    }
  }

  class StringBuilderOptimizerEscapeAnalysisConfiguration implements EscapeAnalysisConfiguration {
    final Value builder;
    final DexType builderType;

    private StringBuilderOptimizerEscapeAnalysisConfiguration(Value builder) {
      this.builder = builder;
      assert builder.getTypeLattice().isClassType();
      builderType = builder.getTypeLattice().asClassTypeLatticeElement().getClassType();
    }

    private void logEscapingRoute(boolean legitimate) {
      if (!legitimate) {
        numberOfBuildersThatEscape++;
      }
    }

    @Override
    public boolean isLegitimateEscapeRoute(
        AppView<?> appView,
        EscapeAnalysis escapeAnalysis,
        Instruction escapeRoute,
        DexMethod context) {
      if (escapeRoute.isReturn() || escapeRoute.isThrow() || escapeRoute.isStaticPut()) {
        logEscapingRoute(false);
        return false;
      }
      if (escapeRoute.isInvokeMethod()) {
        // Program class may call String#intern(). Only allow library calls.
        // TODO(b/114002137): For now, we allow only library calls to avoid a case like
        //   identity(Builder.toString()).intern(); but it's too restrictive.
        DexClass holderClass =
            appView.definitionFor(escapeRoute.asInvokeMethod().getInvokedMethod().holder);
        if (holderClass != null && !holderClass.isLibraryClass()) {
          logEscapingRoute(false);
          return false;
        }

        InvokeMethod invoke = escapeRoute.asInvokeMethod();
        DexMethod invokedMethod = invoke.getInvokedMethod();
        // Make sure builder's uses are local, i.e., not escaping from the current method.
        if (invokedMethod.holder != builderType) {
          logEscapingRoute(false);
          return false;
        }
        // <init> is legitimate.
        if (optimizationConfiguration.isBuilderInit(invokedMethod, builderType)) {
          return true;
        }
        if (optimizationConfiguration.isToStringMethod(invokedMethod)) {
          Value out = escapeRoute.outValue();
          if (out != null) {
            // If Builder#toString is interned, it could be used for equality check.
            // Replacing builder-based runtime result with a compile time constant may change
            // the program's runtime behavior.
            for (Instruction outUser : out.uniqueUsers()) {
              if (outUser.isInvokeMethodWithReceiver()
                  && outUser.asInvokeMethodWithReceiver().getInvokedMethod()
                      == factory.stringMethods.intern) {
                numberOfBuildersWhoseResultIsInterned++;
                return false;
              }
            }
          }
          // Otherwise, use of Builder#toString is legitimate.
          return true;
        }
        // Even though all invocations belong to the builder type, there are some methods other
        // than append/toString, e.g., setCharAt, setLength, subSequence, etc.
        // Seeing any of them indicates that this code is not trivial.
        if (!optimizationConfiguration.isAppendMethod(invokedMethod)) {
          numberOfBuildersWithNonTrivialStateChange++;
          return false;
        }
        if (!optimizationConfiguration.isSupportedAppendMethod(invoke)) {
          return false;
        }

        // Reaching here means that this invocation is part of trivial patterns we're looking for.
        return true;
      }
      if (escapeRoute.isArrayPut()) {
        Value array = escapeRoute.asArrayPut().array().getAliasedValue();
        boolean legitimate = !array.isPhi() && array.definition.isCreatingArray();
        logEscapingRoute(legitimate);
        return legitimate;
      }
      if (escapeRoute.isInstancePut()) {
        Value instance = escapeRoute.asInstancePut().object().getAliasedValue();
        boolean legitimate = !instance.isPhi() && instance.definition.isNewInstance();
        logEscapingRoute(legitimate);
        return legitimate;
      }
      // All other cases are not legitimate.
      logEscapingRoute(false);
      return false;
    }
  }

  // A chain of builder's internal state changes.
  static class BuilderState {
    BuilderState previous;
    String addition;
    Set<BuilderState> nexts;

    private BuilderState() {
      previous = null;
      addition = null;
      nexts = null;
    }

    static BuilderState createRoot() {
      return new BuilderState();
    }

    BuilderState createChild(String addition) {
      BuilderState newState = new BuilderState();
      newState.previous = this;
      newState.addition = addition;
      if (this.nexts == null) {
        this.nexts = Sets.newIdentityHashSet();
      }
      this.nexts.add(newState);
      return newState;
    }
  }
}
