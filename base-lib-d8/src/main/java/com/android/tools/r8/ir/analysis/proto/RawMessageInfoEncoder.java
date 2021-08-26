// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldInfo;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldType;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoMessageInfo;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoObject;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoOneOfObjectPair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.List;

public class RawMessageInfoEncoder {

  private final DexItemFactory dexItemFactory;

  RawMessageInfoEncoder(DexItemFactory dexItemFactory) {
    this.dexItemFactory = dexItemFactory;
  }

  DexString encodeInfo(ProtoMessageInfo protoMessageInfo) {
    IntList info = new IntArrayList();
    info.add(protoMessageInfo.getFlags());
    info.add(protoMessageInfo.numberOfFields());

    if (protoMessageInfo.hasFields()) {
      int minFieldNumber = Integer.MAX_VALUE;
      int maxFieldNumber = Integer.MIN_VALUE;
      int mapFieldCount = 0;
      int repeatedFieldCount = 0;
      int checkInitialized = 0;

      for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
        int fieldNumber = protoFieldInfo.getNumber();
        if (fieldNumber < minFieldNumber) {
          minFieldNumber = fieldNumber;
        }
        if (fieldNumber > maxFieldNumber) {
          maxFieldNumber = fieldNumber;
        }
        ProtoFieldType fieldType = protoFieldInfo.getType();
        if (fieldType.id() == ProtoFieldType.MAP_ID) {
          mapFieldCount++;
        } else if (!fieldType.isSingular()) {
          repeatedFieldCount++;
        }
        if (fieldType.needsIsInitializedCheck()) {
          checkInitialized++;
        }
      }

      info.add(protoMessageInfo.numberOfOneOfObjects());
      info.add(protoMessageInfo.numberOfHasBitsObjects());
      info.add(minFieldNumber);
      info.add(maxFieldNumber);
      info.add(protoMessageInfo.numberOfFields());
      info.add(mapFieldCount);
      info.add(repeatedFieldCount);
      info.add(checkInitialized);

      for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
        info.add(protoFieldInfo.getNumber());
        info.add(protoFieldInfo.getType().serialize());
        if (protoFieldInfo.hasAuxData()) {
          info.add(protoFieldInfo.getAuxData());
        }
      }
    }

    return encodeInfo(info);
  }

  private DexString encodeInfo(IntList info) {
    byte[] result = new byte[countBytes(info)];
    int numberOfExtraChars = 0;
    int offset = 0;
    IntListIterator iterator = info.iterator();
    while (iterator.hasNext()) {
      int value = iterator.nextInt();
      while (value >= 0xD800) {
        char c = (char) (0xE000 | (value & 0x1FFF));
        offset = DexString.encodeToMutf8(c, result, offset);
        numberOfExtraChars++;
        value >>= 13;
      }
      offset = DexString.encodeToMutf8((char) value, result, offset);
    }
    result[offset] = 0;
    return dexItemFactory.createString(info.size() + numberOfExtraChars, result);
  }

  List<ProtoObject> encodeObjects(ProtoMessageInfo protoMessageInfo) {
    List<ProtoObject> result = new ArrayList<>();
    if (protoMessageInfo.numberOfOneOfObjects() > 0) {
      for (ProtoOneOfObjectPair oneOfObjectPair : protoMessageInfo.getOneOfObjects()) {
        oneOfObjectPair.forEach(result::add);
      }
    }
    if (protoMessageInfo.numberOfHasBitsObjects() > 0) {
      result.addAll(protoMessageInfo.getHasBitsObjects());
    }
    if (protoMessageInfo.hasFields()) {
      for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
        result.addAll(protoFieldInfo.getObjects());
      }
    }
    return result;
  }

  private static int countBytes(IntList info) {
    // We need an extra byte for the terminating '0'.
    int result = 1;
    IntListIterator iterator = info.iterator();
    while (iterator.hasNext()) {
      int value = iterator.nextInt();
      while (value >= 0xD800) {
        char c = (char) (0xE000 | (value & 0x1FFF));
        value >>= 13;
        result += DexString.countBytes(c);
      }
      result += DexString.countBytes((char) value);
    }
    return result;
  }
}
