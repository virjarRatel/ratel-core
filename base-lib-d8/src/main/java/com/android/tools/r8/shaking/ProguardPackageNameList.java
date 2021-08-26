// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexType;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class ProguardPackageNameList {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    /** Map used to store pairs of patterns and whether they are negated. */
    private final Object2BooleanMap<ProguardPackageMatcher> matchers =
        new Object2BooleanArrayMap<>();

    private Builder() {}

    public ProguardPackageNameList.Builder addPackageName(
        boolean isNegated, ProguardPackageMatcher className) {
      matchers.put(className, isNegated);
      return this;
    }

    ProguardPackageNameList build() {
      return new ProguardPackageNameList(matchers);
    }
  }

  private final Object2BooleanMap<ProguardPackageMatcher> packageNames;

  private ProguardPackageNameList(Object2BooleanMap<ProguardPackageMatcher> pacakgeNames) {
    this.packageNames = pacakgeNames;
  }

  public void writeTo(StringBuilder builder) {
    boolean first = true;
    for (Object2BooleanMap.Entry<ProguardPackageMatcher> packageName :
        packageNames.object2BooleanEntrySet()) {
      if (!first) {
        builder.append(',');
      }
      if (packageName.getBooleanValue()) {
        builder.append('!');
      }
      builder.append(packageName.getKey().toString());
      first = false;
    }
  }

  public boolean matches(DexType type) {
    for (Object2BooleanMap.Entry<ProguardPackageMatcher> packageName :
        packageNames.object2BooleanEntrySet()) {
      if (packageName.getKey().matches(type)) {
        // If we match a negation, abort as non-match. If we match a positive, return true.
        return !packageName.getBooleanValue();
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProguardPackageNameList)) {
      return false;
    }
    ProguardPackageNameList other = (ProguardPackageNameList) o;
    if (packageNames.size() != other.packageNames.size()) {
      return false;
    }
    ObjectIterator<Object2BooleanMap.Entry<ProguardPackageMatcher>> i1 =
        packageNames.object2BooleanEntrySet().iterator();
    ObjectIterator<Object2BooleanMap.Entry<ProguardPackageMatcher>> i2 =
        other.packageNames.object2BooleanEntrySet().iterator();
    while (i1.hasNext()) {
      Object2BooleanMap.Entry<ProguardPackageMatcher> e1 = i1.next();
      Object2BooleanMap.Entry<ProguardPackageMatcher> e2 = i2.next();
      if (!e1.equals(e2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    ObjectIterator<Object2BooleanMap.Entry<ProguardPackageMatcher>> iterator =
        packageNames.object2BooleanEntrySet().iterator();
    while (iterator.hasNext()) {
      Object2BooleanMap.Entry<ProguardPackageMatcher> packageMatcher = iterator.next();
      hash = hash * (packageMatcher.getBooleanValue() ? 1 : 2);
      hash = hash * 13 + packageMatcher.getKey().hashCode();
    }
    return hash;
  }
}
