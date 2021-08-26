// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.cf.code.CfInvoke;
import com.android.tools.r8.code.InvokePolymorphicRange;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.conversion.CfBuilder;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.DefaultInliningOracle;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.ir.optimize.InliningConstraints;
import com.android.tools.r8.ir.optimize.inliner.WhyAreYouNotInliningReporter;
import java.util.Collection;
import java.util.List;

public class InvokePolymorphic extends InvokeMethod {

  private final DexProto proto;

  public InvokePolymorphic(DexMethod target, DexProto proto, Value result, List<Value> arguments) {
    super(target, result, arguments);
    this.proto = proto;
  }

  @Override
  public int opcode() {
    return Opcodes.INVOKE_POLYMORPHIC;
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public DexType getReturnType() {
    return proto.returnType;
  }

  @Override
  public Type getType() {
    return Type.POLYMORPHIC;
  }

  @Override
  protected String getTypeString() {
    return "Polymorphic";
  }

  public DexProto getProto() {
    return proto;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new InvokePolymorphicRange(
              firstRegister, argumentRegisters, getInvokedMethod(), getProto());
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new com.android.tools.r8.code.InvokePolymorphic(
              argumentRegistersCount,
              getInvokedMethod(),
              getProto(),
              individualArgumentRegisters[0], // C
              individualArgumentRegisters[1], // D
              individualArgumentRegisters[2], // E
              individualArgumentRegisters[3], // F
              individualArgumentRegisters[4]); // G
    }
    addInvokeAndMoveResult(instruction, builder);
  }

  @Override
  public void buildCf(CfBuilder builder) {
    DexMethod dexMethod = getInvokedMethod();
    DexItemFactory factory = builder.appView.dexItemFactory();
    // When we translate InvokeVirtual on MethodHandle/VarHandle into InvokePolymorphic,
    // we translate the invoked prototype into a generic prototype that simply accepts Object[].
    // To translate InvokePolymorphic back into InvokeVirtual, use the original prototype
    // that is stored in getProto().
    DexMethod method = factory.createMethod(dexMethod.holder, getProto(), dexMethod.name);
    builder.add(new CfInvoke(org.objectweb.asm.Opcodes.INVOKEVIRTUAL, method, false));
  }

  @Override
  public boolean identicalNonValueNonPositionParts(Instruction other) {
    return other.isInvokePolymorphic()
        && proto.equals(other.asInvokePolymorphic().proto)
        && super.identicalNonValueNonPositionParts(other);
  }

  @Override
  public boolean isInvokePolymorphic() {
    return true;
  }

  @Override
  public InvokePolymorphic asInvokePolymorphic() {
    return this;
  }

  @Override
  public DexEncodedMethod lookupSingleTarget(AppView<?> appView, DexType invocationContext) {
    // TODO(herhut): Implement lookup target for invokePolymorphic.
    return null;
  }

  @Override
  public Collection<DexEncodedMethod> lookupTargets(
      AppView<? extends AppInfoWithSubtyping> appView, DexType invocationContext) {
    // TODO(herhut): Implement lookup target for invokePolymorphic.
    return null;
  }

  @Override
  public ConstraintWithTarget inliningConstraint(
      InliningConstraints inliningConstraints, DexType invocationContext) {
    return inliningConstraints.forInvokePolymorphic(getInvokedMethod(), invocationContext);
  }

  @Override
  public InlineAction computeInlining(
      DexEncodedMethod singleTarget,
      Reason reason,
      DefaultInliningOracle decider,
      ClassInitializationAnalysis classInitializationAnalysis,
      WhyAreYouNotInliningReporter whyAreYouNotInliningReporter) {
    // We never determine a single target for invoke-polymorphic.
    if (singleTarget != null) {
      throw new Unreachable(
          "Unexpected invoke-polymorphic with `"
              + singleTarget.method.toSourceString()
              + "` as single target");
    }
    throw new Unreachable("Unexpected attempt to inline invoke that does not have a single target");
  }
}
