// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.NestMemberClassAttribute;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.origin.SynthesizedOrigin;
import com.android.tools.r8.utils.BooleanUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

// NestBasedAccessDesugaring contains common code between the two subclasses
// which are specialized for d8 and r8
public abstract class NestBasedAccessDesugaring {

  // Short names to avoid creating long strings
  private static final String NEST_ACCESS_NAME_PREFIX = "-$$Nest$";
  private static final String NEST_ACCESS_METHOD_NAME_PREFIX = NEST_ACCESS_NAME_PREFIX + "m";
  private static final String NEST_ACCESS_STATIC_METHOD_NAME_PREFIX =
      NEST_ACCESS_NAME_PREFIX + "sm";
  private static final String NEST_ACCESS_FIELD_GET_NAME_PREFIX = NEST_ACCESS_NAME_PREFIX + "fget";
  private static final String NEST_ACCESS_STATIC_GET_FIELD_NAME_PREFIX =
      NEST_ACCESS_NAME_PREFIX + "sfget";
  private static final String NEST_ACCESS_FIELD_PUT_NAME_PREFIX = NEST_ACCESS_NAME_PREFIX + "fput";
  private static final String NEST_ACCESS_STATIC_PUT_FIELD_NAME_PREFIX =
      NEST_ACCESS_NAME_PREFIX + "sfput";
  public static final String NEST_CONSTRUCTOR_NAME = NEST_ACCESS_NAME_PREFIX + "Constructor";
  private static final String FULL_NEST_CONTRUCTOR_NAME = "L" + NEST_CONSTRUCTOR_NAME + ";";

  protected final AppView<?> appView;
  // Following maps are there to avoid creating the bridges multiple times
  // and remember the bridges to add once the nests are processed.
  final Map<DexMethod, DexEncodedMethod> bridges = new ConcurrentHashMap<>();
  final Map<DexField, DexEncodedMethod> getFieldBridges = new ConcurrentHashMap<>();
  final Map<DexField, DexEncodedMethod> putFieldBridges = new ConcurrentHashMap<>();
  // Common single empty class for nest based private constructors
  private final DexProgramClass nestConstructor;
  private boolean nestConstructorUsed = false;

  NestBasedAccessDesugaring(AppView<?> appView) {
    this.appView = appView;
    this.nestConstructor = createNestAccessConstructor();
  }

  DexType getNestConstructorType() {
    return nestConstructor.type;
  }

  abstract void reportMissingNestHost(DexClass clazz);

  abstract void reportIncompleteNest(List<DexType> nest);

  DexClass definitionFor(DexType type) {
    return appView.definitionFor(appView.graphLense().lookupType(type));
  }

  private DexEncodedMethod definitionFor(
      DexMethod method, DexMethod context, Invoke.Type invokeType) {
    return appView.definitionFor(
        appView.graphLense().lookupMethod(method, context, invokeType).getMethod());
  }

  private DexEncodedField definitionFor(DexField field) {
    return appView.definitionFor(appView.graphLense().lookupField(field));
  }

  // Extract the list of types in the programClass' nest, of host hostClass
  private List<DexType> extractNest(DexClass clazz) {
    assert clazz != null;
    DexClass hostClass = clazz.isNestHost() ? clazz : definitionFor(clazz.getNestHost());
    if (hostClass == null) {
      reportMissingNestHost(clazz);
      // Missing nest host means the class is considered as not being part of a nest.
      clazz.clearNestHost();
      return null;
    }
    List<DexType> classesInNest =
        new ArrayList<>(hostClass.getNestMembersClassAttributes().size() + 1);
    for (NestMemberClassAttribute nestmate : hostClass.getNestMembersClassAttributes()) {
      classesInNest.add(nestmate.getNestMember());
    }
    classesInNest.add(hostClass.type);
    return classesInNest;
  }

  Future<?> asyncProcessNest(DexClass clazz, ExecutorService executorService) {
    return executorService.submit(
        () -> {
          List<DexType> nest = extractNest(clazz);
          // Nest is null when nest host is missing, we do nothing in this case.
          if (nest != null) {
            processNest(nest);
          }
          return null; // we want a Callable not a Runnable to be able to throw
        });
  }

  private void processNest(List<DexType> nest) {
    boolean reported = false;
    for (DexType type : nest) {
      DexClass clazz = definitionFor(type);
      if (clazz == null) {
        if (!reported) {
          reportIncompleteNest(nest);
          reported = true;
        }
      } else {
        if (shouldProcessClassInNest(clazz, nest)) {
          NestBasedAccessDesugaringUseRegistry registry =
              new NestBasedAccessDesugaringUseRegistry(clazz);
          for (DexEncodedMethod method : clazz.methods()) {
            registry.setContext(method.method);
            method.registerCodeReferences(registry);
          }
        }
      }
    }
  }

  protected abstract boolean shouldProcessClassInNest(DexClass clazz, List<DexType> nest);

  private DexProgramClass createNestAccessConstructor() {
    return new DexProgramClass(
        appView.dexItemFactory().createType(FULL_NEST_CONTRUCTOR_NAME),
        null,
        new SynthesizedOrigin("Nest based access desugaring", getClass()),
        // Make the synthesized class public since shared in the whole program.
        ClassAccessFlags.fromDexAccessFlags(
            Constants.ACC_FINAL | Constants.ACC_SYNTHETIC | Constants.ACC_PUBLIC),
        appView.dexItemFactory().objectType,
        DexTypeList.empty(),
        appView.dexItemFactory().createString("nest"),
        null,
        Collections.emptyList(),
        null,
        Collections.emptyList(),
        DexAnnotationSet.empty(),
        DexEncodedField.EMPTY_ARRAY,
        DexEncodedField.EMPTY_ARRAY,
        DexEncodedMethod.EMPTY_ARRAY,
        DexEncodedMethod.EMPTY_ARRAY,
        appView.dexItemFactory().getSkipNameValidationForTesting(),
        DexProgramClass::checksumFromType);
  }

  void synthesizeNestConstructor(DexApplication.Builder<?> builder) {
    if (nestConstructorUsed) {
      appView.appInfo().addSynthesizedClass(nestConstructor);
      builder.addSynthesizedClass(nestConstructor, true);
    }
  }

  public static boolean isNestConstructor(DexType type) {
    return type.getName().equals(NEST_CONSTRUCTOR_NAME);
  }

  private DexString computeMethodBridgeName(DexEncodedMethod method) {
    String methodName = method.method.name.toString();
    String fullName;
    if (method.isStatic()) {
      fullName = NEST_ACCESS_STATIC_METHOD_NAME_PREFIX + methodName;
    } else {
      fullName = NEST_ACCESS_METHOD_NAME_PREFIX + methodName;
    }
    return appView.dexItemFactory().createString(fullName);
  }

  private DexString computeFieldBridgeName(DexEncodedField field, boolean isGet) {
    String fieldName = field.field.name.toString();
    String fullName;
    if (isGet && !field.isStatic()) {
      fullName = NEST_ACCESS_FIELD_GET_NAME_PREFIX + fieldName;
    } else if (isGet) {
      fullName = NEST_ACCESS_STATIC_GET_FIELD_NAME_PREFIX + fieldName;
    } else if (!field.isStatic()) {
      fullName = NEST_ACCESS_FIELD_PUT_NAME_PREFIX + fieldName;
    } else {
      fullName = NEST_ACCESS_STATIC_PUT_FIELD_NAME_PREFIX + fieldName;
    }
    return appView.dexItemFactory().createString(fullName);
  }

  private DexMethod computeMethodBridge(DexEncodedMethod encodedMethod) {
    DexMethod method = encodedMethod.method;
    DexProto proto =
        encodedMethod.accessFlags.isStatic()
            ? method.proto
            : appView.dexItemFactory().prependTypeToProto(method.holder, method.proto);
    return appView
        .dexItemFactory()
        .createMethod(method.holder, proto, computeMethodBridgeName(encodedMethod));
  }

  private DexMethod computeInitializerBridge(DexMethod method) {
    DexProto newProto =
        appView.dexItemFactory().appendTypeToProto(method.proto, nestConstructor.type);
    return appView.dexItemFactory().createMethod(method.holder, newProto, method.name);
  }

  private DexMethod computeFieldBridge(DexEncodedField field, boolean isGet) {
    DexType holderType = field.field.holder;
    DexType fieldType = field.field.type;
    int bridgeParameterCount =
        BooleanUtils.intValue(!field.isStatic()) + BooleanUtils.intValue(!isGet);
    DexType[] parameters = new DexType[bridgeParameterCount];
    if (!isGet) {
      parameters[parameters.length - 1] = fieldType;
    }
    if (!field.isStatic()) {
      parameters[0] = holderType;
    }
    DexType returnType = isGet ? fieldType : appView.dexItemFactory().voidType;
    DexProto proto = appView.dexItemFactory().createProto(returnType, parameters);
    return appView
        .dexItemFactory()
        .createMethod(holderType, proto, computeFieldBridgeName(field, isGet));
  }

  boolean invokeRequiresRewriting(DexEncodedMethod method, DexClass contextClass) {
    assert method != null;
    // Rewrite only when targeting other nest members private fields.
    if (!method.accessFlags.isPrivate() || method.method.holder == contextClass.type) {
      return false;
    }
    DexClass methodHolder = definitionFor(method.method.holder);
    assert methodHolder != null; // from encodedMethod
    return methodHolder.getNestHost() == contextClass.getNestHost();
  }

  boolean fieldAccessRequiresRewriting(DexEncodedField field, DexClass contextClass) {
    assert field != null;
    // Rewrite only when targeting other nest members private fields.
    if (!field.accessFlags.isPrivate() || field.field.holder == contextClass.type) {
      return false;
    }
    DexClass fieldHolder = definitionFor(field.field.holder);
    assert fieldHolder != null; // from encodedField
    return fieldHolder.getNestHost() == contextClass.getNestHost();
  }

  private boolean holderRequiresBridge(DexClass holder) {
    // Bridges are added on program classes only.
    // Bridges on class paths are added in different compilation units.
    if (holder.isProgramClass()) {
      return false;
    } else if (holder.isClasspathClass()) {
      return true;
    }
    assert holder.isLibraryClass();
    List<DexType> nest = extractNest(holder);
    assert nest != null : "Should be a compilation error if missing nest host on library class.";
    reportIncompleteNest(nest);
    throw new Unreachable(
        "Incomplete nest due to missing library class should raise a compilation error.");
  }

  DexMethod ensureFieldAccessBridge(DexEncodedField field, boolean isGet) {
    DexClass holder = definitionFor(field.field.holder);
    assert holder != null;
    DexMethod bridgeMethod = computeFieldBridge(field, isGet);
    if (holderRequiresBridge(holder)) {
      return bridgeMethod;
    }
    // The map is used to avoid creating multiple times the bridge
    // and remembers the bridges to add.
    Map<DexField, DexEncodedMethod> fieldMap = isGet ? getFieldBridges : putFieldBridges;
    fieldMap.computeIfAbsent(
        field.field,
        k ->
            DexEncodedMethod.createFieldAccessorBridge(
                new DexFieldWithAccess(field, isGet), holder, bridgeMethod));
    return bridgeMethod;
  }

  DexMethod ensureInvokeBridge(DexEncodedMethod method) {
    // We add bridges only when targeting other nest members.
    DexClass holder = definitionFor(method.method.holder);
    assert holder != null;
    DexMethod bridgeMethod;
    if (method.isInstanceInitializer()) {
      nestConstructorUsed = true;
      bridgeMethod = computeInitializerBridge(method.method);
    } else {
      bridgeMethod = computeMethodBridge(method);
    }
    if (holderRequiresBridge(holder)) {
      return bridgeMethod;
    }
    // The map is used to avoid creating multiple times the bridge
    // and remembers the bridges to add.
    bridges.computeIfAbsent(
        method.method,
        k ->
            method.isInstanceInitializer()
                ? method.toInitializerForwardingBridge(holder, bridgeMethod)
                : method.toStaticForwardingBridge(holder, computeMethodBridge(method)));
    return bridgeMethod;
  }

  protected class NestBasedAccessDesugaringUseRegistry extends UseRegistry {

    private final DexClass currentClass;
    private DexMethod context;

    NestBasedAccessDesugaringUseRegistry(DexClass currentClass) {
      super(appView.options().itemFactory);
      this.currentClass = currentClass;
    }

    public void setContext(DexMethod context) {
      this.context = context;
    }

    private boolean registerInvoke(DexMethod method, Invoke.Type invokeType) {
      // Calls to non class type are not done through nest based access control.
      // Work-around for calls to enum.clone().
      if (!method.holder.isClassType()) {
        return false;
      }
      DexEncodedMethod encodedMethod = definitionFor(method, context, invokeType);
      if (encodedMethod != null && invokeRequiresRewriting(encodedMethod, currentClass)) {
        ensureInvokeBridge(encodedMethod);
        return true;
      }
      return false;
    }

    private boolean registerFieldAccess(DexField field, boolean isGet) {
      DexEncodedField encodedField = definitionFor(field);
      if (encodedField != null && fieldAccessRequiresRewriting(encodedField, currentClass)) {
        ensureFieldAccessBridge(encodedField, isGet);
        return true;
      }
      return false;
    }

    @Override
    public boolean registerInvokeVirtual(DexMethod method) {
      // Calls to class nest mate private methods are targeted by invokeVirtual in jdk11.
      // The spec recommends to do so, but do not enforce it, hence invokeDirect is also registered.
      return registerInvoke(method, Invoke.Type.VIRTUAL);
    }

    @Override
    public boolean registerInvokeDirect(DexMethod method) {
      return registerInvoke(method, Invoke.Type.DIRECT);
    }

    @Override
    public boolean registerInvokeStatic(DexMethod method) {
      return registerInvoke(method, Invoke.Type.STATIC);
    }

    @Override
    public boolean registerInvokeInterface(DexMethod method) {
      // Calls to interface nest mate private methods are targeted by invokeInterface in jdk11.
      // The spec recommends to do so, but do not enforce it, hence invokeDirect is also registered.
      return registerInvoke(method, Invoke.Type.INTERFACE);
    }

    @Override
    public boolean registerInvokeSuper(DexMethod method) {
      // Cannot target private method.
      return false;
    }

    @Override
    public boolean registerInstanceFieldWrite(DexField field) {
      return registerFieldAccess(field, false);
    }

    @Override
    public boolean registerInstanceFieldRead(DexField field) {
      return registerFieldAccess(field, true);
    }

    @Override
    public boolean registerNewInstance(DexType type) {
      // Unrelated to access based control.
      // The <init> method has to be rewritten instead
      // and <init> is called through registerInvoke.
      return false;
    }

    @Override
    public boolean registerStaticFieldRead(DexField field) {
      return registerFieldAccess(field, true);
    }

    @Override
    public boolean registerStaticFieldWrite(DexField field) {
      return registerFieldAccess(field, false);
    }

    @Override
    public boolean registerTypeReference(DexType type) {
      // Unrelated to access based control.
      return false;
    }
  }

  public static final class DexFieldWithAccess {

    private final DexEncodedField field;
    private final boolean isGet;

    DexFieldWithAccess(DexEncodedField field, boolean isGet) {
      this.field = field;
      this.isGet = isGet;
    }

    @Override
    public int hashCode() {
      return Objects.hash(field, isGet);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (getClass() != o.getClass()) {
        return false;
      }
      DexFieldWithAccess other = (DexFieldWithAccess) o;
      return isGet == other.isGet && field == other.field;
    }

    public boolean isGet() {
      return isGet;
    }

    public boolean isStatic() {
      return field.accessFlags.isStatic();
    }

    public boolean isPut() {
      return !isGet();
    }

    public boolean isInstance() {
      return !isStatic();
    }

    public boolean isStaticGet() {
      return isStatic() && isGet();
    }

    public boolean isStaticPut() {
      return isStatic() && isPut();
    }

    public boolean isInstanceGet() {
      return isInstance() && isGet();
    }

    public boolean isInstancePut() {
      return isInstance() && isPut();
    }

    public DexType getType() {
      return field.field.type;
    }

    public DexType getHolder() {
      return field.field.holder;
    }

    public DexField getField() {
      return field.field;
    }
  }
}
