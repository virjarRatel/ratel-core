// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProguardKeepRule extends ProguardKeepRuleBase {

  public static class Builder extends ProguardKeepRuleBase.Builder<ProguardKeepRule, Builder> {

    protected Builder() {
      super();
    }

    @Override
    public Builder self() {
      return this;
    }

    @Override
    public ProguardKeepRule build() {
      return new ProguardKeepRule(origin, getPosition(), source, classAnnotation, classAccessFlags,
          negatedClassAccessFlags, classTypeNegated, classType, classNames, inheritanceAnnotation,
          inheritanceClassName, inheritanceIsExtends, memberRules, type, modifiersBuilder.build());
    }
  }

  protected ProguardKeepRule(
      Origin origin,
      Position position,
      String source,
      ProguardTypeMatcher classAnnotation,
      ProguardAccessFlags classAccessFlags,
      ProguardAccessFlags negatedClassAccessFlags,
      boolean classTypeNegated,
      ProguardClassType classType,
      ProguardClassNameList classNames,
      ProguardTypeMatcher inheritanceAnnotation,
      ProguardTypeMatcher inheritanceClassName,
      boolean inheritanceIsExtends,
      List<ProguardMemberRule> memberRules,
      ProguardKeepRuleType type,
      ProguardKeepRuleModifiers modifiers) {
    super(origin, position, source, classAnnotation, classAccessFlags, negatedClassAccessFlags,
        classTypeNegated, classType, classNames, inheritanceAnnotation, inheritanceClassName,
        inheritanceIsExtends, memberRules, type, modifiers);
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  protected ProguardKeepRule materialize(DexItemFactory dexItemFactory) {
    return new ProguardKeepRule(
        getOrigin(),
        getPosition(),
        getSource(),
        getClassAnnotation() == null ? null : getClassAnnotation().materialize(dexItemFactory),
        getClassAccessFlags(),
        getNegatedClassAccessFlags(),
        getClassTypeNegated(),
        getClassType(),
        getClassNames() == null ? null : getClassNames().materialize(dexItemFactory),
        getInheritanceAnnotation() == null
            ? null
            : getInheritanceAnnotation().materialize(dexItemFactory),
        getInheritanceClassName() == null
            ? null
            : getInheritanceClassName().materialize(dexItemFactory),
        getInheritanceIsExtends(),
        getMemberRules() == null
            ? null
            : getMemberRules().stream()
                .map(memberRule -> memberRule.materialize(dexItemFactory))
                .collect(Collectors.toList()),
        getType(),
        getModifiers());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProguardKeepRule)) {
      return false;
    }
    ProguardKeepRule that = (ProguardKeepRule) o;
    return super.equals(that);
  }

  static void appendNonEmpty(StringBuilder builder, String pre, Object item, String post) {
    if (item == null) {
      return;
    }
    String text = item.toString();
    if (!text.isEmpty()) {
      if (pre != null) {
        builder.append(pre);
      }
      builder.append(text);
      if (post != null) {
        builder.append(post);
      }
    }
  }

  public static ProguardKeepRule defaultKeepAllRule(
      Consumer<ProguardKeepRuleModifiers.Builder> modifiers) {
    ProguardKeepRule.Builder builder = ProguardKeepRule.builder();
    builder.setOrigin(
        new Origin(Origin.root()) {
          @Override
          public String part() {
            return "<DEFAULT_KEEP_ALL_RULE>";
          }
        });
    builder.setClassType(ProguardClassType.CLASS);
    builder.matchAllSpecification();
    builder.setType(ProguardKeepRuleType.KEEP);
    modifiers.accept(builder.getModifiersBuilder());
    return builder.build();
  }

  @Override
  public boolean isProguardKeepRule() {
    return true;
  }

  @Override
  public ProguardKeepRule asProguardKeepRule() {
    return this;
  }
}
