// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class ProtoOneOfObjectPair {

  private final ProtoFieldObject oneOfObject;
  private final ProtoFieldObject oneOfCaseObject;

  public ProtoOneOfObjectPair(ProtoFieldObject oneOfObject, ProtoFieldObject oneOfCaseObject) {
    this.oneOfObject = oneOfObject;
    this.oneOfCaseObject = oneOfCaseObject;
  }

  public ProtoFieldObject getOneOfObject() {
    return oneOfObject;
  }

  public ProtoFieldObject getOneOfCaseObject() {
    return oneOfCaseObject;
  }

  public void forEach(Consumer<ProtoFieldObject> fn) {
    fn.accept(oneOfObject);
    fn.accept(oneOfCaseObject);
  }

  public Stream<ProtoFieldObject> stream() {
    return Stream.of(oneOfObject, oneOfCaseObject);
  }
}
