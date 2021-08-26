// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.analysis.type;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Value;
import java.util.function.Function;

/**
 * The base abstraction of lattice elements for local type analysis.
 */
public abstract class TypeLatticeElement {
  public static final BottomTypeLatticeElement BOTTOM = BottomTypeLatticeElement.getInstance();
  public static final TopTypeLatticeElement TOP = TopTypeLatticeElement.getInstance();
  public static final BooleanTypeLatticeElement BOOLEAN = BooleanTypeLatticeElement.getInstance();
  public static final ByteTypeLatticeElement BYTE = ByteTypeLatticeElement.getInstance();
  static final ShortTypeLatticeElement SHORT = ShortTypeLatticeElement.getInstance();
  static final CharTypeLatticeElement CHAR = CharTypeLatticeElement.getInstance();
  public static final IntTypeLatticeElement INT = IntTypeLatticeElement.getInstance();
  public static final FloatTypeLatticeElement FLOAT = FloatTypeLatticeElement.getInstance();
  public static final SinglePrimitiveTypeLatticeElement SINGLE =
      SinglePrimitiveTypeLatticeElement.getInstance();
  public static final LongTypeLatticeElement LONG = LongTypeLatticeElement.getInstance();
  public static final DoubleTypeLatticeElement DOUBLE = DoubleTypeLatticeElement.getInstance();
  public static final WidePrimitiveTypeLatticeElement WIDE =
      WidePrimitiveTypeLatticeElement.getInstance();
  public static final ReferenceTypeLatticeElement NULL =
      ReferenceTypeLatticeElement.getNullTypeLatticeElement();

  public TypeLatticeElement fixupClassTypeReferences(
      Function<DexType, DexType> mapping, AppView<? extends AppInfoWithSubtyping> appView) {
    return this;
  }

  public boolean isNullable() {
    return nullability().isNullable();
  }

  public abstract Nullability nullability();

  /**
   * Computes the least upper bound of the current and the other elements.
   *
   * @param other {@link TypeLatticeElement} to join.
   * @param appView {@link DexDefinitionSupplier}.
   * @return {@link TypeLatticeElement}, a least upper bound of {@param this} and {@param other}.
   */
  public TypeLatticeElement join(TypeLatticeElement other, AppView<?> appView) {
    if (this == other) {
      return this;
    }
    if (isBottom()) {
      return other;
    }
    if (other.isBottom()) {
      return this;
    }
    if (isTop() || other.isTop()) {
      return TOP;
    }
    if (isPrimitive()) {
      return other.isPrimitive()
          ? asPrimitiveTypeLatticeElement().join(other.asPrimitiveTypeLatticeElement())
          : TOP;
    }
    if (other.isPrimitive()) {
      // By the above case, !(isPrimitive())
      return TOP;
    }
    // From now on, this and other are precise reference types, i.e., either ArrayType or ClassType.
    assert isReference() && other.isReference();
    assert isPreciseType() && other.isPreciseType();
    Nullability nullabilityJoin = nullability().join(other.nullability());
    if (isNullType()) {
      return other.asReferenceTypeLatticeElement().getOrCreateVariant(nullabilityJoin);
    }
    if (other.isNullType()) {
      return this.asReferenceTypeLatticeElement().getOrCreateVariant(nullabilityJoin);
    }
    if (getClass() != other.getClass()) {
      return objectClassType(appView, nullabilityJoin);
    }
    // From now on, getClass() == other.getClass()
    if (isArrayType()) {
      assert other.isArrayType();
      return asArrayTypeLatticeElement().join(other.asArrayTypeLatticeElement(), appView);
    }
    if (isClassType()) {
      assert other.isClassType();
      return asClassTypeLatticeElement().join(other.asClassTypeLatticeElement(), appView);
    }
    throw new Unreachable("unless a new type lattice is introduced.");
  }

  public static TypeLatticeElement join(
      Iterable<TypeLatticeElement> typeLattices, AppView<?> appView) {
    TypeLatticeElement result = BOTTOM;
    for (TypeLatticeElement other : typeLattices) {
      result = result.join(other, appView);
    }
    return result;
  }

  /**
   * Determines the strict partial order of the given {@link TypeLatticeElement}s.
   *
   * @param other expected to be *strictly* bigger than {@param this}
   * @param appView {@link DexDefinitionSupplier} to compute the least upper bound of {@link
   *     TypeLatticeElement}
   * @return {@code true} if {@param this} is strictly less than {@param other}.
   */
  public boolean strictlyLessThan(TypeLatticeElement other, AppView<?> appView) {
    if (equals(other)) {
      return false;
    }
    TypeLatticeElement lub = join(other, appView);
    return !equals(lub) && other.equals(lub);
  }

  /**
   * Determines the partial order of the given {@link TypeLatticeElement}s.
   *
   * @param other expected to be bigger than or equal to {@param this}
   * @param appView {@link DexDefinitionSupplier} to compute the least upper bound of {@link
   *     TypeLatticeElement}
   * @return {@code true} if {@param this} is less than or equal to {@param other}.
   */
  public boolean lessThanOrEqual(TypeLatticeElement other, AppView<?> appView) {
    return equals(other) || strictlyLessThan(other, appView);
  }

  /**
   * Determines if this {@link TypeLatticeElement} is less than or equal to the given {@link
   * TypeLatticeElement} up to nullability.
   *
   * @param other to check for equality with this
   * @return {@code true} if {@param this} is equal up to nullability with {@param other}.
   */
  public boolean lessThanOrEqualUpToNullability(TypeLatticeElement other, AppView<?> appView) {
    if (this == other) {
      return true;
    }
    if (this.isTop()) {
      return other.isTop();
    }
    if (other.isTop()) {
      return true;
    }
    if (this.isBottom()) {
      return true;
    }
    if (other.isBottom()) {
      return false;
    }
    if (isPrimitive()) {
      // Primitives cannot be nullable.
      return lessThanOrEqual(other, appView);
    }
    assert isReference() && other.isReference();
    ReferenceTypeLatticeElement otherAsNullable =
        other.isNullable()
            ? other.asReferenceTypeLatticeElement()
            : other.asReferenceTypeLatticeElement().getOrCreateVariant(Nullability.maybeNull());
    return lessThanOrEqual(otherAsNullable, appView);
  }

  /**
   * Determines if the {@link TypeLatticeElement}s are equal up to nullability.
   *
   * @param other to check for equality with this
   * @return {@code true} if {@param this} is equal up to nullability with {@param other}.
   */
  public boolean equalUpToNullability(TypeLatticeElement other) {
    if (this == other) {
      return true;
    }
    if (isPrimitive() || other.isPrimitive()) {
      return false;
    }
    assert isReference() && other.isReference();
    ReferenceTypeLatticeElement thisAsMaybeNull =
        this.asReferenceTypeLatticeElement().getOrCreateVariant(Nullability.maybeNull());
    ReferenceTypeLatticeElement otherAsMaybeNull =
        other.asReferenceTypeLatticeElement().getOrCreateVariant(Nullability.maybeNull());
    return thisAsMaybeNull.equals(otherAsMaybeNull);
  }

  /**
   * Determines if this type is based on a missing class, directly or indirectly.
   *
   * @return {@code} true if this type is based on a missing class.
   * @param appView
   */
  public boolean isBasedOnMissingClass(AppView<? extends AppInfoWithSubtyping> appView) {
    return false;
  }

  /**
   * Represents a type that can be everything.
   *
   * @return {@code true} if the corresponding {@link Value} could be any kinds.
   */
  public boolean isTop() {
    return false;
  }

  /**
   * Represents an empty type.
   *
   * @return {@code true} if the type of corresponding {@link Value} is not determined yet.
   */
  public boolean isBottom() {
    return false;
  }

  public boolean isReference() {
    return false;
  }

  public ReferenceTypeLatticeElement asReferenceTypeLatticeElement() {
    return null;
  }

  public boolean isArrayType() {
    return false;
  }

  public ArrayTypeLatticeElement asArrayTypeLatticeElement() {
    return null;
  }

  public boolean isClassType() {
    return false;
  }

  public ClassTypeLatticeElement asClassTypeLatticeElement() {
    return null;
  }

  public boolean isPrimitive() {
    return false;
  }

  public PrimitiveTypeLatticeElement asPrimitiveTypeLatticeElement() {
    return null;
  }

  public boolean isSinglePrimitive() {
    return false;
  }

  public boolean isWidePrimitive() {
    return false;
  }

  boolean isBoolean() {
    return false;
  }

  boolean isByte() {
    return false;
  }

  boolean isShort() {
    return false;
  }

  boolean isChar() {
    return false;
  }

  public boolean isInt() {
    return false;
  }

  public boolean isFloat() {
    return false;
  }

  public boolean isLong() {
    return false;
  }

  public boolean isDouble() {
    return false;
  }

  public boolean isPreciseType() {
    return isArrayType()
        || isClassType()
        || isNullType()
        || isInt()
        || isFloat()
        || isLong()
        || isDouble()
        || isBottom();
  }

  public boolean isFineGrainedType() {
    return isBoolean()
        || isByte()
        || isShort()
        || isChar();
  }

  /**
   * Determines if this type only includes null values that are defined by a const-number
   * instruction in the same enclosing method.
   *
   * These null values can be assigned to any type.
   */
  public boolean isNullType() {
    return false;
  }

  /**
   * Determines if this type only includes null values.
   *
   * These null values cannot be assigned to any type. For example, it is a type error to "throw v"
   * where the value `v` satisfies isDefinitelyNull(), because the static type of `v` may not be a
   * subtype of Throwable.
   */
  public boolean isDefinitelyNull() {
    return nullability().isDefinitelyNull();
  }

  public boolean isDefinitelyNotNull() {
    return nullability().isDefinitelyNotNull();
  }

  public int requiredRegisters() {
    assert !isBottom() && !isTop();
    return 1;
  }

  public static ClassTypeLatticeElement objectClassType(
      AppView<?> appView, Nullability nullability) {
    return fromDexType(appView.dexItemFactory().objectType, nullability, appView)
        .asClassTypeLatticeElement();
  }

  static ArrayTypeLatticeElement objectArrayType(AppView<?> appView, Nullability nullability) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    return fromDexType(
            dexItemFactory.createArrayType(1, dexItemFactory.objectType), nullability, appView)
        .asArrayTypeLatticeElement();
  }

  public static ClassTypeLatticeElement classClassType(
      AppView<?> appView, Nullability nullability) {
    return fromDexType(appView.dexItemFactory().classType, nullability, appView)
        .asClassTypeLatticeElement();
  }

  public static ClassTypeLatticeElement stringClassType(
      AppView<?> appView, Nullability nullability) {
    return fromDexType(appView.dexItemFactory().stringType, nullability, appView)
        .asClassTypeLatticeElement();
  }

  public static TypeLatticeElement fromDexType(
      DexType type, Nullability nullability, AppView<?> appView) {
    return fromDexType(type, nullability, appView, false);
  }

  public static TypeLatticeElement fromDexType(
      DexType type, Nullability nullability, AppView<?> appView, boolean asArrayElementType) {
    if (type == DexItemFactory.nullValueType) {
      assert !nullability.isDefinitelyNotNull();
      return NULL;
    }
    if (type.isPrimitiveType()) {
      return PrimitiveTypeLatticeElement.fromDexType(type, asArrayElementType);
    }
    return appView.dexItemFactory().createReferenceTypeLatticeElement(type, nullability, appView);
  }

  public boolean isValueTypeCompatible(TypeLatticeElement other) {
    return (isReference() && other.isReference())
        || (isSinglePrimitive() && other.isSinglePrimitive())
        || (isWidePrimitive() && other.isWidePrimitive());
  }

  @Override
  public abstract String toString();

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
