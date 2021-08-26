// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.AbstractError;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.AbstractFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.ConcreteMutableFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.EmptyFieldSet;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.UnknownFieldSet;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.Collections;
import java.util.List;

public abstract class FieldInstruction extends Instruction {

  private final DexField field;

  protected FieldInstruction(DexField field, Value dest, Value value) {
    this(field, dest, Collections.singletonList(value));
  }

  protected FieldInstruction(DexField field, Value dest, List<Value> inValues) {
    super(dest, inValues);
    assert field != null;
    this.field = field;
  }

  public abstract Value value();

  public FieldMemberType getType() {
    return FieldMemberType.fromDexType(field.type);
  }

  public DexField getField() {
    return field;
  }

  @Override
  public boolean isFieldInstruction() {
    return true;
  }

  @Override
  public FieldInstruction asFieldInstruction() {
    return this;
  }

  @Override
  public AbstractError instructionInstanceCanThrow(AppView<?> appView, DexType context) {
    DexEncodedField resolvedField;
    if (appView.enableWholeProgramOptimizations()) {
      // TODO(b/123857022): Should be possible to use definitionFor().
      resolvedField = appView.appInfo().resolveField(field);
    } else {
      // In D8, only allow the field in the same context.
      if (field.holder != context) {
        return AbstractError.top();
      }
      // Note that, in D8, we are not using AppInfo#resolveField to avoid traversing the hierarchy.
      DexClass holder = appView.definitionFor(field.holder);
      if (holder == null) {
        return AbstractError.top();
      }
      resolvedField = holder.lookupField(field);
    }
    // * NoSuchFieldError (resolution failure).
    if (resolvedField == null) {
      if (appView.enableWholeProgramOptimizations()) {
        return AbstractError.specific(appView.dexItemFactory().noSuchFieldErrorType);
      } else {
        // In D8, the field lookup can only consult the context definition. Nothing can be concluded
        // from a lookup failure. For example, it could be ICCE or IAE if the current field access
        // is referring to incompatible or invisible field in a super type, respectively.
        return AbstractError.top();
      }
    }
    // * IncompatibleClassChangeError (instance-* for static field and vice versa).
    if (resolvedField.isStaticMember()) {
      if (isInstanceGet() || isInstancePut()) {
        return AbstractError.specific(appView.dexItemFactory().icceType);
      }
    } else {
      if (isStaticGet() || isStaticPut()) {
        return AbstractError.specific(appView.dexItemFactory().icceType);
      }
    }
    // * IllegalAccessError (not visible from the access context).
    if (!isMemberVisibleFromOriginalContext(
        appView, context, resolvedField.field.holder, resolvedField.accessFlags)) {
      return AbstractError.specific(appView.dexItemFactory().illegalAccessErrorType);
    }
    // TODO(b/137168535): Without non-null tracking, only locally created receiver is allowed in D8.
    // * NullPointerException (null receiver).
    if (isInstanceGet() || isInstancePut()) {
      Value receiver = inValues.get(0);
      if (receiver.isAlwaysNull(appView) || receiver.typeLattice.isNullable()) {
        return AbstractError.specific(appView.dexItemFactory().npeType);
      }
    }
    // For D8, reaching here means the field is in the same context, hence the class is guaranteed
    // to be initialized already.
    if (!appView.enableWholeProgramOptimizations()) {
      return AbstractError.bottom();
    }
    boolean mayTriggerClassInitialization = isStaticGet() || isStaticPut();
    if (mayTriggerClassInitialization) {
      // Only check for <clinit> side effects if there is no -assumenosideeffects rule.
      if (appView.appInfo().hasLiveness()) {
        AppInfoWithLiveness appInfoWithLiveness = appView.appInfo().withLiveness();
        if (appInfoWithLiveness.noSideEffects.containsKey(resolvedField.field)) {
          return AbstractError.bottom();
        }
      }
      // May trigger <clinit> that may have side effects.
      if (field.holder.classInitializationMayHaveSideEffects(
          appView,
          // Types that are a super type of `context` are guaranteed to be initialized already.
          type -> appView.isSubtype(context, type).isTrue())) {
        return AbstractError.top();
      }
    }
    return AbstractError.bottom();
  }

  @Override
  public boolean hasInvariantOutType() {
    // TODO(jsjeon): what if the target field is known to be non-null?
    return true;
  }

  @Override
  public AbstractFieldSet readSet(AppView<?> appView, DexType context) {
    if (instructionMayTriggerMethodInvocation(appView, context)) {
      // This may trigger class initialization, which could potentially read any field.
      return UnknownFieldSet.getInstance();
    }

    if (isFieldGet()) {
      DexField field = getField();
      DexEncodedField encodedField = null;
      if (appView.enableWholeProgramOptimizations()) {
        encodedField = appView.appInfo().resolveField(field);
      } else {
        DexClass clazz = appView.definitionFor(field.holder);
        if (clazz != null) {
          encodedField = clazz.lookupField(field);
        }
      }
      if (encodedField != null) {
        return new ConcreteMutableFieldSet(encodedField);
      }
      return UnknownFieldSet.getInstance();
    }

    assert isFieldPut();
    return EmptyFieldSet.getInstance();
  }

  /**
   * Returns {@code true} if this instruction may store an instance of a class that has a non-
   * default finalize() method in a field. In that case, it is not safe to remove this instruction,
   * since that could change the lifetime of the value.
   */
  boolean isStoringObjectWithFinalizer(AppInfoWithLiveness appInfo) {
    assert isFieldPut();
    TypeLatticeElement type = value().getTypeLattice();
    TypeLatticeElement baseType =
        type.isArrayType() ? type.asArrayTypeLatticeElement().getArrayBaseTypeLattice() : type;
    if (baseType.isClassType()) {
      Value root = value().getAliasedValue();
      if (!root.isPhi() && root.definition.isNewInstance()) {
        DexClass clazz = appInfo.definitionFor(root.definition.asNewInstance().clazz);
        if (clazz == null) {
          return true;
        }
        if (clazz.superType == null) {
          return false;
        }
        DexItemFactory dexItemFactory = appInfo.dexItemFactory();
        DexEncodedMethod resolutionResult =
            appInfo
                .resolveMethod(clazz.type, dexItemFactory.objectMethods.finalize)
                .getSingleTarget();
        return resolutionResult != null && resolutionResult.isProgramMethod(appInfo);
      }

      return appInfo.mayHaveFinalizeMethodDirectlyOrIndirectly(
          baseType.asClassTypeLatticeElement());
    }

    return false;
  }

  @Override
  public AbstractValue getAbstractValue(AppView<?> appView, DexType context) {
    assert isFieldGet();
    DexEncodedField field = appView.appInfo().resolveField(getField());
    if (field != null) {
      return field.getOptimizationInfo().getAbstractValue();
    }
    return UnknownValue.getInstance();
  }
}
