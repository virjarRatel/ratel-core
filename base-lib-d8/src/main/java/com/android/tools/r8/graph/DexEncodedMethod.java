// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_INLINING_CANDIDATE_ANY;
import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_INLINING_CANDIDATE_SAME_CLASS;
import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_INLINING_CANDIDATE_SAME_NEST;
import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_INLINING_CANDIDATE_SAME_PACKAGE;
import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_INLINING_CANDIDATE_SUBCLASS;
import static com.android.tools.r8.graph.DexEncodedMethod.CompilationState.PROCESSED_NOT_INLINING_CANDIDATE;

import com.android.tools.r8.cf.code.CfConstNull;
import com.android.tools.r8.cf.code.CfConstString;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfStackInstruction.Opcode;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.code.Const;
import com.android.tools.r8.code.ConstString;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.InvokeDirect;
import com.android.tools.r8.code.InvokeStatic;
import com.android.tools.r8.code.NewInstance;
import com.android.tools.r8.code.Throw;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.JumboStringRewriter;
import com.android.tools.r8.dex.MethodToCodeObjectMapping;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.desugar.NestBasedAccessDesugaring.DexFieldWithAccess;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.NestUtils;
import com.android.tools.r8.ir.optimize.info.CallSiteOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.DefaultMethodOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.MethodOptimizationInfo;
import com.android.tools.r8.ir.optimize.info.UpdatableMethodOptimizationInfo;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.ir.synthetic.EmulateInterfaceSyntheticCfCodeProvider;
import com.android.tools.r8.ir.synthetic.FieldAccessorSourceCode;
import com.android.tools.r8.ir.synthetic.ForwardMethodSourceCode;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OptionalBool;
import com.android.tools.r8.utils.Pair;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import org.objectweb.asm.Opcodes;

public class DexEncodedMethod extends KeyedDexItem<DexMethod> {

  public static final String CONFIGURATION_DEBUGGING_PREFIX = "Shaking error: Missing method in ";

  /**
   * Encodes the processing state of a method.
   *
   * <p>We also use this enum to encode under what constraints a method may be inlined.
   */
  // TODO(b/128967328): Need to extend this to a state with the context.
  public enum CompilationState {
    /**
     * Has not been processed, yet.
     */
    NOT_PROCESSED,
    /**
     * Has been processed but cannot be inlined due to instructions that are not supported.
     */
    PROCESSED_NOT_INLINING_CANDIDATE,
    /**
     * Code only contains instructions that access public entities and can this be inlined into any
     * context.
     */
    PROCESSED_INLINING_CANDIDATE_ANY,
    /**
     * Code also contains instructions that access protected entities that reside in a different
     * package and hence require subclass relationship to be visible.
     */
    PROCESSED_INLINING_CANDIDATE_SUBCLASS,
    /**
     * Code contains instructions that reference package private entities or protected entities from
     * the same package.
     */
    PROCESSED_INLINING_CANDIDATE_SAME_PACKAGE,
    /** Code contains instructions that reference private entities. */
    PROCESSED_INLINING_CANDIDATE_SAME_NEST,
    /** Code contains invoke super */
    PROCESSED_INLINING_CANDIDATE_SAME_CLASS
  }

  public static final DexEncodedMethod[] EMPTY_ARRAY = {};
  public static final DexEncodedMethod SENTINEL =
      new DexEncodedMethod(null, null, null, ParameterAnnotationsList.empty(), null);
  public static final DexEncodedMethod ANNOTATION_REFERENCE =
      new DexEncodedMethod(null, null, null, ParameterAnnotationsList.empty(), null);
  public static final Int2ReferenceMap<DebugLocalInfo> NO_PARAMETER_INFO =
      new Int2ReferenceArrayMap<>(0);

  public final DexMethod method;
  public final MethodAccessFlags accessFlags;
  public DexAnnotationSet annotations;
  public ParameterAnnotationsList parameterAnnotationsList;
  private Code code;
  // TODO(b/128967328): towards finer-grained inlining constraints,
  //   we need to maintain a set of states with (potentially different) contexts.
  private CompilationState compilationState = CompilationState.NOT_PROCESSED;
  private MethodOptimizationInfo optimizationInfo = DefaultMethodOptimizationInfo.DEFAULT_INSTANCE;
  private CallSiteOptimizationInfo callSiteOptimizationInfo = CallSiteOptimizationInfo.BOTTOM;
  private int classFileVersion = -1;

  private DexEncodedMethod defaultInterfaceMethodImplementation = null;

  private OptionalBool isLibraryMethodOverride = OptionalBool.unknown();

  private Int2ReferenceMap<DebugLocalInfo> parameterInfo = NO_PARAMETER_INFO;

  // This flag indicates the current instance is no longer up-to-date as another instance was
  // created based on this. Any further (public) operations on this instance will raise an error
  // to catch potential bugs due to the inconsistency (e.g., http://b/111893131)
  // Any newly added `public` method should check if `this` instance is obsolete.
  private boolean obsolete = false;

  private void checkIfObsolete() {
    assert !obsolete;
  }

  public boolean isObsolete() {
    // Do not be cheating. This util can be used only if you're going to do appropriate action,
    // e.g., using GraphLense#mapDexEncodedMethod to look up the correct, up-to-date instance.
    return obsolete;
  }

  public void setObsolete() {
    // By assigning an Exception, you can see when/how this instance becomes obsolete v.s.
    // when/how such obsolete instance is used.
    obsolete = true;
  }

  public DexEncodedMethod getDefaultInterfaceMethodImplementation() {
    return defaultInterfaceMethodImplementation;
  }

  public void setDefaultInterfaceMethodImplementation(DexEncodedMethod implementation) {
    assert defaultInterfaceMethodImplementation == null;
    assert implementation != null;
    assert code != null;
    assert code == implementation.getCode();
    accessFlags.setAbstract();
    removeCode();
    defaultInterfaceMethodImplementation = implementation;
  }

  /**
   * Flags this method as no longer being obsolete.
   *
   * Example use case: The vertical class merger optimistically merges two classes before it is
   * guaranteed that the two classes can be merged. In this process, methods are moved from the
   * source class to the target class using {@link #toTypeSubstitutedMethod(DexMethod)}, which
   * causes the original methods of the source class to become obsolete. If vertical class merging
   * is aborted, the original methods of the source class needs to be marked as not being obsolete.
   */
  public void unsetObsolete() {
    obsolete = false;
  }

  public DexEncodedMethod(
      DexMethod method,
      MethodAccessFlags accessFlags,
      DexAnnotationSet annotations,
      ParameterAnnotationsList parameterAnnotationsList,
      Code code) {
    this.method = method;
    this.accessFlags = accessFlags;
    this.annotations = annotations;
    this.parameterAnnotationsList = parameterAnnotationsList;
    this.code = code;
    assert code == null || !shouldNotHaveCode();
    assert parameterAnnotationsList != null;
  }

  public DexEncodedMethod(
      DexMethod method,
      MethodAccessFlags flags,
      DexAnnotationSet annotationSet,
      ParameterAnnotationsList annotationsList,
      Code code,
      int classFileVersion) {
    this(method, flags, annotationSet, annotationsList, code);
    this.classFileVersion = classFileVersion;
  }

  public OptionalBool isLibraryMethodOverride() {
    return isVirtualMethod() ? isLibraryMethodOverride : OptionalBool.FALSE;
  }

  public void setLibraryMethodOverride(OptionalBool isLibraryMethodOverride) {
    assert isVirtualMethod();
    assert !isLibraryMethodOverride.isUnknown();
    assert isLibraryMethodOverride.isPossiblyFalse()
            || this.isLibraryMethodOverride.isPossiblyTrue()
        : "Method `"
            + method.toSourceString()
            + "` went from not overriding a library method to overriding a library method";
    assert isLibraryMethodOverride.isPossiblyTrue()
            || this.isLibraryMethodOverride.isPossiblyFalse()
        : "Method `"
            + method.toSourceString()
            + "` went from overriding a library method to not overriding a library method";
    this.isLibraryMethodOverride = isLibraryMethodOverride;
  }

  public boolean isProgramMethod(DexDefinitionSupplier definitions) {
    if (method.holder.isClassType()) {
      DexClass clazz = definitions.definitionFor(method.holder);
      return clazz != null && clazz.isProgramClass();
    }
    return false;
  }

  public boolean isProcessed() {
    checkIfObsolete();
    return compilationState != CompilationState.NOT_PROCESSED;
  }

  public boolean isAbstract() {
    return accessFlags.isAbstract();
  }

  public boolean isFinal() {
    return accessFlags.isFinal();
  }

  public boolean isInitializer() {
    checkIfObsolete();
    return isInstanceInitializer() || isClassInitializer();
  }

  public boolean isInstanceInitializer() {
    checkIfObsolete();
    return accessFlags.isConstructor() && !accessFlags.isStatic();
  }

  public boolean isDefaultInitializer() {
    checkIfObsolete();
    return isInstanceInitializer() && method.proto.parameters.isEmpty();
  }

  public boolean isClassInitializer() {
    checkIfObsolete();
    return accessFlags.isConstructor() && accessFlags.isStatic();
  }

  public boolean isDefaultMethod() {
    // Assumes holder is an interface
    return !isStatic() && !isAbstract() && !isPrivateMethod() && !isInstanceInitializer();
  }

  /**
   * Returns true if this method can be invoked via invoke-virtual, invoke-super or
   * invoke-interface.
   */
  public boolean isVirtualMethod() {
    checkIfObsolete();
    return !accessFlags.isStatic() && !accessFlags.isPrivate() && !accessFlags.isConstructor();
  }

  /**
   * Returns true if this method can be invoked via invoke-virtual, invoke-super or invoke-interface
   * and is non-abstract.
   */
  public boolean isNonAbstractVirtualMethod() {
    checkIfObsolete();
    return isVirtualMethod() && !accessFlags.isAbstract();
  }

  public boolean isNonAbstractNonNativeMethod() {
    checkIfObsolete();
    return !accessFlags.isAbstract() && !accessFlags.isNative();
  }

  public boolean isPublicized() {
    checkIfObsolete();
    return accessFlags.isPromotedToPublic();
  }

  public boolean isPublicMethod() {
    checkIfObsolete();
    return accessFlags.isPublic();
  }

  public boolean isPrivateMethod() {
    checkIfObsolete();
    return accessFlags.isPrivate();
  }

  /**
   * Returns true if this method can be invoked via invoke-direct.
   */
  public boolean isDirectMethod() {
    checkIfObsolete();
    return (accessFlags.isPrivate() || accessFlags.isConstructor()) && !accessFlags.isStatic();
  }

  @Override
  public boolean isStatic() {
    checkIfObsolete();
    return accessFlags.isStatic();
  }

  @Override
  public boolean isStaticMember() {
    checkIfObsolete();
    return isStatic();
  }

  /**
   * Returns true if this method is synthetic.
   */
  public boolean isSyntheticMethod() {
    checkIfObsolete();
    return accessFlags.isSynthetic();
  }

  public boolean isOnlyInlinedIntoNestMembers() {
    return compilationState == PROCESSED_INLINING_CANDIDATE_SAME_NEST;
  }

  public boolean isInliningCandidate(
      DexEncodedMethod container,
      Reason inliningReason,
      AppInfoWithSubtyping appInfo,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    checkIfObsolete();
    return isInliningCandidate(
        container.method.holder, inliningReason, appInfo, whyAreYouNotInliningReporter);
  }

  public boolean isInliningCandidate(
      DexType containerType,
      Reason inliningReason,
      AppInfoWithSubtyping appInfo,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    checkIfObsolete();

    if (inliningReason == Reason.FORCE) {
      // Make sure we would be able to inline this normally.
      if (!isInliningCandidate(
          containerType, Reason.SIMPLE, appInfo, whyAreYouNotInliningReporter)) {
        // If not, raise a flag, because some optimizations that depend on force inlining would
        // silently produce an invalid code, which is worse than an internal error.
        throw new InternalCompilerError("FORCE inlining on non-inlinable: " + toSourceString());
      }
      return true;
    }

    // TODO(b/128967328): inlining candidate should satisfy all states if multiple states are there.
    switch (compilationState) {
      case PROCESSED_INLINING_CANDIDATE_ANY:
        return true;

      case PROCESSED_INLINING_CANDIDATE_SUBCLASS:
        if (appInfo.isSubtype(containerType, method.holder)) {
          return true;
        }
        whyAreYouNotInliningReporter.reportCallerNotSubtype();
        return false;

      case PROCESSED_INLINING_CANDIDATE_SAME_PACKAGE:
        if (containerType.isSamePackage(method.holder)) {
          return true;
        }
        whyAreYouNotInliningReporter.reportCallerNotSamePackage();
        return false;

      case PROCESSED_INLINING_CANDIDATE_SAME_NEST:
        if (NestUtils.sameNest(containerType, method.holder, appInfo)) {
          return true;
        }
        whyAreYouNotInliningReporter.reportCallerNotSameNest();
        return false;

      case PROCESSED_INLINING_CANDIDATE_SAME_CLASS:
        if (containerType == method.holder) {
          return true;
        }
        whyAreYouNotInliningReporter.reportCallerNotSameClass();
        return false;

      case PROCESSED_NOT_INLINING_CANDIDATE:
        whyAreYouNotInliningReporter.reportInlineeNotInliningCandidate();
        return false;

      case NOT_PROCESSED:
        whyAreYouNotInliningReporter.reportInlineeNotProcessed();
        return false;

      default:
        throw new Unreachable("Unexpected compilation state: " + compilationState);
    }
  }

  public boolean markProcessed(ConstraintWithTarget state) {
    checkIfObsolete();
    CompilationState prevCompilationState = compilationState;
    switch (state.constraint) {
      case ALWAYS:
        compilationState = PROCESSED_INLINING_CANDIDATE_ANY;
        break;
      case SUBCLASS:
        compilationState = PROCESSED_INLINING_CANDIDATE_SUBCLASS;
        break;
      case PACKAGE:
        compilationState = PROCESSED_INLINING_CANDIDATE_SAME_PACKAGE;
        break;
      case SAMENEST:
        compilationState = PROCESSED_INLINING_CANDIDATE_SAME_NEST;
        break;
      case SAMECLASS:
        compilationState = PROCESSED_INLINING_CANDIDATE_SAME_CLASS;
        break;
      case NEVER:
        compilationState = PROCESSED_NOT_INLINING_CANDIDATE;
        break;
    }
    return prevCompilationState != compilationState;
  }

  public void markNotProcessed() {
    checkIfObsolete();
    compilationState = CompilationState.NOT_PROCESSED;
  }

  public IRCode buildIR(AppView<?> appView, Origin origin) {
    checkIfObsolete();
    return code == null ? null : code.buildIR(this, appView, origin);
  }

  public IRCode buildInliningIR(
      DexEncodedMethod context,
      AppView<?> appView,
      ValueNumberGenerator valueNumberGenerator,
      Position callerPosition,
      Origin origin) {
    checkIfObsolete();
    return code.buildInliningIR(
        context, this, appView, valueNumberGenerator, callerPosition, origin);
  }

  public void setCode(Code newCode, AppView<?> appView) {
    checkIfObsolete();
    // If the locals are not kept, we might still need information to satisfy -keepparameternames.
    // The information needs to be retrieved on the original code object before replacing it.
    if (code != null && code.isCfCode() && !hasParameterInfo() && !keepLocals(appView.options())) {
      setParameterInfo(code.collectParameterInfo(this, appView));
    }
    code = newCode;
  }

  public void setCode(IRCode ir, RegisterAllocator registerAllocator, AppView<?> appView) {
    checkIfObsolete();
    DexBuilder builder = new DexBuilder(ir, registerAllocator);
    setCode(builder.build(), appView);
  }

  public boolean keepLocals(InternalOptions options) {
    if (options.testing.noLocalsTableOnInput) {
      return false;
    }
    return options.debug || getOptimizationInfo().isReachabilitySensitive();
  }

  private void setParameterInfo(Int2ReferenceMap<DebugLocalInfo> parameterInfo) {
    assert this.parameterInfo == NO_PARAMETER_INFO;
    this.parameterInfo = parameterInfo;
  }

  public boolean hasParameterInfo() {
    return parameterInfo != NO_PARAMETER_INFO;
  }

  public Map<Integer, DebugLocalInfo> getParameterInfo() {
    return parameterInfo;
  }

  @Override
  public String toString() {
    checkIfObsolete();
    return "Encoded method " + method;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems,
      DexMethod method, int instructionOffset) {
    checkIfObsolete();
    this.method.collectIndexedItems(indexedItems);
    if (code != null) {
      code.collectIndexedItems(indexedItems, this.method);
    }
    annotations.collectIndexedItems(indexedItems);
    parameterAnnotationsList.collectIndexedItems(indexedItems);
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    mixedItems.visit(this);
  }

  public void collectMixedSectionItemsWithCodeMapping(
      MixedSectionCollection mixedItems, MethodToCodeObjectMapping mapping) {
    DexCode code = mapping.getCode(this);
    if (code != null) {
      code.collectMixedSectionItems(mixedItems);
    }
    annotations.collectMixedSectionItems(mixedItems);
    parameterAnnotationsList.collectMixedSectionItems(mixedItems);
  }

  public boolean shouldNotHaveCode() {
    return accessFlags.isAbstract() || accessFlags.isNative();
  }

  public boolean hasCode() {
    return code != null;
  }

  public Code getCode() {
    checkIfObsolete();
    return code;
  }

  public void removeCode() {
    checkIfObsolete();
    code = null;
  }

  public int getClassFileVersion() {
    checkIfObsolete();
    assert classFileVersion >= 0;
    return classFileVersion;
  }

  public boolean hasClassFileVersion() {
    checkIfObsolete();
    return classFileVersion >= 0;
  }

  public void upgradeClassFileVersion(int version) {
    checkIfObsolete();
    assert version >= 0;
    assert !hasClassFileVersion() || version >= getClassFileVersion();
    classFileVersion = version;
  }

  public String qualifiedName() {
    checkIfObsolete();
    return method.qualifiedName();
  }

  public String descriptor() {
    checkIfObsolete();
    return descriptor(NamingLens.getIdentityLens());
  }

  public String descriptor(NamingLens namingLens) {
    checkIfObsolete();
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    for (DexType type : method.proto.parameters.values) {
      builder.append(namingLens.lookupDescriptor(type).toString());
    }
    builder.append(")");
    builder.append(namingLens.lookupDescriptor(method.proto.returnType).toString());
    return builder.toString();
  }

  public String toSmaliString(ClassNameMapper naming) {
    checkIfObsolete();
    StringBuilder builder = new StringBuilder();
    builder.append(".method ");
    builder.append(accessFlags.toSmaliString());
    builder.append(" ");
    builder.append(method.name.toSmaliString());
    builder.append(method.proto.toSmaliString());
    builder.append("\n");
    if (code != null) {
      DexCode dexCode = code.asDexCode();
      builder.append("    .registers ");
      builder.append(dexCode.registerSize);
      builder.append("\n\n");
      builder.append(dexCode.toSmaliString(naming));
    }
    builder.append(".end method\n");
    return builder.toString();
  }

  @Override
  public String toSourceString() {
    checkIfObsolete();
    return method.toSourceString();
  }

  public DexEncodedMethod toAbstractMethod() {
    checkIfObsolete();
    // 'final' wants this to be *not* overridden, while 'abstract' wants this to be implemented in
    // a subtype, i.e., self contradict.
    assert !accessFlags.isFinal();
    // static abstract is an invalid access combination and we should never create that.
    assert !accessFlags.isStatic();
    accessFlags.setAbstract();
    this.code = null;
    return this;
  }

  /**
   * Generates a {@link DexCode} object for the given instructions.
   */
  private DexCode generateCodeFromTemplate(
      int numberOfRegisters, int outRegisters, Instruction... instructions) {
    int offset = 0;
    for (Instruction instruction : instructions) {
      instruction.setOffset(offset);
      offset += instruction.getSize();
    }
    int requiredArgRegisters = accessFlags.isStatic() ? 0 : 1;
    for (DexType type : method.proto.parameters.values) {
      requiredArgRegisters += ValueType.fromDexType(type).requiredRegisters();
    }
    return new DexCode(
        Math.max(numberOfRegisters, requiredArgRegisters),
        requiredArgRegisters,
        outRegisters,
        instructions,
        new DexCode.Try[0],
        new DexCode.TryHandler[0],
        null);
  }

  public DexCode buildEmptyThrowingDexCode() {
    Instruction insn[] = {new Const(0, 0), new Throw(0)};
    return generateCodeFromTemplate(1, 0, insn);
  }

  public DexEncodedMethod toEmptyThrowingMethod(InternalOptions options) {
    return options.isGeneratingClassFiles()
        ? toEmptyThrowingMethodCf()
        : toEmptyThrowingMethodDex();
  }

  public DexEncodedMethod toEmptyThrowingMethodDex() {
    checkIfObsolete();
    assert !shouldNotHaveCode();
    Builder builder = builder(this);
    builder.setCode(buildEmptyThrowingDexCode());
    // Note that we are not marking this instance obsolete, since this util is only used by
    // TreePruner while keeping non-live yet targeted, empty method. Such method can be retrieved
    // again only during the 2nd round of tree sharking, and seeing an obsolete empty body v.s.
    // seeing this empty throwing code do not matter.
    // If things are changed, the cure point is obsolete instances inside RootSet.
    return builder.build();
  }

  public CfCode buildEmptyThrowingCfCode() {
    CfInstruction insn[] = {new CfConstNull(), new CfThrow()};
    return new CfCode(
        method.holder,
        1,
        method.proto.parameters.size() + 1,
        Arrays.asList(insn),
        Collections.emptyList(),
        Collections.emptyList());
  }

  public DexEncodedMethod toEmptyThrowingMethodCf() {
    checkIfObsolete();
    assert !shouldNotHaveCode();
    Builder builder = builder(this);
    builder.setCode(buildEmptyThrowingCfCode());
    // Note that we are not marking this instance obsolete:
    // refer to Dex-backend version of this method above.
    return builder.build();
  }

  public DexEncodedMethod toMethodThatLogsError(AppView<?> appView) {
    if (appView.options().isGeneratingDex()) {
      return toMethodThatLogsErrorDexCode(appView.dexItemFactory());
    } else {
      return toMethodThatLogsErrorCfCode(appView.dexItemFactory());
    }
  }

  public static void setDebugInfoWithFakeThisParameter(Code code, int arity, AppView<?> appView) {
    if (code.isDexCode()) {
      DexCode dexCode = code.asDexCode();
      dexCode.setDebugInfo(dexCode.debugInfoWithFakeThisParameter(appView.dexItemFactory()));
      assert (dexCode.getDebugInfo() == null)
          || (arity == dexCode.getDebugInfo().parameters.length);
    } else {
      // TODO(b/134732760): Patch Cf debug info.
      assert appView.options().isDesugaredLibraryCompilation();
    }
  }

  private DexEncodedMethod toMethodThatLogsErrorDexCode(DexItemFactory itemFactory) {
    checkIfObsolete();
    Signature signature = MethodSignature.fromDexMethod(method);
    DexString message =
        itemFactory.createString(
            CONFIGURATION_DEBUGGING_PREFIX + method.holder.toSourceString() + ": " + signature);
    DexString tag = itemFactory.createString("[R8]");
    DexType[] args = {itemFactory.stringType, itemFactory.stringType};
    DexProto proto = itemFactory.createProto(itemFactory.intType, args);
    DexMethod logMethod =
        itemFactory.createMethod(
            itemFactory.createType("Landroid/util/Log;"), proto, itemFactory.createString("e"));
    DexType exceptionType = itemFactory.createType("Ljava/lang/RuntimeException;");
    DexMethod exceptionInitMethod =
        itemFactory.createMethod(
            exceptionType,
            itemFactory.createProto(itemFactory.voidType, itemFactory.stringType),
            itemFactory.constructorMethodName);
    DexCode code =
        generateCodeFromTemplate(
            2,
            2,
            new ConstString(0, tag),
            new ConstString(1, message),
            new InvokeStatic(2, logMethod, 0, 1, 0, 0, 0),
            new NewInstance(0, exceptionType),
            new InvokeDirect(2, exceptionInitMethod, 0, 1, 0, 0, 0),
            new Throw(0));
    Builder builder = builder(this);
    builder.setCode(code);
    setObsolete();
    return builder.build();
  }

  private DexEncodedMethod toMethodThatLogsErrorCfCode(DexItemFactory itemFactory) {
    checkIfObsolete();
    Signature signature = MethodSignature.fromDexMethod(method);
    DexString message =
        itemFactory.createString(
            CONFIGURATION_DEBUGGING_PREFIX + method.holder.toSourceString() + ": " + signature);
    DexString tag = itemFactory.createString("[R8]");
    DexType logger = itemFactory.createType("Ljava/util/logging/Logger;");
    DexMethod getLogger =
        itemFactory.createMethod(
            logger,
            itemFactory.createProto(logger, itemFactory.stringType),
            itemFactory.createString("getLogger"));
    DexMethod severe =
        itemFactory.createMethod(
            logger,
            itemFactory.createProto(itemFactory.voidType, itemFactory.stringType),
            itemFactory.createString("severe"));
    DexType exceptionType = itemFactory.createType("Ljava/lang/RuntimeException;");
    DexMethod exceptionInitMethod =
        itemFactory.createMethod(
            exceptionType,
            itemFactory.createProto(itemFactory.voidType, itemFactory.stringType),
            itemFactory.constructorMethodName);
    int locals = method.proto.parameters.size() + 1;
    if (!isStaticMember()) {
      // Consider `this` pointer
      locals++;
    }
    ImmutableList.Builder<CfInstruction> instructionBuilder = ImmutableList.builder();
    instructionBuilder
        .add(new CfConstString(tag))
        .add(new CfInvoke(Opcodes.INVOKESTATIC, getLogger, false))
        .add(new CfStore(ValueType.OBJECT, locals - 1))
        .add(new CfLoad(ValueType.OBJECT, locals - 1))
        .add(new CfConstString(message))
        .add(new CfInvoke(Opcodes.INVOKEVIRTUAL, severe, false))
        .add(new CfNew(exceptionType))
        .add(new CfStackInstruction(Opcode.Dup))
        .add(new CfConstString(message))
        .add(new CfInvoke(Opcodes.INVOKESPECIAL, exceptionInitMethod, false))
        .add(new CfThrow());
    CfCode code =
        new CfCode(
            method.holder,
            3,
            locals,
            instructionBuilder.build(),
            Collections.emptyList(),
            Collections.emptyList());
    Builder builder = builder(this);
    builder.setCode(code);
    setObsolete();
    return builder.build();
  }

  public DexEncodedMethod toTypeSubstitutedMethod(DexMethod method) {
    checkIfObsolete();
    return toTypeSubstitutedMethod(method, null);
  }

  public DexEncodedMethod toTypeSubstitutedMethod(DexMethod method, Consumer<Builder> consumer) {
    checkIfObsolete();
    if (this.method == method) {
      return this;
    }
    Builder builder = builder(this);
    builder.setMethod(method);
    // TODO(b/112847660): Fix type fixers that use this method: Class staticizer
    // TODO(b/112847660): Fix type fixers that use this method: Uninstantiated type optimization
    // TODO(b/112847660): Fix type fixers that use this method: Unused argument removal
    // TODO(b/112847660): Fix type fixers that use this method: Vertical class merger
    // setObsolete();
    if (consumer != null) {
      consumer.accept(builder);
    }
    return builder.build();
  }

  public DexEncodedMethod toRenamedMethod(DexString name, DexItemFactory factory) {
    checkIfObsolete();
    if (method.name == name) {
      return this;
    }
    DexMethod newMethod = factory.createMethod(method.holder, method.proto, name);
    Builder builder = builder(this);
    builder.setMethod(newMethod);
    setObsolete();
    return builder.build();
  }

  public DexEncodedMethod toInitializerForwardingBridge(DexClass holder, DexMethod newMethod) {
    assert accessFlags.isPrivate()
        : "Expected to create bridge for private constructor as part of nest-based access"
            + " desugaring";
    Builder builder = builder(this);
    builder.setMethod(newMethod);
    ForwardMethodSourceCode.Builder forwardSourceCodeBuilder =
        ForwardMethodSourceCode.builder(newMethod);
    forwardSourceCodeBuilder
        .setReceiver(holder.type)
        .setTargetReceiver(holder.type)
        .setTarget(method)
        .setInvokeType(Invoke.Type.DIRECT)
        .setExtraNullParameter();
    builder.setCode(
        new SynthesizedCode(
            forwardSourceCodeBuilder::build, registry -> registry.registerInvokeDirect(method)));
    assert !builder.accessFlags.isStatic();
    assert !holder.isInterface();
    builder.accessFlags.unsetPrivate();
    builder.accessFlags.setSynthetic();
    builder.accessFlags.setConstructor();
    return builder.build();
  }

  public static DexEncodedMethod createFieldAccessorBridge(
      DexFieldWithAccess fieldWithAccess, DexClass holder, DexMethod newMethod) {
    assert holder.type == fieldWithAccess.getHolder();
    MethodAccessFlags accessFlags =
        MethodAccessFlags.fromSharedAccessFlags(
            Constants.ACC_SYNTHETIC
                | Constants.ACC_STATIC
                | (holder.isInterface() ? Constants.ACC_PUBLIC : 0),
            false);
    Code code =
        new SynthesizedCode(
            callerPosition ->
                new FieldAccessorSourceCode(
                    null, newMethod, callerPosition, newMethod, fieldWithAccess),
            registry -> {
              if (fieldWithAccess.isInstanceGet()) {
                registry.registerInstanceFieldRead(fieldWithAccess.getField());
              } else if (fieldWithAccess.isStaticGet()) {
                registry.registerStaticFieldRead(fieldWithAccess.getField());
              } else if (fieldWithAccess.isInstancePut()) {
                registry.registerInstanceFieldWrite(fieldWithAccess.getField());
              } else {
                assert fieldWithAccess.isStaticPut();
                registry.registerStaticFieldWrite(fieldWithAccess.getField());
              }
            });
    return new DexEncodedMethod(
        newMethod, accessFlags, DexAnnotationSet.empty(), ParameterAnnotationsList.empty(), code);
  }

  public DexEncodedMethod toRenamedHolderMethod(DexType newHolderType, DexItemFactory factory) {
    DexEncodedMethod.Builder builder = DexEncodedMethod.builder(this);
    builder.setMethod(factory.createMethod(newHolderType, method.proto, method.name));
    return builder.build();
  }

  public static DexEncodedMethod toEmulateDispatchLibraryMethod(
      DexType interfaceType,
      DexMethod newMethod,
      DexMethod companionMethod,
      DexMethod libraryMethod,
      List<Pair<DexType, DexMethod>> extraDispatchCases,
      AppView<?> appView) {
    MethodAccessFlags accessFlags =
        MethodAccessFlags.fromSharedAccessFlags(
            Constants.ACC_SYNTHETIC | Constants.ACC_STATIC | Constants.ACC_PUBLIC, false);
    CfCode code =
        new EmulateInterfaceSyntheticCfCodeProvider(
                interfaceType, companionMethod, libraryMethod, extraDispatchCases, appView)
            .generateCfCode();
    return new DexEncodedMethod(
        newMethod, accessFlags, DexAnnotationSet.empty(), ParameterAnnotationsList.empty(), code);
  }

  public DexEncodedMethod toStaticForwardingBridge(DexClass holder, DexMethod newMethod) {
    assert accessFlags.isPrivate()
        : "Expected to create bridge for private method as part of nest-based access desugaring";
    Builder builder = builder(this);
    builder.setMethod(newMethod);
    ForwardMethodSourceCode.Builder forwardSourceCodeBuilder =
        ForwardMethodSourceCode.builder(newMethod);
    forwardSourceCodeBuilder
        .setTargetReceiver(accessFlags.isStatic() ? null : method.holder)
        .setTarget(method)
        .setInvokeType(accessFlags.isStatic() ? Invoke.Type.STATIC : Invoke.Type.DIRECT)
        .setIsInterface(holder.isInterface());
    builder.setCode(
        new SynthesizedCode(
            forwardSourceCodeBuilder::build,
            registry -> {
              if (accessFlags.isStatic()) {
                registry.registerInvokeStatic(method);
              } else {
                registry.registerInvokeDirect(method);
              }
            }));
    builder.accessFlags.setSynthetic();
    builder.accessFlags.setStatic();
    builder.accessFlags.unsetPrivate();
    if (holder.isInterface()) {
      builder.accessFlags.setPublic();
    }
    return builder.build();
  }

  public DexEncodedMethod toForwardingMethod(DexClass holder, DexDefinitionSupplier definitions) {
    checkIfObsolete();
    // Clear the final flag, as this method is now overwritten. Do this before creating the builder
    // for the forwarding method, as the forwarding method will copy the access flags from this,
    // and if different forwarding methods are created in different subclasses the first could be
    // final.
    accessFlags.demoteFromFinal();
    DexMethod newMethod =
        definitions.dexItemFactory().createMethod(holder.type, method.proto, method.name);
    Invoke.Type type = accessFlags.isStatic() ? Invoke.Type.STATIC : Invoke.Type.SUPER;
    Builder builder = builder(this);
    builder.setMethod(newMethod);
    if (accessFlags.isAbstract()) {
      // If the forwarding target is abstract, we can just create an abstract method. While it
      // will not actually forward, it will create the same exception when hit at runtime.
      builder.accessFlags.setAbstract();
    } else {
      // Create code that forwards the call to the target.
      DexClass target = definitions.definitionFor(method.holder);
      ForwardMethodSourceCode.Builder forwardSourceCodeBuilder =
          ForwardMethodSourceCode.builder(newMethod);
      forwardSourceCodeBuilder
          .setReceiver(accessFlags.isStatic() ? null : holder.type)
          .setTargetReceiver(accessFlags.isStatic() ? null : method.holder)
          .setTarget(method)
          .setInvokeType(type)
          .setIsInterface(target.isInterface());
      builder.setCode(
          new SynthesizedCode(
              forwardSourceCodeBuilder::build,
              registry -> {
                if (accessFlags.isStatic()) {
                  registry.registerInvokeStatic(method);
                } else {
                  registry.registerInvokeSuper(method);
                }
              }));
      builder.accessFlags.setBridge();
    }
    builder.accessFlags.setSynthetic();
    // Note that we are not marking this instance obsolete, since it is not: the newly synthesized
    // forwarding method has a separate code that literally forwards to the current method.
    return builder.build();
  }

  public DexEncodedMethod toStaticMethodWithoutThis() {
    checkIfObsolete();
    assert !accessFlags.isStatic();
    Builder builder =
        builder(this)
            .promoteToStatic()
            .withoutThisParameter()
            .adjustOptimizationInfoAfterRemovingThisParameter();
    DexEncodedMethod method = builder.build();
    method.copyMetadata(this);
    setObsolete();
    return method;
  }

  /** Rewrites the code in this method to have JumboString bytecode if required by mapping. */
  public DexCode rewriteCodeWithJumboStrings(
      ObjectToOffsetMapping mapping, DexItemFactory factory, boolean force) {
    checkIfObsolete();
    assert code == null || code.isDexCode();
    if (code == null) {
      return null;
    }
    DexCode code = this.code.asDexCode();
    DexString firstJumboString = null;
    if (force) {
      firstJumboString = mapping.getFirstString();
    } else {
      assert code.highestSortingString != null
          || Arrays.stream(code.instructions).noneMatch(Instruction::isConstString);
      assert Arrays.stream(code.instructions).noneMatch(Instruction::isDexItemBasedConstString);
      if (code.highestSortingString != null
          && mapping.getOffsetFor(code.highestSortingString) > Constants.MAX_NON_JUMBO_INDEX) {
        firstJumboString = mapping.getFirstJumboString();
      }
    }
    if (firstJumboString != null) {
      JumboStringRewriter rewriter = new JumboStringRewriter(this, firstJumboString, factory);
      return rewriter.rewrite();
    }
    return code;
  }

  public String codeToString() {
    checkIfObsolete();
    return code == null ? "<no code>" : code.toString(this, null);
  }

  @Override
  public DexMethod getKey() {
    // Here, we can't check if the current instance of DexEncodedMethod is obsolete
    // because itself can be used as a key while making mappings to avoid using obsolete instances.
    return method;
  }

  @Override
  public DexReference toReference() {
    checkIfObsolete();
    return method;
  }

  @Override
  public boolean isDexEncodedMethod() {
    checkIfObsolete();
    return true;
  }

  @Override
  public DexEncodedMethod asDexEncodedMethod() {
    checkIfObsolete();
    return this;
  }

  public boolean hasAnnotation() {
    checkIfObsolete();
    return !annotations.isEmpty() || !parameterAnnotationsList.isEmpty();
  }

  public void registerCodeReferences(UseRegistry registry) {
    checkIfObsolete();
    if (code != null) {
      if (Log.ENABLED) {
        Log.verbose(getClass(), "Registering definitions reachable from `%s`.", method);
      }
      code.registerCodeReferences(this, registry);
    }
  }

  public static int slowCompare(DexEncodedMethod m1, DexEncodedMethod m2) {
    return m1.method.slowCompareTo(m2.method);
  }

  public MethodOptimizationInfo getOptimizationInfo() {
    checkIfObsolete();
    return optimizationInfo;
  }

  public synchronized UpdatableMethodOptimizationInfo getMutableOptimizationInfo() {
    checkIfObsolete();
    if (optimizationInfo == DefaultMethodOptimizationInfo.DEFAULT_INSTANCE) {
      optimizationInfo = optimizationInfo.mutableCopy();
    }
    return (UpdatableMethodOptimizationInfo) optimizationInfo;
  }

  public void setOptimizationInfo(UpdatableMethodOptimizationInfo info) {
    checkIfObsolete();
    optimizationInfo = info;
  }

  public synchronized CallSiteOptimizationInfo getCallSiteOptimizationInfo() {
    checkIfObsolete();
    return callSiteOptimizationInfo;
  }

  public synchronized void joinCallSiteOptimizationInfo(
      CallSiteOptimizationInfo other, AppView<?> appView) {
    checkIfObsolete();
    callSiteOptimizationInfo = callSiteOptimizationInfo.join(other, appView, this);
  }

  public void copyMetadata(DexEncodedMethod from) {
    checkIfObsolete();
    // Record that the current method uses identifier name string if the inlinee did so.
    if (from.getOptimizationInfo().useIdentifierNameString()) {
      getMutableOptimizationInfo().markUseIdentifierNameString();
    }
    if (from.classFileVersion > classFileVersion) {
      upgradeClassFileVersion(from.getClassFileVersion());
    }
  }

  private static Builder builder(DexEncodedMethod from) {
    return new Builder(from);
  }

  public static class Builder {

    private DexMethod method;
    private final MethodAccessFlags accessFlags;
    private final DexAnnotationSet annotations;
    private ParameterAnnotationsList parameterAnnotations;
    private Code code;
    private CompilationState compilationState;
    private MethodOptimizationInfo optimizationInfo;
    private final int classFileVersion;

    private Builder(DexEncodedMethod from) {
      // Copy all the mutable state of a DexEncodedMethod here.
      method = from.method;
      accessFlags = from.accessFlags.copy();
      annotations = from.annotations;
      code = from.code;
      compilationState = from.compilationState;
      optimizationInfo = from.optimizationInfo.mutableCopy();
      classFileVersion = from.classFileVersion;

      if (from.parameterAnnotationsList.isEmpty()
          || from.parameterAnnotationsList.size() == method.proto.parameters.size()) {
        parameterAnnotations = from.parameterAnnotationsList;
      } else {
        // If the there are missing parameter annotations populate these when creating the builder.
        parameterAnnotations =
            from.parameterAnnotationsList.withParameterCount(method.proto.parameters.size());
      }
    }

    public void setMethod(DexMethod method) {
      this.method = method;
    }

    public Builder setParameterAnnotations(ParameterAnnotationsList parameterAnnotations) {
      this.parameterAnnotations = parameterAnnotations;
      return this;
    }

    public Builder removeParameterAnnotations(IntPredicate predicate) {
      if (parameterAnnotations.isEmpty()) {
        // Nothing to do.
        return this;
      }

      List<DexAnnotationSet> newParameterAnnotations = new ArrayList<>();
      int newNumberOfMissingParameterAnnotations = 0;

      for (int oldIndex = 0; oldIndex < parameterAnnotations.size(); oldIndex++) {
        if (!predicate.test(oldIndex)) {
          if (parameterAnnotations.isMissing(oldIndex)) {
            newNumberOfMissingParameterAnnotations++;
          } else {
            newParameterAnnotations.add(parameterAnnotations.get(oldIndex));
          }
        }
      }

      if (newParameterAnnotations.isEmpty()) {
        return setParameterAnnotations(ParameterAnnotationsList.empty());
      }

      return setParameterAnnotations(
          new ParameterAnnotationsList(
              newParameterAnnotations.toArray(DexAnnotationSet.EMPTY_ARRAY),
              newNumberOfMissingParameterAnnotations));
    }

    public Builder promoteToStatic() {
      this.accessFlags.promoteToStatic();
      return this;
    }

    public Builder withoutThisParameter() {
      assert code != null;
      if (code.isDexCode()) {
        code = code.asDexCode().withoutThisParameter();
      } else {
        throw new Unreachable("Code " + code.getClass().getSimpleName() + " is not supported.");
      }
      return this;
    }

    public Builder adjustOptimizationInfoAfterRemovingThisParameter() {
      if (optimizationInfo.isUpdatableMethodOptimizationInfo()) {
        optimizationInfo.asUpdatableMethodOptimizationInfo()
            .adjustOptimizationInfoAfterRemovingThisParameter();
      }
      return this;
    }

    public void setCode(Code code) {
      this.code = code;
    }

    public DexEncodedMethod build() {
      assert method != null;
      assert accessFlags != null;
      assert annotations != null;
      assert parameterAnnotations != null;
      assert parameterAnnotations.isEmpty()
          || parameterAnnotations.size() == method.proto.parameters.size();
      DexEncodedMethod result =
          new DexEncodedMethod(
              method, accessFlags, annotations, parameterAnnotations, code, classFileVersion);
      result.compilationState = compilationState;
      result.optimizationInfo = optimizationInfo;
      return result;
    }
  }
}
