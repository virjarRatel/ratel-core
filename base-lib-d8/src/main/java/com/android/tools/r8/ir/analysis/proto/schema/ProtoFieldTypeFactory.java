// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

public class ProtoFieldTypeFactory {

  private Int2ReferenceMap<ProtoFieldType> fieldTypes = new Int2ReferenceOpenHashMap<>();

  public ProtoFieldType createField(int fieldTypeIdWithExtraBits) {
    ProtoFieldType result = fieldTypes.get(fieldTypeIdWithExtraBits);
    if (result == null) {
      result = ProtoFieldType.fromFieldIdWithExtraBits(fieldTypeIdWithExtraBits);
      fieldTypes.put(fieldTypeIdWithExtraBits, result);
    }
    return result;
  }
}
