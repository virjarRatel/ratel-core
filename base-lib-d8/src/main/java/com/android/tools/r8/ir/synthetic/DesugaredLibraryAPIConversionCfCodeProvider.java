// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.cf.code.CfCheckCast;
import com.android.tools.r8.cf.code.CfConstNull;
import com.android.tools.r8.cf.code.CfConstString;
import com.android.tools.r8.cf.code.CfFieldInstruction;
import com.android.tools.r8.cf.code.CfIf;
import com.android.tools.r8.cf.code.CfInstanceOf;
import com.android.tools.r8.cf.code.CfInstruction;
import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.cf.code.CfLabel;
import com.android.tools.r8.cf.code.CfLoad;
import com.android.tools.r8.cf.code.CfNew;
import com.android.tools.r8.cf.code.CfReturn;
import com.android.tools.r8.cf.code.CfReturnVoid;
import com.android.tools.r8.cf.code.CfStackInstruction;
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.desugar.DesugaredLibraryAPIConverter;
import com.android.tools.r8.utils.StringDiagnostic;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;

public abstract class DesugaredLibraryAPIConversionCfCodeProvider extends SyntheticCfCodeProvider {

  DesugaredLibraryAPIConversionCfCodeProvider(AppView<?> appView, DexType holder) {
    super(appView, holder);
  }

  boolean shouldConvert(
      DexType type, DesugaredLibraryAPIConverter converter, DexString methodName) {
    if (!appView.rewritePrefix.hasRewrittenType(type)) {
      return false;
    }
    if (converter.canConvert(type)) {
      return true;
    }
    appView
        .options()
        .reporter
        .warning(
            new StringDiagnostic(
                "Desugared library API conversion failed for "
                    + type
                    + ", unexpected behavior for method "
                    + methodName
                    + "."));
    return false;
  }

  DexType vivifiedTypeFor(DexType type) {
    return DesugaredLibraryAPIConverter.vivifiedTypeFor(type, appView);
  }

  public static class APIConverterVivifiedWrapperCfCodeProvider
      extends DesugaredLibraryAPIConversionCfCodeProvider {

    DexField wrapperField;
    DexMethod forwardMethod;
    DesugaredLibraryAPIConverter converter;
    boolean itfCall;

    public APIConverterVivifiedWrapperCfCodeProvider(
        AppView<?> appView,
        DexMethod forwardMethod,
        DexField wrapperField,
        DesugaredLibraryAPIConverter converter,
        boolean itfCall) {
      super(appView, wrapperField.holder);
      this.forwardMethod = forwardMethod;
      this.wrapperField = wrapperField;
      this.converter = converter;
      this.itfCall = itfCall;
    }

    @Override
    public CfCode generateCfCode() {
      DexItemFactory factory = appView.dexItemFactory();
      List<CfInstruction> instructions = new ArrayList<>();
      // Wrapped value is a vivified type. Method uses type as external. Forward method should
      // use vivifiedTypes.

      instructions.add(new CfLoad(ValueType.fromDexType(wrapperField.holder), 0));
      instructions.add(new CfFieldInstruction(Opcodes.GETFIELD, wrapperField, wrapperField));
      int index = 1;
      int stackIndex = 1;
      DexType[] newParameters = forwardMethod.proto.parameters.values.clone();
      for (DexType param : forwardMethod.proto.parameters.values) {
        instructions.add(new CfLoad(ValueType.fromDexType(param), stackIndex));
        if (shouldConvert(param, converter, forwardMethod.name)) {
          instructions.add(
              new CfInvoke(
                  Opcodes.INVOKESTATIC,
                  converter.createConversionMethod(param, param, vivifiedTypeFor(param)),
                  false));
          newParameters[index - 1] = vivifiedTypeFor(param);
        }
        if (param == factory.longType || param == factory.doubleType) {
          stackIndex++;
        }
        stackIndex++;
        index++;
      }

      DexType returnType = forwardMethod.proto.returnType;
      DexType forwardMethodReturnType =
          appView.rewritePrefix.hasRewrittenType(returnType)
              ? vivifiedTypeFor(returnType)
              : returnType;

      DexProto newProto = factory.createProto(forwardMethodReturnType, newParameters);
      DexMethod newForwardMethod =
          factory.createMethod(wrapperField.type, newProto, forwardMethod.name);

      if (itfCall) {
        instructions.add(new CfInvoke(Opcodes.INVOKEINTERFACE, newForwardMethod, true));
      } else {
        instructions.add(new CfInvoke(Opcodes.INVOKEVIRTUAL, newForwardMethod, false));
      }

      if (shouldConvert(returnType, converter, forwardMethod.name)) {
        instructions.add(
            new CfInvoke(
                Opcodes.INVOKESTATIC,
                converter.createConversionMethod(
                    returnType, vivifiedTypeFor(returnType), returnType),
                false));
      }
      if (returnType == factory.voidType) {
        instructions.add(new CfReturnVoid());
      } else {
        instructions.add(new CfReturn(ValueType.fromDexType(returnType)));
      }
      return standardCfCodeFromInstructions(instructions);
    }
  }

  public static class APIConverterWrapperCfCodeProvider
      extends DesugaredLibraryAPIConversionCfCodeProvider {

    DexField wrapperField;
    DexMethod forwardMethod;
    DesugaredLibraryAPIConverter converter;
    boolean itfCall;

    public APIConverterWrapperCfCodeProvider(
        AppView<?> appView,
        DexMethod forwardMethod,
        DexField wrapperField,
        DesugaredLibraryAPIConverter converter,
        boolean itfCall) {
      //  Var wrapperField is null if should forward to receiver.
      super(appView, wrapperField == null ? forwardMethod.holder : wrapperField.holder);
      this.forwardMethod = forwardMethod;
      this.wrapperField = wrapperField;
      this.converter = converter;
      this.itfCall = itfCall;
    }

    @Override
    public CfCode generateCfCode() {
      DexItemFactory factory = appView.dexItemFactory();
      List<CfInstruction> instructions = new ArrayList<>();
      // Wrapped value is a type. Method uses vivifiedTypes as external. Forward method should
      // use types.

      // Var wrapperField is null if should forward to receiver.
      if (wrapperField == null) {
        instructions.add(new CfLoad(ValueType.fromDexType(forwardMethod.holder), 0));
      } else {
        instructions.add(new CfLoad(ValueType.fromDexType(wrapperField.holder), 0));
        instructions.add(new CfFieldInstruction(Opcodes.GETFIELD, wrapperField, wrapperField));
      }
      int stackIndex = 1;
      for (DexType param : forwardMethod.proto.parameters.values) {
        instructions.add(new CfLoad(ValueType.fromDexType(param), stackIndex));
        if (shouldConvert(param, converter, forwardMethod.name)) {
          instructions.add(
              new CfInvoke(
                  Opcodes.INVOKESTATIC,
                  converter.createConversionMethod(param, vivifiedTypeFor(param), param),
                  false));
        }
        if (param == factory.longType || param == factory.doubleType) {
          stackIndex++;
        }
        stackIndex++;
      }

      if (itfCall) {
        instructions.add(new CfInvoke(Opcodes.INVOKEINTERFACE, forwardMethod, true));
      } else {
        instructions.add(new CfInvoke(Opcodes.INVOKEVIRTUAL, forwardMethod, false));
      }

      DexType returnType = forwardMethod.proto.returnType;
      if (shouldConvert(returnType, converter, forwardMethod.name)) {
        instructions.add(
            new CfInvoke(
                Opcodes.INVOKESTATIC,
                converter.createConversionMethod(
                    returnType, returnType, vivifiedTypeFor(returnType)),
                false));
        returnType = vivifiedTypeFor(returnType);
      }
      if (returnType == factory.voidType) {
        instructions.add(new CfReturnVoid());
      } else {
        instructions.add(new CfReturn(ValueType.fromDexType(returnType)));
      }
      return standardCfCodeFromInstructions(instructions);
    }
  }

  public static class APIConverterWrapperConversionCfCodeProvider extends SyntheticCfCodeProvider {

    DexType argType;
    DexField reverseWrapperField;
    DexField wrapperField;

    public APIConverterWrapperConversionCfCodeProvider(
        AppView<?> appView, DexType argType, DexField reverseWrapperField, DexField wrapperField) {
      super(appView, wrapperField.holder);
      this.argType = argType;
      this.reverseWrapperField = reverseWrapperField;
      this.wrapperField = wrapperField;
    }

    @Override
    public CfCode generateCfCode() {
      DexItemFactory factory = appView.dexItemFactory();
      List<CfInstruction> instructions = new ArrayList<>();

      // if (arg == null) { return null };
      CfLabel nullDest = new CfLabel();
      instructions.add(new CfLoad(ValueType.fromDexType(argType), 0));
      instructions.add(new CfIf(If.Type.NE, ValueType.OBJECT, nullDest));
      instructions.add(new CfConstNull());
      instructions.add(new CfReturn(ValueType.OBJECT));
      instructions.add(nullDest);

      // This part is skipped if there is no reverse wrapper.
      // if (arg instanceOf ReverseWrapper) { return ((ReverseWrapper) arg).wrapperField};
      if (reverseWrapperField != null) {
        CfLabel unwrapDest = new CfLabel();
        instructions.add(new CfLoad(ValueType.fromDexType(argType), 0));
        instructions.add(new CfInstanceOf(reverseWrapperField.holder));
        instructions.add(new CfIf(If.Type.EQ, ValueType.INT, unwrapDest));
        instructions.add(new CfLoad(ValueType.fromDexType(argType), 0));
        instructions.add(new CfCheckCast(reverseWrapperField.holder));
        instructions.add(
            new CfFieldInstruction(Opcodes.GETFIELD, reverseWrapperField, reverseWrapperField));
        instructions.add(new CfReturn(ValueType.fromDexType(reverseWrapperField.type)));
        instructions.add(unwrapDest);
      }

      // return new Wrapper(wrappedValue);
      instructions.add(new CfNew(wrapperField.holder));
      instructions.add(CfStackInstruction.fromAsm(Opcodes.DUP));
      instructions.add(new CfLoad(ValueType.fromDexType(argType), 0));
      instructions.add(
          new CfInvoke(
              Opcodes.INVOKESPECIAL,
              factory.createMethod(
                  wrapperField.holder,
                  factory.createProto(factory.voidType, argType),
                  factory.initMethodName),
              false));
      instructions.add(new CfReturn(ValueType.fromDexType(wrapperField.holder)));
      return standardCfCodeFromInstructions(instructions);
    }
  }

  public static class APIConverterConstructorCfCodeProvider extends SyntheticCfCodeProvider {

    DexField wrapperField;

    public APIConverterConstructorCfCodeProvider(AppView<?> appView, DexField wrapperField) {
      super(appView, wrapperField.holder);
      this.wrapperField = wrapperField;
    }

    @Override
    public CfCode generateCfCode() {
      DexItemFactory factory = appView.dexItemFactory();
      List<CfInstruction> instructions = new ArrayList<>();
      instructions.add(new CfLoad(ValueType.fromDexType(wrapperField.holder), 0));
      instructions.add(
          new CfInvoke(
              Opcodes.INVOKESPECIAL,
              factory.createMethod(
                  factory.objectType,
                  factory.createProto(factory.voidType),
                  factory.initMethodName),
              false));
      instructions.add(new CfLoad(ValueType.fromDexType(wrapperField.holder), 0));
      instructions.add(new CfLoad(ValueType.fromDexType(wrapperField.type), 1));
      instructions.add(new CfFieldInstruction(Opcodes.PUTFIELD, wrapperField, wrapperField));
      instructions.add(new CfReturnVoid());
      return standardCfCodeFromInstructions(instructions);
    }
  }

  public static class APIConverterThrowRuntimeExceptionCfCodeProvider
      extends SyntheticCfCodeProvider {
    DexString message;

    public APIConverterThrowRuntimeExceptionCfCodeProvider(
        AppView<?> appView, DexString message, DexType holder) {
      super(appView, holder);
      this.message = message;
    }

    @Override
    public CfCode generateCfCode() {
      DexItemFactory factory = appView.dexItemFactory();
      List<CfInstruction> instructions = new ArrayList<>();
      instructions.add(new CfNew(factory.runtimeExceptionType));
      instructions.add(CfStackInstruction.fromAsm(Opcodes.DUP));
      instructions.add(new CfConstString(message));
      instructions.add(
          new CfInvoke(
              Opcodes.INVOKESPECIAL,
              factory.createMethod(
                  factory.runtimeExceptionType,
                  factory.createProto(factory.voidType, factory.stringType),
                  factory.initMethodName),
              false));
      instructions.add(new CfThrow());
      return standardCfCodeFromInstructions(instructions);
    }
  }
}
