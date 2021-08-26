// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming.dexitembasedstring;

import static com.android.tools.r8.utils.DescriptorUtils.getCanonicalNameFromDescriptor;
import static com.android.tools.r8.utils.DescriptorUtils.getClassNameFromDescriptor;
import static com.android.tools.r8.utils.DescriptorUtils.getUnqualifiedClassNameFromDescriptor;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinitionSupplier;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.NamingLens;
import com.google.common.base.Strings;

public class ClassNameComputationInfo extends NameComputationInfo<DexType> {

  public enum ClassNameMapping {
    NONE,
    NAME, // getName()
    TYPE_NAME, // getTypeName()
    CANONICAL_NAME, // getCanonicalName()
    SIMPLE_NAME; // getSimpleName()

    boolean needsToComputeClassName() {
      return this != NONE;
    }

    boolean needsToRegisterTypeReference() {
      return this == SIMPLE_NAME;
    }

    public DexString map(String descriptor, DexClass holder, DexItemFactory dexItemFactory) {
      return map(descriptor, holder, dexItemFactory, 0);
    }

    public DexString map(
        String descriptor, DexClass holder, DexItemFactory dexItemFactory, int arrayDepth) {
      String name;
      switch (this) {
        case NAME:
          name = getClassNameFromDescriptor(descriptor);
          if (arrayDepth > 0) {
            name = Strings.repeat("[", arrayDepth) + "L" + name + ";";
          }
          break;

        case TYPE_NAME:
          // TODO(b/119426668): desugar Type#getTypeName
          throw new Unreachable("Type#getTypeName not supported yet");
          // name = getClassNameFromDescriptor(descriptor);
          // if (arrayDepth > 0) {
          //   name = name + Strings.repeat("[]", arrayDepth);
          // }
          // break;
        case CANONICAL_NAME:
          name = getCanonicalNameFromDescriptor(descriptor);
          if (arrayDepth > 0) {
            name = name + Strings.repeat("[]", arrayDepth);
          }
          break;

        case SIMPLE_NAME:
          assert holder != null;
          boolean renamed = !descriptor.equals(holder.type.toDescriptorString());
          boolean needsToRetrieveInnerName = holder.isMemberClass() || holder.isLocalClass();
          if (!renamed && needsToRetrieveInnerName) {
            name = holder.getInnerClassAttributeForThisClass().getInnerName().toString();
          } else {
            name = getUnqualifiedClassNameFromDescriptor(descriptor);
          }
          if (arrayDepth > 0) {
            name = name + Strings.repeat("[]", arrayDepth);
          }
          break;

        default:
          throw new Unreachable("Unexpected ClassNameMapping: " + this);
      }
      return dexItemFactory.createString(name);
    }
  }

  private static final ClassNameComputationInfo CANONICAL_NAME_INSTANCE =
      new ClassNameComputationInfo(ClassNameMapping.CANONICAL_NAME);

  private static final ClassNameComputationInfo NAME_INSTANCE =
      new ClassNameComputationInfo(ClassNameMapping.NAME);

  private static final ClassNameComputationInfo NONE_INSTANCE =
      new ClassNameComputationInfo(ClassNameMapping.NONE);

  private static final ClassNameComputationInfo SIMPLE_NAME_INSTANCE =
      new ClassNameComputationInfo(ClassNameMapping.SIMPLE_NAME);

  private static final ClassNameComputationInfo TYPE_NAME_INSTANCE =
      new ClassNameComputationInfo(ClassNameMapping.TYPE_NAME);

  private final int arrayDepth;
  private final ClassNameMapping mapping;

  private ClassNameComputationInfo(ClassNameMapping mapping) {
    this(mapping, 0);
  }

  private ClassNameComputationInfo(ClassNameMapping mapping, int arrayDepth) {
    this.mapping = mapping;
    this.arrayDepth = arrayDepth;
  }

  public static ClassNameComputationInfo create(ClassNameMapping mapping, int arrayDepth) {
    return arrayDepth > 0
        ? new ClassNameComputationInfo(mapping, arrayDepth)
        : getInstance(mapping);
  }

  public static ClassNameComputationInfo getInstance(ClassNameMapping mapping) {
    switch (mapping) {
      case CANONICAL_NAME:
        return CANONICAL_NAME_INSTANCE;
      case NAME:
        return NAME_INSTANCE;
      case NONE:
        return NONE_INSTANCE;
      case SIMPLE_NAME:
        return SIMPLE_NAME_INSTANCE;
      case TYPE_NAME:
        return TYPE_NAME_INSTANCE;
      default:
        throw new Unreachable("Unexpected ClassNameMapping: " + mapping);
    }
  }

  public static ClassNameComputationInfo none() {
    return NONE_INSTANCE;
  }

  @Override
  public boolean needsToComputeName() {
    return mapping.needsToComputeClassName();
  }

  @Override
  public boolean needsToRegisterReference() {
    return mapping.needsToRegisterTypeReference();
  }

  @Override
  public DexString internalComputeNameFor(
      DexType type, DexDefinitionSupplier definitions, NamingLens namingLens) {
    return mapping.map(
        namingLens.lookupDescriptor(type).toString(),
        definitions.definitionFor(type),
        definitions.dexItemFactory(),
        arrayDepth);
  }

  @Override
  public boolean isClassNameComputationInfo() {
    return true;
  }

  @Override
  public ClassNameComputationInfo asClassNameComputationInfo() {
    return this;
  }

  @Override
  public boolean equals(Object other) {
    if (getClass() != other.getClass()) {
      return false;
    }
    ClassNameComputationInfo otherInfo = (ClassNameComputationInfo) other;
    return this.arrayDepth == otherInfo.arrayDepth && this.mapping == otherInfo.mapping;
  }

  @Override
  public int hashCode() {
    return mapping.ordinal() * 31 + arrayDepth;
  }
}
