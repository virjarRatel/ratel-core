// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.jar;

import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.GraphLenseLookupResult;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.TryCatchBlockNode;

// This visitor can be used to determine if a piece of jar code has any instructions that the
// inliner would not be willing to inline. This can be used to determine if a method can be force
// inlined although its IR is still not available.
//
// Note that this class has only been implemented for the hooks in InliningConstraints that may
// return a non-ALWAYS inlining constraint (e.g., InliningConstraints.forReturn is not called).
public class InliningConstraintVisitor extends MethodVisitor {

  private final JarApplicationReader application;
  private final AppView<AppInfoWithLiveness> appView;
  private final GraphLense graphLense;
  private final InliningConstraints inliningConstraints;
  private final DexEncodedMethod method;
  private final DexType invocationContext;

  private ConstraintWithTarget constraint;

  public InliningConstraintVisitor(
      JarApplicationReader application,
      AppView<AppInfoWithLiveness> appView,
      GraphLense graphLense,
      DexEncodedMethod method,
      DexType invocationContext) {
    super(ASM_VERSION);
    assert graphLense.isContextFreeForMethods();
    this.application = application;
    this.appView = appView;
    this.graphLense = graphLense;
    this.inliningConstraints = new InliningConstraints(appView, graphLense);
    this.method = method;
    this.invocationContext = invocationContext;

    // Model a synchronized method as having a monitor instruction.
    this.constraint = method.accessFlags.isSynchronized()
        ? inliningConstraints.forMonitor() : ConstraintWithTarget.ALWAYS;
  }

  public void disallowStaticInterfaceMethodCalls() {
    inliningConstraints.disallowStaticInterfaceMethodCalls();
  }

  public ConstraintWithTarget getConstraint() {
    return constraint;
  }

  private void updateConstraint(ConstraintWithTarget other) {
    constraint = ConstraintWithTarget.meet(constraint, other, appView);
  }

  // Used to signal that the result is ready, such that we do not need to visit all instructions of
  // the method, if we can see early on that it cannot be inlined anyway.
  public boolean isFinished() {
    return constraint == ConstraintWithTarget.NEVER;
  }

  public void accept(TryCatchBlockNode tryCatchBlock) {
    // Model a try-catch as a move-exception instruction.
    updateConstraint(inliningConstraints.forMoveException());
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    DexField field = application.getField(owner, name, desc);
    switch (opcode) {
      case Opcodes.GETFIELD:
        updateConstraint(inliningConstraints.forInstanceGet(field, invocationContext));
        break;

      case Opcodes.PUTFIELD:
        updateConstraint(inliningConstraints.forInstancePut(field, invocationContext));
        break;

      case Opcodes.GETSTATIC:
        updateConstraint(inliningConstraints.forStaticGet(field, invocationContext));
        break;

      case Opcodes.PUTSTATIC:
        updateConstraint(inliningConstraints.forStaticPut(field, invocationContext));
        break;

      default:
        throw new Unreachable("Unexpected opcode " + opcode);
    }
  }

  @Override
  public void visitLdcInsn(Object cst) {
    if (cst instanceof Type && ((Type) cst).getSort() != Type.METHOD) {
      DexType type = application.getType((Type) cst);
      updateConstraint(inliningConstraints.forConstClass(type, invocationContext));
    } else if (cst instanceof Handle) {
      updateConstraint(inliningConstraints.forConstMethodHandle());
    } else if (cst instanceof ConstantDynamic) {
      throw new CompilationError("Unsupported dynamic constant: " + cst.toString());
    } else {
      updateConstraint(inliningConstraints.forConstInstruction());
    }
  }

  @Override
  public void visitInvokeDynamicInsn(
      String name,
      String descriptor,
      Handle bootstrapMethodHandle,
      Object... bootstrapMethodArguments) {
    updateConstraint(inliningConstraints.forInvokeCustom());
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    DexType ownerType = application.getTypeFromName(owner);
    DexMethod target = application.getMethod(ownerType, name, desc);
    updateConstraint(
        getConstraintForInvoke(
            opcode, target, graphLense, appView, inliningConstraints, invocationContext));
  }

  public static ConstraintWithTarget getConstraintForInvoke(
      int opcode,
      DexMethod target,
      GraphLense graphLense,
      AppView<?> appView,
      InliningConstraints inliningConstraints,
      DexType invocationContext) {
    // Find the DEX invocation type.
    Invoke.Type type;
    switch (opcode) {
      case Opcodes.INVOKEINTERFACE:
        // Could have changed to an invoke-virtual instruction due to vertical class merging
        // (if an interface is merged into a class).
        type = graphLense.lookupMethod(target, null, Invoke.Type.INTERFACE).getType();
        assert type == Invoke.Type.INTERFACE || type == Invoke.Type.VIRTUAL;
        break;

      case Opcodes.INVOKESPECIAL:
        if (appView.dexItemFactory().isConstructor(target)) {
          type = Invoke.Type.DIRECT;
          assert noNeedToUseGraphLense(target, type, graphLense);
        } else if (target.holder == invocationContext) {
          // The method could have been publicized.
          type = graphLense.lookupMethod(target, null, Invoke.Type.DIRECT).getType();
          assert type == Invoke.Type.DIRECT || type == Invoke.Type.VIRTUAL;
        } else {
          // This is a super call. Note that the vertical class merger translates some invoke-super
          // instructions to invoke-direct. However, when that happens, the invoke instruction and
          // the target method end up being in the same class, and therefore, we will allow inlining
          // it. The result of using type=SUPER below will be the same, since it leads to the
          // inlining constraint SAMECLASS.
          // TODO(christofferqa): Consider using graphLense.lookupMethod (to do this, we need the
          // context for the graph lense, though).
          type = Invoke.Type.SUPER;
          assert noNeedToUseGraphLense(target, type, graphLense);
        }
        break;

      case Opcodes.INVOKESTATIC: {
        // Static invokes may have changed as a result of horizontal class merging.
        GraphLenseLookupResult lookup = graphLense.lookupMethod(target, null, Invoke.Type.STATIC);
        target = lookup.getMethod();
        type = lookup.getType();
        break;
      }

      case Opcodes.INVOKEVIRTUAL: {
        type = Invoke.Type.VIRTUAL;
          // Instructions that target a private method in the same class translates to
          // invoke-direct.
          if (target.holder == invocationContext) {
            DexClass clazz = appView.definitionFor(target.holder);
          if (clazz != null && clazz.lookupDirectMethod(target) != null) {
            type = Invoke.Type.DIRECT;
          }
        }

        // Virtual invokes may have changed to interface invokes as a result of member rebinding.
        GraphLenseLookupResult lookup = graphLense.lookupMethod(target, null, type);
        target = lookup.getMethod();
        type = lookup.getType();
        break;
      }

      default:
        throw new Unreachable("Unexpected opcode " + opcode);
    }

    return inliningConstraints.forInvoke(target, type, invocationContext);
  }

  private static boolean noNeedToUseGraphLense(
      DexMethod method, Invoke.Type type, GraphLense graphLense) {
    assert graphLense.lookupMethod(method, null, type).getType() == type;
    return true;
  }

  @Override
  public void visitInsn(int opcode) {
    switch (opcode) {
      case Opcodes.MONITORENTER:
      case Opcodes.MONITOREXIT:
        updateConstraint(inliningConstraints.forMonitor());
        break;

      default:
        // All instructions here lead to the inlining constraint ALWAYS.
    }
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    DexType type = application.getTypeFromDescriptor(desc);
    updateConstraint(inliningConstraints.forInvokeMultiNewArray(type, invocationContext));
  }

  @Override
  public void visitTypeInsn(int opcode, String typeName) {
    DexType type = application.getTypeFromName(typeName);
    switch (opcode) {
      case Opcodes.ANEWARRAY:
        updateConstraint(inliningConstraints.forNewArrayEmpty(type, invocationContext));
        break;

      case Opcodes.CHECKCAST:
        updateConstraint(inliningConstraints.forCheckCast(type, invocationContext));
        break;

      case Opcodes.INSTANCEOF:
        updateConstraint(inliningConstraints.forInstanceOf(type, invocationContext));
        break;

      case Opcodes.NEW:
        updateConstraint(inliningConstraints.forNewInstance(type, invocationContext));
        break;

      default:
        throw new Unreachable("Unexpected opcode " + opcode);
    }
  }
}
