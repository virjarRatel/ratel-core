package com.virjar.ratel.builder.injector;

import com.virjar.ratel.allcommon.ClassNames;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstructionFactory;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


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

    public static Method buildStaticContextMethod(String className, Method mehtod) {
        ArrayList<Instruction> instructions = new ArrayList<>(Collections.singletonList(
                ImmutableInstructionFactory.INSTANCE.makeInstruction35c(Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, getStaticContextMethodRef())
        ));
        MethodImplementation implementation = mehtod.getImplementation();
        MethodImplementation newImplementation = null;
        if (implementation != null) {
            int registerCount = implementation.getRegisterCount();
            for (Instruction instruction : mehtod.getImplementation().getInstructions()) {
                instructions.add(instruction);
            }
            newImplementation = new ImmutableMethodImplementation(registerCount, instructions, implementation.getTryBlocks(), implementation.getDebugItems());
        }

        return new ImmutableMethod(className, mehtod.getName(), mehtod.getParameters(), mehtod.getReturnType(), mehtod.getAccessFlags(), mehtod.getAnnotations(),
                mehtod.getHiddenApiRestrictions(), newImplementation);
    }


}