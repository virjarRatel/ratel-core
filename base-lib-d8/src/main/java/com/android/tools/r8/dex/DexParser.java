// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.utils.EncodedValueUtils.parseDouble;
import static com.android.tools.r8.utils.EncodedValueUtils.parseFloat;
import static com.android.tools.r8.utils.EncodedValueUtils.parseSigned;
import static com.android.tools.r8.utils.EncodedValueUtils.parseUnsigned;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.InstructionFactory;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.Descriptor;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexCode.Try;
import com.android.tools.r8.graph.DexCode.TryHandler;
import com.android.tools.r8.graph.DexCode.TryHandler.TypeAddrPair;
import com.android.tools.r8.graph.DexDebugEvent;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedArray;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMemberAnnotation;
import com.android.tools.r8.graph.DexMemberAnnotation.DexFieldAnnotation;
import com.android.tools.r8.graph.DexMemberAnnotation.DexMethodAnnotation;
import com.android.tools.r8.graph.DexMemberAnnotation.DexParameterAnnotation;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProgramClass.ChecksumSupplier;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueMethodType;
import com.android.tools.r8.graph.DexValue.DexValueNull;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.graph.EnclosingMethodAttribute;
import com.android.tools.r8.graph.FieldAccessFlags;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.graph.OffsetToObjectMapping;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.PathOrigin;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Pair;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DexParser {

  private final int NO_INDEX = -1;
  private final Origin origin;
  private DexReader dexReader;
  private final DexSection[] dexSections;
  private int[] stringIDs;
  private final ClassKind classKind;
  private final InternalOptions options;
  private Object2LongMap<String> checksums;

  public static DexSection[] parseMapFrom(Path file) throws IOException {
    return parseMapFrom(Files.newInputStream(file), new PathOrigin(file));
  }

  public static DexSection[] parseMapFrom(InputStream stream, Origin origin) throws IOException {
    return parseMapFrom(new DexReader(origin, ByteStreams.toByteArray(stream)));
  }

  private static DexSection[] parseMapFrom(DexReader dexReader) {
    DexParser dexParser = new DexParser(dexReader, ClassKind.PROGRAM, new InternalOptions());
    return dexParser.dexSections;
  }

  public void close() {
    // This close behavior is needed to reduce peak memory usage of D8/R8.
    indexedItems = null;
    codes = null;
    offsetMap = null;
    dexReader = null;
    stringIDs = null;
  }

  // Mapping from indexes to indexable dex items.
  private OffsetToObjectMapping indexedItems = new OffsetToObjectMapping();

  // Mapping from offset to code item;
  private Int2ReferenceMap<DexCode> codes = new Int2ReferenceOpenHashMap<>();

  // Mapping from offset to dex item;
  private Int2ReferenceMap<Object> offsetMap = new Int2ReferenceOpenHashMap<>();

  // Factory to canonicalize certain dexitems.
  private final DexItemFactory dexItemFactory;

  public DexParser(DexReader dexReader, ClassKind classKind, InternalOptions options) {
    assert dexReader.getOrigin() != null;
    this.origin = dexReader.getOrigin();
    this.dexReader = dexReader;
    this.dexItemFactory = options.itemFactory;
    dexReader.setByteOrder();
    dexSections = parseMap();
    parseStringIDs();
    this.classKind = classKind;
    this.options = options;
  }

  private void ensureCodesInited(int offset) {
    if (offset == 0) {
      return;
    }

    if (codes == null) {
      codes = new Int2ReferenceOpenHashMap<>();
    }

    if (classKind == ClassKind.LIBRARY) {
      // Ignore contents of library files.
      return;
    }
    DexSection dexSection = lookupSection(Constants.TYPE_CODE_ITEM);
    if (dexSection.length == 0) {
      return;
    }

    if (!codes.containsKey(offset)) {
      int currentPos = dexReader.position();
      dexReader.position(offset);
      dexReader.align(4);
      DexCode code = parseCodeItem();
      codes.put(offset, code);  // Update the file local offset to code mapping.
      dexReader.position(currentPos);
    }
  }

  private DexTypeList parseTypeList() {
    DexType[] result = new DexType[dexReader.getUint()];
    for (int j = 0; j < result.length; j++) {
      result[j] = indexedItems.getType(dexReader.getUshort());
    }
    return new DexTypeList(result);
  }

  private DexTypeList typeListAt(int offset) {
    if (offset == 0) {
      return DexTypeList.empty();
    }
    return (DexTypeList) cacheAt(offset, this::parseTypeList);
  }

  private DexValue parseEncodedValue() {
    int header = dexReader.get() & 0xff;
    int valueArg = header >> 5;
    int valueType = header & 0x1f;
    switch (valueType) {
      case DexValue.VALUE_BYTE: {
        assert valueArg == 0;
        byte value = (byte) parseSigned(dexReader, 1);
        return DexValue.DexValueByte.create(value);
      }
      case DexValue.VALUE_SHORT: {
        int size = valueArg + 1;
        short value = (short) parseSigned(dexReader, size);
        return DexValue.DexValueShort.create(value);
      }
      case DexValue.VALUE_CHAR: {
        int size = valueArg + 1;
        char value = (char) parseUnsigned(dexReader, size);
        return DexValue.DexValueChar.create(value);
      }
      case DexValue.VALUE_INT: {
        int size = valueArg + 1;
        int value = (int) parseSigned(dexReader, size);
        return DexValue.DexValueInt.create(value);
      }
      case DexValue.VALUE_LONG: {
        int size = valueArg + 1;
        long value = parseSigned(dexReader, size);
        return DexValue.DexValueLong.create(value);
      }
      case DexValue.VALUE_FLOAT: {
        int size = valueArg + 1;
        return DexValue.DexValueFloat.create(parseFloat(dexReader, size));
      }
      case DexValue.VALUE_DOUBLE: {
        int size = valueArg + 1;
        return DexValue.DexValueDouble.create(parseDouble(dexReader, size));
      }
      case DexValue.VALUE_STRING: {
        int size = valueArg + 1;
        int index = (int) parseUnsigned(dexReader, size);
        DexString value = indexedItems.getString(index);
        return new DexValue.DexValueString(value);
      }
      case DexValue.VALUE_TYPE: {
        int size = valueArg + 1;
        DexType value = indexedItems.getType((int) parseUnsigned(dexReader, size));
        return new DexValue.DexValueType(value);
      }
      case DexValue.VALUE_FIELD: {
        int size = valueArg + 1;
        DexField value = indexedItems.getField((int) parseUnsigned(dexReader, size));
        checkName(value.name);
        return new DexValue.DexValueField(value);
      }
      case DexValue.VALUE_METHOD: {
        int size = valueArg + 1;
        DexMethod value = indexedItems.getMethod((int) parseUnsigned(dexReader, size));
        checkName(value.name);
        return new DexValue.DexValueMethod(value);
      }
      case DexValue.VALUE_ENUM: {
        int size = valueArg + 1;
        DexField value = indexedItems.getField((int) parseUnsigned(dexReader, size));
        return new DexValue.DexValueEnum(value);
      }
      case DexValue.VALUE_ARRAY: {
        assert valueArg == 0;
        return new DexValue.DexValueArray(parseEncodedArrayValues());
      }
      case DexValue.VALUE_ANNOTATION: {
        assert valueArg == 0;
        return new DexValue.DexValueAnnotation(parseEncodedAnnotation());
      }
      case DexValue.VALUE_NULL: {
        assert valueArg == 0;
        return DexValueNull.NULL;
      }
      case DexValue.VALUE_BOOLEAN: {
        // 0 is false, and 1 is true.
        return DexValue.DexValueBoolean.create(valueArg != 0);
      }
      case DexValue.VALUE_METHOD_TYPE: {
        int size = valueArg + 1;
        DexProto value = indexedItems.getProto((int) parseUnsigned(dexReader, size));
        return new DexValue.DexValueMethodType(value);
      }
      case DexValue.VALUE_METHOD_HANDLE: {
        int size = valueArg + 1;
        DexMethodHandle value = indexedItems.getMethodHandle((int) parseUnsigned(dexReader, size));
        return new DexValue.DexValueMethodHandle(value);
      }
      default:
        throw new IndexOutOfBoundsException();
    }
  }

  private void checkName(DexString name) {
    if (!options.itemFactory.getSkipNameValidationForTesting()
        && !name.isValidSimpleName(options.minApiLevel)) {
      throw new CompilationError("Space characters in SimpleName '"
        + name.toASCIIString()
        + "' are not allowed prior to DEX version 040");
    }
  }

  private DexEncodedAnnotation parseEncodedAnnotation() {
    int typeIdx = dexReader.getUleb128();
    int size = dexReader.getUleb128();
    DexAnnotationElement[] elements = new DexAnnotationElement[size];
    for (int i = 0; i < size; i++) {
      int nameIdx = dexReader.getUleb128();
      DexValue value = parseEncodedValue();
      elements[i] = new DexAnnotationElement(indexedItems.getString(nameIdx), value);
    }
    return new DexEncodedAnnotation(indexedItems.getType(typeIdx), elements);
  }

  private DexValue[] parseEncodedArrayValues() {
    int size = dexReader.getUleb128();
    DexValue[] values = new DexValue[size];
    for (int i = 0; i < size; i++) {
      values[i] = parseEncodedValue();
    }
    return values;
  }


  private DexEncodedArray parseEncodedArray() {
    return new DexEncodedArray(parseEncodedArrayValues());
  }

  private DexEncodedArray encodedArrayAt(int offset) {
    return (DexEncodedArray) cacheAt(offset, this::parseEncodedArray);
  }

  private DexFieldAnnotation[] parseFieldAnnotations(int size) {
    if (size == 0) {
      return null;
    }
    int[] fieldIndices = new int[size];
    int[] annotationOffsets = new int[size];
    for (int i = 0; i < size; i++) {
      fieldIndices[i] = dexReader.getUint();
      annotationOffsets[i] = dexReader.getUint();
    }
    int saved = dexReader.position();
    DexFieldAnnotation[] result = new DexFieldAnnotation[size];
    for (int i = 0; i < size; i++) {
      DexField field = indexedItems.getField(fieldIndices[i]);
      DexAnnotationSet annotation = annotationSetAt(annotationOffsets[i]);
      result[i] = new DexFieldAnnotation(field, annotation);
    }
    dexReader.position(saved);
    return result;
  }

  private DexMethodAnnotation[] parseMethodAnnotations(int size) {
    if (size == 0) {
      return null;
    }
    int[] methodIndices = new int[size];
    int[] annotationOffsets = new int[size];
    for (int i = 0; i < size; i++) {
      methodIndices[i] = dexReader.getUint();
      annotationOffsets[i] = dexReader.getUint();
    }
    int saved = dexReader.position();
    DexMethodAnnotation[] result = new DexMethodAnnotation[size];
    for (int i = 0; i < size; i++) {
      DexMethod method = indexedItems.getMethod(methodIndices[i]);
      DexAnnotationSet annotation = annotationSetAt(annotationOffsets[i]);
      result[i] = new DexMethodAnnotation(method, annotation);
    }
    dexReader.position(saved);
    return result;
  }

  private ParameterAnnotationsList annotationSetRefListAt(int offset) {
    return (ParameterAnnotationsList) cacheAt(offset, this::parseAnnotationSetRefList);
  }

  private ParameterAnnotationsList parseAnnotationSetRefList() {
    int size = dexReader.getUint();
    int[] annotationOffsets = new int[size];
    for (int i = 0; i < size; i++) {
      annotationOffsets[i] = dexReader.getUint();
    }
    DexAnnotationSet[] values = new DexAnnotationSet[size];
    for (int i = 0; i < size; i++) {
      values[i] = annotationSetAt(annotationOffsets[i]);
    }
    return new ParameterAnnotationsList(values);
  }

  private DexParameterAnnotation[] parseParameterAnnotations(int size) {
    if (size == 0) {
      return null;
    }
    int[] methodIndices = new int[size];
    int[] annotationOffsets = new int[size];
    for (int i = 0; i < size; i++) {
      methodIndices[i] = dexReader.getUint();
      annotationOffsets[i] = dexReader.getUint();
    }
    int saved = dexReader.position();
    DexParameterAnnotation[] result = new DexParameterAnnotation[size];
    for (int i = 0; i < size; i++) {
      DexMethod method = indexedItems.getMethod(methodIndices[i]);
      result[i] = new DexParameterAnnotation(
          method,
          annotationSetRefListAt(annotationOffsets[i])
              .withParameterCount(method.proto.parameters.size()));
    }
    dexReader.position(saved);
    return result;
  }

  private <T> Object cacheAt(int offset, Supplier<T> function, Supplier<T> defaultValue) {
    if (offset == 0) {
      return defaultValue.get();
    }
    return cacheAt(offset, function);
  }

  private <T> Object cacheAt(int offset, Supplier<T> function) {
    if (offset == 0) {
      return null;  // return null for offset zero.
    }
    Object result = offsetMap.get(offset);
    if (result != null) {
      return result;  // return the cached result.
    }
    // Cache is empty so parse the structure.
    dexReader.position(offset);
    result = function.get();
    // Update the map.
    offsetMap.put(offset, result);
    assert offsetMap.get(offset) == result;
    return result;
  }

  private DexAnnotation parseAnnotation() {
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Reading Annotation @ 0x%08x.", dexReader.position());
    }
    int visibility = dexReader.get();
    return new DexAnnotation(visibility, parseEncodedAnnotation());
  }

  private DexAnnotation annotationAt(int offset) {
    return (DexAnnotation) cacheAt(offset, this::parseAnnotation);
  }

  private DexAnnotationSet parseAnnotationSet() {
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Reading AnnotationSet @ 0x%08x.", dexReader.position());
    }
    int size = dexReader.getUint();
    int[] annotationOffsets = new int[size];
    for (int i = 0; i < size; i++) {
      annotationOffsets[i] = dexReader.getUint();
    }
    DexAnnotation[] result = new DexAnnotation[size];
    int actualSize = 0;
    for (int i = 0; i < size; i++) {
      DexAnnotation dexAnnotation = annotationAt(annotationOffsets[i]);
      if (retainAnnotation(dexAnnotation)) {
        result[actualSize++] = dexAnnotation;
      }
    }
    if (actualSize < size) {
      DexAnnotation[] temp = new DexAnnotation[actualSize];
      System.arraycopy(result, 0, temp, 0, actualSize);
      result = temp;
    }
    DexType dupType = DexAnnotationSet.findDuplicateEntryType(result);
    if (dupType != null) {
      throw new CompilationError(
          "Multiple annotations of type `" + dupType.toSourceString() + "`");
    }
    return new DexAnnotationSet(result);
  }

  private boolean retainAnnotation(DexAnnotation annotation) {
    return annotation.visibility != DexAnnotation.VISIBILITY_BUILD
        || DexAnnotation.retainCompileTimeAnnotation(annotation.annotation.type, options);
  }

  private DexAnnotationSet annotationSetAt(int offset) {
    return (DexAnnotationSet) cacheAt(offset, this::parseAnnotationSet, DexAnnotationSet::empty);
  }

  private AnnotationsDirectory annotationsDirectoryAt(int offset) {
    return (AnnotationsDirectory) cacheAt(offset, this::parseAnnotationsDirectory,
        AnnotationsDirectory::empty);
  }

  private AnnotationsDirectory parseAnnotationsDirectory() {
    int classAnnotationsOff = dexReader.getUint();
    int fieldsSize = dexReader.getUint();
    int methodsSize = dexReader.getUint();
    int parametersSize = dexReader.getUint();
    final DexFieldAnnotation[] fields = parseFieldAnnotations(fieldsSize);
    final DexMethodAnnotation[] methods = parseMethodAnnotations(methodsSize);
    final DexParameterAnnotation[] parameters = parseParameterAnnotations(parametersSize);
    return new AnnotationsDirectory(
        annotationSetAt(classAnnotationsOff),
        fields,
        methods,
        parameters);
  }

  private DexDebugInfo debugInfoAt(int offset) {
    return (DexDebugInfo) cacheAt(offset, this::parseDebugInfo);
  }

  private DexDebugInfo parseDebugInfo() {
    int start = dexReader.getUleb128();
    int parametersSize = dexReader.getUleb128();
    DexString[] parameters = new DexString[parametersSize];
    for (int i = 0; i < parametersSize; i++) {
      int index = dexReader.getUleb128p1();
      if (index != NO_INDEX) {
        parameters[i] = indexedItems.getString(index);
      }
    }
    List<DexDebugEvent> events = new ArrayList<>();
    for (int head = dexReader.getUbyte(); head != Constants.DBG_END_SEQUENCE; head = dexReader.getUbyte()) {
      switch (head) {
        case Constants.DBG_ADVANCE_PC:
          events.add(dexItemFactory.createAdvancePC(dexReader.getUleb128()));
          break;
        case Constants.DBG_ADVANCE_LINE:
          events.add(dexItemFactory.createAdvanceLine(dexReader.getSleb128()));
          break;
        case Constants.DBG_START_LOCAL: {
          int registerNum = dexReader.getUleb128();
          int nameIdx = dexReader.getUleb128p1();
          int typeIdx = dexReader.getUleb128p1();
          events.add(new DexDebugEvent.StartLocal(
              registerNum,
              nameIdx == NO_INDEX ? null : indexedItems.getString(nameIdx),
              typeIdx == NO_INDEX ? null : indexedItems.getType(typeIdx),
              null));
          break;
        }
        case Constants.DBG_START_LOCAL_EXTENDED: {
          int registerNum = dexReader.getUleb128();
          int nameIdx = dexReader.getUleb128p1();
          int typeIdx = dexReader.getUleb128p1();
          int sigIdx = dexReader.getUleb128p1();
          events.add(new DexDebugEvent.StartLocal(
              registerNum,
              nameIdx == NO_INDEX ? null : indexedItems.getString(nameIdx),
              typeIdx == NO_INDEX ? null : indexedItems.getType(typeIdx),
              sigIdx == NO_INDEX ? null : indexedItems.getString(sigIdx)));
          break;
        }
        case Constants.DBG_END_LOCAL: {
          events.add(dexItemFactory.createEndLocal(dexReader.getUleb128()));
          break;
        }
        case Constants.DBG_RESTART_LOCAL: {
          events.add(dexItemFactory.createRestartLocal(dexReader.getUleb128()));
          break;
        }
        case Constants.DBG_SET_PROLOGUE_END: {
          events.add(dexItemFactory.createSetPrologueEnd());
          break;
        }
        case Constants.DBG_SET_EPILOGUE_BEGIN: {
          events.add(dexItemFactory.createSetEpilogueBegin());
          break;
        }
        case Constants.DBG_SET_FILE: {
          int nameIdx = dexReader.getUleb128p1();
          DexString sourceFile = nameIdx == NO_INDEX ? null : indexedItems.getString(nameIdx);
          events.add(dexItemFactory.createSetFile(sourceFile));
          break;
        }
        default: {
          assert head >= 0x0a && head <= 0xff;
          events.add(dexItemFactory.createDefault(head));
        }
      }
    }
    return new DexDebugInfo(start, parameters, events.toArray(DexDebugEvent.EMPTY_ARRAY));
  }

  private static class MemberAnnotationIterator<S extends Descriptor<?, S>, T extends DexItem> {

    private int index = 0;
    private final DexMemberAnnotation<S, T>[] annotations;
    private final Supplier<T> emptyValue;

    private MemberAnnotationIterator(DexMemberAnnotation<S, T>[] annotations,
        Supplier<T> emptyValue) {
      this.annotations = annotations;
      this.emptyValue = emptyValue;
    }

    // Get the annotation set for an item. This method assumes that it is always called with
    // an item that is higher in the sorting order than the last item.
    T getNextFor(S item) {
      // TODO(ager): We could use the indices from the file to make this search faster using
      // compareTo instead of slowCompareTo. That would require us to assign indices during
      // reading. Those indices should be cleared after reading to make sure that we resort
      // everything correctly at the end.
      while (index < annotations.length && annotations[index].item.slowCompareTo(item) < 0) {
        index++;
      }
      if (index >= annotations.length || !annotations[index].item.equals(item)) {
        return emptyValue.get();
      }
      return annotations[index].annotations;
    }
  }

  private DexEncodedField[] readFields(int size, DexFieldAnnotation[] annotations,
      DexValue[] staticValues) {
    DexEncodedField[] fields = new DexEncodedField[size];
    int fieldIndex = 0;
    MemberAnnotationIterator<DexField, DexAnnotationSet> annotationIterator =
        new MemberAnnotationIterator<>(annotations, DexAnnotationSet::empty);
    for (int i = 0; i < size; i++) {
      fieldIndex += dexReader.getUleb128();
      DexField field = indexedItems.getField(fieldIndex);
      FieldAccessFlags accessFlags = FieldAccessFlags.fromDexAccessFlags(dexReader.getUleb128());
      DexAnnotationSet fieldAnnotations = annotationIterator.getNextFor(field);
      DexValue staticValue = null;
      if (accessFlags.isStatic()) {
        if (staticValues != null && i < staticValues.length) {
          staticValue = staticValues[i];
        }
      }
      fields[i] = new DexEncodedField(field, accessFlags, fieldAnnotations, staticValue);
    }
    return fields;
  }

  private DexEncodedMethod[] readMethods(
      int size,
      DexMethodAnnotation[] annotations,
      DexParameterAnnotation[] parameters,
      boolean skipCodes,
      boolean ensureNonAbstract) {
    DexEncodedMethod[] methods = new DexEncodedMethod[size];
    int methodIndex = 0;
    MemberAnnotationIterator<DexMethod, DexAnnotationSet> annotationIterator =
        new MemberAnnotationIterator<>(annotations, DexAnnotationSet::empty);
    MemberAnnotationIterator<DexMethod, ParameterAnnotationsList> parameterAnnotationsIterator =
        new MemberAnnotationIterator<>(parameters, ParameterAnnotationsList::empty);
    for (int i = 0; i < size; i++) {
      methodIndex += dexReader.getUleb128();
      MethodAccessFlags accessFlags = MethodAccessFlags.fromDexAccessFlags(dexReader.getUleb128());
      int codeOff = dexReader.getUleb128();
      DexCode code = null;
      if (!skipCodes) {
        ensureCodesInited(codeOff);
        assert codeOff == 0 || codes.get(codeOff) != null;
        code = codes.get(codeOff);
      }
      DexMethod method = indexedItems.getMethod(methodIndex);
      DexEncodedMethod encodedMethod =
          new DexEncodedMethod(
              method,
              accessFlags,
              annotationIterator.getNextFor(method),
              parameterAnnotationsIterator.getNextFor(method),
              code);
      if (accessFlags.isAbstract() && ensureNonAbstract) {
        accessFlags.unsetAbstract();
        encodedMethod =
            options.isGeneratingClassFiles()
                ? encodedMethod.toEmptyThrowingMethodCf()
                : encodedMethod.toEmptyThrowingMethodDex();
      }
      methods[i] = encodedMethod;
    }
    return methods;
  }

  void addClassDefsTo(Consumer<DexClass> classCollection) {
    final DexSection dexSection = lookupSection(Constants.TYPE_CLASS_DEF_ITEM);
    final int length = dexSection.length;
    indexedItems.initializeClasses(length);
    if (length == 0) {
      return;
    }
    dexReader.position(dexSection.offset);

    int[] classIndices = new int[length];
    int[] accessFlags = new int[length];
    int[] superclassIndices = new int[length];
    int[] interfacesOffsets = new int[length];
    int[] sourceFileIndices = new int[length];
    int[] annotationsOffsets = new int[length];
    int[] classDataOffsets = new int[length];
    int[] staticValuesOffsets = new int[length];

    for (int i = 0; i < length; i++) {
      if (Log.ENABLED) {
        Log.verbose(getClass(), "Reading ClassDef @ 0x%08x.", dexReader.position());
      }
      classIndices[i] = dexReader.getUint();
      accessFlags[i] = dexReader.getUint();
      superclassIndices[i] = dexReader.getInt();
      interfacesOffsets[i] = dexReader.getUint();
      sourceFileIndices[i] = dexReader.getInt();
      annotationsOffsets[i] = dexReader.getUint();
      classDataOffsets[i] = dexReader.getUint();
      staticValuesOffsets[i] = dexReader.getUint();
    }

    for (int i = 0; i < length; i++) {
      int superclassIdx = superclassIndices[i];
      DexType superclass = superclassIdx == NO_INDEX ? null : indexedItems.getType(superclassIdx);
      int srcIdx = sourceFileIndices[i];
      DexString source = srcIdx == NO_INDEX ? null : indexedItems.getString(srcIdx);
      DexType type = indexedItems.getType(classIndices[i]);
      ClassAccessFlags flags = ClassAccessFlags.fromDexAccessFlags(accessFlags[i]);
      // Check if constraints from
      // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1 are met.
      if (!flags.areValid(Constants.CORRESPONDING_CLASS_FILE_VERSION, false)) {
        throw new CompilationError("Class " + type.toSourceString()
            + " has illegal access flags. Found: " + flags, origin);
      }
      DexEncodedField[] staticFields = DexEncodedField.EMPTY_ARRAY;
      DexEncodedField[] instanceFields = DexEncodedField.EMPTY_ARRAY;
      DexEncodedMethod[] directMethods = DexEncodedMethod.EMPTY_ARRAY;
      DexEncodedMethod[] virtualMethods = DexEncodedMethod.EMPTY_ARRAY;
      AnnotationsDirectory annotationsDirectory = annotationsDirectoryAt(annotationsOffsets[i]);

      Long checksum = null;
      if (checksums != null && !checksums.isEmpty()) {
        String desc = type.descriptor.toASCIIString();
        checksum = checksums.getOrDefault(desc, null);
        if (!options.dexClassChecksumFilter.test(desc, checksum)) {
          continue;
        }
      }
      if (classDataOffsets[i] != 0) {
        DexEncodedArray staticValues = encodedArrayAt(staticValuesOffsets[i]);

        dexReader.position(classDataOffsets[i]);
        int staticFieldsSize = dexReader.getUleb128();
        int instanceFieldsSize = dexReader.getUleb128();
        int directMethodsSize = dexReader.getUleb128();
        int virtualMethodsSize = dexReader.getUleb128();

        staticFields = readFields(staticFieldsSize, annotationsDirectory.fields,
            staticValues != null ? staticValues.values : null);
        instanceFields = readFields(instanceFieldsSize, annotationsDirectory.fields, null);
        directMethods =
            readMethods(
                directMethodsSize,
                annotationsDirectory.methods,
                annotationsDirectory.parameters,
                classKind != ClassKind.PROGRAM,
                options.canHaveDalvikAbstractMethodOnNonAbstractClassVerificationBug()
                    && !flags.isAbstract());
        virtualMethods =
            readMethods(
                virtualMethodsSize,
                annotationsDirectory.methods,
                annotationsDirectory.parameters,
                classKind != ClassKind.PROGRAM,
                options.canHaveDalvikAbstractMethodOnNonAbstractClassVerificationBug()
                    && !flags.isAbstract());
      }

      AttributesAndAnnotations attrs =
          new AttributesAndAnnotations(type, annotationsDirectory.clazz, options.itemFactory);

      Long finalChecksum = checksum;
      ChecksumSupplier checksumSupplier =
          finalChecksum == null ? DexProgramClass::invalidChecksumRequest : c -> finalChecksum;

      DexClass clazz =
          classKind.create(
              type,
              Kind.DEX,
              origin,
              flags,
              superclass,
              typeListAt(interfacesOffsets[i]),
              source,
              null,
              Collections.emptyList(),
              attrs.getEnclosingMethodAttribute(),
              attrs.getInnerClasses(),
              attrs.getAnnotations(),
              staticFields,
              instanceFields,
              directMethods,
              virtualMethods,
              dexItemFactory.getSkipNameValidationForTesting(),
              checksumSupplier);
      classCollection.accept(clazz);  // Update the application object.
    }
  }

  private void parseStringIDs() {
    DexSection dexSection = lookupSection(Constants.TYPE_STRING_ID_ITEM);
    stringIDs = new int[dexSection.length];
    if (dexSection.length == 0) {
      return;
    }
    dexReader.position(dexSection.offset);
    for (int i = 0; i < dexSection.length; i++) {
      stringIDs[i] = dexReader.getUint();
    }
  }

  private DexSection lookupSection(int type) {
    for (DexSection s : dexSections) {
      if (s.type == type) {
        return s;
      }
    }
    // If the section doesn't exist, return an empty section of this type.
    return new DexSection(type, 0, 0, 0);
  }

  private DexSection[] parseMap() {
    // Read the dexSections information from the MAP.
    int mapOffset = dexReader.getUint(Constants.MAP_OFF_OFFSET);
    dexReader.position(mapOffset);
    int mapSize = dexReader.getUint();
    final DexSection[] result = new DexSection[mapSize];
    for (int i = 0; i < mapSize; i++) {
      int type = dexReader.getUshort();
      int unused = dexReader.getUshort();
      int size = dexReader.getUint();
      int offset = dexReader.getUint();
      result[i] = new DexSection(type, unused, size, offset);
    }
    if (Log.ENABLED) {
      for (int i = 0; i < result.length; i++) {
        DexSection dexSection = result[i];
        int nextOffset = i < result.length - 1 ? result[i + 1].offset : dexSection.offset;
        Log.debug(this.getClass(), "Read section 0x%04x @ 0x%08x #items %08d size 0x%08x.",
            dexSection.type, dexSection.offset, dexSection.length, nextOffset - dexSection.offset);
      }
    }
    for (int i = 0; i < mapSize - 1; i++) {
      result[i].setEnd(result[i + 1].offset);
    }
    result[mapSize - 1].setEnd(dexReader.end());
    return result;
  }

  private DexCode parseCodeItem() {
    int registerSize = dexReader.getUshort();
    int insSize = dexReader.getUshort();
    int outsSize = dexReader.getUshort();
    int triesSize = dexReader.getUshort();
    int debugInfoOff = dexReader.getUint();
    int insnsSize = dexReader.getUint();
    short[] code = new short[insnsSize];
    Try[] tries = new Try[triesSize];
    DexCode.TryHandler[] handlers = null;

    if (insnsSize != 0) {
      for (int i = 0; i < insnsSize; i++) {
        code[i] = dexReader.getShort();
      }
      if (insnsSize % 2 != 0) {
        dexReader.getUshort();  // Skip padding ushort
      }
      if (triesSize > 0) {
        Int2IntArrayMap handlerMap = new Int2IntArrayMap();
        // tries: try_item[tries_size].
        for (int i = 0; i < triesSize; i++) {
          int startAddr = dexReader.getUint();
          int insnCount = dexReader.getUshort();
          int handlerOff = dexReader.getUshort();
          tries[i] = new Try(startAddr, insnCount, handlerOff);
        }
        // handlers: encoded_catch_handler_list
        int encodedCatchHandlerListPosition = dexReader.position();
        // - size: uleb128
        int size = dexReader.getUleb128();
        handlers = new TryHandler[size];
        // - list: encoded_catch_handler[handlers_size]
        for (int i = 0; i < size; i++) {
          // encoded_catch_handler
          int encodedCatchHandlerOffset = dexReader.position() - encodedCatchHandlerListPosition;
          handlerMap.put(encodedCatchHandlerOffset, i);
          // - size:	sleb128
          int hsize = dexReader.getSleb128();
          int realHsize = Math.abs(hsize);
          // - handlers	encoded_type_addr_pair[abs(size)]
          TryHandler.TypeAddrPair pairs[] = new TryHandler.TypeAddrPair[realHsize];
          for (int j = 0; j < realHsize; j++) {
            int typeIdx = dexReader.getUleb128();
            int addr = dexReader.getUleb128();
            pairs[j] = new TypeAddrPair(indexedItems.getType(typeIdx), addr);
          }
          int catchAllAddr = -1;
          if (hsize <= 0) {
            catchAllAddr = dexReader.getUleb128();
          }
          handlers[i] = new TryHandler(pairs, catchAllAddr);
        }
        // Convert the handler offsets inside the Try objects to indexes.
        for (Try t : tries) {
          t.setHandlerIndex(handlerMap);
        }
      }
    }
    // Store and restore offset information around reading debug info.
    int saved = dexReader.position();
    DexDebugInfo debugInfo = debugInfoAt(debugInfoOff);
    dexReader.position(saved);
    InstructionFactory factory = new InstructionFactory();
    Instruction[] instructions =
        factory.readSequenceFrom(ShortBuffer.wrap(code), 0, code.length, indexedItems);
    return new DexCode(registerSize, insSize, outsSize, instructions, tries, handlers, debugInfo);
  }

  void populateIndexTables() {
    // Populate structures that are already sorted upon read.
    populateStrings();  // Depends on nothing.
    populateChecksums(); // Depends on Strings.
    populateTypes();  // Depends on Strings.
    populateFields();  // Depends on Types, and Strings.
    populateProtos();  // Depends on Types and Strings.
    populateMethods();  // Depends on Protos, Types, and Strings.
    populateMethodHandles(); // Depends on Methods and Fields
    populateCallSites(); // Depends on MethodHandles
  }

  private void populateStrings() {
    indexedItems.initializeStrings(stringIDs.length);
    for (int i = 0; i < stringIDs.length; i++) {
      indexedItems.setString(i, stringAt(i));
    }
  }

  private void populateMethodHandles() {
    DexSection dexSection = lookupSection(Constants.TYPE_METHOD_HANDLE_ITEM);
    indexedItems.initializeMethodHandles(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setMethodHandle(i, methodHandleAt(i));
    }
  }

  private void populateCallSites() {
    DexSection dexSection = lookupSection(Constants.TYPE_CALL_SITE_ID_ITEM);
    indexedItems.initializeCallSites(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setCallSites(i, callSiteAt(i));
    }
  }

  private void populateTypes() {
    DexSection dexSection = lookupSection(Constants.TYPE_TYPE_ID_ITEM);
    assert verifyOrderOfTypeIds(dexSection);
    indexedItems.initializeTypes(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setType(i, typeAt(i));
    }
  }

  private void populateChecksums() {
    ClassesChecksum parsedChecksums = new ClassesChecksum();
    for (int i = stringIDs.length - 1; i >= 0; i--) {
      DexString value = indexedItems.getString(i);
      if (ClassesChecksum.definitelyPrecedesChecksumMarker(value)) {
        break;
      }
      parsedChecksums.tryParseAndAppend(value);
    }
    this.checksums = parsedChecksums.getChecksums();
  }

  /**
   * From https://source.android.com/devices/tech/dalvik/dex-format#file-layout:
   *
   * <p>This list must be sorted by string_id index, and it must not contain any duplicate entries.
   */
  private boolean verifyOrderOfTypeIds(DexSection dexSection) {
    if (dexSection.length >= 2) {
      int initialOffset = dexSection.offset;
      dexReader.position(initialOffset);

      int prevStringIndex = dexReader.getUint();

      for (int index = 1; index < dexSection.length; index++) {
        int offset = initialOffset + Constants.TYPE_TYPE_ID_ITEM_SIZE * index;
        dexReader.position(offset);

        int stringIndex = dexReader.getUint();

        boolean isValidOrder = stringIndex > prevStringIndex;
        assert isValidOrder
            : String.format(
                "Out-of-order type ids (type #%s: `%s`, type #%s: `%s`)",
                index - 1,
                indexedItems.getString(prevStringIndex),
                index,
                indexedItems.getString(stringIndex));

        prevStringIndex = stringIndex;
      }
    }
    return true;
  }

  private void populateFields() {
    DexSection dexSection = lookupSection(Constants.TYPE_FIELD_ID_ITEM);
    assert verifyOrderOfFieldIds(dexSection);
    indexedItems.initializeFields(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setField(i, fieldAt(i));
    }
  }

  /**
   * From https://source.android.com/devices/tech/dalvik/dex-format#file-layout:
   *
   * <p>This list must be sorted, where the defining type (by type_id index) is the major order,
   * field name (by string_id index) is the intermediate order, and type (by type_id index) is the
   * minor order. The list must not contain any duplicate entries.
   */
  private boolean verifyOrderOfFieldIds(DexSection dexSection) {
    if (dexSection.length >= 2) {
      int initialOffset = dexSection.offset;
      dexReader.position(initialOffset);

      int prevHolderIndex = dexReader.getUshort();
      int prevTypeIndex = dexReader.getUshort();
      int prevNameIndex = dexReader.getUint();

      for (int index = 1; index < dexSection.length; index++) {
        int offset = initialOffset + Constants.TYPE_FIELD_ID_ITEM_SIZE * index;
        dexReader.position(offset);

        int holderIndex = dexReader.getUshort();
        int typeIndex = dexReader.getUshort();
        int nameIndex = dexReader.getUint();

        boolean isValidOrder;
        if (holderIndex == prevHolderIndex) {
          if (nameIndex == prevNameIndex) {
            isValidOrder = typeIndex > prevTypeIndex;
          } else {
            isValidOrder = nameIndex > prevNameIndex;
          }
        } else {
          isValidOrder = holderIndex > prevHolderIndex;
        }

        assert isValidOrder
            : String.format(
                "Out-of-order field ids (field #%s: `%s`, field #%s: `%s`)",
                index - 1,
                dexItemFactory
                    .createField(
                        indexedItems.getType(prevHolderIndex),
                        indexedItems.getType(prevTypeIndex),
                        indexedItems.getString(prevNameIndex))
                    .toSourceString(),
                index,
                dexItemFactory
                    .createField(
                        indexedItems.getType(holderIndex),
                        indexedItems.getType(typeIndex),
                        indexedItems.getString(nameIndex))
                    .toSourceString());

        prevHolderIndex = holderIndex;
        prevTypeIndex = typeIndex;
        prevNameIndex = nameIndex;
      }
    }
    return true;
  }

  private void populateProtos() {
    DexSection dexSection = lookupSection(Constants.TYPE_PROTO_ID_ITEM);
    indexedItems.initializeProtos(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setProto(i, protoAt(i));
    }
  }

  private void populateMethods() {
    DexSection dexSection = lookupSection(Constants.TYPE_METHOD_ID_ITEM);
    assert verifyOrderOfMethodIds(dexSection);
    indexedItems.initializeMethods(dexSection.length);
    for (int i = 0; i < dexSection.length; i++) {
      indexedItems.setMethod(i, methodAt(i));
    }
  }

  /**
   * From https://source.android.com/devices/tech/dalvik/dex-format#file-layout:
   *
   * <p>This list must be sorted, where the defining type (by type_id index) is the major order,
   * method name (by string_id index) is the intermediate order, and method prototype
   * (by proto_id index) is the minor order. The list must not contain any duplicate entries.
   *
   */
  private boolean verifyOrderOfMethodIds(DexSection dexSection) {
    if (dexSection.length >= 2) {
      int initialOffset = dexSection.offset;
      dexReader.position(initialOffset);

      int prevHolderIndex = dexReader.getUshort();
      int prevProtoIndex = dexReader.getUshort();
      int prevNameIndex = dexReader.getUint();

      for (int index = 1; index < dexSection.length; index++) {
        int offset = initialOffset + Constants.TYPE_METHOD_ID_ITEM_SIZE * index;
        dexReader.position(offset);

        int holderIndex = dexReader.getUshort();
        int protoIndex = dexReader.getUshort();
        int nameIndex = dexReader.getUint();

        boolean isValidOrder;
        if (holderIndex == prevHolderIndex) {
          if (nameIndex == prevNameIndex) {
            isValidOrder = protoIndex > prevProtoIndex;
          } else {
            isValidOrder = nameIndex > prevNameIndex;
          }
        } else {
          isValidOrder = holderIndex > prevHolderIndex;
        }

        assert isValidOrder
            : String.format(
                "Out-of-order method ids (method #%s: `%s`, method #%s: `%s`)",
                index - 1,
                dexItemFactory
                    .createMethod(
                        indexedItems.getType(prevHolderIndex),
                        indexedItems.getProto(prevProtoIndex),
                        indexedItems.getString(prevNameIndex))
                    .toSourceString(),
                index,
                dexItemFactory
                    .createMethod(
                        indexedItems.getType(holderIndex),
                        indexedItems.getProto(protoIndex),
                        indexedItems.getString(nameIndex))
                    .toSourceString());

        prevHolderIndex = holderIndex;
        prevProtoIndex = protoIndex;
        prevNameIndex = nameIndex;
      }
    }
    return true;
  }

  private DexString stringAt(int index) {
    final int offset = stringIDs[index];
    dexReader.position(offset);
    int size = dexReader.getUleb128();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte read;
    do {
      read = dexReader.get();
      os.write(read);
    } while (read != 0);
    return dexItemFactory.createString(size, os.toByteArray());
  }

  private DexType typeAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_TYPE_ID_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int offset = dexSection.offset + (Constants.TYPE_TYPE_ID_ITEM_SIZE * index);
    int stringIndex = dexReader.getUint(offset);
    return dexItemFactory.createType(indexedItems.getString(stringIndex));
  }

  private DexField fieldAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_FIELD_ID_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int offset = dexSection.offset + (Constants.TYPE_FIELD_ID_ITEM_SIZE * index);
    dexReader.position(offset);
    int classIndex = dexReader.getUshort();
    int typeIndex = dexReader.getUshort();
    int nameIndex = dexReader.getUint();
    DexType clazz = indexedItems.getType(classIndex);
    DexType type = indexedItems.getType(typeIndex);
    DexString name = indexedItems.getString(nameIndex);
    return dexItemFactory.createField(clazz, type, name);
  }

  private DexMethodHandle methodHandleAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_METHOD_HANDLE_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int offset = dexSection.offset + (Constants.TYPE_METHOD_HANDLE_ITEM_SIZE * index);
    dexReader.position(offset);
    MethodHandleType type = MethodHandleType.getKind(dexReader.getUshort());
    dexReader.getUshort(); // unused
    int indexFieldOrMethod = dexReader.getUshort();
    Descriptor<? extends DexItem, ? extends Descriptor<?, ?>> fieldOrMethod;
    switch (type) {
      case INSTANCE_GET:
      case INSTANCE_PUT:
      case STATIC_GET:
      case STATIC_PUT: {
        fieldOrMethod = indexedItems.getField(indexFieldOrMethod);
        break;
      }
      case INVOKE_CONSTRUCTOR:
      case INVOKE_DIRECT:
      case INVOKE_INTERFACE:
      case INVOKE_INSTANCE:
      case INVOKE_STATIC: {
        fieldOrMethod = indexedItems.getMethod(indexFieldOrMethod);
        break;
      }
      default:
        throw new AssertionError("Method handle type unsupported in a dex file.");
    }
    dexReader.getUshort(); // unused

    return dexItemFactory.createMethodHandle(
        type, fieldOrMethod, type == MethodHandleType.INVOKE_INTERFACE);
  }

  private DexCallSite callSiteAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_CALL_SITE_ID_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int callSiteOffset =
        dexReader.getUint(dexSection.offset + (Constants.TYPE_CALL_SITE_ID_ITEM_SIZE * index));
    DexEncodedArray callSiteEncodedArray = encodedArrayAt(callSiteOffset);
    DexValue[] values = callSiteEncodedArray.values;
    assert values[0] instanceof DexValueMethodHandle;
    assert values[1] instanceof DexValueString;
    assert values[2] instanceof DexValueMethodType;

    return dexItemFactory.createCallSite(
        ((DexValueString) values[1]).value,
        ((DexValueMethodType) values[2]).value,
        ((DexValueMethodHandle) values[0]).value,
        // 3 means first extra args
        Arrays.asList(Arrays.copyOfRange(values, 3, values.length)));
  }

  private DexProto protoAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_PROTO_ID_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int offset = dexSection.offset + (Constants.TYPE_PROTO_ID_ITEM_SIZE * index);
    dexReader.position(offset);
    int shortyIndex = dexReader.getUint();
    int returnTypeIndex = dexReader.getUint();
    int parametersOffsetIndex = dexReader.getUint();
    DexString shorty = indexedItems.getString(shortyIndex);
    DexType returnType = indexedItems.getType(returnTypeIndex);
    DexTypeList parameters = typeListAt(parametersOffsetIndex);
    return dexItemFactory.createProto(returnType, parameters, shorty);
  }

  private DexMethod methodAt(int index) {
    DexSection dexSection = lookupSection(Constants.TYPE_METHOD_ID_ITEM);
    if (index >= dexSection.length) {
      return null;
    }
    int offset = dexSection.offset + (Constants.TYPE_METHOD_ID_ITEM_SIZE * index);
    dexReader.position(offset);
    int classIndex = dexReader.getUshort();
    int protoIndex = dexReader.getUshort();
    int nameIndex = dexReader.getUint();
    return dexItemFactory.createMethod(
        indexedItems.getType(classIndex),
        indexedItems.getProto(protoIndex),
        indexedItems.getString(nameIndex));
  }

  private static class AnnotationsDirectory {

    private static final DexParameterAnnotation[] NO_PARAMETER_ANNOTATIONS =
        new DexParameterAnnotation[0];

    private static final DexFieldAnnotation[] NO_FIELD_ANNOTATIONS =
        new DexFieldAnnotation[0];

    private static final DexMethodAnnotation[] NO_METHOD_ANNOTATIONS =
        new DexMethodAnnotation[0];

    private static final AnnotationsDirectory THE_EMPTY_ANNOTATIONS_DIRECTORY =
        new AnnotationsDirectory(DexAnnotationSet.empty(),
            NO_FIELD_ANNOTATIONS, new DexMethodAnnotation[0],
            NO_PARAMETER_ANNOTATIONS);

    public final DexAnnotationSet clazz;
    public final DexFieldAnnotation[] fields;
    public final DexMethodAnnotation[] methods;
    public final DexParameterAnnotation[] parameters;

    AnnotationsDirectory(DexAnnotationSet clazz,
        DexFieldAnnotation[] fields,
        DexMethodAnnotation[] methods,
        DexParameterAnnotation[] parameters) {
      this.clazz = clazz == null ? DexAnnotationSet.empty() : clazz;
      this.fields = fields == null ? NO_FIELD_ANNOTATIONS : fields;
      this.methods = methods == null ? NO_METHOD_ANNOTATIONS : methods;
      this.parameters = parameters == null ? NO_PARAMETER_ANNOTATIONS : parameters;
    }

    public static AnnotationsDirectory empty() {
      return THE_EMPTY_ANNOTATIONS_DIRECTORY;
    }
  }

  private static class AttributesAndAnnotations {

    private final DexAnnotationSet originalAnnotations;
    private EnclosingMethodAttribute enclosingMethodAttribute = null;
    private List<InnerClassAttribute> innerClasses = null;
    private List<DexAnnotation> lazyAnnotations = null;

    public DexAnnotationSet getAnnotations() {
      if (lazyAnnotations != null) {
        int size = lazyAnnotations.size();
        return size == 0
            ? DexAnnotationSet.empty()
            : new DexAnnotationSet(lazyAnnotations.toArray(DexAnnotation.EMPTY_ARRAY));
      }
      return originalAnnotations;
    }

    public List<InnerClassAttribute> getInnerClasses() {
      return innerClasses == null ? Collections.emptyList() : innerClasses;
    }

    public EnclosingMethodAttribute getEnclosingMethodAttribute() {
      return enclosingMethodAttribute;
    }

    public AttributesAndAnnotations(
        DexType type, DexAnnotationSet annotations, DexItemFactory factory) {
      this.originalAnnotations = annotations;
      DexType enclosingClass = null;
      DexMethod enclosingMethod = null;
      List<DexType> memberClasses = null;

      for (int i = 0; i < annotations.annotations.length; i++) {
        DexAnnotation annotation = annotations.annotations[i];
        if (DexAnnotation.isEnclosingClassAnnotation(annotation, factory)) {
          ensureAnnotations(i);
          enclosingClass = DexAnnotation.getEnclosingClassFromAnnotation(annotation, factory);
        } else if (DexAnnotation.isEnclosingMethodAnnotation(annotation, factory)) {
          ensureAnnotations(i);
          enclosingMethod = DexAnnotation.getEnclosingMethodFromAnnotation(annotation, factory);
        } else if (DexAnnotation.isInnerClassAnnotation(annotation, factory)) {
          ensureAnnotations(i);
          if (innerClasses == null) {
            innerClasses = new ArrayList<>(annotations.annotations.length - i);
          }
          Pair<DexString, Integer> entry =
              DexAnnotation.getInnerClassFromAnnotation(annotation, factory);
          innerClasses.add(
              new InnerClassAttribute(entry.getSecond(), type, null, entry.getFirst()));
        } else if (DexAnnotation.isMemberClassesAnnotation(annotation, factory)) {
          ensureAnnotations(i);
          List<DexType> members = DexAnnotation.getMemberClassesFromAnnotation(annotation, factory);
          if (memberClasses == null) {
            memberClasses = members;
          } else {
            memberClasses.addAll(members);
          }
        } else {
          copyAnnotation(annotation);
        }
      }

      if (enclosingClass != null || enclosingMethod != null) {
        assert enclosingClass == null || enclosingMethod == null;
        if (enclosingMethod != null) {
          enclosingMethodAttribute = new EnclosingMethodAttribute(enclosingMethod);
        } else {
          InnerClassAttribute namedEnclosing = null;
          if (innerClasses != null) {
            for (InnerClassAttribute innerClass : innerClasses) {
              if (type == innerClass.getInner()) {
                // If inner-class is anonymous then we create an enclosing-method attribute.
                // Unfortunately we can't distinguish member classes from local classes, and thus
                // can't at this point conform to the spec which requires a enclosing-method
                // attribute iff the inner-class is anonymous or local. A local inner class will
                // thus be represented as an ordinary member class and given an inner-classes
                // entry below.
                namedEnclosing = innerClass.isNamed() ? innerClass : null;
                break;
              }
            }
          }
          if (namedEnclosing == null) {
            enclosingMethodAttribute = new EnclosingMethodAttribute(enclosingClass);
          } else {
            assert innerClasses != null;
            innerClasses.remove(namedEnclosing);
            innerClasses.add(
                new InnerClassAttribute(
                    namedEnclosing.getAccess(),
                    type,
                    enclosingClass,
                    namedEnclosing.getInnerName()));
          }
        }
      }

      if (memberClasses != null) {
        if (innerClasses == null) {
          innerClasses = new ArrayList<>(memberClasses.size());
        }
        for (DexType memberClass : memberClasses) {
          innerClasses.add(InnerClassAttribute.createUnknownNamedInnerClass(memberClass, type));
        }
      }
    }

    private void ensureAnnotations(int index) {
      if (lazyAnnotations == null) {
        lazyAnnotations = new ArrayList<>(originalAnnotations.annotations.length);
        lazyAnnotations.addAll(Arrays.asList(originalAnnotations.annotations).subList(0, index));
      }
    }

    private void copyAnnotation(DexAnnotation annotation) {
      if (lazyAnnotations != null) {
        lazyAnnotations.add(annotation);
      }
    }
  }
}
