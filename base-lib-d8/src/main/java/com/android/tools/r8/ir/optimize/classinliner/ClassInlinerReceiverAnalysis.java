// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.classinliner;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.analysis.type.ClassTypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.BooleanLatticeElement;
import com.android.tools.r8.utils.OptionalBool;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * This analysis determines whether a method returns the receiver. The analysis is specific to the
 * class inliner, and the result is therefore not sound in general.
 *
 * <p>The analysis makes the following assumptions.
 *
 * <ul>
 *   <li>None of the given method's arguments is an alias of the receiver (except for the receiver
 *       itself).
 *   <li>The receiver is not stored in the heap. Thus, it is guaranteed that (i) all field-get
 *       instructions do not return an alias of the receiver, and (ii) invoke instructions can only
 *       return an alias of the receiver if the receiver is given as an argument.
 * </ul>
 */
public class ClassInlinerReceiverAnalysis {

  private final AppView<?> appView;
  private final DexEncodedMethod method;
  private final IRCode code;
  private final Value receiver;

  private final Map<Value, OptionalBool> isReceiverAliasCache = new IdentityHashMap<>();

  public ClassInlinerReceiverAnalysis(AppView<?> appView, DexEncodedMethod method, IRCode code) {
    this.appView = appView;
    this.method = method;
    this.code = code;
    this.receiver = code.getThis();
    assert !receiver.hasAliasedValue();
    assert receiver.getTypeLattice().isClassType();
  }

  public OptionalBool computeReturnsReceiver() {
    if (method.method.proto.returnType.isVoidType()) {
      return OptionalBool.FALSE;
    }

    List<BasicBlock> normalExitBlocks = code.computeNormalExitBlocks();
    if (normalExitBlocks.isEmpty()) {
      return OptionalBool.FALSE;
    }

    BooleanLatticeElement result = OptionalBool.BOTTOM;
    for (BasicBlock block : normalExitBlocks) {
      Value returnValue = block.exit().asReturn().returnValue();
      result = result.join(getOrComputeIsReceiverAlias(returnValue));

      // Stop as soon as we reach unknown.
      if (result.isUnknown()) {
        return OptionalBool.UNKNOWN;
      }
    }
    assert !result.isBottom();
    return result.asOptionalBool();
  }

  private OptionalBool getOrComputeIsReceiverAlias(Value value) {
    Value root = value.getAliasedValue();
    return isReceiverAliasCache.computeIfAbsent(root, this::computeIsReceiverAlias);
  }

  private OptionalBool computeIsReceiverAlias(Value value) {
    assert !value.hasAliasedValue();

    if (value == receiver) {
      // Guaranteed to return the receiver.
      return OptionalBool.TRUE;
    }

    ClassTypeLatticeElement valueType = value.getTypeLattice().asClassTypeLatticeElement();
    if (valueType == null) {
      return OptionalBool.FALSE;
    }

    ClassTypeLatticeElement receiverType = receiver.getTypeLattice().asClassTypeLatticeElement();
    if (!valueType.isRelatedTo(receiverType, appView)) {
      // Guaranteed not to return the receiver.
      return OptionalBool.FALSE;
    }

    if (value.isPhi()) {
      // Not sure what is returned.
      return OptionalBool.UNKNOWN;
    }

    Instruction definition = value.definition;
    if (definition.isArrayGet() || definition.isFieldGet()) {
      // Guaranteed not to return the receiver, since the class inliner does not allow the
      // receiver to flow into any arrays or fields.
      return OptionalBool.FALSE;
    }

    if (definition.isConstInstruction() || definition.isCreatingInstanceOrArray()) {
      // Guaranteed not to return the receiver.
      return OptionalBool.FALSE;
    }

    if (definition.isInvokeMethod()) {
      // Since the class inliner does not allow the receiver to flow into the heap, the only way for
      // the invoked method to return the receiver is if one of the given arguments is an alias of
      // the receiver.
      InvokeMethod invoke = definition.asInvokeMethod();
      for (Value argument : invoke.arguments()) {
        if (getOrComputeIsReceiverAlias(argument).isPossiblyTrue()) {
          return OptionalBool.UNKNOWN;
        }
      }

      return OptionalBool.FALSE;
    }

    // Not sure what is returned.
    return OptionalBool.UNKNOWN;
  }
}
