// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import static com.android.tools.r8.ir.analysis.proto.ProtoUtils.getInfoValueFromMessageInfoConstructionInvoke;
import static com.android.tools.r8.ir.analysis.proto.ProtoUtils.getObjectsValueFromMessageInfoConstructionInvoke;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.ir.analysis.proto.schema.DeadProtoFieldObject;
import com.android.tools.r8.ir.analysis.proto.schema.LiveProtoFieldObject;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldInfo;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldType;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldTypeFactory;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoMessageInfo;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoObject;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoObjectFromInvokeStatic;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoObjectFromStaticGet;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoTypeObject;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.naming.dexitembasedstring.NameComputationInfo;
import com.android.tools.r8.utils.ThrowingCharIterator;
import com.android.tools.r8.utils.ThrowingIntIterator;
import com.android.tools.r8.utils.ThrowingIterator;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalInt;

/**
 * The generated class for a protobuf message will have a dynamicMethod(), where the schema of the
 * protobuf message is encoded:
 *
 * <pre>
 *   class SomeMessage {
 *     ...
 *     Object dynamicMethod(MethodToInvoke method) {
 *       switch (method) {
 *         ...
 *         case BUILD_MESSAGE_INFO:
 *           Object[] objects = new Object[] { ... };
 *           String info = "...";
 *           return newMessageInfo(DEFAULT_INSTANCE, info, objects);
 *         ...
 *       }
 *     }
 *   }
 * </pre>
 *
 * This class can be used to decode the encoded schema, given the values `objects` and `info`.
 */
public class RawMessageInfoDecoder {

  private final ProtoFieldTypeFactory factory;
  private final ProtoReferences references;

  RawMessageInfoDecoder(ProtoFieldTypeFactory factory, ProtoReferences references) {
    this.factory = factory;
    this.references = references;
  }

  public ProtoMessageInfo run(
      DexEncodedMethod dynamicMethod, DexClass context, InvokeMethod invoke) {
    assert references.isMessageInfoConstructionMethod(invoke.getInvokedMethod());
    Value infoValue = getInfoValueFromMessageInfoConstructionInvoke(invoke, references);
    Value objectsValue = getObjectsValueFromMessageInfoConstructionInvoke(invoke, references);
    return run(dynamicMethod, context, infoValue, objectsValue);
  }

  public ProtoMessageInfo run(
      DexEncodedMethod dynamicMethod, DexClass context, Value infoValue, Value objectsValue) {
    try {
      ProtoMessageInfo.Builder builder = ProtoMessageInfo.builder(dynamicMethod);
      ThrowingIntIterator<InvalidRawMessageInfoException> infoIterator =
          createInfoIterator(infoValue);

      // flags := info[0].
      int flags = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);
      builder.setFlags(flags);

      // fieldCount := info[1].
      int fieldCount = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);
      if (fieldCount == 0) {
        return builder.build();
      }

      // numberOfOneOfObjects := info[2].
      int numberOfOneOfObjects = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);

      // numberOfHasBitsObjects := info[3].
      int numberOfHasBitsObjects = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);

      // minFieldNumber     := info[4].
      // maxFieldNumber     := info[5].
      // entryCount         := info[6].
      // mapFieldCount      := info[7].
      // repeatedFieldCount := info[8].
      // checkInitialized   := info[9].
      for (int i = 4; i < 10; i++) {
        // No need to store these values, since they can be computed from the rest (and need to be
        // recomputed if the proto is changed).
        infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);
      }

      ThrowingIterator<Value, InvalidRawMessageInfoException> objectIterator =
          createObjectIterator(objectsValue);

      for (int i = 0; i < numberOfOneOfObjects; i++) {
        ProtoObject oneOfObject =
            createProtoObject(
                objectIterator.computeNextIfAbsent(this::invalidObjectsFailure), context);
        if (!oneOfObject.isProtoFieldObject()) {
          throw new InvalidRawMessageInfoException();
        }
        ProtoObject oneOfCaseObject =
            createProtoObject(
                objectIterator.computeNextIfAbsent(this::invalidObjectsFailure), context);
        if (!oneOfCaseObject.isProtoFieldObject()) {
          throw new InvalidRawMessageInfoException();
        }
        builder.addOneOfObject(
            oneOfObject.asProtoFieldObject(), oneOfCaseObject.asProtoFieldObject());
      }

      for (int i = 0; i < numberOfHasBitsObjects; i++) {
        ProtoObject hasBitsObject =
            createProtoObject(
                objectIterator.computeNextIfAbsent(this::invalidObjectsFailure), context);
        if (!hasBitsObject.isProtoFieldObject()) {
          throw new InvalidRawMessageInfoException();
        }
        builder.addHasBitsObject(hasBitsObject.asProtoFieldObject());
      }

      boolean isProto2 = ProtoUtils.isProto2(flags);
      for (int i = 0; i < fieldCount; i++) {
        // Extract field-specific portion of "info" string.
        int fieldNumber = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);
        int fieldTypeWithExtraBits = infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure);
        ProtoFieldType fieldType = factory.createField(fieldTypeWithExtraBits);

        OptionalInt auxData;
        if (fieldType.hasAuxData(isProto2)) {
          auxData = OptionalInt.of(infoIterator.nextIntComputeIfAbsent(this::invalidInfoFailure));
        } else {
          auxData = OptionalInt.empty();
        }

        // Extract field-specific portion of "objects" array.
        int numberOfObjects = fieldType.numberOfObjects(isProto2, factory);
        try {
          List<ProtoObject> objects = new ArrayList<>(numberOfObjects);
          for (Value value : objectIterator.take(numberOfObjects)) {
            objects.add(createProtoObject(value, context));
          }
          builder.addField(new ProtoFieldInfo(fieldNumber, fieldType, auxData, objects));
        } catch (NoSuchElementException e) {
          throw new InvalidRawMessageInfoException();
        }
      }

      // Verify that the input was fully consumed.
      if (infoIterator.hasNext() || objectIterator.hasNext()) {
        throw new InvalidRawMessageInfoException();
      }

      return builder.build();
    } catch (InvalidRawMessageInfoException e) {
      // This should generally not happen, so leave an assert here just in case.
      assert false;
      return null;
    }
  }

  private ProtoObject createProtoObject(Value value, DexClass context)
      throws InvalidRawMessageInfoException {
    Value root = value.getAliasedValue();
    if (!root.isPhi()) {
      Instruction definition = root.definition;
      if (definition.isConstClass()) {
        ConstClass constClass = definition.asConstClass();
        return new ProtoTypeObject(constClass.getValue());
      } else if (definition.isConstString()) {
        ConstString constString = definition.asConstString();
        DexField field = context.lookupUniqueInstanceFieldWithName(constString.getValue());
        if (field != null) {
          return new LiveProtoFieldObject(field);
        }
        // This const-string refers to a field that no longer exists. In this case, we create a
        // special dead-object instead of failing with an InvalidRawMessageInfoException below.
        return new DeadProtoFieldObject(context.type, constString.getValue());
      } else if (definition.isDexItemBasedConstString()) {
        DexItemBasedConstString constString = definition.asDexItemBasedConstString();
        DexReference reference = constString.getItem();
        NameComputationInfo<?> nameComputationInfo = constString.getNameComputationInfo();
        if (reference.isDexField()
            && nameComputationInfo.isFieldNameComputationInfo()
            && nameComputationInfo.asFieldNameComputationInfo().isForFieldName()) {
          DexField field = reference.asDexField();
          DexEncodedField encodedField = context.lookupInstanceField(field);
          if (encodedField != null) {
            return new LiveProtoFieldObject(field);
          }
          // This const-string refers to a field that no longer exists. In this case, we create a
          // special dead-object instead of failing with an InvalidRawMessageInfoException below.
          return new DeadProtoFieldObject(context.type, field.name);
        }
      } else if (definition.isInvokeStatic()) {
        InvokeStatic invoke = definition.asInvokeStatic();
        if (invoke.arguments().isEmpty()) {
          return new ProtoObjectFromInvokeStatic(invoke.getInvokedMethod());
        }
      } else if (definition.isStaticGet()) {
        StaticGet staticGet = definition.asStaticGet();
        return new ProtoObjectFromStaticGet(staticGet.getField());
      }
    }
    throw new InvalidRawMessageInfoException();
  }

  private int invalidInfoFailure() throws InvalidRawMessageInfoException {
    throw new InvalidRawMessageInfoException();
  }

  private Value invalidObjectsFailure() throws InvalidRawMessageInfoException {
    throw new InvalidRawMessageInfoException();
  }

  public static ThrowingIntIterator<InvalidRawMessageInfoException> createInfoIterator(
      Value infoValue) throws InvalidRawMessageInfoException {
    if (!infoValue.isPhi() && infoValue.definition.isConstString()) {
      return createInfoIterator(infoValue.definition.asConstString().getValue());
    }
    throw new InvalidRawMessageInfoException();
  }

  /** Returns an iterator that yields the integers that results from decoding the given string. */
  private static ThrowingIntIterator<InvalidRawMessageInfoException> createInfoIterator(
      DexString info) {
    return new ThrowingIntIterator<InvalidRawMessageInfoException>() {

      private final ThrowingCharIterator<UTFDataFormatException> charIterator = info.iterator();

      @Override
      public boolean hasNext() {
        return charIterator.hasNext();
      }

      @Override
      public int nextInt() throws InvalidRawMessageInfoException {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        int value = 0;
        int shift = 0;
        while (true) {
          char c;
          try {
            c = charIterator.nextChar();
          } catch (UTFDataFormatException e) {
            throw new InvalidRawMessageInfoException();
          }
          if (c >= 0xD800 && c < 0xE000) {
            throw new InvalidRawMessageInfoException();
          }
          if (c < 0xD800) {
            return value | (c << shift);
          }
          value |= (c & 0x1FFF) << shift;
          shift += 13;
          if (!hasNext()) {
            throw new InvalidRawMessageInfoException();
          }
        }
      }
    };
  }

  /**
   * Returns an iterator that yields the values that are stored in the `objects` array that is
   * passed to GeneratedMessageLite.newMessageInfo(). The array values are returned in-order, i.e.,
   * the value objects[i] will be returned prior to the value objects[i+1].
   */
  private static ThrowingIterator<Value, InvalidRawMessageInfoException> createObjectIterator(
      Value objectsValue) throws InvalidRawMessageInfoException {
    if (objectsValue.isPhi() || !objectsValue.definition.isNewArrayEmpty()) {
      throw new InvalidRawMessageInfoException();
    }

    NewArrayEmpty newArrayEmpty = objectsValue.definition.asNewArrayEmpty();
    int expectedArraySize = objectsValue.uniqueUsers().size() - 1;

    // Verify that the size is correct.
    Value sizeValue = newArrayEmpty.size().getAliasedValue();
    if (sizeValue.isPhi()
        || !sizeValue.definition.isConstNumber()
        || sizeValue.definition.asConstNumber().getIntValue() != expectedArraySize) {
      throw new InvalidRawMessageInfoException();
    }

    // Create an iterator for the block of interest.
    InstructionIterator instructionIterator = newArrayEmpty.getBlock().iterator();
    instructionIterator.nextUntil(instruction -> instruction == newArrayEmpty);

    return new ThrowingIterator<Value, InvalidRawMessageInfoException>() {

      private int expectedNextIndex = 0;

      @Override
      public boolean hasNext() {
        while (instructionIterator.hasNext()) {
          Instruction next = instructionIterator.peekNext();
          if (isArrayPutOfInterest(next)) {
            return true;
          }
          if (next.isJumpInstruction()) {
            return false;
          }
          instructionIterator.next();
        }
        return false;
      }

      @Override
      public Value next() throws InvalidRawMessageInfoException {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ArrayPut arrayPut = instructionIterator.next().asArrayPut();

        // Verify that the index correct.
        Value indexValue = arrayPut.index().getAliasedValue();
        if (indexValue.isPhi()
            || !indexValue.definition.isConstNumber()
            || indexValue.definition.asConstNumber().getIntValue() != expectedNextIndex) {
          throw new InvalidRawMessageInfoException();
        }

        expectedNextIndex++;
        return arrayPut.value().getAliasedValue();
      }

      private boolean isArrayPutOfInterest(Instruction instruction) {
        return instruction.isArrayPut()
            && instruction.asArrayPut().array().getAliasedValue() == objectsValue;
      }
    };
  }

  private static class InvalidRawMessageInfoException extends Exception {}
}
