// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.info.initializer;

import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.analysis.fieldvalueanalysis.AbstractFieldSet;

public abstract class InstanceInitializerInfo {

  public abstract DexMethod getParent();

  /**
   * Returns an abstraction of the set of fields that may be as a result of executing this
   * initializer.
   */
  public abstract AbstractFieldSet readSet();

  /**
   * Returns true if one of the instance fields on the enclosing class may be initialized with a
   * value that may depend on the runtime environment by this constructor.
   *
   * <p>Example: returns false if all fields are assigned with one of the arguments or a constant.
   *
   * <p>Example: returns true if a field is assigned with the value of a static-get instruction that
   * reads a field from a different class.
   */
  public abstract boolean instanceFieldInitializationMayDependOnEnvironment();

  /**
   * Returns false if the only side effects that this initializer may have is the instance-put
   * instructions that assign a field on the enclosing class.
   *
   * <p>Example: returns true if the initializer invokes {@link java.io.PrintStream#println()}.
   *
   * <p>Example: returns false if the initializer merely assigns the value of one of the arguments
   * or a constant to an instance-field on the enclosing class.
   */
  public abstract boolean mayHaveOtherSideEffectsThanInstanceFieldAssignments();

  /**
   * The receiver always escapes from a constructor, since it is passed to the constructor in the
   * super class. This method returns true if the receiver never escapes outside this constructor or
   * any of its (transitive) super constructors.
   */
  public abstract boolean receiverNeverEscapesOutsideConstructorChain();

  public final boolean receiverMayEscapeOutsideConstructorChain() {
    return !receiverNeverEscapesOutsideConstructorChain();
  }

  public boolean isDefaultInfo() {
    return false;
  }
}
