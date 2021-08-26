// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static com.android.tools.r8.utils.DescriptorUtils.javaTypeToDescriptorIfValidJavaType;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CheckCast;
import com.android.tools.r8.ir.code.ConstantValueUtils;
import com.android.tools.r8.ir.code.DexItemBasedConstString;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.Value;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class IdentifierNameStringUtils {

  /**
   * Checks if the given {@param method} is a reflection method in Java.
   *
   * @param dexItemFactory where pre-defined descriptors are retrieved
   * @param method to test
   * @return {@code true} if the given {@param method} is a reflection method in Java.
   */
  public static boolean isReflectionMethod(DexItemFactory dexItemFactory, DexMethod method) {
    // So, why is this simply not like:
    //   return dexItemFactory.classMethods.isReflectiveClassLookup(method)
    //       || dexItemFactory.classMethods.isReflectiveMemberLookup(method)
    //       || dexItemFactory.atomicFieldUpdaterMethods.isFieldUpdater(method);
    // ?
    // That is because the counter part of other shrinkers supports users' own reflective methods
    // whose signature matches with reflection methods in Java. Hence, explicit signature matching.
    // See tests {@link IdentifierMinifierTest#test2_rule3},
    //   {@link IdentifierNameStringMarkerTest#reflective_field_singleUseOperand_renamed}, or
    //   {@link IdentifierNameStringMarkerTest#reflective_method_singleUseOperand_renamed}.
    //
    // For java.lang.Class:
    //   (String) -> java.lang.Class | java.lang.reflect.Field
    //   (String, boolean, ClassLoader) -> java.lang.Class
    //   (String, Class[]) -> java.lang.reflect.Method
    // For java.util.concurrent.atomic.Atomic(Integer|Long)FieldUpdater:
    //   (Class, String) -> $holderType
    // For java.util.concurrent.atomic.AtomicReferenceFieldUpdater:
    //   (Class, Class, String) -> $holderType
    // For any other types:
    //   (Class, String) -> java.lang.reflect.Field
    //   (Class, String, Class[]) -> java.lang.reflect.Method
    int arity = method.getArity();
    if (method.holder == dexItemFactory.classType) {
      // Virtual methods of java.lang.Class, such as getField, getMethod, etc.
      if (arity == 0 || arity > 3) {
        return false;
      }
      if (arity == 1) {
        if (method.proto.returnType != dexItemFactory.classType
            && method.proto.returnType != dexItemFactory.fieldType) {
          return false;
        }
      } else if (arity == 2) {
        if (method.proto.returnType != dexItemFactory.methodType) {
          return false;
        }
      } else {
        if (method.proto.returnType != dexItemFactory.classType) {
          return false;
        }
      }
      if (method.proto.parameters.values[0] != dexItemFactory.stringType) {
        return false;
      }
      if (arity == 2) {
        if (method.proto.parameters.values[1] != dexItemFactory.classArrayType) {
          return false;
        }
      }
      if (arity == 3) {
        if (method.proto.parameters.values[1] != dexItemFactory.booleanType
            && method.proto.parameters.values[2] != dexItemFactory.classLoaderType) {
          return false;
        }
      }
    } else if (
        method.holder.descriptor == dexItemFactory.intFieldUpdaterDescriptor
            || method.holder.descriptor == dexItemFactory.longFieldUpdaterDescriptor) {
      // Atomic(Integer|Long)FieldUpdater->newUpdater(Class, String)AtomicFieldUpdater
      if (arity != 2) {
        return false;
      }
      if (method.proto.returnType != method.holder) {
        return false;
      }
      if (method.proto.parameters.values[0] != dexItemFactory.classType) {
        return false;
      }
      if (method.proto.parameters.values[1] != dexItemFactory.stringType) {
        return false;
      }
    } else if (method.holder.descriptor == dexItemFactory.referenceFieldUpdaterDescriptor) {
      // AtomicReferenceFieldUpdater->newUpdater(Class, Class, String)AtomicFieldUpdater
      if (arity != 3) {
        return false;
      }
      if (method.proto.returnType != method.holder) {
        return false;
      }
      if (method.proto.parameters.values[0] != dexItemFactory.classType) {
        return false;
      }
      if (method.proto.parameters.values[1] != dexItemFactory.classType) {
        return false;
      }
      if (method.proto.parameters.values[2] != dexItemFactory.stringType) {
        return false;
      }
    } else {
      // Methods whose first argument is of java.lang.Class type.
      if (arity != 2 && arity != 3) {
        return false;
      }
      if (arity == 2) {
        if (method.proto.returnType != dexItemFactory.fieldType) {
          return false;
        }
      } else {
        if (method.proto.returnType != dexItemFactory.methodType) {
          return false;
        }
      }
      if (method.proto.parameters.values[0] != dexItemFactory.classType) {
        return false;
      }
      if (method.proto.parameters.values[1] != dexItemFactory.stringType) {
        return false;
      }
      if (arity == 3) {
        if (method.proto.parameters.values[2] != dexItemFactory.classArrayType) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns true if the given invoke instruction is calling `boolean java.lang.String.equals(
   * java.lang.String)`, and one of the arguments is defined by an invoke-instruction that calls
   * `java.lang.String java.lang.Class.getName()`.
   */
  static boolean isClassNameComparison(InvokeMethod invoke, DexItemFactory dexItemFactory) {
    return invoke.isInvokeVirtual()
        && isClassNameComparison(invoke.asInvokeVirtual(), dexItemFactory);
  }

  static boolean isClassNameComparison(InvokeVirtual invoke, DexItemFactory dexItemFactory) {
    return invoke.getInvokedMethod() == dexItemFactory.stringMethods.equals
        && (isClassNameValue(invoke.getReceiver(), dexItemFactory)
            || isClassNameValue(invoke.inValues().get(1), dexItemFactory));
  }

  private static boolean isClassNameValue(Value value, DexItemFactory dexItemFactory) {
    Value root = value.getAliasedValue();
    return !root.isPhi()
        && root.definition.isInvokeVirtual()
        && root.definition.asInvokeVirtual().getInvokedMethod()
            == dexItemFactory.classMethods.getName;
  }

  /**
   * Returns a {@link DexReference} if one of the arguments to the invoke instruction is a constant
   * string that corresponds to either a class or member name (i.e., an identifier).
   *
   * @param definitions {@link DexDefinitionSupplier} that gives access to {@link DexItemFactory}.
   * @param invoke {@link InvokeMethod} that is expected to have an identifier in its arguments.
   * @return {@link DexReference} corresponding to the first constant string argument that matches a
   *     class or member name, or {@code null} if no such constant was found.
   */
  public static DexReference identifyIdentifier(
      InvokeMethod invoke, DexDefinitionSupplier definitions) {
    List<Value> ins = invoke.arguments();
    // The only static calls: Class#forName,
    //   which receive either (String) or (String, boolean, ClassLoader) as ins.
    if (invoke.isInvokeStatic()) {
      InvokeStatic invokeStatic = invoke.asInvokeStatic();
      if (definitions.dexItemFactory().classMethods
          .isReflectiveClassLookup(invokeStatic.getInvokedMethod())) {
        return ConstantValueUtils.getDexTypeFromClassForName(invokeStatic, definitions);
      }
    }

    if (invoke.isInvokeVirtual()) {
      InvokeVirtual invokeVirtual = invoke.asInvokeVirtual();
      if (isClassNameComparison(invokeVirtual, definitions.dexItemFactory())) {
        int argumentIndex = getPositionOfFirstConstString(invokeVirtual);
        if (argumentIndex >= 0) {
          return inferTypeFromConstStringValue(
              definitions, invokeVirtual.inValues().get(argumentIndex));
        }
      }
    }

    // All the other cases receive either (Class, String) or (Class, String, Class[]) as ins.
    if (ins.size() == 1) {
      return null;
    }

    boolean isReferenceFieldUpdater =
        invoke.getReturnType().descriptor
            == definitions.dexItemFactory().referenceFieldUpdaterDescriptor;
    int positionOfIdentifier = isReferenceFieldUpdater ? 2 : 1;
    Value in = ins.get(positionOfIdentifier);
    if (in.isConstString()) {
      Value classValue = ins.get(0);
      if (!classValue.isConstClass()) {
        return null;
      }
      DexType holderType = classValue.getConstInstruction().asConstClass().getValue();
      if (holderType.isArrayType()) {
        // None of the fields or methods of an array type will be renamed, since they are all
        // declared in the library. Hence there is no need to handle this case.
        return null;
      }
      DexClass holder = definitions.definitionFor(holderType);
      if (holder == null) {
        return null;
      }
      DexString dexString = in.getConstInstruction().asConstString().getValue();
      int numOfParams = ins.size();
      if (isReferenceFieldUpdater) {
        Value fieldTypeValue = ins.get(1);
        if (!fieldTypeValue.isConstClass()) {
          return null;
        }
        DexType fieldType = fieldTypeValue.getConstInstruction().asConstClass().getValue();
        return inferFieldInHolder(holder, dexString.toString(), fieldType);
      }
      if (numOfParams == 2) {
        return inferFieldInHolder(holder, dexString.toString(), null);
      }
      assert numOfParams == 3;
      DexTypeList arguments =
          retrieveDexTypeListFromClassList(invoke, ins.get(2), definitions.dexItemFactory());
      if (arguments == null) {
        return null;
      }
      return inferMethodInHolder(holder, dexString.toString(), arguments);
    }
    if (in.isDexItemBasedConstString()) {
      DexItemBasedConstString constString = in.getConstInstruction().asDexItemBasedConstString();
      return constString.getItem();
    }
    return null;
  }

  static int getPositionOfFirstConstString(Instruction instruction) {
    List<Value> inValues = instruction.inValues();
    for (int i = 0; i < inValues.size(); i++) {
      Value value = inValues.get(i).getAliasedValue();
      if (value.isConstString() || value.isDexItemBasedConstString()) {
        return i;
      }
    }
    return -1;
  }

  static DexReference inferMemberOrTypeFromNameString(
      DexDefinitionSupplier definitions, DexString dexString) {
    // "fully.qualified.ClassName.fieldOrMethodName"
    // "fully.qualified.ClassName#fieldOrMethodName"
    DexReference itemBasedString = inferMemberFromNameString(definitions, dexString);
    if (itemBasedString == null) {
      // "fully.qualified.ClassName"
      return inferTypeFromNameString(definitions, dexString);
    }
    return itemBasedString;
  }

  public static DexType inferTypeFromNameString(
      DexDefinitionSupplier definitions, DexString dexString) {
    String maybeDescriptor = javaTypeToDescriptorIfValidJavaType(dexString.toString());
    if (maybeDescriptor != null) {
      return definitions.dexItemFactory().createType(maybeDescriptor);
    }
    return null;
  }

  public static DexType inferTypeFromConstStringValue(
      DexDefinitionSupplier definitions, Value value) {
    Value root = value.getAliasedValue();
    assert !root.isPhi();
    assert root.isConstString() || root.isDexItemBasedConstString();
    if (root.isConstString()) {
      return inferTypeFromNameString(definitions, root.definition.asConstString().getValue());
    }
    if (root.isDexItemBasedConstString()) {
      DexReference reference = root.definition.asDexItemBasedConstString().getItem();
      if (reference.isDexType()) {
        return reference.asDexType();
      }
    }
    return null;
  }

  private static DexReference inferMemberFromNameString(
      DexDefinitionSupplier definitions, DexString dexString) {
    String identifier = dexString.toString();
    String typeIdentifier = null;
    String memberIdentifier = null;
    String[] items = identifier.split("#");
    // "x#y#z"
    if (items.length > 2) {
      return null;
    }
    // "fully.qualified.ClassName#fieldOrMethodName"
    if (items.length == 2) {
      typeIdentifier = items[0];
      memberIdentifier = items[1];
    } else {
      int lastDot = identifier.lastIndexOf(".");
      // "fully.qualified.ClassName.fieldOrMethodName"
      if (0 < lastDot && lastDot < identifier.length() - 1) {
        typeIdentifier = identifier.substring(0, lastDot);
        memberIdentifier = identifier.substring(lastDot + 1);
      }
    }
    if (typeIdentifier == null) {
      return null;
    }
    String maybeDescriptor = javaTypeToDescriptorIfValidJavaType(typeIdentifier);
    if (maybeDescriptor == null) {
      return null;
    }
    DexType type = definitions.dexItemFactory().createType(maybeDescriptor);
    DexClass holder = definitions.definitionFor(type);
    if (holder == null) {
      return null;
    }
    DexReference itemBasedString = inferFieldInHolder(holder, memberIdentifier, null);
    if (itemBasedString == null) {
      itemBasedString = inferMethodNameInHolder(holder, memberIdentifier);
    }
    return itemBasedString;
  }

  private static DexReference inferFieldInHolder(DexClass holder, String name, DexType fieldType) {
    for (DexEncodedField encodedField : holder.fields()) {
      if (encodedField.field.name.toString().equals(name)
          && (fieldType == null || encodedField.field.type == fieldType)) {
        return encodedField.field;
      }
    }
    return null;
  }

  private static DexReference inferMethodNameInHolder(DexClass holder, String name) {
    for (DexEncodedMethod encodedMethod : holder.methods()) {
      if (encodedMethod.method.name.toString().equals(name)) {
        return encodedMethod.method;
      }
    }
    return null;
  }

  private static DexReference inferMethodInHolder(
      DexClass holder, String name, DexTypeList arguments) {
    assert arguments != null;
    for (DexEncodedMethod encodedMethod : holder.methods()) {
      if (encodedMethod.method.name.toString().equals(name)
          && encodedMethod.method.proto.parameters.equals(arguments)) {
        return encodedMethod.method;
      }
    }
    return null;
  }

  private static DexType getTypeFromConstClassOrBoxedPrimitive(
      Value value, DexItemFactory factory) {
    if (value.isPhi()) {
      return null;
    }
    if (value.isConstant() && value.getConstInstruction().isConstClass()) {
      return value.getConstInstruction().asConstClass().getValue();
    }
    if (value.definition.isStaticGet()) {
      return factory.primitiveTypesBoxedTypeFields.boxedFieldTypeToPrimitiveType(
          value.definition.asStaticGet().getField());
    }
    return null;
  }

  // Perform a conservative evaluation of an array content of dex type values from its construction
  // until its use at a given instruction.
  private static DexType[] evaluateTypeArrayContentFromConstructionToUse(
      NewArrayEmpty newArray,
      List<CheckCast> aliases,
      int size,
      Instruction user,
      DexItemFactory factory) {
    DexType[] values = new DexType[size];
    int remaining = size;
    Set<Instruction> users = Sets.newIdentityHashSet();
    users.addAll(newArray.outValue().uniqueUsers());
    for (CheckCast alias : aliases) {
      users.addAll(alias.outValue().uniqueUsers());
    }
    // Follow the path from the array construction to the requested use collecting the constants
    // put into the array. Conservatively bail out if the content of the array cannot be statically
    // computed.
    BasicBlock block = newArray.getBlock();
    InstructionIterator iterator = block.iterator();
    iterator.nextUntil(i -> i == newArray);
    do {
      while (iterator.hasNext()) {
        Instruction instruction = iterator.next();
        // Ignore instructions which do not use the array.
        if (!users.contains(instruction)) {
          continue;
        }
        if (instruction == user) {
          // Return the array content if all elements are known when hitting the user for which
          // the content was requested.
          return remaining == 0 ? values : null;
        }
        // Any other kinds of use besides array-put mean that the array escapes and its content
        // could be altered.
        if (!instruction.isArrayPut()) {
          if (instruction.isCheckCast() && aliases.contains(instruction.asCheckCast())) {
            continue;
          }
          values = new DexType[size];
          remaining = size;
          continue;
        }
        ArrayPut arrayPut = instruction.asArrayPut();
        if (!arrayPut.index().isConstNumber()) {
          return null;
        }
        int index = arrayPut.index().getConstInstruction().asConstNumber().getIntValue();
        if (index < 0 || index >= values.length) {
          return null;
        }
        DexType type = getTypeFromConstClassOrBoxedPrimitive(arrayPut.value(), factory);
        if (type == null) {
          return null;
        }
        // Allow several writes to the same array element.
        if (values[index] == null) {
          remaining--;
        }
        values[index] = type;
      }
      if (!block.exit().isGoto()) {
        return null;
      }
      block = block.exit().asGoto().getTarget();
      // Don't allow any other control flow into the sequence of blocks filling the array from
      // construction to requested use. This will also includes loopback and guarantee that
      // this will terminate without marking visited blocks.
      if (block.getPredecessors().size() != 1) {
        return null;
      }
      iterator = block.iterator();
    } while (iterator != null);
    return null;
  }

  /**
   * Visits all {@link ArrayPut}'s with the given {@param classListValue} as array and {@link Class}
   * as value. Then collects all corresponding {@link DexType}s so as to determine reflective cases.
   *
   * @param invoke the instruction that invokes a reflective method with -identifiernamestring rule
   * @param classListValue the register that holds an array of {@link Class}'s
   * @return a list of {@link DexType} that corresponds to const class in {@param classListValue}
   */
  private static DexTypeList retrieveDexTypeListFromClassList(
      InvokeMethod invoke, Value classListValue, DexItemFactory factory) {

    // The code
    //   A.class.getMethod("m", String.class, String.class)
    // results in the following Java byte code from javac:
    //
    // LDC LA;.class
    // LDC "m"
    // ICONST_2
    // ANEWARRAY java/lang/Class
    // DUP
    // ICONST_0
    // LDC Ljava/lang/String;.class
    // AASTORE
    // DUP
    // ICONST_1
    // LDC Ljava/lang/String;.class
    // AASTORE
    // INVOKEVIRTUAL java/lang/Class.getMethod \
    //     (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    // Besides the code pattern above this supports a series of check-cast instructions. e.g.:
    //
    // A.class.getMethod("name", (Class<?>[]) new Class<?>[]{String.class})

    List<CheckCast> aliases = new ArrayList<>();
    if (!classListValue.isPhi()
        && classListValue.definition.isCheckCast()
        && classListValue.definition.asCheckCast().getType() == factory.classArrayType) {
      while (!classListValue.isPhi() && classListValue.definition.isCheckCast()) {
        aliases.add(classListValue.definition.asCheckCast());
        classListValue = classListValue.definition.asCheckCast().object();
      }
    }
    if (classListValue.isPhi()) {
      return null;
    }

    // A null argument list is an empty argument list
    if (classListValue.isZero()) {
      return DexTypeList.empty();
    }

    // Make sure this Value refers to a new array.
    if (!classListValue.definition.isNewArrayEmpty()
        || !classListValue.definition.asNewArrayEmpty().size().isConstant()) {
      return null;
    }

    int size =
        classListValue
            .definition
            .asNewArrayEmpty()
            .size()
            .getConstInstruction()
            .asConstNumber()
            .getIntValue();
    if (size == 0) {
      return DexTypeList.empty();
    }

    DexType[] arrayContent =
        evaluateTypeArrayContentFromConstructionToUse(
            classListValue.definition.asNewArrayEmpty(), aliases, size, invoke, factory);

    if (arrayContent == null) {
      return null;
    }
    return new DexTypeList(arrayContent);
  }
}
