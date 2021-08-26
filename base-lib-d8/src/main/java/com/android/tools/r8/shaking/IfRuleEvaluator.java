// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.Enqueuer.Mode;
import com.android.tools.r8.shaking.RootSetBuilder.ConsequentRootSet;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class IfRuleEvaluator {

  private final AppView<? extends AppInfoWithSubtyping> appView;
  private final ExecutorService executorService;
  private final List<Future<?>> futures = new ArrayList<>();
  private final Map<Wrapper<ProguardIfRule>, Set<ProguardIfRule>> ifRules;
  private final Set<DexEncodedField> liveFields;
  private final Set<DexEncodedMethod> liveMethods;
  private final Set<DexProgramClass> liveTypes;
  private final Mode mode;
  private final RootSetBuilder rootSetBuilder;
  private final Set<DexEncodedMethod> targetedMethods;

  IfRuleEvaluator(
      AppView<? extends AppInfoWithSubtyping> appView,
      ExecutorService executorService,
      Map<Wrapper<ProguardIfRule>, Set<ProguardIfRule>> ifRules,
      Set<DexEncodedField> liveFields,
      Set<DexEncodedMethod> liveMethods,
      Set<DexProgramClass> liveTypes,
      Mode mode,
      RootSetBuilder rootSetBuilder,
      Set<DexEncodedMethod> targetedMethods) {
    this.appView = appView;
    this.executorService = executorService;
    this.ifRules = ifRules;
    this.liveFields = liveFields;
    this.liveMethods = liveMethods;
    this.liveTypes = liveTypes;
    this.mode = mode;
    this.rootSetBuilder = rootSetBuilder;
    this.targetedMethods = targetedMethods;
  }

  public ConsequentRootSet run() throws ExecutionException {
    appView.appInfo().app().timing.begin("Find consequent items for -if rules...");
    try {
      if (ifRules != null && !ifRules.isEmpty()) {
        Iterator<Map.Entry<Wrapper<ProguardIfRule>, Set<ProguardIfRule>>> it =
            ifRules.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Wrapper<ProguardIfRule>, Set<ProguardIfRule>> ifRuleEntry = it.next();
          ProguardIfRule ifRule = ifRuleEntry.getKey().get();

          // Depending on which types that trigger the -if rule, the application of the subsequent
          // -keep rule may vary (due to back references). So, we need to try all pairs of -if
          // rule and live types.
          for (DexProgramClass clazz :
              ifRule.relevantCandidatesForRule(appView, appView.appInfo().classes())) {
            if (!isEffectivelyLive(clazz)) {
              continue;
            }

            // Check if the class matches the if-rule.
            if (appView.options().testing.measureProguardIfRuleEvaluations) {
              appView.options()
                  .testing
                  .proguardIfRuleEvaluationData
                  .numberOfProguardIfRuleClassEvaluations++;
            }
            if (evaluateClassForIfRule(ifRule, clazz, clazz)) {
              // When matching an if rule against a type, the if-rule are filled with the current
              // capture of wildcards. Propagate this down to member rules with same class part
              // equivalence.
              ifRuleEntry
                  .getValue()
                  .removeIf(
                      memberRule -> {
                        registerClassCapture(memberRule, clazz, clazz);
                        if (appView.options().testing.measureProguardIfRuleEvaluations) {
                          appView.options()
                              .testing
                              .proguardIfRuleEvaluationData
                              .numberOfProguardIfRuleMemberEvaluations++;
                        }
                        return evaluateIfRuleMembersAndMaterialize(memberRule, clazz, clazz)
                            && canRemoveSubsequentKeepRule(memberRule);
                      });
            }

            // Check if one of the types that have been merged into `clazz` satisfies the if-rule.
            if (appView.options().enableVerticalClassMerging
                && appView.verticallyMergedClasses() != null) {
              Iterable<DexType> sources =
                  appView.verticallyMergedClasses().getSourcesFor(clazz.type);
              for (DexType sourceType : sources) {
                // Note that, although `sourceType` has been merged into `type`, the dex class for
                // `sourceType` is still available until the second round of tree shaking. This
                // way we can still retrieve the access flags of `sourceType`.
                DexClass sourceClass = appView.definitionFor(sourceType);
                assert sourceClass != null;
                if (appView.options().testing.measureProguardIfRuleEvaluations) {
                  appView.options()
                      .testing
                      .proguardIfRuleEvaluationData
                      .numberOfProguardIfRuleClassEvaluations++;
                }
                if (evaluateClassForIfRule(ifRule, sourceClass, clazz)) {
                  ifRuleEntry
                      .getValue()
                      .removeIf(
                          memberRule -> {
                            registerClassCapture(memberRule, sourceClass, clazz);
                            if (appView.options().testing.measureProguardIfRuleEvaluations) {
                              appView.options()
                                  .testing
                                  .proguardIfRuleEvaluationData
                                  .numberOfProguardIfRuleMemberEvaluations++;
                            }
                            return evaluateIfRuleMembersAndMaterialize(
                                    memberRule, sourceClass, clazz)
                                && canRemoveSubsequentKeepRule(memberRule);
                          });
                }
              }
            }
          }
          if (ifRuleEntry.getValue().isEmpty()) {
            it.remove();
          }
        }
        ThreadUtils.awaitFutures(futures);
      }
    } finally {
      appView.appInfo().app().timing.end();
    }
    return rootSetBuilder.buildConsequentRootSet();
  }

  private boolean canRemoveSubsequentKeepRule(ProguardIfRule rule) {
    // We cannot remove an if-rule if there is a kept graph consumer, otherwise we would not record
    // all edges.
    return Iterables.isEmpty(rule.subsequentRule.getWildcards())
        && appView.options().keptGraphConsumer == null;
  }

  /**
   * To ensure the matching work correctly going forward, when a class has matched, we have to run
   * the capturing again on the member rule to ensure the wild cards are correctly populated.
   *
   * @param memberRule The member rule to populate with wildcards.
   * @param source The source class.
   * @param target The target class that can be different when we have vertically merged classes.
   */
  private void registerClassCapture(ProguardIfRule memberRule, DexClass source, DexClass target) {
    boolean classNameResult = memberRule.getClassNames().matches(source.type);
    assert classNameResult;
    if (memberRule.hasInheritanceClassName()) {
      boolean inheritanceResult = rootSetBuilder.satisfyInheritanceRule(target, memberRule);
      assert inheritanceResult;
    }
  }

  private boolean isEffectivelyLive(DexProgramClass clazz) {
    // A type is effectively live if (1) it is truly live, (2) the value of one of its fields has
    // been inlined by the member value propagation, or (3) the return value of one of its methods
    // has been forwarded by the member value propagation.
    if (liveTypes.contains(clazz)) {
      return true;
    }
    for (DexEncodedField field : clazz.fields()) {
      if (field.getOptimizationInfo().valueHasBeenPropagated()) {
        return true;
      }
    }
    for (DexEncodedMethod method : clazz.methods()) {
      if (method.getOptimizationInfo().returnValueHasBeenPropagated()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines if `sourceClass` satisfies the given if-rule class specification. If `sourceClass`
   * has not been merged into another class, then `targetClass` is the same as `sourceClass`.
   * Otherwise, `targetClass` denotes the class that `sourceClass` has been merged into.
   */
  private boolean evaluateClassForIfRule(
      ProguardIfRule rule, DexClass sourceClass, DexClass targetClass) {
    if (!RootSetBuilder.satisfyClassType(rule, sourceClass)) {
      return false;
    }
    if (!RootSetBuilder.satisfyAccessFlag(rule, sourceClass)) {
      return false;
    }
    if (!RootSetBuilder.satisfyAnnotation(rule, sourceClass)) {
      return false;
    }
    if (!rule.getClassNames().matches(sourceClass.type)) {
      return false;
    }
    if (rule.hasInheritanceClassName()) {
      // Try another live type since the current one doesn't satisfy the inheritance rule.
      return rootSetBuilder.satisfyInheritanceRule(sourceClass, rule);
    }
    return true;
  }

  private boolean evaluateIfRuleMembersAndMaterialize(
      ProguardIfRule rule, DexClass sourceClass, DexClass targetClass) {
    Collection<ProguardMemberRule> memberKeepRules = rule.getMemberRules();
    if (memberKeepRules.isEmpty()) {
      materializeIfRule(rule, ImmutableSet.of(sourceClass.toReference()));
      return true;
    }

    Set<DexDefinition> filteredMembers = Sets.newIdentityHashSet();
    Iterables.addAll(
        filteredMembers,
        targetClass.fields(
            f ->
                (liveFields.contains(f) || f.getOptimizationInfo().valueHasBeenPropagated())
                    && appView.graphLense().getOriginalFieldSignature(f.field).holder
                        == sourceClass.type));
    Iterables.addAll(
        filteredMembers,
        targetClass.methods(
            m ->
                (liveMethods.contains(m)
                        || targetedMethods.contains(m)
                        || m.getOptimizationInfo().returnValueHasBeenPropagated())
                    && appView.graphLense().getOriginalMethodSignature(m.method).holder
                        == sourceClass.type));

    // If the number of member rules to hold is more than live members, we can't make it.
    if (filteredMembers.size() < memberKeepRules.size()) {
      return false;
    }

    // Depending on which members trigger the -if rule, the application of the subsequent
    // -keep rule may vary (due to back references). So, we need to try literally all
    // combinations of live members. But, we can at least limit the number of elements per
    // combination as the size of member rules to satisfy.
    for (Set<DexDefinition> combination :
        Sets.combinations(filteredMembers, memberKeepRules.size())) {
      Collection<DexEncodedField> fieldsInCombination =
          DexDefinition.filterDexEncodedField(combination.stream()).collect(Collectors.toList());
      Collection<DexEncodedMethod> methodsInCombination =
          DexDefinition.filterDexEncodedMethod(combination.stream()).collect(Collectors.toList());
      // Member rules are combined as AND logic: if found unsatisfied member rule, this
      // combination of live members is not a good fit.
      boolean satisfied =
          memberKeepRules.stream()
              .allMatch(
                  memberRule ->
                      rootSetBuilder.ruleSatisfiedByFields(memberRule, fieldsInCombination)
                          || rootSetBuilder.ruleSatisfiedByMethods(
                              memberRule, methodsInCombination));
      if (satisfied) {
        materializeIfRule(rule, ImmutableSet.of(sourceClass.toReference()));
        if (canRemoveSubsequentKeepRule(rule)) {
          return true;
        }
      }
    }
    return false;
  }

  private void materializeIfRule(ProguardIfRule rule, Set<DexReference> preconditions) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    ProguardIfRule materializedRule = rule.materialize(dexItemFactory, preconditions);

    if (mode.isInitialTreeShaking() && !rule.isUsed()) {
      // We need to abort class inlining of classes that could be matched by the condition of this
      // -if rule.
      ClassInlineRule neverClassInlineRuleForCondition =
          materializedRule.neverClassInlineRuleForCondition(dexItemFactory);
      if (neverClassInlineRuleForCondition != null) {
        rootSetBuilder.runPerRule(executorService, futures, neverClassInlineRuleForCondition, null);
      }

      // If the condition of the -if rule has any members, then we need to keep these members to
      // ensure that the subsequent rule will be applied again in the second round of tree
      // shaking.
      InlineRule neverInlineRuleForCondition =
          materializedRule.neverInlineRuleForCondition(dexItemFactory);
      if (neverInlineRuleForCondition != null) {
        rootSetBuilder.runPerRule(executorService, futures, neverInlineRuleForCondition, null);
      }
    }

    // Keep whatever is required by the -if rule.
    rootSetBuilder.runPerRule(
        executorService, futures, materializedRule.subsequentRule, materializedRule);
    rule.markAsUsed();
  }
}
