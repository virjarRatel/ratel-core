// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.graph.ResolutionResult.IncompatibleClassResult;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.synthetic.ExceptionThrowingSourceCode;
import com.android.tools.r8.ir.synthetic.ForwardMethodSourceCode;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.objectweb.asm.Opcodes;

/**
 * Default and static method interface desugaring processor for classes.
 *
 * <p>The core algorithm of the class processing is to ensure that for any type, all of its super
 * and implements hierarchy is computed first, and based on the summaries of these types the summary
 * of the class can be computed and the required forwarding methods on that type can be generated.
 * In other words, the traversal is in top-down (edges from type to its subtypes) topological order.
 * The traversal is lazy, starting from the unordered set of program classes.
 */
final class ClassProcessor {

  // Collection for method signatures that may cause forwarding methods to be created.
  private static class MethodSignatures {
    static final MethodSignatures EMPTY = new MethodSignatures(Collections.emptySet());

    static MethodSignatures create(Set<Wrapper<DexMethod>> signatures) {
      return signatures.isEmpty() ? EMPTY : new MethodSignatures(signatures);
    }

    final Set<Wrapper<DexMethod>> signatures;

    MethodSignatures(Set<Wrapper<DexMethod>> signatures) {
      this.signatures = Collections.unmodifiableSet(signatures);
    }

    MethodSignatures merge(MethodSignatures other) {
      if (isEmpty()) {
        return other;
      }
      if (other.isEmpty()) {
        return this;
      }
      Set<Wrapper<DexMethod>> merged = new HashSet<>(signatures);
      merged.addAll(other.signatures);
      return signatures.size() == merged.size() ? this : new MethodSignatures(merged);
    }

    MethodSignatures merge(List<MethodSignatures> others) {
      MethodSignatures merged = this;
      for (MethodSignatures other : others) {
        merged = merged.merge(others);
      }
      return merged;
    }

    boolean isEmpty() {
      return signatures.isEmpty();
    }
  }

  // Collection of information known at the point of a given (non-library) class.
  // This info is immutable and shared as it is often the same on a significant part of the
  // class hierarchy. Thus, in the case of additions the parent pointer will contain prior info.
  private static class ClassInfo {

    static final ClassInfo EMPTY = new ClassInfo(null, ImmutableList.of());

    final ClassInfo parent;

    // List of methods that are known to be forwarded to by a forwarding method at this point in the
    // class hierarchy. This set consists of the default interface methods, i.e., the targets of the
    // forwarding methods, *not* the forwarding methods themselves.
    final ImmutableList<DexEncodedMethod> forwardedMethodTargets;

    ClassInfo(ClassInfo parent, ImmutableList<DexEncodedMethod> forwardedMethodTargets) {
      this.parent = parent;
      this.forwardedMethodTargets = forwardedMethodTargets;
    }

    static ClassInfo create(
        ClassInfo parent, ImmutableList<DexEncodedMethod> forwardedMethodTargets) {
      return forwardedMethodTargets.isEmpty()
          ? parent
          : new ClassInfo(parent, forwardedMethodTargets);
    }

    public boolean isEmpty() {
      return this == EMPTY;
    }

    boolean isTargetedByForwards(DexEncodedMethod method) {
      return forwardedMethodTargets.contains(method)
          || (parent != null && parent.isTargetedByForwards(method));
    }
  }

  // Helper to keep track of the direct active subclass and nearest program subclass for reporting.
  private static class ReportingContext {
    final DexClass directSubClass;
    final DexProgramClass closestProgramSubClass;

    public ReportingContext(DexClass directSubClass, DexProgramClass closestProgramSubClass) {
      this.directSubClass = directSubClass;
      this.closestProgramSubClass = closestProgramSubClass;
    }

    ReportingContext forClass(DexClass directSubClass) {
      return new ReportingContext(
          directSubClass,
          directSubClass.isProgramClass()
              ? directSubClass.asProgramClass()
              : closestProgramSubClass);
    }

    public void reportDependency(DexClass clazz, AppView<?> appView) {
      // If the direct subclass is in the compilation unit, report its dependencies.
      if (clazz != directSubClass && directSubClass.isProgramClass()) {
        InterfaceMethodRewriter.reportDependencyEdge(
            clazz, directSubClass.asProgramClass(), appView);
      }
    }

    public void reportMissingType(DexType missingType, InterfaceMethodRewriter rewriter) {
      rewriter.warnMissingInterface(closestProgramSubClass, closestProgramSubClass, missingType);
    }
  }

  // Specialized context to disable reporting when traversing the library strucure.
  private static class LibraryReportingContext extends ReportingContext {
    static final LibraryReportingContext LIBRARY_CONTEXT = new LibraryReportingContext();

    LibraryReportingContext() {
      super(null, null);
    }

    @Override
    ReportingContext forClass(DexClass directSubClass) {
      return this;
    }

    @Override
    public void reportDependency(DexClass clazz, AppView<?> appView) {
      // Don't report dependencies in the library.
    }

    @Override
    public void reportMissingType(DexType missingType, InterfaceMethodRewriter rewriter) {
      // Ignore missing types in the library.
    }
  }

  private final AppView<?> appView;
  private final DexItemFactory dexItemFactory;
  private final InterfaceMethodRewriter rewriter;
  private final Consumer<DexEncodedMethod> newSynthesizedMethodConsumer;
  private final MethodSignatureEquivalence equivalence = MethodSignatureEquivalence.get();
  private final boolean needsLibraryInfo;

  // Mapping from program and classpath classes to their information summary.
  private final Map<DexClass, ClassInfo> classInfo = new IdentityHashMap<>();

  // Mapping from library classes to their information summary.
  private final Map<DexLibraryClass, MethodSignatures> libraryClassInfo = new IdentityHashMap<>();

  // Mapping from arbitrary interfaces to an information summary.
  private final Map<DexClass, MethodSignatures> interfaceInfo = new IdentityHashMap<>();

  // Mapping from actual program classes to the synthesized forwarding methods to be created.
  private final Map<DexProgramClass, List<DexEncodedMethod>> newSyntheticMethods =
      new IdentityHashMap<>();

  ClassProcessor(
      AppView<?> appView,
      InterfaceMethodRewriter rewriter,
      Consumer<DexEncodedMethod> newSynthesizedMethodConsumer) {
    this.appView = appView;
    this.dexItemFactory = appView.dexItemFactory();
    this.rewriter = rewriter;
    this.newSynthesizedMethodConsumer = newSynthesizedMethodConsumer;
    needsLibraryInfo =
        !appView.options().desugaredLibraryConfiguration.getEmulateLibraryInterface().isEmpty()
            || !appView
                .options()
                .desugaredLibraryConfiguration
                .getRetargetCoreLibMember()
                .isEmpty();
  }

  private boolean needsLibraryInfo() {
    return needsLibraryInfo;
  }

  private boolean ignoreLibraryInfo() {
    return !needsLibraryInfo;
  }

  public void processClass(DexProgramClass clazz) {
    visitClassInfo(clazz, new ReportingContext(clazz, clazz));
  }

  final void addSyntheticMethods() {
    for (DexProgramClass clazz : newSyntheticMethods.keySet()) {
      List<DexEncodedMethod> newForwardingMethods = newSyntheticMethods.get(clazz);
      if (newForwardingMethods != null) {
        clazz.appendVirtualMethods(newForwardingMethods);
        newForwardingMethods.forEach(newSynthesizedMethodConsumer);
      }
    }
  }

  // Computes the set of method signatures that may need forwarding methods on derived classes.
  private MethodSignatures computeInterfaceInfo(DexClass iface, MethodSignatures signatures) {
    assert iface.isInterface();
    assert iface.superType == dexItemFactory.objectType;
    // Add non-library default methods as well as those for desugared library classes.
    if (!iface.isLibraryClass() || (needsLibraryInfo() && rewriter.isInDesugaredLibrary(iface))) {
      List<DexEncodedMethod> methods = iface.virtualMethods();
      List<Wrapper<DexMethod>> additions = new ArrayList<>(methods.size());
      for (DexEncodedMethod method : methods) {
        if (method.isDefaultMethod()) {
          additions.add(equivalence.wrap(method.method));
        }
      }
      if (!additions.isEmpty()) {
        signatures = signatures.merge(MethodSignatures.create(new HashSet<>(additions)));
      }
    }
    return signatures;
  }

  // Computes the set of signatures of that may need forwarding methods on classes that derive
  // from a library class.
  private MethodSignatures computeLibraryClassInfo(
      DexLibraryClass clazz, MethodSignatures signatures) {
    // The result is the identity as the library class does not itself contribute to the set.
    return signatures;
  }

  // The computation of a class information and the insertions of forwarding methods.
  private ClassInfo computeClassInfo(
      DexClass clazz, ClassInfo superInfo, MethodSignatures signatures) {
    Builder<DexEncodedMethod> additionalForwards = ImmutableList.builder();
    for (Wrapper<DexMethod> wrapper : signatures.signatures) {
      resolveForwardForSignature(
          clazz,
          wrapper.get(),
          (targetHolder, target) -> {
            if (!superInfo.isTargetedByForwards(target)) {
              additionalForwards.add(target);
              addForwardingMethod(targetHolder, target, clazz);
            }
          });
    }
    return ClassInfo.create(superInfo, additionalForwards.build());
  }

  // Resolves a method signature from the point of 'clazz', if it must target a default method
  // the 'addForward' call-back is called with the target of the forward.
  private void resolveForwardForSignature(
      DexClass clazz, DexMethod method, BiConsumer<DexClass, DexEncodedMethod> addForward) {
    ResolutionResult resolution = appView.appInfo().resolveMethod(clazz, method);
    // If resolution fails, install a method throwing IncompatibleClassChangeError.
    if (resolution.isFailedResolution()) {
      assert resolution instanceof IncompatibleClassResult;
      addICCEThrowingMethod(method, clazz);
      return;
    }
    DexEncodedMethod target = resolution.getSingleTarget();
    DexClass targetHolder = appView.definitionFor(target.method.holder);
    // Don-t forward if the target is explicitly marked as 'dont-rewrite'
    if (targetHolder == null || dontRewrite(targetHolder, target)) {
      return;
    }

    // If resolution targets a default interface method, forward it.
    if (targetHolder.isInterface() && target.isDefaultMethod()) {
      addForward.accept(targetHolder, target);
      return;
    }

    // Remaining edge cases only pertain to desugaring of library methods.
    DexLibraryClass libraryHolder = targetHolder.asLibraryClass();
    if (libraryHolder == null || ignoreLibraryInfo()) {
      return;
    }

    if (isRetargetMethod(libraryHolder, target)) {
      addForward.accept(targetHolder, target);
      return;
    }

    // If target is a non-interface library class it may be an emulated interface.
    if (!libraryHolder.isInterface()) {
      // Here we use step-3 of resolution to find a maximally specific default interface method.
      target =
          appView
              .appInfo()
              .resolveMaximallySpecificMethods(libraryHolder, method)
              .getSingleTarget();
      if (target != null && rewriter.isEmulatedInterface(target.method.holder)) {
        targetHolder = appView.definitionFor(target.method.holder);
        addForward.accept(targetHolder, target);
      }
    }
  }

  private boolean isRetargetMethod(DexLibraryClass holder, DexEncodedMethod method) {
    assert needsLibraryInfo();
    assert holder.type == method.method.holder;
    assert method.isVirtualMethod();
    if (method.isFinal()) {
      return false;
    }
    Map<DexType, DexType> typeMap =
        appView
            .options()
            .desugaredLibraryConfiguration
            .getRetargetCoreLibMember()
            .get(method.method.name);
    return typeMap != null && typeMap.containsKey(holder.type);
  }

  private boolean dontRewrite(DexClass clazz, DexEncodedMethod method) {
    return needsLibraryInfo() && clazz.isLibraryClass() && rewriter.dontRewrite(method.method);
  }

  // Construction of actual forwarding methods.

  private void addSyntheticMethod(DexProgramClass clazz, DexEncodedMethod newMethod) {
    newSyntheticMethods.computeIfAbsent(clazz, key -> new ArrayList<>()).add(newMethod);
  }

  private void addICCEThrowingMethod(DexMethod method, DexClass clazz) {
    if (!clazz.isProgramClass()) {
      return;
    }
    DexMethod newMethod = dexItemFactory.createMethod(clazz.type, method.proto, method.name);
    DexEncodedMethod newEncodedMethod =
        new DexEncodedMethod(
            newMethod,
            MethodAccessFlags.fromCfAccessFlags(Opcodes.ACC_PUBLIC, false),
            DexAnnotationSet.empty(),
            ParameterAnnotationsList.empty(),
            new SynthesizedCode(
                callerPosition ->
                    new ExceptionThrowingSourceCode(
                        clazz.type, method, callerPosition, dexItemFactory.icceType)));
    addSyntheticMethod(clazz.asProgramClass(), newEncodedMethod);
  }

  // Note: The parameter 'target' may be a public method on a class in case of desugared
  // library retargeting (See below target.isInterface check).
  private void addForwardingMethod(DexClass targetHolder, DexEncodedMethod target, DexClass clazz) {
    assert targetHolder != null;
    if (!clazz.isProgramClass()) {
      return;
    }
    DexMethod method = target.method;
    // NOTE: Never add a forwarding method to methods of classes unknown or coming from android.jar
    // even if this results in invalid code, these classes are never desugared.
    // In desugared library, emulated interface methods can be overridden by retarget lib members.
    DexMethod forwardMethod =
        targetHolder.isInterface()
            ? rewriter.defaultAsMethodOfCompanionClass(method)
            : retargetMethod(appView, method);
    // New method will have the same name, proto, and also all the flags of the
    // default method, including bridge flag.
    DexMethod newMethod = dexItemFactory.createMethod(clazz.type, method.proto, method.name);
    MethodAccessFlags newFlags = target.accessFlags.copy();
    // Some debuggers (like IntelliJ) automatically skip synthetic methods on single step.
    newFlags.setSynthetic();
    ForwardMethodSourceCode.Builder forwardSourceCodeBuilder =
        ForwardMethodSourceCode.builder(newMethod);
    forwardSourceCodeBuilder
        .setReceiver(clazz.type)
        .setTarget(forwardMethod)
        .setInvokeType(Invoke.Type.STATIC)
        .setIsInterface(false); // Holder is companion class, not an interface.
    DexEncodedMethod newEncodedMethod =
        new DexEncodedMethod(
            newMethod,
            newFlags,
            target.annotations,
            target.parameterAnnotationsList,
            new SynthesizedCode(forwardSourceCodeBuilder::build));
    addSyntheticMethod(clazz.asProgramClass(), newEncodedMethod);
  }

  static DexMethod retargetMethod(AppView<?> appView, DexMethod method) {
    Map<DexString, Map<DexType, DexType>> retargetCoreLibMember =
        appView.options().desugaredLibraryConfiguration.getRetargetCoreLibMember();
    Map<DexType, DexType> typeMap = retargetCoreLibMember.get(method.name);
    assert typeMap != null;
    assert typeMap.get(method.holder) != null;
    return appView
        .dexItemFactory()
        .createMethod(
            typeMap.get(method.holder),
            appView.dexItemFactory().prependTypeToProto(method.holder, method.proto),
            method.name);
  }

  // Topological order traversal and its helpers.

  private DexClass definitionOrNull(DexType type, ReportingContext context) {
    // No forwards at the top of the class hierarchy (assuming java.lang.Object is never amended).
    if (type == null || type == dexItemFactory.objectType) {
      return null;
    }
    DexClass clazz = appView.definitionFor(type);
    if (clazz == null) {
      context.reportMissingType(type, rewriter);
      return null;
    }
    return clazz;
  }

  private ClassInfo visitClassInfo(DexType type, ReportingContext context) {
    DexClass clazz = definitionOrNull(type, context);
    return clazz == null ? ClassInfo.EMPTY : visitClassInfo(clazz, context);
  }

  private ClassInfo visitClassInfo(DexClass clazz, ReportingContext context) {
    assert !clazz.isInterface();
    if (clazz.isLibraryClass()) {
      return ClassInfo.EMPTY;
    }
    context.reportDependency(clazz, appView);
    return classInfo.computeIfAbsent(clazz, key -> visitClassInfoRaw(key, context));
  }

  private ClassInfo visitClassInfoRaw(DexClass clazz, ReportingContext context) {
    // We compute both library and class information, but one of them is empty, since a class is
    // a library class or is not, but cannot be both.
    ReportingContext thisContext = context.forClass(clazz);
    ClassInfo superInfo = visitClassInfo(clazz.superType, thisContext);
    MethodSignatures signatures = visitLibraryClassInfo(clazz.superType);
    assert superInfo.isEmpty() || signatures.isEmpty();
    for (DexType iface : clazz.interfaces.values) {
      signatures = signatures.merge(visitInterfaceInfo(iface, thisContext));
    }
    return computeClassInfo(clazz, superInfo, signatures);
  }

  private MethodSignatures visitLibraryClassInfo(DexType type) {
    // No desugaring required, no library class analysis.
    if (ignoreLibraryInfo()) {
      return MethodSignatures.EMPTY;
    }
    DexClass clazz = definitionOrNull(type, LibraryReportingContext.LIBRARY_CONTEXT);
    return clazz == null ? MethodSignatures.EMPTY : visitLibraryClassInfo(clazz);
  }

  private MethodSignatures visitLibraryClassInfo(DexClass clazz) {
    assert !clazz.isInterface();
    return clazz.isLibraryClass()
        ? libraryClassInfo.computeIfAbsent(clazz.asLibraryClass(), this::visitLibraryClassInfoRaw)
        : MethodSignatures.EMPTY;
  }

  private MethodSignatures visitLibraryClassInfoRaw(DexLibraryClass clazz) {
    MethodSignatures signatures = visitLibraryClassInfo(clazz.superType);
    for (DexType iface : clazz.interfaces.values) {
      signatures =
          signatures.merge(visitInterfaceInfo(iface, LibraryReportingContext.LIBRARY_CONTEXT));
    }
    return computeLibraryClassInfo(clazz, signatures);
  }

  private MethodSignatures visitInterfaceInfo(DexType iface, ReportingContext context) {
    DexClass definition = definitionOrNull(iface, context);
    return definition == null ? MethodSignatures.EMPTY : visitInterfaceInfo(definition, context);
  }

  private MethodSignatures visitInterfaceInfo(DexClass iface, ReportingContext context) {
    if (iface.isLibraryClass() && ignoreLibraryInfo()) {
      return MethodSignatures.EMPTY;
    }
    context.reportDependency(iface, appView);
    return interfaceInfo.computeIfAbsent(iface, key -> visitInterfaceInfoRaw(key, context));
  }

  private MethodSignatures visitInterfaceInfoRaw(DexClass iface, ReportingContext context) {
    ReportingContext thisContext = context.forClass(iface);
    MethodSignatures signatures = MethodSignatures.EMPTY;
    for (DexType superiface : iface.interfaces.values) {
      signatures = signatures.merge(visitInterfaceInfo(superiface, thisContext));
    }
    return computeInterfaceInfo(iface, signatures);
  }
}
