// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.DesugarGraphConsumer;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication.Builder;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.NestedGraphLense;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeSuper;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.desugar.DefaultMethodsHelper.Collection;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.origin.SynthesizedOrigin;
import com.android.tools.r8.position.MethodPosition;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Pair;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

//
// Default and static interface method desugaring rewriter (note that lambda
// desugaring should have already processed the code before this rewriter).
//
// In short, during default and static interface method desugaring
// the following actions are performed:
//
//   (1) All static interface methods are moved into companion classes. All calls
//       to these methods are redirected appropriately. All references to these
//       methods from method handles are reported as errors.
//
// Companion class is a synthesized class (<interface-name>-CC) created to host
// static and former default interface methods (see below) from the interface.
//
//   (2) All default interface methods are made static and moved into companion
//       class.
//
//   (3) All calls to default interface methods made via 'super' are changed
//       to directly call appropriate static methods in companion classes.
//
//   (4) All other calls or references to default interface methods are not changed.
//
//   (5) For all program classes explicitly implementing interfaces we analyze the
//       set of default interface methods missing and add them, the created methods
//       forward the call to an appropriate method in interface companion class.
//
public final class InterfaceMethodRewriter {

  // Public for testing.
  public static final String EMULATE_LIBRARY_CLASS_NAME_SUFFIX = "$-EL";
  public static final String DISPATCH_CLASS_NAME_SUFFIX = "$-DC";
  public static final String COMPANION_CLASS_NAME_SUFFIX = "$-CC";
  public static final String DEFAULT_METHOD_PREFIX = "$default$";
  public static final String PRIVATE_METHOD_PREFIX = "$private$";

  private final AppView<?> appView;
  private final IRConverter converter;
  private final InternalOptions options;
  final DexItemFactory factory;
  private final Map<DexType, DexType> emulatedInterfaces = new IdentityHashMap<>();
  // The emulatedMethod set is there to avoid doing the emulated look-up too often.
  private final Set<DexString> emulatedMethods = Sets.newIdentityHashSet();

  // All forwarding methods generated during desugaring. We don't synchronize access
  // to this collection since it is only filled in ClassProcessor running synchronously.
  private final Set<DexEncodedMethod> synthesizedMethods = Sets.newIdentityHashSet();

  // Caches default interface method info for already processed interfaces.
  private final Map<DexType, DefaultMethodsHelper.Collection> cache = new ConcurrentHashMap<>();

  /** Interfaces requiring dispatch classes to be created, and appropriate callers. */
  private final ConcurrentMap<DexLibraryClass, Set<DexProgramClass>> requiredDispatchClasses =
      new ConcurrentHashMap<>();

  /**
   * Defines a minor variation in desugaring.
   */
  public enum Flavor {
    /**
     * Process all application resources.
     */
    IncludeAllResources,
    /**
     * Process all but DEX application resources.
     */
    ExcludeDexResources
  }

  public InterfaceMethodRewriter(AppView<?> appView, IRConverter converter) {
    assert converter != null;
    this.appView = appView;
    this.converter = converter;
    this.options = appView.options();
    this.factory = appView.dexItemFactory();
    initializeEmulatedInterfaceVariables();
  }

  private void initializeEmulatedInterfaceVariables() {
    Map<DexType, DexType> emulateLibraryInterface =
        options.desugaredLibraryConfiguration.getEmulateLibraryInterface();
    for (DexType interfaceType : emulateLibraryInterface.keySet()) {
      emulatedInterfaces.put(interfaceType, emulateLibraryInterface.get(interfaceType));
      addRewritePrefix(interfaceType, emulateLibraryInterface.get(interfaceType).toSourceString());
      DexClass emulatedInterfaceClass = appView.definitionFor(interfaceType);
      if (emulatedInterfaceClass != null) {
        for (DexEncodedMethod encodedMethod :
            emulatedInterfaceClass.methods(DexEncodedMethod::isDefaultMethod)) {
          emulatedMethods.add(encodedMethod.method.name);
        }
      }
    }
  }

  private void addRewritePrefix(DexType interfaceType, String rewrittenType) {
    appView.rewritePrefix.rewriteType(
        getCompanionClassType(interfaceType),
        factory.createType(
            DescriptorUtils.javaTypeToDescriptor(rewrittenType + COMPANION_CLASS_NAME_SUFFIX)));
    appView.rewritePrefix.rewriteType(
        getEmulateLibraryInterfaceClassType(interfaceType, factory),
        factory.createType(
            DescriptorUtils.javaTypeToDescriptor(
                rewrittenType + EMULATE_LIBRARY_CLASS_NAME_SUFFIX)));
  }

  boolean isEmulatedInterface(DexType itf) {
    return emulatedInterfaces.containsKey(itf);
  }

  // Rewrites the references to static and default interface methods.
  // NOTE: can be called for different methods concurrently.
  public void rewriteMethodReferences(DexEncodedMethod encodedMethod, IRCode code) {
    if (synthesizedMethods.contains(encodedMethod)) {
      return;
    }

    ListIterator<BasicBlock> blocks = code.listIterator();
    AppInfo appInfo = appView.appInfo();
    while (blocks.hasNext()) {
      BasicBlock block = blocks.next();
      InstructionListIterator instructions = block.listIterator(code);
      while (instructions.hasNext()) {
        Instruction instruction = instructions.next();

        if (instruction.isInvokeCustom()) {
          // Check that static interface methods are not referenced
          // from invoke-custom instructions via method handles.
          DexCallSite callSite = instruction.asInvokeCustom().getCallSite();
          reportStaticInterfaceMethodHandle(encodedMethod.method, callSite.bootstrapMethod);
          for (DexValue arg : callSite.bootstrapArgs) {
            if (arg instanceof DexValue.DexValueMethodHandle) {
              reportStaticInterfaceMethodHandle(encodedMethod.method,
                  ((DexValue.DexValueMethodHandle) arg).value);
            }
          }
          continue;
        }

        if (instruction.isInvokeStatic()) {
          InvokeStatic invokeStatic = instruction.asInvokeStatic();
          DexMethod method = invokeStatic.getInvokedMethod();
          DexClass clazz = appInfo.definitionFor(method.holder);
          if (BackportedMethodRewriter.hasRewrittenMethodPrefix(method.holder)) {
            // We did not create this code yet, but it will not require rewriting.
            continue;
          }
          if (clazz == null) {
            // NOTE: leave unchanged those calls to undefined targets. This may lead to runtime
            // exception but we can not report it as error since it can also be the intended
            // behavior.
            warnMissingType(encodedMethod.method, method.holder);
          } else if (clazz.isInterface()) {
            if (isNonDesugaredLibraryClass(clazz)) {
              // NOTE: we intentionally don't desugar static calls into static interface
              // methods coming from android.jar since it is only possible in case v24+
              // version of android.jar is provided.
              //
              // We assume such calls are properly guarded by if-checks like
              //    'if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.XYZ) { ... }'
              //
              // WARNING: This may result in incorrect code on older platforms!
              // Retarget call to an appropriate method of companion class.

              if (!options.canLeaveStaticInterfaceMethodInvokes()) {
                // On pre-L devices static calls to interface methods result in verifier
                // rejecting the whole class. We have to create special dispatch classes,
                // so the user class is not rejected because it make this call directly.
                instructions.replaceCurrentInstruction(
                    new InvokeStatic(staticAsMethodOfDispatchClass(method),
                        invokeStatic.outValue(), invokeStatic.arguments()));
                requiredDispatchClasses
                    .computeIfAbsent(clazz.asLibraryClass(), k -> Sets.newConcurrentHashSet())
                    .add(appInfo.definitionFor(encodedMethod.method.holder).asProgramClass());
              }
            } else {
              instructions.replaceCurrentInstruction(
                  new InvokeStatic(staticAsMethodOfCompanionClass(method),
                      invokeStatic.outValue(), invokeStatic.arguments()));
            }
          }
          continue;
        }

        if (instruction.isInvokeSuper()) {
          InvokeSuper invokeSuper = instruction.asInvokeSuper();
          DexMethod invokedMethod = invokeSuper.getInvokedMethod();
          DexClass clazz = appInfo.definitionFor(invokedMethod.holder);
          if (clazz == null) {
            // NOTE: leave unchanged those calls to undefined targets. This may lead to runtime
            // exception but we can not report it as error since it can also be the intended
            // behavior.
            warnMissingType(encodedMethod.method, invokedMethod.holder);
          } else if (clazz.isInterface() && !clazz.isLibraryClass()) {
            // NOTE: we intentionally don't desugar super calls into interface methods
            // coming from android.jar since it is only possible in case v24+ version
            // of android.jar is provided.
            //
            // We assume such calls are properly guarded by if-checks like
            //    'if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.XYZ) { ... }'
            //
            // WARNING: This may result in incorrect code on older platforms!
            // Retarget call to an appropriate method of companion class.
            DexMethod amendedMethod =
                amendDefaultMethod(
                    appInfo.definitionFor(encodedMethod.method.holder), invokedMethod);
            instructions.replaceCurrentInstruction(
                new InvokeStatic(defaultAsMethodOfCompanionClass(amendedMethod),
                    invokeSuper.outValue(), invokeSuper.arguments()));
          } else {
            DexType dexType = maximallySpecificEmulatedInterfaceOrNull(invokedMethod);
            if (dexType != null) {
              // That invoke super may not resolve since the super method may not be present
              // since it's in the emulated interface. We need to force resolution. If it resolves
              // to a library method, then it needs to be rewritten.
              // If it resolves to a program overrides, the invoke-super can remain.
              DexEncodedMethod dexEncodedMethod =
                  appView
                      .appInfo()
                      .lookupSuperTarget(invokeSuper.getInvokedMethod(), code.method.method.holder);
              if (dexEncodedMethod != null) {
                DexClass dexClass = appView.definitionFor(dexEncodedMethod.method.holder);
                if (dexClass != null && dexClass.isLibraryClass()) {
                  // Rewriting is required because the super invoke resolves into a missing
                  // method (method is on desugared library). Find out if it needs to be
                  // retarget or if it just calls a companion class method and rewrite.
                  Map<DexString, Map<DexType, DexType>> retargetCoreLibMember =
                      options.desugaredLibraryConfiguration.getRetargetCoreLibMember();
                  Map<DexType, DexType> typeMap =
                      retargetCoreLibMember.get(dexEncodedMethod.method.name);
                  if (typeMap == null || !typeMap.containsKey(dexEncodedMethod.method.holder)) {
                    DexMethod originalCompanionMethod =
                        instanceAsMethodOfCompanionClass(
                            dexEncodedMethod.method, DEFAULT_METHOD_PREFIX, factory);
                    DexMethod companionMethod =
                        factory.createMethod(
                            getCompanionClassType(dexType),
                            factory.protoWithDifferentFirstParameter(
                                originalCompanionMethod.proto, dexType),
                            originalCompanionMethod.name);
                    instructions.replaceCurrentInstruction(
                        new InvokeStatic(
                            companionMethod, invokeSuper.outValue(), invokeSuper.arguments()));
                  } else {
                    DexMethod retargetMethod =
                        factory.createMethod(
                            typeMap.get(dexEncodedMethod.method.holder),
                            factory.prependTypeToProto(
                                dexEncodedMethod.method.holder, dexEncodedMethod.method.proto),
                            dexEncodedMethod.method.name);
                    instructions.replaceCurrentInstruction(
                        new InvokeStatic(
                            retargetMethod, invokeSuper.outValue(), invokeSuper.arguments()));
                  }
                }
              }
            }
          }
          continue;
        }

        if (instruction.isInvokeDirect()) {
          InvokeDirect invokeDirect = instruction.asInvokeDirect();
          DexMethod method = invokeDirect.getInvokedMethod();
          if (factory.isConstructor(method)) {
            continue;
          }

          DexClass clazz = appInfo.definitionFor(method.holder);
          if (clazz == null) {
            // Report missing class since we don't know if it is an interface.
            warnMissingType(encodedMethod.method, method.holder);

          } else if (clazz.isInterface()) {
            if (clazz.isLibraryClass()) {
              throw new CompilationError("Unexpected call to a private method " +
                  "defined in library class " + clazz.toSourceString(),
                  getMethodOrigin(encodedMethod.method));
            }

            // This might be either private method call, or a call to default
            // interface method made via invoke-direct.
            DexEncodedMethod virtualTarget = null;
            for (DexEncodedMethod candidate : clazz.virtualMethods()) {
              if (candidate.method == method) {
                virtualTarget = candidate;
                break;
              }
            }

            if (virtualTarget != null) {
              // This is a invoke-direct call to a virtual method.
              instructions.replaceCurrentInstruction(
                  new InvokeStatic(defaultAsMethodOfCompanionClass(method),
                      invokeDirect.outValue(), invokeDirect.arguments()));

            } else {
              // Otherwise this must be a private instance method call. Note that the referenced
              // method is expected to be in the current class since it is private, but desugaring
              // may move some methods or their code into other classes.

              instructions.replaceCurrentInstruction(
                  new InvokeStatic(privateAsMethodOfCompanionClass(method),
                      invokeDirect.outValue(), invokeDirect.arguments()));
            }
          }
        }

        if (instruction.isInvokeVirtual() || instruction.isInvokeInterface()) {
          InvokeMethod invokeMethod = instruction.asInvokeMethod();
          DexMethod invokedMethod = invokeMethod.getInvokedMethod();
          DexType dexType = maximallySpecificEmulatedInterfaceOrNull(invokedMethod);
          if (dexType != null) {
            rewriteCurrentInstructionToEmulatedInterfaceCall(
                dexType, invokedMethod, invokeMethod, instructions);
          }
        }
      }
    }
  }

  private DexType maximallySpecificEmulatedInterfaceOrNull(DexMethod invokedMethod) {
    // Here we try to avoid doing the expensive look-up on all invokes.
    if (!emulatedMethods.contains(invokedMethod.name)) {
      return null;
    }
    DexClass dexClass = appView.definitionFor(invokedMethod.holder);
    // We cannot rewrite the invoke we do not know what the class is.
    if (dexClass == null) {
      return null;
    }
    // TODO(b/120884788): Make sure program class are looked up before library class.
    // Since program classes are desugared, no need to rewrite invokes which can target only
    // program types.
    if (!appView.options().isDesugaredLibraryCompilation() && !dexClass.isLibraryClass()) {
      return null;
    }
    // Since desugared library classes are desugared, no need to rewrite invokes which can target
    // only such classes program types.
    if (appView.rewritePrefix.hasRewrittenType(dexClass.type)) {
      return null;
    }
    ResolutionResult resolutionResult =
        appView.appInfo().resolveMaximallySpecificMethods(dexClass, invokedMethod);
    if (!resolutionResult.isSingleResolution()) {
      // At this point we are in a library class. Failures can happen with NoSuchMethod if a
      // library class implement a method with same signature but not related to emulated
      // interfaces.
      return null;
    }
    DexEncodedMethod singleTarget = resolutionResult.getSingleTarget();
    if (!singleTarget.isAbstract() && isEmulatedInterface(singleTarget.method.holder)) {
      return singleTarget.method.holder;
    }
    return null;
  }

  private void rewriteCurrentInstructionToEmulatedInterfaceCall(
      DexType emulatedItf,
      DexMethod invokedMethod,
      InvokeMethod invokeMethod,
      InstructionListIterator instructions) {
    DexEncodedMethod defaultMethod = appView.definitionFor(emulatedItf).lookupMethod(invokedMethod);
    if (defaultMethod != null && !dontRewrite(defaultMethod.method)) {
      assert !defaultMethod.isAbstract();
      instructions.replaceCurrentInstruction(
          new InvokeStatic(
              emulateInterfaceLibraryMethod(invokedMethod, emulatedItf, factory),
              invokeMethod.outValue(),
              invokeMethod.arguments()));
    }
  }

  private boolean isNonDesugaredLibraryClass(DexClass clazz) {
    return clazz.isLibraryClass() && !isInDesugaredLibrary(clazz);
  }

  boolean isInDesugaredLibrary(DexClass clazz) {
    assert clazz.isLibraryClass() || options.isDesugaredLibraryCompilation();
    if (emulatedInterfaces.containsKey(clazz.type)) {
      return true;
    }
    return appView.rewritePrefix.hasRewrittenType(clazz.type);
  }

  boolean dontRewrite(DexMethod method) {
    for (Pair<DexType, DexString> dontRewrite :
        options.desugaredLibraryConfiguration.getDontRewriteInvocation()) {
      if (method.holder == dontRewrite.getFirst() && method.name == dontRewrite.getSecond()) {
        return true;
      }
    }
    return false;
  }

  private void warnMissingEmulatedInterface(DexType interfaceType) {
    StringDiagnostic warning =
        new StringDiagnostic(
            "Cannot emulate interface "
                + interfaceType.getName()
                + " because the interface is missing.");
    options.reporter.warning(warning);
  }

  private void generateEmulateInterfaceLibrary(Builder<?> builder) {
    // Emulated library interfaces should generate the Emulated Library EL dispatch class.
    Map<DexType, List<DexType>> emulatedInterfacesHierarchy = processEmulatedInterfaceHierarchy();
    for (DexType interfaceType : emulatedInterfaces.keySet()) {
      DexClass theInterface = appView.definitionFor(interfaceType);
      if (theInterface == null) {
        warnMissingEmulatedInterface(interfaceType);
      } else if (theInterface.isProgramClass()) {
        DexProgramClass synthesizedClass =
            synthesizeEmulateInterfaceLibraryClass(
                theInterface.asProgramClass(), emulatedInterfacesHierarchy);
        if (synthesizedClass != null) {
          builder.addSynthesizedClass(synthesizedClass, isInMainDexList(interfaceType));
          appView.appInfo().addSynthesizedClass(synthesizedClass);
        }
      }
    }
  }

  private Map<DexType, List<DexType>> processEmulatedInterfaceHierarchy() {
    // TODO(b/134732760): deal with retarget-core-library-member.
    Map<DexType, List<DexType>> emulatedInterfacesHierarchy = new IdentityHashMap<>();
    Set<DexType> processed = Sets.newIdentityHashSet();
    for (DexType interfaceType : emulatedInterfaces.keySet()) {
      processEmulatedInterfaceHierarchy(interfaceType, processed, emulatedInterfacesHierarchy);
    }
    return emulatedInterfacesHierarchy;
  }

  private void processEmulatedInterfaceHierarchy(
      DexType interfaceType,
      Set<DexType> processed,
      Map<DexType, List<DexType>> emulatedInterfacesHierarchy) {
    if (processed.contains(interfaceType)) {
      return;
    }
    emulatedInterfacesHierarchy.put(interfaceType, new ArrayList<>());
    processed.add(interfaceType);
    DexClass theInterface = appView.definitionFor(interfaceType);
    if (theInterface == null) {
      warnMissingEmulatedInterface(interfaceType);
      return;
    }
    LinkedList<DexType> workList = new LinkedList<>(Arrays.asList(theInterface.interfaces.values));
    while (!workList.isEmpty()) {
      DexType next = workList.removeLast();
      if (emulatedInterfaces.containsKey(next)) {
        processEmulatedInterfaceHierarchy(next, processed, emulatedInterfacesHierarchy);
        emulatedInterfacesHierarchy.get(next).add(interfaceType);
        DexClass nextClass = appView.definitionFor(next);
        if (nextClass != null) {
          workList.addAll(Arrays.asList(nextClass.interfaces.values));
        }
      }
    }
  }

  private boolean implementsInterface(DexClass clazz, DexType interfaceType) {
    LinkedList<DexType> workList = new LinkedList<>(Arrays.asList(clazz.interfaces.values));
    while (!workList.isEmpty()) {
      DexType next = workList.removeLast();
      if (interfaceType == next) {
        return true;
      }
      DexClass nextClass = appView.definitionFor(next);
      if (nextClass != null) {
        workList.addAll(Arrays.asList(nextClass.interfaces.values));
      }
    }
    return false;
  }

  private static DexMethod emulateInterfaceLibraryMethod(
      DexMethod method, DexType holder, DexItemFactory factory) {
    return factory.createMethod(
        getEmulateLibraryInterfaceClassType(holder, factory),
        factory.prependTypeToProto(holder, method.proto),
        factory.createString(method.name.toString()));
  }

  private DexProgramClass synthesizeEmulateInterfaceLibraryClass(
      DexProgramClass theInterface, Map<DexType, List<DexType>> emulatedInterfacesHierarchy) {
    List<DexEncodedMethod> emulationMethods = new ArrayList<>();
    for (DexEncodedMethod method : theInterface.methods()) {
      if (method.isDefaultMethod()) {
        DexMethod libraryMethod =
            factory.createMethod(
                emulatedInterfaces.get(theInterface.type), method.method.proto, method.method.name);
        DexMethod originalCompanionMethod =
            method.isStatic()
                ? staticAsMethodOfCompanionClass(method.method)
                : instanceAsMethodOfCompanionClass(method.method, DEFAULT_METHOD_PREFIX, factory);
        DexMethod companionMethod =
            factory.createMethod(
                getCompanionClassType(theInterface.type),
                originalCompanionMethod.proto,
                originalCompanionMethod.name);

        // To properly emulate the library interface call, we need to compute the interfaces
        // inheriting from the interface and manually implement the dispatch with instance of.
        // The list guarantees that an interface is always after interfaces it extends,
        // hence reverse iteration.
        List<DexType> subInterfaces = emulatedInterfacesHierarchy.get(theInterface.type);
        List<Pair<DexType, DexMethod>> extraDispatchCases = new ArrayList<>();
        // In practice, there is usually a single case (except for tests),
        // so we do not bother to make the following loop more clever.
        Map<DexString, Map<DexType, DexType>> retargetCoreLibMember =
            options.desugaredLibraryConfiguration.getRetargetCoreLibMember();
        for (DexString methodName : retargetCoreLibMember.keySet()) {
          if (method.method.name == methodName) {
            for (DexType inType : retargetCoreLibMember.get(methodName).keySet()) {
              DexClass inClass = appView.definitionFor(inType);
              if (inClass != null && implementsInterface(inClass, theInterface.type)) {
                extraDispatchCases.add(
                    new Pair<>(
                        inType,
                        factory.createMethod(
                            retargetCoreLibMember.get(methodName).get(inType),
                            factory.protoWithDifferentFirstParameter(
                                originalCompanionMethod.proto, inType),
                            method.method.name)));
              }
            }
          }
        }
        if (subInterfaces != null) {
          for (int i = subInterfaces.size() - 1; i >= 0; i--) {
            DexClass subInterfaceClass = appView.definitionFor(subInterfaces.get(i));
            assert subInterfaceClass != null; // Else computation of subInterface would have failed.
            // if the method is implemented, extra dispatch is required.
            DexEncodedMethod result = subInterfaceClass.lookupVirtualMethod(method.method);
            if (result != null && !result.isAbstract()) {
              extraDispatchCases.add(
                  new Pair<>(
                      subInterfaceClass.type,
                      factory.createMethod(
                          getCompanionClassType(subInterfaceClass.type),
                          factory.protoWithDifferentFirstParameter(
                              originalCompanionMethod.proto, subInterfaceClass.type),
                          originalCompanionMethod.name)));
            }
          }
        }
        emulationMethods.add(
            DexEncodedMethod.toEmulateDispatchLibraryMethod(
                method.method.holder,
                emulateInterfaceLibraryMethod(method.method, method.method.holder, factory),
                companionMethod,
                libraryMethod,
                extraDispatchCases,
                appView));
      }
    }
    if (emulationMethods.isEmpty()) {
      return null;
    }
    DexType emulateLibraryClassType =
        getEmulateLibraryInterfaceClassType(theInterface.type, factory);
    ClassAccessFlags emulateLibraryClassFlags = theInterface.accessFlags.copy();
    emulateLibraryClassFlags.unsetAbstract();
    emulateLibraryClassFlags.unsetInterface();
    emulateLibraryClassFlags.unsetAnnotation();
    emulateLibraryClassFlags.setFinal();
    emulateLibraryClassFlags.setSynthetic();
    emulateLibraryClassFlags.setPublic();
    synthesizedMethods.addAll(emulationMethods);
    return new DexProgramClass(
        emulateLibraryClassType,
        null,
        new SynthesizedOrigin("interface desugaring (libs)", getClass()),
        emulateLibraryClassFlags,
        factory.objectType,
        DexTypeList.empty(),
        theInterface.sourceFile,
        null,
        Collections.emptyList(),
        null,
        Collections.emptyList(),
        DexAnnotationSet.empty(),
        DexEncodedField.EMPTY_ARRAY,
        DexEncodedField.EMPTY_ARRAY,
        // All synthesized methods are static in this case.
        emulationMethods.toArray(DexEncodedMethod.EMPTY_ARRAY),
        DexEncodedMethod.EMPTY_ARRAY,
        factory.getSkipNameValidationForTesting(),
        DexProgramClass::checksumFromType,
        Collections.singletonList(theInterface));
  }

  private static String getEmulateLibraryInterfaceClassDescriptor(String descriptor) {
    return descriptor.substring(0, descriptor.length() - 1)
        + EMULATE_LIBRARY_CLASS_NAME_SUFFIX
        + ";";
  }

  static DexType getEmulateLibraryInterfaceClassType(DexType type, DexItemFactory factory) {
    assert type.isClassType();
    String descriptor = type.descriptor.toString();
    String elTypeDescriptor = getEmulateLibraryInterfaceClassDescriptor(descriptor);
    return factory.createType(elTypeDescriptor);
  }

  private void reportStaticInterfaceMethodHandle(DexMethod referencedFrom, DexMethodHandle handle) {
    if (handle.type.isInvokeStatic()) {
      DexClass holderClass = appView.definitionFor(handle.asMethod().holder);
      // NOTE: If the class definition is missing we can't check. Let it be handled as any other
      // missing call target.
      if (holderClass == null) {
        warnMissingType(referencedFrom, handle.asMethod().holder);
      } else if (holderClass.isInterface()) {
        throw new Unimplemented(
            "Desugaring of static interface method handle in `"
                + referencedFrom.toSourceString()
                + "` is not yet supported.");
      }
    }
  }

  public static String getCompanionClassDescriptor(String descriptor) {
    return descriptor.substring(0, descriptor.length() - 1) + COMPANION_CLASS_NAME_SUFFIX + ";";
  }

  // Gets the companion class for the interface `type`.
  static DexType getCompanionClassType(DexType type, DexItemFactory factory) {
    assert type.isClassType();
    String descriptor = type.descriptor.toString();
    String ccTypeDescriptor = getCompanionClassDescriptor(descriptor);
    return factory.createType(ccTypeDescriptor);
  }

  DexType getCompanionClassType(DexType type) {
    return getCompanionClassType(type, factory);
  }

  // Gets the forwarding class for the interface `type`.
  final DexType getDispatchClassType(DexType type) {
    assert type.isClassType();
    String descriptor = type.descriptor.toString();
    String dcTypeDescriptor = descriptor.substring(0, descriptor.length() - 1)
        + DISPATCH_CLASS_NAME_SUFFIX + ";";
    return factory.createType(dcTypeDescriptor);
  }

  // Checks if `type` is a companion class.
  public static boolean isCompanionClassType(DexType type) {
    return type.descriptor.toString().endsWith(COMPANION_CLASS_NAME_SUFFIX + ";");
  }

  // Gets the interface class for a companion class `type`.
  private DexType getInterfaceClassType(DexType type) {
    return getInterfaceClassType(type, factory);
  }

  // Gets the interface class for a companion class `type`.
  public static DexType getInterfaceClassType(DexType type, DexItemFactory factory) {
    assert isCompanionClassType(type);
    String descriptor = type.descriptor.toString();
    String interfaceTypeDescriptor = descriptor.substring(0,
        descriptor.length() - 1 - COMPANION_CLASS_NAME_SUFFIX.length()) + ";";
    return factory.createType(interfaceTypeDescriptor);
  }

  private boolean isInMainDexList(DexType iface) {
    return appView.appInfo().isInMainDexList(iface);
  }

  // Represent a static interface method as a method of companion class.
  final DexMethod staticAsMethodOfCompanionClass(DexMethod method) {
    // No changes for static methods.
    return factory.createMethod(getCompanionClassType(method.holder), method.proto, method.name);
  }

  // Represent a static interface method as a method of dispatch class.
  final DexMethod staticAsMethodOfDispatchClass(DexMethod method) {
    return factory.createMethod(getDispatchClassType(method.holder), method.proto, method.name);
  }

  // Checks if the type ends with dispatch class suffix.
  public static boolean hasDispatchClassSuffix(DexType clazz) {
    return clazz.getName().endsWith(DISPATCH_CLASS_NAME_SUFFIX);
  }

  private static DexMethod instanceAsMethodOfCompanionClass(
      DexMethod method, String prefix, DexItemFactory factory) {
    // Add an implicit argument to represent the receiver.
    DexType[] params = method.proto.parameters.values;
    DexType[] newParams = new DexType[params.length + 1];
    newParams[0] = method.holder;
    System.arraycopy(params, 0, newParams, 1, params.length);

    // Add prefix to avoid name conflicts.
    return factory.createMethod(
        getCompanionClassType(method.holder, factory),
        factory.createProto(method.proto.returnType, newParams),
        factory.createString(prefix + method.name.toString()));
  }

  // It is possible that referenced method actually points to an interface which does
  // not define this default methods, but inherits it. We are making our best effort
  // to find an appropriate method, but still use the original one in case we fail.
  private DexMethod amendDefaultMethod(DexClass classToDesugar, DexMethod method) {
    DexMethod singleCandidate = getOrCreateInterfaceInfo(
        classToDesugar, classToDesugar, method.holder).getSingleCandidate(method);
    return singleCandidate != null ? singleCandidate : method;
  }

  // Represent a default interface method as a method of companion class.
  public static DexMethod defaultAsMethodOfCompanionClass(
      DexMethod method, DexItemFactory factory) {
    return instanceAsMethodOfCompanionClass(method, DEFAULT_METHOD_PREFIX, factory);
  }

  DexMethod defaultAsMethodOfCompanionClass(DexMethod method) {
    return defaultAsMethodOfCompanionClass(method, factory);
  }

  // Represent a private instance interface method as a method of companion class.
  static DexMethod privateAsMethodOfCompanionClass(DexMethod method, DexItemFactory factory) {
    // Add an implicit argument to represent the receiver.
    return instanceAsMethodOfCompanionClass(method, PRIVATE_METHOD_PREFIX, factory);
  }

  DexMethod privateAsMethodOfCompanionClass(DexMethod method) {
    return privateAsMethodOfCompanionClass(method, factory);
  }

  private void renameEmulatedInterfaces() {
    // Rename manually the emulated interfaces, we do not use the PrefixRenamingLens
    // because both the normal and emulated interfaces are used.
    for (DexClass clazz : appView.appInfo().classes()) {
      if (clazz.isInterface()) {
        DexType newType = inferEmulatedInterfaceName(clazz);
        if (newType != null && !appView.rewritePrefix.hasRewrittenType(clazz.type)) {
          // We do not rewrite if it is already going to be rewritten using the a rewritingPrefix.
          addRewritePrefix(clazz.type, newType.toString());
          renameEmulatedInterfaces(clazz, newType);
        }
      }
    }
  }

  private DexType inferEmulatedInterfaceName(DexClass subInterface) {
    DexType newType = emulatedInterfaces.get(subInterface.type);
    if (newType != null) {
      return newType;
    }
    LinkedList<DexType> workList = new LinkedList<>(Arrays.asList(subInterface.interfaces.values));
    while (!workList.isEmpty()) {
      DexType next = workList.removeFirst();
      if (emulatedInterfaces.get(next) != null) {
        return inferEmulatedInterfaceName(subInterface.type, next);
      }
      DexClass nextClass = appView.definitionFor(next);
      if (nextClass != null) {
        workList.addAll(Arrays.asList(nextClass.interfaces.values));
      }
    }
    return null;
  }

  private DexType inferEmulatedInterfaceName(DexType subInterfaceType, DexType interfaceType) {
    String initialPrefix = interfaceType.getPackageName();
    String rewrittenPrefix = emulatedInterfaces.get(interfaceType).getPackageName();
    String suffix = subInterfaceType.toString().substring(initialPrefix.length());
    return factory.createType(DescriptorUtils.javaTypeToDescriptor(rewrittenPrefix + suffix));
  }

  private void renameEmulatedInterfaces(DexClass theInterface, DexType renamedInterface) {
    theInterface.type = renamedInterface;
    theInterface.setVirtualMethods(renameHolder(theInterface.virtualMethods(), renamedInterface));
    theInterface.setDirectMethods(renameHolder(theInterface.directMethods(), renamedInterface));
  }

  private DexEncodedMethod[] renameHolder(List<DexEncodedMethod> methods, DexType newName) {
    DexEncodedMethod[] newMethods = new DexEncodedMethod[methods.size()];
    for (int i = 0; i < newMethods.length; i++) {
      newMethods[i] = methods.get(i).toRenamedHolderMethod(newName, factory);
    }
    return newMethods;
  }

  private void duplicateEmulatedInterfaces() {
    // All classes implementing an emulated interface now implements the interface and the
    // emulated one, as well as hidden overrides, for correct emulated dispatch.
    for (DexClass clazz : appView.appInfo().classes()) {
      if (clazz.type == appView.dexItemFactory().objectType) {
        continue;
      }
      List<DexType> extraInterfaces = new ArrayList<>();
      for (DexType type : clazz.interfaces.values) {
        if (emulatedInterfaces.containsKey(type)) {
          extraInterfaces.add(emulatedInterfaces.get(type));
        }
      }
      if (!appView.options().isDesugaredLibraryCompilation()) {
        assert clazz.superType != null;
        DexClass superClazz = appView.definitionFor(clazz.superType);
        if (superClazz != null && superClazz.isLibraryClass()) {
          List<DexType> itfs = emulatedInterfacesOf(superClazz);
          for (DexType itf : itfs) {
            extraInterfaces.add(emulatedInterfaces.get(itf));
          }
        }
        // Remove duplicates.
        extraInterfaces = new ArrayList<>(new LinkedHashSet<>(extraInterfaces));
      }
      if (!extraInterfaces.isEmpty()) {
        DexType[] newInterfaces =
            Arrays.copyOf(
                clazz.interfaces.values, clazz.interfaces.size() + extraInterfaces.size());
        for (int i = clazz.interfaces.size(); i < newInterfaces.length; i++) {
          newInterfaces[i] = extraInterfaces.get(i - clazz.interfaces.size());
        }
        clazz.interfaces = new DexTypeList(newInterfaces);
      }
    }
  }

  private List<DexType> emulatedInterfacesOf(DexClass superClazz) {
    if (superClazz.type == factory.objectType) {
      return Collections.emptyList();
    }
    ArrayList<DexType> itfs = new ArrayList<>();
    LinkedList<DexType> workList = new LinkedList<>();
    workList.add(superClazz.type);
    while (!workList.isEmpty()) {
      DexType dexType = workList.removeFirst();
      DexClass dexClass = appView.definitionFor(dexType);
      if (dexClass != null) {
        if (dexClass.superType != factory.objectType) {
          workList.add(dexClass.superType);
        }
        for (DexType itf : dexClass.interfaces.values) {
          if (emulatedInterfaces.containsKey(itf)) {
            itfs.add(itf);
          } else {
            workList.add(itf);
          }
        }
      }
    }
    return itfs;
  }

  /**
   * Move static and default interface methods to companion classes, add missing methods to forward
   * to moved default methods implementation.
   */
  public void desugarInterfaceMethods(
      Builder<?> builder,
      Flavor flavour,
      ExecutorService executorService)
      throws ExecutionException {
    if (appView.options().isDesugaredLibraryCompilation()) {
      generateEmulateInterfaceLibrary(builder);
    }
    duplicateEmulatedInterfaces();

    // Process all classes first. Add missing forwarding methods to
    // replace desugared default interface methods.
    processClasses(builder, flavour, synthesizedMethods::add);

    // Process interfaces, create companion or dispatch class if needed, move static
    // methods to companion class, copy default interface methods to companion classes,
    // make original default methods abstract, remove bridge methods, create dispatch
    // classes if needed.
    AppInfo appInfo = appView.appInfo();
    for (Entry<DexType, DexProgramClass> entry : processInterfaces(builder, flavour).entrySet()) {
      // Don't need to optimize synthesized class since all of its methods
      // are just moved from interfaces and don't need to be re-processed.
      DexProgramClass synthesizedClass = entry.getValue();
      builder.addSynthesizedClass(synthesizedClass, isInMainDexList(entry.getKey()));
      appInfo.addSynthesizedClass(synthesizedClass);
    }

    if (appView.options().isDesugaredLibraryCompilation()) {
      renameEmulatedInterfaces();
    }

    converter.processMethodsConcurrently(synthesizedMethods, executorService);

    // Cached data is not needed any more.
    clear();
  }

  private void clear() {
    this.cache.clear();
    this.synthesizedMethods.clear();
    this.requiredDispatchClasses.clear();
  }

  private static boolean shouldProcess(
      DexProgramClass clazz, Flavor flavour, boolean mustBeInterface) {
    return (!clazz.originatesFromDexResource() || flavour == Flavor.IncludeAllResources)
        && clazz.isInterface() == mustBeInterface;
  }

  private Map<DexType, DexProgramClass> processInterfaces(Builder<?> builder, Flavor flavour) {
    NestedGraphLense.Builder graphLensBuilder = GraphLense.builder();
    InterfaceProcessor processor = new InterfaceProcessor(appView, this);
    for (DexProgramClass clazz : builder.getProgramClasses()) {
      if (shouldProcess(clazz, flavour, true)) {
        processor.process(clazz.asProgramClass(), graphLensBuilder);
      }
    }
    for (Entry<DexLibraryClass, Set<DexProgramClass>> entry : requiredDispatchClasses.entrySet()) {
      synthesizedMethods.addAll(processor.process(entry.getKey(), entry.getValue()));
    }
    if (appView.enableWholeProgramOptimizations()) {
      appView.setGraphLense(graphLensBuilder.build(appView.dexItemFactory(), appView.graphLense()));
    }
    return processor.syntheticClasses;
  }

  private void processClasses(
      Builder<?> builder, Flavor flavour, Consumer<DexEncodedMethod> newSynthesizedMethodConsumer) {
    ClassProcessor processor = new ClassProcessor(appView, this, newSynthesizedMethodConsumer);
    // First we compute all desugaring *without* introducing forwarding methods.
    for (DexProgramClass clazz : builder.getProgramClasses()) {
      if (shouldProcess(clazz, flavour, false)) {
        processor.processClass(clazz);
      }
    }
    // Then we introduce forwarding methods.
    processor.addSyntheticMethods();
  }

  final boolean isDefaultMethod(DexEncodedMethod method) {
    assert !method.accessFlags.isConstructor();
    assert !method.accessFlags.isStatic();

    if (method.accessFlags.isAbstract()) {
      return false;
    }
    if (method.accessFlags.isNative()) {
      throw new Unimplemented("Native default interface methods are not yet supported.");
    }
    if (!method.accessFlags.isPublic()) {
      // NOTE: even though the class is allowed to have non-public interface methods
      // with code, for example private methods, all such methods we are aware of are
      // created by the compiler for stateful lambdas and they must be converted into
      // static methods by lambda desugaring by this time.
      throw new Unimplemented("Non public default interface methods are not yet supported.");
    }
    return true;
  }

  public static boolean isEmulatedInterfaceDispatch(AppView<?> appView, DexEncodedMethod method) {
    // Answers true if this method is already managed through emulated interface dispatch.
    Map<DexType, DexType> emulateLibraryInterface =
        appView.options().desugaredLibraryConfiguration.getEmulateLibraryInterface();
    if (emulateLibraryInterface.isEmpty()) {
      return false;
    }
    DexMethod methodToFind = method.method;

    // Look-up all superclass and interfaces, if an emulated interface is found, and it implements
    // the method, answers true.
    LinkedList<DexType> workList = new LinkedList<>();
    workList.add(methodToFind.holder);
    while (!workList.isEmpty()) {
      DexType dexType = workList.removeFirst();
      DexClass dexClass = appView.definitionFor(dexType);
      assert dexClass != null; // It is a library class, or we are doing L8 compilation.
      if (dexClass.isInterface() && emulateLibraryInterface.containsKey(dexType)) {
        DexEncodedMethod dexEncodedMethod = dexClass.lookupMethod(methodToFind);
        if (dexEncodedMethod != null) {
          return true;
        }
      }
      Collections.addAll(workList, dexClass.interfaces.values);
      if (dexClass.superType != appView.dexItemFactory().objectType) {
        workList.add(dexClass.superType);
      }
    }
    return false;
  }

  public void warnMissingInterface(
      DexClass classToDesugar, DexClass implementing, DexType missing) {
    // We use contains() on non hashed collection, but we know it's a 8 cases collection.
    // j$ interfaces won't be missing, they are in the desugared library.
    if (!emulatedInterfaces.values().contains(missing)) {
      options.warningMissingInterfaceForDesugar(classToDesugar, implementing, missing);
    }
  }

  private void warnMissingType(DexMethod referencedFrom, DexType missing) {
    // Companion/Emulated interface/Conversion classes for desugared library won't be missing,
    // they are in the desugared library.
    if (appView.rewritePrefix.hasRewrittenType(missing)
        || DesugaredLibraryWrapperSynthesizer.isSynthesizedWrapper(missing)
        || appView
            .options()
            .desugaredLibraryConfiguration
            .getCustomConversions()
            .values()
            .contains(missing)) {
      return;
    }
    DexMethod method = appView.graphLense().getOriginalMethodSignature(referencedFrom);
    Origin origin = getMethodOrigin(method);
    MethodPosition position = new MethodPosition(method);
    options.warningMissingTypeForDesugar(origin, position, missing, method.holder);
  }

  private Origin getMethodOrigin(DexMethod method) {
    DexType holder = method.holder;
    if (isCompanionClassType(holder)) {
      holder = getInterfaceClassType(holder);
    }
    DexClass clazz = appView.definitionFor(holder);
    return clazz == null ? Origin.unknown() : clazz.getOrigin();
  }

  final DefaultMethodsHelper.Collection getOrCreateInterfaceInfo(
      DexClass classToDesugar,
      DexClass implementing,
      DexType iface) {
    DefaultMethodsHelper.Collection collection = cache.get(iface);
    if (collection != null) {
      return collection;
    }
    collection = createInterfaceInfo(classToDesugar, implementing, iface);
    Collection existing = cache.putIfAbsent(iface, collection);
    return existing != null ? existing : collection;
  }

  private DefaultMethodsHelper.Collection createInterfaceInfo(
      DexClass classToDesugar,
      DexClass implementing,
      DexType iface) {
    DefaultMethodsHelper helper = new DefaultMethodsHelper();
    DexClass definedInterface = appView.definitionFor(iface);
    if (definedInterface == null) {
      warnMissingInterface(classToDesugar, implementing, iface);
      return helper.wrapInCollection();
    }
    if (!definedInterface.isInterface()) {
      throw new CompilationError(
          "Type " + iface.toSourceString() + " is referenced as an interface from `"
              + implementing.toString() + "`.");
    }

    if (isNonDesugaredLibraryClass(definedInterface)) {
      // NOTE: We intentionally ignore all candidates coming from android.jar
      // since it is only possible in case v24+ version of android.jar is provided.
      // WARNING: This may result in incorrect code if something else than Android bootclasspath
      // classes are given as libraries!
      return helper.wrapInCollection();
    }

    // At this point we likely have a non-library type that may depend on default method information
    // from its interfaces and the dependency should be reported.
    if (!definedInterface.isLibraryClass()) {
      reportDependencyEdge(definedInterface, implementing, appView);
    }

    // Merge information from all superinterfaces.
    for (DexType superinterface : definedInterface.interfaces.values) {
      helper.merge(getOrCreateInterfaceInfo(classToDesugar, definedInterface, superinterface));
    }

    // Hide by virtual methods of this interface.
    for (DexEncodedMethod virtual : definedInterface.virtualMethods()) {
      helper.hideMatches(virtual.method);
    }

    // Add all default methods of this interface.
    for (DexEncodedMethod encoded : definedInterface.virtualMethods()) {
      if (isDefaultMethod(encoded)) {
        helper.addDefaultMethod(encoded);
      }
    }

    return helper.wrapInCollection();
  }

  public static void reportDependencyEdge(
      DexClass dependency, DexClass dependent, AppView<?> appView) {
    DesugarGraphConsumer consumer = appView.options().desugarGraphConsumer;
    if (consumer != null) {
      Origin dependencyOrigin = dependency.getOrigin();
      Origin dependentOrigin = dependent.getOrigin();
      if (dependencyOrigin != dependentOrigin) {
        consumer.accept(dependencyOrigin, dependentOrigin);
      }
    }
  }
}
