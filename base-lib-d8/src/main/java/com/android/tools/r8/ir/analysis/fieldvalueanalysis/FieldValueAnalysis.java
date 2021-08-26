// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.fieldvalueanalysis;

import static com.android.tools.r8.ir.code.Opcodes.ARRAY_PUT;
import static com.android.tools.r8.ir.code.Opcodes.INVOKE_DIRECT;
import static com.android.tools.r8.ir.code.Opcodes.STATIC_PUT;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.SingleEnumValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.DominatorTree.Assumption;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.info.OptimizationFeedback;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.DequeUtils;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FieldValueAnalysis {

  private final AppView<AppInfoWithLiveness> appView;
  private final DexProgramClass clazz;
  private final IRCode code;
  private final OptimizationFeedback feedback;
  private final DexEncodedMethod method;

  private Map<BasicBlock, AbstractFieldSet> fieldsMaybeReadBeforeBlockInclusiveCache;

  private FieldValueAnalysis(
      AppView<AppInfoWithLiveness> appView,
      IRCode code,
      OptimizationFeedback feedback,
      DexProgramClass clazz,
      DexEncodedMethod method) {
    assert clazz.type == method.method.holder;
    this.appView = appView;
    this.clazz = clazz;
    this.code = code;
    this.feedback = feedback;
    this.method = method;
    assert this.clazz != null;
  }

  public static void run(
      AppView<?> appView, IRCode code, OptimizationFeedback feedback, DexEncodedMethod method) {
    if (!appView.enableWholeProgramOptimizations()) {
      return;
    }
    assert appView.appInfo().hasLiveness();
    if (!method.isInitializer()) {
      return;
    }
    DexProgramClass clazz = appView.definitionFor(method.method.holder).asProgramClass();
    if (method.isInstanceInitializer()) {
      if (!appView.options().enableValuePropagationForInstanceFields) {
        return;
      }
      DexEncodedMethod otherInstanceInitializer =
          clazz.lookupDirectMethod(other -> other.isInstanceInitializer() && other != method);
      if (otherInstanceInitializer != null) {
        // Conservatively bail out.
        // TODO(b/125282093): Handle multiple instance initializers on the same class.
        return;
      }
    }

    new FieldValueAnalysis(appView.withLiveness(), code, feedback, clazz, method)
        .computeFieldOptimizationInfo();
  }

  private Map<BasicBlock, AbstractFieldSet> getOrCreateFieldsMaybeReadBeforeBlockInclusive() {
    if (fieldsMaybeReadBeforeBlockInclusiveCache == null) {
      fieldsMaybeReadBeforeBlockInclusiveCache = createFieldsMaybeReadBeforeBlockInclusive();
    }
    return fieldsMaybeReadBeforeBlockInclusiveCache;
  }

  /** This method analyzes initializers with the purpose of computing field optimization info. */
  private void computeFieldOptimizationInfo() {
    AppInfoWithLiveness appInfo = appView.appInfo();
    DominatorTree dominatorTree = null;

    DexType context = method.method.holder;

    // Find all the static-put instructions that assign a field in the enclosing class which is
    // guaranteed to be assigned only in the current initializer.
    boolean isStraightLineCode = true;
    Map<DexEncodedField, LinkedList<FieldInstruction>> putsPerField = new IdentityHashMap<>();
    for (Instruction instruction : code.instructions()) {
      if (instruction.isFieldPut()) {
        FieldInstruction fieldPut = instruction.asFieldInstruction();
        DexField field = fieldPut.getField();
        DexEncodedField encodedField = appInfo.resolveField(field);
        if (encodedField != null
            && encodedField.field.holder == context
            && appInfo.isFieldOnlyWrittenInMethod(encodedField, method)) {
          putsPerField.computeIfAbsent(encodedField, ignore -> new LinkedList<>()).add(fieldPut);
        }
      }
      if (instruction.isJumpInstruction()) {
        if (!instruction.isGoto() && !instruction.isReturn()) {
          isStraightLineCode = false;
        }
      }
    }

    List<BasicBlock> normalExitBlocks = code.computeNormalExitBlocks();
    for (Entry<DexEncodedField, LinkedList<FieldInstruction>> entry : putsPerField.entrySet()) {
      DexEncodedField encodedField = entry.getKey();
      LinkedList<FieldInstruction> fieldPuts = entry.getValue();
      if (fieldPuts.size() > 1) {
        continue;
      }
      FieldInstruction fieldPut = fieldPuts.getFirst();
      if (!isStraightLineCode) {
        if (dominatorTree == null) {
          dominatorTree = new DominatorTree(code, Assumption.NO_UNREACHABLE_BLOCKS);
        }
        if (!dominatorTree.dominatesAllOf(fieldPut.getBlock(), normalExitBlocks)) {
          continue;
        }
      }
      if (fieldMaybeReadBeforeInstruction(encodedField, fieldPut)) {
        continue;
      }
      updateFieldOptimizationInfo(encodedField, fieldPut.value());
    }
  }

  private boolean fieldMaybeReadBeforeInstruction(
      DexEncodedField encodedField, Instruction instruction) {
    BasicBlock block = instruction.getBlock();

    // First check if the field may be read in any of the (transitive) predecessor blocks.
    if (fieldMaybeReadBeforeBlock(encodedField, block)) {
      return true;
    }

    // Then check if any of the instructions that precede the given instruction in the current block
    // may read the field.
    DexType context = method.method.holder;
    InstructionIterator instructionIterator = block.iterator();
    while (instructionIterator.hasNext()) {
      Instruction current = instructionIterator.next();
      if (current == instruction) {
        break;
      }
      if (current.readSet(appView, context).contains(encodedField)) {
        return true;
      }
    }

    // Otherwise, the field is not read prior to the given instruction.
    return false;
  }

  private boolean fieldMaybeReadBeforeBlock(DexEncodedField encodedField, BasicBlock block) {
    for (BasicBlock predecessor : block.getPredecessors()) {
      if (fieldMaybeReadBeforeBlockInclusive(encodedField, predecessor)) {
        return true;
      }
    }
    return false;
  }

  private boolean fieldMaybeReadBeforeBlockInclusive(
      DexEncodedField encodedField, BasicBlock block) {
    return getOrCreateFieldsMaybeReadBeforeBlockInclusive().get(block).contains(encodedField);
  }

  /**
   * Eagerly creates a mapping from each block to the set of fields that may be read in that block
   * and its transitive predecessors.
   */
  private Map<BasicBlock, AbstractFieldSet> createFieldsMaybeReadBeforeBlockInclusive() {
    DexType context = method.method.holder;
    Map<BasicBlock, AbstractFieldSet> result = new IdentityHashMap<>();
    Deque<BasicBlock> worklist = DequeUtils.newArrayDeque(code.entryBlock());
    while (!worklist.isEmpty()) {
      BasicBlock block = worklist.removeFirst();
      boolean seenBefore = result.containsKey(block);
      AbstractFieldSet readSet =
          result.computeIfAbsent(block, ignore -> EmptyFieldSet.getInstance());
      if (readSet.isTop()) {
        // We already have unknown information for this block.
        continue;
      }

      assert readSet.isKnownFieldSet();
      KnownFieldSet knownReadSet = readSet.asKnownFieldSet();
      int oldSize = seenBefore ? knownReadSet.size() : -1;

      // Everything that is read in the predecessor blocks should also be included in the read set
      // for the current block, so here we join the information from the predecessor blocks into the
      // current read set.
      boolean blockOrPredecessorMaybeReadAnyField = false;
      for (BasicBlock predecessor : block.getPredecessors()) {
        AbstractFieldSet predecessorReadSet =
            result.getOrDefault(predecessor, EmptyFieldSet.getInstance());
        if (predecessorReadSet.isBottom()) {
          continue;
        }
        if (predecessorReadSet.isTop()) {
          blockOrPredecessorMaybeReadAnyField = true;
          break;
        }
        assert predecessorReadSet.isConcreteFieldSet();
        if (!knownReadSet.isConcreteFieldSet()) {
          knownReadSet = new ConcreteMutableFieldSet();
        }
        knownReadSet.asConcreteFieldSet().addAll(predecessorReadSet.asConcreteFieldSet());
      }

      if (!blockOrPredecessorMaybeReadAnyField) {
        // Finally, we update the read set with the fields that are read by the instructions in the
        // current block.
        for (Instruction instruction : block.getInstructions()) {
          AbstractFieldSet instructionReadSet = instruction.readSet(appView, context);
          if (instructionReadSet.isBottom()) {
            continue;
          }
          if (instructionReadSet.isTop()) {
            blockOrPredecessorMaybeReadAnyField = true;
            break;
          }
          if (!knownReadSet.isConcreteFieldSet()) {
            knownReadSet = new ConcreteMutableFieldSet();
          }
          knownReadSet.asConcreteFieldSet().addAll(instructionReadSet.asConcreteFieldSet());
        }
      }

      boolean changed = false;
      if (blockOrPredecessorMaybeReadAnyField) {
        // Record that this block reads all fields.
        result.put(block, UnknownFieldSet.getInstance());
        changed = true;
      } else if (knownReadSet.size() != oldSize) {
        assert knownReadSet.size() > oldSize;
        changed = true;
      }

      if (changed) {
        // Rerun the analysis for all successors because the state of the current block changed.
        worklist.addAll(block.getSuccessors());
      }
    }
    return result;
  }

  private void updateFieldOptimizationInfo(DexEncodedField field, Value value) {
    // Abstract value.
    Value root = value.getAliasedValue();
    AbstractValue abstractValue = computeAbstractValue(root);
    if (!abstractValue.isUnknown()) {
      feedback.recordFieldHasAbstractValue(field, appView, abstractValue);
    }

    // Dynamic upper bound type.
    TypeLatticeElement fieldType =
        TypeLatticeElement.fromDexType(field.field.type, Nullability.maybeNull(), appView);
    TypeLatticeElement dynamicUpperBoundType = value.getDynamicUpperBoundType(appView);
    if (dynamicUpperBoundType.strictlyLessThan(fieldType, appView)) {
      feedback.markFieldHasDynamicUpperBoundType(field, dynamicUpperBoundType);
    }

    // Dynamic lower bound type.
    ClassTypeLatticeElement dynamicLowerBoundType = value.getDynamicLowerBoundType(appView);
    if (dynamicLowerBoundType != null) {
      assert dynamicLowerBoundType.lessThanOrEqual(dynamicUpperBoundType, appView);
      feedback.markFieldHasDynamicLowerBoundType(field, dynamicLowerBoundType);
    }
  }

  private AbstractValue computeAbstractValue(Value value) {
    assert !value.hasAliasedValue();
    if (clazz.isEnum()) {
      SingleEnumValue singleEnumValue = getSingleEnumValue(value);
      if (singleEnumValue != null) {
        return singleEnumValue;
      }
    }
    if (!value.isPhi()) {
      return value.definition.getAbstractValue(appView, clazz.type);
    }
    return UnknownValue.getInstance();
  }

  /**
   * If {@param value} is defined by a new-instance instruction that instantiates the enclosing enum
   * class, and the value is assigned into exactly one static enum field on the enclosing enum
   * class, then returns a {@link SingleEnumValue} instance. Otherwise, returns {@code null}.
   *
   * <p>Note that enum constructors also store the newly instantiated enums in the {@code $VALUES}
   * array field on the enum. Therefore, this code also allows {@param value} to be stored into an
   * array as long as the array is identified as being the {@code $VALUES} array.
   */
  private SingleEnumValue getSingleEnumValue(Value value) {
    assert clazz.isEnum();
    assert !value.hasAliasedValue();
    if (value.isPhi() || !value.definition.isNewInstance()) {
      return null;
    }

    NewInstance newInstance = value.definition.asNewInstance();
    if (newInstance.clazz != clazz.type) {
      return null;
    }

    if (value.hasDebugUsers() || value.hasPhiUsers()) {
      return null;
    }

    DexEncodedField enumField = null;
    for (Instruction user : value.uniqueUsers()) {
      switch (user.opcode()) {
        case ARRAY_PUT:
          // Check that this is assigning the enum into the enum values array.
          ArrayPut arrayPut = user.asArrayPut();
          if (arrayPut.value().getAliasedValue() != value || !isEnumValuesArray(arrayPut.array())) {
            return null;
          }
          break;

        case INVOKE_DIRECT:
          // Check that this is the corresponding constructor call.
          InvokeDirect invoke = user.asInvokeDirect();
          if (!appView.dexItemFactory().isConstructor(invoke.getInvokedMethod())
              || invoke.getReceiver() != value) {
            return null;
          }
          break;

        case STATIC_PUT:
          DexEncodedField field = clazz.lookupStaticField(user.asStaticPut().getField());
          if (field != null && field.accessFlags.isEnum()) {
            if (enumField != null) {
              return null;
            }
            enumField = field;
          }
          break;

        default:
          return null;
      }
    }

    if (enumField == null) {
      return null;
    }

    return appView.abstractValueFactory().createSingleEnumValue(enumField.field);
  }

  private boolean isEnumValuesArray(Value value) {
    assert clazz.isEnum();
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    DexField valuesField =
        dexItemFactory.createField(
            clazz.type,
            clazz.type.toArrayType(1, dexItemFactory),
            dexItemFactory.enumValuesFieldName);

    Value root = value.getAliasedValue();
    if (root.isPhi()) {
      return false;
    }

    Instruction definition = root.definition;
    if (definition.isNewArrayEmpty()) {
      for (Instruction user : root.aliasedUsers()) {
        if (user.isStaticPut() && user.asStaticPut().getField() == valuesField) {
          return true;
        }
      }
    } else if (definition.isStaticGet()) {
      return definition.asStaticGet().getField() == valuesField;
    }

    return false;
  }
}
