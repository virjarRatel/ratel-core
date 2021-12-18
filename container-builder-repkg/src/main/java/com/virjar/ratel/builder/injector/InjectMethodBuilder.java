package com.virjar.ratel.builder.injector;

import com.virjar.ratel.allcommon.ClassNames;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstructionFactory;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by ljh102 on 2017/1/30.
 * 创建需要注入的方法
 */
public class InjectMethodBuilder {

    private static MethodReference getStaticContextMethodRef() {
        String bootStrapCintNativeName = ClassNames.INJECT_REBUILD_BOOTSTRAP_CINT.getClassName().replaceAll("\\.", "/");
        return new ImmutableMethodReference("L" + bootStrapCintNativeName + ";", "startup", null, "V");
    }

    public static Method buildStaticContextMethod(String className) {
        ArrayList<ImmutableInstruction> instructions = new ArrayList<>(
                Arrays.asList(ImmutableInstructionFactory.INSTANCE.makeInstruction35c(Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, getStaticContextMethodRef()),
                        ImmutableInstructionFactory.INSTANCE.makeInstruction10x(Opcode.RETURN_VOID))
        );

        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(0, instructions, null, null);
        return new ImmutableMethod(className, "<clinit>", new ArrayList<>(), "V", AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(), null, null, methodImpl);
    }

    public static Method renameOriginCInitMethod(Method origin) {
        return new ImmutableMethod(origin.getDefiningClass(),
                "cinit_" + ThreadLocalRandom.current().nextInt(100),
                origin.getParameters(),
                origin.getReturnType(),
                AccessFlags.STATIC.getValue(),
                origin.getAnnotations(),
                origin.getHiddenApiRestrictions(),
                origin.getImplementation()
        );
    }


    public static Method buildStaticContextMethod(String className, Method backupCInitMethod) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        // 调用平头哥的初始化
        instructions.add(ImmutableInstructionFactory.INSTANCE.makeInstruction35c(
                Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, getStaticContextMethodRef())
        );

        // 调用原来的静态代码块
        ImmutableMethodReference backupCInitMethodReference = new ImmutableMethodReference(
                backupCInitMethod.getDefiningClass(), backupCInitMethod.getName(), null, "V");
        instructions.add(ImmutableInstructionFactory.INSTANCE.makeInstruction35c(
                Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, backupCInitMethodReference)
        );

        // 函数返回
        instructions.add(ImmutableInstructionFactory.INSTANCE.makeInstruction10x(Opcode.RETURN_VOID));

        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(0, instructions, null, null);
        return new ImmutableMethod(className, "<clinit>", new ArrayList<>(), "V", AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(), null, null, methodImpl);
    }


}