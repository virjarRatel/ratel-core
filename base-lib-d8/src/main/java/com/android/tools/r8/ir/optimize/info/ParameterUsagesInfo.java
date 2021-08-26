// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.If.Type;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ParameterUsagesInfo {

  private ImmutableList<ParameterUsage> parametersUsages;

  public ParameterUsagesInfo(List<ParameterUsage> usages) {
    assert !usages.isEmpty();
    parametersUsages = ImmutableList.copyOf(usages);
    assert parametersUsages.size() ==
        parametersUsages.stream().map(usage -> usage.index).collect(Collectors.toSet()).size();
  }

  ParameterUsage getParameterUsage(int index) {
    for (ParameterUsage usage : parametersUsages) {
      if (usage.index == index) {
        return usage;
      }
    }
    return null;
  }

  ParameterUsagesInfo remove(int index) {
    assert parametersUsages.size() > 0;
    assert 0 <= index && index <= ListUtils.last(parametersUsages).index;
    ImmutableList.Builder<ParameterUsage> builder = ImmutableList.builder();
    for (ParameterUsage usage : parametersUsages) {
      // Once we remove or pass the designated index, copy-n-shift the remaining usages.
      if (index < usage.index) {
        builder.add(ParameterUsage.copyAndShift(usage, 1));
      } else if (index == usage.index) {
        // Do not add the parameter usage with the matched index.
      } else {
        // Until we meet the `parameter` of interest, keep copying.
        assert usage.index < index;
        builder.add(usage);
      }
    }
    ImmutableList<ParameterUsage> adjustedUsages = builder.build();
    if (adjustedUsages.isEmpty()) {
      return DefaultMethodOptimizationInfo.UNKNOWN_PARAMETER_USAGE_INFO;
    }
    return new ParameterUsagesInfo(adjustedUsages);
  }

  public final static class ParameterUsage {

    public final int index;
    public final Set<Type> ifZeroTest;
    public final List<Pair<Invoke.Type, DexMethod>> callsReceiver;

    // If a field of this argument is assigned: arg.f = x.
    public final boolean hasFieldAssignment;

    // If a field of this argument is read: x = arg.f.
    public final boolean hasFieldRead;

    // If this argument is assigned to a field: x.f = arg.
    public final boolean isAssignedToField;

    // If this argument is returned: return arg.
    public final boolean isReturned;

    ParameterUsage(
        int index,
        Set<Type> ifZeroTest,
        List<Pair<Invoke.Type, DexMethod>> callsReceiver,
        boolean hasFieldAssignment,
        boolean hasFieldRead,
        boolean isAssignedToField,
        boolean isReturned) {
      this.index = index;
      this.ifZeroTest =
          ifZeroTest.isEmpty() ? Collections.emptySet() : ImmutableSet.copyOf(ifZeroTest);
      this.callsReceiver =
          callsReceiver.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(callsReceiver);
      this.hasFieldAssignment = hasFieldAssignment;
      this.hasFieldRead = hasFieldRead;
      this.isAssignedToField = isAssignedToField;
      this.isReturned = isReturned;
    }

    static ParameterUsage copyAndShift(ParameterUsage original, int shift) {
      assert original.index >= shift;
      return new ParameterUsage(
          original.index - shift,
          original.ifZeroTest,
          original.callsReceiver,
          original.hasFieldAssignment,
          original.hasFieldRead,
          original.isAssignedToField,
          original.isReturned);
    }

    public boolean notUsed() {
      return (ifZeroTest == null || ifZeroTest.isEmpty())
          && (callsReceiver == null || callsReceiver.isEmpty())
          && !hasFieldAssignment
          && !hasFieldRead
          && !isAssignedToField
          && !isReturned;
    }
  }

  public static class ParameterUsageBuilder {

    private final int index;
    private final Value arg;
    private final Set<Type> ifZeroTestTypes = new HashSet<>();
    private final List<Pair<Invoke.Type, DexMethod>> callsOnReceiver = new ArrayList<>();

    private boolean hasFieldAssignment = false;
    private boolean hasFieldRead = false;
    private boolean isAssignedToField = false;
    private boolean isReturned = false;

    ParameterUsageBuilder(Value arg, int index) {
      this.arg = arg;
      this.index = index;
    }

    // Returns false if the instruction is not supported.
    public boolean note(Instruction instruction) {
      if (instruction.isAssume()) {
        // Keep examining other users if there are no phi users, but the param usage builder should
        // consider aliased users.
        return !instruction.outValue().hasPhiUsers();
      }
      if (instruction.isIf()) {
        return note(instruction.asIf());
      }
      if (instruction.isInstanceGet()) {
        return note(instruction.asInstanceGet());
      }
      if (instruction.isInstancePut()) {
        return note(instruction.asInstancePut());
      }
      if (instruction.isInvokeMethodWithReceiver()) {
        return note(instruction.asInvokeMethodWithReceiver());
      }
      if (instruction.isReturn()) {
        return note(instruction.asReturn());
      }
      return false;
    }

    public ParameterUsage build() {
      return new ParameterUsage(
          index,
          ifZeroTestTypes,
          callsOnReceiver,
          hasFieldAssignment,
          hasFieldRead,
          isAssignedToField,
          isReturned);
    }

    private boolean note(If ifInstruction) {
      if (ifInstruction.asIf().isZeroTest()) {
        assert ifInstruction.inValues().size() == 1
            && ifInstruction.inValues().get(0).getAliasedValue() == arg;
        ifZeroTestTypes.add(ifInstruction.asIf().getType());
        return true;
      }
      return false;
    }

    private boolean note(InstanceGet instanceGetInstruction) {
      assert arg != instanceGetInstruction.outValue();
      if (instanceGetInstruction.object().getAliasedValue() == arg) {
        hasFieldRead = true;
        return true;
      }
      return false;
    }

    private boolean note(InstancePut instancePutInstruction) {
      assert arg != instancePutInstruction.outValue();
      if (instancePutInstruction.object().getAliasedValue() == arg) {
        hasFieldAssignment = true;
        isAssignedToField |= instancePutInstruction.value().getAliasedValue() == arg;
        return true;
      }
      if (instancePutInstruction.value().getAliasedValue() == arg) {
        isAssignedToField = true;
        return true;
      }
      return false;
    }

    private boolean note(InvokeMethodWithReceiver invokeInstruction) {
      if (ListUtils.lastIndexMatching(
          invokeInstruction.inValues(), v -> v.getAliasedValue() == arg) == 0) {
        callsOnReceiver.add(
            new Pair<>(
                invokeInstruction.asInvokeMethodWithReceiver().getType(),
                invokeInstruction.asInvokeMethodWithReceiver().getInvokedMethod()));
        return true;
      }
      return false;
    }

    private boolean note(Return returnInstruction) {
      assert returnInstruction.inValues().size() == 1
          && returnInstruction.inValues().get(0).getAliasedValue() == arg;
      isReturned = true;
      return true;
    }
  }
}
