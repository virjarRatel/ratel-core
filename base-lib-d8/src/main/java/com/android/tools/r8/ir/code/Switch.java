// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import java.util.function.Consumer;

public abstract class Switch extends JumpInstruction {

  private final int[] targetBlockIndices;
  private int fallthroughBlockIndex;

  public Switch(Value in, int[] targetBlockIndices, int fallthroughBlockIndex) {
    super(in);
    this.targetBlockIndices = targetBlockIndices;
    this.fallthroughBlockIndex = fallthroughBlockIndex;
  }

  public Value value() {
    return inValues.get(0);
  }

  public boolean valid() {
    for (int i = 0; i < numberOfKeys(); i++) {
      assert getTargetBlockIndex(i) != getFallthroughBlockIndex();
    }
    return true;
  }

  public BasicBlock targetBlock(int index) {
    return getBlock().getSuccessors().get(targetBlockIndices()[index]);
  }

  public int getTargetBlockIndex(int index) {
    return targetBlockIndices[index];
  }

  public int[] targetBlockIndices() {
    return targetBlockIndices;
  }

  public void forEachTarget(Consumer<BasicBlock> fn) {
    for (int i = 0; i < targetBlockIndices.length; i++) {
      fn.accept(targetBlock(i));
    }
  }

  @Override
  public BasicBlock fallthroughBlock() {
    return getBlock().getSuccessors().get(fallthroughBlockIndex);
  }

  public int getFallthroughBlockIndex() {
    return fallthroughBlockIndex;
  }

  public void setFallthroughBlockIndex(int i) {
    fallthroughBlockIndex = i;
  }

  @Override
  public void setFallthroughBlock(BasicBlock block) {
    getBlock().getMutableSuccessors().set(fallthroughBlockIndex, block);
  }

  public int numberOfKeys() {
    return targetBlockIndices.length;
  }

  @Override
  public boolean isSwitch() {
    return true;
  }

  @Override
  public Switch asSwitch() {
    return this;
  }
}
