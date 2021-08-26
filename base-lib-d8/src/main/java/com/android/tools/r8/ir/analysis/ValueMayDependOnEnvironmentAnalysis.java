// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis;

import static com.android.tools.r8.graph.DexProgramClass.asProgramClassOrNull;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeNewArray;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.NewArrayFilledData;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.LongInterval;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An analysis that is used to determine if a value may depend on the runtime environment. The
 * primary use case of this analysis is to determine if a class initializer can safely be postponed:
 *
 * <p>If a class initializer does not have any other side effects than the field assignments to the
 * static fields of the enclosing class, and all the values that are being stored into the static
 * fields do not depend on the runtime environment (i.e., the heap, which can be accessed via
 * static-get instructions), then the class initializer can safely be postponed.
 *
 * <p>All compile time constants are independent of the runtime environment. Furthermore, all newly
 * instantiated arrays and objects are independent of the environment, as long as their content
 * (i.e., array elements and field values) does not depend on the runtime environment.
 *
 * <p>Example: Values that are considered to be independent of the runtime environment:
 *
 * <ul>
 *   <li>{@code true}
 *   <li>{@code 42}
 *   <li>{@code "Hello world!"}
 *   <li>{@code new Object()}
 *   <li>{@code new MyClassWithTrivialInitializer("ABC")}
 *   <li>{@code new MyStandardEnum(42, "A")}
 *   <li>{@code new int[] {0, 1, 2}}
 *   <li>{@code new Object[] {new Object(), new MyClassWithTrivialInitializer()}}
 * </ul>
 *
 * <p>Example: Values that are considered to be dependent on the runtime environment:
 *
 * <ul>
 *   <li>{@code argX} (for methods with arguments)
 *   <li>{@code OtherClass.STATIC_FIELD} (reads a value from the runtime environment)
 *   <li>{@code new MyClassWithTrivialInitializer(OtherClass.FIELD)}
 * </ul>
 */
public class ValueMayDependOnEnvironmentAnalysis {

  private final AppView<?> appView;
  private final IRCode code;
  private final DexType context;

  private final Set<Value> knownNotToDependOnEnvironment = Sets.newIdentityHashSet();
  private final Set<Value> visited = Sets.newIdentityHashSet();

  // Lazily computed mapping from final field definitions of the enclosing class to the static-put
  // instructions in the class initializer that assigns these final fields.
  private Map<DexEncodedField, List<StaticPut>> finalFieldPuts;

  public ValueMayDependOnEnvironmentAnalysis(AppView<?> appView, IRCode code) {
    this.appView = appView;
    this.code = code;
    this.context = code.method.method.holder;
  }

  public boolean valueMayDependOnEnvironment(Value value) {
    boolean result = valueMayDependOnEnvironment(value, Sets.newIdentityHashSet());
    assert visited.isEmpty();
    return result;
  }

  private boolean valueMayDependOnEnvironment(
      Value value, Set<Value> assumedNotToDependOnEnvironment) {
    Value root = value.getAliasedValue();
    if (assumedNotToDependOnEnvironment.contains(root)) {
      return false;
    }
    if (knownNotToDependOnEnvironment.contains(root)) {
      return false;
    }
    if (!visited.add(root)) {
      // Guard against cycle by conservatively returning true.
      return true;
    }
    try {
      if (root.isConstant()) {
        return false;
      }
      if (isConstantArrayThroughoutMethod(root, assumedNotToDependOnEnvironment)) {
        return false;
      }
      if (root.getAbstractValue(appView, context).isSingleEnumValue()) {
        return false;
      }
      if (isNewInstanceWithoutEnvironmentDependentFields(root, assumedNotToDependOnEnvironment)) {
        return false;
      }
      if (isAliasOfValueThatIsIndependentOfEnvironment(root, assumedNotToDependOnEnvironment)) {
        return false;
      }
      return true;
    } finally {
      boolean changed = visited.remove(root);
      assert changed;
    }
  }

  private boolean valueMayNotDependOnEnvironmentAssumingArrayDoesNotDependOnEnvironment(
      Value value, Value array, Set<Value> assumedNotToDependOnEnvironment) {
    assert !value.hasAliasedValue();
    assert !array.hasAliasedValue();

    if (assumedNotToDependOnEnvironment.add(array)) {
      boolean valueMayDependOnEnvironment =
          valueMayDependOnEnvironment(value, assumedNotToDependOnEnvironment);
      boolean changed = assumedNotToDependOnEnvironment.remove(array);
      assert changed;
      return !valueMayDependOnEnvironment;
    }
    return !valueMayDependOnEnvironment(value, assumedNotToDependOnEnvironment);
  }

  /**
   * Used to identify if an array is "constant" in the sense that none of the values written into
   * the array may depend on the environment.
   *
   * <p>Examples include {@code new int[] {1,2,3}} and {@code new Object[]{new Object()}}.
   */
  public boolean isConstantArrayThroughoutMethod(Value value) {
    boolean result = isConstantArrayThroughoutMethod(value, Sets.newIdentityHashSet());
    assert visited.isEmpty();
    return result;
  }

  private boolean isConstantArrayThroughoutMethod(
      Value value, Set<Value> assumedNotToDependOnEnvironment) {
    Value root = value.getAliasedValue();
    if (root.isPhi()) {
      // Would need to track the aliases, just give up.
      return false;
    }

    Instruction definition = root.definition;

    // Check that it is a constant array with a known size at this point in the IR.
    long size;
    if (definition.isInvokeNewArray()) {
      InvokeNewArray invokeNewArray = definition.asInvokeNewArray();
      for (Value argument : invokeNewArray.arguments()) {
        if (!argument.isConstant()) {
          return false;
        }
      }
      size = invokeNewArray.arguments().size();
    } else if (definition.isNewArrayEmpty()) {
      NewArrayEmpty newArrayEmpty = definition.asNewArrayEmpty();
      Value sizeValue = newArrayEmpty.size().getAliasedValue();
      if (!sizeValue.hasValueRange()) {
        return false;
      }
      LongInterval sizeRange = sizeValue.getValueRange();
      if (!sizeRange.isSingleValue()) {
        return false;
      }
      size = sizeRange.getSingleValue();
    } else {
      // Some other array creation.
      return false;
    }

    if (size < 0) {
      // Check for NegativeArraySizeException.
      return false;
    }

    if (size == 0) {
      // Empty arrays are always constant.
      return true;
    }

    // Allow array-put and new-array-filled-data instructions that immediately follow the array
    // creation.
    Set<Value> arrayValues = Sets.newIdentityHashSet();
    Set<Instruction> consumedInstructions = Sets.newIdentityHashSet();

    for (Instruction instruction : definition.getBlock().instructionsAfter(definition)) {
      if (instruction.isArrayPut()) {
        ArrayPut arrayPut = instruction.asArrayPut();
        Value array = arrayPut.array().getAliasedValue();
        if (array != root) {
          // This ends the chain of array-put instructions that are allowed immediately after the
          // array creation.
          break;
        }

        LongInterval indexRange = arrayPut.index().getValueRange();
        if (!indexRange.isSingleValue()) {
          return false;
        }

        long index = indexRange.getSingleValue();
        if (index < 0 || index >= size) {
          return false;
        }

        // Check if the value being written into the array may depend on the environment.
        //
        // When analyzing if the value may depend on the environment, we assume that the current
        // array does not depend on the environment. Otherwise, we would classify the value as
        // possibly depending on the environment since it could escape via the array and then
        // be mutated indirectly.
        Value rhs = arrayPut.value().getAliasedValue();
        if (!valueMayNotDependOnEnvironmentAssumingArrayDoesNotDependOnEnvironment(
            rhs, root, assumedNotToDependOnEnvironment)) {
          return false;
        }

        arrayValues.add(rhs);
        consumedInstructions.add(arrayPut);
        continue;
      }

      if (instruction.isNewArrayFilledData()) {
        NewArrayFilledData newArrayFilledData = instruction.asNewArrayFilledData();
        Value array = newArrayFilledData.src();
        if (array != root) {
          break;
        }

        consumedInstructions.add(newArrayFilledData);
        continue;
      }

      if (instruction.instructionMayHaveSideEffects(appView, context)) {
        // This ends the chain of array-put instructions that are allowed immediately after the
        // array creation.
        break;
      }
    }

    // Check that the array is not mutated before the end of this method.
    //
    // Currently, we only allow the array to flow into static-put instructions that are not
    // followed by an instruction that may have side effects. Instructions that do not have any
    // side effects are ignored because they cannot mutate the array.
    if (valueMayBeMutatedBeforeMethodExit(
        root, assumedNotToDependOnEnvironment, consumedInstructions)) {
      return false;
    }

    if (assumedNotToDependOnEnvironment.isEmpty()) {
      knownNotToDependOnEnvironment.add(root);
      knownNotToDependOnEnvironment.addAll(arrayValues);
    }

    return true;
  }

  private boolean isNewInstanceWithoutEnvironmentDependentFields(
      Value value, Set<Value> assumedNotToDependOnEnvironment) {
    assert !value.hasAliasedValue();

    if (value.isPhi() || !value.definition.isNewInstance()) {
      return false;
    }

    NewInstance newInstance = value.definition.asNewInstance();
    DexProgramClass clazz = asProgramClassOrNull(appView.definitionFor(newInstance.clazz));
    if (clazz == null) {
      return false;
    }

    // Find the single constructor invocation.
    InvokeMethod constructorInvoke = null;
    for (Instruction instruction : value.uniqueUsers()) {
      if (!instruction.isInvokeDirect()) {
        continue;
      }

      InvokeDirect invoke = instruction.asInvokeDirect();
      if (!appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())) {
        continue;
      }

      if (invoke.getReceiver().getAliasedValue() != value) {
        continue;
      }

      if (constructorInvoke == null) {
        constructorInvoke = invoke;
      } else {
        // Not a single constructor invocation, give up.
        return false;
      }
    }

    if (constructorInvoke == null) {
      // Didn't find a constructor invocation, give up.
      return false;
    }

    // Check that it is a trivial initializer (otherwise, the constructor could do anything).
    DexEncodedMethod constructor = appView.definitionFor(constructorInvoke.getInvokedMethod());
    if (constructor == null) {
      return false;
    }

    if (clazz.hasInstanceFieldsDirectlyOrIndirectly(appView)) {
      InstanceInitializerInfo initializerInfo =
          constructor.getOptimizationInfo().getInstanceInitializerInfo();
      if (initializerInfo.instanceFieldInitializationMayDependOnEnvironment()) {
        return false;
      }

      // Check that none of the arguments to the constructor depend on the environment.
      for (int i = 1; i < constructorInvoke.arguments().size(); i++) {
        Value argument = constructorInvoke.arguments().get(i);
        if (valueMayDependOnEnvironment(argument, assumedNotToDependOnEnvironment)) {
          return false;
        }
      }

      // Finally, check that the object does not escape.
      if (valueMayBeMutatedBeforeMethodExit(
          value, assumedNotToDependOnEnvironment, ImmutableSet.of(constructorInvoke))) {
        return false;
      }
    }

    if (assumedNotToDependOnEnvironment.isEmpty()) {
      knownNotToDependOnEnvironment.add(value);
    }

    return true;
  }

  private boolean valueMayBeMutatedBeforeMethodExit(
      Value value, Set<Value> assumedNotToDependOnEnvironment, Set<Instruction> whitelist) {
    assert !value.hasAliasedValue();

    if (value.numberOfPhiUsers() > 0) {
      // Could be mutated indirectly.
      return true;
    }

    Set<Instruction> visited = Sets.newIdentityHashSet();
    for (Instruction user : value.uniqueUsers()) {
      if (whitelist.contains(user)) {
        continue;
      }

      if (user.isArrayPut()) {
        ArrayPut arrayPut = user.asArrayPut();
        if (value == arrayPut.value()
            && !valueMayDependOnEnvironment(arrayPut.array(), assumedNotToDependOnEnvironment)) {
          continue;
        }
      }

      if (user.isStaticPut()) {
        StaticPut staticPut = user.asStaticPut();
        if (visited.contains(staticPut)) {
          // Already visited previously.
          continue;
        }
        for (Instruction instruction : code.getInstructionsReachableFrom(staticPut)) {
          if (!visited.add(instruction)) {
            // Already visited previously.
            continue;
          }
          if (instruction.isStaticPut()) {
            StaticPut otherStaticPut = instruction.asStaticPut();
            if (otherStaticPut.getField().holder == staticPut.getField().holder
                && instruction.instructionInstanceCanThrow(appView, context).cannotThrow()) {
              continue;
            }
            return true;
          }
          if (instruction.instructionMayTriggerMethodInvocation(appView, context)
              && instruction.instructionMayHaveSideEffects(appView, context)) {
            return true;
          }
        }
        continue;
      }

      // Other user than array-put or static-put, just give up.
      return false;
    }

    return false;
  }

  private boolean isAliasOfValueThatIsIndependentOfEnvironment(
      Value value, Set<Value> assumedNotToDependOnEnvironment) {
    // If we are inside a class initializer, and we are reading a final field of the enclosing
    // class, then check if there is a single write to that field in the class initializer, and that
    // the value being written into that field does not depend on the environment.
    //
    // The reason why we do not currently treat final fields that are written in multiple places is
    // that the value of the field could then be dependent on the environment due to the control
    // flow.
    if (code.method.isClassInitializer()) {
      assert !value.hasAliasedValue();
      if (value.isPhi()) {
        return false;
      }
      Instruction definition = value.definition;
      if (definition.isStaticGet()) {
        StaticGet staticGet = definition.asStaticGet();
        DexEncodedField field = appView.appInfo().resolveField(staticGet.getField());
        if (field != null && field.field.holder == context) {
          List<StaticPut> finalFieldPuts = computeFinalFieldPuts().get(field);
          if (finalFieldPuts == null || finalFieldPuts.size() != 1) {
            return false;
          }
          StaticPut staticPut = ListUtils.first(finalFieldPuts);
          return !valueMayDependOnEnvironment(staticPut.value(), assumedNotToDependOnEnvironment);
        }
      }
    }
    return false;
  }

  private Map<DexEncodedField, List<StaticPut>> computeFinalFieldPuts() {
    assert code.method.isClassInitializer();
    if (finalFieldPuts == null) {
      finalFieldPuts = new IdentityHashMap<>();
      for (StaticPut staticPut : code.<StaticPut>instructions(Instruction::isStaticPut)) {
        DexEncodedField field = appView.appInfo().resolveField(staticPut.getField());
        if (field != null && field.field.holder == context && field.isFinal()) {
          finalFieldPuts.computeIfAbsent(field, ignore -> new ArrayList<>()).add(staticPut);
        }
      }
    }
    return finalFieldPuts;
  }
}
