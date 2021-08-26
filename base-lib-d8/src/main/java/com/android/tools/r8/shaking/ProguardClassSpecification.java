// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.position.TextPosition;
import com.android.tools.r8.position.TextRange;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class ProguardClassSpecification {

  public abstract static class
  Builder<C extends ProguardClassSpecification, B extends Builder<C, B>> {

    protected Origin origin;
    protected Position start;
    protected Position end;
    protected String source;
    protected ProguardTypeMatcher classAnnotation;
    protected ProguardAccessFlags classAccessFlags = new ProguardAccessFlags();
    protected ProguardAccessFlags negatedClassAccessFlags = new ProguardAccessFlags();
    protected boolean classTypeNegated = false;
    protected ProguardClassType classType = ProguardClassType.UNSPECIFIED;
    protected ProguardClassNameList classNames;
    protected ProguardTypeMatcher inheritanceAnnotation;
    protected ProguardTypeMatcher inheritanceClassName;
    protected boolean inheritanceIsExtends = false;
    protected List<ProguardMemberRule> memberRules = new LinkedList<>();

    protected Builder() {
      this(Origin.unknown(), Position.UNKNOWN);
    }

    protected Builder(Origin origin, Position start) {
      this.origin = origin;
      this.start = start;
    }

    public abstract C build();

    public abstract B self();

    public B setOrigin(Origin origin) {
      this.origin = origin;
      return self();
    }

    public B setStart(Position start) {
      this.start = start;
      return self();
    }

    public B setEnd(Position end) {
      this.end = end;
      return self();
    }

    public B setSource(String source) {
      this.source = source;
      return self();
    }

    public Position getPosition() {
      if (start == null) {
        return Position.UNKNOWN;
      }
      if (end == null || !((start instanceof TextPosition) && (end instanceof TextPosition))) {
        return start;
      }
      return new TextRange((TextPosition) start, (TextPosition) end);
    }

    public List<ProguardMemberRule> getMemberRules() {
      return memberRules;
    }

    public B setMemberRules(List<ProguardMemberRule> memberRules) {
      this.memberRules = memberRules;
      return self();
    }

    public boolean getInheritanceIsExtends() {
      return inheritanceIsExtends;
    }

    public void setInheritanceIsExtends(boolean inheritanceIsExtends) {
      this.inheritanceIsExtends = inheritanceIsExtends;
    }

    public boolean hasInheritanceClassName() {
      return inheritanceClassName != null;
    }

    public ProguardTypeMatcher getInheritanceClassName() {
      return inheritanceClassName;
    }

    public void setInheritanceClassName(ProguardTypeMatcher inheritanceClassName) {
      this.inheritanceClassName = inheritanceClassName;
    }

    public ProguardTypeMatcher getInheritanceAnnotation() {
      return inheritanceAnnotation;
    }

    public void setInheritanceAnnotation(ProguardTypeMatcher inheritanceAnnotation) {
      this.inheritanceAnnotation = inheritanceAnnotation;
    }

    public ProguardClassNameList getClassNames() {
      return classNames;
    }

    public B setClassNames(ProguardClassNameList classNames) {
      this.classNames = classNames;
      return self();
    }

    public ProguardClassType getClassType() {
      return classType;
    }

    public B setClassType(ProguardClassType classType) {
      this.classType = classType;
      return self();
    }

    public boolean getClassTypeNegated() {
      return classTypeNegated;
    }

    public void setClassTypeNegated(boolean classTypeNegated) {
      this.classTypeNegated = classTypeNegated;
    }

    public ProguardAccessFlags getClassAccessFlags() {
      return classAccessFlags;
    }

    public void setClassAccessFlags(ProguardAccessFlags flags) {
      classAccessFlags = flags;
    }

    public ProguardAccessFlags getNegatedClassAccessFlags() {
      return negatedClassAccessFlags;
    }

    public void setNegatedClassAccessFlags(ProguardAccessFlags flags) {
      negatedClassAccessFlags = flags;
    }

    public ProguardTypeMatcher getClassAnnotation() {
      return classAnnotation;
    }

    public void setClassAnnotation(ProguardTypeMatcher classAnnotation) {
      this.classAnnotation = classAnnotation;
    }

    protected void matchAllSpecification() {
      setClassNames(ProguardClassNameList.singletonList(ProguardTypeMatcher.defaultAllMatcher()));
      setMemberRules(ImmutableList.of(ProguardMemberRule.defaultKeepAllRule()));
    }
  }

  private final Origin origin;
  private final Position position;
  private final String source;
  private final ProguardTypeMatcher classAnnotation;
  private final ProguardAccessFlags classAccessFlags;
  private final ProguardAccessFlags negatedClassAccessFlags;
  private final boolean classTypeNegated;
  private final ProguardClassType classType;
  private final ProguardClassNameList classNames;
  private final ProguardTypeMatcher inheritanceAnnotation;
  private final ProguardTypeMatcher inheritanceClassName;
  private final boolean inheritanceIsExtends;
  private final List<ProguardMemberRule> memberRules;

  protected ProguardClassSpecification(
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
    assert origin != null;
    assert position != null;
    assert source != null || origin != Origin.unknown();
    this.origin = origin;
    this.position = position;
    this.source =source;
    this.classAnnotation = classAnnotation;
    this.classAccessFlags = classAccessFlags;
    this.negatedClassAccessFlags = negatedClassAccessFlags;
    this.classTypeNegated = classTypeNegated;
    this.classType = classType;
    assert classType != null;
    this.classNames = classNames;
    this.inheritanceAnnotation = inheritanceAnnotation;
    this.inheritanceClassName = inheritanceClassName;
    this.inheritanceIsExtends = inheritanceIsExtends;
    this.memberRules = memberRules;
  }

  public Origin getOrigin() {
    return origin;
  }

  public Position getPosition() {
    return position;
  }

  public String getSource() {
    return source;
  }

  public List<ProguardMemberRule> getMemberRules() {
    return memberRules;
  }

  public boolean getInheritanceIsExtends() {
    return inheritanceIsExtends;
  }

  public boolean getInheritanceIsImplements() {
    return !inheritanceIsExtends;
  }

  public boolean hasInheritanceClassName() {
    return inheritanceClassName != null;
  }

  public ProguardTypeMatcher getInheritanceClassName() {
    return inheritanceClassName;
  }

  public ProguardTypeMatcher getInheritanceAnnotation() {
    return inheritanceAnnotation;
  }

  public ProguardClassNameList getClassNames() {
    return classNames;
  }

  public ProguardClassType getClassType() {
    return classType;
  }

  public boolean getClassTypeNegated() {
    return classTypeNegated;
  }

  public ProguardAccessFlags getClassAccessFlags() {
    return classAccessFlags;
  }

  public ProguardAccessFlags getNegatedClassAccessFlags() {
    return negatedClassAccessFlags;
  }

  public ProguardTypeMatcher getClassAnnotation() {
    return classAnnotation;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProguardClassSpecification)) {
      return false;
    }
    ProguardClassSpecification that = (ProguardClassSpecification) o;

    if (classTypeNegated != that.classTypeNegated) {
      return false;
    }
    if (inheritanceIsExtends != that.inheritanceIsExtends) {
      return false;
    }
    if (!Objects.equals(classAnnotation, that.classAnnotation)) {
      return false;
    }
    if (!classAccessFlags.equals(that.classAccessFlags)) {
      return false;
    }
    if (!negatedClassAccessFlags.equals(that.negatedClassAccessFlags)) {
      return false;
    }
    if (classType != that.classType) {
      return false;
    }
    if (!classNames.equals(that.classNames)) {
      return false;
    }
    if (!Objects.equals(inheritanceAnnotation, that.inheritanceAnnotation)) {
      return false;
    }
    if (!Objects.equals(inheritanceClassName, that.inheritanceClassName)) {
      return false;
    }
    return memberRules.equals(that.memberRules);
  }

  @Override
  public int hashCode() {
    // Used multiplier 3 to avoid too much overflow when computing hashCode.
    int result = (classAnnotation != null ? classAnnotation.hashCode() : 0);
    result = 3 * result + classAccessFlags.hashCode();
    result = 3 * result + negatedClassAccessFlags.hashCode();
    result = 3 * result + (classTypeNegated ? 1 : 0);
    result = 3 * result + (classType != null ? classType.hashCode() : 0);
    result = 3 * result + classNames.hashCode();
    result = 3 * result + (inheritanceAnnotation != null ? inheritanceAnnotation.hashCode() : 0);
    result = 3 * result + (inheritanceClassName != null ? inheritanceClassName.hashCode() : 0);
    result = 3 * result + (inheritanceIsExtends ? 1 : 0);
    result = 3 * result + memberRules.hashCode();
    return result;
  }

  protected StringBuilder append(StringBuilder builder, boolean includeMemberRules) {
    boolean needsSpaceBeforeClassType =
        StringUtils.appendNonEmpty(builder, "@", classAnnotation, null)
            | StringUtils.appendNonEmpty(builder, "", classAccessFlags, null)
            | StringUtils.appendNonEmpty(
                builder, "!", negatedClassAccessFlags.toString().replace(" ", " !"), null);
    if (needsSpaceBeforeClassType) {
      builder.append(' ');
    }
    if (classTypeNegated) {
      builder.append('!');
    }
    builder.append(classType);
    builder.append(' ');
    classNames.writeTo(builder);
    if (hasInheritanceClassName()) {
      builder.append(' ').append(inheritanceIsExtends ? "extends" : "implements");
      StringUtils.appendNonEmpty(builder, "@", inheritanceAnnotation, null);
      builder.append(' ');
      builder.append(inheritanceClassName);
    }
    if (includeMemberRules && !memberRules.isEmpty()) {
      builder.append(" {").append(System.lineSeparator());
      memberRules.forEach(memberRule -> {
        builder.append("  ");
        builder.append(memberRule);
        builder.append(";").append(System.lineSeparator());
      });
      builder.append("}");
    }
    return builder;
  }

  /**
   * Short String representation without member rules.
   */
  public String toShortString() {
    return append(new StringBuilder(), false).toString();
  }

  @Override
  public String toString() {
    return append(new StringBuilder(), true).toString();
  }
}
