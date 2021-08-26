// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.optimize.classinliner.ClassInlinerEligibilityInfo;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsage;
import com.android.tools.r8.ir.optimize.info.initializer.DefaultInstanceInitializerInfo;
import com.android.tools.r8.ir.optimize.info.initializer.InstanceInitializerInfo;
import java.util.BitSet;
import java.util.Set;
import java.util.function.Function;

public class UpdatableMethodOptimizationInfo implements MethodOptimizationInfo {

  private boolean cannotBeKept = false;
  private boolean classInitializerMayBePostponed = false;
  private boolean hasBeenInlinedIntoSingleCallSite = false;
  private Set<DexType> initializedClassesOnNormalExit =
      DefaultMethodOptimizationInfo.UNKNOWN_INITIALIZED_CLASSES_ON_NORMAL_EXIT;
  private int returnedArgument = DefaultMethodOptimizationInfo.UNKNOWN_RETURNED_ARGUMENT;
  private boolean mayHaveSideEffects = DefaultMethodOptimizationInfo.UNKNOWN_MAY_HAVE_SIDE_EFFECTS;
  private boolean returnValueOnlyDependsOnArguments =
      DefaultMethodOptimizationInfo.UNKNOWN_RETURN_VALUE_ONLY_DEPENDS_ON_ARGUMENTS;
  private boolean neverReturnsNull = DefaultMethodOptimizationInfo.UNKNOWN_NEVER_RETURNS_NULL;
  private boolean neverReturnsNormally =
      DefaultMethodOptimizationInfo.UNKNOWN_NEVER_RETURNS_NORMALLY;
  private AbstractValue abstractReturnValue =
      DefaultMethodOptimizationInfo.UNKNOWN_ABSTRACT_RETURN_VALUE;
  private TypeLatticeElement returnsObjectWithUpperBoundType =
      DefaultMethodOptimizationInfo.UNKNOWN_TYPE;
  private ClassTypeLatticeElement returnsObjectWithLowerBoundType =
      DefaultMethodOptimizationInfo.UNKNOWN_CLASS_TYPE;
  private InlinePreference inlining = InlinePreference.Default;
  private boolean useIdentifierNameString =
      DefaultMethodOptimizationInfo.DOES_NOT_USE_IDENTIFIER_NAME_STRING;
  private boolean checksNullReceiverBeforeAnySideEffect =
      DefaultMethodOptimizationInfo.UNKNOWN_CHECKS_NULL_RECEIVER_BEFORE_ANY_SIDE_EFFECT;
  private boolean triggersClassInitBeforeAnySideEffect =
      DefaultMethodOptimizationInfo.UNKNOWN_TRIGGERS_CLASS_INIT_BEFORE_ANY_SIDE_EFFECT;
  // Stores information about instance methods and constructors for
  // class inliner, null value indicates that the method is not eligible.
  private ClassInlinerEligibilityInfo classInlinerEligibility =
      DefaultMethodOptimizationInfo.UNKNOWN_CLASS_INLINER_ELIGIBILITY;
  private InstanceInitializerInfo instanceInitializerInfo =
      DefaultInstanceInitializerInfo.getInstance();
  private boolean initializerEnablingJavaAssertions =
      DefaultMethodOptimizationInfo.UNKNOWN_INITIALIZER_ENABLING_JAVA_ASSERTIONS;
  private ParameterUsagesInfo parametersUsages =
      DefaultMethodOptimizationInfo.UNKNOWN_PARAMETER_USAGE_INFO;
  // Stores information about nullability hint per parameter. If set, that means, the method
  // somehow (e.g., null check, such as arg != null, or using checkParameterIsNotNull) ensures
  // the corresponding parameter is not null, or throws NPE before any other side effects.
  // This info is used by {@link UninstantiatedTypeOptimization#rewriteInvoke} that replaces an
  // invocation with null throwing code if an always-null argument is passed. Also used by Inliner
  // to give a credit to null-safe code, e.g., Kotlin's null safe argument.
  // Note that this bit set takes into account the receiver for instance methods.
  private BitSet nonNullParamOrThrow =
      DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_OR_THROW_FACTS;
  // Stores information about nullability facts per parameter. If set, that means, the method
  // somehow (e.g., null check, such as arg != null, or NPE-throwing instructions such as array
  // access or another invocation) ensures the corresponding parameter is not null, and that is
  // guaranteed until the normal exits. That is, if the invocation of this method is finished
  // normally, the recorded parameter is definitely not null. These facts are used to propagate
  // non-null information through {@link NonNullTracker}.
  // Note that this bit set takes into account the receiver for instance methods.
  private BitSet nonNullParamOnNormalExits =
      DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_ON_NORMAL_EXITS_FACTS;
  private boolean reachabilitySensitive = false;
  private boolean returnValueHasBeenPropagated = false;

  UpdatableMethodOptimizationInfo() {
    // Intentionally left empty, just use the default values.
  }

  // Copy constructor used to create a mutable copy. Do not forget to copy from template when a new
  // field is added.
  private UpdatableMethodOptimizationInfo(UpdatableMethodOptimizationInfo template) {
    cannotBeKept = template.cannotBeKept;
    classInitializerMayBePostponed = template.classInitializerMayBePostponed;
    hasBeenInlinedIntoSingleCallSite = template.hasBeenInlinedIntoSingleCallSite;
    initializedClassesOnNormalExit = template.initializedClassesOnNormalExit;
    returnedArgument = template.returnedArgument;
    mayHaveSideEffects = template.mayHaveSideEffects;
    returnValueOnlyDependsOnArguments = template.returnValueOnlyDependsOnArguments;
    neverReturnsNull = template.neverReturnsNull;
    neverReturnsNormally = template.neverReturnsNormally;
    abstractReturnValue = template.abstractReturnValue;
    returnsObjectWithUpperBoundType = template.returnsObjectWithUpperBoundType;
    returnsObjectWithLowerBoundType = template.returnsObjectWithLowerBoundType;
    inlining = template.inlining;
    useIdentifierNameString = template.useIdentifierNameString;
    checksNullReceiverBeforeAnySideEffect = template.checksNullReceiverBeforeAnySideEffect;
    triggersClassInitBeforeAnySideEffect = template.triggersClassInitBeforeAnySideEffect;
    classInlinerEligibility = template.classInlinerEligibility;
    instanceInitializerInfo = template.instanceInitializerInfo;
    initializerEnablingJavaAssertions = template.initializerEnablingJavaAssertions;
    parametersUsages = template.parametersUsages;
    nonNullParamOrThrow = template.nonNullParamOrThrow;
    nonNullParamOnNormalExits = template.nonNullParamOnNormalExits;
    reachabilitySensitive = template.reachabilitySensitive;
    returnValueHasBeenPropagated = template.returnValueHasBeenPropagated;
  }

  public void fixupClassTypeReferences(
      Function<DexType, DexType> mapping, AppView<? extends AppInfoWithSubtyping> appView) {
    if (returnsObjectWithUpperBoundType != null) {
      returnsObjectWithUpperBoundType =
          returnsObjectWithUpperBoundType.fixupClassTypeReferences(mapping, appView);
    }
    if (returnsObjectWithLowerBoundType != null) {
      returnsObjectWithLowerBoundType =
          returnsObjectWithLowerBoundType.fixupClassTypeReferences(mapping, appView);
    }
  }

  @Override
  public boolean isDefaultMethodOptimizationInfo() {
    return false;
  }

  @Override
  public boolean isUpdatableMethodOptimizationInfo() {
    return true;
  }

  @Override
  public UpdatableMethodOptimizationInfo asUpdatableMethodOptimizationInfo() {
    return this;
  }

  @Override
  public boolean cannotBeKept() {
    return cannotBeKept;
  }

  // TODO(b/140214568): Should be package-private.
  public void markCannotBeKept() {
    cannotBeKept = true;
  }

  @Override
  public boolean classInitializerMayBePostponed() {
    return classInitializerMayBePostponed;
  }

  void markClassInitializerMayBePostponed() {
    classInitializerMayBePostponed = true;
  }

  @Override
  public TypeLatticeElement getDynamicUpperBoundType() {
    return returnsObjectWithUpperBoundType;
  }

  @Override
  public ClassTypeLatticeElement getDynamicLowerBoundType() {
    return returnsObjectWithLowerBoundType;
  }

  @Override
  public Set<DexType> getInitializedClassesOnNormalExit() {
    return initializedClassesOnNormalExit;
  }

  @Override
  public InstanceInitializerInfo getInstanceInitializerInfo() {
    return instanceInitializerInfo;
  }

  @Override
  public ParameterUsage getParameterUsages(int parameter) {
    return parametersUsages == null ? null : parametersUsages.getParameterUsage(parameter);
  }

  @Override
  public BitSet getNonNullParamOrThrow() {
    return nonNullParamOrThrow;
  }

  @Override
  public BitSet getNonNullParamOnNormalExits() {
    return nonNullParamOnNormalExits;
  }

  @Override
  public boolean hasBeenInlinedIntoSingleCallSite() {
    return hasBeenInlinedIntoSingleCallSite;
  }

  void markInlinedIntoSingleCallSite() {
    hasBeenInlinedIntoSingleCallSite = true;
  }

  @Override
  public boolean isReachabilitySensitive() {
    return reachabilitySensitive;
  }

  @Override
  public boolean returnsArgument() {
    return returnedArgument != -1;
  }

  @Override
  public int getReturnedArgument() {
    assert returnsArgument();
    return returnedArgument;
  }

  @Override
  public boolean neverReturnsNull() {
    return neverReturnsNull;
  }

  @Override
  public boolean neverReturnsNormally() {
    return neverReturnsNormally;
  }

  @Override
  public ClassInlinerEligibilityInfo getClassInlinerEligibility() {
    return classInlinerEligibility;
  }

  @Override
  public AbstractValue getAbstractReturnValue() {
    return abstractReturnValue;
  }

  @Override
  public boolean isInitializerEnablingJavaAssertions() {
    return initializerEnablingJavaAssertions;
  }

  @Override
  public boolean useIdentifierNameString() {
    return useIdentifierNameString;
  }

  @Override
  public boolean forceInline() {
    return inlining == InlinePreference.ForceInline;
  }

  @Override
  public boolean neverInline() {
    return inlining == InlinePreference.NeverInline;
  }

  @Override
  public boolean checksNullReceiverBeforeAnySideEffect() {
    return checksNullReceiverBeforeAnySideEffect;
  }

  @Override
  public boolean triggersClassInitBeforeAnySideEffect() {
    return triggersClassInitBeforeAnySideEffect;
  }

  @Override
  public boolean mayHaveSideEffects() {
    return mayHaveSideEffects;
  }

  @Override
  public boolean returnValueOnlyDependsOnArguments() {
    return returnValueOnlyDependsOnArguments;
  }

  void setParameterUsages(ParameterUsagesInfo parametersUsages) {
    this.parametersUsages = parametersUsages;
  }

  void setNonNullParamOrThrow(BitSet facts) {
    this.nonNullParamOrThrow = facts;
  }

  void setNonNullParamOnNormalExits(BitSet facts) {
    this.nonNullParamOnNormalExits = facts;
  }

  public void setReachabilitySensitive(boolean reachabilitySensitive) {
    this.reachabilitySensitive = reachabilitySensitive;
  }

  void setClassInlinerEligibility(ClassInlinerEligibilityInfo eligibility) {
    this.classInlinerEligibility = eligibility;
  }

  void setInstanceInitializerInfo(InstanceInitializerInfo instanceInitializerInfo) {
    this.instanceInitializerInfo = instanceInitializerInfo;
  }

  void setInitializerEnablingJavaAssertions() {
    this.initializerEnablingJavaAssertions = true;
  }

  void markInitializesClassesOnNormalExit(Set<DexType> initializedClassesOnNormalExit) {
    this.initializedClassesOnNormalExit = initializedClassesOnNormalExit;
  }

  void markReturnsArgument(int argument) {
    assert argument >= 0;
    assert returnedArgument == -1 || returnedArgument == argument;
    returnedArgument = argument;
  }

  void markMayNotHaveSideEffects() {
    mayHaveSideEffects = false;
  }

  void markReturnValueOnlyDependsOnArguments() {
    returnValueOnlyDependsOnArguments = true;
  }

  void markNeverReturnsNull() {
    neverReturnsNull = true;
  }

  void markNeverReturnsNormally() {
    neverReturnsNormally = true;
  }

  void markReturnsAbstractValue(AbstractValue value) {
    assert !abstractReturnValue.isSingleValue() || abstractReturnValue.asSingleValue() == value
        : "return single value changed from " + abstractReturnValue + " to " + value;
    abstractReturnValue = value;
  }

  void markReturnsObjectWithUpperBoundType(AppView<?> appView, TypeLatticeElement type) {
    assert type != null;
    // We may get more precise type information if the method is reprocessed (e.g., due to
    // optimization info collected from all call sites), and hence the
    // `returnsObjectWithUpperBoundType` is allowed to become more precise.
    // TODO(b/142559221): non-materializable assume instructions?
    // Nullability could be less precise, though. For example, suppose a value is known to be
    // non-null after a safe invocation, hence recorded with the non-null variant. If that call is
    // inlined and the method is reprocessed, such non-null assumption cannot be made again.
    assert returnsObjectWithUpperBoundType == DefaultMethodOptimizationInfo.UNKNOWN_TYPE
            || type.lessThanOrEqualUpToNullability(returnsObjectWithUpperBoundType, appView)
        : "upper bound type changed from " + returnsObjectWithUpperBoundType + " to " + type;
    returnsObjectWithUpperBoundType = type;
  }

  void markReturnsObjectWithLowerBoundType(ClassTypeLatticeElement type) {
    assert type != null;
    // Currently, we only have a lower bound type when we have _exact_ runtime type information.
    // Thus, the type should never become more precise (although the nullability could).
    assert returnsObjectWithLowerBoundType == DefaultMethodOptimizationInfo.UNKNOWN_CLASS_TYPE
            || (type.equalUpToNullability(returnsObjectWithLowerBoundType)
                && type.nullability()
                    .lessThanOrEqual(returnsObjectWithLowerBoundType.nullability()))
        : "lower bound type changed from " + returnsObjectWithLowerBoundType + " to " + type;
    returnsObjectWithLowerBoundType = type;
  }

  // TODO(b/140214568): Should be package-private.
  public void markForceInline() {
    // For concurrent scenarios we should allow the flag to be already set
    assert inlining == InlinePreference.Default || inlining == InlinePreference.ForceInline;
    inlining = InlinePreference.ForceInline;
  }

  // TODO(b/140214568): Should be package-private.
  public void unsetForceInline() {
    // For concurrent scenarios we should allow the flag to be already unset
    assert inlining == InlinePreference.Default || inlining == InlinePreference.ForceInline;
    inlining = InlinePreference.Default;
  }

  // TODO(b/140214568): Should be package-private.
  public void markNeverInline() {
    // For concurrent scenarios we should allow the flag to be already set
    assert inlining == InlinePreference.Default || inlining == InlinePreference.NeverInline;
    inlining = InlinePreference.NeverInline;
  }

  // TODO(b/140214568): Should be package-private.
  public void markUseIdentifierNameString() {
    useIdentifierNameString = true;
  }

  void markCheckNullReceiverBeforeAnySideEffect(boolean mark) {
    checksNullReceiverBeforeAnySideEffect = mark;
  }

  void markTriggerClassInitBeforeAnySideEffect(boolean mark) {
    triggersClassInitBeforeAnySideEffect = mark;
  }

  // TODO(b/140214568): Should be package-private.
  public void markAsPropagated() {
    returnValueHasBeenPropagated = true;
  }

  @Override
  public boolean returnValueHasBeenPropagated() {
    return returnValueHasBeenPropagated;
  }

  @Override
  public UpdatableMethodOptimizationInfo mutableCopy() {
    assert this != DefaultMethodOptimizationInfo.DEFAULT_INSTANCE;
    return new UpdatableMethodOptimizationInfo(this);
  }

  public void adjustOptimizationInfoAfterRemovingThisParameter() {
    // cannotBeKept: doesn't depend on `this`
    // classInitializerMayBePostponed: `this` could trigger <clinit> of the previous holder.
    classInitializerMayBePostponed = false;
    // hasBeenInlinedIntoSingleCallSite: then it should not be staticized.
    hasBeenInlinedIntoSingleCallSite = false;
    // initializedClassesOnNormalExit: `this` could trigger <clinit> of the previous holder.
    initializedClassesOnNormalExit =
        DefaultMethodOptimizationInfo.UNKNOWN_INITIALIZED_CLASSES_ON_NORMAL_EXIT;
    // At least, `this` pointer is not used in `returnedArgument`.
    assert returnedArgument == DefaultMethodOptimizationInfo.UNKNOWN_RETURNED_ARGUMENT
        || returnedArgument > 0;
    returnedArgument =
        returnedArgument == DefaultMethodOptimizationInfo.UNKNOWN_RETURNED_ARGUMENT
            ? DefaultMethodOptimizationInfo.UNKNOWN_RETURNED_ARGUMENT : returnedArgument - 1;
    // mayHaveSideEffects: `this` Argument didn't have side effects, so removing it doesn't affect
    //   whether or not the method may have side effects.
    // returnValueOnlyDependsOnArguments:
    //   if the method did before, so it does even after removing `this` Argument
    // code is not changed, and thus the following *return* info is not changed either.
    //   * neverReturnsNull
    //   * neverReturnsNormally
    //   * constantValue
    //   * returnsObjectWithUpperBoundType
    //   * returnsObjectWithLowerBoundType
    // inlining: it is not inlined, and thus staticized. No more chance of inlining, though.
    inlining = InlinePreference.Default;
    // useIdentifierNameString: code is not changed.
    // checksNullReceiverBeforeAnySideEffect: no more receiver.
    checksNullReceiverBeforeAnySideEffect =
        DefaultMethodOptimizationInfo.UNKNOWN_CHECKS_NULL_RECEIVER_BEFORE_ANY_SIDE_EFFECT;
    // triggersClassInitBeforeAnySideEffect: code is not changed.
    triggersClassInitBeforeAnySideEffect =
        DefaultMethodOptimizationInfo.UNKNOWN_TRIGGERS_CLASS_INIT_BEFORE_ANY_SIDE_EFFECT;
    // classInlinerEligibility: chances are the method is not an instance method anymore.
    classInlinerEligibility = DefaultMethodOptimizationInfo.UNKNOWN_CLASS_INLINER_ELIGIBILITY;
    // initializerInfo: the computed initializer info may become invalid.
    instanceInitializerInfo = null;
    // initializerEnablingJavaAssertions: `this` could trigger <clinit> of the previous holder.
    initializerEnablingJavaAssertions =
        DefaultMethodOptimizationInfo.UNKNOWN_INITIALIZER_ENABLING_JAVA_ASSERTIONS;
    parametersUsages =
        parametersUsages == DefaultMethodOptimizationInfo.UNKNOWN_PARAMETER_USAGE_INFO
            ? DefaultMethodOptimizationInfo.UNKNOWN_PARAMETER_USAGE_INFO
            : parametersUsages.remove(0);
    nonNullParamOrThrow =
        nonNullParamOrThrow == DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_OR_THROW_FACTS
            ? DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_OR_THROW_FACTS
            : nonNullParamOrThrow.get(1, nonNullParamOrThrow.length());
    nonNullParamOnNormalExits =
        nonNullParamOnNormalExits
                == DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_ON_NORMAL_EXITS_FACTS
            ? DefaultMethodOptimizationInfo.NO_NULL_PARAMETER_ON_NORMAL_EXITS_FACTS
            : nonNullParamOnNormalExits.get(1, nonNullParamOnNormalExits.length());
    // reachabilitySensitive: doesn't depend on `this`
    // returnValueHasBeenPropagated: doesn't depend on `this`
  }
}
