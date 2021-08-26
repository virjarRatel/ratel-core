// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.synthetic;

import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.ir.code.ValueType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.utils.BooleanUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

// Source code representing simple forwarding method.
public final class ForwardMethodSourceCode extends SyntheticSourceCode {

  public static Builder builder(DexMethod method) {
    return new Builder(method);
  }

  public static class Builder {

    private DexType receiver;
    private DexMethod method;
    private DexMethod originalMethod;
    private DexType targetReceiver;
    private DexMethod target;
    private Invoke.Type invokeType;
    private boolean castResult;
    private boolean isInterface;
    private boolean extraNullParameter;

    public Builder(DexMethod method) {
      this.method = method;
      this.originalMethod = method;
    }

    public Builder setReceiver(DexType receiver) {
      this.receiver = receiver;
      return this;
    }

    public Builder setMethod(DexMethod method) {
      this.method = method;
      return this;
    }

    public Builder setOriginalMethod(DexMethod originalMethod) {
      this.originalMethod = originalMethod;
      return this;
    }

    public Builder setTargetReceiver(DexType targetReceiver) {
      this.targetReceiver = targetReceiver;
      return this;
    }

    public Builder setTarget(DexMethod target) {
      this.target = target;
      return this;
    }

    public Builder setInvokeType(Type invokeType) {
      this.invokeType = invokeType;
      return this;
    }

    public Builder setCastResult() {
      this.castResult = true;
      return this;
    }

    public Builder setIsInterface(boolean isInterface) {
      this.isInterface = isInterface;
      return this;
    }

    public Builder setExtraNullParameter() {
      this.extraNullParameter = true;
      return this;
    }

    public ForwardMethodSourceCode build(Position callerPosition) {
      return new ForwardMethodSourceCode(
          receiver,
          method,
          originalMethod,
          targetReceiver,
          target,
          invokeType,
          callerPosition,
          isInterface,
          castResult,
          extraNullParameter);
    }
  }

  private final DexType targetReceiver;
  private final DexMethod target;
  private final Invoke.Type invokeType;
  private final boolean castResult;
  private final boolean isInterface;
  private final boolean extraNullParameter;

  protected ForwardMethodSourceCode(
      DexType receiver,
      DexMethod method,
      DexMethod originalMethod,
      DexType targetReceiver,
      DexMethod target,
      Type invokeType,
      Position callerPosition,
      boolean isInterface,
      boolean castResult,
      boolean extraNullParameter) {
    super(receiver, method, callerPosition, originalMethod);
    assert (targetReceiver == null) == (invokeType == Invoke.Type.STATIC);

    this.target = target;
    this.targetReceiver = targetReceiver;
    this.invokeType = invokeType;
    this.isInterface = isInterface;
    this.castResult = castResult;
    this.extraNullParameter = extraNullParameter;
    assert checkSignatures();

    switch (invokeType) {
      case DIRECT:
      case STATIC:
      case SUPER:
      case INTERFACE:
      case VIRTUAL:
        break;
      default:
        throw new Unimplemented("Invoke type " + invokeType + " is not yet supported.");
    }
  }

  private boolean checkSignatures() {
    List<DexType> sourceParams = new ArrayList<>();
    if (receiver != null) {
      sourceParams.add(receiver);
    }
    sourceParams.addAll(Lists.newArrayList(proto.parameters.values));
    if (extraNullParameter) {
      sourceParams.remove(sourceParams.size() - 1);
    }

    List<DexType> targetParams = new ArrayList<>();
    if (targetReceiver != null) {
      targetParams.add(targetReceiver);
    }
    targetParams.addAll(Lists.newArrayList(target.proto.parameters.values));

    assert sourceParams.size() == targetParams.size();
    for (int i = 0; i < sourceParams.size(); i++) {
      DexType source = sourceParams.get(i);
      DexType target = targetParams.get(i);

      // We assume source is compatible with target if they both are classes.
      // This check takes care of receiver widening conversion but does not
      // many others, like conversion from an array to Object.
      assert (source.isClassType() && target.isClassType()) || source == target;
    }

    assert this.proto.returnType == target.proto.returnType || castResult;
    return true;
  }

  @Override
  protected void prepareInstructions() {
    // Prepare call arguments.
    List<ValueType> argValueTypes = new ArrayList<>();
    List<Integer> argRegisters = new ArrayList<>();

    if (receiver != null) {
      argValueTypes.add(ValueType.OBJECT);
      argRegisters.add(getReceiverRegister());
    }

    DexType[] accessorParams = proto.parameters.values;
    for (int i = 0; i < accessorParams.length - BooleanUtils.intValue(extraNullParameter); i++) {
      argValueTypes.add(ValueType.fromDexType(accessorParams[i]));
      argRegisters.add(getParamRegister(i));
    }

    // Method call to the target method.
    add(
        builder ->
            builder.addInvoke(
                this.invokeType,
                this.target,
                this.target.proto,
                argValueTypes,
                argRegisters,
                this.isInterface));

    // Does the method return value?
    if (proto.returnType.isVoidType()) {
      add(IRBuilder::addReturn);
    } else {
      ValueType valueType = ValueType.fromDexType(proto.returnType);
      int tempValue = nextRegister(valueType);
      add(builder -> builder.addMoveResult(tempValue));
      if (this.proto.returnType != target.proto.returnType) {
        add(builder -> builder.addCheckCast(tempValue, this.proto.returnType));
      }
      add(builder -> builder.addReturn(tempValue));
    }
  }
}
