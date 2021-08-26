// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda.kotlin;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.synthetic.SyntheticSourceCode;
import java.util.ArrayList;
import java.util.List;

final class KotlinLambdaVirtualMethodSourceCode extends SyntheticSourceCode {
  private final DexItemFactory factory;
  private final DexField idField;
  private final List<DexEncodedMethod> implMethods;

  KotlinLambdaVirtualMethodSourceCode(
      DexItemFactory factory,
      DexType groupClass,
      DexMethod method,
      DexField idField,
      List<DexEncodedMethod> implMethods,
      Position callerPosition) {
    super(groupClass, method, callerPosition);
    this.factory = factory;
    this.idField = idField;
    this.implMethods = implMethods;
  }

  @Override
  protected void prepareInstructions() {
    int implMethodCount = implMethods.size();
    // We generate a single switch on lambda $id value read from appropriate
    // field, and for each lambda id generate a call to appropriate method of
    // the lambda class. Since this methods are marked as 'force inline',
    // they are inlined by the inliner.

    // Return value register if needed.
    DexType returnType = proto.returnType;
    boolean returnsValue = returnType != factory.voidType;
    ValueType retValueType = returnsValue ? ValueType.fromDexType(returnType) : null;
    int retRegister = returnsValue ? nextRegister(retValueType) : -1;

    // Lambda id register to switch on.
    int idRegister = nextRegister(ValueType.INT);
    add(builder -> builder.addInstanceGet(idRegister, getReceiverRegister(), idField));

    // Switch on id.
    // Note that 'keys' and 'offsets' are just captured here and filled
    // in with values when appropriate basic blocks are created.
    int[] keys = new int[implMethodCount];
    int[] offsets = new int[implMethodCount];
    int[] fallthrough = new int[1]; // Array as a container for late initialization.
    int switchIndex = lastInstructionIndex();
    add(builder -> builder.addSwitch(idRegister, keys, fallthrough[0], offsets),
        builder -> endsSwitch(builder, switchIndex, fallthrough[0], offsets));

    // Fallthrough treated as unreachable.
    fallthrough[0] = nextInstructionIndex();
    int nullRegister = nextRegister(ValueType.OBJECT);
    add(builder -> builder.addNullConst(nullRegister));
    add(builder -> builder.addThrow(nullRegister), endsBlock);

    List<Value> arguments = new ArrayList<>(proto.parameters.values.length + 1);

    // Blocks for each lambda id.
    for (int i = 0; i < implMethodCount; i++) {
      keys[i] = i;
      DexEncodedMethod impl = implMethods.get(i);
      if (impl == null) {
        // Virtual method is missing in lambda class.
        offsets[i] = fallthrough[0];
        continue;
      }
      offsets[i] = nextInstructionIndex();

      // Emit fake call on `this` receiver.
      add(
          builder -> {
            if (arguments.isEmpty()) {
              arguments.add(builder.getReceiverValue());
              List<Value> argumentValues = builder.getArgumentValues();
              if (argumentValues != null) {
                arguments.addAll(builder.getArgumentValues());
              }
            }
            builder.addInvoke(
                Type.VIRTUAL, impl.method, impl.method.proto, arguments, false /* isInterface */);
          });

      // Handle return value if needed.
      if (returnsValue) {
        add(builder -> builder.addMoveResult(retRegister));
        add(builder -> builder.addReturn(retRegister), endsBlock);
      } else {
        add(IRBuilder::addReturn, endsBlock);
      }
    }
  }
}
