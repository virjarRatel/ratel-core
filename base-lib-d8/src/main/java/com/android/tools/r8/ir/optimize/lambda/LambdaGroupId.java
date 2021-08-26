// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize.lambda;

// Represents a lambda group identifier uniquely identifying the groups
// of potentially mergeable lambdas.
//
// Implements hashCode/equals in a way that guarantees that if two lambda
// classes has equal ids they belong to the same lambda group.
public interface LambdaGroupId {
  LambdaGroup createGroup();

  @Override
  int hashCode();

  @Override
  boolean equals(Object obj);
}
