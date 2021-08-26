// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PrefixRewritingMapper {

  public static PrefixRewritingMapper empty() {
    return new EmptyPrefixRewritingMapper();
  }

  public abstract DexType rewrittenType(DexType type);

  public abstract void rewriteType(DexType type, DexType rewrittenType);

  public boolean hasRewrittenType(DexType type) {
    return rewrittenType(type) != null;
  }

  public boolean hasRewrittenTypeInSignature(DexProto proto) {
    if (hasRewrittenType(proto.returnType)) {
      return true;
    }
    for (DexType paramType : proto.parameters.values) {
      if (hasRewrittenType(paramType)) {
        return true;
      }
    }
    return false;
  }

  public abstract boolean isRewriting();

  public static class DesugarPrefixRewritingMapper extends PrefixRewritingMapper {

    private final Set<DexType> notRewritten = Sets.newConcurrentHashSet();
    private final Map<DexType, DexType> rewritten = new ConcurrentHashMap<>();
    private final Map<DexString, DexString> initialPrefixes;
    private final DexItemFactory factory;

    public DesugarPrefixRewritingMapper(Map<String, String> prefixes, DexItemFactory factory) {
      this.factory = factory;
      ImmutableMap.Builder<DexString, DexString> builder = ImmutableMap.builder();
      for (String key : prefixes.keySet()) {
        builder.put(toDescriptorPrefix(key), toDescriptorPrefix(prefixes.get(key)));
      }
      this.initialPrefixes = builder.build();
      validatePrefixes(prefixes);
    }

    private DexString toDescriptorPrefix(String prefix) {
      return factory.createString("L" + DescriptorUtils.getBinaryNameFromJavaType(prefix));
    }

    private void validatePrefixes(Map<String, String> initialPrefixes) {
      String[] prefixes = initialPrefixes.keySet().toArray(new String[0]);
      for (int i = 0; i < prefixes.length; i++) {
        for (int j = i + 1; j < prefixes.length; j++) {
          String small, large;
          if (prefixes[i].length() < prefixes[j].length()) {
            small = prefixes[i];
            large = prefixes[j];
          } else {
            small = prefixes[j];
            large = prefixes[i];
          }
          if (large.startsWith(small)) {
            throw new CompilationError(
                "Inconsistent prefix in desugared library:"
                    + " Should a class starting with "
                    + small
                    + " be rewritten using "
                    + small
                    + " -> "
                    + initialPrefixes.get(small)
                    + " or using "
                    + large
                    + " - > "
                    + initialPrefixes.get(large)
                    + " ?");
          }
        }
      }
    }

    @Override
    public DexType rewrittenType(DexType type) {
      if (notRewritten.contains(type)) {
        return null;
      }
      if (rewritten.containsKey(type)) {
        return rewritten.get(type);
      }
      return computePrefix(type);
    }

    @Override
    public void rewriteType(DexType type, DexType rewrittenType) {
      assert !notRewritten.contains(type)
          : "New rewriting rule for "
              + type
              + " but the compiler has already made decisions based on the fact that this type was"
              + " not rewritten";
      assert !rewritten.containsKey(type) || rewritten.get(type) == rewrittenType
          : "New rewriting rule for "
              + type
              + " but the compiler has already made decisions based on a different rewriting rule"
              + " for this type";
      rewritten.put(type, rewrittenType);
    }

    private DexType computePrefix(DexType type) {
      DexString prefixToMatch = type.descriptor.withoutArray(factory);
      DexType result = lookup(type, prefixToMatch, initialPrefixes);
      if (result != null) {
        return result;
      }
      notRewritten.add(type);
      return null;
    }

    private DexType lookup(DexType type, DexString prefixToMatch, Map<DexString, DexString> map) {
      // TODO: We could use tries instead of looking-up everywhere.
      for (DexString prefix : map.keySet()) {
        if (prefixToMatch.startsWith(prefix)) {
          DexString rewrittenTypeDescriptor =
              type.descriptor.withNewPrefix(prefix, map.get(prefix), factory);
          DexType rewrittenType = factory.createType(rewrittenTypeDescriptor);
          rewriteType(type, rewrittenType);
          return rewrittenType;
        }
      }
      return null;
    }

    @Override
    public boolean isRewriting() {
      return true;
    }
  }

  public static class EmptyPrefixRewritingMapper extends PrefixRewritingMapper {

    @Override
    public DexType rewrittenType(DexType type) {
      return null;
    }

    @Override
    public void rewriteType(DexType type, DexType rewrittenType) {}

    @Override
    public boolean isRewriting() {
      return false;
    }
  }
}
