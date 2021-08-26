// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.code.Const;
import com.android.tools.r8.code.Const16;
import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.ConstHigh16;
import com.android.tools.r8.code.ConstString;
import com.android.tools.r8.code.ConstStringJumbo;
import com.android.tools.r8.code.ConstWide16;
import com.android.tools.r8.code.ConstWide32;
import com.android.tools.r8.code.FillArrayData;
import com.android.tools.r8.code.FillArrayDataPayload;
import com.android.tools.r8.code.Format35c;
import com.android.tools.r8.code.Format3rc;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.InvokeDirect;
import com.android.tools.r8.code.InvokeDirectRange;
import com.android.tools.r8.code.InvokeInterface;
import com.android.tools.r8.code.InvokeInterfaceRange;
import com.android.tools.r8.code.InvokeStatic;
import com.android.tools.r8.code.InvokeStaticRange;
import com.android.tools.r8.code.InvokeSuper;
import com.android.tools.r8.code.InvokeSuperRange;
import com.android.tools.r8.code.InvokeVirtual;
import com.android.tools.r8.code.InvokeVirtualRange;
import com.android.tools.r8.code.NewArray;
import com.android.tools.r8.code.Sget;
import com.android.tools.r8.code.SgetBoolean;
import com.android.tools.r8.code.SgetByte;
import com.android.tools.r8.code.SgetChar;
import com.android.tools.r8.code.SgetObject;
import com.android.tools.r8.code.SgetShort;
import com.android.tools.r8.code.SgetWide;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.ir.code.SingleConstant;
import com.android.tools.r8.ir.code.WideConstant;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * This class is deprecated and should not be used. It is a temporary solution to use R8 to analyze
 * dex files and compute resource shrinker related bits.
 *
 * <p>Users or this API should implement {@link ReferenceChecker} interface, and through callbacks
 * in that interface, they will be notified when element relevant for resource shrinking is found.
 *
 * <p>This class extracts all integer constants and string constants, which might refer to resource.
 * More specifically, we look for the following while analyzing dex:
 * <ul>
 *   <li>const instructions that might load integers or strings
 *   <li>static fields that have an initial value. This initial value might be integer, string,
 *   or array of integers.
 *   <li>integer array payloads. Only payloads referenced in fill-array-data instructions will be
 *   processed. More specifically, if a payload is referenced in fill-array-data, and we are able
 *   to determine that array is not array of integers, payload will be ignored. Otherwise, it will
 *   be processed once fill-array-data-payload instruction is encountered.
 *   <li>all annotations (class, field, method) that contain annotation element whose value is
 *   integer, string or array of integers are processed.
 * </ul>
 *
 * <p>Please note that switch payloads are not analyzed. Although they might contain integer
 * constants, ones referring to resource ids would have to be loaded in the code analyzed in list
 * above.
 *
 * <p>Usage of this feature is intentionally not supported from the command line.
 */

// TODO(b/121121779) Remove keep if possible.
@Deprecated
@Keep
final public class ResourceShrinker {

  @Keep
  public final static class Command extends BaseCommand {

    Command(AndroidApp app) {
      super(app);
    }

    @Override
    InternalOptions getInternalOptions() {
      return new InternalOptions();
    }
  }

  @Keep
  public final static class Builder extends BaseCommand.Builder<Command, Builder> {

    @Override
    Builder self() {
      return this;
    }

    @Override
    Command makeCommand() {
      return new Command(getAppBuilder().build());
    }
  }

  /**
   * Classes that would like to process data relevant to resource shrinking should implement this
   * interface.
   */
  @KeepForSubclassing
  public interface ReferenceChecker {

    /**
     * Returns if the class with specified internal name should be processed. Typically,
     * resource type classes like R$drawable, R$styleable etc. should be skipped.
     */
    boolean shouldProcess(String internalName);

    void referencedInt(int value);

    void referencedString(String value);

    void referencedStaticField(String internalName, String fieldName);

    void referencedMethod(String internalName, String methodName, String methodDescriptor);
  }

  private static final class DexClassUsageVisitor {

    private final DexProgramClass classDef;
    private final ReferenceChecker callback;

    DexClassUsageVisitor(DexProgramClass classDef, ReferenceChecker callback) {
      this.classDef = classDef;
      this.callback = callback;
    }

    public void visit() {
      if (!callback.shouldProcess(classDef.type.getInternalName())) {
        return;
      }

      for (DexEncodedField field : classDef.staticFields()) {
        DexValue staticValue = field.getStaticValue();
        if (staticValue != null) {
          processFieldValue(staticValue);
        }
      }

      for (DexEncodedMethod method : classDef.allMethodsSorted()) {
        processMethod(method);
      }

      if (classDef.hasAnnotations()) {
        processAnnotations(classDef);
      }
    }

    private void processFieldValue(DexValue value) {
      if (value instanceof DexValue.DexValueString) {
        callback.referencedString(((DexValue.DexValueString) value).value.toString());
      } else if (value instanceof DexValue.DexValueInt) {
        int constantValue = ((DexValue.DexValueInt) value).getValue();
        callback.referencedInt(constantValue);
      } else if (value instanceof DexValue.DexValueArray) {
        DexValue.DexValueArray arrayEncodedValue = (DexValue.DexValueArray) value;
        for (DexValue encodedValue : arrayEncodedValue.getValues()) {
          if (encodedValue instanceof DexValue.DexValueInt) {
            int constantValue = ((DexValue.DexValueInt) encodedValue).getValue();
            callback.referencedInt(constantValue);
          }
        }
      }
    }

    private void processMethod(DexEncodedMethod method) {
      Code implementation = method.getCode();
      if (implementation != null) {

        // Tracks the offsets of integer array payloads.
        final Set<Integer> methodIntArrayPayloadOffsets = Sets.newHashSet();
        // First we collect payloads, and then we process them because payload can be before the
        // fill-array-data instruction referencing it.
        final List<FillArrayDataPayload> payloads = Lists.newArrayList();

        Instruction[] instructions = implementation.asDexCode().instructions;
        int current = 0;
        while (current < instructions.length) {
          Instruction instruction = instructions[current];
          if (isIntConstInstruction(instruction)) {
            processIntConstInstruction(instruction);
          } else if (isStringConstInstruction(instruction)) {
            processStringConstantInstruction(instruction);
          } else if (isGetStatic(instruction)) {
            processGetStatic(instruction);
          } else if (isInvokeInstruction(instruction)) {
            processInvokeInstruction(instruction);
          } else if (isInvokeRangeInstruction(instruction)) {
            processInvokeRangeInstruction(instruction);
          } else if (instruction instanceof FillArrayData) {
            processFillArray(instructions, current, methodIntArrayPayloadOffsets);
          } else if (instruction instanceof FillArrayDataPayload) {
            payloads.add((FillArrayDataPayload) instruction);
          }
          current++;
        }

        for (FillArrayDataPayload payload : payloads) {
          if (isIntArrayPayload(payload, methodIntArrayPayloadOffsets)) {
            processIntArrayPayload(payload);
          }
        }
      }
    }

    private void processAnnotations(DexProgramClass classDef) {
      Stream<DexAnnotation> instanceFieldAnnotations =
          classDef.instanceFields().stream()
              .filter(DexEncodedField::hasAnnotation)
              .flatMap(f -> Arrays.stream(f.annotations.annotations));
      Stream<DexAnnotation> staticFieldAnnotations =
          classDef.staticFields().stream()
              .filter(DexEncodedField::hasAnnotation)
              .flatMap(f -> Arrays.stream(f.annotations.annotations));
      Stream<DexAnnotation> virtualMethodAnnotations =
          classDef.virtualMethods().stream()
              .filter(DexEncodedMethod::hasAnnotation)
              .flatMap(m -> Arrays.stream(m.annotations.annotations));
      Stream<DexAnnotation> directMethodAnnotations =
          classDef.directMethods().stream()
              .filter(DexEncodedMethod::hasAnnotation)
              .flatMap(m -> Arrays.stream(m.annotations.annotations));
      Stream<DexAnnotation> classAnnotations = Arrays.stream(classDef.annotations.annotations);

      Streams.concat(
          instanceFieldAnnotations,
          staticFieldAnnotations,
          virtualMethodAnnotations,
          directMethodAnnotations,
          classAnnotations)
          .forEach(annotation -> {
            for (DexAnnotationElement element : annotation.annotation.elements) {
              DexValue value = element.value;
              processAnnotationValue(value);
            }
          });
    }

    private void processIntArrayPayload(Instruction instruction) {
      FillArrayDataPayload payload = (FillArrayDataPayload) instruction;

      for (int i = 0; i < payload.data.length / 2; i++) {
        int intValue = payload.data[2 * i + 1] << 16 | payload.data[2 * i];
        callback.referencedInt(intValue);
      }
    }

    private boolean isIntArrayPayload(
        Instruction instruction, Set<Integer> methodIntArrayPayloadOffsets) {
      if (!(instruction instanceof FillArrayDataPayload)) {
        return false;
      }

      FillArrayDataPayload payload = (FillArrayDataPayload) instruction;
      return methodIntArrayPayloadOffsets.contains(payload.getOffset());
    }

    private void processFillArray(
        Instruction[] instructions, int current, Set<Integer> methodIntArrayPayloadOffsets) {
      FillArrayData fillArrayData = (FillArrayData) instructions[current];
      if (current > 0 && instructions[current - 1] instanceof NewArray) {
        NewArray newArray = (NewArray) instructions[current - 1];
        if (!Objects.equals(newArray.getType().descriptor.toString(), "[I")) {
          return;
        }
        // Typically, new-array is right before fill-array-data. If not, assume referenced array is
        // of integers. This can be improved later, but for now we make sure no ints are missed.
      }

      methodIntArrayPayloadOffsets.add(fillArrayData.getPayloadOffset() + fillArrayData.offset);
    }

    private void processAnnotationValue(DexValue value) {
      if (value instanceof DexValue.DexValueInt) {
        DexValue.DexValueInt dexValueInt = (DexValue.DexValueInt) value;
        callback.referencedInt(dexValueInt.value);
      } else if (value instanceof DexValue.DexValueString) {
        DexValue.DexValueString dexValueString = (DexValue.DexValueString) value;
        callback.referencedString(dexValueString.value.toString());
      } else if (value instanceof DexValue.DexValueArray) {
        DexValue.DexValueArray dexValueArray = (DexValue.DexValueArray) value;
        for (DexValue dexValue : dexValueArray.getValues()) {
          processAnnotationValue(dexValue);
        }
      } else if (value instanceof DexValue.DexValueAnnotation) {
        DexValue.DexValueAnnotation dexValueAnnotation = (DexValue.DexValueAnnotation) value;
        for (DexAnnotationElement element : dexValueAnnotation.value.elements) {
          processAnnotationValue(element.value);
        }
      }
    }

    private boolean isIntConstInstruction(Instruction instruction) {
      int opcode = instruction.getOpcode();
      return opcode == Const4.OPCODE
          || opcode == Const16.OPCODE
          || opcode == Const.OPCODE
          || opcode == ConstWide32.OPCODE
          || opcode == ConstHigh16.OPCODE
          || opcode == ConstWide16.OPCODE;
    }

    private void processIntConstInstruction(Instruction instruction) {
      assert isIntConstInstruction(instruction);

      int constantValue;
      if (instruction instanceof SingleConstant) {
        SingleConstant singleConstant = (SingleConstant) instruction;
        constantValue = singleConstant.decodedValue();
      } else if (instruction instanceof WideConstant) {
        WideConstant wideConstant = (WideConstant) instruction;
        if (((int) wideConstant.decodedValue()) != wideConstant.decodedValue()) {
          // We care only about values that fit in int range.
          return;
        }
        constantValue = (int) wideConstant.decodedValue();
      } else {
        throw new AssertionError("Not an int const instruction.");
      }

      callback.referencedInt(constantValue);
    }

    private boolean isStringConstInstruction(Instruction instruction) {
      int opcode = instruction.getOpcode();
      return opcode == ConstString.OPCODE || opcode == ConstStringJumbo.OPCODE;
    }

    private void processStringConstantInstruction(Instruction instruction) {
      assert isStringConstInstruction(instruction);

      String constantValue;
      if (instruction instanceof ConstString) {
        ConstString constString = (ConstString) instruction;
        constantValue = constString.getString().toString();
      } else if (instruction instanceof ConstStringJumbo) {
        ConstStringJumbo constStringJumbo = (ConstStringJumbo) instruction;
        constantValue = constStringJumbo.getString().toString();
      } else {
        throw new AssertionError("Not a string constant instruction.");
      }

      callback.referencedString(constantValue);
    }

    private boolean isGetStatic(Instruction instruction) {
      int opcode = instruction.getOpcode();
      return opcode == Sget.OPCODE
          || opcode == SgetBoolean.OPCODE
          || opcode == SgetByte.OPCODE
          || opcode == SgetChar.OPCODE
          || opcode == SgetObject.OPCODE
          || opcode == SgetShort.OPCODE
          || opcode == SgetWide.OPCODE;
    }

    private void processGetStatic(Instruction instruction) {
      assert isGetStatic(instruction);

      DexField field;
      if (instruction instanceof Sget) {
        Sget sget = (Sget) instruction;
        field = sget.getField();
      } else if (instruction instanceof SgetBoolean) {
        SgetBoolean sgetBoolean = (SgetBoolean) instruction;
        field = sgetBoolean.getField();
      } else if (instruction instanceof SgetByte) {
        SgetByte sgetByte = (SgetByte) instruction;
        field = sgetByte.getField();
      } else if (instruction instanceof SgetChar) {
        SgetChar sgetChar = (SgetChar) instruction;
        field = sgetChar.getField();
      } else if (instruction instanceof SgetObject) {
        SgetObject sgetObject = (SgetObject) instruction;
        field = sgetObject.getField();
      } else if (instruction instanceof SgetShort) {
        SgetShort sgetShort = (SgetShort) instruction;
        field = sgetShort.getField();
      } else if (instruction instanceof SgetWide) {
        SgetWide sgetWide = (SgetWide) instruction;
        field = sgetWide.getField();
      } else {
        throw new AssertionError("Not a get static instruction");
      }

      callback.referencedStaticField(field.holder.getInternalName(), field.name.toString());
    }

    private boolean isInvokeInstruction(Instruction instruction) {
      int opcode = instruction.getOpcode();
      return opcode == InvokeVirtual.OPCODE
          || opcode == InvokeSuper.OPCODE
          || opcode == InvokeDirect.OPCODE
          || opcode == InvokeStatic.OPCODE
          || opcode == InvokeInterface.OPCODE;
    }

    private void processInvokeInstruction(Instruction instruction) {
      assert isInvokeInstruction(instruction);

      Format35c ins35c = (Format35c) instruction;
      DexMethod method = (DexMethod) ins35c.BBBB;

      callback.referencedMethod(
          method.holder.getInternalName(),
          method.name.toString(),
          method.proto.toDescriptorString());
    }

    private boolean isInvokeRangeInstruction(Instruction instruction) {
      int opcode = instruction.getOpcode();
      return opcode == InvokeVirtualRange.OPCODE
          || opcode == InvokeSuperRange.OPCODE
          || opcode == InvokeDirectRange.OPCODE
          || opcode == InvokeStaticRange.OPCODE
          || opcode == InvokeInterfaceRange.OPCODE;
    }

    private void processInvokeRangeInstruction(Instruction instruction) {
      assert isInvokeRangeInstruction(instruction);

      Format3rc ins3rc = (Format3rc) instruction;
      DexMethod method = (DexMethod) ins3rc.BBBB;

      callback.referencedMethod(
          method.holder.getInternalName(),
          method.name.toString(),
          method.proto.toDescriptorString());
    }
  }

  public static void run(Command command, ReferenceChecker callback)
      throws IOException, ExecutionException {
    AndroidApp inputApp = command.getInputApp();
    Timing timing = new Timing("resource shrinker analyzer");
    DexApplication dexApplication =
        new ApplicationReader(inputApp, command.getInternalOptions(), timing).read();
    for (DexProgramClass programClass : dexApplication.classes()) {
      new DexClassUsageVisitor(programClass, callback).visit();
    }
  }
}
