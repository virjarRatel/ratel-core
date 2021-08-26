// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.Keep;
import com.android.tools.r8.naming.ClassNamingForNameMapper.MappedRange;
import com.android.tools.r8.naming.ClassNamingForNameMapper.MappedRangesOfName;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.Range;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.references.MethodReference;
import com.android.tools.r8.references.MethodReference.UnknownMethodReference;
import com.android.tools.r8.references.Reference;
import com.android.tools.r8.references.TypeReference;
import com.android.tools.r8.utils.DescriptorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Keep
public class RetraceMethodResult extends Result<RetraceMethodResult.Element, RetraceMethodResult> {

  private final String obfuscatedName;
  private final RetraceClassResult.Element classElement;
  private final MappedRangesOfName mappedRanges;
  private Boolean isAmbiguousCached = null;

  RetraceMethodResult(
      RetraceClassResult.Element classElement,
      MappedRangesOfName mappedRanges,
      String obfuscatedName) {
    this.classElement = classElement;
    this.mappedRanges = mappedRanges;
    this.obfuscatedName = obfuscatedName;
    assert classElement != null;
  }

  public UnknownMethodReference getUnknownReference() {
    return new UnknownMethodReference(classElement.getClassReference(), obfuscatedName);
  }

  private boolean hasRetraceResult() {
    return mappedRanges != null && mappedRanges.getMappedRanges().size() > 0;
  }

  public boolean isAmbiguous() {
    if (isAmbiguousCached != null) {
      return isAmbiguousCached;
    }
    if (!hasRetraceResult()) {
      return false;
    }
    assert mappedRanges != null;
    Range minifiedRange = null;
    boolean seenNull = false;
    for (MappedRange mappedRange : mappedRanges.getMappedRanges()) {
      if (minifiedRange != null && !minifiedRange.equals(mappedRange.minifiedRange)) {
        isAmbiguousCached = true;
        return true;
      } else if (minifiedRange == null) {
        if (seenNull) {
          isAmbiguousCached = true;
          return true;
        }
        seenNull = true;
      }
      minifiedRange = mappedRange.minifiedRange;
    }
    isAmbiguousCached = false;
    return false;
  }

  public RetraceMethodResult narrowByLine(int linePosition) {
    if (!hasRetraceResult()) {
      return this;
    }
    List<MappedRange> narrowedRanges = this.mappedRanges.allRangesForLine(linePosition, false);
    if (narrowedRanges.isEmpty()) {
      narrowedRanges = new ArrayList<>();
      for (MappedRange mappedRange : this.mappedRanges.getMappedRanges()) {
        if (mappedRange.minifiedRange == null) {
          narrowedRanges.add(mappedRange);
        }
      }
    }
    return new RetraceMethodResult(
        classElement, new MappedRangesOfName(narrowedRanges), obfuscatedName);
  }

  Stream<Element> stream() {
    if (!hasRetraceResult()) {
      return Stream.of(new Element(this, classElement, getUnknownReference(), null));
    }
    return mappedRanges.getMappedRanges().stream()
        .map(
            mappedRange -> {
              MethodSignature signature = mappedRange.signature;
              ClassReference holder =
                  mappedRange.signature.isQualified()
                      ? Reference.classFromDescriptor(
                          DescriptorUtils.javaTypeToDescriptor(
                              mappedRange.signature.toHolderFromQualified()))
                      : classElement.getClassReference();
              List<TypeReference> formalTypes = new ArrayList<>(signature.parameters.length);
              for (String parameter : signature.parameters) {
                formalTypes.add(Reference.typeFromTypeName(parameter));
              }
              TypeReference returnType =
                  Reference.returnTypeFromDescriptor(
                      DescriptorUtils.javaTypeToDescriptor(signature.type));
              MethodReference retracedMethod =
                  Reference.method(
                      holder,
                      signature.isQualified() ? signature.toUnqualifiedName() : signature.name,
                      formalTypes,
                      returnType);
              return new Element(this, classElement, retracedMethod, mappedRange);
            });
  }

  @Override
  public RetraceMethodResult forEach(Consumer<Element> resultConsumer) {
    stream().forEach(resultConsumer);
    return this;
  }

  public static class Element {

    private final MethodReference methodReference;
    private final RetraceMethodResult retraceMethodResult;
    private final RetraceClassResult.Element classElement;
    private final MappedRange mappedRange;

    private Element(
        RetraceMethodResult retraceMethodResult,
        RetraceClassResult.Element classElement,
        MethodReference methodReference,
        MappedRange mappedRange) {
      this.classElement = classElement;
      this.retraceMethodResult = retraceMethodResult;
      this.methodReference = methodReference;
      this.mappedRange = mappedRange;
    }

    public MethodReference getMethodReference() {
      return methodReference;
    }

    public RetraceMethodResult getRetraceMethodResult() {
      return retraceMethodResult;
    }

    public RetraceClassResult.Element getClassElement() {
      return classElement;
    }

    public int getOriginalLineNumber(int linePosition) {
      return mappedRange != null ? mappedRange.getOriginalLineNumber(linePosition) : linePosition;
    }
  }
}
