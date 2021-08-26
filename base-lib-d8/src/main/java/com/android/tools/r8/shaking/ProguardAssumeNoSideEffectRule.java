// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import java.util.List;

public class ProguardAssumeNoSideEffectRule extends ProguardConfigurationRule {

  public static class Builder
      extends ProguardConfigurationRule.Builder<ProguardAssumeNoSideEffectRule, Builder> {

    private Builder() {
      super();
    }

    @Override
    public Builder self() {
      return this;
    }

    @Override
    public ProguardAssumeNoSideEffectRule build() {
      return new ProguardAssumeNoSideEffectRule(origin, getPosition(), source, classAnnotation,
          classAccessFlags, negatedClassAccessFlags, classTypeNegated, classType, classNames,
          inheritanceAnnotation, inheritanceClassName, inheritanceIsExtends, memberRules);
    }
  }

  private ProguardAssumeNoSideEffectRule(
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
      List<ProguardMemberRule> memberRules) {
    super(origin, position, source, classAnnotation, classAccessFlags, negatedClassAccessFlags,
        classTypeNegated, classType, classNames, inheritanceAnnotation, inheritanceClassName,
        inheritanceIsExtends, memberRules);
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean applyToNonProgramClasses() {
    return true;
  }

  @Override
  String typeString() {
    return "assumenosideeffects";
  }
}
