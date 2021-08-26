// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import java.util.List;
import java.util.function.Consumer;

public class ProguardKeepRuleBase extends ProguardConfigurationRule {

  public static abstract class Builder<C extends ProguardKeepRuleBase, B extends Builder<C, B>>
      extends ProguardConfigurationRule.Builder<C, B> {

    protected ProguardKeepRuleType type;
    protected final ProguardKeepRuleModifiers.Builder modifiersBuilder =
        ProguardKeepRuleModifiers.builder();

    protected Builder() {
      super();
    }

    public B setType(ProguardKeepRuleType type) {
      this.type = type;
      return self();
    }

    public ProguardKeepRuleModifiers.Builder getModifiersBuilder() {
      return modifiersBuilder;
    }

    public B updateModifiers(Consumer<ProguardKeepRuleModifiers.Builder> consumer) {
      consumer.accept(getModifiersBuilder());
      return self();
    }
  }

  private final ProguardKeepRuleType type;
  private final ProguardKeepRuleModifiers modifiers;

  protected ProguardKeepRuleBase(
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
        inheritanceIsExtends, memberRules);
    this.type = type;
    this.modifiers = modifiers;
  }

  public ProguardKeepRuleType getType() {
    return type;
  }

  public ProguardKeepRuleModifiers getModifiers() {
    return modifiers;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProguardKeepRuleBase)) {
      return false;
    }
    ProguardKeepRuleBase that = (ProguardKeepRuleBase) o;

    if (type != that.type) {
      return false;
    }
    if (!modifiers.equals(that.modifiers)) {
      return false;
    }
    return super.equals(that);
  }

  @Override
  public int hashCode() {
    // Used multiplier 3 to avoid too much overflow when computing hashCode.
    int result = type.hashCode();
    result = 3 * result + modifiers.hashCode();
    result = 3 * result + super.hashCode();
    return result;
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

  @Override
  String typeString() {
    return type.toString();
  }

  @Override
  String modifierString() {
    return modifiers.toString();
  }
}
