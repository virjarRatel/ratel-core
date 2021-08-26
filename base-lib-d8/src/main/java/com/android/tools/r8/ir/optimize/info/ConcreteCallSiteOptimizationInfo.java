// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize.info;

import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;
import static com.android.tools.r8.ir.analysis.type.Nullability.maybeNull;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.analysis.value.AbstractValue;
import com.android.tools.r8.ir.analysis.value.UnknownValue;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.info.ParameterUsagesInfo.ParameterUsage;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.List;
import java.util.Objects;

// Accumulated optimization info from call sites.
public class ConcreteCallSiteOptimizationInfo extends CallSiteOptimizationInfo {

  // inValues() size == DexMethod.arity + (isStatic ? 0 : 1) // receiver
  // That is, this information takes into account the receiver as well.
  private final int size;
  private final Int2ReferenceMap<TypeLatticeElement> dynamicUpperBoundTypes;
  private final Int2ReferenceMap<AbstractValue> constants;

  private ConcreteCallSiteOptimizationInfo(
      DexEncodedMethod encodedMethod, boolean allowConstantPropagation) {
    this(encodedMethod.method.getArity() + (encodedMethod.isStatic() ? 0 : 1),
        allowConstantPropagation);
  }

  private ConcreteCallSiteOptimizationInfo(int size, boolean allowConstantPropagation) {
    assert size > 0;
    this.size = size;
    this.dynamicUpperBoundTypes = new Int2ReferenceArrayMap<>(size);
    this.constants = allowConstantPropagation ? new Int2ReferenceArrayMap<>(size) : null;
  }

  CallSiteOptimizationInfo join(
      ConcreteCallSiteOptimizationInfo other, AppView<?> appView, DexEncodedMethod encodedMethod) {
    assert this.size == other.size;
    boolean allowConstantPropagation = appView.options().enablePropagationOfConstantsAtCallSites;
    ConcreteCallSiteOptimizationInfo result =
        new ConcreteCallSiteOptimizationInfo(this.size, allowConstantPropagation);
    assert result.dynamicUpperBoundTypes != null;
    for (int i = 0; i < result.size; i++) {
      if (allowConstantPropagation) {
        assert result.constants != null;
        AbstractValue abstractValue =
            getAbstractArgumentValue(i).join(other.getAbstractArgumentValue(i));
        if (abstractValue.isNonTrivial()) {
          result.constants.put(i, abstractValue);
        }
      }

      TypeLatticeElement thisUpperBoundType = getDynamicUpperBoundType(i);
      if (thisUpperBoundType == null) {
        // This means the corresponding argument is primitive. The counterpart should be too.
        assert other.getDynamicUpperBoundType(i) == null;
        continue;
      }
      assert thisUpperBoundType.isReference();
      TypeLatticeElement otherUpperBoundType = other.getDynamicUpperBoundType(i);
      assert otherUpperBoundType != null && otherUpperBoundType.isReference();
      result.dynamicUpperBoundTypes.put(
          i, thisUpperBoundType.join(otherUpperBoundType, appView));
    }
    if (result.hasUsefulOptimizationInfo(appView, encodedMethod)) {
      return result;
    }
    // As soon as we know the argument collection so far does not have any useful optimization info,
    // move to TOP so that further collection can be simply skipped.
    return TOP;
  }

  private TypeLatticeElement[] getStaticTypes(AppView<?> appView, DexEncodedMethod encodedMethod) {
    int argOffset = encodedMethod.isStatic() ? 0 : 1;
    int size = encodedMethod.method.getArity() + argOffset;
    TypeLatticeElement[] staticTypes = new TypeLatticeElement[size];
    if (!encodedMethod.isStatic()) {
      staticTypes[0] =
          TypeLatticeElement.fromDexType(
              encodedMethod.method.holder, definitelyNotNull(), appView);
    }
    for (int i = 0; i < encodedMethod.method.getArity(); i++) {
      staticTypes[i + argOffset] =
          TypeLatticeElement.fromDexType(
              encodedMethod.method.proto.parameters.values[i], maybeNull(), appView);
    }
    return staticTypes;
  }

  @Override
  public boolean hasUsefulOptimizationInfo(AppView<?> appView, DexEncodedMethod encodedMethod) {
    TypeLatticeElement[] staticTypes = getStaticTypes(appView, encodedMethod);
    for (int i = 0; i < size; i++) {
      ParameterUsage parameterUsage = encodedMethod.getOptimizationInfo().getParameterUsages(i);
      // If the parameter is not used, passing accurate argument info doesn't matter.
      if (parameterUsage != null && parameterUsage.notUsed()) {
        continue;
      }
      AbstractValue abstractValue = getAbstractArgumentValue(i);
      if (abstractValue.isNonTrivial()) {
        assert appView.options().enablePropagationOfConstantsAtCallSites;
        return true;
      }

      if (!staticTypes[i].isReference()) {
        continue;
      }
      TypeLatticeElement dynamicUpperBoundType = getDynamicUpperBoundType(i);
      if (dynamicUpperBoundType == null) {
        continue;
      }
      assert appView.options().enablePropagationOfDynamicTypesAtCallSites;
      // To avoid the full join of type lattices below, separately check if the nullability of
      // arguments is improved, and if so, we can eagerly conclude that we've collected useful
      // call site information for this method.
      Nullability nullability = dynamicUpperBoundType.nullability();
      if (nullability.isDefinitelyNull()) {
        return true;
      }
      // TODO(b/139246447): Similar to nullability, if dynamic lower bound type is available,
      //   we stop here and regard that call sites of this method have useful info.
      // In general, though, we're looking for (strictly) better dynamic types for arguments.
      if (dynamicUpperBoundType.strictlyLessThan(staticTypes[i], appView)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TypeLatticeElement getDynamicUpperBoundType(int argIndex) {
    assert 0 <= argIndex && argIndex < size;
    assert dynamicUpperBoundTypes != null;
    return dynamicUpperBoundTypes.getOrDefault(argIndex, null);
  }

  @Override
  public AbstractValue getAbstractArgumentValue(int argIndex) {
    assert 0 <= argIndex && argIndex < size;
    // TODO(b/69963623): Remove this once enabled.
    if (constants == null) {
      return UnknownValue.getInstance();
    }
    return constants.getOrDefault(argIndex, UnknownValue.getInstance());
  }

  public static CallSiteOptimizationInfo fromArguments(
      AppView<? extends AppInfoWithSubtyping> appView,
      DexEncodedMethod method,
      List<Value> inValues) {
    boolean allowConstantPropagation = appView.options().enablePropagationOfConstantsAtCallSites;
    ConcreteCallSiteOptimizationInfo newCallSiteInfo =
        new ConcreteCallSiteOptimizationInfo(method, allowConstantPropagation);
    assert newCallSiteInfo.size == inValues.size();
    assert newCallSiteInfo.dynamicUpperBoundTypes != null;
    for (int i = 0; i < newCallSiteInfo.size; i++) {
      Value arg = inValues.get(i);
      if (allowConstantPropagation) {
        assert newCallSiteInfo.constants != null;
        Value aliasedValue = arg.getAliasedValue();
        if (!aliasedValue.isPhi()) {
          AbstractValue abstractValue =
              aliasedValue.definition.getAbstractValue(appView, method.method.holder);
          if (abstractValue.isNonTrivial()) {
            newCallSiteInfo.constants.put(i, abstractValue);
          }
        }
      }

      if (arg.getTypeLattice().isPrimitive()) {
        continue;
      }
      assert arg.getTypeLattice().isReference();
      newCallSiteInfo.dynamicUpperBoundTypes.put(i, arg.getDynamicUpperBoundType(appView));
    }
    if (newCallSiteInfo.hasUsefulOptimizationInfo(appView, method)) {
      return newCallSiteInfo;
    }
    // As soon as we know the current call site does not have any useful optimization info,
    // return TOP so that further collection can be simply skipped.
    return TOP;
  }

  @Override
  public boolean isConcreteCallSiteOptimizationInfo() {
    return true;
  }

  @Override
  public ConcreteCallSiteOptimizationInfo asConcreteCallSiteOptimizationInfo() {
    return this;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ConcreteCallSiteOptimizationInfo)) {
      return false;
    }
    ConcreteCallSiteOptimizationInfo otherInfo = (ConcreteCallSiteOptimizationInfo) other;
    return Objects.equals(this.dynamicUpperBoundTypes, otherInfo.dynamicUpperBoundTypes)
        && Objects.equals(this.constants, otherInfo.constants);
  }

  @Override
  public int hashCode() {
    assert this.dynamicUpperBoundTypes != null;
    return System.identityHashCode(dynamicUpperBoundTypes) * 7 + System.identityHashCode(constants);
  }

  @Override
  public String toString() {
    return dynamicUpperBoundTypes.toString()
        + (constants == null ? "" : (System.lineSeparator() + constants.toString()));
  }
}
