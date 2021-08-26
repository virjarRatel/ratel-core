// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import java.util.Collection;

// Computes the inlining constraint for a given instruction.
public class InliningConstraints {

  private AppView<AppInfoWithLiveness> appView;

  private boolean allowStaticInterfaceMethodCalls = true;

  // Currently used only by the vertical class merger (in all other cases this is the identity).
  //
  // When merging a type A into its subtype B we need to inline A.<init>() into B.<init>().
  // Therefore, we need to be sure that A.<init>() can in fact be inlined into B.<init>() *before*
  // we merge the two classes. However, at this point, we may reject the method A.<init>() from
  // being inlined into B.<init>() only because it is not declared in the same class as B (which
  // it would be after merging A and B).
  //
  // To circumvent this problem, the vertical class merger creates a graph lense that maps the
  // type A to B, to create a temporary view of what the world would look like after class merging.
  private GraphLense graphLense;

  public InliningConstraints(AppView<AppInfoWithLiveness> appView, GraphLense graphLense) {
    assert graphLense.isContextFreeForMethods();
    assert appView.graphLense() != graphLense || graphLense.isIdentityLense();
    this.appView = appView;
    this.graphLense = graphLense; // Note: Intentionally *not* appView.graphLense().
  }

  public void disallowStaticInterfaceMethodCalls() {
    allowStaticInterfaceMethodCalls = false;
  }

  public ConstraintWithTarget forAlwaysMaterializingUser() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forArgument() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forArrayGet() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forArrayLength() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forArrayPut() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forBinop() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDexItemBasedConstString(
      DexReference type, DexType invocationContext) {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forCheckCast(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forConstClass(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forConstInstruction() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDebugLocalRead() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDebugLocalsChange() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDebugPosition() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDup() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forDup2() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forInstanceGet(DexField field, DexType invocationContext) {
    DexField lookup = graphLense.lookupField(field);
    return forFieldInstruction(
        lookup, appView.appInfo().lookupInstanceTarget(lookup.holder, lookup), invocationContext);
  }

  public ConstraintWithTarget forInstanceOf(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forInstancePut(DexField field, DexType invocationContext) {
    DexField lookup = graphLense.lookupField(field);
    return forFieldInstruction(
        lookup, appView.appInfo().lookupInstanceTarget(lookup.holder, lookup), invocationContext);
  }

  public ConstraintWithTarget forInvoke(DexMethod method, Type type, DexType invocationContext) {
    switch (type) {
      case DIRECT:
        return forInvokeDirect(method, invocationContext);
      case INTERFACE:
        return forInvokeInterface(method, invocationContext);
      case STATIC:
        return forInvokeStatic(method, invocationContext);
      case SUPER:
        return forInvokeSuper(method, invocationContext);
      case VIRTUAL:
        return forInvokeVirtual(method, invocationContext);
      case CUSTOM:
        return forInvokeCustom();
      case POLYMORPHIC:
        return forInvokePolymorphic(method, invocationContext);
      default:
        throw new Unreachable("Unexpected type: " + type);
    }
  }

  public ConstraintWithTarget forInvokeCustom() {
    // TODO(b/135965362): Test and support inlining invoke dynamic.
    return ConstraintWithTarget.NEVER;
  }

  public ConstraintWithTarget forInvokeDirect(DexMethod method, DexType invocationContext) {
    DexMethod lookup = graphLense.lookupMethod(method);
    return forSingleTargetInvoke(
        lookup, appView.appInfo().lookupDirectTarget(lookup), invocationContext);
  }

  public ConstraintWithTarget forInvokeInterface(DexMethod method, DexType invocationContext) {
    DexMethod lookup = graphLense.lookupMethod(method);
    return forVirtualInvoke(
        lookup,
        invocationContext,
        true);
  }

  public ConstraintWithTarget forInvokeMultiNewArray(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forInvokeNewArray(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forInvokePolymorphic(DexMethod method, DexType invocationContext) {
    return ConstraintWithTarget.NEVER;
  }

  public ConstraintWithTarget forInvokeStatic(DexMethod method, DexType invocationContext) {
    DexMethod lookup = graphLense.lookupMethod(method);
    return forSingleTargetInvoke(
        lookup, appView.appInfo().lookupStaticTarget(lookup), invocationContext);
  }

  public ConstraintWithTarget forInvokeSuper(DexMethod method, DexType invocationContext) {
    // The semantics of invoke super depend on the context.
    return new ConstraintWithTarget(Constraint.SAMECLASS, invocationContext);
  }

  public ConstraintWithTarget forInvokeVirtual(DexMethod method, DexType invocationContext) {
    DexMethod lookup = graphLense.lookupMethod(method);
    return forVirtualInvoke(
        lookup,
        invocationContext,
        false);
  }

  public ConstraintWithTarget forJumpInstruction() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forLoad() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forMonitor() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forMove() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forMoveException() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forNewArrayEmpty(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forNewArrayFilledData() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forNewInstance(DexType type, DexType invocationContext) {
    return ConstraintWithTarget.classIsVisible(invocationContext, type, appView);
  }

  public ConstraintWithTarget forAssume() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forPop() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forReturn() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forStaticGet(DexField field, DexType invocationContext) {
    DexField lookup = graphLense.lookupField(field);
    return forFieldInstruction(
        lookup, appView.appInfo().lookupStaticTarget(lookup.holder, lookup), invocationContext);
  }

  public ConstraintWithTarget forStaticPut(DexField field, DexType invocationContext) {
    DexField lookup = graphLense.lookupField(field);
    return forFieldInstruction(
        lookup, appView.appInfo().lookupStaticTarget(lookup.holder, lookup), invocationContext);
  }

  public ConstraintWithTarget forStore() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forSwap() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forThrow() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forUnop() {
    return ConstraintWithTarget.ALWAYS;
  }

  public ConstraintWithTarget forConstMethodHandle() {
    return ConstraintWithTarget.NEVER;
  }

  public ConstraintWithTarget forConstMethodType() {
    return ConstraintWithTarget.NEVER;
  }

  private ConstraintWithTarget forFieldInstruction(
      DexField field, DexEncodedField target, DexType invocationContext) {
    // Resolve the field if possible and decide whether the instruction can inlined.
    DexType fieldHolder = graphLense.lookupType(field.holder);
    DexClass fieldClass = appView.definitionFor(fieldHolder);
    if (target != null && fieldClass != null) {
      ConstraintWithTarget fieldConstraintWithTarget =
          ConstraintWithTarget.deriveConstraint(
              invocationContext, fieldHolder, target.accessFlags, appView);

      // If the field has not been member-rebound, then we also need to make sure that the
      // `invocationContext` has access to the definition of the field.
      //
      // See, for example, InlineNonReboundFieldTest (b/128604123).
      if (field.holder != target.field.holder) {
        DexType actualFieldHolder = graphLense.lookupType(target.field.holder);
        fieldConstraintWithTarget =
            ConstraintWithTarget.meet(
                fieldConstraintWithTarget,
                ConstraintWithTarget.deriveConstraint(
                    invocationContext, actualFieldHolder, target.accessFlags, appView),
                appView);
      }

      ConstraintWithTarget classConstraintWithTarget =
          ConstraintWithTarget.deriveConstraint(
              invocationContext, fieldHolder, fieldClass.accessFlags, appView);
      return ConstraintWithTarget.meet(
          fieldConstraintWithTarget, classConstraintWithTarget, appView);
    }
    return ConstraintWithTarget.NEVER;
  }

  private ConstraintWithTarget forSingleTargetInvoke(
      DexMethod method, DexEncodedMethod target, DexType invocationContext) {
    if (method.holder.isArrayType()) {
      return ConstraintWithTarget.ALWAYS;
    }
    if (target != null) {
      DexType methodHolder = graphLense.lookupType(target.method.holder);
      DexClass methodClass = appView.definitionFor(methodHolder);
      if (methodClass != null) {
        if (!allowStaticInterfaceMethodCalls && methodClass.isInterface() && target.hasCode()) {
          // See b/120121170.
          return ConstraintWithTarget.NEVER;
        }

        ConstraintWithTarget methodConstraintWithTarget =
            ConstraintWithTarget.deriveConstraint(
                invocationContext, methodHolder, target.accessFlags, appView);
        // We also have to take the constraint of the enclosing class into account.
        ConstraintWithTarget classConstraintWithTarget =
            ConstraintWithTarget.deriveConstraint(
                invocationContext, methodHolder, methodClass.accessFlags, appView);
        return ConstraintWithTarget.meet(
            methodConstraintWithTarget, classConstraintWithTarget, appView);
      }
    }
    return ConstraintWithTarget.NEVER;
  }

  private ConstraintWithTarget forVirtualInvoke(
      DexMethod method,
      DexType invocationContext,
      boolean isInterface) {
    if (method.holder.isArrayType()) {
      return ConstraintWithTarget.ALWAYS;
    }

    // Perform resolution and derive inlining constraints based on the accessibility of the
    // resolution result.
    ResolutionResult resolutionResult =
        appView.appInfo().resolveMethod(method.holder, method, isInterface);
    if (!resolutionResult.isValidVirtualTarget(appView.options())) {
      return ConstraintWithTarget.NEVER;
    }

    DexEncodedMethod resolutionTarget = resolutionResult.getSingleTarget();
    if (resolutionTarget == null) {
      // This will fail at runtime.
      return ConstraintWithTarget.NEVER;
    }

    DexType methodHolder = graphLense.lookupType(resolutionTarget.method.holder);
    DexClass methodClass = appView.definitionFor(methodHolder);
    assert methodClass != null;
    ConstraintWithTarget methodConstraintWithTarget =
        ConstraintWithTarget.deriveConstraint(
            invocationContext, methodHolder, resolutionTarget.accessFlags, appView);
    // We also have to take the constraint of the enclosing class of the resolution result
    // into account. We do not allow inlining this method if it is calling something that
    // is inaccessible. Inlining in that case could move the code to another package making a
    // call succeed that should not succeed. Conversely, if the resolution result is accessible,
    // we have to make sure that inlining cannot make it inaccessible.
    ConstraintWithTarget classConstraintWithTarget =
        ConstraintWithTarget.deriveConstraint(
            invocationContext, methodHolder, methodClass.accessFlags, appView);
    ConstraintWithTarget result =
        ConstraintWithTarget.meet(methodConstraintWithTarget, classConstraintWithTarget, appView);
    if (result == ConstraintWithTarget.NEVER) {
      return result;
    }

    // For each of the actual potential targets, derive constraints based on the accessibility
    // of the method itself.
    Collection<DexEncodedMethod> targets =
        resolutionResult.lookupVirtualDispatchTargets(isInterface, appView.appInfo());
    for (DexEncodedMethod target : targets) {
      methodHolder = graphLense.lookupType(target.method.holder);
      assert appView.definitionFor(methodHolder) != null;
      methodConstraintWithTarget =
          ConstraintWithTarget.deriveConstraint(
              invocationContext, methodHolder, target.accessFlags, appView);
      result = ConstraintWithTarget.meet(result, methodConstraintWithTarget, appView);
      if (result == ConstraintWithTarget.NEVER) {
        return result;
      }
    }

    return result;
  }
}
