// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;

import com.android.tools.r8.dex.DexOutputBuffer;
import com.android.tools.r8.dex.FileWriter;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.ConstInstruction;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.naming.dexitembasedstring.NameComputationInfo;
import com.android.tools.r8.utils.BooleanUtils;
import com.android.tools.r8.utils.EncodedValueUtils;
import java.util.Arrays;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

public abstract class DexValue extends DexItem {

  public static final DexValue[] EMPTY_ARRAY = {};

  public static final UnknownDexValue UNKNOWN = UnknownDexValue.UNKNOWN;

  public static final byte VALUE_BYTE = 0x00;
  public static final byte VALUE_SHORT = 0x02;
  public static final byte VALUE_CHAR = 0x03;
  public static final byte VALUE_INT = 0x04;
  public static final byte VALUE_LONG = 0x06;
  public static final byte VALUE_FLOAT = 0x10;
  public static final byte VALUE_DOUBLE = 0x11;
  public static final byte VALUE_METHOD_TYPE = 0x15;
  public static final byte VALUE_METHOD_HANDLE = 0x16;
  public static final byte VALUE_STRING = 0x17;
  public static final byte VALUE_TYPE = 0x18;
  public static final byte VALUE_FIELD = 0x19;
  public static final byte VALUE_METHOD = 0x1a;
  public static final byte VALUE_ENUM = 0x1b;
  public static final byte VALUE_ARRAY = 0x1c;
  public static final byte VALUE_ANNOTATION = 0x1d;
  public static final byte VALUE_NULL = 0x1e;
  public static final byte VALUE_BOOLEAN = 0x1f;

  public DexValueMethodHandle asDexValueMethodHandle() {
    return null;
  }

  public DexValueMethodType asDexValueMethodType() {
    return null;
  }

  public DexValueType asDexValueType() {
    return null;
  }

  public DexValueArray asDexValueArray() {
    return null;
  }

  public boolean isDexValueType() {
    return false;
  }

  public boolean isDexValueArray() {
    return false;
  }

  public boolean isDexValueInt() {
    return false;
  }

  public DexValueInt asDexValueInt() {
    return null;
  }

  public static DexValue fromAsmBootstrapArgument(
      Object value, JarApplicationReader application, DexType clazz) {
    if (value instanceof Integer) {
      return DexValue.DexValueInt.create((Integer) value);
    } else if (value instanceof Long) {
      return DexValue.DexValueLong.create((Long) value);
    } else if (value instanceof Float) {
      return DexValue.DexValueFloat.create((Float) value);
    } else if (value instanceof Double) {
      return DexValue.DexValueDouble.create((Double) value);
    } else if (value instanceof String) {
      return new DexValue.DexValueString(application.getString((String) value));

    } else if (value instanceof Type) {
      Type type = (Type) value;
      switch (type.getSort()) {
        case Type.OBJECT:
          return new DexValue.DexValueType(
              application.getTypeFromDescriptor(((Type) value).getDescriptor()));
        case Type.METHOD:
          return new DexValue.DexValueMethodType(
              application.getProto(((Type) value).getDescriptor()));
        default:
          throw new Unreachable("Type sort is not supported: " + type.getSort());
      }
    } else if (value instanceof Handle) {
      return new DexValue.DexValueMethodHandle(
          DexMethodHandle.fromAsmHandle((Handle) value, application, clazz));
    } else {
      throw new Unreachable(
          "Unsupported bootstrap static argument of type " + value.getClass().getSimpleName());
    }
  }

  private static void writeHeader(byte type, int arg, DexOutputBuffer dest) {
    dest.putByte((byte) ((arg << 5) | type));
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    // Should never be visited.
    throw new Unreachable();
  }

  public abstract void sort();

  public abstract void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping);

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object other);

  @Override
  public abstract String toString();

  public static DexValue defaultForType(DexType type) {
    switch (type.toShorty()) {
      case 'Z':
        return DexValueBoolean.DEFAULT;
      case 'B':
        return DexValueByte.DEFAULT;
      case 'C':
        return DexValueChar.DEFAULT;
      case 'S':
        return DexValueShort.DEFAULT;
      case 'I':
        return DexValueInt.DEFAULT;
      case 'J':
        return DexValueLong.DEFAULT;
      case 'F':
        return DexValueFloat.DEFAULT;
      case 'D':
        return DexValueDouble.DEFAULT;
      case 'L':
        return DexValueNull.NULL;
      default:
        throw new Unreachable("No default value for unexpected type " + type);
    }
  }

  public abstract Object getBoxedValue();

  /** Returns an instruction that can be used to materialize this {@link DexValue} (or null). */
  public ConstInstruction asConstInstruction(
      AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
    return null;
  }

  public boolean isDefault(DexType type) {
    return this == defaultForType(type);
  }

  /**
   * Whether creating this value as a default value for a field might trigger an allocation.
   *
   * <p>This is conservative. It also considers allocations due to class loading when referencing a
   * field or method.
   */
  public boolean mayHaveSideEffects() {
    return true;
  }

  public abstract Object asAsmEncodedObject();

  static public class UnknownDexValue extends DexValue {

    // Singleton instance.
    public static final UnknownDexValue UNKNOWN = new UnknownDexValue();

    private UnknownDexValue() {
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      throw new Unreachable();
    }

    @Override
    public void sort() {
      throw new Unreachable();
    }

    @Override
    public boolean mayHaveSideEffects() {
      return true;
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      throw new Unreachable();
    }

    @Override
    public Object getBoxedValue() {
      throw new Unreachable();
    }

    @Override
    public Object asAsmEncodedObject() {
      throw new Unreachable();
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
      return other == this;
    }

    @Override
    public String toString() {
      return "UNKNOWN";
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return null;
    }
  }

  static private abstract class SimpleDexValue extends DexValue {

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      // Intentionally left empty
    }

    @Override
    public void sort() {
      // Intentionally empty
    }

    @Override
    public boolean mayHaveSideEffects() {
      return false;
    }

    protected static void writeIntegerTo(byte type, long value, int expected,
        DexOutputBuffer dest) {
      // Leave space for header.
      dest.forward(1);
      int length = dest.putSignedEncodedValue(value, expected);
      dest.rewind(length + 1);
      writeHeader(type, length - 1, dest);
      dest.forward(length);
    }
  }

  static public class DexValueByte extends SimpleDexValue {

    public static final DexValueByte DEFAULT = new DexValueByte((byte) 0);

    final byte value;

    private DexValueByte(byte value) {
      this.value = value;
    }

    public static DexValueByte create(byte value) {
      return value == DEFAULT.value ? DEFAULT : new DexValueByte(value);
    }

    public byte getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeHeader(VALUE_BYTE, 0, dest);
      dest.putSignedEncodedValue(value, 1);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Integer.valueOf(value);
    }

    @Override
    public int hashCode() {
      return value * 3;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return other instanceof DexValueByte && value == ((DexValueByte) other).value;
    }

    @Override
    public String toString() {
      return "Byte " + value;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createIntConstant(value, local);
    }
  }

  static public class DexValueShort extends SimpleDexValue {

    public static final DexValueShort DEFAULT = new DexValueShort((short) 0);
    final short value;

    private DexValueShort(short value) {
      this.value = value;
    }

    public static DexValueShort create(short value) {
      return value == DEFAULT.value ? DEFAULT : new DexValueShort(value);
    }

    public short getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeIntegerTo(VALUE_SHORT, value, Short.BYTES, dest);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Integer.valueOf(value);
    }

    @Override
    public int hashCode() {
      return value * 7;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return other instanceof DexValueShort && value == ((DexValueShort) other).value;
    }

    @Override
    public String toString() {
      return "Short " + value;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createIntConstant(value, local);
    }
  }

  static public class DexValueChar extends SimpleDexValue {

    public static final DexValueChar DEFAULT = new DexValueChar((char) 0);
    final char value;

    private DexValueChar(char value) {
      this.value = value;
    }

    public static DexValueChar create(char value) {
      return value == DEFAULT.value ? DEFAULT : new DexValueChar(value);
    }

    public char getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      dest.forward(1);
      int length = dest.putUnsignedEncodedValue(value, 2);
      dest.rewind(length + 1);
      writeHeader(VALUE_CHAR, length - 1, dest);
      dest.forward(length);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Integer.valueOf(value);
    }

    @Override
    public int hashCode() {
      return value * 5;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return other instanceof DexValueChar && value == ((DexValueChar) other).value;
    }

    @Override
    public String toString() {
      return "Char " + value;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createIntConstant(value, local);
    }
  }

  static public class DexValueInt extends SimpleDexValue {

    public static final DexValueInt DEFAULT = new DexValueInt(0);
    public final int value;

    private DexValueInt(int value) {
      this.value = value;
    }

    public static DexValueInt create(int value) {
      return value == DEFAULT.value ? DEFAULT : new DexValueInt(value);
    }

    public int getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeIntegerTo(VALUE_INT, value, Integer.BYTES, dest);
    }

    @Override
    public boolean isDexValueInt() {
      return true;
    }

    @Override
    public DexValueInt asDexValueInt() {
      return this;
    }

    @Override
    public Object asAsmEncodedObject() {
      return Integer.valueOf(value);
    }

    @Override
    public int hashCode() {
      return value * 11;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return other instanceof DexValueInt && value == ((DexValueInt) other).value;
    }

    @Override
    public String toString() {
      return "Int " + value;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createIntConstant(value, local);
    }
  }

  static public class DexValueLong extends SimpleDexValue {

    public static final DexValueLong DEFAULT = new DexValueLong(0);
    final long value;

    private DexValueLong(long value) {
      this.value = value;
    }

    public static DexValueLong create(long value) {
      return value == DEFAULT.value ? DEFAULT : new DexValueLong(value);
    }

    public long getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeIntegerTo(VALUE_LONG, value, Long.BYTES, dest);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Long.valueOf(value);
    }

    @Override
    public int hashCode() {
      return (int) value * 13;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return other instanceof DexValueLong && value == ((DexValueLong) other).value;
    }

    @Override
    public String toString() {
      return "Long " + value;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createLongConstant(value, local);
    }
  }

  static public class DexValueFloat extends SimpleDexValue {

    public static final DexValueFloat DEFAULT = new DexValueFloat(0);
    final float value;

    private DexValueFloat(float value) {
      this.value = value;
    }

    public static DexValueFloat create(float value) {
      return Float.compare(value, DEFAULT.value) == 0 ? DEFAULT : new DexValueFloat(value);
    }

    public float getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      dest.forward(1);
      int length = EncodedValueUtils.putFloat(dest, value);
      dest.rewind(length + 1);
      writeHeader(VALUE_FLOAT, length - 1, dest);
      dest.forward(length);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Float.valueOf(value);
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createFloatConstant(value, local);
    }

    @Override
    public int hashCode() {
      return (int) (value * 19);
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return (other instanceof DexValueFloat) &&
          (Float.compare(value, ((DexValueFloat) other).value) == 0);
    }

    @Override
    public String toString() {
      return "Float " + value;
    }

  }

  static public class DexValueDouble extends SimpleDexValue {

    public static final DexValueDouble DEFAULT = new DexValueDouble(0);

    final double value;

    private DexValueDouble(double value) {
      this.value = value;
    }

    public static DexValueDouble create(double value) {
      return Double.compare(value, DEFAULT.value) == 0 ? DEFAULT : new DexValueDouble(value);
    }

    public double getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      dest.forward(1);
      int length = EncodedValueUtils.putDouble(dest, value);
      dest.rewind(length + 1);
      writeHeader(VALUE_DOUBLE, length - 1, dest);
      dest.forward(length);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Double.valueOf(value);
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createDoubleConstant(value, local);
    }

    @Override
    public int hashCode() {
      return (int) (value * 29);
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return (other instanceof DexValueDouble) &&
          (Double.compare(value, ((DexValueDouble) other).value) == 0);
    }

    @Override
    public String toString() {
      return "Double " + value;
    }
  }

  static private abstract class NestedDexValue<T extends IndexedDexItem> extends DexValue {

    public final T value;

    private NestedDexValue(T value) {
      this.value = value;
    }

    protected abstract byte getValueKind();

    public T getValue() {
      return value;
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      int offset = value.getOffset(mapping);
      dest.forward(1);
      int length = dest.putUnsignedEncodedValue(offset, 4);
      dest.rewind(length + 1);
      writeHeader(getValueKind(), length - 1, dest);
      dest.forward(length);
    }

    @Override
    public Object getBoxedValue() {
      throw new Unreachable("No boxed value for DexValue " + this.getClass().getSimpleName());
    }

    @Override
    public Object asAsmEncodedObject() {
      throw new Unreachable("No ASM conversion for DexValue " + this.getClass().getSimpleName());
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }

    @Override
    public void sort() {
      // Intentionally empty.
    }

    @Override
    public int hashCode() {
      return value.hashCode() * 7 + getValueKind();
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (other instanceof NestedDexValue) {
        NestedDexValue<?> that = (NestedDexValue<?>) other;
        return that.getValueKind() == getValueKind() && that.value.equals(value);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Item " + getValueKind() + " " + value;
    }
  }

  static public class DexValueString extends NestedDexValue<DexString> {

    public DexValueString(DexString value) {
      super(value);
    }

    @Override
    public Object asAsmEncodedObject() {
      return value.toString();
    }

    @Override
    protected byte getValueKind() {
      return VALUE_STRING;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      TypeLatticeElement type = TypeLatticeElement.stringClassType(appView, definitelyNotNull());
      Value outValue = code.createValue(type, local);
      ConstString instruction =
          new ConstString(outValue, value, ThrowingInfo.defaultForConstString(appView.options()));
      if (!instruction.instructionInstanceCanThrow()) {
        return instruction;
      }
      return null;
    }

    @Override
    public boolean mayHaveSideEffects() {
      // Assuming that strings do not have side-effects.
      return false;
    }
  }

  public static class DexItemBasedValueString extends NestedDexValue<DexReference> {

    private final NameComputationInfo<?> nameComputationInfo;

    public DexItemBasedValueString(DexReference value, NameComputationInfo<?> nameComputationInfo) {
      super(value);
      this.nameComputationInfo = nameComputationInfo;
    }

    public NameComputationInfo<?> getNameComputationInfo() {
      return nameComputationInfo;
    }

    @Override
    public Object asAsmEncodedObject() {
      return value.toString();
    }

    @Override
    protected byte getValueKind() {
      return VALUE_STRING;
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      TypeLatticeElement type = TypeLatticeElement.stringClassType(appView, definitelyNotNull());
      Value outValue = code.createValue(type, local);
      DexItemBasedConstString instruction =
          new DexItemBasedConstString(
              outValue,
              value,
              nameComputationInfo,
              ThrowingInfo.defaultForConstString(appView.options()));
      // DexItemBasedConstString cannot throw.
      assert !instruction.instructionInstanceCanThrow();
      return instruction;
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      throw new Unreachable(
          "DexItemBasedValueString values should always be rewritten into DexValueString");
    }
  }

  static public class DexValueType extends NestedDexValue<DexType> {

    public DexValueType(DexType value) {
      super(value);
    }

    @Override
    protected byte getValueKind() {
      return VALUE_TYPE;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }

    @Override
    public DexValueType asDexValueType() {
      return this;
    }

    @Override
    public boolean isDexValueType() {
      return true;
    }
  }

  static public class DexValueField extends NestedDexValue<DexField> {

    public DexValueField(DexField value) {
      super(value);
    }

    @Override
    protected byte getValueKind() {
      return VALUE_FIELD;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }
  }

  static public class DexValueMethod extends NestedDexValue<DexMethod> {

    public DexValueMethod(DexMethod value) {
      super(value);
    }

    @Override
    protected byte getValueKind() {
      return VALUE_METHOD;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }
  }

  static public class DexValueEnum extends NestedDexValue<DexField> {

    public DexValueEnum(DexField value) {
      super(value);
    }

    @Override
    protected byte getValueKind() {
      return VALUE_ENUM;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }
  }

  static public class DexValueMethodType extends NestedDexValue<DexProto> {

    public DexValueMethodType(DexProto value) {
      super(value);
    }

    @Override
    public DexValueMethodType asDexValueMethodType() {
      return this;
    }

    @Override
    protected byte getValueKind() {
      return VALUE_METHOD_TYPE;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }
  }

  static public class DexValueArray extends DexValue {

    final DexValue[] values;

    public DexValueArray(DexValue[] values) {
      this.values = values;
    }

    public DexValue[] getValues() {
      return values;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      collectAll(indexedItems, values);
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeHeader(VALUE_ARRAY, 0, dest);
      dest.putUleb128(values.length);
      for (DexValue value : values) {
        value.writeTo(dest, mapping);
      }
    }

    @Override
    public Object getBoxedValue() {
      throw new Unreachable("No boxed value for DexValueArray");
    }

    @Override
    public Object asAsmEncodedObject() {
      throw new Unreachable("No ASM conversion for DexValueArray");
    }

    @Override
    public void sort() {
      for (DexValue value : values) {
        value.sort();
      }
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (other instanceof DexValueArray) {
        DexValueArray that = (DexValueArray) other;
        return Arrays.equals(that.values, values);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Array " + Arrays.toString(values);
    }

    @Override
    public boolean isDexValueArray() {
      return true;
    }

    @Override
    public DexValueArray asDexValueArray() {
      return this;
    }
  }

  static public class DexValueAnnotation extends DexValue {

    public final DexEncodedAnnotation value;

    public DexValueAnnotation(DexEncodedAnnotation value) {
      this.value = value;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeHeader(VALUE_ANNOTATION, 0, dest);
      FileWriter.writeEncodedAnnotation(value, dest, mapping);
    }

    @Override
    public Object getBoxedValue() {
      throw new Unreachable("No boxed value for DexValueAnnotation");
    }

    @Override
    public Object asAsmEncodedObject() {
      throw new Unreachable("No ASM conversion for DexValueAnnotation");
    }

    @Override
    public void sort() {
      value.sort();
    }

    @Override
    public int hashCode() {
      return value.hashCode() * 7;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (other instanceof DexValueAnnotation) {
        DexValueAnnotation that = (DexValueAnnotation) other;
        return that.value.equals(value);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Annotation " + value;
    }
  }

  static public class DexValueNull extends SimpleDexValue {

    public static final DexValue NULL = new DexValueNull();

    // See DexValueNull.NULL
    private DexValueNull() {
    }

    public Object getValue() {
      return null;
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeHeader(VALUE_NULL, 0, dest);
    }

    @Override
    public Object getBoxedValue() {
      return null;
    }

    @Override
    public Object asAsmEncodedObject() {
      return null;
    }

    @Override
    public int hashCode() {
      return 42;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return (other instanceof DexValueNull);
    }

    @Override
    public String toString() {
      return "Null";
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createConstNull(local);
    }
  }

  static public class DexValueBoolean extends SimpleDexValue {

    private static final DexValueBoolean TRUE = new DexValueBoolean(true);
    private static final DexValueBoolean FALSE = new DexValueBoolean(false);
    // Use a separate instance for the default value to distinguish it from an explicit false value.
    private static final DexValueBoolean DEFAULT = new DexValueBoolean(false);

    final boolean value;

    private DexValueBoolean(boolean value) {
      this.value = value;
    }

    public static DexValueBoolean create(boolean value) {
      return value ? TRUE : FALSE;
    }

    public boolean getValue() {
      return value;
    }

    @Override
    public Object getBoxedValue() {
      return getValue();
    }

    @Override
    public void writeTo(DexOutputBuffer dest, ObjectToOffsetMapping mapping) {
      writeHeader(VALUE_BOOLEAN, value ? 1 : 0, dest);
    }

    @Override
    public Object asAsmEncodedObject() {
      return Integer.valueOf(value ? 1 : 0);
    }

    @Override
    public int hashCode() {
      return value ? 1234 : 4321;
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      return (other instanceof DexValueBoolean) && ((DexValueBoolean) other).value == value;
    }

    @Override
    public String toString() {
      return value ? "True" : "False";
    }

    @Override
    public ConstInstruction asConstInstruction(
        AppView<? extends AppInfoWithSubtyping> appView, IRCode code, DebugLocalInfo local) {
      return code.createIntConstant(BooleanUtils.intValue(value), local);
    }
  }

  static public class DexValueMethodHandle extends NestedDexValue<DexMethodHandle> {

    public DexValueMethodHandle(DexMethodHandle value) {
      super(value);
    }

    @Override
    public DexValueMethodHandle asDexValueMethodHandle() {
      return this;
    }

    @Override
    protected byte getValueKind() {
      return VALUE_METHOD_HANDLE;
    }

    @Override
    public void collectIndexedItems(IndexedItemCollection indexedItems,
        DexMethod method, int instructionOffset) {
      value.collectIndexedItems(indexedItems, method, instructionOffset);
    }
  }
}
