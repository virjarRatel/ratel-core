// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.ProguardConfigurationParser.IdentifierPatternWithWildcards;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.LongInterval;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProguardConfigurationUtils {

  private static Origin proguardCompatOrigin =
      new Origin(Origin.root()) {
        @Override
        public String part() {
          return "<PROGUARD_COMPATIBILITY_RULE>";
        }
      };

  private static Origin synthesizedRecompilationOrigin =
      new Origin(Origin.root()) {
        @Override
        public String part() {
          return "<SYNTHESIZED_RECOMPILATION_RULE>";
        }
      };

  public static ProguardKeepRule buildDefaultInitializerKeepRule(DexClass clazz) {
    ProguardKeepRule.Builder builder = ProguardKeepRule.builder();
    builder.setOrigin(proguardCompatOrigin);
    builder.setType(ProguardKeepRuleType.KEEP);
    builder.getModifiersBuilder().setAllowsObfuscation(true);
    builder.getModifiersBuilder().setAllowsOptimization(true);
    builder.getClassAccessFlags().setVisibility(clazz.accessFlags);
    builder.setClassType(ProguardClassType.CLASS);
    builder.setClassNames(
        ProguardClassNameList.singletonList(ProguardTypeMatcher.create(clazz.type)));
    if (clazz.hasDefaultInitializer()) {
      ProguardMemberRule.Builder memberRuleBuilder = ProguardMemberRule.builder();
      memberRuleBuilder.setRuleType(ProguardMemberType.INIT);
      memberRuleBuilder.setName(IdentifierPatternWithWildcards.withoutWildcards("<init>"));
      memberRuleBuilder.setArguments(ImmutableList.of());
      builder.getMemberRules().add(memberRuleBuilder.build());
    }
    return builder.build();
  }

  public static ProguardKeepRule buildMethodKeepRule(DexClass clazz, DexEncodedMethod method) {
    // TODO(b/122295241): These generated rules should be linked into the graph, eg, the method
    // using identified reflection should be the source keeping the target alive.
    assert clazz.type == method.method.holder;
    ProguardKeepRule.Builder builder = ProguardKeepRule.builder();
    builder.setOrigin(proguardCompatOrigin);
    builder.setType(ProguardKeepRuleType.KEEP_CLASS_MEMBERS);
    builder.getModifiersBuilder().setAllowsObfuscation(true);
    builder.getModifiersBuilder().setAllowsOptimization(true);
    builder.getClassAccessFlags().setVisibility(clazz.accessFlags);
    if (clazz.isInterface()) {
      builder.setClassType(ProguardClassType.INTERFACE);
    } else {
      builder.setClassType(ProguardClassType.CLASS);
    }
    builder.setClassNames(
        ProguardClassNameList.singletonList(ProguardTypeMatcher.create(clazz.type)));
    ProguardMemberRule.Builder memberRuleBuilder = ProguardMemberRule.builder();
    memberRuleBuilder.setRuleType(ProguardMemberType.METHOD);
    memberRuleBuilder.getAccessFlags().setFlags(method.accessFlags);
    memberRuleBuilder.setName(
        IdentifierPatternWithWildcards.withoutWildcards(method.method.name.toString()));
    memberRuleBuilder.setTypeMatcher(ProguardTypeMatcher.create(method.method.proto.returnType));
    List<ProguardTypeMatcher> arguments = Arrays.stream(method.method.proto.parameters.values)
        .map(ProguardTypeMatcher::create)
        .collect(Collectors.toList());
    memberRuleBuilder.setArguments(arguments);
    builder.getMemberRules().add(memberRuleBuilder.build());
    return builder.build();
  }

  public static ProguardAssumeNoSideEffectRule buildAssumeNoSideEffectsRuleForApiLevel(
      DexItemFactory factory, AndroidApiLevel apiLevel) {
    Origin synthesizedFromApiLevel =
        new Origin(Origin.root()) {
          @Override
          public String part() {
            return "<SYNTHESIZED_FROM_API_LEVEL_" + apiLevel.getLevel() + ">";
          }
        };

    ProguardAccessFlags publicStaticFinalFlags = new ProguardAccessFlags();
    publicStaticFinalFlags.setPublic();
    publicStaticFinalFlags.setStatic();
    publicStaticFinalFlags.setFinal();

    return ProguardAssumeNoSideEffectRule.builder()
        .setOrigin(synthesizedFromApiLevel)
        .setClassType(ProguardClassType.CLASS)
        .setClassNames(
            ProguardClassNameList.singletonList(
                ProguardTypeMatcher.create(factory.createType("Landroid/os/Build$VERSION;"))))
        .setMemberRules(
            ImmutableList.of(
                ProguardMemberRule.builder()
                    .setAccessFlags(publicStaticFinalFlags)
                    .setRuleType(ProguardMemberType.FIELD)
                    .setTypeMatcher(ProguardTypeMatcher.create(factory.intType))
                    .setName(IdentifierPatternWithWildcards.withoutWildcards("SDK_INT"))
                    .setReturnValue(
                        new ProguardMemberRuleReturnValue(
                            new LongInterval(apiLevel.getLevel(), Integer.MAX_VALUE)))
                    .build()))
        .build();
  }

  /**
   * Check if an explicit rule matching the field public static final int
   * android.os.Build$VERSION.SDK_INT is present.
   */
  public static boolean hasExplicitAssumeValuesOrAssumeNoSideEffectsRuleForMinSdk(
      DexItemFactory factory, List<ProguardConfigurationRule> rules) {
    for (ProguardConfigurationRule rule : rules) {
      if (!(rule instanceof ProguardAssumeValuesRule
          || rule instanceof ProguardAssumeNoSideEffectRule)) {
        continue;
      }
      if (rule.getClassType() != ProguardClassType.CLASS) {
        continue;
      }
      if (rule.hasInheritanceClassName()
          && !rule.getInheritanceClassName().matches(factory.objectType)) {
        continue;
      }
      if (!rule.getClassNames().matches(factory.createType("Landroid/os/Build$VERSION;"))) {
        continue;
      }
      for (ProguardMemberRule memberRule : rule.getMemberRules()) {
        if (memberRule.getRuleType() == ProguardMemberType.ALL
            || memberRule.getRuleType() == ProguardMemberType.ALL_FIELDS) {
          return true;
        }
        if (memberRule.getRuleType() != ProguardMemberType.FIELD) {
          continue;
        }
        if (memberRule.getAccessFlags().isProtected()
            || memberRule.getAccessFlags().isPrivate()
            || memberRule.getAccessFlags().isAbstract()
            || memberRule.getAccessFlags().isTransient()
            || memberRule.getAccessFlags().isVolatile()) {
          continue;
        }
        if (memberRule.getNegatedAccessFlags().isPublic()
            || memberRule.getNegatedAccessFlags().isStatic()
            || memberRule.getNegatedAccessFlags().isFinal()) {
          continue;
        }
        if (!memberRule.getType().matches(factory.intType)) {
          continue;
        }
        if (!memberRule.getName().matches("SDK_INT")) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  public static void synthesizeKeepRulesForRecompilation(
      ProguardConfigurationRule rule, List<ProguardConfigurationRule> synthesizedKeepRules) {
    if (rule.hasInheritanceClassName()) {
      ProguardTypeMatcher inheritanceClassName = rule.getInheritanceClassName();
      synthesizedKeepRules.add(
          ProguardKeepRule.builder()
              .setOrigin(synthesizedRecompilationOrigin)
              .setType(ProguardKeepRuleType.KEEP)
              .setClassType(
                  rule.getInheritanceIsExtends()
                      ? ProguardClassType.CLASS
                      : ProguardClassType.INTERFACE)
              .setClassNames(ProguardClassNameList.singletonList(inheritanceClassName))
              .build());
    }
  }
}
