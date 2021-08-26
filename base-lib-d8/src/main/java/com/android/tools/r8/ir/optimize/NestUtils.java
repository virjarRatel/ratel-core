// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeInterface;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeVirtual;

public class NestUtils {

  public static boolean sameNest(DexType type1, DexType type2, DexDefinitionSupplier definitions) {
    if (type1 == type2) {
      return true;
    }
    DexClass clazz1 = definitions.definitionFor(type1);
    if (clazz1 == null) {
      // Conservatively return false
      return false;
    }
    if (!clazz1.isInANest()) {
      return false;
    }
    DexClass clazz2 = definitions.definitionFor(type2);
    if (clazz2 == null) {
      // Conservatively return false
      return false;
    }
    return clazz1.getNestHost() == clazz2.getNestHost();
  }

  public static void rewriteNestCallsForInlining(
      IRCode code, DexType callerHolder, AppView<?> appView) {
    // This method is called when inlining code into the nest member callerHolder.
    InstructionListIterator iterator = code.instructionListIterator();
    DexClass callerHolderClass = appView.definitionFor(callerHolder);
    assert callerHolderClass != null;
    assert code.method.method.holder != callerHolder;
    while (iterator.hasNext()) {
      Instruction instruction = iterator.next();
      if (instruction.isInvokeDirect()) {
        InvokeDirect invoke = instruction.asInvokeDirect();
        DexMethod method = invoke.getInvokedMethod();
        DexEncodedMethod encodedMethod = appView.definitionFor(method);
        if (encodedMethod != null && !encodedMethod.isInstanceInitializer()) {
          assert encodedMethod.isPrivateMethod();
          // Call to private method which has now to be interface/virtual
          // (Now call to nest member private method).
          if (invoke.isInterface()) {
            iterator.replaceCurrentInstruction(
                new InvokeInterface(method, invoke.outValue(), invoke.inValues()));
          } else {
            iterator.replaceCurrentInstruction(
                new InvokeVirtual(method, invoke.outValue(), invoke.inValues()));
          }
        }
      } else if (instruction.isInvokeInterface() || instruction.isInvokeVirtual()) {
        InvokeMethod invoke = instruction.asInvokeMethod();
        DexMethod method = invoke.getInvokedMethod();
        if (method.holder == callerHolder) {
          DexEncodedMethod encodedMethod = appView.definitionFor(method);
          if (encodedMethod != null && encodedMethod.isPrivateMethod()) {
            // Interface/virtual nest member call to private method,
            // which has now to be a direct call
            // (Now call to same class private method).
            iterator.replaceCurrentInstruction(
                new InvokeDirect(
                    method, invoke.outValue(), invoke.inValues(), callerHolderClass.isInterface()));
          }
        }
      }
    }
  }
}
