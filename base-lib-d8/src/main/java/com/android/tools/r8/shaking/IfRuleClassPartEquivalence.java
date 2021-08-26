// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.google.common.base.Equivalence;
import com.google.common.collect.Iterables;
import java.util.Objects;

public class IfRuleClassPartEquivalence extends Equivalence<ProguardIfRule> {

  @Override
  protected boolean doEquivalent(ProguardIfRule p1, ProguardIfRule p2) {
    if (!Objects.equals(p1.getClassAnnotation(), p2.getClassAnnotation())) {
      return false;
    }
    if (!p1.getClassAccessFlags().equals(p2.getClassAccessFlags())
        || !p1.getNegatedClassAccessFlags().equals(p2.getNegatedClassAccessFlags())) {
      return false;
    }
    if (p1.getClassType() != p2.getClassType()
        || p1.getClassTypeNegated() != p2.getClassTypeNegated()) {
      return false;
    }
    if (p1.getInheritanceIsExtends() != p2.getInheritanceIsExtends()) {
      return false;
    }
    if (!Objects.equals(p1.getInheritanceAnnotation(), p2.getInheritanceAnnotation())) {
      return false;
    }
    if (!Objects.equals(p1.getInheritanceClassName(), p2.getInheritanceClassName())) {
      return false;
    }
    if (!p1.getClassNames().equals(p2.getClassNames())) {
      return false;
    }
    return true;
  }

  @Override
  protected int doHash(ProguardIfRule rule) {
    int result = (rule.getClassAnnotation() != null ? rule.getClassAnnotation().hashCode() : 0);
    result = 3 * result + rule.getClassAccessFlags().hashCode();
    result = 3 * result + rule.getNegatedClassAccessFlags().hashCode();
    result = 3 * result + (rule.getClassTypeNegated() ? 1 : 0);
    result = 3 * result + (rule.getClassType() != null ? rule.getClassType().hashCode() : 0);
    result = 3 * result + rule.getClassNames().hashCode();
    result =
        3 * result
            + (rule.getInheritanceAnnotation() != null
                ? rule.getInheritanceAnnotation().hashCode()
                : 0);
    result =
        3 * result
            + (rule.getInheritanceClassName() != null
                ? rule.getInheritanceClassName().hashCode()
                : 0);
    result = 3 * result + (rule.getInheritanceIsImplements() ? 1 : 0);
    result = 3 * result + Iterables.size(rule.subsequentRule.getWildcards());
    return result;
  }
}
