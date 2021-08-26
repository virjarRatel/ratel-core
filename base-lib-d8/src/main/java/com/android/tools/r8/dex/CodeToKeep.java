// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dex;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CodeToKeep {

  static CodeToKeep createCodeToKeep(InternalOptions options, NamingLens namingLens) {
    if ((!namingLens.hasPrefixRewritingLogic()
            && options.desugaredLibraryConfiguration.getEmulateLibraryInterface().isEmpty())
        || options.isDesugaredLibraryCompilation()) {
      return new NopCodeToKeep();
    }
    return new DesugaredLibraryCodeToKeep(namingLens, options);
  }

  abstract void recordMethod(DexMethod method);

  abstract void recordField(DexField field);

  abstract void recordClass(DexType type);

  abstract void recordClassAllAccesses(DexType type);

  abstract boolean isNop();

  abstract void generateKeepRules(InternalOptions options);

  public static class DesugaredLibraryCodeToKeep extends CodeToKeep {

    private static class KeepStruct {
      Set<DexField> fields = Sets.newConcurrentHashSet();
      Set<DexMethod> methods = Sets.newConcurrentHashSet();
      boolean all = false;
    }

    private final NamingLens namingLens;
    private final Set<DexType> emulatedInterfaces = Sets.newIdentityHashSet();
    private final Map<DexType, KeepStruct> toKeep = new ConcurrentHashMap<>();
    private final InternalOptions options;

    public DesugaredLibraryCodeToKeep(NamingLens namingLens, InternalOptions options) {
      emulatedInterfaces.addAll(
          options.desugaredLibraryConfiguration.getEmulateLibraryInterface().values());
      this.namingLens = namingLens;
      this.options = options;
    }

    private boolean shouldKeep(DexType type) {
      return namingLens.prefixRewrittenType(type) != null || emulatedInterfaces.contains(type);
    }

    @Override
    void recordMethod(DexMethod method) {
      if (shouldKeep(method.holder)) {
        keepClass(method.holder);
        toKeep.get(method.holder).methods.add(method);
      }
      if (shouldKeep(method.proto.returnType)) {
        keepClass(method.proto.returnType);
      }
      for (DexType type : method.proto.parameters.values) {
        if (shouldKeep(type)) {
          keepClass(type);
        }
      }
    }

    @Override
    void recordField(DexField field) {
      if (shouldKeep(field.holder)) {
        keepClass(field.holder);
        toKeep.get(field.holder).fields.add(field);
      }
      if (shouldKeep(field.type)) {
        keepClass(field.type);
      }
    }

    @Override
    void recordClass(DexType type) {
      if (shouldKeep(type)) {
        keepClass(type);
      }
    }

    @Override
    void recordClassAllAccesses(DexType type) {
      if (shouldKeep(type)) {
        keepClass(type);
        toKeep.get(type).all = true;
      }
    }

    private void keepClass(DexType type) {
      DexType baseType = type.lookupBaseType(options.itemFactory);
      toKeep.putIfAbsent(baseType, new KeepStruct());
    }

    @Override
    boolean isNop() {
      return false;
    }

    private String convertType(DexType type) {
      DexString rewriteType = namingLens.prefixRewrittenType(type);
      DexString descriptor = rewriteType != null ? rewriteType : type.descriptor;
      return DescriptorUtils.descriptorToJavaType(descriptor.toString());
    }

    @Override
    void generateKeepRules(InternalOptions options) {
      // TODO(b/134734081): Stream the consumer instead of building the String.
      StringBuilder sb = new StringBuilder();
      String cr = System.lineSeparator();
      for (DexType type : toKeep.keySet()) {
        KeepStruct keepStruct = toKeep.get(type);
        sb.append("-keep class ").append(convertType(type));
        if (keepStruct.all) {
          sb.append(" { *; }").append(cr);
          continue;
        }
        if (keepStruct.fields.isEmpty() && keepStruct.methods.isEmpty()) {
          sb.append(cr);
          continue;
        }
        sb.append(" {").append(cr);
        for (DexField field : keepStruct.fields) {
          sb.append("    ")
              .append(convertType(field.type))
              .append(" ")
              .append(field.name)
              .append(";")
              .append(cr);
        }
        for (DexMethod method : keepStruct.methods) {
          sb.append("    ")
              .append(convertType(method.proto.returnType))
              .append(" ")
              .append(method.name)
              .append("(");
          for (int i = 0; i < method.getArity(); i++) {
            if (i != 0) {
              sb.append(", ");
            }
            sb.append(convertType(method.proto.parameters.values[i]));
          }
          sb.append(");").append(cr);
        }
        sb.append("}").append(cr);
      }
      options.desugaredLibraryKeepRuleConsumer.accept(sb.toString(), options.reporter);
      options.desugaredLibraryKeepRuleConsumer.finished(options.reporter);
    }
  }

  public static class NopCodeToKeep extends CodeToKeep {

    @Override
    void recordMethod(DexMethod method) {}

    @Override
    void recordField(DexField field) {}

    @Override
    void recordClass(DexType type) {}

    @Override
    void recordClassAllAccesses(DexType type) {}

    @Override
    boolean isNop() {
      return true;
    }

    @Override
    void generateKeepRules(InternalOptions options) {
      throw new Unreachable("Has no keep rules to generate");
    }
  }
}
