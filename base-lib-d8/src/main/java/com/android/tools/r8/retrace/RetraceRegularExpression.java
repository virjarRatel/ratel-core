// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.references.MethodReference;
import com.android.tools.r8.references.Reference;
import com.android.tools.r8.references.TypeReference;
import com.android.tools.r8.retrace.RetraceClassResult.Element;
import com.android.tools.r8.retrace.RetraceRegularExpression.RetraceString.RetraceStringBuilder;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RetraceRegularExpression {

  private final RetraceBase retraceBase;
  private final List<String> stackTrace;
  private final DiagnosticsHandler diagnosticsHandler;
  private final String regularExpression;

  private static final int NO_MATCH = -1;

  private final RegularExpressionGroup[] groups =
      new RegularExpressionGroup[] {
        new TypeNameGroup(),
        new BinaryNameGroup(),
        new MethodNameGroup(),
        new FieldNameGroup(),
        new SourceFileGroup(),
        new LineNumberGroup(),
        new FieldOrReturnTypeGroup(),
        new MethodArgumentsGroup()
      };

  private static final String CAPTURE_GROUP_PREFIX = "captureGroup";

  RetraceRegularExpression(
      RetraceBase retraceBase,
      List<String> stackTrace,
      DiagnosticsHandler diagnosticsHandler,
      String regularExpression) {
    this.retraceBase = retraceBase;
    this.stackTrace = stackTrace;
    this.diagnosticsHandler = diagnosticsHandler;
    this.regularExpression = regularExpression;
  }

  public RetraceCommandLineResult retrace() {
    List<RegularExpressionGroupHandler> handlers = new ArrayList<>();
    String regularExpression = registerGroups(this.regularExpression, handlers);
    Pattern compiledPattern = Pattern.compile(regularExpression);
    List<String> result = new ArrayList<>();
    for (String string : stackTrace) {
      Matcher matcher = compiledPattern.matcher(string);
      List<RetraceString> retracedStrings =
          Lists.newArrayList(RetraceStringBuilder.create(string).build());
      if (matcher.matches()) {
        for (RegularExpressionGroupHandler handler : handlers) {
          retracedStrings = handler.handleMatch(retracedStrings, matcher, retraceBase);
        }
      }
      if (retracedStrings.isEmpty()) {
        // We could not find a match. Output the identity.
        result.add(string);
      } else {
        for (RetraceString retracedString : retracedStrings) {
          result.add(retracedString.getRetracedString());
        }
      }
    }
    return new RetraceCommandLineResult(result);
  }

  private String registerGroups(
      String regularExpression, List<RegularExpressionGroupHandler> handlers) {
    int currentIndex = 0;
    int captureGroupIndex = 0;
    while (currentIndex < regularExpression.length()) {
      RegularExpressionGroup firstGroup = null;
      int firstIndexFromCurrent = regularExpression.length();
      for (RegularExpressionGroup group : groups) {
        int nextIndexOf = regularExpression.indexOf(group.shortName(), currentIndex);
        if (nextIndexOf > NO_MATCH && nextIndexOf < firstIndexFromCurrent) {
          // Check if previous character in the regular expression is not \\ to ensure not
          // overriding a matching on shortName.
          if (nextIndexOf > 0 && regularExpression.charAt(nextIndexOf - 1) == '\\') {
            continue;
          }
          firstGroup = group;
          firstIndexFromCurrent = nextIndexOf;
        }
      }
      if (firstGroup != null) {
        String captureGroupName = CAPTURE_GROUP_PREFIX + (captureGroupIndex++);
        String patternToInsert = "(?<" + captureGroupName + ">" + firstGroup.subExpression() + ")";
        regularExpression =
            regularExpression.substring(0, firstIndexFromCurrent)
                + patternToInsert
                + regularExpression.substring(
                    firstIndexFromCurrent + firstGroup.shortName().length());
        handlers.add(firstGroup.createHandler(captureGroupName));
        firstIndexFromCurrent += patternToInsert.length();
      }
      currentIndex = firstIndexFromCurrent;
    }
    return regularExpression;
  }

  static class RetraceString {

    private final Element classContext;
    private final ClassNameGroup classNameGroup;
    private final ClassReference qualifiedContext;
    private final RetraceMethodResult.Element methodContext;
    private final TypeReference typeOrReturnTypeContext;
    private final boolean hasTypeOrReturnTypeContext;
    private final String retracedString;
    private final int adjustedIndex;

    private RetraceString(
        Element classContext,
        ClassNameGroup classNameGroup,
        ClassReference qualifiedContext,
        RetraceMethodResult.Element methodContext,
        TypeReference typeOrReturnTypeContext,
        boolean hasTypeOrReturnTypeContext,
        String retracedString,
        int adjustedIndex) {
      this.classContext = classContext;
      this.classNameGroup = classNameGroup;
      this.qualifiedContext = qualifiedContext;
      this.methodContext = methodContext;
      this.typeOrReturnTypeContext = typeOrReturnTypeContext;
      this.hasTypeOrReturnTypeContext = hasTypeOrReturnTypeContext;
      this.retracedString = retracedString;
      this.adjustedIndex = adjustedIndex;
    }

    String getRetracedString() {
      return retracedString;
    }

    boolean hasTypeOrReturnTypeContext() {
      return hasTypeOrReturnTypeContext;
    }

    Element getClassContext() {
      return classContext;
    }

    RetraceMethodResult.Element getMethodContext() {
      return methodContext;
    }

    TypeReference getTypeOrReturnTypeContext() {
      return typeOrReturnTypeContext;
    }

    public ClassReference getQualifiedContext() {
      return qualifiedContext;
    }

    RetraceStringBuilder transform() {
      return RetraceStringBuilder.create(this);
    }

    static class RetraceStringBuilder {

      private Element classContext;
      private ClassNameGroup classNameGroup;
      private ClassReference qualifiedContext;
      private RetraceMethodResult.Element methodContext;
      private TypeReference typeOrReturnTypeContext;
      private boolean hasTypeOrReturnTypeContext;
      private String retracedString;
      private int adjustedIndex;

      private int maxReplaceStringIndex = NO_MATCH;

      private RetraceStringBuilder(
          Element classContext,
          ClassNameGroup classNameGroup,
          ClassReference qualifiedContext,
          RetraceMethodResult.Element methodContext,
          TypeReference typeOrReturnTypeContext,
          boolean hasTypeOrReturnTypeContext,
          String retracedString,
          int adjustedIndex) {
        this.classContext = classContext;
        this.classNameGroup = classNameGroup;
        this.qualifiedContext = qualifiedContext;
        this.methodContext = methodContext;
        this.typeOrReturnTypeContext = typeOrReturnTypeContext;
        this.hasTypeOrReturnTypeContext = hasTypeOrReturnTypeContext;
        this.retracedString = retracedString;
        this.adjustedIndex = adjustedIndex;
      }

      static RetraceStringBuilder create(String string) {
        return new RetraceStringBuilder(null, null, null, null, null, false, string, 0);
      }

      static RetraceStringBuilder create(RetraceString string) {
        return new RetraceStringBuilder(
            string.classContext,
            string.classNameGroup,
            string.qualifiedContext,
            string.methodContext,
            string.typeOrReturnTypeContext,
            string.hasTypeOrReturnTypeContext,
            string.retracedString,
            string.adjustedIndex);
      }

      RetraceStringBuilder setClassContext(Element classContext, ClassNameGroup classNameGroup) {
        this.classContext = classContext;
        this.classNameGroup = classNameGroup;
        return this;
      }

      RetraceStringBuilder setMethodContext(RetraceMethodResult.Element methodContext) {
        this.methodContext = methodContext;
        return this;
      }

      RetraceStringBuilder setTypeOrReturnTypeContext(TypeReference typeOrReturnTypeContext) {
        hasTypeOrReturnTypeContext = true;
        this.typeOrReturnTypeContext = typeOrReturnTypeContext;
        return this;
      }

      RetraceStringBuilder setQualifiedContext(ClassReference qualifiedContext) {
        this.qualifiedContext = qualifiedContext;
        return this;
      }

      RetraceStringBuilder replaceInString(String oldString, String newString) {
        int oldStringStartIndex = retracedString.indexOf(oldString);
        assert oldStringStartIndex > NO_MATCH;
        int oldStringEndIndex = oldStringStartIndex + oldString.length();
        return replaceInStringRaw(newString, oldStringStartIndex, oldStringEndIndex);
      }

      RetraceStringBuilder replaceInString(String newString, int originalFrom, int originalTo) {
        return replaceInStringRaw(
            newString, originalFrom + adjustedIndex, originalTo + adjustedIndex);
      }

      RetraceStringBuilder replaceInStringRaw(String newString, int from, int to) {
        assert from <= to;
        assert from > maxReplaceStringIndex;
        String prefix = retracedString.substring(0, from);
        String postFix = retracedString.substring(to);
        this.retracedString = prefix + newString + postFix;
        this.adjustedIndex = adjustedIndex + newString.length() - (to - from);
        maxReplaceStringIndex = prefix.length() + newString.length();
        return this;
      }

      RetraceString build() {
        return new RetraceString(
            classContext,
            classNameGroup,
            qualifiedContext,
            methodContext,
            typeOrReturnTypeContext,
            hasTypeOrReturnTypeContext,
            retracedString,
            adjustedIndex);
      }
    }
  }

  private interface RegularExpressionGroupHandler {

    List<RetraceString> handleMatch(
        List<RetraceString> strings, Matcher matcher, RetraceBase retraceBase);
  }

  private abstract static class RegularExpressionGroup {

    abstract String shortName();

    abstract String subExpression();

    abstract RegularExpressionGroupHandler createHandler(String captureGroup);
  }

  // TODO(b/145731185): Extend support for identifiers with strings inside back ticks.
  private static final String javaIdentifierSegment = "[\\p{L}\\p{N}_\\p{Sc}]+";

  private abstract static class ClassNameGroup extends RegularExpressionGroup {

    abstract String getClassName(ClassReference classReference);

    abstract ClassReference classFromMatch(String match);

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String typeName = matcher.group(captureGroup);
        RetraceClassResult retraceResult = retraceBase.retrace(classFromMatch(typeName));
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          retraceResult.forEach(
              element -> {
                retracedStrings.add(
                    retraceString
                        .transform()
                        .setClassContext(element, this)
                        .setMethodContext(null)
                        .replaceInString(
                            getClassName(element.getClassReference()),
                            matcher.start(captureGroup),
                            matcher.end(captureGroup))
                        .build());
              });
        }
        return retracedStrings;
      };
    }
  }

  private static class TypeNameGroup extends ClassNameGroup {

    @Override
    String shortName() {
      return "%c";
    }

    @Override
    String subExpression() {
      return "(" + javaIdentifierSegment + "\\.)*" + javaIdentifierSegment;
    }

    @Override
    String getClassName(ClassReference classReference) {
      return classReference.getTypeName();
    }

    @Override
    ClassReference classFromMatch(String match) {
      return Reference.classFromTypeName(match);
    }
  }

  private static class BinaryNameGroup extends ClassNameGroup {

    @Override
    String shortName() {
      return "%C";
    }

    @Override
    String subExpression() {
      return "(?:" + javaIdentifierSegment + "\\/)*" + javaIdentifierSegment;
    }

    @Override
    String getClassName(ClassReference classReference) {
      return classReference.getBinaryName();
    }

    @Override
    ClassReference classFromMatch(String match) {
      return Reference.classFromBinaryName(match);
    }
  }

  private static class MethodNameGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%m";
    }

    @Override
    String subExpression() {
      return javaIdentifierSegment;
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String methodName = matcher.group(captureGroup);
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          if (retraceString.classContext == null) {
            retracedStrings.add(retraceString);
            continue;
          }
          retraceString
              .getClassContext()
              .lookupMethod(methodName)
              .forEach(
                  element -> {
                    MethodReference methodReference = element.getMethodReference();
                    if (retraceString.hasTypeOrReturnTypeContext()) {
                      if (methodReference.getReturnType() == null
                          && retraceString.getTypeOrReturnTypeContext() != null) {
                        return;
                      } else if (methodReference.getReturnType() != null
                          && !methodReference
                              .getReturnType()
                              .equals(retraceString.getTypeOrReturnTypeContext())) {
                        return;
                      }
                    }
                    RetraceStringBuilder newRetraceString = retraceString.transform();
                    ClassReference existingClass =
                        retraceString.getClassContext().getClassReference();
                    ClassReference holder = methodReference.getHolderClass();
                    if (holder != existingClass) {
                      // The element is defined on another holder.
                      newRetraceString
                          .replaceInString(
                              newRetraceString.classNameGroup.getClassName(existingClass),
                              newRetraceString.classNameGroup.getClassName(holder))
                          .setQualifiedContext(holder);
                    }
                    newRetraceString
                        .setMethodContext(element)
                        .replaceInString(
                            methodReference.getMethodName(),
                            matcher.start(captureGroup),
                            matcher.end(captureGroup));
                    retracedStrings.add(newRetraceString.build());
                  });
        }
        return retracedStrings;
      };
    }
  }

  private static class FieldNameGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%f";
    }

    @Override
    String subExpression() {
      return javaIdentifierSegment;
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String methodName = matcher.group(captureGroup);
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          if (retraceString.getClassContext() == null) {
            retracedStrings.add(retraceString);
            continue;
          }
          retraceString
              .getClassContext()
              .lookupField(methodName)
              .forEach(
                  element -> {
                    RetraceStringBuilder newRetraceString = retraceString.transform();
                    ClassReference existingClass =
                        retraceString.getClassContext().getClassReference();
                    ClassReference holder = element.getFieldReference().getHolderClass();
                    if (holder != existingClass) {
                      // The element is defined on another holder.
                      newRetraceString
                          .replaceInString(
                              newRetraceString.classNameGroup.getClassName(existingClass),
                              newRetraceString.classNameGroup.getClassName(holder))
                          .setQualifiedContext(holder);
                    }
                    newRetraceString.replaceInString(
                        element.getFieldReference().getFieldName(),
                        matcher.start(captureGroup),
                        matcher.end(captureGroup));
                    retracedStrings.add(newRetraceString.build());
                  });
        }
        return retracedStrings;
      };
    }
  }

  private static class SourceFileGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%s";
    }

    @Override
    String subExpression() {
      return "(?:\\w+\\.)*\\w+";
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String fileName = matcher.group(captureGroup);
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          if (retraceString.classContext == null) {
            retracedStrings.add(retraceString);
            continue;
          }
          String newSourceFile =
              retraceString.getQualifiedContext() != null
                  ? retraceBase.retraceSourceFile(
                      retraceString.classContext.getClassReference(),
                      fileName,
                      retraceString.getQualifiedContext(),
                      true)
                  : retraceString.classContext.retraceSourceFile(fileName, retraceBase);
          retracedStrings.add(
              retraceString
                  .transform()
                  .replaceInString(
                      newSourceFile, matcher.start(captureGroup), matcher.end(captureGroup))
                  .build());
        }
        return retracedStrings;
      };
    }
  }

  private class LineNumberGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%l";
    }

    @Override
    String subExpression() {
      return "\\d*";
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String lineNumberAsString = matcher.group(captureGroup);
        int lineNumber =
            lineNumberAsString.isEmpty() ? NO_MATCH : Integer.parseInt(lineNumberAsString);
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          RetraceMethodResult.Element methodContext = retraceString.methodContext;
          if (methodContext == null) {
            retracedStrings.add(retraceString);
            continue;
          }
          Set<MethodReference> narrowedSet =
              methodContext.getRetraceMethodResult().narrowByLine(lineNumber).stream()
                  .map(RetraceMethodResult.Element::getMethodReference)
                  .collect(Collectors.toSet());
          if (!narrowedSet.contains(methodContext.getMethodReference())) {
            // Prune the retraceString since we now have line number information and this is not
            // a part of the result.
            diagnosticsHandler.info(
                new StringDiagnostic(
                    "Pruning "
                        + retraceString.getRetracedString()
                        + " from result because line number "
                        + lineNumber
                        + " does not match."));
            continue;
          }
          String newLineNumber =
              lineNumber > NO_MATCH
                  ? methodContext.getOriginalLineNumber(lineNumber) + ""
                  : lineNumberAsString;
          retracedStrings.add(
              retraceString
                  .transform()
                  .replaceInString(
                      newLineNumber, matcher.start(captureGroup), matcher.end(captureGroup))
                  .build());
        }
        return retracedStrings;
      };
    }
  }

  private static final String JAVA_TYPE_REGULAR_EXPRESSION =
      "(" + javaIdentifierSegment + "\\.)*" + javaIdentifierSegment + "[\\[\\]]*";

  private static class FieldOrReturnTypeGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%t";
    }

    @Override
    String subExpression() {
      return JAVA_TYPE_REGULAR_EXPRESSION;
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        String typeName = matcher.group(captureGroup);
        String descriptor = DescriptorUtils.javaTypeToDescriptor(typeName);
        if (!DescriptorUtils.isDescriptor(descriptor) && !"V".equals(descriptor)) {
          return strings;
        }
        TypeReference typeReference = Reference.returnTypeFromDescriptor(descriptor);
        List<RetraceString> retracedStrings = new ArrayList<>();
        RetraceTypeResult retracedType = retraceBase.retrace(typeReference);
        for (RetraceString retraceString : strings) {
          retracedType.forEach(
              element -> {
                TypeReference retracedReference = element.getTypeReference();
                retracedStrings.add(
                    retraceString
                        .transform()
                        .setTypeOrReturnTypeContext(retracedReference)
                        .replaceInString(
                            retracedReference == null ? "void" : retracedReference.getTypeName(),
                            matcher.start(captureGroup),
                            matcher.end(captureGroup))
                        .build());
              });
        }
        return retracedStrings;
      };
    }
  }

  private class MethodArgumentsGroup extends RegularExpressionGroup {

    @Override
    String shortName() {
      return "%a";
    }

    @Override
    String subExpression() {
      return "((" + JAVA_TYPE_REGULAR_EXPRESSION + "\\,)*" + JAVA_TYPE_REGULAR_EXPRESSION + ")?";
    }

    @Override
    RegularExpressionGroupHandler createHandler(String captureGroup) {
      return (strings, matcher, retraceBase) -> {
        if (matcher.start(captureGroup) == NO_MATCH) {
          return strings;
        }
        Set<List<TypeReference>> initialValue = new LinkedHashSet<>();
        initialValue.add(new ArrayList<>());
        Set<List<TypeReference>> allRetracedReferences =
            Arrays.stream(matcher.group(captureGroup).split(","))
                .map(String::trim)
                .reduce(
                    initialValue,
                    (acc, typeName) -> {
                      String descriptor = DescriptorUtils.javaTypeToDescriptor(typeName);
                      if (!DescriptorUtils.isDescriptor(descriptor) && !"V".equals(descriptor)) {
                        return acc;
                      }
                      TypeReference typeReference = Reference.returnTypeFromDescriptor(descriptor);
                      Set<List<TypeReference>> retracedTypes = new LinkedHashSet<>();
                      retraceBase
                          .retrace(typeReference)
                          .forEach(
                              element -> {
                                for (List<TypeReference> currentReferences : acc) {
                                  ArrayList<TypeReference> newList =
                                      new ArrayList<>(currentReferences);
                                  newList.add(element.getTypeReference());
                                  retracedTypes.add(newList);
                                }
                              });
                      return retracedTypes;
                    },
                    (l1, l2) -> {
                      l1.addAll(l2);
                      return l1;
                    });
        List<RetraceString> retracedStrings = new ArrayList<>();
        for (RetraceString retraceString : strings) {
          if (retraceString.getMethodContext() != null
              && !allRetracedReferences.contains(
                  retraceString.getMethodContext().getMethodReference().getFormalTypes())) {
            // Prune the string since we now know the formals.
            String formals =
                retraceString.getMethodContext().getMethodReference().getFormalTypes().stream()
                    .map(TypeReference::getTypeName)
                    .collect(Collectors.joining(","));
            diagnosticsHandler.info(
                new StringDiagnostic(
                    "Pruning "
                        + retraceString.getRetracedString()
                        + " from result because formals ("
                        + formals
                        + ") do not match result set."));
            continue;
          }
          for (List<TypeReference> retracedReferences : allRetracedReferences) {
            retracedStrings.add(
                retraceString
                    .transform()
                    .replaceInString(
                        retracedReferences.stream()
                            .map(TypeReference::getTypeName)
                            .collect(Collectors.joining(",")),
                        matcher.start(captureGroup),
                        matcher.end(captureGroup))
                    .build());
          }
        }
        return retracedStrings;
      };
    }
  }
}
