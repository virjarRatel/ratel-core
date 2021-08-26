// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.ConstInstruction;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.Position;
import com.android.tools.r8.utils.BooleanUtils;
import com.android.tools.r8.utils.IteratorUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A GraphLense implements a virtual view on top of the graph, used to delay global rewrites until
 * later IR processing stages.
 * <p>
 * Valid remappings are limited to the following operations:
 * <ul>
 * <li>Mapping a classes type to one of the super/subtypes.</li>
 * <li>Renaming private methods/fields.</li>
 * <li>Moving methods/fields to a super/subclass.</li>
 * <li>Replacing method/field references by the same method/field on a super/subtype</li>
 * <li>Moved methods might require changed invocation type at the call site</li>
 * </ul>
 * Note that the latter two have to take visibility into account.
 */
public abstract class GraphLense {

  /**
   * Result of a method lookup in a GraphLense.
   *
   * This provide the new target and the invoke type to use.
   */
  public static class GraphLenseLookupResult {

    private final DexMethod method;
    private final Type type;

    public GraphLenseLookupResult(DexMethod method, Type type) {
      this.method = method;
      this.type = type;
    }

    public DexMethod getMethod() {
      return method;
    }

    public Type getType() {
      return type;
    }
  }

  public static class RewrittenPrototypeDescription {

    public static class RemovedArgumentInfo {

      public static class Builder {

        private int argumentIndex = -1;
        private boolean isAlwaysNull = false;
        private DexType type = null;

        public Builder setArgumentIndex(int argumentIndex) {
          this.argumentIndex = argumentIndex;
          return this;
        }

        public Builder setIsAlwaysNull() {
          this.isAlwaysNull = true;
          return this;
        }

        public Builder setType(DexType type) {
          this.type = type;
          return this;
        }

        public RemovedArgumentInfo build() {
          assert argumentIndex >= 0;
          assert type != null;
          return new RemovedArgumentInfo(argumentIndex, isAlwaysNull, type);
        }
      }

      private final int argumentIndex;
      private final boolean isAlwaysNull;
      private final DexType type;

      private RemovedArgumentInfo(int argumentIndex, boolean isAlwaysNull, DexType type) {
        this.argumentIndex = argumentIndex;
        this.isAlwaysNull = isAlwaysNull;
        this.type = type;
      }

      public static Builder builder() {
        return new Builder();
      }

      public int getArgumentIndex() {
        return argumentIndex;
      }

      public DexType getType() {
        return type;
      }

      public boolean isAlwaysNull() {
        return isAlwaysNull;
      }

      public boolean isNeverUsed() {
        return !isAlwaysNull;
      }

      public RemovedArgumentInfo withArgumentIndex(int argumentIndex) {
        return this.argumentIndex != argumentIndex
            ? new RemovedArgumentInfo(argumentIndex, isAlwaysNull, type)
            : this;
      }
    }

    public static class RemovedArgumentsInfo {

      private static final RemovedArgumentsInfo empty = new RemovedArgumentsInfo(null);

      private final List<RemovedArgumentInfo> removedArguments;

      public RemovedArgumentsInfo(List<RemovedArgumentInfo> removedArguments) {
        assert verifyRemovedArguments(removedArguments);
        this.removedArguments = removedArguments;
      }

      private static boolean verifyRemovedArguments(List<RemovedArgumentInfo> removedArguments) {
        if (removedArguments != null && !removedArguments.isEmpty()) {
          // Check that list is sorted by argument indices.
          int lastArgumentIndex = removedArguments.get(0).getArgumentIndex();
          for (int i = 1; i < removedArguments.size(); ++i) {
            int currentArgumentIndex = removedArguments.get(i).getArgumentIndex();
            assert lastArgumentIndex < currentArgumentIndex;
            lastArgumentIndex = currentArgumentIndex;
          }
        }
        return true;
      }

      public static RemovedArgumentsInfo empty() {
        return empty;
      }

      public ListIterator<RemovedArgumentInfo> iterator() {
        return removedArguments == null
            ? Collections.emptyListIterator()
            : removedArguments.listIterator();
      }

      public boolean hasRemovedArguments() {
        return removedArguments != null && !removedArguments.isEmpty();
      }

      public boolean isArgumentRemoved(int argumentIndex) {
        if (removedArguments != null) {
          for (RemovedArgumentInfo info : removedArguments) {
            if (info.getArgumentIndex() == argumentIndex) {
              return true;
            }
          }
        }
        return false;
      }

      public int numberOfRemovedArguments() {
        return removedArguments != null ? removedArguments.size() : 0;
      }

      public RemovedArgumentsInfo combine(RemovedArgumentsInfo info) {
        assert info != null;
        if (hasRemovedArguments()) {
          if (!info.hasRemovedArguments()) {
            return this;
          }
        } else {
          return info;
        }

        List<RemovedArgumentInfo> newRemovedArguments = new LinkedList<>(removedArguments);
        ListIterator<RemovedArgumentInfo> iterator = newRemovedArguments.listIterator();
        int offset = 0;
        for (RemovedArgumentInfo pending : info.removedArguments) {
          RemovedArgumentInfo next = IteratorUtils.peekNext(iterator);
          while (next != null && next.getArgumentIndex() <= pending.getArgumentIndex() + offset) {
            iterator.next();
            next = IteratorUtils.peekNext(iterator);
            offset++;
          }
          iterator.add(pending.withArgumentIndex(pending.getArgumentIndex() + offset));
        }
        return new RemovedArgumentsInfo(newRemovedArguments);
      }

      public Consumer<DexEncodedMethod.Builder> createParameterAnnotationsRemover(
          DexEncodedMethod method) {
        if (numberOfRemovedArguments() > 0 && !method.parameterAnnotationsList.isEmpty()) {
          return builder -> {
            int firstArgumentIndex = BooleanUtils.intValue(!method.isStatic());
            builder.removeParameterAnnotations(
                oldIndex -> isArgumentRemoved(oldIndex + firstArgumentIndex));
          };
        }
        return null;
      }
    }

    private static final RewrittenPrototypeDescription none = new RewrittenPrototypeDescription();

    private final boolean hasBeenChangedToReturnVoid;
    private final boolean extraNullParameter;
    private final RemovedArgumentsInfo removedArgumentsInfo;

    private RewrittenPrototypeDescription() {
      this(false, false, RemovedArgumentsInfo.empty());
    }

    public RewrittenPrototypeDescription(
        boolean hasBeenChangedToReturnVoid,
        boolean extraNullParameter,
        RemovedArgumentsInfo removedArgumentsInfo) {
      assert removedArgumentsInfo != null;
      this.extraNullParameter = extraNullParameter;
      this.hasBeenChangedToReturnVoid = hasBeenChangedToReturnVoid;
      this.removedArgumentsInfo = removedArgumentsInfo;
    }

    public static RewrittenPrototypeDescription none() {
      return none;
    }

    public boolean isEmpty() {
      return !extraNullParameter
          && !hasBeenChangedToReturnVoid
          && !getRemovedArgumentsInfo().hasRemovedArguments();
    }

    public boolean hasExtraNullParameter() {
      return extraNullParameter;
    }

    public boolean hasBeenChangedToReturnVoid() {
      return hasBeenChangedToReturnVoid;
    }

    public RemovedArgumentsInfo getRemovedArgumentsInfo() {
      return removedArgumentsInfo;
    }

    /**
     * Returns the {@link ConstInstruction} that should be used to materialize the result of
     * invocations to the method represented by this {@link RewrittenPrototypeDescription}.
     *
     * <p>This method should only be used for methods that return a constant value and whose return
     * type has been changed to void.
     *
     * <p>Note that the current implementation always returns null at this point.
     */
    public ConstInstruction getConstantReturn(IRCode code, Position position) {
      assert hasBeenChangedToReturnVoid;
      ConstInstruction instruction = code.createConstNull();
      instruction.setPosition(position);
      return instruction;
    }

    public DexType rewriteReturnType(DexType returnType, DexItemFactory dexItemFactory) {
      return hasBeenChangedToReturnVoid ? dexItemFactory.voidType : returnType;
    }

    public DexType[] rewriteParameters(DexType[] params) {
      RemovedArgumentsInfo removedArgumentsInfo = getRemovedArgumentsInfo();
      if (removedArgumentsInfo.hasRemovedArguments()) {
        DexType[] newParams =
            new DexType[params.length - removedArgumentsInfo.numberOfRemovedArguments()];
        int newParamIndex = 0;
        for (int oldParamIndex = 0; oldParamIndex < params.length; ++oldParamIndex) {
          if (!removedArgumentsInfo.isArgumentRemoved(oldParamIndex)) {
            newParams[newParamIndex] = params[oldParamIndex];
            ++newParamIndex;
          }
        }
        return newParams;
      }
      return params;
    }

    public DexProto rewriteProto(DexProto proto, DexItemFactory dexItemFactory) {
      DexType newReturnType = rewriteReturnType(proto.returnType, dexItemFactory);
      DexType[] newParameters = rewriteParameters(proto.parameters.values);
      return dexItemFactory.createProto(newReturnType, newParameters);
    }

    public RewrittenPrototypeDescription withConstantReturn() {
      return !hasBeenChangedToReturnVoid
          ? new RewrittenPrototypeDescription(true, extraNullParameter, removedArgumentsInfo)
          : this;
    }

    public RewrittenPrototypeDescription withRemovedArguments(RemovedArgumentsInfo other) {
      return new RewrittenPrototypeDescription(
          hasBeenChangedToReturnVoid, extraNullParameter, removedArgumentsInfo.combine(other));
    }

    public RewrittenPrototypeDescription withExtraNullParameter() {
      return !extraNullParameter
          ? new RewrittenPrototypeDescription(
              hasBeenChangedToReturnVoid, true, removedArgumentsInfo)
          : this;
    }
  }

  public static class Builder {

    protected Builder() {}

    protected final Map<DexType, DexType> typeMap = new IdentityHashMap<>();
    protected final Map<DexMethod, DexMethod> methodMap = new IdentityHashMap<>();
    protected final Map<DexField, DexField> fieldMap = new IdentityHashMap<>();

    private final BiMap<DexField, DexField> originalFieldSignatures = HashBiMap.create();
    private final BiMap<DexMethod, DexMethod> originalMethodSignatures = HashBiMap.create();

    public void map(DexType from, DexType to) {
      if (from == to) {
        return;
      }
      typeMap.put(from, to);
    }

    public void map(DexMethod from, DexMethod to) {
      if (from == to) {
        return;
      }
      methodMap.put(from, to);
    }

    public void map(DexField from, DexField to) {
      if (from == to) {
        return;
      }
      fieldMap.put(from, to);
    }

    public void move(DexMethod from, DexMethod to) {
      if (from == to) {
        return;
      }
      map(from, to);
      originalMethodSignatures.put(to, from);
    }

    public void move(DexField from, DexField to) {
      if (from == to) {
        return;
      }
      fieldMap.put(from, to);
      originalFieldSignatures.put(to, from);
    }

    public GraphLense build(DexItemFactory dexItemFactory) {
      return build(dexItemFactory, getIdentityLense());
    }

    public GraphLense build(DexItemFactory dexItemFactory, GraphLense previousLense) {
      if (typeMap.isEmpty() && methodMap.isEmpty() && fieldMap.isEmpty()) {
        return previousLense;
      }
      return new NestedGraphLense(
          typeMap,
          methodMap,
          fieldMap,
          originalFieldSignatures,
          originalMethodSignatures,
          previousLense,
          dexItemFactory);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public abstract DexType getOriginalType(DexType type);

  public abstract DexField getOriginalFieldSignature(DexField field);

  public abstract DexMethod getOriginalMethodSignature(DexMethod method);

  public abstract DexField getRenamedFieldSignature(DexField originalField);

  public abstract DexMethod getRenamedMethodSignature(DexMethod originalMethod);

  public DexEncodedMethod mapDexEncodedMethod(
      DexEncodedMethod originalEncodedMethod, DexDefinitionSupplier definitions) {
    assert originalEncodedMethod != DexEncodedMethod.SENTINEL;
    if (originalEncodedMethod == DexEncodedMethod.ANNOTATION_REFERENCE) {
      return DexEncodedMethod.ANNOTATION_REFERENCE;
    }
    DexMethod newMethod = getRenamedMethodSignature(originalEncodedMethod.method);
    // Note that:
    // * Even if `newMethod` is the same as `originalEncodedMethod.method`, we still need to look it
    //   up, since `originalEncodedMethod` may be obsolete.
    // * We can't directly use AppInfo#definitionFor(DexMethod) since definitions may not be
    //   updated either yet.
    DexClass newHolder = definitions.definitionFor(newMethod.holder);
    assert newHolder != null;
    DexEncodedMethod newEncodedMethod = newHolder.lookupMethod(newMethod);
    assert newEncodedMethod != null;
    return newEncodedMethod;
  }

  public abstract DexType lookupType(DexType type);

  // This overload can be used when the graph lense is known to be context insensitive.
  public DexMethod lookupMethod(DexMethod method) {
    assert isContextFreeForMethod(method);
    return lookupMethod(method, null, null).getMethod();
  }

  public abstract GraphLenseLookupResult lookupMethod(
      DexMethod method, DexMethod context, Type type);

  public abstract RewrittenPrototypeDescription lookupPrototypeChanges(DexMethod method);

  // Context sensitive graph lenses should override this method.
  public Set<DexMethod> lookupMethodInAllContexts(DexMethod method) {
    assert isContextFreeForMethod(method);
    DexMethod result = lookupMethod(method);
    if (result != null) {
      return ImmutableSet.of(result);
    }
    return ImmutableSet.of();
  }

  public abstract DexField lookupField(DexField field);

  public DexMethod lookupGetFieldForMethod(DexField field, DexMethod context) {
    return null;
  }

  public DexMethod lookupPutFieldForMethod(DexField field, DexMethod context) {
    return null;
  }

  public DexReference lookupReference(DexReference reference) {
    if (reference.isDexType()) {
      return lookupType(reference.asDexType());
    } else if (reference.isDexMethod()) {
      return lookupMethod(reference.asDexMethod());
    } else {
      assert reference.isDexField();
      return lookupField(reference.asDexField());
    }
  }

  // The method lookupMethod() maps a pair INVOKE=(method signature, invoke type) to a new pair
  // INVOKE'=(method signature', invoke type'). This mapping can be context sensitive, meaning that
  // the result INVOKE' depends on where the invocation INVOKE is in the program. This is, for
  // example, used by the vertical class merger to translate invoke-super instructions that hit
  // a method in the direct super class to invoke-direct instructions after class merging.
  //
  // This method can be used to determine if a graph lense is context sensitive. If a graph lense
  // is context insensitive, it is safe to invoke lookupMethod() without a context (or to pass null
  // as context). Trying to invoke a context sensitive graph lense without a context will lead to
  // an assertion error.
  public abstract boolean isContextFreeForMethods();

  public boolean isContextFreeForMethod(DexMethod method) {
    return isContextFreeForMethods();
  }

  public static GraphLense getIdentityLense() {
    return IdentityGraphLense.getInstance();
  }

  public final boolean isIdentityLense() {
    return this == getIdentityLense();
  }

  public <T extends DexDefinition> boolean assertDefinitionsNotModified(Iterable<T> definitions) {
    for (DexDefinition definition : definitions) {
      DexReference reference = definition.toReference();
      // We allow changes to bridge methods as these get retargeted even if they are kept.
      boolean isBridge =
          definition.isDexEncodedMethod() && definition.asDexEncodedMethod().accessFlags.isBridge();
      assert isBridge || lookupReference(reference) == reference;
    }
    return true;
  }

  public <T extends DexReference> boolean assertReferencesNotModified(Iterable<T> references) {
    for (DexReference reference : references) {
      if (reference.isDexField()) {
        DexField field = reference.asDexField();
        assert getRenamedFieldSignature(field) == field;
      } else if (reference.isDexMethod()) {
        DexMethod method = reference.asDexMethod();
        assert getRenamedMethodSignature(method) == method;
      } else {
        assert reference.isDexType();
        DexType type = reference.asDexType();
        assert lookupType(type) == type;
      }
    }
    return true;
  }

  public ImmutableList<DexReference> rewriteReferencesConservatively(List<DexReference> original) {
    ImmutableList.Builder<DexReference> builder = ImmutableList.builder();
    for (DexReference item : original) {
      if (item.isDexMethod()) {
        DexMethod method = item.asDexMethod();
        if (isContextFreeForMethod(method)) {
          builder.add(lookupMethod(method));
        } else {
          builder.addAll(lookupMethodInAllContexts(method));
        }
      } else {
        builder.add(lookupReference(item));
      }
    }
    return builder.build();
  }

  public ImmutableSet<DexReference> rewriteReferencesConservatively(Set<DexReference> original) {
    ImmutableSet.Builder<DexReference> builder = ImmutableSet.builder();
    for (DexReference item : original) {
      if (item.isDexMethod()) {
        DexMethod method = item.asDexMethod();
        if (isContextFreeForMethod(method)) {
          builder.add(lookupMethod(method));
        } else {
          builder.addAll(lookupMethodInAllContexts(method));
        }
      } else {
        builder.add(lookupReference(item));
      }
    }
    return builder.build();
  }

  public Set<DexReference> rewriteMutableReferencesConservatively(Set<DexReference> original) {
    Set<DexReference> result = Sets.newIdentityHashSet();
    for (DexReference item : original) {
      if (item.isDexMethod()) {
        DexMethod method = item.asDexMethod();
        if (isContextFreeForMethod(method)) {
          result.add(lookupMethod(method));
        } else {
          result.addAll(lookupMethodInAllContexts(method));
        }
      } else {
        result.add(lookupReference(item));
      }
    }
    return result;
  }

  public Object2BooleanMap<DexReference> rewriteReferencesConservatively(
      Object2BooleanMap<DexReference> original) {
    Object2BooleanMap<DexReference> result = new Object2BooleanArrayMap<>();
    for (Object2BooleanMap.Entry<DexReference> entry : original.object2BooleanEntrySet()) {
      DexReference item = entry.getKey();
      if (item.isDexMethod()) {
        DexMethod method = item.asDexMethod();
        if (isContextFreeForMethod(method)) {
          result.put(lookupMethod(method), entry.getBooleanValue());
        } else {
          for (DexMethod candidate: lookupMethodInAllContexts(method)) {
            result.put(candidate, entry.getBooleanValue());
          }
        }
      } else {
        result.put(lookupReference(item), entry.getBooleanValue());
      }
    }
    return result;
  }

  public ImmutableSet<DexType> rewriteTypesConservatively(Set<DexType> original) {
    ImmutableSet.Builder<DexType> builder = ImmutableSet.builder();
    for (DexType item : original) {
      builder.add(lookupType(item));
    }
    return builder.build();
  }

  public Set<DexType> rewriteMutableTypesConservatively(Set<DexType> original) {
    Set<DexType> result = Sets.newIdentityHashSet();
    for (DexType item : original) {
      result.add(lookupType(item));
    }
    return result;
  }

  public ImmutableSortedSet<DexMethod> rewriteMethodsWithRenamedSignature(Set<DexMethod> methods) {
    ImmutableSortedSet.Builder<DexMethod> builder =
        new ImmutableSortedSet.Builder<>(PresortedComparable::slowCompare);
    for (DexMethod method : methods) {
      builder.add(getRenamedMethodSignature(method));
    }
    return builder.build();
  }

  public ImmutableSortedSet<DexMethod> rewriteMethodsConservatively(Set<DexMethod> original) {
    ImmutableSortedSet.Builder<DexMethod> builder =
        new ImmutableSortedSet.Builder<>(PresortedComparable::slowCompare);
    if (isContextFreeForMethods()) {
      for (DexMethod item : original) {
        builder.add(lookupMethod(item));
      }
    } else {
      for (DexMethod item : original) {
        // Avoid using lookupMethodInAllContexts when possible.
        if (isContextFreeForMethod(item)) {
          builder.add(lookupMethod(item));
        } else {
          // The lense is context sensitive, but we do not have the context here. Therefore, we
          // conservatively look up the method in all contexts.
          builder.addAll(lookupMethodInAllContexts(item));
        }
      }
    }
    return builder.build();
  }

  public SortedSet<DexMethod> rewriteMutableMethodsConservatively(Set<DexMethod> original) {
    SortedSet<DexMethod> result = new TreeSet<>(PresortedComparable::slowCompare);
    if (isContextFreeForMethods()) {
      for (DexMethod item : original) {
        result.add(lookupMethod(item));
      }
    } else {
      for (DexMethod item : original) {
        // Avoid using lookupMethodInAllContexts when possible.
        if (isContextFreeForMethod(item)) {
          result.add(lookupMethod(item));
        } else {
          // The lense is context sensitive, but we do not have the context here. Therefore, we
          // conservatively look up the method in all contexts.
          result.addAll(lookupMethodInAllContexts(item));
        }
      }
    }
    return result;
  }

  public static <T extends DexReference, S> ImmutableMap<T, S> rewriteReferenceKeys(
      Map<T, S> original, Function<T, T> rewrite) {
    ImmutableMap.Builder<T, S> builder = new ImmutableMap.Builder<>();
    for (T item : original.keySet()) {
      builder.put(rewrite.apply(item), original.get(item));
    }
    return builder.build();
  }

  public static <T extends DexReference, S> Map<T, S> rewriteMutableReferenceKeys(
      Map<T, S> original, Function<T, T> rewrite) {
    Map<T, S> result = new IdentityHashMap<>();
    for (T item : original.keySet()) {
      result.put(rewrite.apply(item), original.get(item));
    }
    return result;
  }

  public boolean verifyMappingToOriginalProgram(
      Iterable<DexProgramClass> classes,
      DexApplication originalApplication,
      DexItemFactory dexItemFactory) {
    // Collect all original fields and methods for efficient querying.
    Set<DexField> originalFields = Sets.newIdentityHashSet();
    Set<DexMethod> originalMethods = Sets.newIdentityHashSet();
    for (DexProgramClass clazz : originalApplication.classes()) {
      for (DexEncodedField field : clazz.fields()) {
        originalFields.add(field.field);
      }
      for (DexEncodedMethod method : clazz.methods()) {
        originalMethods.add(method.method);
      }
    }

    // Check that all fields and methods in the generated program can be mapped back to one of the
    // original fields or methods.
    for (DexProgramClass clazz : classes) {
      if (clazz.type.isD8R8SynthesizedClassType()) {
        continue;
      }
      for (DexEncodedField field : clazz.fields()) {
        DexField originalField = getOriginalFieldSignature(field.field);
        assert originalFields.contains(originalField)
            : "Unable to map field `" + field.field.toSourceString() + "` back to original program";
      }
      for (DexEncodedMethod method : clazz.methods()) {
        if (method.accessFlags.isSynthetic()) {
          // This could be a bridge that has been inserted, for example, as a result of member
          // rebinding. Consider only skipping the check below for methods that have been
          // synthesized by R8.
          continue;
        }
        DexMethod originalMethod = getOriginalMethodSignature(method.method);
        assert originalMethods.contains(originalMethod)
                || verifyIsBridgeMethod(
                    originalMethod, originalApplication, originalMethods, dexItemFactory)
            : "Unable to map method `"
                + originalMethod.toSourceString()
                + "` back to original program";
      }
    }

    return true;
  }

  // Check if `method` is a bridge method for a method that is in the original application.
  // This is needed because member rebinding synthesizes bridge methods for visibility.
  private static boolean verifyIsBridgeMethod(
      DexMethod method,
      DexApplication originalApplication,
      Set<DexMethod> originalMethods,
      DexItemFactory dexItemFactory) {
    Deque<DexType> worklist = new ArrayDeque<>();
    Set<DexType> visited = Sets.newIdentityHashSet();
    worklist.add(method.holder);
    while (!worklist.isEmpty()) {
      DexType holder = worklist.removeFirst();
      if (!visited.add(holder)) {
        // Already visited previously.
        continue;
      }
      DexMethod targetMethod = dexItemFactory.createMethod(holder, method.proto, method.name);
      if (originalMethods.contains(targetMethod)) {
        return true;
      }
      // Stop traversing upwards if we reach the Object.
      if (holder == dexItemFactory.objectType) {
        continue;
      }
      DexClass clazz = originalApplication.definitionFor(holder);
      if (clazz != null) {
        worklist.add(clazz.superType);
        Collections.addAll(worklist, clazz.interfaces.values);
      }
    }
    return false;
  }

  private static class IdentityGraphLense extends GraphLense {

    private static IdentityGraphLense INSTANCE = new IdentityGraphLense();

    private IdentityGraphLense() {}

    private static IdentityGraphLense getInstance() {
      return INSTANCE;
    }

    @Override
    public DexType getOriginalType(DexType type) {
      return type;
    }

    @Override
    public DexField getOriginalFieldSignature(DexField field) {
      return field;
    }

    @Override
    public DexMethod getOriginalMethodSignature(DexMethod method) {
      return method;
    }

    @Override
    public DexField getRenamedFieldSignature(DexField originalField) {
      return originalField;
    }

    @Override
    public DexMethod getRenamedMethodSignature(DexMethod originalMethod) {
      return originalMethod;
    }

    @Override
    public DexType lookupType(DexType type) {
      return type;
    }

    @Override
    public GraphLenseLookupResult lookupMethod(DexMethod method, DexMethod context, Type type) {
      return new GraphLenseLookupResult(method, type);
    }

    @Override
    public RewrittenPrototypeDescription lookupPrototypeChanges(DexMethod method) {
      return RewrittenPrototypeDescription.none();
    }

    @Override
    public DexField lookupField(DexField field) {
      return field;
    }

    @Override
    public boolean isContextFreeForMethods() {
      return true;
    }
  }

  /**
   * GraphLense implementation with a parent lense using a simple mapping for type, method and field
   * mapping.
   *
   * <p>Subclasses can override the lookup methods.
   *
   * <p>For method mapping where invocation type can change just override {@link
   * #mapInvocationType(DexMethod, DexMethod, Type)} if the default name mapping applies, and only
   * invocation type might need to change.
   */
  public static class NestedGraphLense extends GraphLense {

    protected final GraphLense previousLense;
    protected final DexItemFactory dexItemFactory;

    protected final Map<DexType, DexType> typeMap;
    private final Map<DexType, DexType> arrayTypeCache = new IdentityHashMap<>();
    protected final Map<DexMethod, DexMethod> methodMap;
    protected final Map<DexField, DexField> fieldMap;

    // Maps that store the original signature of fields and methods that have been affected, for
    // example, by vertical class merging. Needed to generate a correct Proguard map in the end.
    protected final BiMap<DexField, DexField> originalFieldSignatures;
    protected final BiMap<DexMethod, DexMethod> originalMethodSignatures;

    // Overrides this if the sub type needs to be a nested lense while it doesn't have any mappings
    // at all, e.g., publicizer lense that changes invocation type only.
    protected boolean isLegitimateToHaveEmptyMappings() {
      return false;
    }

    public NestedGraphLense(
        Map<DexType, DexType> typeMap,
        Map<DexMethod, DexMethod> methodMap,
        Map<DexField, DexField> fieldMap,
        BiMap<DexField, DexField> originalFieldSignatures,
        BiMap<DexMethod, DexMethod> originalMethodSignatures,
        GraphLense previousLense,
        DexItemFactory dexItemFactory) {
      assert !typeMap.isEmpty() || !methodMap.isEmpty() || !fieldMap.isEmpty()
          || isLegitimateToHaveEmptyMappings();
      this.typeMap = typeMap.isEmpty() ? null : typeMap;
      this.methodMap = methodMap;
      this.fieldMap = fieldMap;
      this.originalFieldSignatures = originalFieldSignatures;
      this.originalMethodSignatures = originalMethodSignatures;
      this.previousLense = previousLense;
      this.dexItemFactory = dexItemFactory;
    }

    @Override
    public DexType getOriginalType(DexType type) {
      return previousLense.getOriginalType(type);
    }

    @Override
    public DexField getOriginalFieldSignature(DexField field) {
      DexField originalField =
          originalFieldSignatures != null
              ? originalFieldSignatures.getOrDefault(field, field)
              : field;
      return previousLense.getOriginalFieldSignature(originalField);
    }

    @Override
    public DexMethod getOriginalMethodSignature(DexMethod method) {
      DexMethod originalMethod =
          originalMethodSignatures != null
              ? originalMethodSignatures.getOrDefault(method, method)
              : method;
      return previousLense.getOriginalMethodSignature(originalMethod);
    }

    @Override
    public DexField getRenamedFieldSignature(DexField originalField) {
      DexField renamedField = previousLense.getRenamedFieldSignature(originalField);
      return originalFieldSignatures != null
          ? originalFieldSignatures.inverse().getOrDefault(renamedField, renamedField)
          : renamedField;
    }

    @Override
    public DexMethod getRenamedMethodSignature(DexMethod originalMethod) {
      DexMethod renamedMethod = previousLense.getRenamedMethodSignature(originalMethod);
      return originalMethodSignatures != null
          ? originalMethodSignatures.inverse().getOrDefault(renamedMethod, renamedMethod)
          : renamedMethod;
    }

    @Override
    public DexType lookupType(DexType type) {
      if (type.isArrayType()) {
        synchronized (this) {
          // This block need to be synchronized due to arrayTypeCache.
          DexType result = arrayTypeCache.get(type);
          if (result == null) {
            DexType baseType = type.toBaseType(dexItemFactory);
            DexType newType = lookupType(baseType);
            if (baseType == newType) {
              result = type;
            } else {
              result = type.replaceBaseType(newType, dexItemFactory);
            }
            arrayTypeCache.put(type, result);
          }
          return result;
        }
      }
      DexType previous = previousLense.lookupType(type);
      return typeMap != null ? typeMap.getOrDefault(previous, previous) : previous;
    }

    @Override
    public GraphLenseLookupResult lookupMethod(DexMethod method, DexMethod context, Type type) {
      DexMethod previousContext =
          originalMethodSignatures != null
              ? originalMethodSignatures.getOrDefault(context, context)
              : context;
      GraphLenseLookupResult previous = previousLense.lookupMethod(method, previousContext, type);
      DexMethod newMethod = methodMap.get(previous.getMethod());
      if (newMethod == null) {
        return previous;
      }
      // TODO(sgjesse): Should we always do interface to virtual mapping? Is it a performance win
      // that only subclasses which are known to need it actually do it?
      return new GraphLenseLookupResult(
          newMethod, mapInvocationType(newMethod, method, previous.getType()));
    }

    @Override
    public RewrittenPrototypeDescription lookupPrototypeChanges(DexMethod method) {
      return previousLense.lookupPrototypeChanges(method);
    }

    @Override
    public DexMethod lookupGetFieldForMethod(DexField field, DexMethod context) {
      return previousLense.lookupGetFieldForMethod(field, context);
    }

    @Override
    public DexMethod lookupPutFieldForMethod(DexField field, DexMethod context) {
      return previousLense.lookupPutFieldForMethod(field, context);
    }

    /**
     * Default invocation type mapping.
     *
     * <p>This is an identity mapping. If a subclass need invocation type mapping either override
     * this method or {@link #lookupMethod(DexMethod, DexMethod, Type)}
     */
    protected Type mapInvocationType(DexMethod newMethod, DexMethod originalMethod, Type type) {
      return type;
    }

    /**
     * Standard mapping between interface and virtual invoke type.
     *
     * <p>Handle methods moved from interface to class or class to interface.
     */
    protected final Type mapVirtualInterfaceInvocationTypes(
        DexDefinitionSupplier definitions,
        DexMethod newMethod,
        DexMethod originalMethod,
        Type type) {
      if (type == Type.VIRTUAL || type == Type.INTERFACE) {
        // Get the invoke type of the actual definition.
        DexClass newTargetClass = definitions.definitionFor(newMethod.holder);
        if (newTargetClass == null) {
          return type;
        }
        DexClass originalTargetClass = definitions.definitionFor(originalMethod.holder);
        if (originalTargetClass != null
            && (originalTargetClass.isInterface() ^ (type == Type.INTERFACE))) {
          // The invoke was wrong to start with, so we keep it wrong. This is to ensure we get
          // the IncompatibleClassChangeError the original invoke would have triggered.
          return newTargetClass.accessFlags.isInterface() ? Type.VIRTUAL : Type.INTERFACE;
        }
        return newTargetClass.accessFlags.isInterface() ? Type.INTERFACE : Type.VIRTUAL;
      }
      return type;
    }

    @Override
    public Set<DexMethod> lookupMethodInAllContexts(DexMethod method) {
      Set<DexMethod> result = new HashSet<>();
      for (DexMethod previous : previousLense.lookupMethodInAllContexts(method)) {
        result.add(methodMap.getOrDefault(previous, previous));
      }
      return result;
    }

    @Override
    public DexField lookupField(DexField field) {
      DexField previous = previousLense.lookupField(field);
      return fieldMap.getOrDefault(previous, previous);
    }

    @Override
    public boolean isContextFreeForMethods() {
      return previousLense.isContextFreeForMethods();
    }

    @Override
    public boolean isContextFreeForMethod(DexMethod method) {
      return previousLense.isContextFreeForMethod(method);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (typeMap != null) {
        for (Map.Entry<DexType, DexType> entry : typeMap.entrySet()) {
          builder.append(entry.getKey().toSourceString()).append(" -> ");
          builder.append(entry.getValue().toSourceString()).append(System.lineSeparator());
        }
      }
      for (Map.Entry<DexMethod, DexMethod> entry : methodMap.entrySet()) {
        builder.append(entry.getKey().toSourceString()).append(" -> ");
        builder.append(entry.getValue().toSourceString()).append(System.lineSeparator());
      }
      for (Map.Entry<DexField, DexField> entry : fieldMap.entrySet()) {
        builder.append(entry.getKey().toSourceString()).append(" -> ");
        builder.append(entry.getValue().toSourceString()).append(System.lineSeparator());
      }
      builder.append(previousLense.toString());
      return builder.toString();
    }
  }
}
