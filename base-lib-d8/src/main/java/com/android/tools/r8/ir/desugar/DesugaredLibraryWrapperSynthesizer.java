// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.CfCode;
import com.android.tools.r8.graph.ClassAccessFlags;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProgramClass.ChecksumSupplier;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.FieldAccessFlags;
import com.android.tools.r8.graph.MethodAccessFlags;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.synthetic.DesugaredLibraryAPIConversionCfCodeProvider.APIConverterConstructorCfCodeProvider;
import com.android.tools.r8.ir.synthetic.DesugaredLibraryAPIConversionCfCodeProvider.APIConverterThrowRuntimeExceptionCfCodeProvider;
import com.android.tools.r8.ir.synthetic.DesugaredLibraryAPIConversionCfCodeProvider.APIConverterVivifiedWrapperCfCodeProvider;
import com.android.tools.r8.ir.synthetic.DesugaredLibraryAPIConversionCfCodeProvider.APIConverterWrapperCfCodeProvider;
import com.android.tools.r8.ir.synthetic.DesugaredLibraryAPIConversionCfCodeProvider.APIConverterWrapperConversionCfCodeProvider;
import com.android.tools.r8.origin.SynthesizedOrigin;
import com.android.tools.r8.utils.Box;
import com.android.tools.r8.utils.Pair;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

// I am responsible for the generation of wrappers used to call library APIs when desugaring
// libraries. Wrappers can be both ways, wrapping the desugarType as a type, or the type as
// a desugar type.
// This file use the vivifiedType -> type, type -> desugarType convention described in the
// DesugaredLibraryAPIConverter class.
// Wrappers contain the following:
// - a single static method convert, which is used by the DesugaredLibraryAPIConverter for
// conversion, it's the main public API (public).
// - a constructor setting the wrappedValue (private).
// - a getter for the wrappedValue (public unwrap()).
// - a single instance field holding the wrapped value (private final).
// - a copy of all implemented methods in the class/interface wrapped. Such methods only do type
// conversions and forward the call to the wrapped type. Parameters and return types are also
// converted.
// Generation of the conversion method in the wrappers is postponed until the compiler knows if the
// reversed wrapper is needed.

// Example of the type wrapper ($-WRP) of java.util.BiFunction at the end of the compilation. I
// omitted
// generic values for simplicity and wrote .... instead of .util.function. Note the difference
// between $-WRP and $-V-WRP wrappers:
// public class j$....BiFunction$-WRP implements java....BiFunction {
//   private final j$....BiFunction wrappedValue;
//   private BiFunction (j$....BiFunction wrappedValue) {
//     this.wrappedValue = wrappedValue;
//   }
//   public R apply(T t, U u) {
//     return wrappedValue.apply(t, u);
//   }
//   public BiFunction andThen(java....Function after) {
//     j$....BiFunction afterConverted = j$....BiFunction$-V-WRP.convert(after);
//     return wrappedValue.andThen(afterConverted);
//   }
//   public static convert(j$....BiFunction function){
//     if (function == null) {
//       return null;
//     }
//     if (function instanceof j$....BiFunction$-V-WRP) {
//       return ((j$....BiFunction$-V-WRP) function).wrappedValue;
//     }
//     return new j$....BiFunction$-WRP(wrappedValue);
//     }
//   }
public class DesugaredLibraryWrapperSynthesizer {

  public static final String WRAPPER_PREFIX = "$r8$wrapper$";
  public static final String TYPE_WRAPPER_SUFFIX = "$-WRP";
  public static final String VIVIFIED_TYPE_WRAPPER_SUFFIX = "$-V-WRP";

  private final AppView<?> appView;
  private final Map<DexType, Pair<DexType, DexProgramClass>> typeWrappers =
      new ConcurrentHashMap<>();
  private final Map<DexType, Pair<DexType, DexProgramClass>> vivifiedTypeWrappers =
      new ConcurrentHashMap<>();
  // The invalidWrappers are wrappers with incorrect behavior because of final methods that could
  // not be overridden. Such wrappers are awful because the runtime behavior is undefined and does
  // not raise explicit errors. So we register them here and conversion methods for such wrappers
  // raise a runtime exception instead of generating the wrapper.
  private final Set<DexType> invalidWrappers = Sets.newConcurrentHashSet();
  private final Set<DexType> generatedWrappers = Sets.newConcurrentHashSet();
  private final DexItemFactory factory;
  private final DesugaredLibraryAPIConverter converter;

  DesugaredLibraryWrapperSynthesizer(AppView<?> appView, DesugaredLibraryAPIConverter converter) {
    this.appView = appView;
    this.factory = appView.dexItemFactory();
    this.converter = converter;
  }

  public static boolean isSynthesizedWrapper(DexType clazz) {
    return clazz.descriptor.toString().contains(WRAPPER_PREFIX);
  }

  boolean hasSynthesized(DexType type) {
    return generatedWrappers.contains(type);
  }

  // Wrapper initial generation section.
  // 1. Generate wrappers without conversion methods.
  // 2. Compute wrapper types.

  boolean canGenerateWrapper(DexType type) {
    DexClass dexClass = appView.definitionFor(type);
    if (dexClass == null) {
      return false;
    }
    return dexClass.isLibraryClass() || appView.options().isDesugaredLibraryCompilation();
  }

  DexType getTypeWrapper(DexType type) {
    return getWrapper(type, TYPE_WRAPPER_SUFFIX, typeWrappers, this::generateTypeWrapper);
  }

  DexType getVivifiedTypeWrapper(DexType type) {
    return getWrapper(
        type,
        VIVIFIED_TYPE_WRAPPER_SUFFIX,
        vivifiedTypeWrappers,
        this::generateVivifiedTypeWrapper);
  }

  private DexType createWrapperType(DexType type, String suffix) {
    String desugaredLibPrefix =
        appView
            .options()
            .desugaredLibraryConfiguration
            .getSynthesizedLibraryClassesPackagePrefix(appView);
    return factory.createType(
        "L"
            + desugaredLibPrefix
            + WRAPPER_PREFIX
            + type.toString().replace('.', '$')
            + suffix
            + ";");
  }

  private DexType getWrapper(
      DexType type,
      String suffix,
      Map<DexType, Pair<DexType, DexProgramClass>> wrappers,
      BiFunction<DexClass, DexType, DexProgramClass> wrapperGenerator) {
    // Answers the DexType of the wrapper. Generate the wrapper DexProgramClass if not already done,
    // except the conversions methods. Conversion method generation is postponed to know if the
    // reverse wrapper is present at generation time.
    // We generate the type while locking the concurrent hash map, but we release the lock before
    // generating the actual class to avoid locking for too long (hence the Pair).
    assert !type.toString().startsWith(DesugaredLibraryAPIConverter.VIVIFIED_PREFIX);
    Box<Boolean> toGenerate = new Box<>(false);
    Pair<DexType, DexProgramClass> pair =
        wrappers.computeIfAbsent(
            type,
            t -> {
              toGenerate.set(true);
              DexType wrapperType = createWrapperType(type, suffix);
              generatedWrappers.add(wrapperType);
              return new Pair<>(wrapperType, null);
            });
    if (toGenerate.get()) {
      assert pair.getSecond() == null;
      DexClass dexClass = appView.definitionFor(type);
      // The dexClass should be a library class, so it cannot be null.
      assert dexClass != null
          && (dexClass.isLibraryClass() || appView.options().isDesugaredLibraryCompilation());
      if (dexClass.accessFlags.isFinal()) {
        throw appView
            .options()
            .reporter
            .fatalError(
                new StringDiagnostic(
                    "Cannot generate a wrapper for final class "
                        + dexClass.type
                        + ". Add a custom conversion in the desugared library."));
      }
      pair.setSecond(wrapperGenerator.apply(dexClass, pair.getFirst()));
    }
    return pair.getFirst();
  }

  private DexType vivifiedTypeFor(DexType type) {
    return DesugaredLibraryAPIConverter.vivifiedTypeFor(type, appView);
  }

  private DexProgramClass generateTypeWrapper(DexClass dexClass, DexType typeWrapperType) {
    DexType type = dexClass.type;
    DexEncodedField wrapperField = synthesizeWrappedValueField(typeWrapperType, type);
    return synthesizeWrapper(
        vivifiedTypeFor(type),
        dexClass,
        synthesizeVirtualMethodsForTypeWrapper(dexClass, wrapperField),
        wrapperField);
  }

  private DexProgramClass generateVivifiedTypeWrapper(
      DexClass dexClass, DexType vivifiedTypeWrapperType) {
    DexType type = dexClass.type;
    DexEncodedField wrapperField =
        synthesizeWrappedValueField(vivifiedTypeWrapperType, vivifiedTypeFor(type));
    return synthesizeWrapper(
        type,
        dexClass,
        synthesizeVirtualMethodsForVivifiedTypeWrapper(dexClass, wrapperField),
        wrapperField);
  }

  private DexProgramClass synthesizeWrapper(
      DexType wrappingType,
      DexClass clazz,
      DexEncodedMethod[] virtualMethods,
      DexEncodedField wrapperField) {
    boolean isItf = clazz.isInterface();
    DexType superType = isItf ? factory.objectType : wrappingType;
    DexTypeList interfaces =
        isItf ? new DexTypeList(new DexType[] {wrappingType}) : DexTypeList.empty();
    return new DexProgramClass(
        wrapperField.field.holder,
        null,
        new SynthesizedOrigin("Desugared library API Converter", getClass()),
        ClassAccessFlags.fromSharedAccessFlags(
            Constants.ACC_FINAL | Constants.ACC_SYNTHETIC | Constants.ACC_PUBLIC),
        superType,
        interfaces,
        clazz.sourceFile,
        null,
        Collections.emptyList(),
        null,
        Collections.emptyList(),
        DexAnnotationSet.empty(),
        DexEncodedField.EMPTY_ARRAY, // No static fields.
        new DexEncodedField[] {wrapperField},
        new DexEncodedMethod[] {
          synthesizeConstructor(wrapperField.field)
        }, // Conversions methods will be added later.
        virtualMethods,
        factory.getSkipNameValidationForTesting(),
        getChecksumSupplier(this, clazz.type),
        Collections.emptyList());
  }

  private ChecksumSupplier getChecksumSupplier(
      DesugaredLibraryWrapperSynthesizer synthesizer, DexType keyType) {
    return clazz -> {
      // The synthesized type wrappers are constructed lazily, so their lookup must be delayed
      // until the point the checksum is requested (at write time). The presence of a wrapper
      // affects the implementation of the conversion functions, so they must be accounted for in
      // the checksum.
      boolean hasWrapper = synthesizer.typeWrappers.containsKey(keyType);
      boolean hasViviWrapper = synthesizer.vivifiedTypeWrappers.containsKey(keyType);
      return ((long) clazz.type.hashCode())
          + 7 * (long) Boolean.hashCode(hasWrapper)
          + 11 * (long) Boolean.hashCode(hasViviWrapper);
    };
  }

  private DexEncodedMethod[] synthesizeVirtualMethodsForVivifiedTypeWrapper(
      DexClass dexClass, DexEncodedField wrapperField) {
    List<DexEncodedMethod> dexMethods = allImplementedMethods(dexClass);
    List<DexEncodedMethod> generatedMethods = new ArrayList<>();
    // Each method should use only types in their signature, but each method the wrapper forwards
    // to should used only vivified types.
    // Generated method looks like:
    // long foo (type, int)
    //   v0 <- arg0;
    //   v1 <- arg1;
    //   v2 <- convertTypeToVivifiedType(v0);
    //   v3 <- wrappedValue.foo(v2,v1);
    //   return v3;
    Set<DexMethod> finalMethods = Sets.newIdentityHashSet();
    for (DexEncodedMethod dexEncodedMethod : dexMethods) {
      DexClass holderClass = appView.definitionFor(dexEncodedMethod.method.holder);
      assert holderClass != null;
      DexMethod methodToInstall =
          factory.createMethod(
              wrapperField.field.holder,
              dexEncodedMethod.method.proto,
              dexEncodedMethod.method.name);
      CfCode cfCode;
      if (dexEncodedMethod.isFinal()) {
        invalidWrappers.add(wrapperField.field.holder);
        finalMethods.add(dexEncodedMethod.method);
        continue;
      } else {
        cfCode =
            new APIConverterVivifiedWrapperCfCodeProvider(
                    appView,
                    methodToInstall,
                    wrapperField.field,
                    converter,
                    holderClass.isInterface())
                .generateCfCode();
      }
      DexEncodedMethod newDexEncodedMethod =
          newSynthesizedMethod(methodToInstall, dexEncodedMethod, cfCode);
      generatedMethods.add(newDexEncodedMethod);
    }
    return finalizeWrapperMethods(generatedMethods, finalMethods);
  }

  private DexEncodedMethod[] synthesizeVirtualMethodsForTypeWrapper(
      DexClass dexClass, DexEncodedField wrapperField) {
    List<DexEncodedMethod> dexMethods = allImplementedMethods(dexClass);
    List<DexEncodedMethod> generatedMethods = new ArrayList<>();
    // Each method should use only vivified types in their signature, but each method the wrapper
    // forwards
    // to should used only types.
    // Generated method looks like:
    // long foo (type, int)
    //   v0 <- arg0;
    //   v1 <- arg1;
    //   v2 <- convertVivifiedTypeToType(v0);
    //   v3 <- wrappedValue.foo(v2,v1);
    //   return v3;
    Set<DexMethod> finalMethods = Sets.newIdentityHashSet();
    for (DexEncodedMethod dexEncodedMethod : dexMethods) {
      DexClass holderClass = appView.definitionFor(dexEncodedMethod.method.holder);
      assert holderClass != null || appView.options().isDesugaredLibraryCompilation();
      boolean isInterface = holderClass == null || holderClass.isInterface();
      DexMethod methodToInstall =
          DesugaredLibraryAPIConverter.methodWithVivifiedTypeInSignature(
              dexEncodedMethod.method, wrapperField.field.holder, appView);
      CfCode cfCode;
      if (dexEncodedMethod.isFinal()) {
        invalidWrappers.add(wrapperField.field.holder);
        finalMethods.add(dexEncodedMethod.method);
        continue;
      } else {
        cfCode =
            new APIConverterWrapperCfCodeProvider(
                    appView, dexEncodedMethod.method, wrapperField.field, converter, isInterface)
                .generateCfCode();
      }
      DexEncodedMethod newDexEncodedMethod =
          newSynthesizedMethod(methodToInstall, dexEncodedMethod, cfCode);
      generatedMethods.add(newDexEncodedMethod);
    }
    return finalizeWrapperMethods(generatedMethods, finalMethods);
  }

  private DexEncodedMethod[] finalizeWrapperMethods(
      List<DexEncodedMethod> generatedMethods, Set<DexMethod> finalMethods) {
    if (finalMethods.isEmpty()) {
      return generatedMethods.toArray(DexEncodedMethod.EMPTY_ARRAY);
    }
    // Wrapper is invalid, no need to add the virtual methods.
    reportFinalMethodsInWrapper(finalMethods);
    return DexEncodedMethod.EMPTY_ARRAY;
  }

  private void reportFinalMethodsInWrapper(Set<DexMethod> methods) {
    String[] methodArray =
        methods.stream().map(method -> method.holder + "#" + method.name).toArray(String[]::new);
    appView
        .options()
        .reporter
        .warning(
            new StringDiagnostic(
                "Desugared library API conversion: cannot wrap final methods "
                    + Arrays.toString(methodArray)
                    + ". "
                    + methods.iterator().next().holder
                    + " is marked as invalid and will throw a runtime exception upon conversion."));
  }

  DexEncodedMethod newSynthesizedMethod(
      DexMethod methodToInstall, DexEncodedMethod template, Code code) {
    MethodAccessFlags newFlags = template.accessFlags.copy();
    assert newFlags.isPublic();
    newFlags.unsetAbstract();
    newFlags.setSynthetic();
    return new DexEncodedMethod(
        methodToInstall,
        newFlags,
        DexAnnotationSet.empty(),
        ParameterAnnotationsList.empty(),
        code);
  }

  private List<DexEncodedMethod> allImplementedMethods(DexClass libraryClass) {
    LinkedList<DexClass> workList = new LinkedList<>();
    List<DexEncodedMethod> implementedMethods = new ArrayList<>();
    workList.add(libraryClass);
    while (!workList.isEmpty()) {
      DexClass dexClass = workList.removeFirst();
      for (DexEncodedMethod virtualMethod : dexClass.virtualMethods()) {
        if (!virtualMethod.isPrivateMethod()) {
          boolean alreadyAdded = false;
          // This looks quadratic but given the size of the collections met in practice for
          // desugared libraries (Max ~15) it does not matter.
          for (DexEncodedMethod alreadyImplementedMethod : implementedMethods) {
            if (alreadyImplementedMethod.method.match(virtualMethod.method)) {
              alreadyAdded = true;
              continue;
            }
          }
          if (!alreadyAdded) {
            implementedMethods.add(virtualMethod);
          }
        }
      }
      for (DexType itf : dexClass.interfaces.values) {
        DexClass itfClass = appView.definitionFor(itf);
        // Cannot be null in program since we started from a LibraryClass.
        assert itfClass != null || appView.options().isDesugaredLibraryCompilation();
        if (itfClass != null) {
          workList.add(itfClass);
        }
      }
      if (dexClass.superType != factory.objectType) {
        DexClass superClass = appView.definitionFor(dexClass.superType);
        assert superClass != null; // Cannot be null since we started from a LibraryClass.
        workList.add(superClass);
      }
    }
    return implementedMethods;
  }

  private DexEncodedField synthesizeWrappedValueField(DexType holder, DexType fieldType) {
    DexField field = factory.createField(holder, fieldType, factory.wrapperFieldName);
    // Field is package private to be accessible from convert methods without a getter.
    FieldAccessFlags fieldAccessFlags =
        FieldAccessFlags.fromCfAccessFlags(Constants.ACC_FINAL | Constants.ACC_SYNTHETIC);
    return new DexEncodedField(field, fieldAccessFlags, DexAnnotationSet.empty(), null);
  }

  private DexEncodedMethod synthesizeConstructor(DexField field) {
    DexMethod method =
        factory.createMethod(
            field.holder,
            factory.createProto(factory.voidType, field.type),
            factory.initMethodName);
    return newSynthesizedMethod(
        method,
        Constants.ACC_PRIVATE | Constants.ACC_SYNTHETIC,
        true,
        new APIConverterConstructorCfCodeProvider(appView, field).generateCfCode());
  }

  private DexEncodedMethod newSynthesizedMethod(
      DexMethod methodToInstall, int flags, boolean constructor, Code code) {
    MethodAccessFlags accessFlags = MethodAccessFlags.fromSharedAccessFlags(flags, constructor);
    return new DexEncodedMethod(
        methodToInstall,
        accessFlags,
        DexAnnotationSet.empty(),
        ParameterAnnotationsList.empty(),
        code);
  }

  // Wrapper finalization section.
  // 1. Generate conversions methods (convert(type)).
  // 2. Add the synthesized classes.
  // 3. Process all methods.

  void finalizeWrappers(
      DexApplication.Builder<?> builder, IRConverter irConverter, ExecutorService executorService)
      throws ExecutionException {
    assert verifyAllClassesGenerated();
    // We register first, then optimize to avoid warnings due to missing parameter types.
    registerWrappers(builder, typeWrappers);
    registerWrappers(builder, vivifiedTypeWrappers);
    finalizeWrappers(irConverter, executorService, typeWrappers, this::generateTypeConversions);
    finalizeWrappers(
        irConverter,
        executorService,
        vivifiedTypeWrappers,
        this::generateVivifiedTypeConversions);
  }

  private void registerWrappers(
      DexApplication.Builder<?> builder, Map<DexType, Pair<DexType, DexProgramClass>> wrappers) {
    for (DexType type : wrappers.keySet()) {
      DexProgramClass pgrmClass = wrappers.get(type).getSecond();
      assert pgrmClass != null;
      registerSynthesizedClass(pgrmClass, builder);
    }
  }

  private void finalizeWrappers(
      IRConverter irConverter,
      ExecutorService executorService,
      Map<DexType, Pair<DexType, DexProgramClass>> wrappers,
      BiConsumer<DexType, DexProgramClass> generateConversions)
      throws ExecutionException {
    for (DexType type : wrappers.keySet()) {
      DexProgramClass pgrmClass = wrappers.get(type).getSecond();
      assert pgrmClass != null;
      generateConversions.accept(type, pgrmClass);
      irConverter.optimizeSynthesizedClass(pgrmClass, executorService);
    }
  }

  private boolean verifyAllClassesGenerated() {
    for (Pair<DexType, DexProgramClass> pair : vivifiedTypeWrappers.values()) {
      assert pair.getSecond() != null;
    }
    for (Pair<DexType, DexProgramClass> pair : typeWrappers.values()) {
      assert pair.getSecond() != null;
    }
    return true;
  }

  private void registerSynthesizedClass(
      DexProgramClass synthesizedClass, DexApplication.Builder<?> builder) {
    builder.addSynthesizedClass(synthesizedClass, false);
    appView.appInfo().addSynthesizedClass(synthesizedClass);
  }

  private void generateTypeConversions(DexType type, DexProgramClass synthesizedClass) {
    Pair<DexType, DexProgramClass> reverse = vivifiedTypeWrappers.get(type);
    assert reverse == null || reverse.getSecond() != null;
    synthesizedClass.addDirectMethod(
        synthesizeConversionMethod(
            synthesizedClass.type,
            type,
            type,
            vivifiedTypeFor(type),
            reverse == null ? null : reverse.getSecond()));
  }

  private void generateVivifiedTypeConversions(DexType type, DexProgramClass synthesizedClass) {
    Pair<DexType, DexProgramClass> reverse = typeWrappers.get(type);
    synthesizedClass.addDirectMethod(
        synthesizeConversionMethod(
            synthesizedClass.type,
            type,
            vivifiedTypeFor(type),
            type,
            reverse == null ? null : reverse.getSecond()));
  }

  private DexEncodedMethod synthesizeConversionMethod(
      DexType holder,
      DexType type,
      DexType argType,
      DexType returnType,
      DexClass reverseWrapperClassOrNull) {
    DexMethod method =
        factory.createMethod(
            holder, factory.createProto(returnType, argType), factory.convertMethodName);
    CfCode cfCode;
    if (invalidWrappers.contains(holder)) {
      cfCode =
          new APIConverterThrowRuntimeExceptionCfCodeProvider(
                  appView,
                  factory.createString(
                      "Unsupported conversion for "
                          + type
                          + ". See compilation time warnings for more details."),
                  holder)
              .generateCfCode();
    } else {
      DexField uniqueFieldOrNull =
          reverseWrapperClassOrNull == null
              ? null
              : reverseWrapperClassOrNull.instanceFields().get(0).field;
      cfCode =
          new APIConverterWrapperConversionCfCodeProvider(
                  appView,
                  argType,
                  uniqueFieldOrNull,
                  factory.createField(holder, returnType, factory.wrapperFieldName))
              .generateCfCode();
    }
    return newSynthesizedMethod(
        method,
        Constants.ACC_SYNTHETIC | Constants.ACC_STATIC | Constants.ACC_PUBLIC,
        false,
        cfCode);
  }
}
