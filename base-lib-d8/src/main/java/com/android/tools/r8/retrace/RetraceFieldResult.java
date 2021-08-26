// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.Keep;
import com.android.tools.r8.naming.MemberNaming;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.references.FieldReference;
import com.android.tools.r8.references.FieldReference.UnknownFieldReference;
import com.android.tools.r8.references.Reference;
import com.android.tools.r8.utils.DescriptorUtils;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Keep
public class RetraceFieldResult extends Result<RetraceFieldResult.Element, RetraceFieldResult> {

  private final RetraceClassResult.Element classElement;
  private final List<MemberNaming> memberNamings;
  private final String obfuscatedName;

  RetraceFieldResult(
      RetraceClassResult.Element classElement,
      List<MemberNaming> memberNamings,
      String obfuscatedName) {
    this.classElement = classElement;
    this.memberNamings = memberNamings;
    this.obfuscatedName = obfuscatedName;
    assert classElement != null;
    assert memberNamings == null
        || (!memberNamings.isEmpty() && memberNamings.stream().allMatch(Objects::nonNull));
  }

  private boolean hasRetraceResult() {
    return memberNamings != null;
  }

  private boolean isAmbiguous() {
    if (!hasRetraceResult()) {
      return false;
    }
    assert memberNamings != null;
    return memberNamings.size() > 1;
  }

  Stream<Element> stream() {
    if (!hasRetraceResult()) {
      return Stream.of(
          new Element(
              this,
              classElement,
              new UnknownFieldReference(classElement.getClassReference(), obfuscatedName)));
    }
    assert !memberNamings.isEmpty();
    return memberNamings.stream()
        .map(
            memberNaming -> {
              assert memberNaming.isFieldNaming();
              FieldSignature fieldSignature =
                  memberNaming.getOriginalSignature().asFieldSignature();
              ClassReference holder =
                  fieldSignature.isQualified()
                      ? Reference.classFromDescriptor(
                          DescriptorUtils.javaTypeToDescriptor(
                              fieldSignature.toHolderFromQualified()))
                      : classElement.getClassReference();
              return new Element(
                  this,
                  classElement,
                  Reference.field(
                      holder,
                      fieldSignature.isQualified()
                          ? fieldSignature.toUnqualifiedName()
                          : fieldSignature.name,
                      Reference.typeFromTypeName(fieldSignature.type)));
            });
  }

  @Override
  public RetraceFieldResult forEach(Consumer<Element> resultConsumer) {
    stream().forEach(resultConsumer);
    return this;
  }

  public static class Element {

    private final FieldReference fieldReference;
    private final RetraceFieldResult retraceFieldResult;
    private final RetraceClassResult.Element classElement;

    private Element(
        RetraceFieldResult retraceFieldResult,
        RetraceClassResult.Element classElement,
        FieldReference fieldReference) {
      this.classElement = classElement;
      this.fieldReference = fieldReference;
      this.retraceFieldResult = retraceFieldResult;
    }

    public FieldReference getFieldReference() {
      return fieldReference;
    }

    public RetraceFieldResult getRetraceFieldResult() {
      return getRetraceFieldResult();
    }

    public RetraceClassResult.Element getClassElement() {
      return classElement;
    }
  }
}
