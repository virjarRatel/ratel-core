// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.retrace;

import com.android.tools.r8.Keep;
import com.android.tools.r8.naming.ClassNamingForNameMapper;
import com.android.tools.r8.naming.ClassNamingForNameMapper.MappedRangesOfName;
import com.android.tools.r8.naming.MemberNaming;
import com.android.tools.r8.references.ClassReference;
import com.android.tools.r8.references.Reference;
import com.android.tools.r8.retrace.RetraceClassResult.Element;
import com.android.tools.r8.utils.Box;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Keep
public class RetraceClassResult extends Result<Element, RetraceClassResult> {

  private final ClassReference obfuscatedReference;
  private final ClassNamingForNameMapper mapper;

  private RetraceClassResult(ClassReference obfuscatedReference, ClassNamingForNameMapper mapper) {
    this.obfuscatedReference = obfuscatedReference;
    this.mapper = mapper;
  }

  static RetraceClassResult create(
      ClassReference obfuscatedReference, ClassNamingForNameMapper mapper) {
    return new RetraceClassResult(obfuscatedReference, mapper);
  }

  public RetraceFieldResult lookupField(String fieldName) {
    return lookup(
        fieldName,
        (mapper, name) -> {
          List<MemberNaming> memberNamings = mapper.mappedNamingsByName.get(name);
          if (memberNamings == null || memberNamings.isEmpty()) {
            return null;
          }
          return memberNamings;
        },
        RetraceFieldResult::new);
  }

  public RetraceMethodResult lookupMethod(String methodName) {
    return lookup(
        methodName,
        (mapper, name) -> {
          MappedRangesOfName mappedRanges = mapper.mappedRangesByRenamedName.get(name);
          if (mappedRanges == null || mappedRanges.getMappedRanges().isEmpty()) {
            return null;
          }
          return mappedRanges;
        },
        RetraceMethodResult::new);
  }

  private <T, R> R lookup(
      String name,
      BiFunction<ClassNamingForNameMapper, String, T> lookupFunction,
      ResultConstructor<T, R> constructor) {
    Box<R> elementBox = new Box<>();
    forEach(
        element -> {
          assert !elementBox.isSet();
          T mappedRangesForT = null;
          if (element.mapper != null) {
            mappedRangesForT = lookupFunction.apply(element.mapper, name);
          }
          elementBox.set(constructor.create(element, mappedRangesForT, name));
        });
    return elementBox.get();
  }

  private boolean hasRetraceResult() {
    return mapper != null;
  }

  Stream<Element> stream() {
    return Stream.of(
        new Element(
            this,
            mapper == null ? obfuscatedReference : Reference.classFromTypeName(mapper.originalName),
            mapper));
  }

  @Override
  public RetraceClassResult forEach(Consumer<Element> resultConsumer) {
    stream().forEach(resultConsumer);
    return this;
  }

  private interface ResultConstructor<T, R> {
    R create(Element element, T mappings, String obfuscatedName);
  }

  public static class Element {

    private final RetraceClassResult classResult;
    private final ClassReference classReference;
    private final ClassNamingForNameMapper mapper;

    public Element(
        RetraceClassResult classResult,
        ClassReference classReference,
        ClassNamingForNameMapper mapper) {
      this.classResult = classResult;
      this.classReference = classReference;
      this.mapper = mapper;
    }

    public ClassReference getClassReference() {
      return classReference;
    }

    public RetraceClassResult getRetraceClassResult() {
      return classResult;
    }

    public String retraceSourceFile(String fileName, RetraceBase retraceBase) {
      return retraceBase.retraceSourceFile(
          classResult.obfuscatedReference, fileName, classReference, mapper != null);
    }

    public RetraceFieldResult lookupField(String fieldName) {
      return lookup(
          fieldName,
          (mapper, name) -> {
            List<MemberNaming> memberNamings = mapper.mappedNamingsByName.get(name);
            if (memberNamings == null || memberNamings.isEmpty()) {
              return null;
            }
            return memberNamings;
          },
          RetraceFieldResult::new);
    }

    public RetraceMethodResult lookupMethod(String methodName) {
      return lookup(
          methodName,
          (mapper, name) -> {
            MappedRangesOfName mappedRanges = mapper.mappedRangesByRenamedName.get(name);
            if (mappedRanges == null || mappedRanges.getMappedRanges().isEmpty()) {
              return null;
            }
            return mappedRanges;
          },
          RetraceMethodResult::new);
    }

    private <T, R> R lookup(
        String name,
        BiFunction<ClassNamingForNameMapper, String, T> lookupFunction,
        ResultConstructor<T, R> constructor) {
      return constructor.create(
          this, mapper != null ? lookupFunction.apply(mapper, name) : null, name);
    }
  }
}
