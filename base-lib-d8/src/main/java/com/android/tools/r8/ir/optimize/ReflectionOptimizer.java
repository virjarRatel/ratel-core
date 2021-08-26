// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import static com.android.tools.r8.graph.DexProgramClass.asProgramClassOrNull;
import static com.android.tools.r8.ir.analysis.type.Nullability.definitelyNotNull;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.ClassInitializationAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeAnalysis;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.optimize.Inliner.ConstraintWithTarget;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.Sets;
import java.util.Set;

public class ReflectionOptimizer {

  // Rewrite getClass() to const-class if the type of the given instance is effectively final.
  // Rewrite forName() to const-class if the type is resolvable, accessible and already initialized.
  public static void rewriteGetClassOrForNameToConstClass(
      AppView<AppInfoWithLiveness> appView, IRCode code) {
    Set<Value> affectedValues = Sets.newIdentityHashSet();
    DexType context = code.method.method.holder;
    ClassInitializationAnalysis classInitializationAnalysis =
        new ClassInitializationAnalysis(appView, code);
    for (BasicBlock block : code.blocks) {
      // Conservatively bail out if the containing block has catch handlers.
      // TODO(b/118509730): unless join of all catch types is ClassNotFoundException ?
      if (block.hasCatchHandlers()) {
        continue;
      }
      InstructionListIterator it = block.listIterator(code);
      while (it.hasNext()) {
        Instruction current = it.next();
        if (!current.hasOutValue() || !current.outValue().isUsed()) {
          continue;
        }
        DexType type = null;
        if (current.isInvokeVirtual()) {
          type = getTypeForGetClass( appView, context, current.asInvokeVirtual());
        } else if (current.isInvokeStatic()) {
          type = getTypeForClassForName(
              appView, classInitializationAnalysis, context, current.asInvokeStatic());
        }
        if (type != null) {
          affectedValues.addAll(current.outValue().affectedValues());
          TypeLatticeElement typeLattice =
              TypeLatticeElement.classClassType(appView, definitelyNotNull());
          Value value = code.createValue(typeLattice, current.getLocalInfo());
          ConstClass constClass = new ConstClass(value, type);
          it.replaceCurrentInstruction(constClass);
        }
      }
    }
    classInitializationAnalysis.finish();
    // Newly introduced const-class is not null, and thus propagate that information.
    if (!affectedValues.isEmpty()) {
      new TypeAnalysis(appView).narrowing(affectedValues);
    }
    assert code.isConsistentSSA();
  }

  private static DexType getTypeForGetClass(
      AppView<AppInfoWithLiveness> appView,
      DexType context,
      InvokeVirtual invoke) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    DexMethod invokedMethod = invoke.getInvokedMethod();
    // Class<?> Object#getClass() is final and cannot be overridden.
    if (invokedMethod != dexItemFactory.objectMethods.getClass) {
      return null;
    }
    Value in = invoke.getReceiver();
    if (in.hasLocalInfo()) {
      return null;
    }
    TypeLatticeElement inType = in.getTypeLattice();
    // Check the receiver is either class type or array type. Also make sure it is not
    // nullable.
    if (!(inType.isClassType() || inType.isArrayType())
        || inType.isNullable()) {
      return null;
    }
    DexType type =
        inType.isClassType()
            ? inType.asClassTypeLatticeElement().getClassType()
            : inType.asArrayTypeLatticeElement().getArrayType(dexItemFactory);
    DexType baseType = type.toBaseType(dexItemFactory);
    // Make sure base type is a class type.
    if (!baseType.isClassType()) {
      return null;
    }
    // Only consider program class, e.g., platform can introduce subtypes in different
    // versions.
    DexProgramClass clazz = asProgramClassOrNull(appView.definitionFor(baseType));
    if (clazz == null) {
      return null;
    }
    // Only consider effectively final class. Exception: new Base().getClass().
    if (appView.appInfo().hasSubtypes(baseType)
        && appView.appInfo().isInstantiatedIndirectly(clazz)
        && (in.isPhi() || !in.definition.isCreatingInstanceOrArray())) {
      return null;
    }
    // Make sure the target (base) type is visible.
    ConstraintWithTarget constraints =
        ConstraintWithTarget.classIsVisible(context, baseType, appView);
    if (constraints == ConstraintWithTarget.NEVER) {
      return null;
    }
    return type;
  }

  private static DexType getTypeForClassForName(
      AppView<AppInfoWithLiveness> appView,
      ClassInitializationAnalysis classInitializationAnalysis,
      DexType context,
      InvokeStatic invoke) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    DexMethod invokedMethod = invoke.getInvokedMethod();
    // Class<?> Class#forName(String) is final and cannot be overridden.
    if (invokedMethod != dexItemFactory.classMethods.forName) {
      return null;
    }
    assert invoke.inValues().size() == 1;
    Value in = invoke.inValues().get(0).getAliasedValue();
    // Only consider const-string input without locals.
    if (in.hasLocalInfo() || in.isPhi()) {
      return null;
    }
    // Also, check if the result of forName() is updatable via locals.
    Value out = invoke.outValue();
    if (out != null && out.hasLocalInfo()) {
      return null;
    }
    DexType type = null;
    if (in.definition.isDexItemBasedConstString()) {
      if (in.definition.asDexItemBasedConstString().getItem().isDexType()) {
        type = in.definition.asDexItemBasedConstString().getItem().asDexType();
      }
    } else if (in.definition.isConstString()) {
      String name = in.definition.asConstString().getValue().toString();
      // Convert the name into descriptor if the given name is a valid java type.
      String descriptor = DescriptorUtils.javaTypeToDescriptorIfValidJavaType(name);
      // Otherwise, it may be an array's fully qualified name from Class<?>#getName().
      if (descriptor == null && name.startsWith("[") && name.endsWith(";")) {
        // E.g., [Lx.y.Z; -> [Lx/y/Z;
        descriptor = name.replace(
            DescriptorUtils.JAVA_PACKAGE_SEPARATOR,
            DescriptorUtils.DESCRIPTOR_PACKAGE_SEPARATOR);
      }
      if (descriptor == null
          || descriptor.indexOf(DescriptorUtils.JAVA_PACKAGE_SEPARATOR) > 0) {
        return null;
      }
      type = dexItemFactory.createType(descriptor);
      // Check if the given name refers to a reference type.
      if (!type.isReferenceType()) {
        return null;
      }
    } else {
      // Bail out for non-deterministic input to Class<?>#forName(name).
      return null;
    }
    if (type == null) {
      return null;
    }
    // Make sure the (base) type is resolvable.
    DexType baseType = type.toBaseType(dexItemFactory);
    DexClass baseClazz = appView.definitionFor(baseType);
    if (baseClazz == null || !baseClazz.isResolvable(appView)) {
      return null;
    }
    // Make sure the (base) type is visible.
    ConstraintWithTarget constraints =
        ConstraintWithTarget.classIsVisible(context, baseType, appView);
    if (constraints == ConstraintWithTarget.NEVER) {
      return null;
    }
    // Make sure the type is already initialized.
    // Note that, if the given name refers to an array type, the corresponding Class<?> won't
    // be initialized. So, it's okay to rewrite the instruction.
    if (type.isClassType()
        && !classInitializationAnalysis.isClassDefinitelyLoadedBeforeInstruction(type, invoke)) {
      return null;
    }
    return type;
  }
}
