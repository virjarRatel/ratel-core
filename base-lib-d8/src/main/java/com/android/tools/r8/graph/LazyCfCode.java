// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

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
import com.android.tools.r8.cf.code.CfThrow;
import com.android.tools.r8.cf.code.CfTryCatch;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.CfCode.LocalVariableInfo;
import com.android.tools.r8.graph.JarClassFileReader.ReparseContext;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.ProguardConfiguration;
import com.android.tools.r8.shaking.ProguardKeepAttributes;
import com.android.tools.r8.utils.InternalOptions;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class LazyCfCode extends Code {

  private static class JsrEncountered extends RuntimeException {
    public JsrEncountered(String s) {
      super(s);
    }
  }

  public LazyCfCode(
      DexMethod method, Origin origin, ReparseContext context, JarApplicationReader application) {
    this.origin = origin;
    this.context = context;
    this.application = application;
    context.codeList.add(this);
  }

  private final Origin origin;
  private JarApplicationReader application;
  private CfCode code;
  protected ReparseContext context;
  private boolean reachabilitySensitive = false;

  public void markReachabilitySensitive() {
    assert code == null;
    reachabilitySensitive = true;
  }

  @Override
  public boolean isCfCode() {
    return true;
  }

  @Override
  public LazyCfCode asLazyCfCode() {
    return this;
  }

  @Override
  public CfCode asCfCode() {
    if (code == null) {
      ReparseContext context = this.context;
      JarApplicationReader application = this.application;
      assert application != null;
      assert context != null;
      // The ClassCodeVisitor is in charge of setting this.context to null.
      try {
        parseCode(context, false);
      } catch (JsrEncountered e) {
        for (Code code : context.codeList) {
          code.asLazyCfCode().code = null;
          code.asLazyCfCode().context = context;
          code.asLazyCfCode().application = application;
        }
        try {
          parseCode(context, true);
        } catch (JsrEncountered e1) {
          throw new Unreachable(e1);
        }
      }
      assert verifyNoReparseContext(context.owner);
    }
    assert code != null;
    return code;
  }

  public void parseCode(ReparseContext context, boolean useJsrInliner) {
    int parsingOptions = getParsingOptions(application, reachabilitySensitive);
    ClassCodeVisitor classVisitor =
        new ClassCodeVisitor(context.owner, createCodeLocator(context), application, useJsrInliner);
    new ClassReader(context.classCache).accept(classVisitor, parsingOptions);
  }

  private void setCode(CfCode code) {
    assert this.code == null;
    assert this.context != null;
    this.code = code;
    this.context = null;
    this.application = null;
  }

  @Override
  protected int computeHashCode() {
    throw new Unimplemented();
  }

  @Override
  protected boolean computeEquals(Object other) {
    throw new Unimplemented();
  }

  @Override
  public boolean isEmptyVoidMethod() {
    return asCfCode().isEmptyVoidMethod();
  }

  @Override
  public int estimatedSizeForInlining() {
    return asCfCode().estimatedSizeForInlining();
  }

  @Override
  public boolean estimatedSizeForInliningAtMost(int threshold) {
    return asCfCode().estimatedSizeForInliningAtMost(threshold);
  }

  @Override
  public IRCode buildIR(DexEncodedMethod encodedMethod, AppView<?> appView, Origin origin) {
    return asCfCode().buildIR(encodedMethod, appView, origin);
  }

  @Override
  public IRCode buildInliningIR(
      DexEncodedMethod context,
      DexEncodedMethod encodedMethod,
      AppView<?> appView,
      ValueNumberGenerator valueNumberGenerator,
      Position callerPosition,
      Origin origin) {
    return asCfCode()
        .buildInliningIR(
            context, encodedMethod, appView, valueNumberGenerator, callerPosition, origin);
  }

  @Override
  public void registerCodeReferences(DexEncodedMethod method, UseRegistry registry) {
    asCfCode().registerCodeReferences(method, registry);
  }

  @Override
  public void registerArgumentReferences(DexEncodedMethod method, ArgumentUse registry) {
    asCfCode().registerArgumentReferences(method, registry);
  }

  @Override
  public String toString() {
    return asCfCode().toString();
  }

  @Override
  public String toString(DexEncodedMethod method, ClassNameMapper naming) {
    return asCfCode().toString(method, naming);
  }

  protected BiFunction<String, String, LazyCfCode> createCodeLocator(ReparseContext context) {
    return new DefaultCodeLocator(context, application);
  }

  private static class DefaultCodeLocator implements BiFunction<String, String, LazyCfCode> {
    private final ReparseContext context;
    private final JarApplicationReader application;
    private int methodIndex = 0;

    private DefaultCodeLocator(ReparseContext context, JarApplicationReader application) {
      this.context = context;
      this.application = application;
    }

    @Override
    public LazyCfCode apply(String name, String desc) {
      return context.codeList.get(methodIndex++).asLazyCfCode();
    }
  }

  private static class ClassCodeVisitor extends ClassVisitor {

    private final DexClass clazz;
    private final BiFunction<String, String, LazyCfCode> codeLocator;
    private final JarApplicationReader application;
    private boolean usrJsrInliner;

    ClassCodeVisitor(
        DexClass clazz,
        BiFunction<String, String, LazyCfCode> codeLocator,
        JarApplicationReader application,
        boolean useJsrInliner) {
      super(InternalOptions.ASM_VERSION);
      this.clazz = clazz;
      this.codeLocator = codeLocator;
      this.application = application;
      this.usrJsrInliner = useJsrInliner;
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      MethodAccessFlags flags = JarClassFileReader.createMethodAccessFlags(name, access);
      if (!flags.isAbstract() && !flags.isNative()) {
        LazyCfCode code = codeLocator.apply(name, desc);
        if (code != null) {
          DexMethod method = application.getMethod(clazz.type, name, desc);
          MethodCodeVisitor methodVisitor = new MethodCodeVisitor(application, method, code);
          if (!usrJsrInliner) {
            return methodVisitor;
          }
          return new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);
        }
      }
      return null;
    }
  }

  private static class MethodCodeVisitor extends MethodVisitor {
    private final JarApplicationReader application;
    private final DexItemFactory factory;
    private int maxStack;
    private int maxLocals;
    private List<CfInstruction> instructions;
    private List<CfTryCatch> tryCatchRanges;
    private List<LocalVariableInfo> localVariables;
    private final Map<DebugLocalInfo, DebugLocalInfo> canonicalDebugLocalInfo = new HashMap<>();
    private Map<Label, CfLabel> labelMap;
    private final LazyCfCode code;
    private final DexMethod method;

    MethodCodeVisitor(JarApplicationReader application, DexMethod method, LazyCfCode code) {
      super(InternalOptions.ASM_VERSION);
      assert code != null;
      this.application = application;
      this.factory = application.getFactory();
      this.code = code;
      this.method = method;
    }

    @Override
    public void visitCode() {
      maxStack = 0;
      maxLocals = 0;
      instructions = new ArrayList<>();
      tryCatchRanges = new ArrayList<>();
      localVariables = new ArrayList<>();
      labelMap = new IdentityHashMap<>();
    }

    @Override
    public void visitEnd() {
      code.setCode(
          new CfCode(
              method.holder, maxStack, maxLocals, instructions, tryCatchRanges, localVariables));
    }

    @Override
    public void visitFrame(
        int frameType, int nLocals, Object[] localTypes, int nStack, Object[] stackTypes) {
      assert frameType == Opcodes.F_NEW;
      Int2ReferenceSortedMap<FrameType> parsedLocals = parseLocals(nLocals, localTypes);
      List<FrameType> parsedStack = parseStack(nStack, stackTypes);
      instructions.add(new CfFrame(parsedLocals, parsedStack));
    }

    private Int2ReferenceSortedMap<FrameType> parseLocals(int typeCount, Object[] asmTypes) {
      Int2ReferenceSortedMap<FrameType> types = new Int2ReferenceAVLTreeMap<>();
      int i = 0;
      for (int j = 0; j < typeCount; j++) {
        Object localType = asmTypes[j];
        FrameType value = getFrameType(localType);
        types.put(i++, value);
        if (value.isWide()) {
          i++;
        }
      }
      return types;
    }

    private List<FrameType> parseStack(int nStack, Object[] stackTypes) {
      List<FrameType> dexStack = new ArrayList<>(nStack);
      for (int i = 0; i < nStack; i++) {
        dexStack.add(getFrameType(stackTypes[i]));
      }
      return dexStack;
    }

    private FrameType getFrameType(Object localType) {
      if (localType instanceof Label) {
        return FrameType.uninitializedNew(getLabel((Label) localType));
      } else if (localType == Opcodes.UNINITIALIZED_THIS) {
        return FrameType.uninitializedThis();
      } else if (localType == null || localType == Opcodes.TOP) {
        return FrameType.top();
      } else {
        return FrameType.initialized(parseAsmType(localType));
      }
    }

    private CfLabel getLabel(Label label) {
      return labelMap.computeIfAbsent(label, l -> new CfLabel());
    }

    private DexType parseAsmType(Object local) {
      assert local != null && local != Opcodes.TOP;
      if (local == Opcodes.INTEGER) {
        return factory.intType;
      } else if (local == Opcodes.FLOAT) {
        return factory.floatType;
      } else if (local == Opcodes.LONG) {
        return factory.longType;
      } else if (local == Opcodes.DOUBLE) {
        return factory.doubleType;
      } else if (local == Opcodes.NULL) {
        return DexItemFactory.nullValueType;
      } else if (local instanceof String) {
        return createTypeFromInternalType((String) local);
      } else {
        throw new Unreachable("Unexpected ASM type: " + local);
      }
    }

    private DexType createTypeFromInternalType(String local) {
      assert local.indexOf('.') == -1;
      return factory.createType(Type.getObjectType(local).getDescriptor());
    }

    @Override
    public void visitInsn(int opcode) {
      switch (opcode) {
        case Opcodes.NOP:
          instructions.add(new CfNop());
          break;
        case Opcodes.ACONST_NULL:
          instructions.add(new CfConstNull());
          break;
        case Opcodes.ICONST_M1:
        case Opcodes.ICONST_0:
        case Opcodes.ICONST_1:
        case Opcodes.ICONST_2:
        case Opcodes.ICONST_3:
        case Opcodes.ICONST_4:
        case Opcodes.ICONST_5:
          instructions.add(new CfConstNumber(opcode - Opcodes.ICONST_0, ValueType.INT));
          break;
        case Opcodes.LCONST_0:
        case Opcodes.LCONST_1:
          instructions.add(new CfConstNumber(opcode - Opcodes.LCONST_0, ValueType.LONG));
          break;
        case Opcodes.FCONST_0:
        case Opcodes.FCONST_1:
        case Opcodes.FCONST_2:
          instructions.add(
              new CfConstNumber(
                  Float.floatToRawIntBits(opcode - Opcodes.FCONST_0), ValueType.FLOAT));
          break;
        case Opcodes.DCONST_0:
        case Opcodes.DCONST_1:
          instructions.add(
              new CfConstNumber(
                  Double.doubleToRawLongBits(opcode - Opcodes.DCONST_0), ValueType.DOUBLE));
          break;
        case Opcodes.IALOAD:
        case Opcodes.LALOAD:
        case Opcodes.FALOAD:
        case Opcodes.DALOAD:
        case Opcodes.AALOAD:
        case Opcodes.BALOAD:
        case Opcodes.CALOAD:
        case Opcodes.SALOAD:
          instructions.add(new CfArrayLoad(getMemberTypeForOpcode(opcode)));
          break;
        case Opcodes.IASTORE:
        case Opcodes.LASTORE:
        case Opcodes.FASTORE:
        case Opcodes.DASTORE:
        case Opcodes.AASTORE:
        case Opcodes.BASTORE:
        case Opcodes.CASTORE:
        case Opcodes.SASTORE:
          instructions.add(new CfArrayStore(getMemberTypeForOpcode(opcode)));
          break;
        case Opcodes.POP:
        case Opcodes.POP2:
        case Opcodes.DUP:
        case Opcodes.DUP_X1:
        case Opcodes.DUP_X2:
        case Opcodes.DUP2:
        case Opcodes.DUP2_X1:
        case Opcodes.DUP2_X2:
        case Opcodes.SWAP:
          instructions.add(CfStackInstruction.fromAsm(opcode));
          break;
        case Opcodes.IADD:
        case Opcodes.LADD:
        case Opcodes.FADD:
        case Opcodes.DADD:
        case Opcodes.ISUB:
        case Opcodes.LSUB:
        case Opcodes.FSUB:
        case Opcodes.DSUB:
        case Opcodes.IMUL:
        case Opcodes.LMUL:
        case Opcodes.FMUL:
        case Opcodes.DMUL:
        case Opcodes.IDIV:
        case Opcodes.LDIV:
        case Opcodes.FDIV:
        case Opcodes.DDIV:
        case Opcodes.IREM:
        case Opcodes.LREM:
        case Opcodes.FREM:
        case Opcodes.DREM:
          instructions.add(CfArithmeticBinop.fromAsm(opcode));
          break;
        case Opcodes.INEG:
        case Opcodes.LNEG:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
          instructions.add(CfNeg.fromAsm(opcode));
          break;
        case Opcodes.ISHL:
        case Opcodes.LSHL:
        case Opcodes.ISHR:
        case Opcodes.LSHR:
        case Opcodes.IUSHR:
        case Opcodes.LUSHR:
        case Opcodes.IAND:
        case Opcodes.LAND:
        case Opcodes.IOR:
        case Opcodes.LOR:
        case Opcodes.IXOR:
        case Opcodes.LXOR:
          instructions.add(CfLogicalBinop.fromAsm(opcode));
          break;
        case Opcodes.I2L:
        case Opcodes.I2F:
        case Opcodes.I2D:
        case Opcodes.L2I:
        case Opcodes.L2F:
        case Opcodes.L2D:
        case Opcodes.F2I:
        case Opcodes.F2L:
        case Opcodes.F2D:
        case Opcodes.D2I:
        case Opcodes.D2L:
        case Opcodes.D2F:
        case Opcodes.I2B:
        case Opcodes.I2C:
        case Opcodes.I2S:
          instructions.add(CfNumberConversion.fromAsm(opcode));
          break;
        case Opcodes.LCMP:
        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
          instructions.add(CfCmp.fromAsm(opcode));
          break;
        case Opcodes.IRETURN:
          instructions.add(new CfReturn(ValueType.INT));
          break;
        case Opcodes.LRETURN:
          instructions.add(new CfReturn(ValueType.LONG));
          break;
        case Opcodes.FRETURN:
          instructions.add(new CfReturn(ValueType.FLOAT));
          break;
        case Opcodes.DRETURN:
          instructions.add(new CfReturn(ValueType.DOUBLE));
          break;
        case Opcodes.ARETURN:
          instructions.add(new CfReturn(ValueType.OBJECT));
          break;
        case Opcodes.RETURN:
          instructions.add(new CfReturnVoid());
          break;
        case Opcodes.ARRAYLENGTH:
          instructions.add(new CfArrayLength());
          break;
        case Opcodes.ATHROW:
          instructions.add(new CfThrow());
          break;
        case Opcodes.MONITORENTER:
          instructions.add(new CfMonitor(Monitor.Type.ENTER));
          break;
        case Opcodes.MONITOREXIT:
          instructions.add(new CfMonitor(Monitor.Type.EXIT));
          break;
        default:
          throw new Unreachable("Unknown instruction");
      }
    }

    private static MemberType getMemberTypeForOpcode(int opcode) {
      switch (opcode) {
        case Opcodes.IALOAD:
        case Opcodes.IASTORE:
          return MemberType.INT;
        case Opcodes.FALOAD:
        case Opcodes.FASTORE:
          return MemberType.FLOAT;
        case Opcodes.LALOAD:
        case Opcodes.LASTORE:
          return MemberType.LONG;
        case Opcodes.DALOAD:
        case Opcodes.DASTORE:
          return MemberType.DOUBLE;
        case Opcodes.AALOAD:
        case Opcodes.AASTORE:
          return MemberType.OBJECT;
        case Opcodes.BALOAD:
        case Opcodes.BASTORE:
          return MemberType.BOOLEAN_OR_BYTE;
        case Opcodes.CALOAD:
        case Opcodes.CASTORE:
          return MemberType.CHAR;
        case Opcodes.SALOAD:
        case Opcodes.SASTORE:
          return MemberType.SHORT;
        default:
          throw new Unreachable("Unexpected array opcode " + opcode);
      }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      switch (opcode) {
        case Opcodes.SIPUSH:
        case Opcodes.BIPUSH:
          instructions.add(new CfConstNumber(operand, ValueType.INT));
          break;
        case Opcodes.NEWARRAY:
          instructions.add(
              new CfNewArray(factory.createArrayType(1, arrayTypeDesc(operand, factory))));
          break;
        default:
          throw new Unreachable("Unexpected int opcode " + opcode);
      }
    }

    private static DexType arrayTypeDesc(int arrayTypeCode, DexItemFactory factory) {
      switch (arrayTypeCode) {
        case Opcodes.T_BOOLEAN:
          return factory.booleanType;
        case Opcodes.T_CHAR:
          return factory.charType;
        case Opcodes.T_FLOAT:
          return factory.floatType;
        case Opcodes.T_DOUBLE:
          return factory.doubleType;
        case Opcodes.T_BYTE:
          return factory.byteType;
        case Opcodes.T_SHORT:
          return factory.shortType;
        case Opcodes.T_INT:
          return factory.intType;
        case Opcodes.T_LONG:
          return factory.longType;
        default:
          throw new Unreachable("Unexpected array-type code " + arrayTypeCode);
      }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      ValueType type;
      switch (opcode) {
        case Opcodes.ILOAD:
        case Opcodes.ISTORE:
          type = ValueType.INT;
          break;
        case Opcodes.FLOAD:
        case Opcodes.FSTORE:
          type = ValueType.FLOAT;
          break;
        case Opcodes.LLOAD:
        case Opcodes.LSTORE:
          type = ValueType.LONG;
          break;
        case Opcodes.DLOAD:
        case Opcodes.DSTORE:
          type = ValueType.DOUBLE;
          break;
        case Opcodes.ALOAD:
        case Opcodes.ASTORE:
          type = ValueType.OBJECT;
          break;
        case Opcodes.RET:
          throw new JsrEncountered("RET should be handled by the ASM jsr inliner");
        default:
          throw new Unreachable("Unexpected VarInsn opcode: " + opcode);
      }
      if (Opcodes.ILOAD <= opcode && opcode <= Opcodes.ALOAD) {
        instructions.add(new CfLoad(type, var));
      } else {
        instructions.add(new CfStore(type, var));
      }
    }

    @Override
    public void visitTypeInsn(int opcode, String typeName) {
      DexType type = factory.createType(Type.getObjectType(typeName).getDescriptor());
      switch (opcode) {
        case Opcodes.NEW:
          instructions.add(new CfNew(type));
          break;
        case Opcodes.ANEWARRAY:
          instructions.add(new CfNewArray(factory.createArrayType(1, type)));
          break;
        case Opcodes.CHECKCAST:
          instructions.add(new CfCheckCast(type));
          break;
        case Opcodes.INSTANCEOF:
          instructions.add(new CfInstanceOf(type));
          break;
        default:
          throw new Unreachable("Unexpected TypeInsn opcode: " + opcode);
      }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      DexField field =
          factory.createField(createTypeFromInternalType(owner), factory.createType(desc), name);
      // TODO(mathiasr): Don't require CfFieldInstruction::declaringField. It is needed for proper
      // renaming in the backend, but it is not available here in the frontend.
      instructions.add(new CfFieldInstruction(opcode, field, field));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      DexMethod method = application.getMethod(owner, name, desc);
      instructions.add(new CfInvoke(opcode, method, itf));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      DexCallSite callSite =
          DexCallSite.fromAsmInvokeDynamic(application, method.holder, name, desc, bsm, bsmArgs);
      instructions.add(new CfInvokeDynamic(callSite));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      CfLabel target = getLabel(label);
      if (Opcodes.IFEQ <= opcode && opcode <= Opcodes.IF_ACMPNE) {
        if (opcode <= Opcodes.IFLE) {
          // IFEQ, IFNE, IFLT, IFGE, IFGT, or IFLE.
          instructions.add(new CfIf(ifType(opcode), ValueType.INT, target));
        } else {
          // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, or
          // IF_ACMPNE.
          ValueType valueType;
          if (opcode <= Opcodes.IF_ICMPLE) {
            valueType = ValueType.INT;
          } else {
            valueType = ValueType.OBJECT;
          }
          instructions.add(new CfIfCmp(ifType(opcode), valueType, target));
        }
      } else {
        // GOTO, JSR, IFNULL or IFNONNULL.
        switch (opcode) {
          case Opcodes.GOTO:
            instructions.add(new CfGoto(target));
            break;
          case Opcodes.IFNULL:
          case Opcodes.IFNONNULL:
            If.Type type = opcode == Opcodes.IFNULL ? If.Type.EQ : If.Type.NE;
            instructions.add(new CfIf(type, ValueType.OBJECT, target));
            break;
          case Opcodes.JSR:
            throw new JsrEncountered("JSR should be handled by the ASM jsr inliner");
          default:
            throw new Unreachable("Unexpected JumpInsn opcode: " + opcode);
        }
      }
    }

    private static If.Type ifType(int opcode) {
      switch (opcode) {
        case Opcodes.IFEQ:
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ACMPEQ:
          return If.Type.EQ;
        case Opcodes.IFNE:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ACMPNE:
          return If.Type.NE;
        case Opcodes.IFLT:
        case Opcodes.IF_ICMPLT:
          return If.Type.LT;
        case Opcodes.IFGE:
        case Opcodes.IF_ICMPGE:
          return If.Type.GE;
        case Opcodes.IFGT:
        case Opcodes.IF_ICMPGT:
          return If.Type.GT;
        case Opcodes.IFLE:
        case Opcodes.IF_ICMPLE:
          return If.Type.LE;
        default:
          throw new Unreachable("Unexpected If instruction opcode: " + opcode);
      }
    }

    @Override
    public void visitLabel(Label label) {
      instructions.add(getLabel(label));
    }

    @Override
    public void visitLdcInsn(Object cst) {
      if (cst instanceof Type) {
        Type type = (Type) cst;
        if (type.getSort() == Type.METHOD) {
          DexProto proto = application.getProto(type.getDescriptor());
          instructions.add(new CfConstMethodType(proto));
        } else {
          instructions.add(new CfConstClass(factory.createType(type.getDescriptor())));
        }
      } else if (cst instanceof String) {
        instructions.add(new CfConstString(factory.createString((String) cst)));
      } else if (cst instanceof Long) {
        instructions.add(new CfConstNumber((Long) cst, ValueType.LONG));
      } else if (cst instanceof Double) {
        long l = Double.doubleToRawLongBits((Double) cst);
        instructions.add(new CfConstNumber(l, ValueType.DOUBLE));
      } else if (cst instanceof Integer) {
        instructions.add(new CfConstNumber((Integer) cst, ValueType.INT));
      } else if (cst instanceof Float) {
        long i = Float.floatToRawIntBits((Float) cst);
        instructions.add(new CfConstNumber(i, ValueType.FLOAT));
      } else if (cst instanceof Handle) {
        instructions.add(
            new CfConstMethodHandle(
                DexMethodHandle.fromAsmHandle((Handle) cst, application, method.holder)));
      } else if (cst instanceof ConstantDynamic) {
        throw new CompilationError("Unsupported dynamic constant: " + cst.toString());
      } else {
        throw new CompilationError("Unsupported constant: " + cst.toString());
      }
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      instructions.add(new CfIinc(var, increment));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
      assert max == min + labels.length - 1;
      ArrayList<CfLabel> targets = new ArrayList<>(labels.length);
      for (Label label : labels) {
        targets.add(getLabel(label));
      }
      instructions.add(new CfSwitch(CfSwitch.Kind.TABLE, getLabel(dflt), new int[] {min}, targets));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      ArrayList<CfLabel> targets = new ArrayList<>(labels.length);
      for (Label label : labels) {
        targets.add(getLabel(label));
      }
      instructions.add(new CfSwitch(CfSwitch.Kind.LOOKUP, getLabel(dflt), keys, targets));
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      if (!application.options.isGeneratingDex()) {
        instructions.add(new CfMultiANewArray(factory.createType(desc), dims));
        return;
      }
      // When generating DEX code a multianewarray is desugared to a reflective creation.
      // The stack transformation is:
      //   ..., count1, ..., countN (where N = dims)
      //   ->
      //   ..., arrayref(of type : desc)
      //
      // This is unfolded to a call to java.lang.reflect.Array.newInstance to the same effect:
      // ..., count1, ..., countN
      visitLdcInsn(dims);
      // ..., count1, ..., countN, dims
      visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
      // ..., count1, ..., countN, dim-array
      for (int i = dims - 1; i >= 0; i--) {
        visitInsn(Opcodes.DUP_X1);
        // ..., count1, ..., dim-array, countN, dim-array
        visitInsn(Opcodes.SWAP);
        // ..., count1, ..., dim-array, dim-array, countN
        visitLdcInsn(i);
        // ..., count1, ..., dim-array, dim-array, countN, index
        visitInsn(Opcodes.SWAP);
        // ..., count1, ..., dim-array, dim-array, index, countN
        visitInsn(Opcodes.IASTORE);
        // ..., count1, ..., dim-array
      }
      visitLdcInsn(Type.getType(desc.substring(dims)));
      // ..., dim-array, dim-member-type
      visitInsn(Opcodes.SWAP);
      // ..., dim-member-type, dim-array
      visitMethodInsn(
          Opcodes.INVOKESTATIC,
          "java/lang/reflect/Array",
          "newInstance",
          "(Ljava/lang/Class;[I)Ljava/lang/Object;",
          false);
      // ..., ref
      visitTypeInsn(Opcodes.CHECKCAST, desc);
      // ..., arrayref(of type : desc)
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      List<DexType> guards =
          Collections.singletonList(
              type == null ? factory.throwableType : createTypeFromInternalType(type));
      List<CfLabel> targets = Collections.singletonList(getLabel(handler));
      tryCatchRanges.add(new CfTryCatch(getLabel(start), getLabel(end), guards, targets));
    }

    @Override
    public void visitLocalVariable(
        String name, String desc, String signature, Label start, Label end, int index) {
      DebugLocalInfo debugLocalInfo =
          canonicalize(
              new DebugLocalInfo(
                  factory.createString(name),
                  factory.createType(desc),
                  signature == null ? null : factory.createString(signature)));
      localVariables.add(
          new LocalVariableInfo(index, debugLocalInfo, getLabel(start), getLabel(end)));
    }

    private DebugLocalInfo canonicalize(DebugLocalInfo debugLocalInfo) {
      return canonicalDebugLocalInfo.computeIfAbsent(debugLocalInfo, o -> debugLocalInfo);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      instructions.add(new CfPosition(getLabel(start), new Position(line, null, method, null)));
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      assert maxStack >= 0;
      assert maxLocals >= 0;
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
    }
  }

  private static int getParsingOptions(
      JarApplicationReader application, boolean reachabilitySensitive) {
    int parsingOptions =
        application.options.testing.readInputStackMaps
            ? ClassReader.EXPAND_FRAMES
            : ClassReader.SKIP_FRAMES;

    ProguardConfiguration configuration = application.options.getProguardConfiguration();
    if (configuration != null && !configuration.isKeepParameterNames()) {
      ProguardKeepAttributes keep =
          application.options.getProguardConfiguration().getKeepAttributes();
      if (!application.options.getProguardConfiguration().isKeepParameterNames()
          && !keep.localVariableTable
          && !keep.localVariableTypeTable
          && !keep.lineNumberTable
          && !reachabilitySensitive) {
        parsingOptions |= ClassReader.SKIP_DEBUG;
      }
    }
    return parsingOptions;
  }

  @Override
  public boolean verifyNoInputReaders() {
    assert context == null && application == null;
    return true;
  }

  private static boolean verifyNoReparseContext(DexClass owner) {
    for (DexEncodedMethod method : owner.virtualMethods()) {
      Code code = method.getCode();
      assert code == null || code.verifyNoInputReaders();
    }
    for (DexEncodedMethod method : owner.directMethods()) {
      Code code = method.getCode();
      assert code == null || code.verifyNoInputReaders();
    }
    return true;
  }

  @Override
  public Int2ReferenceMap<DebugLocalInfo> collectParameterInfo(
      DexEncodedMethod encodedMethod, AppView<?> appView) {
    return asCfCode().collectParameterInfo(encodedMethod, appView);
  }
}
