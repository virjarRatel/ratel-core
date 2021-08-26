// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import static com.android.tools.r8.graph.DexProgramClass.asProgramClassOrNull;
import static com.android.tools.r8.graph.GraphLense.rewriteReferenceKeys;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DirectMappedDexApplication;
import com.android.tools.r8.graph.FieldAccessInfo;
import com.android.tools.r8.graph.FieldAccessInfoCollection;
import com.android.tools.r8.graph.FieldAccessInfoCollectionImpl;
import com.android.tools.r8.graph.FieldAccessInfoImpl;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.PresortedComparable;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.graph.ResolutionResult.SingleResolutionResult;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.utils.CollectionUtils;
import com.android.tools.r8.utils.SetUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Encapsulates liveness and reachability information for an application. */
public class AppInfoWithLiveness extends AppInfoWithSubtyping {

  /**
   * Set of types that are mentioned in the program. We at least need an empty abstract classitem
   * for these.
   */
  private final Set<DexType> liveTypes;
  /** Set of annotation types that are instantiated. */
  private final Set<DexType> instantiatedAnnotationTypes;
  /**
   * Set of service types (from META-INF/services/) that may have been instantiated reflectively via
   * ServiceLoader.load() or ServiceLoader.loadInstalled().
   */
  public final Set<DexType> instantiatedAppServices;
  /** Set of types that are actually instantiated. These cannot be abstract. */
  final Set<DexType> instantiatedTypes;
  /** Cache for {@link #isInstantiatedDirectlyOrIndirectly(DexProgramClass)}. */
  private final IdentityHashMap<DexType, Boolean> indirectlyInstantiatedTypes =
      new IdentityHashMap<>();
  /**
   * Set of methods that are the immediate target of an invoke. They might not actually be live but
   * are required so that invokes can find the method. If such a method is not live (i.e. not
   * contained in {@link #liveMethods}, it may be marked as abstract and its implementation may be
   * removed.
   */
  final SortedSet<DexMethod> targetedMethods;

  final Set<DexMethod> targetedMethodsThatMustRemainNonAbstract;
  /**
   * Set of program methods that are used as the bootstrap method for an invoke-dynamic instruction.
   */
  public final SortedSet<DexMethod> bootstrapMethods;
  /** Set of methods that are the immediate target of an invoke-dynamic. */
  public final SortedSet<DexMethod> methodsTargetedByInvokeDynamic;
  /** Set of virtual methods that are the immediate target of an invoke-direct. */
  final SortedSet<DexMethod> virtualMethodsTargetedByInvokeDirect;
  /**
   * Set of methods that belong to live classes and can be reached by invokes. These need to be
   * kept.
   */
  public final SortedSet<DexMethod> liveMethods;
  /**
   * Information about all fields that are accessed by the program. The information includes whether
   * a given field is read/written by the program, and it also includes all indirect accesses to
   * each field. The latter is used, for example, during member rebinding.
   */
  private final FieldAccessInfoCollectionImpl fieldAccessInfoCollection;
  /** Set of all methods referenced in virtual invokes, along with calling context. */
  public final SortedMap<DexMethod, Set<DexEncodedMethod>> virtualInvokes;
  /** Set of all methods referenced in interface invokes, along with calling context. */
  public final SortedMap<DexMethod, Set<DexEncodedMethod>> interfaceInvokes;
  /** Set of all methods referenced in super invokes, along with calling context. */
  public final SortedMap<DexMethod, Set<DexEncodedMethod>> superInvokes;
  /** Set of all methods referenced in direct invokes, along with calling context. */
  public final SortedMap<DexMethod, Set<DexEncodedMethod>> directInvokes;
  /** Set of all methods referenced in static invokes, along with calling context. */
  public final SortedMap<DexMethod, Set<DexEncodedMethod>> staticInvokes;
  /**
   * Set of live call sites in the code. Note that if desugaring has taken place call site objects
   * will have been removed from the code.
   */
  public final Set<DexCallSite> callSites;
  /**
   * Set of method signatures used in invoke-super instructions that either cannot be resolved or
   * resolve to a private method (leading to an IllegalAccessError).
   */
  public final SortedSet<DexMethod> brokenSuperInvokes;
  /** Set of all items that have to be kept independent of whether they are used. */
  final Set<DexReference> pinnedItems;
  /** All items with assumemayhavesideeffects rule. */
  public final Map<DexReference, ProguardMemberRule> mayHaveSideEffects;
  /** All items with assumenosideeffects rule. */
  public final Map<DexReference, ProguardMemberRule> noSideEffects;
  /** All items with assumevalues rule. */
  public final Map<DexReference, ProguardMemberRule> assumedValues;
  /** All methods that should be inlined if possible due to a configuration directive. */
  public final Set<DexMethod> alwaysInline;
  /** All methods that *must* be inlined due to a configuration directive (testing only). */
  public final Set<DexMethod> forceInline;
  /** All methods that *must* never be inlined due to a configuration directive (testing only). */
  public final Set<DexMethod> neverInline;
  /** Items for which to print inlining decisions for (testing only). */
  public final Set<DexMethod> whyAreYouNotInlining;
  /** All methods that may not have any parameters with a constant value removed. */
  public final Set<DexMethod> keepConstantArguments;
  /** All methods that may not have any unused arguments removed. */
  public final Set<DexMethod> keepUnusedArguments;
  /** All types that *must* never be inlined due to a configuration directive (testing only). */
  public final Set<DexType> neverClassInline;
  /** All types that *must* never be merged due to a configuration directive (testing only). */
  public final Set<DexType> neverMerge;
  /** Set of const-class references. */
  public final Set<DexType> constClassReferences;
  /**
   * All methods and fields whose value *must* never be propagated due to a configuration directive.
   * (testing only).
   */
  private final Set<DexReference> neverPropagateValue;
  /**
   * All items with -identifiernamestring rule. Bound boolean value indicates the rule is explicitly
   * specified by users (<code>true</code>) or not, i.e., implicitly added by R8 (<code>false</code>
   * ).
   */
  public final Object2BooleanMap<DexReference> identifierNameStrings;
  /** A set of types that have been removed by the {@link TreePruner}. */
  final Set<DexType> prunedTypes;
  /** A map from switchmap class types to their corresponding switchmaps. */
  final Map<DexField, Int2ReferenceMap<DexField>> switchMaps;
  /** A map from enum types to their value types and ordinals. */
  final Map<DexType, Map<DexField, EnumValueInfo>> enumValueInfoMaps;

  public static final class EnumValueInfo {
    /** The anonymous subtype of this specific value or the enum type. */
    public final DexType type;
    public final int ordinal;

    public EnumValueInfo(DexType type, int ordinal) {
      this.type = type;
      this.ordinal = ordinal;
    }
  }

  final Set<DexType> instantiatedLambdas;

  // TODO(zerny): Clean up the constructors so we have just one.
  private AppInfoWithLiveness(
      DexApplication application,
      Set<DexType> liveTypes,
      Set<DexType> instantiatedAnnotationTypes,
      Set<DexType> instantiatedAppServices,
      Set<DexType> instantiatedTypes,
      SortedSet<DexMethod> targetedMethods,
      Set<DexMethod> targetedMethodsThatMustRemainNonAbstract,
      SortedSet<DexMethod> bootstrapMethods,
      SortedSet<DexMethod> methodsTargetedByInvokeDynamic,
      SortedSet<DexMethod> virtualMethodsTargetedByInvokeDirect,
      SortedSet<DexMethod> liveMethods,
      FieldAccessInfoCollectionImpl fieldAccessInfoCollection,
      SortedMap<DexMethod, Set<DexEncodedMethod>> virtualInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> interfaceInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> superInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> directInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> staticInvokes,
      Set<DexCallSite> callSites,
      SortedSet<DexMethod> brokenSuperInvokes,
      Set<DexReference> pinnedItems,
      Map<DexReference, ProguardMemberRule> mayHaveSideEffects,
      Map<DexReference, ProguardMemberRule> noSideEffects,
      Map<DexReference, ProguardMemberRule> assumedValues,
      Set<DexMethod> alwaysInline,
      Set<DexMethod> forceInline,
      Set<DexMethod> neverInline,
      Set<DexMethod> whyAreYouNotInlining,
      Set<DexMethod> keepConstantArguments,
      Set<DexMethod> keepUnusedArguments,
      Set<DexType> neverClassInline,
      Set<DexType> neverMerge,
      Set<DexReference> neverPropagateValue,
      Object2BooleanMap<DexReference> identifierNameStrings,
      Set<DexType> prunedTypes,
      Map<DexField, Int2ReferenceMap<DexField>> switchMaps,
      Map<DexType, Map<DexField, EnumValueInfo>> enumValueInfoMaps,
      Set<DexType> instantiatedLambdas,
      Set<DexType> constClassReferences) {
    super(application);
    this.liveTypes = liveTypes;
    this.instantiatedAnnotationTypes = instantiatedAnnotationTypes;
    this.instantiatedAppServices = instantiatedAppServices;
    this.instantiatedTypes = instantiatedTypes;
    this.targetedMethods = targetedMethods;
    this.targetedMethodsThatMustRemainNonAbstract = targetedMethodsThatMustRemainNonAbstract;
    this.bootstrapMethods = bootstrapMethods;
    this.methodsTargetedByInvokeDynamic = methodsTargetedByInvokeDynamic;
    this.virtualMethodsTargetedByInvokeDirect = virtualMethodsTargetedByInvokeDirect;
    this.liveMethods = liveMethods;
    this.fieldAccessInfoCollection = fieldAccessInfoCollection;
    this.pinnedItems = pinnedItems;
    this.mayHaveSideEffects = mayHaveSideEffects;
    this.noSideEffects = noSideEffects;
    this.assumedValues = assumedValues;
    this.virtualInvokes = virtualInvokes;
    this.interfaceInvokes = interfaceInvokes;
    this.superInvokes = superInvokes;
    this.directInvokes = directInvokes;
    this.staticInvokes = staticInvokes;
    this.callSites = callSites;
    this.brokenSuperInvokes = brokenSuperInvokes;
    this.alwaysInline = alwaysInline;
    this.forceInline = forceInline;
    this.neverInline = neverInline;
    this.whyAreYouNotInlining = whyAreYouNotInlining;
    this.keepConstantArguments = keepConstantArguments;
    this.keepUnusedArguments = keepUnusedArguments;
    this.neverClassInline = neverClassInline;
    this.neverMerge = neverMerge;
    this.neverPropagateValue = neverPropagateValue;
    this.identifierNameStrings = identifierNameStrings;
    this.prunedTypes = prunedTypes;
    this.switchMaps = switchMaps;
    this.enumValueInfoMaps = enumValueInfoMaps;
    this.instantiatedLambdas = instantiatedLambdas;
    this.constClassReferences = constClassReferences;
  }

  public AppInfoWithLiveness(
      AppInfoWithSubtyping appInfoWithSubtyping,
      Set<DexType> liveTypes,
      Set<DexType> instantiatedAnnotationTypes,
      Set<DexType> instantiatedAppServices,
      Set<DexType> instantiatedTypes,
      SortedSet<DexMethod> targetedMethods,
      Set<DexMethod> targetedMethodsThatMustRemainNonAbstract,
      SortedSet<DexMethod> bootstrapMethods,
      SortedSet<DexMethod> methodsTargetedByInvokeDynamic,
      SortedSet<DexMethod> virtualMethodsTargetedByInvokeDirect,
      SortedSet<DexMethod> liveMethods,
      FieldAccessInfoCollectionImpl fieldAccessInfoCollection,
      SortedMap<DexMethod, Set<DexEncodedMethod>> virtualInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> interfaceInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> superInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> directInvokes,
      SortedMap<DexMethod, Set<DexEncodedMethod>> staticInvokes,
      Set<DexCallSite> callSites,
      SortedSet<DexMethod> brokenSuperInvokes,
      Set<DexReference> pinnedItems,
      Map<DexReference, ProguardMemberRule> mayHaveSideEffects,
      Map<DexReference, ProguardMemberRule> noSideEffects,
      Map<DexReference, ProguardMemberRule> assumedValues,
      Set<DexMethod> alwaysInline,
      Set<DexMethod> forceInline,
      Set<DexMethod> neverInline,
      Set<DexMethod> whyAreYouNotInlining,
      Set<DexMethod> keepConstantArguments,
      Set<DexMethod> keepUnusedArguments,
      Set<DexType> neverClassInline,
      Set<DexType> neverMerge,
      Set<DexReference> neverPropagateValue,
      Object2BooleanMap<DexReference> identifierNameStrings,
      Set<DexType> prunedTypes,
      Map<DexField, Int2ReferenceMap<DexField>> switchMaps,
      Map<DexType, Map<DexField, EnumValueInfo>> enumValueInfoMaps,
      Set<DexType> instantiatedLambdas,
      Set<DexType> constClassReferences) {
    super(appInfoWithSubtyping);
    this.liveTypes = liveTypes;
    this.instantiatedAnnotationTypes = instantiatedAnnotationTypes;
    this.instantiatedAppServices = instantiatedAppServices;
    this.instantiatedTypes = instantiatedTypes;
    this.targetedMethods = targetedMethods;
    this.targetedMethodsThatMustRemainNonAbstract = targetedMethodsThatMustRemainNonAbstract;
    this.bootstrapMethods = bootstrapMethods;
    this.methodsTargetedByInvokeDynamic = methodsTargetedByInvokeDynamic;
    this.virtualMethodsTargetedByInvokeDirect = virtualMethodsTargetedByInvokeDirect;
    this.liveMethods = liveMethods;
    this.fieldAccessInfoCollection = fieldAccessInfoCollection;
    this.pinnedItems = pinnedItems;
    this.mayHaveSideEffects = mayHaveSideEffects;
    this.noSideEffects = noSideEffects;
    this.assumedValues = assumedValues;
    this.virtualInvokes = virtualInvokes;
    this.interfaceInvokes = interfaceInvokes;
    this.superInvokes = superInvokes;
    this.directInvokes = directInvokes;
    this.staticInvokes = staticInvokes;
    this.callSites = callSites;
    this.brokenSuperInvokes = brokenSuperInvokes;
    this.alwaysInline = alwaysInline;
    this.forceInline = forceInline;
    this.neverInline = neverInline;
    this.whyAreYouNotInlining = whyAreYouNotInlining;
    this.keepConstantArguments = keepConstantArguments;
    this.keepUnusedArguments = keepUnusedArguments;
    this.neverClassInline = neverClassInline;
    this.neverMerge = neverMerge;
    this.neverPropagateValue = neverPropagateValue;
    this.identifierNameStrings = identifierNameStrings;
    this.prunedTypes = prunedTypes;
    this.switchMaps = switchMaps;
    this.enumValueInfoMaps = enumValueInfoMaps;
    this.instantiatedLambdas = instantiatedLambdas;
    this.constClassReferences = constClassReferences;
  }

  private AppInfoWithLiveness(AppInfoWithLiveness previous) {
    this(
        previous,
        previous.liveTypes,
        previous.instantiatedAnnotationTypes,
        previous.instantiatedAppServices,
        previous.instantiatedTypes,
        previous.targetedMethods,
        previous.targetedMethodsThatMustRemainNonAbstract,
        previous.bootstrapMethods,
        previous.methodsTargetedByInvokeDynamic,
        previous.virtualMethodsTargetedByInvokeDirect,
        previous.liveMethods,
        previous.fieldAccessInfoCollection,
        previous.virtualInvokes,
        previous.interfaceInvokes,
        previous.superInvokes,
        previous.directInvokes,
        previous.staticInvokes,
        previous.callSites,
        previous.brokenSuperInvokes,
        previous.pinnedItems,
        previous.mayHaveSideEffects,
        previous.noSideEffects,
        previous.assumedValues,
        previous.alwaysInline,
        previous.forceInline,
        previous.neverInline,
        previous.whyAreYouNotInlining,
        previous.keepConstantArguments,
        previous.keepUnusedArguments,
        previous.neverClassInline,
        previous.neverMerge,
        previous.neverPropagateValue,
        previous.identifierNameStrings,
        previous.prunedTypes,
        previous.switchMaps,
        previous.enumValueInfoMaps,
        previous.instantiatedLambdas,
        previous.constClassReferences);
    copyMetadataFromPrevious(previous);
  }

  private AppInfoWithLiveness(
      AppInfoWithLiveness previous,
      DexApplication application,
      Collection<DexType> removedClasses,
      Collection<DexReference> additionalPinnedItems) {
    this(
        application,
        previous.liveTypes,
        previous.instantiatedAnnotationTypes,
        previous.instantiatedAppServices,
        previous.instantiatedTypes,
        previous.targetedMethods,
        previous.targetedMethodsThatMustRemainNonAbstract,
        previous.bootstrapMethods,
        previous.methodsTargetedByInvokeDynamic,
        previous.virtualMethodsTargetedByInvokeDirect,
        previous.liveMethods,
        previous.fieldAccessInfoCollection,
        previous.virtualInvokes,
        previous.interfaceInvokes,
        previous.superInvokes,
        previous.directInvokes,
        previous.staticInvokes,
        previous.callSites,
        previous.brokenSuperInvokes,
        additionalPinnedItems == null
            ? previous.pinnedItems
            : CollectionUtils.mergeSets(previous.pinnedItems, additionalPinnedItems),
        previous.mayHaveSideEffects,
        previous.noSideEffects,
        previous.assumedValues,
        previous.alwaysInline,
        previous.forceInline,
        previous.neverInline,
        previous.whyAreYouNotInlining,
        previous.keepConstantArguments,
        previous.keepUnusedArguments,
        previous.neverClassInline,
        previous.neverMerge,
        previous.neverPropagateValue,
        previous.identifierNameStrings,
        removedClasses == null
            ? previous.prunedTypes
            : CollectionUtils.mergeSets(previous.prunedTypes, removedClasses),
        previous.switchMaps,
        previous.enumValueInfoMaps,
        previous.instantiatedLambdas,
        previous.constClassReferences);
    copyMetadataFromPrevious(previous);
    assert removedClasses == null || assertNoItemRemoved(previous.pinnedItems, removedClasses);
  }

  private AppInfoWithLiveness(
      AppInfoWithLiveness previous, DirectMappedDexApplication application, GraphLense lense) {
    super(application);
    this.liveTypes = rewriteItems(previous.liveTypes, lense::lookupType);
    this.instantiatedAnnotationTypes =
        rewriteItems(previous.instantiatedAnnotationTypes, lense::lookupType);
    this.instantiatedAppServices =
        rewriteItems(previous.instantiatedAppServices, lense::lookupType);
    this.instantiatedTypes = rewriteItems(previous.instantiatedTypes, lense::lookupType);
    this.instantiatedLambdas = rewriteItems(previous.instantiatedLambdas, lense::lookupType);
    this.targetedMethods = lense.rewriteMethodsConservatively(previous.targetedMethods);
    this.targetedMethodsThatMustRemainNonAbstract =
        lense.rewriteMethodsConservatively(previous.targetedMethodsThatMustRemainNonAbstract);
    this.bootstrapMethods = lense.rewriteMethodsConservatively(previous.bootstrapMethods);
    this.methodsTargetedByInvokeDynamic =
        lense.rewriteMethodsConservatively(previous.methodsTargetedByInvokeDynamic);
    this.virtualMethodsTargetedByInvokeDirect =
        lense.rewriteMethodsConservatively(previous.virtualMethodsTargetedByInvokeDirect);
    this.liveMethods = lense.rewriteMethodsConservatively(previous.liveMethods);
    this.fieldAccessInfoCollection =
        previous.fieldAccessInfoCollection.rewrittenWithLens(application, lense);
    this.pinnedItems = lense.rewriteReferencesConservatively(previous.pinnedItems);
    this.virtualInvokes =
        rewriteKeysConservativelyWhileMergingValues(
            previous.virtualInvokes, lense::lookupMethodInAllContexts);
    this.interfaceInvokes =
        rewriteKeysConservativelyWhileMergingValues(
            previous.interfaceInvokes, lense::lookupMethodInAllContexts);
    this.superInvokes =
        rewriteKeysConservativelyWhileMergingValues(
            previous.superInvokes, lense::lookupMethodInAllContexts);
    this.directInvokes =
        rewriteKeysConservativelyWhileMergingValues(
            previous.directInvokes, lense::lookupMethodInAllContexts);
    this.staticInvokes =
        rewriteKeysConservativelyWhileMergingValues(
            previous.staticInvokes, lense::lookupMethodInAllContexts);
    // TODO(sgjesse): Rewrite call sites as well? Right now they are only used by minification
    // after second tree shaking.
    this.callSites = previous.callSites;
    this.brokenSuperInvokes = lense.rewriteMethodsConservatively(previous.brokenSuperInvokes);
    // Don't rewrite pruned types - the removed types are identified by their original name.
    this.prunedTypes = previous.prunedTypes;
    this.mayHaveSideEffects =
        rewriteReferenceKeys(previous.mayHaveSideEffects, lense::lookupReference);
    this.noSideEffects = rewriteReferenceKeys(previous.noSideEffects, lense::lookupReference);
    this.assumedValues = rewriteReferenceKeys(previous.assumedValues, lense::lookupReference);
    assert lense.assertDefinitionsNotModified(
        previous.alwaysInline.stream()
            .map(this::definitionFor)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    this.alwaysInline = lense.rewriteMethodsWithRenamedSignature(previous.alwaysInline);
    this.forceInline = lense.rewriteMethodsWithRenamedSignature(previous.forceInline);
    this.neverInline = lense.rewriteMethodsWithRenamedSignature(previous.neverInline);
    this.whyAreYouNotInlining =
        lense.rewriteMethodsWithRenamedSignature(previous.whyAreYouNotInlining);
    this.keepConstantArguments =
        lense.rewriteMethodsWithRenamedSignature(previous.keepConstantArguments);
    this.keepUnusedArguments =
        lense.rewriteMethodsWithRenamedSignature(previous.keepUnusedArguments);
    assert lense.assertDefinitionsNotModified(
        previous.neverMerge.stream()
            .map(this::definitionFor)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    this.neverClassInline = rewriteItems(previous.neverClassInline, lense::lookupType);
    this.neverMerge = rewriteItems(previous.neverMerge, lense::lookupType);
    this.neverPropagateValue = lense.rewriteReferencesConservatively(previous.neverPropagateValue);
    this.identifierNameStrings =
        lense.rewriteReferencesConservatively(previous.identifierNameStrings);
    // Switchmap classes should never be affected by renaming.
    assert lense.assertDefinitionsNotModified(
        previous.switchMaps.keySet().stream()
            .map(this::definitionFor)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    this.switchMaps = rewriteReferenceKeys(previous.switchMaps, lense::lookupField);
    this.enumValueInfoMaps = rewriteReferenceKeys(previous.enumValueInfoMaps, lense::lookupType);
    this.constClassReferences = previous.constClassReferences;
  }

  public AppInfoWithLiveness(
      AppInfoWithLiveness previous,
      Map<DexField, Int2ReferenceMap<DexField>> switchMaps,
      Map<DexType, Map<DexField, EnumValueInfo>> enumValueInfoMaps) {
    super(previous);
    this.liveTypes = previous.liveTypes;
    this.instantiatedAnnotationTypes = previous.instantiatedAnnotationTypes;
    this.instantiatedAppServices = previous.instantiatedAppServices;
    this.instantiatedTypes = previous.instantiatedTypes;
    this.instantiatedLambdas = previous.instantiatedLambdas;
    this.targetedMethods = previous.targetedMethods;
    this.targetedMethodsThatMustRemainNonAbstract =
        previous.targetedMethodsThatMustRemainNonAbstract;
    this.bootstrapMethods = previous.bootstrapMethods;
    this.methodsTargetedByInvokeDynamic = previous.methodsTargetedByInvokeDynamic;
    this.virtualMethodsTargetedByInvokeDirect = previous.virtualMethodsTargetedByInvokeDirect;
    this.liveMethods = previous.liveMethods;
    this.fieldAccessInfoCollection = previous.fieldAccessInfoCollection;
    this.pinnedItems = previous.pinnedItems;
    this.mayHaveSideEffects = previous.mayHaveSideEffects;
    this.noSideEffects = previous.noSideEffects;
    this.assumedValues = previous.assumedValues;
    this.virtualInvokes = previous.virtualInvokes;
    this.interfaceInvokes = previous.interfaceInvokes;
    this.superInvokes = previous.superInvokes;
    this.directInvokes = previous.directInvokes;
    this.staticInvokes = previous.staticInvokes;
    this.callSites = previous.callSites;
    this.brokenSuperInvokes = previous.brokenSuperInvokes;
    this.alwaysInline = previous.alwaysInline;
    this.forceInline = previous.forceInline;
    this.neverInline = previous.neverInline;
    this.whyAreYouNotInlining = previous.whyAreYouNotInlining;
    this.keepConstantArguments = previous.keepConstantArguments;
    this.keepUnusedArguments = previous.keepUnusedArguments;
    this.neverClassInline = previous.neverClassInline;
    this.neverMerge = previous.neverMerge;
    this.neverPropagateValue = previous.neverPropagateValue;
    this.identifierNameStrings = previous.identifierNameStrings;
    this.prunedTypes = previous.prunedTypes;
    this.switchMaps = switchMaps;
    this.enumValueInfoMaps = enumValueInfoMaps;
    this.constClassReferences = previous.constClassReferences;
    previous.markObsolete();
  }

  public boolean isLiveProgramClass(DexProgramClass clazz) {
    return liveTypes.contains(clazz.type);
  }

  public boolean isLiveProgramType(DexType type) {
    DexClass clazz = definitionFor(type);
    return clazz != null && clazz.isProgramClass() && isLiveProgramClass(clazz.asProgramClass());
  }

  public boolean isNonProgramTypeOrLiveProgramType(DexType type) {
    if (liveTypes.contains(type)) {
      return true;
    }
    if (prunedTypes.contains(type)) {
      return false;
    }
    DexClass clazz = definitionFor(type);
    return clazz == null || !clazz.isProgramClass();
  }

  public Collection<DexClass> computeReachableInterfaces(Set<DexCallSite> desugaredCallSites) {
    Set<DexClass> interfaces = Sets.newIdentityHashSet();
    Set<DexType> seen = Sets.newIdentityHashSet();
    Deque<DexType> worklist = new ArrayDeque<>();
    for (DexProgramClass clazz : classes()) {
      worklist.add(clazz.type);
    }
    // TODO(b/129458850): Remove this once desugared classes are made part of the program classes.
    for (DexCallSite callSite : desugaredCallSites) {
      for (DexEncodedMethod method : lookupLambdaImplementedMethods(callSite)) {
        worklist.add(method.method.holder);
      }
    }
    for (DexCallSite callSite : callSites) {
      for (DexEncodedMethod method : lookupLambdaImplementedMethods(callSite)) {
        worklist.add(method.method.holder);
      }
    }
    while (!worklist.isEmpty()) {
      DexType type = worklist.pop();
      if (!seen.add(type)) {
        continue;
      }
      DexClass definition = definitionFor(type);
      if (definition == null) {
        continue;
      }
      if (definition.isInterface()) {
        interfaces.add(definition);
      }
      if (definition.superType != null) {
        worklist.add(definition.superType);
      }
      Collections.addAll(worklist, definition.interfaces.values);
    }
    return interfaces;
  }

  /**
   * Const-classes is a conservative set of types that may be lock-candidates and cannot be merged.
   * When using synchronized blocks, we cannot ensure that const-class locks will not flow in. This
   * can potentially cause incorrect behavior when merging classes. A conservative choice is to not
   * merge any const-class classes. More info at b/142438687.
   */
  public boolean isLockCandidate(DexType type) {
    return constClassReferences.contains(type);
  }

  public AppInfoWithLiveness withStaticFieldWrites(
      Map<DexEncodedField, Set<DexEncodedMethod>> writesWithContexts) {
    assert checkIfObsolete();
    if (writesWithContexts.isEmpty()) {
      return this;
    }
    AppInfoWithLiveness result = new AppInfoWithLiveness(this);
    writesWithContexts.forEach(
        (encodedField, contexts) -> {
          DexField field = encodedField.field;
          FieldAccessInfoImpl fieldAccessInfo = result.fieldAccessInfoCollection.get(field);
          if (fieldAccessInfo == null) {
            fieldAccessInfo = new FieldAccessInfoImpl(field);
            result.fieldAccessInfoCollection.extend(field, fieldAccessInfo);
          }
          for (DexEncodedMethod context : contexts) {
            fieldAccessInfo.recordWrite(field, context);
          }
        });
    return result;
  }

  public AppInfoWithLiveness withoutStaticFieldsWrites(Set<DexField> noLongerWrittenFields) {
    assert checkIfObsolete();
    if (noLongerWrittenFields.isEmpty()) {
      return this;
    }
    AppInfoWithLiveness result = new AppInfoWithLiveness(this);
    result.fieldAccessInfoCollection.forEach(
        info -> {
          if (noLongerWrittenFields.contains(info.getField())) {
            // Note that this implicitly mutates the current AppInfoWithLiveness, since the `info`
            // instance is shared between the old and the new AppInfoWithLiveness. This should not
            // lead to any problems, though, since the new AppInfo replaces the old AppInfo (we
            // never use an obsolete AppInfo).
            info.clearWrites();
          }
        });
    return result;
  }

  private <T extends PresortedComparable<T>> SortedSet<T> filter(
      Set<T> items, Predicate<T> predicate) {
    return ImmutableSortedSet.copyOf(
        PresortedComparable::slowCompareTo,
        items.stream().filter(predicate).collect(Collectors.toList()));
  }

  public Map<DexField, EnumValueInfo> getEnumValueInfoMapFor(DexType enumClass) {
    assert checkIfObsolete();
    return enumValueInfoMaps.get(enumClass);
  }

  public Int2ReferenceMap<DexField> getSwitchMapFor(DexField field) {
    assert checkIfObsolete();
    return switchMaps.get(field);
  }

  /** This method provides immutable access to `fieldAccessInfoCollection`. */
  public FieldAccessInfoCollection<? extends FieldAccessInfo> getFieldAccessInfoCollection() {
    return fieldAccessInfoCollection;
  }

  private boolean assertNoItemRemoved(Collection<DexReference> items, Collection<DexType> types) {
    Set<DexType> typeSet = ImmutableSet.copyOf(types);
    for (DexReference item : items) {
      DexType typeToCheck;
      if (item.isDexType()) {
        typeToCheck = item.asDexType();
      } else if (item.isDexMethod()) {
        typeToCheck = item.asDexMethod().holder;
      } else {
        assert item.isDexField();
        typeToCheck = item.asDexField().holder;
      }
      assert !typeSet.contains(typeToCheck);
    }
    return true;
  }

  private boolean isInstantiatedDirectly(DexProgramClass clazz) {
    assert checkIfObsolete();
    DexType type = clazz.type;
    return type.isD8R8SynthesizedClassType()
        || instantiatedTypes.contains(type)
        || instantiatedAnnotationTypes.contains(type);
  }

  public boolean isInstantiatedIndirectly(DexProgramClass clazz) {
    assert checkIfObsolete();
    if (hasAnyInstantiatedLambdas(clazz)) {
      return true;
    }
    DexType type = clazz.type;
    synchronized (indirectlyInstantiatedTypes) {
      if (indirectlyInstantiatedTypes.containsKey(type)) {
        return indirectlyInstantiatedTypes.get(type).booleanValue();
      }
      for (DexType directSubtype : allImmediateSubtypes(type)) {
        DexProgramClass directSubClass = asProgramClassOrNull(definitionFor(directSubtype));
        if (directSubClass == null || isInstantiatedDirectlyOrIndirectly(directSubClass)) {
          indirectlyInstantiatedTypes.put(type, Boolean.TRUE);
          return true;
        }
      }
      indirectlyInstantiatedTypes.put(type, Boolean.FALSE);
      return false;
    }
  }

  public boolean isInstantiatedDirectlyOrIndirectly(DexProgramClass clazz) {
    assert checkIfObsolete();
    return isInstantiatedDirectly(clazz) || isInstantiatedIndirectly(clazz);
  }

  public boolean isFieldRead(DexEncodedField encodedField) {
    assert checkIfObsolete();
    DexField field = encodedField.field;
    FieldAccessInfoImpl info = fieldAccessInfoCollection.get(field);
    if (info != null && info.isRead()) {
      return true;
    }
    return isPinned(field)
        // Fields in the class that is synthesized by D8/R8 would be used soon.
        || field.holder.isD8R8SynthesizedClassType()
        // For library classes we don't know whether a field is read.
        || isLibraryOrClasspathField(encodedField);
  }

  public boolean isFieldWritten(DexEncodedField encodedField) {
    assert checkIfObsolete();
    return isFieldWrittenByFieldPutInstruction(encodedField) || isPinned(encodedField.field);
  }

  public boolean isFieldWrittenByFieldPutInstruction(DexEncodedField encodedField) {
    assert checkIfObsolete();
    DexField field = encodedField.field;
    FieldAccessInfoImpl info = fieldAccessInfoCollection.get(field);
    if (info != null && info.isWritten()) {
      // The field is written directly by the program itself.
      return true;
    }
    if (field.holder.isD8R8SynthesizedClassType()) {
      // Fields in the class that is synthesized by D8/R8 would be used soon.
      return true;
    }
    if (isLibraryOrClasspathField(encodedField)) {
      // For library classes we don't know whether a field is rewritten.
      return true;
    }
    return false;
  }

  public boolean isFieldOnlyWrittenInMethod(DexEncodedField field, DexEncodedMethod method) {
    assert checkIfObsolete();
    assert isFieldWritten(field) : "Expected field `" + field.toSourceString() + "` to be written";
    if (!isPinned(field.field)) {
      FieldAccessInfo fieldAccessInfo = fieldAccessInfoCollection.get(field.field);
      return fieldAccessInfo != null
          && fieldAccessInfo.isWritten()
          && !fieldAccessInfo.isWrittenOutside(method);
    }
    return false;
  }

  public boolean isStaticFieldWrittenOnlyInEnclosingStaticInitializer(DexEncodedField field) {
    assert checkIfObsolete();
    assert isFieldWritten(field) : "Expected field `" + field.toSourceString() + "` to be written";
    DexEncodedMethod staticInitializer =
        definitionFor(field.field.holder).asProgramClass().getClassInitializer();
    return staticInitializer != null && isFieldOnlyWrittenInMethod(field, staticInitializer);
  }

  public boolean mayPropagateValueFor(DexReference reference) {
    assert checkIfObsolete();
    return !isPinned(reference) && !neverPropagateValue.contains(reference);
  }

  private boolean isLibraryOrClasspathField(DexEncodedField field) {
    DexClass holder = definitionFor(field.field.holder);
    return holder == null || holder.isLibraryClass() || holder.isClasspathClass();
  }

  private static <T extends PresortedComparable<T>> ImmutableSortedSet<T> rewriteItems(
      Set<T> original, Function<T, T> rewrite) {
    Builder<T> builder = new Builder<>(PresortedComparable::slowCompare);
    for (T item : original) {
      builder.add(rewrite.apply(item));
    }
    return builder.build();
  }

  private static <T extends PresortedComparable<T>, S>
      SortedMap<T, Set<S>> rewriteKeysConservativelyWhileMergingValues(
          Map<T, Set<S>> original, Function<T, Set<T>> rewrite) {
    SortedMap<T, Set<S>> result = new TreeMap<>(PresortedComparable::slowCompare);
    for (T item : original.keySet()) {
      Set<T> rewrittenKeys = rewrite.apply(item);
      for (T rewrittenKey : rewrittenKeys) {
        result
            .computeIfAbsent(rewrittenKey, k -> Sets.newIdentityHashSet())
            .addAll(original.get(item));
      }
    }
    return Collections.unmodifiableSortedMap(result);
  }

  @Override
  protected boolean hasAnyInstantiatedLambdas(DexProgramClass clazz) {
    assert checkIfObsolete();
    return instantiatedLambdas.contains(clazz.type);
  }

  @Override
  public boolean hasLiveness() {
    assert checkIfObsolete();
    return true;
  }

  @Override
  public AppInfoWithLiveness withLiveness() {
    assert checkIfObsolete();
    return this;
  }

  public boolean isPinned(DexReference reference) {
    assert checkIfObsolete();
    return pinnedItems.contains(reference);
  }

  public boolean hasPinnedInstanceInitializer(DexType type) {
    assert type.isClassType();
    DexProgramClass clazz = asProgramClassOrNull(definitionFor(type));
    if (clazz != null) {
      for (DexEncodedMethod method : clazz.directMethods()) {
        if (method.isInstanceInitializer() && isPinned(method.method)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean canVirtualMethodBeImplementedInExtraSubclass(
      DexProgramClass clazz, DexMethod method) {
    // For functional interfaces that are instantiated by lambdas, we may not have synthesized all
    // the lambda classes yet, and therefore the set of subtypes for the holder may still be
    // incomplete.
    if (hasAnyInstantiatedLambdas(clazz)) {
      return true;
    }
    // If `clazz` is kept and `method` is a library method or a library method override, then it is
    // possible to create a class that inherits from `clazz` and overrides the library method.
    // Similarly, if `clazz` is kept and `method` is kept directly on `clazz` or indirectly on one
    // of its supertypes, then it is possible to create a class that inherits from `clazz` and
    // overrides the kept method.
    if (isPinned(clazz.type)) {
      ResolutionResult resolutionResult = resolveMethod(clazz, method);
      if (resolutionResult.isSingleResolution()) {
        DexEncodedMethod resolutionTarget = resolutionResult.getSingleTarget();
        return !resolutionTarget.isProgramMethod(this)
            || resolutionTarget.isLibraryMethodOverride().isPossiblyTrue()
            || isVirtualMethodPinnedDirectlyOrInAncestor(clazz, method);
      }
    }
    return false;
  }

  private boolean isVirtualMethodPinnedDirectlyOrInAncestor(
      DexProgramClass currentClass, DexMethod method) {
    // Look in all ancestor types, including `currentClass` itself.
    Set<DexProgramClass> visited = SetUtils.newIdentityHashSet(currentClass);
    Deque<DexProgramClass> worklist = new ArrayDeque<>(visited);
    while (!worklist.isEmpty()) {
      DexClass clazz = worklist.removeFirst();
      assert visited.contains(clazz);
      DexEncodedMethod methodInClass = clazz.lookupVirtualMethod(method);
      if (methodInClass != null && isPinned(methodInClass.method)) {
        return true;
      }
      for (DexType superType : clazz.allImmediateSupertypes()) {
        DexProgramClass superClass = asProgramClassOrNull(definitionFor(superType));
        if (superClass != null && visited.add(superClass)) {
          worklist.addLast(superClass);
        }
      }
    }
    return false;
  }

  public Set<DexReference> getPinnedItems() {
    assert checkIfObsolete();
    return pinnedItems;
  }

  /**
   * Returns a copy of this AppInfoWithLiveness where the set of classes is pruned using the given
   * DexApplication object.
   */
  public AppInfoWithLiveness prunedCopyFrom(
      DexApplication application,
      Collection<DexType> removedClasses,
      Collection<DexReference> additionalPinnedItems) {
    assert checkIfObsolete();
    return new AppInfoWithLiveness(this, application, removedClasses, additionalPinnedItems);
  }

  public AppInfoWithLiveness rewrittenWithLense(
      DirectMappedDexApplication application, GraphLense lense) {
    assert checkIfObsolete();
    return new AppInfoWithLiveness(this, application, lense);
  }

  /**
   * Returns true if the given type was part of the original program but has been removed during
   * tree shaking.
   */
  public boolean wasPruned(DexType type) {
    assert checkIfObsolete();
    return prunedTypes.contains(type);
  }

  public Set<DexType> getPrunedTypes() {
    assert checkIfObsolete();
    return prunedTypes;
  }

  public DexEncodedMethod lookupSingleTarget(
      Type type, DexMethod target, DexType invocationContext) {
    assert checkIfObsolete();
    DexType holder = target.holder;
    if (!holder.isClassType()) {
      return null;
    }
    switch (type) {
      case VIRTUAL:
        return lookupSingleVirtualTarget(target, invocationContext);
      case INTERFACE:
        return lookupSingleInterfaceTarget(target, invocationContext);
      case DIRECT:
        return lookupDirectTarget(target);
      case STATIC:
        return lookupStaticTarget(target);
      case SUPER:
        return lookupSuperTarget(target, invocationContext);
      default:
        return null;
    }
  }

  private DexEncodedMethod validateSingleVirtualTarget(
      DexEncodedMethod singleTarget, DexEncodedMethod resolutionResult) {
    assert SingleResolutionResult.isValidVirtualTarget(options(), resolutionResult);

    if (singleTarget == null || singleTarget == DexEncodedMethod.SENTINEL) {
      return null;
    }

    // Art978_virtual_interfaceTest correctly expects an IncompatibleClassChangeError exception
    // at runtime.
    if (isInvalidSingleVirtualTarget(singleTarget, resolutionResult)) {
      return null;
    }

    return singleTarget;
  }

  private boolean isInvalidSingleVirtualTarget(
      DexEncodedMethod singleTarget, DexEncodedMethod resolutionResult) {
    assert SingleResolutionResult.isValidVirtualTarget(options(), resolutionResult);
    // Art978_virtual_interfaceTest correctly expects an IncompatibleClassChangeError exception
    // at runtime.
    return !singleTarget.accessFlags.isAtLeastAsVisibleAs(resolutionResult.accessFlags);
  }

  /** For mapping invoke virtual instruction to single target method. */
  public DexEncodedMethod lookupSingleVirtualTarget(DexMethod method, DexType invocationContext) {
    assert checkIfObsolete();
    return lookupSingleVirtualTarget(method, invocationContext, method.holder, null);
  }

  public DexEncodedMethod lookupSingleVirtualTarget(
      DexMethod method,
      DexType invocationContext,
      DexType refinedReceiverType,
      ClassTypeLatticeElement receiverLowerBoundType) {
    assert checkIfObsolete();
    // TODO: replace invocationContext by a DexProgramClass typed formal.
    DexProgramClass invocationClass = asProgramClassOrNull(definitionFor(invocationContext));
    assert invocationClass != null;

    ResolutionResult resolutionResult = resolveMethodOnClass(method.holder, method);
    if (!resolutionResult.isAccessibleForVirtualDispatchFrom(invocationClass, this)) {
      return null;
    }

    DexEncodedMethod topTarget = resolutionResult.getSingleTarget();
    if (topTarget == null) {
      // A null target represents a valid target without a known defintion, ie, array clone().
      return null;
    }

    // If the target is a private method, then the invocation is a direct access to a nest member.
    if (topTarget.isPrivateMethod()) {
      return topTarget;
    }

    // If the lower-bound on the receiver type is the same as the upper-bound, then we have exact
    // runtime type information. In this case, the invoke will dispatch to the resolution result
    // from the runtime type of the receiver.
    if (receiverLowerBoundType != null) {
      if (receiverLowerBoundType.getClassType() == refinedReceiverType) {
        if (resolutionResult.isSingleResolution()
            && resolutionResult.isValidVirtualTargetForDynamicDispatch()) {
          ResolutionResult refinedResolutionResult = resolveMethod(refinedReceiverType, method);
          if (refinedResolutionResult.isSingleResolution()
              && refinedResolutionResult.isValidVirtualTargetForDynamicDispatch()) {
            return validateSingleVirtualTarget(
                refinedResolutionResult.getSingleTarget(), resolutionResult.getSingleTarget());
          }
        }
        return null;
      } else {
        // We should never hit the case at the moment, but if we start tracking more precise lower-
        // bound type information, we should handle this case as well.
      }
    }

    // This implements the logic from
    // https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokevirtual
    assert method != null;
    assert isSubtype(refinedReceiverType, method.holder);
    if (method.holder.isArrayType()) {
      return null;
    }
    DexClass holder = definitionFor(method.holder);
    if (holder == null || holder.isNotProgramClass()) {
      return null;
    }
    assert !holder.isInterface();
    boolean refinedReceiverIsStrictSubType = refinedReceiverType != method.holder;
    DexProgramClass refinedHolder =
        (refinedReceiverIsStrictSubType ? definitionFor(refinedReceiverType) : holder)
            .asProgramClass();
    if (refinedHolder == null) {
      return null;
    }
    assert !refinedHolder.isInterface();
    if (method.isSingleVirtualMethodCached(refinedReceiverType)) {
      return method.getSingleVirtualMethodCache(refinedReceiverType);
    }
    // First get the target for the holder type.
    ResolutionResult topMethod = resolveMethodOnClass(holder, method);
    // We might hit none or multiple targets. Both make this fail at runtime.
    if (!topMethod.isSingleResolution() || !topMethod.isValidVirtualTarget(options())) {
      method.setSingleVirtualMethodCache(refinedReceiverType, null);
      return null;
    }
    // Now, resolve the target with the refined receiver type.
    ResolutionResult refinedResolutionResult =
        refinedReceiverIsStrictSubType ? resolveMethodOnClass(refinedHolder, method) : topMethod;
    DexEncodedMethod topSingleTarget = refinedResolutionResult.getSingleTarget();
    DexClass topHolder = definitionFor(topSingleTarget.method.holder);
    // We need to know whether the top method is from an interface, as that would allow it to be
    // shadowed by a default method from an interface further down.
    boolean topIsFromInterface = topHolder.isInterface();
    // Now look at all subtypes and search for overrides.
    DexEncodedMethod result =
        validateSingleVirtualTarget(
            findSingleTargetFromSubtypes(
                refinedHolder,
                method,
                topSingleTarget,
                !refinedHolder.accessFlags.isAbstract(),
                topIsFromInterface),
            topMethod.getSingleTarget());
    assert result != DexEncodedMethod.SENTINEL;
    method.setSingleVirtualMethodCache(refinedReceiverType, result);
    return result;
  }

  /**
   * Computes which methods overriding <code>method</code> are visible for the subtypes of type.
   *
   * <p><code>candidate</code> is the definition further up the hierarchy that is visible from the
   * subtypes. If <code>candidateIsReachable</code> is true, the provided candidate is already a
   * target for a type further up the chain, so anything found in subtypes is a conflict. If it is
   * false, the target exists but is not reachable from a live type.
   *
   * <p>Returns <code>null</code> if the given type has no subtypes or all subtypes are abstract.
   * Returns {@link DexEncodedMethod#SENTINEL} if multiple live overrides were found. Returns the
   * single virtual target otherwise.
   */
  private DexEncodedMethod findSingleTargetFromSubtypes(
      DexProgramClass clazz,
      DexMethod method,
      DexEncodedMethod candidate,
      boolean candidateIsReachable,
      boolean checkForInterfaceConflicts) {
    // If the invoke could target a method in a class that is not visible to R8, then give up.
    if (canVirtualMethodBeImplementedInExtraSubclass(clazz, method)) {
      return DexEncodedMethod.SENTINEL;
    }
    // If the candidate is reachable, we already have a previous result.
    DexEncodedMethod result = candidateIsReachable ? candidate : null;
    for (DexType subtype : allImmediateExtendsSubtypes(clazz.type)) {
      DexProgramClass subclass = asProgramClassOrNull(definitionFor(subtype));
      if (subclass == null) {
        // Can't guarantee a single target.
        return DexEncodedMethod.SENTINEL;
      }
      DexEncodedMethod target = subclass.lookupVirtualMethod(method);
      if (target != null && !target.isPrivateMethod()) {
        // We found a method on this class. If this class is not abstract it is a runtime
        // reachable override and hence a conflict.
        if (!subclass.accessFlags.isAbstract()) {
          if (result != null && result != target) {
            // We found a new target on this subtype that does not match the previous one. Fail.
            return DexEncodedMethod.SENTINEL;
          }
          // Add the first or matching target.
          result = target;
        }
      }
      if (checkForInterfaceConflicts) {
        // We have to check whether there are any default methods in implemented interfaces.
        if (interfacesMayHaveDefaultFor(subclass.interfaces, method)) {
          return DexEncodedMethod.SENTINEL;
        }
      }
      DexEncodedMethod newCandidate = target == null ? candidate : target;
      // If we have a new target and did not fail, it is not an override of a reachable method.
      // Whether the target is actually reachable depends on whether this class is abstract.
      // If we did not find a new target, the candidate is reachable if it was before, or if this
      // class is not abstract.
      boolean newCandidateIsReachable =
          !subclass.accessFlags.isAbstract() || ((target == null) && candidateIsReachable);
      DexEncodedMethod subtypeTarget =
          findSingleTargetFromSubtypes(
              subclass, method, newCandidate, newCandidateIsReachable, checkForInterfaceConflicts);
      if (subtypeTarget != null) {
        // We found a target in the subclasses. If we already have a different result, fail.
        if (result != null && result != subtypeTarget) {
          return DexEncodedMethod.SENTINEL;
        }
        // Remember this new result.
        result = subtypeTarget;
      }
    }
    return result;
  }

  /**
   * Checks whether any interface in the given list or their super interfaces implement a default
   * method.
   *
   * <p>This method is conservative for unknown interfaces and interfaces from the library.
   */
  private boolean interfacesMayHaveDefaultFor(DexTypeList ifaces, DexMethod method) {
    for (DexType iface : ifaces.values) {
      DexClass clazz = definitionFor(iface);
      if (clazz == null || clazz.isNotProgramClass()) {
        return true;
      }
      DexEncodedMethod candidate = clazz.lookupMethod(method);
      if (candidate != null && !candidate.accessFlags.isAbstract()) {
        return true;
      }
      if (interfacesMayHaveDefaultFor(clazz.interfaces, method)) {
        return true;
      }
    }
    return false;
  }

  public DexEncodedMethod lookupSingleInterfaceTarget(DexMethod method, DexType invocationContext) {
    assert checkIfObsolete();
    return lookupSingleInterfaceTarget(method, invocationContext, method.holder, null);
  }

  public DexEncodedMethod lookupSingleInterfaceTarget(
      DexMethod method,
      DexType invocationContext,
      DexType refinedReceiverType,
      ClassTypeLatticeElement receiverLowerBoundType) {
    assert checkIfObsolete();
    // Replace DexType invocationContext by DexProgramClass throughout.
    DexProgramClass invocationClass = asProgramClassOrNull(definitionFor(invocationContext));
    assert invocationClass != null;

    // If the lower-bound on the receiver type is the same as the upper-bound, then we have exact
    // runtime type information. In this case, the invoke will dispatch to the resolution result
    // from the runtime type of the receiver.
    if (receiverLowerBoundType != null) {
      if (receiverLowerBoundType.getClassType() == refinedReceiverType) {
        ResolutionResult resolutionResult = resolveMethod(method.holder, method, true);
        if (resolutionResult.isSingleResolution()
            && resolutionResult.isValidVirtualTargetForDynamicDispatch()) {
          ResolutionResult refinedResolutionResult = resolveMethod(refinedReceiverType, method);
          if (refinedResolutionResult.isSingleResolution()
              && refinedResolutionResult.isValidVirtualTargetForDynamicDispatch()) {
            return validateSingleVirtualTarget(
                refinedResolutionResult.getSingleTarget(), resolutionResult.getSingleTarget());
          }
        }
        return null;
      } else {
        // We should never hit the case at the moment, but if we start tracking more precise lower-
        // bound type information, we should handle this case as well.
      }
    }

    DexProgramClass holder = asProgramClassOrNull(definitionFor(method.holder));
    if (holder == null || !holder.accessFlags.isInterface()) {
      return null;
    }
    // First check that there is a visible and valid target for this invoke-interface to hit.
    // If there is none, this will fail at runtime.
    ResolutionResult topResolution = resolveMethodOnInterface(holder, method);
    if (!topResolution.isAccessibleForVirtualDispatchFrom(invocationClass, this)) {
      return null;
    }

    DexEncodedMethod topTarget = topResolution.getSingleTarget();
    if (topTarget == null) {
      // An null target represents a valid target with no known defintion, eg, array clone().
      return null;
    }

    // If the target is a private method, then the invocation is a direct access to a nest member.
    if (topTarget.isPrivateMethod()) {
      return topTarget;
    }

    // If the invoke could target a method in a class that is not visible to R8, then give up.
    if (canVirtualMethodBeImplementedInExtraSubclass(holder, method)) {
      return null;
    }

    DexProgramClass refinedReceiverClass = definitionFor(refinedReceiverType).asProgramClass();
    if (refinedReceiverClass == null) {
      return null;
    }

    // For functional interfaces that are instantiated by lambdas, we may not have synthesized all
    // the lambda classes yet, and therefore the set of subtypes for the holder may still be
    // incomplete.
    if (hasAnyInstantiatedLambdas(refinedReceiverClass)) {
      return null;
    }

    Iterable<DexType> subtypesToExplore =
        isInstantiatedDirectly(refinedReceiverClass)
            ? Iterables.concat(ImmutableList.of(refinedReceiverType), subtypes(refinedReceiverType))
            : subtypes(refinedReceiverType);

    // The loop will ignore uninstantiated classes as they will not be a target at runtime.
    DexEncodedMethod result = null;
    for (DexType type : subtypesToExplore) {
      DexProgramClass clazz = asProgramClassOrNull(definitionFor(type));
      if (clazz == null) {
        // Cannot guarantee a single target.
        return null;
      }

      // If the invoke could target a method in a class that is not visible to R8, then give up.
      if (canVirtualMethodBeImplementedInExtraSubclass(clazz, method)) {
        return null;
      }

      if (!isInstantiatedDirectly(clazz)) {
        // This is not a possible receiver at runtime.
        continue;
      }

      // TODO(b/145344105): Abstract classes should never be considered instantiated.
      // assert (!clazz.isAbstract() && !clazz.isInterface()) || clazz.isAnnotation();

      DexEncodedMethod resolutionResult = resolveMethod(clazz, method).getSingleTarget();
      if (resolutionResult == null || isInvalidSingleVirtualTarget(resolutionResult, topTarget)) {
        // This will fail at runtime.
        return null;
      }
      if (result != null && result != resolutionResult) {
        return null;
      }
      result = resolutionResult;
    }
    assert result == null || !isInvalidSingleVirtualTarget(result, topTarget);
    return result == null || !result.isVirtualMethod() ? null : result;
  }

  public AppInfoWithLiveness addSwitchMaps(Map<DexField, Int2ReferenceMap<DexField>> switchMaps) {
    assert checkIfObsolete();
    assert this.switchMaps.isEmpty();
    return new AppInfoWithLiveness(this, switchMaps, enumValueInfoMaps);
  }

  public AppInfoWithLiveness addEnumValueInfoMaps(
      Map<DexType, Map<DexField, EnumValueInfo>> enumValueInfoMaps) {
    assert checkIfObsolete();
    assert this.enumValueInfoMaps.isEmpty();
    return new AppInfoWithLiveness(this, switchMaps, enumValueInfoMaps);
  }
}
