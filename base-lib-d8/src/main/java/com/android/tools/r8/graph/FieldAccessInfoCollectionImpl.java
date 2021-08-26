// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import com.android.tools.r8.utils.SetUtils;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class FieldAccessInfoCollectionImpl
    implements FieldAccessInfoCollection<FieldAccessInfoImpl> {

  private Map<DexField, FieldAccessInfoImpl> infos = new IdentityHashMap<>();

  @Override
  public FieldAccessInfoImpl get(DexField field) {
    return infos.get(field);
  }

  public void extend(DexField field, FieldAccessInfoImpl info) {
    assert !infos.containsKey(field);
    infos.put(field, info);
  }

  @Override
  public void forEach(Consumer<FieldAccessInfoImpl> consumer) {
    // Verify that the mapping os one-to-one, otherwise the caller could receive duplicates.
    assert verifyMappingIsOneToOne();
    infos.values().forEach(consumer);
  }

  public void removeIf(BiPredicate<DexField, FieldAccessInfoImpl> predicate) {
    infos.entrySet().removeIf(entry -> predicate.test(entry.getKey(), entry.getValue()));
  }

  public FieldAccessInfoCollectionImpl rewrittenWithLens(
      DexDefinitionSupplier definitions, GraphLense lens) {
    FieldAccessInfoCollectionImpl collection = new FieldAccessInfoCollectionImpl();
    infos.forEach(
        (field, info) ->
            collection.infos.put(
                lens.lookupField(field), info.rewrittenWithLens(definitions, lens)));
    return collection;
  }

  // This is used to verify that the temporary mappings inserted into `infos` by the Enqueuer are
  // removed.
  public boolean verifyMappingIsOneToOne() {
    assert infos.values().size() == SetUtils.newIdentityHashSet(infos.values()).size();
    return true;
  }
}
