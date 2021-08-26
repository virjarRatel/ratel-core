// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.type.Nullability;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.Argument;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.StackValue;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Compute the types of all reference values.
// The actual types are needed to emit stack-map frames for Java 1.6 and above.
public class TypeVerificationHelper {

  public interface TypeInfo {
    DexType getDexType();
  }

  public static class InitializedTypeInfo implements TypeInfo {
    final DexType type;

    private InitializedTypeInfo(DexType type) {
      assert type != null;
      this.type = type;
    }

    @Override
    public DexType getDexType() {
      return type;
    }

    @Override
    public String toString() {
      return type.toString();
    }
  }

  public static class NewInstanceInfo implements TypeInfo {
    public final NewInstance newInstance;

    public NewInstanceInfo(NewInstance newInstance) {
      assert newInstance != null;
      this.newInstance = newInstance;
    }

    @Override
    public DexType getDexType() {
      return newInstance.clazz;
    }

    @Override
    public String toString() {
      return "new:" + getDexType();
    }
  }

  public static class ThisInstanceInfo implements TypeInfo {
    final DexType type;
    public final Argument thisArgument;

    public ThisInstanceInfo(Argument thisArgument, DexType type) {
      assert thisArgument != null;
      assert type != null;
      this.thisArgument = thisArgument;
      this.type = type;
    }

    @Override
    public DexType getDexType() {
      return type;
    }

    @Override
    public String toString() {
      return "this:" + getDexType();
    }
  }

  private final TypeInfo INT;
  private final TypeInfo FLOAT;
  private final TypeInfo LONG;
  private final TypeInfo DOUBLE;

  private final AppView<?> appView;
  private final IRCode code;

  private Map<Value, TypeInfo> types;
  private Map<NewInstance, NewInstanceInfo> newInstanceInfos = new IdentityHashMap<>();

  // Flag to indicate that we are computing types in the fixed point.
  private boolean computingVerificationTypes = false;

  public TypeVerificationHelper(AppView<?> appView, IRCode code) {
    this.appView = appView;
    this.code = code;

    DexItemFactory dexItemFactory = appView.dexItemFactory();
    INT = new InitializedTypeInfo(dexItemFactory.intType);
    FLOAT = new InitializedTypeInfo(dexItemFactory.floatType);
    LONG = new InitializedTypeInfo(dexItemFactory.longType);
    DOUBLE = new InitializedTypeInfo(dexItemFactory.doubleType);
  }

  public TypeInfo createInitializedType(DexType type) {
    if (!type.isPrimitiveType()) {
      return new InitializedTypeInfo(type);
    }
    if (type.isLongType()) {
      return LONG;
    }
    if (type.isDoubleType()) {
      return DOUBLE;
    }
    if (type.isFloatType()) {
      return FLOAT;
    }
    assert type.isBooleanType()
        || type.isByteType()
        || type.isCharType()
        || type.isShortType()
        || type.isIntType();
    return INT;
  }

  public DexType getDexType(Value value) {
    assert computingVerificationTypes;
    assert value.outType().isObject();
    TypeInfo typeInfo = types.get(value);
    return typeInfo == null ? null : typeInfo.getDexType();
  }

  public TypeInfo getTypeInfo(Value value) {
    if (value instanceof FixedLocalValue) {
      value = ((FixedLocalValue) value).getPhi();
    }
    if (value instanceof StackValue) {
      return ((StackValue) value).getTypeInfo();
    }
    switch (value.outType()) {
      case OBJECT:
        return types.get(value);
      case INT:
        return INT;
      case FLOAT:
        return FLOAT;
      case LONG:
        return LONG;
      case DOUBLE:
        return DOUBLE;
      default:
        throw new Unreachable("Unexpected type: " + value.outType() + " for value: " + value);
    }
  }

  // Helper to compute the join of a set of reference types.
  public DexType join(Set<DexType> types) {
    // We should not be joining empty sets of types.
    assert !types.isEmpty();
    if (types.size() == 1) {
      return types.iterator().next();
    }
    Iterator<DexType> iterator = types.iterator();
    TypeLatticeElement result = getLatticeElement(iterator.next());
    while (iterator.hasNext()) {
      result = result.join(getLatticeElement(iterator.next()), appView);
    }
    // All types are reference types so the join is either a class or an array.
    if (result.isClassType()) {
      return result.asClassTypeLatticeElement().getClassType();
    } else if (result.isArrayType()) {
      return result.asArrayTypeLatticeElement().getArrayType(appView.dexItemFactory());
    }
    throw new CompilationError("Unexpected join " + result + " of types: " +
        String.join(", ",
            types.stream().map(DexType::toSourceString).collect(Collectors.toList())));
  }

  public TypeInfo join(TypeInfo info1, TypeInfo info2) {
    if (info1 == info2) {
      return info1;
    }

    DexType type1 = info1.getDexType();
    DexType type2 = info2.getDexType();

    if ((info1 instanceof InitializedTypeInfo)
        && (info2 instanceof InitializedTypeInfo)
        && type1 == type2) {
      return info1;
    }

    assert !type1.isPrimitiveType() && !type2.isPrimitiveType();
    return createInitializedType(join(ImmutableSet.of(type1, type2)));
  }

  private TypeLatticeElement getLatticeElement(DexType type) {
    return TypeLatticeElement.fromDexType(type, Nullability.maybeNull(), appView);
  }

  public Map<Value, TypeInfo> computeVerificationTypes() {
    computingVerificationTypes = true;
    types = new HashMap<>();
    List<ConstNumber> nullsUsedInPhis = new ArrayList<>();
    Set<Value> worklist = Sets.newIdentityHashSet();
    {
      InstructionIterator it = code.instructionIterator();
      Instruction instruction = null;
      // Set the out-value types of each argument based on the method signature.
      int argumentIndex = code.method.accessFlags.isStatic() ? 0 : -1;
      while (it.hasNext()) {
        instruction = it.next();
        if (!instruction.isArgument()) {
          break;
        }
        TypeInfo argumentType;
        if (argumentIndex < 0) {
          argumentType =
              code.method.isInstanceInitializer()
                  ? new ThisInstanceInfo(instruction.asArgument(), code.method.method.holder)
                  : createInitializedType(code.method.method.holder);
        } else {
          argumentType =
              createInitializedType(code.method.method.proto.parameters.values[argumentIndex]);
        }
        Value outValue = instruction.outValue();
        if (outValue.outType().isObject()) {
          types.put(outValue, argumentType);
          addUsers(outValue, worklist);
        }
        ++argumentIndex;
      }
      // Compute the out-value type of each normal instruction with an invariant out-value type.
      while (instruction != null) {
        assert !instruction.isArgument();
        if (instruction.outValue() != null) {
          if (instruction.isNewInstance()) {
            NewInstanceInfo newInstanceInfo =
                newInstanceInfos.computeIfAbsent(instruction.asNewInstance(), NewInstanceInfo::new);
            types.put(instruction.outValue(), newInstanceInfo);
            addUsers(instruction.outValue(), worklist);
          } else if (instruction.outType().isObject()) {
            Value outValue = instruction.outValue();
            if (instruction.hasInvariantOutType()) {
              if (instruction.isConstNumber()) {
                assert instruction.asConstNumber().isZero();
                if (outValue.numberOfAllUsers() == outValue.numberOfPhiUsers()) {
                  nullsUsedInPhis.add(instruction.asConstNumber());
                }
              }
              DexType type = instruction.computeVerificationType(appView, this);
              types.put(outValue, createInitializedType(type));
              addUsers(outValue, worklist);
            }
          }
        }
        instruction = it.hasNext() ? it.next() : null;
      }
    }
    // Compute the fixed-point of all the out-value types.
    while (!worklist.isEmpty()) {
      Value item = worklist.iterator().next();
      worklist.remove(item);
      assert item.outType().isObject();
      TypeInfo typeInfo = types.get(item);
      DexType previousType = typeInfo == null ? null : typeInfo.getDexType();
      DexType refinedType = computeVerificationType(item);
      if (previousType != refinedType) {
        types.put(item, createInitializedType(refinedType));
        addUsers(item, worklist);
      }
    }
    computingVerificationTypes = false;
    for (ConstNumber instruction : nullsUsedInPhis) {
      TypeInfo refinedType = null;
      for (Phi phi : instruction.outValue().uniquePhiUsers()) {
        if (refinedType == null) {
          refinedType = types.get(phi);
        } else if (refinedType.getDexType() != types.get(phi).getDexType()) {
          refinedType = null;
          break;
        }
      }
      if (refinedType != null) {
        types.put(instruction.outValue(), refinedType);
      }
    }
    return types;
  }

  private DexType computeVerificationType(Value value) {
    return value.isPhi()
        ? value.asPhi().computeVerificationType(this)
        : value.definition.computeVerificationType(appView, this);
  }

  private static void addUsers(Value value, Set<Value> worklist) {
    worklist.addAll(value.uniquePhiUsers());
    for (Instruction instruction : value.uniqueUsers()) {
      if (instruction.outValue() != null
          && instruction.outType().isObject()
          && !instruction.hasInvariantOutType()) {
        worklist.add(instruction.outValue());
      }
    }
  }
}
