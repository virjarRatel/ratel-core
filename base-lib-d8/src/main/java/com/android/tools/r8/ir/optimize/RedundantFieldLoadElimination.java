// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Eliminate redundant field loads.
 *
 * <p>Simple algorithm that goes through all blocks in one pass in dominator order and propagates
 * active field sets across control-flow edges where the target has only one predecessor.
 */
// TODO(ager): Evaluate speed/size for computing active field sets in a fixed-point computation.
public class RedundantFieldLoadElimination {

  private final AppView<?> appView;
  private final DexEncodedMethod method;
  private final IRCode code;
  private final DominatorTree dominatorTree;

  // Values that may require type propagation.
  private final Set<Value> affectedValues = Sets.newIdentityHashSet();

  // Maps keeping track of fields that have an already loaded value at basic block entry.
  private final Map<BasicBlock, Map<FieldAndObject, FieldInstruction>> activeInstanceFieldsAtEntry =
      new IdentityHashMap<>();
  private final Map<BasicBlock, Map<DexField, FieldInstruction>> activeStaticFieldsAtEntry =
      new IdentityHashMap<>();

  // Maps keeping track of fields with already loaded values for the current block during
  // elimination.
  private Map<FieldAndObject, FieldInstruction> activeInstanceFields;
  private Map<DexField, FieldInstruction> activeStaticFields;

  public RedundantFieldLoadElimination(AppView<?> appView, IRCode code) {
    this.appView = appView;
    this.method = code.method;
    this.code = code;
    dominatorTree = new DominatorTree(code);
  }

  public static boolean shouldRun(AppView<?> appView, IRCode code) {
    return appView.options().enableRedundantFieldLoadElimination
        && code.metadata().mayHaveFieldGet();
  }

  private static class FieldAndObject {
    private final DexField field;
    private final Value object;

    private FieldAndObject(DexField field, Value receiver) {
      assert receiver == receiver.getAliasedValue();
      this.field = field;
      this.object = receiver;
    }

    @Override
    public int hashCode() {
      return field.hashCode() * 7 + object.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof FieldAndObject)) {
        return false;
      }
      FieldAndObject o = (FieldAndObject) other;
      return o.object == object && o.field == field;
    }
  }

  private boolean couldBeVolatile(DexField field) {
    if (!appView.enableWholeProgramOptimizations()) {
      if (field.holder != method.method.holder) {
        return true;
      }
      DexEncodedField definition = appView.definitionFor(field);
      return definition == null || definition.accessFlags.isVolatile();
    }
    DexEncodedField definition = appView.appInfo().resolveField(field);
    return definition == null || definition.accessFlags.isVolatile();
  }

  public void run() {
    DexType context = method.method.holder;
    for (BasicBlock block : dominatorTree.getSortedBlocks()) {
      activeInstanceFields =
          activeInstanceFieldsAtEntry.containsKey(block)
              ? activeInstanceFieldsAtEntry.get(block)
              : new HashMap<>();
      activeStaticFields =
          activeStaticFieldsAtEntry.containsKey(block)
              ? activeStaticFieldsAtEntry.get(block)
              : new IdentityHashMap<>();
      InstructionListIterator it = block.listIterator(code);
      while (it.hasNext()) {
        Instruction instruction = it.next();
        if (instruction.isFieldInstruction()) {
          DexField field = instruction.asFieldInstruction().getField();
          if (couldBeVolatile(field)) {
            killAllActiveFields();
            continue;
          }

          assert !couldBeVolatile(field);

          if (instruction.isInstanceGet()) {
            InstanceGet instanceGet = instruction.asInstanceGet();
            if (instanceGet.outValue().hasLocalInfo()) {
              continue;
            }
            Value object = instanceGet.object().getAliasedValue();
            FieldAndObject fieldAndObject = new FieldAndObject(field, object);
            if (activeInstanceFields.containsKey(fieldAndObject)) {
              FieldInstruction active = activeInstanceFields.get(fieldAndObject);
              eliminateRedundantRead(it, instanceGet, active);
            } else {
              activeInstanceFields.put(fieldAndObject, instanceGet);
            }
          } else if (instruction.isInstancePut()) {
            InstancePut instancePut = instruction.asInstancePut();
            // An instance-put instruction can potentially write the given field on all objects
            // because of aliases.
            killActiveFields(instancePut);
            // ... but at least we know the field value for this particular object.
            Value object = instancePut.object().getAliasedValue();
            FieldAndObject fieldAndObject = new FieldAndObject(field, object);
            activeInstanceFields.put(fieldAndObject, instancePut);
          } else if (instruction.isStaticGet()) {
            StaticGet staticGet = instruction.asStaticGet();
            if (staticGet.outValue().hasLocalInfo()) {
              continue;
            }
            if (activeStaticFields.containsKey(field)) {
              FieldInstruction active = activeStaticFields.get(field);
              eliminateRedundantRead(it, staticGet, active);
            } else {
              // A field get on a different class can cause <clinit> to run and change static
              // field values.
              killActiveFields(staticGet);
              activeStaticFields.put(field, staticGet);
            }
          } else if (instruction.isStaticPut()) {
            StaticPut staticPut = instruction.asStaticPut();
            // A field put on a different class can cause <clinit> to run and change static
            // field values.
            killActiveFields(staticPut);
            activeStaticFields.put(field, staticPut);
          }
        } else if (instruction.isMonitor()) {
          if (instruction.asMonitor().isEnter()) {
            killAllActiveFields();
          }
        } else if (instruction.isInvokeMethod() || instruction.isInvokeCustom()) {
          killAllActiveFields();
        } else if (instruction.isNewInstance()) {
          NewInstance newInstance = instruction.asNewInstance();
          if (newInstance.clazz.classInitializationMayHaveSideEffects(
              appView,
              // Types that are a super type of `context` are guaranteed to be initialized already.
              type -> appView.isSubtype(context, type).isTrue())) {
            killAllActiveFields();
          }
        } else {
          // If the current instruction could trigger a method invocation, it could also cause field
          // values to change. In that case, it must be handled above.
          assert !instruction.instructionMayTriggerMethodInvocation(appView, context);

          // If this assertion fails for a new instruction we need to determine if that instruction
          // has side-effects that can change the value of fields. If so, it must be handled above.
          // If not, it can be safely added to the assert.
          assert instruction.isArgument()
                  || instruction.isArrayGet()
                  || instruction.isArrayLength()
                  || instruction.isArrayPut()
                  || instruction.isAssume()
                  || instruction.isBinop()
                  || instruction.isCheckCast()
                  || instruction.isConstClass()
                  || instruction.isConstMethodHandle()
                  || instruction.isConstMethodType()
                  || instruction.isConstNumber()
                  || instruction.isConstString()
                  || instruction.isDebugInstruction()
                  || instruction.isDexItemBasedConstString()
                  || instruction.isGoto()
                  || instruction.isIf()
                  || instruction.isInstanceOf()
                  || instruction.isInvokeMultiNewArray()
                  || instruction.isInvokeNewArray()
                  || instruction.isMoveException()
                  || instruction.isNewArrayEmpty()
                  || instruction.isNewArrayFilledData()
                  || instruction.isReturn()
                  || instruction.isSwitch()
                  || instruction.isThrow()
                  || instruction.isUnop()
              : "Unexpected instruction of type " + instruction.getClass().getTypeName();
        }
      }
      propagateActiveFieldsFrom(block);
    }
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
    assert code.isConsistentSSA();
  }

  private void propagateActiveFieldsFrom(BasicBlock block) {
    for (BasicBlock successor : block.getSuccessors()) {
      // Allow propagation across exceptional edges, just be careful not to propagate if the
      // throwing instruction is a field instruction.
      if (successor.getPredecessors().size() == 1) {
        if (block.hasCatchSuccessor(successor)) {
          Instruction exceptionalExit = block.exceptionalExit();
          if (exceptionalExit != null && exceptionalExit.isFieldInstruction()) {
            killActiveFieldsForExceptionalExit(exceptionalExit.asFieldInstruction());
          }
        }
        assert !activeInstanceFieldsAtEntry.containsKey(successor);
        activeInstanceFieldsAtEntry.put(successor, new HashMap<>(activeInstanceFields));
        assert !activeStaticFieldsAtEntry.containsKey(successor);
        activeStaticFieldsAtEntry.put(successor, new IdentityHashMap<>(activeStaticFields));
      }
    }
  }

  private void killAllActiveFields() {
    activeInstanceFields.clear();
    activeStaticFields.clear();
  }

  private void killActiveFields(FieldInstruction instruction) {
    DexField field = instruction.getField();
    if (instruction.isInstancePut()) {
      // Remove all the field/object pairs that refer to this field to make sure
      // that we are conservative.
      List<FieldAndObject> keysToRemove = new ArrayList<>();
      for (FieldAndObject key : activeInstanceFields.keySet()) {
        if (key.field == field) {
          keysToRemove.add(key);
        }
      }
      keysToRemove.forEach(activeInstanceFields::remove);
    } else if (instruction.isStaticPut()) {
      if (field.holder != code.method.method.holder) {
        // Accessing a static field on a different object could cause <clinit> to run which
        // could modify any static field on any other object.
        activeStaticFields.clear();
      } else {
        activeStaticFields.remove(field);
      }
    } else if (instruction.isStaticGet()) {
      if (field.holder != code.method.method.holder) {
        // Accessing a static field on a different object could cause <clinit> to run which
        // could modify any static field on any other object.
        activeStaticFields.clear();
      }
    } else if (instruction.isInstanceGet()) {
      throw new Unreachable();
    }
  }

  // If a field get instruction throws an exception it did not have an effect on the
  // value of the field. Therefore, when propagating across exceptional edges for a
  // field get instruction we have to exclude that field from the set of known
  // field values.
  private void killActiveFieldsForExceptionalExit(FieldInstruction instruction) {
    DexField field = instruction.getField();
    if (instruction.isInstanceGet()) {
      Value object = instruction.asInstanceGet().object().getAliasedValue();
      FieldAndObject fieldAndObject = new FieldAndObject(field, object);
      activeInstanceFields.remove(fieldAndObject);
    } else if (instruction.isStaticGet()) {
      activeStaticFields.remove(field);
    }
  }

  private void eliminateRedundantRead(
      InstructionListIterator it, FieldInstruction redundant, FieldInstruction active) {
    affectedValues.addAll(redundant.value().affectedValues());
    redundant.value().replaceUsers(active.value());
    it.removeOrReplaceByDebugLocalRead();
    active.value().uniquePhiUsers().forEach(Phi::removeTrivialPhi);
  }
}
