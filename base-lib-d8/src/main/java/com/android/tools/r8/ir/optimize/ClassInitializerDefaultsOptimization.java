// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.naming.dexitembasedstring.ClassNameComputationInfo.ClassNameMapping.CANONICAL_NAME;
import static com.android.tools.r8.naming.dexitembasedstring.ClassNameComputationInfo.ClassNameMapping.NAME;
import static com.android.tools.r8.naming.dexitembasedstring.ClassNameComputationInfo.ClassNameMapping.SIMPLE_NAME;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexItemBasedValueString;
import com.android.tools.r8.graph.DexValue.DexValueBoolean;
import com.android.tools.r8.graph.DexValue.DexValueByte;
import com.android.tools.r8.graph.DexValue.DexValueChar;
import com.android.tools.r8.graph.DexValue.DexValueDouble;
import com.android.tools.r8.graph.DexValue.DexValueFloat;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueLong;
import com.android.tools.r8.graph.DexValue.DexValueNull;
import com.android.tools.r8.graph.DexValue.DexValueShort;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.ir.analysis.ValueMayDependOnEnvironmentAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.FieldInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.naming.dexitembasedstring.ClassNameComputationInfo;
import com.android.tools.r8.naming.dexitembasedstring.ClassNameComputationInfo.ClassNameMapping;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Action;
import com.android.tools.r8.utils.IteratorUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassInitializerDefaultsOptimization {

  private class WaveDoneAction implements Action {

    private final Map<DexEncodedField, DexValue> fieldsWithStaticValues;
    private final Set<DexField> noLongerWrittenFields;

    public WaveDoneAction(
        Map<DexEncodedField, DexValue> fieldsWithStaticValues,
        Set<DexField> noLongerWrittenFields) {
      this.fieldsWithStaticValues = fieldsWithStaticValues;
      this.noLongerWrittenFields = noLongerWrittenFields;
    }

    public synchronized void join(
        Map<DexEncodedField, DexValue> fieldsWithStaticValues,
        Set<DexField> noLongerWrittenFields) {
      this.fieldsWithStaticValues.putAll(fieldsWithStaticValues);
      this.noLongerWrittenFields.addAll(noLongerWrittenFields);
    }

    @Override
    public void execute() {
      // Update AppInfo.
      AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
      appViewWithLiveness.setAppInfo(
          appViewWithLiveness.appInfo().withoutStaticFieldsWrites(noLongerWrittenFields));

      // Update static field values of classes.
      fieldsWithStaticValues.forEach(DexEncodedField::setStaticValue);
    }
  }

  private final AppView<?> appView;
  private final IRConverter converter;
  private final DexItemFactory dexItemFactory;

  private WaveDoneAction waveDoneAction = null;

  public ClassInitializerDefaultsOptimization(AppView<?> appView, IRConverter converter) {
    this.appView = appView;
    this.converter = converter;
    this.dexItemFactory = appView.dexItemFactory();
  }

  public void optimize(DexEncodedMethod method, IRCode code) {
    if (!method.isClassInitializer()) {
      return;
    }

    DexClass clazz = appView.definitionFor(method.method.holder);
    if (clazz == null) {
      return;
    }

    // Collect straight-line static puts up to the first side-effect that is not
    // a static put on a field on this class with a value that can be hoisted to
    // the field initial value.
    Set<StaticPut> unnecessaryStaticPuts = Sets.newIdentityHashSet();
    Collection<StaticPut> finalFieldPuts =
        findFinalFieldPutsWhileCollectingUnnecessaryStaticPuts(code, clazz, unnecessaryStaticPuts);

    // Return eagerly if there is nothing to optimize.
    if (finalFieldPuts.isEmpty()) {
      assert unnecessaryStaticPuts.isEmpty();
      return;
    }

    Map<DexEncodedField, DexValue> fieldsWithStaticValues = new IdentityHashMap<>();

    // Set initial values for static fields from the definitive static put instructions collected.
    for (StaticPut put : finalFieldPuts) {
      DexEncodedField field = appView.appInfo().resolveField(put.getField());
      DexType fieldType = field.field.type;
      Value value = put.value();
      if (unnecessaryStaticPuts.contains(put)) {
        if (fieldType == dexItemFactory.stringType) {
          fieldsWithStaticValues.put(field, getDexStringValue(value, method.method.holder));
        } else if (fieldType.isClassType() || fieldType.isArrayType()) {
          if (value.isZero()) {
            fieldsWithStaticValues.put(field, DexValueNull.NULL);
          } else {
            throw new Unreachable("Unexpected default value for field type " + fieldType + ".");
          }
        } else {
          ConstNumber cnst = value.getConstInstruction().asConstNumber();
          if (fieldType == dexItemFactory.booleanType) {
            fieldsWithStaticValues.put(field, DexValueBoolean.create(cnst.getBooleanValue()));
          } else if (fieldType == dexItemFactory.byteType) {
            fieldsWithStaticValues.put(field, DexValueByte.create((byte) cnst.getIntValue()));
          } else if (fieldType == dexItemFactory.shortType) {
            fieldsWithStaticValues.put(field, DexValueShort.create((short) cnst.getIntValue()));
          } else if (fieldType == dexItemFactory.intType) {
            fieldsWithStaticValues.put(field, DexValueInt.create(cnst.getIntValue()));
          } else if (fieldType == dexItemFactory.longType) {
            fieldsWithStaticValues.put(field, DexValueLong.create(cnst.getLongValue()));
          } else if (fieldType == dexItemFactory.floatType) {
            fieldsWithStaticValues.put(field, DexValueFloat.create(cnst.getFloatValue()));
          } else if (fieldType == dexItemFactory.doubleType) {
            fieldsWithStaticValues.put(field, DexValueDouble.create(cnst.getDoubleValue()));
          } else if (fieldType == dexItemFactory.charType) {
            fieldsWithStaticValues.put(field, DexValueChar.create((char) cnst.getIntValue()));
          } else {
            throw new Unreachable("Unexpected field type " + fieldType + ".");
          }
        }
      }
    }

    if (!unnecessaryStaticPuts.isEmpty()) {
      // Remove the static put instructions now replaced by static field initial values.
      Set<Instruction> unnecessaryInstructions = Sets.newIdentityHashSet();

      // Note: Traversing code.instructions(), and not unnecessaryStaticPuts(), to ensure
      // deterministic iteration order.
      InstructionListIterator instructionIterator = code.instructionListIterator();
      while (instructionIterator.hasNext()) {
        Instruction instruction = instructionIterator.next();
        if (!instruction.isStaticPut()
            || !unnecessaryStaticPuts.contains(instruction.asStaticPut())) {
          continue;
        }
        // Get a hold of the in-value.
        Value inValue = instruction.asStaticPut().value();

        // Remove the static-put instruction.
        instructionIterator.removeOrReplaceByDebugLocalRead();

        // Collect, for removal, the instruction that created the value for the static put,
        // if all users are gone. This is done even if these instructions can throw as for
        // the current patterns matched these exceptions are not detectable.
        if (inValue.numberOfAllUsers() > 0) {
          continue;
        }
        if (inValue.isConstString()) {
          unnecessaryInstructions.add(inValue.definition);
        } else if (!inValue.isPhi() && inValue.definition.isInvokeVirtual()) {
          unnecessaryInstructions.add(inValue.definition);
        }
      }

      // Remove the instructions collected for removal.
      if (unnecessaryInstructions.size() > 0) {
        IteratorUtils.removeIf(code.instructionListIterator(), unnecessaryInstructions::contains);
      }
    }

    // If we are in R8, and we have removed all static-put instructions to some field, then record
    // that the field is no longer written.
    if (appView.enableWholeProgramOptimizations() && converter.isInWave()) {
      if (appView.appInfo().hasLiveness()) {
        AppView<AppInfoWithLiveness> appViewWithLiveness = appView.withLiveness();
        AppInfoWithLiveness appInfoWithLiveness = appViewWithLiveness.appInfo();

        // First collect all the candidate fields that are *potentially* no longer being written to.
        Set<DexField> candidates =
            finalFieldPuts.stream()
                .filter(unnecessaryStaticPuts::contains)
                .map(FieldInstruction::getField)
                .map(appInfoWithLiveness::resolveField)
                .filter(appInfoWithLiveness::isStaticFieldWrittenOnlyInEnclosingStaticInitializer)
                .map(field -> field.field)
                .collect(Collectors.toSet());

        // Then retain only these fields that are actually no longer being written to.
        for (Instruction instruction : code.instructions()) {
          if (instruction.isStaticPut()) {
            StaticPut staticPutInstruction = instruction.asStaticPut();
            DexField field = staticPutInstruction.getField();
            DexEncodedField encodedField = appInfoWithLiveness.resolveField(field);
            if (encodedField != null) {
              candidates.remove(encodedField.field);
            }
          }
        }

        // Finally, remove these fields from the set of assigned static fields.
        synchronized (this) {
          if (waveDoneAction == null) {
            waveDoneAction = new WaveDoneAction(fieldsWithStaticValues, candidates);
            converter.addWaveDoneAction(
                () -> {
                  waveDoneAction.execute();
                  waveDoneAction = null;
                });
          } else {
            waveDoneAction.join(fieldsWithStaticValues, candidates);
          }
        }
      } else {
        assert false;
      }
    } else {
      fieldsWithStaticValues.forEach(DexEncodedField::setStaticValue);
    }
  }

  private DexValue getDexStringValue(Value inValue, DexType holder) {
    if (inValue.isPhi()) {
      return null;
    }
    if (inValue.isConstant()) {
      if (inValue.isConstNumber()) {
        assert inValue.isZero();
        return DexValueNull.NULL;
      }
      if (inValue.isConstString()) {
        ConstString cnst = inValue.getConstInstruction().asConstString();
        return new DexValueString(cnst.getValue());
      }
      if (inValue.isDexItemBasedConstString()) {
        DexItemBasedConstString cnst = inValue.getConstInstruction().asDexItemBasedConstString();
        assert !cnst.getNameComputationInfo().needsToComputeName();
        return new DexItemBasedValueString(cnst.getItem(), cnst.getNameComputationInfo());
      }
      assert false;
      return null;
    }

    // If it is not a constant it must be the result of a virtual invoke to one of the
    // reflective lookup methods.
    InvokeVirtual invoke = inValue.getAliasedValue().definition.asInvokeVirtual();
    return getDexStringValueForInvoke(invoke.getInvokedMethod(), holder);
  }

  private DexValue getDexStringValueForInvoke(DexMethod invokedMethod, DexType holder) {
    DexClass clazz = appView.definitionFor(holder);
    if (clazz == null) {
      assert false;
      return null;
    }

    if (appView.options().isMinifying() && appView.rootSet().mayBeMinified(holder, appView)) {
      if (invokedMethod == dexItemFactory.classMethods.getName) {
        return new DexItemBasedValueString(holder, ClassNameComputationInfo.getInstance(NAME));
      }
      if (invokedMethod == dexItemFactory.classMethods.getCanonicalName) {
        return new DexItemBasedValueString(
            holder, ClassNameComputationInfo.getInstance(CANONICAL_NAME));
      }
      if (invokedMethod == dexItemFactory.classMethods.getSimpleName) {
        return new DexItemBasedValueString(
            holder, ClassNameComputationInfo.getInstance(SIMPLE_NAME));
      }
      if (invokedMethod == dexItemFactory.classMethods.getTypeName) {
        // TODO(b/119426668): desugar Type#getTypeName
      }
      assert false;
      return null;
    }

    ClassNameMapping mapping = null;
    if (invokedMethod == dexItemFactory.classMethods.getName) {
      mapping = NAME;
    } else if (invokedMethod == dexItemFactory.classMethods.getCanonicalName) {
      mapping = CANONICAL_NAME;
    } else if (invokedMethod == dexItemFactory.classMethods.getSimpleName) {
      mapping = SIMPLE_NAME;
    } else if (invokedMethod == dexItemFactory.classMethods.getTypeName) {
      // TODO(b/119426668): desugar Type#getTypeName
    }
    if (mapping != null) {
      return new DexValueString(mapping.map(holder.toDescriptorString(), clazz, dexItemFactory));
    }
    assert false;
    return null;
  }

  private Collection<StaticPut> findFinalFieldPutsWhileCollectingUnnecessaryStaticPuts(
      IRCode code, DexClass clazz, Set<StaticPut> unnecessaryStaticPuts) {
    ValueMayDependOnEnvironmentAnalysis environmentAnalysis =
        new ValueMayDependOnEnvironmentAnalysis(appView, code);
    Map<DexField, StaticPut> finalFieldPuts = Maps.newIdentityHashMap();
    Map<DexField, Set<StaticPut>> isWrittenBefore = Maps.newIdentityHashMap();
    Set<DexField> isReadBefore = Sets.newIdentityHashSet();
    final int color = code.reserveMarkingColor();
    try {
      BasicBlock block = code.entryBlock();
      while (!block.isMarked(color) && block.getPredecessors().size() <= 1) {
        block.mark(color);
        for (Instruction instruction : block.getInstructions()) {
          if (instruction.isArrayPut()) {
            // Array stores do not impact our ability to move constants into the class definition,
            // as long as the instructions do not throw.
            ArrayPut arrayPut = instruction.asArrayPut();
            if (arrayPut.instructionInstanceCanThrow(appView, clazz.type).isThrowing()) {
              return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
            }
          } else if (instruction.isStaticGet()) {
            StaticGet get = instruction.asStaticGet();
            DexEncodedField field = appView.appInfo().resolveField(get.getField());
            if (field != null && field.field.holder == clazz.type) {
              isReadBefore.add(field.field);
            } else if (instruction.instructionMayHaveSideEffects(appView, clazz.type)) {
              // Reading another field is only OK if the read does not have side-effects.
              return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
            }
          } else if (instruction.isStaticPut()) {
            StaticPut put = instruction.asStaticPut();
            if (put.getField().holder != clazz.type) {
              // Can cause clinit on another class which can read uninitialized static fields
              // of this class.
              return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
            }
            DexField field = put.getField();
            Value value = put.value();
            TypeLatticeElement valueType = value.getTypeLattice();
            if (clazz.definesStaticField(field)) {
              if (isReadBefore.contains(field)) {
                // Promoting this put to a class constant would cause a previous static-get
                // instruction to read a different value.
                continue;
              }
              if (value.isDexItemBasedConstStringThatNeedsToComputeClassName()) {
                continue;
              }
              if (value.isConstant()) {
                if (field.type.isReferenceType() && value.isZero()) {
                  finalFieldPuts.put(field, put);
                  unnecessaryStaticPuts.add(put);
                  // If this field has been written before, those static-put's up to this point are
                  // redundant. We should remove them all together; otherwise, remaining static-put
                  // that is not constant can change the program semantics. See b/138912149.
                  if (isWrittenBefore.containsKey(field)) {
                    unnecessaryStaticPuts.addAll(isWrittenBefore.get(field));
                    isWrittenBefore.remove(field);
                  }
                  continue;
                } else if (field.type.isPrimitiveType()
                    || field.type == dexItemFactory.stringType) {
                  finalFieldPuts.put(field, put);
                  unnecessaryStaticPuts.add(put);
                  if (isWrittenBefore.containsKey(field)) {
                    unnecessaryStaticPuts.addAll(isWrittenBefore.get(field));
                    isWrittenBefore.remove(field);
                  }
                  continue;
                }
                // Still constant, but not able to represent it as static encoded values, e.g.,
                // const-class, const-method-handle, etc. This static-put can be redundant if the
                // field is rewritten with another constant. Will fall through and track static-put.
              } else if (isClassNameConstantOf(clazz, put)) {
                // Collect put of class name constant as a potential default value.
                finalFieldPuts.put(field, put);
                unnecessaryStaticPuts.add(put);
                if (isWrittenBefore.containsKey(field)) {
                  unnecessaryStaticPuts.addAll(isWrittenBefore.get(field));
                  isWrittenBefore.remove(field);
                }
                continue;
              } else if (valueType.isReference() && valueType.isDefinitelyNotNull()) {
                finalFieldPuts.put(field, put);
                continue;
              }
              // static-put that is reaching here can be redundant if the corresponding field is
              // rewritten with another constant (of course before being read).
              // However, if static-put is still remaining in `isWrittenBefore`, that indicates
              // the previous candidate as final field put is no longer valid.
              isWrittenBefore
                  .computeIfAbsent(field, ignore -> Sets.newIdentityHashSet())
                  .add(put);
            } else {
              // Writing another field is not OK.
              return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
            }
          } else if (instruction.instructionMayHaveSideEffects(appView, clazz.type)) {
            // Some other instruction that has side-effects. Stop here.
            return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
          } else {
            // TODO(b/120138731): This check should be removed when the Class.get*Name()
            // optimizations become enabled.
            if (isClassNameConstantOf(clazz, instruction)) {
              // OK, this does not read one of the fields in the enclosing class.
              continue;
            }
            // Give up if this invoke-instruction may return a value that has been computed based on
            // the value of one of the fields in the enclosing class.
            if (instruction.isInvoke() && instruction.hasOutValue()) {
              Value outValue = instruction.outValue();
              if (outValue.numberOfAllUsers() > 0) {
                if (instruction.isInvokeNewArray()
                    && environmentAnalysis.isConstantArrayThroughoutMethod(outValue)) {
                  // OK, this value is technically a constant.
                  continue;
                }
                return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
              }
            }
          }
        }
        if (block.exit().isGoto()) {
          block = block.exit().asGoto().getTarget();
        }
      }
    } finally {
      code.returnMarkingColor(color);
    }
    return validateFinalFieldPuts(finalFieldPuts, isWrittenBefore);
  }

  private Collection<StaticPut> validateFinalFieldPuts(
      Map<DexField, StaticPut> finalFieldPuts,
      Map<DexField, Set<StaticPut>> isWrittenBefore) {
    // If a field is rewritten again with other values that we can't represent as static encoded
    // values, that would be recorded at `isWrittenBefore`, which is used to collect and remove
    // redundant static-puts. The remnant indicates that the candidate for final field put is not
    // valid anymore, so remove it.
    //
    // For example,
    //   static String x;
    //
    //   static {
    //     x = "constant";     // will be in finalFieldPut
    //     x = <non-constant>  // will be added to isWrittenBefore
    //     // x = "another-constant"
    //   }
    // If there is another static-put with a constant, static-put stored in `isWrittenBefore` is
    // used to remove redundant static-put together. (And the previous constant is overwritten.)
    // If not, `x` has "constant" as a default value, whereas static-put with non-constant is
    // remaining. If other optimizations (most likely member value propagation) rely on encoded
    // values, leaving it can cause incorrect optimizations. Thus, we invalidate candidates of
    // final field puts at all.
    isWrittenBefore.keySet().forEach(finalFieldPuts::remove);
    return finalFieldPuts.values();
  }

  // Check if the static put is a constant derived from the class holding the method.
  // This checks for java.lang.Class.get*Name.
  private boolean isClassNameConstantOf(DexClass clazz, StaticPut put) {
    if (put.getField().type != dexItemFactory.stringType) {
      return false;
    }
    Value value = put.value().getAliasedValue();
    if (value.isPhi()) {
      return false;
    }
    return isClassNameConstantOf(clazz, value.definition);
  }

  private boolean isClassNameConstantOf(DexClass clazz, Instruction instruction) {
    if (instruction.isInvokeVirtual()) {
      InvokeVirtual invoke = instruction.asInvokeVirtual();
      if (!dexItemFactory.classMethods.isReflectiveNameLookup(invoke.getInvokedMethod())) {
        return false;
      }
      Value inValue = invoke.inValues().get(0);
      return !inValue.isPhi()
          && inValue.definition.isConstClass()
          && inValue.definition.asConstClass().getValue() == clazz.type;
    }
    return false;
  }
}
