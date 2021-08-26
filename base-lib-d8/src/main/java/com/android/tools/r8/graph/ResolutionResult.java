// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import static com.android.tools.r8.graph.DexProgramClass.asProgramClassOrNull;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.SetUtils;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ResolutionResult {

  /**
   * Returns true if resolution succeeded *and* the resolved method has a known definition.
   *
   * <p>Note that {@code !isSingleResolution() && !isFailedResolution()} can be true. In that case
   * that resolution has succeeded, but the definition of the resolved method is unknown. In
   * particular this is the case for the clone() method on arrays.
   */
  public boolean isSingleResolution() {
    return false;
  }

  /** Returns non-null if isSingleResolution() is true, otherwise null. */
  public SingleResolutionResult asSingleResolution() {
    return null;
  }

  /**
   * Returns true if resolution failed.
   *
   * <p>Note the disclaimer in the doc of {@code isSingleResolution()}.
   */
  public boolean isFailedResolution() {
    return false;
  }

  /** Returns non-null if isFailedResolution() is true, otherwise null. */
  public FailedResolutionResult asFailedResolution() {
    return null;
  }

  /** Short-hand to get the single resolution method if resolution finds it, null otherwise. */
  public final DexEncodedMethod getSingleTarget() {
    return isSingleResolution() ? asSingleResolution().getResolvedMethod() : null;
  }

  public abstract boolean isAccessibleFrom(DexProgramClass context, AppInfoWithSubtyping appInfo);

  public abstract boolean isAccessibleForVirtualDispatchFrom(
      DexProgramClass context, AppInfoWithSubtyping appInfo);

  public abstract boolean isValidVirtualTarget(InternalOptions options);

  public abstract boolean isValidVirtualTargetForDynamicDispatch();

  /** Lookup the single target of an invoke-super on this resolution result if possible. */
  public abstract DexEncodedMethod lookupInvokeSuperTarget(DexType context, AppInfo appInfo);

  public final Set<DexEncodedMethod> lookupVirtualDispatchTargets(
      boolean isInterface, AppInfoWithSubtyping appInfo) {
    return isInterface ? lookupInterfaceTargets(appInfo) : lookupVirtualTargets(appInfo);
  }

  public abstract Set<DexEncodedMethod> lookupVirtualTargets(AppInfoWithSubtyping appInfo);

  public abstract Set<DexEncodedMethod> lookupInterfaceTargets(AppInfoWithSubtyping appInfo);

  /** Result for a resolution that succeeds with a known declaration/definition. */
  public static class SingleResolutionResult extends ResolutionResult {
    private final DexClass initialResolutionHolder;
    private final DexClass resolvedHolder;
    private final DexEncodedMethod resolvedMethod;

    public static boolean isValidVirtualTarget(InternalOptions options, DexEncodedMethod target) {
      return options.canUseNestBasedAccess()
          ? (!target.accessFlags.isStatic() && !target.accessFlags.isConstructor())
          : target.isVirtualMethod();
    }

    public SingleResolutionResult(
        DexClass initialResolutionHolder,
        DexClass resolvedHolder,
        DexEncodedMethod resolvedMethod) {
      assert initialResolutionHolder != null;
      assert resolvedHolder != null;
      assert resolvedMethod != null;
      assert resolvedHolder.type == resolvedMethod.method.holder;
      this.resolvedHolder = resolvedHolder;
      this.resolvedMethod = resolvedMethod;
      this.initialResolutionHolder = initialResolutionHolder;
    }

    public DexClass getResolvedHolder() {
      return resolvedHolder;
    }

    public DexEncodedMethod getResolvedMethod() {
      return resolvedMethod;
    }

    @Override
    public boolean isSingleResolution() {
      return true;
    }

    @Override
    public SingleResolutionResult asSingleResolution() {
      return this;
    }

    @Override
    public boolean isAccessibleFrom(DexProgramClass context, AppInfoWithSubtyping appInfo) {
      return AccessControl.isMethodAccessible(
          resolvedMethod, initialResolutionHolder, context, appInfo);
    }

    @Override
    public boolean isAccessibleForVirtualDispatchFrom(
        DexProgramClass context, AppInfoWithSubtyping appInfo) {
      // If a private method is accessible (which implies it is via its nest), then it is a valid
      // virtual dispatch target if non-static.
      return isAccessibleFrom(context, appInfo)
          && (resolvedMethod.isVirtualMethod()
              || (resolvedMethod.isPrivateMethod() && !resolvedMethod.isStatic()));
    }

    @Override
    public boolean isValidVirtualTarget(InternalOptions options) {
      return isValidVirtualTarget(options, resolvedMethod);
    }

    @Override
    public boolean isValidVirtualTargetForDynamicDispatch() {
      return resolvedMethod.isVirtualMethod();
    }

    /**
     * Lookup super method following the super chain from the holder of {@code method}.
     *
     * <p>This method will resolve the method on the holder of {@code method} and only return a
     * non-null value if the result of resolution was an instance (i.e. non-static) method.
     *
     * <p>Additionally, this will also verify that the invoke super is valid, i.e., it is on the
     * same type or a super type of the current context. The spec says that it has invoke super
     * semantics, if the type is a supertype of the current class. If it is the same or a subtype,
     * it has invoke direct semantics. The latter case is illegal, so we map it to a super call
     * here. In R8, we abort at a later stage (see. See also <a href=
     * "https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokespecial" </a>
     * for invokespecial dispatch and <a href="https://docs.oracle.com/javase/specs/jvms/"
     * "se7/html/jvms-4.html#jvms-4.10.1.9.invokespecial"</a> for verification requirements. In
     * particular, the requirement isAssignable(class(CurrentClassName, L), class(MethodClassName,
     * L)). com.android.tools.r8.cf.code.CfInvoke#isInvokeSuper(DexType)}.
     *
     * @param context the class the invoke is contained in, i.e., the holder of the caller.
     * @param appInfo Application info.
     * @return The actual target for the invoke-super or {@code null} if none found.
     */
    @Override
    public DexEncodedMethod lookupInvokeSuperTarget(DexType context, AppInfo appInfo) {
      DexMethod method = resolvedMethod.method;
      // TODO(b/145775365): Check the requirements for an invoke-special to a protected method.
      // See https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokespecial

      if (appInfo.hasSubtyping()
          && !appInfo.withSubtyping().isSubtype(context, initialResolutionHolder.type)) {
        DexClass contextClass = appInfo.definitionFor(context);
        throw new CompilationError(
            "Illegal invoke-super to " + method.toSourceString() + " from class " + context,
            contextClass != null ? contextClass.getOrigin() : Origin.unknown());
      }

      // According to
      // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokespecial, use
      // the "symbolic reference" if the "symbolic reference" does not name a class.
      // TODO(b/145775365): This looks like the exact opposite of what the spec says, second item is
      //  - is-class(sym-ref) => is-super(sym-ref, current-class)
      //  this implication trivially holds for !is-class(sym-ref) == is-inteface(sym-ref), thus
      //  the resolution should specifically *not* use the "symbolic reference".
      if (initialResolutionHolder.isInterface()) {
        // TODO(b/145775365): This does not consider a static method!
        return appInfo.resolveMethodOnInterface(initialResolutionHolder, method).getSingleTarget();
      }
      // Then, resume on the search, but this time, starting from the holder of the caller.
      DexClass contextClass = appInfo.definitionFor(context);
      if (contextClass == null || contextClass.superType == null) {
        return null;
      }
      SingleResolutionResult resolution =
          appInfo.resolveMethodOnClass(contextClass.superType, method).asSingleResolution();
      return resolution != null && !resolution.resolvedMethod.isStatic()
          ? resolution.resolvedMethod
          : null;
    }

    @Override
    // TODO(b/140204899): Leverage refined receiver type if available.
    public Set<DexEncodedMethod> lookupVirtualTargets(AppInfoWithSubtyping appInfo) {
      assert isValidVirtualTarget(appInfo.app().options);
      // First add the target for receiver type method.type.
      DexEncodedMethod encodedMethod = getSingleTarget();
      Set<DexEncodedMethod> result = SetUtils.newIdentityHashSet(encodedMethod);
      // Add all matching targets from the subclass hierarchy.
      DexMethod method = encodedMethod.method;
      // TODO(b/140204899): Instead of subtypes of holder, we could iterate subtypes of refined
      //   receiver type if available.
      for (DexType type : appInfo.subtypes(method.holder)) {
        DexClass clazz = appInfo.definitionFor(type);
        if (!clazz.isInterface()) {
          ResolutionResult methods = appInfo.resolveMethodOnClass(clazz, method);
          DexEncodedMethod target = methods.getSingleTarget();
          if (target != null && target.isVirtualMethod()) {
            result.add(target);
          }
        }
      }
      return result;
    }

    @Override
    // TODO(b/140204899): Leverage refined receiver type if available.
    public Set<DexEncodedMethod> lookupInterfaceTargets(AppInfoWithSubtyping appInfo) {
      assert isValidVirtualTarget(appInfo.app().options);
      Set<DexEncodedMethod> result = Sets.newIdentityHashSet();
      if (isSingleResolution()) {
        // Add default interface methods to the list of targets.
        //
        // This helps to make sure we take into account synthesized lambda classes
        // that we are not aware of. Like in the following example, we know that all
        // classes, XX in this case, override B::bar(), but there are also synthesized
        // classes for lambda which don't, so we still need default method to be live.
        //
        //   public static void main(String[] args) {
        //     X x = () -> {};
        //     x.bar();
        //   }
        //
        //   interface X {
        //     void foo();
        //     default void bar() { }
        //   }
        //
        //   class XX implements X {
        //     public void foo() { }
        //     public void bar() { }
        //   }
        //
        DexEncodedMethod singleTarget = getSingleTarget();
        if (singleTarget.hasCode()) {
          DexProgramClass holder =
              asProgramClassOrNull(appInfo.definitionFor(singleTarget.method.holder));
          if (appInfo.hasAnyInstantiatedLambdas(holder)) {
            result.add(singleTarget);
          }
        }
      }

      DexEncodedMethod encodedMethod = getSingleTarget();
      DexMethod method = encodedMethod.method;
      Consumer<DexEncodedMethod> addIfNotAbstract =
          m -> {
            if (!m.accessFlags.isAbstract()) {
              result.add(m);
            }
          };
      // Default methods are looked up when looking at a specific subtype that does not override
      // them.
      // Otherwise, we would look up default methods that are actually never used. However, we have
      // to
      // add bridge methods, otherwise we can remove a bridge that will be used.
      Consumer<DexEncodedMethod> addIfNotAbstractAndBridge =
          m -> {
            if (!m.accessFlags.isAbstract() && m.accessFlags.isBridge()) {
              result.add(m);
            }
          };

      // TODO(b/140204899): Instead of subtypes of holder, we could iterate subtypes of refined
      //   receiver type if available.
      for (DexType type : appInfo.subtypes(method.holder)) {
        DexClass clazz = appInfo.definitionFor(type);
        if (clazz.isInterface()) {
          ResolutionResult targetMethods = appInfo.resolveMethodOnInterface(clazz, method);
          if (targetMethods.isSingleResolution()) {
            addIfNotAbstractAndBridge.accept(targetMethods.getSingleTarget());
          }
        } else {
          ResolutionResult targetMethods = appInfo.resolveMethodOnClass(clazz, method);
          if (targetMethods.isSingleResolution()) {
            addIfNotAbstract.accept(targetMethods.getSingleTarget());
          }
        }
      }
      return result;
    }
  }

  abstract static class EmptyResult extends ResolutionResult {

    @Override
    public final DexEncodedMethod lookupInvokeSuperTarget(DexType context, AppInfo appInfo) {
      return null;
    }

    @Override
    public final Set<DexEncodedMethod> lookupVirtualTargets(AppInfoWithSubtyping appInfo) {
      return null;
    }

    @Override
    public final Set<DexEncodedMethod> lookupInterfaceTargets(AppInfoWithSubtyping appInfo) {
      return null;
    }
  }

  /** Singleton result for the special case resolving the array clone() method. */
  public static class ArrayCloneMethodResult extends EmptyResult {

    static final ArrayCloneMethodResult INSTANCE = new ArrayCloneMethodResult();

    private ArrayCloneMethodResult() {
      // Intentionally left empty.
    }

    @Override
    public boolean isAccessibleFrom(DexProgramClass context, AppInfoWithSubtyping appInfo) {
      return true;
    }

    @Override
    public boolean isAccessibleForVirtualDispatchFrom(
        DexProgramClass context, AppInfoWithSubtyping appInfo) {
      return true;
    }

    @Override
    public boolean isValidVirtualTarget(InternalOptions options) {
      return true;
    }

    @Override
    public boolean isValidVirtualTargetForDynamicDispatch() {
      return true;
    }
  }

  /** Base class for all types of failed resolutions. */
  public abstract static class FailedResolutionResult extends EmptyResult {

    @Override
    public boolean isFailedResolution() {
      return true;
    }

    @Override
    public FailedResolutionResult asFailedResolution() {
      return this;
    }

    public void forEachFailureDependency(Consumer<DexEncodedMethod> methodCausingFailureConsumer) {
      // Default failure has no dependencies.
    }

    @Override
    public boolean isAccessibleFrom(DexProgramClass context, AppInfoWithSubtyping appInfo) {
      return false;
    }

    @Override
    public boolean isAccessibleForVirtualDispatchFrom(
        DexProgramClass context, AppInfoWithSubtyping appInfo) {
      return false;
    }

    @Override
    public boolean isValidVirtualTarget(InternalOptions options) {
      return false;
    }

    @Override
    public boolean isValidVirtualTargetForDynamicDispatch() {
      return false;
    }
  }

  public static class ClassNotFoundResult extends FailedResolutionResult {
    static final ClassNotFoundResult INSTANCE = new ClassNotFoundResult();

    private ClassNotFoundResult() {
      // Intentionally left empty.
    }
  }

  public static class IncompatibleClassResult extends FailedResolutionResult {
    static final IncompatibleClassResult INSTANCE =
        new IncompatibleClassResult(Collections.emptyList());

    private final Collection<DexEncodedMethod> methodsCausingError;

    private IncompatibleClassResult(Collection<DexEncodedMethod> methodsCausingError) {
      this.methodsCausingError = methodsCausingError;
    }

    static IncompatibleClassResult create(Collection<DexEncodedMethod> methodsCausingError) {
      return methodsCausingError.isEmpty()
          ? INSTANCE
          : new IncompatibleClassResult(methodsCausingError);
    }

    @Override
    public void forEachFailureDependency(Consumer<DexEncodedMethod> methodCausingFailureConsumer) {
      this.methodsCausingError.forEach(methodCausingFailureConsumer);
    }
  }

  public static class NoSuchMethodResult extends FailedResolutionResult {
    static final NoSuchMethodResult INSTANCE = new NoSuchMethodResult();

    private NoSuchMethodResult() {
      // Intentionally left empty.
    }
  }
}
