// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// Represents the lambda descriptor inferred from calls site.
public final class LambdaDescriptor {
  private static final int LAMBDA_ALT_SERIALIZABLE = 1;
  private static final int LAMBDA_ALT_HAS_EXTRA_INTERFACES = 2;
  private static final int LAMBDA_ALT_HAS_BRIDGES = 4;
  private static final int LAMBDA_ALT_MASK = LAMBDA_ALT_SERIALIZABLE
      | LAMBDA_ALT_HAS_EXTRA_INTERFACES | LAMBDA_ALT_HAS_BRIDGES;

  static final LambdaDescriptor MATCH_FAILED = new LambdaDescriptor();

  final String uniqueId;
  final DexString name;
  final DexProto erasedProto;
  final DexProto enforcedProto;
  public final DexMethodHandle implHandle;

  final List<DexType> interfaces = new ArrayList<>();
  final Set<DexProto> bridges = Sets.newIdentityHashSet();
  final DexTypeList captures;

  // Used for accessibility analysis and few assertions only.
  private final DexEncodedMethod targetMethod;

  private LambdaDescriptor() {
    uniqueId = null;
    name = null;
    erasedProto = null;
    enforcedProto = null;
    implHandle = null;
    captures = null;
    targetMethod = null;
  }

  private LambdaDescriptor(AppInfo appInfo, DexCallSite callSite,
      DexString name, DexProto erasedProto, DexProto enforcedProto,
      DexMethodHandle implHandle, DexType mainInterface, DexTypeList captures) {
    assert appInfo != null;
    assert callSite != null;
    assert name != null;
    assert erasedProto != null;
    assert enforcedProto != null;
    assert implHandle != null;
    assert mainInterface != null;
    assert captures != null;

    this.uniqueId = callSite.getHash();
    this.name = name;
    this.erasedProto = erasedProto;
    this.enforcedProto = enforcedProto;
    this.implHandle = implHandle;
    this.captures = captures;

    this.interfaces.add(mainInterface);
    this.targetMethod = lookupTargetMethod(appInfo);
  }

  final DexType getImplReceiverType() {
    // The receiver of instance impl-method is captured as the first captured
    // value or should be the first argument of the enforced method signature.
    DexType[] params = enforcedProto.parameters.values;
    DexType[] captures = this.captures.values;
    assert captures.length > 0 || params.length > 0;
    return captures.length > 0 ? captures[0] : params[0];
  }

  private DexEncodedMethod lookupTargetMethod(AppInfo appInfo) {
    // Find the lambda's impl-method target.
    DexMethod method = implHandle.asMethod();
    switch (implHandle.type) {
      case INVOKE_DIRECT:
      case INVOKE_INSTANCE: {
        DexEncodedMethod target = appInfo.lookupVirtualTarget(getImplReceiverType(), method);
        if (target == null) {
          target = appInfo.lookupDirectTarget(method);
        }
        assert target == null
            || (implHandle.type.isInvokeInstance() && isInstanceMethod(target))
            || (implHandle.type.isInvokeDirect() && isPrivateInstanceMethod(target))
            || (implHandle.type.isInvokeDirect() && isPublicizedInstanceMethod(target));
        return target;
      }

      case INVOKE_STATIC: {
        DexEncodedMethod target = appInfo.lookupStaticTarget(method);
        assert target == null || target.accessFlags.isStatic();
        return target;
      }

      case INVOKE_CONSTRUCTOR: {
        DexEncodedMethod target = appInfo.lookupDirectTarget(method);
        assert target == null || target.accessFlags.isConstructor();
        return target;
      }

      case INVOKE_INTERFACE: {
        DexEncodedMethod target = appInfo.lookupVirtualTarget(getImplReceiverType(), method);
        assert target == null || isInstanceMethod(target);
        return target;
      }

      default:
        throw new Unreachable("Unexpected method handle kind in " + implHandle);
    }
  }

  private boolean isInstanceMethod(DexEncodedMethod encodedMethod) {
    assert encodedMethod != null;
    return !encodedMethod.accessFlags.isConstructor() && !encodedMethod.isStatic();
  }

  private boolean isPrivateInstanceMethod(DexEncodedMethod encodedMethod) {
    assert encodedMethod != null;
    return encodedMethod.isPrivateMethod() && isInstanceMethod(encodedMethod);
  }

  private boolean isPublicizedInstanceMethod(DexEncodedMethod encodedMethod) {
    assert encodedMethod != null;
    return encodedMethod.isPublicized() && isInstanceMethod(encodedMethod);
  }

  final MethodAccessFlags getAccessibility() {
    return targetMethod == null ? null : targetMethod.accessFlags;
  }

  final boolean targetFoundInClass(DexType type) {
    return targetMethod != null && targetMethod.method.holder == type;
  }

  /** If the lambda delegates to lambda$ method. */
  public boolean delegatesToLambdaImplMethod() {
    DexString methodName = implHandle.asMethod().name;
    return methodName.toString().startsWith(LambdaRewriter.EXPECTED_LAMBDA_METHOD_PREFIX);
  }

  /** Is a stateless lambda, i.e. lambda does not capture any values */
  final boolean isStateless() {
    return captures.isEmpty();
  }

  /** Checks if call site needs a accessor when referenced from `accessedFrom`. */
  boolean needsAccessor(DexType accessedFrom) {
    if (delegatesToLambdaImplMethod()) {
      return false;
    }

    if (implHandle.type.isInvokeInterface()) {
      // Interface methods must be public.
      return false;
    }

    boolean staticTarget = implHandle.type.isInvokeStatic();
    boolean instanceTarget = implHandle.type.isInvokeInstance() || implHandle.type.isInvokeDirect();
    boolean initTarget = implHandle.type.isInvokeConstructor();
    assert instanceTarget || staticTarget || initTarget;
    assert !implHandle.type.isInvokeDirect() || isPrivateInstanceMethod(targetMethod);

    if (targetMethod == null) {
      // The target cannot be a private method, since otherwise it
      // should have been found.

      if (staticTarget || initTarget) {
        // Create accessor only in case it is accessed from other
        // package, since otherwise it can be called directly.
        // NOTE: This case is different from regular instance method case
        // because the method being called must be present in method holder,
        // and not in one from its supertypes.
        boolean accessedFromSamePackage =
            accessedFrom.getPackageDescriptor().equals(
                implHandle.asMethod().holder.getPackageDescriptor());
        return !accessedFromSamePackage;
      }

      // Since instance method was not found, always generate an accessor
      // since it may be a protected method located in another package.
      return true;
    }

    MethodAccessFlags flags = targetMethod.accessFlags;

    // Private methods always need accessors.
    if (flags.isPrivate()) {
      return true;
    }
    if (flags.isPublic()) {
      return false;
    }

    boolean accessedFromSamePackage =
        accessedFrom.getPackageDescriptor().equals(
            targetMethod.method.holder.getPackageDescriptor());
    assert flags.isProtected() || accessedFromSamePackage;
    return flags.isProtected() && !accessedFromSamePackage;
  }

  /**
   * Matches call site for lambda metafactory invocation pattern and
   * returns extracted match information, or null if match failed.
   */
  public static LambdaDescriptor tryInfer(DexCallSite callSite, AppInfo appInfo) {
    LambdaDescriptor descriptor = infer(callSite, appInfo);
    return descriptor == MATCH_FAILED ? null : descriptor;
  }

  /**
   * Matches call site for lambda metafactory invocation pattern and
   * returns extracted match information, or MATCH_FAILED if match failed.
   */
  static LambdaDescriptor infer(DexCallSite callSite, AppInfo appInfo) {
    // We expect bootstrap method to be either `metafactory` or `altMetafactory` method
    // of `java.lang.invoke.LambdaMetafactory` class. Both methods are static.
    if (!callSite.bootstrapMethod.type.isInvokeStatic()) {
      return LambdaDescriptor.MATCH_FAILED;
    }

    DexItemFactory factory = appInfo.dexItemFactory();
    DexMethod bootstrapMethod = callSite.bootstrapMethod.asMethod();
    if (!factory.isLambdaMetafactoryMethod(bootstrapMethod)) {
      // It is not a lambda, thus no need to manage this call site.
      return LambdaDescriptor.MATCH_FAILED;
    }

    // 'Method name' operand of the invoke-custom instruction represents
    // the name of the functional interface main method.
    DexString funcMethodName = callSite.methodName;

    // Signature of main functional interface method.
    DexValue.DexValueMethodType funcErasedSignature =
        getBootstrapArgument(callSite.bootstrapArgs, 0, DexValue.DexValueMethodType.class);

    // Method handle of the implementation method.
    DexMethodHandle lambdaImplMethodHandle =
        getBootstrapArgument(callSite.bootstrapArgs, 1, DexValue.DexValueMethodHandle.class).value;
    // Even though there are some limitations on which method handle kinds are
    // allowed for lambda impl-methods, there is no way to detect unsupported
    // handle kinds after they are transformed into DEX method handle.

    // Signature to be enforced on main method.
    DexValue.DexValueMethodType funcEnforcedSignature =
        getBootstrapArgument(callSite.bootstrapArgs, 2, DexValue.DexValueMethodType.class);
    if (!isEnforcedSignatureValid(
        factory, funcEnforcedSignature.value, funcErasedSignature.value)) {
      throw new Unreachable(
          "Enforced and erased signatures are inconsistent in " + callSite.toString());
    }

    // 'Method type' of the invoke-custom instruction represents the signature
    // of the lambda method factory.
    DexProto lambdaFactoryProto = callSite.methodProto;
    // Main functional interface is the return type of the lambda factory method.
    DexType mainFuncInterface = lambdaFactoryProto.returnType;
    // Lambda captures are represented as parameters of the lambda factory method.
    DexTypeList captures = lambdaFactoryProto.parameters;

    // Create a match.
    LambdaDescriptor match = new LambdaDescriptor(appInfo, callSite,
        funcMethodName, funcErasedSignature.value, funcEnforcedSignature.value,
        lambdaImplMethodHandle, mainFuncInterface, captures);

    if (bootstrapMethod == factory.metafactoryMethod) {
      if (callSite.bootstrapArgs.size() != 3) {
        throw new Unreachable(
            "Unexpected number of metafactory method arguments in " + callSite.toString());
      }
    } else {
      extractAltMetafactory(
          factory,
          callSite.bootstrapArgs,
          interfaceType -> {
            if (!match.interfaces.contains(interfaceType)) {
              match.interfaces.add(interfaceType);
            }
          },
          match.bridges::add);
    }

    return match;
  }

  private static void extractAltMetafactory(
      DexItemFactory dexItemFactory,
      List<DexValue> bootstrapArgs,
      Consumer<DexType> interfaceConsumer,
      Consumer<DexProto> bridgeConsumer) {
    int argIndex = 3;
    int flagsArg =
        getBootstrapArgument(bootstrapArgs, argIndex++, DexValue.DexValueInt.class).value;
    assert (flagsArg & ~LAMBDA_ALT_MASK) == 0;

    // Load extra interfaces if any.
    if ((flagsArg & LAMBDA_ALT_HAS_EXTRA_INTERFACES) != 0) {
      int count = getBootstrapArgument(bootstrapArgs, argIndex++, DexValue.DexValueInt.class).value;
      for (int i = 0; i < count; i++) {
        DexType interfaceType =
            getBootstrapArgument(bootstrapArgs, argIndex++, DexValue.DexValueType.class).value;
        interfaceConsumer.accept(interfaceType);
      }
    }

    // If the lambda is serializable, add it.
    if ((flagsArg & LAMBDA_ALT_SERIALIZABLE) != 0) {
      interfaceConsumer.accept(dexItemFactory.serializableType);
    }

    // Load bridges if any.
    if ((flagsArg & LAMBDA_ALT_HAS_BRIDGES) != 0) {
      int count = getBootstrapArgument(bootstrapArgs, argIndex++, DexValue.DexValueInt.class).value;
      for (int i = 0; i < count; i++) {
        DexProto bridgeProto =
            getBootstrapArgument(bootstrapArgs, argIndex++, DexValue.DexValueMethodType.class)
                .value;
        bridgeConsumer.accept(bridgeProto);
      }
    }

    if (bootstrapArgs.size() != argIndex) {
      throw new Unreachable("Unexpected number of metafactory method arguments in DexCallSite");
    }
  }

  public static List<DexType> getInterfaces(DexCallSite callSite, AppInfo appInfo) {
    LambdaDescriptor descriptor = infer(callSite, appInfo);
    if (descriptor == LambdaDescriptor.MATCH_FAILED) {
      return null;
    }
    return descriptor.interfaces;
  }

  @SuppressWarnings("unchecked")
  private static <T> T getBootstrapArgument(List<DexValue> bootstrapArgs, int i, Class<T> clazz) {
    if (bootstrapArgs.size() < i) {
      throw new Unreachable(
          "Expected to find at least " + i + " bootstrap arguments in DexCallSite");
    }
    DexValue value = bootstrapArgs.get(i);
    if (!clazz.isAssignableFrom(value.getClass())) {
      throw new Unreachable("Unexpected type of bootstrap arguments #" + i + " in DexCallSite");
    }
    return (T) value;
  }

  private static boolean isEnforcedSignatureValid(
      DexItemFactory factory, DexProto enforced, DexProto erased) {
    if (!isSameOrDerived(factory, enforced.returnType, erased.returnType)) {
      return false;
    }
    DexType[] enforcedValues = enforced.parameters.values;
    DexType[] erasedValues = erased.parameters.values;
    int count = enforcedValues.length;
    if (count != erasedValues.length) {
      return false;
    }
    for (int i = 0; i < count; i++) {
      if (!isSameOrDerived(factory, enforcedValues[i], erasedValues[i])) {
        return false;
      }
    }
    return true;
  }

  // Checks if the types are the same OR both types are reference types and
  // `subType` is derived from `b`. Note that in the latter case we only check if
  // both types are class types, for the reasons mentioned in isSameOrAdaptableTo(...).
  static boolean isSameOrDerived(
      DexItemFactory factory, DexType subType, DexType superType) {
    if (subType == superType || (subType.isClassType() && superType.isClassType())) {
      return true;
    }

    if (subType.isArrayType()) {
      if (superType.isArrayType()) {
        // X[] -> Y[].
        return isSameOrDerived(factory,
            subType.toArrayElementType(factory), superType.toArrayElementType(factory));
      }
      return superType == factory.objectType; // T[] -> Object.
    }

    return false;
  }
}
