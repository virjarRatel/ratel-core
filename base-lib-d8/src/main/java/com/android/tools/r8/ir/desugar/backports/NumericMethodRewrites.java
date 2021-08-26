package com.android.tools.r8.ir.desugar.backports;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.code.Add;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Value;
import java.util.List;

public final class NumericMethodRewrites {
  public static void rewriteToInvokeMath(InvokeMethod invoke, InstructionListIterator iterator,
      DexItemFactory factory) {
    InvokeStatic mathInvoke = new InvokeStatic(
        factory.createMethod(factory.mathType, invoke.getInvokedMethod().proto,
            invoke.getInvokedMethod().name), invoke.outValue(), invoke.inValues(), false);
    iterator.replaceCurrentInstruction(mathInvoke);
  }

  public static void rewriteToAddInstruction(InvokeMethod invoke, InstructionListIterator iterator,
      DexItemFactory factory) {
    List<Value> values = invoke.inValues();
    assert values.size() == 2;

    NumericType numericType = NumericType.fromDexType(invoke.getReturnType());
    Add add = new Add(numericType, invoke.outValue(), values.get(0), values.get(1));
    iterator.replaceCurrentInstruction(add);
  }

  public static void rewriteAsIdentity(InvokeMethod invoke, InstructionListIterator iterator,
      DexItemFactory factory) {
    List<Value> values = invoke.inValues();
    assert values.size() == 1;
    invoke.outValue().replaceUsers(values.get(0));
    iterator.remove();
  }

  private NumericMethodRewrites() {
  }
}
