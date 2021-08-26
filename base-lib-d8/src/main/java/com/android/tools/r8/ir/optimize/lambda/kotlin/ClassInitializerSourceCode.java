// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.synthetic.SyntheticSourceCode;
import com.google.common.collect.Lists;
import java.util.List;

final class ClassInitializerSourceCode extends SyntheticSourceCode {
  private final DexItemFactory factory;
  private final KotlinLambdaGroup group;

  ClassInitializerSourceCode(
      DexMethod method, DexItemFactory factory, KotlinLambdaGroup group, Position callerPosition) {
    super(null, method, callerPosition);
    assert method.proto.returnType == factory.voidType;
    assert method.proto.parameters == DexTypeList.empty();
    this.factory = factory;
    this.group = group;
  }

  @Override
  protected void prepareInstructions() {
    DexType groupClassType = group.getGroupClassType();
    DexMethod lambdaConstructorMethod = factory.createMethod(groupClassType,
        factory.createProto(factory.voidType, factory.intType), factory.constructorMethodName);

    int instance = nextRegister(ValueType.OBJECT);
    int lambdaId = nextRegister(ValueType.INT);
    List<ValueType> argTypes = Lists.newArrayList(ValueType.OBJECT, ValueType.INT);
    List<Integer> argRegisters = Lists.newArrayList(instance, lambdaId);

    group.forEachLambda(
        info -> {
          DexType lambda = info.clazz.type;
          if (group.isSingletonLambda(lambda)) {
            int id = group.lambdaId(lambda);
            add(builder -> builder.addNewInstance(instance, groupClassType));
            add(builder -> builder.addConst(TypeLatticeElement.INT, lambdaId, id));
            add(
                builder ->
                    builder.addInvoke(
                        Type.DIRECT,
                        lambdaConstructorMethod,
                        lambdaConstructorMethod.proto,
                        argTypes,
                        argRegisters,
                        false /* isInterface*/));
            add(
                builder ->
                    builder.addStaticPut(instance, group.getSingletonInstanceField(factory, id)));
          }
        });

    assert this.nextInstructionIndex() > 0 : "no single field initialized";
    add(IRBuilder::addReturn);
  }
}
