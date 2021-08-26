// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.desugar.InterfaceMethodRewriter;
import com.android.tools.r8.kotlin.KotlinMetadataRewriter;
import com.android.tools.r8.naming.ClassNameMinifier.ClassRenaming;
import com.android.tools.r8.naming.FieldNameMinifier.FieldRenaming;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.MethodNameMinifier.MethodRenaming;
import com.android.tools.r8.naming.Minifier.MinificationClassNamingStrategy;
import com.android.tools.r8.naming.Minifier.MinificationPackageNamingStrategy;
import com.android.tools.r8.naming.Minifier.MinifierMemberNamingStrategy;
import com.android.tools.r8.position.Position;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.Timing;
import com.android.tools.r8.utils.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * The ProguardMapMinifier will assign names to classes and members following the initial naming
 * seed given by the mapping files.
 *
 * <p>First the object hierarchy is traversed maintaining a collection of all program classes and
 * classes that needs to be renamed in {@link #mappedClasses}. For each level we keep track of all
 * renamed members and propagate all non-private items to descendants. This is necessary to ensure
 * that virtual methods are renamed when there are "gaps" in the hierarchy. We keep track of all
 * namings such that future renaming of non-private members will not collide or fail with an error.
 *
 * <p>Second, we compute desugared default interface methods and companion classes to ensure these
 * can be referred to by clients.
 *
 * <p>Third, we traverse all reachable interfaces for class mappings and add them to our tracking
 * maps. Otherwise, the minification follows the ordinary minification.
 */
public class ProguardMapMinifier {

  private final AppView<AppInfoWithLiveness> appView;
  private final DexItemFactory factory;
  private final SeedMapper seedMapper;
  private final Set<DexCallSite> desugaredCallSites;
  private final BiMap<DexType, DexString> mappedNames = HashBiMap.create();
  // To keep the order deterministic, we sort the classes by their type, which is a unique key.
  private final Set<DexClass> mappedClasses = new TreeSet<>((a, b) -> a.type.slowCompareTo(b.type));
  private final Map<DexReference, MemberNaming> memberNames = Maps.newIdentityHashMap();
  private final Map<DexType, DexString> syntheticCompanionClasses = Maps.newIdentityHashMap();
  private final Map<DexMethod, DexString> defaultInterfaceMethodImplementationNames =
      Maps.newIdentityHashMap();
  private final Map<DexMethod, DexString> additionalMethodNamings = Maps.newIdentityHashMap();
  private final Map<DexField, DexString> additionalFieldNamings = Maps.newIdentityHashMap();

  public ProguardMapMinifier(
      AppView<AppInfoWithLiveness> appView,
      SeedMapper seedMapper,
      Set<DexCallSite> desugaredCallSites) {
    this.appView = appView;
    this.factory = appView.dexItemFactory();
    this.seedMapper = seedMapper;
    this.desugaredCallSites = desugaredCallSites;
  }

  public NamingLens run(ExecutorService executorService, Timing timing) throws ExecutionException {

    ArrayDeque<Map<DexReference, MemberNaming>> nonPrivateMembers = new ArrayDeque<>();

    timing.begin("MappingInterfaces");
    Set<DexClass> interfaces = new TreeSet<>((a, b) -> a.type.slowCompareTo(b.type));
    AppInfoWithLiveness appInfo = appView.appInfo();
    for (DexClass dexClass : appInfo.app().asDirect().allClasses()) {
      if (dexClass.isInterface()) {
        // Only visit top level interfaces because computeMapping will visit the hierarchy.
        if (dexClass.interfaces.isEmpty()) {
          computeMapping(dexClass.type, nonPrivateMembers);
        }
        interfaces.add(dexClass);
      }
    }
    assert nonPrivateMembers.isEmpty();
    timing.end();

    timing.begin("MappingClasses");
    appInfo.forAllImmediateExtendsSubtypes(
        factory.objectType,
        subType -> {
          DexClass dexClass = appView.definitionFor(subType);
          if (dexClass != null && !dexClass.isInterface()) {
            computeMapping(subType, nonPrivateMembers);
          }
        });
    assert nonPrivateMembers.isEmpty();
    timing.end();

    timing.begin("MappingDefaultInterfaceMethods");
    computeDefaultInterfaceMethodMethods();
    timing.end();

    appView.options().reporter.failIfPendingErrors();

    timing.begin("MinifyClasses");
    ClassNameMinifier classNameMinifier =
        new ClassNameMinifier(
            appView,
            new ApplyMappingClassNamingStrategy(appView, mappedNames),
            // The package naming strategy will actually not be used since all classes and methods
            // will be output with identity name if not found in mapping. However, there is a check
            // in the ClassNameMinifier that the strategy should produce a "fresh" name so we just
            // use the existing strategy.
            new MinificationPackageNamingStrategy(appView),
            mappedClasses);
    ClassRenaming classRenaming =
        classNameMinifier.computeRenaming(timing, syntheticCompanionClasses);
    timing.end();

    ApplyMappingMemberNamingStrategy nameStrategy =
        new ApplyMappingMemberNamingStrategy(appView, memberNames);
    timing.begin("MinifyMethods");
    MethodRenaming methodRenaming =
        new MethodNameMinifier(appView, nameStrategy)
            .computeRenaming(interfaces, desugaredCallSites, timing);
    // Amend the method renamings with the default interface methods.
    methodRenaming.renaming.putAll(defaultInterfaceMethodImplementationNames);
    methodRenaming.renaming.putAll(additionalMethodNamings);
    timing.end();

    timing.begin("MinifyFields");
    FieldRenaming fieldRenaming =
        new FieldNameMinifier(appView, nameStrategy).computeRenaming(interfaces, timing);
    fieldRenaming.renaming.putAll(additionalFieldNamings);
    timing.end();

    appView.options().reporter.failIfPendingErrors();

    NamingLens lens = new MinifiedRenaming(appView, classRenaming, methodRenaming, fieldRenaming);

    timing.begin("MinifyIdentifiers");
    new IdentifierMinifier(appView, lens).run(executorService);
    timing.end();

    timing.begin("MinifyKotlinMetadata");
    new KotlinMetadataRewriter(appView, lens).run(executorService);
    timing.end();

    return lens;
  }

  private void computeMapping(DexType type, Deque<Map<DexReference, MemberNaming>> buildUpNames) {
    ClassNamingForMapApplier classNaming = seedMapper.getClassNaming(type);
    DexClass dexClass = appView.definitionFor(type);

    // Keep track of classes that needs to get renamed.
    if (dexClass != null && (classNaming != null || dexClass.isProgramClass())) {
      mappedClasses.add(dexClass);
    }

    Map<DexReference, MemberNaming> nonPrivateMembers = new IdentityHashMap<>();

    if (classNaming != null) {
      // TODO(b/133091438) assert that !dexClass.isLibraryClass();
      DexString mappedName = factory.createString(classNaming.renamedName);
      checkAndAddMappedNames(type, mappedName, classNaming.position);
      if (dexClass != null) {
        KotlinMetadataRewriter.removeKotlinMetadataFromRenamedClass(appView, dexClass);
      }

      classNaming.forAllMemberNaming(
          memberNaming -> addMemberNamings(type, memberNaming, nonPrivateMembers, false));
    } else {
      // We have to ensure we do not rename to an existing member, that cannot be renamed.
      if (dexClass == null || !appView.options().isMinifying()) {
        checkAndAddMappedNames(type, type.descriptor, Position.UNKNOWN);
      } else if (appView.options().isMinifying()
          && appView.rootSet().mayNotBeMinified(type, appView)) {
        checkAndAddMappedNames(type, type.descriptor, Position.UNKNOWN);
      }
    }

    for (Map<DexReference, MemberNaming> parentMembers : buildUpNames) {
      for (DexReference key : parentMembers.keySet()) {
        if (key.isDexMethod()) {
          DexMethod parentReference = key.asDexMethod();
          DexMethod parentReferenceOnCurrentType =
              factory.createMethod(type, parentReference.proto, parentReference.name);
          if (!memberNames.containsKey(parentReferenceOnCurrentType)) {
            addMemberNaming(
                parentReferenceOnCurrentType, parentMembers.get(key), additionalMethodNamings);
          } else {
            DexEncodedMethod encodedMethod = appView.definitionFor(parentReferenceOnCurrentType);
            assert encodedMethod == null
                || encodedMethod.accessFlags.isStatic()
                || memberNames
                    .get(parentReferenceOnCurrentType)
                    .getRenamedName()
                    .equals(parentMembers.get(key).getRenamedName());
          }
        } else {
          DexField parentReference = key.asDexField();
          DexField parentReferenceOnCurrentType =
              factory.createField(type, parentReference.type, parentReference.name);
          if (!memberNames.containsKey(parentReferenceOnCurrentType)) {
            addMemberNaming(
                parentReferenceOnCurrentType, parentMembers.get(key), additionalFieldNamings);
          } else {
            // Fields are allowed to be named differently in the hierarchy.
          }
        }
      }
    }

    if (dexClass != null) {
      // If a class is marked as abstract it is allowed to not implement methods from interfaces
      // thus the map will not contain a mapping. Also, if an interface is defined in the library
      // and the class is in the program, we have to build up the correct names to reserve them.
      if (dexClass.isProgramClass() || dexClass.isAbstract()) {
        addNonPrivateInterfaceMappings(type, nonPrivateMembers, dexClass.interfaces.values);
      }
    }

    if (nonPrivateMembers.size() > 0) {
      buildUpNames.addLast(nonPrivateMembers);
      appView
          .appInfo()
          .forAllImmediateExtendsSubtypes(type, subType -> computeMapping(subType, buildUpNames));
      buildUpNames.removeLast();
    } else {
      appView
          .appInfo()
          .forAllImmediateExtendsSubtypes(type, subType -> computeMapping(subType, buildUpNames));
    }
  }

  private void addNonPrivateInterfaceMappings(
      DexType type, Map<DexReference, MemberNaming> nonPrivateMembers, DexType[] interfaces) {
    for (DexType iface : interfaces) {
      ClassNamingForMapApplier interfaceNaming = seedMapper.getClassNaming(iface);
      if (interfaceNaming != null) {
        interfaceNaming.forAllMemberNaming(
            memberNaming -> addMemberNamings(type, memberNaming, nonPrivateMembers, true));
      }
      DexClass ifaceClass = appView.definitionFor(iface);
      if (ifaceClass != null) {
        addNonPrivateInterfaceMappings(type, nonPrivateMembers, ifaceClass.interfaces.values);
      }
    }
  }

  private void addMemberNamings(
      DexType type,
      MemberNaming memberNaming,
      Map<DexReference, MemberNaming> nonPrivateMembers,
      boolean addToAdditionalMaps) {
    Signature signature = memberNaming.getOriginalSignature();
    assert !signature.isQualified();
    if (signature instanceof MethodSignature) {
      DexMethod originalMethod = ((MethodSignature) signature).toDexMethod(factory, type);
      addMemberNaming(
          originalMethod, memberNaming, addToAdditionalMaps ? additionalMethodNamings : null);
      DexEncodedMethod encodedMethod = appView.definitionFor(originalMethod);
      if (encodedMethod == null || !encodedMethod.accessFlags.isPrivate()) {
        nonPrivateMembers.put(originalMethod, memberNaming);
      }
    } else {
      DexField originalField = ((FieldSignature) signature).toDexField(factory, type);
      addMemberNaming(
          originalField, memberNaming, addToAdditionalMaps ? additionalFieldNamings : null);
      DexEncodedField encodedField = appView.definitionFor(originalField);
      if (encodedField == null || !encodedField.accessFlags.isPrivate()) {
        nonPrivateMembers.put(originalField, memberNaming);
      }
    }
  }

  private <T extends DexReference> void addMemberNaming(
      T member, MemberNaming memberNaming, Map<T, DexString> additionalMemberNamings) {
    assert !memberNames.containsKey(member)
        || memberNames.get(member).getRenamedName().equals(memberNaming.getRenamedName());
    memberNames.put(member, memberNaming);
    if (additionalMemberNamings != null) {
      DexString renamedName = factory.createString(memberNaming.getRenamedName());
      additionalMemberNamings.put(member, renamedName);
    }
  }

  private void checkAndAddMappedNames(DexType type, DexString mappedName, Position position) {
    if (mappedNames.inverse().containsKey(mappedName)
        && mappedNames.inverse().get(mappedName) != type) {
      appView
          .options()
          .reporter
          .error(
              ApplyMappingError.mapToExistingClass(
                  type.toString(), mappedName.toString(), position));
    } else {
      mappedNames.put(type, mappedName);
    }
  }

  private void computeDefaultInterfaceMethodMethods() {
    for (String key : seedMapper.getKeyset()) {
      ClassNamingForMapApplier classNaming = seedMapper.getMapping(key);
      DexType type = factory.lookupType(factory.createString(key));
      if (type == null) {
        // The map contains additional mapping of classes compared to what we have seen. This should
        // have no effect.
        continue;
      }
      DexClass dexClass = appView.definitionFor(type);
      if (dexClass == null) {
        computeDefaultInterfaceMethodMappingsForType(
            type,
            classNaming,
            syntheticCompanionClasses,
            defaultInterfaceMethodImplementationNames);
      }
    }
  }

  private void computeDefaultInterfaceMethodMappingsForType(
      DexType type,
      ClassNamingForMapApplier classNaming,
      Map<DexType, DexString> syntheticCompanionClasses,
      Map<DexMethod, DexString> defaultInterfaceMethodImplementationNames) {
    // If the class does not resolve, then check if it is a companion class for an interface on
    // the class path.
    if (!InterfaceMethodRewriter.isCompanionClassType(type)) {
      return;
    }
    DexClass interfaceType =
        appView.definitionFor(InterfaceMethodRewriter.getInterfaceClassType(type, factory));
    if (interfaceType == null || !interfaceType.isClasspathClass()) {
      return;
    }
    syntheticCompanionClasses.put(type, factory.createString(classNaming.renamedName));
    for (List<MemberNaming> namings : classNaming.getQualifiedMethodMembers().values()) {
      // If the qualified name has been mapped to multiple names we can't compute a mapping (and it
      // should not be possible that this is a default interface method in that case.)
      if (namings.size() != 1) {
        continue;
      }
      MemberNaming naming = namings.get(0);
      MethodSignature signature = (MethodSignature) naming.getOriginalSignature();
      if (signature.name.startsWith(interfaceType.type.toSourceString())) {
        DexMethod defaultMethod =
            InterfaceMethodRewriter.defaultAsMethodOfCompanionClass(
                signature.toUnqualified().toDexMethod(factory, interfaceType.type), factory);
        assert defaultMethod.holder == type;
        defaultInterfaceMethodImplementationNames.put(
            defaultMethod, factory.createString(naming.getRenamedName()));
      }
    }
  }

  static class ApplyMappingClassNamingStrategy extends MinificationClassNamingStrategy {

    private final Map<DexType, DexString> mappings;

    ApplyMappingClassNamingStrategy(AppView<?> appView, Map<DexType, DexString> mappings) {
      super(appView);
      this.mappings = mappings;
    }

    @Override
    public DexString next(
        DexType type,
        char[] packagePrefix,
        InternalNamingState state,
        Predicate<DexString> isUsed) {
      assert !mappings.containsKey(type);
      assert appView.rootSet().mayBeMinified(type, appView);
      return super.next(type, packagePrefix, state, isUsed);
    }

    @Override
    public DexString reservedDescriptor(DexType type) {
      // TODO(b/136694827): Check for already used and report an error. It seems like this can be
      //  done already in the reservation step for classes since there is only one 'path', unlike
      //  members that can be reserved differently in the hierarchy.
      DexClass clazz = appView.definitionFor(type);
      if (clazz == null) {
        return type.descriptor;
      }
      if (clazz.isNotProgramClass() && mappings.containsKey(type)) {
        return mappings.get(type);
      }
      if (clazz.isProgramClass() && appView.rootSet().mayBeMinified(type, appView)) {
        if (mappings.containsKey(type)) {
          return mappings.get(type);
        }
        return null;
      } else if (clazz.isProgramClass()
          && !appView.rootSet().mayBeMinified(type, appView)
          && mappings.containsKey(type)) {
        // TODO(b/136694827): Report a warning here since the user may find this not intuitive.
      }
      return type.descriptor;
    }

    @Override
    public boolean isRenamedByApplyMapping(DexType type) {
      return mappings.containsKey(type);
    }
  }

  static class ApplyMappingMemberNamingStrategy extends MinifierMemberNamingStrategy {

    private final Map<DexReference, MemberNaming> mappedNames;
    private final DexItemFactory factory;
    private final Reporter reporter;

    public ApplyMappingMemberNamingStrategy(
        AppView<?> appView, Map<DexReference, MemberNaming> mappedNames) {
      super(appView);
      this.mappedNames = mappedNames;
      this.factory = appView.dexItemFactory();
      this.reporter = appView.options().reporter;
    }

    @Override
    public DexString next(
        DexMethod method,
        InternalNamingState internalState,
        BiPredicate<DexString, DexMethod> isAvailable) {
      DexEncodedMethod definition = appView.definitionFor(method);
      DexString nextName =
          nextName(
              method,
              definition,
              method.name,
              method.holder,
              internalState,
              isAvailable,
              super::next);
      assert nextName == method.name || !definition.isClassInitializer();
      assert nextName == method.name
          || !appView.definitionFor(method.holder).accessFlags.isAnnotation();
      return nextName;
    }

    @Override
    public DexString next(
        DexField field,
        InternalNamingState internalState,
        BiPredicate<DexString, DexField> isAvailable) {
      return nextName(
          field,
          appView.definitionFor(field),
          field.name,
          field.holder,
          internalState,
          isAvailable,
          super::next);
    }

    private <T extends DexReference> DexString nextName(
        T reference,
        DexDefinition definition,
        DexString name,
        DexType holderType,
        InternalNamingState internalState,
        BiPredicate<DexString, T> isAvailable,
        TriFunction<T, InternalNamingState, BiPredicate<DexString, T>, DexString> generateName) {
      assert definition.isDexEncodedMethod() || definition.isDexEncodedField();
      assert definition.toReference() == reference;
      DexClass holder = appView.definitionFor(holderType);
      assert holder != null;
      DexString reservedName = getReservedName(definition, name, holder);
      if (reservedName != null) {
        if (!isAvailable.test(reservedName, reference)) {
          reportReservationError(definition.asDexReference(), reservedName);
        }
        return reservedName;
      }
      assert !mappedNames.containsKey(reference);
      assert appView.rootSet().mayBeMinified(reference, appView);
      return generateName.apply(reference, internalState, isAvailable);
    }

    @Override
    public DexString getReservedName(DexEncodedMethod method, DexClass holder) {
      return getReservedName(method, method.method.name, holder);
    }

    @Override
    public DexString getReservedName(DexEncodedField field, DexClass holder) {
      return getReservedName(field, field.field.name, holder);
    }

    private DexString getReservedName(DexDefinition definition, DexString name, DexClass holder) {
      assert definition.isDexEncodedMethod() || definition.isDexEncodedField();
      // Always consult the mapping for renamed members that are not on program path.
      DexReference reference = definition.toReference();
      if (holder.isNotProgramClass()) {
        if (mappedNames.containsKey(reference)) {
          return factory.createString(mappedNames.get(reference).getRenamedName());
        }
        return name;
      }
      assert holder.isProgramClass();
      DexString reservedName =
          definition.isDexEncodedMethod()
              ? super.getReservedName(definition.asDexEncodedMethod(), holder)
              : super.getReservedName(definition.asDexEncodedField(), holder);
      if (reservedName != null) {
        return reservedName;
      }
      if (mappedNames.containsKey(reference)) {
        return factory.createString(mappedNames.get(reference).getRenamedName());
      }
      return null;
    }

    @Override
    public boolean allowMemberRenaming(DexClass holder) {
      return true;
    }

    void reportReservationError(DexReference source, DexString name) {
      MemberNaming memberNaming = mappedNames.get(source);
      assert source.isDexMethod() || source.isDexField();
      ApplyMappingError applyMappingError =
          ApplyMappingError.mapToExistingMember(
              source.toSourceString(),
              name.toString(),
              memberNaming == null ? Position.UNKNOWN : memberNaming.position);
      // TODO(b/136694827) Enable when we have proper support
      // reporter.error(applyMappingError);
    }
  }
}
