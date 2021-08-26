// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import com.android.tools.r8.cf.code.CfArithmeticBinop;
import com.android.tools.r8.cf.code.CfArrayLength;
import com.android.tools.r8.cf.code.CfArrayLoad;
import com.android.tools.r8.cf.code.CfArrayStore;
import com.android.tools.r8.cf.code.CfCheckCast;
import com.android.tools.r8.cf.code.CfCmp;
import com.android.tools.r8.cf.code.CfConstClass;
import com.android.tools.r8.cf.code.CfConstMethodHandle;
import com.android.tools.r8.cf.code.CfConstMethodType;
import com.android.tools.r8.cf.code.CfConstNull;
import com.android.tools.r8.cf.code.CfConstNumber;
import com.android.tools.r8.cf.code.CfConstString;
import com.android.tools.r8.cf.code.CfDexItemBasedConstString;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.cf.code.CfFrame;
import com.android.tools.r8.cf.code.CfFrame.FrameType;
import com.android.tools.r8.cf.code.CfGoto;
import com.android.tools.r8.cf.code.CfIf;
import com.android.tools.r8.cf.code.CfIfCmp;
import com.android.tools.r8.cf.code.CfIinc;
import com.android.tools.r8.cf.code.CfInstanceOf;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfInvokeDynamic;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfLogicalBinop;
import com.android.tools.r8.cf.code.CfMonitor;
import com.android.tools.r8.cf.code.CfMultiANewArray;
import com.android.tools.r8.cf.code.CfNeg;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfNewArray;
import com.android.tools.r8.cf.code.CfNop;
import com.android.tools.r8.cf.code.CfNumberConversion;
import com.android.tools.r8.cf.code.CfPosition;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfReturnVoid;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfStore;
import com.android.tools.r8.cf.code.CfSwitch;
import com.android.tools.r8.cf.code.CfSwitch.Kind;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.CfCode.LocalVariableInfo;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.DescriptorUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;

/**
 * Utility to print CF code and instructions.
 *
 * <p>This implementation prints the code formatted according to the Jasmin syntax.
 */
public class CfPrinter {

  private static final boolean PRINT_INSTRUCTION_INDEX = true;
  private static final boolean PRINT_INLINE_LOCALS = true;

  private final String indent;

  // Sorted list of labels.
  private final List<CfLabel> sortedLabels;
  // Map from label to its sorted-order index.
  private final Reference2IntMap<CfLabel> labelToIndex;
  // Map from sorted-order label index to the locals live at that label.
  private final List<List<LocalVariableInfo>> localsAtLabel;

  private final StringBuilder builder = new StringBuilder();
  private final ClassNameMapper mapper;

  private int nextInstructionIndex = 0;
  private final int instructionIndexSpace;

  /** Entry for printing single instructions without global knowledge (eg, label numbers). */
  public CfPrinter() {
    indent = "";
    labelToIndex = null;
    mapper = null;
    instructionIndexSpace = 0;

    sortedLabels = Collections.emptyList();
    localsAtLabel = Collections.emptyList();
  }

  /** Entry for printing a complete code object. */
  public CfPrinter(CfCode code) {
    this(code, null, null);
  }

  /** Entry for printing a complete method object. */
  public CfPrinter(CfCode code, DexEncodedMethod method, ClassNameMapper mapper) {
    this.mapper = mapper;
    indent = "  ";
    instructionIndexSpace = ("" + code.getInstructions().size()).length();
    labelToIndex = new Reference2IntOpenHashMap<>();
    sortedLabels = new ArrayList<>();
    for (CfInstruction instruction : code.getInstructions()) {
      if (instruction instanceof CfLabel) {
        labelToIndex.put((CfLabel) instruction, sortedLabels.size());
        sortedLabels.add((CfLabel) instruction);
      }
    }
    if (method != null) {
      builder.append(".method ");
      appendMethod(method.method);
      newline();
    }
    builder.append(".limit stack ").append(code.getMaxStack());
    newline();
    builder.append(".limit locals ").append(code.getMaxLocals());

    List<LocalVariableInfo> localVariables = getSortedLocalVariables(code);
    localsAtLabel = computeLocalsAtLabels(localVariables);

    for (LocalVariableInfo local : localVariables) {
      DebugLocalInfo info = local.getLocal();
      newline();
      builder
          .append(".var ")
          .append(local.getIndex())
          .append(" is ")
          .append(info.name)
          .append(" ")
          .append(info.type.toDescriptorString())
          .append(" from ")
          .append(getLabel(local.getStart()))
          .append(" to ")
          .append(getLabel(local.getEnd()));
      if (info.signature != null) {
        appendComment(info.signature.toString());
      }
    }
    for (CfTryCatch tryCatch : code.getTryCatchRanges()) {
      for (int i = 0; i < tryCatch.guards.size(); i++) {
        newline();
        DexType guard = tryCatch.guards.get(i);
        assert guard != null;
        builder
            .append(".catch ")
            .append(guard.getInternalName()) // Do we wan't to write 'all' here?
            .append(" from ")
            .append(getLabel(tryCatch.start))
            .append(" to ")
            .append(getLabel(tryCatch.end))
            .append(" using ")
            .append(getLabel(tryCatch.targets.get(i)));
      }
    }
    for (CfInstruction instruction : code.getInstructions()) {
      instruction.print(this);
    }
    newline();
    if (method != null) {
      builder.append(".end method");
      newline();
    }
  }

  private List<List<LocalVariableInfo>> computeLocalsAtLabels(
      List<LocalVariableInfo> localVariables) {
    if (!PRINT_INLINE_LOCALS) {
      return null;
    }
    List<List<LocalVariableInfo>> localsAtLabel = new ArrayList<>(sortedLabels.size());
    Set<LocalVariableInfo> openLocals = new HashSet<>();
    ListIterator<LocalVariableInfo> nextLocalVariableEntry = localVariables.listIterator();
    for (CfLabel orderedLabel : sortedLabels) {
      int thisIndex = labelToIndex.getInt(orderedLabel);
      openLocals.removeIf(o -> labelToIndex.getInt(o.getEnd()) <= thisIndex);
      while (nextLocalVariableEntry.hasNext()) {
        LocalVariableInfo next = nextLocalVariableEntry.next();
        int startIndex = labelToIndex.getInt(next.getStart());
        int endIndex = labelToIndex.getInt(next.getEnd());
        if (startIndex <= thisIndex) {
          if (thisIndex < endIndex) {
            openLocals.add(next);
          }
        } else {
          nextLocalVariableEntry.previous();
          break;
        }
      }
      ArrayList<LocalVariableInfo> locals = new ArrayList<>(openLocals);
      locals.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));
      localsAtLabel.add(locals);
    }
    return localsAtLabel;
  }

  private List<LocalVariableInfo> getSortedLocalVariables(CfCode code) {
    List<LocalVariableInfo> localVariables = new ArrayList<>(code.getLocalVariables());
    localVariables.sort(
        (a, b) -> {
          // Start with locals starting early.
          int first =
              Integer.compare(labelToIndex.getInt(a.getStart()), labelToIndex.getInt(b.getStart()));
          if (first != 0) {
            return first;
          }
          // Then locals with longer scope (ie, reverse order).
          int second =
              Integer.compare(labelToIndex.getInt(b.getEnd()), labelToIndex.getInt(a.getEnd()));
          if (second != 0) {
            return second;
          }
          // Finally, locals with smallest index.
          return Integer.compare(a.getIndex(), b.getIndex());
        });
    return localVariables;
  }

  private void print(String name) {
    indent();
    builder.append(name);
  }

  public void print(CfNop nop) {
    print("nop");
  }

  public void print(CfStackInstruction instruction) {
    switch (instruction.getOpcode()) {
      case Pop:
        print("pop");
        return;
      case Pop2:
        print("pop2");
        return;
      case Dup:
        print("dup");
        return;
      case DupX1:
        print("dup_x1");
        return;
      case DupX2:
        print("dup_x2");
        return;
      case Dup2:
        print("dup2");
        return;
      case Dup2X1:
        print("dup2_x1");
        return;
      case Dup2X2:
        print("dup2_x2");
        return;
      case Swap:
        print("swap");
        return;
      default:
        throw new Unreachable("Invalid instruction for CfStackInstruction");
    }
  }

  public void print(CfThrow insn) {
    print("athrow");
  }

  public void print(CfConstNull constNull) {
    print("aconst_null");
  }

  public void print(CfConstNumber constNumber) {
    indent();
    // TODO(zerny): Determine when the number matches the tconst_X instructions.
    switch (constNumber.getType()) {
      case INT:
        builder.append("ldc ").append(constNumber.getIntValue());
        break;
      case FLOAT:
        builder.append("ldc ").append(constNumber.getFloatValue());
        break;
      case LONG:
        builder.append("ldc_w ").append(constNumber.getLongValue());
        break;
      case DOUBLE:
        builder.append("ldc_w ").append(constNumber.getDoubleValue());
        break;
      default:
        throw new Unreachable("Unexpected const-number type: " + constNumber.getType());
    }
  }

  public void print(CfConstClass constClass) {
    indent();
    builder.append("ldc ");
    appendType(constClass.getType());
  }

  public void print(CfReturnVoid ret) {
    print("return");
  }

  public void print(CfReturn ret) {
    print(typePrefix(ret.getType()) + "return");
  }

  public void print(CfMonitor monitor) {
    print(monitor.getType() == Monitor.Type.ENTER ? "monitorenter" : "monitorexit");
  }

  public void print(CfArithmeticBinop arithmeticBinop) {
    print(opcodeName(arithmeticBinop.getAsmOpcode()));
  }

  public void print(CfCmp cmp) {
    print(opcodeName(cmp.getAsmOpcode()));
  }

  public void print(CfLogicalBinop logicalBinop) {
    print(opcodeName(logicalBinop.getAsmOpcode()));
  }

  public void print(CfNeg neg) {
    print(opcodeName(neg.getAsmOpcode()));
  }

  public void print(CfNumberConversion numberConversion) {
    print(opcodeName(numberConversion.getAsmOpcode()));
  }

  public void print(CfConstString constString) {
    indent();
    builder.append("ldc ").append(constString.getString());
  }

  public void print(CfDexItemBasedConstString constString) {
    indent();
    builder.append("ldc* ").append(constString.getItem().toString());
  }

  public void print(CfArrayLoad arrayLoad) {
    indent();
    builder.append(typePrefix(arrayLoad.getType())).append("aload");
  }

  public void print(CfArrayStore arrayStore) {
    indent();
    builder.append(typePrefix(arrayStore.getType())).append("astore");
  }

  public void print(CfInvoke invoke) {
    indent();
    builder.append(opcodeName(invoke.getOpcode())).append(' ');
    appendMethod(invoke.getMethod());
  }

  public void print(CfInvokeDynamic invoke) {
    indent();
    builder.append(opcodeName(Opcodes.INVOKEDYNAMIC)).append(' ');
    builder.append(invoke.getCallSite().methodName);
    builder.append(invoke.getCallSite().methodProto.toDescriptorString());
  }

  public void print(CfFrame frame) {
    indent();
    builder.append("; frame: [");
    {
      String separator = "";
      for (Entry<FrameType> entry : frame.getLocals().int2ReferenceEntrySet()) {
        builder.append(separator).append(entry.getIntKey()).append(':');
        print(entry.getValue());
        separator = ", ";
      }
    }
    builder.append("] [");
    {
      String separator = "";
      for (FrameType element : frame.getStack()) {
        builder.append(separator);
        print(element);
        separator = ", ";
      }
    }
    builder.append(']');
  }

  private void print(FrameType type) {
    if (type.isUninitializedNew()) {
      builder.append("uninitialized ").append(getLabel(type.getUninitializedLabel()));
    } else if (type.isInitialized()) {
      appendType(type.getInitializedType());
    } else {
      builder.append(type.toString());
    }
  }

  public void print(CfInstanceOf insn) {
    indent();
    builder.append("instanceof ");
    appendClass(insn.getType());
  }

  public void print(CfCheckCast insn) {
    indent();
    builder.append("checkcast ");
    appendClass(insn.getType());
  }

  public void print(CfFieldInstruction insn) {
    indent();
    switch (insn.getOpcode()) {
      case Opcodes.GETFIELD:
        builder.append("getfield ");
        break;
      case Opcodes.PUTFIELD:
        builder.append("putfield ");
        break;
      case Opcodes.GETSTATIC:
        builder.append("getstatic ");
        break;
      case Opcodes.PUTSTATIC:
        builder.append("putstatic ");
        break;
      default:
        throw new Unreachable("Unexpected field-instruction opcode " + insn.getOpcode());
    }
    appendField(insn.getField());
    builder.append(' ');
    appendDescriptor(insn.getField().type);
  }

  public void print(CfNew newInstance) {
    indent();
    builder.append("new ");
    appendClass(newInstance.getType());
  }

  public void print(CfNewArray newArray) {
    indent();
    String elementDescriptor = newArray.getType().toDescriptorString().substring(1);
    if (newArray.getType().isPrimitiveArrayType()) {
      // Primitive arrays are formatted as the Java type: int, byte, etc.
      builder.append("newarray ");
      builder.append(DescriptorUtils.descriptorToJavaType(elementDescriptor));
    } else {
      builder.append("anewarray ");
      if (elementDescriptor.charAt(0) == '[') {
        // Arrays of arrays are formatted using the descriptor syntax.
        builder.append(elementDescriptor);
      } else {
        // Arrays of class types are formatted using the "internal name".
        builder.append(Type.getType(elementDescriptor).getInternalName());
      }
    }
  }

  public void print(CfMultiANewArray multiANewArray) {
    indent();
    builder.append("multianewarray ");
    appendClass(multiANewArray.getType());
    builder.append(' ').append(multiANewArray.getDimensions());
  }

  public void print(CfArrayLength arrayLength) {
    print("arraylength");
  }

  public void print(CfLabel label) {
    newline();
    instructionIndex();
    builder.append(getLabel(label)).append(':');
    if (PRINT_INLINE_LOCALS) {
      int labelNumber = labelToIndex.getInt(label);
      List<LocalVariableInfo> locals = localsAtLabel.get(labelNumber);
      appendComment(
          "locals: "
              + String.join(
                  ", ",
                  locals.stream().map(LocalVariableInfo::toString).collect(Collectors.toList())));
    }
  }

  public void print(CfPosition instruction) {
    Position position = instruction.getPosition();
    indent();
    builder.append(".line ").append(position.line);
    if (position.file != null || position.callerPosition != null) {
      appendComment(position.toString());
    }
  }

  public void print(CfGoto jump) {
    indent();
    builder.append("goto ").append(getLabel(jump.getTarget()));
  }

  private String ifPostfix(If.Type kind) {
    return kind.toString().toLowerCase();
  }

  public void print(CfIf conditional) {
    indent();
    if (conditional.getType().isObject()) {
      builder.append("if").append(conditional.getKind() == If.Type.EQ ? "null" : "nonnull");
    } else {
      builder.append("if").append(ifPostfix(conditional.getKind()));
    }
    builder.append(' ').append(getLabel(conditional.getTarget()));
  }

  public void print(CfIfCmp conditional) {
    indent();
    builder
        .append(conditional.getType().isObject() ? "if_acmp" : "if_icmp")
        .append(ifPostfix(conditional.getKind()))
        .append(' ')
        .append(getLabel(conditional.getTarget()));
  }

  public void print(CfSwitch cfSwitch) {
    indent();
    Kind kind = cfSwitch.getKind();
    builder.append(kind == Kind.LOOKUP ? "lookup" : "table").append("switch");
    IntList keys = cfSwitch.getKeys();
    List<CfLabel> targets = cfSwitch.getSwitchTargets();
    for (int i = 0; i < targets.size(); i++) {
      indent();
      int key = kind == Kind.LOOKUP ? keys.getInt(i) : (keys.getInt(0) + i);
      builder
          .append("  ")
          .append(key)
          .append(": ")
          .append(getLabel(targets.get(i)));
    }
    indent();
    builder
        .append("  default: ")
        .append(getLabel(cfSwitch.getDefaultTarget()));
  }

  public void print(CfLoad load) {
    printPrefixed(load.getType(), "load", load.getLocalIndex());
  }

  public void print(CfStore store) {
    printPrefixed(store.getType(), "store", store.getLocalIndex());
  }

  public void print(CfIinc instruction) {
    indent();
    builder
        .append("iinc ")
        .append(instruction.getLocalIndex())
        .append(' ')
        .append(instruction.getIncrement());
  }

  private void printPrefixed(ValueType type, String instruction, int local) {
    indent();
    builder.append(typePrefix(type)).append(instruction).append(' ').append(local);
  }

  private char typePrefix(ValueType type) {
    switch (type) {
      case OBJECT:
        return 'a';
      case INT:
        return 'i';
      case FLOAT:
        return 'f';
      case LONG:
        return 'l';
      case DOUBLE:
        return 'd';
      default:
        throw new Unreachable("Unexpected type for prefix: " + type);
    }
  }

  public char typePrefix(MemberType type) {
    switch (type) {
      case OBJECT:
        return 'a';
      case BOOLEAN_OR_BYTE:
        return 'b';
      case CHAR:
        return 'c';
      case SHORT:
        return 's';
      case INT:
        return 'i';
      case FLOAT:
        return 'f';
      case LONG:
        return 'l';
      case DOUBLE:
        return 'd';
      default:
        throw new Unreachable("Unexpected member type for prefix: " + type);
    }
  }

  public char typePrefix(NumericType type) {
    switch (type) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return 'i';
      case FLOAT:
        return 'f';
      case LONG:
        return 'l';
      case DOUBLE:
        return 'd';
      default:
        throw new Unreachable("Unexpected numeric type for prefix: " + type);
    }
  }

  public void print(CfConstMethodHandle handle) {
    indent();
    builder.append("ldc ");
    builder.append(handle.getHandle().toString());
  }

  public void print(CfConstMethodType type) {
    indent();
    builder.append("ldc ");
    builder.append(type.getType().toString());
  }

  private String getLabel(CfLabel label) {
    return labelToIndex != null ? ("L" + labelToIndex.getInt(label)) : "L?";
  }

  private void newline() {
    if (builder.length() > 0) {
      builder.append('\n');
    }
  }

  private void instructionIndex() {
    if (PRINT_INSTRUCTION_INDEX && instructionIndexSpace > 0) {
      builder.append(String.format("%" + instructionIndexSpace + "d: ", nextInstructionIndex++));
    }
  }

  private void indent() {
    newline();
    instructionIndex();
    builder.append(indent);
  }

  private void comment(String comment) {
    indent();
    builder.append("; ").append(comment);
  }

  private void appendComment(String comment) {
    builder.append(" ; ").append(comment);
  }

  private void appendDescriptor(DexType type) {
    if (mapper != null) {
      builder.append(DescriptorUtils.javaTypeToDescriptor(mapper.originalNameOf(type)));
      return;
    }
    builder.append(type.toDescriptorString());
  }

  private void appendType(DexType type) {
    if (type.isArrayType() || type.isClassType()) {
      appendClass(type);
    } else {
      builder.append(type);
    }
  }

  private void appendClass(DexType type) {
    assert type.isArrayType() || type.isClassType();
    if (mapper == null) {
      builder.append(type.getInternalName());
    } else if (type == DexItemFactory.nullValueType) {
      builder.append("NULL");
    } else {
      builder.append(
          DescriptorUtils.descriptorToInternalName(
              DescriptorUtils.javaTypeToDescriptor(mapper.originalNameOf(type))));
    }
  }

  private void appendField(DexField field) {
    if (mapper != null) {
      builder.append(mapper.originalSignatureOf(field).toString());
      return;
    }
    appendClass(field.holder);
    builder.append('/').append(field.name);
  }

  private void appendMethod(DexMethod method) {
    if (mapper != null) {
      MethodSignature signature = mapper.originalSignatureOf(method);
      builder.append(mapper.originalNameOf(method.holder)).append('.');
      builder.append(signature.name).append(signature.toDescriptor());
      return;
    }
    builder.append(method.qualifiedName());
    builder.append(method.proto.toDescriptorString());
  }

  private String opcodeName(int opcode) {
    return Printer.OPCODES[opcode].toLowerCase();
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
